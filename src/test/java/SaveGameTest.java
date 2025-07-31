import model.*;
import model.persistence.GameSerializer;
import model.persistence.ScorePersistence;

/**
 * Test simple para verificar el guardado automático
 */
public class SaveGameTest {
    public static void main(String[] args) {
        System.out.println("=== TEST DE GUARDADO AUTOMÁTICO ===");
        
        // Crear un juego nuevo
        Game game = new Game("TestPlayer");
        System.out.println("Juego creado para: " + game.getHumanNickname());
        
        // Simular colocación automática de barcos del jugador
        // Colocar un barco manualmente para test
        try {
            Ship frigate = new Frigate();
            game.placeHumanShip(frigate, 0, 0, Orientation.HORIZONTAL);
            System.out.println("Barco colocado - debería haber guardado automáticamente");
        } catch (Exception e) {
            System.err.println("Error colocando barco: " + e.getMessage());
        }
        
        // Verificar que se guardó
        boolean hasSaved = GameSerializer.hasSavedGame();
        System.out.println("¿Hay archivo guardado?: " + hasSaved);
        
        if (hasSaved) {
            // Cargar el juego
            Game loadedGame = GameSerializer.loadGame();
            if (loadedGame != null) {
                System.out.println("Juego cargado exitosamente!");
                System.out.println("Nickname cargado: " + loadedGame.getHumanNickname());
                System.out.println("Estado: " + loadedGame.getGameState());
                System.out.println("Barcos del jugador: " + loadedGame.getHumanPlayer().getBoard().getShips().size());
            }
            
            // Mostrar información guardada
            String gameInfo = ScorePersistence.loadGameInfo();
            System.out.println("\n=== INFORMACIÓN GUARDADA ===");
            System.out.println(gameInfo);
        }
        
        System.out.println("=== FIN DEL TEST ===");
    }
}
