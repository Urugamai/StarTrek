import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Mark on 8/12/2015.
 */
public class Galaxy {

	public Sector[][] allocate(Class<Sector> c,int row,int column){
		Sector[][] matrix = (Sector[][]) Array.newInstance(c, column, row);
		for (int x = 0; x < column; x++) {
			//matrix[x] = (Sector[]) Array.newInstance(c,column);
			for(int y = 0; y < row; y++) {
				matrix[x][y] = new Sector(x,y);
			}
		}
		return matrix;
	}

	public Sector[][] sectorArray;

	private int galaxyWidth, galaxyHeight;

	private int alienCount = 0;					// Galactic count of found aliens
	private int starbaseCount = 0;				// Galactic allocation of starbases

	public static class locationSpec {
		private int gx;
		private int gy;
		private int gz;

		public locationSpec() {}
		public locationSpec(int x, int y) { gx = x; gy = y; gz = 0; }
		public locationSpec(int x, int y, int z) { gx = x; gy = y; gz = z; }
		public int getGx() { return gx; }
		public int getGy() { return gy; }
		public int getGz() { return gz; }
		public void setGx(int X) { gx = X; }
		public void setGy(int Y) { gy = Y; }
		public void setGz(int Z) { gz = Z; }
		public void addGx(int dx) { gx += dx; }
		public void addGy(int dy) { gy += dy; }
		public void addGz(int dz) { gz += dz; }
	}

	public Galaxy( int width, int height ) {
		galaxyWidth = width;
		galaxyHeight = height;
		sectorArray = allocate(Sector.class, width, height);
	}

	public Sector getSector(locationSpec loc) {
		return getSector(loc.getGx(), loc.getGy());
	}

	public Sector getSector( int sectorX, int sectorY) {
		assert(sectorX < galaxyWidth);
		assert(sectorY < galaxyHeight);

		Sector req = sectorArray[sectorX][sectorY];

		assert(req != null);

		return req;
	}

	public int getEnemyCount() {
		return alienCount;
	}

	public int getStarbaseCount() {
		return starbaseCount;
	}

	public locationSpec getNearestStarbase(locationSpec loc1) {
		locationSpec loc2, returnLoc = null;
		double minDistance = -1;

		for ( int gx = 0; gx < galaxyWidth; gx++ ) {
			for ( int gy = 0; gy <galaxyHeight; gy++) {
				Sector aSector = sectorArray[gx][gy];
				if (aSector.getStarbaseCount() > 0) {
					loc2 = new locationSpec(gx, gy, 0);
						returnLoc = loc2;
					}
				}
			}
		return returnLoc;
	}

	public locationSpec getNearestEnemyByCount(locationSpec loc1, int enemyCount) {
		locationSpec loc2, returnLoc = null;
		double minDistance = -1;

		for ( int gx = 0; gx < galaxyWidth; gx++ ) {
			for ( int gy = 0; gy <galaxyHeight; gy++) {
				Sector aSector = sectorArray[gx][gy];
				if (aSector.getEnemyCount() == enemyCount) {
					loc2 = new locationSpec(aSector.getGalacticX(), aSector.getGalacticY(), aSector.getGalacticZ());
						returnLoc = loc2;
				}
			}
		}

		return returnLoc;
	}

	public locationSpec getNearestEnemy(locationSpec loc1) {
		locationSpec loc2, returnLoc = null;
		double minDistance = -1;

		for ( int gx = 0; gx < galaxyWidth; gx++ ) {
			for (int gy = 0; gy < galaxyHeight; gy++) {
				Sector aSector = sectorArray[gx][gy];
				if (aSector.getEnemyCount() > 0) {
					loc2 = new locationSpec(aSector.getGalacticX(), aSector.getGalacticY(), aSector.getGalacticZ());
					returnLoc = loc2;
				}
			}
		}

		return returnLoc;
	}

	public void doLRS() {
	}

	public void doSRS() {
	}

	public void drawSector(int sectorX, int sectorY) {
		Sector viewSector = sectorArray[sectorX][sectorY];

		viewSector.draw();
	}

	public void drawShipStatus() {
	}

	// Draw a section of the galaxy with the players current sector in the centre
	public void drawGalaxy() {
		int mapLines = Constants.screenLines - 10;
		int halfScreen = mapLines / 2;
		int mapCols = 20;
		int halfMapCols = mapCols / 2;

		int centreX = galaxyWidth / 2;
		int centreY = galaxyHeight / 2;
		int currentX, currentY;

		int currentRow, currentCol;

		// Fill the map as an empty display
		StringBuilder currentLine[] = new StringBuilder[mapLines + 1];
		String sectorText = "[   ]";
		String numbers = "0123456789";
		for (int row = 0; row <= mapLines; row++) {
			currentLine[row] = new StringBuilder();    // empty
			for (int col = 0; col <= mapCols + 1; col++) {
				currentLine[row].append(sectorText);
			}
		}

		doLRS();    // TODO: Remove this in final version, debug only

		int eCount;
		int sCount;
		int pCount;
		alienCount = 0;
		starbaseCount = 0;
		for (int gx = 0; gx < galaxyWidth; gx++) {
			for (int gy = 0; gy < galaxyHeight; gy++) {
				Sector aSector = sectorArray[gx][gy];
				eCount = aSector.LRS_EnemyCount;
				sCount = aSector.LRS_StarbaseCount;
				pCount = aSector.LRS_PlanetCount;
				alienCount += (eCount >= 0 ? eCount : 0);
				starbaseCount += (sCount >= 0 ? sCount : 0);

				currentX = aSector.getGalacticX();
				currentY = aSector.getGalacticY();

			}

			for (int row = 0; row < mapLines; row++) {
			}
		}
	}

	// Galactic logic implemented here and passed down the classes
	public void doLogic(double delta, ArrayList<Transaction> transactions) {
		for (int gx = 0; gx < galaxyWidth; gx++) {
			for (int gy = 0; gy < galaxyHeight; gy++) {
				Sector aSector = sectorArray[gx][gy];
				aSector.doLogic(delta, transactions);
				//aSector.processCollisions();
			}
		}
	}

	public void processTransactions(ArrayList<Transaction> transactions) {

		for (Transaction trans : transactions) {
			if (!trans.active) continue;
			if (trans.type == Transaction.Type.GALAXY) {
				//TODO implement Galaxy transactions
				System.err.println("Galaxy: " + trans.type + ", " + trans.subType + ", " + trans.who + ", " + trans.action + ", " + trans.what + ", " + trans.howMuch);
				trans.active = false;
			}
		}

		for (int gx = 0; gx < galaxyWidth; gx++) {
			for (int gy = 0; gy < galaxyHeight; gy++) {
				Sector aSector = sectorArray[gx][gy];
				aSector.processTransactions(transactions);
			}
		}

		boolean isEmpty = true;
		for (Transaction trans : transactions) {
			if (trans.active) {
				System.err.println("Unhandled Transaction: " + trans.type + ", " + trans.subType + ", " + trans.who + ", " + trans.action + ", " + trans.what + ", " + trans.howMuch);
				isEmpty = false;
			}
		}
		if (isEmpty) transactions.clear();
	}
}

