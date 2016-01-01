/**
 * Created by Mark on 8/12/2015.
 */
public class Galaxy {
	private Game				game;						/** The game in which this entity exists */
	private static final int sizeX = 10;					// Width of galaxy
	private static final int sizeY = 10;					// Height of galaxy
	private int alienCount = 0;
	private int starbaseCount = 0;
	private GameText galacticMap;

	private Sector[][] sectorList = new Sector[sizeX][sizeY];
	private int[][][] LRS = new int[sizeX][sizeY][Constants.LRSItems.Size];

	public class galacticLocation {
		private int gx;
		private int gy;

		public galacticLocation() {}
		public galacticLocation(int x, int y) { gx = x; gy = y; }
		public int getGx() { return gx; }
		public int getGy() { return gy; }
		public void setGx(int X) { if (X <= sizeX && X >= 0 ) gx = X; }
		public void setGy(int Y) { if (Y <= sizeY && Y >= 0 ) gy = Y; }
	}

	public Galaxy(Game game) {
		this.game = game;

		for (int gx = 0; gx < sizeX; gx++) {
			for (int gy = 0; gy < sizeY; gy++) {
				sectorList[gx][gy] = new Sector(game);
				if (sectorList[gx][gy].getStarbaseCount() > 0) {
					LRS[gx][gy][Constants.LRSItems.Enemy.value()] = sectorList[gx][gy].getEnemyCount();
					LRS[gx][gy][Constants.LRSItems.Starbases.value()] = sectorList[gx][gy].getStarbaseCount();
					LRS[gx][gy][Constants.LRSItems.Planets.value()] = sectorList[gx][gy].getPlanetCount();
				} else {
					LRS[gx][gy][Constants.LRSItems.Enemy.value()] = -1;
					LRS[gx][gy][Constants.LRSItems.Starbases.value()] = -1;
					LRS[gx][gy][Constants.LRSItems.Planets.value()] = -1;
				}
			}
		}

		galacticMap = new GameText(10,game.getHeight(),40);
	}

	public void initSectors(int width, int height) {
		alienCount = 0;
		while (alienCount < Constants.startEnemyCount) {
			for (int gx = 0; gx < sizeX; gx++) {
				for (int gy = 0; gy < sizeY; gy++) {
					sectorList[gx][gy].initEntities(width, height);
					alienCount += sectorList[gx][gy].getEnemyCount();
					starbaseCount += sectorList[gx][gy].getStarbaseCount();
				}
			}
		}
	}

	public galacticLocation getSafeSector() {
		galacticLocation gl = new galacticLocation();
		int minEnemyCount = 99, enemyCount;
		int minStarbaseCount = 99, starbaseCount;
		//float minGravity = 99, gravity;
		int bestX = (int)(sizeX/2), bestY = (int)(sizeY/2);  // default to (near) middle sector

		for (int gx = 0; gx < sizeX; gx++) {
			for (int gy = 0; gy < sizeY; gy++) {
				enemyCount = sectorList[gx][gy].getEnemyCount();
				starbaseCount = sectorList[gx][gy].getStarbaseCount();
				//gravity = sectorList[gx][gy].getStarGravity();

				if (enemyCount <= minEnemyCount && starbaseCount <= minStarbaseCount ) {
					bestX = gx; bestY = gy;
					minEnemyCount = enemyCount;
					minStarbaseCount = starbaseCount;
					//minGravity = gravity;
				}
			}
		}
		gl.setGx(bestX);
		gl.setGy(bestY);

		return gl;
	}

	public galacticLocation getLeastSafeSector() {
		galacticLocation gl = new galacticLocation();
		int maxEnemyCount = 0, enemyCount;
		int minStarbaseCount = 99, starbaseCount;
		//float maxGravity = 0, gravity;
		int bestX = (int)(sizeX/2), bestY = (int)(sizeY/2);  // default to (near) middle sector

		for (int gx = 0; gx < sizeX; gx++) {
			for (int gy = 0; gy < sizeY; gy++) {
				enemyCount = sectorList[gx][gy].getEnemyCount();
				starbaseCount = sectorList[gx][gy].getStarbaseCount();
				//gravity = sectorList[gx][gy].getStarGravity();

				if (enemyCount >= maxEnemyCount && starbaseCount <= minStarbaseCount) {
					bestX = gx; bestY = gy;
					maxEnemyCount = enemyCount;
					minStarbaseCount = starbaseCount;
					//maxGravity = gravity;
				}
			}
		}
		gl.setGx(bestX);
		gl.setGy(bestY);

		return gl;
	}

	public galacticLocation getGalacticLocation(int x, int y) {
		galacticLocation gl = new galacticLocation(x,y);
		return gl;
	}

	public int getEnemyCount() {
		return alienCount;
	}

	public int getStarbaseCount() {
		return starbaseCount;
	}

	public Sector getSector(galacticLocation sector) {
		return sectorList[sector.getGx()][sector.getGy()];
	}

	public void doLRS(galacticLocation currentSector ) {
		for (int gx = Math.max(0, currentSector.getGx()-1); gx <= Math.min(sizeX-1, currentSector.getGx()+1); gx++ ) {
			for (int gy = Math.max(0, currentSector.getGy()-1); gy <= Math.min(sizeY-1, currentSector.getGy()+1); gy++ ) {
				LRS[gx][gy][Constants.LRSItems.Enemy.value()] = sectorList[gx][gy].getEnemyCount();
				LRS[gx][gy][Constants.LRSItems.Starbases.value()] = sectorList[gx][gy].getStarbaseCount();
				LRS[gx][gy][Constants.LRSItems.Planets.value()] = sectorList[gx][gy].getPlanetCount();
			}
		}
	}

	public void doSRS(galacticLocation currentSector ) {
		int gx = currentSector.getGx();
		int gy = currentSector.getGy();
		LRS[gx][gy][Constants.LRSItems.Enemy.value()] = sectorList[gx][gy].getEnemyCount();
		LRS[gx][gy][Constants.LRSItems.Starbases.value()] = sectorList[gx][gy].getStarbaseCount();
		LRS[gx][gy][Constants.LRSItems.Planets.value()] = sectorList[gx][gy].getPlanetCount();
	}

	public void draw() {
		int currentRow = 1;
		String currentLine = "";
		for (int gy = 0; gy < sizeY; gy++) {
			currentRow = (sizeY - gy)*2 + 6;
			for (int gx = 0; gx < sizeX; gx++) {

					currentLine += "[";
					currentLine += LRS[gx][gy][Constants.LRSItems.Enemy.value()] < 0 ? " " : LRS[gx][gy][Constants.LRSItems.Enemy.value()];
					currentLine += LRS[gx][gy][Constants.LRSItems.Starbases.value()] < 0 ? " " : LRS[gx][gy][Constants.LRSItems.Starbases.value()];
					currentLine += LRS[gx][gy][Constants.LRSItems.Planets.value()] < 0 ? " " : LRS[gx][gy][Constants.LRSItems.Planets.value()];
					currentLine += "] ";
			}
			galacticMap.writeLine(currentRow, currentLine );
//			System.out.println(currentLine);
			currentLine = "";
		}

		galacticMap.writeLine(sizeY*2 + 5 + 6, "FEDERATION SPACE GALACTIC MAP");
		galacticMap.writeLine(sizeY*2 + 3 + 6, "There are " + alienCount + " enemy ships currently in federation space");
		galacticMap.writeLine(sizeY*2 + 2 + 6, "You currently have " + starbaseCount + " starbases available");
		galacticMap.draw();
		galacticMap.writeLine(0 + 6, "Only updated after Long Range Scan performed.");
	}

	// Galactic logic implemented here and passed down the classes
	public void doLogic(double delta) {
		Entity entity;

		for (int gy = 0; gy < sizeY; gy++) {
			for (int gx = 0; gx < sizeX; gx++) {
				sectorList[gx][gy].doLogic(delta);
				while ( (entity = sectorList[gx][gy].getEntityLeavingSector()) != null) {
					transferEntity(entity, gx, gy);
				}
			}
		}
	}

	// Transfer entity from current sector to the one they just travelled into (boundary crossing)
	// OR stop at boundary if it is the galactic edge
	private void transferEntity(Entity entity, int currentGx, int currentGy) {
		int gx = currentGx, gy = currentGy;
		int newX = entity.getX(), newY = entity.getY();
		int sectorWidth = sectorList[currentGx][currentGy].getSectorWidth();
		int sectorHeight = sectorList[currentGx][currentGy].getSectorHeight();
		int spriteWidth = entity.sprite.getWidth();
		int spriteHeight = entity.sprite.getHeight();

		if (newX < 0) {
			gx--;
			newX = sectorWidth - spriteWidth;
		} else if (newX >= sectorWidth) {
			gx++;
			newX = entity.sprite.getWidth();
		}
		if (newY < 0) {
			gy--;
			newY = sectorHeight - spriteHeight;
		} else if (newY >= sectorList[currentGx][currentGy].getSectorHeight()) {
			gy++;
			newY = entity.sprite.getHeight();
		}

		if (gx < 0) {
			gx = 0;
			newY = spriteWidth;
			entity.setVelocity(0);
			entity.setThrust(0, 0);    // all stop
		} else if (gx >= sizeX) {
			gx = sizeX;
			newX = sectorWidth - spriteWidth;
			entity.setVelocity(0);
			entity.setThrust(0, 0);    // all stop
		}

		if (gy < 0) {
			gy = 0;
			newY = spriteHeight;
			entity.setVelocity(0);
			entity.setThrust(0, 0);    // all stop
		}
		if (gy >= sizeY) {
			gy = sizeY;
			newY = sectorHeight - spriteHeight;
			entity.setVelocity(0);
			entity.setThrust(0, 0);    // all stop
		}

		// Move entity from currentGx/currentGy to new gx/gy
		if (sectorList[currentGx][currentGy].takeEntity(entity)) {
			if (sectorList[gx][gy].putEntity(entity)) {
				// transfer successful
				if (entity instanceof PlayerShipEntity) game.setCurrentSector(gx,gy);
				entity.setLocation(newX, newY, 0);
				entity.setSector(sectorList[gx][gy]);
			} else {
				// application failure: report it as 'ship hit a mine between sectors, destroyed'
			}
		} else {
			// Hotel California - you can never leave...  Dont know why this would happen (yet)
			// report it as 'warp drive failure, ship is stuck in this sector'
		}
	}
}

