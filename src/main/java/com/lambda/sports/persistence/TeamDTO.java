package com.lambda.sports.persistence;

import java.util.List;

/** Flat data-transfer object representing a team on disk. */
public class TeamDTO {

    public String          name;
    /** Enum {@code name()} of the assigned tactic, e.g. "AGGRESSIVE" or "ALL_AROUND". */
    public String          tacticType;
    public List<PlayerDTO> players;
    public List<CoachDTO>  coaches;

    public TeamDTO() {}
}
