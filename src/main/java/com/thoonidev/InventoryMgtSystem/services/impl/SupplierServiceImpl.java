package com.thoonidev.InventoryMgtSystem.services.impl;

import com.thoonidev.InventoryMgtSystem.dtos.Response;
import com.thoonidev.InventoryMgtSystem.dtos.SupplierDTO;
import com.thoonidev.InventoryMgtSystem.exceptions.NotFoundException;
import com.thoonidev.InventoryMgtSystem.models.Supplier;
import com.thoonidev.InventoryMgtSystem.repositories.SupplierRepository;
import com.thoonidev.InventoryMgtSystem.services.SupplierService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SupplierServiceImpl implements SupplierService {

    private final SupplierRepository supplierRepository;
    private final ModelMapper modelMapper;
    private final MessageSource messageSource;

    @Override
    public Response addSupplier(SupplierDTO supplierDTO) {
        Supplier supplierToSave = modelMapper.map(supplierDTO, Supplier.class);
        supplierRepository.save(supplierToSave);

        return Response.builder()
                .status(200)
                .message(getMessage("supplier.saved"))
                .build();
    }

    @Override
    public Response updateSupplier(Long id, SupplierDTO supplierDTO) {
        Supplier existingSupplier = supplierRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(getMessage("supplier.not_found")));

        if (supplierDTO.getName() != null) existingSupplier.setName(supplierDTO.getName());
        if (supplierDTO.getContactInfo() != null) existingSupplier.setContactInfo(supplierDTO.getContactInfo());
        if (supplierDTO.getAddress() != null) existingSupplier.setAddress(supplierDTO.getAddress());

        supplierRepository.save(existingSupplier);

        return Response.builder()
                .status(200)
                .message(getMessage("supplier.updated"))
                .build();
    }

    @Override
    public Response getAllSupplier() {
        List<Supplier> suppliers = supplierRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
        List<SupplierDTO> supplierDTOList = modelMapper.map(suppliers, new TypeToken<List<SupplierDTO>>() {}.getType());

        return Response.builder()
                .status(200)
                .message(getMessage("supplier.list"))
                .suppliers(supplierDTOList)
                .build();
    }

    @Override
    public Response getSupplierById(Long id) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(getMessage("supplier.not_found")));

        SupplierDTO supplierDTO = modelMapper.map(supplier, SupplierDTO.class);

        return Response.builder()
                .status(200)
                .message(getMessage("supplier.found"))
                .supplier(supplierDTO)
                .build();
    }

    @Override
    public Response deleteSupplier(Long id) {
        supplierRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(getMessage("supplier.not_found")));

        supplierRepository.deleteById(id);

        return Response.builder()
                .status(200)
                .message(getMessage("supplier.deleted"))
                .build();
    }

    // Helper method to get messages by location
    private String getMessage(String key) {
        return messageSource.getMessage(key, null, LocaleContextHolder.getLocale());
    }
}
