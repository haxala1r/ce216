package com.lambda.sports;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract base for every sport supported by the framework.
 * Subclasses define sport-specific rules: player count, points system,
 * team creation, and match simulation.
 */
public abstract class AbstractSport {

    protected final int playersPerTeam;
    protected final int winPoints;
    protected final int drawPoints;
    protected final int matchDuration;   // total minutes per match
    protected final int numQuarters;     // number of quarters/periods per match

    protected AbstractSport(int playersPerTeam, int winPoints, int drawPoints,
                             int matchDuration, int numQuarters) {
        this.playersPerTeam = playersPerTeam;
        this.winPoints      = winPoints;
        this.drawPoints     = drawPoints;
        this.matchDuration  = matchDuration;
        this.numQuarters    = numQuarters;
    }

    /**
     * Creates {@code count} ready-to-play teams for this sport.
     * Each team is fully populated with players and assigned a default tactic.
     */
    public abstract List<Team> createTeams(int count);

    /**
     * Simulates a single match between {@code home} and {@code away}
     * using the provided engine.
     */
    public abstract MatchResult simulateMatch(Team home, Team away, MatchEngine engine);

    /**
     * Generates a complete double round-robin schedule (every team plays every
     * other team once at home and once away) and returns the full list of results.
     */
    public List<MatchResult> generateSchedule(List<Team> teams, MatchEngine engine) {
        List<MatchResult> results = new ArrayList<>();
        for (int i = 0; i < teams.size(); i++) {
            for (int j = 0; j < teams.size(); j++) {
                if (i != j) {
                    results.add(simulateMatch(teams.get(i), teams.get(j), engine));
                }
            }
        }
        return results;
    }

    // ── Getters ──────────────────────────────────────────────────────────────

    public int getPlayersPerTeam() { return playersPerTeam; }
    public int getWinPoints()      { return winPoints; }
    public int getDrawPoints()     { return drawPoints; }
    public int getMatchDuration()  { return matchDuration; }
    public int getNumQuarters()    { return numQuarters; }
}
