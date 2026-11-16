import java.time.LocalDate;
import java.util.*;

public class Sale {
    private final List<SaleItem> items = new ArrayList<>();
    private int id;
    private LocalDate saleDate;
    private int employeeId;
    private int clientId;
    private int contractTypeId;
    private String notes;

    public Sale(Map<String, Object> row) {
        this.id = Values.integer(row, "id");
        this.saleDate = Values.date(row, "sale_date");
        this.employeeId = Values.integer(row, "employee_id");
        this.clientId = Values.integer(row, "client_id");
        this.contractTypeId = Values.integer(row, "contract_type_id");
        this.notes = Values.string(row, "notes");
    }
    public int getId() { return id; }
    public LocalDate getSaleDate() { return saleDate; }
    public int getEmployeeId() { return employeeId; }
    public int getClientId() { return clientId; }
    public int getContractTypeId() { return contractTypeId; }
    public String getNotes() { return notes; }
    void addItem(SaleItem item) { items.add(item); }
    public List<SaleItem> getItems() { return Collections.unmodifiableList(items); }
    public long calculateTotal() {
        long total = 0;
        for (SaleItem item : items) if (item.getSaleId() == id) total = Math.addExact(total, item.getPrice());
        return total;
    }
    public Sale createSale(AgencyData data, List<Map.Entry<Integer,Long>> items) {
        Objects.requireNonNull(items);
        if (data.sales().stream().anyMatch(existing -> existing.getId() == id))
            throw new IllegalArgumentException("Sale already registered or ID is stale");
        Participant buyer = AgencyData.require(data.clients(),Client::getId,clientId,"Client");
        if (!buyer.canPurchase()) throw new IllegalArgumentException("Participant cannot purchase");
        AgencyData.require(data.employees(),Employee::getId,employeeId,"Employee");
        AgencyData.require(data.contractTypes(),ContractType::getId,contractTypeId,"Contract type");
        if (items.isEmpty()) throw new IllegalArgumentException("Sale must contain a property");
        Set<Integer> seen = new HashSet<>(); List<RealEstateObject> checked = new ArrayList<>();
        for (Map.Entry<Integer,Long> item : items) {
            if (item.getValue() <= 0) throw new IllegalArgumentException("Price must be a positive whole number of rubles");
            if (!seen.add(item.getKey())) throw new IllegalArgumentException("Duplicate property in sale");
            RealEstateObject object = data.property(item.getKey());
            String reason = data.availability(object,saleDate,LocalDate.of(9999,12,31),clientId);
            if (reason != null) throw new IllegalArgumentException(reason);
            if (data.saleItems().stream().anyMatch(x -> x.getRealEstateObjectId() == object.getId()))
                throw new IllegalArgumentException("Property already sold");
            checked.add(object);
        }
        int itemId = AgencyData.nextId(data.saleItems(),SaleItem::getId);
        data.sales().add(this);
        for (int i = 0; i < checked.size(); i++) {
            RealEstateObject object = checked.get(i);
            SaleItem position = new SaleItem(AgencyData.row("id",itemId++,"sale_id",id,"real_estate_object_id",object.getId(),
                "price",items.get(i).getValue(),"line_number",i+1));
            data.saleItems().add(position);
            addItem(position);
            object.setRealEstateStatusId(4);
            for (Booking b : data.bookings()) if (b.getRealEstateObjectId() == object.getId() && b.overlaps(saleDate,LocalDate.of(9999,12,31))) b.setStatus("fulfilled");
        }
        return this;
    }
}
