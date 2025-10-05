package com.finmodel;

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
        return Map.of(
                "id", finalVersion.getParent().getId(),
                "name", finalVersion.getParent().getName(),
                "balance", finalVersion.getBalance(),
                "rate", finalVersion.getRate()
        );
    }
}