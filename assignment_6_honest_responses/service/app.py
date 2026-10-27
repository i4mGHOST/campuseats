import os
import uuid
import time
import random
import requests
from typing import Optional, List, Dict, Any
from flask import Flask, request, jsonify, Response
from models import OrderModel, OrderItem
from store import store
from errors import problem

# Try importing dicttoxml for XML content negotiation
try:
    import dicttoxml
except ImportError:
    dicttoxml = None

app = Flask(__name__)

PAYMENTS_SERVICE_URL = os.getenv("PAYMENTS_SERVICE_URL", "http://localhost:5001/payments")

# Task C2 & B3: Collect ALL field-level validation errors instead of stopping at the first one
def validate_order_request(data: dict) -> List[Dict[str, str]]:
    errors = []
    if not isinstance(data, dict):
        return [{"field": "body", "reason": "Request body must be a JSON object."}]
    
    if "studentId" not in data or not isinstance(data["studentId"], str) or not data["studentId"].strip():
        errors.append({"field": "studentId", "reason": "Field 'studentId' is required and must be a non-empty string."})
    
    if "paymentMethodId" not in data or not isinstance(data["paymentMethodId"], str) or not data["paymentMethodId"].strip():
        errors.append({"field": "paymentMethodId", "reason": "Field 'paymentMethodId' is required and must be a non-empty string."})
    
    items = data.get("items")
    if not items or not isinstance(items, list) or len(items) == 0:
        errors.append({"field": "items", "reason": "Items list must be non-empty (empty-cart)."})
    else:
        for idx, item in enumerate(items):
            if not isinstance(item, dict):
                errors.append({"field": f"items[{idx}]", "reason": f"Item at index {idx} must be an object."})
                continue
            if "itemId" not in item or not isinstance(item["itemId"], str):
                errors.append({"field": f"items[{idx}].itemId", "reason": "Item requires a string 'itemId'."})
            if "quantity" not in item or not isinstance(item["quantity"], int) or item["quantity"] < 1:
                errors.append({"field": f"items[{idx}].quantity", "reason": "Quantity must be an integer greater than or equal to 1."})
            if "unitPriceCents" not in item or not isinstance(item["unitPriceCents"], int) or item["unitPriceCents"] < 0:
                errors.append({"field": f"items[{idx}].unitPriceCents", "reason": "Unit price must be a non-negative integer."})
    
    return errors

def validate_cancellation_request(data: dict) -> Optional[str]:
    if not isinstance(data, dict):
        return "Request body must be a JSON object."
    if "reason" not in data or not isinstance(data["reason"], str) or not data["reason"].strip():
        return "Field 'reason' is required and must be a non-empty string."
    return None

# Task C3 & C4: Content Negotiation Helper
def format_response(data_dict: Dict[str, Any], status_code: int, root_name: str = "Order") -> Response:
    accept = request.headers.get("Accept", "application/json")
    
    if "*/*" in accept or "application/json" in accept or accept == "":
        resp = jsonify(data_dict)
        resp.status_code = status_code
        resp.headers["Content-Type"] = "application/json; charset=utf-8"
        return resp
    elif "application/xml" in accept or "text/xml" in accept:
        if dicttoxml is not None:
            xml_data = dicttoxml.dicttoxml(data_dict, custom_root=root_name, attr_type=False)
            resp = Response(xml_data, status=status_code, mimetype="application/xml")
            resp.headers["Content-Type"] = "application/xml; charset=utf-8"
            return resp
        else:
            resp = jsonify(data_dict)
            resp.status_code = status_code
            resp.headers["Content-Type"] = "application/json; charset=utf-8"
            return resp
    else:
        # Task C3: Return 406 Not Acceptable if requested media type cannot be produced
        return problem(406, "Not Acceptable", "Requested media type not supported. Use application/json or application/xml.", "https://api.campuseats.com/errors/not-acceptable")

# Task D2 & A4: Outbound call with retry safety and Retry-After handling
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
            response = requests.post(PAYMENTS_SERVICE_URL, json=payload, headers=headers, timeout=3.0)
            
            # Rule: 4xx Client Errors MUST NEVER be retried
            if 400 <= response.status_code < 500:
                return {"success": False, "status_code": response.status_code, "error": "payment-declined"}
            
            if response.status_code in (200, 201):
                data = response.json()
                return {"success": True, "transactionId": data.get("transactionId", f"tx_{uuid.uuid4().hex[:8]}")}
                
        except (requests.exceptions.Timeout, requests.exceptions.ConnectionError) as e:
            if attempt == max_retries - 1:
                raise e
        
        sleep_time = base_delay * (2 ** attempt) + random.uniform(0, 0.05)
        time.sleep(sleep_time)
        
    raise requests.exceptions.ConnectionError("Payment dependency unreachable after maximum retries.")

# Endpoint 1: Create Order
@app.route("/orders", methods=["POST"])
def create_order():
    idempotency_key = request.headers.get("Idempotency-Key")
    
    if idempotency_key:
        existing_order = store.get_by_idempotency_key(idempotency_key)
        if existing_order:
            resp = format_response(existing_order.as_json(), 201, root_name="Order")
            if isinstance(resp, Response) or hasattr(resp, 'headers'):
                resp.headers["Location"] = f"/orders/{existing_order.order_id}"
                resp.headers["X-Idempotent-Replay"] = "true"
            return resp

    data = request.get_json(silent=True)
    if data is None:
        return problem(400, "Malformed JSON", "The request body could not be parsed as valid JSON.", "https://api.campuseats.com/errors/malformed-json")
    
    # Task C2 & B3: Validate all fields and return 422 with errors array if invalid
    validation_errors = validate_order_request(data)
    if validation_errors:
        return problem(
            status=422,
            title="Unprocessable Entity",
            detail="Multiple request fields failed validation checks.",
            error_type="https://api.campuseats.com/errors/validation-error",
            errors=validation_errors
        )

    items = [OrderItem(i["itemId"], i["name"], i["quantity"], i["unitPriceCents"]) for i in data["items"]]
    total_amount = sum(i.quantity * i.unit_price_cents for i in items)
    if total_amount <= 0:
        return problem(422, "Unprocessable Entity", "Order total amount must be greater than zero.", "https://api.campuseats.com/errors/empty-cart")

    order_id = f"ord_{uuid.uuid4().hex[:8]}"
    order = OrderModel(
        order_id=order_id,
        student_id=data["studentId"],
        items=items,
        payment_method_id=data["paymentMethodId"],
        idempotency_key=idempotency_key
    )

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
            return problem(422, "Payment Declined", "Payment rejected by the gateway.", "https://api.campuseats.com/errors/payment-declined")
    except Exception as e:
        order.status = "FAILED"
        store.save(order)
        # Task A4: Include Retry-After on 503 Service Unavailable / Gateway failure
        err_resp = problem(503, "Service Unavailable", f"Payment service dependency unavailable: {str(e)}", "https://api.campuseats.com/errors/service-unavailable")
        err_resp.headers["Retry-After"] = "30"
        return err_resp

    store.save(order)
    
    resp = format_response(order.as_json(), 201, root_name="Order")
    if hasattr(resp, 'headers'):
        resp.headers["Location"] = f"/orders/{order.order_id}"
    return resp

# Endpoint 2: Read Single Order
@app.route("/orders/<order_id>", methods=["GET"])
def get_order(order_id):
    order = store.get_by_id(order_id)
    if not order:
        return problem(404, "Order Not Found", f"No order found with ID '{order_id}'.", "https://api.campuseats.com/errors/order-not-found")
    return format_response(order.as_json(), 200, root_name="Order")

# Endpoint 3: Filtered List of Orders
@app.route("/orders", methods=["GET"])
def list_orders():
    student_id = request.args.get("student_id")
    status = request.args.get("status")
    
    orders = store.list_orders(student_id=student_id, status=status)
    return format_response({"orders": [o.as_json() for o in orders], "count": len(orders)}, 200, root_name="OrdersList")

# Endpoint 4: State-Changing Sub-Resource Cancellation
@app.route("/orders/<order_id>/cancellation", methods=["POST"])
def cancel_order(order_id):
    order = store.get_by_id(order_id)
    if not order:
        return problem(404, "Order Not Found", f"No order found with ID '{order_id}'.", "https://api.campuseats.com/errors/order-not-found")

    data = request.get_json(silent=True)
    if data is None:
        return problem(400, "Malformed JSON", "The request body could not be parsed as valid JSON.", "https://api.campuseats.com/errors/malformed-json")

    val_err = validate_cancellation_request(data)
    if val_err:
        return problem(400, "Invalid Request Body", val_err, "https://api.campuseats.com/errors/validation-error")

    if order.status == "CANCELLED":
        return problem(409, "State Conflict", f"Order '{order_id}' is already cancelled.", "https://api.campuseats.com/errors/illegal-transition")
    
    if order.status == "COMPLETED":
        return problem(409, "State Conflict", f"Order '{order_id}' cannot be cancelled because it has already been completed.", "https://api.campuseats.com/errors/illegal-transition")

    order.status = "CANCELLED"
    order.cancellation_reason = data["reason"]
    store.save(order)

    return format_response({
        "message": "Order successfully cancelled",
        "orderId": order_id,
        "status": order.status,
        "cancellationReason": order.cancellation_reason
    }, 200, root_name="OrderCancellation")

if __name__ == "__main__":
    app.run(host="0.0.0.0", port=5000, debug=True)