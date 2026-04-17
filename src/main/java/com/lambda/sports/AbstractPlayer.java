package com.lambda.sports;

/**
 * Base class for any player in any sport.
 * Concrete subclasses add sport-specific attributes and override getSkillRating().
 */
public abstract class AbstractPlayer {

    protected final String name;
    protected int age;
    protected boolean injured;
    protected int injuryGamesLeft;

    protected AbstractPlayer(String name, int age) {
        this.name = name;
        this.age = age;
        this.injured = false;
        this.injuryGamesLeft = 0;
    }

    /**
     * Returns a numeric skill rating for this player.
     * Used by MatchEngine to calculate match outcomes.
     */
    public abstract double getSkillRating();

    /**
     * Applies a training session to this player.  Each subclass decides which
     * of its sport-specific attributes to boost; values are clamped to the
     * 0 – 10 scale so training cannot push a player past the maximum.
     *
     * @param intensity the training boost to apply (typically 0.0 – 0.5)
     */
    public abstract void applyTraining(double intensity);

    /**
     * Increments this player's age by one year. Called by
     * {@code GameSession.advanceToNextSeason()} when a new season starts.
     */
    public void ageByOneYear() {
        this.age++;
    }

    /**
     * Marks the player as injured for the given number of games.
     *
     * @param games number of games the player will miss
     */
    public void applyInjury(int games) {
        if (games > 0) {
            this.injured = true;
            this.injuryGamesLeft = games;
        }
    }

    /**
     * Called after each match to decrement the injury counter.
     * Clears the injured flag when the counter reaches zero.
     */
    public void recover() {
        if (injuryGamesLeft > 0) {
            injuryGamesLeft--;
            if (injuryGamesLeft == 0) {
                injured = false;
            }
        }
    }

    // ── Getters ──────────────────────────────────────────────────────────────

    public String getName()           { return name; }
    public int    getAge()            { return age; }
    public boolean isInjured()        { return injured; }
    public int    getInjuryGamesLeft(){ return injuryGamesLeft; }
}
