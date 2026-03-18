package model;
 
import java.math.BigDecimal;
import java.time.LocalDateTime;
 
public class Order {
    private int orderId;
    private int userId;
    private int addressId;
    private Integer promotionId;
    private Integer deliveryPartnerId;
    private String status;           // pending | confirmed | shipping | delivered | cancelled
    private BigDecimal subtotal;
    private BigDecimal discountAmount;
    private BigDecimal shippingFee;
    private BigDecimal totalAmount;
    private String note;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
 
    // ── extra display fields (not persisted here, joined from other tables) ─
    private String recipientName;
    private String addressText;
    private String userEmail;
    private String userFullName;
 
    public Order() {}
 
    // Getters & Setters
    public int getOrderId()                          { return orderId; }
    public void setOrderId(int v)                    { this.orderId = v; }
    public int getUserId()                           { return userId; }
    public void setUserId(int v)                     { this.userId = v; }
    public int getAddressId()                        { return addressId; }
    public void setAddressId(int v)                  { this.addressId = v; }
    public Integer getPromotionId()                  { return promotionId; }
    public void setPromotionId(Integer v)            { this.promotionId = v; }
    public Integer getDeliveryPartnerId()            { return deliveryPartnerId; }
    public void setDeliveryPartnerId(Integer v)      { this.deliveryPartnerId = v; }
    public String getStatus()                        { return status; }
    public void setStatus(String v)                  { this.status = v; }
    public BigDecimal getSubtotal()                  { return subtotal; }
    public void setSubtotal(BigDecimal v)            { this.subtotal = v; }
    public BigDecimal getDiscountAmount()            { return discountAmount; }
    public void setDiscountAmount(BigDecimal v)      { this.discountAmount = v; }
    public BigDecimal getShippingFee()               { return shippingFee; }
    public void setShippingFee(BigDecimal v)         { this.shippingFee = v; }
    public BigDecimal getTotalAmount()               { return totalAmount; }
    public void setTotalAmount(BigDecimal v)         { this.totalAmount = v; }
    public String getNote()                          { return note; }
    public void setNote(String v)                    { this.note = v; }
    public LocalDateTime getCreatedAt()              { return createdAt; }
    public void setCreatedAt(LocalDateTime v)        { this.createdAt = v; }
    public LocalDateTime getUpdatedAt()              { return updatedAt; }
    public void setUpdatedAt(LocalDateTime v)        { this.updatedAt = v; }
    public String getRecipientName()                 { return recipientName; }
    public void setRecipientName(String v)           { this.recipientName = v; }
    public String getAddressText()                   { return addressText; }
    public void setAddressText(String v)             { this.addressText = v; }
    public String getUserEmail()                     { return userEmail; }
    public void setUserEmail(String v)               { this.userEmail = v; }
    public String getUserFullName()                  { return userFullName; }
    public void setUserFullName(String v)            { this.userFullName = v; }
}