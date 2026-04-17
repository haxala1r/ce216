package com.lambda.sports.headball;

import com.lambda.sports.AbstractPlayer;

/**
 * A Headball player.
 *
 * <p>Skill rating = headingAbility × 0.6 + jumpHeight × 0.4</p>
 * Both attributes are on a 0 – 10 scale, so skill rating is also 0 – 10.
 *
 * <p>Attributes are mutable through {@link #applyTraining(double)} so that
 * coaches can improve players over the course of a season.  Training applies
 * a small boost weighted slightly towards heading (which has higher impact
 * on skill rating) and clamps each attribute at 10.</p>
 */
public class HeadballPlayer extends AbstractPlayer {

    /** Proficiency at heading the ball (0 – 10). */
    private double headingAbility;

    /** Jump height, relevant for reaching aerial balls (0 – 10). */
    private double jumpHeight;

    public HeadballPlayer(String name, int age, double headingAbility, double jumpHeight) {
        super(name, age);
        this.headingAbility = headingAbility;
        this.jumpHeight     = jumpHeight;
    }

    /** Weighted combination: heading is the dominant attribute in Headball. */
    @Override
    public double getSkillRating() {
        return headingAbility * 0.6 + jumpHeight * 0.4;
    }

    /**
     * Boosts both attributes by a small amount, clamped to the 0 – 10 scale.
     * Heading receives a slightly larger boost because it dominates skill rating.
     */
    @Override
    public void applyTraining(double intensity) {
        if (intensity <= 0) return;
        this.headingAbility = Math.min(10.0, headingAbility + intensity * 1.1);
        this.jumpHeight     = Math.min(10.0, jumpHeight     + intensity * 0.9);
    }

    public double getHeadingAbility() { return headingAbility; }
    public double getJumpHeight()     { return jumpHeight; }
}
