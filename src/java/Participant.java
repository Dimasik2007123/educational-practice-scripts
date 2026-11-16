/** A person taking part in a sale or booking. */
public interface Participant {
    String role();
    String describe();
    boolean canPurchase();
}
