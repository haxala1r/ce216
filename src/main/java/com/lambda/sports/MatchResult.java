package com.lambda.sports;

/**
 * Immutable record of a completed match.
 * Stores both the final aggregate score and the per-quarter breakdown.
 */
public class MatchResult {

    private final Team home;
    private final Team away;
    private final int  homeScore;
    private final int  awayScore;
    /** [quarter][0 = home goals, 1 = away goals] */
    private final int[][] quarterScores;

    public MatchResult(Team home, Team away, int homeScore, int awayScore,
                       int[][] quarterScores) {
        this.home          = home;
        this.away          = away;
        this.homeScore     = homeScore;
        this.awayScore     = awayScore;
        this.quarterScores = quarterScores;
    }

    public boolean isHomeWin() { return homeScore > awayScore; }
    public boolean isAwayWin() { return awayScore > homeScore; }
    public boolean isDraw()    { return homeScore == awayScore; }

    // ── Getters ──────────────────────────────────────────────────────────────

    public Team    getHome()          { return home; }
    public Team    getAway()          { return away; }
    public int     getHomeScore()     { return homeScore; }
    public int     getAwayScore()     { return awayScore; }
    public int[][] getQuarterScores() { return quarterScores; }

    @Override
    public String toString() {
        return home.getName() + " " + homeScore + " - " + awayScore + " " + away.getName();
    }
}
