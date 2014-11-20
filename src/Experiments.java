/**
 * Created by sdierauf on 11/19/14.
 */
public class Experiments {

  public static void main(String[] args) {
    CensusSolver solver;
    CensusData data = PopulationQuery.parse("CenPop2010.txt");
//    for (int i = 0; i < 100; i++) {
//      solver = new LinearDumbCensusSolver(100, 500, data);
//    }
//    for (int i = 0; i < 100; i++) {
//      solver = new ParallelDumbCensusSolver(100, 500, data);
//    }
//    for (int i = 0; i < 100; i++) {
//      solver = new LinearSmartCensusSolver(100, 500, data);
//    }
    for (int i = 0; i < 100; i++) {
      solver = new ParallelSmartCensusSolver(100, 500, data);
    }

  }


}
