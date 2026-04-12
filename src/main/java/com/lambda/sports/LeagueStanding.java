package com.lambda.sports;

/**
 * Tracks the season record for a single team: wins, draws, losses, goals, points.
 */
public class LeagueStanding {

    private final Team team;
    private int played;
    private int wins;
    private int draws;
    private int losses;
    private int points;
    private int goalsFor;
    private int goalsAgainst;

    public LeagueStanding(Team team) {
        this.team = team;
    }

    /**
     * Updates the standing after a completed match.
     *
     * @param result     the match result to record
     * @param isHome     {@code true} if this team was the home side
     * @param winPoints  points awarded for a win (sport-specific)
     * @param drawPoints points awarded for a draw (sport-specific)
     */
    public void update(MatchResult result, boolean isHome,
                       int winPoints, int drawPoints) {
        played++;
        int myScore  = isHome ? result.getHomeScore() : result.getAwayScore();
        int oppScore = isHome ? result.getAwayScore()  : result.getHomeScore();

        goalsFor     += myScore;
        goalsAgainst += oppScore;

        if (myScore > oppScore) {
            wins++;
            points += winPoints;
        } else if (myScore == oppScore) {
            draws++;
            points += drawPoints;
        } else {
            losses++;
        }
    }

    // ── Getters ──────────────────────────────────────────────────────────────

    public Team getTeam()           { return team; }
    public int  getPlayed()         { return played; }
    public int  getWins()           { return wins; }
    public int  getDraws()          { return draws; }
    public int  getLosses()         { return losses; }
    public int  getPoints()         { return points; }
    public int  getGoalsFor()       { return goalsFor; }
    public int  getGoalsAgainst()   { return goalsAgainst; }
    public int  getGoalDifference() { return goalsFor - goalsAgainst; }
}
