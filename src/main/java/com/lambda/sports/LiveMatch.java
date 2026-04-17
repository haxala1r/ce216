package com.lambda.sports;

import java.util.ArrayList;
import java.util.List;

/**
 * Models a match that is currently in progress.  Used by the interactive
 * GUI to step through a match period-by-period, allowing the user to change
 * tactics or substitute players between periods (as required by the project
 * spec: <i>"once a quarter ends, the player can make changes to tactics or
 * substitute players"</i>).
 *
 * <p>Concrete subclasses define what a "period" means for their sport:</p>
 * <ul>
 *   <li>{@code HeadballLiveMatch} — a fixed number of quarters (4)</li>
 *   <li>{@code VolleyballLiveMatch} — a variable number of sets (3 – 5)</li>
 * </ul>
 *
 * <p>Tactic changes and substitutions made through {@link Team#setTactic} or
 * {@link Team#makeSubstitution} on the home/away team between calls to
 * {@link #playNextPeriod} take effect immediately for the next period.</p>
 */
public abstract class LiveMatch {

    protected final Team           home;
    protected final Team           away;
    protected final List<int[]>    periodScores = new ArrayList<>();
    protected boolean              finished;

    protected LiveMatch(Team home, Team away) {
        this.home     = home;
        this.away     = away;
        this.finished = false;
    }

    /**
     * Plays the next period of the match using {@code engine}.  The result is
     * appended to {@link #periodScores} and the match may be marked finished.
     * Calling after {@link #isFinished()} returns true is a no-op.
     */
    public abstract void playNextPeriod(MatchEngine engine);

    /**
     * Finalises this match into an immutable {@link MatchResult} and runs
     * post-match injury rolls on both teams.  Should be called once after
     * {@link #isFinished()} returns true.
     *
     * @param engine the engine used for injury rolls (kept seeded)
     */
    public abstract MatchResult finalizeMatch(MatchEngine engine);

    /** Total number of periods this match might play (max for variable-length sports). */
    public abstract int getMaxPeriods();

    /** Returns true if no more periods will be played. */
    public boolean isFinished() { return finished; }

    /** 1-based index of the period that will be played next (or {@code maxPeriods+1} when done). */
    public int getNextPeriodNumber() { return periodScores.size() + 1; }

    /** Number of periods completed so far. */
    public int getPeriodsPlayed() { return periodScores.size(); }

    /** Returns the score of period {@code i} (0-indexed), {@code [home, away]}. */
    public int[] getPeriodScore(int i) { return periodScores.get(i); }

    /** Returns a defensive copy of all period scores played so far. */
    public int[][] getAllPeriodScores() {
        int[][] copy = new int[periodScores.size()][];
        for (int i = 0; i < periodScores.size(); i++) {
            copy[i] = periodScores.get(i).clone();
        }
        return copy;
    }

    /** Cumulative home score so far (goals or sets-won, depending on sport). */
    public abstract int getHomeRunningScore();

    /** Cumulative away score so far (goals or sets-won, depending on sport). */
    public abstract int getAwayRunningScore();

    public Team getHome() { return home; }
    public Team getAway() { return away; }
}
