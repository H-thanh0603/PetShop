package DAO;

import Model.Order;
import Model.OrderItem;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLSyntaxErrorException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class OrderDAOLegacySchemaFallbackTest {

    @Test
    void saveOrderFallsBackWhenTotalsColumnsDoNotExistYet() throws Exception {
        Connection conn = mock(Connection.class);
        PreparedStatement modernPs = mock(PreparedStatement.class);
        PreparedStatement legacyPs = mock(PreparedStatement.class);
        PreparedStatement logPs = mock(PreparedStatement.class);
        Statement stmt = mock(Statement.class);
        ResultSet generatedKeys = mock(ResultSet.class);
        List<String> preparedSql = new ArrayList<>();

        when(conn.createStatement()).thenReturn(stmt);
        when(conn.prepareStatement(anyString(), anyInt())).thenAnswer(invocation -> {
            String sql = invocation.getArgument(0);
            preparedSql.add(sql);
            return preparedSql.size() == 1 ? modernPs : legacyPs;
        });
        when(conn.prepareStatement(anyString())).thenAnswer(invocation -> {
            String sql = invocation.getArgument(0);
            preparedSql.add(sql);
            return logPs;
        });
        when(modernPs.executeUpdate()).thenThrow(unknownColumn("subtotal"));
        when(legacyPs.executeUpdate()).thenReturn(1);
        when(legacyPs.getGeneratedKeys()).thenReturn(generatedKeys);
        when(generatedKeys.next()).thenReturn(true);
        when(generatedKeys.getInt(1)).thenReturn(789);
        when(logPs.executeUpdate()).thenReturn(1);

        int orderId = new OrderDAO().saveOrder(conn, order());

        assertEquals(789, orderId);
        assertTrue(preparedSql.get(0).contains("subtotal"));
        assertTrue(preparedSql.stream().anyMatch(sql -> sql.startsWith("INSERT INTO orders")
                && !sql.contains("subtotal")));
    }

    @Test
    void saveOrderItemFallsBackWhenSnapshotColumnsDoNotExistYet() throws Exception {
        Connection conn = mock(Connection.class);
        PreparedStatement modernPs = mock(PreparedStatement.class);
        PreparedStatement legacyPs = mock(PreparedStatement.class);
        List<String> preparedSql = new ArrayList<>();

        when(conn.prepareStatement(anyString())).thenAnswer(invocation -> {
            String sql = invocation.getArgument(0);
            preparedSql.add(sql);
            return preparedSql.size() == 1 ? modernPs : legacyPs;
        });
        when(modernPs.executeUpdate()).thenThrow(unknownColumn("product_name_snapshot"));
        when(legacyPs.executeUpdate()).thenReturn(1);

        assertTrue(new OrderDAO().saveOrderItem(conn, orderItem()));
        assertTrue(preparedSql.get(0).contains("product_name_snapshot"));
        assertTrue(preparedSql.stream().anyMatch(sql -> sql.startsWith("INSERT INTO order_items")
                && !sql.contains("product_name_snapshot")));
    }

    private Order order() {
        Order order = new Order();
        order.setUserId(9);
        order.setRecipientFullname("Test User");
        order.setRecipientPhone("0900000000");
        order.setShippingAddress("Test address");
        order.setNote("");
        order.setSubtotal(new BigDecimal("100000"));
        order.setShippingFee(BigDecimal.ZERO);
        order.setDiscountAmount(BigDecimal.ZERO);
        order.setTotalAmount(new BigDecimal("100000"));
        order.setStatus("Pending");
        order.setPayment_method("COD");
        order.setPayment_status(false);
        order.setCreatedAt(Timestamp.valueOf(LocalDateTime.now()));
        return order;
    }

    private OrderItem orderItem() {
        OrderItem item = new OrderItem();
        item.setOrderId(789);
        item.setProductId(11);
        item.setQuantity(1);
        item.setPrice(new BigDecimal("100000"));
        item.setOriginalPrice(new BigDecimal("100000"));
        item.setFinalPrice(new BigDecimal("100000"));
        item.setDiscountAmount(BigDecimal.ZERO);
        item.setProductNameSnapshot("Product");
        item.setProductImageSnapshot("product.jpg");
        return item;
    }

    private SQLSyntaxErrorException unknownColumn(String column) {
        return new SQLSyntaxErrorException(
                "Unknown column '" + column + "' in 'field list'",
                "42S22",
                1054
        );
    }
}
