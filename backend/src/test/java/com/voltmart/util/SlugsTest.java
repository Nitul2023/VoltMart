package com.voltmart.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SlugsTest {
    @Test
    void normalizesName() {
        assertEquals("nexora-pulse-1", Slugs.from("Nexora Pulse 1"));
    }

    @Test
    void blankBecomesItem() {
        assertEquals("item", Slugs.from("   "));
    }
}
