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
        // No inicializar el juego aquí si se va a pasar desde fuera
        if (game == null) {
            initializeGame();
        }
        createGridButtons();
        createOpponentGridButtons();
    }

    // Método para recibir un tablero pre-configurado desde ShipPlacementController
    public void initializeWithPlayerBoard(Board playerBoard) {
        this.game = new Game("Player");

        // Copiar el tablero configurado al jugador
        HumanPlayer human = game.getHumanPlayer();
        copyBoard(playerBoard, human.getBoard());

        // Iniciar el juego
        if (game.allHumanShipsPlaced()) {
            game.startGamePlay();
        }

        updatePlayerBoardDisplay();
        updateStatusLabel();
    }

    // Método simplificado para copiar un tablero a otro
    private void copyBoard(Board source, Board destination) {
        // Copiar todos los barcos del tablero origen al destino
        for (int row = 0; row < Board.SIZE; row++) {
            for (int col = 0; col < Board.SIZE; col++) {
                if (source.hasShip(row, col)) {
                    try {
                        Cell sourceCell = source.getCell(row, col);
                        Ship ship = sourceCell.getShipPart();

                        if (ship != null && !destination.hasShip(row, col)) {
                            // Encontrar la posición inicial del barco
                            int[] shipStart = findShipStart(source, ship, row, col);
                            Orientation orientation = determineOrientation(source, ship, shipStart[0], shipStart[1]);

                            // Crear una nueva instancia del barco
                            Ship newShip = createShipCopy(ship);
                            if (newShip != null) {
                                destination.placeShip(newShip, shipStart[0], shipStart[1], orientation);
                            }
                        }
                    } catch (Exception e) {
                        System.out.println("Error copiando barco: " + e.getMessage());
                    }
                }
            }
        }
    }

    // Método auxiliar para encontrar el inicio de un barco
    private int[] findShipStart(Board board, Ship ship, int currentRow, int currentCol) {
        int startRow = currentRow;
        int startCol = currentCol;

        // Buscar hacia arriba
        while (startRow > 0 && board.hasShip(startRow - 1, currentCol) &&
                board.getCell(startRow - 1, currentCol).getShipPart() == ship) {
            startRow--;
        }

        // Buscar hacia la izquierda
        while (startCol > 0 && board.hasShip(currentRow, startCol - 1) &&
                board.getCell(currentRow, startCol - 1).getShipPart() == ship) {
            startCol--;
        }

        return new int[]{startRow, startCol};
    }

    // Método auxiliar para determinar la orientación de un barco
    private Orientation determineOrientation(Board board, Ship ship, int startRow, int startCol) {
        // Verificar si el barco se extiende horizontalmente
        if (startCol + 1 < Board.SIZE && board.hasShip(startRow, startCol + 1) &&
                board.getCell(startRow, startCol + 1).getShipPart() == ship) {
            return Orientation.HORIZONTAL;
        }
        return Orientation.VERTICAL;
    }

    // Método auxiliar para crear una copia de un barco
    private Ship createShipCopy(Ship original) {
        if (original instanceof AircraftCarrier) return new AircraftCarrier();
        if (original instanceof Submarine) return new Submarine();
        if (original instanceof Destroyer) return new Destroyer();
        if (original instanceof Frigate) return new Frigate();
        return null;
    }

    private void initializeGame() {
        game = new Game("Player");
        placePlayerShipsAutomatically();

        if (game.allHumanShipsPlaced()) {
            game.startGamePlay();
        }

        updatePlayerBoardDisplay();
        updateStatusLabel();
    }

    // Método para mostrar los barcos del jugador en el tablero
    private void updatePlayerBoardDisplay() {
        if (game == null || gridButtons == null) return;

        HumanPlayer human = game.getHumanPlayer();
        Board board = human.getBoard();

        for (int row = 0; row < Board.SIZE; row++) {
            for (int col = 0; col < Board.SIZE; col++) {
                Button cell = gridButtons[row][col];

                if (board.hasShip(row, col)) {
                    // Mostrar barcos del jugador en azul
                    cell.setStyle("-fx-background-color: darkblue; -fx-border-color: white; -fx-border-width: 2;");
                    cell.setText("■");
                } else {
                    // Celdas vacías
                    cell.setStyle("-fx-background-color: lightcyan; -fx-border-color: navy; -fx-border-width: 1;");
                    cell.setText("");
                }
            }
        }
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
            System.out.println("Celda ya disparada: " + e.getMessage());
            updateStatusLabel("¡Esa celda ya fue disparada! Elige otra.");
        }
    }

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

            // Actualizar la visualización del tablero del jugador después del disparo de la máquina
            updatePlayerBoardAfterMachineShot();

            if (result != ShotResult.WATER && game.getGameState() == GameState.MACHINE_TURN) {
                // Usar PauseTransition en lugar de Thread.sleep
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

    // Método para actualizar la apariencia de la celda después de un disparo
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

    // Método para actualizar la visualización del oponente
    private void updateOpponentDisplay() {
        if (game == null || opponentButtons == null) return;

        // Aquí puedes agregar lógica para mostrar información del oponente
        // Por ejemplo, actualizar un grid que muestre los disparos realizados
    }

    // Método para actualizar el label de estado
    private void updateStatusLabel() {
        if (game == null || statusLabel == null) return;

        GameState state = game.getGameState();
        switch (state) {
            case PLAYER_TURN:
                statusLabel.setText("Tu turno - Haz clic en una celda para disparar");
                break;
            case MACHINE_TURN:
                statusLabel.setText("Turno de la máquina...");
                break;
            case GAME_OVER_HUMAN_WINS:
                statusLabel.setText("¡Felicidades! Has ganado la batalla naval");
                break;
            case GAME_OVER_MACHINE_WINS:
                statusLabel.setText("La máquina ha ganado. ¡Mejor suerte la próxima vez!");
                break;
            default:
                statusLabel.setText("Preparando el juego...");
                break;
        }
    }

    // Sobrecarga del método para mensajes personalizados
    private void updateStatusLabel(String message) {
        if (statusLabel != null) {
            statusLabel.setText(message);
        }
    }

    // Método para verificar si el juego ha terminado
    private void checkGameOver() {
        if (game == null) return;

        GameState state = game.getGameState();
        if (state == GameState.GAME_OVER_HUMAN_WINS || state == GameState.GAME_OVER_MACHINE_WINS) {
            // Deshabilitar todos los botones del grid
            for (int row = 0; row < Board.SIZE; row++) {
                for (int col = 0; col < Board.SIZE; col++) {
                    if (gridButtons[row][col] != null) {
                        gridButtons[row][col].setDisable(true);
                    }
                }
            }
        }
    }

    // Método para actualizar el tablero del jugador después del disparo de la máquina
    private void updatePlayerBoardAfterMachineShot() {
        if (game == null || gridButtons == null) return;

        HumanPlayer human = game.getHumanPlayer();
        Board board = human.getBoard();

        for (int row = 0; row < Board.SIZE; row++) {
            for (int col = 0; col < Board.SIZE; col++) {
                Button cell = gridButtons[row][col];

                if (board.hasShip(row, col)) {
                    Cell boardCell = board.getCell(row, col);

                    // Verificar si esta celda fue impactada
                    if (boardCell.getCellState() == CellState.HIT_SHIP_PART) {
                        // Mostrar impacto en barco del jugador
                        gridButtons[row][col].setStyle("-fx-background-color: orange; -fx-border-color: red; -fx-border-width: 2;");
                        gridButtons[row][col].setText("💥");
                    } else if (boardCell.getCellState() == CellState.SUNK_SHIP_PART) {
                        // Barco hundido
                        gridButtons[row][col].setStyle("-fx-background-color: darkred; -fx-border-color: black; -fx-border-width: 2;");
                        gridButtons[row][col].setText("💥");
                    } else {
                        // Barco no impactado
                        gridButtons[row][col].setStyle("-fx-background-color: darkblue; -fx-border-color: white; -fx-border-width: 2;");
                        gridButtons[row][col].setText("■");
                    }
                } else {
                    // Verificar si esta celda de agua fue disparada
                    if (board.wasShot(row, col)) {
                        // Agua disparada
                        gridButtons[row][col].setStyle("-fx-background-color: lightblue; -fx-border-color: blue; -fx-border-width: 1;");
                        gridButtons[row][col].setText("~");
                    } else {
                        // Agua normal
                        gridButtons[row][col].setStyle("-fx-background-color: lightcyan; -fx-border-color: navy; -fx-border-width: 1;");
                        gridButtons[row][col].setText("");
                    }
                }
            }
        }
    }

    // Método para mostrar/ocultar el tablero del oponente (si existe esta funcionalidad)
    @FXML
    private void toggleOpponentBoard() {
        showingOpponentBoard = !showingOpponentBoard;
        if (showOpponentButton != null) {
            showOpponentButton.setText(showingOpponentBoard ? "Ocultar tablero oponente" : "Mostrar tablero oponente");
        }
        // Aquí puedes agregar lógica para mostrar/ocultar el grid del oponente
    }
}