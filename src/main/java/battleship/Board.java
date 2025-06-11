package battleship;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Board {
    private Ship[][] grid;
    private boolean[][] shots;
    private Set<Ship> ships;
    private Set<Ship> revealedShips;
    private static final int BOARD_SIZE = 10;
    private int shotCount;

    public Board() {
        grid = new Ship[BOARD_SIZE][BOARD_SIZE];
        shots = new boolean[BOARD_SIZE][BOARD_SIZE];
        ships = new HashSet<>();
        revealedShips = new HashSet<>();
        shotCount = 0;
    }

    public boolean placeShip(Ship ship, int row, int col, boolean horizontal) {
        if (!isValidPlacement(ship, row, col, horizontal)) {
            return false;
        }

        ship.place(row, col, horizontal);
        if (horizontal) {
            for (int c = col; c < col + ship.getLength(); c++) {
                grid[row][c] = ship;
            }
        } else {
            for (int r = row; r < row + ship.getLength(); r++) {
                grid[r][col] = ship;
            }
        }
        ships.add(ship);
        return true;
    }

    public boolean isValidPlacement(Ship ship, int row, int col, boolean horizontal) {
        if (!isValidPosition(row, col)) {
            return false;
        }

        int length = ship.getLength();
        if (horizontal) {
            if (col + length > BOARD_SIZE) {
                return false;
            }
            for (int c = col; c < col + length; c++) {
                if (grid[row][c] != null) {
                    return false;
                }
            }
        } else {
            if (row + length > BOARD_SIZE) {
                return false;
            }
            for (int r = row; r < row + length; r++) {
                if (grid[r][col] != null) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean shoot(int row, int col) {
        if (row < 0 || row >= 10 || col < 0 || col >= 10) {
            return false;
        }
        
        if (!shots[row][col]) {
            shots[row][col] = true;
            shotCount++;
            Ship ship = grid[row][col];
            if (ship != null) {
                ship.isHit(row, col);
            }
            return true;
        }
        return false;
    }

    public Ship getShipAt(int row, int col) {
        if (!isValidPosition(row, col)) {
            return null;
        }
        return grid[row][col];
    }

    public boolean isValidPosition(int row, int col) {
        return row >= 0 && row < BOARD_SIZE && col >= 0 && col < BOARD_SIZE;
    }

    public boolean allShipsSunk() {
        for (Ship ship : ships) {
            if (!ship.isSunk()) {
                return false;
            }
        }
        return true;
    }

    public List<Ship> getShips() {
        return new ArrayList<>(ships);
    }

    public void revealShip(Ship ship) {
        if (ship != null && ships.contains(ship)) {
            revealedShips.add(ship);
        }
    }

    public boolean isShipRevealed(Ship ship) {
        return ship != null && revealedShips.contains(ship);
    }

    public void clearRevealedShips() {
        revealedShips.clear();
    }

    public boolean hasBeenShot(int row, int col) {
        return shots[row][col];
    }

    public int getShotCount() {
        return shotCount;
    }
}