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

### Responses

| Status Code | Scenario | Header | Body |
|-------------|----------|--------|------|
| `201 Created` | First request | - | `{"status":"success","message":"Charged 100.00 RWF","transactionId":"TXN-...","cached":false}` |
| `200 OK` | Duplicate request | `X-Cache-Hit: true` | `{"status":"success","message":"Charged 100.00 RWF","transactionId":"TXN-...","cached":true}` |
| `422 Unprocessable Entity` | Same key, different body | - | `{"status":"error","message":"Idempotency key already used for a different request body","cached":false}` |

### Example:

```bash
curl -X POST http://localhost:9090/api/v1/process-payment \
  -H "Idempotency-Key: test-123" \
  -H "Content-Type: application/json" \
  -d '{"amount":100,"currency":"RWF"}'
```
### GET /health

Response:

Idempotency Gateway is running


---




