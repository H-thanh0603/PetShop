package services;

import Context.DBContext;
import DAO.CartDAO;
import DAO.CouponDao;
import DAO.InventoryBatchDAO;
import DAO.OrderDAO;
import DAO.PaymentTransactionDAO;
import DAO.ProductDAO;
import DAO.UserDAO;
import Model.CartItem;
import Model.CouponValidationResult;
import Model.Order;
import Model.Product;
import Model.User;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CheckoutServiceInventoryBatchTest {

    @Test
    void processCheckoutConsumesTrackedInventoryBatchesBeforeUpdatingProductStock() throws Exception {
        ProductDAO productDAO = mock(ProductDAO.class);
        UserDAO userDAO = mock(UserDAO.class);
        CouponDao couponDao = mock(CouponDao.class);
        OrderDAO orderDAO = mock(OrderDAO.class);
        PaymentTransactionDAO paymentTransactionDAO = mock(PaymentTransactionDAO.class);
        CartDAO cartDAO = mock(CartDAO.class);
        OrderEmailService orderEmailService = mock(OrderEmailService.class);
        InventoryBatchDAO inventoryBatchDAO = mock(InventoryBatchDAO.class);

        Connection conn = mock(Connection.class);
        User user = new User();
        user.setId(7);
        user.setFullname("Nguyen Van A");
        user.setPhone("0901234567");

        Product latestProduct = new Product();
        latestProduct.setId(11);
        latestProduct.setName("Pate meo");
        latestProduct.setPrice(new BigDecimal("80000"));
        latestProduct.setStock(5);

        Product cartProduct = new Product();
        cartProduct.setId(11);
        cartProduct.setPrice(new BigDecimal("80000"));

        Map<Integer, CartItem> cart = new HashMap<>();
        cart.put(11, new CartItem(cartProduct, 2));

        when(productDAO.getProductByIdForUpdate(conn, 11)).thenReturn(latestProduct);
        when(orderDAO.saveOrder(eq(conn), any(Order.class))).thenReturn(901);
        when(orderDAO.saveOrderItem(eq(conn), any())).thenReturn(true);
        when(paymentTransactionDAO.save(eq(conn), any())).thenReturn(77);
        when(productDAO.decreaseStock(conn, 11, 2)).thenReturn(true);
        when(inventoryBatchDAO.hasTrackedBatchesForProduct(conn, 11)).thenReturn(true);
        when(inventoryBatchDAO.consumeProductStock(conn, 11, 2, 901, 7,
                "Checkout order #901")).thenReturn(true);

        CheckoutService service = new CheckoutService(
                productDAO, userDAO, couponDao, orderDAO, paymentTransactionDAO,
                cartDAO, orderEmailService, inventoryBatchDAO
        );

        try (MockedStatic<DBContext> mockedDBContext = mockStatic(DBContext.class)) {
            mockedDBContext.when(DBContext::getConnection).thenReturn(conn);

            CheckoutResult result = service.processCheckout(
                    user,
                    cart,
                    "123 Nguyen Hue, Phuong Ben Nghe, Quan 1, Ho Chi Minh",
                    "",
                    CouponValidationResult.empty(),
                    "cod",
                    30000
            );

            assertTrue(result.isSuccess());
        }

        verify(inventoryBatchDAO).consumeProductStock(conn, 11, 2, 901, 7,
                "Checkout order #901");
    }
}
