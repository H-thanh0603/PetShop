package services;

import Util.ShippingConfig;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class ShippingService {
    private static final String TOKEN = ShippingConfig.get("TOKEN");
    private static final int SHOP_ID = ShippingConfig.getInt("SHOP_ID");
    private static final int FROM_DISTRICT_ID = ShippingConfig.getInt("FROM_DISTRICT_ID");
    private static final String FROM_WARD_CODE = ShippingConfig.get("FROM_WARD_CODE");
    private static final String BASE_URL = ShippingConfig.get("BASE_URL");
    private final HttpClient client = HttpClient.newHttpClient();

    private HttpRequest.Builder baseRequest(String url) {
        return HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .header("Token", TOKEN);
    }
    public Integer getProvinceIdByName(String provinceName) throws IOException, InterruptedException {
        HttpRequest request = baseRequest(BASE_URL + "/master-data/province")
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
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
        JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
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
        JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
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
        JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
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

        JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
        JsonObject data = json.getAsJsonObject("data");

        if (data == null || data.get("total") == null) {
            throw new RuntimeException("GHN fee response lỗi: " + response.body());
        }

        return data.get("total").getAsInt();
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

