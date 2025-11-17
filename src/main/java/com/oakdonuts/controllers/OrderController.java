package com.oakdonuts.controllers;

import com.oakdonuts.database.DatabaseManager;
import com.oakdonuts.models.MenuItem;
import com.oakdonuts.models.Order;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class OrderController {
    private DatabaseManager dbManager;
    private List<MenuItem> currentCart;

    public OrderController(DatabaseManager dbManager) {
        this.dbManager = dbManager;
        this.currentCart = new ArrayList<>();
    }

    // MENU ITEMS operations
    public List<MenuItem> getMenuItems() {
        return dbManager.getAllMenuItems();
    }

    public List<MenuItem> getMenuItemsByCategory(String category) {
        List<MenuItem> allItems = dbManager.getAllMenuItems();
        List<MenuItem> filtered = new ArrayList<>();
        for (MenuItem item : allItems) {
            if (item.getCategory().equals(category)) {
                filtered.add(item);
            }
        }
        return filtered;
    }

    public MenuItem getMenuItemById(int id) {
        return dbManager.getMenuItemById(id);
    }

    public int addMenuItem(MenuItem item) {
        return dbManager.addMenuItem(item);
    }

    public boolean updateMenuItem(MenuItem item) {
        return dbManager.updateMenuItem(item);
    }

    public boolean deleteMenuItem(int id) {
        return dbManager.deleteMenuItem(id);
    }

    // CART operations
    public void addToCart(MenuItem item) {
        currentCart.add(item);
    }

    public void removeFromCart(MenuItem item) {
        currentCart.remove(item);
    }

    public List<MenuItem> getCart() {
        return currentCart;
    }

    public double getCartTotal() {
        double total = 0;
        for (MenuItem item : currentCart) {
            total += item.getPrice();
        }
        return total;
    }

    public void clearCart() {
        currentCart.clear();
    }

    // ORDER operations
    public String createOrder() {
        if (currentCart.isEmpty()) {
            return null;
        }

        String transactionId = generateTransactionId();
        Order order = new Order(
            transactionId,
            LocalDateTime.now(),
            new ArrayList<>(currentCart),
            getCartTotal()
        );

        if (dbManager.createOrder(order)) {
            clearCart();
            return transactionId;
        }
        return null;
    }

    public List<Order> getOrderHistory() {
        return dbManager.getAllOrders();
    }

    public Order getOrder(String transactionId) {
        return dbManager.getOrderByTransactionId(transactionId);
    }

    public boolean updateOrderStatus(String transactionId, String status) {
        return dbManager.updateOrderStatus(transactionId, status);
    }

    public boolean deleteOrder(String transactionId) {
        return dbManager.deleteOrder(transactionId);
    }

    // Utility
    private String generateTransactionId() {
        return "OD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    public void closeDatabase() {
        dbManager.close();
    }
}
