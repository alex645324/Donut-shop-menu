package com.oakdonuts;

import com.oakdonuts.database.DatabaseManager;
import com.oakdonuts.models.MenuItem;
import com.oakdonuts.models.Order;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

// Main application winndow - manages all GUI components for the Oak Donuts ordering ssytsem
public class MainFrame extends JFrame {
    private DatabaseManager db;
    private JTabbedPane tabs;
    private List<MenuItem> cart;
    private DefaultTableModel menuTableModel, ordersTableModel;
    private JTable menuTable, ordersTable, cartTable;
    private DefaultTableModel cartTableModel;
    private JLabel cartTotalLabel, orderDetailsLabel;
    private JTextArea orderDetailsArea;

    // Constructor - setup main window and all tabs
    public MainFrame() {
        setTitle("Oak Donuts (OD) - Menu & Ordering System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);

        db = new DatabaseManager();
        cart = new ArrayList<>();

        tabs = new JTabbedPane();
        tabs.addTab("Menu", createMenuTab());
        tabs.addTab("Options", createOrderTab());
        tabs.addTab("Order Summary", createHistoryTab());
        tabs.addTab("Friends Orders", createFriendsOrdersTab());

        add(tabs);
        setVisible(true);

        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent e) {
                db.close();
                System.exit(0);
            }
        });
    }

    // MENU TAB - display menu items and add/delete items
    private JPanel createMenuTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // MENU TABLE BLOCK
        menuTableModel = new DefaultTableModel(new String[]{"ID", "Name", "Description", "Price", "Category"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        menuTable = new JTable(menuTableModel);
        JScrollPane scrollPane = new JScrollPane(menuTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        // ADD ITEM FORM BLOCK
        JPanel formPanel = new JPanel(new GridLayout(2, 4, 5, 5));
        formPanel.setBorder(BorderFactory.createTitledBorder("Add New Item"));

        JTextField nameField = new JTextField();
        JTextField descField = new JTextField();
        JTextField priceField = new JTextField();
        JTextField categoryField = new JTextField();

        formPanel.add(new JLabel("Name:"));
        formPanel.add(nameField);
        formPanel.add(new JLabel("Description:"));
        formPanel.add(descField);
        formPanel.add(new JLabel("Price:"));
        formPanel.add(priceField);
        formPanel.add(new JLabel("Category:"));
        formPanel.add(categoryField);

        // ADD ITEM BUTTON BLOCK
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton addBtn = new JButton("Add Item");
        addBtn.addActionListener(e -> {
            try {
                String name = nameField.getText().trim();
                String desc = descField.getText().trim();
                String priceStr = priceField.getText().trim();
                String category = categoryField.getText().trim();

                if (name.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Please enter a name");
                    return;
                }
                if (category.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Please enter a category");
                    return;
                }
                if (priceStr.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Please enter a price");
                    return;
                }

                double price = Double.parseDouble(priceStr);
                db.addMenuItem(name, desc, price, category);
                JOptionPane.showMessageDialog(this, "Item added successfully!");
                loadMenu();
                nameField.setText("");
                descField.setText("");
                priceField.setText("");
                categoryField.setText("");
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Price must be a valid number");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        });

        // DELETE ITEM BUTTON BLOCK
        JButton deleteBtn = new JButton("Delete Selected");
        deleteBtn.addActionListener(e -> {
            int row = menuTable.getSelectedRow();
            if (row >= 0) {
                int id = (int) menuTableModel.getValueAt(row, 0);
                db.deleteMenuItem(id);
                loadMenu();
            }
        });

        buttonPanel.add(addBtn);
        buttonPanel.add(deleteBtn);

        JPanel bottomPanel = new JPanel(new BorderLayout(10, 10));
        bottomPanel.add(formPanel, BorderLayout.NORTH);
        bottomPanel.add(buttonPanel, BorderLayout.SOUTH);
        panel.add(bottomPanel, BorderLayout.SOUTH);

        loadMenu();
        return panel;
    }

    // OPTIONS TAB - shopping cart and checkout
    private JPanel createOrderTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // MENU LIST BLOCK - left side with available items
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBorder(BorderFactory.createTitledBorder("Available Items"));
        JList<MenuItem> menuList = new JList<>();
        menuList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        leftPanel.add(new JScrollPane(menuList), BorderLayout.CENTER);

        // ADD TO CART BUTTON BLOCK
        JButton addToCartBtn = new JButton("Add to Cart");
        addToCartBtn.addActionListener(e -> {
            MenuItem selected = menuList.getSelectedValue();
            if (selected != null) {
                cart.add(selected);
                updateCartDisplay();
            }
        });
        leftPanel.add(addToCartBtn, BorderLayout.SOUTH);

        // SHOPPING CART BLOCK - right side with cart items
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBorder(BorderFactory.createTitledBorder("Shopping Cart"));
        cartTableModel = new DefaultTableModel(new String[]{"Item", "Price"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        cartTable = new JTable(cartTableModel);
        rightPanel.add(new JScrollPane(cartTable), BorderLayout.CENTER);

        // REMOVE ITEM BUTTON BLOCK
        JButton removeBtn = new JButton("Remove Selected");
        removeBtn.addActionListener(e -> {
            int row = cartTable.getSelectedRow();
            if (row >= 0) {
                cart.remove(row);
                updateCartDisplay();
            }
        });

        // CART TOTAL DISPLAY BLOCK
        cartTotalLabel = new JLabel("Total: $0.00");
        cartTotalLabel.setFont(new Font("Arial", Font.BOLD, 16));

        // CHECKOUT BUTTON BLOCK
        JButton checkoutBtn = new JButton("Checkout");
        checkoutBtn.addActionListener(e -> {
            if (cart.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Cart is empty!");
                return;
            }
            double total = cart.stream().mapToDouble(item -> item.price).sum();
            JOptionPane.showMessageDialog(this, "Total: $" + String.format("%.2f", total));
            String transactionId = "OD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            db.createOrder(transactionId, date, total, cart);
            cart.clear();
            updateCartDisplay();
            loadOrders();
        });

        JPanel cartBottomPanel = new JPanel(new BorderLayout());
        JPanel cartButtonPanel = new JPanel(new FlowLayout());
        cartButtonPanel.add(removeBtn);
        cartButtonPanel.add(checkoutBtn);
        cartBottomPanel.add(cartTotalLabel, BorderLayout.WEST);
        cartBottomPanel.add(cartButtonPanel, BorderLayout.EAST);
        rightPanel.add(cartBottomPanel, BorderLayout.SOUTH);

        JPanel containerPanel = new JPanel(new GridLayout(1, 2, 10, 10));
        containerPanel.add(leftPanel);
        containerPanel.add(rightPanel);
        panel.add(containerPanel, BorderLayout.CENTER);

        menuList.setListData(db.getMenuItems().toArray(new MenuItem[0]));
        updateCartDisplay();

        return panel;
    }

    // UPDATE CART DISPLAY - refresh cart table and total
    private void updateCartDisplay() {
        cartTableModel.setRowCount(0);
        double total = 0;
        for (MenuItem item : cart) {
            cartTableModel.addRow(new Object[]{item.name, "$" + String.format("%.2f", item.price)});
            total += item.price;
        }
        cartTotalLabel.setText("Total: $" + String.format("%.2f", total));
    }

    // ORDER SUMMARY TAB - display order history and details
    private JPanel createHistoryTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // ORDERS TABLE BLOCK - left side showing order list
        ordersTableModel = new DefaultTableModel(new String[]{"Transaction ID", "Date", "Items", "Total", "Status"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        ordersTable = new JTable(ordersTableModel);

        // SPLIT PANE BLOCK - orders on left, details on right
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setLeftComponent(new JScrollPane(ordersTable));

        orderDetailsArea = new JTextArea();
        orderDetailsArea.setEditable(false);
        orderDetailsArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        splitPane.setRightComponent(new JScrollPane(orderDetailsArea));
        splitPane.setDividerLocation(300);

        // ORDER SELECTION LISTENER BLOCK - display details when order selected
        ordersTable.getSelectionModel().addListSelectionListener(e -> {
            int row = ordersTable.getSelectedRow();
            if (row >= 0) {
                String transactionId = (String) ordersTableModel.getValueAt(row, 0);
                List<Order> orders = db.getOrders();
                for (Order order : orders) {
                    if (order.transactionId.equals(transactionId)) {
                        StringBuilder details = new StringBuilder();
                        details.append("=== ORDER DETAILS ===\n\n");
                        details.append("Transaction ID: ").append(order.transactionId).append("\n");
                        details.append("Date: ").append(order.date).append("\n");
                        details.append("Status: ").append(order.status).append("\n");
                        details.append("Total: $").append(String.format("%.2f", order.totalPrice)).append("\n\n");
                        details.append("--- ITEMS ---\n");
                        for (MenuItem item : order.items) {
                            details.append("• ").append(item.name).append(" - $").append(String.format("%.2f", item.price)).append("\n");
                        }
                        orderDetailsArea.setText(details.toString());
                        break;
                    }
                }
            }
        });

        panel.add(splitPane, BorderLayout.CENTER);

        // DELETE ORDER BUTTON BLOCK
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton deleteBtn = new JButton("Delete Order");
        deleteBtn.addActionListener(e -> {
            int row = ordersTable.getSelectedRow();
            if (row >= 0) {
                String transactionId = (String) ordersTableModel.getValueAt(row, 0);
                if (JOptionPane.showConfirmDialog(this, "Delete this order?") == JOptionPane.YES_OPTION) {
                    db.deleteOrder(transactionId);
                    loadOrders();
                    orderDetailsArea.setText("");
                }
            }
        });

        actionPanel.add(deleteBtn);
        panel.add(actionPanel, BorderLayout.SOUTH);

        loadOrders();
        return panel;
    }


    private void loadMenu() {
        menuTableModel.setRowCount(0);
        for (MenuItem item : db.getMenuItems()) {
            menuTableModel.addRow(new Object[]{item.id, item.name, item.description, "$" + String.format("%.2f", item.price), item.category});
        }
    }

    // LOAD ORDERS - refresh orders table from database
    private void loadOrders() {
        ordersTableModel.setRowCount(0);
        for (Order order : db.getOrders()) {
            ordersTableModel.addRow(new Object[]{order.transactionId, order.date, order.items.size(), "$" + String.format("%.2f", order.totalPrice), order.status});
        }
    }

    // FRIENDS ORDERS TAB - display friends' orders with placeholder data
    private JPanel createFriendsOrdersTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // TITLE BLOCK
        JLabel titleLabel = new JLabel("What Your Friends Ordered");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        panel.add(titleLabel, BorderLayout.NORTH);

        // FRIENDS TABLE BLOCK
        DefaultTableModel friendsTableModel = new DefaultTableModel(new String[]{"Friend Name", "Items Ordered", "Total", "Date"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable friendsTable = new JTable(friendsTableModel);
        friendsTable.setRowHeight(30);

        // PLACEHOLDER DATA BLOCK
        friendsTableModel.addRow(new Object[]{"Sarah", "Boston Cream, Classic Glazed", "$6.00", "2025-11-16 14:30"});
        friendsTableModel.addRow(new Object[]{"Mike", "Chocolate Cake (x2)", "$6.00", "2025-11-16 13:15"});
        friendsTableModel.addRow(new Object[]{"Emma", "Strawberry Jam", "$3.25", "2025-11-16 12:00"});
        friendsTableModel.addRow(new Object[]{"Alex", "Vanilla Frosted, Powdered Sugar", "$5.25", "2025-11-16 11:45"});
        friendsTableModel.addRow(new Object[]{"Jordan", "Maple Glazed (x3)", "$8.25", "2025-11-16 10:30"});
        friendsTableModel.addRow(new Object[]{"Taylor", "Chocolate Chip", "$3.25", "2025-11-16 09:00"});

        JScrollPane scrollPane = new JScrollPane(friendsTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    // APPLICATION ENTRY POINT
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainFrame());
    }
}
