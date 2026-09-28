package com.petshop.web;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.petshop.repository.AdminActionLogRepository;
import com.petshop.repository.InventoryBatchRepository;
import com.petshop.dao.ProductDAO;
import com.petshop.model.InventoryBatch;
import com.petshop.model.Product;
import com.petshop.model.ProductAdminInventoryView;
import com.petshop.model.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

/**
 * Replaces InventoryServlet (/admin/inventory) 1:1 — same overview page,
 * same per-product batch panel, same addBatch import flow. AuthorizationFilter
 * still guards all admin paths.
 */
@Controller
public class AdminInventoryController {

    private final ProductDAO productDAO;
    private final InventoryBatchRepository inventoryBatchDAO;
    private final AdminActionLogRepository actionLog;

    @Autowired
    public AdminInventoryController(ProductDAO productDAO, InventoryBatchRepository inventoryBatchDAO,
                                    AdminActionLogRepository actionLog) {
        this.productDAO = productDAO;
        this.inventoryBatchDAO = inventoryBatchDAO;
        this.actionLog = actionLog;
    }


    @GetMapping("/admin/inventory")
    public String inventory(
            @RequestParam(value = "productId", required = false) String productIdRaw,
            Model model) {
        List<Product> products = productDAO.getAllProducts();
        Map<Integer, ProductAdminInventoryView> inventoryByProduct =
                inventoryBatchDAO.getProductAdminInventoryViews(30);

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

        model.addAttribute("products", products);
        model.addAttribute("inventoryByProduct", inventoryByProduct);
        model.addAttribute("lowStockCount", lowStockCount);
        model.addAttribute("nearExpiryCount", nearExpiryCount);
        model.addAttribute("expiredCount", expiredCount);

        if (productIdRaw != null && !productIdRaw.isEmpty()) {
            try {
                int productId = Integer.parseInt(productIdRaw);
                List<InventoryBatch> batches = inventoryBatchDAO.findAllocatableBatchesForProduct(productId);
                model.addAttribute("selectedProductBatches", batches);
                model.addAttribute("selectedProductId", productId);
            } catch (NumberFormatException e) {
                // Ignore
            }
        }

        return "pages/admin/inventory";
    }

    @PostMapping("/admin/inventory")
    public String inventoryPost(
            @RequestParam(value = "action", required = false) String action,
            @RequestParam(value = "productId", required = false) String productIdRaw,
            @RequestParam(value = "quantity", required = false) String quantityRaw,
            @RequestParam(value = "unitCost", required = false) String unitCostRaw,
            @RequestParam(value = "expiryDate", required = false) String expiryStr,
            @RequestParam(value = "batchCode", required = false) String batchCode,
            @RequestParam(value = "note", required = false) String note,
            HttpServletRequest request,
            HttpSession session) {
        User user = (User) session.getAttribute("user");

        if ("addBatch".equals(action)) {
            try {
                int productId = Integer.parseInt(productIdRaw);
                int quantity = Integer.parseInt(quantityRaw);
                BigDecimal cost = new BigDecimal(unitCostRaw);

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

        return "redirect:" + request.getContextPath() + "/admin/inventory";
    }
}
