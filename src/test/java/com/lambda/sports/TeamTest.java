package com.lambda.sports;

import com.lambda.sports.headball.HeadballPlayer;
import com.lambda.sports.headball.HeadballTactic;
import com.lambda.sports.headball.HeadballTacticType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TeamTest {

    private Team team;
    private HeadballPlayer p1, p2, p3;

    @BeforeEach
    void setUp() {
        team = new Team("Test FC");
        p1 = new HeadballPlayer("Alice", 22, 8.0, 7.0);
        p2 = new HeadballPlayer("Bob",   25, 6.0, 5.0);
        p3 = new HeadballPlayer("Carol", 21, 9.0, 8.0);
        team.addPlayer(p1);
        team.addPlayer(p2);
        team.addPlayer(p3);
    }

    @Test
    void testGetName_returnsCorrectName() {
        assertEquals("Test FC", team.getName());
    }

    @Test
    void testAddPlayer_increasesPlayerCount() {
        int before = team.getPlayers().size();
        team.addPlayer(new HeadballPlayer("Dave", 23, 7.0, 6.0));
        assertEquals(before + 1, team.getPlayers().size());
    }

    @Test
    void testSetTactic_updatesTactic() {
        HeadballTactic tactic = new HeadballTactic(HeadballTacticType.AGGRESSIVE);
        team.setTactic(tactic);
        assertEquals(tactic, team.getTactic());
    }

    @Test
    void testMakeSubstitution_swapsPlayerPositions() {
        team.makeSubstitution(0, 2);
        assertEquals(p3, team.getPlayers().get(0));
        assertEquals(p1, team.getPlayers().get(2));
    }

    @Test
    void testMakeSubstitution_validIndices_returnsTrue() {
        assertTrue(team.makeSubstitution(0, 1));
    }

    @Test
    void testMakeSubstitution_negativeIndex_returnsFalse() {
        assertFalse(team.makeSubstitution(-1, 0));
    }

    @Test
    void testMakeSubstitution_outOfBoundsIndex_returnsFalse() {
        assertFalse(team.makeSubstitution(0, 99));
    }

    @Test
    void testGetAvailablePlayers_excludesInjuredPlayer() {
        p2.applyInjury(2);
        List<AbstractPlayer> available = team.getAvailablePlayers();
        assertFalse(available.contains(p2));
        assertTrue(available.contains(p1));
        assertTrue(available.contains(p3));
    }

    @Test
    void testGetAverageSkill_calculatesWeightedAverage() {
        // p1: 8*0.6 + 7*0.4 = 7.60
        // p2: 6*0.6 + 5*0.4 = 5.60
        // p3: 9*0.6 + 8*0.4 = 8.60   avg = 21.8 / 3
        double expected = (7.6 + 5.6 + 8.6) / 3.0;
        assertEquals(expected, team.getAverageSkill(), 0.001);
    }
}
