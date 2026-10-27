from typing import Dict, Optional, List
from models import OrderModel

class OrderStore:
    def __init__(self):
        self._orders: Dict[str, OrderModel] = {}
        self._idempotency_map: Dict[str, OrderModel] = {}

    def get_by_id(self, order_id: str) -> Optional[OrderModel]:
        return self._orders.get(order_id)

    def get_by_idempotency_key(self, key: str) -> Optional[OrderModel]:
        return self._idempotency_map.get(key)

    def save(self, order: OrderModel) -> OrderModel:
        self._orders[order.order_id] = order
        if order.idempotency_key:
            self._idempotency_map[order.idempotency_key] = order
        return order

    def list_orders(self, student_id: Optional[str] = None, status: Optional[str] = None) -> List[OrderModel]:
        results = list(self._orders.values())
        if student_id:
            results = [o for o in results if o.student_id == student_id]
        if status:
            results = [o for o in results if o.status == status]
        return results

    def clear(self):
        self._orders.clear()
        self._idempotency_map.clear()

store = OrderStore()