package com.petshop.util;

import tools.jackson.core.json.JsonReadFeature;
import tools.jackson.databind.json.JsonMapper;

/**
 * Mapper JSON dùng chung cho toàn app, thay cho Gson (đã gỡ).
 * Đọc theo kiểu lenient như Gson cũ để không vỡ khi GHN/webhook trả JSON lỗi.
 */
public final class Json {

    public static final JsonMapper MAPPER = JsonMapper.builder()
            .enable(JsonReadFeature.ALLOW_UNQUOTED_PROPERTY_NAMES)
            .enable(JsonReadFeature.ALLOW_SINGLE_QUOTES)
            .enable(JsonReadFeature.ALLOW_MISSING_VALUES)
            .enable(JsonReadFeature.ALLOW_TRAILING_COMMA)
            .enable(JsonReadFeature.ALLOW_JAVA_COMMENTS)
            .build();

    private Json() {
    }
}
