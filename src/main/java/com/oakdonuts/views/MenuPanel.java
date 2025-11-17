package com.oakdonuts.views;

import com.oakdonuts.controllers.OrderController;
import com.oakdonuts.models.MenuItem;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;

public class MenuPanel extends JPanel {
    private OrderController controller;
    private JTable menuTable;
    private DefaultTableModel tableModel;
    private JTextField nameField, descField, priceField, categoryField;

    public MenuPanel(OrderController controller) {
        this.controller = controller;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Top panel for menu display
        add(createMenuDisplayPanel(), BorderLayout.CENTER);

        // Bottom panel for CRUD operations
        add(createCRUDPanel(), BorderLayout.SOUTH);

        loadMenuData();
    }

    private JPanel createMenuDisplayPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // Table for menu items
        String[] columns = {"ID", "Name", "Description", "Price", "Category"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        menuTable = new JTable(tableModel);
        menuTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        menuTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (menuTable.getSelectedRow() >= 0) {
                    loadSelectedItemToForm();
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(menuTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createCRUDPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(2, 4, 5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Add/Edit Menu Item"));

        panel.add(new JLabel("Name:"));
        nameField = new JTextField();
        panel.add(nameField);

        panel.add(new JLabel("Description:"));
        descField = new JTextField();
        panel.add(descField);

        panel.add(new JLabel("Price:"));
        priceField = new JTextField();
        panel.add(priceField);

        panel.add(new JLabel("Category:"));
        categoryField = new JTextField();
        panel.add(categoryField);

        // Buttons
        JButton addBtn = new JButton("Add");
        addBtn.addActionListener(e -> addMenuItem());
        panel.add(addBtn);

        JButton updateBtn = new JButton("Update");
        updateBtn.addActionListener(e -> updateMenuItem());
        panel.add(updateBtn);

        JButton deleteBtn = new JButton("Delete");
        deleteBtn.addActionListener(e -> deleteMenuItem());
        panel.add(deleteBtn);

        JButton clearBtn = new JButton("Clear");
        clearBtn.addActionListener(e -> clearForm());
        panel.add(clearBtn);

        return panel;
    }

    private void loadMenuData() {
        tableModel.setRowCount(0);
        List<MenuItem> items = controller.getMenuItems();
        for (MenuItem item : items) {
            tableModel.addRow(new Object[]{
                item.getId(),
                item.getName(),
                item.getDescription(),
                String.format("$%.2f", item.getPrice()),
                item.getCategory()
            });
        }
    }

    private void loadSelectedItemToForm() {
        int selectedRow = menuTable.getSelectedRow();
        if (selectedRow >= 0) {
            int id = (int) tableModel.getValueAt(selectedRow, 0);
            nameField.setText((String) tableModel.getValueAt(selectedRow, 1));
            descField.setText((String) tableModel.getValueAt(selectedRow, 2));
            priceField.setText(((String) tableModel.getValueAt(selectedRow, 3)).replace("$", ""));
            categoryField.setText((String) tableModel.getValueAt(selectedRow, 4));
        }
    }

    private void addMenuItem() {
        try {
            String name = nameField.getText().trim();
            String desc = descField.getText().trim();
            double price = Double.parseDouble(priceField.getText().trim());
            String category = categoryField.getText().trim();

            if (name.isEmpty() || category.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Name and Category are required!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            MenuItem item = new MenuItem(name, desc, price, category);
            int id = controller.addMenuItem(item);
            if (id > 0) {
                JOptionPane.showMessageDialog(this, "Menu item added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                clearForm();
                loadMenuData();
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid price format!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateMenuItem() {
        int selectedRow = menuTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select an item to update!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            int id = (int) tableModel.getValueAt(selectedRow, 0);
            String name = nameField.getText().trim();
            String desc = descField.getText().trim();
            double price = Double.parseDouble(priceField.getText().trim());
            String category = categoryField.getText().trim();

            if (name.isEmpty() || category.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Name and Category are required!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            MenuItem item = new MenuItem(id, name, desc, price, category);
            if (controller.updateMenuItem(item)) {
                JOptionPane.showMessageDialog(this, "Menu item updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                clearForm();
                loadMenuData();
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid price format!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteMenuItem() {
        int selectedRow = menuTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select an item to delete!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int id = (int) tableModel.getValueAt(selectedRow, 0);
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this item?", "Confirm", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            if (controller.deleteMenuItem(id)) {
                JOptionPane.showMessageDialog(this, "Menu item deleted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                clearForm();
                loadMenuData();
            }
        }
    }

    private void clearForm() {
        nameField.setText("");
        descField.setText("");
        priceField.setText("");
        categoryField.setText("");
        menuTable.clearSelection();
    }
}
