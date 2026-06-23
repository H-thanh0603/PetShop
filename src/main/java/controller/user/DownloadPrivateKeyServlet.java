package controller.user;

import DAO.OrderSignDAO;
import Model.OrderSign;
import Model.User;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.Base64;

@WebServlet("/user/download-private-key")
public class DownloadPrivateKeyServlet extends HttpServlet {

    private final OrderSignDAO orderSignDAO = new OrderSignDAO();

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null) {
            response.sendError(401);
            return;
        }

        User user = (User) session.getAttribute("user");
        if (user == null) {
            response.sendError(401);
            return;
        }

        String orderIdParam = request.getParameter("orderId");
        if (orderIdParam == null || orderIdParam.isEmpty()) {
            response.sendError(400);
            return;
        }

        int orderId;
        try {
            orderId = Integer.parseInt(orderIdParam);
        } catch (NumberFormatException e) {
            response.sendError(400);
            return;
        }

        OrderSign orderSign = orderSignDAO.findByOrderId(orderId);
        if (orderSign == null) {
            response.sendError(404);
            return;
        }

        if (orderSign.getUserId() != user.getId()) {
            response.sendError(403);
            return;
        }

        String privateKeyBase64 = orderSign.getPrivateKey();
        if (privateKeyBase64 == null || privateKeyBase64.isEmpty()) {
            response.sendError(404);
            return;
        }

        try {
            byte[] keyBytes = Base64.getDecoder().decode(privateKeyBase64);
            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition",
                    "attachment; filename=\"private_key_" + orderId + ".der\"");
            response.setContentLength(keyBytes.length);
            response.getOutputStream().write(keyBytes);
            response.getOutputStream().flush();
        } catch (Exception e) {
            response.sendError(500);
        }
    }
}
