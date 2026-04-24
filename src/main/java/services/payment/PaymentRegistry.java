package services.payment;

import java.util.HashMap;
import java.util.Map;

/**
 * Registry mapping payment method keys to their PaymentProvider implementations.
 * Providers are registered at startup; CheckoutServlet looks up by key.
 *
 * To add a new payment method:
 *   1. Implement PaymentProvider
 *   2. Call PaymentRegistry.getInstance().register("key", new YourProvider())
 *      — no changes to CheckoutServlet needed.
 */
public class PaymentRegistry {

    private static final PaymentRegistry INSTANCE = new PaymentRegistry();
    private final Map<String, PaymentProvider> providers = new HashMap<>();

    private PaymentRegistry() {
        // Register built-in providers
        register("cod",           new CODPaymentProvider());
        register("momo",          new MoMoPaymentProvider());
        register("bank_transfer", new BankTransferPaymentProvider());
    }

    public static PaymentRegistry getInstance() {
        return INSTANCE;
    }

    public void register(String key, PaymentProvider provider) {
        if (key == null || provider == null) throw new IllegalArgumentException("key and provider must not be null");
        providers.put(key.toLowerCase(), provider);
    }

    /**
     * Look up a provider by key.
     * @return the provider, or null if not found
     */
    public PaymentProvider get(String key) {
        if (key == null) return null;
        return providers.get(key.toLowerCase());
    }
}
