"""Алгоритм 3: проверка комплекта объектов и оформление сделки."""

from datetime import date

from data_utils import (
    require_row,
    next_id,
    availability_reason,
    active_booking,
)


def create_sale(
    data, employee_id, client_id, contract_type_id, items, as_of, notes=None
):
    """items — пары (object_id, price); цены в целых рублях.

    Сначала проверяется весь комплект. Собственная бронь разрешена,
    чужая — запрещена. При отказе данные остаются неизменными.
    """
    date.fromisoformat(as_of)
    require_row(data["client"], client_id, "Клиент")
    require_row(data["employee"], employee_id, "Сотрудник")
    require_row(data["contract_type"], contract_type_id, "Тип договора")
    if not items:
        raise ValueError("Сделка должна содержать хотя бы один объект")

    checked = []
    for object_id, price in items:
        if isinstance(price, bool) or not isinstance(price, int) or price <= 0:
            raise ValueError(
                "Цена должна быть положительным целым числом рублей"
            )

        for previous, _ in checked:
            if previous["id"] == object_id:
                raise ValueError("Объект повторяется в сделке")

        obj = require_row(data["real_estate_object"], object_id, "Объект")
        reason = availability_reason(data, obj, as_of, "9999-12-31", client_id)
        if reason is not None:
            raise ValueError(reason)

        for existing_item in data["sale_item"]:
            if existing_item["real_estate_object_id"] == object_id:
                raise ValueError("Объект уже присутствует в продаже")
        checked.append((obj, price))

    sale_id = next_id(data["sale"])
    item_id = next_id(data["sale_item"])
    sale = {
        "id": sale_id,
        "sale_date": as_of,
        "employee_id": employee_id,
        "client_id": client_id,
        "contract_type_id": contract_type_id,
        "notes": notes,
    }
    data["sale"].append(sale)

    for line_number, (obj, price) in enumerate(checked, start=1):
        data["sale_item"].append(
            {
                "id": item_id,
                "sale_id": sale_id,
                "real_estate_object_id": obj["id"],
                "price": price,
                "line_number": line_number,
            }
        )
        item_id += 1
        obj["real_estate_status_id"] = 4
        for booking in data["booking"]:
            if (
                booking["real_estate_object_id"] == obj["id"]
                and active_booking(booking, as_of, "9999-12-31")
            ):
                booking["status"] = "fulfilled"

    return dict(sale)
