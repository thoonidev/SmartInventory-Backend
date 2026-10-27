package com.thoonidev.InventoryMgtSystem.services.impl;

import com.thoonidev.InventoryMgtSystem.dtos.Response;
import com.thoonidev.InventoryMgtSystem.dtos.TransactionDTO;
import com.thoonidev.InventoryMgtSystem.dtos.TransactionRequest;
import com.thoonidev.InventoryMgtSystem.enums.TransactionStatus;
import com.thoonidev.InventoryMgtSystem.enums.TransactionType;
import com.thoonidev.InventoryMgtSystem.exceptions.NameValueRequiredException;
import com.thoonidev.InventoryMgtSystem.exceptions.NotFoundException;
import com.thoonidev.InventoryMgtSystem.models.Product;
import com.thoonidev.InventoryMgtSystem.models.Supplier;
import com.thoonidev.InventoryMgtSystem.models.Transaction;
import com.thoonidev.InventoryMgtSystem.models.User;
import com.thoonidev.InventoryMgtSystem.repositories.ProductRepository;
import com.thoonidev.InventoryMgtSystem.repositories.SupplierRepository;
import com.thoonidev.InventoryMgtSystem.repositories.TransactionRepository;
import com.thoonidev.InventoryMgtSystem.services.TransactionService;
import com.thoonidev.InventoryMgtSystem.services.UserService;
import com.thoonidev.InventoryMgtSystem.specification.TransactionFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final ProductRepository productRepository;
    private final SupplierRepository supplierRepository;
    private final UserService userService;
    private final ModelMapper modelMapper;
    private final MessageSource messageSource;

    @Override
    public Response purchase(TransactionRequest transactionRequest) {
        Long productId = transactionRequest.getProductId();
        Long supplierId = transactionRequest.getSupplierId();
        Integer quantity = transactionRequest.getQuantity();

        if (supplierId == null) throw new NameValueRequiredException(getMessage("supplier.id.required"));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException(getMessage("product.not_found")));

        Supplier supplier = supplierRepository.findById(supplierId)
                .orElseThrow(() -> new NotFoundException(getMessage("supplier.not_found")));

        User user = userService.getCurrentLoggedInUser();

        product.setStockQuantity(product.getStockQuantity() + quantity);
        productRepository.save(product);

        Transaction transaction = Transaction.builder()
                .transactionType(TransactionType.PURCHASE)
                .status(TransactionStatus.COMPLETED)
                .product(product)
                .user(user)
                .supplier(supplier)
                .totalProducts(quantity)
                .totalPrice(product.getPrice().multiply(BigDecimal.valueOf(quantity)))
                .description(transactionRequest.getDescription())
                .note(transactionRequest.getNote())
                .build();

        transactionRepository.save(transaction);

        return Response.builder()
                .status(200)
                .message(getMessage("transaction.purchase.success"))
                .build();
    }

    @Override
    public Response sell(TransactionRequest transactionRequest) {
        Long productId = transactionRequest.getProductId();
        Integer quantity = transactionRequest.getQuantity();

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException(getMessage("product.not_found")));

        User user = userService.getCurrentLoggedInUser();

        product.setStockQuantity(product.getStockQuantity() - quantity);
        productRepository.save(product);

        Transaction transaction = Transaction.builder()
                .transactionType(TransactionType.SALE)
                .status(TransactionStatus.COMPLETED)
                .product(product)
                .user(user)
                .totalProducts(quantity)
                .totalPrice(product.getPrice().multiply(BigDecimal.valueOf(quantity)))
                .description(transactionRequest.getDescription())
                .note(transactionRequest.getNote())
                .build();

        transactionRepository.save(transaction);

        return Response.builder()
                .status(200)
                .message(getMessage("transaction.sale.success"))
                .build();
    }

    @Override
    public Response returnToSupplier(TransactionRequest transactionRequest) {
        Long productId = transactionRequest.getProductId();
        Long supplierId = transactionRequest.getSupplierId();
        Integer quantity = transactionRequest.getQuantity();

        if (supplierId == null) throw new NameValueRequiredException(getMessage("supplier.id.required"));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException(getMessage("product.not_found")));

        Supplier supplier = supplierRepository.findById(supplierId)
                .orElseThrow(() -> new NotFoundException(getMessage("supplier.not_found")));

        User user = userService.getCurrentLoggedInUser();

        product.setStockQuantity(product.getStockQuantity() - quantity);
        productRepository.save(product);

        Transaction transaction = Transaction.builder()
                .transactionType(TransactionType.RETURN_TO_SUPPLIER)
                .status(TransactionStatus.PROCESSING)
                .product(product)
                .user(user)
                .supplier(supplier)
                .totalProducts(quantity)
                .totalPrice(BigDecimal.ZERO)
                .description(transactionRequest.getDescription())
                .note(transactionRequest.getNote())
                .build();

        transactionRepository.save(transaction);

        return Response.builder()
                .status(200)
                .message(getMessage("transaction.return.success"))
                .build();
    }

    @Override
    public Response getAllTransactions(int page, int size, String filter) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        Specification<Transaction> spec = TransactionFilter.byFilter(filter);

        Page<Transaction> transactionPage = transactionRepository.findAll(spec, pageable);

        List<TransactionDTO> transactionDTOS = modelMapper.map(transactionPage.getContent(), new TypeToken<List<TransactionDTO>>() {}.getType());

        transactionDTOS.forEach(transactionDTO -> {
            transactionDTO.setUser(null);
            transactionDTO.setProduct(null);
            transactionDTO.setSupplier(null);
        });

        return Response.builder()
                .status(200)
                .message(getMessage("transaction.list.success"))
                .transactions(transactionDTOS)
                .totalElements(transactionPage.getTotalElements())
                .totalPages(transactionPage.getTotalPages())
                .build();
    }

    @Override
    public Response getAllTransactionById(Long id) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(getMessage("transaction.not_found")));

        TransactionDTO transactionDTO = modelMapper.map(transaction, TransactionDTO.class);
        transactionDTO.getUser().setTransactions(null);

        return Response.builder()
                .status(200)
                .message(getMessage("transaction.detail.success"))
                .transaction(transactionDTO)
                .build();
    }

    @Override
    public Response getAllTransactionByMonthAndYear(int month, int year) {
        List<Transaction> transactions = transactionRepository.findAll(TransactionFilter.byMonthAndYear(month, year));

        List<TransactionDTO> transactionDTOS = modelMapper.map(transactions, new TypeToken<List<TransactionDTO>>() {}.getType());

        transactionDTOS.forEach(transactionDTO -> {
            transactionDTO.setUser(null);
            transactionDTO.setProduct(null);
            transactionDTO.setSupplier(null);
        });

        return Response.builder()
                .status(200)
                .message(getMessage("transaction.list.success"))
                .transactions(transactionDTOS)
                .build();
    }

    @Override
    public Response updateTransactionStatus(Long transactionId, TransactionStatus status) {
        Transaction existingTransaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new NotFoundException(getMessage("transaction.not_found")));

        existingTransaction.setStatus(status);
        existingTransaction.setUpdateAt(LocalDateTime.now());

        transactionRepository.save(existingTransaction);

        return Response.builder()
                .status(200)
                .message(getMessage("transaction.status.updated"))
                .build();
    }

    private String getMessage(String key) {
        return messageSource.getMessage(key, null, LocaleContextHolder.getLocale());
    }
}
