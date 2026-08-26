package edu.neumont.Pwest.finalfinal;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.ResourceBundle;
import javafx.fxml.Initializable;

import javax.swing.*;

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

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // TODO: load persisted counter data and populate counterListContainer
        // TODO: load persisted chat history (if you keep any) into chatHistoryList

        searchButton.setOnAction(e -> onSearch());
        addCounterButton.setOnAction(e -> onAddCounter());
        chatSendButton.setOnAction(e -> onSendChat());
        teamBuilderButton.setOnAction(e -> onSwitchToTeamBuilder());
    }
    @FXML
    private String onSearch() {
        String query = speciesSearchField.getText().trim().toLowerCase().replace(" ", "-");

        if (query.isEmpty()) {

            Alert errorAlert = new Alert(Alert.AlertType.WARNING);
            errorAlert.setHeaderText("INVALID QUERY");
            errorAlert.setContentText("Input cannot be empty");
            errorAlert.showAndWait();
            return null;
        }

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

        // TODO: call your Pokémon API layer with `query`, then either
        // scroll to / highlight an existing counter row, or feed the
        // result into onAddCounter().
    }

    private void onAddCounter() {
        String url= onSearch();
        if (url == null) {
            return;
        }
        String query = speciesSearchField.getText().trim().toLowerCase().replace(" ", "-");




        HttpResponse<String> response = null;

        HBox hBox = new HBox();
        Label speciesNameLabel = new Label(query);
        counterListContainer.getChildren().add(hBox);
        hBox.getChildren().add(speciesNameLabel);

        Region spacer = new Region();
        Region spacer1 = new Region();
        spacer.setPrefSize(50, 10);
        spacer1.setPrefSize(50, 10);
        hBox.getChildren().add(spacer);

       Label counterValueNumLabel = new  Label("0");
        Label counterValueTextLabel = new  Label("#:"+counterValueNumLabel.getText());
        hBox.getChildren().add(counterValueTextLabel);

        EventHandler<ActionEvent> event = new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e)
            {
                counterValueNumLabel.setText(counterValueNumLabel.getText()+1);
            }
        };

        Button incrementButton = new Button("+1");
        incrementButton.setOnAction(event);
        hBox.getChildren().add(spacer1);
        hBox.getChildren().add(incrementButton);



        // HBox row
        //                                  Label   speciesNameLabel   ("Pokémon->GetName()")
        //                                  Region  spacer             (HBox.hgrow = ALWAYS)
        //                                  Label   counterValueLabel  ("#: num")
        //                                  Button  incrementButton    ("num++")

        // TODO: build a new counter row (see the comment in Tracker.fxml
        // for the suggested HBox structure) and append it to
        // counterListContainer.getChildren(). Persist the updated list.
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
}