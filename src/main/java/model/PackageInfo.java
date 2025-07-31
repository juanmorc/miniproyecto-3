package model;

/**
 * Battle Naval Fury - A JavaFX implementation of the classic Battleship game.
 * 
 * <h2>Overview</h2>
 * <p>Battle Naval Fury is a complete implementation of the traditional Battleship
 * game, featuring both human vs. computer gameplay and a rich graphical interface.
 * The game includes manual ship placement, intelligent AI opponent, automatic
 * game saving/loading, and comprehensive game state management.</p>
 * 
 * <h2>Game Features</h2>
 * <ul>
 *   <li><strong>Manual Ship Placement:</strong> Players can manually place ships
 *       with click-to-place interface and orientation selection</li>
 *   <li><strong>Automatic Ship Placement:</strong> Optional automatic placement
 *       for quick game setup</li>
 *   <li><strong>AI Opponent:</strong> Intelligent machine player with random
 *       ship placement and strategic shooting</li>
 *   <li><strong>Game Persistence:</strong> Automatic saving and loading of
 *       game states using serialization</li>
 *   <li><strong>Visual Interface:</strong> Rich JavaFX interface with board
 *       visualization and game controls</li>
 *   <li><strong>Turn Management:</strong> Proper turn-based gameplay with
 *       visual feedback</li>
 * </ul>
 * 
 * <h2>Fleet Configuration</h2>
 * <p>Each player controls a fleet consisting of:</p>
 * <ul>
 *   <li>1 Aircraft Carrier (5 cells)</li>
 *   <li>2 Submarines (3 cells each)</li>
 *   <li>3 Destroyers (2 cells each)</li>
 *   <li>4 Frigates (1 cell each)</li>
 * </ul>
 * 
 * <h2>Package Structure</h2>
 * <ul>
 *   <li><strong>model:</strong> Core game logic, entities, and business rules</li>
 *   <li><strong>model.exceptions:</strong> Custom exceptions for game-specific errors</li>
 *   <li><strong>model.persistence:</strong> Game saving and loading functionality</li>
 *   <li><strong>controller:</strong> JavaFX controllers managing UI interactions</li>
 *   <li><strong>view:</strong> JavaFX view components and layouts</li>
 *   <li><strong>application:</strong> Main application entry point</li>
 * </ul>
 * 
 * <h2>Key Classes</h2>
 * <ul>
 *   <li>{@link Game} - Main game controller and state manager</li>
 *   <li>{@link Board} - Game board with ship placement and shot tracking</li>
 *   <li>{@link Player} - Abstract base class for human and AI players</li>
 *   <li>{@link Ship} - Abstract base class for all ship types</li>
 *   <li>{@link Cell} - Individual board cell with state management</li>
 * </ul>
 * 
 * <h2>Game Flow</h2>
 * <ol>
 *   <li><strong>Initialization:</strong> Create players and empty boards</li>
 *   <li><strong>Ship Placement:</strong> Human places ships manually or automatically,
 *       AI places ships randomly</li>
 *   <li><strong>Battle Phase:</strong> Turn-based shooting until one player wins</li>
 *   <li><strong>Game End:</strong> Victory condition reached, game state cleaned up</li>
 * </ol>
 * 
 * <h2>Technical Features</h2>
 * <ul>
 *   <li><strong>Serialization:</strong> Complete game state persistence</li>
 *   <li><strong>Exception Handling:</strong> Robust error management</li>
 *   <li><strong>Design Patterns:</strong> Observer pattern for UI updates,
 *       Strategy pattern for AI behavior</li>
 *   <li><strong>Thread Safety:</strong> Proper handling of UI and background tasks</li>
 * </ul>
 * 
 * @author Battle Naval Fury Development Team
 * @version 1.0
 * @since 1.0
 */
public final class PackageInfo {
    
    /**
     * Private constructor to prevent instantiation of utility class.
     */
    private PackageInfo() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }
    
    /**
     * Gets the current version of the Battle Naval Fury game.
     * 
     * @return the game version as a string
     */
    public static String getVersion() {
        return "1.0";
    }
    
    /**
     * Gets the game name.
     * 
     * @return the name of the game
     */
    public static String getGameName() {
        return "Battle Naval Fury";
    }
    
    /**
     * Gets information about the development team.
     * 
     * @return development team information
     */
    public static String getTeamInfo() {
        return "Battle Naval Fury Development Team - 2025";
    }
}
