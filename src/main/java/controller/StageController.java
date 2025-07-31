package controller;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
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
    @FXML private Button autoPlaceButton;
    @FXML private Button toggleOrientationButton;
    @FXML private Label shipToPlaceLabel;

    private Game game;
    private Button[][] gridButtons;
    private Button[][] opponentButtons;
    private boolean showingOpponentBoard = false;
    
    // Variables para colocación manual de barcos
    private boolean isManualPlacementMode = true;
    private Orientation currentOrientation = Orientation.HORIZONTAL;
    private ShipType currentShipToPlace = null;
    private int shipPlacementIndex = 0;

    @FXML
    public void initialize() {
        System.out.println("board view loaded");
        initializeGame();
        createGridButtons();
        createOpponentGridButtons();
    }

    private void initializeGame() {
        // Intentar cargar juego guardado primero
        Game savedGame = Game.loadGame();
        
        if (savedGame != null) {
            game = savedGame;
            isManualPlacementMode = false; // Juego cargado, no necesita colocación
            System.out.println("Juego cargado desde archivo guardado.");
            System.out.println("Estado: " + game.getGameState());
            System.out.println("Jugador: " + game.getHumanNickname());
            
            // Si el juego guardado estaba terminado, empezar uno nuevo
            if (game.getGameState() == GameState.GAME_OVER_HUMAN_WINS || 
                game.getGameState() == GameState.GAME_OVER_MACHINE_WINS) {
                System.out.println("El juego guardado estaba terminado. Iniciando juego nuevo.");
                game.deleteSaveFile();
                startNewGame();
            }
        } else {
            System.out.println("No hay juego guardado. Iniciando juego nuevo.");
            startNewGame();
        }

        updateStatusLabel();
        updateShipPlacementUI();
    }

    private void startNewGame() {
        game = new Game("Player");
        isManualPlacementMode = true;
        shipPlacementIndex = 0;
        currentShipToPlace = Game.FLEET_CONFIGURATION.get(shipPlacementIndex);
        
        // No colocar barcos automáticamente, dejar que el usuario los coloque
        // Solo colocar barcos de la máquina
        // La máquina ya coloca sus barcos en el constructor de Game
    }

    /**
     * Actualiza la interfaz de colocación de barcos
     */
    private void updateShipPlacementUI() {
        if (shipToPlaceLabel != null) {
            if (isManualPlacementMode && currentShipToPlace != null) {
                shipToPlaceLabel.setText("Colocar: " + currentShipToPlace.getDisplayName() + 
                                       " (" + (shipPlacementIndex + 1) + "/" + Game.FLEET_CONFIGURATION.size() + ")");
            } else if (!isManualPlacementMode) {
                shipToPlaceLabel.setText("¡Todos los barcos colocados!");
            } else {
                shipToPlaceLabel.setText("Colocación completada");
            }
        }
        
        if (toggleOrientationButton != null) {
            toggleOrientationButton.setVisible(isManualPlacementMode);
            if (isManualPlacementMode) {
                toggleOrientationButton.setText("Orientación: " + 
                    (currentOrientation == Orientation.HORIZONTAL ? "Horizontal" : "Vertical"));
            }
        }
        
        if (autoPlaceButton != null) {
            autoPlaceButton.setVisible(isManualPlacementMode);
        }
    }

    /**
     * Cambia la orientación del barco a colocar
     */
    @FXML
    private void toggleOrientation() {
        if (isManualPlacementMode) {
            currentOrientation = (currentOrientation == Orientation.HORIZONTAL) ? 
                Orientation.VERTICAL : Orientation.HORIZONTAL;
            updateShipPlacementUI();
        }
    }

    /**
     * Coloca todos los barcos automáticamente
     */
    @FXML
    private void autoPlaceShips() {
        if (isManualPlacementMode) {
            // Colocar barcos restantes automáticamente
            while (shipPlacementIndex < Game.FLEET_CONFIGURATION.size()) {
                ShipType type = Game.FLEET_CONFIGURATION.get(shipPlacementIndex);
                Ship ship = createShip(type);
                if (ship != null) {
                    if (placeShipRandomly(ship, game.getHumanPlayer().getBoard())) {
                        shipPlacementIndex++;
                    } else {
                        updateStatusLabel("Error colocando barco automáticamente: " + type.getDisplayName());
                        break;
                    }
                }
            }
            
            if (shipPlacementIndex >= Game.FLEET_CONFIGURATION.size()) {
                isManualPlacementMode = false;
                currentShipToPlace = null;
                game.startGamePlay();
                updateGridFromGameState();
                updateShipPlacementUI();
                updateStatusLabel("¡Barcos colocados automáticamente! ¡Comienza la batalla!");
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
        
        // Actualizar apariencia si hay un juego cargado
        updateGridFromGameState();
    }

    /**
     * Actualiza la apariencia del grid basándose en el estado del juego cargado
     */
    private void updateGridFromGameState() {
        if (game == null) return;
        
        Board playerBoard = game.getHumanPlayer().getBoard();
        
        for (int row = 0; row < Board.SIZE; row++) {
            for (int col = 0; col < Board.SIZE; col++) {
                Button button = gridButtons[row][col];
                Cell cell = playerBoard.getCell(row, col);
                
                if (playerBoard.wasShot(row, col)) {
                    // La celda fue disparada, actualizar apariencia
                    if (cell.getShipPart() != null) {
                        // Hay un barco aquí
                        if (cell.getShipPart().isSunk()) {
                            button.setText("X");
                            button.setStyle("-fx-background-color: red; -fx-border-color: black;");
                        } else {
                            button.setText("X");
                            button.setStyle("-fx-background-color: orange; -fx-border-color: black;");
                        }
                    } else {
                        // Es agua
                        button.setText("○");
                        button.setStyle("-fx-background-color: lightblue; -fx-border-color: black;");
                    }
                    button.setDisable(true);
                } else if (cell.getShipPart() != null) {
                    // Hay un barco no disparado - mostrar como barco para el jugador
                    Ship ship = cell.getShipPart();
                    String shipSymbol = getShipSymbol(ship.getType());
                    button.setText(shipSymbol);
                    button.setStyle("-fx-background-color: darkgray; -fx-border-color: black; -fx-text-fill: white; -fx-font-weight: bold;");
                } else {
                    // Celda vacía
                    button.setText("");
                    button.setStyle("-fx-background-color: lightgray; -fx-border-color: black;");
                    if (isManualPlacementMode) {
                        button.setDisable(false); // Permitir clic para colocar barcos
                    }
                }
            }
        }
    }

    /**
     * Obtiene el símbolo visual para cada tipo de barco
     */
    private String getShipSymbol(ShipType type) {
        switch (type) {
            case AIRCRAFT_CARRIER: return "A";
            case SUBMARINE: return "S";
            case DESTROYER: return "D";
            case FRIGATE: return "F";
            default: return "■";
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
        if (isManualPlacementMode && game.getGameState() == GameState.SHIP_PLACEMENT) {
            // Modo de colocación de barcos
            handleShipPlacement(row, col);
        } else if (game.getGameState() != GameState.PLAYER_TURN) {
            return;
        } else {
            // Modo de disparo
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
    }

    private void handleShipPlacement(int row, int col) {
        if (currentShipToPlace == null) return;

        try {
            Ship ship = createShip(currentShipToPlace);
            if (ship == null) return;

            // Verificar si se puede colocar el barco
            if (game.getHumanPlayer().getBoard().canPlaceShip(ship, row, col, currentOrientation)) {
                game.placeHumanShip(ship, row, col, currentOrientation);
                
                System.out.println("Barco " + currentShipToPlace.getDisplayName() + 
                                 " colocado en (" + row + "," + col + ") " + currentOrientation);
                
                // Actualizar visualmente
                updateGridFromGameState();
                
                // Avanzar al siguiente barco
                shipPlacementIndex++;
                if (shipPlacementIndex < Game.FLEET_CONFIGURATION.size()) {
                    currentShipToPlace = Game.FLEET_CONFIGURATION.get(shipPlacementIndex);
                    updateStatusLabel();
                } else {
                    // Todos los barcos colocados
                    isManualPlacementMode = false;
                    currentShipToPlace = null;
                    game.startGamePlay();
                    updateStatusLabel("¡Todos los barcos colocados! ¡Comienza la batalla!");
                    
                    // Deshabilitar botones ya que no es el turno del jugador aún
                    disableGridForShooting();
                }
                
                updateShipPlacementUI();
                
            } else {
                updateStatusLabel("No puedes colocar el " + currentShipToPlace.getDisplayName() + 
                                " ahí. Verificar espacio y orientación.");
            }
        } catch (Exception e) {
            updateStatusLabel("Error colocando barco: " + e.getMessage());
            System.err.println("Error en colocación: " + e.getMessage());
        }
    }

    /**
     * Deshabilita el grid para los disparos (solo cuando no es turno del jugador)
     */
    private void disableGridForShooting() {
        for (int row = 0; row < Board.SIZE; row++) {
            for (int col = 0; col < Board.SIZE; col++) {
                if (!game.getHumanPlayer().getBoard().wasShot(row, col)) {
                    gridButtons[row][col].setDisable(game.getGameState() != GameState.PLAYER_TURN);
                }
            }
        }
    }

    /**
     * Habilita el grid para el turno del jugador
     */
    private void enableGridForPlayerTurn() {
        if (game.getGameState() == GameState.PLAYER_TURN) {
            for (int row = 0; row < Board.SIZE; row++) {
                for (int col = 0; col < Board.SIZE; col++) {
                    // Solo habilitar celdas que no han sido disparadas
                    if (!game.getHumanPlayer().getBoard().wasShot(row, col)) {
                        gridButtons[row][col].setDisable(false);
                    }
                }
            }
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
            } else {
                // Turno del jugador, habilitar grid
                enableGridForPlayerTurn();
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
            case ALREADY_SHOT:
                // No debería llegar aquí, pero por seguridad
                cell.setText("!");
                cell.setStyle("-fx-background-color: gray; -fx-border-color: black;");
                break;
            case INVALID_SHOT:
                // No debería llegar aquí, pero por seguridad
                cell.setText("?");
                cell.setStyle("-fx-background-color: darkgray; -fx-border-color: black;");
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
                case INITIALIZING:
                    status = "Initializing game...";
                    break;
                case SHIP_PLACEMENT:
                    if (isManualPlacementMode && currentShipToPlace != null) {
                        status = "Haz clic para colocar: " + currentShipToPlace.getDisplayName() + 
                               " (Orientación: " + (currentOrientation == Orientation.HORIZONTAL ? "Horizontal" : "Vertical") + ")";
                    } else {
                        status = "Placing ships...";
                    }
                    break;
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
            
            // Limpiar archivos de guardado cuando el juego termine
            game.deleteSaveFile();
        }
    }
}