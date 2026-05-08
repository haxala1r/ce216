package com.lambda.sports.gui;

import com.lambda.sports.*;
import com.lambda.sports.game.GameSession;
import com.lambda.sports.headball.HeadballTactic;
import com.lambda.sports.headball.HeadballTacticType;
import com.lambda.sports.volleyball.VolleyballTactic;
import com.lambda.sports.volleyball.VolleyballTacticType;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;

import java.util.List;

/**
 * The interactive match screen.  Lets the user step through a {@link LiveMatch}
 * one period at a time (quarter for Headball, set for Volleyball).  Between
 * periods the user can change either team's tactic or perform substitutions
 * — the spec mandates exactly this between-period interaction model.
 *
 * <p>The screen is split into three regions:</p>
 * <ul>
 *   <li><b>Top:</b> running scoreline + period progress label</li>
 *   <li><b>Middle:</b> per-period score history (text log)</li>
 *   <li><b>Bottom:</b> tactic dropdowns and substitution spinners for each team,
 *       plus the "Play Next Period" / "Finish Match" action button</li>
 * </ul>
 */
public class MatchView {

    private final GuiApp        app;
    private final GameSession   session;
    private final LiveMatch     liveMatch;

    private final BorderPane    root;
    private final Label         scorelineLabel;
    private final Label         periodProgressLabel;
    private final TextArea      logArea;
    private final ComboBox<String> homeTacticPicker;
    private final ComboBox<String> awayTacticPicker;
    private final Spinner<Integer> homeOutSpinner;
    private final Spinner<Integer> homeInSpinner;
    private final Spinner<Integer> awayOutSpinner;
    private final Spinner<Integer> awayInSpinner;
    private final Button        actionButton;

    public MatchView(GuiApp app, LiveMatch liveMatch) {
        this.app                 = app;
        this.session             = app.getSession();
        this.liveMatch           = liveMatch;
        this.scorelineLabel      = new Label();
        this.periodProgressLabel = new Label();
        this.logArea             = new TextArea();
        this.homeTacticPicker    = new ComboBox<>();
        this.awayTacticPicker    = new ComboBox<>();
        this.homeOutSpinner      = new Spinner<>();
        this.homeInSpinner       = new Spinner<>();
        this.awayOutSpinner      = new Spinner<>();
        this.awayInSpinner       = new Spinner<>();
        this.actionButton        = new Button();
        this.root                = build();
        refresh();
    }

    private BorderPane build() {
        BorderPane bp = new BorderPane();
        bp.setTop(buildTopPane());
        bp.setCenter(buildCenterPane());
        bp.setBottom(buildBottomPane());
        return bp;
    }

    // ── Top: scoreline + progress ────────────────────────────────────────────

    private VBox buildTopPane() {
        scorelineLabel.setFont(Font.font(28));
        scorelineLabel.getStyleClass().add("title-label");

        periodProgressLabel.getStyleClass().add("subtitle-label");

        VBox box = new VBox(4, scorelineLabel, periodProgressLabel);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(20, 20, 12, 20));
        return box;
    }

    // ── Centre: log of period scores ────────────────────────────────────────

    private VBox buildCenterPane() {
        logArea.setEditable(false);
        logArea.setPrefRowCount(8);
        logArea.setStyle("-fx-font-family: 'monospace'; -fx-font-size: 13px;");
        VBox.setVgrow(logArea, Priority.ALWAYS);

        VBox box = new VBox(8, new Label("Period scores so far:"), logArea);
        box.setPadding(new Insets(6, 28, 6, 28));
        return box;
    }

    // ── Bottom: per-team controls + action button ───────────────────────────

    private VBox buildBottomPane() {
        // Tactic dropdowns
        List<String> tactics = tacticOptionsFor(session.getSportId());
        homeTacticPicker.getItems().setAll(tactics);
        awayTacticPicker.getItems().setAll(tactics);
        homeTacticPicker.getSelectionModel().select(liveMatch.getHome().getTactic().getTacticName());
        awayTacticPicker.getSelectionModel().select(liveMatch.getAway().getTactic().getTacticName());
        homeTacticPicker.setOnAction(e -> applyHomeTactic());
        awayTacticPicker.setOnAction(e -> applyAwayTactic());

        // Substitution spinners
        configureSubSpinners(liveMatch.getHome(), homeOutSpinner, homeInSpinner);
        configureSubSpinners(liveMatch.getAway(), awayOutSpinner, awayInSpinner);
        Button homeSwap = new Button("Swap");
        Button awaySwap = new Button("Swap");
        homeSwap.setOnAction(e -> performSwap(liveMatch.getHome(), homeOutSpinner, homeInSpinner));
        awaySwap.setOnAction(e -> performSwap(liveMatch.getAway(), awayOutSpinner, awayInSpinner));

        VBox homeColumn = teamControls(liveMatch.getHome(), homeTacticPicker,
                                        homeOutSpinner, homeInSpinner, homeSwap);
        VBox awayColumn = teamControls(liveMatch.getAway(), awayTacticPicker,
                                        awayOutSpinner, awayInSpinner, awaySwap);

        HBox columns = new HBox(24, homeColumn, awayColumn);
        columns.setAlignment(Pos.CENTER);
        HBox.setHgrow(homeColumn, Priority.ALWAYS);
        HBox.setHgrow(awayColumn, Priority.ALWAYS);
        homeColumn.setMaxWidth(Double.MAX_VALUE);
        awayColumn.setMaxWidth(Double.MAX_VALUE);

        // Action button (Play Next / Finish)
        actionButton.setDefaultButton(true);
        actionButton.setOnAction(e -> onActionClicked());

        HBox actionBar = new HBox(actionButton);
        actionBar.setAlignment(Pos.CENTER_RIGHT);
        actionBar.setPadding(new Insets(10, 0, 0, 0));

        VBox box = new VBox(12, columns, new Separator(), actionBar);
        box.setPadding(new Insets(6, 28, 18, 28));
        return box;
    }

    private VBox teamControls(Team t, ComboBox<String> tacticPicker,
                               Spinner<Integer> out, Spinner<Integer> in, Button swap) {
        Label heading = new Label(t.getName());
        heading.getStyleClass().add("section-header");

        HBox tacticRow = new HBox(8, new Label("Tactic:"), tacticPicker);
        tacticRow.setAlignment(Pos.CENTER_LEFT);
        tacticPicker.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(tacticPicker, Priority.ALWAYS);

        HBox subRow = new HBox(6,
            new Label("Out:"), out,
            new Label("In:"),  in,
            swap);
        subRow.setAlignment(Pos.CENTER_LEFT);

        VBox box = new VBox(8, heading, tacticRow, subRow);
        return box;
    }

    private static void configureSubSpinners(Team t, Spinner<Integer> out, Spinner<Integer> in) {
        int max = Math.max(0, t.getPlayers().size() - 1);
        out.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, max, 0));
        in .setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, max, Math.min(1, max)));
        out.setPrefWidth(70);
        in .setPrefWidth(70);
        out.setEditable(false);
        in .setEditable(false);
    }

    private static List<String> tacticOptionsFor(String sportId) {
        return switch (sportId) {
            case "headball" -> List.of(
                HeadballTacticType.AGGRESSIVE.name(),
                HeadballTacticType.BALANCED.name(),
                HeadballTacticType.DEFENSIVE.name());
            case "volleyball" -> List.of(
                VolleyballTacticType.OFFENSIVE.name(),
                VolleyballTacticType.ALL_AROUND.name(),
                VolleyballTacticType.DEFENSIVE.name());
            default -> List.of();
        };
    }

    // ── Actions ──────────────────────────────────────────────────────────────

    private void applyHomeTactic() {
        String name = homeTacticPicker.getValue();
        if (name == null) return;
        liveMatch.getHome().setTactic(buildTactic(name));
    }

    private void applyAwayTactic() {
        String name = awayTacticPicker.getValue();
        if (name == null) return;
        liveMatch.getAway().setTactic(buildTactic(name));
    }

    private ITactic buildTactic(String name) {
        return switch (session.getSportId()) {
            case "headball"   -> new HeadballTactic(HeadballTacticType.valueOf(name));
            case "volleyball" -> new VolleyballTactic(VolleyballTacticType.valueOf(name));
            default            -> new DefaultTactic();
        };
    }

    private void performSwap(Team t, Spinner<Integer> out, Spinner<Integer> in) {
        boolean ok = t.makeSubstitution(out.getValue(), in.getValue());
        if (!ok) {
            new Alert(Alert.AlertType.WARNING,
                "Invalid swap — pick two different valid indices.").showAndWait();
        }
    }

    /** Either plays the next period, or (if the match is finished) records and exits. */
    private void onActionClicked() {
        if (liveMatch.isFinished()) {
            MatchResult result = session.recordInteractiveResult(liveMatch);
            new Alert(Alert.AlertType.INFORMATION, "Final result:\n\n" + result).showAndWait();
            // Return to dashboard with fresh state
            if (session.isSeasonComplete()) {
                app.setView(new SeasonEndView(app).getRoot());
            } else {
                app.setView(new DashboardView(app).getRoot());
            }
            return;
        }
        liveMatch.playNextPeriod(session.getEngine());
        refresh();
    }

    /** Pulls fresh state out of the LiveMatch and updates every widget. */
    private void refresh() {
        // Scoreline
        scorelineLabel.setText(liveMatch.getHome().getName() + "  "
            + liveMatch.getHomeRunningScore() + " – " + liveMatch.getAwayRunningScore()
            + "  " + liveMatch.getAway().getName());

        String periodWord = "headball".equals(session.getSportId()) ? "Quarter" : "Set";
        if (liveMatch.isFinished()) {
            periodProgressLabel.setText("Match complete · " + liveMatch.getPeriodsPlayed()
                                         + " " + periodWord.toLowerCase() + "s played");
            actionButton.setText("Finish & Continue");
        } else {
            periodProgressLabel.setText("Next: " + periodWord + " " + liveMatch.getNextPeriodNumber()
                                         + "  ·  Played: " + liveMatch.getPeriodsPlayed()
                                         + " / " + liveMatch.getMaxPeriods());
            actionButton.setText("Play Next " + periodWord);
        }

        // Period log
        StringBuilder sb = new StringBuilder();
        int[][] all = liveMatch.getAllPeriodScores();
        for (int i = 0; i < all.length; i++) {
            sb.append(String.format("%-8s %d : %d   %s%n",
                periodWord + " " + (i + 1),
                all[i][0], all[i][1],
                "headball".equals(session.getSportId()) ? "(quarter goals)" : "(set points)"));
        }
        if (all.length == 0) sb.append("(no periods played yet)\n");
        logArea.setText(sb.toString());

        // Refresh sub spinner ranges (squad list size shouldn't change but keep
        // them in sync defensively)
        configureSubSpinners(liveMatch.getHome(), homeOutSpinner, homeInSpinner);
        configureSubSpinners(liveMatch.getAway(), awayOutSpinner, awayInSpinner);
    }

    public Parent getRoot() { return root; }
}
