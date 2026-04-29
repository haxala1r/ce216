package com.lambda.sports.persistence;

/** Flat data-transfer object representing a played match on disk. */
public class MatchResultDTO {

    /** Index into the parent {@code GameStateDTO.teams} list. */
    public int     homeIndex;
    /** Index into the parent {@code GameStateDTO.teams} list. */
    public int     awayIndex;
    public int     homeScore;
    public int     awayScore;
    /** {@code [period][0 = home, 1 = away]} — quarter or set scores. */
    public int[][] quarterScores;

    public MatchResultDTO() {}
}
