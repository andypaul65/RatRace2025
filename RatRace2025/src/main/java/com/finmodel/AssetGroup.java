package com.finmodel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssetGroup {

    private String name;

    private List<Entity> assets;

    private List<Entity> liabilities;

    private double initialFunds;

    // For hierarchical grouping
    private List<AssetGroup> subGroups;

    // Metadata for UI
    private String description;

    private String category;

    /**
     * Calculate total asset value across all assets in this group and subgroups
     */
    public double getTotalAssetValue() {
        double total = 0.0;
        if (assets != null) {
            total += assets.stream().mapToDouble(Entity::getInitialValue).sum();
        }
        if (subGroups != null) {
            total += subGroups.stream().mapToDouble(AssetGroup::getTotalAssetValue).sum();
        }
        return total;
    }

    /**
     * Calculate total liability value across all liabilities in this group and subgroups
     */
    public double getTotalLiabilityValue() {
        double total = 0.0;
        if (liabilities != null) {
            total += liabilities.stream().mapToDouble(Entity::getInitialValue).sum();
        }
        if (subGroups != null) {
            total += subGroups.stream().mapToDouble(AssetGroup::getTotalLiabilityValue).sum();
        }
        return total;
    }

    /**
     * Get net worth (assets - liabilities)
     */
    public double getNetWorth() {
        return getTotalAssetValue() - getTotalLiabilityValue();
    }

    /**
     * Get all entities (assets and liabilities) in this group and subgroups
     */
    public List<Entity> getAllEntities() {
        List<Entity> allEntities = new ArrayList<>();
        if (assets != null) allEntities.addAll(assets);
        if (liabilities != null) allEntities.addAll(liabilities);
        if (subGroups != null) {
            for (AssetGroup subGroup : subGroups) {
                allEntities.addAll(subGroup.getAllEntities());
            }
        }
        return allEntities;
    }

    /**
     * Convert to Sankey node data for UI
     */
    public Map<String, Object> toSankeyNode(double currentBalance, String periodId) {
        Map<String, Object> node = new HashMap<>();
        node.put("id", name + "_" + periodId);
        node.put("name", name);
        node.put("groupName", name);
        node.put("category", category != null ? category : "Portfolio");
        node.put("description", description);
        node.put("balance", currentBalance);
        node.put("initialValue", getTotalAssetValue());
        node.put("isGroup", true);
        node.put("hasSubGroups", subGroups != null && !subGroups.isEmpty());
        node.put("level", 0); // Root level
        return node;
    }

}