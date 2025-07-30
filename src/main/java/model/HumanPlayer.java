package model;

public class HumanPlayer extends Player {
    private static final long serialVersionUID = 1L;

    public HumanPlayer(String name) {
        super(name);
    }

    @Override
    public void placeShips(){
        System.out.println(name + " está colocando sus barcos.");
    }

    @Override
    public int[] getNextShot(Board opponentBoard){
        throw new UnsupportedOperationException("El disparo del jugador humano es manejado por la interfaz.");
    }
}
