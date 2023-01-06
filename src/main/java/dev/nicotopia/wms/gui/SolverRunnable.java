package dev.nicotopia.wms.gui;

import dev.nicotopia.wms.Game;
import dev.nicotopia.wms.Solver;

public class SolverRunnable implements Runnable {
    public interface FinishedCallback {
        public void onSolvingFinished(boolean solved, Solver solver);
    }

    private final FinishedCallback callback;
    private Solver solver;

    public SolverRunnable(FinishedCallback callback) {
        this.callback = callback;
    }

    @Override
    public synchronized void run() {
        while (!Thread.currentThread().isInterrupted()) {
            while (this.solver == null) {
                try {
                    this.wait();
                } catch (InterruptedException ex) {
                }
            }
            if (this.solver != null) {
                callback.onSolvingFinished(this.solver.solve(), solver);
                this.solver = null;
            }
        }
    }

    public synchronized void startSolving(Game game) {
        this.solver = new Solver(game);
        this.notifyAll();
    }
}
