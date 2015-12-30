/**
 * Created by Mark on 8/12/2015.
 */
public class Galaxy {
	private Game				game;						/** The game in which this entity exists */
	private static final int sizeX = 10;
	private static final int sizeY = 10;
	private int alienCount = 0;
	private int starbaseCount = 0;
	private GameText galacticMap;

	private Sector[][] sectorList = new Sector[sizeX][sizeY];
	private int[][][] LRS = new int[sizeX][sizeY][Constants.LRSItems.Size];

	public class galacticLocation {
		private int gx;
		private int gy;

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

	public int getSectorEnemyCount(galacticLocation sector) {
		return sectorList[sector.getGx()][sector.getGy()].getEnemyCount();
	}

	public int getSectorStarbaseCount(galacticLocation sector) {
		return sectorList[sector.getGx()][sector.getGy()].getStarbaseCount();
	}

//	public float getSectorGravity(galacticLocation sector) {
//		return sectorList[sector.getGx()][sector.getGy()].getStarGravity();
//	}

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

		galacticMap.writeLine(22 + 6, "FEDERATION SPACE GALACTIC MAP");
		galacticMap.draw();
		galacticMap.writeLine(0 + 6, "Only updated after Long Range Scan performed.");
	}
}

