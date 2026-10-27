package com.thoonidev.InventoryMgtSystem.repositories;

import com.thoonidev.InventoryMgtSystem.models.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}
