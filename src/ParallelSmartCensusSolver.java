import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.RecursiveTask;

/**
 * Stefan Dierauf Nov 2014
 */
public class ParallelSmartCensusSolver extends SmartCensusSolver implements CensusSolver {
  protected ForkJoinPool pool = new ForkJoinPool();
  protected static int dataCUTOFF;
  protected static int arrCUTOFF;
  protected CensusData data;

  public ParallelSmartCensusSolver(int columns, int rows, CensusData data) {
    super(columns, rows, data);
    this.data = data;
    dataCUTOFF = data.data_size / 10 + 1;
    arrCUTOFF = columns * rows / 25 + 1;
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
    makePopSumArray(theUSA);
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
      if (maxIndex - minIndex <= dataCUTOFF) {
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
        int midway = (maxIndex - minIndex) / 2 + minIndex;
        ParallelBuildPopulationArray fork = new ParallelBuildPopulationArray(minIndex, midway);
        ParallelBuildPopulationArray main = new ParallelBuildPopulationArray(midway, maxIndex);
        fork.fork();
        main.compute();
        fork.join();
        this.partialPop = fork.partialPop + main.partialPop;

        ParallelAddMatrices adder = new ParallelAddMatrices(fork.grid, main.grid, 0,
            fork.grid.length, 0, fork.grid[0].length, this.grid);
        adder.compute();
        return this.grid;
      }
    }
  }

  @SuppressWarnings("serial")
  private class ParallelAddMatrices extends RecursiveAction {
    public int[][] data1;
    public int[][] data2;
    public int[][] finalGrid;
    public int minXIndex;
    public int maxXIndex;
    public int maxYIndex;
    public int minYIndex;

    public ParallelAddMatrices(int[][] data1, int[][] data2, int minXIndex, int maxXIndex,
                               int minYIndex, int maxYIndex, int[][] finalGrid) {
      this.data1 = data1;
      this.data2 = data2;
      this.minXIndex = minXIndex;
      this.maxXIndex = maxXIndex;
      this.minYIndex = minYIndex;
      this.maxYIndex = maxYIndex;
      this.finalGrid = finalGrid;
    }

    protected void compute() {
      if (Math.max(maxXIndex - minXIndex, maxYIndex - minYIndex) <= arrCUTOFF) {
        for (int i = minXIndex; i < maxXIndex; i++) {
          for (int j = minYIndex; j < maxYIndex; j++) {
            finalGrid[i][j] = data1[i][j] + data2[i][j];
          }
        }
      } else {
        int midwayX = (maxXIndex - minXIndex) / 2 + minXIndex;
        int midwayY = (maxYIndex - minYIndex) / 2 + minYIndex;
        ParallelAddMatrices fork = new ParallelAddMatrices(data1, data2, minXIndex,
            midwayX, minYIndex, midwayY, finalGrid);
        ParallelAddMatrices fork2 = new ParallelAddMatrices(data1, data2, minXIndex,
            midwayX, midwayY, maxYIndex, finalGrid);
        ParallelAddMatrices fork3 = new ParallelAddMatrices(data1, data2, midwayX,
            maxXIndex, minYIndex, midwayY, finalGrid);
        ParallelAddMatrices main = new ParallelAddMatrices(data1, data2, midwayX,
            maxXIndex, midwayY, maxYIndex, finalGrid);
        fork.fork();
        fork2.fork();
        fork3.fork();
        main.compute();
        fork.join();
        fork2.join();
        fork3.join();
      }
    }
  }
}