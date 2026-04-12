package com.lambda.sports;

import com.lambda.sports.headball.HeadballPlayer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HeadballPlayerTest {

    @Test
    void testSkillRating_calculatesWeightedFormula() {
        HeadballPlayer p = new HeadballPlayer("Tester", 25, 8.0, 6.0);
        // 8.0*0.6 + 6.0*0.4 = 4.8 + 2.4 = 7.2
        assertEquals(7.2, p.getSkillRating(), 0.001);
    }

    @Test
    void testSkillRating_pureHeadingSkill() {
        HeadballPlayer p = new HeadballPlayer("Heading", 22, 10.0, 0.0);
        // 10*0.6 + 0*0.4 = 6.0
        assertEquals(6.0, p.getSkillRating(), 0.001);
    }

    @Test
    void testSkillRating_pureJumpSkill() {
        HeadballPlayer p = new HeadballPlayer("Jumper", 22, 0.0, 10.0);
        // 0*0.6 + 10*0.4 = 4.0
        assertEquals(4.0, p.getSkillRating(), 0.001);
    }

    @Test
    void testInitialState_notInjured() {
        HeadballPlayer p = new HeadballPlayer("Fresh", 20, 7.0, 7.0);
        assertFalse(p.isInjured());
        assertEquals(0, p.getInjuryGamesLeft());
    }

    @Test
    void testApplyInjury_setsInjuredTrue() {
        HeadballPlayer p = new HeadballPlayer("PlayerA", 24, 6.0, 6.0);
        p.applyInjury(3);
        assertTrue(p.isInjured());
    }

    @Test
    void testApplyInjury_setsCorrectGamesLeft() {
        HeadballPlayer p = new HeadballPlayer("PlayerB", 24, 6.0, 6.0);
        p.applyInjury(3);
        assertEquals(3, p.getInjuryGamesLeft());
    }

    @Test
    void testRecover_decrementsInjuryCounter() {
        HeadballPlayer p = new HeadballPlayer("PlayerC", 24, 6.0, 6.0);
        p.applyInjury(3);
        p.recover();
        assertEquals(2, p.getInjuryGamesLeft());
        assertTrue(p.isInjured());
    }

    @Test
    void testRecover_clearsInjuredFlagWhenCounterReachesZero() {
        HeadballPlayer p = new HeadballPlayer("PlayerD", 24, 6.0, 6.0);
        p.applyInjury(1);
        p.recover();
        assertFalse(p.isInjured());
        assertEquals(0, p.getInjuryGamesLeft());
    }
}
