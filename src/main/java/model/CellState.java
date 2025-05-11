package model;

import java.io.Serializable;

public enum CellState implements Serializable {
    EMPTY, SHIP_PART, WATER, HIT_SHIP_PART, SUNK_SHIP_PART
}
