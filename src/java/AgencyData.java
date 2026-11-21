import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;
import java.util.function.ToIntFunction;
import java.time.LocalDate;

/** Collections of domain objects loaded from the same JSON as the Python program. */
public class AgencyData {
    private final List<Position> positions;
    private final List<Client> clients;
    private final List<Employee> employees;
    private final List<RealEstateType> propertyTypes;
    private final List<RealEstateStatus> propertyStatuses;
    private final List<ConstructionStatus> constructionStatuses;
    private final List<ContractType> contractTypes;
    private final List<KeysType> keysTypes;
    private final List<RealEstateObject> properties;
    private final List<Booking> bookings;
    private final List<Sale> sales;
    private final List<SaleItem> saleItems;
    private final List<AcceptanceCertificate> certificates;
    private final List<Keys> keys;
    private final List<AcceptanceCertificateItem> certificateItems;

    private AgencyData(Map<String,Object> data) {
        positions = load(data,"position",Position::new);
        clients = load(data,"client",Client::new);
        employees = load(data,"employee",Employee::new);
        propertyTypes = load(data,"real_estate_type",RealEstateType::new);
        propertyStatuses = load(data,"real_estate_status",RealEstateStatus::new);
        constructionStatuses = load(data,"construction_status",ConstructionStatus::new);
        contractTypes = load(data,"contract_type",ContractType::new);
        keysTypes = load(data,"keys_type",KeysType::new);
        properties = load(data,"real_estate_object",RealEstateObject::new);
        bookings = load(data,"booking",Booking::new);
        sales = load(data,"sale",Sale::new);
        saleItems = load(data,"sale_item",SaleItem::new);
        certificates = load(data,"acceptance_certificate",AcceptanceCertificate::new);
        keys = load(data,"keys",Keys::new);
        certificateItems = load(data,"acceptance_certificate_item",AcceptanceCertificateItem::new);
        validate();
        for (SaleItem item : saleItems) require(sales,Sale::getId,item.getSaleId(),"Sale").addItem(item);
    }
    private static <T> void unique(List<T> list, java.util.function.ToIntFunction<T> field, String name, boolean positive) {
        Set<Integer> seen = new HashSet<>();
        for (T value : list) {
            int id = field.applyAsInt(value);
            if ((positive && id <= 0) || !seen.add(id)) throw new IllegalArgumentException("Invalid or duplicate " + name);
        }
    }
    private static <T> void reference(List<T> list, java.util.function.ToIntFunction<T> field, int id, String name) {
        if (list.stream().noneMatch(x -> field.applyAsInt(x) == id)) throw new IllegalArgumentException("Missing reference: " + name + " " + id);
    }
    private void validate() {
        unique(positions,Position::getId,"position.id",true); unique(clients,Client::getId,"client.id",true);
        unique(employees,Employee::getId,"employee.id",true); unique(propertyTypes,RealEstateType::getId,"type.id",true);
        unique(propertyStatuses,RealEstateStatus::getId,"status.id",true); unique(constructionStatuses,ConstructionStatus::getId,"construction.id",true);
        unique(contractTypes,ContractType::getId,"contract.id",true); unique(keysTypes,KeysType::getId,"keysType.id",true);
        unique(properties,RealEstateObject::getId,"property.id",true); unique(bookings,Booking::getId,"booking.id",true);
        unique(sales,Sale::getId,"sale.id",true); unique(saleItems,SaleItem::getId,"saleItem.id",true);
        unique(certificates,AcceptanceCertificate::getId,"certificate.id",true); unique(keys,Keys::getId,"keys.id",true);
        unique(certificateItems,AcceptanceCertificateItem::getKeysId,"certificateItem.keysId",false);
        unique(saleItems,SaleItem::getRealEstateObjectId,"saleItem.property",false);
        unique(certificates,AcceptanceCertificate::getSaleItemId,"certificate.saleItem",false);
        unique(keys,Keys::getLockerCode,"keys.code",false); unique(keys,Keys::getKeySetNumber,"keys.setNumber",false);
        for (Employee x : employees) reference(positions,Position::getId,x.getPositionId(),"position");
        for (RealEstateObject x : properties) {
            reference(propertyTypes,RealEstateType::getId,x.getRealEstateTypeId(),"property type");
            reference(propertyStatuses,RealEstateStatus::getId,x.getRealEstateStatusId(),"property status");
            reference(constructionStatuses,ConstructionStatus::getId,x.getConstructionStatusId(),"construction status");
            if (x.getPrice() <= 0) throw new IllegalArgumentException("Price must be positive");
        }
        for (Booking x : bookings) {
            reference(clients,Client::getId,x.getClientId(),"booking client"); reference(employees,Employee::getId,x.getEmployeeId(),"booking employee");
            reference(properties,RealEstateObject::getId,x.getRealEstateObjectId(),"booking property");
            if (!Set.of("active","cancelled","fulfilled").contains(x.getStatus()) || x.getBookingDate().isAfter(x.getExpirationDate())) throw new IllegalArgumentException("Invalid booking");
        }
        for (Sale x : sales) { reference(clients,Client::getId,x.getClientId(),"sale client"); reference(employees,Employee::getId,x.getEmployeeId(),"sale employee"); reference(contractTypes,ContractType::getId,x.getContractTypeId(),"contract type"); }
        for (SaleItem x : saleItems) { reference(sales,Sale::getId,x.getSaleId(),"sale"); reference(properties,RealEstateObject::getId,x.getRealEstateObjectId(),"sale property"); if (x.getPrice()<=0) throw new IllegalArgumentException("Price must be positive"); }
        for (AcceptanceCertificate x : certificates) reference(saleItems,SaleItem::getId,x.getSaleItemId(),"certificate sale item");
        for (Keys x : keys) { reference(properties,RealEstateObject::getId,x.getRealEstateObjectId(),"key property"); reference(keysTypes,KeysType::getId,x.getKeysTypeId(),"key type"); if (x.getLockerCode()<100000 || x.getLockerCode()>999999) throw new IllegalArgumentException("Invalid key code"); }
        for (AcceptanceCertificateItem x : certificateItems) { reference(certificates,AcceptanceCertificate::getId,x.getAcceptanceCertificateId(),"certificate"); reference(keys,Keys::getId,x.getKeysId(),"keys"); }
    }
    @SuppressWarnings("unchecked")
    private static <T> List<T> load(Map<String,Object> data, String key, Function<Map<String,Object>,T> factory) {
        Object value = data.get(key);
        if (!(value instanceof List<?>)) throw new IllegalArgumentException("Expected array: " + key);
        List<T> result = new ArrayList<>();
        for (Object item : (List<?>) value) {
            if (!(item instanceof Map<?,?>)) throw new IllegalArgumentException("Expected object in " + key);
            result.add(factory.apply((Map<String,Object>) item));
        }
        return result;
    }
    @SuppressWarnings("unchecked")
    public static AgencyData load(Path path) throws IOException {
        Object value = Json.read(path);
        if (!(value instanceof Map<?,?>)) throw new IllegalArgumentException("Expected JSON object");
        return new AgencyData((Map<String,Object>)value);
    }
    public List<RealEstateObject> getProperties() { return Collections.unmodifiableList(properties); }
    public List<Client> getClients() { return Collections.unmodifiableList(clients); }
    public List<Employee> getEmployees() { return Collections.unmodifiableList(employees); }
    public List<Booking> getBookings() { return Collections.unmodifiableList(bookings); }
    public List<Sale> getSales() { return Collections.unmodifiableList(sales); }
    public List<SaleItem> getSaleItems() { return Collections.unmodifiableList(saleItems); }
    public List<Keys> getKeys() { return Collections.unmodifiableList(keys); }
    public List<AcceptanceCertificateItem> getCertificateItems() { return Collections.unmodifiableList(certificateItems); }
    public RealEstateObject getProperty(int id) { return property(id); }

    public List<RealEstateObject> searchObjects(Map<String,Object> criteria) {
        Map<String,Object> filters = new HashMap<>(criteria);
        Object requestedDate = filters.remove("available_on");
        LocalDate start = requestedDate == null ? null : LocalDate.parse(requestedDate.toString());
        LocalDate end = start == null ? null : start.plusDays(14);
        Object requestedStatus = filters.get("real_estate_status_id");
        if (start != null && requestedStatus instanceof Number
                && ((Number) requestedStatus).doubleValue() == 2.0) {
            filters.remove("real_estate_status_id");
        }
        List<RealEstateObject> result = new ArrayList<>();
        for (RealEstateObject object : properties) {
            if (object.matches(filters) && (start == null || availability(object, start, end, null) == null)) {
                result.add(object);
            }
        }
        return result;
    }

    public Booking prepareBooking(int objectId, int clientId, int employeeId, LocalDate date) {
        Objects.requireNonNull(date);
        return new Booking(row("id",nextId(bookings,Booking::getId), "booking_date",date.toString(),
            "expiration_date",date.plusDays(14).toString(), "client_id",clientId,
            "employee_id",employeeId,"real_estate_object_id",objectId,"status","active"));
    }

    public Sale prepareSale(int employeeId, int clientId, int contractId, LocalDate date, String notes) {
        Objects.requireNonNull(date);
        return new Sale(row("id",nextId(sales,Sale::getId),"sale_date",date.toString(),
            "employee_id",employeeId,"client_id",clientId,"contract_type_id",contractId,"notes",notes));
    }

    public Map<String,Object> generateSalesReport(LocalDate start, LocalDate end) {
        if (start.isAfter(end)) throw new IllegalArgumentException("Start date after end date");
        List<Map<String,Object>> rows = new ArrayList<>();
        long total = 0;
        for (Sale sale : sales) {
            if (!sale.getSaleDate().isBefore(start) && !sale.getSaleDate().isAfter(end)) {
                long sum = sale.calculateTotal();
                rows.add(Map.of("sale_id",sale.getId(),"sale_date",sale.getSaleDate(),"client_id",sale.getClientId(),"total",sum));
                total = Math.addExact(total,sum);
            }
        }
        return Map.of("rows",rows,"sales_count",rows.size(),"sales_total",total);
    }
    List<Position> positions() { return positions; }
    List<Client> clients() { return clients; }
    List<Employee> employees() { return employees; }
    List<RealEstateType> propertyTypes() { return propertyTypes; }
    List<RealEstateStatus> propertyStatuses() { return propertyStatuses; }
    List<ConstructionStatus> constructionStatuses() { return constructionStatuses; }
    List<ContractType> contractTypes() { return contractTypes; }
    List<KeysType> keysTypes() { return keysTypes; }
    List<RealEstateObject> properties() { return properties; }
    List<Booking> bookings() { return bookings; }
    List<Sale> sales() { return sales; }
    List<SaleItem> saleItems() { return saleItems; }
    List<AcceptanceCertificate> certificates() { return certificates; }
    List<Keys> keys() { return keys; }
    List<AcceptanceCertificateItem> certificateItems() { return certificateItems; }

static <T> T require(List<T> items, ToIntFunction<T> id, int value, String title) {
        return items.stream().filter(x -> id.applyAsInt(x) == value).findFirst()
            .orElseThrow(() -> new IllegalArgumentException(title + " not found: " + value));
    }
    static <T> int nextId(List<T> items, ToIntFunction<T> id) {
        return Math.addExact(items.stream().mapToInt(id).max().orElse(0), 1);
    }
    static Map<String,Object> row(Object... pairs) {
        Map<String,Object> result = new HashMap<>();
        for (int i = 0; i < pairs.length; i += 2) result.put((String)pairs[i], pairs[i+1]);
        return result;
    }
    RealEstateObject property(int id) { return require(properties(), RealEstateObject::getId, id, "Property"); }
    String availability(RealEstateObject o, LocalDate start, LocalDate end, Integer owner) {
        ConstructionStatus status = require(constructionStatuses(), ConstructionStatus::getId, o.getConstructionStatusId(), "Construction status");
        if (!status.getIsAvailableForSale()) return "Property unavailable due to construction status";
        if (o.getRealEstateStatusId() != 2 && o.getRealEstateStatusId() != 3) return "Property is not available";
        for (Booking b : bookings()) {
            if (b.getRealEstateObjectId() == o.getId() && b.overlaps(start,end)
                && (owner == null || b.getClientId() != owner)) return "Conflicting active booking";
        }
        return null;
    }
}
