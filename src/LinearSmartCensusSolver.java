/**
 * Stefan Dierauf Nov 2014
 * Linear but smart CensusSolver, aka v3
 * Does some additional preprocessing so all calls to singleInteraction are constant time
 *
 */
public class LinearSmartCensusSolver extends SmartCensusSolver implements CensusSolver {

  public LinearSmartCensusSolver(int columns, int rows, CensusData data) {

    super(columns, rows, data);
    maxLongitude = Integer.MIN_VALUE;
    minLongitude = Integer.MAX_VALUE;
    maxLatitude = Integer.MIN_VALUE;
    minLatitude = Integer.MAX_VALUE;
    theUSA = new int[rows][columns];

    //find four corners of the US
    for (int i = 0; i < data.data_size; i++) {
      CensusGroup group = data.data[i];
      if (group.longitude > maxLongitude) {
        maxLongitude = group.longitude;
      }
      if (group.longitude < minLongitude) {
        minLongitude = group.longitude;
      }
      if (group.latitude > maxLatitude) {
        maxLatitude = group.latitude;
      }
      if (group.latitude < minLatitude) {
        minLatitude = group.latitude;
      }
      totalPopulation += group.population;
    }

    latitudeUnit = (maxLatitude - minLatitude) / rows;
    longitudeUnit = (maxLongitude - minLongitude) / columns;

    //Build up the initial array like the linear dumb solver
    for (int i = 0; i < data.data_size; i++) {
      CensusGroup group = data.data[i];
      int row = (int) ((group.latitude - minLatitude) / latitudeUnit);
      if (row == rows) row--;
      int col = (int) ((group.longitude - minLongitude) / longitudeUnit);
      if (col == columns) col--;
        this.theUSA[row][col] += group.population;
    }

    //this next part is really horrible and should be optimized just using theUSA in place
    //but yolo for now. I am lazy
    // TODO: fix this!
    int[][] temp = new int[rows][columns];
    for (int i = 0; i < theUSA.length; i++) {
      for (int j = 0; j < theUSA[i].length; j++){
        int popOfRectangle = 0;
		for (int i1 = 0; i1 <= i; i1++) {
		  for (int j1 = 0; j1 <= j; j1++) {
		    popOfRectangle += theUSA[i1][j1];
		  }
		}
		temp[i][j] = popOfRectangle;
      }
    }

    theUSA = temp;
  }
}
