package com.oakdonuts.database;

import com.oakdonuts.models.MenuItem;
import com.oakdonuts.models.Order;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

// DatabaseManager - handles all database operations for menu items and orders
public class DatabaseManager {
    private Connection conn;

    // CONSTRUCTOR - initialize database connection and create tables
    public DatabaseManager() {
        try {
            conn = DriverManager.getConnection("jdbc:derby:./data/OakDonutsDB;create=true");
            createTables();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // CREATE TABLES BLOCK - create database schema and populate initial data
    private void createTables() {
        try {
            Statement stmt = conn.createStatement();
            // MENU ITEMS TABLE
            try {
                stmt.execute("CREATE TABLE MENU_ITEMS (ID INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY, NAME VARCHAR(100), DESCRIPTION VARCHAR(500), PRICE DOUBLE, CATEGORY VARCHAR(50))");
            } catch (Exception e) {
            }
            // ORDERS TABLE
            try {
                stmt.execute("CREATE TABLE ORDERS (TRANSACTION_ID VARCHAR(50) PRIMARY KEY, ORDER_DATE VARCHAR(50), TOTAL_PRICE DOUBLE, STATUS VARCHAR(20))");
            } catch (Exception e) {
            }
            // ORDER ITEMS TABLE
            try {
                stmt.execute("CREATE TABLE ORDER_ITEMS (ID INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY, TRANSACTION_ID VARCHAR(50), MENU_ITEM_ID INT)");
            } catch (Exception e) {
            }

            // INSERT INITIAL DATA BLOCK - populate sample menu items if database is empty
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM MENU_ITEMS");
            rs.next();
            if (rs.getInt(1) == 0) {
                stmt.execute("INSERT INTO MENU_ITEMS (NAME, DESCRIPTION, PRICE, CATEGORY) VALUES ('Classic Glazed', 'Traditional glazed donut', 2.50, 'glaze')");
                stmt.execute("INSERT INTO MENU_ITEMS (NAME, DESCRIPTION, PRICE, CATEGORY) VALUES ('Chocolate Cake', 'Rich chocolate cake donut', 3.00, 'cake')");
                stmt.execute("INSERT INTO MENU_ITEMS (NAME, DESCRIPTION, PRICE, CATEGORY) VALUES ('Vanilla Frosted', 'Vanilla frosted with sprinkles', 2.75, 'glaze')");
                stmt.execute("INSERT INTO MENU_ITEMS (NAME, DESCRIPTION, PRICE, CATEGORY) VALUES ('Strawberry Jam', 'Filled with fresh strawberry jam', 3.25, 'specialty')");
                stmt.execute("INSERT INTO MENU_ITEMS (NAME, DESCRIPTION, PRICE, CATEGORY) VALUES ('Boston Cream', 'Cream filled with chocolate top', 3.50, 'specialty')");
                stmt.execute("INSERT INTO MENU_ITEMS (NAME, DESCRIPTION, PRICE, CATEGORY) VALUES ('Maple Glazed', 'Maple flavored donut', 2.75, 'glaze')");
                stmt.execute("INSERT INTO MENU_ITEMS (NAME, DESCRIPTION, PRICE, CATEGORY) VALUES ('Chocolate Chip', 'Chocolate cake with chips', 3.25, 'cake')");
                stmt.execute("INSERT INTO MENU_ITEMS (NAME, DESCRIPTION, PRICE, CATEGORY) VALUES ('Powdered Sugar', 'Classic powdered sugar donut', 2.50, 'glaze')");
            }
            stmt.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // GET MENU ITEMS - retrieve all menu items from database
    public List<MenuItem> getMenuItems() {
        List<MenuItem> items = new ArrayList<>();
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM MENU_ITEMS");
            while (rs.next()) {
                MenuItem item = new MenuItem(rs.getInt("ID"), rs.getString("NAME"), rs.getString("DESCRIPTION"), rs.getDouble("PRICE"), rs.getString("CATEGORY"));
                items.add(item);
            }
            stmt.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return items;
    }

    // ADD MENU ITEM - insert new item into database
    public void addMenuItem(String name, String desc, double price, String category) {
        try {
            Statement stmt = conn.createStatement();
            stmt.execute("INSERT INTO MENU_ITEMS (NAME, DESCRIPTION, PRICE, CATEGORY) VALUES ('" + name + "', '" + desc + "', " + price + ", '" + category + "')");
            stmt.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // UPDATE MENU ITEM - modify existing menu item in database
    public void updateMenuItem(int id, String name, String desc, double price, String category) {
        try {
            Statement stmt = conn.createStatement();
            stmt.execute("UPDATE MENU_ITEMS SET NAME='" + name + "', DESCRIPTION='" + desc + "', PRICE=" + price + ", CATEGORY='" + category + "' WHERE ID=" + id);
            stmt.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // DELETE MENU ITEM - remove item from database by ID
    public void deleteMenuItem(int id) {
        try {
            Statement stmt = conn.createStatement();
            stmt.execute("DELETE FROM MENU_ITEMS WHERE ID=" + id);
            stmt.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // GET ORDERS - retrieve all orders from database with items
    public List<Order> getOrders() {
        List<Order> orders = new ArrayList<>();
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM ORDERS ORDER BY ORDER_DATE DESC");
            while (rs.next()) {
                Order order = new Order();
                order.transactionId = rs.getString("TRANSACTION_ID");
                order.date = rs.getString("ORDER_DATE");
                order.totalPrice = rs.getDouble("TOTAL_PRICE");
                order.status = rs.getString("STATUS");
                order.items = getOrderItems(order.transactionId);
                orders.add(order);
            }
            stmt.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return orders;
    }

    // CREATE ORDER - save new order and items to database
    public void createOrder(String transactionId, String date, double totalPrice, List<MenuItem> items) {
        try {
            Statement stmt = conn.createStatement();
            stmt.execute("INSERT INTO ORDERS (TRANSACTION_ID, ORDER_DATE, TOTAL_PRICE, STATUS) VALUES ('" + transactionId + "', '" + date + "', " + totalPrice + ", 'pending')");
            for (MenuItem item : items) {
                stmt.execute("INSERT INTO ORDER_ITEMS (TRANSACTION_ID, MENU_ITEM_ID) VALUES ('" + transactionId + "', " + item.id + ")");
            }
            stmt.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // UPDATE ORDER STATUS - change status of order in database
    public void updateOrderStatus(String transactionId, String status) {
        try {
            Statement stmt = conn.createStatement();
            stmt.execute("UPDATE ORDERS SET STATUS='" + status + "' WHERE TRANSACTION_ID='" + transactionId + "'");
            stmt.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // DELETE ORDER - remove order and related items from database
    public void deleteOrder(String transactionId) {
        try {
            Statement stmt = conn.createStatement();
            stmt.execute("DELETE FROM ORDER_ITEMS WHERE TRANSACTION_ID='" + transactionId + "'");
            stmt.execute("DELETE FROM ORDERS WHERE TRANSACTION_ID='" + transactionId + "'");
            stmt.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // GET ORDER ITEMS - retrieve all items for a specific order
    private List<MenuItem> getOrderItems(String transactionId) {
        List<MenuItem> items = new ArrayList<>();
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT m.* FROM MENU_ITEMS m JOIN ORDER_ITEMS oi ON m.ID=oi.MENU_ITEM_ID WHERE oi.TRANSACTION_ID='" + transactionId + "'");
            while (rs.next()) {
                MenuItem item = new MenuItem(rs.getInt("ID"), rs.getString("NAME"), rs.getString("DESCRIPTION"), rs.getDouble("PRICE"), rs.getString("CATEGORY"));
                items.add(item);
            }
            stmt.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return items;
    }

    // CLOSE CONNECTION - disconnect from database
    public void close() {
        try {
            if (conn != null) conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
