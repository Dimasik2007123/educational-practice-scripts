"""Служебные процедуры; все коллекции передаются через параметры."""

import json
from datetime import date
from pathlib import Path


def find_by_id(rows, row_id):
    for row in rows:
        if row["id"] == row_id:
            return row
    return None


def require_row(rows, row_id, title):
    row = find_by_id(rows, row_id)
    if row is None:
        raise ValueError(f"{title} с ID {row_id} не найден")
    return row


def next_id(rows, field="id"):
    maximum = 0
    for row in rows:
        maximum = max(maximum, row[field])
    return maximum + 1


def active_booking(booking, start, end):
    return (
        booking["status"] == "active"
        and booking["booking_date"] <= end
        and start <= booking["expiration_date"]
    )


def availability_reason(data, obj, start, end, owner_id=None):
    """Интервалы замкнуты. Для продажи owner_id разрешает собственную бронь."""
    construction = require_row(
        data["construction_status"],
        obj["construction_status_id"],
        "Статус строительства",
    )
    if not construction["is_available_for_sale"]:
        return "Объект недоступен для продажи по статусу строительства"
    if obj["real_estate_status_id"] not in (2, 3):
        return "Объект не свободен для бронирования или продажи"
    for booking in data["booking"]:
        if (
            booking["real_estate_object_id"] == obj["id"]
            and active_booking(booking, start, end)
        ):
            if owner_id is None or booking["client_id"] != owner_id:
                return "Объект имеет конфликтующую активную бронь"
    return None


def load_data(path):
    data = json.loads(Path(path).read_text(encoding="utf-8"))
    validate_data(data)
    return data


def save_data(data, path):
    Path(path).write_text(
        json.dumps(data, ensure_ascii=False, indent=2) + "\n", encoding="utf-8"
    )


def validate_data(data):
    """Проверить ключи, ссылки, даты и цены при загрузке JSON."""
    entities = (
        "client",
        "employee",
        "position",
        "real_estate_type",
        "real_estate_status",
        "construction_status",
        "contract_type",
        "keys_type",
        "real_estate_object",
        "booking",
        "sale",
        "sale_item",
        "acceptance_certificate",
        "keys",
        "acceptance_certificate_item",
    )

    for entity in entities:
        rows = data.get(entity)
        if not isinstance(rows, list):
            raise ValueError(f"Коллекция {entity} должна быть списком")
        seen = set()
        for row in rows:
            if not isinstance(row, dict):
                raise ValueError(f"Запись {entity} должна быть словарём")
            if entity == "acceptance_certificate_item":
                identity = row["keys_id"]
            else:
                identity = row["id"]
                if (
                    isinstance(identity, bool)
                    or not isinstance(identity, int)
                    or identity <= 0
                ):
                    raise ValueError(f"Некорректный ID в {entity}")
            if identity in seen:
                raise ValueError(f"Повторяющийся ключ в {entity}")
            seen.add(identity)

    links = (
        ("employee", "position_id", "position"),
        ("real_estate_object", "real_estate_type_id", "real_estate_type"),
        ("real_estate_object", "real_estate_status_id", "real_estate_status"),
        (
            "real_estate_object",
            "construction_status_id",
            "construction_status",
        ),
        ("booking", "client_id", "client"),
        ("booking", "employee_id", "employee"),
        ("booking", "real_estate_object_id", "real_estate_object"),
        ("sale", "client_id", "client"),
        ("sale", "employee_id", "employee"),
        ("sale", "contract_type_id", "contract_type"),
        ("sale_item", "sale_id", "sale"),
        ("sale_item", "real_estate_object_id", "real_estate_object"),
        ("acceptance_certificate", "sale_item_id", "sale_item"),
        ("keys", "real_estate_object_id", "real_estate_object"),
        ("keys", "keys_type_id", "keys_type"),
        (
            "acceptance_certificate_item",
            "acceptance_certificate_id",
            "acceptance_certificate",
        ),
        ("acceptance_certificate_item", "keys_id", "keys"),
    )

    for entity, field, parent in links:
        for row in data[entity]:
            require_row(data[parent], row[field], parent)

    for entity, field in (
        ("real_estate_object", "price"),
        ("sale_item", "price"),
    ):
        for row in data[entity]:
            if (
                isinstance(row[field], bool)
                or not isinstance(row[field], int)
                or row[field] <= 0
            ):
                raise ValueError(
                    "Цены задаются положительными целыми числами рублей"
                )

    for entity, field in (
        ("sale_item", "real_estate_object_id"),
        ("acceptance_certificate", "sale_item_id"),
        ("keys", "locker_code"),
        ("keys", "key_set_number"),
    ):
        values = [row[field] for row in data[entity]]
        if len(values) != len(set(values)):
            raise ValueError(f"Повторяющееся значение {entity}.{field}")

    for key in data["keys"]:
        if (
            isinstance(key["locker_code"], bool)
            or not isinstance(key["locker_code"], int)
            or not 100000 <= key["locker_code"] <= 999999
        ):
            raise ValueError("Код постамата должен быть шестизначным числом")

    for booking in data["booking"]:
        if booking["status"] not in ("active", "cancelled", "fulfilled"):
            raise ValueError("Некорректное состояние брони")
        if booking["booking_date"] > booking["expiration_date"]:
            raise ValueError("Дата начала брони позже окончания")

    for entity in entities:
        for row in data[entity]:
            for field in (
                "booking_date",
                "expiration_date",
                "sale_date",
                "issue_date",
                "completion_date",
            ):
                if field in row:
                    value = row[field]
                    if date.fromisoformat(value).isoformat() != value:
                        raise ValueError("Даты задаются в формате YYYY-MM-DD")
