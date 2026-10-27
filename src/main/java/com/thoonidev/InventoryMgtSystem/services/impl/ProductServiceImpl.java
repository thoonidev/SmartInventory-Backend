package com.thoonidev.InventoryMgtSystem.services.impl;

import com.thoonidev.InventoryMgtSystem.dtos.ProductDTO;
import com.thoonidev.InventoryMgtSystem.dtos.Response;
import com.thoonidev.InventoryMgtSystem.exceptions.NotFoundException;
import com.thoonidev.InventoryMgtSystem.models.Category;
import com.thoonidev.InventoryMgtSystem.models.Product;
import com.thoonidev.InventoryMgtSystem.repositories.CategoryRepository;
import com.thoonidev.InventoryMgtSystem.repositories.ProductRepository;
import com.thoonidev.InventoryMgtSystem.services.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ModelMapper modelMapper;
    private final CategoryRepository categoryRepository;
    private final MessageSource messageSource;

    private static final String IMAGE_DIRECTORY = System.getProperty("user.dir") + "/product-images/";
    private static final String IMAGE_DIRECTORY_2 = "\\";

    @Override
    public Response saveProduct(ProductDTO productDTO, MultipartFile imageFile) {
        Category category = categoryRepository.findById(productDTO.getCategoryId())
                .orElseThrow(() -> new NotFoundException(getMessage("category.not_found")));

        Product productToSave = Product.builder()
                .name(productDTO.getName())
                .sku(productDTO.getSku())
                .price(productDTO.getPrice())
                .stockQuantity(productDTO.getStockQuantity())
                .description(productDTO.getDescription())
                .category(category)
                .build();

        if (imageFile != null && !imageFile.isEmpty()) {
            String imagePath = saveImage2(imageFile);
            productToSave.setImageUrl(imagePath);
        }

        productRepository.save(productToSave);

        return Response.builder()
                .status(200)
                .message(getMessage("product.saved"))
                .build();
    }

    @Override
    public Response updateProduct(ProductDTO productDTO, MultipartFile imageFile) {
        Product existingProduct = productRepository.findById(productDTO.getProductId())
                .orElseThrow(() -> new NotFoundException(getMessage("product.not_found")));

        if (imageFile != null && !imageFile.isEmpty()) {
            String imagePath = saveImage2(imageFile);
            existingProduct.setImageUrl(imagePath);
        }

        if (productDTO.getCategoryId() != null && productDTO.getCategoryId() > 0) {
            Category category = categoryRepository.findById(productDTO.getCategoryId())
                    .orElseThrow(() -> new NotFoundException(getMessage("category.not_found")));
            existingProduct.setCategory(category);
        }

        if (productDTO.getName() != null && !productDTO.getName().isBlank()) {
            existingProduct.setName(productDTO.getName());
        }
        if (productDTO.getSku() != null && !productDTO.getSku().isBlank()) {
            existingProduct.setSku(productDTO.getSku());
        }
        if (productDTO.getDescription() != null && !productDTO.getDescription().isBlank()) {
            existingProduct.setDescription(productDTO.getDescription());
        }
        if (productDTO.getPrice() != null && productDTO.getPrice().compareTo(BigDecimal.ZERO) >= 0) {
            existingProduct.setPrice(productDTO.getPrice());
        }
        if (productDTO.getStockQuantity() != null && productDTO.getStockQuantity() >= 0) {
            existingProduct.setStockQuantity(productDTO.getStockQuantity());
        }

        productRepository.save(existingProduct);

        return Response.builder()
                .status(200)
                .message(getMessage("product.updated"))
                .build();
    }

    @Override
    public Response getAllProducts() {
        List<Product> productList = productRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
        List<ProductDTO> productDTOList = modelMapper.map(productList, new TypeToken<List<ProductDTO>>() {
        }.getType());

        return Response.builder()
                .status(200)
                .message(getMessage("product.list"))
                .products(productDTOList)
                .build();
    }

    @Override
    public Response getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(getMessage("product.not_found")));

        return Response.builder()
                .status(200)
                .message(getMessage("product.found"))
                .product(modelMapper.map(product, ProductDTO.class))
                .build();
    }

    @Override
    public Response deleteProduct(Long id) {
        productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(getMessage("product.not_found")));

        productRepository.deleteById(id);

        return Response.builder()
                .status(200)
                .message(getMessage("product.deleted"))
                .build();
    }

    @Override
    public Response searchProduct(String input) {
        List<Product> products = productRepository.findByNameContainingOrDescriptionContaining(input, input);

        if (products.isEmpty()) {
            throw new NotFoundException(getMessage("product.search.empty"));
        }

        List<ProductDTO> productDTOList = modelMapper.map(products, new TypeToken<List<ProductDTO>>() {}.getType());

        return Response.builder()
                .status(200)
                .message(getMessage("product.search.success"))
                .products(productDTOList)
                .build();
    }

    private String saveImage(MultipartFile imageFile) {
        if (!imageFile.getContentType().startsWith("image/") || imageFile.getSize() > 1024 * 1024 * 1024) {
            throw new IllegalArgumentException(getMessage("image.invalid"));
        }

        File directory = new File(IMAGE_DIRECTORY);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        String uniqueFileName = UUID.randomUUID() + "_" + imageFile.getOriginalFilename();
        String imagePath = IMAGE_DIRECTORY + uniqueFileName;

        try {
            File destinationFile = new File(imagePath);
            imageFile.transferTo(destinationFile);
        } catch (Exception e) {
            throw new IllegalArgumentException(getMessage("image.save.error", new Object[]{e.getMessage()}));
        }

        return imagePath;
    }

    private String saveImage2(MultipartFile imageFile) {
        if (!imageFile.getContentType().startsWith("image/") || imageFile.getSize() > 1024 * 1024 * 1024) {
            throw new IllegalArgumentException(getMessage("image.invalid"));
        }

        File directory = new File(IMAGE_DIRECTORY_2);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        String uniqueFileName = UUID.randomUUID() + "_" + imageFile.getOriginalFilename();
        String imagePath = IMAGE_DIRECTORY_2 + uniqueFileName;

        try {
            File destinationFile = new File(imagePath);
            imageFile.transferTo(destinationFile);
        } catch (Exception e) {
            throw new IllegalArgumentException(getMessage("image.save.error", new Object[]{e.getMessage()}));
        }

        return "products/" + uniqueFileName;
    }

    private String getMessage(String key) {
        return messageSource.getMessage(key, null, LocaleContextHolder.getLocale());
    }

    private String getMessage(String key, Object[] args) {
        return messageSource.getMessage(key, args, LocaleContextHolder.getLocale());
    }
}
