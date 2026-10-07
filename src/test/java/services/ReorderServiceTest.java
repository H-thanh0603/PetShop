package services;

import com.petshop.repository.CartRepository;
import com.petshop.repository.OrderRepository;
import com.petshop.model.Order;
import com.petshop.model.OrderItem;
import com.petshop.model.Product;
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
        OrderRepository orderDAO = mock(OrderRepository.class);
        CartRepository cartDAO = mock(CartRepository.class);

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
        OrderRepository orderDAO = mock(OrderRepository.class);
        CartRepository cartDAO = mock(CartRepository.class);

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
