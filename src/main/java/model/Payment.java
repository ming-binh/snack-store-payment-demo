package model;
 
import java.math.BigDecimal;
import java.time.LocalDateTime;
 
public class Payment {
    private int paymentId;
    private int orderId;
    private String method;          // cod | bank_transfer | vnpay | momo | zalopay
    private String status;          // pending | success | failed | refunded
    private BigDecimal amount;
    private String transactionRef;
    private String gatewayResponse;
    private LocalDateTime paidAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
 
    // extra join fields for display
    private String orderStatus;
    private String userFullName;
    private String userEmail;
 
    public Payment() {}
 
    public Payment(int orderId, String method, BigDecimal amount) {
        this.orderId = orderId;
        this.method  = method;
        this.amount  = amount;
        this.status  = "pending";
    }
 
    public int getPaymentId()                        { return paymentId; }
    public void setPaymentId(int v)                  { this.paymentId = v; }
    public int getOrderId()                          { return orderId; }
    public void setOrderId(int v)                    { this.orderId = v; }
    public String getMethod()                        { return method; }
    public void setMethod(String v)                  { this.method = v; }
    public String getStatus()                        { return status; }
    public void setStatus(String v)                  { this.status = v; }
    public BigDecimal getAmount()                    { return amount; }
    public void setAmount(BigDecimal v)              { this.amount = v; }
    public String getTransactionRef()                { return transactionRef; }
    public void setTransactionRef(String v)          { this.transactionRef = v; }
    public String getGatewayResponse()               { return gatewayResponse; }
    public void setGatewayResponse(String v)         { this.gatewayResponse = v; }
    public LocalDateTime getPaidAt()                 { return paidAt; }
    public void setPaidAt(LocalDateTime v)           { this.paidAt = v; }
    public LocalDateTime getCreatedAt()              { return createdAt; }
    public void setCreatedAt(LocalDateTime v)        { this.createdAt = v; }
    public LocalDateTime getUpdatedAt()              { return updatedAt; }
    public void setUpdatedAt(LocalDateTime v)        { this.updatedAt = v; }
    public String getOrderStatus()                   { return orderStatus; }
    public void setOrderStatus(String v)             { this.orderStatus = v; }
    public String getUserFullName()                  { return userFullName; }
    public void setUserFullName(String v)            { this.userFullName = v; }
    public String getUserEmail()                     { return userEmail; }
    public void setUserEmail(String v)               { this.userEmail = v; }
}
 