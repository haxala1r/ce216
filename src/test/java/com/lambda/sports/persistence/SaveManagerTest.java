package com.lambda.sports.persistence;

import com.lambda.sports.AbstractPlayer;
import com.lambda.sports.LeagueStanding;
import com.lambda.sports.MatchResult;
import com.lambda.sports.Team;
import com.lambda.sports.game.GameSession;
import com.lambda.sports.headball.HeadballPlayer;
import com.lambda.sports.headball.HeadballSport;
import com.lambda.sports.headball.HeadballTactic;
import com.lambda.sports.headball.HeadballTacticType;
import com.lambda.sports.volleyball.VolleyballSport;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SaveManagerTest {

    @Test
    void testSaveAndLoad_headballSession_preservesStandings(@TempDir Path tmp) throws Exception {
        HeadballSport sport = new HeadballSport();
        GameSession original = new GameSession("Headball Test", "headball",
                sport, sport.createTeams(4), 42L);
        original.playNextMatch();
        original.playNextMatch();
        original.playNextMatch();

        Path file = tmp.resolve("session.json");
        SaveManager mgr = new SaveManager();
        mgr.save(original, file);
        GameSession restored = mgr.load(file);

        assertEquals(original.getSessionName(),      restored.getSessionName());
        assertEquals(original.getSportId(),          restored.getSportId());
        assertEquals(original.getRngSeed(),          restored.getRngSeed());
        assertEquals(original.getMatchesPlayed(),    restored.getMatchesPlayed());
        assertEquals(original.getTotalFixtures(),    restored.getTotalFixtures());

        List<LeagueStanding> origStandings = original.getLeague().getSortedStandings();
        List<LeagueStanding> restStandings = restored.getLeague().getSortedStandings();
        assertEquals(origStandings.size(), restStandings.size());
        for (int i = 0; i < origStandings.size(); i++) {
            assertEquals(origStandings.get(i).getPoints(),   restStandings.get(i).getPoints());
            assertEquals(origStandings.get(i).getGoalsFor(), restStandings.get(i).getGoalsFor());
        }
    }

    @Test
    void testSaveAndLoad_volleyballSession_preservesStateAndContinues(@TempDir Path tmp) throws Exception {
        VolleyballSport sport = new VolleyballSport();
        GameSession original = new GameSession("Volley Test", "volleyball",
                sport, sport.createTeams(4), 99L);
        for (int i = 0; i < 4; i++) original.playNextMatch();

        Path file = tmp.resolve("volley.json");
        SaveManager mgr = new SaveManager();
        mgr.save(original, file);
        GameSession restored = mgr.load(file);

        // Continue the season on the restored copy — must not throw
        assertTrue(restored.hasMoreMatches());
        restored.playRemainingSeason();
        assertEquals(original.getTotalFixtures(), restored.getMatchesPlayed());
    }

    @Test
    void testSaveAndLoad_preservesPlayerInjuryState(@TempDir Path tmp) throws Exception {
        HeadballSport sport = new HeadballSport();
        List<Team> teams = sport.createTeams(2);
        Team home = teams.get(0);
        // Manually injure a player so we know it's set
        AbstractPlayer victim = home.getPlayers().get(0);
        victim.applyInjury(3);

        GameSession s = new GameSession("Injury Test", "headball", sport, teams, 1L);
        Path file = tmp.resolve("injuries.json");
        new SaveManager().save(s, file);
        GameSession restored = new SaveManager().load(file);

        AbstractPlayer restoredVictim = restored.getTeams().get(0).getPlayers().get(0);
        assertEquals(victim.getName(),              restoredVictim.getName());
        assertEquals(victim.isInjured(),            restoredVictim.isInjured());
        assertEquals(victim.getInjuryGamesLeft(),   restoredVictim.getInjuryGamesLeft());
    }

    @Test
    void testSaveAndLoad_preservesAssignedTactic(@TempDir Path tmp) throws Exception {
        HeadballSport sport = new HeadballSport();
        List<Team> teams = sport.createTeams(2);
        teams.get(0).setTactic(new HeadballTactic(HeadballTacticType.AGGRESSIVE));
        teams.get(1).setTactic(new HeadballTactic(HeadballTacticType.DEFENSIVE));

        GameSession s = new GameSession("Tactic Test", "headball", sport, teams, 7L);
        Path file = tmp.resolve("tactics.json");
        new SaveManager().save(s, file);
        GameSession r = new SaveManager().load(file);

        assertEquals("AGGRESSIVE", r.getTeams().get(0).getTactic().getTacticName());
        assertEquals("DEFENSIVE",  r.getTeams().get(1).getTactic().getTacticName());
    }

    @Test
    void testListSaves_returnsEmptyListForMissingDirectory(@TempDir Path tmp) {
        SaveManager mgr = new SaveManager();
        Path missing = tmp.resolve("does-not-exist");
        assertTrue(mgr.listSaves(missing).isEmpty());
    }

    @Test
    void testListSaves_findsExistingJsonFiles(@TempDir Path tmp) throws Exception {
        HeadballSport sport = new HeadballSport();
        GameSession s = new GameSession("File Test", "headball", sport, sport.createTeams(2), 3L);
        SaveManager mgr = new SaveManager();
        mgr.save(s, tmp.resolve("first.json"));
        mgr.save(s, tmp.resolve("second.json"));
        // Drop a non-JSON sibling — should be ignored
        java.nio.file.Files.writeString(tmp.resolve("readme.txt"), "ignore me");

        assertEquals(2, mgr.listSaves(tmp).size());
    }

    @Test
    void testSaveAndLoad_preservesCoaches(@TempDir Path tmp) throws Exception {
        HeadballSport sport = new HeadballSport();
        List<Team> teams = sport.createTeams(2);
        // Confirm the sport actually generated coaches
        assertFalse(teams.get(0).getCoaches().isEmpty());
        int coachCountBefore = teams.get(0).getCoaches().size();
        String firstCoachName = teams.get(0).getCoaches().get(0).getName();
        double firstCoachSkill = teams.get(0).getCoaches().get(0).getTrainingSkill();

        GameSession s = new GameSession("Coach Test", "headball", sport, teams, 5L);
        Path file = tmp.resolve("coaches.json");
        new SaveManager().save(s, file);
        GameSession restored = new SaveManager().load(file);

        Team restoredHome = restored.getTeams().get(0);
        assertEquals(coachCountBefore, restoredHome.getCoaches().size());
        assertEquals(firstCoachName,   restoredHome.getCoaches().get(0).getName());
        assertEquals(firstCoachSkill,  restoredHome.getCoaches().get(0).getTrainingSkill(), 0.001);
    }

    @Test
    void testSaveAndLoad_preservesSeasonNumber(@TempDir Path tmp) throws Exception {
        HeadballSport sport = new HeadballSport();
        GameSession s = new GameSession("Season Test", "headball", sport, sport.createTeams(2), 1L);
        s.playRemainingSeason();
        s.advanceToNextSeason();
        s.playRemainingSeason();
        s.advanceToNextSeason();
        assertEquals(3, s.getSeasonNumber());

        Path file = tmp.resolve("season3.json");
        new SaveManager().save(s, file);
        GameSession restored = new SaveManager().load(file);

        assertEquals(3, restored.getSeasonNumber());
    }

    @Test
    void testSaveAndLoad_preservesTrainedSkill(@TempDir Path tmp) throws Exception {
        // Skill should improve mid-season (training); confirm the boost persists
        HeadballSport sport = new HeadballSport();
        GameSession s = new GameSession("Train Test", "headball", sport, sport.createTeams(4), 2L);
        for (int i = 0; i < 6; i++) s.playNextMatch();   // 6 rounds of training
        Team home  = s.getTeams().get(0);
        double avgSkillBefore = 0;
        for (AbstractPlayer p : home.getPlayers()) avgSkillBefore += p.getSkillRating();
        avgSkillBefore /= home.getPlayers().size();

        Path file = tmp.resolve("trained.json");
        new SaveManager().save(s, file);
        GameSession restored = new SaveManager().load(file);

        Team restoredHome = restored.getTeams().get(0);
        double avgSkillAfter = 0;
        for (AbstractPlayer p : restoredHome.getPlayers()) avgSkillAfter += p.getSkillRating();
        avgSkillAfter /= restoredHome.getPlayers().size();

        assertEquals(avgSkillBefore, avgSkillAfter, 0.001,
            "Trained skill must persist across save/load");
    }
}
