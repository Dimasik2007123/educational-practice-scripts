import random
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

def generate_key_code(object_id, client_id, keys_type_id=1):
    connection = get_connection()
    cursor = connection.cursor()
    while True:
        code = random.randint(100000, 999999)
        cursor.execute("SELECT id FROM keys WHERE locker_code = %s", (code,))
        if not cursor.fetchone():
            break
    key_set_number = random.randint(100000, 999999)
    cursor.execute("""
        INSERT INTO keys (
            real_estate_object_id, client_id, locker_code,
            key_set_number, quantity, issue_date, keys_type_id
        ) VALUES (%s, %s, %s, %s, %s, %s, %s)
        RETURNING id
    """, (object_id, client_id, code, key_set_number, 6, date.today(), keys_type_id))
    key_id = cursor.fetchone()[0]
    connection.commit()
    cursor.close()
    connection.close()
    return code

if __name__ == "__main__":
    code = generate_key_code(15, 1, keys_type_id=1)
    print(f"Код для постамата: {code}")