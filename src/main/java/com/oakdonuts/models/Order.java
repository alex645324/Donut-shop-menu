package com.oakdonuts.models;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class Order {
    private String transactionId;
    private LocalDateTime date;
    private List<MenuItem> items;
    private double totalPrice;
    private String status; // pending, completed, cancelled

    public Order() {
        this.items = new ArrayList<>();
        this.status = "pending";
    }

    public Order(String transactionId, LocalDateTime date, List<MenuItem> items, double totalPrice) {
        this.transactionId = transactionId;
        this.date = date;
        this.items = items != null ? items : new ArrayList<>();
        this.totalPrice = totalPrice;
        this.status = "pending";
    }

    // Getters and Setters
    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public List<MenuItem> getItems() {
        return items;
    }

    public void setItems(List<MenuItem> items) {
        this.items = items;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void addItem(MenuItem item) {
        this.items.add(item);
        this.totalPrice += item.getPrice();
    }

    public void removeItem(MenuItem item) {
        if (this.items.remove(item)) {
            this.totalPrice -= item.getPrice();
        }
    }

    public String getFormattedDate() {
        if (date == null) return "";
        return date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    @Override
    public String toString() {
        return "Order{" +
                "transactionId='" + transactionId + '\'' +
                ", date=" + getFormattedDate() +
                ", items=" + items.size() +
                ", totalPrice=$" + String.format("%.2f", totalPrice) +
                ", status='" + status + '\'' +
                '}';
    }
}
