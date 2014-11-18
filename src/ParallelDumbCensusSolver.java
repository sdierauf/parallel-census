import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.RecursiveTask;

/**
 * Stefan Dierauf Nov 2014
 * Parallel Dumb CensusSolver
 * aka v2
 */


public class ParallelDumbCensusSolver implements CensusSolver {
  protected ForkJoinPool pool = new ForkJoinPool();
  protected static int CUTOFF = 200;

  protected CensusData data;

  private final int columns;
  private final int rows;

  private float minLongitude;
  private float maxLongitude;
  private float minLatitude;
  private float maxLatitude;

  protected float longitudeUnit;
  protected float latitudeUnit;

//  private int[][] theUSA;
  private float totalPopulation;




  public ParallelDumbCensusSolver(int columns, int rows, CensusData data) {
    this.columns = columns;
    this.rows = rows;
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
    Pair<int[][], Float> result = pool.invoke(buildPopulationArray);
    totalPopulation = result.getElementB();
  }

  @Override
  public Pair<Integer, Float> singleInteraction(int west, int south, int east, int north) {
    if (!checkParams(west, south, east, north)) {
      return null;
    } else {
      north = north - 1;
      east = east - 1;
      south = south - 1;
      west = west - 1;
      ParallelAdd2dArray add2dArray = new ParallelAdd2dArray(west, south, east, north);
      int popOfRectangle = pool.invoke(add2dArray);

      return new Pair<Integer, Float>(popOfRectangle, 100 * (float) popOfRectangle / (float)totalPopulation);
    }

  }

  private boolean checkParams(int west, int south, int east, int north) {
    return west > 0 && south > 0
        && east <= this.columns && north <= this.rows
        && east >= west && north >= south;
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

  private class ParallelAdd2dArray extends RecursiveTask<Integer> {

    public int minRow;
    public int maxRow;
    public int minCol;
    public int maxCol;

    public final int ARR_CUTOFF = 10;

    ParallelAdd2dArray(int minCol, int minRow, int maxCol, int maxRow) {
      this.minCol = minCol;
      this.minRow = minRow;
      this.maxCol = maxCol;
      this.maxRow = maxRow;

    }

    protected Integer compute() {
      if (this.maxRow - this.minRow <= ARR_CUTOFF) {
        int partialPop = 0;
        for (int i = minRow; i <= maxRow; i++) {
          for (int j = minCol; j <= maxCol; j++) {
            partialPop += theUSA[i][j];
          }
        }
        return partialPop;
      } else {
        int midRow = this.minRow + (this.maxRow - this.minRow) / 2;
        ParallelAdd2dArray fork = new ParallelAdd2dArray(minCol, minRow, maxCol, midRow - 1);
        ParallelAdd2dArray main = new ParallelAdd2dArray(minCol, midRow, maxCol, maxRow);
        fork.fork();
        int result = main.compute();
        int result1 = fork.join();
        return new Integer(result + result1);
      }
    }
  }



}
