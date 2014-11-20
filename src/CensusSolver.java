/**
 * Stefan Dierauf Nov 2014
 * Generic interface for a CensusSolver
 *
 * There are four versions of CensusSolver but they should all follow the same interface;
 * v1 = linear, dumb
 * v2 = Parallel, dumb
 * v3 = linear, clever
 * v4 = Parallel, clever
 * v5 = Quantum, ridiculous
 * v6 = Relativistic, interstellar
 */

public interface CensusSolver {


  /**
   * Runs a single interaction with the census solver,
   * defines the size of the coordinates of the rectangle
   * 
   * @param west
   * @param south
   * @param east
   * @param north
   * @return
   */
  public Pair<Integer, Float> singleInteraction(int west, int south, int east, int north);


}