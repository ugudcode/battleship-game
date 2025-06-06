package battleship;

public class Konami {
    private boolean showEnemyShips = false;
    private boolean autoPlaceShips = false;
    private boolean nukeMode = false;

    public void processCheatCode(String code) {
        switch (code.toLowerCase()) {
            case "debug":
                showEnemyShips = true;
                autoPlaceShips = true;
                System.out.println("DEBUG MODE ACTIVATED: Ships auto-placed and enemy ships visible");
                break;
            case "showships":
                showEnemyShips = true;
                System.out.println("CHEAT ACTIVATED: Enemy ships are now visible");
                break;
            case "autoplace":
                autoPlaceShips = true;
                System.out.println("CHEAT ACTIVATED: Ships will be auto-placed");
                break;
            case "nuke":
                nukeMode = true;
                System.out.println("CHEAT ACTIVATED: Nuclear strike ready - next shot will sink all enemy ships!");
                break;
            default:
                // No valid cheat code entered
                break;
        }
    }

    public void autoPlaceShips(Board board) {
        Ship[] ships = {
            new Carrier(),
            new Battleship(),
            new Cruiser(),
            new Submarine(),
            new Destroyer()
        };

        for (Ship ship : ships) {
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
    }

    public boolean shouldShowEnemyShips() {
        return showEnemyShips;
    }

    public boolean shouldAutoPlaceShips() {
        return autoPlaceShips;
    }

    public boolean toggleNukeMode() {
        nukeMode = !nukeMode;
        return nukeMode;
    }

    public boolean isNukeModeActive() {
        return nukeMode;
    }
} 