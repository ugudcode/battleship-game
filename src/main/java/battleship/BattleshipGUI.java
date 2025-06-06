package battleship;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.border.*;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;
import java.util.Arrays;

public class BattleshipGUI extends JFrame {
    private static final int BOARD_SIZE = 10;
    private static final String[] SHIP_TYPES = {
        "Carrier",
        "Battleship",
        "Cruiser",
        "Submarine",
        "Destroyer"
    };
    
    private final int CELL_SIZE = 40;
    private final Color WATER_COLOR = new Color(0, 105, 148);
    private final Color SHIP_COLOR = new Color(60, 60, 60);
    private final Color HIT_COLOR = new Color(220, 0, 0);
    private final Color MISS_COLOR = new Color(200, 200, 200);
    private final Color BACKGROUND_COLOR = new Color(240, 240, 240);
    private final Color TEXT_COLOR = new Color(33, 33, 33);
    private final Color BORDER_COLOR = new Color(100, 100, 100);
    private final Color CARRIER_PREVIEW_COLOR = new Color(255, 255, 0, 80);
    private final Color OVERLAY_COLOR = new Color(0, 255, 0, 80);
    private final Color PLACEMENT_OVERLAY_COLOR = new Color(100, 100, 100, 80);
    private final Color SUNK_COLOR = new Color(128, 128, 128);
    private final Color REVEALED_SHIP_COLOR = new Color(100, 200, 100);
    private final Font MAIN_FONT = new Font("Arial", Font.BOLD, 14);
    private final Font STATUS_FONT = new Font("Arial", Font.BOLD, 16);
    private final int BOARD_SPACING = 20;
    private final int WINDOW_PADDING = 25;

    private Board playerBoard;
    private Board computerBoard;
    private JPanel playerBoardPanel;
    private JPanel computerBoardPanel;
    private JPanel mainPanel;
    private JPanel shipSelectionPanel;
    private JLabel statusLabel;
    private JLabel instructionLabel;
    private JLabel timerLabel;
    private JLabel hoverPositionLabel;
    private JComboBox<String> attackShipSelector;
    private JToggleButton carrierModeButton;
    private JTextField debugTextField;
    private JButton[][] playerButtons;
    private JButton[][] computerButtons;
    private boolean gameStarted = false;
    private boolean isPlacingShips = true;
    private boolean debugMode = false;
    private Set<String> placedShipTypes = new HashSet<>();
    private Ship selectedShip;
    private Konami cheats;
    private int lastPreviewRow = -1;
    private int lastPreviewCol = -1;
    private boolean isShowingPreview = false;
    private Timer boardSwitchTimer;
    private int gameTimeSeconds = 0;
    private Timer gameTimer;
    private Timer boardUpdateTimer;
    private Point currentOverlayPosition;
    private boolean isOverlayActive = false;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            BattleshipGUI gui = new BattleshipGUI();
            gui.setVisible(true);
        });
    }

    public BattleshipGUI() {
        super("Battleship Game");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        
        // Initialize boards
        playerBoard = new Board();
        computerBoard = new Board();
        
        // Initialize UI components with better spacing and layout
        mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout(BOARD_SPACING, BOARD_SPACING));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(WINDOW_PADDING, WINDOW_PADDING, WINDOW_PADDING, WINDOW_PADDING));
        mainPanel.setBackground(BACKGROUND_COLOR);
        
        // Create game status panel
        JPanel statusPanel = new JPanel(new BorderLayout(5, 5));
        statusPanel.setBackground(BACKGROUND_COLOR);
        
        statusLabel = new JLabel("Welcome to Battleship! Place your ships.", SwingConstants.CENTER);
        statusLabel.setFont(STATUS_FONT);
        statusLabel.setForeground(TEXT_COLOR);
        statusLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        
        instructionLabel = new JLabel("Select a ship to place", SwingConstants.CENTER);
        instructionLabel.setFont(MAIN_FONT);
        instructionLabel.setForeground(TEXT_COLOR);
        
        timerLabel = new JLabel("Time: 0:00", SwingConstants.CENTER);
        timerLabel.setFont(MAIN_FONT);
        timerLabel.setForeground(TEXT_COLOR);
        
        hoverPositionLabel = new JLabel("", SwingConstants.CENTER);
        hoverPositionLabel.setFont(MAIN_FONT);
        hoverPositionLabel.setForeground(TEXT_COLOR);
        
        statusPanel.add(statusLabel, BorderLayout.NORTH);
        statusPanel.add(instructionLabel, BorderLayout.CENTER);
        JPanel bottomStatus = new JPanel(new BorderLayout());
        bottomStatus.setBackground(BACKGROUND_COLOR);
        bottomStatus.add(timerLabel, BorderLayout.WEST);
        bottomStatus.add(hoverPositionLabel, BorderLayout.EAST);
        statusPanel.add(bottomStatus, BorderLayout.SOUTH);
        
        mainPanel.add(statusPanel, BorderLayout.NORTH);
        
        // Create boards panel with proper spacing
        JPanel boardsPanel = new JPanel(new GridLayout(1, 2, BOARD_SPACING, 0));
        boardsPanel.setBackground(BACKGROUND_COLOR);
        
        // Initialize button arrays
        playerButtons = new JButton[BOARD_SIZE][BOARD_SIZE];
        computerButtons = new JButton[BOARD_SIZE][BOARD_SIZE];
        
        // Create and add board panels
        playerBoardPanel = createBoardPanel(true);
        computerBoardPanel = createBoardPanel(false);
        
        boardsPanel.add(playerBoardPanel);
        boardsPanel.add(computerBoardPanel);
        mainPanel.add(boardsPanel, BorderLayout.CENTER);
        
        // Create ship selection panel with better styling
        shipSelectionPanel = new JPanel();
        shipSelectionPanel.setLayout(new BoxLayout(shipSelectionPanel, BoxLayout.Y_AXIS));
        shipSelectionPanel.setBackground(BACKGROUND_COLOR);
        shipSelectionPanel.setBorder(BorderFactory.createEmptyBorder(0, BOARD_SPACING, 0, 0));
        
        // Add ship selection buttons
        for (String shipType : SHIP_TYPES) {
            JButton shipButton = new JButton(shipType);
            shipButton.setFont(MAIN_FONT);
            shipButton.setAlignmentX(Component.CENTER_ALIGNMENT);
            shipButton.setMaximumSize(new Dimension(150, 35));
            shipButton.setBackground(BACKGROUND_COLOR);
            shipButton.setForeground(TEXT_COLOR);
            shipButton.setFocusPainted(false);
            shipButton.addActionListener(e -> selectShip(shipType));
            shipSelectionPanel.add(shipButton);
            shipSelectionPanel.add(Box.createVerticalStrut(5));
        }
        
        // Add auto-place button
        JButton autoPlaceButton = new JButton("Auto Place");
        autoPlaceButton.setFont(MAIN_FONT);
        autoPlaceButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        autoPlaceButton.setMaximumSize(new Dimension(150, 35));
        autoPlaceButton.setBackground(BACKGROUND_COLOR);
        autoPlaceButton.setForeground(TEXT_COLOR);
        autoPlaceButton.setFocusPainted(false);
        autoPlaceButton.addActionListener(e -> {
            if (isPlacingShips) {
                autoPlaceShips();
            }
        });
        shipSelectionPanel.add(Box.createVerticalStrut(15));
        shipSelectionPanel.add(autoPlaceButton);
        
        // Add carrier mode toggle button
        carrierModeButton = new JToggleButton("Target Mode: ROW");
        carrierModeButton.setFont(MAIN_FONT);
        carrierModeButton.setVisible(false);
        carrierModeButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        carrierModeButton.setMaximumSize(new Dimension(150, 35));
        carrierModeButton.setBackground(BACKGROUND_COLOR);
        carrierModeButton.setForeground(TEXT_COLOR);
        carrierModeButton.setFocusPainted(false);
        carrierModeButton.addActionListener(e -> {
            boolean isRowMode = carrierModeButton.isSelected();
            carrierModeButton.setText("Target Mode: " + (isRowMode ? "ROW" : "COLUMN"));
            String selectedShipName = (String) attackShipSelector.getSelectedItem();
            if (selectedShipName != null && selectedShipName.equals("Carrier")) {
                Point lastHovered = findLastHoveredPosition();
                if (lastHovered != null) {
                    updateTargetingOverlay(lastHovered.x, lastHovered.y);
                }
            }
        });
        shipSelectionPanel.add(Box.createVerticalStrut(10));
        shipSelectionPanel.add(carrierModeButton);
        
        // Add attack ship selector
        attackShipSelector = new JComboBox<>(SHIP_TYPES);
        attackShipSelector.setFont(MAIN_FONT);
        attackShipSelector.setMaximumSize(new Dimension(150, 35));
        attackShipSelector.setAlignmentX(Component.CENTER_ALIGNMENT);
        attackShipSelector.setEnabled(false);
        attackShipSelector.addActionListener(e -> {
            String selectedShip = (String) attackShipSelector.getSelectedItem();
            carrierModeButton.setVisible(selectedShip != null && selectedShip.equals("Carrier"));
            clearTargetingOverlay();
        });
        shipSelectionPanel.add(Box.createVerticalStrut(15));
        shipSelectionPanel.add(attackShipSelector);
        
        mainPanel.add(shipSelectionPanel, BorderLayout.EAST);
        
        // Set up the main window
        setContentPane(mainPanel);
        pack();
        setLocationRelativeTo(null);
        
        // Initialize game timers
        setupGameTimers();
        
        // Add keyboard listener for ship rotation
        setupKeyboardListener();
    }

    private void setupGameTimers() {
        gameTimer = new Timer(1000, e -> {
            gameTimeSeconds++;
            updateTimerLabel();
        });

        boardUpdateTimer = new Timer(100, e -> {
            updatePlayerBoard();
            updateComputerBoard();
        });
    }

    private void updateTimerLabel() {
        int minutes = gameTimeSeconds / 60;
        int seconds = gameTimeSeconds % 60;
        timerLabel.setText(String.format("Time: %d:%02d", minutes, seconds));
    }

    private void setupKeyboardListener() {
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(e -> {
            if (e.getID() == KeyEvent.KEY_PRESSED && e.getKeyCode() == KeyEvent.VK_R) {
                if (selectedShip != null) {
                    selectedShip.toggleOrientation();
                    Point lastHovered = findLastHoveredPosition();
                    if (lastHovered != null) {
                        showShipPlacementPreview(lastHovered.x, lastHovered.y);
                        updatePlacementLabel(lastHovered.x, lastHovered.y);
                    }
                }
            }
            return false;
        });
    }

    private void selectShip(String shipType) {
        if (isPlacingShips) {
            // Check if this ship type has already been placed
            if (placedShipTypes.contains(shipType)) {
                statusLabel.setText("You have already placed a " + shipType + "!");
                return;
            }
            
            selectedShip = createShip(shipType);
            updateInstructionLabel();
        }
    }

    private JPanel createBoardPanel(boolean isPlayer) {
        JPanel panel = new JPanel(new GridLayout(BOARD_SIZE, BOARD_SIZE, 1, 1));
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(5, 5, 5, 5),
            BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 2),
                isPlayer ? "Your Board" : "Computer's Board",
                TitledBorder.CENTER,
                TitledBorder.TOP,
                MAIN_FONT,
                TEXT_COLOR
            )
        ));
        panel.setBackground(BORDER_COLOR);

        JButton[][] buttons = isPlayer ? playerButtons : computerButtons;
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                JButton button = new JButton();
                button.setPreferredSize(new Dimension(CELL_SIZE, CELL_SIZE));
                button.setBackground(WATER_COLOR);
                button.setOpaque(true);
                button.setBorderPainted(true);
                button.setFocusPainted(false);
                button.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));

                final int finalRow = row;
                final int finalCol = col;

                if (isPlayer) {
                    button.addMouseListener(new MouseAdapter() {
                        @Override
                        public void mouseEntered(MouseEvent e) {
                            if (isPlacingShips && selectedShip != null) {
                                showShipPlacementPreview(finalRow, finalCol);
                                updatePlacementLabel(finalRow, finalCol);
                            }
                        }

                        @Override
                        public void mouseExited(MouseEvent e) {
                            if (isPlacingShips && selectedShip != null) {
                                clearShipPlacementPreview();
                            }
                        }

                        @Override
                        public void mouseClicked(MouseEvent e) {
                            if (isPlacingShips) {
                                handlePlaceShip(finalRow, finalCol);
                            }
                        }
                    });
                } else {
                    button.addMouseListener(new MouseAdapter() {
                        @Override
                        public void mouseEntered(MouseEvent e) {
                            if (gameStarted && !isPlacingShips) {
                                updateHoverIndicator(finalRow, finalCol);
                                showCarrierTargetPreview(finalRow, finalCol, true);
                            }
                        }

                        @Override
                        public void mouseExited(MouseEvent e) {
                            if (gameStarted && !isPlacingShips) {
                                showCarrierTargetPreview(finalRow, finalCol, false);
                            }
                        }

                        @Override
                        public void mouseClicked(MouseEvent e) {
                            if (gameStarted && !isPlacingShips && button.isEnabled()) {
                                handlePlayerShot(finalRow, finalCol, button);
                            }
                        }
                    });
                }

                buttons[row][col] = button;
                panel.add(button);
            }
        }
        return panel;
    }

    private void showShipPlacementPreview(int row, int col) {
        if (selectedShip == null || !isPlacingShips) return;

        // Clear previous preview
        if (isShowingPreview) {
            clearShipPlacementPreview();
        }

        boolean canPlace = canPlaceShip(row, col, selectedShip);
        Color previewColor = canPlace ? 
            new Color(0, 255, 0, 100) : // Semi-transparent green
            new Color(255, 0, 0, 100);  // Semi-transparent red

        int length = selectedShip.getLength();
        boolean horizontal = selectedShip.isHorizontal();

        for (int i = 0; i < length; i++) {
            int r = horizontal ? row : row + i;
            int c = horizontal ? col + i : col;
            
            if (r < BOARD_SIZE && c < BOARD_SIZE) {
                Color currentColor = playerButtons[r][c].getBackground();
                playerButtons[r][c].setBackground(blend(currentColor, previewColor));
            }
        }

        isShowingPreview = true;
        lastPreviewRow = row;
        lastPreviewCol = col;
    }

    private void clearShipPlacementPreview() {
        if (!isShowingPreview) return;

        boolean horizontal = selectedShip.isHorizontal();
        int length = selectedShip.getLength();

        for (int i = 0; i < length; i++) {
            int r = horizontal ? lastPreviewRow : lastPreviewRow + i;
            int c = horizontal ? lastPreviewCol + i : lastPreviewCol;
            
            if (r < BOARD_SIZE && c < BOARD_SIZE) {
                Ship existingShip = playerBoard.getShipAt(r, c);
                if (existingShip != null) {
                    playerButtons[r][c].setBackground(SHIP_COLOR);
                } else {
                    playerButtons[r][c].setBackground(WATER_COLOR);
                }
            }
        }
        isShowingPreview = false;
    }

    private void updatePlacementLabel(int row, int col) {
        if (selectedShip == null) return;
        
        String position = String.format("%c%d", (char)('A' + col), row + 1);
        boolean wouldBeLegal = playerBoard.isValidPlacement(selectedShip, row, col, isPlacingShips);
        String orientation = isPlacingShips ? "horizontally" : "vertically";
        
        if (wouldBeLegal) {
            hoverPositionLabel.setText(String.format("Place %s %s at %s (Press R to rotate)", 
                selectedShip.getName(), orientation, position));
            hoverPositionLabel.setForeground(new Color(0, 120, 0));
        } else {
            hoverPositionLabel.setText(String.format("Cannot place %s %s at %s", 
                selectedShip.getName(), orientation, position));
            hoverPositionLabel.setForeground(new Color(180, 0, 0));
        }
    }

    private void updateHoverIndicator(int row, int col) {
        String position = String.format("%c%d", (char)('A' + col), row + 1);
        String selectedShipName = (String) attackShipSelector.getSelectedItem();
        hoverPositionLabel.setText(String.format("Targeting position %s with %s", position, selectedShipName));
    }

    private Ship createShip(String shipType) {
        switch (shipType) {
            case "Carrier":
                return new Carrier();
            case "Battleship":
                return new Battleship();
            case "Cruiser":
                return new Cruiser();
            case "Submarine":
                return new Submarine();
            case "Destroyer":
                return new Destroyer();
            default:
                throw new IllegalArgumentException("Unknown ship type: " + shipType);
        }
    }

    private void updateInstructionLabel() {
        if (isPlacingShips) {
            int shipsPlaced = placedShipTypes.size();
            if (selectedShip == null) {
                instructionLabel.setText(String.format("Select a ship to place (%d of %d ships placed)", 
                    shipsPlaced, SHIP_TYPES.length));
            } else {
                instructionLabel.setText(String.format("Click to place %s (%d of %d ships placed) - Press R to rotate", 
                    selectedShip.getName(), shipsPlaced, SHIP_TYPES.length));
            }
        } else if (gameStarted) {
            instructionLabel.setText("Select a ship to attack with and click on the enemy board");
        }
    }

    private boolean canPlaceShip(int row, int col, Ship ship) {
        if (ship == null) return false;
        
        int length = ship.getLength();
        boolean horizontal = ship.isHorizontal();
        
        // Check if ship would extend beyond board
        if (horizontal && col + length > BOARD_SIZE) return false;
        if (!horizontal && row + length > BOARD_SIZE) return false;
        
        // Check for overlapping ships
        for (int i = 0; i < length; i++) {
            int r = horizontal ? row : row + i;
            int c = horizontal ? col + i : col;
            if (playerBoard.getShipAt(r, c) != null) {
                return false;
            }
        }
        
        return true;
    }

    private void handlePlaceShip(int row, int col) {
        if (selectedShip == null || !canPlaceShip(row, col, selectedShip)) return;

        // Clear any existing preview
        clearShipPlacementPreview();

        // Place the ship
        selectedShip.setRow(row);
        selectedShip.setCol(col);
        if (playerBoard.placeShip(selectedShip, row, col, selectedShip.isHorizontal())) {
            placedShipTypes.add(selectedShip.getName());

            // Update the visual representation
            int length = selectedShip.getLength();
            boolean horizontal = selectedShip.isHorizontal();

            for (int i = 0; i < length; i++) {
                int r = horizontal ? row : row + i;
                int c = horizontal ? col + i : col;
                playerButtons[r][c].setBackground(SHIP_COLOR);
            }

            // Disable the placed ship's button
            for (Component c : shipSelectionPanel.getComponents()) {
                if (c instanceof JButton && ((JButton) c).getText().equals(selectedShip.getName())) {
                    c.setEnabled(false);
                    break;
                }
            }

            selectedShip = null;
            updateInstructionLabel();

            // Check if all ships are placed
            if (placedShipTypes.size() == SHIP_TYPES.length) {
                startGame();
            }
        }
    }

    private void handlePlayerShot(int row, int col, JButton button) {
        String selectedShipName = (String) attackShipSelector.getSelectedItem();
        Ship attackingShip = findPlayerShip(selectedShipName);
        
        if (attackingShip == null || computerBoard.hasBeenShot(row, col)) {
            return;
        }

        // Special case for Submarine - it reveals instead of attacking
        if (attackingShip instanceof Submarine) {
            // Clear any previously revealed ships
            computerBoard.clearRevealedShips();
            
            // Perform the reveal ability
            attackingShip.performAbility(computerBoard, row, col);
            updateComputerBoard();
            
            // Start computer's turn after a delay to show the revealed ship
            Timer revealTimer = new Timer(2000, e -> {
                computerBoard.clearRevealedShips();
                updateComputerBoard();
                
                // Check if game should continue
                if (!computerBoard.allShipsSunk() && !playerBoard.allShipsSunk()) {
                    statusLabel.setText("Computer's turn - Preparing attack...");
                    updateBoardTitles(false);
                    startBoardSwitchTimer();
                }
                ((Timer)e.getSource()).stop();
            });
            revealTimer.setRepeats(false);
            revealTimer.start();
            return;
        }
        
        // Update carrier mode if using carrier
        if (attackingShip instanceof Carrier) {
            ((Carrier) attackingShip).setTargetRow(carrierModeButton.isSelected());
        }
        
        // Normal attack flow for other ships
        computerBoard.shoot(row, col);
        Ship hitShip = computerBoard.getShipAt(row, col);
        
        // Update UI and perform ability
        if (hitShip != null) {
            button.setBackground(HIT_COLOR);
            if (hitShip.isSunk()) {
                statusLabel.setText(String.format("💥 %s SUNK by your %s! 💥", 
                    hitShip.getName(), attackingShip.getName()));
            } else {
                statusLabel.setText(String.format("🎯 Hit on enemy %s with your %s!", 
                    hitShip.getName(), attackingShip.getName()));
            }
            
            // Perform ability
            attackingShip.performAbility(computerBoard, row, col);
        } else {
            button.setBackground(MISS_COLOR);
            statusLabel.setText("💨 Miss!");
            
            // For Carrier, perform ability even on miss
            if (attackingShip instanceof Carrier) {
                attackingShip.performAbility(computerBoard, row, col);
            }
        }
        
        // Update the board to reflect any changes from abilities
        updateComputerBoard();
        
        // Check for game over
        if (computerBoard.allShipsSunk()) {
            gameOver(true);
            return;
        }
        
        // Check if game should continue
        if (!playerBoard.allShipsSunk()) {
            // Start computer's turn
            statusLabel.setText("Computer's turn - Preparing attack...");
            updateBoardTitles(false);
            startBoardSwitchTimer();
        } else {
            gameOver(false);
        }
    }

    private void handleNuclearStrike() {
        statusLabel.setText("☢️ NUCLEAR STRIKE LAUNCHED! ☢️");
        for (int r = 0; r < BOARD_SIZE; r++) {
            for (int c = 0; c < BOARD_SIZE; c++) {
                computerBoard.shoot(r, c);
            }
        }
        updateComputerBoard();
        gameOver(true);
    }

    private void updateShotResult(JButton button, Ship hitShip, Ship attackingShip, int row, int col) {
        if (hitShip != null) {
            button.setBackground(HIT_COLOR);
            if (hitShip.isSunk()) {
                statusLabel.setText(String.format("💥 %s SUNK by your %s! 💥", 
                    hitShip.getName(), attackingShip.getName()));
            } else {
                statusLabel.setText(String.format("🎯 Hit on enemy %s with your %s!", 
                    hitShip.getName(), attackingShip.getName()));
            }
            
            // Perform special ability for non-Carrier ships on hit
            if (!(attackingShip instanceof Carrier)) {
                Timer abilityTimer = new Timer(500, e -> {
                    attackingShip.performAbility(computerBoard, row, col);
                    updateComputerBoard();
                    checkShipsAndGameStatus(true);
                    ((Timer)e.getSource()).stop();
                    
                    // Start computer's turn after ability completes
                    if (!computerBoard.allShipsSunk()) {
                        statusLabel.setText("Computer's turn - Preparing attack...");
                        updateBoardTitles(false);
                        startBoardSwitchTimer();
                    }
                });
                abilityTimer.setRepeats(false);
                abilityTimer.start();
            }
        } else {
            button.setBackground(MISS_COLOR);
            statusLabel.setText(String.format("💨 Miss with your %s!", attackingShip.getName()));
            
            // Start computer's turn after miss
            Timer turnTimer = new Timer(500, e -> {
                if (!computerBoard.allShipsSunk()) {
                    statusLabel.setText("Computer's turn - Preparing attack...");
                    updateBoardTitles(false);
                    startBoardSwitchTimer();
                }
                ((Timer)e.getSource()).stop();
            });
            turnTimer.setRepeats(false);
            turnTimer.start();
        }
    }

    private void checkShipsAndGameStatus(boolean isPlayerTurn) {
        // Check all ships to update their sunk status
        for (Ship ship : computerBoard.getShips()) {
            boolean isSunk = true;
            int row = ship.getRow();
            int col = ship.getCol();
            int length = ship.getLength();
            boolean horizontal = ship.isHorizontal();

            for (int i = 0; i < length; i++) {
                int checkRow = horizontal ? row : row + i;
                int checkCol = horizontal ? col + i : col;
                
                // If any part hasn't been shot, the ship is not sunk
                if (!computerBoard.hasBeenShot(checkRow, checkCol)) {
                    isSunk = false;
                    break;
                }
            }
            
            if (isSunk && !ship.isSunk()) {
                ship.setSunk(true);
                statusLabel.setText("💥 " + ship.getName() + " has been SUNK! 💥");
            }
        }

        // Check if all ships are sunk
        if (computerBoard.allShipsSunk()) {
            gameOver(true);
        }
    }

    private Ship findPlayerShip(String shipName) {
        String baseName = shipName.replaceAll(" \\(\\d+\\)$", "");
        return playerBoard.getShips().stream()
            .filter(ship -> ship.getName().equals(baseName))
            .findFirst()
            .orElse(null);
    }

    private void updatePlayerBoard() {
        SwingUtilities.invokeLater(() -> {
            for (int row = 0; row < BOARD_SIZE; row++) {
                for (int col = 0; col < BOARD_SIZE; col++) {
                    JButton button = playerButtons[row][col];
                    Ship ship = playerBoard.getShipAt(row, col);
                    Color newColor;
                    
                    if (ship != null) {
                        if (playerBoard.hasBeenShot(row, col)) {
                            newColor = HIT_COLOR;
                        } else {
                            newColor = SHIP_COLOR;
                        }
                    } else {
                        if (playerBoard.hasBeenShot(row, col)) {
                            newColor = MISS_COLOR;
                        } else {
                            newColor = WATER_COLOR;
                        }
                    }
                    
                    // Only update if color has changed
                    if (!button.getBackground().equals(newColor)) {
                        button.setBackground(newColor);
                    }
                }
            }
        });
    }

    private void updateComputerBoard() {
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                JButton button = computerButtons[i][j];
                Ship ship = computerBoard.getShipAt(i, j);
                
                if (computerBoard.hasBeenShot(i, j)) {
                    if (ship != null) {
                        if (ship.isSunk()) {
                            button.setBackground(SUNK_COLOR);
                        } else {
                            button.setBackground(HIT_COLOR);
                        }
                    } else {
                        button.setBackground(MISS_COLOR);
                    }
                } else if (ship != null && computerBoard.isShipRevealed(ship)) {
                    // Show revealed ships from submarine ability
                    button.setBackground(REVEALED_SHIP_COLOR);
                } else {
                    button.setBackground(WATER_COLOR);
                }
            }
        }
    }

    private void showCarrierTargetPreview(int row, int col, boolean show) {
        String selectedShipName = (String) attackShipSelector.getSelectedItem();
        Ship attackingShip = findPlayerShip(selectedShipName);
        
        if (!(attackingShip instanceof Carrier)) {
            return;
        }

        // Clear any existing overlay
        clearTargetingOverlay();
        
        if (!show) {
            return;
        }

        // Show new overlay based on ship type and position
        if (attackingShip instanceof Carrier) {
            boolean isRowMode = carrierModeButton.isSelected();
            if (isRowMode) {
                // Highlight entire row
                for (int j = 0; j < BOARD_SIZE; j++) {
                    if (!computerBoard.hasBeenShot(row, j)) {
                        computerButtons[row][j].setBackground(OVERLAY_COLOR);
                    }
                }
            } else {
                // Highlight entire column
                for (int i = 0; i < BOARD_SIZE; i++) {
                    if (!computerBoard.hasBeenShot(i, col)) {
                        computerButtons[i][col].setBackground(OVERLAY_COLOR);
                    }
                }
            }
            currentOverlayPosition = new Point(row, col);
            isOverlayActive = true;
        }
    }

    private Color blend(Color base, Color overlay) {
        float[] baseHSB = Color.RGBtoHSB(base.getRed(), base.getGreen(), base.getBlue(), null);
        // Make the base color brighter and slightly tinted with the overlay
        return new Color(
            (base.getRed() * 3 + overlay.getRed()) / 4,
            (base.getGreen() * 3 + overlay.getGreen()) / 4,
            (base.getBlue() * 3 + overlay.getBlue()) / 4,
            base.getAlpha()
        );
    }

    private void updateTargetingOverlay(int row, int col) {
        if (!gameStarted || computerBoardPanel == null) return;

        String selectedShipName = (String) attackShipSelector.getSelectedItem();
        Ship attackingShip = findPlayerShip(selectedShipName);
        
        // Only clear previous overlay if we're showing a new position
        if (currentOverlayPosition == null || 
            currentOverlayPosition.x != row || 
            currentOverlayPosition.y != col) {
            clearTargetingOverlay();
        }

        // Show new overlay based on ship type and position
        if (attackingShip instanceof Carrier) {
            boolean isRowMode = carrierModeButton.isSelected();
            if (isRowMode) {
                // Highlight entire row
                for (int j = 0; j < BOARD_SIZE; j++) {
                    if (!computerBoard.hasBeenShot(row, j)) {
                        computerButtons[row][j].setBackground(OVERLAY_COLOR);
                    }
                }
            } else {
                // Highlight entire column
                for (int i = 0; i < BOARD_SIZE; i++) {
                    if (!computerBoard.hasBeenShot(i, col)) {
                        computerButtons[i][col].setBackground(OVERLAY_COLOR);
                    }
                }
            }
            currentOverlayPosition = new Point(row, col);
            isOverlayActive = true;
        }
    }

    private Point findLastHoveredPosition() {
        // Find the last hovered button position
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                if (computerButtons[i][j].getBackground().equals(OVERLAY_COLOR)) {
                    return new Point(i, j);
                }
            }
        }
        return null;
    }

    private void updateButtonColor(JButton button, int row, int col, Board board) {
        button.setOpaque(true);  // Always set opaque first
        
        Ship ship = board.getShipAt(row, col);
        if (board.hasBeenShot(row, col)) {
            if (ship != null) {
                button.setBackground(HIT_COLOR);
            } else {
                button.setBackground(MISS_COLOR);
            }
        } else {
            button.setBackground(WATER_COLOR);
        }
    }

    private void clearTargetingOverlay() {
        if (!isOverlayActive || computerBoardPanel == null) return;

        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                JButton button = computerButtons[i][j];
                if (button.getBackground().equals(OVERLAY_COLOR)) {
                    updateButtonColor(button, i, j, computerBoard);
                }
            }
        }
        currentOverlayPosition = null;
        isOverlayActive = false;
    }

    private void updateBoardTitles(boolean isPlayerTurn) {
        Border playerBorder = BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            isPlayerTurn ? "Your Board - Defending" : "Your Board - Under Attack!",
            TitledBorder.CENTER,
            TitledBorder.TOP,
            MAIN_FONT,
            TEXT_COLOR
        );
        
        Border computerBorder = BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            isPlayerTurn ? "Enemy Board - Select Target" : "Enemy Board - Planning Attack...",
            TitledBorder.CENTER,
            TitledBorder.TOP,
            MAIN_FONT,
            TEXT_COLOR
        );
        
        playerBoardPanel.setBorder(playerBorder);
        computerBoardPanel.setBorder(computerBorder);
    }

    private void placeComputerShips() {
        Ship[] ships = {
            new Carrier(), new Battleship(), new Cruiser(),
            new Submarine(), new Destroyer()
        };

        for (Ship ship : ships) {
            boolean placed = false;
            while (!placed) {
                int row = (int) (Math.random() * BOARD_SIZE);
                int col = (int) (Math.random() * BOARD_SIZE);
                boolean horizontal = Math.random() < 0.5;
                placed = computerBoard.placeShip(ship, row, col, horizontal);
            }
        }
    }

    private void startBoardSwitchTimer() {
        disableBoards();
        if (boardSwitchTimer != null) {
            boardSwitchTimer.stop();
        }
        boardSwitchTimer = new Timer(1000, e -> {
            computerTurn();
            ((Timer)e.getSource()).stop();
        });
        boardSwitchTimer.setRepeats(false);
        boardSwitchTimer.start();
    }

    private void computerTurn() {
        boolean validShot = false;
        final Ship attackingShip = selectRandomNonSunkShip(computerBoard);

        if (attackingShip == null) {
            enableBoards();  // Re-enable player's turn if no ships available
            updateBoardTitles(true);
            statusLabel.setText("Your turn - Select a target!");
            return;
        }

        while (!validShot) {
            int row = (int) (Math.random() * BOARD_SIZE);
            int col = (int) (Math.random() * BOARD_SIZE);
            
            if (!playerBoard.hasBeenShot(row, col)) {  // Only shoot at unshot locations
                validShot = true;
                playerBoard.shoot(row, col);
                final Ship hitShip = playerBoard.getShipAt(row, col);
                
                // Update UI
                SwingUtilities.invokeLater(() -> {
                    statusLabel.setText(String.format("Computer attacks with %s", attackingShip.getName()));
                    updatePlayerBoard();
                    
                    if (hitShip != null) {
                        if (hitShip.isSunk()) {
                            statusLabel.setText(String.format("💥 Your %s was SUNK by enemy %s! 💥", 
                                hitShip.getName(), attackingShip.getName()));
                        } else {
                            statusLabel.setText(String.format("🎯 Enemy %s hit your %s!", 
                                attackingShip.getName(), hitShip.getName()));
                        }
                        
                        // Perform ability
                        attackingShip.performAbility(playerBoard, row, col);
                        updatePlayerBoard();
                        
                        // Check for game over
                        if (playerBoard.allShipsSunk()) {
                            gameOver(false);
                            return;
                        }
                    } else {
                        statusLabel.setText(String.format("💨 Enemy %s missed!", attackingShip.getName()));
                    }
                    
                    // Check if game should continue
                    if (!computerBoard.allShipsSunk()) {
                        // Start player's turn
                        Timer turnTimer = new Timer(1500, e -> {
                            statusLabel.setText("Your turn - Select a target!");
                            updateBoardTitles(true);
                            enableBoards();
                            ((Timer)e.getSource()).stop();
                        });
                        turnTimer.setRepeats(false);
                        turnTimer.start();
                    } else {
                        gameOver(true);
                    }
                });
            }
        }
    }

    private Ship selectRandomNonSunkShip(Board board) {
        List<Ship> ships = board.getShips();
        List<Ship> availableShips = new ArrayList<>();
        
        // Get all non-sunk ships
        for (Ship ship : ships) {
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

    private void disableBoards() {
        setButtonsEnabled(computerBoardPanel, false);
        setButtonsEnabled(playerBoardPanel, false);
    }

    private void enableBoards() {
        if (!gameStarted) return;
        setButtonsEnabled(computerBoardPanel, true);
        setButtonsEnabled(playerBoardPanel, false);
    }

    private void setButtonsEnabled(JPanel panel, boolean enabled) {
        for (Component comp : panel.getComponents()) {
            if (comp instanceof JButton) {
                JButton button = (JButton) comp;
                if (enabled) {
                    // Only enable buttons that haven't been shot at
                    int row = -1, col = -1;
                    for (int i = 0; i < BOARD_SIZE; i++) {
                        for (int j = 0; j < BOARD_SIZE; j++) {
                            if (panel == computerBoardPanel && button == computerButtons[i][j]) {
                                row = i;
                                col = j;
                                break;
                            }
                        }
                        if (row != -1) break;
                    }
                    if (row != -1 && !computerBoard.hasBeenShot(row, col)) {
                        button.setEnabled(true);
                    }
                } else {
                    button.setEnabled(false);
                }
            }
        }
    }

    private void startGame() {
        SwingUtilities.invokeLater(() -> {
            gameStarted = true;
            isPlacingShips = false;
            
            // Place computer ships
            placeComputerShips();
            
            statusLabel.setText("Game started! Take your shot!");
            carrierModeButton.setVisible(true);
            attackShipSelector.setEnabled(true);
            
            // Reset and start game timer
            gameTimeSeconds = 0;
            updateTimerLabel();
            if (gameTimer != null) {
                gameTimer.stop();
            }
            gameTimer = new Timer(1000, e -> {
                gameTimeSeconds++;
                updateTimerLabel();
            });
            gameTimer.start();
            
            // Start board update timer
            if (boardUpdateTimer != null) {
                boardUpdateTimer.stop();
            }
            boardUpdateTimer = new Timer(100, e -> {
                updatePlayerBoard();
                updateComputerBoard();
            });
            boardUpdateTimer.start();
            
            enableBoards();
        });
    }

    private void autoPlaceShips() {
        // Clear any existing ships
        playerBoard = new Board();
        
        // Create and place ships in random positions
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
                int row = (int) (Math.random() * BOARD_SIZE);
                int col = (int) (Math.random() * BOARD_SIZE);
                boolean horizontal = Math.random() < 0.5;
                ship.setHorizontal(horizontal);
                if (playerBoard.placeShip(ship, row, col, horizontal)) {
                    placed = true;
                    placedShipTypes.add(ship.getName());
                }
            }
        }

        // Update the board display
        updatePlayerBoard();

        // Disable all ship selection buttons
        for (Component c : shipSelectionPanel.getComponents()) {
            if (c instanceof JButton) {
                JButton button = (JButton) c;
                if (Arrays.asList(SHIP_TYPES).contains(button.getText())) {
                    button.setEnabled(false);
                }
            }
        }

        // Start the game if all ships are placed
        if (placedShipTypes.size() == SHIP_TYPES.length) {
            startGame();
        }
    }

    private void gameOver(boolean playerWon) {
        // Stop all timers
        if (gameTimer != null) {
            gameTimer.stop();
        }
        if (boardUpdateTimer != null) {
            boardUpdateTimer.stop();
        }
        if (boardSwitchTimer != null) {
            boardSwitchTimer.stop();
        }
        
        // Disable all boards
        disableBoards();
        
        // Update UI
        String message = playerWon ? 
            "🎉 Congratulations! You've won! 🎉" : 
            "💀 Game Over - The enemy has destroyed your fleet! 💀";
        statusLabel.setText(message);
        
        // Show final game state
        updatePlayerBoard();
        updateComputerBoard();
        
        // Show game over dialog
        String timeStr = String.format("%d:%02d", gameTimeSeconds / 60, gameTimeSeconds % 60);
        String title = playerWon ? "Victory!" : "Defeat!";
        String fullMessage = String.format("%s\nGame Time: %s", message, timeStr);
        
        int choice = JOptionPane.showConfirmDialog(
            this,
            fullMessage + "\nWould you like to play again?",
            title,
            JOptionPane.YES_NO_OPTION,
            JOptionPane.INFORMATION_MESSAGE
        );
        
        if (choice == JOptionPane.YES_OPTION) {
            // Reset and start new game
            dispose();
            new BattleshipGUI().setVisible(true);
        } else {
            dispose();
        }
    }
} 