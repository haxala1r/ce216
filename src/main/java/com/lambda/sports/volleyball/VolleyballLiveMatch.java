package com.lambda.sports.volleyball;

import com.lambda.sports.LiveMatch;
import com.lambda.sports.MatchEngine;
import com.lambda.sports.MatchResult;
import com.lambda.sports.Team;

/**
 * A Volleyball match in progress.  Plays sets one by one (best of 5).
 * Sets 1 – 4 are played to 25 with a 2-point margin; the deciding 5th set
 * is played to 15 with a 2-point margin.  The match ends as soon as one
 * side has won three sets, even if fewer than five sets have been played.
 *
 * <p>Tactic changes and substitutions made between sets take effect
 * immediately for the next set, mirroring the project specification's
 * "between-quarter" rule.</p>
 */
public class VolleyballLiveMatch extends LiveMatch {

    private static final int MAX_SETS = 5;

    private int homeSets = 0;
    private int awaySets = 0;

    public VolleyballLiveMatch(Team home, Team away) {
        super(home, away);
    }

    @Override
    public void playNextPeriod(MatchEngine engine) {
        if (finished) return;
        int target = (periodScores.size() == 4) ? 15 : 25;
        int[] result = engine.simulateVolleyballSet(home, away, target);
        periodScores.add(result);
        if (result[0] > result[1]) homeSets++;
        else                       awaySets++;
        if (homeSets >= 3 || awaySets >= 3 || periodScores.size() >= MAX_SETS) {
            finished = true;
        }
    }

    @Override
    public MatchResult finalizeMatch(MatchEngine engine) {
        engine.handleInjuries(home);
        engine.handleInjuries(away);
        return new MatchResult(home, away, homeSets, awaySets, getAllPeriodScores());
    }

    @Override
    public int getMaxPeriods() { return MAX_SETS; }

    @Override
    public int getHomeRunningScore() { return homeSets; }

    @Override
    public int getAwayRunningScore() { return awaySets; }
}
