package com.oakdonuts.views;

import com.oakdonuts.controllers.OrderController;
import com.oakdonuts.models.MenuItem;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class OrderPanel extends JPanel {
    private OrderController controller;
    private JList<MenuItem> menuList;
    private JTable cartTable;
    private DefaultTableModel cartModel;
    private JLabel totalLabel;
    private JComboBox<String> categoryCombo;

    public OrderPanel(OrderController controller) {
        this.controller = controller;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Left: Menu display with categories
        add(createMenuPanel(), BorderLayout.WEST);

        // Center: Shopping cart
        add(createCartPanel(), BorderLayout.CENTER);

        // Bottom: Total and Place Order
        add(createCheckoutPanel(), BorderLayout.SOUTH);

        loadMenuData();
    }

    private JPanel createMenuPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Menu Items"));
        panel.setPreferredSize(new Dimension(250, 400));

        // Category filter
        JPanel categoryPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        categoryPanel.add(new JLabel("Category:"));
        categoryCombo = new JComboBox<>(new String[]{"All", "glaze", "cake", "specialty"});
        categoryCombo.addActionListener(e -> filterMenuByCategory());
        categoryPanel.add(categoryCombo);
        panel.add(categoryPanel, BorderLayout.NORTH);

        // Menu list
        menuList = new JList<>();
        menuList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(menuList);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Add to cart button
        JButton addBtn = new JButton("Add to Cart");
        addBtn.addActionListener(e -> addToCart());
        panel.add(addBtn, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createCartPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Shopping Cart"));

        // Cart table
        String[] columns = {"Item Name", "Price"};
        cartModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        cartTable = new JTable(cartModel);
        cartTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scrollPane = new JScrollPane(cartTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Remove button
        JButton removeBtn = new JButton("Remove Selected");
        removeBtn.addActionListener(e -> removeFromCart());
        panel.add(removeBtn, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createCheckoutPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));

        totalLabel = new JLabel("Total: $0.00");
        totalLabel.setFont(new Font("Arial", Font.BOLD, 16));
        panel.add(totalLabel);

        JButton placeOrderBtn = new JButton("Place Order");
        placeOrderBtn.setFont(new Font("Arial", Font.BOLD, 14));
        placeOrderBtn.addActionListener(e -> placeOrder());
        panel.add(placeOrderBtn);

        JButton clearCartBtn = new JButton("Clear Cart");
        clearCartBtn.addActionListener(e -> {
            controller.clearCart();
            updateCartDisplay();
        });
        panel.add(clearCartBtn);

        return panel;
    }

    private void loadMenuData() {
        List<MenuItem> items = controller.getMenuItems();
        menuList.setListData(items.toArray(new MenuItem[0]));
    }

    private void filterMenuByCategory() {
        String selected = (String) categoryCombo.getSelectedItem();
        List<MenuItem> items;

        if ("All".equals(selected)) {
            items = controller.getMenuItems();
        } else {
            items = controller.getMenuItemsByCategory(selected);
        }

        menuList.setListData(items.toArray(new MenuItem[0]));
    }

    private void addToCart() {
        MenuItem selected = menuList.getSelectedValue();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Please select an item!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        controller.addToCart(selected);
        updateCartDisplay();
    }

    private void removeFromCart() {
        int selectedRow = cartTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select an item to remove!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        List<MenuItem> cart = controller.getCart();
        MenuItem item = cart.get(selectedRow);
        controller.removeFromCart(item);
        updateCartDisplay();
    }

    private void updateCartDisplay() {
        cartModel.setRowCount(0);
        List<MenuItem> cart = controller.getCart();

        for (MenuItem item : cart) {
            cartModel.addRow(new Object[]{
                item.getName(),
                String.format("$%.2f", item.getPrice())
            });
        }

        double total = controller.getCartTotal();
        totalLabel.setText("Total: $" + String.format("%.2f", total));
    }

    private void placeOrder() {
        if (controller.getCart().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Cart is empty!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String transactionId = controller.createOrder();
        if (transactionId != null) {
            JOptionPane.showMessageDialog(this, "Order placed successfully!\nTransaction ID: " + transactionId, "Success", JOptionPane.INFORMATION_MESSAGE);
            updateCartDisplay();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to place order!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
