import unittest
from unittest.mock import patch
from app import app
from store import store

class OrdersServiceTestCase(unittest.TestCase):
    def setUp(self):
        self.client = app.test_client()
        store.clear()

    @patch("app.call_payment_service")
    def test_01_create_order_success(self, mock_payment):
        """Test 1: Create succeeds with 201 Created and Location header."""
        mock_payment.return_value = {"success": True, "transactionId": "tx_mock_12345"}
        
        payload = {
            "studentId": "usr_883920194",
            "paymentMethodId": "pm_card_visa_4242",
            "items": [{"itemId": "item_01", "name": "Burger", "quantity": 2, "unitPriceCents": 500}]
        }
        res = self.client.post("/orders", json=payload)
        self.assertEqual(res.status_code, 201)
        self.assertIn("Location", res.headers)
        self.assertTrue(res.headers["Location"].startswith("/orders/ord_"))
        self.assertEqual(res.json["status"], "PAID")

    @patch("app.call_payment_service")
    def test_02_idempotent_repeat(self, mock_payment):
        """Test 2: Idempotent repeat returns original response with 201."""
        mock_payment.return_value = {"success": True, "transactionId": "tx_mock_12345"}
        
        payload = {
            "studentId": "usr_883920194",
            "paymentMethodId": "pm_card_visa_4242",
            "items": [{"itemId": "item_01", "name": "Burger", "quantity": 1, "unitPriceCents": 1000}]
        }
        headers = {"Idempotency-Key": "unique-key-112233"}
        
        # First Call
        res1 = self.client.post("/orders", json=payload, headers=headers)
        self.assertEqual(res1.status_code, 201)
        order_id_1 = res1.json["orderId"]
        
        # Repeated Call
        res2 = self.client.post("/orders", json=payload, headers=headers)
        self.assertEqual(res2.status_code, 201)
        self.assertEqual(res2.json["orderId"], order_id_1)
        self.assertEqual(res2.headers.get("X-Idempotent-Replay"), "true")
        
        # Payment call executed only once
        self.assertEqual(mock_payment.call_count, 1)

    def test_03_validation_failure_400(self):
        """Test 3: Malformed body triggers 400 Bad Request with Problem Details."""
        payload = {"studentId": "usr_883920194"} # missing items & paymentMethodId
        res = self.client.post("/orders", json=payload)
        self.assertEqual(res.status_code, 400)
        self.assertEqual(res.headers["Content-Type"], "application/problem+json")
        self.assertIn("detail", res.json)

    def test_04_unknown_id_404(self):
        """Test 4: Unknown ID returns 404 Not Found."""
        res = self.client.get("/orders/ord_non_existent_999")
        self.assertEqual(res.status_code, 404)
        self.assertEqual(res.headers["Content-Type"], "application/problem+json")
        self.assertEqual(res.json["title"], "Order Not Found")

    @patch("app.call_payment_service")
    def test_05_state_conflict_409(self, mock_payment):
        """Test 5: Cancelling an already cancelled order returns 409 Conflict."""
        mock_payment.return_value = {"success": True, "transactionId": "tx_mock_12345"}
        
        payload = {
            "studentId": "usr_883920194",
            "paymentMethodId": "pm_card_visa_4242",
            "items": [{"itemId": "item_01", "name": "Burger", "quantity": 1, "unitPriceCents": 1000}]
        }
        res_create = self.client.post("/orders", json=payload)
        order_id = res_create.json["orderId"]
        
        # First Cancellation
        res_cancel_1 = self.client.post(f"/orders/{order_id}/cancellation", json={"reason": "Changed mind"})
        self.assertEqual(res_cancel_1.status_code, 200)
        
        # Second Cancellation (Conflict)
        res_cancel_2 = self.client.post(f"/orders/{order_id}/cancellation", json={"reason": "Duplicate cancel"})
        self.assertEqual(res_cancel_2.status_code, 409)
        self.assertEqual(res_cancel_2.json["title"], "State Conflict")

if __name__ == "__main__":
    unittest.main()