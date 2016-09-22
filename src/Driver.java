import java.awt.*;
import java.io.IOException;

public class Driver
{
    
    public static void main(String[] args) throws IOException {
        //construct DrawingPanel, and get its Graphics context
        DrawingPanel panel = new DrawingPanel(212, 117);
        Graphics g = panel.getGraphics();
        
        //Test Step 1 - construct mountain map data
        MapDataDrawer map = new MapDataDrawer("NevadaToCalifornia.txt");

        //Test Step 2 - min, max, minRow in col
        int min = map.findMinValue();
        System.out.println("Min value in map: "+min);
        
        int max = map.findMaxValue();
        System.out.println("Max value in map: "+max);
        
        int minRow = map.indexOfMinInCol(0);
        System.out.println("Row with lowest val in col 0: "+minRow);

        g.setColor(Color.RED);
        int bestGreedyRow = map.indexOfLowestElevPath(g);
        int bestDijkRow = map.indexOfLowestOptPath(g);

        //Test Step 3 - draw the map
        map.drawMap(g);
        
        //Test Step 4 - draw a greedy path
        g.setColor(Color.RED); //can set the color of the 'brush' before drawing, then method doesn't need to worry about it
        int totalChange = map.drawLowestElevPath(g, minRow); //use minRow from Step 2 as starting point
        System.out.println("Lowest-Elevation-Change Path starting at row "+minRow+" gives total change of: "+totalChange);
        
        //Test Step 5 - draw the best path
        
        //map.drawMap(g); //use this to get rid of all red lines
        g.setColor(Color.GREEN); //set brush to green for drawing best path
        totalChange = map.drawLowestElevPath(g, bestGreedyRow);
        System.out.println("The Lowest-Elevation-Change Path starts at row: "+bestGreedyRow+" and gives a total change of: "+totalChange);
        g.setColor(Color.BLUE);
        totalChange = map.drawOptimalPath(g, bestDijkRow);
        System.out.println("The Lowest-Elevation-Change Path starts at row: "+bestDijkRow+" and gives a total change of: "+totalChange);
    }
}
