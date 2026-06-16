package DAO;

import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLSyntaxErrorException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProductDAOStockReservationTest {

    @Test
    void reserveStockTracksReservedQuantityInsteadOfSellingImmediately() throws Exception {
        ProductDAO productDAO = new ProductDAO();
        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        String[] sql = new String[1];

        when(conn.prepareStatement(anyString())).thenAnswer(invocation -> {
            sql[0] = invocation.getArgument(0);
            return ps;
        });
        when(ps.executeUpdate()).thenReturn(1);

        assertTrue(productDAO.reserveStock(conn, 11, 2));

        assertTrue(sql[0].contains("reserved_quantity"),
                "reserveStock must increase reserved_quantity so finalize/release operate on the same stock model");
        verify(ps).setInt(1, 2);
        verify(ps).setInt(2, 2);
        verify(ps).setInt(3, 11);
        verify(ps).setInt(4, 2);
    }

    @Test
    void reserveStockFallsBackToLegacyStockDecrementWhenReservedColumnIsMissing() throws Exception {
        ProductDAO productDAO = new ProductDAO();
        Connection conn = mock(Connection.class);
        PreparedStatement modernPs = mock(PreparedStatement.class);
        PreparedStatement legacyPs = mock(PreparedStatement.class);
        List<String> preparedSql = new ArrayList<>();

        when(conn.prepareStatement(anyString())).thenAnswer(invocation -> {
            String sql = invocation.getArgument(0);
            preparedSql.add(sql);
            return preparedSql.size() == 1 ? modernPs : legacyPs;
        });
        when(modernPs.executeUpdate()).thenThrow(unknownColumn("reserved_quantity"));
        when(legacyPs.executeUpdate()).thenReturn(1);

        assertTrue(productDAO.reserveStock(conn, 395, 1));
        assertTrue(preparedSql.get(0).contains("reserved_quantity"));
        assertTrue(preparedSql.stream().anyMatch(sql -> sql.contains("SET stock = stock - ?")
                && !sql.contains("reserved_quantity")));
    }

    @Test
    void releaseReservedStockFallsBackToLegacyStockIncreaseWhenReservedColumnIsMissing() throws Exception {
        ProductDAO productDAO = new ProductDAO();
        Connection conn = mock(Connection.class);
        PreparedStatement modernPs = mock(PreparedStatement.class);
        PreparedStatement legacyPs = mock(PreparedStatement.class);
        List<String> preparedSql = new ArrayList<>();

        when(conn.prepareStatement(anyString())).thenAnswer(invocation -> {
            String sql = invocation.getArgument(0);
            preparedSql.add(sql);
            return preparedSql.size() == 1 ? modernPs : legacyPs;
        });
        when(modernPs.executeUpdate()).thenThrow(unknownColumn("reserved_quantity"));
        when(legacyPs.executeUpdate()).thenReturn(1);

        assertTrue(productDAO.releaseReservedStock(conn, 395, 1));
        assertTrue(preparedSql.stream().anyMatch(sql -> sql.contains("SET stock = stock + ?")
                && !sql.contains("reserved_quantity")));
    }

    @Test
    void finalizeReservedStockIsNoopWhenLegacyReserveAlreadyDecrementedStock() throws Exception {
        ProductDAO productDAO = new ProductDAO();
        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);

        when(conn.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeUpdate()).thenThrow(unknownColumn("reserved_quantity"));

        assertTrue(productDAO.finalizeReservedStock(conn, 395, 1));
    }

    private SQLSyntaxErrorException unknownColumn(String column) {
        return new SQLSyntaxErrorException(
                "Unknown column '" + column + "' in 'where clause'",
                "42S22",
                1054
        );
    }
}
