/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author SONY
 */
import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

class Product {
    private String id;
    private String name;
    private long price;
    private String imageUrl;

    public Product(String id, String name, long price, String imageUrl) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.imageUrl = imageUrl;
    }
    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public long getPrice() { return price; }
    public String getImageUrl() { return imageUrl; }
}

class OrderLine {
    private Product product;
    private int quantity;

    public OrderLine(Product product, int quantity) {
        this.product = product;
        this.quantity = quantity;
    }
    // Getters and Increment
    public Product getProduct() { return product; }
    public int getQuantity() { return quantity; }
    public void incrementQuantity() { this.quantity++; }
}

class OrderTableModel extends AbstractTableModel {
    private String[] columnNames = {"Tên món", "Số lượng", "Đơn giá", "Thành tiền"};
    private List<OrderLine> data;

    public OrderTableModel() {
        this.data = new ArrayList<>();
    }

    public void updateData(List<OrderLine> currentOrder) {
        this.data = new ArrayList<>(currentOrder); // Create copy for safety
        fireTableDataChanged();
    }

    @Override
    public int getRowCount() { return data.size(); }

    @Override
    public int getColumnCount() { return columnNames.length; }

    @Override
    public String getColumnName(int col) { return columnNames[col]; }

    @Override
    public Object getValueAt(int row, int col) {
        OrderLine line = data.get(row);
        switch (col) {
            case 0: return line.getProduct().getName();
            case 1: return line.getQuantity();
            case 2: return String.format("%,d", line.getProduct().getPrice());
            case 3: return String.format("%,d", line.getProduct().getPrice() * line.getQuantity());
            default: return null;
        }
    }
}
