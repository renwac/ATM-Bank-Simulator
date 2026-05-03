package com.atmbanksimulator;

import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.media.AudioClip;
import javafx.scene.text.*;
import javafx.stage.Stage;

import java.util.function.Function;

// ===== 🙂 View (Eyes / Ears / Nose / Mouth / Face) =====

// The View class creates the GUI for the application.
// It does not know anything about business logic;
// it only updates the display when notified by the UIModel.
class View {
    int H = 600;         // Height of window pixels
    int W = 460;         // Width  of window pixels

    Controller controller; // Reference to the Controller (part of the MVC setup)

    // Components (controls and layout) of the user interface
    private Label laMsg;        // Message label, e.g. shows "Welcome to ATM" at startup (not the window title)
    private TextField tfInput;  // Input field where numbers typed on the keypad appear
    private TextArea taResult;  // Output area where instructions and results are displayed
    private ScrollPane scrollPane; // Provides scrollbars around the TextArea
    private GridPane grid;      // Main layout container (grid-based)
    private TilePane buttonPane;// Container for ATM keypad buttons (tiled layout)
    private Stage window;       // Keep a reference to the current window so we can switch scenes
    private AudioClip buttonSound;

    private void playButtonSound() {
        if (buttonSound != null) {
            buttonSound.play();
        }
    }

    // start() is called from Main to set up the UI.
    // Important: create controls here (not in the constructor or as field initializers),
    // so that everything is initialized in the correct order.
    public void start(Stage window) {
        this.window = window;
        var buttonSoundUrl = getClass().getResource("/button.wav");
        if (buttonSoundUrl != null) {
            buttonSound = new AudioClip(buttonSoundUrl.toExternalForm());
        }
        window.setTitle("ATM-Bank Simulator"); //set window title
        window.setScene(createWelcomeScene());
        window.show();
    }

    // Welcome page - this is what the user sees upon starting
    private Scene createWelcomeScene() {
        VBox root = new VBox(24);
        root.setStyle(
                "-fx-alignment: center;" +
                        "-fx-background-color: #1a1f2e;" +
                        "-fx-padding: 60;"
        );

        Label title = new Label("💳 ATM");
        title.setStyle(
                "-fx-font-family: 'Courier New';" +
                        "-fx-font-size: 32pt;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #00e5ff;"
        );

        Label sub = new Label("Bank Simulator");
        sub.setStyle(
                "-fx-font-family: 'Courier New';" +
                        "-fx-font-size: 13pt;" +
                        "-fx-text-fill: #7ec8f8;"
        );

        Button startBtn = new Button("INSERT CARD");
        startBtn.setStyle(
                "-fx-font-family: 'Courier New';" +
                        "-fx-font-size: 14pt;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #78f8a0;" +
                        "-fx-background-color: #1a3a1e;" +
                        "-fx-border-color: #306040;" +
                        "-fx-border-radius: 6px;" +
                        "-fx-background-radius: 6px;" +
                        "-fx-padding: 10 30 10 30;"
        );
        startBtn.setOnAction(e -> window.setScene(createMainScene()));

        root.getChildren().addAll(title, sub, startBtn);
        return new Scene(root, W, H);
    }

    // Main ATM page: login, bal, deposit, withdraw, etc.
    private Scene createMainScene() {
        // Create the user interface component objects.
        // The ATM UI is organized as a vertical grid with four main parts:
        // 1. A message label
        // 2. A text field showing numbers
        // 3. A text area showing transaction results, summaries, and user instructions
        // 4. A tiled panel of buttons
        grid = new GridPane(); // top layout
        grid.setId("Layout");  // assign an id to be used in css file
        buttonPane = new TilePane(); // kept for css compatibility (not added to scene)
        buttonPane.setId("Buttons");

        // controls
        laMsg = new Label("Welcome to Bank-ATM");  // Message bar at the top
        laMsg.setMaxWidth(Double.MAX_VALUE);
        laMsg.setAlignment(javafx.geometry.Pos.CENTER);
        grid.add(laMsg, 0, 0);         // Add to GUI at the top

        tfInput = new TextField();     // text field for numbers
        tfInput.setEditable(false);     // Read only (user can't type in)
        grid.add(tfInput, 0, 1);    // Add to GUI on second row

        taResult = new TextArea();         // text area for instructions, transaction results
        taResult.setEditable(false);       // Read only
        scrollPane  = new ScrollPane();    // create a scrolling window
        scrollPane.setContent(taResult);   // put the text area 'inside' the scrolling window
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        grid.add( scrollPane, 0, 2);    // add the scrolling window to GUI on third row

        // Numpad on the left (3 cols), action buttons on the right (2 cols),
        // joined by a small gap inside an HBox.
        String numpadTexts[][] = {
                {"7",   "8", "9"},
                {"4",   "5", "6"},
                {"1",   "2", "3"},
                {"CLR", "0", ""}
        };
        String actionTexts[][] = {
                {"Deposit",  "NewAcc"},
                {"Withdraw", "Transfer"},
                {"Balance",  "ChangePw"},
                {"Finish",   "Enter"}
        };

        Function<String, String> styleFor = label -> {
            switch (label) {
                case "0": case "1": case "2": case "3":
                case "4": case "5": case "6": case "7":
                case "8": case "9":  return "num-button";
                case "Deposit": case "Withdraw": case "Balance":
                case "Transfer": case "ChangePw": case "NewAcc": return "action-button";
                case "CLR":     return "clear-button";
                case "Finish":  return "danger-button";
                case "Enter":   return "confirm-button";
                default:                 return "";
            }
        };

        TilePane numpadPane = new TilePane();
        numpadPane.setId("Numpad");
        for (String[] row : numpadTexts) {
            for (String text : row) {
                if (!text.isEmpty()) {
                    Button btn = new Button(text);
                    btn.setOnAction(this::buttonClicked);
                    String sc = styleFor.apply(text);
                    if (!sc.isEmpty()) btn.getStyleClass().add(sc);
                    numpadPane.getChildren().add(btn);
                } else {
                    numpadPane.getChildren().add(new Text());
                }
            }
        }

        TilePane actionPane = new TilePane();
        actionPane.setId("Actions");
        for (String[] row : actionTexts) {
            for (String text : row) {
                if (!text.isEmpty()) {
                    Button btn = new Button(text);
                    btn.setOnAction(this::buttonClicked);
                    String sc = styleFor.apply(text);
                    if (!sc.isEmpty()) btn.getStyleClass().add(sc);
                    actionPane.getChildren().add(btn);
                } else {
                    actionPane.getChildren().add(new Text());
                }
            }
        }

        HBox bottomRow = new HBox(20, numpadPane, actionPane);
        bottomRow.setAlignment(javafx.geometry.Pos.CENTER);
        grid.add(bottomRow, 0, 3);

        // add the complete GUI to the window and display it
        Scene scene = new Scene(grid, W, H);
        scene.getStylesheets().add("atm.css"); // tell to use our css file
        return scene;
    }

    // Goodbye page shown when Fin is pressed
    private Scene createGoodbyeScene() {
        VBox root = new VBox(24);
        root.setStyle(
                "-fx-alignment: center;" +
                        "-fx-background-color: #1a1f2e;" +
                        "-fx-padding: 60;"
        );

        Label title = new Label("Thank you");
        title.setStyle(
                "-fx-font-family: 'Courier New';" +
                        "-fx-font-size: 28pt;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #00e5ff;"
        );

        Label sub = new Label("Please take your card");
        sub.setStyle(
                "-fx-font-family: 'Courier New';" +
                        "-fx-font-size: 13pt;" +
                        "-fx-text-fill: #c8d6e5;"
        );

        Button closeBtn = new Button("CLOSE");
        closeBtn.setStyle(
                "-fx-font-family: 'Courier New';" +
                        "-fx-font-size: 14pt;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #f87878;" +
                        "-fx-background-color: #3a1a1a;" +
                        "-fx-border-color: #703030;" +
                        "-fx-border-radius: 6px;" +
                        "-fx-background-radius: 6px;" +
                        "-fx-padding: 10 30 10 30;"
        );
        closeBtn.setOnAction(e -> window.close());

        root.getChildren().addAll(title, sub, closeBtn);
        return new Scene(root, W, H);
    }

    // This is how the View talks to the Controller
    // This method is called when a button is pressed
    // It fetches the label on the button and passes it to the controller's process method
    private void buttonClicked(ActionEvent event) {
        // this line asks the event to provide the actual Button object that was clicked
        Button b = ((Button) event.getSource());
        String text = b.getText();   // get the button label
        System.out.println("View::buttonClicked: label = " + text);  // Pass it to the controller's process method
        playButtonSound();

        if ("Finish".equals(text)) {
            controller.process(text);
            window.setScene(createGoodbyeScene());
        } else {
            controller.process(text);
        }
    }

    // This method is called by the UIModel whenever the UIModel changes.
    // It receives updated information from the UIModel and displays them in the GUI.
    // - msg → shown in the top message label
    // - tfInputMsg → shown in the text field (user input area)
    // - taResultMsg → shown in the text area (instructions / results)
    public void update(String msg,String tfInputMsg,String taResultMsg)
    {
        if (laMsg != null) {
            laMsg.setText(msg);
        }
        if (tfInput != null) {
            tfInput.setText(tfInputMsg);
        }
        if (taResult != null) {
            taResult.setText(taResultMsg);
        }
    }
}
