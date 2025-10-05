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
    private Map<String, Object> baseProperties;
    private boolean isTemplate;

    public Entity cloneAsNew() {
        return Entity.builder()
                .id(this.id)
                .name(this.name)
                .baseProperties(new HashMap<>(this.baseProperties))
                .isTemplate(false)
                .build();
    }

    public EntityVersion createInitialVersion(Date date) {
        return EntityVersion.builder()
                .parent(this)
                .date(date)
                .sequence(0)
                .balance(0.0)
                .rate(0.0)
                .attributes(new HashMap<>(this.baseProperties != null ? this.baseProperties : Map.of()))
                .previous(null)
                .build();
    }
}