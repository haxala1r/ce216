package com.lambda.sports.headball;

import com.lambda.sports.LiveMatch;
import com.lambda.sports.MatchEngine;
import com.lambda.sports.MatchResult;
import com.lambda.sports.Team;

/**
 * A Headball match in progress.  Plays exactly {@code totalQuarters} quarters
 * (typically 4).  Tactic changes and substitutions made between calls to
 * {@link #playNextPeriod} take effect immediately for the next quarter.
 */
public class HeadballLiveMatch extends LiveMatch {

    private final int totalQuarters;

    public HeadballLiveMatch(Team home, Team away, int totalQuarters) {
        super(home, away);
        this.totalQuarters = totalQuarters;
    }

    @Override
    public void playNextPeriod(MatchEngine engine) {
        if (finished) return;
        int[] qScore = engine.simulateQuarter(home, away);
        periodScores.add(qScore);
        if (periodScores.size() >= totalQuarters) finished = true;
    }

    @Override
    public MatchResult finalizeMatch(MatchEngine engine) {
        // Pad any unplayed quarters with zeros (defensive, shouldn't happen)
        while (periodScores.size() < totalQuarters) periodScores.add(new int[]{0, 0});
        engine.handleInjuries(home);
        engine.handleInjuries(away);
        return new MatchResult(home, away,
                                getHomeRunningScore(),
                                getAwayRunningScore(),
                                getAllPeriodScores());
    }

    @Override
    public int getMaxPeriods() { return totalQuarters; }

    @Override
    public int getHomeRunningScore() {
        int sum = 0;
        for (int[] q : periodScores) sum += q[0];
        return sum;
    }

    @Override
    public int getAwayRunningScore() {
        int sum = 0;
        for (int[] q : periodScores) sum += q[1];
        return sum;
    }
}
