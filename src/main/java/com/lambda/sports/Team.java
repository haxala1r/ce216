package com.lambda.sports;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a team in any sport.
 * Holds the squad, coaching staff, current tactic, and substitution logic.
 */
public class Team {

    private final String               name;
    private final List<AbstractPlayer> players;
    private final List<Coach>          coaches;
    private ITactic                    tactic;

    public Team(String name) {
        this.name    = name;
        this.players = new ArrayList<>();
        this.coaches = new ArrayList<>();
        this.tactic  = new DefaultTactic();
    }

    /** Adds a player to the squad. */
    public void addPlayer(AbstractPlayer player) {
        players.add(player);
    }

    /** Adds a coach to the staff. */
    public void addCoach(Coach coach) {
        coaches.add(coach);
    }

    /**
     * Assigns a new tactic and immediately applies it to the team.
     *
     * @param tactic the tactic to assign
     */
    public void setTactic(ITactic tactic) {
        this.tactic = tactic;
        tactic.applyTactic(this);
    }

    /**
     * Swaps the players at positions {@code outIndex} and {@code inIndex}
     * in the squad list. The first {@code playersPerTeam} entries are on-field;
     * the rest are substitutes.
     *
     * @return {@code true} if the substitution was valid and applied,
     *         {@code false} if either index is out of bounds or identical
     */
    public boolean makeSubstitution(int outIndex, int inIndex) {
        if (outIndex < 0 || inIndex < 0
                || outIndex >= players.size() || inIndex >= players.size()) {
            return false;
        }
        if (outIndex == inIndex) return false;

        AbstractPlayer tmp = players.get(outIndex);
        players.set(outIndex, players.get(inIndex));
        players.set(inIndex, tmp);
        return true;
    }

    /** Returns all players who are not currently injured. */
    public List<AbstractPlayer> getAvailablePlayers() {
        List<AbstractPlayer> available = new ArrayList<>();
        for (AbstractPlayer p : players) {
            if (!p.isInjured()) available.add(p);
        }
        return available;
    }

    /**
     * Calculates the average skill rating of the available (non-injured) players.
     * Falls back to the whole squad if everyone is injured.
     */
    public double getAverageSkill() {
        List<AbstractPlayer> pool = getAvailablePlayers();
        if (pool.isEmpty()) pool = players;
        if (pool.isEmpty()) return 0.0;
        double sum = 0.0;
        for (AbstractPlayer p : pool) sum += p.getSkillRating();
        return sum / pool.size();
    }

    /**
     * Runs a single training week: every coach trains the entire squad once.
     * Multiple coaches stack their boosts.  Injured players are skipped
     * inside {@link Coach#train(List)} since they cannot train while recovering.
     */
    public void runTraining() {
        for (Coach c : coaches) c.train(players);
    }

    // ── Getters ──────────────────────────────────────────────────────────────

    public String               getName()    { return name; }
    public List<AbstractPlayer> getPlayers() { return players; }
    public List<Coach>          getCoaches() { return coaches; }
    public ITactic              getTactic()  { return tactic; }
}
