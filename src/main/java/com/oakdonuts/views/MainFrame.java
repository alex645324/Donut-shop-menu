package com.oakdonuts.views;

import com.oakdonuts.controllers.OrderController;
import com.oakdonuts.database.DatabaseManager;

import javax.swing.*;

public class MainFrame extends JFrame {
    private OrderController controller;
    private DatabaseManager dbManager;

    public MainFrame() {
        setTitle("Oak Donuts (OD) - Menu & Ordering System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 700);
        setLocationRelativeTo(null);
        setResizable(true);

        // Initialize database and controller
        dbManager = new DatabaseManager();
        controller = new OrderController(dbManager);

        // Create tabbed interface
        JTabbedPane tabbedPane = new JTabbedPane();

        MenuPanel menuPanel = new MenuPanel(controller);
        OrderPanel orderPanel = new OrderPanel(controller);
        OrderHistoryPanel historyPanel = new OrderHistoryPanel(controller);

        tabbedPane.addTab("Menu", menuPanel);
        tabbedPane.addTab("Place Order", orderPanel);
        tabbedPane.addTab("Order History", historyPanel);

        add(tabbedPane);

        // Handle window closing
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                controller.closeDatabase();
                System.exit(0);
            }
        });

        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainFrame());
    }
}
