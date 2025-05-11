package model;

public enum ShipType {
    AIRCRAFT_CARRIER(4, "Portaaviones"),
    SUBMARINE(3, "Submarino"),
    DESTROYER(2, "Destructor"),
    FRIGATE(1, "Fragata");

    private final int size;
    private final String DisplayName;

    ShipType(int size, String DisplayName) {
        this.size = size;
        this.DisplayName = DisplayName;
    }

    public int getSize() {
        return size;
    }

    public String getDisplayName() {
        return DisplayName;
    }
}
