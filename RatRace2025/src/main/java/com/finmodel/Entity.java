package com.finmodel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Entity {
    private String id;
    private String name;
    private String primaryCategory;
    private String detailedCategory;
    private double initialValue; // For ROI tracking, starting value of the entity
    private Map<String, Object> baseProperties;
    private boolean isTemplate;

    public Entity cloneAsNew() {
        return Entity.builder()
                .id(this.id)
                .name(this.name)
                .primaryCategory(this.primaryCategory)
                .detailedCategory(this.detailedCategory)
                .initialValue(this.initialValue)
                .baseProperties(new HashMap<>(this.baseProperties != null ? this.baseProperties : Map.of()))
                .isTemplate(false)
                .build();
    }

    public EntityVersion createInitialVersion(Date date) {
        return EntityVersion.builder()
                .parent(this)
                .date(date)
                .sequence(0)
                .balance(this.initialValue)
                .rate(0.0)
                .attributes(new HashMap<>(this.baseProperties != null ? this.baseProperties : Map.of()))
                .previous(null)
                .build();
    }
}