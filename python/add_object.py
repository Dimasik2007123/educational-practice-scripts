from datetime import date
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

def add_object(real_estate_type_id, address, cadastral_number, floor, apartment_number,
               rooms_count, total_area, living_area, construction_status_id,
               completion_date, price, real_estate_status_id):
    connection = get_connection()
    cursor = connection.cursor()
    cursor.execute("""
        INSERT INTO real_estate_object (
            real_estate_type_id, address, cadastral_number, floor, apartment_number,
            rooms_count, total_area, living_area, construction_status_id,
            completion_date, price, real_estate_status_id
        ) VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)
        RETURNING id
    """, (
        real_estate_type_id,
        address,
        cadastral_number,
        floor,
        apartment_number,
        rooms_count,
        total_area,
        living_area,
        construction_status_id,
        completion_date,
        price,
        real_estate_status_id
    ))
    object_id = cursor.fetchone()[0]
    connection.commit()
    cursor.close()
    connection.close()
    return object_id


if __name__ == "__main__":
    address = (143006, 'Россия', 'Одинцово', 'ул. Березовая', 5, 1)
    new_id = add_object(
        real_estate_type_id=1,
        address=address,
        cadastral_number='50:20:0030206:15199',
        floor=10,
        apartment_number=170,
        rooms_count=2,
        total_area=40.0,
        living_area=28.2,
        construction_status_id=8,
        completion_date=date(2026, 9, 30),
        price=8000000.00,
        real_estate_status_id=5
    )
    print(f"Объект добавлен с ID: {new_id}")