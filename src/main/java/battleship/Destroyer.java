package battleship;

public class Destroyer extends Ship {
    public Destroyer() {
        super("Destroyer", 2);
    }

    @Override
    public void performAbility(Board targetBoard, int row, int col) {
        // Ability: Deals double damage to the hit location
        Ship hitShip = targetBoard.getShipAt(row, col);
        if (hitShip != null) {
            targetBoard.shoot(row, col); // Shoot again at the same spot
            System.out.println("Destroyer special ability: Double damage dealt!");
        }
    }
} 