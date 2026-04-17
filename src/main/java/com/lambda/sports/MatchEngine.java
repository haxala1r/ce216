package com.lambda.sports;

import java.util.Arrays;
import java.util.Random;

/**
 * Simulates matches using skill ratings and tactic multipliers.
 *
 * <p>The engine exposes both <b>full-match</b> simulations (for auto-play
 * and tests) and <b>per-period</b> simulations (used by the interactive
 * GUI to step through a match quarter-by-quarter or set-by-set):</p>
 *
 * <ul>
 *   <li>{@link #simulateMatch}              — full headball-style match</li>
 *   <li>{@link #simulateQuarter}            — one quarter only (interactive)</li>
 *   <li>{@link #simulateVolleyballMatch}    — full best-of-5 volleyball match</li>
 *   <li>{@link #simulateVolleyballSet}      — one volleyball set only (interactive)</li>
 *   <li>{@link #handleInjuries}             — post-match injury roll</li>
 * </ul>
 *
 * <p>Constructing the engine with a fixed seed makes every simulation fully
 * reproducible, which is important for deterministic tests.</p>
 */
public class MatchEngine {

    private final Random random;

    public MatchEngine(long seed) {
        this.random = new Random(seed);
    }

    // ── Quarter-based (Headball-style) ───────────────────────────────────────

    /**
     * Runs a full match simulation using fixed-length quarters.
     * Calls {@link #simulateQuarter} once per quarter under the hood, so the
     * scoring distribution is identical to interactive play.
     *
     * @param home        home team
     * @param away        away team
     * @param numQuarters number of quarters/periods to simulate
     * @return the resulting {@link MatchResult}
     */
    public MatchResult simulateMatch(Team home, Team away, int numQuarters) {
        int[][] quarterScores = new int[numQuarters][2];

        for (int q = 0; q < numQuarters; q++) {
            int[] qScore = simulateQuarter(home, away);
            quarterScores[q][0] = qScore[0];
            quarterScores[q][1] = qScore[1];
        }

        int homeTotal = 0, awayTotal = 0;
        for (int q = 0; q < numQuarters; q++) {
            homeTotal += quarterScores[q][0];
            awayTotal += quarterScores[q][1];
        }

        // Injuries are handled AFTER scoring so skill ratings are unaffected
        handleInjuries(home);
        handleInjuries(away);

        return new MatchResult(home, away, homeTotal, awayTotal, quarterScores);
    }

    /**
     * Simulates a single quarter and returns {@code [homeGoals, awayGoals]}.
     * Used both by {@link #simulateMatch} and by the interactive
     * {@code HeadballLiveMatch} so that quarter scoring is consistent
     * across auto-play and step-through play.
     */
    public int[] simulateQuarter(Team home, Team away) {
        int homeGoals = calculateQuarterScore(home, away);
        int awayGoals = calculateQuarterScore(away, home);
        return new int[]{homeGoals, awayGoals};
    }

    /**
     * Calculates how many goals the attacking team scores in one quarter.
     * Tuned for Headball-style high scoring (10 – 20 goal-range matches).
     */
    private int calculateQuarterScore(Team attacking, Team defending) {
        double attackPower  = attacking.getAverageSkill()
                              * attacking.getTactic().getAttackMultiplier();
        double defensePower = defending.getAverageSkill()
                              * defending.getTactic().getDefenseMultiplier();
        double total = attackPower + defensePower;
        // Stronger base conversion than before: ratio is taken raw rather than
        // halved, so equal teams hit ~50 % rather than ~25 %.
        double chance = (total > 0) ? (attackPower / total) : 0.5;
        // Clamp so neither side becomes impossible to score against
        if (chance < 0.20) chance = 0.20;
        if (chance > 0.80) chance = 0.80;

        // 6 – 12 attempts per quarter at ~50 % conversion ⇒ 3 – 6 goals/quarter,
        // or roughly 12 – 24 goals across a 4-quarter match.
        int attempts = random.nextInt(7) + 6;
        int score = 0;
        for (int i = 0; i < attempts; i++) {
            if (random.nextDouble() < chance) score++;
        }
        return score;
    }

    // ── Set-based (Volleyball) ───────────────────────────────────────────────

    /**
     * Simulates a best-of-5 volleyball match.  The {@link MatchResult}
     * returned contains:
     * <ul>
     *   <li><b>homeScore / awayScore</b> = sets won by each side (0 – 3)</li>
     *   <li><b>quarterScores</b> = points scored in each set actually played
     *       (length 3, 4 or 5)</li>
     * </ul>
     */
    public MatchResult simulateVolleyballMatch(Team home, Team away) {
        int[][] setPoints = new int[5][2];
        int homeSets = 0;
        int awaySets = 0;
        int setsPlayed = 0;

        while (homeSets < 3 && awaySets < 3 && setsPlayed < 5) {
            int target = (setsPlayed == 4) ? 15 : 25;
            int[] result = simulateVolleyballSet(home, away, target);
            setPoints[setsPlayed][0] = result[0];
            setPoints[setsPlayed][1] = result[1];
            if (result[0] > result[1]) homeSets++;
            else                       awaySets++;
            setsPlayed++;
        }

        // Trim to the actual number of sets played
        int[][] played = Arrays.copyOf(setPoints, setsPlayed);

        handleInjuries(home);
        handleInjuries(away);

        return new MatchResult(home, away, homeSets, awaySets, played);
    }

    /**
     * Simulates a single volleyball set rally-by-rally.  Each rally is awarded
     * to one of the two teams based on the ratio of effective attacking power
     * (skill × attack-multiplier, with the opposing defence-multiplier
     * dampening the attacker) to the sum of both teams' powers.  The set ends
     * as soon as a side reaches {@code targetPoints} with a 2-point lead.
     *
     * <p>A safety cap of 50 points per side prevents runaway loops in the
     * astronomically unlikely event that the chance is exactly 0.5 forever.</p>
     *
     * @param home          home team
     * @param away          away team
     * @param targetPoints  points needed to win the set (25 or 15)
     * @return a 2-element array {@code [homePoints, awayPoints]}
     */
    public int[] simulateVolleyballSet(Team home, Team away, int targetPoints) {
        double homeAttack = home.getAverageSkill() * home.getTactic().getAttackMultiplier();
        double awayAttack = away.getAverageSkill() * away.getTactic().getAttackMultiplier();
        double homeDef    = home.getAverageSkill() * home.getTactic().getDefenseMultiplier();
        double awayDef    = away.getAverageSkill() * away.getTactic().getDefenseMultiplier();

        double homeRallyChance;
        double totalAttack = homeAttack + awayAttack;
        if (totalAttack <= 0) {
            homeRallyChance = 0.5;
        } else {
            double base = homeAttack / totalAttack;
            double defAdjust = (homeDef - awayDef) / (4.0 * (homeDef + awayDef + 0.001));
            homeRallyChance = base + defAdjust;
        }
        if (homeRallyChance < 0.15) homeRallyChance = 0.15;
        if (homeRallyChance > 0.85) homeRallyChance = 0.85;

        int homePts = 0, awayPts = 0;
        final int safetyCap = 50;

        while (true) {
            if (random.nextDouble() < homeRallyChance) homePts++;
            else                                       awayPts++;

            boolean atTarget = homePts >= targetPoints || awayPts >= targetPoints;
            boolean twoPointLead = Math.abs(homePts - awayPts) >= 2;
            if (atTarget && twoPointLead) break;
            if (homePts >= safetyCap || awayPts >= safetyCap) break;
        }
        return new int[]{homePts, awayPts};
    }

    // ── Injury handling (shared) ─────────────────────────────────────────────

    /**
     * After a match, each player has a 5 % chance of getting injured for 1–3
     * games.  Players who are already injured recover by one game.
     *
     * @param team the team whose players are processed
     */
    public void handleInjuries(Team team) {
        for (AbstractPlayer player : team.getPlayers()) {
            if (player.isInjured()) {
                player.recover();
            } else if (random.nextDouble() < 0.05) {
                player.applyInjury(random.nextInt(3) + 1);
            }
        }
    }

    /**
     * Returns a fresh random double in [0,1) from the engine's seeded RNG.
     * Exposed so high-level controllers (e.g. coin-toss tiebreakers in
     * {@link League}) can stay deterministic under the same seed.
     */
    public double nextRandom() {
        return random.nextDouble();
    }
}
