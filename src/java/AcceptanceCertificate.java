import java.time.LocalDate;
import java.util.*;

public class AcceptanceCertificate {
    private int id;
    private LocalDate issueDate;
    private int saleItemId;

    public AcceptanceCertificate(Map<String, Object> row) {
        this.id = Values.integer(row, "id");
        this.issueDate = Values.date(row, "issue_date");
        this.saleItemId = Values.integer(row, "sale_item_id");
    }
    public int getId() { return id; }
    public LocalDate getIssueDate() { return issueDate; }
    public int getSaleItemId() { return saleItemId; }



    public Keys generateKeyCode(AgencyData data, LocalDate date, int keysTypeId) {
        AcceptanceCertificate cert = AgencyData.require(data.certificates(),AcceptanceCertificate::getId,id,"Certificate");
        if (cert.getIssueDate().isAfter(date)) throw new IllegalArgumentException("Certificate not signed yet");
        SaleItem item = AgencyData.require(data.saleItems(),SaleItem::getId,cert.getSaleItemId(),"Sale item");
        Sale sale = AgencyData.require(data.sales(),Sale::getId,item.getSaleId(),"Sale");
        if (sale.getSaleDate().isAfter(cert.getIssueDate())) throw new IllegalArgumentException("Certificate predates sale");
        RealEstateObject object = data.property(item.getRealEstateObjectId());
        ConstructionStatus status = AgencyData.require(data.constructionStatuses(),ConstructionStatus::getId,object.getConstructionStatusId(),"Construction status");
        AgencyData.require(data.keysTypes(),KeysType::getId,keysTypeId,"Keys type");
        if (object.getRealEstateStatusId() != 4 || status.getNumber() < 5) throw new IllegalArgumentException("Property is not sold or not ready");
        if (data.keys().stream().anyMatch(k -> k.getRealEstateObjectId() == object.getId() && k.getKeysTypeId() == keysTypeId))
            throw new IllegalArgumentException("Code for this property and key type already exists");
        Set<Integer> used = new HashSet<>(); for (Keys key : data.keys()) used.add(key.getLockerCode());
        if (used.size() >= 900000) throw new IllegalArgumentException("No free codes");
        int code; do { code = 100000 + new java.security.SecureRandom().nextInt(900000); } while (used.contains(code));
        int line = 1; for (AcceptanceCertificateItem x : data.certificateItems()) if (x.getAcceptanceCertificateId() == id) line = Math.max(line, x.getLineNumber()+1);
        Keys key = new Keys(AgencyData.row("id",AgencyData.nextId(data.keys(),Keys::getId),"real_estate_object_id",object.getId(),
            "key_set_number",AgencyData.nextId(data.keys(),Keys::getKeySetNumber),"quantity",6,"issue_date",date.toString(),
            "locker_code",code,"keys_type_id",keysTypeId));
        data.keys().add(key); data.certificateItems().add(new AcceptanceCertificateItem(AgencyData.row("acceptance_certificate_id",id,"keys_id",key.getId(),"line_number",line)));
        return key;
    }
}
