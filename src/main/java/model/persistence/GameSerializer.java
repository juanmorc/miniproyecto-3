package model.persistence;

import model.Game;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Handles serialization and deserialization of Game objects for persistence.
 * 
 * <p>This class provides static methods to save and load game states to/from
 * binary files using Java serialization. It automatically manages the save
 * directory structure and handles file operations.</p>
 * 
 * <p>The serialized games are saved in a "game_saves" directory with the
 * filename "current_game.ser".</p>
 * 
 * @author Battle Naval Fury Team
 * @version 1.0
 * @since 1.0
 */
public class GameSerializer {
    
    /** Directory name where save files are stored */
    private static final String SAVE_DIRECTORY = "game_saves";
    
    /** Filename for the serialized game file */
    private static final String GAME_FILE = "current_game.ser";
    
    /** Complete path to the game save file */
    private static final String GAME_PATH = SAVE_DIRECTORY + File.separator + GAME_FILE;

    /**
     * Saves the current game state to a serialized file.
     * 
     * <p>This method serializes the entire Game object to a binary file,
     * preserving all game state including board configurations, player
     * information, and current game progress.</p>
     * 
     * <p>The save directory is created automatically if it doesn't exist.
     * Any existing save file is overwritten.</p>
     * 
     * @param game the Game object to be saved
     * @throws IllegalArgumentException if game is null
     * @see #loadGame()
     * @see #hasSavedGame()
     */
    public static void saveGame(Game game) {
        if (game == null) {
            throw new IllegalArgumentException("Game cannot be null");
        }
        
        try {
            // Crear directorio si no existe
            Path saveDir = Paths.get(SAVE_DIRECTORY);
            if (!Files.exists(saveDir)) {
                Files.createDirectories(saveDir);
            }

            // Serializar el objeto Game
            try (FileOutputStream fileOut = new FileOutputStream(GAME_PATH);
                 ObjectOutputStream objectOut = new ObjectOutputStream(fileOut)) {
                
                objectOut.writeObject(game);
                System.out.println("Juego guardado exitosamente en: " + GAME_PATH);
            }
        } catch (IOException e) {
            System.err.println("Error guardando el juego: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Loads a previously saved game from the serialized file.
     * 
     * <p>This method deserializes a Game object from the save file,
     * restoring the complete game state including all board configurations,
     * player data, and game progress.</p>
     * 
     * @return the loaded Game object, or null if no save file exists or 
     *         if loading fails
     * @see #saveGame(Game)
     * @see #hasSavedGame()
     */
    public static Game loadGame() {
        File gameFile = new File(GAME_PATH);
        if (!gameFile.exists()) {
            System.out.println("No se encontró archivo de guardado previo.");
            return null;
        }

        try (FileInputStream fileIn = new FileInputStream(GAME_PATH);
             ObjectInputStream objectIn = new ObjectInputStream(fileIn)) {
            
            Game game = (Game) objectIn.readObject();
            System.out.println("Juego cargado exitosamente desde: " + GAME_PATH);
            return game;
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error cargando el juego: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Checks if a saved game file exists.
     * 
     * @return true if a save file exists, false otherwise
     * @see #saveGame(Game)
     * @see #loadGame()
     */
    public static boolean hasSavedGame() {
        return new File(GAME_PATH).exists();
    }

    /**
     * Deletes the current save file if it exists.
     * 
     * <p>This method is typically called when a game ends to clean up
     * save files and prevent loading of completed games.</p>
     * 
     * @see #saveGame(Game)
     */
    public static void deleteSaveFile() {
        File gameFile = new File(GAME_PATH);
        if (gameFile.exists()) {
            if (gameFile.delete()) {
                System.out.println("Archivo de guardado eliminado.");
            } else {
                System.err.println("No se pudo eliminar el archivo de guardado.");
            }
        }
    }
}
