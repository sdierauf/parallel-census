import java.io.File;
import java.io.FileWriter;

/**
 * Created by sdierauf on 11/19/14.
 */
public class Timer {

  public long start;
  public long end;
  public FileWriter f;


  public Timer(String filename) {
    try {
       f = new FileWriter(filename, true);
    } catch (Exception e) {
      System.out.println("fajnfadsn");
      e.printStackTrace();
    }

  }

  public Timer start() {
    start = System.nanoTime();
    return this;
  }

  public long end() {
    end = System.nanoTime();
//    System.out.println(end - start);
//    try {
//      f.write("" + (end - start));
//    } catch (Exception e) {
//      System.out.println("oh noes");
//      e.printStackTrace();
//    }
//
    return end - start;
  }
}
