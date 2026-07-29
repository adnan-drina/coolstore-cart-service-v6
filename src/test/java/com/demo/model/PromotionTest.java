package com.demo.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class PromotionTest {

    @Test
    void defaultConstructorCreatesEmptyPromotion() {
        Promotion p = new Promotion();
        assertNull(p.getItemId());
        assertEquals(0.0, p.getPercentOff(), 0);
    }

    @Test
    void parameterizedConstructorSetsAllFields() {
        Promotion p = new Promotion("1111", 15.5);
        assertEquals("1111", p.getItemId());
        assertEquals(15.5, p.getPercentOff(), 0.001);
    }

    @Test
    void settersUpdateFields() {
        Promotion p = new Promotion();
        p.setItemId("2222");
        p.setPercentOff(25.0);

        assertEquals("2222", p.getItemId());
        assertEquals(25.0, p.getPercentOff(), 0.001);
    }

    @Test
    void toStringContainsAllFields() {
        Promotion p = new Promotion("1111", 15.5);
        String s = p.toString();
        assertTrue(s.contains("1111"));
        assertTrue(s.contains("15.5"));
    }

    @Test
    void zeroPercentOffPreserved() {
        Promotion p = new Promotion("9999", 0);
        assertEquals(0.0, p.getPercentOff(), 0.001);
    }

    @Test
    void largePercentOffPreserved() {
        Promotion p = new Promotion("9999", 100);
        assertEquals(100.0, p.getPercentOff(), 0.001);
    }
}
