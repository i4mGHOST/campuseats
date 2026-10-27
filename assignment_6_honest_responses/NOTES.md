# CS543 Assignment 6: Honest responses: status codes, errors & validation

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

## 1. Your status map: every endpoint, its success status, and the status for each failure with one line on why each

* **POST /orders** 
	* **Success:** `201 Created` – Order successfully created and registered in the system. 
	* **Failures:** 
		* `400 Bad Request` – Malformed JSON syntax or missing request body. 
		* `422 Unprocessable Entity` – Request failed semantic field validation (e.g., empty items or invalid quantity)
		* `409 Conflict` – Duplicate order detected via idempotency key collision

* **GET /orders/{id}**
	* **Success:** `200 OK` – Order retrieved successfully. 
	* **Failures:** 
		* `404 Not Found` – The requested order ID does not exist in the database.

* **PATCH /orders/{id}/status** 
	* **Success:** `200 OK` – Order lifecycle state updated successfully.
	* **Failures:** 
		* `404 Not Found` – Order ID could not be found
		* `409 Conflict` (`illegal-transition`) – Attempting an invalid state jump (e.g., changing from `DELIVERED` back to `PENDING`).

* **DELETE /orders/{id}** 
	* **Success:** `204 No Content` – Order successfully canceled/deleted. 
	* **Failures:** 
		* `404 Not Found` – Order ID does not exist.
		* `409 Conflict` – Cannot delete an order that is already out for delivery or completed. 

--- 
## 2. Your type catalogue: the stable string for each failure and what triggers it 

* `empty-cart`: Triggered when a `POST /orders` request is submitted with an empty items array (`[]`). 
* `item-unavailable`: Triggered when an ordered menu item is out of stock or marked inactive in the catalog service. 
* `payment-declined`: Triggered when the external SOAP payment gateway rejects the transaction due to insufficient funds or card errors. 
* `order-not-found`: Triggered when a `GET`, `PATCH`, or `DELETE` request references a non-existent order identifier. 
* `illegal-transition`: Triggered when a `PATCH` status update violates the allowed order lifecycle state machine rules.

--- 
## 3. Paste one Problem Details body from your service and label its fields .
```json
{
"type":"[https://api.campuseats.com/errors/empty-cart](https://api.campuseats.com/errors/empty-cart)",
"title": "Cart Cannot Be Empty",
"status": 422,
 "detail": "The orders payload must contain at least one valid menu item."
 }
```
 
 ---
 

## 4. Show a field-level 422 that reports two bad fields at once

```json
{ 
	"type": "[https://api.campuseats.com/errors/validation-error](https://api.campuseats.com/errors/validation-error)", 
	"title": "Unprocessable Entity", 
	"status": 422, 
	"detail": "Multiple request fields failed validation checks.", "errors": [ 
			{ 
				"field": "items[0].quantity", 
				"reason": "Quantity must be an integer greater than or equal to 1." 
			}, 
			{ 
				"field": "deliveryAddress", 
				"reason": "Delivery address string is required and cannot be blank." 
			}
		] 
}
```

--- 

## 5. Which endpoints are safe to retry, and which status codes tell a client so?

- **Safe to retry:** Idempotent operations and safe read methods, specifically `GET /orders/{id}`. `POST /orders` is safe to retry _only_ if the client supplies an explicit `Idempotency-Key` header to prevent duplicate order generation.
    
- **Indicating status codes:**
    - `429 Too Many Requests` (accompanied by a `Retry-After` header telling the client how many seconds to wait).
    - `503 Service Unavailable` (indicating temporary downstream overload or maintenance, also paired with `Retry-After`).

---

## 6. Where did you map an internal error to a clean client error, and what did you hide from the caller?

- **Implementation location:** Wrapped inside the database persistence layer handlers and the external SOAP payment gateway integration wrappers in `app.py`.
    
- **What was hidden:** Raw PostgreSQL connection drop exceptions, SQL syntax violation strings, internal Python tracebacks, and raw XML fault strings returned by the external SOAP payment provider. These are logged securely server-side using Python's logging module while returning a clean, mapped `500 Internal Server Error` Problem Details response to the client.

---

## 7. Show the same order as JSON and as XML, and the Accept header that chose each.

**JSON Representation** (Triggered by `Accept: application/json` or omitting the header)

```json
{ 
	"id": "ord_101", 
	"status": "PENDING", 
	"total": 15.50 
}
```

**XML Representation** (Triggered by `Accept: application/xml`)

```xml
<Order> 
	<id>ord_101</id> 
	<status>PENDING</status> 
	<total>15.50</total> 
</Order>
```

---