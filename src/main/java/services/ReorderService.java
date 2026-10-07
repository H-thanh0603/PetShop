package services;

import org.springframework.stereotype.Service;

import com.petshop.repository.CartRepository;
import com.petshop.repository.OrderRepository;
import com.petshop.model.Order;
import com.petshop.model.OrderItem;

@Service
public class ReorderService {
    private final OrderRepository orderDAO;
    private final CartRepository cartDAO;

    public ReorderService(OrderRepository orderDAO, CartRepository cartDAO) {
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
