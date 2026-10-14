"""Алгоритм 5: соединение сделок с позициями и расчёт итогов за период."""

from datetime import date


def generate_sales_report(sales, sale_items, start_date, end_date):
    """Границы периода включены."""
    date.fromisoformat(start_date)
    date.fromisoformat(end_date)
    if start_date > end_date:
        raise ValueError("Начало периода позже его окончания")

    rows = []
    total = 0

    for sale in sales:
        if start_date <= sale["sale_date"] <= end_date:
            sale_total = 0
            for item in sale_items:
                if item["sale_id"] == sale["id"]:
                    sale_total += item["price"]
            rows.append(
                {
                    "sale_id": sale["id"],
                    "sale_date": sale["sale_date"],
                    "client_id": sale["client_id"],
                    "total": sale_total,
                }
            )
            total += sale_total

    return {"rows": rows, "sales_count": len(rows), "sales_total": total}
