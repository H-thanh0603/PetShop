package com.petshop.model;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Maps VARCHAR status ('active'/...) to boolean with EXACT legacy semantics:
 * the old DAO read {@code rs.getBoolean("status")} inside try/catch-default-true.
 * Probed driver behavior (Task 4): 'active'/'inactive' throw NumberFormatException
 * (caught -> true); '1'/'true' -> true; '0'/'false'/'' -> false; NULL -> false.
 */
@Converter(autoApply = false)
public class StatusBooleanConverter implements AttributeConverter<Boolean, String> {

    @Override
    public String convertToDatabaseColumn(Boolean attribute) {
        if (attribute == null) {
            return null;
        }
        // Match the old DAO writes (deactivateUser wrote literal 0; addUser
        // wrote literal 1). Reads accept both numeric and word forms.
        return attribute ? "1" : "0";
    }

    @Override
    public Boolean convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return false;
        }
        String value = dbData.trim();
        if (value.equalsIgnoreCase("true") || value.equals("1")) {
            return true;
        }
        if (value.equalsIgnoreCase("false") || value.equals("0") || value.isEmpty()) {
            return false;
        }
        // 'active', 'inactive', and any other non-boolean string threw
        // NumberFormatException in the driver -> caught -> default true.
        return true;
    }
}
