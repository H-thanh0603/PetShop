package controller.updateinformation;

import DAO.AddressDao;
import Model.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.sql.Timestamp;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AddressServletCheckoutFlowTest {

    @Test
    @DisplayName("adding an address from my account should save it as the active address")
    void addFromMyAccountShouldForceDefault() throws Exception {
        AddressDao dao = mock(AddressDao.class);
        AddressServlet servlet = new AddressServlet();
        injectField(servlet, "dao", dao);

        User user = new User();
        user.setId(7);

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);

        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(user);
        when(request.getParameter("_method")).thenReturn(null);
        when(request.getParameter("addressDetail")).thenReturn("12 Tran Hung Dao");
        when(request.getParameter("province")).thenReturn("Ho Chi Minh");
        when(request.getParameter("district")).thenReturn("Quan 5");
        when(request.getParameter("ward")).thenReturn("Phuong 2");
        when(request.getParameter("source")).thenReturn("account");
        when(request.getParameter("redirect")).thenReturn("account");
        when(request.getContextPath()).thenReturn("/petshop");
        when(dao.hasAnyAddress(7)).thenReturn(true);
        when(dao.addAddress(eq(7), eq(true), any(Timestamp.class), eq("12 Tran Hung Dao"),
                eq("Ho Chi Minh"), eq("Quan 5"), eq("Phuong 2"))).thenReturn(true);

        servlet.doPost(request, response);

        verify(dao).addAddress(
                eq(7),
                eq(true),
                any(Timestamp.class),
                eq("12 Tran Hung Dao"),
                eq("Ho Chi Minh"),
                eq("Quan 5"),
                eq("Phuong 2")
        );
        verify(response).sendRedirect("/petshop/my-account");
    }

    @Test
    @DisplayName("adding an address from checkout should save it as the active address")
    void addFromCheckoutShouldForceDefault() throws Exception {
        AddressDao dao = mock(AddressDao.class);
        AddressServlet servlet = new AddressServlet();
        injectField(servlet, "dao", dao);

        User user = new User();
        user.setId(7);

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);

        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(user);
        when(request.getParameter("_method")).thenReturn(null);
        when(request.getParameter("addressDetail")).thenReturn("123 Nguyen Hue");
        when(request.getParameter("province")).thenReturn("Ho Chi Minh");
        when(request.getParameter("district")).thenReturn("Quan 1");
        when(request.getParameter("ward")).thenReturn("Ben Nghe");
        when(request.getParameter("source")).thenReturn("checkout");
        when(request.getParameter("redirect")).thenReturn("checkout");
        when(request.getContextPath()).thenReturn("/petshop");
        when(dao.hasAnyAddress(7)).thenReturn(true);
        when(dao.addAddress(eq(7), eq(true), any(Timestamp.class), eq("123 Nguyen Hue"),
                eq("Ho Chi Minh"), eq("Quan 1"), eq("Ben Nghe"))).thenReturn(true);

        servlet.doPost(request, response);

        verify(dao).addAddress(
                eq(7),
                eq(true),
                any(Timestamp.class),
                eq("123 Nguyen Hue"),
                eq("Ho Chi Minh"),
                eq("Quan 1"),
                eq("Ben Nghe")
        );
        verify(response).sendRedirect("/petshop/checkout");
    }

    @Test
    @DisplayName("setting default address should work through post and return to my account")
    void setDefaultAddressShouldUsePostFlow() throws Exception {
        AddressDao dao = mock(AddressDao.class);
        AddressServlet servlet = new AddressServlet();
        injectField(servlet, "dao", dao);

        User user = new User();
        user.setId(7);

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);

        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(user);
        when(request.getParameter("_method")).thenReturn("patch");
        when(request.getParameter("action")).thenReturn("setDefault");
        when(request.getParameter("id")).thenReturn("13");
        when(request.getParameter("redirect")).thenReturn("account");
        when(request.getContextPath()).thenReturn("/petshop");
        when(dao.setDefaultAddress(7, 13)).thenReturn(true);

        servlet.doPost(request, response);

        verify(dao).setDefaultAddress(7, 13);
        verify(response).sendRedirect("/petshop/my-account");
    }

    @Test
    @DisplayName("updating an address from checkout should keep it as the active address")
    void updateFromCheckoutShouldForceDefault() throws Exception {
        AddressDao dao = mock(AddressDao.class);
        AddressServlet servlet = new AddressServlet();
        injectField(servlet, "dao", dao);

        User user = new User();
        user.setId(7);

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);

        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(user);
        when(request.getParameter("_method")).thenReturn("put");
        when(request.getParameter("id")).thenReturn("11");
        when(request.getParameter("addressDetail")).thenReturn("456 Le Loi");
        when(request.getParameter("province")).thenReturn("Ho Chi Minh");
        when(request.getParameter("district")).thenReturn("Quan 1");
        when(request.getParameter("ward")).thenReturn("Ben Thanh");
        when(request.getParameter("source")).thenReturn("checkout");
        when(request.getParameter("redirect")).thenReturn("checkout");
        when(request.getContextPath()).thenReturn("/petshop");
        when(dao.updateAddress(eq(11), eq(7), eq(true), any(Timestamp.class), eq("456 Le Loi"),
                eq("Ho Chi Minh"), eq("Quan 1"), eq("Ben Thanh"))).thenReturn(true);

        servlet.doPost(request, response);

        verify(dao).updateAddress(
                eq(11),
                eq(7),
                eq(true),
                any(Timestamp.class),
                eq("456 Le Loi"),
                eq("Ho Chi Minh"),
                eq("Quan 1"),
                eq("Ben Thanh")
        );
        verify(response).sendRedirect("/petshop/checkout");
    }

    private static void injectField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
