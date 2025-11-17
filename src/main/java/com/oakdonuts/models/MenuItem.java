package com.oakdonuts.models;

// MenuItem - represents a donut product on the menu
public class MenuItem {
    public int id;
    public String name;
    public String description;
    public double price;
    public String category;

    // EMPTY CONSTRUCTOR - used for creating blank MenuItem objects
    public MenuItem() {
    }

    // FULL CONSTRUCTOR - creates MenuItem with all fields including ID
    public MenuItem(int id, String name, String description, double price, String category) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.category = category;
    }

    // CONSTRUCTOR WITHOUT ID - used when creating new items (ID assigned by database)
    public MenuItem(String name, String description, double price, String category) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.category = category;
    }

    // TOSTRING - display item name and price
    public String toString() {
        return name + " - $" + String.format("%.2f", price);
    }
}
