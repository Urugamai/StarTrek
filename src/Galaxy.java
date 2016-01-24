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
	private GameText shipStatusDisplay;
	protected PlayerShipEntity playerShip = null;

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
		public void addGx(int dx) { gx += dx; }
		public void addGy(int dy) { gy += dy; }
		public void addGz(int dz) { gz += dz; }
	}

	public Galaxy(Game game) {
		this.game = game;
		screenWidth = this.game.getWidth();
		screenHeight = this.game.getHeight();

		playerSector = new Sector(this, 0, 0, 0, screenWidth, screenHeight);
		sectorListHead.add(playerSector);

		galacticMap = new GameText(10, screenHeight, Constants.screenLines);
		shipStatusDisplay = new GameText(10, screenHeight, Constants.screenLines);
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
		return getSector(loc.getGx(), loc.getGy(), loc.getGz());
	}

	public Sector getSector( int X, int Y, int Z) {
		Sector req = findSector(X, Y, Z);
		if (req == null) req = addSector(X, Y, Z);
		return req;
	}

	public void initSectors() {

	}

	public void initPlayerShip() {
		playerShip = playerSector.initPlayerShip();
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

	public float getPlayerVelocity() {
		return playerSector.getShipVelocity();
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

	public locationSpec getNearestStarbase(locationSpec loc1) {
		locationSpec loc2, returnLoc = null;
		double minDistance = -1;

		for ( Sector aSector : sectorListHead) {
			if (aSector.getStarbaseCount() > 0) {
				loc2 = new locationSpec(aSector.getGalacticX(), aSector.getGalacticY(), aSector.getGalacticZ());	//NOTE possibly should only use LRS data rather than actual but we are not pre-building sectors (yet)
				double dist = game.compute_distance_between(loc1, loc2);
				if (minDistance < 0 || dist < minDistance) {
					minDistance = dist;
					returnLoc = loc2;
				}
			}
		}

		return returnLoc;
	}

	public locationSpec getNearestEnemyByCount(locationSpec loc1, int enemyCount) {
		locationSpec loc2, returnLoc = null;
		double minDistance = -1;

		for ( Sector aSector : sectorListHead) {
			if (aSector.getEnemyCount() == enemyCount ) {
				loc2 = new locationSpec(aSector.getGalacticX(), aSector.getGalacticY(), aSector.getGalacticZ());	//NOTE possibly should only use LRS data rather than actual but we are not pre-building sectors (yet)
				double dist = game.compute_distance_between(loc1, loc2);
				if (minDistance < 0 || dist < minDistance) {
					minDistance = dist;
					returnLoc = loc2;
				}
			}
		}

		return returnLoc;
	}

	public locationSpec getNearestEnemy(locationSpec loc1) {
		locationSpec loc2, returnLoc = null;
		double minDistance = -1;

		for ( Sector aSector : sectorListHead) {
			if (aSector.getEnemyCount() > 0 ) {
				loc2 = new locationSpec(aSector.getGalacticX(), aSector.getGalacticY(), aSector.getGalacticZ());	//NOTE possibly should only use LRS data rather than actual but we are not pre-building sectors (yet)
				double dist = game.compute_distance_between(loc1, loc2);
				if (minDistance < 0 || dist < minDistance) {
					minDistance = dist;
					returnLoc = loc2;
				}
			}
		}

		return returnLoc;
	}

	public void setPlayerWarp(float warpSpeed, float duration) {
		playerSector.setShipWarp(warpSpeed, duration);
	}

	public void doLRS() {
		Sector loc;

		int Xstart = playerSector.getGalacticX() - 1;
		int Xend = playerSector.getGalacticX() + 1;
		int Ystart = playerSector.getGalacticY() - 1;
		int Yend = playerSector.getGalacticY() + 1;
		int Zstart = playerSector.getGalacticZ() - 1;
		int Zend = playerSector.getGalacticZ() + 1;

		for (int x = Xstart; x <= Xend; x++) {
			for (int y = Ystart; y <= Yend; y++) {
				for (int z = Zstart; z <= Zend; z++) {
					loc = getSector(x, y, z);
					loc.doSRS();
				}
			}
		}
	}

	public void doSRS() {
		playerSector.doSRS();
	}

	public void drawSector() {
		playerSector.draw();
	}

	public void drawShipStatus() {
		if (playerShip == null) return;

		int currentLine = 6;

		shipStatusDisplay.writeLine(currentLine++, "Energy: " + playerShip.energyLevel);
		shipStatusDisplay.writeLine(currentLine++, "Torpedoes: " + playerShip.torpedoCount);
		shipStatusDisplay.writeLine(currentLine++, "Shields: " + playerShip.shieldPercent + "%");
		shipStatusDisplay.writeLine(currentLine++, "Structural Integrity: " + playerShip.solidity + "%");
		shipStatusDisplay.writeLine(currentLine++, "Docked: " + playerShip.isDocked());
		shipStatusDisplay.writeLine(currentLine++, "Heading: " + playerShip.getHeading() + " Mark 0" );	// inclination not used yet
		shipStatusDisplay.writeLine(currentLine++, "Velocity: " + playerShip.velocity);
		shipStatusDisplay.writeLine(currentLine++, "Impulse: " + playerShip.getThrust());
		shipStatusDisplay.writeLine(currentLine++, "Warp: " + playerShip.getWarpSpeed());
		shipStatusDisplay.writeLine(currentLine++, "Current Sector: (" + playerSector.getGalacticX() + ", " + playerSector.getGalacticY() + ", " + playerSector.getGalacticZ() + ")" );
		shipStatusDisplay.writeLine(currentLine++, "Current Location: (" + playerShip.getX() + ", " + playerShip.getY() + ", " + playerShip.getZ() + ")" );

		for (Entity ent : playerSector.entities) {
			if (ent instanceof RomulanEntity) {
				shipStatusDisplay.writeLine(currentLine++, "Enemy Location: ("  + ent.getX() + ", " + ent.getY() + ", " + ent.getZ() + ")" );
			}
		}
		currentLine++;
		if (playerShip.isDocked()) {
			shipStatusDisplay.writeLine(currentLine++, "Starbase Energy: " + playerShip.dockedTo.energyLevel);
			shipStatusDisplay.writeLine(currentLine++, "Starbase Torpedoes: " + playerShip.dockedTo.torpedoCount);
			shipStatusDisplay.writeLine(currentLine++, "Starbase Shields: " + playerShip.dockedTo.shieldPercent + "%");
			shipStatusDisplay.writeLine(currentLine++, "Starbase Structural Integrity: " + playerShip.dockedTo.solidity + "%");
			shipStatusDisplay.writeLine(currentLine++, "Starbase Current Location: (" + playerShip.dockedTo.getX() + ", " + playerShip.dockedTo.getY() + ", " + playerShip.dockedTo.getZ() + ")" );
		}

		shipStatusDisplay.draw();
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

		doLRS();	// TODO: Remove this in final version, debug only

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
			aSector.checkHits();
		}

		boolean leaving = true;
		while (leaving) {
			leaving = false;
			for (Sector aSector : sectorListHead) {
				if (aSector.doJumps(delta) ) { leaving = true; break; }
				if (aSector.checkLeaving()) { leaving = true; break; }
			}
		}

		for ( Sector aSector : sectorListHead) {
			aSector.doRemove();
			aSector.doAdd();
		}
	}
}

