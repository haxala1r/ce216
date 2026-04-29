package com.lambda.sports.persistence;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.lambda.sports.*;
import com.lambda.sports.game.GameSession;
import com.lambda.sports.headball.HeadballPlayer;
import com.lambda.sports.headball.HeadballSport;
import com.lambda.sports.headball.HeadballTactic;
import com.lambda.sports.headball.HeadballTacticType;
import com.lambda.sports.volleyball.VolleyballPlayer;
import com.lambda.sports.volleyball.VolleyballSport;
import com.lambda.sports.volleyball.VolleyballTactic;
import com.lambda.sports.volleyball.VolleyballTacticType;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Reads and writes {@link GameSession} objects as pretty-printed JSON.
 *
 * <p>All persistence logic lives here so the rest of the model stays
 * Jackson-free.  The on-disk format is described by {@link GameStateDTO}.</p>
 *
 * <p>Polymorphic types (any subclass of {@link AbstractPlayer},
 * {@link AbstractSport}, {@link ITactic}) are flattened into DTOs at save
 * time and reconstructed at load time using the {@code sportId} tag.</p>
 */
public class SaveManager {

    private static final String HEADBALL   = "headball";
    private static final String VOLLEYBALL = "volleyball";

    private final ObjectMapper mapper;

    public SaveManager() {
        this.mapper = new ObjectMapper();
        this.mapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    // ── Save ─────────────────────────────────────────────────────────────────

    /**
     * Serialises {@code session} to {@code file} as pretty-printed JSON.
     *
     * @throws IOException if the file cannot be written
     */
    public void save(GameSession session, Path file) throws IOException {
        GameStateDTO dto = toDTO(session);
        Files.createDirectories(file.toAbsolutePath().getParent());
        mapper.writeValue(file.toFile(), dto);
    }

    private GameStateDTO toDTO(GameSession session) {
        GameStateDTO dto = new GameStateDTO();
        dto.sessionName      = session.getSessionName();
        dto.sportId          = session.getSportId();
        dto.rngSeed          = session.getRngSeed();
        dto.seasonNumber     = session.getSeasonNumber();
        dto.nextFixtureIndex = session.getNextFixtureIndex();

        // Team list (the indices below refer to position in this list)
        dto.teams = new ArrayList<>();
        for (Team t : session.getTeams()) dto.teams.add(teamToDTO(t));

        // Match results — store team references as indices into the team list
        dto.playedResults = new ArrayList<>();
        for (MatchResult r : session.getLeague().getResults()) {
            MatchResultDTO mr = new MatchResultDTO();
            mr.homeIndex     = session.getTeams().indexOf(r.getHome());
            mr.awayIndex     = session.getTeams().indexOf(r.getAway());
            mr.homeScore     = r.getHomeScore();
            mr.awayScore     = r.getAwayScore();
            mr.quarterScores = r.getQuarterScores();
            dto.playedResults.add(mr);
        }
        return dto;
    }

    private TeamDTO teamToDTO(Team team) {
        TeamDTO dto = new TeamDTO();
        dto.name       = team.getName();
        dto.tacticType = team.getTactic().getTacticName();
        dto.players    = new ArrayList<>();
        for (AbstractPlayer p : team.getPlayers()) dto.players.add(playerToDTO(p));
        dto.coaches    = new ArrayList<>();
        for (Coach c : team.getCoaches()) dto.coaches.add(coachToDTO(c));
        return dto;
    }

    private CoachDTO coachToDTO(Coach c) {
        CoachDTO dto = new CoachDTO();
        dto.name          = c.getName();
        dto.age           = c.getAge();
        dto.trainingSkill = c.getTrainingSkill();
        return dto;
    }

    private PlayerDTO playerToDTO(AbstractPlayer player) {
        PlayerDTO dto = new PlayerDTO();
        dto.name            = player.getName();
        dto.age             = player.getAge();
        dto.injured         = player.isInjured();
        dto.injuryGamesLeft = player.getInjuryGamesLeft();

        if (player instanceof HeadballPlayer hb) {
            dto.type           = HEADBALL;
            dto.headingAbility = hb.getHeadingAbility();
            dto.jumpHeight     = hb.getJumpHeight();
        } else if (player instanceof VolleyballPlayer vb) {
            dto.type         = VOLLEYBALL;
            dto.attackSkill  = vb.getAttackSkill();
            dto.defenseSkill = vb.getDefenseSkill();
            dto.serveSkill   = vb.getServeSkill();
        } else {
            throw new IllegalArgumentException(
                "Unknown player concrete type: " + player.getClass().getName());
        }
        return dto;
    }

    // ── Load ─────────────────────────────────────────────────────────────────

    /**
     * Deserialises a {@link GameSession} from the given JSON file.
     *
     * @throws IOException              if the file cannot be read
     * @throws IllegalArgumentException if the file references an unknown sport
     */
    public GameSession load(Path file) throws IOException {
        GameStateDTO dto = mapper.readValue(file.toFile(), GameStateDTO.class);
        return fromDTO(dto);
    }

    private GameSession fromDTO(GameStateDTO dto) {
        AbstractSport sport = createSport(dto.sportId);

        // Reconstruct teams (in the same order the indices were saved)
        List<Team> teams = new ArrayList<>();
        for (TeamDTO td : dto.teams) teams.add(teamFromDTO(td, dto.sportId));

        // Reconstruct match results, resolving team indices back to references
        List<MatchResult> playedResults = new ArrayList<>();
        for (MatchResultDTO mrd : dto.playedResults) {
            playedResults.add(new MatchResult(
                teams.get(mrd.homeIndex),
                teams.get(mrd.awayIndex),
                mrd.homeScore,
                mrd.awayScore,
                mrd.quarterScores));
        }

        return new GameSession(dto.sessionName, dto.sportId, sport, teams,
                                dto.rngSeed, playedResults, dto.nextFixtureIndex,
                                dto.seasonNumber);
    }

    private AbstractSport createSport(String sportId) {
        return switch (sportId) {
            case HEADBALL   -> new HeadballSport();
            case VOLLEYBALL -> new VolleyballSport();
            default -> throw new IllegalArgumentException("Unknown sportId: " + sportId);
        };
    }

    private Team teamFromDTO(TeamDTO td, String sportId) {
        Team team = new Team(td.name);
        team.setTactic(buildTactic(sportId, td.tacticType));
        for (PlayerDTO pd : td.players) team.addPlayer(playerFromDTO(pd));
        if (td.coaches != null) {                       // null in v1 saves
            for (CoachDTO cd : td.coaches) {
                team.addCoach(new Coach(cd.name, cd.age, cd.trainingSkill));
            }
        }
        return team;
    }

    private ITactic buildTactic(String sportId, String tacticTypeName) {
        // Default fallback — used if the saved tactic name is unrecognised
        if (tacticTypeName == null) return new DefaultTactic();
        try {
            return switch (sportId) {
                case HEADBALL   -> new HeadballTactic(HeadballTacticType.valueOf(tacticTypeName));
                case VOLLEYBALL -> new VolleyballTactic(VolleyballTacticType.valueOf(tacticTypeName));
                default          -> new DefaultTactic();
            };
        } catch (IllegalArgumentException ex) {
            // Unknown enum value (e.g. file from a different version) — degrade gracefully
            return new DefaultTactic();
        }
    }

    private AbstractPlayer playerFromDTO(PlayerDTO pd) {
        AbstractPlayer player = switch (pd.type) {
            case HEADBALL -> new HeadballPlayer(
                pd.name, pd.age,
                nullSafe(pd.headingAbility), nullSafe(pd.jumpHeight));
            case VOLLEYBALL -> new VolleyballPlayer(
                pd.name, pd.age,
                nullSafe(pd.attackSkill),
                nullSafe(pd.defenseSkill),
                nullSafe(pd.serveSkill));
            default -> throw new IllegalArgumentException(
                "Unknown player type: " + pd.type);
        };
        if (pd.injured && pd.injuryGamesLeft > 0) {
            player.applyInjury(pd.injuryGamesLeft);
        }
        return player;
    }

    private static double nullSafe(Double d) { return d == null ? 0.0 : d; }

    // ── Save-file directory listing helpers ──────────────────────────────────

    /**
     * Lists every {@code *.json} file in {@code dir} that the GUI's load
     * dialog could feed back to {@link #load(Path)}.
     *
     * @return an empty list if the directory does not exist
     */
    public List<Path> listSaves(Path dir) {
        File[] kids = dir.toFile().listFiles((d, name) -> name.endsWith(".json"));
        List<Path> out = new ArrayList<>();
        if (kids != null) for (File f : kids) out.add(f.toPath());
        return out;
    }
}
