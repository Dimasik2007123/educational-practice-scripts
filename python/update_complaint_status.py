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

def update_complaint_status(complaint_id, new_status):
    connection = get_connection()
    cursor = connection.cursor()
    try:
        cursor.execute("SELECT id FROM complaint WHERE id = %s", (complaint_id,))
        if not cursor.fetchone():
            print("Претензия не найдена")
            return False
        cursor.execute("""
            UPDATE complaint SET status = %s WHERE id = %s
        """, (new_status, complaint_id))
        connection.commit()
        return True
    except Exception as e:
        print(f"Ошибка: {e}")
        return False
    finally:
        cursor.close()
        connection.close()

if __name__ == "__main__":
    result = update_complaint_status(11, "устранена")
    print(f"Статус претензии 11 обновлен: {result}")