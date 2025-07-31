package model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Represents a computer-controlled player in the naval battle game.
 * 
 * <p>The machine player implements AI strategies for both ship placement
 * and shot selection. Currently uses random algorithms for both actions,
 * providing a challenging but unpredictable opponent for human players.</p>
 * 
 * <p>Ship placement follows the standard fleet configuration:
 * 1 Aircraft Carrier, 2 Submarines, 3 Destroyers, and 4 Frigates.
 * Ships are placed randomly on valid positions with random orientations.</p>
 * 
 * @author Battle Naval Fury Team
 * @version 1.0
 * @since 1.0
 * @see Player
 * @see HumanPlayer
 * @see AI
 */
public class MachinePlayer extends Player {
    
    /**
     * Serial version UID for serialization compatibility.
     */
    private static final long serialVersionUID = 1L;
    
    /**
     * Random number generator for AI decision making.
     */
    private Random randomGenerator;

    /**
     * Constructs a new machine player with the specified name.
     * 
     * <p>Initializes the random number generator for AI decision making.</p>
     * 
     * @param name the name of the machine player
     */
    public MachinePlayer(String name) {
        super(name);
        this.randomGenerator = new Random();
    }

    /**
     * Automatically places all ships on the machine player's board.
     * 
     * <p>Uses a random placement strategy to position the standard fleet:
     * 1 Aircraft Carrier, 2 Submarines, 3 Destroyers, and 4 Frigates.
     * Ships are placed in random order with random orientations on
     * valid positions.</p>
     * 
     * <p>The method attempts up to 100 placements per ship to handle
     * cases where the board becomes crowded. If a ship cannot be placed
     * after 100 attempts, an error message is logged.</p>
     */
    @Override
    public void placeShips(){
        System.out.println(name + " está colocando sus barcos automáticamente.");
        List<ShipType> shipsToPlaceConfig = new ArrayList<>();
        // 1 portaaviones
        shipsToPlaceConfig.add(ShipType.AIRCRAFT_CARRIER);
        // 2 Submarinos
        shipsToPlaceConfig.add(ShipType.SUBMARINE);
        shipsToPlaceConfig.add(ShipType.SUBMARINE);
        // 3 destructores
        shipsToPlaceConfig.add(ShipType.DESTROYER);
        shipsToPlaceConfig.add(ShipType.DESTROYER);
        shipsToPlaceConfig.add(ShipType.DESTROYER);
        // 4 fragatas
        shipsToPlaceConfig.add(ShipType.FRIGATE);
        shipsToPlaceConfig.add(ShipType.FRIGATE);
        shipsToPlaceConfig.add(ShipType.FRIGATE);
        shipsToPlaceConfig.add(ShipType.FRIGATE);

        Collections.shuffle(shipsToPlaceConfig);

        for (ShipType type : shipsToPlaceConfig) {
            Ship ship;

            switch (type) {
                case AIRCRAFT_CARRIER: ship = new AircraftCarrier(); break;
                case SUBMARINE: ship = new Submarine(); break;
                case DESTROYER: ship = new Destroyer(); break;
                case FRIGATE: ship = new Frigate(); break;
                default: continue;
            }

            boolean placed = false;
            int attempts = 0;

            while (!placed && attempts < 100) {
                int row = randomGenerator.nextInt(Board.SIZE);
                int col = randomGenerator.nextInt(Board.SIZE);
                Orientation orientation = randomGenerator.nextBoolean() ? Orientation.HORIZONTAL : Orientation.VERTICAL;

                try {
                    if (board.canPlaceShip(ship, col, row, orientation)) {
                        board.placeShip(ship, row, col, orientation);
                        placed = true;
                        System.out.println("IA colocó " + type.getDisplayName() + " en (" + row + "," + col + ") " + orientation);
                    }
                } catch (Exception e) {
                    System.err.println("Error colocando barco: " + e.getMessage());
                }
                attempts++;
            }

            if (!placed) {
                System.err.println("IA no pudo colocar: " + type.getDisplayName() + ". El tablero podría estar muy lleno.");
            }
        }
    }

    /**
     * Determines the next shot coordinates using random targeting.
     * 
     * <p>Scans the opponent's board for cells that have not been shot at
     * and randomly selects one as the target. This provides unpredictable
     * but fair AI behavior.</p>
     * 
     * @param opponentBoard the opponent's board to analyze
     * @return array containing [row, col] coordinates for the shot,
     *         or null if no valid targets remain
     */
    @Override
    public int[] getNextShot(Board opponentBoard) {
        List<Cell> availableCells = new ArrayList<>();
        for (int i = 0; i < Board.SIZE; i++) {
            for (int j = 0; j < Board.SIZE; j++) {
                if (!opponentBoard.getCell(i, j).isShot()){
                    availableCells.add(opponentBoard.getCell(i, j));
                }
            }
        }
        if (availableCells.isEmpty()){
            return null;
        }

        Cell targetCell = availableCells.get(randomGenerator.nextInt(availableCells.size()));
        return new int[]{targetCell.getRow(), targetCell.getCol()};
    }
}