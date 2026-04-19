package com.lambda.sports;

import com.lambda.sports.headball.HeadballPlayer;
import com.lambda.sports.volleyball.VolleyballPlayer;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CoachTest {

    @Test
    void testTrain_increasesHeadballPlayerSkill() {
        Coach c = new Coach("Coach Andy", 50, 7.0);
        HeadballPlayer p = new HeadballPlayer("Tester", 22, 5.0, 5.0);
        double before = p.getSkillRating();
        c.train(List.of(p));
        assertTrue(p.getSkillRating() > before,
            "Skill should increase after training (was " + before
                + ", now " + p.getSkillRating() + ")");
    }

    @Test
    void testTrain_increasesVolleyballPlayerSkill() {
        Coach c = new Coach("Coach Beata", 48, 6.0);
        VolleyballPlayer p = new VolleyballPlayer("Tester", 23, 5.0, 5.0, 5.0);
        double before = p.getSkillRating();
        c.train(List.of(p));
        assertTrue(p.getSkillRating() > before);
    }

    @Test
    void testTrain_skipsInjuredPlayers() {
        Coach c = new Coach("Coach Carl", 55, 8.0);
        HeadballPlayer p = new HeadballPlayer("Hurt", 22, 5.0, 5.0);
        p.applyInjury(2);
        double before = p.getSkillRating();
        c.train(List.of(p));
        assertEquals(before, p.getSkillRating(), 0.0001,
            "Injured players should not be trained");
    }

    @Test
    void testTrain_skillIsClampedToTen() {
        Coach c = new Coach("Coach Drago", 60, 10.0);   // maximum training skill
        HeadballPlayer p = new HeadballPlayer("Maxed", 22, 9.95, 9.95);
        // Train many times — skill must never exceed 10.
        for (int i = 0; i < 50; i++) c.train(List.of(p));
        assertTrue(p.getHeadingAbility() <= 10.0);
        assertTrue(p.getJumpHeight()     <= 10.0);
        assertEquals(10.0, p.getSkillRating(), 0.001);
    }

    @Test
    void testTrain_higherSkillCoach_givesBiggerBoost() {
        HeadballPlayer p1 = new HeadballPlayer("A", 22, 5.0, 5.0);
        HeadballPlayer p2 = new HeadballPlayer("B", 22, 5.0, 5.0);
        new Coach("Weak", 50, 2.0).train(List.of(p1));
        new Coach("Strong", 50, 9.0).train(List.of(p2));
        assertTrue(p2.getSkillRating() > p1.getSkillRating(),
            "Higher-skill coach should boost more");
    }

    @Test
    void testAgeByOneYear_incrementsAge() {
        Coach c = new Coach("Coach Age", 40, 5.0);
        c.ageByOneYear();
        assertEquals(41, c.getAge());
    }
}
