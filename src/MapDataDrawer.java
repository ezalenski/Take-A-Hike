import com.sun.tools.javac.util.Pair;

import java.awt.*;
import java.io.FileReader;
import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.Scanner;

public class MapDataDrawer {

    private int[][] grid;
    private int rows;
    private int cols;
    private int minElevation;
    private int maxElevation;

    /**
     * read in rows and columns from file and populates grid with corresponding data.
     *
     * @param filename
     * @throws IOException
     */
    public MapDataDrawer(String filename) throws IOException {
        Scanner sc = new Scanner(new FileReader(filename));
        this.rows = sc.nextInt();
        this.cols = sc.nextInt();
        sc.nextLine();

        grid = new int[this.rows][this.cols];
        int row = 0;
        int col = 0;
        int min = Integer.MAX_VALUE;
        int max = 0;
        while (sc.hasNextInt()) {
            grid[row][col] = sc.nextInt();
            min = (grid[row][col] < min) ? grid[row][col] : min;
            max = (grid[row][col] > max) ? grid[row][col] : max;
            col++;
            if (col == this.cols) {
                col = 0;
                row++;
            }
        }
        minElevation = min;
        maxElevation = max;
    }

    /**
     * @return the min value in the entire grid
     */
    public int findMinValue() {
        return minElevation;
    }

    /**
     * @return the max value in the entire grid
     */
    public int findMaxValue() {
        return maxElevation;
    }

    /**
     * @param col the column of the grid to check
     * @return the index of the row with the lowest value in the given col for the grid
     */
    public int indexOfMinInCol(int col) {
        int min = Integer.MAX_VALUE;
        int minRow = -1;
        for (int i = 0; i < rows; i++) {
            if (grid[i][col] < min) {
                min = grid[i][col];
                minRow = i;
            }
        }
        return minRow;
    }

    /**
     * Draws the grid using the given Graphics object.
     * Colors should be grayscale values 0-255, scaled based on min/max values in grid
     */
    public void drawMap(Graphics g) {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                double ratio = (double) (grid[i][j] - minElevation) / (maxElevation - minElevation);
                int c = (int) (ratio * 255);    //calculate the grayscale value
                g.setColor(new Color(c, c, c));    // set all 3 of the RGB colors to be the same 0..255 value// While data was stored as row,col, the graphing expects it to come in as col,row, so reverse it here:
                g.fillRect(j, i, 1, 1);         // Draw a 1x1 rectangle corresponding to row,col
            }
        }
    }

    /**
     * Find a path from West-to-East starting at given row.
     * Choose a foward step out of 3 possible forward locations, using greedy method described in assignment.
     *
     * @return the total change in elevation traveled from West-to-East
     */
    public int drawLowestElevPath(Graphics g, int row) {
        int accumulatedDiff = 0;
        for (int col = 0; col < cols; col++) {
            if (g != null)
                g.fillRect(col, row, 1, 1);
            Pair<Integer, Integer> results = nextBestRow(row, col);
            row = results.fst;
            if (results.snd != Integer.MAX_VALUE)
                accumulatedDiff += results.snd;
        }
        return accumulatedDiff;
    }

    /**
     * finds and returns a pair containing the next best row to move for the greedy algorithm
     * and the different in elevation moving to that best row.
     *
     * @param row
     * @param col
     * @return
     */
    private Pair<Integer, Integer> nextBestRow(int row, int col) {
        Pair<Integer, Integer> diffTop = new Pair<>(row - 1, getDiff(row, col, row - 1, col + 1));
        Pair<Integer, Integer> diffMid = new Pair<>(row, getDiff(row, col, row, col + 1));
        Pair<Integer, Integer> diffBot = new Pair<>(row + 1, getDiff(row, col, row + 1, col + 1));
        return (diffTop.snd < diffMid.snd) ? ((diffTop.snd < diffBot.snd) ? diffTop : diffBot) : ((diffMid.snd < diffBot.snd) ? diffMid : diffBot);
    }

    /**
     * gets the absolute difference in elevation when moving from r1, c1 to r2, c2
     *
     * @param r1
     * @param c1
     * @param r2
     * @param c2
     * @return
     */
    private int getDiff(int r1, int c1, int r2, int c2) {
        if (outOfBounds(r1, rows) || outOfBounds(c1, cols) || outOfBounds(r2, rows) || outOfBounds(c2, cols))
            return Integer.MAX_VALUE;
        else
            return Math.abs(grid[r2][c2] - grid[r1][c1]);
    }

    /**
     * checks if i is within array bounds of 0 and the bounds
     *
     * @param i
     * @param bounds
     * @return
     */
    private boolean outOfBounds(int i, int bounds) {
        return i < 0 || i >= bounds;
    }

    /**
     * @return the index of the starting row for the lowest-elevation-change path in the entire grid.
     */
    public int indexOfLowestElevPath(Graphics g) {
        int min = Integer.MAX_VALUE;
        int minRow = -1;
        for (int row = 0; row < rows; row++) {
            int diffElev = drawLowestElevPath(g, row);
            if (diffElev < min) {
                min = diffElev;
                minRow = row;
            }
        }
        return minRow;
    }

    /**
     * draws the overall optimal path from the target row beginning at col 0
     * and ending at the last column using Dijkstra's algorithm.
     *
     * @param g
     * @return
     */
    public int drawOptimalPath(Graphics g) {
        PriorityQueue<Node> pq = new PriorityQueue<>();
        HashMap<String, Node> map = initHashMap();

        for (int i = 0; i < rows; i++) {
            Node start = map.get(Integer.toString(i) + " 0");
            start.deltaElev = 0;
            pq.add(start);
        }

        while (!pq.isEmpty()) {
            Node curr = pq.poll();
            curr.done = true;

            //Check neighbors of the node
            for (int i = -1; i <= 1; i++) {
                for (int j = -1; j <= 1; j++) {
                    if (!(i == 0 && j == 0) && map.containsKey(Integer.toString(curr.x + i) + " " + Integer.toString(curr.y + j))) {
                        int newDeltaE = curr.deltaElev + getDiff(curr.x, curr.y, curr.x + i, curr.y + j);
                        Node adjh = map.get(Integer.toString(curr.x + i) + " " + Integer.toString(curr.y + j));
                        if (!adjh.done && (Integer.compare(newDeltaE, adjh.deltaElev) == -1)) {
                            if (pq.contains(adjh)) {
                                pq.remove(adjh);
                            }
                            adjh.deltaElev = newDeltaE;
                            adjh.prev = curr;
                            pq.add(adjh);
                        }
                    }
                }
            }
        }
        Node end = getDestinationNode(map);
        return drawPathFromDijkstra(g, end);
    }

    /**
     * initializes hashmap for dijkstra's
     *
     * @return
     */
    private HashMap<String, Node> initHashMap() {
        HashMap<String, Node> map = new HashMap<>();

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                Node h = new Node(i, j);
                map.put(Integer.toString(i) + " " + Integer.toString(j), h);
            }
        }
        return map;
    }

    /**
     * finds the node that has the least change in elevation to get to from the last column
     *
     * @param map
     * @return
     */
    private Node getDestinationNode(HashMap<String, Node> map) {
        int minDelta = Integer.MAX_VALUE;
        Node end = null;
        for (Node h : map.values()) {
            if (h.y == cols - 1 && Math.abs(h.deltaElev) < minDelta) {
                minDelta = h.deltaElev;
                end = h;
            }
        }

        return end;
    }

    /**
     * draws the path for dijkstra's algorithm
     *
     * @param g
     * @param end
     * @return
     */
    private int drawPathFromDijkstra(Graphics g, Node end) {
        int sum = 0;
        while (end != null) {
            g.fillRect(end.y, end.x, 1, 1);
            if (end.prev != null) {
                sum += Math.abs(getElevation(end.x, end.y) - getElevation(end.prev.x, end.prev.y));
            }
            end = end.prev;
        }
        return sum;
    }

    /**
     * gets the elevation for any given point
     *
     * @param x
     * @param y
     * @return
     */
    private int getElevation(int x, int y) {
        return grid[x][y];
    }

    /**
     * Node class used by Dijkstra's
     */
    private class Node implements Comparator<Node>, Comparable<Node> {
        public final int x;
        public final int y;
        public boolean done = false;
        public int deltaElev = Integer.MAX_VALUE;
        public Node prev = null;

        private Node(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public int compare(Node o1, Node o2) {
            return Integer.compare(o1.deltaElev, o2.deltaElev);
        }

        @Override
        public int compareTo(Node o) {
            return Integer.compare(this.deltaElev, o.deltaElev);
        }
    }
}