/**
 * Stefan Dierauf Nov 2014
 * Linear, dumb solution for a CensusSolver
 * aka v1
 */

public class LinearDumbCensusSolver implements CensusSolver {
  private CensusData data;
  private int totalPopulation;
  private int columns;
  private int rows;
  private float latUnit;
  private float lonUnit;
  private float maxLon = Integer.MIN_VALUE;
  private float minLon = Integer.MAX_VALUE;
  private float maxLat = Integer.MIN_VALUE;
  private float minLat = Integer.MAX_VALUE;

  public LinearDumbCensusSolver(int columns, int rows, CensusData data) {
    this.data = data;
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
      totalPopulation += group.population;
    }
    //latitude varies from south to north, with southern latitudes being less than northern latitudes
    //longitude varies from west to east, with western longitudes being less than eastern longitudes
    latUnit = (maxLat - minLat) / rows;
    lonUnit = (maxLon - minLon) / columns;
  }

  @Override
  public Pair<Integer, Float> singleInteraction(int west, int south, int east, int north) {
    if (!checkParams(west, south, east, north)) {
      return null;
    } else {
      int popOfRectangle = 0;
      west = west - 1;
      south = south - 1;
      Rectangle selection = new Rectangle(minLon + west * lonUnit,
          minLon + east * lonUnit,
          minLat + north * latUnit,
          minLat + south * latUnit);
      for (int i = 0; i < this.data.data_size; i++) {
        CensusGroup group = this.data.data[i];
        if (selection.contains(group.longitude, group.latitude)) {
          popOfRectangle += group.population;
        }
      }
      return new Pair<Integer, Float>(popOfRectangle, (float)(popOfRectangle * 100.0 / totalPopulation));
    }
  }

  private boolean checkParams(int west, int south, int east, int north) {
    return west > 0 && south > 0
        && east <= this.columns && north <= this.rows
        && east >= west && north >= south;
  }

}
