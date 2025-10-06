package com.finmodel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssetGroup {

    private String name;

    private List<Entity> assets;

    private List<Entity> liabilities;

    private double initialFunds;

    // Future: add methods for ROI calculation, total value, etc.

}