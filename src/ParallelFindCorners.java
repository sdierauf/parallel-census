import java.util.concurrent.RecursiveTask;

@SuppressWarnings("serial")
public class ParallelFindCorners extends RecursiveTask<Integer> {
  public float minLongitude;
  public float maxLongitude;
  public float minLatitude;
  public float maxLatitude;
  public static int CUTOFF;

  private int minIndex;
  private int maxIndex;
  private CensusData data;
  public int population;

  ParallelFindCorners(int minIndex, int maxIndex, CensusData data) {
    CUTOFF = data.data_size / 10;
    this.minIndex = minIndex;
    this.maxIndex = maxIndex;
    this.population = 0;
    this.data = data;
  }

  protected Integer compute() {
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
      ParallelFindCorners fork = new ParallelFindCorners(minIndex, midway, data);
      ParallelFindCorners main = new ParallelFindCorners(midway, maxIndex, data);
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