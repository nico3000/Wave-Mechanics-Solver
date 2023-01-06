package dev.nicotopia.wms.gui;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JLabel;

import dev.nicotopia.wms.Game;
import dev.nicotopia.wms.Game.Direction;

public class BorderMouseAdapter implements MouseListener, MouseMotionListener {
    private record RegisterData(Direction dir, int idx) {
    }

    private final Game game;
    private final Map<JLabel, RegisterData> labels = new HashMap<>();
    private boolean down = false;

    public BorderMouseAdapter(Game game) {
        this.game = game;
    }

    public void registerLabel(JLabel label, Direction dir, int idx) {
        this.labels.put(label, new RegisterData(dir, idx));
        label.addMouseListener(this);
        label.addMouseMotionListener(this);
    }

    @Override
    public void mouseDragged(MouseEvent e) {

    }

    @Override
    public void mouseMoved(MouseEvent e) {
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            this.down = true;
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            this.down = false;
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        RegisterData data = this.labels.get(e.getSource());
        if (this.down && data != null && data.idx != 0) {
            this.game.setSplit(data.dir, data.idx);
        }
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }
}
