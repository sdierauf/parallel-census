/**
 * Stefan Dierauf Nov 2014
 * Linear, dumb solution for a CensusSolver
 * aka v1
 */

public class LinearDumbCensusSolver implements CensusSolver {
  private int[][] theUSA;
  private int totalPopualtion;

  public LinearDumbCensusSolver(int columns, int rows, CensusData data) {
    theUSA = new int[rows][columns];
    int row = 0;
    int col = 0;
    

  }

  @Override
  public Pair<Integer, Float> singleInteraction(int west, int south, int east, int north) {
    return null;
  }
}
