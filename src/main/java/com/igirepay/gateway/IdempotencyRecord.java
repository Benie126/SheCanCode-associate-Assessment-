package com.igirepay.gateway;

import java.time.LocalDateTime;

public class IdempotencyRecord {
    
    private final String requestBody;
    private final String transactionId;
    private final String status;
    private final LocalDateTime timestamp;
    
    public IdempotencyRecord(String requestBody, String transactionId, 
                            String status, LocalDateTime timestamp) {
        this.requestBody = requestBody;
        this.transactionId = transactionId;
        this.status = status;
        this.timestamp = timestamp;
    }
    
    public String getRequestBody() {
        return requestBody;
    }
    
    public String getTransactionId() {
        return transactionId;
    }
    
    public String getStatus() {
        return status;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public boolean isProcessing() {
        return "PROCESSING".equals(status);
    }
    
    public boolean isCompleted() {
        return "COMPLETED".equals(status);
    }
}