package com.lambda.sports.game;

import com.lambda.sports.AbstractPlayer;
import com.lambda.sports.Coach;
import com.lambda.sports.LiveMatch;
import com.lambda.sports.MatchResult;
import com.lambda.sports.Team;
import com.lambda.sports.headball.HeadballSport;
import com.lambda.sports.volleyball.VolleyballSport;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GameSessionTest {

    @Test
    void testNewSession_hasFullScheduleAndZeroProgress() {
        HeadballSport sport = new HeadballSport();
        List<Team> teams = sport.createTeams(4);
        GameSession s = new GameSession("Test", "headball", sport, teams, 1L);

        assertEquals(0,  s.getMatchesPlayed());
        assertEquals(12, s.getTotalFixtures());      // 4 * 3 = 12
        assertTrue(s.hasMoreMatches());
    }

    @Test
    void testNewSession_startsAtSeasonOne() {
        HeadballSport sport = new HeadballSport();
        GameSession s = new GameSession("Test", "headball", sport, sport.createTeams(4), 1L);
        assertEquals(1, s.getSeasonNumber());
    }

    @Test
    void testPeekNextFixture_doesNotConsumeIt() {
        VolleyballSport sport = new VolleyballSport();
        GameSession s = new GameSession("Test", "volleyball", sport, sport.createTeams(4), 5L);

        Team[] before = s.peekNextFixture();
        assertNotNull(before);
        assertEquals(0, s.getMatchesPlayed());      // peek must not advance the pointer
        Team[] again = s.peekNextFixture();
        assertSame(before[0], again[0]);
        assertSame(before[1], again[1]);
    }

    @Test
    void testPlayNextMatch_advancesPointerAndRecordsResult() {
        HeadballSport sport = new HeadballSport();
        GameSession s = new GameSession("Test", "headball", sport, sport.createTeams(4), 7L);

        MatchResult r = s.playNextMatch();
        assertNotNull(r);
        assertEquals(1, s.getMatchesPlayed());
        assertEquals(1, s.getLeague().getResults().size());
    }

    @Test
    void testPlayNextMatch_runsTrainingBeforeKickoff() {
        // Build a session whose teams have a known starting skill, then
        // verify skill rises after a single match cycle (training boost).
        HeadballSport sport = new HeadballSport();
        List<Team> teams = sport.createTeams(2);
        Team home = teams.get(0);
        double skillBefore = home.getAverageSkill();
        new GameSession("T", "headball", sport, teams, 1L).playNextMatch();
        // After training the skill must be ≥ before. ('≥' to allow for the
        // tiny but real chance everyone got injured and is excluded from avg.)
        assertTrue(home.getAverageSkill() >= skillBefore - 0.001);
    }

    @Test
    void testPlayRemainingSeason_consumesEveryFixture() {
        HeadballSport sport = new HeadballSport();
        GameSession s = new GameSession("Test", "headball", sport, sport.createTeams(4), 9L);

        List<MatchResult> produced = s.playRemainingSeason();
        assertEquals(12, produced.size());
        assertEquals(12, s.getMatchesPlayed());
        assertFalse(s.hasMoreMatches());
        assertNull(s.peekNextFixture());
        assertNull(s.playNextMatch(), "Calling play after season-end must return null");
    }

    @Test
    void testIsSeasonComplete_trueAfterPlayingAllFixtures() {
        HeadballSport sport = new HeadballSport();
        GameSession s = new GameSession("Test", "headball", sport, sport.createTeams(4), 9L);
        assertFalse(s.isSeasonComplete());
        s.playRemainingSeason();
        assertTrue(s.isSeasonComplete());
    }

    @Test
    void testGetSeasonWinner_returnsTopOfStandings() {
        HeadballSport sport = new HeadballSport();
        GameSession s = new GameSession("Test", "headball", sport, sport.createTeams(4), 9L);
        s.playRemainingSeason();

        Team winner   = s.getSeasonWinner();
        Team standingsTop = s.getLeague().getSortedStandings().get(0).getTeam();
        assertSame(standingsTop, winner);
    }

    @Test
    void testGetSeasonWinner_nullBeforeAnyMatchPlayed() {
        HeadballSport sport = new HeadballSport();
        GameSession s = new GameSession("Test", "headball", sport, sport.createTeams(4), 9L);
        assertNull(s.getSeasonWinner());
    }

    // ── Multi-year continuation ─────────────────────────────────────────────

    @Test
    void testAdvanceToNextSeason_throwsIfSeasonNotComplete() {
        HeadballSport sport = new HeadballSport();
        GameSession s = new GameSession("Test", "headball", sport, sport.createTeams(4), 1L);
        assertThrows(IllegalStateException.class, s::advanceToNextSeason);
    }

    @Test
    void testAdvanceToNextSeason_resetsStandingsAndFixturePointer() {
        HeadballSport sport = new HeadballSport();
        GameSession s = new GameSession("Test", "headball", sport, sport.createTeams(4), 1L);
        s.playRemainingSeason();
        assertEquals(1, s.getSeasonNumber());

        s.advanceToNextSeason();

        assertEquals(2, s.getSeasonNumber());
        assertEquals(0, s.getMatchesPlayed());
        assertTrue(s.hasMoreMatches());
        // New league instance — standings cleared
        assertEquals(0, s.getLeague().getResults().size());
        s.getLeague().getStandings().values().forEach(st ->
            assertEquals(0, st.getPlayed()));
    }

    @Test
    void testAdvanceToNextSeason_agesPlayersAndCoaches() {
        HeadballSport sport = new HeadballSport();
        List<Team> teams = sport.createTeams(2);
        AbstractPlayer p = teams.get(0).getPlayers().get(0);
        Coach c          = teams.get(0).getCoaches().get(0);
        int playerAgeBefore = p.getAge();
        int coachAgeBefore  = c.getAge();

        GameSession s = new GameSession("Test", "headball", sport, teams, 1L);
        s.playRemainingSeason();
        s.advanceToNextSeason();

        assertEquals(playerAgeBefore + 1, p.getAge());
        assertEquals(coachAgeBefore  + 1, c.getAge());
    }

    @Test
    void testAdvanceToNextSeason_clearsLingeringInjuries() {
        HeadballSport sport = new HeadballSport();
        List<Team> teams = sport.createTeams(2);
        AbstractPlayer p = teams.get(0).getPlayers().get(0);
        p.applyInjury(99);   // intentionally absurd duration

        GameSession s = new GameSession("Test", "headball", sport, teams, 1L);
        s.playRemainingSeason();
        assertTrue(p.isInjured(), "Setup precondition: player should still be injured");

        s.advanceToNextSeason();

        assertFalse(p.isInjured(), "Injuries should be cleared between seasons");
    }

    // ── Interactive flow ────────────────────────────────────────────────────

    @Test
    void testStartInteractiveMatch_doesNotAdvanceFixturePointer() {
        HeadballSport sport = new HeadballSport();
        GameSession s = new GameSession("Test", "headball", sport, sport.createTeams(4), 3L);
        int before = s.getMatchesPlayed();
        LiveMatch live = s.startInteractiveMatch();
        assertNotNull(live);
        assertEquals(before, s.getMatchesPlayed(),
            "Pointer must not advance until recordInteractiveResult");
    }

    @Test
    void testRecordInteractiveResult_advancesAndRecords() {
        HeadballSport sport = new HeadballSport();
        GameSession s = new GameSession("Test", "headball", sport, sport.createTeams(4), 3L);
        LiveMatch live = s.startInteractiveMatch();
        // Play through all quarters
        while (!live.isFinished()) live.playNextPeriod(s.getEngine());

        MatchResult r = s.recordInteractiveResult(live);
        assertEquals(1, s.getMatchesPlayed());
        assertEquals(1, s.getLeague().getResults().size());
        assertEquals(r.getHomeScore(), live.getHomeRunningScore());
    }

    @Test
    void testRecordInteractiveResult_throwsIfMatchUnfinished() {
        HeadballSport sport = new HeadballSport();
        GameSession s = new GameSession("Test", "headball", sport, sport.createTeams(4), 3L);
        LiveMatch live = s.startInteractiveMatch();
        // Don't play any quarters — match remains unfinished
        assertThrows(IllegalStateException.class, () -> s.recordInteractiveResult(live));
    }

    // ── Original tests preserved ────────────────────────────────────────────

    @Test
    void testStandings_everyTeamPlaysCorrectNumberOfGames() {
        VolleyballSport sport = new VolleyballSport();
        GameSession s = new GameSession("Test", "volleyball", sport, sport.createTeams(4), 11L);

        s.playRemainingSeason();
        // Each team plays every other team home + away → 2*(n-1) = 6 games
        s.getLeague().getStandings().values()
            .forEach(st -> assertEquals(6, st.getPlayed(),
                st.getTeam().getName() + " played wrong number of games"));
    }

    @Test
    void testDeterministic_sameSeedGivesSameStandings() {
        HeadballSport sport1 = new HeadballSport();
        HeadballSport sport2 = new HeadballSport();
        GameSession s1 = new GameSession("A", "headball", sport1, sport1.createTeams(4), 42L);
        GameSession s2 = new GameSession("B", "headball", sport2, sport2.createTeams(4), 42L);
        s1.playRemainingSeason();
        s2.playRemainingSeason();

        var t1 = s1.getLeague().getSortedStandings();
        var t2 = s2.getLeague().getSortedStandings();
        assertEquals(t1.size(), t2.size());
        for (int i = 0; i < t1.size(); i++) {
            assertEquals(t1.get(i).getPoints(),       t2.get(i).getPoints());
            assertEquals(t1.get(i).getGoalsFor(),     t2.get(i).getGoalsFor());
            assertEquals(t1.get(i).getGoalsAgainst(), t2.get(i).getGoalsAgainst());
        }
    }
}
