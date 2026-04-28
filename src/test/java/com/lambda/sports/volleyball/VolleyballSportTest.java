package com.lambda.sports.volleyball;

import com.lambda.sports.MatchEngine;
import com.lambda.sports.MatchResult;
import com.lambda.sports.Team;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class VolleyballSportTest {

    private VolleyballSport sport;
    private MatchEngine engine;

    @BeforeEach
    void setUp() {
        sport  = new VolleyballSport();
        engine = new MatchEngine(123L);
    }

    @Test
    void testWinPoints_isThree() {
        assertEquals(3, sport.getWinPoints());
    }

    @Test
    void testDrawPoints_isZero_becauseVolleyballHasNoDraws() {
        assertEquals(0, sport.getDrawPoints());
    }

    @Test
    void testPlayersPerTeam_isSix() {
        assertEquals(6, sport.getPlayersPerTeam());
    }

    @Test
    void testNumQuarters_isFive_max5SetsInBestOf5() {
        assertEquals(5, sport.getNumQuarters());
    }

    @Test
    void testCreateTeams_returnsRequestedCount() {
        assertEquals(4, sport.createTeams(4).size());
    }

    @Test
    void testCreateTeams_eachTeamHasAtLeastTwelvePlayers() {
        // 6 starters + 6 subs
        for (Team t : sport.createTeams(4)) {
            assertTrue(t.getPlayers().size() >= 12,
                t.getName() + " has " + t.getPlayers().size() + " players, expected ≥ 12");
        }
    }

    @Test
    void testSimulateMatch_alwaysHasAWinner_noDrawsPossible() {
        List<Team> teams = sport.createTeams(2);
        for (int i = 0; i < 20; i++) {                     // multiple seeds
            MatchEngine e = new MatchEngine(i);
            MatchResult r = sport.simulateMatch(teams.get(0), teams.get(1), e);
            assertNotEquals(r.getHomeScore(), r.getAwayScore(),
                "Volleyball produced a draw on seed " + i);
        }
    }

    @Test
    void testSimulateMatch_winningSideHasExactlyThreeSets() {
        List<Team> teams = sport.createTeams(2);
        MatchResult r = sport.simulateMatch(teams.get(0), teams.get(1), engine);
        int winner = Math.max(r.getHomeScore(), r.getAwayScore());
        int loser  = Math.min(r.getHomeScore(), r.getAwayScore());
        assertEquals(3, winner, "Winner should always reach 3 sets");
        assertTrue(loser < 3,  "Loser should have fewer than 3 sets");
    }

    @Test
    void testSimulateMatch_setsPlayedBetween3And5() {
        List<Team> teams = sport.createTeams(2);
        MatchResult r = sport.simulateMatch(teams.get(0), teams.get(1), engine);
        int sets = r.getQuarterScores().length;
        assertTrue(sets >= 3 && sets <= 5,
            "Sets played must be in [3,5], got " + sets);
    }

    @Test
    void testSimulateMatch_setsWonEqualsTotalSetsPlayed() {
        List<Team> teams = sport.createTeams(2);
        MatchResult r = sport.simulateMatch(teams.get(0), teams.get(1), engine);
        assertEquals(r.getQuarterScores().length, r.getHomeScore() + r.getAwayScore());
    }

    @Test
    void testGenerateSchedule_doubleRoundRobin_correctMatchCount() {
        List<Team> teams = sport.createTeams(4);
        // double round-robin: n*(n-1) = 4*3 = 12
        assertEquals(12, sport.generateSchedule(teams, engine).size());
    }
}
