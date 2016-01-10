/**
 * Created by Mark on 8/12/2015.
 */
public class Galaxy {
	private Game	game;						/** The game in which this entity exists */

	private int 	sizeX = 0;					// current Width of galaxy
	private int 	sizeY = 0;					// current Height of galaxy
	private int 	sizeZ = 0;					// current Depth of galaxy

	private int alienCount = 0;					// Galactic count of found aliens
	private int starbaseCount = 0;				// Galactic allocation of starbases

	private GameText galacticMap;

	private Sector headSector = null;
	public Sector playerSector = null;

	public static class galacticLocation {
		private int gx;
		private int gy;
		private int gz;

		public galacticLocation() {}
		public galacticLocation(int x, int y) { gx = x; gy = y; gz = 0; }
		public galacticLocation(int x, int y, int z) { gx = x; gy = y; gz = z; }
		public int getGx() { return gx; }
		public int getGy() { return gy; }
		public int getGz() { return gz; }
		public void setGx(int X) { gx = X; }
		public void setGy(int Y) { gy = Y; }
		public void setGz(int Z) { gz = Z; }
	}

	public Galaxy(Game game) {
		this.game = game;

		headSector = new Sector(game, 1, 0, 0);
		galacticMap = new GameText(10, game.getHeight(), Constants.screenLines);
	}

	public void initSectors(int width, int height) {
		alienCount = 0;
		starbaseCount = 0;
		for ( Sector aSector = headSector; aSector != null; aSector = aSector.Next) {
			alienCount += aSector.getEnemyCount();
			starbaseCount += aSector.getStarbaseCount();
		}
	}

	public void initPlayerShip() {
		headSector.initPlayerShip();
		setPlayerSector(headSector);
	}

	public void setPlayerSector(Sector sector) {
		playerSector = sector;
	}

	public galacticLocation getGalacticLocation(int x, int y) {
		galacticLocation gl = new galacticLocation(x,y);
		return gl;
	}

	public void setPlayerHeading(float heading, float inclination) {
		playerSector.setShipHeading(heading, inclination);
	}

	public void setPlayerThrust(float accel, float duration) {
		playerSector.setShipThrust(accel, duration);
	}

	public void setPlayerVelocity(float velocity) {
		playerSector.setShipVelocity(velocity);
	}

	public void playerFired(float direction) {
		playerSector.tryToFire(direction);
	}

	public int getEnemyCount() {
		return alienCount;
	}

	public int getStarbaseCount() {
		return starbaseCount;
	}

	public Sector getSector(galacticLocation sector) {
		for ( Sector aSector = headSector; aSector != null; aSector = aSector.Next) {
			if (sector.getGx() == aSector.getGalacticX() && sector.getGy() == aSector.getGalacticY() && sector.getGz() == aSector.getGalacticZ()) return aSector;
		}
		return null;
	}

	public void doLRS() {
		playerSector.doLRS();
	}

	public void doSRS() {
		playerSector.doSRS();
	}

	public void drawSector() {
		playerSector.draw();
	}

	// Draw a section of the galaxy with the players current sector in the centre
	public void drawGalaxy() {
		int mapLines = Constants.screenLines - 10;
		int halfScreen = mapLines / 2;
		int mapCols = 20;
		int halfMapCols = mapCols / 2;

		int centreX = playerSector.getGalacticX(), startX = centreX - halfMapCols, endX = centreX + halfMapCols;
		int centreY = playerSector.getGalacticY(), startY = centreY - halfScreen, endY = centreY + halfScreen;
		int currentX, currentY;

		int currentRow, currentCol;

		// Fill the map as an empty display
		StringBuilder currentLine[] = new StringBuilder[mapLines+1];
		String sectorText = "[   ]";
		String numbers = "0123456789";
		for(int row = 0; row <= mapLines; row++) {
			currentLine[row] = new StringBuilder();	// empty
			for (int col = 0; col <= mapCols+1; col++) {
				currentLine[row].append(sectorText);
			}
		}

		for ( Sector aSector = headSector; aSector != null; aSector = aSector.Next) {
			if ( !(	startX <= aSector.getGalacticX() && aSector.getGalacticX() <= endX
				&&	startY <= aSector.getGalacticY() && aSector.getGalacticY() <= endY ) ) continue;	// not on the map

			currentX = aSector.getGalacticX(); currentY = aSector.getGalacticY();
			currentRow = mapLines - (currentY - startY);
			currentCol = (currentX - startX)*sectorText.length();
			assert(currentRow >= 0);
			assert(currentCol >= 0);
			assert(currentRow <= mapLines);
			assert(currentCol <= mapCols*sectorText.length());

			int eCount = aSector.LRS_EnemyCount;
			int sCount = aSector.LRS_StarbaseCount;
			int pCount = aSector.LRS_PlanetCount;
			if (aSector == playerSector) {
				eCount = 10;
				sCount = 10;
				pCount = 10;
			}

			if (eCount > 9) {
				currentLine[currentRow].setCharAt(currentCol+1, '*');
			} else if(eCount >= 0) {
				currentLine[currentRow].setCharAt(currentCol+1, numbers.charAt(eCount));
			}

			if (sCount > 9) {
				currentLine[currentRow].setCharAt(currentCol+2, '*');
			} else if(sCount >= 0) {
				currentLine[currentRow].setCharAt(currentCol+2, numbers.charAt(sCount));
			}

			if (pCount > 9) {
				currentLine[currentRow].setCharAt(currentCol+3, '*');
			} else if(pCount >= 0) {
				currentLine[currentRow].setCharAt(currentCol+3, numbers.charAt(pCount));
			}
		}

		for(int row = 0; row < mapLines; row++) {
			galacticMap.writeLine(row+5, currentLine[row].toString());
//			System.out.println(currentLine);
		}

		galacticMap.writeLine(mapLines + 9, "FEDERATION SPACE GALACTIC MAP");
		galacticMap.writeLine(mapLines + 8, "There are " + alienCount + " enemy ships known to be in federation space");
		galacticMap.writeLine(mapLines + 7, "You currently have " + starbaseCount + " starbases available");
		galacticMap.draw();
//		galacticMap.writeLine(0, "Only updated after Long Range Scan performed.");
	}

	// Galactic logic implemented here and passed down the classes
	public void doLogic(double delta) {
		for ( Sector aSector = headSector; aSector != null; aSector = aSector.Next) {
			aSector.doLogic(delta);
		}
	}
}

