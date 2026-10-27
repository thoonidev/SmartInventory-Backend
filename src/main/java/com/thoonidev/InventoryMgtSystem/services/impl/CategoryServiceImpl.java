package com.thoonidev.InventoryMgtSystem.services.impl;

import com.thoonidev.InventoryMgtSystem.dtos.CategoryDTO;
import com.thoonidev.InventoryMgtSystem.dtos.Response;
import com.thoonidev.InventoryMgtSystem.exceptions.NotFoundException;
import com.thoonidev.InventoryMgtSystem.models.Category;
import com.thoonidev.InventoryMgtSystem.repositories.CategoryRepository;
import com.thoonidev.InventoryMgtSystem.services.CategoryService;
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
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final ModelMapper modelMapper;
    private final MessageSource messageSource;

    @Override
    public Response createCategory(CategoryDTO categoryDTO) {
        Category categoryToSave = modelMapper.map(categoryDTO, Category.class);
        categoryRepository.save(categoryToSave);

        String message = messageSource.getMessage("category.saved", null, LocaleContextHolder.getLocale());

        return Response.builder()
                .status(200)
                .message(message)
                .build();
    }

    @Override
    public Response getAllCategories() {
        List<Category> categories = categoryRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
        categories.forEach(category -> category.setProducts(null));

        List<CategoryDTO> categoryDTOList = modelMapper.map(categories, new TypeToken<List<CategoryDTO>>() {
        }.getType());

        String message = messageSource.getMessage("category.list", null, LocaleContextHolder.getLocale());

        return Response.builder()
                .status(200)
                .message(message)
                .categories(categoryDTOList)
                .build();
    }

    @Override
    public Response getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        messageSource.getMessage("category.not_found", null, LocaleContextHolder.getLocale())
                ));

        CategoryDTO categoryDTO = modelMapper.map(category, CategoryDTO.class);

        String message = messageSource.getMessage("category.found", null, LocaleContextHolder.getLocale());

        return Response.builder()
                .status(200)
                .message(message)
                .category(categoryDTO)
                .build();
    }

    @Override
    public Response updateCategory(Long id, CategoryDTO categoryDTO) {
        Category existingCategory = categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        messageSource.getMessage("category.not_found", null, LocaleContextHolder.getLocale())
                ));

        existingCategory.setName(categoryDTO.getName());
        categoryRepository.save(existingCategory);

        String message = messageSource.getMessage("category.updated", null, LocaleContextHolder.getLocale());

        return Response.builder()
                .status(200)
                .message(message)
                .build();
    }

    @Override
    public Response deleteCategory(Long id) {
        categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        messageSource.getMessage("category.not_found", null, LocaleContextHolder.getLocale())
                ));

        categoryRepository.deleteById(id);

        String message = messageSource.getMessage("category.deleted", null, LocaleContextHolder.getLocale());

        return Response.builder()
                .status(200)
                .message(message)
                .build();
    }
}
