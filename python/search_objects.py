import psycopg2
import psycopg2.extras
import os
from dotenv import load_dotenv

load_dotenv()

def get_connection():
    conn = psycopg2.connect(
        host=os.getenv('DB_HOST'),
        port=os.getenv('DB_PORT'),
        database=os.getenv('DB_NAME'),
        user=os.getenv('DB_USER'),
        password=os.getenv('DB_PASSWORD')
    )
    psycopg2.extras.register_composite('address_type', conn, globally=True)
    return conn

def search_objects(floor=None, rooms_count=None, min_price=None, max_price=None,
                   city=None, min_area=None, max_area=None, type_id=None,
                   real_estate_status_id=None, construction_status_id=None,
                   min_completion_date=None, max_completion_date=None):
    connection = get_connection()
    cursor = connection.cursor()
    query = """
         SELECT reo.id, reo.address, reo.floor, reo.rooms_count, reo.total_area,
             reo.price, ret.name, res.name, cs.name, reo.completion_date
         FROM real_estate_object AS reo
         JOIN real_estate_type AS ret ON ret.id = reo.real_estate_type_id
         JOIN real_estate_status AS res ON res.id = reo.real_estate_status_id
         JOIN construction_status AS cs ON cs.id = reo.construction_status_id
        WHERE 1=1
    """
    params = []

    if floor is not None:
        query += " AND floor = %s"
        params.append(floor)
    if rooms_count is not None:
        query += " AND rooms_count = %s"
        params.append(rooms_count)
    if min_price is not None:
        query += " AND price >= %s"
        params.append(min_price)
    if max_price is not None:
        query += " AND price <= %s"
        params.append(max_price)
    if city is not None:
        query += " AND (address).city = %s"
        params.append(city)
    if min_area is not None:
        query += " AND total_area >= %s"
        params.append(min_area)
    if max_area is not None:
        query += " AND total_area <= %s"
        params.append(max_area)
    if type_id is not None:
        query += " AND reo.real_estate_type_id = %s"
        params.append(type_id)
    if real_estate_status_id is not None:
        query += " AND reo.real_estate_status_id = %s"
        params.append(real_estate_status_id)
    if construction_status_id is not None:
        query += " AND reo.construction_status_id = %s"
        params.append(construction_status_id)
    if min_completion_date is not None:
        query += " AND reo.completion_date >= %s"
        params.append(min_completion_date)
    if max_completion_date is not None:
        query += " AND reo.completion_date <= %s"
        params.append(max_completion_date)

    cursor.execute(query, params)
    results = cursor.fetchall()
    cursor.close()
    connection.close()
    return results

if __name__ == "__main__":
    print("\nЭтаж=4, цена от 5 млн")
    objects = search_objects(floor=4, min_price=5000000, city="Москва")
    if not objects:
        print("Объекты не найдены")
    else:
        for obj in objects:
            address = obj[1]
            address_str = f"г. {address.city}, ул. {address.street}, д. {address.house}"
            print(
                f"ID: {obj[0]}, Тип: {obj[6]}, Адрес: {address_str}, Комнат: {obj[3]}, "
                f"Цена: {obj[5]}, Статус: {obj[7]}, Строительство: {obj[8]}, "
                f"Срок сдачи: {obj[9]}"
            )
