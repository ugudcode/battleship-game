package battleship;

public class Carrier extends Ship {
    private boolean targetRow;  // true for row targeting, false for column targeting

    public Carrier() {
        super("Carrier", 5);
        this.targetRow = true;  // default to row targeting
    }

    public void setTargetRow(boolean targetRow) {
        this.targetRow = targetRow;
    }

    public boolean isTargetingRow() {
        return targetRow;
    }

    @Override
    public void performAbility(Board targetBoard, int row, int col) {
        if (targetRow) {
            System.out.println("Carrier special ability: Targeting entire row " + row + "!");
            // Attack entire row except the original hit location
            for (int c = 0; c < 10; c++) {
                if (!targetBoard.hasBeenShot(row, c)) {
                    targetBoard.shoot(row, c);
                }
            }
        } else {
            System.out.println("Carrier special ability: Targeting entire column " + col + "!");
            // Attack entire column except the original hit location
            for (int r = 0; r < 10; r++) {
                if (!targetBoard.hasBeenShot(r, col)) {
                    targetBoard.shoot(r, col);
                }
            }
        }
    }
} 