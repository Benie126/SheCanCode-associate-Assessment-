package com.igirepay.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@SpringBootApplication
@RestController
@RequestMapping("/api/v1")
public class Main {
    
    private final IdempotencyService idempotencyService;
    
    public Main(IdempotencyService idempotencyService) {
        this.idempotencyService = idempotencyService;
    }
    
    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }
    
    @PostMapping("/process-payment")
    public ResponseEntity<?> processPayment(
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @RequestBody PaymentRequest request) {
        
        try {
            PaymentHandler response = idempotencyService.process(idempotencyKey, request);
            
            if (response.isCached()) {
                return ResponseEntity.ok()
                        .header("X-Cache-Hit", "true")
                        .body(response);
            }
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                    .body(PaymentHandler.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(PaymentHandler.error("Internal server error"));
        }
    }
    
    @GetMapping("/health")
    public String health() {
        return "Idempotency Gateway is running";
    }
}
