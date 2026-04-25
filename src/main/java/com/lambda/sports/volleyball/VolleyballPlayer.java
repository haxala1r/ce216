package com.lambda.sports.volleyball;

import com.lambda.sports.AbstractPlayer;

/**
 * A Volleyball player.
 *
 * <p>Skill rating is the weighted average of three core attributes:</p>
 * <pre>
 * skill = attackSkill × 0.45  +  defenseSkill × 0.35  +  serveSkill × 0.20
 * </pre>
 * All three attributes are on a 0 – 10 scale, so skill rating is also 0 – 10.
 *
 * <p>Attributes are mutable through {@link #applyTraining(double)}; training
 * distributes the boost across all three skills proportionally to their
 * weight in skill rating, and clamps each attribute at 10.</p>
 */
public class VolleyballPlayer extends AbstractPlayer {

    private double attackSkill;
    private double defenseSkill;
    private double serveSkill;

    public VolleyballPlayer(String name, int age,
                            double attackSkill, double defenseSkill, double serveSkill) {
        super(name, age);
        this.attackSkill  = attackSkill;
        this.defenseSkill = defenseSkill;
        this.serveSkill   = serveSkill;
    }

    /**
     * Weighted combination: attack is the dominant factor in volleyball,
     * with defence (block/dig) close behind and serve as a smaller modifier.
     */
    @Override
    public double getSkillRating() {
        return attackSkill * 0.45 + defenseSkill * 0.35 + serveSkill * 0.20;
    }

    /**
     * Boosts all three attributes by an amount proportional to their weight
     * in skill rating, clamped to the 0 – 10 scale.
     */
    @Override
    public void applyTraining(double intensity) {
        if (intensity <= 0) return;
        this.attackSkill  = Math.min(10.0, attackSkill  + intensity * 1.10);
        this.defenseSkill = Math.min(10.0, defenseSkill + intensity * 0.95);
        this.serveSkill   = Math.min(10.0, serveSkill   + intensity * 0.80);
    }

    public double getAttackSkill()  { return attackSkill; }
    public double getDefenseSkill() { return defenseSkill; }
    public double getServeSkill()   { return serveSkill; }
}
