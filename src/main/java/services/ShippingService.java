package services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import Util.ShippingConfig;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.MalformedJsonException;

import Model.Order;
import Model.OrderItem;

import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

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
        JsonObject json = parseJsonLenient(response.body());
        JsonArray data = json.getAsJsonArray("data");

        for (int i = 0; i < data.size(); i++) {
            JsonObject p = data.get(i).getAsJsonObject();
            String name = p.get("ProvinceName").getAsString();
            if (normalize(name).equals(normalize(provinceName))) {
                return p.get("ProvinceID").getAsInt();
            }
        }
        return null;
    }
    public Integer getDistrictIdByName(String provinceName, String districtName) throws IOException, InterruptedException {
        Integer provinceId = getProvinceIdByName(provinceName);
        if (provinceId == null) return null;

        JsonObject body = new JsonObject();
        body.addProperty("province_id", provinceId);

        HttpRequest request = baseRequest(BASE_URL + "/master-data/district")
                .method("GET", HttpRequest.BodyPublishers.ofString(body.toString()))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        JsonObject json = parseJsonLenient(response.body());
        JsonArray data = json.getAsJsonArray("data");

        for (int i = 0; i < data.size(); i++) {
            JsonObject d = data.get(i).getAsJsonObject();
            String name = d.get("DistrictName").getAsString();
            if (normalize(name).equals(normalize(districtName))) {
                return d.get("DistrictID").getAsInt();
            }
        }
        return null;
    }

    public String getWardCodeByName(String provinceName, String districtName, String wardName) throws IOException, InterruptedException {
        Integer districtId = getDistrictIdByName(provinceName, districtName);
        if (districtId == null) return null;

        JsonObject body = new JsonObject();
        body.addProperty("district_id", districtId);

        HttpRequest request = baseRequest(BASE_URL + "/master-data/ward?district_id")
                .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        JsonObject json = parseJsonLenient(response.body());
        JsonArray data = json.getAsJsonArray("data");

        for (int i = 0; i < data.size(); i++) {
            JsonObject w = data.get(i).getAsJsonObject();
            String name = w.get("WardName").getAsString();
            if (normalize(name).equals(normalize(wardName))) {
                return w.get("WardCode").getAsString();
            }
        }
        return null;
    }

    public Integer getAvailableServiceId(int toDistrictId) throws IOException, InterruptedException {
        JsonObject body = new JsonObject();
        body.addProperty("shop_id", SHOP_ID);
        body.addProperty("from_district", FROM_DISTRICT_ID);
        body.addProperty("to_district", toDistrictId);

        HttpRequest request = baseRequest(BASE_URL + "/v2/shipping-order/available-services")
                .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        JsonObject json = parseJsonLenient(response.body());
        JsonArray data = json.getAsJsonArray("data");

        if (data == null || data.size() == 0) return null;
        return data.get(0).getAsJsonObject().get("service_id").getAsInt();
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

        JsonObject body = new JsonObject();
        body.addProperty("from_district_id", FROM_DISTRICT_ID);
        body.addProperty("from_ward_code", FROM_WARD_CODE);
        body.addProperty("service_id", serviceId);
        body.addProperty("to_district_id", toDistrictId);
        body.addProperty("to_ward_code", toWardCode);
        body.addProperty("height", height);
        body.addProperty("length", length);
        body.addProperty("weight", weight);
        body.addProperty("width", width);
        body.addProperty("insurance_value", 0);

        HttpRequest request = baseRequest(BASE_URL + "/v2/shipping-order/fee")
                .header("ShopId", String.valueOf(SHOP_ID))
                .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        JsonObject json = parseJsonLenient(response.body());
        JsonObject data = json.getAsJsonObject("data");

        if (data == null || data.get("total") == null) {
            throw new RuntimeException("GHN fee response lỗi: " + response.body());
        }

        return data.get("total").getAsInt();
    }

    // ========== GHN order creation & status sync ==========

    /**
     * Push order to GHN via 5sao.ghn.dev API so GHN shippers can manage it.
     * Returns a JSON object with "order_code" and "sort_code" from GHN.
     */
    public JsonObject createGhnOrder(Order order) throws Exception {
        // Build items array
        JsonArray itemsArray = new JsonArray();
        if (order.getItems() != null) {
            for (OrderItem item : order.getItems()) {
                JsonObject itemObj = new JsonObject();
                itemObj.addProperty("name", item.getProductNameSnapshot() != null
                        ? item.getProductNameSnapshot() : "San pham");
                itemObj.addProperty("quantity", item.getQuantity());
                itemObj.addProperty("price", item.getPrice() != null
                        ? item.getPrice().intValue() : 0);
                itemObj.addProperty("weight", 200);
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

        JsonObject reqBody = new JsonObject();
        reqBody.addProperty("shop_id", GHN_ORDER_SHOP_ID);
        reqBody.addProperty("to_name", order.getRecipientFullname());
        reqBody.addProperty("to_phone", order.getRecipientPhone());
        reqBody.addProperty("to_address", order.getShippingAddress());
        reqBody.addProperty("to_district_id", toDistrictId);
        reqBody.addProperty("to_ward_code", toWardCode);
        reqBody.addProperty("cod_amount", order.getPayment_status() ? 0 : order.getTotalAmount().intValue());
        reqBody.addProperty("weight", Math.max(200, order.getItems() != null ? order.getItems().size() * 200 : 200));
        reqBody.addProperty("length", 10);
        reqBody.addProperty("width", 10);
        reqBody.addProperty("height", 10);
        reqBody.addProperty("service_type_id", 2);
        reqBody.addProperty("payment_type_id", 1);
        reqBody.addProperty("note", order.getNote() != null ? order.getNote() : "");
        reqBody.addProperty("required_note", "KHONGCHOXEMHANG"); // GHN required field
        reqBody.add("items", itemsArray);

        HttpRequest request = ghnOrderRequest(GHN_ORDER_URL + "/v2/shipping-order/create")
                .POST(HttpRequest.BodyPublishers.ofString(reqBody.toString()))
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

        JsonObject json = parseJsonLenient(respBody);
        int code = json.has("code") ? json.get("code").getAsInt() : -1;
        if (code != 200) {
            String message = json.has("message") ? json.get("message").getAsString()
                    : json.has("msg") ? json.get("msg").getAsString() : respBody;
            String codeMsg = json.has("code_message") ? json.get("code_message").getAsString() : "";
            String codeMsgVal = json.has("code_message_value") ? json.get("code_message_value").getAsString() : "";
            String fullMsg = message;
            if (!codeMsgVal.isEmpty() && !codeMsgVal.equals(message)) {
                fullMsg = message + " (" + codeMsgVal + ")";
            }
            throw new RuntimeException("GHN create order failed (code=" + code + "): " + fullMsg);
        }

        if (!json.has("data") || json.get("data").isJsonNull()) {
            throw new RuntimeException("GHN create order succeeded but returned no data.");
        }

        return json.getAsJsonObject("data");
    }

    /**
     * Sync order status from GHN.
     * Returns the GHN status string (e.g. "picking", "delivering", "delivered", "returned").
     */
    public String syncGhnStatus(String ghnOrderId) throws Exception {
        JsonObject reqBody = new JsonObject();
        reqBody.addProperty("order_code", ghnOrderId);

        HttpRequest request = ghnOrderRequest(GHN_ORDER_URL + "/v2/shipping-order/detail")
                .POST(HttpRequest.BodyPublishers.ofString(reqBody.toString()))
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

        JsonObject json = parseJsonLenient(respBody);
        int code = json.has("code") ? json.get("code").getAsInt() : -1;
        if (code != 200) {
            String message = json.has("message") ? json.get("message").getAsString()
                    : json.has("msg") ? json.get("msg").getAsString() : respBody;
            throw new RuntimeException("GHN sync status failed (code=" + code + "): " + message);
        }

        JsonObject data = json.has("data") && !json.get("data").isJsonNull()
                ? json.getAsJsonObject("data") : null;
        return data != null && data.has("status") ? data.get("status").getAsString() : "unknown";
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
    private static JsonObject parseJsonLenient(String json) {
        try {
            // First try strict parsing
            return new JsonParser().parse(json).getAsJsonObject();
        } catch (JsonSyntaxException | IllegalStateException e) {
            // Fall back to lenient parsing
            try (JsonReader reader = new JsonReader(new StringReader(json))) {
                reader.setLenient(true);
                return new JsonParser().parse(reader).getAsJsonObject();
            } catch (Exception ex) {
                throw new RuntimeException("Failed to parse JSON response: " + json, ex);
            }
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

