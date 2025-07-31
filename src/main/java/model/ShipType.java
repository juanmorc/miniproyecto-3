package model;

/**
 * Enumeration representing the different types of ships in the naval battle game.
 * 
 * <p>Each ship type has a specific size (number of cells it occupies) and
 * a display name for user interface purposes. The ships available are:</p>
 * <ul>
 *   <li>Aircraft Carrier - 4 cells</li>
 *   <li>Submarine - 3 cells</li>
 *   <li>Destroyer - 2 cells</li>
 *   <li>Frigate - 1 cell</li>
 * </ul>
 * 
 * @author Battle Naval Fury Team
 * @version 1.0
 * @since 1.0
 * @see Ship
 * @see Board
 */
public enum ShipType {
    
    /**
     * Aircraft Carrier - the largest ship with 4 cells.
     */
    AIRCRAFT_CARRIER(4, "Portaaviones"),
    
    /**
     * Submarine - medium-large ship with 3 cells.
     */
    SUBMARINE(3, "Submarino"),
    
    /**
     * Destroyer - medium ship with 2 cells.
     */
    DESTROYER(2, "Destructor"),
    
    /**
     * Frigate - the smallest ship with 1 cell.
     */
    FRIGATE(1, "Fragata");

    /**
     * The number of cells this ship type occupies.
     */
    private final int size;
    
    /**
     * The display name for this ship type.
     */
    private final String DisplayName;

    /**
     * Constructs a ship type with the specified size and display name.
     * 
     * @param size the number of cells this ship type occupies
     * @param DisplayName the display name for this ship type
     */
    ShipType(int size, String DisplayName) {
        this.size = size;
        this.DisplayName = DisplayName;
    }

    /**
     * Gets the size of this ship type.
     * 
     * @return the number of cells this ship type occupies
     */
    public int getSize() {
        return size;
    }

    /**
     * Gets the display name of this ship type.
     * 
     * @return the localized display name
     */
    public String getDisplayName() {
        return DisplayName;
    }
}
