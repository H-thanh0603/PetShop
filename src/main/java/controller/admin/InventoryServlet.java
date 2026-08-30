package controller.admin;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import DAO.InventoryBatchDAO;
import DAO.ProductDAO;
import DAO.AdminActionLogDAO;
import Model.InventoryBatch;
import Model.Product;
import Model.ProductAdminInventoryView;
import Model.User;

public class InventoryServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private final AdminActionLogDAO actionLog = new AdminActionLogDAO();

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        ProductDAO productDAO = new ProductDAO();
        InventoryBatchDAO inventoryBatchDAO = new InventoryBatchDAO();
        
        List<Product> products = productDAO.getAllProducts();
        Map<Integer, ProductAdminInventoryView> inventoryByProduct = inventoryBatchDAO.getProductAdminInventoryViews(30);
        
        int lowStockCount = 0;
        int nearExpiryCount = 0;
        int expiredCount = 0;
        
        for (Product p : products) {
            if (p.getStock() > 0 && p.getStock() < 10) {
                lowStockCount++;
            }
            ProductAdminInventoryView view = inventoryByProduct.get(p.getId());
            if (view != null) {
                if (view.getExpiredQuantity() > 0) {
                    expiredCount++;
                } else if (view.getNearExpiryQuantity() > 0) {
                    nearExpiryCount++;
                }
            }
        }
        
        request.setAttribute("products", products);
        request.setAttribute("inventoryByProduct", inventoryByProduct);
        request.setAttribute("lowStockCount", lowStockCount);
        request.setAttribute("nearExpiryCount", nearExpiryCount);
        request.setAttribute("expiredCount", expiredCount);
        
        String productIdStr = request.getParameter("productId");
        if (productIdStr != null && !productIdStr.isEmpty()) {
            try {
                int productId = Integer.parseInt(productIdStr);
                List<InventoryBatch> batches = inventoryBatchDAO.findAllocatableBatchesForProduct(productId);
                request.setAttribute("selectedProductBatches", batches);
                request.setAttribute("selectedProductId", productId);
            } catch (NumberFormatException e) {
                // Ignore
            }
        }
        
        request.getRequestDispatcher("/pages/admin/inventory.jsp").forward(request, response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");
        
        String action = request.getParameter("action");
        InventoryBatchDAO inventoryBatchDAO = new InventoryBatchDAO();
        
        if ("addBatch".equals(action)) {
            try {
                int productId = Integer.parseInt(request.getParameter("productId"));
                int quantity = Integer.parseInt(request.getParameter("quantity"));
                BigDecimal cost = new BigDecimal(request.getParameter("unitCost"));
                String expiryStr = request.getParameter("expiryDate");
                String batchCode = request.getParameter("batchCode");
                String note = request.getParameter("note");
                
                InventoryBatch batch = new InventoryBatch();
                batch.setProductId(productId);
                batch.setReceivedQuantity(quantity);
                batch.setRemainingQuantity(quantity);
                batch.setUnitCost(cost);
                batch.setBatchCode(batchCode);
                batch.setNote(note);
                batch.setReceivedAt(Timestamp.valueOf(LocalDateTime.now()));
                
                if (expiryStr != null && !expiryStr.isEmpty()) {
                    LocalDate expiryDate = LocalDate.parse(expiryStr);
                    batch.setExpiryDate(Timestamp.valueOf(LocalDateTime.of(expiryDate, LocalTime.MAX)));
                }
                
                boolean success = inventoryBatchDAO.recordImportBatch(batch, user != null ? user.getId() : null);
                
                if (success) {
                    session.setAttribute("message", "Nhập lô hàng mới thành công!");
                    session.setAttribute("messageType", "success");
                    if (user != null) {
                        actionLog.log(user.getId(), "IMPORT_STOCK", "PRODUCT", productId, "SL: " + quantity + ", Cost: " + cost);
                    }
                } else {
                    session.setAttribute("message", "Lỗi khi nhập lô hàng.");
                    session.setAttribute("messageType", "error");
                }
            } catch (Exception e) {
                session.setAttribute("message", "Dữ liệu không hợp lệ: " + e.getMessage());
                session.setAttribute("messageType", "error");
            }
        }
        
        response.sendRedirect(request.getContextPath() + "/admin/inventory");
    }
}
