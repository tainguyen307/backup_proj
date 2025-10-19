package com.womtech.service;

import com.womtech.entity.Inventory;
import com.womtech.entity.Product;
import com.womtech.entity.Location;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface InventoryService {

    List<Inventory> getAllInventory();
   
    Page<Inventory> getAllInventory(Pageable pageable);

    Optional<Inventory> getInventoryByID(String id);

    Optional<Inventory> getInventoryByProductAndLocation(Product product, Location location);

    List<Inventory> getLowStockItems();
    
    Page<Inventory> getLowStockItems(Pageable pageable);


    List<Inventory> getOutOfStockItems();
    Page<Inventory> getOutOfStockItems(Pageable pageable);


    List<Inventory> getInventoryByStatus(Integer status);

    Inventory saveInventory(Inventory inventory);

    Inventory updateStock(String inventoryId, Integer quantity);

    Inventory restockInventory(String inventoryId, Integer quantity);

    void deleteInventory(String id);

    long getTotalCount();

    long getLowStockCount();

    long getOutOfStockCount();

    // Location methods
    List<Location> getAllLocations();

    List<Location> getActiveLocations();
}
