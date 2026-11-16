import java.nio.file.Path;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class Main {
    public static void main(String[] args) throws Exception {
        Path path = Path.of(args.length > 0 ? args[0] : "src/java/data.json");
        LocalDate date = LocalDate.parse(args.length > 1 ? args[1] : "2026-10-13");
        AgencyData data = AgencyData.load(path);
        Participant participant = data.getClients().get(0);
        System.out.println("Участник: " + participant.describe());
        List<RealEstateObject> found = data.searchObjects(Map.of("city","Москва", "min_price",7000000, "max_price",8500000));
        System.out.println("1. Найдены объекты: " + found.stream().map(o -> String.valueOf(o.getId())).collect(Collectors.joining(", ")));
        Booking booking = data.prepareBooking(1,1,1,date).bookObject(data);
        System.out.println("2. Бронь " + booking.getId() + ": объект 1, до " + booking.getExpirationDate());
        try { data.prepareBooking(1,2,1,date).bookObject(data); throw new IllegalStateException("Повторная бронь не отклонена"); }
        catch (IllegalArgumentException e) { System.out.println("Повторная бронь отклонена: " + e.getMessage()); }
        Sale sale = data.prepareSale(1,1,1,date,"Продажа двух объектов")
            .createSale(data,List.of(Map.entry(1,8000000L),Map.entry(3,7000000L)));
        System.out.println("3. Сделка " + sale.getId() + ": объекты 1 и 3");
        SaleItem item = data.getSaleItems().stream().filter(x -> x.getSaleId() == sale.getId() && x.getRealEstateObjectId() == 1).findFirst().orElseThrow();
        AcceptanceCertificate certificate = item.registerCertificate(data,date);
        Keys key = certificate.generateKeyCode(data,date,1);
        System.out.println("4. Код: " + key.getLockerCode());
        Map<String,Object> report = data.generateSalesReport(LocalDate.parse("2026-01-01"),date);
        System.out.println("5. Количество сделок: " + ((Integer) report.get("sales_count")));
        System.out.println("Сумма сделок: " + ((Long) report.get("sales_total")) + " руб.");
        System.out.println("История бронирований: " + data.getBookings().size());
    }
}
