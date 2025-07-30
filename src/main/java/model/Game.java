package model;

import model.exceptions.CellAlreadyShotException;
import model.exceptions.InvalidShipPlacementException;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Game implements Serializable {
    private static final long serialVersionUID = 1L;

    private HumanPlayer humanPlayer;
    private MachinePlayer machinePlayer;
    private Player currentPlayer;
    private GameState gameState;
    private String humanNickname;

    public static final List<ShipType> FLEET_CONFIGURATION = List.of(
            ShipType.AIRCRAFT_CARRIER,
            ShipType.SUBMARINE, ShipType.SUBMARINE,
            ShipType.DESTROYER, ShipType.DESTROYER, ShipType.DESTROYER,
            ShipType.FRIGATE, ShipType.FRIGATE, ShipType.FRIGATE, ShipType.FRIGATE
    );

    public Game(String humanNickname) {
        this.humanNickname = humanNickname;
        this.humanPlayer = new HumanPlayer(humanNickname);
        this.machinePlayer = new MachinePlayer("Máquina");
        this.machinePlayer.placeShips();
        this.currentPlayer = humanPlayer;
        this.gameState = GameState.SHIP_PLACEMENT;
    }

    public Game(HumanPlayer human, MachinePlayer machine, Player current, GameState state, String nickname) {
        this.humanPlayer = human;
        this.machinePlayer = machine;
        this.currentPlayer = current == humanPlayer ? this.humanPlayer : this.machinePlayer;
        this.gameState = state;
        this.humanNickname = nickname;
    }

    public HumanPlayer getHumanPlayer() {
        return humanPlayer;
    }

    public MachinePlayer getMachinePlayer() {
        return machinePlayer;
    }

    public Player getCurrentPlayer() {
        return currentPlayer;
    }

    public GameState getGameState() {
        return gameState;
    }

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

        return result;
    }

    public int getHumanShipsSunkByMachine() {
        return humanPlayer.getBoard().getSunkShipsCount();
    }

    public int getMachineShipsSunkByHuman() {
        return machinePlayer.getBoard().getSunkShipsCount();
    }

}
