package com.oakdonuts.models;

import java.util.ArrayList;
import java.util.List;

// Order - represents a completed order containing items and transaction details
public class Order {
    public String transactionId;
    public String date;
    public List<MenuItem> items;
    public double totalPrice;
    public String status;

    // EMPTY CONSTRUCTOR - creates order with empty item list and pending status
    public Order() {
        this.items = new ArrayList<>();
        this.status = "pending";
    }

    // FULL CONSTRUCTOR - creates order with all details including item list
    public Order(String transactionId, String date, List<MenuItem> items, double totalPrice) {
        this.transactionId = transactionId;
        this.date = date;
        this.items = items != null ? items : new ArrayList<>();
        this.totalPrice = totalPrice;
        this.status = "pending";
    }

    // TOSTRING - display order summary with ID, date, item count, and total price
    public String toString() {
        return "ID: " + transactionId + " | Date: " + date + " | Items: " + items.size() + " | $" + String.format("%.2f", totalPrice);
    }
}
