/**
 * Created by sdierauf on 11/19/14.
 */
public class Experiments {

  public static void main(String[] args) {
    CensusSolver solver;
    CensusData data = PopulationQuery.parse("CenPop2010.txt");
    System.out.println("data size " + data.data_size );
//    for (int i = 0; i < 20; i++) {
//      solver = new ParallelSmartCensusSolver(100, 500, data);
//    }

//    for (int i = 0; i < 20; i++) {
    int rows = 10;
    long[][] times = new long[rows][20];
    int qCount = 9;
    for (int k = 0; k < rows; k++) {
      for (int j = 0; j < 20; j++) {
        Timer t = new Timer().start();
        solver = new ParallelDumbCensusSolver(100, 500, data);
        for (int i = 0; i < qCount; i++) {
          int x = (int) (Math.random() * 99) + 1;
          int y = (int) (Math.random() * 499) + 1;
          solver.singleInteraction(1, 1, x, y);
        }
        times[k][j] = t.end();
      }
      qCount += 2;
    }

    for (int i = 0; i < times.length; i++) {
      System.out.print(1 + 2 * i + "\t");
    }
    System.out.println();
    for (int i = 0; i < times[0].length; i++) {

      String row = "";
      for (int j = 0; j < times.length; j++) {
        if (i > -1) {
//          System.out.println("hi" + times[j][i]);
          row += times[j][i] + "\t";
        }
      }
      System.out.println(row);
    }
//    }
//    System.out.println("linear dumb");
//    for (int i = 0; i < 100; i++) {
//      solver = new LinearDumbCensusSolver(100, 500, data);
//      solver.singleInteraction(1, 1, 50, 100);
//    }
//    System.out.println("end linear dumb");
//    System.out.println("para smart");
//    for (int i = 0; i < 100; i++) {
//      solver = new ParallelDumbCensusSolver(100, 500, data);
//      solver.singleInteraction(1, 1, 50, 100);
//    }
//    System.out.println("end para smart");
//    System.out.println("linear smart");
//    for (int i = 0; i < 100; i++) {
//      solver = new LinearSmartCensusSolver(100, 500, data);
//      solver.singleInteraction(1, 1, 50, 100);
//    }
//    System.out.println("end linear smart");
//    System.out.println("------");
//    for (int i = 0; i < 100; i++) {
//      solver = new ParallelSmartCensusSolver(100, 500, data);
//      solver.singleInteraction(1, 1, 50, 100);
//    }

  }


}
