package com.lambda.sports.volleyball;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class VolleyballTacticTest {

    @Test
    void testOffensive_boostsAttackPenalisesDefence() {
        VolleyballTactic t = new VolleyballTactic(VolleyballTacticType.OFFENSIVE);
        assertTrue(t.getAttackMultiplier()  > 1.0);
        assertTrue(t.getDefenseMultiplier() < 1.0);
    }

    @Test
    void testDefensive_boostsDefencePenalisesAttack() {
        VolleyballTactic t = new VolleyballTactic(VolleyballTacticType.DEFENSIVE);
        assertTrue(t.getDefenseMultiplier() > 1.0);
        assertTrue(t.getAttackMultiplier()  < 1.0);
    }

    @Test
    void testAllAround_neutralOnBothSides() {
        VolleyballTactic t = new VolleyballTactic(VolleyballTacticType.ALL_AROUND);
        assertEquals(1.0, t.getAttackMultiplier(),  0.0001);
        assertEquals(1.0, t.getDefenseMultiplier(), 0.0001);
    }

    @Test
    void testTacticName_isEnumName() {
        assertEquals("OFFENSIVE",
            new VolleyballTactic(VolleyballTacticType.OFFENSIVE).getTacticName());
    }
}
