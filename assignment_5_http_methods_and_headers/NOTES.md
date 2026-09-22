# CS543 Assignment 5: HTTP Methods & Headers

**Team ID:** Team 09 
**Course:** CS543 Web Services  
**Professor:** Dr. Pramit Mazumdar  

### Team Members
- *Name:* **SUVOJIT NAG** | *Roll No:* **20251651094** (Team Leader)
- *Name:* **VANSH PANWAR** | *Roll No:* **20251651101**
- *Name:* **AJEET SHAKYA** | *Roll No:* **20251651010**
- *Name:* **ANKIT** | *Roll No:* **20251651021**
- *Name:* **ANJUL YADAV** | *Roll No:* **20251651020**
---

## Part A & C: Service Modeling & Safe Retries

### A1: CampusEats Method Map
| Action | Method | URL Path |
| :--- | :--- | :--- |
| Create a new order | `POST` | `/orders` |
| List/Filter orders | `GET` | `/orders` |
| Read single order details | `GET` | `/orders/{orderId}` |
| Modify order instructions | `PATCH` | `/orders/{orderId}` |
| Cancel an active order | `POST` | `/orders/{orderId}/cancellation` |
| Delete order record | `DELETE` | `/orders/{orderId}` |
| Query allowed methods | `OPTIONS` | `/orders/{orderId}` |

### C4: The Safe-Retry Plan
| Risky Endpoint | Mechanism | Justification |
| :--- | :--- | :--- |
| `POST /orders` | `Idempotency-Key` header | Not naturally safe or idempotent. A duplicate request without this key would charge the student twice and duplicate food preparation. |
| `PATCH /orders/{orderId}` | `If-Match` header (ETag) | Idempotent, but risky due to the "lost update" problem. `If-Match` prevents two clients from overwriting each other's changes simultaneously. |
| `POST /orders/{orderId}/cancellation` | Internal State Check | Idempotent by domain design. Repeating the cancellation returns a `409 Conflict` if already cancelled, preventing duplicate processing. |

---

## Part D: Headers & Documentation

### A6: One Full HTTP Exchange

**Request:**
```http
POST /orders HTTP/1.1
Host: localhost:5000
Content-Type: application/json
Accept: application/json
Authorization: Bearer mock_token_778
Idempotency-Key: key_12345

{
  "studentId": "20251651094",
  "paymentMethodId": "pm_123",
  "items": [{"itemId": "item_01", "name": "Burger", "quantity": 1, "unitPriceCents": 500}]
}
```

**Response:**

```http
HTTP/1.1 201 CREATED
Content-Type: application/json
Location: /orders/ord_8a7b6c5d
ETag: "d41d8cd98f00b204e9800998ecf8427e"
X-RateLimit-Limit: 100
X-RateLimit-Remaining: 99
Access-Control-Allow-Origin: *
X-Content-Type-Options: nosniff
Strict-Transport-Security: max-age=31536000; includeSubDomains
Date: Tue, 22 Sep 2026 16:13:00 GMT
Server: Werkzeug/2.3.6 Python/3.11.4

{
  "orderId": "ord_8a7b6c5d",
  "status": "PAID"
  ...
}
```

### D2: Headers Table

|**Endpoint**|**Request Headers Needed**|**Response Headers Set**|
|---|---|---|
|`POST /orders`|`Content-Type`, `Accept`, `Authorization`, `Idempotency-Key`|`Location`, `ETag`, Rate-Limits, Security/CORS|
|`GET /orders/{orderId}`|`Accept`, `Authorization`, `If-None-Match`|`ETag`, `Cache-Control`, Rate-Limits, Security/CORS|
|`PATCH /orders/{orderId}`|`Content-Type`, `Accept`, `Authorization`, `If-Match`|`ETag`, Rate-Limits, Security/CORS|
|`OPTIONS /orders/{orderId}`|`Origin`, `Access-Control-Request-Method`|`Allow`, `Access-Control-Allow-Origin`, Security|

## Part D: Questions

### 1. For three of your endpoints, give the method, the success status, and the single response header that matters most — and why.

1. **`POST /orders`**: Success Status `201 Created`. Most important header: `Location`. It informs the client exactly where the newly generated resource resides so they can fetch its real-time state.
    
2. **`GET /orders/{orderId}`**: Success Status `200 OK`. Most important header: `ETag`. It provides a deterministic hash of the resource state, enabling caching (`304 Not Modified`) and safe future updates (`If-Match`).
    
3. **`OPTIONS /orders/{orderId}`**: Success Status `204 No Content`. Most important header: `Allow`. It tells the client exactly which HTTP verbs are permitted on this resource without requiring a destructive trial-and-error approach.
    

### 2. Which of your endpoints are safe, and which are idempotent? Which one is neither, and how did you make it retry-safe?

- **Safe:** `GET /orders`, `GET /orders/{orderId}`, `OPTIONS /orders/{orderId}`. (They alter no state).
    
- **Idempotent:** `DELETE /orders/{orderId}`, `PATCH /orders/{orderId}`, `POST /orders/{orderId}/cancellation`. (Repeating them yields the same system state).
    
- **Neither:** `POST /orders` is neither safe (it mutates state/charges money) nor naturally idempotent. We made it retry-safe by requiring an `Idempotency-Key` header; the server caches the first response and replays it for subsequent requests bearing the same key without re-running the payment logic.

### 3. Show one ETag from your service, the request that returns 304, and the write that returns 412. What does each save or prevent?

- **ETag:** `"a8f5f167f44f4964e6c998dee827110c"`
    
- **Request returning 304:** `GET /orders/ord_123` with header `If-None-Match: "a8f5f167f44f4964e6c998dee827110c"`. This saves **bandwidth and processing time** by preventing the server from transmitting a redundant JSON body when the client already has the latest version.
    
- **Write returning 412:** `PATCH /orders/ord_123` with header `If-Match: "outdated_etag_hash"`. This prevents **the lost update problem (mid-air collisions)**, ensuring that Client B cannot overwrite Client A's changes if the resource was modified since Client B last fetched it.

### 4. You return 422 for one case and 400 for another. Give the exact request that triggers each, and explain the difference.

- **400 Bad Request Trigger:** Sending malformed syntax (e.g., missing quotes around a string or omitting a required field like `"studentId"`).
    
- **422 Unprocessable Entity Trigger:** Sending perfectly formatted JSON with all required fields, but violating domain logic (e.g., `"unitPriceCents": -500`, or a payment failure from the gateway).
    
- **Difference:** `400` means the parser or schema validator rejected the physical shape/syntax of the message. `422` means the syntax is correct, but the business/domain rules refuse to process the semantic meaning of the payload.

### 5. A browser page on another origin calls your API and is blocked — yet your server logs show a 200. Who blocked it, and which response header fixes it?

- **Who blocked it:** The **browser** blocked the response from being handed to the frontend JavaScript. The server successfully processed the request (hence the `200 OK` in the logs), but the browser's Same-Origin Policy (SOP) stepped in during the response phase.
    
- **Fix:** The server must include the `Access-Control-Allow-Origin: *` (or the specific requesting domain) response header to instruct the browser to permit the cross-origin read.

### 6. Name one response where you set Cache-Control to allow caching and one where you must use no-store. Why each?

- **Allow Caching (`Cache-Control: private, max-age=120`):** Used on `GET /orders/{orderId}`. Since order history rarely changes once completed, caching reduces database load. `private` ensures intermediate CDNs don't cache it, only the specific student's local browser.
    
- **No-Store (`Cache-Control: no-store`):** Used on `POST /orders` (checkout/payment). We strictly forbid caching because this response contains sensitive payment transaction IDs and real-time ledger states that must never be written to local disks or proxy caches.

### 7. Your search endpoint is a GET. When would POST be the right choice instead, and what do you give up by switching?

- **When to use POST:** If the search parameters are highly sensitive (e.g., querying by Social Security Number or exact GPS coordinates) or if the query payload is incredibly complex and exceeds the practical length limit of a URL query string (typically ~2000 characters). Query strings are written in plain text to proxy logs and browser histories; POST bodies are not.
    
- **What you give up:** You lose natural HTTP caching. GET requests are natively cached by browsers and CDNs; POST requests are not, breaking idempotency and cacheability semantics.

### 8. Location appears on a 201 and on a 3xx. What does it point to in each case?

- **On a 201 Created:** `Location` points to the URI of the _newly created resource_ (e.g., `/orders/ord_551029481`), telling the client where to find what they just made.
    
- **On a 3xx Redirect (e.g., 301, 302, 308):** `Location` points to the _target URI_ that the client should blindly follow to find the resource that has moved or to complete an auth flow.

---