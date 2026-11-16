import java.time.LocalDate;
import java.util.Map;

public class AcceptanceCertificateItem {
    private int acceptanceCertificateId;
    private int keysId;
    private int lineNumber;

    public AcceptanceCertificateItem(Map<String, Object> row) {
        this.acceptanceCertificateId = Values.integer(row, "acceptance_certificate_id");
        this.keysId = Values.integer(row, "keys_id");
        this.lineNumber = Values.integer(row, "line_number");
    }
    public int getAcceptanceCertificateId() { return acceptanceCertificateId; }
    public int getKeysId() { return keysId; }
    public int getLineNumber() { return lineNumber; }
}
