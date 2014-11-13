import java.util.concurrent.ForkJoinPool;

/**
 * Stefan Dierauf Nov 2014
 * Parallel Dumb CensusSolver
 * aka v2
 */


public class ParallelDumbCensusSolver implements CensusSolver {
  protected static int CUTOFF = 500;
  protected ForkJoinPool pool = new ForkJoinPool();

  private final int columns;
  private final int rows;


  public ParallelDumbCensusSolver(int columns, int rows, CensusData data) {
    this.columns = columns;
    this.rows = rows;
  }

  @Override
  public Pair<Integer, Float> singleInteraction(int west, int south, int east, int north) {
    return null;
  }

}
