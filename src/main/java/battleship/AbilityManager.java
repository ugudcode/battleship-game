package battleship;

import java.util.*;

public class AbilityManager {
    private Map<String, Boolean> unlockedAbilities;
    private Map<String, Long> lastAbilityUse;
    private Map<String, Integer> cooldowns;
    private int missedShots;
    private int destroyedShips;
    private Set<Integer> hitShipRows;
    private Set<Integer> hitShipCols;
    private Set<Integer> hitEmptyRows;
    private Set<Integer> hitEmptyCols;

    public AbilityManager() {
        unlockedAbilities = new HashMap<>();
        lastAbilityUse = new HashMap<>();
        cooldowns = new HashMap<>();
        missedShots = 0;
        destroyedShips = 0;
        hitShipRows = new HashSet<>();
        hitShipCols = new HashSet<>();
        hitEmptyRows = new HashSet<>();
        hitEmptyCols = new HashSet<>();

        // Initialize abilities
        unlockedAbilities.put("Destroyer", true);   // Always available
        unlockedAbilities.put("Battleship", false); // Requires 2 destroyed ships
        unlockedAbilities.put("Carrier", false);    // Unlocks after hitting empty cell and ship in same row/column
        unlockedAbilities.put("Submarine", false);  // Unlocks after 5 missed shots
        unlockedAbilities.put("Cruiser", true);     // Always available

        // Set cooldowns (in seconds)
        cooldowns.put("Battleship", 20);
        cooldowns.put("Carrier", 30);
    }

    public void recordHit(int row, int col, boolean isHit) {
        if (isHit) {
            hitShipRows.add(row);
            hitShipCols.add(col);
            checkCarrierUnlock();
        } else {
            hitEmptyRows.add(row);
            hitEmptyCols.add(col);
            checkCarrierUnlock();
            missedShots++;
            if (missedShots >= 5) {
                unlockAbility("Submarine");
            }
        }
    }

    private void checkCarrierUnlock() {
        // Check rows
        for (Integer row : hitShipRows) {
            if (hitEmptyRows.contains(row)) {
                unlockAbility("Carrier");
                return;
            }
        }
        // Check columns
        for (Integer col : hitShipCols) {
            if (hitEmptyCols.contains(col)) {
                unlockAbility("Carrier");
                return;
            }
        }
    }

    public void onShipDestroyed(String destroyedShipName, String attackingShipName) {
        destroyedShips++;
        
        // Unlock Battleship if two ships have been destroyed
        if (destroyedShips >= 2) {
            unlockAbility("Battleship");
        }
    }

    public void onPlayerShipSunk() {
        // Reset Submarine ability when player destroys a ship
        unlockedAbilities.put("Submarine", false);
        missedShots = 0;  // Reset missed shots counter
    }

    public boolean isAbilityUnlocked(String shipName) {
        return unlockedAbilities.getOrDefault(shipName, false);
    }

    public boolean isAbilityAvailable(String shipName) {
        if (!isAbilityUnlocked(shipName)) {
            return false;
        }

        // Destroyer is always available
        if (shipName.equals("Destroyer")) {
            return true;
        }

        // Check cooldown for other ships
        Long lastUse = lastAbilityUse.get(shipName);
        if (lastUse == null) {
            return true;
        }

        int cooldown = cooldowns.getOrDefault(shipName, 0);
        return (System.currentTimeMillis() - lastUse) >= cooldown * 1000;
    }

    public void useAbility(String shipName) {
        if (shipName.equals("Battleship") || shipName.equals("Carrier")) {
            lastAbilityUse.put(shipName, System.currentTimeMillis());
        }
    }

    public void unlockAbility(String shipName) {
        if (!unlockedAbilities.getOrDefault(shipName, false)) {
            unlockedAbilities.put(shipName, true);
            System.out.println("Unlocked ability for: " + shipName);  // Debug message
        }
    }

    public String getRemainingCooldown(String shipName) {
        Long lastUse = lastAbilityUse.get(shipName);
        if (lastUse == null) {
            return "";
        }

        int cooldown = cooldowns.getOrDefault(shipName, 0);
        long elapsed = (System.currentTimeMillis() - lastUse) / 1000;
        long remaining = cooldown - elapsed;

        if (remaining <= 0) {
            return " - Ready!";
        }
        return String.format(" - %ds remaining", remaining);
    }

    public void useSubmarine() {
        unlockedAbilities.put("Submarine", false);
        missedShots = 0;
    }

    public void reset() {
        unlockedAbilities.clear();
        lastAbilityUse.clear();
        missedShots = 0;
        destroyedShips = 0;
        hitShipRows.clear();
        hitShipCols.clear();
        hitEmptyRows.clear();
        hitEmptyCols.clear();

        // Reset initial ability states
        unlockedAbilities.put("Destroyer", true);
        unlockedAbilities.put("Battleship", false);
        unlockedAbilities.put("Carrier", false);
        unlockedAbilities.put("Submarine", false);
        unlockedAbilities.put("Cruiser", true);
    }

    public String getUnlockStatus(String shipType) {
        if (!unlockedAbilities.getOrDefault(shipType, false)) {
            switch (shipType) {
                case "Carrier":
                    return "Hit empty cell and ship in same row/column";
                case "Battleship":
                    return String.format("Destroy 2 enemy ships to unlock (%d/2)", destroyedShips);
                case "Submarine":
                    return String.format("Miss 5 shots to unlock (%d/5)", missedShots);
                case "Destroyer":
                case "Cruiser":
                    return "Always available";
                default:
                    return "Unknown ability";
            }
        }
        
        // Show cooldown status for Battleship and Carrier
        if ((shipType.equals("Battleship") || shipType.equals("Carrier")) && lastAbilityUse.containsKey(shipType)) {
            String cooldownStatus = getRemainingCooldown(shipType);
            return cooldownStatus.equals(" - Ready!") ? "Ready!" : "Cooldown" + cooldownStatus;
        }
        
        return "Ready!";
    }
} 