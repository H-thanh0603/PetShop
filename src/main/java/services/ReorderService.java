package services;

import DAO.CartDAO;
import DAO.OrderDAO;
import Model.Order;
import Model.OrderItem;

public class ReorderService {
    private final OrderDAO orderDAO;
    private final CartDAO cartDAO;

    public ReorderService() {
        this(new OrderDAO(), new CartDAO());
    }

    public ReorderService(OrderDAO orderDAO, CartDAO cartDAO) {
        this.orderDAO = orderDAO;
        this.cartDAO = cartDAO;
    }

    public boolean reorderToCart(int userId, int orderId) {
        Order order = orderDAO.getOrderById(orderId);
        if (order == null || order.getUserId() != userId || order.getItems() == null || order.getItems().isEmpty()) {
            return false;
        }

        for (OrderItem item : order.getItems()) {
            if (item.getProductId() > 0 && item.getQuantity() > 0) {
                cartDAO.addToCart(userId, item.getProductId(), item.getQuantity());
            }
        }
        return true;
    }
}
