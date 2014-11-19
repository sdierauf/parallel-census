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

    theUSA = makePopSumArray(theUSA);
  }
}
