from datetime import date, timedelta
import psycopg2
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
    return conn

def book_object(object_id, client_id, employee_id):
    connection = get_connection()
    cursor = connection.cursor()
    try:
        cursor.execute("""
            SELECT reo.real_estate_status_id, b.id, b.expiration_date,
                   cs.is_available_for_sale
            FROM real_estate_object AS reo
            LEFT JOIN booking AS b ON b.real_estate_object_id = reo.id
            JOIN construction_status AS cs ON cs.id = reo.construction_status_id
            WHERE reo.id = %s
            FOR UPDATE OF reo
        """, (object_id,))
        object_data = cursor.fetchone()
        if object_data is None:
            print("Объект не найден")
            return False

        status_id, booking_id, expiration_date, is_available_for_sale = object_data
        if not is_available_for_sale:
            print("Объект недоступен для продажи по статусу строительства")
            return False

        expired_booking = (
            status_id == 3
            and booking_id is not None
            and expiration_date < date.today()
        )
        if expired_booking:
            cursor.execute("DELETE FROM booking WHERE id = %s", (booking_id,))
            status_id = 2

        if status_id != 2:
            print("Объект не свободен для бронирования")
            return False
        booking_date = date.today()
        expiration_date = booking_date + timedelta(days=14)
        cursor.execute("""
            INSERT INTO booking (booking_date, expiration_date, client_id, employee_id, real_estate_object_id)
            VALUES (%s, %s, %s, %s, %s)
            RETURNING id
        """, (booking_date, expiration_date, client_id, employee_id, object_id))
        booking_id = cursor.fetchone()[0]
        cursor.execute("""
            UPDATE real_estate_object SET real_estate_status_id = 3
            WHERE id = %s
        """, (object_id,))
        connection.commit()
        print(f"Бронирование создано с ID: {booking_id}")
        return True
    except Exception as e:
        print(f"Ошибка: {e}")
        return False
    finally:
        cursor.close()
        connection.close()

if __name__ == "__main__":
    result = book_object(15, 1, 9)
    print(f"Бронирование выполнено: {result}")