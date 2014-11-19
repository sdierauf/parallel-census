import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

/**
 * Stefan Dierauf Nov 2014
 */
public class ParallelSmartCensusSolver extends SmartCensusSolver implements CensusSolver {
  protected ForkJoinPool pool = new ForkJoinPool();
  protected static int CUTOFF = 200;

  protected CensusData data;

  public ParallelSmartCensusSolver(int columns, int rows, CensusData data) {
	super(columns, rows, data);
    this.data = data;
    
    ParallelFindCorners corners = new ParallelFindCorners(0, data.data_size, data);
    totalPopulation = pool.invoke(corners);
    minLongitude = corners.minLongitude;
    minLatitude = corners.minLatitude;
    maxLongitude = corners.maxLongitude;
    maxLatitude = corners.maxLatitude;
    
    longitudeUnit = (maxLongitude - minLongitude) / columns;
    latitudeUnit = (maxLatitude - minLatitude) / rows;
    
    ParallelBuildPopulationArray buildPopulationArray = new ParallelBuildPopulationArray(0, data.data_size);
    theUSA = pool.invoke(buildPopulationArray);

    theUSA = makePopSumArray(theUSA);
  }

  @SuppressWarnings("serial")
  private class ParallelBuildPopulationArray extends RecursiveTask<int[][]> {
    public int minIndex;
    public int maxIndex;
    public int[][] grid;
    public float partialPop;

    ParallelBuildPopulationArray(int minIndex, int maxIndex) {
      this.minIndex = minIndex;
      this.maxIndex = maxIndex;
      grid = new int[rows][columns];
      partialPop = 0;
    }

    protected int[][] compute() {
      if (maxIndex - minIndex <= CUTOFF * 750) {
        partialPop = 0;
        for (int i = minIndex; i < maxIndex; i++) {
          CensusGroup group = data.data[i];
          int row = (int) ((group.latitude - minLatitude) / latitudeUnit);
          if (row == rows) {
            row--;
          }
          int col = (int) ((group.longitude - minLongitude) / longitudeUnit);
          if (col == columns) {
            col--;
          }
          partialPop += group.population;
          grid[row][col] += group.population;
        }
        return grid;
      } else {
        int midway = (maxIndex - minIndex) / 2;
        ParallelBuildPopulationArray fork = new ParallelBuildPopulationArray(minIndex, midway);
        ParallelBuildPopulationArray main = new ParallelBuildPopulationArray(midway, maxIndex);
        fork.fork();
        main.compute();
        fork.join();
        this.partialPop = fork.partialPop + main.partialPop;
        for (int i = 0; i < rows; i++) {
          for (int j = 0; j < columns; j++) {
            grid[i][j] = fork.grid[i][j] + main.grid[i][j];
          }
        }
        return grid;
      }
    }
  }

}
