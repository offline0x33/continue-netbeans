package com.bajinho.continuebeans;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class AgentModeTest {

    @Test
    void defaultModeIsCode() {
        assertEquals(AgentMode.CODE, AgentMode.defaultMode());
        assertEquals("Code", AgentMode.defaultMode().getLabel());
        assertNotNull(AgentMode.defaultMode().getSystemHint());
    }

    @Test
    void fromLabelAcceptsLabelsCaseInsensitively() {
        assertEquals(AgentMode.CODE, AgentMode.fromLabel("Code"));
        assertEquals(AgentMode.PLANNING, AgentMode.fromLabel("planning"));
        assertEquals(AgentMode.DOCS, AgentMode.fromLabel("DOCS"));
        assertEquals(AgentMode.AGENT, AgentMode.fromLabel("Agent"));
        assertEquals(AgentMode.PLANNING, AgentMode.fromLabel(" Planning "));
    }

    @Test
    void fromLabelAcceptsEnumNames() {
        assertEquals(AgentMode.CODE, AgentMode.fromLabel("CODE"));
        assertEquals(AgentMode.PLANNING, AgentMode.fromLabel("Planning"));
        assertEquals(AgentMode.DOCS, AgentMode.fromLabel("docs"));
        assertEquals(AgentMode.AGENT, AgentMode.fromLabel("AGENT"));
    }

    @Test
    void fromLabelFallsBackToDefaultForMissingOrInvalidValues() {
        assertEquals(AgentMode.CODE, AgentMode.fromLabel(null));
        assertEquals(AgentMode.CODE, AgentMode.fromLabel(""));
        assertEquals(AgentMode.CODE, AgentMode.fromLabel("   "));
        assertEquals(AgentMode.CODE, AgentMode.fromLabel("unknown"));
    }

    @Test
    void toStringReturnsUserFacingLabel() {
        assertEquals("Code", AgentMode.CODE.toString());
        assertEquals("Planning", AgentMode.PLANNING.toString());
        assertEquals("Docs", AgentMode.DOCS.toString());
        assertEquals("Agent", AgentMode.AGENT.toString());
    }

    @Test
    void labelsAndHintsAreDefinedForEveryMode() {
        for (AgentMode mode : AgentMode.values()) {
            assertNotNull(mode.getLabel());
            assertNotNull(mode.getSystemHint());
        }
    }
}
