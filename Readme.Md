# Idempotency Gateway | IgirePay Challenge

A production-grade payment middleware built with Spring Boot that guarantees exactly-once transaction processing. Designed to solve the double-charging problem caused by network timeouts in e-commerce payment systems.


  ## Table of Contents
- [Architecture Diagram](#architecture-diagram)
- [Setup Instructions](#setup-instructions)
- [API Documentation](#api-documentation)
- [Design Decisions](#design-decisions)
- [Developer's Choice Feature](#developers-choice-feature)
- [Bonus: Race Condition Handling](#bonus-race-condition-handling)
- [Testing](#testing)
- [Project Structure](#project-structure)  


## Architecture Diagram

![image alt](https://github.com/Benie126/SheCanCode-associate-Assessment-/blob/66612764de59812a0356fbf5ee9eba2f5d63aa55/screenshots/ARCHITECTURE%20DIAGRAM.png)

## Setup Instructions

### Prerequisites

- Java 17 or higher  
- Maven Wrapper (already included — no installation needed)

---

### Quick Start

#### 1. Clone the Repository

```bash
git clone https://github.com/Benie126/SheCanCode-associate-Assessment-.git
cd SheCanCode-associate-Assessment-
```

#### 2. Start the server:

```bash
.\mvnw.cmd spring-boot:run
```

#### 3. Server runs on:

> http://localhost:9090

#### 4. Verify health:

```bash
curl http://localhost:9090/api/v1/health
```

---

## API Documentation

### Base URL
> http://localhost:9090/api/v1


### Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/process-payment` | Process a payment with idempotency |
| `GET` | `/health` | Health check |

---

### POST /process-payment

Processes a payment with idempotency guarantee.

**Headers:**

| Header | Required | Description |
|--------|----------|-------------|
| `Idempotency-Key` | Yes | Unique identifier for the transaction |
| `Content-Type` | Yes | Must be `application/json` |

**Request Body:**
```json
{
  "amount": 100,
  "currency": "RWF"
}

```

### Responses

| Status Code | Scenario | Header | Body |
|-------------|----------|--------|------|
| `201 Created` | First request | - | `{"status":"success","message":"Charged 100.00 RWF","transactionId":"TXN-...","cached":false}` |
| `200 OK` | Duplicate request | `X-Cache-Hit: true` | `{"status":"success","message":"Charged 100.00 RWF","transactionId":"TXN-...","cached":true}` |
| `422 Unprocessable Entity` | Same key, different body | - | `{"status":"error","message":"Idempotency key already used for a different request body","cached":false}` |

### Example Request

**cURL:**
```bash
curl -X POST http://localhost:9090/api/v1/process-payment \
  -H "Idempotency-Key: test-123" \
  -H "Content-Type: application/json" \
  -d '{"amount":100,"currency":"RWF"}'
```

### GET /health

Response:

> Idempotency Gateway is running


---

## Design Decisions

| Decision | Why |
|----------|-----|
| Spring Boot + Java | Industry standard framework, demonstrates enterprise proficiency |
| ConcurrentHashMap | Thread-safe in-memory storage for concurrent request handling |
| PROCESSING status per key | Prevents race conditions when duplicate requests arrive simultaneously |
| 2-second processing delay | Simulates real-world payment gateway latency |
| Request body comparison | Detects key misuse when different payloads use the same idempotency key |
| Synchronous waiting (polling) | Handles in-flight requests without complex locking mechanisms |
| Minimal dependencies | Only `spring-boot-starter-web` required for faster startup and easier review |
| Audit logging to file | Provides compliance trail and debugging capability for production systems |
| Daemon cleanup thread | Background thread removes expired records without blocking the application |
| Graceful shutdown | Ensures in-flight payments complete before server stops |

## User Stories - Test Evidence

### User Story 1: First Transaction 
- **Status:** 201 Created
- **Response:** `{"status":"success","message":"Charged 100.00 RWF","cached":false}`
- **Proof:** New transaction ID generated, payment processed with 2-second delay

![First Transaction](https://github.com/Benie126/SheCanCode-associate-Assessment-/blob/main/screenshots/us1-first-transaction.PNG?raw=true)

### User Story 2: Duplicate Attempt 
- **Status:** 200 OK 
- **Response:** `{"status":"success","message":"Charged 100.00 RWF","cached":true}`
- **Proof:** Same transaction ID returned instantly, `X-Cache-Hit: true`, no double charge

![Duplicate Attempt](screenshots/us2-duplicate-attempt.png)

### User Story 3: Different Body, Same Key 
- **Status:** 422 Unprocessable Entity 
- **Response:** `{"status":"error","message":"Idempotency key already used for a different request body"}`
- **Proof:** Request rejected, no transaction processed, fraud attempt blocked

![Fraud Detection](screenshots/us3-fraud-detection.png)
## Developer's Choice Feature

### Audit Logging for Compliance & Debugging

Every idempotency event is logged to `audit.log` with timestamp, key, action, and result. This provides a complete audit trail for compliance, dispute resolution, and debugging.

### Why This Matters for Fintech

**1. Regulatory Compliance**
- Financial regulators require audit trails of all payment processing events
- Provides proof that duplicate requests were caught and not double-charged
- Each log entry includes timestamp, idempotency key, action taken, and result

**2. Dispute Resolution**
- When customers claim they were charged twice, the audit log proves otherwise
- Clear record of FIRST_REQUEST, DUPLICATE, and CONFLICT events
- Timestamps show the exact sequence of events

**3. Debugging & Monitoring**
- Developers can trace exactly what happened with any idempotency key
- CONFLICT events reveal potential fraud attempts
- IN_FLIGHT events show race condition handling in action

**4. Operational Visibility**
- Real-time console output shows audit events as they happen
- Log file persists for historical analysis
- Zero performance impact on payment processing

### Log Events

| Action | When It Occurs |
|--------|---------------|
| `FIRST_REQUEST` | New payment processed successfully |
| `DUPLICATE` | Cached response returned - no double charge |
| `CONFLICT` | Fraud attempt blocked - same key, different body |
| `IN_FLIGHT` | Concurrent request waiting for original to complete |
| `IN_FLIGHT_COMPLETE` | Original request finished, result returned to waiting request |
| `FAILED` | Payment processing interrupted or failed |

### Proof of Audit Logging

```bash
PS C:\Users\PROBYTE\Downloads\SheCanCode-associate-Assessment-> type audit.log
[2026-05-24T17:02:44] Key: audit-test | Action: FIRST_REQUEST | Result: Processed 100 RWF
[2026-05-24T17:02:57] Key: audit-test | Action: DUPLICATE | Result: Returned cached response - no double charge

```

### Startup Confirmation

When the server starts, the console displays:

Audit logging started - writing to audit.log




