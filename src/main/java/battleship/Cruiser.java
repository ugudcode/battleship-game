package battleship;

public class Cruiser extends Ship {
    public Cruiser() {
        super("Cruiser", 3);
    }

    @Override
    public void performAbility(Board targetBoard, int row, int col) {
        // Ability: Attack a 2x2 area centered on the hit location
        System.out.println("Cruiser special ability: Bombarding 2x2 area!");
        
        // Calculate the top-left corner of the 2x2 area
        int startRow = row;
        int startCol = col;
        
        // If we're at the bottom or right edge, shift the area to fit on the board
        if (row == 9) startRow--;
        if (col == 9) startCol--;
        
        // Attack all cells in the 2x2 area
        for (int r = startRow; r < startRow + 2; r++) {
            for (int c = startCol; c < startCol + 2; c++) {
                if (r != row || c != col) { // Skip the original hit location
                    targetBoard.shoot(r, c);
                    Ship hitShip = targetBoard.getShipAt(r, c);
                    if (hitShip != null) {
                        System.out.println("💥 Additional hit on " + hitShip.getName() + " at position " + 
                            (char)('A' + c) + (r + 1) + "!");
                    }
                }
            }
        }
    }
} 