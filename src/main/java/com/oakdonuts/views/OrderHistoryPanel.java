package com.oakdonuts.views;

import com.oakdonuts.controllers.OrderController;
import com.oakdonuts.models.MenuItem;
import com.oakdonuts.models.Order;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class OrderHistoryPanel extends JPanel {
    private OrderController controller;
    private JTable ordersTable;
    private DefaultTableModel ordersModel;
    private JTextArea detailsArea;
    private JComboBox<String> statusCombo;

    public OrderHistoryPanel(OrderController controller) {
        this.controller = controller;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Top: Orders list
        add(createOrdersListPanel(), BorderLayout.CENTER);

        // Bottom: Details and actions
        add(createActionsPanel(), BorderLayout.SOUTH);

        loadOrderHistory();
    }

    private JPanel createOrdersListPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // Split pane: Orders table and details
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);

        // Left: Orders table
        String[] columns = {"Transaction ID", "Date", "Items", "Total", "Status"};
        ordersModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        ordersTable = new JTable(ordersModel);
        ordersTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        ordersTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (ordersTable.getSelectedRow() >= 0) {
                    loadOrderDetails();
                }
            }
        });

        JScrollPane tableScroll = new JScrollPane(ordersTable);
        splitPane.setLeftComponent(tableScroll);

        // Right: Order details
        detailsArea = new JTextArea();
        detailsArea.setEditable(false);
        detailsArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane detailsScroll = new JScrollPane(detailsArea);
        splitPane.setRightComponent(detailsScroll);

        splitPane.setDividerLocation(350);
        panel.add(splitPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createActionsPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        panel.setBorder(BorderFactory.createTitledBorder("Order Actions"));

        panel.add(new JLabel("Status:"));
        statusCombo = new JComboBox<>(new String[]{"pending", "completed", "cancelled"});
        panel.add(statusCombo);

        JButton updateStatusBtn = new JButton("Update Status");
        updateStatusBtn.addActionListener(e -> updateOrderStatus());
        panel.add(updateStatusBtn);

        JButton deleteBtn = new JButton("Delete Order");
        deleteBtn.addActionListener(e -> deleteOrder());
        panel.add(deleteBtn);

        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(e -> {
            loadOrderHistory();
            detailsArea.setText("");
        });
        panel.add(refreshBtn);

        return panel;
    }

    private void loadOrderHistory() {
        ordersModel.setRowCount(0);
        List<Order> orders = controller.getOrderHistory();

        for (Order order : orders) {
            ordersModel.addRow(new Object[]{
                order.getTransactionId(),
                order.getFormattedDate(),
                order.getItems().size(),
                String.format("$%.2f", order.getTotalPrice()),
                order.getStatus()
            });
        }
    }

    private void loadOrderDetails() {
        int selectedRow = ordersTable.getSelectedRow();
        if (selectedRow < 0) {
            detailsArea.setText("");
            return;
        }

        String transactionId = (String) ordersModel.getValueAt(selectedRow, 0);
        Order order = controller.getOrder(transactionId);

        if (order != null) {
            StringBuilder details = new StringBuilder();
            details.append("=== ORDER DETAILS ===\n\n");
            details.append("Transaction ID: ").append(order.getTransactionId()).append("\n");
            details.append("Date: ").append(order.getFormattedDate()).append("\n");
            details.append("Status: ").append(order.getStatus()).append("\n");
            details.append("Total: $").append(String.format("%.2f", order.getTotalPrice())).append("\n\n");
            details.append("--- ITEMS ---\n");

            for (MenuItem item : order.getItems()) {
                details.append("• ").append(item.getName())
                        .append(" - $").append(String.format("%.2f", item.getPrice())).append("\n");
            }

            detailsArea.setText(details.toString());
        }
    }

    private void updateOrderStatus() {
        int selectedRow = ordersTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select an order!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String transactionId = (String) ordersModel.getValueAt(selectedRow, 0);
        String newStatus = (String) statusCombo.getSelectedItem();

        if (controller.updateOrderStatus(transactionId, newStatus)) {
            JOptionPane.showMessageDialog(this, "Order status updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            loadOrderHistory();
            detailsArea.setText("");
        } else {
            JOptionPane.showMessageDialog(this, "Failed to update order status!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteOrder() {
        int selectedRow = ordersTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select an order to delete!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String transactionId = (String) ordersModel.getValueAt(selectedRow, 0);
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this order?", "Confirm", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            if (controller.deleteOrder(transactionId)) {
                JOptionPane.showMessageDialog(this, "Order deleted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadOrderHistory();
                detailsArea.setText("");
            } else {
                JOptionPane.showMessageDialog(this, "Failed to delete order!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
