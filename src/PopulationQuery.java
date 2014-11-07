
import java.io.*;
import java.lang.Integer;

public class PopulationQuery {
	// next four constants are relevant to parsing
	public static final int TOKENS_PER_LINE  = 7;
	public static final int POPULATION_INDEX = 4; // zero-based indices
	public static final int LATITUDE_INDEX   = 5;
	public static final int LONGITUDE_INDEX  = 6;

  public static CensusData data = null;
	
	// parse the input file into a large array held in a CensusData object
	public static CensusData parse(String filename) {
		CensusData result = new CensusData();
		
        try {
            BufferedReader fileIn = new BufferedReader(new FileReader(filename));
            
            // Skip the first line of the file
            // After that each line has 7 comma-separated numbers (see constants above)
            // We want to skip the first 4, the 5th is the population (an int)
            // and the 6th and 7th are latitude and longitude (floats)
            // If the population is 0, then the line has latitude and longitude of +.,-.
            // which cannot be parsed as floats, so that's a special case
            //   (we could fix this, but noisy data is a fact of life, more fun
            //    to process the real data as provided by the government)
            
            String oneLine = fileIn.readLine(); // skip the first line

            // read each subsequent line and add relevant data to a big array
            while ((oneLine = fileIn.readLine()) != null) {
                String[] tokens = oneLine.split(",");
                if(tokens.length != TOKENS_PER_LINE)
                	throw new NumberFormatException();
                int population = Integer.parseInt(tokens[POPULATION_INDEX]);
                if(population != 0)
                	result.add(population,
                			   Float.parseFloat(tokens[LATITUDE_INDEX]),
                		       Float.parseFloat(tokens[LONGITUDE_INDEX]));
            }

            fileIn.close();
        } catch(IOException ioe) {
            System.err.println("Error opening/reading/writing input or output file.");
            System.exit(1);
        } catch(NumberFormatException nfe) {
            System.err.println(nfe.toString());
            System.err.println("Error in file format");
            System.exit(1);
        }
        return result;
	}

  //Needed for USMaps
  public static Pair<Integer, Float> singleInteraction(int west, int south, int east, int north) {
    return null;
  }

  //Needed for USMaps
  public static void preprocess(String filename, int columns, int rows, int version) {
    switch (version) {
      case 1:
        //do dumb
      case 2:
        //do parallel dumb
      case 3:
        //do linear smart
      case 4:
        //do parallel smart
      default:
        throw new IllegalArgumentException(); //yolo
    }
  }

  public static void printError(String error) {
    System.out.println("ERROR: " + error);
  }

	// argument 1: file name for input data: pass this to parse
	// argument 2: number of x-dimension buckets
	// argument 3: number of y-dimension buckets
	// argument 4: -v1, -v2, -v3, -v4, or -v5 (WTF IS V5)
	public static void main(String[] args) {
		if (args.length != 4) {
      printError("Incorrect number of args!");
      System.exit(1);
    }

    String fileName = args[0];
    int columns = Integer.parseInt(args[1]);
    int rows = Integer.parseInt(args[2]);
    int version = Integer.parseInt(args[3].substring(2));



	}

}


