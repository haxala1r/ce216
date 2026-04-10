package com.lambda.sports;

/**
 * Neutral tactic assigned to every new team until a specific tactic is set.
 * Both multipliers are 1.0, so there is no attack/defence bias.
 */
public class DefaultTactic implements ITactic {

    @Override public String getTacticName()       { return "Default"; }
    @Override public double getAttackMultiplier() { return 1.0; }
    @Override public double getDefenseMultiplier(){ return 1.0; }
    @Override public void   applyTactic(Team t)   { /* no-op */ }
}
