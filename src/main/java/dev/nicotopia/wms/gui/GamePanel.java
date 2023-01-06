package dev.nicotopia.wms.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;

import dev.nicotopia.wms.Game;
import dev.nicotopia.wms.Game.Direction;
import dev.nicotopia.wms.Solver;

public class GamePanel extends JPanel implements Game.CellListener, Game.SplitListener {
    private static final Dimension CELL_DIMENSION = new Dimension(76, 80);
    private static final int CELL_BORDER_WIDTH = 5;
    private static final int GAME_BORDER_WIDTH = 24;
    private static final Insets CELL_INSETS = new Insets(8, 10, 8, 10);
    private static final Insets TOP_BORDER_INSETS = new Insets(0, 0, 19, 0);
    private static final Insets LEFT_BORDER_INSETS = new Insets(0, 0, 0, 32);
    private static final Insets BOTTOM_BORDER_INSETS = new Insets(28, 0, 0, 0);
    private static final Insets RIGHT_BORDER_INSETS = new Insets(0, 16, 0, 0);
    private static final Color CELL_COLOR_0 = new Color(52, 125, 255);
    private static final Color CELL_COLOR_1 = new Color(255, 189, 52);
    private static final Color BORDER_COLOR_0 = new Color(0, 72, 213);
    private static final Color BORDER_COLOR_1 = new Color(254, 136, 0);
    private static final Font CELL_FONT = new Font("DejaVu Sans", Font.BOLD, 28);
    private static final Font CELL_SOLUTION_FONT = new Font("DejaVu Sans Mono", Font.PLAIN, 20);

    private Game game;
    private List<JLabel> cellLabels = new ArrayList<>();
    private Map<Direction, List<JLabel>> borderLabels = new HashMap<>();
    private CellMouseListener cellMouseListener;
    private BorderMouseAdapter borderMouseAdapter;

    public GamePanel() {
        Arrays.stream(Direction.values()).forEach(d -> this.borderLabels.put(d, new ArrayList<>()));
    }

    public Game getGame() {
        return this.game;
    }

    public void setGame(Game game) {
        this.game = game;
        this.cellLabels.clear();
        this.borderLabels.values().forEach(List::clear);
        this.removeAll();
        if (this.game != null) {
            this.cellMouseListener = new CellMouseListener(game);
            this.borderMouseAdapter = new BorderMouseAdapter(game);
            this.game.addCellListener(this);
            this.game.addSplitListener(this);
            this.setLayout(new GridBagLayout());
            for (int x = 0; x < this.game.getWidth(); ++x) {
                this.addBorderLabel(Direction.UP, x);
            }
            for (int y = 0; y < this.game.getHeight(); ++y) {
                this.addBorderLabel(Direction.LEFT, y);
                for (int x = 0; x < this.game.getWidth(); ++x) {
                    this.addCellLabel(x, y);
                }
                this.addBorderLabel(Direction.RIGHT, y);
            }
            for (int x = 0; x < this.game.getWidth(); ++x) {
                this.addBorderLabel(Direction.DOWN, x);
            }
            Arrays.stream(Direction.values()).forEach(this::onSplitChange);
        }

    }

    public void showSolution(Solver solver) {
        for (int y = 0; y < this.game.getHeight(); ++y) {
            for (int x = 0; x < this.game.getWidth(); ++x) {
                JLabel label = this.cellLabels.get(y * this.game.getWidth() + x);
                label.setFont(CELL_SOLUTION_FONT);
                String steps[] = solver.getCellDirections(x, y).stream().map(d -> switch (d) {
                    case LEFT -> "\u2190";
                    case UP -> "\u2191";
                    case RIGHT -> "\u2192";
                    case DOWN -> "\u2193";
                }).toArray(String[]::new);
                StringBuilder solution = new StringBuilder("<html>");
                for (int i = 0; i < steps.length; ++i) {
                    solution.append(steps[i]).append((i + 1) % 3 == 0 ? "<br>" : " ");
                }
                label.setText(solution.append("</html>").toString());
            }
        }
    }

    public void hideSolution() {
        for (int y = 0; y < this.game.getHeight(); ++y) {
            for (int x = 0; x < this.game.getWidth(); ++x) {
                this.onCellChange(x, y);
            }
        }
    }

    private void addCellLabel(int x, int y) {
        int number = this.game.getNumber(x, y);
        JLabel label = new JLabel(number == 0 ? "" : String.valueOf(number), SwingConstants.CENTER);
        label.setForeground(Color.WHITE);
        label.setPreferredSize(CELL_DIMENSION);
        this.cellMouseListener.registerLabel(label, x, y);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = CELL_INSETS;
        gbc.gridx = x + 1;
        gbc.gridy = y + 1;
        this.add(label, gbc);
        this.cellLabels.add(label);
        this.onCellChange(x, y);
    }

    private void addBorderLabel(Direction dir, int i) {
        JLabel borderLabel = new JLabel();
        borderLabel.setOpaque(true);
        borderLabel.setPreferredSize(switch (dir) {
            case UP, DOWN -> new Dimension(0, GAME_BORDER_WIDTH);
            case LEFT, RIGHT -> new Dimension(GAME_BORDER_WIDTH, 0);
        });
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = switch (dir) {
            case LEFT -> LEFT_BORDER_INSETS;
            case RIGHT -> RIGHT_BORDER_INSETS;
            case UP -> TOP_BORDER_INSETS;
            case DOWN -> BOTTOM_BORDER_INSETS;
        };
        gbc.gridx = switch (dir) {
            case LEFT -> 0;
            case RIGHT -> this.game.getWidth() + 1;
            default -> i == 0 ? 0 : i + 1;
        };
        gbc.gridy = switch (dir) {
            case UP -> 0;
            case DOWN -> this.game.getHeight() + 1;
            default -> i == 0 ? 0 : i + 1;
        };
        if ((dir == Direction.UP || dir == Direction.DOWN) && (i == 0 || i == this.game.getWidth() - 1)) {
            gbc.gridwidth = 2;
        }
        if ((dir == Direction.LEFT || dir == Direction.RIGHT) && (i == 0 || i == this.game.getHeight() - 1)) {
            gbc.gridheight = 2;
        }
        this.borderMouseAdapter.registerLabel(borderLabel, dir, i);
        this.add(borderLabel, gbc);
        this.borderLabels.get(dir).add(borderLabel);
    }

    @Override
    public void onCellChange(int x, int y) {
        JLabel label = this.cellLabels.get(y * this.game.getWidth() + x);
        Game.Color color = this.game.getColor(x, y);
        label.setFont(CELL_FONT);
        label.setOpaque(color != Game.Color.NONE);
        label.setBackground(this.getColor(color));
        label.setBorder(new LineBorder(switch (color) {
            case NONE -> Color.WHITE;
            case COLOR_0 -> BORDER_COLOR_0;
            case COLOR_1 -> BORDER_COLOR_1;
        }, CELL_BORDER_WIDTH));
        int number = this.game.getNumber(x, y);
        label.setText(number == 0 ? "" : String.valueOf(number));
    }

    @Override
    public void onSplitChange(Direction dir) {
        List<JLabel> labels = this.borderLabels.get(dir);
        for (int i = 0; i < labels.size(); ++i) {
            JLabel label = labels.get(i);
            label.setBackground(this.getColor(this.game.getBorderColor(dir, i)));
        }
    }

    private Color getColor(Game.Color color) {
        return switch (color) {
            case NONE -> Color.WHITE;
            case COLOR_0 -> CELL_COLOR_0;
            case COLOR_1 -> CELL_COLOR_1;
        };
    }
}
