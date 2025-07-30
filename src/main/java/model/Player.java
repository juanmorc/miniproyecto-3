package model;

import java.io.Serializable;

public abstract class Player implements Serializable {
    private static final long serialVersionUID = 1L;
    protected String name;
    protected Board board;

    public Player(String name) {
        this.name = name;
        this.board = new Board();
    }

    public String getName() {
        return name;
    }

    public Board getBoard() {
        return board;
    }

    public abstract void placeShips();

    public abstract int[] getNextShot(Board opponentBoard);
}
