"""Алгоритм 1: поиск объектов по фиксированному набору критериев."""

from datetime import date, timedelta

from data_utils import availability_reason


def search_objects(objects, criteria, data=None):
    """Вернуть копии подходящих карточек."""
    filters = dict(criteria)
    requested_date = filters.pop("available_on", None)
    if requested_date is not None:
        if data is None:
            raise ValueError("Для поиска по доступности необходим набор данных")
        start = date.fromisoformat(requested_date)
        if start.isoformat() != requested_date:
            raise ValueError("Дата задаётся в формате YYYY-MM-DD")
        end = (start + timedelta(days=14)).isoformat()
        if filters.get("real_estate_status_id") == 2:
            filters.pop("real_estate_status_id")
    result = []

    for obj in objects:
        if matches_filters(obj, filters) and (
            requested_date is None
            or availability_reason(data, obj, requested_date, end) is None
        ):
            result.append(dict(obj))

    return result


def matches_filters(obj, criteria):
    """Двенадцать критериев: число проверок на один объект ограничено."""
    return (
        (criteria.get("floor") is None or obj["floor"] == criteria["floor"])
        and (
            criteria.get("rooms_count") is None
            or obj["rooms_count"] == criteria["rooms_count"]
        )
        and (criteria.get("city") is None or obj["city"] == criteria["city"])
        and (
            criteria.get("min_price") is None
            or obj["price"] >= criteria["min_price"]
        )
        and (
            criteria.get("max_price") is None
            or obj["price"] <= criteria["max_price"]
        )
        and (
            criteria.get("min_area") is None
            or obj["total_area"] >= criteria["min_area"]
        )
        and (
            criteria.get("max_area") is None
            or obj["total_area"] <= criteria["max_area"]
        )
        and (
            criteria.get("type_id") is None
            or obj["real_estate_type_id"] == criteria["type_id"]
        )
        and (
            criteria.get("real_estate_status_id") is None
            or obj["real_estate_status_id"]
            == criteria["real_estate_status_id"]
        )
        and (
            criteria.get("construction_status_id") is None
            or obj["construction_status_id"]
            == criteria["construction_status_id"]
        )
        and (
            criteria.get("min_completion_date") is None
            or obj["completion_date"] >= criteria["min_completion_date"]
        )
        and (
            criteria.get("max_completion_date") is None
            or obj["completion_date"] <= criteria["max_completion_date"]
        )
    )
