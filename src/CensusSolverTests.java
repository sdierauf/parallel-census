

import org.junit.*;
import static org.junit.Assert.*;
import org.junit.runner.*;
import org.junit.runners.*;
import org.junit.Test;
import org.junit.Before;
import org.junit.rules.ExpectedException;

import java.util.Arrays;
import java.util.Collection;

/**
 * Stefan Dierauf Nov 2014
 * Fuckit going to write spec tests because he gave us more sample output
 *
 *
 */

@RunWith(Parameterized.class)
public class CensusSolverTests {

  @Parameterized.Parameters
  public static Collection<Object[]> data() {
    return Arrays.asList(new Object[][] {
        {LinearDumbCensusSolver.class},
        {LinearSmartCensusSolver.class},
//        {ParallelDumbCensusSolver.class},
//        {ParallelSmartCensusSolver.class}
    });
  }

  public Class testingClass;
  public CensusSolver solver;
  public CensusData data;

  public CensusSolverTests(Class klass) {
    this.testingClass = klass;
    data = PopulationQuery.parse("CenPop2010.txt");
  }

  @Before
  public void setup() {
    buildSolver(20, 25);
  }

  public void buildSolver(int rows, int cols) {
    try {
//      System.out.println(Arrays.toString(testingClass.getConstructors()[0].getGenericParameterTypes()));
      Class[] cArg = new Class[]{Integer.TYPE, Integer.TYPE, CensusData.class};
      this.solver = (CensusSolver) testingClass.getDeclaredConstructor(cArg)
          .newInstance(rows, cols, data);

    } catch (Exception e) {
      e.printStackTrace();
      System.out.println("Fuck you!");
    }
  }

  @Test
  public void test100500() {
    buildSolver(100, 500);
    Pair<Integer, Float> p = solver.singleInteraction(1, 1, 100, 500);
    assertEquals(312471327, (int) p.getElementA());
    assertEquals(100.0, (float) p.getElementB(), 0.01);

    Pair<Integer, Float> p2 = solver.singleInteraction(1, 1, 50, 500);
    assertEquals(27820072, (int) p2.getElementA());
    assertEquals(8.90, (float) p2.getElementB(), 0.01);
  }

  @Test
  public void testHawaii() {
    Pair<Integer, Float> p = solver.singleInteraction(1, 1, 5, 4);
    assertEquals(1360301, (int)p.getElementA());
    assertEquals(0.44, (float)p.getElementB(), 0.01);
  }

  @Test
  public void testAlaska() {
    Pair<Integer, Float> p = solver.singleInteraction(1, 12, 9, 25);
    assertEquals(710231, (int)p.getElementA());
    assertEquals(0.23, (float)p.getElementB(), 0.01);
  }

  @Test
  public void testMainUS() {
    Pair<Integer, Float> p = solver.singleInteraction(9, 1, 20, 13);
    assertEquals(310400795, (int)p.getElementA());
    assertEquals(99.34, (float)p.getElementB(), 0.01);
  }

  @Test
  public void bottom4() {
    Pair<Integer, Float> p = solver.singleInteraction(1, 1, 20, 4);
    assertEquals(36493611, (int)p.getElementA());
    assertEquals(11.68, (float)p.getElementB(), 0.01);
  }

  @Test
  public void middleish3() {
    Pair<Integer, Float> p = solver.singleInteraction(9, 1, 11, 25);
    assertEquals(52392739, (int)p.getElementA());
    assertEquals(16.77, (float)p.getElementB(), 0.01);
  }


}
