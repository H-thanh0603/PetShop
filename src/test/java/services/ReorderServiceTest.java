package services;

import DAO.CartDAO;
import DAO.OrderDAO;
import Model.Order;
import Model.OrderItem;
import Model.Product;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class ReorderServiceTest {

    @Test
    void reordersOnlyOrdersOwnedByCurrentUser() {
        OrderDAO orderDAO = mock(OrderDAO.class);
        CartDAO cartDAO = mock(CartDAO.class);

        Order order = new Order();
        order.setId(42);
        order.setUserId(7);
        order.setItems(List.of(orderItem(11, 2), orderItem(19, 1)));
        when(orderDAO.getOrderById(42)).thenReturn(order);

        ReorderService service = new ReorderService(orderDAO, cartDAO);

        assertTrue(service.reorderToCart(7, 42));
        verify(cartDAO).addToCart(7, 11, 2);
        verify(cartDAO).addToCart(7, 19, 1);
    }

    @Test
    void refusesToReorderAnotherUsersOrder() {
        OrderDAO orderDAO = mock(OrderDAO.class);
        CartDAO cartDAO = mock(CartDAO.class);

        Order order = new Order();
        order.setId(42);
        order.setUserId(8);
        order.setItems(List.of(orderItem(11, 2)));
        when(orderDAO.getOrderById(42)).thenReturn(order);

        ReorderService service = new ReorderService(orderDAO, cartDAO);

        assertFalse(service.reorderToCart(7, 42));
        verifyNoInteractions(cartDAO);
    }

    private OrderItem orderItem(int productId, int quantity) {
        Product product = new Product();
        product.setId(productId);
        OrderItem item = new OrderItem();
        item.setProductId(productId);
        item.setProduct(product);
        item.setQuantity(quantity);
        return item;
    }
}
