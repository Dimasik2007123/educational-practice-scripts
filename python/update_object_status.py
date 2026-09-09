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

def update_object_status(object_id, new_status_id):
    connection = get_connection()
    cursor = connection.cursor()
    try:
        cursor.execute("""
            SELECT real_estate_status_id
            FROM real_estate_object
            WHERE id = %s
            FOR UPDATE
        """, (object_id,))
        object_data = cursor.fetchone()
        if object_data is not None and object_data[0] == 3:
            cursor.execute("""
                DELETE FROM booking
                WHERE real_estate_object_id = %s
            """, (object_id,))

        cursor.execute("""
            UPDATE real_estate_object
            SET real_estate_status_id = %s
            WHERE id = %s
        """, (new_status_id, object_id))
        connection.commit()
        success = True
    except Exception as e:
        print(f"Ошибка: {e}")
        success = False
    cursor.close()
    connection.close()
    return success

if __name__ == "__main__":
    result = update_object_status(15, 2)
    print(f"Статус обновлен: {result}")