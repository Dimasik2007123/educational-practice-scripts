from datetime import date, timedelta
import psycopg2

def get_connection():
    conn = psycopg2.connect(
        host="localhost",
        database="company",
        user="postgres",
        password="G6sjxb7cB"
    )
    return conn

def book_object(object_id, client_id, employee_id):
    connection = get_connection()
    cursor = connection.cursor()
    try:
        cursor.execute("""
            SELECT real_estate_status_id FROM real_estate_object WHERE id = %s
        """, (object_id,))
        status = cursor.fetchone()
        if status and status[0] != 2:
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