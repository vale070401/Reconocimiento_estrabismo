package com.Deteccion_estrabismo.backend.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;

@Component
public class BuildObjectMapper {

    private static ObjectMapper objectMapper;

    public BuildObjectMapper() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        this.objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
    }

    // Método público para obtener el ObjectMapper
    public ObjectMapper getObjectMapper() {
        return this.objectMapper;
    }

    public <T> T converterTo(Object source, Class<T> targetClass) {
        return objectMapper.convertValue(source, targetClass);
    }

    public String converterToString(Object reference) {
        try {
            return objectMapper.writeValueAsString(reference);
        } catch (Exception e) {
            return "";
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T getObjectForUpdate(Object baseObject, Map<String, Object> dataUpdate) {
        Map<String, Object> baseMap = getObjectMapper().convertValue(baseObject, Map.class);
        ArrayList<String> keysList = new ArrayList<>(baseMap.keySet());
        keysList.addAll(dataUpdate.keySet());

        // Only update fields that are present in dataUpdate and are not null
        for (String keyBase : new HashSet<>(keysList)) {
            if (dataUpdate.containsKey(keyBase) && dataUpdate.get(keyBase) != null) {
                baseMap.put(keyBase, dataUpdate.get(keyBase));
            }
        }

        // Convert back to object
        T updatedObject = (T) getObjectMapper().convertValue(baseMap, baseObject.getClass());

        return preserveJpaRelations(baseObject, updatedObject, dataUpdate);
    }

    private <T> T preserveJpaRelations(Object baseObject, Object updatedObject, Map<String, Object> dataUpdate) {

        try {
            // Get all fields from the original object
            java.lang.reflect.Field[] fields = baseObject.getClass().getDeclaredFields();

            for (java.lang.reflect.Field field : fields) {
                // Skip if this field was explicitly updated in dataUpdate
                if (dataUpdate.containsKey(field.getName())) {
                    continue;
                }

                // Check if field has JPA annotations that indicate it's a relation
                boolean isJpaRelation = field.isAnnotationPresent(jakarta.persistence.ManyToOne.class) ||
                        field.isAnnotationPresent(jakarta.persistence.OneToOne.class) ||
                        field.isAnnotationPresent(jakarta.persistence.OneToMany.class) ||
                        field.isAnnotationPresent(jakarta.persistence.ManyToMany.class);

                // Check if field has timestamp annotations
                boolean isTimestamp = field.isAnnotationPresent(CreationTimestamp.class) ||
                        field.isAnnotationPresent(UpdateTimestamp.class);

                if (isJpaRelation || isTimestamp) {
                    field.setAccessible(true);
                    Object originalValue = field.get(baseObject);

                    if (originalValue != null) {
                        field.set(updatedObject, originalValue);
                    }
                }
            }
        } catch (IllegalAccessException e) {
            // Log warning but don't fail the operation
            System.err.println("Warning: Could not preserve JPA relations: " + e.getMessage());
        }

        return (T) updatedObject;
    }
}
