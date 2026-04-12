package com.lambda.sports;

import java.util.*;

/**
 * Manages the full season: recording results and producing a sorted standings
 * table with the correct tie-breaking rules.
 *
 * <p>Tie-breaking order (Headball specification):</p>
 * <ol>
 *   <li>Total points (descending)</li>
 *   <li>Head-to-head points between the tied teams</li>
 *   <li>Goal difference (descending)</li>
 *   <li>Goals scored (descending)</li>
 *   <li>Alphabetical team name (deterministic "coin-toss" proxy)</li>
 * </ol>
 */
public class League {

    private final String name;
    private final AbstractSport sport;
    private final List<Team> teams;
    private final List<MatchResult> results;
    private final Map<Team, LeagueStanding> standings;

    public League(String name, AbstractSport sport, List<Team> teams) {
        this.name      = name;
        this.sport     = sport;
        this.teams     = new ArrayList<>(teams);
        this.results   = new ArrayList<>();
        this.standings = new LinkedHashMap<>();
        for (Team t : teams) standings.put(t, new LeagueStanding(t));
    }

    /**
     * Records a completed match result and updates both teams' standings.
     */
    public void recordResult(MatchResult result) {
        results.add(result);
        LeagueStanding homeS = standings.get(result.getHome());
        LeagueStanding awayS = standings.get(result.getAway());
        if (homeS != null) homeS.update(result, true,  sport.getWinPoints(), sport.getDrawPoints());
        if (awayS != null) awayS.update(result, false, sport.getWinPoints(), sport.getDrawPoints());
    }

    /**
     * Returns the standings sorted according to the tie-breaking rules above.
     */
    public List<LeagueStanding> getSortedStandings() {
        List<LeagueStanding> list = new ArrayList<>(standings.values());
        list.sort((a, b) -> {
            // 1. Points
            if (b.getPoints() != a.getPoints())
                return Integer.compare(b.getPoints(), a.getPoints());
            // 2. Head-to-head (positive = b has more H2H pts than a)
            int h2h = compareHeadToHead(b.getTeam(), a.getTeam());
            if (h2h != 0) return h2h;
            // 3. Goal difference
            if (b.getGoalDifference() != a.getGoalDifference())
                return Integer.compare(b.getGoalDifference(), a.getGoalDifference());
            // 4. Goals scored
            if (b.getGoalsFor() != a.getGoalsFor())
                return Integer.compare(b.getGoalsFor(), a.getGoalsFor());
            // 5. Alphabetical (deterministic)
            return a.getTeam().getName().compareTo(b.getTeam().getName());
        });
        return list;
    }

    /**
     * Compares head-to-head points between two teams across all recorded results.
     *
     * @return positive if {@code t1} has more H2H points than {@code t2},
     *         negative if {@code t2} leads, 0 if equal
     */
    private int compareHeadToHead(Team t1, Team t2) {
        int p1 = 0, p2 = 0;
        for (MatchResult r : results) {
            boolean t1Home = r.getHome() == t1 && r.getAway() == t2;
            boolean t2Home = r.getHome() == t2 && r.getAway() == t1;
            if (!t1Home && !t2Home) continue;

            if (t1Home) {
                if      (r.isHomeWin()) p1 += sport.getWinPoints();
                else if (r.isDraw())  { p1 += sport.getDrawPoints(); p2 += sport.getDrawPoints(); }
                else                   p2 += sport.getWinPoints();
            } else {
                if      (r.isAwayWin()) p1 += sport.getWinPoints();
                else if (r.isDraw())  { p1 += sport.getDrawPoints(); p2 += sport.getDrawPoints(); }
                else                   p2 += sport.getWinPoints();
            }
        }
        return Integer.compare(p1, p2);
    }

    // ── Getters ──────────────────────────────────────────────────────────────

    public String                      getName()      { return name; }
    public List<Team>                  getTeams()     { return teams; }
    public List<MatchResult>           getResults()   { return results; }
    public Map<Team, LeagueStanding>   getStandings() { return standings; }
}
