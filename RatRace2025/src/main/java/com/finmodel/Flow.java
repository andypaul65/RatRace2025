package com.finmodel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Flow {
    private String id;
    private EntityVersion source;
    private EntityVersion target;
    private double amount;
    private String direction;
    private String type;
    private Map<String, Object> metadata;
    private boolean isIntraPeriod;

    public void validate() {
        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("Flow id cannot be null or empty");
        }
        if (source == null) {
            throw new IllegalArgumentException("Flow source cannot be null");
        }
        if (target == null) {
            throw new IllegalArgumentException("Flow target cannot be null");
        }
        if (amount < 0) {
            throw new IllegalArgumentException("Flow amount cannot be negative");
        }
        if (direction == null || direction.isEmpty()) {
            throw new IllegalArgumentException("Flow direction cannot be null or empty");
        }
        if (type == null || type.isEmpty()) {
            throw new IllegalArgumentException("Flow type cannot be null or empty");
        }
    }

    /**
     * Convert to Sankey link data with rich metadata for UI interactions
     */
    public Map<String, Object> toSankeyLink(String periodId) {
        Map<String, Object> link = new HashMap<>();
        link.put("id", id + "_" + periodId);
        link.put("source", source.getParent().getId() + "_" + periodId);
        link.put("target", target.getParent().getId() + "_" + periodId);
        link.put("value", amount);
        link.put("flowId", id);
        link.put("direction", direction);
        link.put("type", type);
        link.put("isIntraPeriod", isIntraPeriod);

        // Add source and target entity info for hover
        link.put("sourceName", source.getParent().getName());
        link.put("targetName", target.getParent().getName());
        link.put("sourceCategory", source.getParent().getPrimaryCategory());
        link.put("targetCategory", target.getParent().getPrimaryCategory());

        // Add metadata if available
        if (metadata != null) {
            link.put("metadata", metadata);
        }

        return link;
    }
}