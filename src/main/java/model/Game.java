package model;

import model.exceptions.CellAlreadyShotException;
import model.exceptions.InvalidShipPlacementException;
import model.persistence.GameSerializer;
import model.persistence.ScorePersistence;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Main game controller class for the Battle Naval Fury game.
 * 
 * <p>This class manages the complete game state, including both human and machine
 * players, game flow, turn management, and ship placement validation. It serves
 * as the central coordinator for all game operations.</p>
 * 
 * <p>The game follows the classic Battleship rules with the following fleet configuration:</p>
 * <ul>
 *   <li>1 Aircraft Carrier (5 cells)</li>
 *   <li>2 Submarines (3 cells each)</li>
 *   <li>3 Destroyers (2 cells each)</li>
 *   <li>4 Frigates (1 cell each)</li>
 * </ul>
 * 
 * <p>The game supports automatic saving and loading of game states through
 * serialization mechanisms.</p>
 * 
 * @author Battle Naval Fury Team
 * @version 1.0
 * @since 1.0
 * @see Player
 * @see HumanPlayer
 * @see MachinePlayer
 * @see GameState
 */
public class Game implements Serializable {
    
    /** Serial version UID for serialization compatibility */
    private static final long serialVersionUID = 1L;

    /** The human player instance */
    private HumanPlayer humanPlayer;
    
    /** The machine/AI player instance */
    private MachinePlayer machinePlayer;
    
    /** Reference to the current active player */
    private Player currentPlayer;
    
    /** Current state of the game */
    private GameState gameState;
    
    /** Nickname of the human player */
    private String humanNickname;

    /**
     * Standard fleet configuration for both players.
     * Defines the types and quantities of ships available in the game.
     */
    public static final List<ShipType> FLEET_CONFIGURATION = List.of(
            ShipType.AIRCRAFT_CARRIER,
            ShipType.SUBMARINE, ShipType.SUBMARINE,
            ShipType.DESTROYER, ShipType.DESTROYER, ShipType.DESTROYER,
            ShipType.FRIGATE, ShipType.FRIGATE, ShipType.FRIGATE, ShipType.FRIGATE
    );

    /**
     * Creates a new game with the specified human player nickname.
     * 
     * <p>Initializes a new game instance with a human player and machine player.
     * The machine player automatically places its ships, while the human player
     * starts in ship placement phase.</p>
     * 
     * @param humanNickname the nickname for the human player
     * @throws IllegalArgumentException if humanNickname is null or empty
     */
    public Game(String humanNickname) {
        if (humanNickname == null || humanNickname.trim().isEmpty()) {
            throw new IllegalArgumentException("Human nickname cannot be null or empty");
        }
        
        this.humanNickname = humanNickname;
        this.humanPlayer = new HumanPlayer(humanNickname);
        this.machinePlayer = new MachinePlayer("Máquina");
        this.machinePlayer.placeShips();
        this.currentPlayer = humanPlayer;
        this.gameState = GameState.SHIP_PLACEMENT;
    }

    /**
     * Creates a game instance from existing game components (used for loading saved games).
     * 
     * @param human the human player instance
     * @param machine the machine player instance  
     * @param current the current active player
     * @param state the current game state
     * @param nickname the human player's nickname
     */
    public Game(HumanPlayer human, MachinePlayer machine, Player current, GameState state, String nickname) {
        this.humanPlayer = human;
        this.machinePlayer = machine;
        this.currentPlayer = current == humanPlayer ? this.humanPlayer : this.machinePlayer;
        this.gameState = state;
        this.humanNickname = nickname;
    }

    /**
     * Gets the human player instance.
     * 
     * @return the human player
     */
    public HumanPlayer getHumanPlayer() {
        return humanPlayer;
    }

    /**
     * Gets the machine player instance.
     * 
     * @return the machine player
     */
    public MachinePlayer getMachinePlayer() {
        return machinePlayer;
    }

    /**
     * Gets the currently active player.
     * 
     * @return the current player (either human or machine)
     */
    public Player getCurrentPlayer() {
        return currentPlayer;
    }

    /**
     * Gets the current game state.
     * 
     * @return the current game state
     * @see GameState
     */
    public GameState getGameState() {
        return gameState;
    }

    /**
     * Gets the human player's nickname.
     * 
     * @return the human player's nickname
     */
    public String getHumanNickname() {
        return humanNickname;
    }

    public void setHumanNickname(String nickname) {
        this.humanNickname = nickname;
        this.humanPlayer = new HumanPlayer(nickname);
    }

    public List<ShipType> getShipsToPlaceForHuman() {
        List<ShipType> placedTypes = new ArrayList<>();
        for (Ship ship : humanPlayer.getBoard().getShips()) {
            placedTypes.add(ship.getType());
        }

        List<ShipType> remainingToPlace = new ArrayList<>(FLEET_CONFIGURATION);
        for (ShipType placed : placedTypes) {
            remainingToPlace.remove(placed);
        }
        return remainingToPlace;
    }

    public boolean allHumanShipsPlaced() {
        return getShipsToPlaceForHuman().isEmpty();
    }

    public void placeHumanShip(Ship ship, int row, int col, Orientation orientation) throws InvalidShipPlacementException {
        if (gameState != GameState.SHIP_PLACEMENT) {
            throw new IllegalStateException("No se pueden colocar barcos fuera d ela fase de colocación.");
        }
        humanPlayer.getBoard().placeShip(ship, row, col, orientation);
        
        // Guardar después de colocar cada barco
        saveGameState();
    }

    public void startGamePlay() {
        if (gameState == GameState.SHIP_PLACEMENT && allHumanShipsPlaced()) {
            gameState = GameState.PLAYER_TURN;
            currentPlayer = humanPlayer;
        } else if (gameState != GameState.SHIP_PLACEMENT) {
            if (currentPlayer == humanPlayer) {
                gameState = GameState.PLAYER_TURN;
            } else {
                throw new IllegalStateException("No se pueden iniciar las jugadas hasta que todos los barcos estén colocados.");
            }
        }
    }

    public ShotResult processPlayerShot(int row, int col) throws CellAlreadyShotException {
        if (currentPlayer != humanPlayer || gameState != GameState.PLAYER_TURN) {
            throw new IllegalStateException("No es el turno del jugador humano o el juego no está en modo de disparo.");
        }

        ShotResult result = machinePlayer.getBoard().receiveShot(row, col);

        if (machinePlayer.getBoard().allShipsSunk()) {
            gameState = GameState.GAME_OVER_HUMAN_WINS;
        } else if (result == ShotResult.WATER) {
            currentPlayer = machinePlayer;
            gameState = GameState.MACHINE_TURN;
        }
        
        // Guardar automáticamente después de cada jugada del jugador
        saveGameState();
        
        return result;
    }

    public ShotResult processMachineShot() {
        if (currentPlayer != machinePlayer || gameState != GameState.MACHINE_TURN) {
            throw new IllegalStateException("No es el turno de la máquina o el juego no está en modo de disparo.");
        }

        int[] coords = machinePlayer.getNextShot(humanPlayer.getBoard());
        if (coords == null) {
            if (humanPlayer.getBoard().allShipsSunk()) {
                gameState = GameState.GAME_OVER_HUMAN_WINS;
            }
            return ShotResult.INVALID_SHOT;
        }
        ShotResult result;
        try {
            result = humanPlayer.getBoard().receiveShot(coords[0], coords[1]);
        } catch (CellAlreadyShotException e) {
            System.err.println("IA intentó disparar a celda ya disparada: " + e.getMessage());
            currentPlayer = humanPlayer;
            gameState = GameState.PLAYER_TURN;
            return ShotResult.ALREADY_SHOT;
        }

        if (humanPlayer.getBoard().allShipsSunk()) {
            gameState = GameState.GAME_OVER_MACHINE_WINS;
        } else if (result == ShotResult.WATER) {
            currentPlayer = humanPlayer;
            gameState = GameState.PLAYER_TURN;
        }

        // Guardar automáticamente después de cada jugada de la máquina
        saveGameState();

        return result;
    }

    public int getHumanShipsSunkByMachine() {
        return humanPlayer.getBoard().getSunkShipsCount();
    }

    public int getMachineShipsSunkByHuman() {
        return machinePlayer.getBoard().getSunkShipsCount();
    }

    /**
     * Guarda el estado actual del juego automáticamente
     */
    private void saveGameState() {
        try {
            GameSerializer.saveGame(this);
            ScorePersistence.saveGameInfo(this);
        } catch (Exception e) {
            System.err.println("Error en el guardado automático: " + e.getMessage());
        }
    }

    /**
     * Guarda el estado del juego manualmente (público)
     */
    public void saveGame() {
        saveGameState();
    }

    /**
     * Carga un juego guardado
     */
    public static Game loadGame() {
        return GameSerializer.loadGame();
    }

    /**
     * Verifica si hay un juego guardado
     */
    public static boolean hasSavedGame() {
        return GameSerializer.hasSavedGame();
    }

    /**
     * Elimina el archivo de guardado (cuando se termina un juego)
     */
    public void deleteSaveFile() {
        if (gameState == GameState.GAME_OVER_HUMAN_WINS || gameState == GameState.GAME_OVER_MACHINE_WINS) {
            GameSerializer.deleteSaveFile();
        }
    }

}
