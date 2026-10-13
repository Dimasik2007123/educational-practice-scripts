"""Алгоритм 4: конечный поиск свободного кода после подписания акта."""

from datetime import date
from data_utils import require_row, next_id


def find_free_key_code(occupied_codes):
    """Вернуть минимальный свободный код 100000..999999.

    Вход — занятые коды; сведения об объектах и документах не нужны.
    Время O(q log(q+1)), дополнительная память O(q).
    """
    occupied = sorted(occupied_codes)
    code = 100000
    for used in occupied:
        if used == code:
            code += 1
        elif used > code:
            break
    if code > 999999:
        raise ValueError("Свободных шестизначных кодов нет")
    return code


def generate_key_code(data, certificate_id, as_of, keys_type_id=1):
    """Найти минимальный неиспользованный код 100000..999999.

    Акт вводится участником процесса. Генерация кода не означает
    фактическое получение ключей или закрытие сделки.
    """
    date.fromisoformat(as_of)
    certificate = require_row(data["acceptance_certificate"], certificate_id, "Акт")
    if certificate["issue_date"] > as_of:
        raise ValueError("Акт ещё не подписан на указанную дату")
    item = require_row(data["sale_item"], certificate["sale_item_id"], "Позиция сделки")
    sale = require_row(data["sale"], item["sale_id"], "Сделка")
    if sale["sale_date"] > certificate["issue_date"]:
        raise ValueError("Дата акта раньше даты сделки")
    obj = require_row(data["real_estate_object"], item["real_estate_object_id"], "Объект")
    construction = require_row(data["construction_status"], obj["construction_status_id"], "Статус строительства")
    require_row(data["keys_type"], keys_type_id, "Тип ключей")
    if obj["real_estate_status_id"] != 4 or construction["number"] < 5:
        raise ValueError("Объект не продан или ещё не готов к передаче")
    for issued in data["keys"]:
        if issued["real_estate_object_id"] == obj["id"] and issued["keys_type_id"] == keys_type_id:
            raise ValueError("Код для данного типа ключей объекта уже сформирован")
    code = find_free_key_code(key["locker_code"] for key in data["keys"])
    key_id = next_id(data["keys"])
    key_set_number = next_id(data["keys"], "key_set_number")
    line_number = 1
    for position in data["acceptance_certificate_item"]:
        if position["acceptance_certificate_id"] == certificate_id:
            line_number = max(line_number, position["line_number"] + 1)
    key = {
        "id": key_id, "real_estate_object_id": obj["id"],
        "key_set_number": key_set_number,
        "quantity": 6, "issue_date": as_of, "locker_code": code,
        "keys_type_id": keys_type_id,
    }
    data["keys"].append(key)
    data["acceptance_certificate_item"].append({
        "acceptance_certificate_id": certificate_id, "keys_id": key_id,
        "line_number": line_number,
    })
    return dict(key)
