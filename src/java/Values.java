import java.time.LocalDate;
import java.util.Map;

final class Values {
    static String string(Map<String,Object> row, String key) {
        Object v = row.get(key); return v == null ? null : (String) v;
    }
    static int integer(Map<String,Object> row, String key) {
        Object value = row.get(key);
        return value instanceof java.math.BigDecimal ? ((java.math.BigDecimal) value).intValueExact() : Math.toIntExact(((Number) value).longValue());
    }
    static long longInteger(Map<String,Object> row, String key) {
        Object value = row.get(key);
        return value instanceof java.math.BigDecimal ? ((java.math.BigDecimal) value).longValueExact() : ((Number) value).longValue();
    }
    static double decimal(Map<String,Object> row, String key) { return ((Number) row.get(key)).doubleValue(); }
    static boolean bool(Map<String,Object> row, String key) { return (Boolean) row.get(key); }
    static LocalDate date(Map<String,Object> row, String key) { return LocalDate.parse(string(row, key)); }
}
