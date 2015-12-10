/**
 * Created by Mark on 8/12/2015.
 */
public class Galaxy {
	private Game				game;						/** The game in which this entity exists */
	private static final int sizeX = 10;
	private static final int sizeY = 10;
	private int alienCount = 0;
	private GameText galacticMap;

	private Sector[][] sectorList = new Sector[sizeX][sizeY];

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
			}
		}

		galacticMap = new GameText(10,game.getHeight(),40);
	}

	public void initSectors(int width, int height) {
		while (alienCount < Constants.startEnemyCount) {
			alienCount = 0;
			for (int gx = 0; gx < sizeX; gx++) {
				for (int gy = 0; gy < sizeY; gy++) {
					sectorList[gx][gy].initEntities(width, height);
					alienCount += sectorList[gx][gy].getEnemyCount();
				}
			}
		}
	}

	public galacticLocation getSafeSector() {
		galacticLocation gl = new galacticLocation();
		int minEnemyCount = 99, enemyCount;
		int minStarbaseCount = 99, starbaseCount;
		float minGravity = 99, gravity;
		int bestX = (int)(sizeX/2), bestY = (int)(sizeY/2);  // default to (near) middle sector

		for (int gx = 0; gx < sizeX; gx++) {
			for (int gy = 0; gy < sizeY; gy++) {
				enemyCount = sectorList[gx][gy].getEnemyCount();
				starbaseCount = sectorList[gx][gy].getStarbaseCount();
				gravity = sectorList[gx][gy].getStarGravity();

				if (enemyCount <= minEnemyCount && starbaseCount <= minStarbaseCount && gravity <= minGravity) {
					bestX = gx; bestY = gy;
					minEnemyCount = enemyCount;
					minStarbaseCount = starbaseCount;
					minGravity = gravity;
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
		float maxGravity = 0, gravity;
		int bestX = (int)(sizeX/2), bestY = (int)(sizeY/2);  // default to (near) middle sector

		for (int gx = 0; gx < sizeX; gx++) {
			for (int gy = 0; gy < sizeY; gy++) {
				enemyCount = sectorList[gx][gy].getEnemyCount();
				starbaseCount = sectorList[gx][gy].getStarbaseCount();
				gravity = sectorList[gx][gy].getStarGravity();

				if (enemyCount >= maxEnemyCount && starbaseCount <= minStarbaseCount && gravity >= maxGravity) {
					bestX = gx; bestY = gy;
					maxEnemyCount = enemyCount;
					minStarbaseCount = starbaseCount;
					maxGravity = gravity;
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

	public float getSectorGravity(galacticLocation sector) {
		return sectorList[sector.getGx()][sector.getGy()].getStarGravity();
	}

	public Sector getSector(galacticLocation sector) {
		return sectorList[sector.getGx()][sector.getGy()];
	}

	public void draw() {
		int currentRow = 1;
		String currentLine = "";
		for (int gy = 0; gy < sizeY; gy++) {
			currentRow = (sizeY - gy)*2 + 6;
			for (int gx = 0; gx < sizeX; gx++) {
				if (sectorList[gx][gy].getSectorLRS()) {
					currentLine += "[";
					currentLine += sectorList[gx][gy].getEnemyCount();
					currentLine += sectorList[gx][gy].getStarbaseCount();
					currentLine += sectorList[gx][gy].getPlanetCount();
					currentLine += "] ";
				} else {
					currentLine += "[";
					currentLine += " ";
					currentLine += " ";
					currentLine += " ";
					currentLine += "] ";
				}
				sectorList[gx][gy].setLRS(false);	// it is in our galactic map, don't refresh until we have scanned it again
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

