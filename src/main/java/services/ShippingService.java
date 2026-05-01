package services;

import Util.ShippingConfig;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class ShippingService {
    private static final Logger logger = LoggerFactory.getLogger(ShippingService.class);

    private static final String TOKEN = ShippingConfig.get("TOKEN");
    private static final int SHOP_ID = ShippingConfig.getInt("SHOP_ID");
    private static final int FROM_DISTRICT_ID = ShippingConfig.getInt("FROM_DISTRICT_ID");
    private static final String FROM_WARD_CODE = ShippingConfig.get("FROM_WARD_CODE");
    private static final String BASE_URL = ShippingConfig.get("BASE_URL");

    // Shared across all ShippingService instances — HttpClient is thread-safe and expensive to create.
    private static final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    // Circuit breaker state
    private static final int CIRCUIT_BREAKER_THRESHOLD = 3;        // failures before opening
    private static final long CIRCUIT_BREAKER_COOLDOWN_MS = 60_000L; // 1 minute cooldown
    private static final AtomicInteger consecutiveFailures = new AtomicInteger(0);
    private static volatile long circuitOpenedAt = 0L;

    // TTL for geographic data caches — 24 hours (geographic data rarely changes)
    private static final long CACHE_TTL_MS = 24 * 60 * 60 * 1000L;

    // In-memory caches shared across all instances (ConcurrentHashMap for thread safety)
    private static final ConcurrentHashMap<String, CacheEntry<Integer>> provinceCache = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, CacheEntry<Integer>> districtCache = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, CacheEntry<String>> wardCache = new ConcurrentHashMap<>();

    /**
     * Simple TTL cache entry wrapper.
     */
    private record CacheEntry<V>(V value, long expiresAt) {
        boolean isExpired() {
            return System.currentTimeMillis() > expiresAt;
        }
    }

    /**
     * Checks whether the circuit breaker is currently open (i.e., calls should be short-circuited).
     * If the cooldown period has elapsed, the circuit is reset to half-open and this returns false.
     */
    private static boolean isCircuitOpen() {
        if (consecutiveFailures.get() < CIRCUIT_BREAKER_THRESHOLD) {
            return false;
        }
        // Check if cooldown has elapsed
        if (System.currentTimeMillis() - circuitOpenedAt >= CIRCUIT_BREAKER_COOLDOWN_MS) {
            // Reset circuit breaker (half-open: allow one attempt)
            consecutiveFailures.set(0);
            circuitOpenedAt = 0L;
            logger.info("Circuit breaker reset after cooldown period");
            return false;
        }
        return true;
    }

    /**
     * Resets the circuit breaker state. Useful for testing or manual recovery.
     */
    public static void resetCircuitBreaker() {
        consecutiveFailures.set(0);
        circuitOpenedAt = 0L;
    }

    /**
     * Clears all geographic caches. Useful for testing or admin cache invalidation.
     */
    public static void clearCache() {
        provinceCache.clear();
        districtCache.clear();
        wardCache.clear();
        logger.debug("Geographic caches cleared");
    }

    private HttpRequest.Builder baseRequest(String url) {
        return HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(8))
                .header("Content-Type", "application/json")
                .header("Token", TOKEN);
    }

    public Integer getProvinceIdByName(String provinceName) throws IOException, InterruptedException {
        String normalizedName = normalize(provinceName);

        CacheEntry<Integer> cached = provinceCache.get(normalizedName);
        if (cached != null && !cached.isExpired()) {
            logger.debug("Cache hit for province: {}", provinceName);
            return cached.value();
        }

        HttpRequest request = baseRequest(BASE_URL + "/master-data/province")
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
        JsonArray data = json.getAsJsonArray("data");

        Integer result = null;
        for (int i = 0; i < data.size(); i++) {
            JsonObject p = data.get(i).getAsJsonObject();
            String name = p.get("ProvinceName").getAsString();
            if (normalize(name).equals(normalizedName)) {
                result = p.get("ProvinceID").getAsInt();
                break;
            }
        }

        if (result != null) {
            provinceCache.put(normalizedName, new CacheEntry<>(result, System.currentTimeMillis() + CACHE_TTL_MS));
        }
        return result;
    }

    public Integer getDistrictIdByName(String provinceName, String districtName) throws IOException, InterruptedException {
        String cacheKey = normalize(provinceName) + "|" + normalize(districtName);

        CacheEntry<Integer> cached = districtCache.get(cacheKey);
        if (cached != null && !cached.isExpired()) {
            logger.debug("Cache hit for district: {}|{}", provinceName, districtName);
            return cached.value();
        }

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

        Integer result = null;
        for (int i = 0; i < data.size(); i++) {
            JsonObject d = data.get(i).getAsJsonObject();
            String name = d.get("DistrictName").getAsString();
            if (normalize(name).equals(normalize(districtName))) {
                result = d.get("DistrictID").getAsInt();
                break;
            }
        }

        if (result != null) {
            districtCache.put(cacheKey, new CacheEntry<>(result, System.currentTimeMillis() + CACHE_TTL_MS));
        }
        return result;
    }

    public String getWardCodeByName(String provinceName, String districtName, String wardName) throws IOException, InterruptedException {
        String cacheKey = normalize(provinceName) + "|" + normalize(districtName) + "|" + normalize(wardName);

        CacheEntry<String> cached = wardCache.get(cacheKey);
        if (cached != null && !cached.isExpired()) {
            logger.debug("Cache hit for ward: {}|{}|{}", provinceName, districtName, wardName);
            return cached.value();
        }

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

        String result = null;
        for (int i = 0; i < data.size(); i++) {
            JsonObject w = data.get(i).getAsJsonObject();
            String name = w.get("WardName").getAsString();
            if (normalize(name).equals(normalize(wardName))) {
                result = w.get("WardCode").getAsString();
                break;
            }
        }

        if (result != null) {
            wardCache.put(cacheKey, new CacheEntry<>(result, System.currentTimeMillis() + CACHE_TTL_MS));
        }
        return result;
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

        if (isCircuitOpen()) {
            throw new RuntimeException("GHN API circuit breaker is open, service temporarily unavailable");
        }

        try {
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

            int fee = data.get("total").getAsInt();

            // Success — reset failure counter
            consecutiveFailures.set(0);
            return fee;
        } catch (Exception e) {
            int failures = consecutiveFailures.incrementAndGet();
            if (failures >= CIRCUIT_BREAKER_THRESHOLD) {
                circuitOpenedAt = System.currentTimeMillis();
                logger.warn("GHN API circuit breaker opened after {} consecutive failures", failures);
            }
            throw e;
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
