"""Алгоритм 2: бронирование с проверкой пересечений и сохранением истории."""

from datetime import date, timedelta
from data_utils import next_id, require_row, availability_reason


def book_object(data, object_id, client_id, employee_id, as_of):
    """Добавить бронь после проверок; при отказе data не меняется."""
    today = date.fromisoformat(as_of)
    expiration = (today + timedelta(days=14)).isoformat()
    require_row(data["client"], client_id, "Клиент")
    require_row(data["employee"], employee_id, "Сотрудник")
    obj = require_row(data["real_estate_object"], object_id, "Объект")
    reason = availability_reason(data, obj, as_of, expiration)
    if reason is not None:
        raise ValueError(reason)
    booking = {
        "id": next_id(data["booking"]), "booking_date": as_of,
        "expiration_date": expiration, "client_id": client_id,
        "employee_id": employee_id, "real_estate_object_id": object_id,
        "status": "active",
    }
    data["booking"].append(booking)
    obj["real_estate_status_id"] = 3
    return dict(booking)
