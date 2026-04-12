package com.lambda.sports;

import com.lambda.sports.headball.HeadballPlayer;
import com.lambda.sports.headball.HeadballTactic;
import com.lambda.sports.headball.HeadballTacticType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MatchEngineTest {

    private Team home;
    private Team away;

    /** Builds a 6-player team with identical, balanced players. */
    private Team buildTeam(String name) {
        Team t = new Team(name);
        t.setTactic(new HeadballTactic(HeadballTacticType.BALANCED));
        for (int i = 0; i < 6; i++) {
            t.addPlayer(new HeadballPlayer("P" + i, 22, 7.0, 6.0));
        }
        return t;
    }

    @BeforeEach
    void setUp() {
        home = buildTeam("Home FC");
        away = buildTeam("Away FC");
    }

    @Test
    void testSimulateMatch_returnsNonNull() {
        MatchEngine engine = new MatchEngine(1L);
        assertNotNull(engine.simulateMatch(home, away, 4));
    }

    @Test
    void testSimulateMatch_scoresAreNonNegative() {
        MatchEngine engine = new MatchEngine(2L);
        MatchResult r = engine.simulateMatch(home, away, 4);
        assertTrue(r.getHomeScore() >= 0);
        assertTrue(r.getAwayScore() >= 0);
    }

    @Test
    void testSimulateMatch_correctQuarterCount() {
        MatchEngine engine = new MatchEngine(3L);
        MatchResult r = engine.simulateMatch(home, away, 4);
        assertEquals(4, r.getQuarterScores().length);
    }

    @Test
    void testSimulateMatch_quarterScoresSumToTotal() {
        MatchEngine engine = new MatchEngine(4L);
        MatchResult r = engine.simulateMatch(home, away, 4);
        int[][] qs = r.getQuarterScores();
        int homeSum = 0, awaySum = 0;
        for (int[] q : qs) { homeSum += q[0]; awaySum += q[1]; }
        assertEquals(r.getHomeScore(), homeSum);
        assertEquals(r.getAwayScore(), awaySum);
    }

    @Test
    void testSimulateMatch_correctTeamsStoredInResult() {
        MatchEngine engine = new MatchEngine(5L);
        MatchResult r = engine.simulateMatch(home, away, 4);
        assertEquals(home, r.getHome());
        assertEquals(away, r.getAway());
    }

    @Test
    void testSimulateMatch_deterministicWithSameSeed() {
        // Use fresh identical teams so first run's injuries don't affect second run
        Team h1 = buildTeam("H1"), a1 = buildTeam("A1");
        Team h2 = buildTeam("H2"), a2 = buildTeam("A2");
        MatchResult r1 = new MatchEngine(999L).simulateMatch(h1, a1, 4);
        MatchResult r2 = new MatchEngine(999L).simulateMatch(h2, a2, 4);
        assertEquals(r1.getHomeScore(), r2.getHomeScore());
        assertEquals(r1.getAwayScore(), r2.getAwayScore());
    }

    @Test
    void testHandleInjuries_decrementsInjuredPlayerCounter() {
        HeadballPlayer injured = new HeadballPlayer("Hurt", 25, 7.0, 6.0);
        injured.applyInjury(3);
        home.addPlayer(injured);
        new MatchEngine(7L).handleInjuries(home);
        assertEquals(2, injured.getInjuryGamesLeft());
    }
}
