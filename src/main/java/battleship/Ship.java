package battleship;

public abstract class Ship {
    protected int length;
    protected boolean[] hits;
    private String name;
    private boolean isHorizontal;
    private boolean sunk;
    private int row;
    private int col;

    public Ship(String name, int length) {
        this.name = name;
        this.length = length;
        this.hits = new boolean[length];
        this.isHorizontal = true;
        this.sunk = false;
        this.row = -1;
        this.col = -1;
    }

    public void place(int row, int col, boolean isHorizontal) {
        this.row = row;
        this.col = col;
        this.isHorizontal = isHorizontal;
    }

    public boolean isHit(int row, int col) {
        if (isHorizontal) {
            if (this.row != row || col < this.col || col >= this.col + length) {
                return false;
            }
            hits[col - this.col] = true;
        } else {
            if (this.col != col || row < this.row || row >= this.row + length) {
                return false;
            }
            hits[row - this.row] = true;
        }
        
        // Check if ship is now sunk
        boolean allHit = true;
        for (boolean hit : hits) {
            if (!hit) {
                allHit = false;
                break;
            }
        }
        if (allHit) {
            setSunk(true);
        }
        
        return true;
    }

    // Method to handle special ability when this ship hits another ship
    public abstract void performAbility(Board targetBoard, int row, int col);

    public boolean isSunk() {
        // Check if all parts are hit
        for (boolean hit : hits) {
            if (!hit) {
                return false;
            }
        }
        return true;
    }

    public void setSunk(boolean sunk) {
        this.sunk = sunk;
    }

    public String getName() {
        return name;
    }

    public int getLength() {
        return length;
    }

    public boolean isHorizontal() {
        return isHorizontal;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public void setHorizontal(boolean horizontal) {
        this.isHorizontal = horizontal;
    }

    public void toggleOrientation() {
        this.isHorizontal = !this.isHorizontal;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public void setCol(int col) {
        this.col = col;
    }
} 