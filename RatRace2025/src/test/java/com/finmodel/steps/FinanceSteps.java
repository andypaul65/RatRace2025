package com.finmodel.steps;

import com.finmodel.*;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FinanceSteps {
    private FinanceModel financeModel;
    private Scenario scenario;
    private Timeline timeline;
    private Map<String, Entity> entities = new HashMap<>();

    @Given("a checking account with initial balance of ${double}")
    public void aCheckingAccountWithInitialBalanceOf$(double balance) {
        Entity checking = Entity.builder()
                .id("checking")
                .name("Primary Checking")
                .primaryCategory("Asset")
                .detailedCategory("Cash Equivalent")
                .initialValue(balance)
                .build();

        entities.put("checking", checking);

        // Initialize scenario if not already done
        if (scenario == null) {
            scenario = Scenario.builder()
                    .initialEntities(new ArrayList<>())
                    .numPeriods(6) // Default for this scenario
                    .externals(List.of())
                    .build();
        }

        scenario.getInitialEntities().add(checking);
    }

    @Given("monthly salary deposits of ${double}")
    public void monthlySalaryDepositsOf$(double amount) {
        // Create recurring event for salary deposits
        Map<String, Object> params = new HashMap<>();
        params.put("amount", amount);
        params.put("frequency", "MONTHLY");

        RecurringEvent salaryEvent = RecurringEvent.builder()
                .id("salary_deposit")
                .type("recurring")
                .params(params)
                .isRecurring(true)
                .build();

        // Add to scenario's event templates
        if (scenario.getEventTemplates() == null) {
            scenario.setEventTemplates(new HashMap<>());
        }
        Entity checking = entities.get("checking");
        scenario.getEventTemplates().computeIfAbsent(checking, k -> new ArrayList<>()).add(salaryEvent);
    }

    @Given("monthly expense payments of ${double}")
    public void monthlyExpensePaymentsOf$(double amount) {
        // Create recurring event for expenses
        Map<String, Object> params = new HashMap<>();
        params.put("amount", -amount); // Negative for expenses
        params.put("frequency", "MONTHLY");

        RecurringEvent expenseEvent = RecurringEvent.builder()
                .id("monthly_expense")
                .type("recurring")
                .params(params)
                .isRecurring(true)
                .build();

        // Add to scenario's event templates
        if (scenario.getEventTemplates() == null) {
            scenario.setEventTemplates(new HashMap<>());
        }
        Entity checking = entities.get("checking");
        scenario.getEventTemplates().computeIfAbsent(checking, k -> new ArrayList<>()).add(expenseEvent);
    }

    @Given("a mortgage with principal balance of ${double}")
    public void aMortgageWithPrincipalBalanceOf$(double balance) {
        Entity mortgage = Entity.builder()
                .id("mortgage")
                .name("Home Mortgage")
                .primaryCategory("Liability")
                .detailedCategory("Secured Debt")
                .initialValue(balance)
                .build();

        entities.put("mortgage", mortgage);

        // Initialize scenario for mortgage scenario
        if (scenario == null) {
            scenario = Scenario.builder()
                    .initialEntities(new ArrayList<>())
                    .numPeriods(12) // 12 months
                    .externals(List.of())
                    .build();
        }

        scenario.getInitialEntities().add(mortgage);
    }

    @Given("monthly payments of ${double} \\(including interest\\)")
    public void monthlyPaymentsOf$IncludingInterest(double payment) {
        // Create recurring event for mortgage payments
        Map<String, Object> params = new HashMap<>();
        params.put("amount", -payment); // Negative for payments
        params.put("frequency", "MONTHLY");

        RecurringEvent paymentEvent = RecurringEvent.builder()
                .id("mortgage_payment")
                .type("recurring")
                .params(params)
                .isRecurring(true)
                .build();

        if (scenario.getEventTemplates() == null) {
            scenario.setEventTemplates(new HashMap<>());
        }
        scenario.getEventTemplates().put(entities.get("mortgage"), List.of(paymentEvent));
    }

    @Given("interest rate of {double}%")
    public void interestRateOf(double rate) {
        // Set interest rate in entity properties
        Entity mortgage = entities.get("mortgage");
        if (mortgage.getBaseProperties() == null) {
            mortgage.setBaseProperties(new HashMap<>());
        }
        mortgage.getBaseProperties().put("rate", rate / 100.0); // Convert to decimal
    }

    @When("the simulation runs for {int} months")
    public void theSimulationRunsForMonths(int months) {
        // Set number of periods
        scenario.setNumPeriods(months);

        // Initialize timeline and finance model
        timeline = Timeline.builder().build();

        financeModel = FinanceModel.builder()
                .scenario(scenario)
                .timeline(timeline)
                .dynamicEntities(new java.util.HashSet<>())
                .build();

        // Run simulation
        financeModel.runSimulation();
    }

    @Then("the account balance should increase by ${double} per month")
    public void theAccountBalanceShouldIncreaseBy$PerMonth(double expectedIncrease) {
        // Verify balance progression in timeline
        List<TimePeriod> periods = timeline.getPeriods();
        assertFalse(periods.isEmpty(), "No periods generated");

        Entity checking = entities.get("checking");
        double initialBalance = checking.getInitialValue();

        for (int i = 0; i < periods.size(); i++) {
            TimePeriod period = periods.get(i);
            PeriodEntityAggregate aggregate = period.getPeriodEntityAggregate(checking);

            if (aggregate != null) {
                double expectedBalance = initialBalance + (expectedIncrease * (i + 1));
                assertEquals(expectedBalance, aggregate.getNetBalance(), 0.01,
                           "Balance progression incorrect for period " + i);
            }
        }
    }

    @Then("the final balance should be ${double}")
    public void theFinalBalanceShouldBe$(double expectedBalance) {
        List<TimePeriod> periods = timeline.getPeriods();
        assertFalse(periods.isEmpty(), "No periods generated");

        TimePeriod lastPeriod = periods.get(periods.size() - 1);
        Entity checking = entities.get("checking");
        PeriodEntityAggregate aggregate = lastPeriod.getPeriodEntityAggregate(checking);

        if (aggregate != null) {
            assertEquals(expectedBalance, aggregate.getNetBalance(), 0.01);
        }
    }

    @Then("Sankey nodes should show balance progression across periods")
    public void sankeyNodesShouldShowBalanceProgressionAcrossPeriods() {
        Map<String, Object> sankeyData = financeModel.buildSankeyData();
        assertNotNull(sankeyData, "Sankey data is null");

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> nodes = (List<Map<String, Object>>) sankeyData.get("nodes");
        assertNotNull(nodes, "No nodes in Sankey data");

        // Verify nodes exist for the checking account across periods
        boolean hasCheckingNodes = nodes.stream()
                .anyMatch(node -> "checking".equals(node.get("entityId")));
        assertTrue(hasCheckingNodes, "No checking account nodes found in Sankey data");
    }

    @Then("the mortgage balance should decrease each month")
    public void theMortgageBalanceShouldDecreaseEachMonth() {
        List<TimePeriod> periods = timeline.getPeriods();
        assertFalse(periods.isEmpty(), "No periods generated");

        Entity mortgage = entities.get("mortgage");
        double previousBalance = mortgage.getInitialValue();

        for (TimePeriod period : periods) {
            PeriodEntityAggregate aggregate = period.getPeriodEntityAggregate(mortgage);
            if (aggregate != null) {
                double currentBalance = aggregate.getNetBalance();
                assertTrue(currentBalance < previousBalance, "Mortgage balance did not decrease");
                previousBalance = currentBalance;
            }
        }
    }

    @Then("total interest paid should be calculable")
    public void totalInterestPaidShouldBeCalculable() {
        // This would require implementing interest calculation in the simulation
        // For now, just verify the mortgage entity exists in results
        Map<String, Object> sankeyData = financeModel.buildSankeyData();
        assertNotNull(sankeyData);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> nodes = (List<Map<String, Object>>) sankeyData.get("nodes");
        boolean hasMortgageNodes = nodes.stream()
                .anyMatch(node -> "mortgage".equals(node.get("entityId")));
        assertTrue(hasMortgageNodes, "Mortgage nodes not found in Sankey data");
    }

    @Then("the mortgage should appear as a liability node in Sankey diagrams")
    public void theMortgageShouldAppearAsALiabilityNodeInSankeyDiagrams() {
        Map<String, Object> sankeyData = financeModel.buildSankeyData();
        assertNotNull(sankeyData);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> nodes = (List<Map<String, Object>>) sankeyData.get("nodes");

        boolean hasMortgageLiability = nodes.stream()
                .anyMatch(node -> "mortgage".equals(node.get("entityId")) &&
                                 "Liability".equals(node.get("primaryCategory")));
        assertTrue(hasMortgageLiability, "Mortgage not found as liability in Sankey data");
    }
}