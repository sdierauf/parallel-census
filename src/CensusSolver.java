/**
 * Stefan Dierauf Nov 2014
 * Generic interface for a CensusSolver
 *
 * There are four versions of CensusSolver but they should all follow the same interface;
 * v1 = linear, dumb
 * v2 = Parallel, dumb
 * v3 = linear, clever
 * v4 = Parallel, clever
 * v5 = ?? da fuck
 */

public interface CensusSolver {

  public Pair<Integer, Float> singleInteraction(int west, int south, int east, int north);


}