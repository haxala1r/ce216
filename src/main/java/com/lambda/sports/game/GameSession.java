package com.lambda.sports.game;

import com.lambda.sports.*;

import java.util.ArrayList;
import java.util.List;

/**
 * High-level controller that owns the full state of a single playthrough:
 * the chosen sport, the teams, the league standings, the deterministic RNG
 * seed, the current season number, and a pointer into the fixture list.
 *
 * <p>This class is the unit that gets serialised to disk by
 * {@code com.lambda.sports.persistence.SaveManager} — keeping all per-session
 * state on one object makes save / load straightforward and means the GUI
 * can ask one place for everything it needs to display.</p>
 *
 * <p>The fixture schedule is a complete double round-robin generated at
 * construction time.  Calls to {@link #playNextMatch()} consume the schedule
 * in order; {@link #hasMoreMatches()} reports whether the season is finished.
 * After a season ends, {@link #advanceToNextSeason()} clears standings,
 * regenerates fixtures, ages all squads, and starts season N + 1.</p>
 */
public class GameSession {

    private final String        sessionName;
    private final String        sportId;          // "headball" or "volleyball"
    private final AbstractSport sport;
    private final List<Team>    teams;
    private final long          rngSeed;
    private final MatchEngine   engine;
    /** Fixture list as [homeIndex, awayIndex] pairs into {@link #teams}. */
    private final List<int[]>   fixtures;

    private League league;
    private int    nextFixtureIndex;
    private int    seasonNumber;

    public GameSession(String sessionName, String sportId,
                        AbstractSport sport, List<Team> teams, long rngSeed) {
        this.sessionName      = sessionName;
        this.sportId          = sportId;
        this.sport            = sport;
        this.teams            = new ArrayList<>(teams);
        this.rngSeed          = rngSeed;
        this.engine           = new MatchEngine(rngSeed);
        this.fixtures         = generateDoubleRoundRobin(teams.size());
        this.nextFixtureIndex = 0;
        this.seasonNumber     = 1;
        this.league           = buildLeagueFor(seasonNumber);
    }

    /**
     * Constructor used by the persistence layer when reconstructing a
     * mid-season game.  Restores the standings by replaying every result
     * already recorded and advances the fixture pointer past them.
     */
    public GameSession(String sessionName, String sportId,
                        AbstractSport sport, List<Team> teams, long rngSeed,
                        List<MatchResult> playedResults, int nextFixtureIndex,
                        int seasonNumber) {
        this(sessionName, sportId, sport, teams, rngSeed);
        this.seasonNumber = seasonNumber;
        this.league       = buildLeagueFor(seasonNumber);
        for (MatchResult r : playedResults) league.recordResult(r);
        this.nextFixtureIndex = nextFixtureIndex;
    }

    /** Each season uses a different but deterministic coin-toss seed. */
    private League buildLeagueFor(int season) {
        return new League(
            sessionName + " League — Season " + season,
            sport,
            teams,
            rngSeed + season - 1);
    }

    private static List<int[]> generateDoubleRoundRobin(int n) {
        List<int[]> list = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (i != j) list.add(new int[]{i, j});
            }
        }
        return list;
    }

    // ── Season flow ──────────────────────────────────────────────────────────

    /** True if at least one fixture is still unplayed in the current season. */
    public boolean hasMoreMatches() {
        return nextFixtureIndex < fixtures.size();
    }

    /** True if the season has ended (all fixtures played). */
    public boolean isSeasonComplete() {
        return !hasMoreMatches();
    }

    /**
     * The team currently top of the standings — i.e. the season winner once
     * the season is complete.  Returns {@code null} if no matches have been
     * played yet (no winner can be determined).
     */
    public Team getSeasonWinner() {
        if (league.getResults().isEmpty()) return null;
        return league.getSortedStandings().get(0).getTeam();
    }

    /**
     * Plays the next scheduled match using the chosen sport's
     * {@code simulateMatch} implementation, records the result in the
     * standings, and advances the fixture pointer.  Both teams undergo a
     * training session immediately before kickoff.
     *
     * @return the freshly simulated {@link MatchResult},
     *         or {@code null} if the season is already complete
     */
    public MatchResult playNextMatch() {
        if (!hasMoreMatches()) return null;
        int[] pair = fixtures.get(nextFixtureIndex++);
        Team home = teams.get(pair[0]);
        Team away = teams.get(pair[1]);

        // Spec: "During the week there will be training, during the weekend there will be the matches."
        home.runTraining();
        away.runTraining();

        MatchResult result = sport.simulateMatch(home, away, engine);
        league.recordResult(result);
        return result;
    }

    /**
     * Plays out every remaining fixture in one go.
     *
     * @return the list of results produced by this call (may be empty)
     */
    public List<MatchResult> playRemainingSeason() {
        List<MatchResult> produced = new ArrayList<>();
        while (hasMoreMatches()) produced.add(playNextMatch());
        return produced;
    }

    /**
     * Returns the team pair for the next fixture without playing it.
     *
     * @return a 2-element array {@code [home, away]}, or {@code null} if done
     */
    public Team[] peekNextFixture() {
        if (!hasMoreMatches()) return null;
        int[] pair = fixtures.get(nextFixtureIndex);
        return new Team[]{teams.get(pair[0]), teams.get(pair[1])};
    }

    // ── Interactive (per-period) match flow ─────────────────────────────────

    /**
     * Starts the next scheduled fixture as an interactive {@link LiveMatch}.
     * The fixture pointer does <b>not</b> advance until
     * {@link #recordInteractiveResult(LiveMatch)} is called with the
     * finished match.  Both teams undergo a training session immediately,
     * before the LiveMatch is returned.
     *
     * @return a fresh {@link LiveMatch}, or {@code null} if the season is over
     */
    public LiveMatch startInteractiveMatch() {
        if (!hasMoreMatches()) return null;
        int[] pair = fixtures.get(nextFixtureIndex);
        Team home = teams.get(pair[0]);
        Team away = teams.get(pair[1]);
        home.runTraining();
        away.runTraining();
        return sport.startLiveMatch(home, away);
    }

    /**
     * Records a finished {@link LiveMatch}: finalises it (which runs injury
     * rolls), records the result in the league, and advances the fixture
     * pointer.  Calling with an unfinished match throws.
     *
     * @return the {@link MatchResult} that was recorded
     * @throws IllegalStateException if the LiveMatch hasn't finished yet
     */
    public MatchResult recordInteractiveResult(LiveMatch match) {
        if (!match.isFinished()) {
            throw new IllegalStateException("Cannot record an unfinished match");
        }
        MatchResult result = match.finalizeMatch(engine);
        league.recordResult(result);
        nextFixtureIndex++;
        return result;
    }

    // ── Multi-year continuation ──────────────────────────────────────────────

    /**
     * Advances to the next season.  Specifically:
     * <ul>
     *   <li>increments {@link #seasonNumber}</li>
     *   <li>creates a fresh {@link League} (cleared standings)</li>
     *   <li>resets the fixture pointer to 0</li>
     *   <li>ages every player and coach by one year</li>
     *   <li>clears any lingering injuries (clean slate)</li>
     * </ul>
     *
     * <p>The team rosters and squad attributes (improved by training over the
     * prior season) carry over to the new season — that's the manager-game
     * progression loop.</p>
     *
     * @throws IllegalStateException if the current season hasn't ended
     */
    public void advanceToNextSeason() {
        if (!isSeasonComplete()) {
            throw new IllegalStateException("Cannot advance: current season is not complete");
        }
        seasonNumber++;
        league           = buildLeagueFor(seasonNumber);
        nextFixtureIndex = 0;

        for (Team t : teams) {
            for (AbstractPlayer p : t.getPlayers()) {
                p.ageByOneYear();
                if (p.isInjured()) {
                    while (p.isInjured()) p.recover();
                }
            }
            for (Coach c : t.getCoaches()) c.ageByOneYear();
        }
    }

    // ── Getters ──────────────────────────────────────────────────────────────

    public String        getSessionName()      { return sessionName; }
    public String        getSportId()          { return sportId; }
    public AbstractSport getSport()            { return sport; }
    public List<Team>    getTeams()            { return teams; }
    public League        getLeague()           { return league; }
    public long          getRngSeed()          { return rngSeed; }
    public MatchEngine   getEngine()           { return engine; }
    public int           getNextFixtureIndex() { return nextFixtureIndex; }
    public int           getTotalFixtures()    { return fixtures.size(); }
    public int           getMatchesPlayed()    { return nextFixtureIndex; }
    public int           getSeasonNumber()     { return seasonNumber; }
}
