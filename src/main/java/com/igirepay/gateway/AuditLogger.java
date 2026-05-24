package com.igirepay.gateway;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;

/**
 * Developer's Choice Feature: Audit Logging
 * 
 * Logs every idempotency event for compliance, debugging, and dispute resolution.
 * In real-world Fintech, this is required by regulators to prove double charges didn't occur.
 */
public class AuditLogger {
    
    private PrintWriter writer;
    
    public AuditLogger() {
        try {
            writer = new PrintWriter(new FileWriter("audit.log", true));
            System.out.println("Audit logging started - writing to audit.log");
        } catch (IOException e) {
            System.err.println("Could not create audit log: " + e.getMessage());
        }
    }
    
    public void log(String idempotencyKey, String action, String result) {
        String entry = String.format("[%s] Key: %s | Action: %s | Result: %s",
            LocalDateTime.now(), idempotencyKey, action, result);
        System.out.println("AUDIT: " + entry);
        if (writer != null) {
            writer.println(entry);
            writer.flush();
        }
    }
    
    public void close() {
        if (writer != null) {
            writer.close();
        }
    }
}