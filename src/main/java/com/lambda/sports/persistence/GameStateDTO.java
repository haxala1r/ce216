package com.lambda.sports.persistence;

import java.util.List;

/**
 * Top-level data-transfer object representing one saved game.
 *
 * <p>Field shape:</p>
 * <pre>
 * {
 *   "version":          2,
 *   "sessionName":      "My First Season",
 *   "sportId":          "volleyball",
 *   "rngSeed":          1742,
 *   "seasonNumber":     2,
 *   "nextFixtureIndex": 7,
 *   "teams":            [ TeamDTO, ... ],
 *   "playedResults":    [ MatchResultDTO, ... ]
 * }
 * </pre>
 *
 * <p>Schema versions: <b>v1</b> — initial release; <b>v2</b> — adds
 * {@code seasonNumber} and {@code TeamDTO.coaches}.  Loading a v1 file
 * still works: missing fields default to season 1 and zero coaches.</p>
 */
public class GameStateDTO {

    /** Schema version, in case the on-disk format evolves later. */
    public int                  version = 2;

    public String               sessionName;
    /** {@code "headball"} or {@code "volleyball"}. */
    public String               sportId;
    public long                 rngSeed;
    /** Defaults to 1 if the field is missing (v1 saves). */
    public int                  seasonNumber = 1;
    public int                  nextFixtureIndex;
    public List<TeamDTO>        teams;
    public List<MatchResultDTO> playedResults;

    public GameStateDTO() {}
}
