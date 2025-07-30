package controller;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.Node;
import javafx.application.Platform;
import javafx.concurrent.Task;
import model.*;
import model.exceptions.CellAlreadyShotException;

public class StageController {
    @FXML private GridPane gameGrid;
    @FXML private GridPane opponentGrid;
    @FXML private Label statusLabel;
    @FXML private Label opponentLabel;
    @FXML private Label showOpponentLabel;
    @FXML private Button showOpponentButton;

    private Game game;
    private Button[][] gridButtons;
    private Button[][] opponentButtons;
    private boolean showingOpponentBoard = false;

    @FXML
    public void initialize() {
        System.out.println("board view loaded");
        initializeGame();
        createGridButtons();
        createOpponentGridButtons();
    }

    private void initializeGame() {
        game = new Game("Player");

        placePlayerShipsAutomatically();

        if (game.allHumanShipsPlaced()) {
            game.startGamePlay();
        }

        updateStatusLabel();
    }

    private void placePlayerShipsAutomatically() {
        HumanPlayer human = game.getHumanPlayer();
        Board board = human.getBoard();

        for (ShipType type : Game.FLEET_CONFIGURATION) {
            Ship ship = createShip(type);
            if (ship != null) {
                if (!placeShipRandomly(ship, board)) {
                    System.out.println("The boat could not be placed: " + type);
                }
            }
        }
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

    private boolean placeShipRandomly(Ship ship, Board board) {
        java.util.Random random = new java.util.Random();
        int attempts = 0;
        int maxAttempts = 1000;

        while (attempts < maxAttempts) {
            int row = random.nextInt(Board.SIZE);
            int col = random.nextInt(Board.SIZE);
            Orientation orientation = random.nextBoolean() ?
                    Orientation.HORIZONTAL : Orientation.VERTICAL;

            try {
                if (orientation == Orientation.HORIZONTAL) {
                    if (col + ship.getSize() > Board.SIZE) {
                        attempts++;
                        continue;
                    }
                } else {
                    if (row + ship.getSize() > Board.SIZE) {
                        attempts++;
                        continue;
                    }
                }
                if (board.canPlaceShip(ship, row, col, orientation)) {
                    board.placeShip(ship, row, col, orientation);
                    System.out.println("Boat placed: " + ship.getClass().getSimpleName() +
                            " en (" + row + "," + col + ") " + orientation);
                    return true;
                }
            } catch (Exception e) {
                System.out.println("Error placing the boat: " + e.getMessage());
            }
            attempts++;
        }
        System.out.println("The boat could'nt be placed from " + maxAttempts + " attempts");
        return false;
    }

    private void createGridButtons() {
        gridButtons = new Button[Board.SIZE][Board.SIZE];
        gameGrid.getChildren().clear();

        for (int row = 0; row < Board.SIZE; row++) {
            for (int col = 0; col < Board.SIZE; col++) {
                Button button = new Button();
                button.setPrefSize(59, 48);
                button.setStyle("-fx-background-color: lightgray; -fx-border-color: black;");

                final int finalRow = row;
                final int finalCol = col;

                button.setOnAction(e -> handleCellClick(finalRow, finalCol));

                gridButtons[row][col] = button;
                gameGrid.add(button, col, row);
            }
        }
    }

    private void createOpponentGridButtons() {
        if (opponentGrid == null) return;

        opponentButtons = new Button[Board.SIZE][Board.SIZE];
        opponentGrid.getChildren().clear();

        for (int row = 0; row < Board.SIZE; row++) {
            for (int col = 0; col < Board.SIZE; col++) {
                Button button = new Button();
                button.setPrefSize(30, 24);
                button.setStyle("-fx-background-color: lightgray; -fx-border-color: black; -fx-font-size: 8px;");
                button.setDisable(true);

                opponentButtons[row][col] = button;
                opponentGrid.add(button, col, row);
            }
        }
    }

    private void handleCellClick(int row, int col) {
        if (game.getGameState() != GameState.PLAYER_TURN) {
            return;
        }
        try {
            ShotResult result = game.processPlayerShot(row, col);
            updateCellAppearance(row, col, result);
            updateOpponentDisplay();

            if (result == ShotResult.WATER) {
                processMachineTurn();
            }

            updateStatusLabel();
            checkGameOver();

        } catch (CellAlreadyShotException e) {
            System.out.println("Celda ya disparada: " + e.getMessage());
            updateStatusLabel("¡Esa celda ya fue disparada! Elige otra.");
        }
    }

    private void processMachineTurn() {
        if (game.getGameState() != GameState.MACHINE_TURN) {
            return;
        }

        Task<Void> machineTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                Thread.sleep(1000);
                return null;
            }
        };

        machineTask.setOnSucceeded(e -> {
            ShotResult result = game.processMachineShot();

            if (result != ShotResult.WATER && game.getGameState() == GameState.MACHINE_TURN) {
                Platform.runLater(() -> {
                    try {
                        Thread.sleep(500);
                        processMachineTurn();
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                    }
                });
            }

            updateStatusLabel();
            checkGameOver();
        });

        Thread machineThread = new Thread(machineTask);
        machineThread.setDaemon(true);
        machineThread.start();
    }

    private void updateCellAppearance(int row, int col, ShotResult result) {
        Button cell = gridButtons[row][col];

        switch (result) {
            case WATER:
                cell.setText("○");
                cell.setStyle("-fx-background-color: lightblue; -fx-border-color: black;");
                break;
            case TOUCH:
                cell.setText("X");
                cell.setStyle("-fx-background-color: orange; -fx-border-color: black;");
                break;
            case SUNK:
                cell.setText("X");
                cell.setStyle("-fx-background-color: red; -fx-border-color: black;");
                break;
        }
        cell.setDisable(true);
    }

    private void updateOpponentDisplay() {
        if (opponentButtons == null || !showingOpponentBoard) return;

        MachinePlayer machine = game.getMachinePlayer();
        Board machineBoard = machine.getBoard();

        for (int row = 0; row < Board.SIZE; row++) {
            for (int col = 0; col < Board.SIZE; col++) {
                Button cell = opponentButtons[row][col];

                if (machineBoard.hasShip(row, col)) {
                    if (machineBoard.wasShot(row, col)) {
                        cell.setText("X");
                        cell.setStyle("-fx-background-color: red; -fx-border-color: black; -fx-font-size: 8px;");
                    } else {
                        cell.setText("■");
                        cell.setStyle("-fx-background-color: darkgray; -fx-border-color: black; -fx-font-size: 8px;");
                    }
                } else if (machineBoard.wasShot(row, col)) {
                    cell.setText("○");
                    cell.setStyle("-fx-background-color: lightblue; -fx-border-color: black; -fx-font-size: 8px;");
                } else {
                    cell.setText("");
                    cell.setStyle("-fx-background-color: lightgray; -fx-border-color: black; -fx-font-size: 8px;");
                }
            }
        }
    }

    @FXML
    private void toggleOpponentBoard() {
        showingOpponentBoard = !showingOpponentBoard;

        if (showingOpponentBoard) {
            opponentGrid.setVisible(true);
            showOpponentButton.setText("Hide opponent's board");
            updateOpponentDisplay();
        } else {
            opponentGrid.setVisible(false);
            showOpponentButton.setText("Show opponent's board");
        }
    }

    private void updateStatusLabel() {
        updateStatusLabel(null);
    }

    private void updateStatusLabel(String customMessage) {
        String status = "";

        if (customMessage != null) {
            status = customMessage;
        } else {
            switch (game.getGameState()) {
                case PLAYER_TURN:
                    status = "Your turn - Click on a cell to shoot";
                    break;
                case MACHINE_TURN:
                    status = "Machine's turn...";
                    break;
                case GAME_OVER_HUMAN_WINS:
                    status = "¡Congrats! ¡You won!";
                    break;
                case GAME_OVER_MACHINE_WINS:
                    status = "The machine has won. ¡Try it again!";
                    break;
            }
        }
        if (statusLabel != null) {
            statusLabel.setText(status);
        }
    }

    private void checkGameOver() {
        if (game.getGameState() == GameState.GAME_OVER_HUMAN_WINS ||
                game.getGameState() == GameState.GAME_OVER_MACHINE_WINS) {
            for (int row = 0; row < Board.SIZE; row++) {
                for (int col = 0; col < Board.SIZE; col++) {
                    gridButtons[row][col].setDisable(true);
                }
            }
            if (!showingOpponentBoard) {
                toggleOpponentBoard();
            }
        }
    }
}