package com.lambda.sports;

import java.util.List;

/**
 * Represents a coach attached to a {@link Team}.
 *
 * <p>Coaches train players during the week between matches.  A coach's
 * {@code trainingSkill} determines how much of a boost their training
 * delivers per session (typically 0 – 10 on the same scale as player
 * attributes).  The {@link #train(List)} method applies a single training
 * round to a list of players.</p>
 *
 * <p>Coaches are sport-agnostic: every player receives the same {@code
 * intensity} value via {@link AbstractPlayer#applyTraining(double)}, and the
 * concrete player subclass decides how to translate that intensity into
 * attribute boosts.</p>
 */
public class Coach {

    /** Maximum boost a single training session can deliver, regardless of skill. */
    private static final double MAX_BOOST_PER_SESSION = 0.30;

    private final String name;
    private int          age;
    /** 0 – 10 scale, parallel to player attributes. */
    private final double trainingSkill;

    public Coach(String name, int age, double trainingSkill) {
        this.name          = name;
        this.age           = age;
        this.trainingSkill = trainingSkill;
    }

    /**
     * Runs a single training session.  Each non-injured player gets a small
     * skill boost proportional to this coach's training skill.  Injured
     * players are skipped — they cannot train while recovering.
     *
     * @param players the squad to train
     */
    public void train(List<AbstractPlayer> players) {
        double intensity = (trainingSkill / 10.0) * MAX_BOOST_PER_SESSION;
        for (AbstractPlayer p : players) {
            if (!p.isInjured()) p.applyTraining(intensity);
        }
    }

    /** Increments this coach's age by one year. */
    public void ageByOneYear() { this.age++; }

    public String getName()          { return name; }
    public int    getAge()           { return age; }
    public double getTrainingSkill() { return trainingSkill; }
}
