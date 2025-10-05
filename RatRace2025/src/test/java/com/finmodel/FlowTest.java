package com.finmodel;

import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class FlowTest {

    @Test
    void testFlowCreation() {
        EntityVersion source = new EntityVersion(
                Entity.builder().id("source").build(),
                new Date(),
                0,
                100.0,
                0.0,
                Map.of(),
                null
        );
        EntityVersion target = new EntityVersion(
                Entity.builder().id("target").build(),
                new Date(),
                0,
                0.0,
                0.0,
                Map.of(),
                null
        );

        Flow flow = Flow.builder()
                .id("flow-id")
                .source(source)
                .target(target)
                .amount(50.0)
                .direction("out")
                .type("transfer")
                .metadata(Map.of("note", "test"))
                .isIntraPeriod(true)
                .build();

        assertEquals("flow-id", flow.getId());
        assertEquals(source, flow.getSource());
        assertEquals(target, flow.getTarget());
        assertEquals(50.0, flow.getAmount());
        assertEquals("out", flow.getDirection());
        assertEquals("transfer", flow.getType());
        assertEquals(Map.of("note", "test"), flow.getMetadata());
        assertTrue(flow.isIntraPeriod());
    }

    @Test
    void testValidateValidFlow() {
        Flow flow = Flow.builder()
                .id("valid-id")
                .source(new EntityVersion(Entity.builder().id("s").build(), new Date(), 0, 0, 0, Map.of(), null))
                .target(new EntityVersion(Entity.builder().id("t").build(), new Date(), 0, 0, 0, Map.of(), null))
                .amount(10.0)
                .direction("in")
                .type("payment")
                .metadata(Map.of())
                .isIntraPeriod(false)
                .build();

        assertDoesNotThrow(flow::validate);
    }

    @Test
    void testValidateInvalidId() {
        Flow flow = Flow.builder()
                .id("")
                .source(new EntityVersion(Entity.builder().id("s").build(), new Date(), 0, 0, 0, Map.of(), null))
                .target(new EntityVersion(Entity.builder().id("t").build(), new Date(), 0, 0, 0, Map.of(), null))
                .amount(10.0)
                .direction("in")
                .type("payment")
                .build();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, flow::validate);
        assertEquals("Flow id cannot be null or empty", exception.getMessage());
    }

    @Test
    void testValidateNegativeAmount() {
        Flow flow = Flow.builder()
                .id("id")
                .source(new EntityVersion(Entity.builder().id("s").build(), new Date(), 0, 0, 0, Map.of(), null))
                .target(new EntityVersion(Entity.builder().id("t").build(), new Date(), 0, 0, 0, Map.of(), null))
                .amount(-5.0)
                .direction("in")
                .type("payment")
                .build();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, flow::validate);
        assertEquals("Flow amount cannot be negative", exception.getMessage());
    }
}