package com.lambda.sports;

import java.util.Random;

/**
 * Simulates matches using skill ratings and tactic multipliers.
 *
 * <p>A match is divided into {@code numQuarters} periods.  For each period the
 * engine rolls a number of scoring attempts for each side and checks whether
 * each attempt succeeds, based on the ratio of attacking vs defending power.
 * After all quarters finish the engine processes potential injuries for both
 * teams.</p>
 *
 * <p>Constructing the engine with a fixed seed makes every simulation fully
 * reproducible, which is important for deterministic tests.</p>
 */
public class MatchEngine {

    private final Random random;

    public MatchEngine(long seed) {
        this.random = new Random(seed);
    }

    /**
     * Runs a full match simulation.
     *
     * @param home        home team
     * @param away        away team
     * @param numQuarters number of quarters/periods to simulate
     * @return the resulting {@link MatchResult}
     */
    public MatchResult simulateMatch(Team home, Team away, int numQuarters) {
        int[][] quarterScores = new int[numQuarters][2];

        for (int q = 0; q < numQuarters; q++) {
            quarterScores[q][0] = calculateQuarterScore(home, away);
            quarterScores[q][1] = calculateQuarterScore(away, home);
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
     * Calculates how many goals the attacking team scores in one quarter.
     * Uses tactic multipliers to adjust raw skill power.
     */
    private int calculateQuarterScore(Team attacking, Team defending) {
        double attackPower  = attacking.getAverageSkill()
                              * attacking.getTactic().getAttackMultiplier();
        double defensePower = defending.getAverageSkill()
                              * defending.getTactic().getDefenseMultiplier();
        double total = attackPower + defensePower;
        double chance = (total > 0) ? (attackPower / total) * 0.5 : 0.25;

        int attempts = random.nextInt(5) + 1;   // 1 – 5 attempts per quarter
        int score = 0;
        for (int i = 0; i < attempts; i++) {
            if (random.nextDouble() < chance) score++;
        }
        return score;
    }

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
}
