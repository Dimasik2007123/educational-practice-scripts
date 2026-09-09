import subprocess
import sys
import os

def run_script(script_name):
    
    result = subprocess.run([sys.executable, script_name], capture_output=True, text=True)
    print(result.stdout)
    if result.stderr:
        print(f"Ошибки:\n{result.stderr}")
    return result

if __name__ == "__main__":

    if not os.path.exists('.env'):
        print("Файл .env не найден! Создайте его с настройками подключения к БД.")
        sys.exit(1)
    
    run_script("add_object.py")
    
    run_script("update_object_status.py")
    
    run_script("search_objects.py")
    
    run_script("book_object.py")
    
    run_script("create_sale.py")
    
    run_script("generate_key_code.py")
    
    run_script("generate_sales_report.py")
    
    run_script("add_complaint.py")
    
    run_script("add_complaint_item.py")
    
    run_script("update_complaint_status.py")