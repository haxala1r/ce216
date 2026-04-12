package com.lambda.sports.headball;

import com.lambda.sports.ITactic;
import com.lambda.sports.Team;

/**
 * A Headball-specific tactic.  The three stances trade attack power for
 * defensive solidity or vice-versa.
 *
 * <pre>
 * AGGRESSIVE : attack ×1.30, defence ×0.75
 * BALANCED   : attack ×1.00, defence ×1.00
 * DEFENSIVE  : attack ×0.75, defence ×1.30
 * </pre>
 */
public class HeadballTactic implements ITactic {

    private final HeadballTacticType type;

    public HeadballTactic(HeadballTacticType type) {
        this.type = type;
    }

    @Override
    public String getTacticName() { return type.name(); }

    @Override
    public double getAttackMultiplier() {
        return switch (type) {
            case AGGRESSIVE -> 1.30;
            case BALANCED   -> 1.00;
            case DEFENSIVE  -> 0.75;
        };
    }

    @Override
    public double getDefenseMultiplier() {
        return switch (type) {
            case AGGRESSIVE -> 0.75;
            case BALANCED   -> 1.00;
            case DEFENSIVE  -> 1.30;
        };
    }

    @Override
    public void applyTactic(Team team) { /* multipliers are resolved at match-time */ }

    public HeadballTacticType getType() { return type; }
}
