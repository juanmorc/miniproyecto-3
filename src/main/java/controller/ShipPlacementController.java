package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import model.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Controller for the ship placement phase of the naval battle game.
 * Handles the user interface for placing ships on the board before starting the game.
 * Provides functionality for manual placement, preview visualization, random placement,
 * and validation of ship positions.
 *
 * @author [Your name]
 * @version 1.0
 * @since 2025
 */

public class ShipPlacementController {
    /** Grid where ships are placed by the player */
    @FXML private GridPane placementGrid;

    /** Small grid showing preview of selected ship */
    @FXML private GridPane previewGrid;

    /** Radio buttons for ship type selection */
    @FXML private RadioButton aircraftCarrierRadio, submarineRadio, destroyerRadio, frigateRadio;

    /** Radio buttons for orientation selection */
    @FXML private RadioButton horizontalRadio, verticalRadio;

    /** Labels showing remaining count for each ship type */
    @FXML private Label aircraftCarrierCount, submarineCount, destroyerCount, frigateCount;

    /** Label showing current status and messages to the user */
    @FXML private Label statusLabel;

    /** Control buttons for various actions */
    @FXML private Button clearBoardButton, randomPlacementButton, startGameButton, backButton;

    /** Toggle groups for mutually exclusive radio button selections */
    @FXML private ToggleGroup shipTypeGroup, orientationGroup;

    /** Matrix of buttons representing the placement board cells */
    private Button[][] gridButtons;

    /** Matrix of buttons for the ship preview display */
    private Button[][] previewButtons;

    /** The player's board where ships will be placed */
    private Board playerBoard;

    /** Map tracking remaining ships of each type */
    private Map<ShipType, Integer> shipCounts;

    /**
     * Initializes the controller and all UI components.
     * Called automatically after FXML loading.
     * Sets up the board, ship counts, buttons, and event listeners.
     */
    @FXML
    public void initialize() {
        initializeBoard();
        initializeShipCounts();
        createGridButtons();
        createPreviewButtons();
        updateUI();

        // Seleccionar portaaviones por defecto
        aircraftCarrierRadio.setSelected(true);

        // Listeners para actualizar vista previa
        shipTypeGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> updatePreview());
        orientationGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> updatePreview());

        updatePreview();
        updateStartButtonState();
    }
    /**
     * Initializes an empty player board.
     */
    private void initializeBoard() {
        playerBoard = new Board();
    }
    /**
     * Initializes the ship count map with the standard fleet configuration.
     * Sets the number of available ships for each type according to game rules.
     */
    private void initializeShipCounts() {
        shipCounts = new HashMap<>();
        shipCounts.put(ShipType.AIRCRAFT_CARRIER, 1);
        shipCounts.put(ShipType.SUBMARINE, 2);
        shipCounts.put(ShipType.DESTROYER, 3);
        shipCounts.put(ShipType.FRIGATE, 4);
    }
    /**
     * Creates the button matrix for the main placement grid.
     * Sets up event handlers for clicking, mouse enter, and mouse exit events
     * to handle ship placement and preview functionality.
     */
    private void createGridButtons() {
        gridButtons = new Button[Board.SIZE][Board.SIZE];
        placementGrid.getChildren().clear();

        for (int row = 0; row < Board.SIZE; row++) {
            for (int col = 0; col < Board.SIZE; col++) {
                Button button = new Button();
                button.setPrefSize(50, 50);
                button.setStyle("-fx-background-color: lightcyan; -fx-border-color: navy; -fx-border-width: 1;");

                final int finalRow = row;
                final int finalCol = col;

                button.setOnAction(e -> handleCellClick(finalRow, finalCol));
                button.setOnMouseEntered(e -> showPlacementPreview(finalRow, finalCol, true));
                button.setOnMouseExited(e -> showPlacementPreview(finalRow, finalCol, false));

                gridButtons[row][col] = button;
                placementGrid.add(button, col, row);
            }
        }
    }
    /**
     * Creates the button matrix for the ship preview grid.
     * This small 5x5 grid shows a preview of the currently selected ship.
     * All buttons are disabled as this is display-only.
     */
    private void createPreviewButtons() {
        previewButtons = new Button[5][5];
        previewGrid.getChildren().clear();

        for (int row = 0; row < 5; row++) {
            for (int col = 0; col < 5; col++) {
                Button button = new Button();
                button.setPrefSize(18, 20);
                button.setStyle("-fx-background-color: lightgray; -fx-border-color: black; -fx-border-width: 0.5;");
                button.setDisable(true);

                previewButtons[row][col] = button;
                previewGrid.add(button, col, row);
            }
        }
    }
    /**
     * Handles click events on the placement grid cells.
     * Attempts to place the selected ship at the clicked position
     * with the selected orientation.
     *
     * @param row The row of the clicked cell
     * @param col The column of the clicked cell
     */
    private void handleCellClick(int row, int col) {
        ShipType selectedType = getSelectedShipType();
        if (selectedType == null) {
            statusLabel.setText("Por favor selecciona un tipo de barco");
            return;
        }

        if (shipCounts.get(selectedType) <= 0) {
            statusLabel.setText("No tienes más barcos de este tipo");
            return;
        }

        Orientation orientation = horizontalRadio.isSelected() ?
                Orientation.HORIZONTAL : Orientation.VERTICAL;

        Ship ship = createShip(selectedType);
        if (ship == null) return;

        try {
            if (playerBoard.canPlaceShip(ship, row, col, orientation)) {
                playerBoard.placeShip(ship, row, col, orientation);
                shipCounts.put(selectedType, shipCounts.get(selectedType) - 1);

                updateBoardDisplay();
                updateUI();
                statusLabel.setText("¡Barco colocado correctamente!");

                selectNextAvailableShip();
                updateStartButtonState();
            } else {
                statusLabel.setText("No se puede colocar el barco en esa posición");
            }
        } catch (Exception e) {
            statusLabel.setText("Error al colocar el barco: " + e.getMessage());
        }
    }
    /**
     * Shows or hides placement preview when hovering over grid cells.
     * Displays a visual preview of where the ship would be placed,
     * using green color for valid positions and red for invalid ones.
     *
     * @param row The row being hovered over
     * @param col The column being hovered over
     * @param show Whether to show (true) or hide (false) the preview
     */
    private void showPlacementPreview(int row, int col, boolean show) {
        ShipType selectedType = getSelectedShipType();
        if (selectedType == null) return;

        Orientation orientation = horizontalRadio.isSelected() ?
                Orientation.HORIZONTAL : Orientation.VERTICAL;

        Ship ship = createShip(selectedType);
        if (ship == null) return;

        updateBoardDisplay();

        if (!show) return;

        try {
            if (playerBoard.canPlaceShip(ship, row, col, orientation)) {
                for (int i = 0; i < ship.getSize(); i++) {
                    int previewRow = orientation == Orientation.HORIZONTAL ? row : row + i;
                    int previewCol = orientation == Orientation.HORIZONTAL ? col + i : col;

                    if (previewRow < Board.SIZE && previewCol < Board.SIZE) {
                        gridButtons[previewRow][previewCol].setStyle(
                                "-fx-background-color: lightgreen; -fx-border-color: darkgreen; -fx-border-width: 2;"
                        );
                    }
                }
            } else {
                for (int i = 0; i < ship.getSize(); i++) {
                    int previewRow = orientation == Orientation.HORIZONTAL ? row : row + i;
                    int previewCol = orientation == Orientation.HORIZONTAL ? col + i : col;

                    if (previewRow < Board.SIZE && previewCol < Board.SIZE) {
                        gridButtons[previewRow][previewCol].setStyle(
                                "-fx-background-color: lightcoral; -fx-border-color: darkred; -fx-border-width: 2;"
                        );
                    }
                }
            }
        } catch (Exception e) {
            // Silenciar excepciones de vista previa
        }
    }
    /**
     * Updates the ship preview display in the small preview grid.
     * Shows how the currently selected ship would look with the chosen orientation.
     */
    private void updatePreview() {
        for (int row = 0; row < 5; row++) {
            for (int col = 0; col < 5; col++) {
                previewButtons[row][col].setStyle("-fx-background-color: lightgray; -fx-border-color: black; -fx-border-width: 0.5;");
            }
        }

        ShipType selectedType = getSelectedShipType();
        if (selectedType == null) return;

        Ship ship = createShip(selectedType);
        if (ship == null) return;

        Orientation orientation = horizontalRadio.isSelected() ?
                Orientation.HORIZONTAL : Orientation.VERTICAL;

        int startRow = orientation == Orientation.HORIZONTAL ? 2 : 2;
        int startCol = orientation == Orientation.HORIZONTAL ? 2 - ship.getSize()/2 : 2;

        if (orientation == Orientation.HORIZONTAL) {
            startCol = Math.max(0, Math.min(startCol, 5 - ship.getSize()));
        } else {
            startRow = Math.max(0, Math.min(startRow, 5 - ship.getSize()));
        }

        for (int i = 0; i < ship.getSize(); i++) {
            int previewRow = orientation == Orientation.HORIZONTAL ? startRow : startRow + i;
            int previewCol = orientation == Orientation.HORIZONTAL ? startCol + i : startCol;

            if (previewRow < 5 && previewCol < 5) {
                previewButtons[previewRow][previewCol].setStyle(
                        "-fx-background-color: darkblue; -fx-border-color: white; -fx-border-width: 1;"
                );
            }
        }
    }
    /**
     * Updates the visual display of the main placement board.
     * Shows placed ships in blue and empty cells in light cyan.
     */
    private void updateBoardDisplay() {
        for (int row = 0; row < Board.SIZE; row++) {
            for (int col = 0; col < Board.SIZE; col++) {
                Button button = gridButtons[row][col];

                if (playerBoard.hasShip(row, col)) {
                    button.setStyle("-fx-background-color: darkblue; -fx-border-color: white; -fx-border-width: 2;");
                    button.setText("■");
                } else {
                    button.setStyle("-fx-background-color: lightcyan; -fx-border-color: navy; -fx-border-width: 1;");
                    button.setText("");
                }
            }
        }
    }
    /**
     * Updates the user interface elements showing ship counts and availability.
     * Updates count labels and enables/disables radio buttons based on remaining ships.
     */
    private void updateUI() {
        aircraftCarrierCount.setText("Restantes: " + shipCounts.get(ShipType.AIRCRAFT_CARRIER));
        submarineCount.setText("Restantes: " + shipCounts.get(ShipType.SUBMARINE));
        destroyerCount.setText("Restantes: " + shipCounts.get(ShipType.DESTROYER));
        frigateCount.setText("Restantes: " + shipCounts.get(ShipType.FRIGATE));

        aircraftCarrierRadio.setDisable(shipCounts.get(ShipType.AIRCRAFT_CARRIER) <= 0);
        submarineRadio.setDisable(shipCounts.get(ShipType.SUBMARINE) <= 0);
        destroyerRadio.setDisable(shipCounts.get(ShipType.DESTROYER) <= 0);
        frigateRadio.setDisable(shipCounts.get(ShipType.FRIGATE) <= 0);
    }
    /**
     * Automatically selects the next available ship type after placing a ship.
     * Prioritizes selection in order: Aircraft Carrier, Submarine, Destroyer, Frigate.
     */
    private void selectNextAvailableShip() {
        if (shipCounts.get(ShipType.AIRCRAFT_CARRIER) > 0 && !aircraftCarrierRadio.isSelected()) {
            aircraftCarrierRadio.setSelected(true);
        } else if (shipCounts.get(ShipType.SUBMARINE) > 0 && !submarineRadio.isSelected()) {
            submarineRadio.setSelected(true);
        } else if (shipCounts.get(ShipType.DESTROYER) > 0 && !destroyerRadio.isSelected()) {
            destroyerRadio.setSelected(true);
        } else if (shipCounts.get(ShipType.FRIGATE) > 0 && !frigateRadio.isSelected()) {
            frigateRadio.setSelected(true);
        }
        updatePreview();
    }
    /**
     * Updates the state of the start game button.
     * Enables the button only when all ships have been placed on the board.
     */
    private void updateStartButtonState() {
        boolean allShipsPlaced = shipCounts.values().stream().allMatch(count -> count == 0);
        startGameButton.setDisable(!allShipsPlaced);

        if (allShipsPlaced) {
            statusLabel.setText("¡Todos los barcos colocados! Puedes iniciar el juego.");
        }
    }
    /**
     * Gets the currently selected ship type from the radio buttons.
     *
     * @return The selected ShipType, or null if none is selected
     */
    private ShipType getSelectedShipType() {
        if (aircraftCarrierRadio.isSelected()) return ShipType.AIRCRAFT_CARRIER;
        if (submarineRadio.isSelected()) return ShipType.SUBMARINE;
        if (destroyerRadio.isSelected()) return ShipType.DESTROYER;
        if (frigateRadio.isSelected()) return ShipType.FRIGATE;
        return null;
    }
    /**
     * Creates a ship instance based on the specified type.
     *
     * @param type The type of ship to create
     * @return A new ship instance, or null if the type is invalid
     */
    private Ship createShip(ShipType type) {
        switch (type) {
            case AIRCRAFT_CARRIER: return new AircraftCarrier();
            case SUBMARINE: return new Submarine();
            case DESTROYER: return new Destroyer();
            case FRIGATE: return new Frigate();
            default: return null;
        }
    }
    /**
     * Places all ships randomly on the board.
     * FXML event handler for the random placement button.
     * Uses the game's fleet configuration to place all required ships automatically.
     */
    @FXML
    private void clearBoard() {
        playerBoard = new Board();
        initializeShipCounts();
        updateBoardDisplay();
        updateUI();
        updateStartButtonState();
        aircraftCarrierRadio.setSelected(true);
        updatePreview();
        statusLabel.setText("Tablero limpiado. Comienza a colocar tus barcos.");
    }

    @FXML
    private void randomPlacement() {
        clearBoard();

        java.util.Random random = new java.util.Random();

        for (ShipType type : Game.FLEET_CONFIGURATION) {
            Ship ship = createShip(type);
            if (ship != null) {
                placeShipRandomly(ship, playerBoard, random);
                shipCounts.put(type, shipCounts.get(type) - 1);
            }
        }

        updateBoardDisplay();
        updateUI();
        updateStartButtonState();
        statusLabel.setText("¡Barcos colocados aleatoriamente!");
    }
    /**
     * Attempts to place a ship randomly on the board.
     * Tries multiple random positions and orientations until a valid placement is found.
     *
     * @param ship The ship to place
     * @param board The board to place the ship on
     * @param random Random number generator for position selection
     * @return true if the ship was successfully placed, false otherwise
     */
    private boolean placeShipRandomly(Ship ship, Board board, java.util.Random random) {
        int attempts = 0;
        int maxAttempts = 1000;

        while (attempts < maxAttempts) {
            int row = random.nextInt(Board.SIZE);
            int col = random.nextInt(Board.SIZE);
            Orientation orientation = random.nextBoolean() ?
                    Orientation.HORIZONTAL : Orientation.VERTICAL;

            try {
                if (board.canPlaceShip(ship, row, col, orientation)) {
                    board.placeShip(ship, row, col, orientation);
                    return true;
                }
            } catch (Exception e) {
                // Continuar intentando
            }
            attempts++;
        }
        return false;
    }
    /**
     * Starts the main game with the configured ship placement.
     * FXML event handler for the start game button.
     * Loads the main game stage and passes the configured board.
     */
    @FXML
    private void startGame() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/stage.fxml"));
            Parent root = loader.load();

            StageController stageController = loader.getController();
            stageController.initializeWithPlayerBoard(playerBoard);

            Stage newStage = new Stage();
            newStage.setTitle("Naval Battle - Juego");
            newStage.setScene(new Scene(root));
            newStage.setResizable(false);
            newStage.show();

            Stage currentStage = (Stage) startGameButton.getScene().getWindow();
            currentStage.close();

        } catch (IOException ex) {
            ex.printStackTrace();
            statusLabel.setText("Error al iniciar el juego");
        }
    }
    /**
     * Returns to the main menu.
     * FXML event handler for the back button.
     * Closes the current stage and opens the main menu.
     */
    @FXML
    private void goBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/inicio.fxml"));
            Parent root = loader.load();
            Stage newStage = new Stage();
            newStage.setTitle("Naval Battle");
            newStage.setScene(new Scene(root));
            newStage.setResizable(false);
            newStage.show();

            Stage currentStage = (Stage) backButton.getScene().getWindow();
            currentStage.close();

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}