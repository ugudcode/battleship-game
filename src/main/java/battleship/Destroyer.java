package battleship;

import java.util.Random;

public class Destroyer extends Ship {
    private static final int LENGTH = 2;
    private static final double PROTECTION_CHANCE = 0.20; // 20% chance to protect
    private Random random;

    public Destroyer() {
        super("Destroyer", LENGTH);
        random = new Random();
    }

    @Override
    public void performAbility(Board targetBoard, int row, int col) {
        // Destroyer no longer has an offensive ability
        // It only has the defensive ability (attemptCancelAttack)
    }

    public boolean attemptCancelAttack(boolean isPlayerAttacking) {
        return random.nextDouble() < PROTECTION_CHANCE;
    }
} 