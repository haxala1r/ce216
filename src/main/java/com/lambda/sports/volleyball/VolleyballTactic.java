package com.lambda.sports.volleyball;

import com.lambda.sports.ITactic;
import com.lambda.sports.Team;

/**
 * A Volleyball-specific tactic.  The three stances trade attacking
 * (spike/serve) power for defensive (block/dig) solidity.
 *
 * <pre>
 * OFFENSIVE  : attack ×1.30, defence ×0.80
 * ALL_AROUND : attack ×1.00, defence ×1.00
 * DEFENSIVE  : attack ×0.80, defence ×1.30
 * </pre>
 */
public class VolleyballTactic implements ITactic {

    private final VolleyballTacticType type;

    public VolleyballTactic(VolleyballTacticType type) {
        this.type = type;
    }

    @Override
    public String getTacticName() { return type.name(); }

    @Override
    public double getAttackMultiplier() {
        return switch (type) {
            case OFFENSIVE  -> 1.30;
            case ALL_AROUND -> 1.00;
            case DEFENSIVE  -> 0.80;
        };
    }

    @Override
    public double getDefenseMultiplier() {
        return switch (type) {
            case OFFENSIVE  -> 0.80;
            case ALL_AROUND -> 1.00;
            case DEFENSIVE  -> 1.30;
        };
    }

    @Override
    public void applyTactic(Team team) { /* multipliers are resolved at match-time */ }

    public VolleyballTacticType getType() { return type; }
}
