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

def add_complaint_item(complaint_id, description, requirement_id, claim_amount=None):
    connection = get_connection()
    cursor = connection.cursor()
    try:
        cursor.execute("SELECT id FROM complaint WHERE id = %s", (complaint_id,))
        if not cursor.fetchone():
            print("Претензия не найдена")
            return False
        cursor.execute("""
            INSERT INTO complaint_item (complaint_id, description, requirement_id, claim_amount)
            VALUES (%s, %s, %s, %s)
        """, (complaint_id, description, requirement_id, claim_amount))
        connection.commit()
        return True
    except Exception as e:
        print(f"Ошибка: {e}")
        return False
    finally:
        cursor.close()
        connection.close()

if __name__ == "__main__":
    result1 = add_complaint_item(
        complaint_id=11,
        description="Царапины на входной двери, требуется покраска",
        requirement_id=1,
        claim_amount=6500.00
    )
    print(f"Позиция 1 добавлена: {result1}")

    result2 = add_complaint_item(
        complaint_id=11,
        description="Не работают 4 розетки на кухне",
        requirement_id=3,
        claim_amount=3500.00
    )
    print(f"Позиция 2 добавлена: {result2}")