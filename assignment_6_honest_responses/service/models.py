from datetime import datetime
from typing import List, Dict, Any, Optional

class OrderItem:
    def __init__(self, item_id: str, name: str, quantity: int, unit_price_cents: int):
        self.item_id = item_id
        self.name = name
        self.quantity = quantity
        self.unit_price_cents = unit_price_cents

    def as_json(self) -> Dict[str, Any]:
        return {
            "itemId": self.item_id,
            "name": self.name,
            "quantity": self.quantity,
            "unitPriceCents": self.unit_price_cents
        }

class OrderModel:
    def __init__(self, order_id: str, student_id: str, items: List[OrderItem], payment_method_id: str, idempotency_key: Optional[str] = None):
        # Private/Internal record field (never exposed to external clients)
        self._internal_db_id = f"db_record_{order_id}_internal"
        self.order_id = order_id
        self.student_id = student_id
        self.items = items
        self.payment_method_id = payment_method_id
        self.status = "CREATED"
        self.total_amount_cents = sum(item.quantity * item.unit_price_cents for item in items)
        self.idempotency_key = idempotency_key
        self.cancellation_reason: Optional[str] = None
        self.created_at = datetime.utcnow().isoformat() + "Z"
        self.payment_transaction_id: Optional[str] = None

    def as_json(self) -> Dict[str, Any]:
        """Task C2: Separates internal store model from external public JSON representation.
        Hides internal credentials and internal store IDs (_internal_db_id)."""
        res = {
            "orderId": self.order_id,
            "studentId": self.student_id,
            "status": self.status,
            "totalAmountCents": self.total_amount_cents,
            "items": [item.as_json() for item in self.items],
            "createdAt": self.created_at
        }
        if self.cancellation_reason:
            res["cancellationReason"] = self.cancellation_reason
        if self.payment_transaction_id:
            res["paymentTransactionId"] = self.payment_transaction_id
        return res