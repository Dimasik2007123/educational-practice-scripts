package javapr.src;

import javapr.src.model.*;
import javapr.src.service.DatabaseService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== ДЕМОНСТРАЦИЯ 10 АЛГОРИТМОВ ===\n");

                // В pr7.SQL уже есть справочники, сотрудники и клиенты. Для каждой
                // проверки создаем только новые зависимые записи.
                RealEstateType type = new RealEstateType(1);
                ConstructionStatus completed = new ConstructionStatus(9);
                RealEstateStatus free = new RealEstateStatus(2);
                Client client = new Client(1);
                Employee employee = new Employee(8);
                int uniquePart = (int) Math.abs(System.nanoTime() % 100000);
                String cadastralNumber = String.format("77:99:0000000:%05d", uniquePart);

                // ==================== АЛГОРИТМ 1: Добавление объекта ====================
                System.out.println("--- Алгоритм 1: Добавление объекта недвижимости ---");
                RealEstateObject obj = new RealEstateObject(
                        type,
                        new Address(101001, "Россия", "Москва", "ул. Тестовая", 100 + uniquePart, null),
                        cadastralNumber,
                        4, 100 + uniquePart, 2,
                        68.5, 50.0,
                        completed,
                        LocalDate.now(),
                        new BigDecimal("13500000.00"),
                        free
                );
        obj.add();
                requireId(obj.getId(), "объект недвижимости");
                System.out.println("Объект добавлен. ID: " + obj.getId() + "\n");

        // ==================== АЛГОРИТМ 2: Обновление статуса объекта ====================
        System.out.println("--- Алгоритм 2: Обновление статуса объекта ---");
                obj.updateStatus(new RealEstateStatus(3));
                obj.updateStatus(free);
        System.out.println("Статус объекта обновлен\n");

        // ==================== АЛГОРИТМ 3: Поиск объектов ====================
        System.out.println("--- Алгоритм 3: Поиск объектов с фильтрацией ---");
        List<RealEstateObject> found = RealEstateObject.searchObjects(
                null, null,
                new BigDecimal("5000000"),
                new BigDecimal("10000000"),
                "Москва", 40.0, 100.0, 1
        );
        for (RealEstateObject foundObject : found) {
            System.out.printf("Кадастровый номер: %s, этаж: %d, площадь: %.2f кв.м%n",
                    foundObject.getCadastralNumber(), foundObject.getFloor(),
                    foundObject.getTotalArea());
        }
        System.out.println("Найдено объектов: " + found.size() + "\n");

        // ==================== АЛГОРИТМ 4: Бронирование ====================
        System.out.println("--- Алгоритм 4: Бронирование объекта ---");
        Booking booking = new Booking(
                LocalDate.now(),
                LocalDate.now().plusDays(14),
                client,
                employee,
                obj
        );
        booking.bookObject();
        System.out.println("Бронирование создано. ID: " + booking.getId() + "\n");

        // ==================== АЛГОРИТМ 5: Оформление сделки ====================
        System.out.println("--- Алгоритм 5: Оформление сделки ---");
        List<Object[]> items = new ArrayList<>();
        items.add(new Object[]{obj, new BigDecimal("13500000.00")});
        ContractType contract = new ContractType(1);
        
        Sale sale = Sale.createSale(
                employee,   // передаем объект Employee
                client,     // передаем объект Client
                contract,   // передаем объект ContractType
                items,
                null
        );
        System.out.println("Сделка создана. ID: " + sale.getId() + "\n");

        // ==================== АЛГОРИТМ 6: Генерация кода постамата ====================
        System.out.println("--- Алгоритм 6: Генерация кода постамата ---");
        KeysType keyType = new KeysType(1);
        Keys keys = new Keys(obj, 900000 + uniquePart, 6, client, keyType);
        int code = keys.generateLockerCode();
        requireId(keys.getId(), "комплект ключей");
        System.out.println("Код постамата: " + code + "\n");

        // ==================== АЛГОРИТМ 7: Отчет по продажам ====================
        System.out.println("--- Алгоритм 7: Отчет по продажам ---");
        List<Sale> report = Sale.generateReport(
                LocalDateTime.now().minusMonths(1),
                LocalDateTime.now().plusMonths(1)
        );
        BigDecimal totalSalesAmount = BigDecimal.ZERO;
        for (Sale reportSale : report) {
            totalSalesAmount = totalSalesAmount.add(reportSale.calculateTotal());
        }
        System.out.println("Количество сделок: " + report.size());
        System.out.println("Сумма сделок: " + totalSalesAmount + "\n");

        // ==================== АЛГОРИТМ 8: Добавление претензии ====================
        System.out.println("--- Алгоритм 8: Добавление претензии ---");
        Complaint complaint = new Complaint(sale, client, obj, "новая");
        complaint.add();
        System.out.println("Претензия добавлена. ID: " + complaint.getId() + "\n");

        // ==================== АЛГОРИТМ 9: Добавление позиции в претензию ====================
        System.out.println("--- Алгоритм 9: Добавление позиции в претензию ---");
        Requirement req = new Requirement(1);    // Устранение недостатков
        complaint.addItem("Царапины на входной двери", req, new BigDecimal("6500.00"));
        System.out.println("Позиция добавлена в претензию\n");

        // ==================== АЛГОРИТМ 10: Обновление статуса претензии ====================
        System.out.println("--- Алгоритм 10: Обновление статуса претензии ---");
        complaint.updateStatus("в работе");
        System.out.println("Статус претензии обновлен\n");

        System.out.println("=== ДЕМОНСТРАЦИЯ ЗАВЕРШЕНА ===");
    }

        private static void requireId(int id, String entityName) {
                if (id == 0) {
                        throw new IllegalStateException("Не удалось добавить: " + entityName
                                        + ". Проверьте подключение к PostgreSQL и схему pr6.SQL.");
                }
        }
}
