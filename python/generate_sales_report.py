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

def generate_sales_report(start_date, end_date):
    connection = get_connection()
    cursor = connection.cursor()
    cursor.execute("""
        SELECT s.id, s.sale_date, s.client_id, SUM(si.price) as total
        FROM sale s
        JOIN sale_item si ON s.id = si.sale_id
        WHERE s.sale_date BETWEEN %s AND %s
        GROUP BY s.id
    """, (start_date, end_date))
    results = cursor.fetchall()
    cursor.close()
    connection.close()
    return results

if __name__ == "__main__":
    from datetime import date
    report = generate_sales_report(date(2026, 1, 1), date(2026, 9, 8))
    for item in report:
        print(f"Сделка {item[0]}: {item[1]} - {item[3]} руб.")