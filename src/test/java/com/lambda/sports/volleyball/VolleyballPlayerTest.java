package com.lambda.sports.volleyball;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class VolleyballPlayerTest {

    @Test
    void testSkillRating_calculatesWeightedFormula() {
        VolleyballPlayer p = new VolleyballPlayer("Tester", 25, 8.0, 6.0, 5.0);
        // 8.0*0.45 + 6.0*0.35 + 5.0*0.20 = 3.6 + 2.1 + 1.0 = 6.7
        assertEquals(6.7, p.getSkillRating(), 0.001);
    }

    @Test
    void testSkillRating_pureAttacker() {
        VolleyballPlayer p = new VolleyballPlayer("Spike", 22, 10.0, 0.0, 0.0);
        // 10*0.45 = 4.5
        assertEquals(4.5, p.getSkillRating(), 0.001);
    }

    @Test
    void testSkillRating_pureDefender() {
        VolleyballPlayer p = new VolleyballPlayer("Block", 22, 0.0, 10.0, 0.0);
        // 10*0.35 = 3.5
        assertEquals(3.5, p.getSkillRating(), 0.001);
    }

    @Test
    void testSkillRating_pureServer() {
        VolleyballPlayer p = new VolleyballPlayer("Serve", 22, 0.0, 0.0, 10.0);
        // 10*0.20 = 2.0
        assertEquals(2.0, p.getSkillRating(), 0.001);
    }

    @Test
    void testInitialState_notInjured() {
        VolleyballPlayer p = new VolleyballPlayer("Fresh", 20, 7.0, 7.0, 7.0);
        assertFalse(p.isInjured());
        assertEquals(0, p.getInjuryGamesLeft());
    }

    @Test
    void testInjuryLifecycle_inheritedFromAbstractPlayer() {
        VolleyballPlayer p = new VolleyballPlayer("Hurt", 24, 6.0, 6.0, 6.0);
        p.applyInjury(2);
        assertTrue(p.isInjured());
        assertEquals(2, p.getInjuryGamesLeft());
        p.recover();
        assertEquals(1, p.getInjuryGamesLeft());
        p.recover();
        assertFalse(p.isInjured());
        assertEquals(0, p.getInjuryGamesLeft());
    }

    @Test
    void testAttributeGetters_returnConstructorValues() {
        VolleyballPlayer p = new VolleyballPlayer("Multi", 26, 7.5, 6.5, 5.5);
        assertEquals(7.5, p.getAttackSkill());
        assertEquals(6.5, p.getDefenseSkill());
        assertEquals(5.5, p.getServeSkill());
    }
}
