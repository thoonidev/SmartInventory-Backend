package com.thoonidev.InventoryMgtSystem.repositories;

import com.thoonidev.InventoryMgtSystem.models.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SupplierRepository extends JpaRepository<Supplier, Long> {
}
