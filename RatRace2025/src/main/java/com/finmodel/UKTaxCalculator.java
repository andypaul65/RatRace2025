package com.finmodel;

import lombok.Data;
import java.time.LocalDate;

/**
 * UK Tax Calculator implementing 2024/25 tax rules
 * Handles Income Tax, National Insurance, and Capital Gains Tax calculations
 */
public class UKTaxCalculator {

    // 2024/25 UK Tax Rates and Thresholds
    public static final class TaxRates {
        // Income Tax thresholds (England, Wales, Northern Ireland)
        public static final double PERSONAL_ALLOWANCE = 12570.00;
        public static final double BASIC_RATE_THRESHOLD = 50270.00;
        public static final double HIGHER_RATE_THRESHOLD = 125140.00;

        // Income Tax rates
        public static final double BASIC_RATE = 0.20;      // 20%
        public static final double HIGHER_RATE = 0.40;     // 40%
        public static final double ADDITIONAL_RATE = 0.45;  // 45%

        // National Insurance thresholds (2024/25)
        public static final double NI_PRIMARY_THRESHOLD = 12570.00;  // Same as personal allowance
        public static final double NI_UPPER_EARNINGS_LIMIT = 50270.00; // Same as higher rate threshold

        // National Insurance rates (employees)
        public static final double NI_MAIN_RATE = 0.08;    // 8% on earnings £12,570-£50,270
        public static final double NI_UPPER_RATE = 0.02;   // 2% on earnings over £50,270

        // Capital Gains Tax
        public static final double CGT_ANNUAL_EXEMPTION = 12300.00;
        public static final double CGT_BASIC_RATE = 0.10;  // 10%
        public static final double CGT_HIGHER_RATE = 0.20; // 20%

        // Scottish Income Tax rates (different from rest of UK)
        public static final double SCOTTISH_STARTER_RATE = 0.19;
        public static final double SCOTTISH_BASIC_RATE = 0.20;
        public static final double SCOTTISH_INTERMEDIATE_RATE = 0.21;
        public static final double SCOTTISH_HIGHER_RATE = 0.42;
        public static final double SCOTTISH_TOP_RATE = 0.47;

        // Scottish thresholds
        public static final double SCOTTISH_STARTER_THRESHOLD = 15100.00;
        public static final double SCOTTISH_BASIC_THRESHOLD = 23600.00;
        public static final double SCOTTISH_INTERMEDIATE_THRESHOLD = 39800.00;
        public static final double SCOTTISH_HIGHER_THRESHOLD = 62500.00;
    }

    /**
     * Calculate UK Income Tax for a given taxable income
     */
    public static double calculateIncomeTax(double taxableIncome, boolean isScottish) {
        if (taxableIncome <= 0) return 0.0;

        double tax = 0.0;
        double remainingIncome = taxableIncome;

        if (isScottish) {
            // Scottish Income Tax rates
            if (remainingIncome > TaxRates.SCOTTISH_HIGHER_THRESHOLD) {
                tax += (remainingIncome - TaxRates.SCOTTISH_HIGHER_THRESHOLD) * TaxRates.SCOTTISH_TOP_RATE;
                remainingIncome = TaxRates.SCOTTISH_HIGHER_THRESHOLD;
            }
            if (remainingIncome > TaxRates.SCOTTISH_INTERMEDIATE_THRESHOLD) {
                tax += (remainingIncome - TaxRates.SCOTTISH_INTERMEDIATE_THRESHOLD) * TaxRates.SCOTTISH_HIGHER_RATE;
                remainingIncome = TaxRates.SCOTTISH_INTERMEDIATE_THRESHOLD;
            }
            if (remainingIncome > TaxRates.SCOTTISH_BASIC_THRESHOLD) {
                tax += (remainingIncome - TaxRates.SCOTTISH_BASIC_THRESHOLD) * TaxRates.SCOTTISH_INTERMEDIATE_RATE;
                remainingIncome = TaxRates.SCOTTISH_BASIC_THRESHOLD;
            }
            if (remainingIncome > TaxRates.SCOTTISH_STARTER_THRESHOLD) {
                tax += (remainingIncome - TaxRates.SCOTTISH_STARTER_THRESHOLD) * TaxRates.SCOTTISH_BASIC_RATE;
                remainingIncome = TaxRates.SCOTTISH_STARTER_THRESHOLD;
            }
            if (remainingIncome > 0) {
                tax += remainingIncome * TaxRates.SCOTTISH_STARTER_RATE;
            }
        } else {
            // Rest of UK Income Tax rates
            if (remainingIncome > TaxRates.HIGHER_RATE_THRESHOLD) {
                tax += (remainingIncome - TaxRates.HIGHER_RATE_THRESHOLD) * TaxRates.ADDITIONAL_RATE;
                remainingIncome = TaxRates.HIGHER_RATE_THRESHOLD;
            }
            if (remainingIncome > TaxRates.BASIC_RATE_THRESHOLD) {
                tax += (remainingIncome - TaxRates.BASIC_RATE_THRESHOLD) * TaxRates.HIGHER_RATE;
                remainingIncome = TaxRates.BASIC_RATE_THRESHOLD;
            }
            if (remainingIncome > 0) {
                tax += remainingIncome * TaxRates.BASIC_RATE;
            }
        }

        return tax;
    }

    /**
     * Calculate National Insurance contributions
     */
    public static double calculateNationalInsurance(double grossIncome, boolean isEmployee) {
        if (grossIncome <= TaxRates.NI_PRIMARY_THRESHOLD) return 0.0;

        double ni = 0.0;
        double earnings = grossIncome;

        // For employees, NI is calculated on earnings between primary threshold and UEL
        if (earnings > TaxRates.NI_UPPER_EARNINGS_LIMIT) {
            ni += (earnings - TaxRates.NI_UPPER_EARNINGS_LIMIT) * TaxRates.NI_UPPER_RATE;
            earnings = TaxRates.NI_UPPER_EARNINGS_LIMIT;
        }

        if (earnings > TaxRates.NI_PRIMARY_THRESHOLD) {
            ni += (earnings - TaxRates.NI_PRIMARY_THRESHOLD) * TaxRates.NI_MAIN_RATE;
        }

        return ni;
    }

    /**
     * Calculate Capital Gains Tax
     */
    public static double calculateCapitalGainsTax(double capitalGains, double otherIncome, boolean isScottish) {
        if (capitalGains <= 0) return 0.0;

        // Apply annual exemption
        double taxableGains = Math.max(0, capitalGains - TaxRates.CGT_ANNUAL_EXEMPTION);

        if (taxableGains <= 0) return 0.0;

        // Determine CGT rate based on total income
        double totalIncome = otherIncome + capitalGains;
        boolean higherRate = totalIncome > TaxRates.BASIC_RATE_THRESHOLD;

        double cgtRate = higherRate ? TaxRates.CGT_HIGHER_RATE : TaxRates.CGT_BASIC_RATE;

        return taxableGains * cgtRate;
    }

    /**
     * Calculate total UK tax liability for a person
     */
    public static TaxCalculationResult calculateTotalTax(Person person,
                                                        double salaryIncome,
                                                        double pensionIncome,
                                                        double dividendIncome,
                                                        double capitalGains) {

        // Calculate gross income
        double grossIncome = salaryIncome + pensionIncome + dividendIncome;

        // Apply personal allowance
        double taxableIncome = Math.max(0, grossIncome - person.getPersonalAllowance());

        // Calculate Income Tax
        double incomeTax = calculateIncomeTax(taxableIncome, person.isScottishTaxpayer());

        // Calculate National Insurance (only on earned income, not pension)
        double niableIncome = salaryIncome + pensionIncome;
        double nationalInsurance = calculateNationalInsurance(niableIncome, true);

        // Calculate Capital Gains Tax
        double capitalGainsTax = calculateCapitalGainsTax(capitalGains, grossIncome, person.isScottishTaxpayer());

        // Calculate totals
        double totalTax = incomeTax + nationalInsurance + capitalGainsTax;
        double effectiveRate = grossIncome > 0 ? (totalTax / grossIncome) * 100 : 0;

        // Update person with tax results
        person.setTaxResults(grossIncome, taxableIncome, incomeTax, nationalInsurance, capitalGainsTax);

        return new TaxCalculationResult(grossIncome, taxableIncome, incomeTax, nationalInsurance, capitalGainsTax, totalTax, effectiveRate);
    }

    /**
     * Calculate tax efficiency metrics
     */
    public static TaxEfficiencyMetrics calculateTaxEfficiency(Person person) {
        double grossIncome = person.getGrossIncome();
        double totalTax = person.getTotalTaxPaid();
        double effectiveRate = person.getEffectiveTaxRate();

        // Calculate marginal tax rate (simplified - would need more context)
        double marginalRate = calculateMarginalRate(person.getTaxableIncome(), person.isScottishTaxpayer());

        // Calculate utilization of personal allowance
        double allowanceUtilization = grossIncome > 0 ?
            Math.min(1.0, grossIncome / (grossIncome + person.getPersonalAllowance())) : 0.0;

        return new TaxEfficiencyMetrics(effectiveRate, marginalRate, allowanceUtilization, totalTax);
    }

    /**
     * Calculate marginal tax rate for additional income
     */
    private static double calculateMarginalRate(double taxableIncome, boolean isScottish) {
        if (isScottish) {
            if (taxableIncome > TaxRates.SCOTTISH_HIGHER_THRESHOLD) return TaxRates.SCOTTISH_TOP_RATE;
            if (taxableIncome > TaxRates.SCOTTISH_INTERMEDIATE_THRESHOLD) return TaxRates.SCOTTISH_HIGHER_RATE;
            if (taxableIncome > TaxRates.SCOTTISH_BASIC_THRESHOLD) return TaxRates.SCOTTISH_INTERMEDIATE_RATE;
            if (taxableIncome > TaxRates.SCOTTISH_STARTER_THRESHOLD) return TaxRates.SCOTTISH_BASIC_RATE;
            return TaxRates.SCOTTISH_STARTER_RATE;
        } else {
            if (taxableIncome > TaxRates.HIGHER_RATE_THRESHOLD) return TaxRates.ADDITIONAL_RATE;
            if (taxableIncome > TaxRates.BASIC_RATE_THRESHOLD) return TaxRates.HIGHER_RATE;
            return TaxRates.BASIC_RATE;
        }
    }

    /**
     * Result class for tax calculations
     */
    @Data
    public static class TaxCalculationResult {
        private final double grossIncome;
        private final double taxableIncome;
        private final double incomeTax;
        private final double nationalInsurance;
        private final double capitalGainsTax;
        private final double totalTaxPaid;
        private final double effectiveTaxRate;

        public TaxCalculationResult(double grossIncome, double taxableIncome, double incomeTax,
                                  double nationalInsurance, double capitalGainsTax, double totalTaxPaid,
                                  double effectiveTaxRate) {
            this.grossIncome = grossIncome;
            this.taxableIncome = taxableIncome;
            this.incomeTax = incomeTax;
            this.nationalInsurance = nationalInsurance;
            this.capitalGainsTax = capitalGainsTax;
            this.totalTaxPaid = totalTaxPaid;
            this.effectiveTaxRate = effectiveTaxRate;
        }
    }

    /**
     * Tax efficiency metrics
     */
    public static class TaxEfficiencyMetrics {
        public final double effectiveTaxRate;
        public final double marginalTaxRate;
        public final double allowanceUtilizationRate;
        public final double totalTaxPaid;

        public TaxEfficiencyMetrics(double effectiveTaxRate, double marginalTaxRate,
                                  double allowanceUtilizationRate, double totalTaxPaid) {
            this.effectiveTaxRate = effectiveTaxRate;
            this.marginalTaxRate = marginalTaxRate;
            this.allowanceUtilizationRate = allowanceUtilizationRate;
            this.totalTaxPaid = totalTaxPaid;
        }
    }
}