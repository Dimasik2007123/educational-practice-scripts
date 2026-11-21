import java.nio.file.Path;
import java.time.LocalDate;
import java.util.*;

/** Regression checks against the behavior of python/*.py on the shared fixture. */
public class Verification {
    private static final LocalDate DAY = LocalDate.parse("2026-10-13");
    private static void check(boolean ok, String message) { if (!ok) throw new AssertionError(message); }
    private static void rejects(Runnable action) {
        try { action.run(); } catch (IllegalArgumentException expected) { return; }
        throw new AssertionError("Expected rejection");
    }
    public static void main(String[] args) throws Exception {
        AgencyData d = AgencyData.load(Path.of(args.length == 0 ? "src/java/data.json" : args[0]));
        check(d.searchObjects(Map.of("city","Москва","min_price",7000000,"max_price",8500000))
            .stream().map(RealEstateObject::getId).toList().equals(List.of(1,2,3,4)), "search");
        check(d.searchObjects(Map.of("min_completion_date","2027-01-01")).size() == 2, "date filter");
        check(d.searchObjects(Map.of("floor", 4)).size() == 6, "floor filter");
        int bookingsBefore = d.getBookings().size();
        int propertyStatusBefore = d.getProperties().get(0).getRealEstateStatusId();
        Booking reversed = new Booking(AgencyData.row("id", 4, "booking_date", DAY.toString(),
            "expiration_date", DAY.minusDays(1).toString(), "client_id", 1,
            "employee_id", 1, "real_estate_object_id", 1, "status", "active"));
        rejects(() -> reversed.bookObject(d));
        check(d.getBookings().size() == bookingsBefore
            && d.getProperties().get(0).getRealEstateStatusId() == propertyStatusBefore,
            "invalid booking dates changed data");
        AgencyData sameDayData = AgencyData.load(Path.of(args.length == 0 ? "src/java/data.json" : args[0]));
        Booking sameDay = new Booking(AgencyData.row("id", 4, "booking_date", DAY.toString(),
            "expiration_date", DAY.toString(), "client_id", 1,
            "employee_id", 1, "real_estate_object_id", 1, "status", "active"));
        sameDay.bookObject(sameDayData);
        check(sameDayData.getBookings().size() == bookingsBefore + 1, "same-day booking");
        Booking b = d.prepareBooking(1,1,1,DAY).bookObject(d);
        check(b.getId() == 4 && b.getExpirationDate().equals(LocalDate.parse("2026-10-27")), "booking");
        rejects(() -> d.prepareBooking(1,2,1,DAY).bookObject(d));
        rejects(() -> d.prepareBooking(4,1,1,DAY).bookObject(d));
        rejects(() -> b.bookObject(d));
        check(d.getBookings().size() == 4, "booking rejection changed data");
        int salesBefore=d.getSales().size(), itemsBefore=d.getSaleItems().size();
        rejects(() -> d.prepareSale(1,1,1,DAY,null).createSale(d,List.of(Map.entry(1,8L),Map.entry(2,9L))));
        check(d.getSales().size()==salesBefore && d.getSaleItems().size()==itemsBefore && d.getProperties().get(0).getRealEstateStatusId()==3, "sale rejection changed data");
        rejects(() -> d.prepareSale(1,1,1,DAY,null).createSale(d,List.of(Map.entry(1,8L),Map.entry(1,9L))));
        Sale sale=d.prepareSale(1,1,1,DAY,null).createSale(d,List.of(Map.entry(1,8000000L),Map.entry(3,7000000L)));
        rejects(() -> sale.createSale(d,List.of(Map.entry(6,1L))));
        check(sale.getId()==2 && b.getStatus().equals("fulfilled") && sale.calculateTotal()==15000000, "sale");
        check(sale.getItems().size()==2 && d.getSales().get(0).getItems().size()==1, "sale items");
        Participant client = d.getClients().get(0), employee = d.getEmployees().get(0);
        check(client.role().equals("client") && employee.role().equals("employee") && client.canPurchase() && !employee.canPurchase(), "participant implementations");
        check(d.getProperties().get(0).getRealEstateStatusId()==4 && d.getProperties().get(2).getRealEstateStatusId()==4, "sale statuses");
        SaleItem item=d.getSaleItems().stream().filter(x -> x.getSaleId()==2 && x.getRealEstateObjectId()==1).findFirst().orElseThrow();
        int certificatesBefore = d.certificates().size();
        rejects(() -> item.registerCertificate(d, DAY.minusDays(1)));
        check(d.certificates().size() == certificatesBefore, "invalid certificate date changed data");
        AcceptanceCertificate cert=item.registerCertificate(d,DAY);
        check(cert.getIssueDate().equals(DAY) && d.certificates().size() == certificatesBefore + 1,
            "certificate registration on sale date");
        rejects(() -> cert.generateKeyCode(d,DAY.minusDays(1),1));
        Keys key=cert.generateKeyCode(d,DAY,1);
        check(key.getLockerCode()>=100000 && key.getLockerCode()<=999999 && key.getLockerCode()!=100000, "key code");
        check(d.getCertificateItems().stream().anyMatch(x -> x.getAcceptanceCertificateId()==cert.getId() && x.getKeysId()==key.getId()), "certificate item");
        rejects(() -> cert.generateKeyCode(d,DAY,1));
        Map<String,Object> report=d.generateSalesReport(LocalDate.parse("2026-01-01"),DAY);
        check(((Integer) report.get("sales_count"))==2 && ((Long) report.get("sales_total"))==24000000 && ((Long)((Map<?,?>)((List<?>)report.get("rows")).get(1)).get("total"))==15000000, "report");
        check(d.generateSalesReport(DAY,DAY).get("sales_total").equals(15000000L), "inclusive dates");
        rejects(() -> d.generateSalesReport(DAY,DAY.minusDays(1)));
        System.out.println("All five algorithms and rejection scenarios passed.");
    }
}
