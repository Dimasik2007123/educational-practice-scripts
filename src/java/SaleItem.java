import java.time.LocalDate;
import java.util.Map;

public class SaleItem {
    private int id;
    private int saleId;
    private int realEstateObjectId;
    private long price;
    private int lineNumber;

    public SaleItem(Map<String, Object> row) {
        this.id = Values.integer(row, "id");
        this.saleId = Values.integer(row, "sale_id");
        this.realEstateObjectId = Values.integer(row, "real_estate_object_id");
        this.price = Values.longInteger(row, "price");
        this.lineNumber = Values.integer(row, "line_number");
    }
    public int getId() { return id; }
    public int getSaleId() { return saleId; }
    public int getRealEstateObjectId() { return realEstateObjectId; }
    public long getPrice() { return price; }
    public int getLineNumber() { return lineNumber; }

    public AcceptanceCertificate registerCertificate(AgencyData data, LocalDate issueDate) {
        AgencyData.require(data.saleItems(),SaleItem::getId,id,"Sale item");
        if (data.certificates().stream().anyMatch(c -> c.getSaleItemId() == id)) throw new IllegalArgumentException("Certificate already exists");
        AcceptanceCertificate cert = new AcceptanceCertificate(AgencyData.row("id",AgencyData.nextId(data.certificates(),AcceptanceCertificate::getId),
            "issue_date",issueDate.toString(),"sale_item_id",id));
        data.certificates().add(cert); return cert;
    }
}
