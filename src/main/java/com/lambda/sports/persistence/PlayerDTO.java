package com.lambda.sports.persistence;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Flat data-transfer object representing a player on disk.
 *
 * <p>Rather than relying on Jackson's polymorphic type handling (which would
 * couple our domain classes to annotations), we store every possible
 * sport-specific attribute as a nullable boxed {@link Double}.  Whichever
 * fields are populated at save time determine which concrete subclass is
 * recreated on load.  Unused fields are omitted from the JSON output via
 * {@link JsonInclude.Include#NON_NULL} for readability.</p>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PlayerDTO {

    /** Sport-specific concrete-class tag: e.g. {@code "headball"} or {@code "volleyball"}. */
    public String  type;
    public String  name;
    public int     age;
    public boolean injured;
    public int     injuryGamesLeft;

    // ── Headball-specific ───────────────────────────────────────────────────
    public Double headingAbility;
    public Double jumpHeight;

    // ── Volleyball-specific ─────────────────────────────────────────────────
    public Double attackSkill;
    public Double defenseSkill;
    public Double serveSkill;

    public PlayerDTO() {} // Jackson needs a no-arg constructor
}
