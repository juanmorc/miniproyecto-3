package model;

import java.io.Serializable;

public class Cell implements Serializable {
    private static final long serialVersionUID = 1L;

    private int row;
    private int col;
    private CellState cellState;
    private Ship shipPart;

    public Cell(int row, int col) {
        this.row = row;
        this.col = col;
        this.cellState = CellState.EMPTY;
        this.shipPart = null;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public CellState getCellState() {
        return cellState;
    }

    public Ship getShipPart() {
        return shipPart;
    }

    public void setShipPart(Ship shipPart) {
        this.shipPart = shipPart;
    }

    public void setCellState(CellState cellState) {
        this.cellState = cellState;
    }

    boolean isShot(){
        return cellState == CellState.WATER || cellState == CellState.HIT_SHIP_PART || cellState == CellState.SUNK_SHIP_PART;
    }

    @Override
    public String toString() {
        return "(" + row + "," + col + ") " + cellState + (shipPart != null ? " [" + shipPart.getType().getDisplayName() + "]" : "");
    }
}
