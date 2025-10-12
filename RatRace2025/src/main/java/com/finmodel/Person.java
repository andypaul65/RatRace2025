package com.finmodel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Scenario component representing a person in the UK tax system.
 * Manages personal tax allowances, income sources, owned assets/liabilities,
 * and tax efficiency calculations.
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Person implements ScenarioComponent {

    public enum MaritalStatus {
        SINGLE, MARRIED, CIVIL_PARTNERSHIP, DIVORCED, WIDOWED
    }

    private String id;
    private String firstName;
    private String lastName;

    @Builder.Default
    private LocalDate dateOfBirth = LocalDate.now().minusYears(30);

    @Builder.Default
    private MaritalStatus maritalStatus = MaritalStatus.SINGLE;

    @Builder.Default
    private String taxCode = "1257L"; // Default UK tax code

    // UK Tax Allowances (2024/25 rates)
    @Builder.Default
    private double personalAllowance = 12570.00;

    @Builder.Default
    private double marriageAllowance = 0.00; // If applicable

    @Builder.Default
    private double blindPersonsAllowance = 0.00;

    // Income sources linked to this person
    @Builder.Default
    private List<String> salaryEntities = new ArrayList<>();

    @Builder.Default
    private List<String> pensionEntities = new ArrayList<>();

    @Builder.Default
    private List<String> dividendEntities = new ArrayList<>();

    // Assets and liabilities owned by this person
    @Builder.Default
    private List<String> ownedAssets = new ArrayList<>();

    @Builder.Default
    private List<String> ownedLiabilities = new ArrayList<>();

    // Tax year information
    @Builder.Default
    private int taxYear = 2024; // UK tax year (e.g., 2024 = 2024/25 tax year)

    // Tax calculation results (populated during simulation)
    @Builder.Default
    private double grossIncome = 0.0;

    @Builder.Default
    private double taxableIncome = 0.0;

    @Builder.Default
    private double incomeTax = 0.0;

    @Builder.Default
    private double nationalInsurance = 0.0;

    @Builder.Default
    private double capitalGainsTax = 0.0;

    @Builder.Default
    private double totalTaxPaid = 0.0;

    @Builder.Default
    private double effectiveTaxRate = 0.0;

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return firstName + " " + lastName;
    }

    @Override
    public List<Entity> getEntities() {
        List<Entity> entities = new ArrayList<>();

        // Create personal tax calculation entity
        entities.add(Entity.builder()
                .id(id + "_tax_calculation")
                .name(getName() + " Tax Calculation")
                .primaryCategory("Expense")
                .detailedCategory("Tax Calculation")
                .initialValue(0.0)
                .build());

        // Create personal allowance tracking entity
        entities.add(Entity.builder()
                .id(id + "_personal_allowance")
                .name(getName() + " Personal Allowance")
                .primaryCategory("Asset")
                .detailedCategory("Tax Allowance")
                .initialValue(personalAllowance)
                .build());

        return entities;
    }

    @Override
    public List<Event> getEvents() {
        List<Event> events = new ArrayList<>();
        List<Entity> entities = getEntities();

        // Find our entities
        Entity taxEntity = entities.stream()
                .filter(e -> e.getId().equals(id + "_tax_calculation"))
                .findFirst().orElse(null);

        // Create tax calculation event (annual)
        if (taxEntity != null) {
            events.add(CalculationEvent.builder()
                    .id(id + "_tax_calculation_event")
                    .type("uk_tax_calculation")
                    .params(java.util.Map.of(
                            "personId", id,
                            "taxYear", taxYear,
                            "personalAllowance", personalAllowance,
                            "salaryEntities", String.join(",", salaryEntities),
                            "pensionEntities", String.join(",", pensionEntities),
                            "dividendEntities", String.join(",", dividendEntities)
                    ))
                    .build());
        }

        return events;
    }

    @Override
    public void validate() throws ValidationException {
        if (id == null || id.trim().isEmpty()) {
            throw new ValidationException("Person id cannot be null or empty");
        }

        if (firstName == null || firstName.trim().isEmpty()) {
            throw new ValidationException("Person first name cannot be null or empty for " + id);
        }

        if (lastName == null || lastName.trim().isEmpty()) {
            throw new ValidationException("Person last name cannot be null or empty for " + id);
        }

        if (personalAllowance < 0) {
            throw new ValidationException("Personal allowance cannot be negative for " + id);
        }

        if (dateOfBirth.isAfter(LocalDate.now())) {
            throw new ValidationException("Date of birth cannot be in the future for " + id);
        }

        // Validate tax code format (basic check)
        if (taxCode != null && !taxCode.matches("[0-9]+[LMNTPY]")) {
            throw new ValidationException("Invalid UK tax code format for " + id + ": " + taxCode);
        }
    }

    @Override
    public String describe() {
        StringBuilder sb = new StringBuilder();
        sb.append("Person [").append(id).append("]: ").append(getName());
        sb.append(" (").append(maritalStatus).append(", ");

        // Calculate age
        int age = LocalDate.now().getYear() - dateOfBirth.getYear();
        sb.append(age).append(" years old)");

        sb.append("\n  Tax Code: ").append(taxCode);
        sb.append("\n  Personal Allowance: £").append(String.format("%.0f", personalAllowance));
        sb.append("\n  Tax Year: ").append(taxYear).append("/").append(taxYear + 1);

        if (!salaryEntities.isEmpty()) {
            sb.append("\n  Salary Sources: ").append(String.join(", ", salaryEntities));
        }

        if (!pensionEntities.isEmpty()) {
            sb.append("\n  Pension Sources: ").append(String.join(", ", pensionEntities));
        }

        if (!ownedAssets.isEmpty()) {
            sb.append("\n  Owned Assets: ").append(String.join(", ", ownedAssets));
        }

        return sb.toString();
    }

    // Getters for tax calculation results
    public double getGrossIncome() { return grossIncome; }
    public double getTaxableIncome() { return taxableIncome; }
    public double getIncomeTax() { return incomeTax; }
    public double getNationalInsurance() { return nationalInsurance; }
    public double getCapitalGainsTax() { return capitalGainsTax; }
    public double getTotalTaxPaid() { return totalTaxPaid; }
    public double getEffectiveTaxRate() { return effectiveTaxRate; }

    // Setters for tax calculation results (used by tax calculation events)
    public void setTaxResults(double grossIncome, double taxableIncome, double incomeTax,
                            double nationalInsurance, double capitalGainsTax) {
        this.grossIncome = grossIncome;
        this.taxableIncome = taxableIncome;
        this.incomeTax = incomeTax;
        this.nationalInsurance = nationalInsurance;
        this.capitalGainsTax = capitalGainsTax;
        this.totalTaxPaid = incomeTax + nationalInsurance + capitalGainsTax;
        this.effectiveTaxRate = grossIncome > 0 ? (totalTaxPaid / grossIncome) * 100.0 : 0.0;
    }

    // Helper methods
    public int getAge() {
        return LocalDate.now().getYear() - dateOfBirth.getYear();
    }

    public boolean isScottishTaxpayer() {
        // Placeholder - would need to determine based on residence
        return false;
    }

    public boolean hasMarriageAllowance() {
        return marriageAllowance > 0;
    }

    public boolean isBlind() {
        return blindPersonsAllowance > 0;
    }
}