import java.util.ArrayList;

/**
 * Created by Mark on 10/07/2016.
 */
public class AIManagement {
	ComputerManagement computer;
	Alliance alliance;
	Galaxy galaxy;

	double thinkClock = 0;

	public AIManagement() {

	}

	public void setComputer(ComputerManagement c) { computer = c; }

	public void setAlliance(Alliance a) { alliance = a; }

	public void setGalaxy(Galaxy g) { galaxy = g; }

	public void doLogic(double secondsElapsed) {
		thinkClock += secondsElapsed;
		Sector sector;

		for (int gx = 0; gx < alliance.galaxySize; gx++) {
			for (int gy = 0; gy < alliance.galaxySize; gy++) {
				sector = galaxy.getSector(gx, gy);
				doLogic(sector);
			}
		}
	}

	public void doLogic(Sector sector) {
		int ex, ey;
		ArrayList<Entity> readyForAI = new ArrayList<>();

		for (Entity entity : sector.entities) {
			if (entity.eType != Entity.SubType.ENEMYSHIP) continue;
			if (thinkClock < entity.AInextThoughtTime) continue;

			readyForAI.add(entity);
		}

		for (Entity entity : readyForAI) {

			if (entity.AInextThoughtTime == 0) {
				entity.AInextThoughtTime = thinkClock + Math.random()*25 + 5;
				continue;
			}

			entity.AInextThoughtTime = thinkClock + Math.random()*25 + 5;

			ex = (int)Math.floor(entity.galacticLoc.x);
			ey = (int)Math.floor(entity.galacticLoc.y);

//			System.out.println("Entity having a thought at " + thinkClock + ", next thought at will be at " + entity.AInextThoughtTime);

			computer.doCommand(entity, "LRS");
			if ( entity.getLRS(ex, ey).enemyCount > 0 ) {
				System.out.println("Found " + entity.getLRS(ex, ey).enemyCount
											+ " enemy for " + entity.eType + " at (" + ex + ", " + ey + ")");
				if (entity.torpedoCount > 0)
					computer.doCommand(entity, "TOR *");
				else
					computer.doCommand(entity, "PHA " + (int)(entity.energyLevel/4));
			}
		}
	}
}
