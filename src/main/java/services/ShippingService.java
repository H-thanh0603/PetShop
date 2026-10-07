package services;

import org.springframework.stereotype.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.petshop.util.Json;
import com.petshop.util.ShippingConfig;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

import com.petshop.model.Order;
import com.petshop.model.OrderItem;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
public class ShippingService {

    private static final Logger logger = LoggerFactory.getLogger(ShippingService.class);

    private static final String TOKEN = ShippingConfig.get("TOKEN");
    private static final int SHOP_ID = ShippingConfig.getInt("SHOP_ID");
    private static final int FROM_DISTRICT_ID = ShippingConfig.getInt("FROM_DISTRICT_ID");
    private static final String FROM_WARD_CODE = ShippingConfig.get("FROM_WARD_CODE");
    private static final String BASE_URL = ShippingConfig.get("BASE_URL");
    // GHN Order Management credentials (for creating/managing orders)
    private static final String GHN_ORDER_URL = ShippingConfig.get("GHN_ORDER_URL");
    private static final String GHN_ORDER_TOKEN = ShippingConfig.get("GHN_ORDER_TOKEN");
    private static final int GHN_ORDER_SHOP_ID = ShippingConfig.getInt("GHN_ORDER_SHOP_ID");
    private final HttpClient client = HttpClient.newHttpClient();

    private HttpRequest.Builder baseRequest(String url) {
        return HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .header("Token", TOKEN);
    }

    private HttpRequest.Builder ghnOrderRequest(String url) {
        return HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .header("Token", GHN_ORDER_TOKEN)
                .header("ShopId", String.valueOf(GHN_ORDER_SHOP_ID));
    }
    public Integer getProvinceIdByName(String provinceName) throws IOException, InterruptedException {
        HttpRequest request = baseRequest(BASE_URL + "/master-data/province")
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        JsonNode json = parseJsonLenient(response.body());
        JsonNode data = json.path("data");

        for (int i = 0; i < data.size(); i++) {
            JsonNode p = data.get(i);
            String name = p.path("ProvinceName").asString();
            if (normalize(name).equals(normalize(provinceName))) {
                return p.path("ProvinceID").asInt();
            }
        }
        return null;
    }
    public Integer getDistrictIdByName(String provinceName, String districtName) throws IOException, InterruptedException {
        Integer provinceId = getProvinceIdByName(provinceName);
        if (provinceId == null) return null;

        ObjectNode body = Json.MAPPER.createObjectNode();
        body.put("province_id", provinceId);

        HttpRequest request = baseRequest(BASE_URL + "/master-data/district")
                .method("GET", HttpRequest.BodyPublishers.ofString(Json.MAPPER.writeValueAsString(body)))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        JsonNode json = parseJsonLenient(response.body());
        JsonNode data = json.path("data");

        for (int i = 0; i < data.size(); i++) {
            JsonNode d = data.get(i);
            String name = d.path("DistrictName").asString();
            if (normalize(name).equals(normalize(districtName))) {
                return d.path("DistrictID").asInt();
            }
        }
        return null;
    }

    public String getWardCodeByName(String provinceName, String districtName, String wardName) throws IOException, InterruptedException {
        Integer districtId = getDistrictIdByName(provinceName, districtName);
        if (districtId == null) return null;

        ObjectNode body = Json.MAPPER.createObjectNode();
        body.put("district_id", districtId);

        HttpRequest request = baseRequest(BASE_URL + "/master-data/ward?district_id")
                .POST(HttpRequest.BodyPublishers.ofString(Json.MAPPER.writeValueAsString(body)))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        JsonNode json = parseJsonLenient(response.body());
        JsonNode data = json.path("data");

        for (int i = 0; i < data.size(); i++) {
            JsonNode w = data.get(i);
            String name = w.path("WardName").asString();
            if (normalize(name).equals(normalize(wardName))) {
                return w.path("WardCode").asString();
            }
        }
        return null;
    }

    public Integer getAvailableServiceId(int toDistrictId) throws IOException, InterruptedException {
        ObjectNode body = Json.MAPPER.createObjectNode();
        body.put("shop_id", SHOP_ID);
        body.put("from_district", FROM_DISTRICT_ID);
        body.put("to_district", toDistrictId);

        HttpRequest request = baseRequest(BASE_URL + "/v2/shipping-order/available-services")
                .POST(HttpRequest.BodyPublishers.ofString(Json.MAPPER.writeValueAsString(body)))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        JsonNode json = parseJsonLenient(response.body());
        JsonNode data = json.path("data");

        if (data == null || data.size() == 0) return null;
        return data.get(0).path("service_id").asInt();
    }

    public int calculateShippingFee(String province, String district, String ward,
                                    int weight, int length, int width, int height)
            throws IOException, InterruptedException {

        Integer toDistrictId = getDistrictIdByName(province, district);
        String toWardCode = getWardCodeByName(province, district, ward);

        if (toDistrictId == null || toWardCode == null) {
            throw new RuntimeException("Không map được district/ward sang mã GHN");
        }

        Integer serviceId = getAvailableServiceId(toDistrictId);
        if (serviceId == null) {
            throw new RuntimeException("Không lấy được service_id từ GHN");
        }

        ObjectNode body = Json.MAPPER.createObjectNode();
        body.put("from_district_id", FROM_DISTRICT_ID);
        body.put("from_ward_code", FROM_WARD_CODE);
        body.put("service_id", serviceId);
        body.put("to_district_id", toDistrictId);
        body.put("to_ward_code", toWardCode);
        body.put("height", height);
        body.put("length", length);
        body.put("weight", weight);
        body.put("width", width);
        body.put("insurance_value", 0);

        HttpRequest request = baseRequest(BASE_URL + "/v2/shipping-order/fee")
                .header("ShopId", String.valueOf(SHOP_ID))
                .POST(HttpRequest.BodyPublishers.ofString(Json.MAPPER.writeValueAsString(body)))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        JsonNode json = parseJsonLenient(response.body());
        JsonNode data = json.path("data");

        if (data == null || data.isMissingNode() || data.path("total").isMissingNode()) {
            throw new RuntimeException("GHN fee response lỗi: " + response.body());
        }

        return data.path("total").asInt();
    }

    // ========== GHN order creation & status sync ==========

    /**
     * Push order to GHN via 5sao.ghn.dev API so GHN shippers can manage it.
     * Returns a JSON object with "order_code" and "sort_code" from GHN.
     */
    public JsonNode createGhnOrder(Order order) throws Exception {
        // Build items array
        ArrayNode itemsArray = Json.MAPPER.createArrayNode();
        if (order.getItems() != null) {
            for (OrderItem item : order.getItems()) {
                ObjectNode itemObj = Json.MAPPER.createObjectNode();
                itemObj.put("name", item.getProductNameSnapshot() != null
                        ? item.getProductNameSnapshot() : "San pham");
                itemObj.put("quantity", item.getQuantity());
                itemObj.put("price", item.getPrice() != null
                        ? item.getPrice().intValue() : 0);
                itemObj.put("weight", 200);
                itemsArray.add(itemObj);
            }
        }

        // Resolve GHN location IDs from shipping address
        String[] addressParts = order.getShippingAddress().split(",");
        String provinceName = addressParts.length > 0 ? addressParts[addressParts.length - 1].trim() : "";
        String districtName = addressParts.length > 1 ? addressParts[addressParts.length - 2].trim() : "";
        String wardName = addressParts.length > 2 ? addressParts[addressParts.length - 3].trim() : "";

        int toDistrictId = FROM_DISTRICT_ID;
        String toWardCode = FROM_WARD_CODE;
        try {
            Integer provId = getProvinceIdByName(provinceName);
            if (provId != null) {
                Integer distId = getDistrictIdByName(provinceName, districtName);
                if (distId != null) {
                    toDistrictId = distId;
                    String ward = getWardCodeByName(provinceName, districtName, wardName);
                    if (ward != null) toWardCode = ward;
                }
            }
        } catch (Exception e) {
            // Use fallback values
        }

        ObjectNode reqBody = Json.MAPPER.createObjectNode();
        reqBody.put("shop_id", GHN_ORDER_SHOP_ID);
        reqBody.put("to_name", order.getRecipientFullname());
        reqBody.put("to_phone", order.getRecipientPhone());
        reqBody.put("to_address", order.getShippingAddress());
        reqBody.put("to_district_id", toDistrictId);
        reqBody.put("to_ward_code", toWardCode);
        reqBody.put("cod_amount", order.getPayment_status() ? 0 : order.getTotalAmount().intValue());
        reqBody.put("weight", Math.max(200, order.getItems() != null ? order.getItems().size() * 200 : 200));
        reqBody.put("length", 10);
        reqBody.put("width", 10);
        reqBody.put("height", 10);
        reqBody.put("service_type_id", 2);
        reqBody.put("payment_type_id", 1);
        reqBody.put("note", order.getNote() != null ? order.getNote() : "");
        reqBody.put("required_note", "KHONGCHOXEMHANG"); // GHN required field
        reqBody.set("items", itemsArray);

        HttpRequest request = ghnOrderRequest(GHN_ORDER_URL + "/v2/shipping-order/create")
                .POST(HttpRequest.BodyPublishers.ofString(Json.MAPPER.writeValueAsString(reqBody)))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        int httpStatus = response.statusCode();
        String respBody = response.body();

        logger.info("[GHN] Create order HTTP status: " + httpStatus);
        logger.info("[GHN] Create order response body: " + respBody);

        logger.info("[GHN] Final HTTP status: " + httpStatus);
        logger.info("[GHN] Response body: " + respBody);

        if (respBody == null || respBody.trim().isEmpty()) {
            throw new RuntimeException("GHN API returned empty response. HTTP status: " + httpStatus);
        }
        if (respBody.trim().startsWith("<")) {
            throw new RuntimeException("GHN API returned HTML instead of JSON. HTTP status: "
                    + httpStatus + ". Body preview: " + respBody.substring(0, Math.min(300, respBody.length())));
        }

        JsonNode json = parseJsonLenient(respBody);
        int code = json.has("code") ? json.path("code").asInt() : -1;
        if (code != 200) {
            String message = json.has("message") ? json.path("message").asString()
                    : json.has("msg") ? json.path("msg").asString() : respBody;
            String codeMsg = json.has("code_message") ? json.path("code_message").asString() : "";
            String codeMsgVal = json.has("code_message_value") ? json.path("code_message_value").asString() : "";
            String fullMsg = message;
            if (!codeMsgVal.isEmpty() && !codeMsgVal.equals(message)) {
                fullMsg = message + " (" + codeMsgVal + ")";
            }
            throw new RuntimeException("GHN create order failed (code=" + code + "): " + fullMsg);
        }

        JsonNode data = json.path("data");
        if (data.isMissingNode() || data.isNull()) {
            throw new RuntimeException("GHN create order succeeded but returned no data.");
        }

        return data;
    }

    /**
     * Sync order status from GHN.
     * Returns the GHN status string (e.g. "picking", "delivering", "delivered", "returned").
     */
    public String syncGhnStatus(String ghnOrderId) throws Exception {
        ObjectNode reqBody = Json.MAPPER.createObjectNode();
        reqBody.put("order_code", ghnOrderId);

        HttpRequest request = ghnOrderRequest(GHN_ORDER_URL + "/v2/shipping-order/detail")
                .POST(HttpRequest.BodyPublishers.ofString(Json.MAPPER.writeValueAsString(reqBody)))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        int httpStatus = response.statusCode();
        String respBody = response.body();

        logger.info("[GHN] Sync HTTP status: " + httpStatus);
        logger.info("[GHN] Sync response body: " + respBody);

        if (respBody == null || respBody.trim().isEmpty()) {
            throw new RuntimeException("GHN sync returned empty response. HTTP status: " + httpStatus);
        }
        if (respBody.trim().startsWith("<")) {
            throw new RuntimeException("GHN sync returned HTML instead of JSON. HTTP status: "
                    + httpStatus + ". Body preview: " + respBody.substring(0, Math.min(300, respBody.length())));
        }

        JsonNode json = parseJsonLenient(respBody);
        int code = json.has("code") ? json.path("code").asInt() : -1;
        if (code != 200) {
            String message = json.has("message") ? json.path("message").asString()
                    : json.has("msg") ? json.path("msg").asString() : respBody;
            throw new RuntimeException("GHN sync status failed (code=" + code + "): " + message);
        }

        JsonNode data = json.has("data") && !json.path("data").isNull()
                ? json.path("data") : null;
        return data != null && data.has("status") ? data.path("status").asString() : "unknown";
    }

    /**
     * Map GHN status to local order status.
     */
    public static String mapGhnStatusToLocal(String ghnStatus) {
        if (ghnStatus == null) return null;
        switch (ghnStatus.toLowerCase()) {
            case "picking":
            case "picked":
                return "Shipping";
            case "delivering":
            case "delivery":
                return "Shipping";
            case "delivered":
            case "success":
                return "Delivered";
            case "return":
            case "returned":
                return "Cancelled";
            case "cancel":
            case "cancelled":
                return "Cancelled";
            default:
                return null; // no mapping needed
        }
    }

    /**
     * Parse JSON with lenient mode to handle malformed responses from GHN API.
     */
    private static JsonNode parseJsonLenient(String json) {
        try {
            return Json.MAPPER.readTree(json);
        } catch (JacksonException e) {
            throw new RuntimeException("Failed to parse JSON response: " + json, e);
        }
    }

    private String normalize(String s) {
        if (s == null) return "";
        return s.trim()
                .toLowerCase()
                .replace("tỉnh ", "")
                .replace("thành phố ", "")
                .replace("tp. ", "")
                .replace("tp ", "")
                .replace("quận ", "")
                .replace("huyện ", "")
                .replace("thị xã ", "")
                .replace("thành phố ", "")
                .replace("phường ", "")
                .replace("xã ", "")
                .replace("thị trấn ", "")
                .replaceAll("\\s+", " ");
    }
}

