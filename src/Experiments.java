/**
 * Created by sdierauf on 11/19/14.
 */
public class Experiments {

  public static void main(String[] args) {
    CensusSolver solver;
    CensusData data = PopulationQuery.parse("CenPop2010.txt");
    System.out.println("linear dumb");
    for (int i = 0; i < 100; i++) {
      solver = new LinearDumbCensusSolver(100, 500, data);
      solver.singleInteraction(1, 1, 50, 100);
    }
    System.out.println("end linear dumb");
    System.out.println("para smart");
    for (int i = 0; i < 100; i++) {
      solver = new ParallelDumbCensusSolver(100, 500, data);
      solver.singleInteraction(1, 1, 50, 100);
    }
    System.out.println("end para smart");
    System.out.println("linear smart");
    for (int i = 0; i < 100; i++) {
      solver = new LinearSmartCensusSolver(100, 500, data);
      solver.singleInteraction(1, 1, 50, 100);
    }
    System.out.println("end linear smart");
    System.out.println("------");
    for (int i = 0; i < 100; i++) {
      solver = new ParallelSmartCensusSolver(100, 500, data);
      solver.singleInteraction(1, 1, 50, 100);
    }

  }


}
