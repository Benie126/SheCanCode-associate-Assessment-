package com.igirepay.gateway;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class IdempotencyService {
    
    private final ConcurrentHashMap<String, IdempotencyRecord> store;
    private final AuditLogger auditLogger;
    
    public IdempotencyService() {
        this.store = new ConcurrentHashMap<>();
        this.auditLogger = new AuditLogger();
        startCleanupThread();
    }
    
    public PaymentHandler process(String idempotencyKey, PaymentRequest request) {
        String requestBody = request.toCompareString();
        IdempotencyRecord existingRecord = store.get(idempotencyKey);
        
        if (existingRecord != null) {
            if (existingRecord.isProcessing()) {
                auditLogger.log(idempotencyKey, "IN_FLIGHT", "Waiting for original request to complete");
                return waitForCompletion(idempotencyKey, request);
            }
            
            if (!existingRecord.getRequestBody().equals(requestBody)) {
                auditLogger.log(idempotencyKey, "CONFLICT", "Rejected - body mismatch (Fraud detected)");
                throw new IllegalArgumentException(
                    "Idempotency key already used for a different request body"
                );
            }
            
            auditLogger.log(idempotencyKey, "DUPLICATE", "Returned cached response - no double charge");
            return PaymentHandler.success(
                request.getAmount(), request.getCurrency(),
                existingRecord.getTransactionId(), existingRecord.getTimestamp(), true
            );
        } else {
            store.put(idempotencyKey, new IdempotencyRecord(
                requestBody, null, "PROCESSING", LocalDateTime.now()
            ));
            
            try {
                System.out.println("Processing payment for key: " + idempotencyKey);
                Thread.sleep(2000);
                
                String transactionId = "TXN-" + 
                    UUID.randomUUID().toString().substring(0, 8).toUpperCase();
                
                IdempotencyRecord completedRecord = new IdempotencyRecord(
                    requestBody, transactionId, "COMPLETED", LocalDateTime.now()
                );
                store.put(idempotencyKey, completedRecord);
                
                auditLogger.log(idempotencyKey, "FIRST_REQUEST", 
                    "Processed " + request.getAmount() + " " + request.getCurrency());
                
                System.out.println("Payment completed: " + transactionId);
                
                return PaymentHandler.success(
                    request.getAmount(), request.getCurrency(),
                    transactionId, completedRecord.getTimestamp(), false
                );
                
            } catch (InterruptedException e) {
                store.remove(idempotencyKey);
                auditLogger.log(idempotencyKey, "FAILED", "Payment processing interrupted");
                Thread.currentThread().interrupt();
                throw new RuntimeException("Payment processing interrupted", e);
            }
        }
    }
    
    private PaymentHandler waitForCompletion(String idempotencyKey, PaymentRequest request) {
        for (int i = 0; i < 30; i++) {
            try {
                Thread.sleep(1000);
                IdempotencyRecord record = store.get(idempotencyKey);
                
                if (record != null && record.isCompleted()) {
                    auditLogger.log(idempotencyKey, "IN_FLIGHT_COMPLETE", "Returned result from original request");
                    return PaymentHandler.success(
                        request.getAmount(), request.getCurrency(),
                        record.getTransactionId(), record.getTimestamp(), true
                    );
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Waiting interrupted", e);
            }
        }
        throw new RuntimeException("Timeout waiting for payment processing");
    }
    
    private void startCleanupThread() {
        Thread cleanupThread = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(60 * 60 * 1000);
                    LocalDateTime oneDayAgo = LocalDateTime.now().minusHours(24);
                    LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
                    store.entrySet().removeIf(entry -> {
                        IdempotencyRecord record = entry.getValue();
                        return record.getTimestamp().isBefore(oneDayAgo) ||
                               (record.isProcessing() && record.getTimestamp().isBefore(oneHourAgo));
                    });
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
        cleanupThread.setDaemon(true);
        cleanupThread.start();
        System.out.println("Cleanup thread started - will run every hour");
    }
}