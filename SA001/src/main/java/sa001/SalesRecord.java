package sa001;

import java.time.LocalDate;

public class SalesRecord {
    private final int orderId;
    private final LocalDate date;
    private final String product;
    private final String category;
    private final int quantity;
    private final double price;
    private final String region;

    public SalesRecord(int orderId, LocalDate date, String product, String category, int quantity, double price, String region) {
        this.orderId = orderId;
        this.date = date;
        this.product = product;
        this.category = category;
        this.quantity = quantity;
        this.price = price;
        this.region = region;
    }

    // Convenience constructor for tests or older code (no date)
    public SalesRecord(int orderId, String product, String category, int quantity, double price, String region) {
        this(orderId, null, product, category, quantity, price, region);
    }

    public int getOrderId() { return orderId; }
    public LocalDate getDate() { return date; }
    public String getProduct() { return product; }
    public String getCategory() { return category; }
    public int getQuantity() { return quantity; }
    public double getPrice() { return price; }
    public String getRegion() { return region; }

    public double getRevenue() {
        return quantity * price;
    }
}
