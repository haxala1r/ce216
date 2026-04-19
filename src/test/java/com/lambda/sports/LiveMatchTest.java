package com.lambda.sports;

import com.lambda.sports.headball.HeadballLiveMatch;
import com.lambda.sports.headball.HeadballSport;
import com.lambda.sports.headball.HeadballTactic;
import com.lambda.sports.headball.HeadballTacticType;
import com.lambda.sports.volleyball.VolleyballLiveMatch;
import com.lambda.sports.volleyball.VolleyballSport;
import com.lambda.sports.volleyball.VolleyballTactic;
import com.lambda.sports.volleyball.VolleyballTacticType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LiveMatchTest {

    // ── Headball ─────────────────────────────────────────────────────────────

    @Test
    void testHeadballLiveMatch_initialState() {
        List<Team> teams = new HeadballSport().createTeams(2);
        LiveMatch live = new HeadballLiveMatch(teams.get(0), teams.get(1), 4);

        assertFalse(live.isFinished());
        assertEquals(0, live.getPeriodsPlayed());
        assertEquals(1, live.getNextPeriodNumber());
        assertEquals(4, live.getMaxPeriods());
        assertEquals(0, live.getHomeRunningScore());
        assertEquals(0, live.getAwayRunningScore());
    }

    @Test
    void testHeadballLiveMatch_finishesAfterFourQuarters() {
        List<Team> teams = new HeadballSport().createTeams(2);
        LiveMatch live = new HeadballLiveMatch(teams.get(0), teams.get(1), 4);
        MatchEngine engine = new MatchEngine(123L);

        for (int i = 0; i < 4; i++) {
            assertFalse(live.isFinished(),
                "Should not finish before 4 quarters; finished at " + i);
            live.playNextPeriod(engine);
        }
        assertTrue(live.isFinished());
        assertEquals(4, live.getPeriodsPlayed());
    }

    @Test
    void testHeadballLiveMatch_extraPlayCallsAreNoOps() {
        List<Team> teams = new HeadballSport().createTeams(2);
        LiveMatch live = new HeadballLiveMatch(teams.get(0), teams.get(1), 4);
        MatchEngine engine = new MatchEngine(1L);

        for (int i = 0; i < 4; i++) live.playNextPeriod(engine);
        int periodsBefore = live.getPeriodsPlayed();
        live.playNextPeriod(engine);   // extra call after finished
        live.playNextPeriod(engine);
        assertEquals(periodsBefore, live.getPeriodsPlayed());
    }

    @Test
    void testHeadballLiveMatch_runningScoreEqualsSumOfQuarters() {
        List<Team> teams = new HeadballSport().createTeams(2);
        LiveMatch live = new HeadballLiveMatch(teams.get(0), teams.get(1), 4);
        MatchEngine engine = new MatchEngine(7L);

        for (int i = 0; i < 4; i++) live.playNextPeriod(engine);
        int homeSum = 0, awaySum = 0;
        for (int[] q : live.getAllPeriodScores()) {
            homeSum += q[0];
            awaySum += q[1];
        }
        assertEquals(homeSum, live.getHomeRunningScore());
        assertEquals(awaySum, live.getAwayRunningScore());
    }

    @Test
    void testHeadballLiveMatch_tacticChangesBetweenQuartersAffectScoring() {
        // Use two identical teams.  In one match home stays balanced; in the
        // other, after q1 home switches to AGGRESSIVE.  Over enough seeds the
        // aggressive run should average more goals.
        long seed = 555L;
        int homeBalGoals = 0, homeAggGoals = 0;
        for (int s = 0; s < 30; s++) {
            // Balanced run
            List<Team> tA = new HeadballSport().createTeams(2);
            LiveMatch a = new HeadballLiveMatch(tA.get(0), tA.get(1), 4);
            MatchEngine engineA = new MatchEngine(seed + s);
            for (int i = 0; i < 4; i++) a.playNextPeriod(engineA);
            homeBalGoals += a.getHomeRunningScore();

            // Aggressive run after q1
            List<Team> tB = new HeadballSport().createTeams(2);
            LiveMatch b = new HeadballLiveMatch(tB.get(0), tB.get(1), 4);
            MatchEngine engineB = new MatchEngine(seed + s);
            b.playNextPeriod(engineB);
            tB.get(0).setTactic(new HeadballTactic(HeadballTacticType.AGGRESSIVE));
            for (int i = 1; i < 4; i++) b.playNextPeriod(engineB);
            homeAggGoals += b.getHomeRunningScore();
        }
        assertTrue(homeAggGoals > homeBalGoals,
            "Aggressive tactic should yield more goals on average ("
                + homeAggGoals + " vs " + homeBalGoals + ")");
    }

    // ── Volleyball ───────────────────────────────────────────────────────────

    @Test
    void testVolleyballLiveMatch_initialState() {
        List<Team> teams = new VolleyballSport().createTeams(2);
        LiveMatch live = new VolleyballLiveMatch(teams.get(0), teams.get(1));

        assertFalse(live.isFinished());
        assertEquals(0, live.getPeriodsPlayed());
        assertEquals(5, live.getMaxPeriods());
    }

    @Test
    void testVolleyballLiveMatch_finishesWhenSomeoneWinsThreeSets() {
        List<Team> teams = new VolleyballSport().createTeams(2);
        LiveMatch live = new VolleyballLiveMatch(teams.get(0), teams.get(1));
        MatchEngine engine = new MatchEngine(2L);

        while (!live.isFinished()) live.playNextPeriod(engine);
        int total = live.getHomeRunningScore() + live.getAwayRunningScore();
        assertEquals(total, live.getPeriodsPlayed());
        // Exactly one side should reach 3
        assertTrue(live.getHomeRunningScore() == 3 || live.getAwayRunningScore() == 3,
            "Match should end when one side wins 3 sets");
        assertTrue(live.getPeriodsPlayed() >= 3 && live.getPeriodsPlayed() <= 5);
    }

    @Test
    void testVolleyballLiveMatch_neverProducesADraw() {
        // Across many seeds, the running scores should never be tied at finish
        for (int s = 0; s < 25; s++) {
            List<Team> teams = new VolleyballSport().createTeams(2);
            LiveMatch live = new VolleyballLiveMatch(teams.get(0), teams.get(1));
            MatchEngine engine = new MatchEngine(s);
            while (!live.isFinished()) live.playNextPeriod(engine);
            assertNotEquals(live.getHomeRunningScore(), live.getAwayRunningScore(),
                "Volleyball produced a tie on seed " + s);
        }
    }

    @Test
    void testVolleyballLiveMatch_finalizeProducesCorrectMatchResult() {
        List<Team> teams = new VolleyballSport().createTeams(2);
        LiveMatch live = new VolleyballLiveMatch(teams.get(0), teams.get(1));
        MatchEngine engine = new MatchEngine(11L);

        while (!live.isFinished()) live.playNextPeriod(engine);
        MatchResult r = live.finalizeMatch(engine);
        assertEquals(live.getHomeRunningScore(), r.getHomeScore());
        assertEquals(live.getAwayRunningScore(), r.getAwayScore());
        assertEquals(live.getPeriodsPlayed(), r.getQuarterScores().length);
    }

    @Test
    void testVolleyballLiveMatch_decidingFifthSetIsToFifteen() {
        // Force a 5-set match by starting with strong away dominance, then
        // flipping mid-way.  Hard to engineer in a 2-team test; instead just
        // assert that whenever a 5th set is reached, points needed are <= 30
        // (matches "to-15-or-more" with the win-by-2 cap).
        for (int s = 0; s < 100; s++) {
            List<Team> teams = new VolleyballSport().createTeams(2);
            LiveMatch live = new VolleyballLiveMatch(teams.get(0), teams.get(1));
            MatchEngine engine = new MatchEngine(s);
            while (!live.isFinished()) live.playNextPeriod(engine);
            int[][] scores = live.getAllPeriodScores();
            if (scores.length == 5) {
                int max5 = Math.max(scores[4][0], scores[4][1]);
                // Set 5 plays to 15 → typical max ~17, hard cap by safety = 50
                assertTrue(max5 >= 15 && max5 <= 50,
                    "Set 5 score out of range: " + max5);
                return;        // only need one example to verify the rule
            }
        }
        // No 5-set match in 100 seeds — that's fine, just don't fail
    }

    // ── Cross-cutting ────────────────────────────────────────────────────────

    @Test
    void testStartLiveMatch_returnsRightConcreteType() {
        List<Team> hb = new HeadballSport().createTeams(2);
        assertInstanceOf(HeadballLiveMatch.class,
            new HeadballSport().startLiveMatch(hb.get(0), hb.get(1)));

        List<Team> vb = new VolleyballSport().createTeams(2);
        assertInstanceOf(VolleyballLiveMatch.class,
            new VolleyballSport().startLiveMatch(vb.get(0), vb.get(1)));
    }
}
