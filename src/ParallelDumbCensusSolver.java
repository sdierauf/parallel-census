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

  private float totalPopulation;




  public ParallelDumbCensusSolver(int columns, int rows, CensusData data) {
    this.columns = columns;
    this.rows = rows;
    this.data = data;
    ParallelFindCorners corners = new ParallelFindCorners(0, data.data_size);
    totalPopulation = pool.invoke(corners);
    minLongitude = corners.minLongitude;
    minLatitude = corners.minLatitude;
    maxLongitude = corners.maxLongitude;
    maxLatitude = corners.maxLatitude;
    longitudeUnit = (maxLongitude - minLongitude) / columns;
    latitudeUnit = (maxLatitude - minLatitude) / rows;
  }

  @Override
  public Pair<Integer, Float> singleInteraction(int west, int south, int east, int north) {
    if (!checkParams(west, south, east, north)) {
      return null;
    } else {
      south = south - 1;
      west = west - 1;
      Rectangle selection = new Rectangle(minLongitude + west * longitudeUnit,
          minLongitude + east * longitudeUnit,
          minLatitude + north * latitudeUnit,
          minLatitude + south * latitudeUnit);
      ParallelCalculatePopulation calculatePopulation = new ParallelCalculatePopulation(0, this.data.data_size, selection);
      int popOfRectangle = pool.invoke(calculatePopulation);
      return new Pair<Integer, Float>(popOfRectangle, 100 * (float) popOfRectangle / (float)totalPopulation);
    }

  }

  private boolean checkParams(int west, int south, int east, int north) {
    return west > 0 && south > 0
        && east <= this.columns && north <= this.rows
        && east >= west && north >= south;
  }


  private class ParallelFindCorners extends RecursiveTask<Float> {
    public float minLongitude;
    public float maxLongitude;
    public float minLatitude;
    public float maxLatitude;

    private int minIndex;
    private int maxIndex;
    public float population;

    ParallelFindCorners(int minIndex, int maxIndex) {
      this.minIndex = minIndex;
      this.maxIndex = maxIndex;
      this.population = 0;
    }

    protected Float compute() {
      if (maxIndex - minIndex <= CUTOFF) {
        minLongitude = Float.POSITIVE_INFINITY;
        maxLongitude = Float.NEGATIVE_INFINITY;
        minLatitude = Float.POSITIVE_INFINITY;
        maxLatitude = Float.NEGATIVE_INFINITY;

        for (int i = minIndex; i < maxIndex; i++) {
          CensusGroup group = data.data[i];
          this.population += group.population;
          minLongitude = Math.min(minLongitude, group.longitude);
          minLatitude = Math.min(minLatitude, group.latitude);
          maxLongitude = Math.max(maxLongitude, group.longitude);
          maxLatitude = Math.max(maxLatitude, group.latitude);
        }
        return this.population;
      } else {
        int midway = (maxIndex - minIndex) / 2 + minIndex;
        ParallelFindCorners fork = new ParallelFindCorners(minIndex, midway);
        ParallelFindCorners main = new ParallelFindCorners(midway, maxIndex);
        fork.fork();
        main.compute();
        fork.join();
        minLongitude = Math.min(fork.minLongitude, main.minLongitude);
        minLatitude = Math.min(fork.minLatitude, main.minLatitude);
        maxLongitude = Math.max(fork.maxLongitude, main.maxLongitude);
        maxLatitude = Math.max(fork.maxLatitude, main.maxLatitude);
        this.population = fork.population + main.population;
        return this.population;
      }
    }
  }

  private class ParallelCalculatePopulation extends RecursiveTask<Integer> {
    private int minIndex;
    private int maxIndex;
    private final Rectangle selection;
    private final int calcPopCutoff = 400;
    private int partialPopulation;


    ParallelCalculatePopulation(int minIndex, int maxIndex, Rectangle selection) {
      this.minIndex = minIndex;
      this.maxIndex = maxIndex;
      this.selection = selection;
      this.partialPopulation = 0;
    }

    protected Integer compute() {
      if (maxIndex - minIndex <= calcPopCutoff) {
        for (int i = minIndex; i < maxIndex; i++) {
          CensusGroup group = data.data[i];
          if (selection.contains(group.longitude, group.latitude)) {
            this.partialPopulation += group.population;
          }
        }
        return this.partialPopulation;
      } else {
        int midway = (maxIndex - minIndex) / 2 + minIndex;
        ParallelCalculatePopulation main = new ParallelCalculatePopulation(minIndex, midway, selection);
        ParallelCalculatePopulation fork = new ParallelCalculatePopulation(midway, maxIndex, selection);
        fork.fork();
        int result1 = main.compute();
        int result2 = fork.join();
        this.partialPopulation = result1 + result2;
        return this.partialPopulation;
      }
    }
  }
  
}
