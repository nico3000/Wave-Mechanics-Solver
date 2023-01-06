package dev.nicotopia.wms.gui;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.swing.JLabel;

import dev.nicotopia.wms.Game;
import dev.nicotopia.wms.Game.Color;
import dev.nicotopia.wms.Game.Direction;;

public class CellMouseListener implements MouseListener, MouseWheelListener {
    private record Position(int x, int y) {
    }

    private final Game game;
    private Optional<MouseEvent> mouseBtn1DownEvt = Optional.empty();
    private Optional<Color> paintColor = Optional.empty();
    private final Map<JLabel, Position> labelPositions = new HashMap<>();

    public CellMouseListener(Game game) {
        this.game = game;
    }

    public void registerLabel(JLabel label, int x, int y) {
        label.addMouseListener(this);
        label.addMouseWheelListener(this);
        this.labelPositions.put(label, new Position(x, y));
    }

    @Override
    public void mousePressed(MouseEvent e) {
        Position p = this.labelPositions.get(e.getSource());
        if (p != null) {
            if (e.getButton() == MouseEvent.BUTTON1) {
                this.mouseBtn1DownEvt = Optional.ofNullable(e);
            } else if (e.getButton() == MouseEvent.BUTTON2) {
                int number = this.game.getNumber(p.x, p.y);
                Color color = this.game.getColor(p.x, p.y);
                this.game.setCell(p.x, p.y, color == Color.NONE ? Color.COLOR_0 : Color.NONE, number);
            } else if (e.getButton() == MouseEvent.BUTTON3) {
                this.game.switchColor(p.x, p.y);
                this.paintColor = Optional.of(this.game.getColor(p.x, p.y));
            }
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            this.mouseBtn1DownEvt.map(evt -> evt.getSource()).map(this.labelPositions::get).ifPresent(p -> {
                int hor = e.getX() - this.mouseBtn1DownEvt.get().getX();
                int ver = e.getY() - this.mouseBtn1DownEvt.get().getY();
                if (16 < Math.max(Math.abs(hor), Math.abs(ver))) {
                    try {
                        if (Math.abs(hor) < Math.abs(ver)) {
                            this.game.wave(p.x, p.y, ver < 0 ? Direction.UP : Direction.DOWN);
                        } else {
                            this.game.wave(p.x, p.y, hor < 0 ? Direction.LEFT : Direction.RIGHT);
                        }
                    } catch (UnsupportedOperationException ex) {
                    }
                }
            });
        } else if (e.getButton() == MouseEvent.BUTTON3) {
            this.paintColor = Optional.empty();
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        Position p = this.labelPositions.get(e.getSource());
        if (p != null && this.paintColor.isPresent()) {
            this.game.setCell(p.x, p.y, this.paintColor.get(), this.game.getNumber(p.x, p.y));
        }
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        Position p = this.labelPositions.get(e.getSource());
        if (p != null) {
            Color color = this.game.getColor(p.x, p.y);
            int n = this.game.getNumber(p.x, p.y) + (e.getUnitsToScroll() < 0 ? 1 : -1);
            n = Math.max(0, Math.min(n, Math.max(this.game.getWidth(), this.game.getHeight())));
            this.game.setCell(p.x, p.y, color, n);
        }
    }
}
