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

public class ShipPlacementController {
    @FXML private GridPane placementGrid;
    @FXML private GridPane previewGrid;

    @FXML private RadioButton aircraftCarrierRadio, submarineRadio, destroyerRadio, frigateRadio;
    @FXML private RadioButton horizontalRadio, verticalRadio;

    @FXML private Label aircraftCarrierCount, submarineCount, destroyerCount, frigateCount;
    @FXML private Label statusLabel;

    @FXML private Button clearBoardButton, randomPlacementButton, startGameButton, backButton;

    @FXML private ToggleGroup shipTypeGroup, orientationGroup;

    private Button[][] gridButtons;
    private Button[][] previewButtons;
    private Board playerBoard;
    private Map<ShipType, Integer> shipCounts;

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

    private void initializeBoard() {
        playerBoard = new Board();
    }

    private void initializeShipCounts() {
        shipCounts = new HashMap<>();
        shipCounts.put(ShipType.AIRCRAFT_CARRIER, 1);
        shipCounts.put(ShipType.SUBMARINE, 2);
        shipCounts.put(ShipType.DESTROYER, 3);
        shipCounts.put(ShipType.FRIGATE, 4);
    }

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

    private void updateStartButtonState() {
        boolean allShipsPlaced = shipCounts.values().stream().allMatch(count -> count == 0);
        startGameButton.setDisable(!allShipsPlaced);

        if (allShipsPlaced) {
            statusLabel.setText("¡Todos los barcos colocados! Puedes iniciar el juego.");
        }
    }

    private ShipType getSelectedShipType() {
        if (aircraftCarrierRadio.isSelected()) return ShipType.AIRCRAFT_CARRIER;
        if (submarineRadio.isSelected()) return ShipType.SUBMARINE;
        if (destroyerRadio.isSelected()) return ShipType.DESTROYER;
        if (frigateRadio.isSelected()) return ShipType.FRIGATE;
        return null;
    }

    private Ship createShip(ShipType type) {
        switch (type) {
            case AIRCRAFT_CARRIER: return new AircraftCarrier();
            case SUBMARINE: return new Submarine();
            case DESTROYER: return new Destroyer();
            case FRIGATE: return new Frigate();
            default: return null;
        }
    }

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