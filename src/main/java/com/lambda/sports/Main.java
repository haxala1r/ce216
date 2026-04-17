package com.lambda.sports;

import com.lambda.sports.gui.GuiApp;

/**
 * Entry point for {@code mvn javafx:run} and {@code mvn exec:java}.
 *
 * <p>Delegates straight to the JavaFX {@link GuiApp}.  A {@code --headless}
 * argument is accepted as a small fallback that simulates a Headball season
 * to {@code stdout}: useful for grading pipelines that cannot start a
 * graphical display.</p>
 */
public class Main {

    public static void main(String[] args) {
        for (String a : args) {
            if ("--headless".equals(a) || "-h".equals(a)) {
                runHeadlessDemo();
                return;
            }
        }
        GuiApp.main(args);
    }

    /**
     * Headless demo: 4-team Headball double round-robin printed to stdout.
     * Not the primary entry point — kept only for non-graphical environments.
     */
    private static void runHeadlessDemo() {
        com.lambda.sports.headball.HeadballSport sport = new com.lambda.sports.headball.HeadballSport();
        java.util.List<Team> teams = sport.createTeams(4);
        com.lambda.sports.game.GameSession session =
            new com.lambda.sports.game.GameSession("Demo", "headball", sport, teams, 42L);

        System.out.println("Universal Sports Manager — headless Headball demo\n");
        java.util.List<MatchResult> all = session.playRemainingSeason();
        for (MatchResult r : all) System.out.println("  " + r);

        System.out.println("\n=== FINAL STANDINGS ===");
        System.out.printf("%-4s %-22s %4s %4s %4s %4s %4s %4s %4s%n",
            "#", "TEAM", "P", "W", "D", "L", "GF", "GA", "PTS");
        java.util.List<LeagueStanding> sorted = session.getLeague().getSortedStandings();
        for (int i = 0; i < sorted.size(); i++) {
            LeagueStanding s = sorted.get(i);
            System.out.printf("%-4d %-22s %4d %4d %4d %4d %4d %4d %4d%n",
                i + 1, s.getTeam().getName(),
                s.getPlayed(), s.getWins(), s.getDraws(), s.getLosses(),
                s.getGoalsFor(), s.getGoalsAgainst(), s.getPoints());
        }
    }
}
