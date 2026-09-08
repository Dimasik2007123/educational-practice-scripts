import psycopg2
import psycopg2.extras

def get_connection():
    conn = psycopg2.connect(
        host="localhost",
        database="company",
        user="postgres",
        password="G6sjxb7cB"
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
    # Поиск по этажу и цене
    print("\n--- 1. Этаж=4, цена от 5 млн ---")
    objects = search_objects(floor=4, min_price=5000000)
    for obj in objects:
        address = obj[1]
        address_str = f"г. {address.city}, ул. {address.street}, д. {address.house}"
        print(f"ID: {obj[0]}, Адрес: {address_str}, Комнат: {obj[3]}, Цена: {obj[5]}")

    # Поиск по городу
    print("\n--- 2. Город = 'Москва' ---")
    objects = search_objects(city='Москва')
    print(f"Найдено объектов в Москве: {len(objects)}")

    # Поиск по количеству комнат и цене
    print("\n--- 3. Комнат=2, цена от 10 до 20 млн ---")
    objects = search_objects(rooms_count=2, min_price=10000000, max_price=20000000)
    for obj in objects:
        address = obj[1]
        address_str = f"г. {address.city}, ул. {address.street}, д. {address.house}"
        print(f"ID: {obj[0]}, Адрес: {address_str}, Комнат: {obj[3]}, Цена: {obj[5]}")

    # Поиск по площади
    print("\n--- 4. Площадь от 50 до 100 кв.м ---")
    objects = search_objects(min_area=50, max_area=100)
    for obj in objects:
        address = obj[1]
        address_str = f"г. {address.city}, ул. {address.street}, д. {address.house}"
        print(f"ID: {obj[0]}, Адрес: {address_str}, Площадь: {obj[4]}, Цена: {obj[5]}")

    # Поиск по типу объекта (1 - квартира)
    print("\n--- 5. Тип объекта = 1 (квартира) ---")
    objects = search_objects(type_id=1)
    print(f"Найдено квартир: {len(objects)}")

    # Комбинированный поиск (все фильтры вместе)
    print("\n--- 6. Комбинированный поиск: Москва, 2 комнаты, цена 5-15 млн, площадь 40-80, этаж 4 ---")
    objects = search_objects(
        city='Москва',
        rooms_count=2,
        min_price=5000000,
        max_price=15000000,
        min_area=40,
        max_area=80,
        floor=4
    )
    print(f"Найдено объектов: {len(objects)}")
    for obj in objects:
        address = obj[1]
        address_str = f"г. {address.city}, ул. {address.street}, д. {address.house}"
        print(f"ID: {obj[0]}, Адрес: {address_str}, Комнат: {obj[3]}, Площадь: {obj[4]}, Цена: {obj[5]}")
