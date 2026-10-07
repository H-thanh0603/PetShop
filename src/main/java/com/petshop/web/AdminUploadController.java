package com.petshop.web;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.petshop.util.AppConfig;
import com.petshop.util.FileUploadUtil;
import com.petshop.util.FileUploadValidator;
import com.petshop.util.Json;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Replaces FileUploadServlet (/admin/upload) 1:1 — same ?type=product folder
 * routing, same JSON contract, files still land under {@code app.upload-dir}.
 * AuthorizationFilter still guards all admin paths.
 */
@Controller
public class AdminUploadController {

    private static final Logger logger = LoggerFactory.getLogger(AdminUploadController.class);

    private static final String UPLOAD_FOLDER_PRODUCT = "shop_pic";
    private static final String UPLOAD_FOLDER_DEFAULT = "uploads";

    @PostMapping(value = "/admin/upload", produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String upload(
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam(value = "type", required = false) String type,
            HttpServletRequest request) {
        try {
            if (file == null || file.isEmpty()) {
                return writeJson(false, "No file uploaded.");
            }

            byte[] head = new byte[16];
            int headLen = file.getInputStream().read(head);
            FileUploadValidator.ValidationResult validationResult = FileUploadValidator.validate(
                    file.getOriginalFilename(), file.getContentType(), file.getSize(),
                    headLen > 0 ? java.util.Arrays.copyOf(head, headLen) : null);
            if (!validationResult.isValid()) {
                return writeJson(false, validationResult.getErrorMessage());
            }

            if (file.getSize() > FileUploadUtil.MAX_FILE_SIZE) {
                return writeJson(false, "File quá lớn. Tối đa 5MB");
            }

            String uploadFolder = "product".equals(type) ? UPLOAD_FOLDER_PRODUCT : UPLOAD_FOLDER_DEFAULT;

            // Store outside the WAR (configurable) so redeploys keep images.
            // Served back at /assets/images/<folder>/<file> by WebMvcConfig.
            String baseDir = AppConfig.getOrDefault("app.upload-dir",
                    System.getProperty("user.dir") + File.separator + "uploads");
            String uploadDir = baseDir + File.separator + uploadFolder;

            String fileName = validationResult.getSecureFileName();

            File dir = new File(uploadDir);
            if (!dir.exists()) dir.mkdirs();

            file.transferTo(new File(uploadDir + File.separator + fileName));

            String fileUrl = request.getContextPath() + "/assets/images/" + uploadFolder + "/" + fileName;
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("fileName", fileName);
            result.put("fileUrl", fileUrl);
            result.put("fileSize", FileUploadUtil.formatFileSize(file.getSize()));
            result.put("message", "Upload thành công!");
            return Json.MAPPER.writeValueAsString(result);
        } catch (Exception e) {
            logger.error("Error uploading file", e);
            try {
                return writeJson(false, "Lỗi server khi upload file");
            } catch (Exception ignored) {
                return "{\"success\":false,\"message\":\"Lỗi server khi upload file\"}";
            }
        }
    }

    private String writeJson(boolean success, String message) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", success);
        result.put("message", message);
        return Json.MAPPER.writeValueAsString(result);
    }

    @GetMapping("/admin/upload")
    public String uploadGet(HttpServletRequest request) {
        // Project ecommerce chỉ hỗ trợ upload ảnh phục vụ quản lý sản phẩm.
        return "redirect:" + request.getContextPath() + "/pages/admin/products";
    }
}
