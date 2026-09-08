import os
import uuid
import time
import random
import requests
from typing import Optional
from flask import Flask, request, jsonify, Response
from models import OrderModel, OrderItem
from store import store
from errors import problem

app = Flask(__name__)

# Task D1: Dependency address resolved strictly from environment variable
PAYMENTS_SERVICE_URL = os.getenv("PAYMENTS_SERVICE_URL", "http://localhost:5001/payments")

# Task C4: Explicit Request Body Validators
def validate_order_request(data: dict) -> Optional[str]:
    if not isinstance(data, dict):
        return "Request body must be a JSON object."
    if "studentId" not in data or not isinstance(data["studentId"], str) or not data["studentId"].strip():
        return "Field 'studentId' is required and must be a non-empty string."
    if "paymentMethodId" not in data or not isinstance(data["paymentMethodId"], str) or not data["paymentMethodId"].strip():
        return "Field 'paymentMethodId' is required and must be a non-empty string."
    if "items" not in data or not isinstance(data["items"], list) or len(data["items"]) == 0:
        return "Field 'items' must be a non-empty array of items."
    
    for idx, item in enumerate(data["items"]):
        if not isinstance(item, dict):
            return f"Item at index {idx} must be an object."
        if "itemId" not in item or not isinstance(item["itemId"], str):
            return f"Item at index {idx} requires a string 'itemId'."
        if "name" not in item or not isinstance(item["name"], str):
            return f"Item at index {idx} requires a string 'name'."
        if "quantity" not in item or not isinstance(item["quantity"], int) or item["quantity"] <= 0:
            return f"Item at index {idx} requires a positive integer 'quantity'."
        if "unitPriceCents" not in item or not isinstance(item["unitPriceCents"], int) or item["unitPriceCents"] < 0:
            return f"Item at index {idx} requires a non-negative integer 'unitPriceCents'."
    
    return None

def validate_cancellation_request(data: dict) -> Optional[str]:
    if not isinstance(data, dict):
        return "Request body must be a JSON object."
    if "reason" not in data or not isinstance(data["reason"], str) or not data["reason"].strip():
        return "Field 'reason' is required and must be a non-empty string."
    return None

# Task D2: Hardened Outbound Call (Timeout, Exponential Backoff, Jitter, Idempotency Header)
def call_payment_service(order_id: str, amount_cents: int, payment_method_id: str, idempotency_key: str) -> dict:
    headers = {
        "Content-Type": "application/json",
        "Idempotency-Key": f"pay_ord_{idempotency_key}"
    }
    payload = {
        "orderId": order_id,
        "amountCents": amount_cents,
        "paymentMethodId": payment_method_id
    }
    
    max_retries = 3
    base_delay = 0.1
    
    for attempt in range(max_retries):
        try:
            # 3.0 second timeout enforcement
            response = requests.post(PAYMENTS_SERVICE_URL, json=payload, headers=headers, timeout=3.0)
            
            # Rule: 4xx Client Errors MUST NEVER be retried
            if 400 <= response.status_code < 500:
                return {"success": False, "status_code": response.status_code, "error": "Payment method declined by banking gateway."}
            
            if response.status_code in (200, 201):
                data = response.json()
                return {"success": True, "transactionId": data.get("transactionId", f"tx_{uuid.uuid4().hex[:8]}")}
                
        except (requests.exceptions.Timeout, requests.exceptions.ConnectionError) as e:
            if attempt == max_retries - 1:
                raise e
        
        # Exponential backoff with jitter
        sleep_time = base_delay * (2 ** attempt) + random.uniform(0, 0.05)
        time.sleep(sleep_time)
        
    raise requests.exceptions.ConnectionError("Payment dependency unreachable after maximum retries.")

# Endpoint 1: Create Order (Task C3, C5, C7)
@app.route("/orders", methods=["POST"])
def create_order():
    idempotency_key = request.headers.get("Idempotency-Key")
    
    # Task C7: Idempotency Key Handling
    if idempotency_key:
        existing_order = store.get_by_idempotency_key(idempotency_key)
        if existing_order:
            resp = jsonify(existing_order.as_json())
            resp.status_code = 201
            resp.headers["Location"] = f"/orders/{existing_order.order_id}"
            resp.headers["X-Idempotent-Replay"] = "true"
            return resp

    data = request.get_json(silent=True)
    if data is None:
        return problem(400, "Malformed JSON", "The request body could not be parsed as valid JSON.")
    
    # Task C4: Input Validation
    validation_error = validate_order_request(data)
    if validation_error:
        return problem(400, "Invalid Request Body", validation_error)

    items = [OrderItem(i["itemId"], i["name"], i["quantity"], i["unitPriceCents"]) for i in data["items"]]
    total_amount = sum(i.quantity * i.unit_price_cents for i in items)
    if total_amount <= 0:
        return problem(422, "Unprocessable Entity", "Order total amount must be greater than zero.")

    order_id = f"ord_{uuid.uuid4().hex[:8]}"
    order = OrderModel(
        order_id=order_id,
        student_id=data["studentId"],
        items=items,
        payment_method_id=data["paymentMethodId"],
        idempotency_key=idempotency_key
    )

    # Task D1 & D2: Outbound call execution
    try:
        pay_res = call_payment_service(
            order_id=order.order_id,
            amount_cents=order.total_amount_cents,
            payment_method_id=order.payment_method_id,
            idempotency_key=idempotency_key or order_id
        )
        if pay_res["success"]:
            order.status = "PAID"
            order.payment_transaction_id = pay_res["transactionId"]
        else:
            order.status = "FAILED"
            store.save(order)
            return problem(422, "Payment Processing Failed", pay_res.get("error", "Payment rejected."))
    except Exception as e:
        order.status = "FAILED"
        store.save(order)
        return problem(502, "Bad Gateway", f"Payment service dependency unavailable: {str(e)}")

    store.save(order)
    
    resp = jsonify(order.as_json())
    resp.status_code = 201
    resp.headers["Location"] = f"/orders/{order.order_id}"
    return resp

# Endpoint 2: Read Single Order (Task C3)
@app.route("/orders/<order_id>", methods=["GET"])
def get_order(order_id):
    order = store.get_by_id(order_id)
    if not order:
        return problem(404, "Order Not Found", f"No order found with ID '{order_id}'.")
    return jsonify(order.as_json()), 200

# Endpoint 3: Filtered List of Orders (Task C3)
@app.route("/orders", methods=["GET"])
def list_orders():
    student_id = request.args.get("student_id")
    status = request.args.get("status")
    
    orders = store.list_orders(student_id=student_id, status=status)
    return jsonify({"orders": [o.as_json() for o in orders], "count": len(orders)}), 200

# Endpoint 4: State-Changing Sub-Resource Cancellation (Task C3, C5)
@app.route("/orders/<order_id>/cancellation", methods=["POST"])
def cancel_order(order_id):
    order = store.get_by_id(order_id)
    if not order:
        return problem(404, "Order Not Found", f"No order found with ID '{order_id}'.")

    data = request.get_json(silent=True)
    if data is None:
        return problem(400, "Malformed JSON", "The request body could not be parsed as valid JSON.")

    val_err = validate_cancellation_request(data)
    if val_err:
        return problem(400, "Invalid Request Body", val_err)

    if order.status == "CANCELLED":
        return problem(409, "State Conflict", f"Order '{order_id}' is already cancelled.")
    
    if order.status == "COMPLETED":
        return problem(409, "State Conflict", f"Order '{order_id}' cannot be cancelled because it has already been completed.")

    order.status = "CANCELLED"
    order.cancellation_reason = data["reason"]
    store.save(order)

    return jsonify({
        "message": "Order successfully cancelled",
        "orderId": order_id,
        "status": order.status,
        "cancellationReason": order.cancellation_reason
    }), 200

if __name__ == "__main__":
    app.run(host="0.0.0.0", port=5000, debug=True)