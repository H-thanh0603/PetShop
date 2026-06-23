package Model;

import java.util.EnumSet;
import java.util.Set;

/**
 * State machine for order status transitions.
 *
 * Valid transitions:
 *   Awaiting Payment → Paid, Pending, Cancelled
 *   Paid             → Confirmed, Cancelled
 *   Pending          → Confirmed, Paid, Cancelled
 *   Confirmed        → Shipping,  Cancelled
 *   Shipping         → Delivered, Cancelled
 *   Delivered        → Completed
 *   Completed        → (none)
 *   Cancelled        → (none)
 */
public enum OrderStatus {

    AWAITING_PAYMENT("Awaiting Payment") {
        @Override
        public Set<OrderStatus> validTargets() {
            return EnumSet.of(PAID, PENDING, CANCELLED);
        }
    },
    PAID("Paid") {
        @Override
        public Set<OrderStatus> validTargets() {
            return EnumSet.of(CONFIRMED, CANCELLED);
        }
    },
    PENDING("Pending") {
        @Override
        public Set<OrderStatus> validTargets() {
            return EnumSet.of(CONFIRMED, PAID, CANCELLED);
        }
    },
    CONFIRMED("Confirmed") {
        @Override
        public Set<OrderStatus> validTargets() {
            return EnumSet.of(SHIPPING, CANCELLED);
        }
    },
    SHIPPING("Shipping") {
        @Override
        public Set<OrderStatus> validTargets() {
            return EnumSet.of(DELIVERED, CANCELLED);
        }
    },
    DELIVERED("Delivered") {
        @Override
        public Set<OrderStatus> validTargets() {
            return EnumSet.of(COMPLETED);
        }
    },
    COMPLETED("Completed") {
        @Override
        public Set<OrderStatus> validTargets() {
            return EnumSet.noneOf(OrderStatus.class);
        }
    },
    CANCELLED("Cancelled") {
        @Override
        public Set<OrderStatus> validTargets() {
            return EnumSet.noneOf(OrderStatus.class);
        }
    };
    
    VERIFY_FAILED("Xác thực thất bại") {
    @Override
    public Set<OrderStatus> validTargets() {
        return EnumSet.noneOf(OrderStatus.class);
        }
    },
    private final String displayName;

    OrderStatus(String displayName) {
        this.displayName = displayName;
    }

    /** The canonical string value stored in the database (e.g. "Pending"). */
    public String getDisplayName() {
        return displayName;
    }

    /** Returns the set of states this status may legally transition to. */
    public abstract Set<OrderStatus> validTargets();

    /**
     * Returns {@code true} if transitioning from {@code this} to {@code target}
     * is a valid state-machine move.
     */
    public boolean canTransitionTo(OrderStatus target) {
        return validTargets().contains(target);
    }

    /**
     * Case-insensitive lookup by display name (e.g. "pending", "Confirmed").
     *
     * @return the matching {@link OrderStatus}, or {@code null} if not found.
     */
    public static OrderStatus fromString(String s) {
        if (s == null) {
            return null;
        }
        for (OrderStatus os : values()) {
            if (os.displayName.equalsIgnoreCase(s)) {
                return os;
            }
        }
        return null;
    }
}
