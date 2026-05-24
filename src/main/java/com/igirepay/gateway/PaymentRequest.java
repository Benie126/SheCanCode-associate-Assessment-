package com.igirepay.gateway;

import java.math.BigDecimal;

public class PaymentRequest {
    
    private BigDecimal amount;
    private String currency;
    
    public PaymentRequest() {}
    
    public PaymentRequest(BigDecimal amount, String currency) {
        this.amount = amount;
        this.currency = currency;
    }
    
    public BigDecimal getAmount() {
        return amount;
    }
    
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
    
    public String getCurrency() {
        return currency;
    }
    
    public void setCurrency(String currency) {
        this.currency = currency;
    }
    
    public String toCompareString() {
        return amount + ":" + currency;
    }
}
