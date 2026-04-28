package com.lambda.sports.volleyball;

import com.lambda.sports.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Volleyball sport module.
 *
 * <p>Rules (FIVB simplified):</p>
 * <ul>
 *   <li>6 players per side on the court, 6 substitutes (12-player squad)</li>
 *   <li>Win = 3 points, Loss = 0 points; <b>draws are impossible</b>
 *       because every match must produce a winner</li>
 *   <li>Match = best-of-5 sets; first team to 3 sets wins</li>
 *   <li>Sets 1–4: first to 25 with 2-point margin.  Set 5 (decider): first to 15 with 2-point margin</li>
 *   <li>Tactics may be changed between sets ("quarters" in the framework)</li>
 * </ul>
 *
 * <p>The {@code MatchResult} produced for a Volleyball match stores
 * <b>sets won</b> as the home/away score (0–3 each) and the per-set point
 * totals in the quarter-score grid.</p>
 */
public class VolleyballSport extends AbstractSport {

    // ── Static name pools ────────────────────────────────────────────────────

    private static final String[] TEAM_NAMES = {
        "Warsaw Spikers",   "Kraków Comets",     "Gdańsk Gliders",   "Poznań Panthers",
        "Wrocław Wolves",   "Łódź Lightning",    "Lublin Lynx",      "Szczecin Sharks"
    };

    /** A pool of plausible Polish-style player names, distinct from the headball pool. */
    private static final String[] PLAYER_NAMES = {
        "Adam Kowal",          "Bartłomiej Zięba",     "Cyprian Lis",
        "Damian Witek",        "Eryk Sokół",           "Fabian Klimek",
        "Gabriel Tomaszewski", "Hieronim Mróz",        "Ireneusz Borowski",
        "Jarosław Karpiński",  "Kornel Nowicki",       "Leszek Brzeziński",
        "Maciej Wójcik",       "Nikodem Cichocki",     "Olaf Maciejewski",
        "Patryk Sienkiewicz",  "Radosław Włodarczyk",  "Sebastian Czarnecki",
        "Tadeusz Lasek",       "Ulryk Pietrzak",       "Wacław Sobczak",
        "Xawery Małecki",      "Yarosław Witkowski",   "Zbigniew Kasprzak",
        "Antoni Marek",        "Beniamin Wrona",       "Czesław Kaliński",
        "Dariusz Jakubowski",  "Edmund Walczak",       "Feliks Szczepański",
        "Gerard Sosnowski",    "Hilary Borowicz",      "Innocenty Olszewski",
        "Janusz Maślak",       "Kacper Sadowski",      "Lechosław Tomczyk",
        "Mariusz Romanowski",  "Natan Pietruszka",     "Otton Skiba",
        "Paweł Janik",         "Roland Górski",        "Stefan Krupiński",
        "Tymon Lipiński",      "Ulrich Domański",      "Wincenty Kępa",
        "Xander Polak",        "Zenon Kalinowski",     "Anatol Drozd"
    };

    /** Pool of coach names. */
    private static final String[] COACH_NAMES = {
        "Antoni Wójcik",     "Bożena Sikora",     "Cezary Madej",      "Dorota Lipa",
        "Edmund Szczepan",   "Felicja Olszewska", "Grzegorz Tomczak",  "Halina Bednarska",
        "Igor Adamski",      "Justyna Sobczak",   "Krzysztof Bielski", "Lucyna Mróz"
    };

    // ── Constructor ──────────────────────────────────────────────────────────

    /**
     * Creates a VolleyballSport instance.
     * super(playersPerTeam=6, winPoints=3, drawPoints=0, matchDuration=90, numQuarters=5)
     *
     * <p>{@code matchDuration} is approximate — real volleyball is open-ended,
     * but ~90 minutes is a fair average. {@code numQuarters} is the maximum
     * number of sets that can be played (5).</p>
     */
    public VolleyballSport() {
        super(6, 3, 0, 90, 5);
    }

    // ── AbstractSport implementation ─────────────────────────────────────────

    /**
     * Creates {@code count} teams, each with 12 players (6 starters + 6 subs)
     * and 2 coaches.  Skills are derived deterministically from the squad
     * index so that tests relying on a fixed seed remain reproducible.
     */
    @Override
    public List<Team> createTeams(int count) {
        List<Team> teams = new ArrayList<>();
        int playerIndex = 0;
        int coachIndex  = 0;
        for (int t = 0; t < count; t++) {
            Team team = new Team(TEAM_NAMES[t % TEAM_NAMES.length]);
            team.setTactic(new VolleyballTactic(VolleyballTacticType.ALL_AROUND));

            int squadSize = playersPerTeam + 6;   // 6 starters + 6 subs
            for (int p = 0; p < squadSize; p++) {
                String pName = PLAYER_NAMES[playerIndex % PLAYER_NAMES.length];
                playerIndex++;
                // Deterministic skill spread in [4.0, 10.0]
                double attack  = 5.0 + (p * 0.55) % 5.0;
                double defense = 4.5 + (p * 0.45) % 5.5;
                double serve   = 4.0 + (p * 0.65) % 6.0;
                team.addPlayer(new VolleyballPlayer(pName, 19 + (p % 14),
                                                     attack, defense, serve));
            }

            // Two coaches per team — head coach (~7.0 skill) + assistant (~5.0)
            team.addCoach(new Coach(COACH_NAMES[coachIndex++ % COACH_NAMES.length],
                                     45 + (t % 20), 7.0));
            team.addCoach(new Coach(COACH_NAMES[coachIndex++ % COACH_NAMES.length],
                                     35 + (t % 15), 5.0));

            teams.add(team);
        }
        return teams;
    }

    /**
     * Delegates to the engine's volleyball-specific simulator (set-based,
     * best-of-5, win-by-2).  This overrides the default
     * "fixed number of equal-length quarters" model used by Headball.
     */
    @Override
    public MatchResult simulateMatch(Team home, Team away, MatchEngine engine) {
        return engine.simulateVolleyballMatch(home, away);
    }

    @Override
    public LiveMatch startLiveMatch(Team home, Team away) {
        return new VolleyballLiveMatch(home, away);
    }
}
