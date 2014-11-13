import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

/**
 * Stefan Dierauf Nov 2014
 * Parallel Dumb CensusSolver
 * aka v2
 */


public class ParallelDumbCensusSolver implements CensusSolver {
  protected ForkJoinPool pool = new ForkJoinPool();
  protected static int CUTOFF = 500;

  protected CensusData data;

  private final int columns;
  private final int rows;

  private float minLongitude;
  private float maxLongitude;
  private float minLatitude;
  private float maxLatitude;

  protected float cellWidth;
  protected float cellHeight;




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
    
  }

  @Override
  public Pair<Integer, Float> singleInteraction(int west, int south, int east, int north) {
    return null;
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
          maxLongitude = Math.min(maxLongitude, group.longitude);
          maxLatitude = Math.min(maxLatitude, group.latitude);
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
        maxLongitude = Math.min(fork.maxLongitude, main.maxLongitude);
        maxLatitude = Math.min(fork.maxLatitude, main.maxLatitude);
      }
    }
  }

  private class

}
