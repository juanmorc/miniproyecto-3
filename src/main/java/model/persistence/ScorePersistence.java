package model.persistence;

import model.Game;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Handles persistence of game information to human-readable text files.
 * 
 * <p>This class provides functionality to save and load game statistics
 * and information in plain text format, making it easy to read and debug
 * game state information.</p>
 * 
 * <p>The information includes player nicknames, game state, statistics,
 * and progress details saved with timestamps.</p>
 * 
 * @author Battle Naval Fury Team
 * @version 1.0
 * @since 1.0
 * @see GameSerializer
 */
public class ScorePersistence {
    
    /** Directory name where save files are stored */
    private static final String SAVE_DIRECTORY = "game_saves";
    
    /** Filename for the game information text file */
    private static final String SCORE_FILE = "game_info.txt";
    
    /** Complete path to the game information file */
    private static final String SCORE_PATH = SAVE_DIRECTORY + File.separator + SCORE_FILE;

    /**
     * Saves comprehensive game information to a human-readable text file.
     * 
     * <p>This method creates a detailed report of the current game state including:</p>
     * <ul>
     *   <li>Save timestamp</li>
     *   <li>Player nickname and current turn</li>
     *   <li>Game state and statistics</li>
     *   <li>Ships count and progress information</li>
     * </ul>
     * 
     * <p>The save directory is created automatically if it doesn't exist.
     * Any existing information file is overwritten.</p>
     * 
     * @param game the Game object whose information will be saved
     * @throws IllegalArgumentException if game is null
     * @see #loadGameInfo()
     * @see #hasGameInfo()
     */
    public static void saveGameInfo(Game game) {
        if (game == null) {
            throw new IllegalArgumentException("Game cannot be null");
        }
        
        try {
            // Crear directorio si no existe
            Path saveDir = Paths.get(SAVE_DIRECTORY);
            if (!Files.exists(saveDir)) {
                Files.createDirectories(saveDir);
            }

            try (PrintWriter writer = new PrintWriter(new FileWriter(SCORE_PATH))) {
                writer.println("=== ESTADO DEL JUEGO ===");
                writer.println("Fecha de guardado: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                writer.println("Nickname del jugador: " + game.getHumanNickname());
                writer.println("Estado actual: " + game.getGameState().toString());
                writer.println("Turno actual: " + game.getCurrentPlayer().getName());
                writer.println();
                
                writer.println("=== ESTADÍSTICAS ===");
                writer.println("Barcos hundidos por el jugador: " + game.getMachineShipsSunkByHuman());
                writer.println("Barcos hundidos por la máquina: " + game.getHumanShipsSunkByMachine());
                writer.println("Total de barcos por jugador: " + game.getHumanPlayer().getBoard().getShips().size());
                writer.println("Total de barcos de la máquina: " + game.getMachinePlayer().getBoard().getShips().size());
                writer.println();

                writer.println("=== PROGRESO DEL JUEGO ===");
                if (game.getGameState().toString().contains("GAME_OVER")) {
                    writer.println("Juego terminado");
                    if (game.getGameState().toString().contains("HUMAN_WINS")) {
                        writer.println("Ganador: " + game.getHumanNickname());
                    } else {
                        writer.println("Ganador: Máquina");
                    }
                } else {
                    writer.println("Juego en progreso");
                    if (game.allHumanShipsPlaced()) {
                        writer.println("Todos los barcos del jugador están colocados");
                    } else {
                        writer.println("Barcos restantes por colocar: " + game.getShipsToPlaceForHuman().size());
                    }
                }
                
                System.out.println("Información del juego guardada en: " + SCORE_PATH);
            }
        } catch (IOException e) {
            System.err.println("Error guardando información del juego: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Loads game information from the text file.
     * 
     * <p>Reads the complete content of the game information file
     * and returns it as a formatted string.</p>
     * 
     * @return the complete game information as a string, or an error message
     *         if the file doesn't exist or cannot be read
     * @see #saveGameInfo(Game)
     * @see #hasGameInfo()
     */
    public static String loadGameInfo() {
        File scoreFile = new File(SCORE_PATH);
        if (!scoreFile.exists()) {
            return "No hay información de juego guardada.";
        }

        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(SCORE_PATH))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        } catch (IOException e) {
            System.err.println("Error leyendo información del juego: " + e.getMessage());
            return "Error al cargar información del juego.";
        }
        
        return content.toString();
    }

    /**
     * Checks if a game information file exists.
     * 
     * @return true if the game information file exists, false otherwise
     * @see #saveGameInfo(Game)
     * @see #loadGameInfo()
     */
    public static boolean hasGameInfo() {
        return new File(SCORE_PATH).exists();
    }
}
