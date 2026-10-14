"""Консольная демонстрация пяти операций КТ2, без БД и внешних библиотек."""

import argparse
from datetime import date
from pathlib import Path

from data_utils import load_data, save_data, validate_data, next_id
from search_objects import search_objects
from book_object import book_object
from create_sale import create_sale
from generate_key_code import generate_key_code
from generate_sales_report import generate_sales_report


def run_demo(data, as_of):
    candidates = search_objects(
        data["real_estate_object"],
        {
            "city": "Москва",
            "min_price": 7000000,
            "max_price": 8500000,
        },
    )
    print("1. Подобрать объекты по требованиям клиента")
    print("Найдены объекты:", ", ".join(str(obj["id"]) for obj in candidates))

    booking = book_object(data, 1, 1, 1, as_of)
    print("2. Забронировать объект")
    print(f"Бронь {booking['id']}: объект 1, до {booking['expiration_date']}")
    try:
        book_object(data, 1, 2, 1, as_of)
    except ValueError as error:
        print("Повторная бронь отклонена:", error)

    sale = create_sale(
        data,
        1,
        1,
        1,
        [(1, 8000000), (3, 7000000)],
        as_of,
        "Продажа двух объектов",
    )
    print("3. Оформить сделку")
    print(
        f"Сделка {sale['id']}: объекты 1 и 3, "
        "собственная бронь сохранена как fulfilled"
    )

    certificate_id = next_id(data["acceptance_certificate"])
    new_item = next(
        item
        for item in data["sale_item"]
        if item["sale_id"] == sale["id"] and item["real_estate_object_id"] == 1
    )
    data["acceptance_certificate"].append(
        {
            "id": certificate_id,
            "issue_date": as_of,
            "sale_item_id": new_item["id"],
        }
    )

    key = generate_key_code(data, certificate_id, as_of)
    print("4. Сформировать код для получения ключей")
    print(f"Код: {key['locker_code']}")

    report = generate_sales_report(
        data["sale"], data["sale_item"], "2026-01-01", as_of
    )
    print("5. Сформировать отчёт по продажам")
    print(f"Количество сделок: {report['sales_count']}")
    print(f"Сумма сделок: {report['sales_total']} руб.")
    print(f"История бронирований: {len(data['booking'])} записи")

    validate_data(data)

    return data


def main():
    parser = argparse.ArgumentParser(
        description="Демонстрация КТ2 на согласованном наборе JSON-данных"
    )
    parser.add_argument(
        "--data", type=Path, default=Path(__file__).with_name("data.json")
    )
    parser.add_argument(
        "--date", default="2026-10-13", help="Расчётная дата YYYY-MM-DD"
    )
    parser.add_argument(
        "--output", type=Path, help="Сохранить результат в отдельный JSON"
    )
    args = parser.parse_args()

    try:
        if date.fromisoformat(args.date).isoformat() != args.date:
            raise ValueError("Расчётная дата задаётся в формате YYYY-MM-DD")
        data = run_demo(load_data(args.data), args.date)
        if args.output is not None:
            if args.output.resolve() == args.data.resolve():
                raise ValueError(
                    "Для результата укажите отдельный файл, "
                    "отличный от входного"
                )
            save_data(data, args.output)
    except (ValueError, KeyError, TypeError, OSError) as error:
        parser.exit(1, f"Ошибка: {error}\n")


if __name__ == "__main__":
    main()
