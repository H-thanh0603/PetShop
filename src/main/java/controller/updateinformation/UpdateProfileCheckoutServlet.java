package controller.updateinformation;

import DAO.UserDAO;
import Model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebServlet("/update-profile-checkout")
public class UpdateProfileCheckoutServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");

        String fullname = request.getParameter("fullname");
        String phone = request.getParameter("phone");

        UserDAO dao = new UserDAO();
        dao.updateProfile(user.getId(), fullname, phone);

        user.setFullname(fullname);
        user.setPhone(phone);

        session.setAttribute("user", user);

        response.sendRedirect(request.getContextPath() + "/checkout");
    }
}
