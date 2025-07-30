package model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public abstract class Ship implements Serializable {
    private static final long serialVersionUID = 1L;

    protected ShipType type;
    protected int size;
    protected int hits;
    protected boolean sunk;
    protected List<Cell> occupiedCells;
    protected Orientation orientation;

    public Ship(ShipType type){
        this.type = type;
        this.size = type.getSize();
        this.hits = 0;
        this.sunk = false;
        this.occupiedCells = new ArrayList<Cell>();
    }

    public ShipType getType() {
        return type;
    }

    public int getSize() {
        return size;
    }

    public int getHits() {
        return hits;
    }

    public boolean isSunk() {
        return sunk;
    }

    public List<Cell> getOccupiedCells() {
        return occupiedCells;
    }

    public Orientation getOrientation() {
        return orientation;
    }

    public void setOrientation(Orientation orientation) {
        this.orientation = orientation;
    }

    public void addOccupiedCell(Cell cell) {
        this.occupiedCells.add(cell);
        cell.setShipPart(this);
        cell.setCellState(CellState.SHIP_PART);
    }

    public boolean registerHit() {
        if(!sunk){
            hits++;
            if (hits >= size){
                sunk = true;
                for (Cell cell : occupiedCells){
                    cell.setCellState(CellState.SUNK_SHIP_PART);
                }
                return true;
            }
        }
        return false;
    }

    public void reset(){
        this.hits = 0;
        this.sunk = false;
    }
}
