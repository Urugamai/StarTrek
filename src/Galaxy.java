import java.util.ArrayList;

/**
 * Created by Mark on 8/12/2015.
 */
public class Galaxy {
	private Game	game;						/** The game in which this entity exists */
	private int screenWidth = 0, screenHeight = 0;

	private int alienCount = 0;					// Galactic count of found aliens
	private int starbaseCount = 0;				// Galactic allocation of starbases

	private GameText galacticMap;

	private ArrayList<Sector> sectorListHead = new ArrayList<Sector>();

	protected Sector playerSector = null;

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
	}

	public Galaxy(Game game) {
		this.game = game;
		screenWidth = this.game.getWidth();
		screenHeight = this.game.getHeight();

		playerSector = new Sector(this, 0, 0, 0, screenWidth, screenHeight);
		sectorListHead.add(playerSector);

		galacticMap = new GameText(10, screenHeight, Constants.screenLines);
	}

	public Sector findSector(locationSpec loc) {
		return findSector(loc.getGx(), loc.getGy(), loc.getGz());
	}

	public Sector findSector(int X, int Y, int Z) {
		for (Sector loc : sectorListHead) {
			if (loc.getGalacticX() != X) continue;
			if (loc.getGalacticY() != Y) continue;
			if (loc.getGalacticZ() != Z) continue;

			return loc;
		}

		return null;
	}

	public Sector addSector(locationSpec loc) {
		return addSector(loc.getGx(), loc.getGy(), loc.getGz());
	}

	public Sector addSector(int X, int Y, int Z) {
		Sector theSector = findSector(X, Y, Z);
		if ( theSector != null) return theSector;

		theSector = new Sector(this, X, Y, Z, screenWidth, screenHeight);
		sectorListHead.add(theSector);

		return theSector;
	}

	public Sector getSector(locationSpec loc) {
		Sector req;
		req = findSector(loc);
		if (req == null) req = addSector(loc);
		return req;
	}

	public void initSectors() {

	}

	public void initPlayerShip() {
		playerSector.initPlayerShip();
	}

	public locationSpec getGalacticLocation(int x, int y, int z) {
		locationSpec gl = new locationSpec(x,y,z);
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

	public void doLRS() {
		int Xstart = playerSector.getGalacticX() - 1;
		int Xend = playerSector.getGalacticX() + 1;
		int Ystart = playerSector.getGalacticY() - 1;
		int Yend = playerSector.getGalacticY() + 1;
		int Zstart = playerSector.getGalacticZ() - 1;
		int Zend = playerSector.getGalacticZ() + 1;

		for (Sector loc : sectorListHead) {
			if (   Xstart <= loc.getGalacticX() && loc.getGalacticX() <= Xend
				&& Ystart <= loc.getGalacticY() && loc.getGalacticY() <= Yend
				&& Zstart <= loc.getGalacticZ() && loc.getGalacticZ() <= Zend
				) loc.doSRS();
		}
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

		doLRS();	// TODO Remove this in final version, debug only

		int eCount;
		int sCount;
		int pCount;
		alienCount = 0;
		starbaseCount = 0;
		for ( Sector aSector : sectorListHead) {
			eCount = aSector.LRS_EnemyCount;
			sCount = aSector.LRS_StarbaseCount;
			pCount = aSector.LRS_PlanetCount;
			alienCount += (eCount >= 0 ? eCount : 0);
			starbaseCount += (sCount >= 0 ? sCount : 0);

			currentX = aSector.getGalacticX(); currentY = aSector.getGalacticY();
			if ( !(	startX <= currentX && currentX <= endX
				&&	startY <= currentY && currentY <= endY ) ) continue;	// not on the map

			currentRow = mapLines - (currentY - startY);
			currentCol = (currentX - startX)*sectorText.length();

			assert(currentRow >= 0);
			assert(currentCol >= 0);
			assert(currentRow <= mapLines);
			assert(currentCol <= mapCols*sectorText.length());

			if (aSector == playerSector) {
				currentLine[currentRow].setCharAt(currentCol+0, '!');
				currentLine[currentRow].setCharAt(currentCol+4, '!');
			}

			if (eCount < 0) {
				currentLine[currentRow].setCharAt(currentCol+1, '?');
			} else if (eCount > 9) {
				currentLine[currentRow].setCharAt(currentCol+1, '*');
			} else if(eCount >= 0) {
				currentLine[currentRow].setCharAt(currentCol+1, numbers.charAt(eCount));
			}

			if (sCount < 0) {
				currentLine[currentRow].setCharAt(currentCol+2, '?');
			} else if (sCount > 9) {
				currentLine[currentRow].setCharAt(currentCol+2, '*');
			} else if(sCount >= 0) {
				currentLine[currentRow].setCharAt(currentCol+2, numbers.charAt(sCount));
			}

			if (pCount < 0) {
				currentLine[currentRow].setCharAt(currentCol+3, '?');
			} else if (pCount > 9) {
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
		for ( Sector aSector : sectorListHead) {
			aSector.doLogic(delta);
		}
		for ( Sector aSector : sectorListHead) {
			aSector.doAdd();
		}
		for ( Sector aSector : sectorListHead) {
			aSector.doRemove();
		}
	}
}

