import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
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
    ParallelFindCorners corners = new ParallelFindCorners(0, data.data_size);
    pool.invoke(corners);
    minLongitude = corners.minLongitude;
    minLatitude = corners.minLatitude;
    maxLongitude = corners.maxLongitude;
    maxLatitude = corners.maxLatitude;
    longitudeUnit = (maxLongitude - minLongitude) / columns;
    latitudeUnit = (maxLatitude - minLatitude) / rows;
    ParallelBuildPopulationArray buildPopulationArray = new ParallelBuildPopulationArray(0, data.data_size);
    Pair<int[][], Float> buildPopArrayResult = pool.invoke(buildPopulationArray);
    theUSA = buildPopArrayResult.getElementA();
    totalPopulation = (int)Math.floor(buildPopArrayResult.getElementB());

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

  private int calculateEachRectangle(int west, int south, int east, int north) {
    int popOfRectangle = 0;
    for (int i = south; i <= north; i++) {
      for (int j = west; j <= east; j++) {
        popOfRectangle += theUSA[i][j];
      }
    }
    return popOfRectangle;
  }

  private class ParallelFindCorners extends RecursiveAction {
    public float minLongitude;
    public float maxLongitude;
    public float minLatitude;
    public float maxLatitude;

    private int minIndex;
    private int maxIndex;

    ParallelFindCorners(int minIndex, int maxIndex) {
      this.minIndex = minIndex;
      this.maxIndex = maxIndex;
    }

    protected void compute() {
      if (maxIndex - minIndex <= CUTOFF) {
        minLongitude = Float.POSITIVE_INFINITY;
        maxLongitude = Float.NEGATIVE_INFINITY;
        minLatitude = Float.POSITIVE_INFINITY;
        maxLatitude = Float.NEGATIVE_INFINITY;

        for (int i = minIndex; i < maxIndex; i++) {
          CensusGroup group = data.data[i];
          minLongitude = Math.min(minLongitude, group.longitude);
          minLatitude = Math.min(minLatitude, group.latitude);
          maxLongitude = Math.max(maxLongitude, group.longitude);
          maxLatitude = Math.max(maxLatitude, group.latitude);
        }
      } else {
        int midway = (maxIndex + minIndex) / 2;
        ParallelFindCorners fork = new ParallelFindCorners(minIndex, midway);
        ParallelFindCorners main = new ParallelFindCorners(midway, maxIndex);
        fork.fork();
        main.compute();
        fork.join();
        minLongitude = Math.min(fork.minLongitude, main.minLongitude);
        minLatitude = Math.min(fork.minLatitude, main.minLatitude);
        maxLongitude = Math.max(fork.maxLongitude, main.maxLongitude);
        maxLatitude = Math.max(fork.maxLatitude, main.maxLatitude);
      }
    }
  }

  private class ParallelBuildPopulationArray extends RecursiveTask<Pair<int[][], Float>> {
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

    protected Pair<int[][], Float> compute() {
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
        return new Pair<int[][], Float>(grid, partialPop);
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
        return new Pair<int[][], Float>(grid, this.partialPop);
      }
    }
  }

}
