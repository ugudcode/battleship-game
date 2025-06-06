package battleship;

import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import java.util.List;
import java.util.ArrayList;

public class BattleshipGame {
    private final Board playerBoard;
    private final Board computerBoard;
    private final Scanner scanner;
    private final Konami cheats;

    public BattleshipGame() {
        playerBoard = new Board();
        computerBoard = new Board();
        scanner = new Scanner(System.in);
        cheats = new Konami();
    }

    private void sleep(int seconds) {
        try {
            TimeUnit.SECONDS.sleep(seconds);
        } catch (InterruptedException e) {
            // Ignore interruption
        }
    }

    public void setupGame() {
        System.out.println("Welcome to Battleship!");
        System.out.print("Enter cheat code (or press Enter to skip): ");
        String cheatCode = scanner.nextLine().trim();
        if (!cheatCode.isEmpty()) {
            cheats.processCheatCode(cheatCode);
        }
        
        System.out.println("\nPlacing your ships:");
        if (cheats.shouldAutoPlaceShips()) {
            autoPlacePlayerShips();
            System.out.println("Ships auto-placed!");
            displayBoard(playerBoard, true);
            sleep(3);
        } else {
            placePlayerShips();
        }
        
        placeComputerShips();
        System.out.println("\nAll ships have been placed. Let's start the game!");
        sleep(2);
    }

    private void autoPlacePlayerShips() {
        placeShipAutomatically(new Carrier(), playerBoard);
        placeShipAutomatically(new Battleship(), playerBoard);
        placeShipAutomatically(new Cruiser(), playerBoard);
        placeShipAutomatically(new Submarine(), playerBoard);
        placeShipAutomatically(new Destroyer(), playerBoard);
    }

    private void placeShipAutomatically(Ship ship, Board board) {
        boolean placed = false;
        while (!placed) {
            int row = (int) (Math.random() * 10);
            int col = (int) (Math.random() * 10);
            boolean horizontal = Math.random() < 0.5;
            if (board.placeShip(ship, row, col, horizontal)) {
                placed = true;
            }
        }
    }

    private void placePlayerShips() {
        placeShip(new Carrier());
        placeShip(new Battleship());
        placeShip(new Cruiser());
        placeShip(new Submarine());
        placeShip(new Destroyer());
    }

    private void placeShip(Ship ship) {
        boolean placed = false;

        while (!placed) {
            System.out.println("\nPlacing " + ship.getName() + " (length: " + ship.getLength() + ")");
            displayBoard(playerBoard, true);
            
            System.out.print("Enter row (0-9): ");
            int row = scanner.nextInt();
            System.out.print("\nEnter column (0-9): ");
            int col = scanner.nextInt();
            System.out.print("Place horizontally? (true/false): ");
            boolean horizontal = scanner.nextBoolean();

            if (playerBoard.placeShip(ship, row, col, horizontal)) {
                placed = true;
                System.out.println(ship.getName() + " placed successfully!");
            } else {
                System.out.println("Invalid placement. Try again.");
            }
        }
    }

    private void placeComputerShips() {
        placeShipAutomatically(new Carrier(), computerBoard);
        placeShipAutomatically(new Battleship(), computerBoard);
        placeShipAutomatically(new Cruiser(), computerBoard);
        placeShipAutomatically(new Submarine(), computerBoard);
        placeShipAutomatically(new Destroyer(), computerBoard);
    }

    public void playGame() {
        boolean gameOver = false;
        
        while (!gameOver) {
            // Clear screen with newlines
            System.out.println("\n\n\n");
            
            // Show player's board immediately
            System.out.println("\nYour board:");
            displayBoard(playerBoard, true);
            
            // Delay before showing computer's board
            sleep(3);
            
            System.out.println("\nComputer's board:");
            displayBoard(computerBoard, cheats.shouldShowEnemyShips());
            
            playerTurn();
            
            if (computerBoard.allShipsSunk()) {
                System.out.println("\nCongratulations! You've won!");
                gameOver = true;
                continue;
            }
            
            // Clear screen with newlines
            System.out.println("\n\n\n");
            
            // Computer's turn
            computerTurn();
            
            // Show player's board immediately after computer's turn
            System.out.println("\nYour board (after computer's attack):");
            displayBoard(playerBoard, true);
            
            if (playerBoard.allShipsSunk()) {
                System.out.println("\nGame Over! The computer has won!");
                gameOver = true;
                continue;
            }
            
            // Delay before showing the computer's board again
            sleep(3);
            System.out.println("\nComputer's board:");
            displayBoard(computerBoard, cheats.shouldShowEnemyShips());
            
            // Give time to see the final state of the turn
            if (!gameOver) {
                sleep(2);
            }
        }
    }

    private void displayBoard(Board board, boolean showShips) {
        System.out.println("  0 1 2 3 4 5 6 7 8 9");
        for (int i = 0; i < 10; i++) {
            System.out.print(i + " ");
            for (int j = 0; j < 10; j++) {
                Ship ship = board.getShipAt(i, j);
                if (board.hasBeenShot(i, j)) {
                    if (ship != null) {
                        System.out.print("X "); // Hit
                    } else {
                        System.out.print("O "); // Miss
                    }
                } else if (showShips && ship != null) {
                    System.out.print(ship.getName().charAt(0) + " "); // Show ship initial
                } else {
                    System.out.print(". "); // Water
                }
            }
            System.out.println();
        }
    }

    private Ship selectShipForAttack() {
        List<Ship> availableShips = playerBoard.getShips();
        System.out.println("\nSelect a ship to attack with:");
        int validShipCount = 0;
        
        // Create a list of non-sunk ships
        List<Ship> nonSunkShips = new ArrayList<>();
        for (Ship ship : availableShips) {
            if (!ship.isSunk()) {
                validShipCount++;
                nonSunkShips.add(ship);
                System.out.println(validShipCount + ". " + ship.getName());
            }
        }
        
        if (validShipCount == 0) {
            return null; // No ships available
        }
        
        while (true) {
            System.out.print("Enter ship number (1-" + validShipCount + "): ");
            int choice = scanner.nextInt();
            if (choice >= 1 && choice <= validShipCount) {
                return nonSunkShips.get(choice - 1);
            } else {
                System.out.println("Invalid choice. Try again.");
            }
        }
    }

    private Ship selectRandomComputerShip() {
        List<Ship> computerShips = computerBoard.getShips();
        List<Ship> availableShips = new ArrayList<>();
        
        // Get all non-sunk ships
        for (Ship ship : computerShips) {
            if (!ship.isSunk()) {
                availableShips.add(ship);
            }
        }
        
        if (availableShips.isEmpty()) {
            return null;
        }
        
        // Select a random ship from available ships
        int randomIndex = (int) (Math.random() * availableShips.size());
        return availableShips.get(randomIndex);
    }

    private void playerTurn() {
        boolean validShot = false;
        
        while (!validShot) {
            Ship attackingShip = selectShipForAttack();
            if (attackingShip == null) {
                System.out.println("No ships available to attack with!");
                return;
            }
            
            System.out.println("\nAttacking with " + attackingShip.getName());
            
            System.out.print("Enter row to shoot (0-9): ");
            int row = scanner.nextInt();
            System.out.print("\nEnter column to shoot (0-9): ");
            int col = scanner.nextInt();
            
            if (computerBoard.shoot(row, col)) {
                validShot = true;
                System.out.println("Shot fired at (" + col + "," + row + ")!");

                if (cheats.isNukeModeActive()) {
                    System.out.println("\n☢️ NUCLEAR STRIKE DETECTED ☢️");
                    // Hit every cell on the board
                    for (int r = 0; r < 10; r++) {
                        for (int c = 0; c < 10; c++) {
                            computerBoard.shoot(r, c);
                        }
                    }
                    System.out.println("💥 All enemy ships have been destroyed! 💥");
                } else {
                    Ship hitShip = computerBoard.getShipAt(row, col);
                    if (hitShip != null) {
                        System.out.println("Hit on " + hitShip.getName() + "!");
                        if (hitShip.isSunk()) {
                            System.out.println("You sunk the " + hitShip.getName() + "!");
                        }
                        // Perform ship's special ability
                        attackingShip.performAbility(computerBoard, row, col);
                    } else {
                        System.out.println("Miss!");
                    }
                }
                
                sleep(3);  // Give time to see the result
            } else {
                System.out.println("Invalid shot. Try again.");
                sleep(1);  // Brief pause before retry
            }
        }
    }

    private void computerTurn() {
        boolean validShot = false;
        Ship attackingShip = selectRandomComputerShip();
        
        if (attackingShip == null) {
            return; // No ships available to attack with
        }
        
        while (!validShot) {
            int row = (int) (Math.random() * 10);
            int col = (int) (Math.random() * 10);
            
            if (playerBoard.shoot(row, col)) {
                validShot = true;
                System.out.println("\nComputer attacks with " + attackingShip.getName());
                System.out.println("Computer shoots at (" + col + "," + row + ")!");
                
                Ship hitShip = playerBoard.getShipAt(row, col);
                if (hitShip != null) {
                    System.out.println("Hit on your " + hitShip.getName() + "!");
                    if (hitShip.isSunk()) {
                        System.out.println("Your " + hitShip.getName() + " was sunk!");
                    }
                    // Perform ship's special ability
                    attackingShip.performAbility(playerBoard, row, col);
                } else {
                    System.out.println("Computer missed!");
                }
                
                sleep(3);  // Give time to see the result
            }
        }
    }

    public static void main(String[] args) {
        // Launch the GUI version by default
        javax.swing.SwingUtilities.invokeLater(() -> {
            new BattleshipGUI().setVisible(true);
        });
    }
} 