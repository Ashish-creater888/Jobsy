package Job_Portal.Module;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.Map;

@Converter
public class CategoryAgeLimitsConverter
        implements AttributeConverter<Map<String, Integer>, String> {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final TypeReference<Map<String, Integer>> MAP_TYPE =
            new TypeReference<>() {};

    @Override
    public String convertToDatabaseColumn(Map<String, Integer> ageLimits) {
        if (ageLimits == null || ageLimits.isEmpty()) {
            return null;
        }
        try {
            return OBJECT_MAPPER.writeValueAsString(ageLimits);
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException("Could not serialize category age limits", exception);
        }
    }

    @Override
    public Map<String, Integer> convertToEntityAttribute(String value) {
        if (value == null || value.isBlank()) {
            return Map.of();
        }
        try {
            return OBJECT_MAPPER.readValue(value, MAP_TYPE);
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException("Could not read category age limits", exception);
        }
    }
}
