package com.lambda.sports;

import com.lambda.sports.headball.HeadballSport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HeadballSportTest {

    private HeadballSport sport;
    private MatchEngine engine;

    @BeforeEach
    void setUp() {
        sport  = new HeadballSport();
        engine = new MatchEngine(42L);
    }

    @Test
    void testWinPoints_isTwo() {
        assertEquals(2, sport.getWinPoints());
    }

    @Test
    void testDrawPoints_isOne() {
        assertEquals(1, sport.getDrawPoints());
    }

    @Test
    void testPlayersPerTeam_isSix() {
        assertEquals(6, sport.getPlayersPerTeam());
    }

    @Test
    void testNumQuarters_isFour() {
        assertEquals(4, sport.getNumQuarters());
    }

    @Test
    void testCreateTeams_returnsRequestedCount() {
        assertEquals(4, sport.createTeams(4).size());
    }

    @Test
    void testCreateTeams_eachTeamHasAtLeastPlayersPerTeamMembers() {
        List<Team> teams = sport.createTeams(4);
        for (Team t : teams) {
            assertTrue(t.getPlayers().size() >= sport.getPlayersPerTeam(),
                    t.getName() + " has fewer players than required");
        }
    }

    @Test
    void testCreateTeams_teamsHaveDistinctNames() {
        List<Team> teams = sport.createTeams(4);
        long distinct = teams.stream().map(Team::getName).distinct().count();
        assertEquals(4, distinct);
    }

    @Test
    void testGenerateSchedule_doubleRoundRobin_correctMatchCount() {
        List<Team> teams = sport.createTeams(4);
        // double round-robin: n*(n-1) = 4*3 = 12 matches
        assertEquals(12, sport.generateSchedule(teams, engine).size());
    }

    @Test
    void testSimulateMatch_returnsNonNullResultWithNonNegativeScores() {
        List<Team> teams = sport.createTeams(2);
        MatchResult r = sport.simulateMatch(teams.get(0), teams.get(1), engine);
        assertNotNull(r);
        assertTrue(r.getHomeScore() >= 0);
        assertTrue(r.getAwayScore() >= 0);
    }
}
