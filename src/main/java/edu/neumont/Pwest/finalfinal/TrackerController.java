package edu.neumont.Pwest.finalfinal;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.io.*;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import javafx.fxml.Initializable;

/**
 * Controller for Tracker.fxml.
 * Rename the package above to match your project structure, and update
 * fx:controller in the FXML to match if you move this class.
 */

/**
 *   HBox row
 *   Label     speciesNameLabel     ("Pikachu")
 *   ComboBox  gameComboBox         ("Yellow" / "Sword" / ...)
 *   Region    spacer               (HBox.hgrow = ALWAYS)
 *   Label     counterValueLabel    ("#: 247")
 *   Button    incrementButton      ("+1")
 */


public class TrackerController implements Initializable {

    // ===== Top bar =====
    @FXML private BorderPane trackerRoot;
    @FXML private Button teamBuilderButton;

    // ===== Center: search + counter list =====
    @FXML private TextField speciesSearchField;
    @FXML private Button searchButton;
    @FXML private Button addCounterButton;
    @FXML private ScrollPane counterScrollPane;
    @FXML private VBox counterListContainer;

    // ===== Right: Prof.Bug panel =====
    @FXML private ListView<String> chatHistoryList;
    @FXML private TextField chatInputField;
    @FXML private Button chatSendButton;

    private List<TrackedEntry> trackedEntries = new ArrayList<>();
    private SpeciesAutocomplete autocomplete;

    static final String SAVE_DIR = String.valueOf(Paths.get(System.getProperty("user.home"), "Documents", "PokeTracker"));
    private static final String SAVE_FILE = SAVE_DIR + "/tracker.json";

    @Override
    public void initialize(URL location, ResourceBundle resources) {


        trackedEntries = loadEntries();
        counterListContainer.getChildren().addAll(
                trackedEntries.stream()
                        .map(this::buildCounterRow)
                        .collect(Collectors.toList())
        );
        searchButton.setOnAction(e -> onSearch());
        addCounterButton.setOnAction(e -> onAddCounter());
        chatSendButton.setOnAction(e -> onSendChat());
        teamBuilderButton.setOnAction(e -> onSwitchToTeamBuilder());

        List<String> allNames;
        try {
            allNames = new APIController().allPokemonNames();
        } catch (IOException | InterruptedException e) {
            // No internet just means no suggestions -- the search box still works by hand.
            e.printStackTrace();
            allNames = List.of();
        }
        autocomplete = SpeciesAutocomplete.attach(speciesSearchField, allNames);
    }
    @FXML
    private String onSearch() {
        InputValidator.SearchQuery parsed = InputValidator.parseSearch(speciesSearchField.getText());

        if (!parsed.isValid()) {
            // Malformed input is caught here, so no request is sent at all.
            Alert errorAlert = new Alert(Alert.AlertType.WARNING);
            errorAlert.setHeaderText("INVALID QUERY");
            errorAlert.setContentText(parsed.getProblem());
            errorAlert.showAndWait();
            return null;
        }

        String query = parsed.getApiQuery();

        APIController controller = new APIController();
        String url = controller.POKE_URL + query;
        HttpClient client = HttpClient.newHttpClient();

        HttpRequest request =  HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET().build();


        HttpResponse<String> response = null;
        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setHeaderText("API can not be reached.");
            errorAlert.setContentText("Due to a error Unable to reach API: \""+url+"\""+"\n*make sure you have internet*");
            errorAlert.showAndWait();
            return null;
        }


        System.out.println("URL: " + url);
        // if invalid user input for the
        assert response != null;
        if (response.statusCode() != 200) {
            try {
                System.out.println("status code " + response.statusCode());
                throw new IOException();
            } catch (IOException e) {
                Alert errorAlert = new Alert(Alert.AlertType.WARNING);
                errorAlert.setHeaderText("Input not valid");
                errorAlert.setContentText("The Pokemon/id does not exist: \""+query+"\"");
                errorAlert.showAndWait();
                return null;
            }
        }

        return url;


    }

    private void onAddCounter() {
        String url = onSearch();
        if (url == null) {
            return;
        }
        String query = InputValidator.parseSearch(speciesSearchField.getText()).getApiQuery();
        APIController apiController = new APIController();
        Pokemon mon =  apiController.fetchPokemon(query);

        TrackedEntry entry = new TrackedEntry(mon.getName(),mon.getNationalDexNumber(),mon.getGamesIncludedIn() );
        trackedEntries.add(entry);
        counterListContainer.getChildren().add(buildCounterRow(entry));
        saveEntries(trackedEntries);



    }

    private HBox buildCounterRow(TrackedEntry entry) {
        HBox row = new HBox(8);
        row.setAlignment(Pos.CENTER_LEFT);

        Label speciesNameLabel = new Label(entry.getSpeciesName()
                .substring(0, 1)
                .toUpperCase()
                .concat
                        (entry.getSpeciesName()
                                .substring(1)));

        speciesNameLabel.setPrefWidth(135);
        speciesNameLabel.setMinWidth(Region.USE_PREF_SIZE);
        speciesNameLabel.setMaxWidth(Region.USE_PREF_SIZE);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        spacer.setPrefWidth(70);
        spacer.setMinWidth(Region.USE_PREF_SIZE);
        spacer.setMaxWidth(Region.USE_PREF_SIZE);

        Label counterValueLabel = new Label("#: " + entry.getCount());

        counterValueLabel.setPrefWidth(50);
        counterValueLabel.setMinWidth(Region.USE_PREF_SIZE);
        counterValueLabel.setMaxWidth(Region.USE_PREF_SIZE);

        ComboBox<String> gameComboBox = new ComboBox<>();

        gameComboBox.setPromptText("Select game");
        gameComboBox.getItems().addAll(entry.getAvailableGames());
        gameComboBox.setValue(entry.getGame());
        gameComboBox.valueProperty().addListener((obs, oldGame, newGame) -> {
            entry.setGame(newGame);
            saveEntries(trackedEntries);
        });
        gameComboBox.setPrefWidth(140);
        gameComboBox.setMinWidth(Region.USE_PREF_SIZE);

        Button incrementButton = new Button("+1");
        incrementButton.setOnAction(e -> {
            entry.setCount(entry.getCount() + 1);
            counterValueLabel.setText("#: " + entry.getCount());
            saveEntries(trackedEntries);
        });

        Button destroyRow = new Button("Gotten");
        destroyRow.setOnAction(e -> {

            counterListContainer.getChildren().remove(row);
            row.getChildren().remove(entry);
            trackedEntries.remove(entry);
            saveEntries(trackedEntries);
        });

        incrementButton.getStyleClass().add("primary-button");
        destroyRow.getStyleClass().add("danger-button");

        row.getChildren().addAll(speciesNameLabel, spacer, gameComboBox,counterValueLabel, incrementButton,destroyRow);
        saveEntries(trackedEntries);
        return row;
    }

    private void onSendChat() {
        String message = chatInputField.getText();
        if (message == null || message.isBlank()) {
            return;
        }
        chatHistoryList.getItems().add("You: " + message);
        chatInputField.clear();

        // TODO: send `message` to Prof.Bug's backend (API call or rule-based
        // system) and append the response with:
        // chatHistoryList.getItems().add("Prof.Bug: " + response);
        // not going to be implemented
    }

    @FXML
    private void onSwitchToTeamBuilder() {


        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("TeamBulider.fxml"));
            Parent teamBuilderRoot = loader.load();
            trackerRoot.getScene().setRoot(teamBuilderRoot);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveEntries(List<TrackedEntry> entries) {
        new File(SAVE_DIR).mkdirs();
        System.out.println(SAVE_DIR);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try (Writer writer = new FileWriter(SAVE_FILE)) {
            gson.toJson(entries, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List<TrackedEntry> loadEntries() {
        if (!Files.exists(Path.of(SAVE_FILE))) {
            return new ArrayList<>();
        }
        Gson gson = new Gson();
        try (Reader reader = Files.newBufferedReader(Path.of(SAVE_FILE))) {
            Type listType = new TypeToken<List<TrackedEntry>>(){}.getType();
            List<TrackedEntry> loaded = gson.fromJson(reader, listType);
            return loaded == null ? new ArrayList<>() : loaded;
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
}


