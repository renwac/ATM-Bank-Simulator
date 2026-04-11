package com.atmbanksimulator;

import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.*;
import javafx.stage.Stage;

// ===== 🙂 View (Eyes / Ears / Nose / Mouth / Face) =====

// The View class creates the GUI for the application.
// It does not know anything about business logic;
// it only updates the display when notified by the UIModel.
class View {
    int H = 500;         // Height of window pixels
    int W = 500;         // Width  of window pixels

    Controller controller; // Reference to the Controller (part of the MVC setup)

    // Components (controls and layout) of the user interface
    private Label laMsg;        // Message label, e.g. shows "Welcome to ATM" at startup (not the window title)
    private TextField tfInput;  // Input field where numbers typed on the keypad appear
    private TextArea taResult;  // Output area where instructions and results are displayed
    private ScrollPane scrollPane; // Provides scrollbars around the TextArea
    private GridPane grid;      // Main layout container (grid-based)
    private TilePane buttonPane;// Container for ATM keypad buttons (tiled layout)
    private Stage window;       // Keep a reference to the current window so we can switch scenes

    // start() is called from Main to set up the UI.
    // Important: create controls here (not in the constructor or as field initializers),
    // so that everything is initialized in the correct order.
    public void start(Stage window) {
        this.window = window;
        window.setTitle("ATM-Bank Simulator"); //set window title
        window.setScene(createWelcomeScene());
        window.show();
    }

    // Welcome page - this is what the user sees upon starting
    private Scene createWelcomeScene() {
        VBox root = new VBox(20); // creates a new window to display welcome screen on
        root.setStyle("-fx-alignment: center;"); // aligns label and button such that they are centred

        Label title = new Label("Welcome"); // welcome text
        title.setFont(Font.font("Arial", FontWeight.BOLD, 28)); // font and fontsize for the welcome text to make it clear

        Button startBtn = new Button("Start"); //start button: takes user to the main page once pressed
        startBtn.setOnAction(e -> window.setScene(createMainScene())); // binds start button so it changes to the main screen

        root.getChildren().addAll(title, startBtn); // implements label and button

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
        buttonPane = new TilePane(); //
        buttonPane.setId("Buttons"); // assign an id to be used in css file

        // controls
        laMsg = new Label("Welcome to Bank-ATM");  // Message bar at the top
        grid.add(laMsg, 0, 0);         // Add to GUI at the top

        tfInput = new TextField();     // text field for numbers
        tfInput.setEditable(false);     // Read only (user can't type in)
        grid.add(tfInput, 0, 1);    // Add to GUI on second row

        taResult = new TextArea();         // text area for instructions, transaction results
        taResult.setEditable(false);       // Read only
        scrollPane  = new ScrollPane();    // create a scrolling window
        scrollPane.setContent(taResult);   // put the text area 'inside' the scrolling window
        grid.add( scrollPane, 0, 2);    // add the scrolling window to GUI on third row

        // Define the button layout as a 2D array of text labels.
        // Empty strings ("") represent blank spaces in the grid.
        String buttonTexts[][] = {
                {"7",    "8",  "9",  "",  "Dep",   ""},
                {"4",    "5",  "6",  "",  "W/D",   ""},
                {"1",    "2",  "3",  "",  "Bal",   "Fin"},
                {"CLR",  "0",  "",   "",  "ChgPw", "Ent"},
                {"",     "",   "",   "",  "NewAcc", ""}  };

        // Build the button panel, loop through the array,
        // - For non-empty strings, create a Button
        // - For empty strings, add an empty Text element as a spacer
        // Add all elements to the buttonPane (a tiled pane),
        // then place the buttonPane into the main grid as the fourth row.
        for ( String[] row: buttonTexts ) {
            for (String text: row) {
                if ( text.length() >= 1 ) {
                    // non-empty string - make a button
                    Button btn = new Button( text );
                    btn.setOnAction( this::buttonClicked );
                    // Register event handler: call buttonClicked() whenever this button is pressed
                    buttonPane.getChildren().add( btn );    // add this button to tiled pane
                } else {
                    // empty string - make an empty Text element as a spacer
                    buttonPane.getChildren().add( new Text() );
                }
            }
        }
        grid.add(buttonPane,0,3); // add the tiled pane of buttons to the main grid

        // add the complete GUI to the window and display it
        Scene scene = new Scene(grid, W, H);
        scene.getStylesheets().add("atm.css"); // tell to use our css file
        return scene;
    }

    // Goodbye page shown when Fin is pressed
    private Scene createGoodbyeScene() {
        VBox root = new VBox(20); // creates a new window to display goodbye screen on
        root.setStyle("-fx-alignment: center;"); // aligns label and button such that they are centred

        Label title = new Label("Goodbye"); // goodbye text
        title.setFont(Font.font("Arial", FontWeight.BOLD, 28)); // font for goodbye text to be easily seen


        Button closeBtn = new Button("Close"); // close button
        closeBtn.setOnAction(e -> window.close()); // binds the close button, so once pressed, the window is closed, and the program is stopped

        root.getChildren().addAll(title, closeBtn); // implements label and button

        return new Scene(root, W, H);
    }

    // This is how the View talks to the Controller
    // This method is called when a button is pressed
    // It fetches the label on the button and passes it to the controller's process method
    private void buttonClicked(ActionEvent event) {
        // this line asks the event to provide the actual Button object that was clicked
        Button b = ((Button) event.getSource());
        String text = b.getText();   // get the button label
        System.out.println( "View::buttonClicked: label = "+ text );  // Pass it to the controller's process method

        if ("Fin".equals(text)) {
            controller.process(text);
            window.setScene(createGoodbyeScene());
        } else {
            controller.process( text );
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
