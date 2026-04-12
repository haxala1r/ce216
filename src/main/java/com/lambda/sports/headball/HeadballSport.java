package com.lambda.sports.headball;

import com.lambda.sports.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Headball sport module.
 *
 * <p>Rules as specified in M1:</p>
 * <ul>
 *   <li>6 players per side on the field, 3 substitutes in the squad</li>
 *   <li>Win = 2 points, Draw = 1 point</li>
 *   <li>40-minute match split into 4 quarters</li>
 *   <li>Substitutions and tactic changes are allowed between quarters</li>
 * </ul>
 */
public class HeadballSport extends AbstractSport {

    // ── Static name pools ────────────────────────────────────────────────────

    private static final String[] TEAM_NAMES = {
        "Alpha Hawks", "Beta Bulls", "Gamma Giants", "Delta Dragons",
        "Epsilon Eagles", "Zeta Zebras", "Eta Hornets", "Theta Tigers"
    };

    private static final String[] PLAYER_NAMES = {
        "Aleksander Nowak",    "Bartosz Kowalski",   "Cezary Wiśniewski",
        "Dawid Wojciechowski", "Emil Kowalczyk",     "Filip Kamiński",
        "Grzegorz Lewandowski","Hubert Zieliński",   "Igor Szymański",
        "Jakub Woźniak",       "Kamil Dąbrowski",    "Łukasz Kozłowski",
        "Marek Jankowski",     "Norbert Mazur",      "Oskar Krawczyk",
        "Piotr Pawłowski",     "Rafał Grabowski",    "Szymon Nowakowski",
        "Tomasz Pawlak",       "Urszula Michalska",  "Wiktoria Adamczyk",
        "Ximena Dudek",        "Yvonne Zając",       "Zuzanna Wieczorek",
        "Adrian Jabłoński",    "Błażej Wróbel",      "Celestyna Stępień",
        "Danuta Ostrowska",    "Edward Duda",        "Felicja Szewczyk",
        "Greta Baran",         "Henryk Sikora",      "Irena Kucharska",
        "Józef Wilk",          "Klaudia Rutkowska",  "Leon Michalak",
        "Marta Krajewska",     "Natalia Konieczna",  "Oktawia Tylor",
        "Przemysław Bąk",      "Renata Czajka",      "Stanisław Góra",
        "Teresa Wróbel",       "Uliusz Zalewska",    "Valentina Krupa",
        "Walenty Gajewski",    "Xena Kubiak",        "Zygmunt Sawicki"
    };

    // ── Constructor ──────────────────────────────────────────────────────────

    /**
     * Creates a HeadballSport instance.
     * super(playersPerTeam=6, winPoints=2, drawPoints=1, matchDuration=40, numQuarters=4)
     */
    public HeadballSport() {
        super(6, 2, 1, 40, 4);
    }

    // ── AbstractSport implementation ─────────────────────────────────────────

    /**
     * Creates {@code count} teams, each with 9 players (6 starters + 3 subs).
     * Skills are assigned deterministically from the index so tests are stable.
     */
    @Override
    public List<Team> createTeams(int count) {
        List<Team> teams = new ArrayList<>();
        int playerIndex = 0;
        for (int t = 0; t < count; t++) {
            Team team = new Team(TEAM_NAMES[t % TEAM_NAMES.length]);
            team.setTactic(new HeadballTactic(HeadballTacticType.BALANCED));

            int squadSize = playersPerTeam + 3;   // 6 starters + 3 subs
            for (int p = 0; p < squadSize; p++) {
                String pName = PLAYER_NAMES[playerIndex % PLAYER_NAMES.length];
                playerIndex++;
                // Deterministic skill spread in [5.0, 10.0]
                double heading = 5.0 + (p * 0.5) % 5.0;
                double jump    = 4.0 + (p * 0.7) % 6.0;
                team.addPlayer(new HeadballPlayer(pName, 20 + (p % 15), heading, jump));
            }
            teams.add(team);
        }
        return teams;
    }

    @Override
    public MatchResult simulateMatch(Team home, Team away, MatchEngine engine) {
        return engine.simulateMatch(home, away, numQuarters);
    }
}
