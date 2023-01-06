package dev.nicotopia.wms;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Set;

public class Game {
    public interface CellListener {
        public void onCellChange(int x, int y);
    }

    public interface SplitListener {
        public void onSplitChange(Direction dir);
    }

    public enum Color {
        NONE, COLOR_0, COLOR_1
    }

    public enum Direction {
        LEFT, UP, RIGHT, DOWN
    }

    public enum SplitType {
        NONE, HOR, VERT, HOR_AND_VERT, VERT_AND_HOR, TOP_LEFT, TOP_RIGHT, BOTTOM_RIGHT, BOTTOM_LEFT
    }

    private final Color colors[][];
    private final int numbers[][];
    private SplitType splitType;
    private final EnumMap<Direction, Integer> splits = new EnumMap<>(Direction.class);
    private final Set<CellListener> cellListeners = new HashSet<>();
    private final Set<SplitListener> splitListeners = new HashSet<>();
    private Color topLeftBorderColor = Color.COLOR_0;

    public Game(int width, int height) {
        this.colors = new Color[height][width];
        this.numbers = new int[height][width];
        this.splitType = SplitType.NONE;
        Arrays.stream(this.colors).forEach(a -> Arrays.fill(a, Color.COLOR_1));
    }

    public int getWidth() {
        return this.colors[0].length;
    }

    public int getHeight() {
        return this.colors.length;
    }

    public Color getTopLeftBorderColor() {
        return this.topLeftBorderColor;
    }

    public void switchTopLeftBorderColor() {
        this.topLeftBorderColor = this.topLeftBorderColor == Color.COLOR_0 ? Color.COLOR_1 : Color.COLOR_0;
        Arrays.stream(Direction.values()).forEach(this::notifySplitListeners);
    }

    public Color getBorderColor(Direction dir, int pos) {
        int split = this.getSplit(dir);
        switch (this.splitType) {
            case NONE:
                return this.topLeftBorderColor;
            case HOR:
                return switch (dir) {
                    case UP -> this.topLeftBorderColor;
                    case DOWN -> this.switchColor(this.topLeftBorderColor);
                    default -> pos < split ? this.topLeftBorderColor : this.switchColor(this.topLeftBorderColor);
                };
            case VERT:
                return switch (dir) {
                    case LEFT -> this.topLeftBorderColor;
                    case RIGHT -> this.switchColor(this.topLeftBorderColor);
                    default -> pos < split ? this.topLeftBorderColor : this.switchColor(this.topLeftBorderColor);
                };
            case HOR_AND_VERT, VERT_AND_HOR:
                return switch (dir) {
                    case LEFT, UP -> pos < split ? this.topLeftBorderColor : this.switchColor(this.topLeftBorderColor);
                    case RIGHT, DOWN ->
                        pos < split ? this.switchColor(this.topLeftBorderColor) : this.topLeftBorderColor;
                };
            case TOP_LEFT:
                return switch (dir) {
                    case LEFT, UP -> pos < split ? this.topLeftBorderColor : this.switchColor(this.topLeftBorderColor);
                    default -> this.switchColor(this.topLeftBorderColor);
                };
            case TOP_RIGHT:
                return switch (dir) {
                    case UP -> pos < split ? this.topLeftBorderColor : this.switchColor(this.topLeftBorderColor);
                    case RIGHT -> pos < split ? this.switchColor(this.topLeftBorderColor) : this.topLeftBorderColor;
                    default -> this.topLeftBorderColor;
                };
            case BOTTOM_RIGHT:
                return switch (dir) {
                    case RIGHT, DOWN ->
                        pos < split ? this.topLeftBorderColor : this.switchColor(this.topLeftBorderColor);
                    default -> this.topLeftBorderColor;
                };
            case BOTTOM_LEFT:
                return switch (dir) {
                    case LEFT -> pos < split ? this.topLeftBorderColor : this.switchColor(this.topLeftBorderColor);
                    case DOWN -> pos < split ? this.switchColor(this.topLeftBorderColor) : this.topLeftBorderColor;
                    default -> this.topLeftBorderColor;
                };
            default:
                return null;
        }
    }

    public SplitType getSplitType() {
        return this.splitType;
    }

    public void setSplitType(SplitType splitType) {
        this.splitType = splitType;
        this.splits.put(Direction.LEFT, this.getWidth() / 2);
        this.splits.put(Direction.UP, this.getHeight() / 2);
        this.splits.put(Direction.RIGHT, this.getWidth() / 2);
        this.splits.put(Direction.DOWN, this.getHeight() / 2);
        Arrays.stream(Direction.values()).forEach(this::notifySplitListeners);
    }

    public void setSplit(Direction dir, int split) {
        if ((this.splitType == SplitType.HOR || this.splitType == SplitType.HOR_AND_VERT)
                && (dir == Direction.LEFT || dir == Direction.RIGHT)) {
            this.setSingleSplit(Direction.LEFT, split);
            this.setSingleSplit(Direction.RIGHT, split);
        } else if ((this.splitType == SplitType.VERT || this.splitType == SplitType.VERT_AND_HOR)
                && (dir == Direction.UP || dir == Direction.DOWN)) {
            this.setSingleSplit(Direction.UP, split);
            this.setSingleSplit(Direction.DOWN, split);
        } else if (this.splitType != SplitType.NONE) {
            this.setSingleSplit(dir, split);
        }
    }

    private void setSingleSplit(Direction dir, int split) {
        this.splits.put(dir, split);
        this.notifySplitListeners(dir);
    }

    public int getSplit(Direction dir) {
        return this.splits.getOrDefault(dir, 0);
    }

    public void setCell(int x, int y, Color color, int number) {
        this.colors[y][x] = color;
        this.numbers[y][x] = number;
        this.notifyCellListeners(x, y);
    }

    public void switchColor(int x, int y) {
        this.colors[y][x] = this.colors[y][x] == Color.COLOR_0 ? Color.COLOR_1 : Color.COLOR_0;
        this.notifyCellListeners(x, y);
    }

    public int getNumber(int x, int y) {
        return this.numbers[y][x];
    }

    public Color getColor(int x, int y) {
        return this.colors[y][x];
    }

    public boolean isCellCorrect(int x, int y) {
        Color c = this.getColor(x, y);
        if (c == Color.NONE) {
            return true;
        }
        return switch (this.splitType) {
            case NONE -> c == this.topLeftBorderColor;
            case HOR -> y < this.getSplit(Direction.LEFT) ^ c != this.topLeftBorderColor;
            case VERT -> x < this.getSplit(Direction.UP) ^ c != this.topLeftBorderColor;
            case HOR_AND_VERT -> ((y < this.getSplit(Direction.LEFT) && x < this.getSplit(Direction.UP))
                    || ((this.getSplit(Direction.LEFT) <= y && this.getSplit(Direction.DOWN) <= x)))
                    ^ c != this.topLeftBorderColor;
            case VERT_AND_HOR -> ((x < this.getSplit(Direction.UP) && y < this.getSplit(Direction.LEFT))
                    || ((this.getSplit(Direction.UP) <= x && this.getSplit(Direction.RIGHT) <= y)))
                    ^ c != this.topLeftBorderColor;
            case TOP_LEFT ->
                (x < this.getSplit(Direction.UP) && y < this.getSplit(Direction.LEFT)) ^ c != this.topLeftBorderColor;
            case TOP_RIGHT ->
                (this.getSplit(Direction.UP) <= x && y < this.getSplit(Direction.RIGHT)) ^ c == this.topLeftBorderColor;
            case BOTTOM_LEFT ->
                (x < this.getSplit(Direction.DOWN) && this.getSplit(Direction.LEFT) <= y)
                        ^ c == this.topLeftBorderColor;
            case BOTTOM_RIGHT ->
                (this.getSplit(Direction.DOWN) <= x && this.getSplit(Direction.RIGHT) <= y)
                        ^ c == this.topLeftBorderColor;
            default -> false;
        };
    }

    public boolean isSolved() {
        for (int y = 0; y < this.getHeight(); ++y) {
            for (int x = 0; x < this.getWidth(); ++x) {
                if (!this.isCellCorrect(x, y)) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean wave(int x, int y, Direction dir) {
        if (this.getNumber(x, y) == 0 || this.getColor(x, y) == Color.NONE) {
            throw new UnsupportedOperationException();
        }
        if (!this.doWave(x, y, dir, this.numbers[y][x]--)) {
            ++this.numbers[y][x];
            return false;
        }
        return true;
    }

    public void reverseWave(int x, int y, Direction dir) {
        if (this.getColor(x, y) == Color.NONE) {
            throw new UnsupportedOperationException();
        }
        if (!this.doWave(x, y, dir, ++this.numbers[y][x])) {
            --this.numbers[y][x];
        }
    }

    private boolean doWave(int x, int y, Direction dir, int number) {
        if ((x == 0 && dir == Direction.LEFT) || (y == 0 && dir == Direction.UP)
                || (x == this.getWidth() - 1 && dir == Direction.RIGHT)
                || (y == this.getHeight() - 1 && dir == Direction.DOWN)) {
            return false;
        }
        int dx = switch (dir) {
            case LEFT -> -1;
            case RIGHT -> 1;
            case UP, DOWN -> 0;
        };
        int dy = switch (dir) {
            case UP -> -1;
            case DOWN -> 1;
            case LEFT, RIGHT -> 0;
        };
        for (int i = 0; i <= number; ++i) {
            int x1 = x + i * dx;
            int y1 = y + i * dy;
            if (0 <= x1 && x1 < this.getWidth() && 0 <= y1 && y1 < this.getHeight()) {
                this.switchColor(x1, y1);
            }
        }
        return true;
    }

    public void addCellListener(CellListener l) {
        this.cellListeners.add(l);
    }

    public void removeCellListener(CellListener l) {
        this.cellListeners.remove(l);
    }

    private void notifyCellListeners(int x, int y) {
        this.cellListeners.forEach(l -> l.onCellChange(x, y));
    }

    public void addSplitListener(SplitListener l) {
        this.splitListeners.add(l);
    }

    public void removeSplitListener(SplitListener l) {
        this.splitListeners.remove(l);
    }

    private void notifySplitListeners(Direction dir) {
        this.splitListeners.forEach(l -> l.onSplitChange(dir));
    }

    private Color switchColor(Color color) {
        return switch (color) {
            case COLOR_0 -> Color.COLOR_1;
            case COLOR_1 -> Color.COLOR_0;
            default -> color;
        };
    }

    public Game copy() {
        Game copy = new Game(this.getWidth(), this.getHeight());
        copy.setSplitType(this.splitType);
        for (int y = 0; y < this.getHeight(); ++y) {
            for (int x = 0; x < this.getWidth(); ++x) {
                copy.setCell(x, y, this.getColor(x, y), this.getNumber(x, y));
            }
        }
        Arrays.stream(Direction.values()).forEach(d -> copy.setSplit(d, this.getSplit(d)));
        copy.topLeftBorderColor = this.topLeftBorderColor;
        return copy;
    }
}
