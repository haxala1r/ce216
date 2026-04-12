package com.lambda.sports;

import com.lambda.sports.headball.HeadballPlayer;
import com.lambda.sports.headball.HeadballSport;
import com.lambda.sports.headball.HeadballTactic;
import com.lambda.sports.headball.HeadballTacticType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LeagueTest {

    private HeadballSport sport;
    private Team teamA, teamB, teamC;
    private League league;

    private Team createTeam(String name) {
        Team t = new Team(name);
        t.setTactic(new HeadballTactic(HeadballTacticType.BALANCED));
        for (int i = 0; i < 6; i++) t.addPlayer(new HeadballPlayer("P" + i, 22, 7.0, 6.0));
        return t;
    }

    /** Builds a MatchResult with the given scores; quarter layout is irrelevant here. */
    private MatchResult makeResult(Team home, Team away, int homeScore, int awayScore) {
        int[][] qs = new int[4][2];
        qs[0][0] = homeScore;
        qs[0][1] = awayScore;
        return new MatchResult(home, away, homeScore, awayScore, qs);
    }

    @BeforeEach
    void setUp() {
        sport  = new HeadballSport();
        teamA  = createTeam("Team A");
        teamB  = createTeam("Team B");
        teamC  = createTeam("Team C");
        league = new League("Test League", sport, List.of(teamA, teamB, teamC));
    }

    @Test
    void testRecordResult_homeWin_winnerReceivesWinPoints() {
        league.recordResult(makeResult(teamA, teamB, 3, 1));
        assertEquals(sport.getWinPoints(), league.getStandings().get(teamA).getPoints());
    }

    @Test
    void testRecordResult_homeWin_loserReceivesZeroPoints() {
        league.recordResult(makeResult(teamA, teamB, 3, 1));
        assertEquals(0, league.getStandings().get(teamB).getPoints());
    }

    @Test
    void testRecordResult_draw_bothTeamsReceiveDrawPoints() {
        league.recordResult(makeResult(teamA, teamB, 2, 2));
        assertEquals(sport.getDrawPoints(), league.getStandings().get(teamA).getPoints());
        assertEquals(sport.getDrawPoints(), league.getStandings().get(teamB).getPoints());
    }

    @Test
    void testRecordResult_awayWin_awayTeamReceivesWinPoints() {
        league.recordResult(makeResult(teamA, teamB, 0, 2));
        assertEquals(sport.getWinPoints(), league.getStandings().get(teamB).getPoints());
        assertEquals(0,                    league.getStandings().get(teamA).getPoints());
    }

    @Test
    void testRecordResult_incrementsPlayedCountForBothTeams() {
        league.recordResult(makeResult(teamA, teamB, 1, 0));
        assertEquals(1, league.getStandings().get(teamA).getPlayed());
        assertEquals(1, league.getStandings().get(teamB).getPlayed());
    }

    @Test
    void testRecordResult_goalsAreTrackedCorrectly() {
        league.recordResult(makeResult(teamA, teamB, 3, 1));
        LeagueStanding sA = league.getStandings().get(teamA);
        LeagueStanding sB = league.getStandings().get(teamB);
        assertEquals(3, sA.getGoalsFor());
        assertEquals(1, sA.getGoalsAgainst());
        assertEquals(1, sB.getGoalsFor());
        assertEquals(3, sB.getGoalsAgainst());
    }

    @Test
    void testGetSortedStandings_highestPointsTeamIsFirst() {
        league.recordResult(makeResult(teamA, teamB, 3, 0));  // A wins
        league.recordResult(makeResult(teamB, teamC, 3, 0));  // B wins
        league.recordResult(makeResult(teamA, teamC, 3, 0));  // A wins
        List<LeagueStanding> sorted = league.getSortedStandings();
        assertEquals(teamA, sorted.get(0).getTeam());
    }

    @Test
    void testGetSortedStandings_returnsAllRegisteredTeams() {
        assertEquals(3, league.getSortedStandings().size());
    }
}
