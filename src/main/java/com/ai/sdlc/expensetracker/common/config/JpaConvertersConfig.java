package com.ai.sdlc.expensetracker.common.config;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.time.YearMonth;

public class JpaConvertersConfig {

    @Converter(autoApply = true)
    public static class YearMonthAttributeConverter implements AttributeConverter<YearMonth, String> {

        @Override
        public String convertToDatabaseColumn(YearMonth attribute) {
            return attribute == null ? null : attribute.toString(); // yyyy-MM
        }

        @Override
        public YearMonth convertToEntityAttribute(String dbData) {
            return dbData == null ? null : YearMonth.parse(dbData);
        }
    }
}
