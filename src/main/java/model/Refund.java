package model;
 
import java.math.BigDecimal;
import java.time.LocalDateTime;
 
public class Refund {
    private int refundId;
    private int paymentId;
    private BigDecimal amount;
    private String reason;
    private String status;           // pending | approved | rejected | completed
    private Integer processedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
 
    // join
    private int orderId;
    private String userFullName;
    private String processedByName;
 
    public Refund() {}
 
    public Refund(int paymentId, BigDecimal amount, String reason) {
        this.paymentId = paymentId;
        this.amount    = amount;
        this.reason    = reason;
        this.status    = "pending";
    }
 
    public int getRefundId()                         { return refundId; }
    public void setRefundId(int v)                   { this.refundId = v; }
    public int getPaymentId()                        { return paymentId; }
    public void setPaymentId(int v)                  { this.paymentId = v; }
    public BigDecimal getAmount()                    { return amount; }
    public void setAmount(BigDecimal v)              { this.amount = v; }
    public String getReason()                        { return reason; }
    public void setReason(String v)                  { this.reason = v; }
    public String getStatus()                        { return status; }
    public void setStatus(String v)                  { this.status = v; }
    public Integer getProcessedBy()                  { return processedBy; }
    public void setProcessedBy(Integer v)            { this.processedBy = v; }
    public LocalDateTime getCreatedAt()              { return createdAt; }
    public void setCreatedAt(LocalDateTime v)        { this.createdAt = v; }
    public LocalDateTime getUpdatedAt()              { return updatedAt; }
    public void setUpdatedAt(LocalDateTime v)        { this.updatedAt = v; }
    public int getOrderId()                          { return orderId; }
    public void setOrderId(int v)                    { this.orderId = v; }
    public String getUserFullName()                  { return userFullName; }
    public void setUserFullName(String v)            { this.userFullName = v; }
    public String getProcessedByName()               { return processedByName; }
    public void setProcessedByName(String v)         { this.processedByName = v; }
}
 