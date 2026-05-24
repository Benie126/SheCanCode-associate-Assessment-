package com.igirepay.gateway;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PaymentHandler {
    
    private final String status;
    private final String message;
    private final String transactionId;
    private final LocalDateTime timestamp;
    private final boolean cached;
    
    private PaymentHandler(String status, String message, String transactionId, 
                          LocalDateTime timestamp, boolean cached) {
        this.status = status;
        this.message = message;
        this.transactionId = transactionId;
        this.timestamp = timestamp;
        this.cached = cached;
    }
    
    public static PaymentHandler success(BigDecimal amount, String currency, 
                                        String transactionId, LocalDateTime timestamp, 
                                        boolean cached) {
        return new PaymentHandler(
            "success",
            String.format("Charged %.2f %s", amount, currency),
            transactionId, timestamp, cached
        );
    }
    
    public static PaymentHandler error(String errorMessage) {
        return new PaymentHandler("error", errorMessage, null, LocalDateTime.now(), false);
    }
    
    public String getStatus() { return status; }
    public String getMessage() { return message; }
    public String getTransactionId() { return transactionId; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public boolean isCached() { return cached; }
}
