package com.finmodel;

import java.time.LocalDate;

/**
 * Demo showing UK tax calculations with Person component.
 */
public class PersonTaxDemo {

    public static void main(String[] args) {
        System.out.println("=== RatRace2025 UK Tax Calculation Demo ===\n");

        // Create a person with UK tax context
        Person person = Person.builder()
                .id("john_doe")
                .firstName("John")
                .lastName("Doe")
                .dateOfBirth(LocalDate.of(1985, 6, 15))
                .maritalStatus(Person.MaritalStatus.MARRIED)
                .taxCode("1257L")
                .personalAllowance(12570.00)
                .build();

        // Validate the person
        try {
            person.validate();
            System.out.println("✅ Person validation passed");
        } catch (ValidationException e) {
            System.out.println("❌ Person validation failed: " + e.getMessage());
            return;
        }

        // Display person info
        System.out.println("👤 " + person.describe());
        System.out.println();

        // Show entities created
        System.out.println("📊 Entities Created:");
        for (Entity entity : person.getEntities()) {
            System.out.printf("  - %s (%s): £%.0f%n",
                    entity.getName(),
                    entity.getPrimaryCategory(),
                    entity.getInitialValue());
        }
        System.out.println();

        // Show events created
        System.out.println("⚡ Events Created:");
        for (Event event : person.getEvents()) {
            System.out.printf("  - %s: %s%n", event.getId(), event.getType());
        }
        System.out.println();

        // Demonstrate tax calculation
        System.out.println("💰 UK Tax Calculation Example:");
        double salaryIncome = 75000.0;
        double pensionIncome = 0.0;
        double dividendIncome = 2000.0;
        double capitalGains = 5000.0;

        System.out.printf("  Salary Income: £%,.0f%n", salaryIncome);
        System.out.printf("  Pension Income: £%,.0f%n", pensionIncome);
        System.out.printf("  Dividend Income: £%,.0f%n", dividendIncome);
        System.out.printf("  Capital Gains: £%,.0f%n", capitalGains);

        try {
            UKTaxCalculator.TaxCalculationResult result =
                UKTaxCalculator.calculateTotalTax(person, salaryIncome, pensionIncome, dividendIncome, capitalGains);

            System.out.println();
            System.out.println("📋 Tax Calculation Results (2024/25):");
            System.out.printf("  Gross Income: £%,.0f%n", result.getGrossIncome());
            System.out.printf("  Taxable Income: £%,.0f%n", result.getTaxableIncome());
            System.out.printf("  Income Tax: £%,.0f%n", result.getIncomeTax());
            System.out.printf("  National Insurance: £%,.0f%n", result.getNationalInsurance());
            System.out.printf("  Capital Gains Tax: £%,.0f%n", result.getCapitalGainsTax());
            System.out.printf("  Total Tax Paid: £%,.0f%n", result.getTotalTaxPaid());
            System.out.printf("  Effective Tax Rate: %.1f%%%n", result.getEffectiveTaxRate());

            // Update person with tax results
            person.setTaxResults(result.getGrossIncome(), result.getTaxableIncome(),
                    result.getIncomeTax(), result.getNationalInsurance(), result.getCapitalGainsTax());

            System.out.println("\n✅ Tax calculation completed successfully!");
            System.out.println("✅ Person object updated with tax results!");

        } catch (Exception e) {
            System.out.println("❌ Tax calculation failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}