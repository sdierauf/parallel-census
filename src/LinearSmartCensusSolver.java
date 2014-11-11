/**
 * Stefan Dierauf Nov 2014
 * Linear but smart CensusSolver, aka v3
 * Does some additional preprocessing so all calls to singleInteraction are constant time
 *
 */
public class LinearSmartCensusSolver implements CensusSolver {

  private int[][] theUSA;
  private int totalPopualtion;
  private int columns;
  private int rows;

  public LinearSmartCensusSolver(int columns, int rows, CensusData data) {

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
    System.out.println(totalPopualtion);


    float latUnit = (maxLat - minLat) / rows;
    float lonUnit = (maxLon - minLon) / columns;

    //Build up the initial array like the linear dumb solver
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
    }

    //this next part is really horrible and should be optimized just using theUSA in place
    //but yolo for now. I am lazy
    int[][] temp = new int[rows][columns];
    for (int i = 0; i < theUSA.length; i++) {
      for (int j = 0; j < theUSA[i].length; j++){
        temp[i][j] = calculateEachRectangle(0, 0, j, i);
      }
    }

    theUSA = temp;

  }


  @Override

  public Pair<Integer, Float> singleInteraction(int west, int south, int east, int north) {
    /**
     * According to the spec
     * Take the value in the bottom-right corner of the query rectangle.
     * Subtract the value just above the top-right corner of the query rectangle (or 0 if that is outside the grid).
     * Subtract the value just left of the bottom-left corner of the query rectangle (or 0 if that is outside the grid).
     * Add the value just above and to the left of the upper-left corner of the query rectangle (or 0 if that is outside the grid).
     */
    //All params should be subtracted by 1!!!!
    north = north - 1;
    east = east - 1;
    south = south - 1;
    west = west - 1;
    //bottom right should be the north east one, because data is inverted, south is on top.
    int bottomRight = theUSA[north][east];
    int aboveTopRight = 0;
    if (south - 1 >= 0) {
      aboveTopRight = theUSA[south - 1][east];
    }
    int leftOfBottomLeft = 0;
    if (west - 1 >= 0) {
      leftOfBottomLeft = theUSA[north][west - 1];
    }
    int leftAndAboveOfUpperLeft = 0;
    if (south - 1 >= 0 && west - 1 >= 0) {
      leftAndAboveOfUpperLeft = theUSA[south - 1][west - 1];
    }
    int population = bottomRight - aboveTopRight - leftOfBottomLeft + leftAndAboveOfUpperLeft;
    return new Pair<Integer, Float>(population, population/(float) totalPopualtion * 100);
  }

  private int calculateEachRectangle(int west, int south, int east, int north) {
    int popOfRectangle = 0;
    for (int i = south; i <= north; i++) {
      for (int j = west; j <= east; j++) {
        popOfRectangle += theUSA[i][j];
      }
    }
    return popOfRectangle;
  }
}
