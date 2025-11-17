package com.oakdonuts.database;

import com.oakdonuts.models.MenuItem;
import com.oakdonuts.models.Order;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {
    private static final String DB_URL = "jdbc:derby:./data/OakDonutsDB;create=true";
    private static final String DRIVER = "org.apache.derby.jdbc.EmbeddedDriver";
    private Connection connection;

    public DatabaseManager() {
        try {
            Class.forName(DRIVER);
            connection = DriverManager.getConnection(DB_URL);
            initializeTables();
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }

    private void initializeTables() {
        try (Statement stmt = connection.createStatement()) {
            // Create MENU_ITEMS table
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS MENU_ITEMS (" +
                "ID INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY, " +
                "NAME VARCHAR(100) NOT NULL, " +
                "DESCRIPTION VARCHAR(500), " +
                "PRICE DOUBLE NOT NULL, " +
                "CATEGORY VARCHAR(50) NOT NULL" +
                ")"
            );

            // Create ORDERS table
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS ORDERS (" +
                "TRANSACTION_ID VARCHAR(50) PRIMARY KEY, " +
                "ORDER_DATE TIMESTAMP NOT NULL, " +
                "TOTAL_PRICE DOUBLE NOT NULL, " +
                "STATUS VARCHAR(20) DEFAULT 'pending'" +
                ")"
            );

            // Create ORDER_ITEMS junction table
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS ORDER_ITEMS (" +
                "ID INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY, " +
                "TRANSACTION_ID VARCHAR(50) NOT NULL, " +
                "MENU_ITEM_ID INT NOT NULL, " +
                "FOREIGN KEY (TRANSACTION_ID) REFERENCES ORDERS(TRANSACTION_ID) ON DELETE CASCADE, " +
                "FOREIGN KEY (MENU_ITEM_ID) REFERENCES MENU_ITEMS(ID)" +
                ")"
            );

            // Check if menu items exist, if not add sample data
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) AS COUNT FROM MENU_ITEMS");
            if (rs.next() && rs.getInt("COUNT") == 0) {
                addSampleMenuItems();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void addSampleMenuItems() {
        String[] items = {
            "('Classic Glazed', 'Traditional glazed donut', 2.50, 'glaze')",
            "('Chocolate Cake', 'Rich chocolate cake donut', 3.00, 'cake')",
            "('Vanilla Frosted', 'Vanilla frosted with sprinkles', 2.75, 'glaze')",
            "('Strawberry Jam', 'Filled with fresh strawberry jam', 3.25, 'specialty')",
            "('Boston Cream', 'Cream filled with chocolate top', 3.50, 'specialty')",
            "('Maple Glazed', 'Maple flavored donut', 2.75, 'glaze')",
            "('Chocolate Chip', 'Chocolate cake with chips', 3.25, 'cake')",
            "('Powdered Sugar', 'Classic powdered sugar donut', 2.50, 'glaze')"
        };

        try (Statement stmt = connection.createStatement()) {
            for (String item : items) {
                stmt.execute("INSERT INTO MENU_ITEMS (NAME, DESCRIPTION, PRICE, CATEGORY) VALUES " + item);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // MENU ITEMS CRUD
    public List<MenuItem> getAllMenuItems() {
        List<MenuItem> items = new ArrayList<>();
        try (Statement stmt = connection.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT * FROM MENU_ITEMS ORDER BY ID");
            while (rs.next()) {
                items.add(new MenuItem(
                    rs.getInt("ID"),
                    rs.getString("NAME"),
                    rs.getString("DESCRIPTION"),
                    rs.getDouble("PRICE"),
                    rs.getString("CATEGORY")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return items;
    }

    public MenuItem getMenuItemById(int id) {
        try (Statement stmt = connection.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT * FROM MENU_ITEMS WHERE ID = " + id);
            if (rs.next()) {
                return new MenuItem(
                    rs.getInt("ID"),
                    rs.getString("NAME"),
                    rs.getString("DESCRIPTION"),
                    rs.getDouble("PRICE"),
                    rs.getString("CATEGORY")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public int addMenuItem(MenuItem item) {
        try (PreparedStatement pstmt = connection.prepareStatement(
            "INSERT INTO MENU_ITEMS (NAME, DESCRIPTION, PRICE, CATEGORY) VALUES (?, ?, ?, ?)",
            Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, item.getName());
            pstmt.setString(2, item.getDescription());
            pstmt.setDouble(3, item.getPrice());
            pstmt.setString(4, item.getCategory());
            pstmt.executeUpdate();

            ResultSet keys = pstmt.getGeneratedKeys();
            if (keys.next()) {
                return keys.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public boolean updateMenuItem(MenuItem item) {
        try (PreparedStatement pstmt = connection.prepareStatement(
            "UPDATE MENU_ITEMS SET NAME = ?, DESCRIPTION = ?, PRICE = ?, CATEGORY = ? WHERE ID = ?")) {
            pstmt.setString(1, item.getName());
            pstmt.setString(2, item.getDescription());
            pstmt.setDouble(3, item.getPrice());
            pstmt.setString(4, item.getCategory());
            pstmt.setInt(5, item.getId());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean deleteMenuItem(int id) {
        try (Statement stmt = connection.createStatement()) {
            return stmt.executeUpdate("DELETE FROM MENU_ITEMS WHERE ID = " + id) > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // ORDERS CRUD
    public List<Order> getAllOrders() {
        List<Order> orders = new ArrayList<>();
        try (Statement stmt = connection.createStatement()) {
            ResultSet rs = stmt.executeQuery(
                "SELECT * FROM ORDERS ORDER BY ORDER_DATE DESC"
            );
            while (rs.next()) {
                Order order = new Order();
                order.setTransactionId(rs.getString("TRANSACTION_ID"));
                order.setDate(rs.getTimestamp("ORDER_DATE").toLocalDateTime());
                order.setTotalPrice(rs.getDouble("TOTAL_PRICE"));
                order.setStatus(rs.getString("STATUS"));
                order.setItems(getOrderItems(rs.getString("TRANSACTION_ID")));
                orders.add(order);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return orders;
    }

    public Order getOrderByTransactionId(String transactionId) {
        try (Statement stmt = connection.createStatement()) {
            ResultSet rs = stmt.executeQuery(
                "SELECT * FROM ORDERS WHERE TRANSACTION_ID = '" + transactionId + "'"
            );
            if (rs.next()) {
                Order order = new Order();
                order.setTransactionId(rs.getString("TRANSACTION_ID"));
                order.setDate(rs.getTimestamp("ORDER_DATE").toLocalDateTime());
                order.setTotalPrice(rs.getDouble("TOTAL_PRICE"));
                order.setStatus(rs.getString("STATUS"));
                order.setItems(getOrderItems(transactionId));
                return order;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean createOrder(Order order) {
        try (PreparedStatement pstmt = connection.prepareStatement(
            "INSERT INTO ORDERS (TRANSACTION_ID, ORDER_DATE, TOTAL_PRICE, STATUS) VALUES (?, ?, ?, ?)")) {
            pstmt.setString(1, order.getTransactionId());
            pstmt.setTimestamp(2, Timestamp.valueOf(order.getDate()));
            pstmt.setDouble(3, order.getTotalPrice());
            pstmt.setString(4, order.getStatus());
            pstmt.executeUpdate();

            // Add items to ORDER_ITEMS
            for (MenuItem item : order.getItems()) {
                addOrderItem(order.getTransactionId(), item.getId());
            }
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateOrderStatus(String transactionId, String status) {
        try (PreparedStatement pstmt = connection.prepareStatement(
            "UPDATE ORDERS SET STATUS = ? WHERE TRANSACTION_ID = ?")) {
            pstmt.setString(1, status);
            pstmt.setString(2, transactionId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean deleteOrder(String transactionId) {
        try (Statement stmt = connection.createStatement()) {
            return stmt.executeUpdate("DELETE FROM ORDERS WHERE TRANSACTION_ID = '" + transactionId + "'") > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Helper methods
    private void addOrderItem(String transactionId, int menuItemId) {
        try (PreparedStatement pstmt = connection.prepareStatement(
            "INSERT INTO ORDER_ITEMS (TRANSACTION_ID, MENU_ITEM_ID) VALUES (?, ?)")) {
            pstmt.setString(1, transactionId);
            pstmt.setInt(2, menuItemId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private List<MenuItem> getOrderItems(String transactionId) {
        List<MenuItem> items = new ArrayList<>();
        try (Statement stmt = connection.createStatement()) {
            ResultSet rs = stmt.executeQuery(
                "SELECT m.* FROM MENU_ITEMS m " +
                "JOIN ORDER_ITEMS oi ON m.ID = oi.MENU_ITEM_ID " +
                "WHERE oi.TRANSACTION_ID = '" + transactionId + "'"
            );
            while (rs.next()) {
                items.add(new MenuItem(
                    rs.getInt("ID"),
                    rs.getString("NAME"),
                    rs.getString("DESCRIPTION"),
                    rs.getDouble("PRICE"),
                    rs.getString("CATEGORY")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return items;
    }

    public void close() {
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
