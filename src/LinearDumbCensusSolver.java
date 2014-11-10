/**
 * Stefan Dierauf Nov 2014
 * Linear, dumb solution for a CensusSolver
 * aka v1
 */

public class LinearDumbCensusSolver implements CensusSolver {
  private int[][] theUSA;
  private int totalPopualtion;
  private int columns;
  private int rows;

  public LinearDumbCensusSolver(int columns, int rows, CensusData data) {
    theUSA = new int[rows][columns];
    float maxLon = Integer.MIN_VALUE;
    float minLon = Integer.MAX_VALUE;
    float maxLat = Integer.MIN_VALUE;
    float minLat = Integer.MAX_VALUE;
    this.columns = columns;
    this.rows = rows;
    //find four corners of the US
    for (int i = 0; i < data.data_size; i++) {
      CensusGroup group = data.data[i];
      if (group.longitude > maxLon) {
        maxLon = group.longitude;
      }
      if (group.longitude < minLon) {
        minLon = group.longitude;
      }
      if (group.latitude > maxLat) {
        maxLat = group.latitude;
      }
      if (group.latitude < minLat) {
        minLat = group.latitude;
      }
      totalPopualtion += group.population;
    }
    // need to find some units to sort into groups
    //latitude varies from south to nort, with southern latitudes being less than northern latitudes
    //longitude varies from west to east, with western longitudes being less than eastern longitudes
    float latUnit = (maxLat - minLat) / rows;
    float lonUnit = (maxLon - minLon) / columns;
    //for each group, subtract the minLon and minLat from each field
    //take those numbers and divide them by their respective units
    //round down to their respective buckets
    System.out.println("lat max min lon max min " + maxLat + " " + minLat + " " + maxLon + " " + minLon);
    int temp = 0;
    for (int i = 0; i < data.data_size; i++) {
      CensusGroup group = data.data[i];
      int row = (int) ((group.latitude - minLat) / latUnit);
      if (row == rows) {
        row--;
      }
      int col = (int) ((group.longitude - minLon) / lonUnit);
      if (col == columns) {
        col--;
      }
      this.theUSA[row][col] += group.population;
      temp += group.population;
    }

  }

  @Override
  public Pair<Integer, Float> singleInteraction(int west, int south, int east, int north) {
    System.out.println(west +  " " + south +  " " + east +  " " + north);
    if (!checkParams(west, south, east, north)) {
      return null;
    } else {
      int popOfRectangle = 0;
      for (int i = south - 1; i < north; i++) {
        for (int j = west - 1; j < east; j++) {
          popOfRectangle += theUSA[i][j];
        }
      }
      return new Pair<Integer, Float>(popOfRectangle, 100 * (float) popOfRectangle / (float)totalPopualtion);
    }
  }

  private boolean checkParams(int west, int south, int east, int north) {
    return west > 0 && south > 0
        && east <= this.columns && north <= this.rows
        && east >= west && north >= south;
  }

}
