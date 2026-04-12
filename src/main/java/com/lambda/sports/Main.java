package com.lambda.sports;

import com.lambda.sports.headball.HeadballSport;

import java.util.List;

/**
 * Entry point for {@code mvn exec:java}.
 *
 * Demonstrates a full double round-robin Headball season with 4 teams,
 * then prints the final standings table to stdout.
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println("   Universal Sports Manager Framework  v1.0.0    ");
        System.out.println("              Team Lambda Transformation          ");
        System.out.println("=================================================");

        // ── Create the sport, engine, teams ──────────────────────────────────
        HeadballSport sport  = new HeadballSport();
        MatchEngine   engine = new MatchEngine(42L);   // fixed seed → reproducible

        System.out.println("\n[+] Creating 4 Headball teams...");
        List<Team> teams = sport.createTeams(4);
        for (Team t : teams) {
            System.out.printf("    %-22s | %d players | avg skill: %.2f%n",
                    t.getName(), t.getPlayers().size(), t.getAverageSkill());
        }

        // ── Set up a league and simulate the full season ──────────────────────
        League league = new League("Headball Premier League", sport, teams);

        System.out.println("\n[+] Simulating full season (double round-robin)...\n");
        List<MatchResult> schedule = sport.generateSchedule(teams, engine);
        for (MatchResult r : schedule) {
            league.recordResult(r);
            System.out.printf("    %s%n", r);
        }

        // ── Print final standings ─────────────────────────────────────────────
        System.out.println();
        System.out.println("=== FINAL STANDINGS ===");
        System.out.printf("%-4s %-22s %4s %4s %4s %4s %4s %4s %4s%n",
                "#", "TEAM", "P", "W", "D", "L", "GF", "GA", "PTS");
        System.out.println("-".repeat(62));

        List<LeagueStanding> sorted = league.getSortedStandings();
        for (int i = 0; i < sorted.size(); i++) {
            LeagueStanding s = sorted.get(i);
            System.out.printf("%-4d %-22s %4d %4d %4d %4d %4d %4d %4d%n",
                    i + 1,
                    s.getTeam().getName(),
                    s.getPlayed(),
                    s.getWins(),
                    s.getDraws(),
                    s.getLosses(),
                    s.getGoalsFor(),
                    s.getGoalsAgainst(),
                    s.getPoints());
        }
        System.out.println("=================================================");
    }
}
