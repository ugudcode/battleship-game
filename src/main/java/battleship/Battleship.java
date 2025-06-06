package battleship;

public class Battleship extends Ship {
    public Battleship() {
        super("Battleship", 4);
    }

    @Override
    public void performAbility(Board targetBoard, int row, int col) {
        // Ability: If it hits a ship, destroy the entire ship
        Ship hitShip = targetBoard.getShipAt(row, col);
        if (hitShip != null) {
            // Mark all parts of the hit ship as hit
            if (hitShip.isHorizontal()) {
                for (int c = hitShip.getCol(); c < hitShip.getCol() + hitShip.getLength(); c++) {
                    targetBoard.shoot(row, c);
                }
            } else {
                for (int r = hitShip.getRow(); r < hitShip.getRow() + hitShip.getLength(); r++) {
                    targetBoard.shoot(r, col);
                }
            }
            System.out.println("Battleship special ability: Entire enemy ship destroyed!");
        }
    }
} 