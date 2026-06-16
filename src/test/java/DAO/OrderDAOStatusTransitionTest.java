package DAO;

import Context.DBContext;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

class OrderDAOStatusTransitionTest {

    @Test
    void onlineUnpaidOrderCannotBeConfirmedByAdmin() throws Exception {
        Connection conn = mock(Connection.class);
        Statement stmt = mock(Statement.class);
        PreparedStatement genericPs = mock(PreparedStatement.class);
        PreparedStatement lockPs = mock(PreparedStatement.class);
        ResultSet lockRs = mock(ResultSet.class);
        List<String> preparedSql = new ArrayList<>();

        when(conn.createStatement()).thenReturn(stmt);
        when(lockRs.next()).thenReturn(true);
        when(lockRs.getString("status")).thenReturn("Pending");
        when(lockRs.getString("payment_method")).thenReturn("BANK_TRANSFER");
        when(lockRs.getBoolean("payment_status")).thenReturn(false);
        when(lockPs.executeQuery()).thenReturn(lockRs);
        when(genericPs.executeUpdate()).thenReturn(1);

        when(conn.prepareStatement(anyString())).thenAnswer(invocation -> {
            String sql = invocation.getArgument(0);
            preparedSql.add(sql);
            if (sql.contains("FROM orders") && sql.contains("FOR UPDATE")) {
                return lockPs;
            }
            return genericPs;
        });

        try (MockedStatic<DBContext> mockedDBContext = mockStatic(DBContext.class)) {
            mockedDBContext.when(DBContext::getConnection).thenReturn(conn);

            assertFalse(new OrderDAO().updateStatus(123, "Confirmed", 9));
        }

        assertFalse(preparedSql.stream().anyMatch(sql -> sql.startsWith("UPDATE orders SET status")),
                "Unpaid online orders must be rejected before updating order status");
    }

    @Test
    void completingCodOrderMarksPaymentPaid() throws Exception {
        Connection conn = mock(Connection.class);
        Statement stmt = mock(Statement.class);
        PreparedStatement genericPs = mock(PreparedStatement.class);
        PreparedStatement lockPs = mock(PreparedStatement.class);
        ResultSet lockRs = mock(ResultSet.class);
        ResultSet emptyRs = mock(ResultSet.class);
        List<String> preparedSql = new ArrayList<>();

        when(conn.createStatement()).thenReturn(stmt);
        when(lockRs.next()).thenReturn(true);
        when(lockRs.getString("status")).thenReturn("Delivered");
        when(lockRs.getString("payment_method")).thenReturn("COD");
        when(lockRs.getBoolean("payment_status")).thenReturn(false);
        when(lockPs.executeQuery()).thenReturn(lockRs);
        when(emptyRs.next()).thenReturn(false);
        when(genericPs.executeQuery()).thenReturn(emptyRs);
        when(genericPs.executeUpdate()).thenReturn(1);

        when(conn.prepareStatement(anyString())).thenAnswer(invocation -> {
            String sql = invocation.getArgument(0);
            preparedSql.add(sql);
            if (sql.contains("FROM orders") && sql.contains("FOR UPDATE")) {
                return lockPs;
            }
            return genericPs;
        });

        try (MockedStatic<DBContext> mockedDBContext = mockStatic(DBContext.class)) {
            mockedDBContext.when(DBContext::getConnection).thenReturn(conn);

            assertTrue(new OrderDAO().updateStatus(123, "Completed", 9));
        }

        assertTrue(preparedSql.stream().anyMatch(sql -> sql.contains("UPDATE orders SET payment_status")),
                "Completed COD orders must mark payment_status as paid");
    }
}
