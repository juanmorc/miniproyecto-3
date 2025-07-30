package model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class MachinePlayer extends Player {
    private static final long serialVersionUID = 1L;
    private Random randomGenerator;

    public MachinePlayer(String name) {
        super(name);
        this.randomGenerator = new Random();
    }

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