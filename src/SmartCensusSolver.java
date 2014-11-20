
public abstract class SmartCensusSolver implements CensusSolver {

	protected int[][] theUSA;
	protected int totalPopulation;
	protected int columns;
	protected int rows;

	protected float minLongitude;
	protected float maxLongitude;
	protected float minLatitude;
	protected float maxLatitude;

	protected float longitudeUnit;
	protected float latitudeUnit;

	public SmartCensusSolver(int columns, int rows, CensusData data) {
		this.columns = columns;
		this.rows = rows;
		theUSA = new int[rows][columns];
	}

	@Override
	public Pair<Integer, Float> singleInteraction(int west, int south, int east, int north) {
		/**
		 * According to the spec
		 * Take the value in the bottom-right corner of the query rectangle.
		 * Subtract the value just above the top-right corner of the query rectangle (or 0 if that is outside the grid).
		 * Subtract the value just left of the bottom-left corner of the query rectangle (or 0 if that is outside the grid).
		 * Add the value just above and to the left of the upper-left corner of the query rectangle (or 0 if that is outside the grid).
		 * 
		 * (In short, take the values in the rectangle from 0,0 to bottom right and subtract the union
		 * of the rectangles from (0,0) to bottom left and from (0,0) to top right)
		 */
		//All params should be subtracted by 1!!!!
		north = north - 1;
		east = east - 1;
		south = south - 1;
		west = west - 1;
		//bottom right should be the north east one, because data is inverted, south is on top.
		int bottomRight = theUSA[north][east];
		int aboveTopRight = 0;
		if (south - 1 >= 0) {
			aboveTopRight = theUSA[south - 1][east];
		}
		int leftOfBottomLeft = 0;
		if (west - 1 >= 0) {
			leftOfBottomLeft = theUSA[north][west - 1];
		}
		int leftAndAboveOfUpperLeft = 0;
		if (south - 1 >= 0 && west - 1 >= 0) {
			leftAndAboveOfUpperLeft = theUSA[south - 1][west - 1];
		}
		int population = bottomRight - aboveTopRight - leftOfBottomLeft + leftAndAboveOfUpperLeft;
		return new Pair<Integer, Float>(population, population/(float) totalPopulation * 100);
	}

	// this *should* be in-place
	public void makePopSumArray(int[][] popArray) {
		for (int i = 0; i < popArray.length; i++) {
			for (int j = 0; j < popArray[0].length; j++){
				if(i >= 1 && j >= 1) {
					popArray[i][j] = popArray[i][j] + popArray[i-1][j]
							+ popArray[i][j-1] - popArray[i-1][j-1];
				} else if(i >= 1) {
					popArray[i][j] = popArray[i][j] + popArray[i-1][j];
				} else if(j >= 1) {
					popArray[i][j] = popArray[i][j] + popArray[i][j-1];
				} else {
					popArray[i][j] = popArray[i][j];
				}
			}
		}
		// return popSumArray;
	}
}
