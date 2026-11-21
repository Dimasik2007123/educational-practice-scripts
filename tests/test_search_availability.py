"""Search and booking must agree on availability for the same period."""

import sys
import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(ROOT / "python"))

from book_object import book_object
from data_utils import load_data
from search_objects import search_objects


class SearchAvailabilityTest(unittest.TestCase):
    def setUp(self):
        self.data = load_data(ROOT / "python" / "data.json")
        self.objects = self.data["real_estate_object"]

    def ids(self, criteria):
        return [item["id"] for item in search_objects(self.objects, criteria, self.data)]

    def test_expired_and_active_bookings_and_other_statuses(self):
        self.assertEqual(self.ids({"real_estate_status_id": 2, "available_on": "2026-10-13"}), [1, 3, 6])
        self.assertEqual(self.ids({"real_estate_status_id": 2}), [1, 4, 6])
        self.assertNotIn(2, self.ids({"available_on": "2026-10-24"}))
        self.assertNotIn(2, self.ids({"available_on": "2026-09-27"}))
        self.assertIn(2, self.ids({"available_on": "2026-10-25"}))

    def test_search_reflects_new_booking_and_requires_data(self):
        book_object(self.data, 1, 1, 1, "2026-10-13")
        self.assertNotIn(1, self.ids({"available_on": "2026-10-13"}))
        with self.assertRaises(ValueError):
            search_objects(self.objects, {"available_on": "2026-10-13"})


if __name__ == "__main__":
    unittest.main()
