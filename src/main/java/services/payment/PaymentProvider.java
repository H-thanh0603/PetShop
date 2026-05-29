package services.payment;

/**
 * Contract for all payment method implementations.
 * Add new payment methods by implementing this interface and registering
 * them in PaymentRegistry — no changes to CheckoutServlet required.
 */
public interface PaymentProvider {
    /**
     * Process a payment of the given amount.
     *
     * @param amount total amount to charge
     * @return PaymentResult indicating success/failure, DB identifier, and paid status
     */
    PaymentResult process(double amount);
}
