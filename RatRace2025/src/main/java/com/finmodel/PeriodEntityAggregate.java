package com.finmodel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record PeriodEntityAggregate(
        EntityVersion finalVersion,
        List<Flow> netIntraFlows,
        List<Flow> interFlows
) {
    public double getNetBalance() {
        return finalVersion.getBalance();
    }

    public Map<String, Object> toSankeyNode() {
        return toSankeyNode("");
    }

    public Map<String, Object> toSankeyNode(String periodId) {
        Entity entity = finalVersion.getParent();
        Map<String, Object> node = new HashMap<>();
        node.put("id", entity.getId() + (periodId.isEmpty() ? "" : "_" + periodId));
        node.put("name", entity.getName());
        node.put("entityId", entity.getId());
        node.put("balance", finalVersion.getBalance());
        node.put("rate", finalVersion.getRate());
        node.put("initialValue", entity.getInitialValue());
        node.put("primaryCategory", entity.getPrimaryCategory());
        node.put("detailedCategory", entity.getDetailedCategory());
        node.put("isGroup", false);
        node.put("hasSubGroups", false);
        node.put("level", 1);
        return node;
    }
}