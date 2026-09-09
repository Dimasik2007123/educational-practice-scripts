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
                   city=None, min_area=None, max_area=None, type_id=None):
    connection = get_connection()
    cursor = connection.cursor()
    query = """
        SELECT id, address, floor, rooms_count, total_area, price, real_estate_status_id
        FROM real_estate_object
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
        query += " AND real_estate_type_id = %s"
        params.append(type_id)

    cursor.execute(query, params)
    results = cursor.fetchall()
    cursor.close()
    connection.close()
    return results

if __name__ == "__main__":
    print("\nЭтаж=4, цена от 5 млн")
    objects = search_objects(floor=4, min_price=5000000)
    for obj in objects:
        address = obj[1]
        address_str = f"г. {address.city}, ул. {address.street}, д. {address.house}"
        print(f"ID: {obj[0]}, Адрес: {address_str}, Комнат: {obj[3]}, Цена: {obj[5]}")
