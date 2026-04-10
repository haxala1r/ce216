package com.lambda.sports;

/**
 * Interface defining tactical behaviour for a sport.
 * Different tactics adjust the attack/defence balance of a team.
 */
public interface ITactic {
    /** Human-readable name of this tactic. */
    String getTacticName();

    /** Multiplier applied to the team's attacking power (e.g. 1.3 for aggressive). */
    double getAttackMultiplier();

    /** Multiplier applied to the team's defensive power (e.g. 1.3 for defensive). */
    double getDefenseMultiplier();

    /** Called when the tactic is assigned to a team. */
    void applyTactic(Team team);
}
