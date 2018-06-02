package com.syl.thread.example;

import java.util.Random;
import java.util.concurrent.*;

/**
 * CellularAutomata
 *
 * Coordinating computation in a cellular automaton with CyclicBarrier
 *
 * @author Brian Goetz and Tim Peierls
 */
public class CellularAutomata {
    private final Board mainBoard;
    private final CyclicBarrier barrier;
    private final Worker[] workers;

    public static void main(String[] args) {
        System.out.println(Runtime.getRuntime().availableProcessors());
        CellularAutomata c = new CellularAutomata(new Board() {
            Random r = new Random();
            @Override
            public int getMaxX() {
                return r.nextInt(100);
            }

            @Override
            public int getMaxY() {
                return r.nextInt(100);
            }

            @Override
            public int getValue(int x, int y) {
                return x+y;
            }

            @Override
            public int setNewValue(int x, int y, int value) {
                return value;
            }

            @Override
            public void commitNewValues() {

            }

            @Override
            public boolean hasConverged() {
                return r.nextBoolean();
            }

            @Override
            public void waitForConvergence() {

            }

            @Override
            public Board getSubBoard(int numPartitions, int index) {

                return this;
            }
        });
        c.start();
    }
    public CellularAutomata(Board board) {
        this.mainBoard = board;
        int count = Runtime.getRuntime().availableProcessors();
        this.barrier = new CyclicBarrier(count,
                new Runnable() {
                    public void run() {
                        mainBoard.commitNewValues();
                    }});
        this.workers = new Worker[count];
        for (int i = 0; i < count; i++)
            workers[i] = new Worker(mainBoard.getSubBoard(count, i));
    }

    private class Worker implements Runnable {
        private final Board board;

        public Worker(Board board) { this.board = board; }
        public void run() {
            while (!board.hasConverged()) {
                for (int x = 0; x < board.getMaxX(); x++)
                    for (int y = 0; y < board.getMaxY(); y++)
                        board.setNewValue(x, y, computeValue(x, y));
                try {
                    barrier.await();
                } catch (InterruptedException ex) {
                    return;
                } catch (BrokenBarrierException ex) {
                    return;
                }
            }
        }

        private int computeValue(int x, int y) {
            // Compute the new value that goes in (x,y)
            return 0;
        }
    }

    public void start() {
        for (int i = 0; i < workers.length; i++)
            new Thread(workers[i]).start();
        mainBoard.waitForConvergence();
    }

    interface Board {
        int getMaxX();
        int getMaxY();
        int getValue(int x, int y);
        int setNewValue(int x, int y, int value);
        void commitNewValues();
        boolean hasConverged();
        void waitForConvergence();
        Board getSubBoard(int numPartitions, int index);
    }
}
