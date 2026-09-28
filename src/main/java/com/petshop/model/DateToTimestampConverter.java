package com.petshop.model;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.sql.Date;
import java.sql.Timestamp;

/**
 * Maps a DATE column to a Timestamp attribute with EXACT legacy semantics:
 * the old DAO read DATE via {@code rs.getTimestamp} (midnight time) and wrote
 * Timestamps that MySQL truncated to the date part.
 */
@Converter(autoApply = false)
public class DateToTimestampConverter implements AttributeConverter<Timestamp, Date> {

    @Override
    public Date convertToDatabaseColumn(Timestamp attribute) {
        if (attribute == null) {
            return null;
        }
        return new Date(attribute.getTime());
    }

    @Override
    public Timestamp convertToEntityAttribute(Date dbData) {
        if (dbData == null) {
            return null;
        }
        return new Timestamp(dbData.getTime());
    }
}
