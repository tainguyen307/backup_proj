package com.womtech.service.impl;

import com.womtech.entity.Inventory;
import com.womtech.entity.Product;
import com.womtech.entity.Location;
import com.womtech.repository.InventoryRepository;
import com.womtech.repository.LocationRepository;
import com.womtech.service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class InventoryServiceImpl implements InventoryService {

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private LocationRepository locationRepository;

    @Override
    public List<Inventory> getAllInventory() {
        return inventoryRepository.findAll();
    }
    
    public Page<Inventory> getAllInventory(Pageable pageable) {
        return inventoryRepository.findAll(pageable);
    }

    @Override
    public Optional<Inventory> getInventoryByID(String id) {
        return inventoryRepository.findById(id);
    }

    @Override
    public Optional<Inventory> getInventoryByProductAndLocation(Product product, Location location) {
        return inventoryRepository.findByProductAndLocation(product, location);
    }

    @Override
    public List<Inventory> getLowStockItems() {
        return inventoryRepository.findLowStockItems();
    }
    
    @Override
    public Page<Inventory> getLowStockItems(Pageable pageable) {
        return inventoryRepository.findLowStockItems(pageable);
    }
    @Override
    public List<Inventory> getOutOfStockItems() {
        return inventoryRepository.findOutOfStockItems();
    }
    @Override
    public Page<Inventory> getOutOfStockItems(Pageable pageable) {
        return inventoryRepository.findOutOfStockItems(pageable);
    }

    @Override
    public List<Inventory> getInventoryByStatus(Integer status) {
        return inventoryRepository.findByStatus(status);
    }

    @Override
    public Inventory saveInventory(Inventory inventory) {
        // Nếu không có ID hoặc ID rỗng → thêm mới
        if (inventory.getInventoryID() == null || inventory.getInventoryID().trim().isEmpty()) {
            return inventoryRepository.save(inventory);
        }

        // Nếu có ID → cập nhật
        Optional<Inventory> existingOpt = inventoryRepository.findById(inventory.getInventoryID());
        if (existingOpt.isPresent()) {
            Inventory existing = existingOpt.get();
            existing.setQuantity(inventory.getQuantity());
            existing.setStatus(inventory.getStatus());
            existing.setLocation(inventory.getLocation());
            existing.setProduct(inventory.getProduct());
            // Có thể set thêm updateAt nếu bạn có field này tự động update
            return inventoryRepository.save(existing);
        } else {
            throw new RuntimeException("Không tìm thấy tồn kho cần cập nhật");
        }
    }


    @Override
    public Inventory updateStock(String inventoryId, Integer quantity) {
        Optional<Inventory> optionalInventory = inventoryRepository.findById(inventoryId);
        if (optionalInventory.isPresent()) {
            Inventory inventory = optionalInventory.get();
            inventory.setQuantity(quantity);
            return inventoryRepository.save(inventory);
        }
        throw new RuntimeException("Inventory not found with id: " + inventoryId);
    }

    @Override
    public Inventory restockInventory(String inventoryId, Integer quantity) {
        Optional<Inventory> optionalInventory = inventoryRepository.findById(inventoryId);
        if (optionalInventory.isPresent()) {
            Inventory inventory = optionalInventory.get();
            inventory.setQuantity(inventory.getQuantity() + quantity);
            return inventoryRepository.save(inventory);
        }
        throw new RuntimeException("Inventory not found with id: " + inventoryId);
    }

    @Override
    public void deleteInventory(String id) {
        inventoryRepository.deleteById(id);
    }

    @Override
    public long getTotalCount() {
        return inventoryRepository.count();
    }

    @Override
    public long getLowStockCount() {
        return inventoryRepository.findLowStockItems().size();
    }

    @Override
    public long getOutOfStockCount() {
        return inventoryRepository.findOutOfStockItems().size();
    }

    @Override
    public List<Location> getAllLocations() {
        return locationRepository.findAll();
    }

    @Override
    public List<Location> getActiveLocations() {
        return locationRepository.findByStatus(1);
    }
}
