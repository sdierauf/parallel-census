import java.io.File;
import java.io.FileWriter;

/**
 * Created by sdierauf on 11/19/14.
 */
public class Timer {

  public long start;
  public long end;


  public Timer() {

  }

  public Timer start() {
    start = System.nanoTime();
    return this;
  }

  public long end() {
    end = System.nanoTime();
//    System.out.println(end - start);
    return end - start;
  }
}
