package dev.nicotopia.wms;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import dev.nicotopia.wms.Game.Color;
import dev.nicotopia.wms.Game.Direction;

public class Solver {
    private record Position(int x, int y) {
    }

    private final Game game;
    private final Map<Position, Stack<Direction>> steps = new HashMap<>();
    private final Map<Position, Set<Position>> reachableBy = new HashMap<>();

    public Solver(Game game) {
        this.game = game.copy();
        this.positions().forEach(b -> this.reachableBy.put(b,
                new HashSet<>(this.positions().filter(a -> this.canAReachB(a, b)).toList())));
    }

    public List<Direction> getCellDirections(int x, int y) {
        List<Direction> l = this.steps.get(new Position(x, y));
        return l == null ? Collections.emptyList() : Collections.unmodifiableList(l);
    }

    public boolean solve() {
        if (!this.isEveryWrongColoredCellReachable()) {
            return false;
        } else if (this.game.isSolved()) {
            return true;
        }
        Position p = this.positions()
                .filter(p0 -> this.game.getNumber(p0.x, p0.y) != 0 && this.game.getColor(p0.x, p0.y) != Color.NONE)
                .findAny().get();
        Stack<Direction> dirs = this.steps.get(p);
        if (dirs == null) {
            this.steps.put(p, dirs = new Stack<>());
        }
        for (Direction dir : Direction.values()) {
            if (this.executeWave(p, dir)) {
                dirs.push(dir);
                if (this.solve()) {
                    return true;
                }
                dirs.pop();
                this.reverseWave(p, dir);
            }
        }
        return false;
    }

    private Stream<Position> positions() {
        return IntStream.range(0, this.game.getHeight())
                .mapToObj(y -> IntStream.range(0, this.game.getWidth()).mapToObj(x -> new Position(x, y)).toList())
                .collect(LinkedList<Position>::new, LinkedList::addAll, LinkedList::addAll).stream();
    }

    private boolean canAReachB(Position a, Position b) {
        int n = this.game.getNumber(a.x, a.y);
        return !a.equals(b) && ((a.x == b.x && Math.abs(b.y - a.y) <= n) || (a.y == b.y && Math.abs(b.x - a.x) <= n));
    }

    private boolean isEveryWrongColoredCellReachable() {
        return this.positions().filter(p -> !this.game.isCellCorrect(p.x, p.y))
                .allMatch(p -> this.game.getNumber(p.x, p.y) != 0
                        || !this.reachableBy.getOrDefault(p, Collections.emptySet()).isEmpty());
    }

    private boolean executeWave(Position p, Direction dir) {
        int n = this.game.getNumber(p.x, p.y);
        if (this.game.wave(p.x, p.y, dir)) {
            this.exactReachables(p, n).map(this.reachableBy::get).forEach(set -> set.remove(p));
            return true;
        }
        return false;
    }

    private void reverseWave(Position p, Direction dir) {
        int n = this.game.getNumber(p.x, p.y);
        this.exactReachables(p, n + 1).map(this.reachableBy::get).forEach(set -> set.add(p));
        this.game.reverseWave(p.x, p.y, dir);
    }

    private Stream<Position> exactReachables(Position base, int n) {
        return Arrays
                .asList(new Position(base.x - n, base.y), new Position(base.x + n, base.y),
                        new Position(base.x, base.y - n), new Position(base.x, base.y + n))
                .stream()
                .filter(b -> 0 <= b.x && b.x < this.game.getWidth() && 0 <= b.y && b.y < this.game.getHeight());
    }
}
