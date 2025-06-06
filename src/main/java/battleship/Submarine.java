package battleship;

import java.util.List;
import java.util.Random;

public class Submarine extends Ship {
    public Submarine() {
        super("Submarine", 3);
    }

    @Override
    public void performAbility(Board targetBoard, int row, int col) {
        // Get all non-revealed ships from the enemy board
        List<Ship> enemyShips = targetBoard.getShips();
        List<Ship> hiddenShips = enemyShips.stream()
            .filter(ship -> !ship.isSunk())
            .toList();

        if (!hiddenShips.isEmpty()) {
            // Select a random non-revealed ship
            Random random = new Random();
            Ship revealedShip = hiddenShips.get(random.nextInt(hiddenShips.size()));
            
            // Mark the ship as temporarily revealed
            targetBoard.revealShip(revealedShip);
            
            System.out.println("Submarine special ability: Sonar detected enemy " + 
                revealedShip.getName() + " at position " + 
                (char)('A' + revealedShip.getCol()) + (revealedShip.getRow() + 1) + "!");
        }
    }
} 