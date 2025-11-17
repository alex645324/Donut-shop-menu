# Oak Donuts (OD) - Menu & Ordering System

A Java-based GUI application for managing a donut shop menu and processing customer orders with persistent storage using Apache Derby Database.

## Features

### 1. **Menu Management (CRUD Operations)**
- View all available donut items in an interactive table
- Add new menu items (name, description, price, category)
- Update existing menu items
- Delete menu items from the system
- Pre-loaded sample menu with 8 donuts across 3 categories

### 2. **Order Placement**
- Browse menu items by category filter (All, Glaze, Cake, Specialty)
- Add items to shopping cart
- Remove items from cart
- View real-time cart total
- Place orders with automatic transaction ID generation
- Unique transaction IDs in format: `OD-XXXXXXXX`

### 3. **Order History & Management**
- View complete order history with transaction details
- Filter and search orders
- View detailed order information including:
  - Transaction ID
  - Order date and time
  - List of items ordered
  - Total price
  - Order status (pending, completed, cancelled)
- Update order status
- Delete orders
- Persistent storage in Derby Database

### 4. **Database Features**
- **Menu Items Table**: Stores all donut menu items
- **Orders Table**: Stores order headers with transaction ID and date
- **Order Items Table**: Junction table linking orders to items
- Automatic table creation on first run
- Sample data initialization
- Full CRUD support for both menu items and orders

## Project Structure

```
OakDonuts/
├── src/main/java/com/oakdonuts/
│   ├── Main/
│   │   └── MainFrame.java          # Main application window
│   ├── models/
│   │   ├── MenuItem.java           # Menu item data model
│   │   └── Order.java              # Order data model
│   ├── database/
│   │   └── DatabaseManager.java    # Derby database operations
│   ├── controllers/
│   │   └── OrderController.java    # Business logic (MVVM pattern)
│   └── views/
│       ├── MenuPanel.java          # Menu display & CRUD
│       ├── OrderPanel.java         # Order placement & cart
│       └── OrderHistoryPanel.java  # Order history & management
├── lib/
│   ├── derby.jar
│   ├── derbyclient.jar
│   └── derbyshared.jar
├── data/                            # Derby database files
├── capture/                         # Screenshot folder
│   ├── screenshot_1_menu.png
│   ├── screenshot_2_place_order.png
│   └── screenshot_3_order_history.png
├── out/production/OakDonuts/       # Compiled classes
├── README.md
└── .gitignore
```

## Technology Stack

- **Language**: Java
- **GUI Framework**: Swing (JFrame, JPanel, JTable, JTabbedPane)
- **Database**: Apache Derby (Embedded)
- **Pattern**: MVVM (Model-View-ViewModel)
- **JDK Version**: Java 8+

## Installation & Setup

### Prerequisites
- Java JDK 8 or higher installed
- Apache Derby JAR files (included in `/lib` directory)

### Compilation

```bash
cd OakDonuts
javac -d out/production/OakDonuts \
  -cp lib/derby.jar:lib/derbyclient.jar:lib/derbyshared.jar \
  src/main/java/com/oakdonuts/models/*.java \
  src/main/java/com/oakdonuts/database/*.java \
  src/main/java/com/oakdonuts/controllers/*.java \
  src/main/java/com/oakdonuts/views/*.java
```

### Running the Application

```bash
java -cp out/production/OakDonuts:lib/derby.jar:lib/derbyclient.jar:lib/derbyshared.jar \
  com.oakdonuts.views.MainFrame
```

## Database Schema

### MENU_ITEMS Table
```sql
CREATE TABLE MENU_ITEMS (
    ID INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    NAME VARCHAR(100) NOT NULL,
    DESCRIPTION VARCHAR(500),
    PRICE DOUBLE NOT NULL,
    CATEGORY VARCHAR(50) NOT NULL
);
```

### ORDERS Table
```sql
CREATE TABLE ORDERS (
    TRANSACTION_ID VARCHAR(50) PRIMARY KEY,
    ORDER_DATE TIMESTAMP NOT NULL,
    TOTAL_PRICE DOUBLE NOT NULL,
    STATUS VARCHAR(20) DEFAULT 'pending'
);
```

### ORDER_ITEMS Table (Junction Table)
```sql
CREATE TABLE ORDER_ITEMS (
    ID INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    TRANSACTION_ID VARCHAR(50) NOT NULL,
    MENU_ITEM_ID INT NOT NULL,
    FOREIGN KEY (TRANSACTION_ID) REFERENCES ORDERS(TRANSACTION_ID) ON DELETE CASCADE,
    FOREIGN KEY (MENU_ITEM_ID) REFERENCES MENU_ITEMS(ID)
);
```

## Sample Menu Data

The application comes with pre-loaded sample donuts:
1. Classic Glazed - $2.50
2. Chocolate Cake - $3.00
3. Vanilla Frosted - $2.75
4. Strawberry Jam - $3.25
5. Boston Cream - $3.50
6. Maple Glazed - $2.75
7. Chocolate Chip - $3.25
8. Powdered Sugar - $2.50

## Usage Guide

### Menu Tab
1. View all available menu items in table format
2. Select an item to view/edit details
3. Use form fields to add or edit items:
   - Name (required)
   - Description (optional)
   - Price (required, numeric)
   - Category (required: glaze, cake, specialty)
4. Click "Add" to add new item
5. Click "Update" to modify selected item
6. Click "Delete" to remove selected item

### Place Order Tab
1. Select category from dropdown filter
2. Browse available items in the list
3. Click item and press "Add to Cart"
4. View cart contents and total price
5. Remove unwanted items using "Remove Selected"
6. Click "Place Order" to finalize and save to database
7. Receive transaction ID confirmation

### Order History Tab
1. View all orders in chronological order (newest first)
2. Select order to view detailed information
3. Change order status (pending → completed → cancelled)
4. Click "Delete Order" to remove order from system
5. Click "Refresh" to reload order list

## CRUD Operations Summary

### Menu Items
- **Create**: Add new menu items via form
- **Read**: Display all items in table, filter by category
- **Update**: Modify existing menu item properties
- **Delete**: Remove items from menu

### Orders
- **Create**: Place new orders from shopping cart
- **Read**: View order history with details
- **Update**: Change order status
- **Delete**: Remove orders from history

## Key Features Implementation

### Transaction ID Generation
- Format: `OD-` + 8-character UUID substring
- Example: `OD-A1B2C3D4`
- Unique constraint on `TRANSACTION_ID` field

### Date & Time Tracking
- Order date stored as `TIMESTAMP` in Derby
- Formatted display: `yyyy-MM-dd HH:mm:ss`
- Automatic timestamp on order creation

### Cart Management
- In-memory shopping cart (clears after order placement)
- Real-time total calculation
- Support for adding multiple instances of same item

### Database Persistence
- All data persisted in Derby embedded database
- Automatic database initialization on first run
- Tables created if they don't exist
- Sample menu data loaded automatically

## Design Patterns

### MVVM Architecture
- **Model**: MenuItem, Order classes
- **View**: MenuPanel, OrderPanel, OrderHistoryPanel, MainFrame
- **ViewModel**: OrderController handles business logic

### Separation of Concerns
- DatabaseManager: Database operations only
- OrderController: Business logic and data transformation
- GUI Panels: UI rendering and user interaction
- Models: Data representation only

## Notes

- Derby database files stored in `./data/` directory
- Compiled classes in `./out/production/OakDonuts/`
- No external dependencies except Derby
- Cross-platform compatible (Windows, macOS, Linux)

## Future Enhancements

- User authentication and admin roles
- Order tracking with customer information
- Receipt printing functionality
- Sales reports and analytics
- Inventory management
- Multiple store locations support
- Payment processing integration

## Author

Created as part of a Java GUI & Database project using Derby and Swing.

## License

This project is provided as-is for educational purposes.
