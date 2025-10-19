package com.womtech.repository;

import com.womtech.entity.Inventory;
import com.womtech.entity.Product;
import com.womtech.entity.Location;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, String> {
    Optional<Inventory> findByProductAndLocation(Product product, Location location);
    List<Inventory> findByProduct(Product product);
    List<Inventory> findByLocation(Location location);
    List<Inventory> findByStatus(Integer status);
    
    @Query("SELECT i FROM Inventory i WHERE i.quantity <= 10 AND i.quantity > 0")
    List<Inventory> findLowStockItems();
    
    @Query("SELECT i FROM Inventory i WHERE i.quantity <= 10 AND i.quantity > 0")
    Page<Inventory> findLowStockItems(Pageable pageable);
    
    @Query("SELECT i FROM Inventory i WHERE i.quantity <= 0")
    List<Inventory> findOutOfStockItems();
    
    @Query("SELECT i FROM Inventory i WHERE i.quantity <= 0")
    Page<Inventory> findOutOfStockItems(Pageable pageable);
    
}
