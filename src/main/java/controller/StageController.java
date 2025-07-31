package controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.Node;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.animation.PauseTransition;
import javafx.util.Duration;
import model.*;
import model.exceptions.CellAlreadyShotException;

/**
 * Main controller for the naval battle game.
 * Handles the game board GUI, player turns,
 * shot visualization and overall game state.
 *
 * @author [Your name]
 * @version 1.0
 * @since 2024
 */
public class StageController {

    /** Grid that displays the human player's board */
    @FXML private GridPane gameGrid;

    /** Grid that displays opponent (machine) information */
    @FXML private GridPane opponentGrid;

    /** Label that shows the current game status */
    @FXML private Label statusLabel;

    /** Label to show opponent information */
    @FXML private Label opponentLabel;

    /** Label to control opponent board visualization */
    @FXML private Label showOpponentLabel;

    /** Button to show/hide opponent board */
    @FXML private Button showOpponentButton;

    /** Game instance that contains all the logic */
    private Game game;

    /** Button matrix representing the player's board cells */
    private Button[][] gridButtons;

    /** Button matrix representing the opponent's board cells */
    private Button[][] opponentButtons;

    /** Indicates if the opponent board is being shown */
    private boolean showingOpponentBoard = false;

    /**
     * Initializes the controller and its components.
     * Executed automatically after loading the FXML.
     */
    @FXML
    public void initialize() {
        System.out.println("board view loaded");
        // Don't initialize the game here if it will be passed from outside
        if (game == null) {
            initializeGame();
        }
        createGridButtons();
        createOpponentGridButtons();
    }

    /**
     * Initializes the game with a pre-configured board from the ship placement controller.
     * This method is called when the player comes from the ship placement screen.
     *
     * @param playerBoard The board already configured with the player's ships
     */
    public void initializeWithPlayerBoard(Board playerBoard) {
        this.game = new Game("Player");

        // Copy the configured board to the player
        HumanPlayer human = game.getHumanPlayer();
        copyBoard(playerBoard, human.getBoard());

        // Start the game
        if (game.allHumanShipsPlaced()) {
            game.startGamePlay();
        }

        updatePlayerBoardDisplay();
        updateStatusLabel();
    }

    /**
     * Copies the content from one board to another.
     * Used to transfer the player's ship configuration
     * from the placement screen to the main game.
     *
     * @param source The source board to copy from
     * @param destination The destination board to copy to
     */
    private void copyBoard(Board source, Board destination) {
        // Copy all ships from source board to destination
        for (int row = 0; row < Board.SIZE; row++) {
            for (int col = 0; col < Board.SIZE; col++) {
                if (source.hasShip(row, col)) {
                    try {
                        Cell sourceCell = source.getCell(row, col);
                        Ship ship = sourceCell.getShipPart();

                        if (ship != null && !destination.hasShip(row, col)) {
                            // Find the ship's starting position
                            int[] shipStart = findShipStart(source, ship, row, col);
                            Orientation orientation = determineOrientation(source, ship, shipStart[0], shipStart[1]);

                            // Create a new ship instance
                            Ship newShip = createShipCopy(ship);
                            if (newShip != null) {
                                destination.placeShip(newShip, shipStart[0], shipStart[1], orientation);
                            }
                        }
                    } catch (Exception e) {
                        System.out.println("Error copying ship: " + e.getMessage());
                    }
                }
            }
        }
    }

    /**
     * Finds the starting position (top-left corner) of a ship.
     * Searches upward and leftward from a given position.
     *
     * @param board The board to search in
     * @param ship The ship to find the start of
     * @param currentRow The current row to search from
     * @param currentCol The current column to search from
     * @return An array with the coordinates [row, column] of the ship's start
     */
    private int[] findShipStart(Board board, Ship ship, int currentRow, int currentCol) {
        int startRow = currentRow;
        int startCol = currentCol;

        // Search upward
        while (startRow > 0 && board.hasShip(startRow - 1, currentCol) &&
                board.getCell(startRow - 1, currentCol).getShipPart() == ship) {
            startRow--;
        }

        // Search leftward
        while (startCol > 0 && board.hasShip(currentRow, startCol - 1) &&
                board.getCell(currentRow, startCol - 1).getShipPart() == ship) {
            startCol--;
        }

        return new int[]{startRow, startCol};
    }

    /**
     * Determines a ship's orientation based on its position on the board.
     *
     * @param board The board where the ship is located
     * @param ship The ship to determine orientation for
     * @param startRow The ship's starting row
     * @param startCol The ship's starting column
     * @return The ship's orientation (HORIZONTAL or VERTICAL)
     */
    private Orientation determineOrientation(Board board, Ship ship, int startRow, int startCol) {
        // Check if the ship extends horizontally
        if (startCol + 1 < Board.SIZE && board.hasShip(startRow, startCol + 1) &&
                board.getCell(startRow, startCol + 1).getShipPart() == ship) {
            return Orientation.HORIZONTAL;
        }
        return Orientation.VERTICAL;
    }

    /**
     * Creates a copy of a ship based on its type.
     *
     * @param original The original ship to create a copy of
     * @return A new instance of the same ship type, or null if type is not recognized
     */
    private Ship createShipCopy(Ship original) {
        if (original instanceof AircraftCarrier) return new AircraftCarrier();
        if (original instanceof Submarine) return new Submarine();
        if (original instanceof Destroyer) return new Destroyer();
        if (original instanceof Frigate) return new Frigate();
        return null;
    }

    /**
     * Initializes a new game with automatic ship placement.
     * Used when no pre-configured board is provided.
     */
    private void initializeGame() {
        game = new Game("Player");
        placePlayerShipsAutomatically();

        if (game.allHumanShipsPlaced()) {
            game.startGamePlay();
        }

        updatePlayerBoardDisplay();
        updateStatusLabel();
    }

    /**
     * Updates the player's board visualization in the GUI.
     * Shows the player's ships in blue and empty cells in light color.
     */
    private void updatePlayerBoardDisplay() {
        if (game == null || gridButtons == null) return;

        HumanPlayer human = game.getHumanPlayer();
        Board board = human.getBoard();

        for (int row = 0; row < Board.SIZE; row++) {
            for (int col = 0; col < Board.SIZE; col++) {
                Button cell = gridButtons[row][col];

                if (board.hasShip(row, col)) {
                    // Show player's ships in blue
                    cell.setStyle("-fx-background-color: darkblue; -fx-border-color: white; -fx-border-width: 2;");
                    cell.setText("■");
                } else {
                    // Empty cells
                    cell.setStyle("-fx-background-color: lightcyan; -fx-border-color: navy; -fx-border-width: 1;");
                    cell.setText("");
                }
            }
        }
    }

    /**
     * Automatically places all player ships in random positions.
     * Used as fallback when there's no manual configuration.
     */
    private void placePlayerShipsAutomatically() {
        HumanPlayer human = game.getHumanPlayer();
        Board board = human.getBoard();

        for (ShipType type : Game.FLEET_CONFIGURATION) {
            Ship ship = createShip(type);
            if (ship != null) {
                if (!placeShipRandomly(ship, board)) {
                    System.out.println("The ship could not be placed: " + type);
                }
            }
        }
    }

    /**
     * Creates a ship instance based on its type.
     *
     * @param type The type of ship to create
     * @return A new instance of the requested ship, or null if type is invalid
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
     * Attempts to place a ship in a random valid position on the board.
     *
     * @param ship The ship to place
     * @param board The board where to place the ship
     * @return true if the ship was placed successfully, false otherwise
     */
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
                    System.out.println("Ship placed: " + ship.getClass().getSimpleName() +
                            " at (" + row + "," + col + ") " + orientation);
                    return true;
                }
            } catch (Exception e) {
                System.out.println("Error placing ship: " + e.getMessage());
            }
            attempts++;
        }
        System.out.println("Ship couldn't be placed after " + maxAttempts + " attempts");
        return false;
    }

    /**
     * Creates the button matrix representing the player's board.
     * Configures event handlers to handle user clicks.
     */
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

    /**
     * Creates the button matrix to show opponent board information.
     * These buttons are disabled as they are for information only.
     */
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

    /**
     * Handles the player's click on a board cell.
     * Processes the player's shot and handles turn changes.
     *
     * @param row The row of the clicked cell
     * @param col The column of the clicked cell
     */
    private void handleCellClick(int row, int col) {
        if (game == null || game.getGameState() != GameState.PLAYER_TURN) {
            return;
        }
        try {
            ShotResult result = game.processPlayerShot(row, col);
            updateCellAppearanceForShot(row, col, result);
            updateOpponentDisplay();

            if (result == ShotResult.WATER) {
                processMachineTurn();
            }

            updateStatusLabel();
            checkGameOver();

        } catch (CellAlreadyShotException e) {
            System.out.println("Cell already shot: " + e.getMessage());
            updateStatusLabel("That cell was already shot! Choose another.");
        }
    }

    /**
     * Processes the machine's turn using threads to avoid blocking the interface.
     * The machine makes its decision after a pause to improve user experience.
     */
    private void processMachineTurn() {
        if (game == null || game.getGameState() != GameState.MACHINE_TURN) {
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

            // Update player board visualization after machine shot
            updatePlayerBoardAfterMachineShot();

            if (result != ShotResult.WATER && game.getGameState() == GameState.MACHINE_TURN) {
                // Use PauseTransition instead of Thread.sleep
                PauseTransition pause = new PauseTransition(Duration.millis(500));
                pause.setOnFinished(event -> processMachineTurn());
                pause.play();
            }

            updateStatusLabel();
            checkGameOver();
        });

        Thread machineThread = new Thread(machineTask);
        machineThread.setDaemon(true);
        machineThread.start();
    }

    /**
     * Updates the visual appearance of a cell after receiving a shot.
     * Changes color and symbol based on the shot result.
     *
     * @param row The row of the shot cell
     * @param col The column of the shot cell
     * @param result The shot result (WATER, TOUCH, SUNK, etc.)
     */
    private void updateCellAppearanceForShot(int row, int col, ShotResult result) {
        if (gridButtons == null || row < 0 || row >= Board.SIZE || col < 0 || col >= Board.SIZE) {
            return;
        }

        Button cell = gridButtons[row][col];
        switch (result) {
            case TOUCH:
                cell.setStyle("-fx-background-color: orange; -fx-border-color: red; -fx-border-width: 2;");
                cell.setText("X");
                break;
            case WATER:
                cell.setStyle("-fx-background-color: cyan; -fx-border-color: blue; -fx-border-width: 2;");
                cell.setText("O");
                break;
            case SUNK:
                cell.setStyle("-fx-background-color: darkred; -fx-border-color: black; -fx-border-width: 2;");
                cell.setText("💥");
                break;
        }
        cell.setDisable(true);
    }

    /**
     * Updates the opponent board visualization.
     * Placeholder method for future opponent information display functionality.
     */
    private void updateOpponentDisplay() {
        if (game == null || opponentButtons == null) return;

        // Here you can add logic to show opponent information
        // For example, update a grid that shows made shots
    }

    /**
     * Updates the game status label according to the current state.
     * Shows appropriate messages for each game phase.
     */
    private void updateStatusLabel() {
        if (game == null || statusLabel == null) return;

        GameState state = game.getGameState();
        switch (state) {
            case PLAYER_TURN:
                statusLabel.setText("Your turn - Click on a cell to shoot");
                break;
            case MACHINE_TURN:
                statusLabel.setText("Machine's turn...");
                break;
            case GAME_OVER_HUMAN_WINS:
                statusLabel.setText("Congratulations! You won the naval battle");
                break;
            case GAME_OVER_MACHINE_WINS:
                statusLabel.setText("The machine has won. Better luck next time!");
                break;
            default:
                statusLabel.setText("Preparing the game...");
                break;
        }
    }

    /**
     * Updates the status label with a custom message.
     *
     * @param message The message to show in the status label
     */
    private void updateStatusLabel(String message) {
        if (statusLabel != null) {
            statusLabel.setText(message);
        }
    }

    /**
     * Checks if the game has ended and disables controls if necessary.
     * Blocks all board buttons when the game ends.
     */
    private void checkGameOver() {
        if (game == null) return;

        GameState state = game.getGameState();
        if (state == GameState.GAME_OVER_HUMAN_WINS || state == GameState.GAME_OVER_MACHINE_WINS) {
            // Disable all grid buttons
            for (int row = 0; row < Board.SIZE; row++) {
                for (int col = 0; col < Board.SIZE; col++) {
                    if (gridButtons[row][col] != null) {
                        gridButtons[row][col].setDisable(true);
                    }
                }
            }
        }
    }

    /**
     * Updates the player's board visualization after the machine shoots.
     * Shows received hits and current state of the player's ships.
     */
    private void updatePlayerBoardAfterMachineShot() {
        if (game == null || gridButtons == null) return;

        HumanPlayer human = game.getHumanPlayer();
        Board board = human.getBoard();

        for (int row = 0; row < Board.SIZE; row++) {
            for (int col = 0; col < Board.SIZE; col++) {
                if (board.hasShip(row, col)) {
                    Cell boardCell = board.getCell(row, col);

                    // Check if this cell was hit
                    if (boardCell.getCellState() == CellState.HIT_SHIP_PART) {
                        // Show hit on player's ship
                        gridButtons[row][col].setStyle("-fx-background-color: orange; -fx-border-color: red; -fx-border-width: 2;");
                        gridButtons[row][col].setText("💥");
                    } else if (boardCell.getCellState() == CellState.SUNK_SHIP_PART) {
                        // Sunk ship
                        gridButtons[row][col].setStyle("-fx-background-color: darkred; -fx-border-color: black; -fx-border-width: 2;");
                        gridButtons[row][col].setText("💥");
                    } else {
                        // Unhit ship
                        gridButtons[row][col].setStyle("-fx-background-color: darkblue; -fx-border-color: white; -fx-border-width: 2;");
                        gridButtons[row][col].setText("■");
                    }
                } else {
                    // Check if this water cell was shot
                    if (board.wasShot(row, col)) {
                        // Shot water
                        gridButtons[row][col].setStyle("-fx-background-color: lightblue; -fx-border-color: blue; -fx-border-width: 1;");
                        gridButtons[row][col].setText("~");
                    } else {
                        // Normal water
                        gridButtons[row][col].setStyle("-fx-background-color: lightcyan; -fx-border-color: navy; -fx-border-width: 1;");
                        gridButtons[row][col].setText("");
                    }
                }
            }
        }
    }

    /**
     * Toggles the opponent board visualization.
     * FXML method to handle the show/hide opponent board button.
     */
    @FXML
    private void toggleOpponentBoard() {
        showingOpponentBoard = !showingOpponentBoard;
        if (showOpponentButton != null) {
            showOpponentButton.setText(showingOpponentBoard ? "Hide opponent board" : "Show opponent board");
        }
        // Here you can add logic to show/hide opponent grid
    }
}