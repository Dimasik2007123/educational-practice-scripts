from datetime import date
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

def create_sale(employee_id, client_id, contract_type_id, items, notes=None):
    connection = get_connection()
    cursor = connection.cursor()
    try:
        cursor.execute("""
            INSERT INTO sale (sale_date, employee_id, client_id, contract_type_id, notes)
            VALUES (%s, %s, %s, %s, %s)
            RETURNING id
        """, (date.today(), employee_id, client_id, contract_type_id, notes))
        sale_id = cursor.fetchone()[0]
        for idx, (object_id, price) in enumerate(items, start=1):
            cursor.execute("""
                INSERT INTO sale_item (sale_id, real_estate_object_id, price, line_number)
                VALUES (%s, %s, %s, %s)
            """, (sale_id, object_id, price, idx))
            cursor.execute("""
                UPDATE real_estate_object SET real_estate_status_id = 4
                WHERE id = %s
            """, (object_id,))
        connection.commit()
        return sale_id
    except Exception as e:
        print(f"Ошибка: {e}")
        return None
    finally:
        cursor.close()
        connection.close()

if __name__ == "__main__":
    sale_id = create_sale(
        employee_id=8,
        client_id=1,
        contract_type_id=1,
        items=[(15, 12000000.00)],
        notes="Продажа двух квартир"
    )
    print(f"Сделка оформлена с ID: {sale_id}")