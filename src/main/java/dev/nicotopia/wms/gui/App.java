package dev.nicotopia.wms.gui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.SpinnerNumberModel;

import dev.nicotopia.wms.Game;
import dev.nicotopia.wms.Game.SplitType;
import dev.nicotopia.wms.Solver;

public class App extends JFrame {
    public static void main(String[] args) throws IOException {
        App app = new App();
        app.setGame(new Game(6, 5));
        app.setVisible(true);
    }

    private final JToolBar toolBar;
    private final GamePanel gamePanel;
    private final JSpinner rowsSpinner = new JSpinner(new SpinnerNumberModel(5, 5, 20, 1));
    private final JSpinner colsSpinner = new JSpinner(new SpinnerNumberModel(5, 5, 25, 1));
    private final SolverRunnable solverRunnable = new SolverRunnable(this::onSolvingFinished);
    private final Map<SplitType, JToggleButton> splitTypeButtons = new EnumMap<>(SplitType.class);
    private final JToggleButton solveButton;

    public App() throws IOException {
        this.setTitle("Wave Mechanics Solver");
        this.setResizable(false);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.toolBar = new JToolBar();
        this.gamePanel = new GamePanel();

        this.toolBar.setFloatable(false);
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(2, 1));
        panel.add(new JLabel("Width"));
        panel.add(new JLabel("Height"));
        this.toolBar.add(panel);
        panel = new JPanel();
        panel.setLayout(new GridLayout(2, 1));
        panel.add(this.colsSpinner);
        panel.add(this.rowsSpinner);
        this.toolBar.add(panel);
        JButton confirmButton = new JButton(new ImageIcon(ImageIO.read(App.class.getResource("/confirm.png"))));
        confirmButton.addActionListener(evt -> this
                .setGame(new Game((Integer) this.colsSpinner.getValue(), (Integer) this.rowsSpinner.getValue())));
        this.toolBar.add(confirmButton);
        this.toolBar.addSeparator();
        ButtonGroup bg = new ButtonGroup();
        this.addSplitTypeButton(bg, SplitType.NONE);
        this.addSplitTypeButton(bg, SplitType.HOR);
        this.addSplitTypeButton(bg, SplitType.HOR_AND_VERT);
        this.addSplitTypeButton(bg, SplitType.VERT);
        this.addSplitTypeButton(bg, SplitType.VERT_AND_HOR);
        this.addSplitTypeButton(bg, SplitType.TOP_LEFT);
        this.addSplitTypeButton(bg, SplitType.TOP_RIGHT);
        this.addSplitTypeButton(bg, SplitType.BOTTOM_LEFT);
        this.addSplitTypeButton(bg, SplitType.BOTTOM_RIGHT);
        JButton swapButton = new JButton(new ImageIcon(ImageIO.read(App.class.getResource("/swap.png"))));
        swapButton.addActionListener(evt -> {
            if (this.gamePanel.getGame() != null) {
                this.gamePanel.getGame().switchTopLeftBorderColor();
            }
        });
        this.toolBar.addSeparator();
        this.toolBar.add(swapButton);
        this.solveButton = new JToggleButton(new ImageIcon(ImageIO.read(App.class.getResource("/solve.png"))),
                false);
        solveButton.addActionListener(this::onSolutionButton);
        this.toolBar.addSeparator();
        this.toolBar.add(solveButton);
        this.add(this.toolBar, BorderLayout.NORTH);
        this.add(this.gamePanel, BorderLayout.SOUTH);
        Thread solverThread = new Thread(this.solverRunnable);
        solverThread.setDaemon(true);
        solverThread.start();
    }

    public void setGame(Game game) {
        this.gamePanel.setGame(game);
        this.colsSpinner.setValue(game.getWidth());
        this.rowsSpinner.setValue(game.getHeight());
        this.splitTypeButtons.get(game.getSplitType()).setSelected(true);
        this.solveButton.setSelected(false);
        this.pack();
    }

    private void onSolutionButton(ActionEvent evt) {
        if (((JToggleButton) evt.getSource()).isSelected()) {
            this.getContentPane().setEnabled(true);
            this.solverRunnable.startSolving(this.gamePanel.getGame());
        } else {
            this.gamePanel.hideSolution();
        }
    }

    public void onSolvingFinished(boolean solved, Solver solver) {
        if (solved) {
            this.gamePanel.showSolution(solver);
        } else {
            JOptionPane.showMessageDialog(this, "Game is not solvable", "Result", JOptionPane.WARNING_MESSAGE);
            this.gamePanel.hideSolution();
            this.solveButton.setSelected(false);
        }
        this.getContentPane().setEnabled(true);
    }

    private void addSplitTypeButton(ButtonGroup bg, SplitType splitType) throws IOException {
        boolean selected = bg.getButtonCount() == 0;
        JToggleButton b = new JToggleButton(
                new ImageIcon(ImageIO.read(App.class.getResource(switch (splitType) {
                    case NONE -> "/split_none.png";
                    case HOR -> "/split_hor.png";
                    case HOR_AND_VERT -> "/split_hor_vert.png";
                    case VERT -> "/split_vert.png";
                    case VERT_AND_HOR -> "/split_vert_hor.png";
                    case TOP_LEFT -> "/split_top_left.png";
                    case TOP_RIGHT -> "/split_top_right.png";
                    case BOTTOM_LEFT -> "/split_bottom_left.png";
                    case BOTTOM_RIGHT -> "/split_bottom_right.png";
                }))), selected);
        b.addActionListener(evt -> {
            if (b.isSelected() && this.gamePanel.getGame() != null) {
                this.gamePanel.getGame().setSplitType(splitType);
            }
        });
        bg.add(b);
        this.toolBar.add(b);
        this.splitTypeButtons.put(splitType, b);
    }
}
