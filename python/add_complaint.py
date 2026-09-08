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

def add_complaint(sale_id, client_id, real_estate_object_id, status="новая"):
    connection = get_connection()
    cursor = connection.cursor()
    cursor.execute("""
        INSERT INTO complaint (filling_date, sale_id, client_id, real_estate_object_id, status)
        VALUES (%s, %s, %s, %s, %s)
        RETURNING id
    """, (date.today(), sale_id, client_id, real_estate_object_id, status))
    complaint_id = cursor.fetchone()[0]
    connection.commit()
    cursor.close()
    connection.close()
    return complaint_id

if __name__ == "__main__":
    complaint_id = add_complaint(
        sale_id=13,
        client_id=1,
        real_estate_object_id=15,
        status="в работе"
    )
    print(f"Претензия добавлена с ID: {complaint_id}")