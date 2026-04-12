package com.lambda.sports.headball;

import com.lambda.sports.AbstractPlayer;

/**
 * A Headball player.
 *
 * <p>Skill rating = headingAbility × 0.6 + jumpHeight × 0.4</p>
 * Both attributes are on a 0 – 10 scale, so skill rating is also 0 – 10.
 */
public class HeadballPlayer extends AbstractPlayer {

    /** Proficiency at heading the ball (0 – 10). */
    private final double headingAbility;

    /** Jump height, relevant for reaching aerial balls (0 – 10). */
    private final double jumpHeight;

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

    public double getHeadingAbility() { return headingAbility; }
    public double getJumpHeight()     { return jumpHeight; }
}
