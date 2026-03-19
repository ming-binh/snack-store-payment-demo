package model;
/**
 * Author: HE190438 Thân Bình Minh
 * Created: 2026-03-19
 */
import java.math.BigDecimal;

public class CartItem implements java.io.Serializable {

    private int productId;
    private String productName;
    private BigDecimal unitPrice;
    private int quantity;
    private String thumbnailUrl;

    public CartItem() {
    }

    public CartItem(int productId, String productName, BigDecimal unitPrice, int quantity, String thumbnailUrl) {
        this.productId = productId;
        this.productName = productName;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
        this.thumbnailUrl = thumbnailUrl;
    }

    public BigDecimal getSubtotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int v) {
        this.productId = v;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String v) {
        this.productName = v;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal v) {
        this.unitPrice = v;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int v) {
        this.quantity = v;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String v) {
        this.thumbnailUrl = v;
    }
}
