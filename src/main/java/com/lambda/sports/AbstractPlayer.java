package com.lambda.sports;

/**
 * Base class for any player in any sport.
 * Concrete subclasses add sport-specific attributes and override getSkillRating().
 */
public abstract class AbstractPlayer {

    protected final String name;
    protected final int age;
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
