import org.lwjgl.util.vector.Vector3f;

/**
 * Created by Mark on 25/06/2016.
 */
public class ComputerManagement {
	private Galaxy galaxy = null;
	private Entity ship = null;

	public void setGalaxy(Galaxy g) {
		galaxy = g;
	}

	public void setShip(Entity s) {
		ship = s;
	}

	public boolean doCommand(String cmd) {
		String[] pieces;

		pieces = cmd.trim().toUpperCase().split("[ ,\t\\n\\x0B\\f\\r]");

		if (pieces[0].compareToIgnoreCase("TOR") == 0 ) { command_TOR(pieces); }
		else if (pieces[0].compareToIgnoreCase("PHA") == 0 ) { command_PHA(pieces); }
		else if (pieces[0].compareToIgnoreCase("IMP") == 0 ) { command_IMP(pieces); }
		else if (pieces[0].compareToIgnoreCase("WARP") == 0 ) { command_WARP(pieces); }
		else if (pieces[0].compareToIgnoreCase("STOP") == 0 ) { command_STOP(pieces); }
		else if (pieces[0].compareToIgnoreCase("COMP") == 0 ) { command_COMP(pieces); }
//		else if (pieces[0].compareToIgnoreCase("LRS") == 0 ) { galaxy.doLRS(); }
//		else if (pieces[0].compareToIgnoreCase("SRS") == 0 ) { galaxy.doSRS(); }
//		else if (pieces[0].compareToIgnoreCase("SHUP") == 0 ) { ship.shieldsUp = true; }
//		else if (pieces[0].compareToIgnoreCase("SHDOWN") == 0 ) { ship.shieldsUp = false; }
		else return false;

		return true;
	}

	private boolean command_TOR(String[] pieces) {
		float angle;
		String direction = "";

		if (pieces.length > 1) {
			direction = pieces[1];
			try {
				angle = Float.valueOf(direction);
			} catch (Exception e) {
				return false;  // no firing for you when you get the parameter wrong
			}
		} else {
			return false;  // no firing for you when you get the parameter wrong
		}

//TODO		galaxy.playerFiredTorpedo(angle);
		return true;
	}

	private boolean command_PHA(String[] pieces) {
		float angle = -1;
		float power = 0;

		if (pieces.length > 2) {
			try {
				angle = Float.valueOf(pieces[1]);
				power = Float.valueOf(pieces[2]);
			} catch (Exception e) {
				return false;  // no firing for you when you get the parameter wrong
			}
		} else {
			return false;  // no firing for you when you get the parameter wrong
		}

//TODO		galaxy.playerFiredPhaser(angle, power);
		return true;
	}

	private boolean command_IMP(String[] pieces) {
		float angle;
		float force;
		float seconds;
		String direction = "", power = "", duration = "";

		if ( pieces.length > 3 ) {
			direction = pieces[1];
			power = pieces[2];
			duration = pieces[3];

			try {
				angle = Float.valueOf(direction) % 360;
				if (angle < 0) angle += 360;
				force = Float.valueOf(power); if (force > 20) force = 20;	// technically could make the energy requirements exponential and so preclude the need for a limit
				seconds = Float.valueOf(duration);							// Dont need time limit as energy reserves will expire and stop progress anyway
			} catch (Exception e) {
				return false; // no moving for you when you get the parameters wrong
			}
		} else {
			return false; // no moving for you when you get the parameters wrong
		}


//TODO		galaxy.setPlayerHeading(angle, 0);
//TODO		galaxy.setPlayerThrust( force, seconds);

		return true;
	}

	private boolean command_WARP(String[] pieces) {
		float angle;
		float force;
		float seconds;
		String direction = "", power = "", duration = "";

		if ( pieces.length > 3 ) {
			direction = pieces[1];
			power = pieces[2];
			duration = pieces[3];

			try {
				angle = Float.valueOf(direction) % 360;
				if (angle < 0) angle += 360;
				force = Float.valueOf(power); if (force > 10) force = 10; // again, energy requirements will preclude this limit in the future
				seconds = Float.valueOf(duration);
			} catch (Exception e) {
				return false; // no moving for you when you get the parameters wrong
			}
		} else {
			return false; // no moving for you when you get the parameters wrong
		}

//TODO		galaxy.setPlayerHeading(angle, 0);
//TODO		galaxy.setPlayerThrust( 0, 6);	// Turn in the desired direction, it takes 6 seconds to do 180 degrees

//TODO		galaxy.setPlayerWarp(force, seconds);

		return true;
	}

	private boolean command_STOP(String[] pieces) {
//TODO		float currentVelocity = galaxy.getPlayerVelocity();
//TODO		if (currentVelocity > 0) {
//TODO			galaxy.setPlayerThrust(-50, currentVelocity / 50.0f + 1);
//TODO		}
		return true;
	}

	private boolean command_COMP_TARGET(String[] pieces) {
		int currentLine = 1;

		if (pieces.length < 3) {
			return false; // no computer command
		}

		if (pieces[2].compareToIgnoreCase("E") == 0 ) {
//TODO			for (Entity ent : galaxy.playerSector.entities) {
//TODO				if (ent instanceof PlayerShipEntity) continue;
//TODO				if (ent instanceof StarbaseEntity) continue;
//TODO				if (ent instanceof StarEntity) continue;

			// Must be an enemy
			if (currentLine < 6) {
//TODO					otherLoc = new Galaxy.locationSpec( ent.getX(), ent.getY(), ent.getZ());
//TODO					textWindow.writeAt(currentLine++, ent.eType + " angle: "  + compute_angle_between(myLoc, otherLoc) + " range " +  compute_distance_between(myLoc, otherLoc) + "." );
			}
//TODO			}
		}
		else if (pieces[2].compareToIgnoreCase("B") == 0 ) {
//TODO			for (Entity ent : galaxy.playerSector.entities) {
//TODO				if (! (ent instanceof StarbaseEntity) ) continue;

			// Must be an Starbase
			if (currentLine < 6) {
//TODO					otherLoc = new Galaxy.locationSpec( ent.getX(), ent.getY(), ent.getZ());
//TODO					textWindow.writeAt(currentLine++, "Starbase angle: "  + compute_angle_between(myLoc, otherLoc) + " range " +  compute_distance_between(myLoc, otherLoc) + "." );
			}
//TODO			}
		}
		else if (pieces[2].compareToIgnoreCase("S") == 0 ) {
//TODO			for (Entity ent : galaxy.playerSector.entities) {
//TODO				if (! (ent instanceof StarEntity) ) continue;

			// Must be a Star
			if (currentLine < 6) {
//TODO					otherLoc = new Galaxy.locationSpec( ent.getX(), ent.getY(), ent.getZ());
//TODO					textWindow.writeAt(currentLine++, "Star angle: "  + compute_angle_between(myLoc, otherLoc) + " range " +  compute_distance_between(myLoc, otherLoc) + "." );
			}
//TODO			}
		}
		else return false;

		return true;
	}

	private boolean command_COMP_NAVIGATION(String[] pieces) {
//TODO		Galaxy.locationSpec myLoc = new Galaxy.locationSpec( galaxy.playerSector.getGalacticX(), galaxy.playerSector.getGalacticY(), galaxy.playerSector.getGalacticZ() );
		Vector3f otherLoc = null;
		int enemyCount = 0;

		if (pieces.length < 3) {
			return false; // no computer command
		}

		if (pieces[2].compareToIgnoreCase("B") == 0 ) {
//TODO			otherLoc = galaxy.getNearestStarbase(myLoc);
			if (otherLoc == null) {
				return true;
			}
//TODO			textWindow.writeAt(1, "Starbase angle: "  + compute_angle_between(myLoc, otherLoc) + " range " +  compute_distance_between(myLoc, otherLoc) + "." );
		}
		else if (pieces[2].compareToIgnoreCase("E") == 0 ) {
			if (pieces.length > 3) {
				try {
					enemyCount = Integer.valueOf(pieces[3]);
				} catch (Exception e) {
					return false; // no moving for you when you get the parameters wrong
				}
//TODO				otherLoc = galaxy.getNearestEnemyByCount(myLoc, enemyCount);
			} else
//TODO				otherLoc = galaxy.getNearestEnemy(myLoc);

				if (otherLoc == null) {
					return true;
				}
//TODO			textWindow.writeAt(1, "Nearest Enemy angle: "  + compute_angle_between(myLoc, otherLoc) + " range " +  compute_distance_between(myLoc, otherLoc) + "." );
		}

		return true;
	}

	private boolean command_COMP(String[] pieces) {
		if (pieces.length < 2) {
			return false; // no computer command
		}

		if (pieces[1].compareToIgnoreCase("TGT") == 0 ) { command_COMP_TARGET(pieces); }
		else if (pieces[1].compareToIgnoreCase("NAV") == 0 ) { command_COMP_NAVIGATION(pieces); }
		else return false;

		return true;
	}

	// TODO work out how to get help on the screen and IF we even want to do this.
//	private void initHelp() {
//		helpWindow.writeLn( "F1                          This help screen");
//		helpWindow.writeLn( "F2                          Galaxy Display");
//		helpWindow.writeLn( "F3                          Sector Display");
//		helpWindow.writeLn( "F4                          Ship Status Display");
//		helpWindow.writeLn("");
//		helpWindow.writeLn( "TOR angle                   Send a torpedo out at the angle (in degrees) provided");
//		helpWindow.writeLn( "PHA angle power             Fire phasers along indicated angle starting with indicated power. Power drops by 1 for each 1 unit of range.");
//		helpWindow.writeLn( "IMP angle force duration    Turn ship towards angle while applying force for indicated number of seconds");
//		helpWindow.writeLn( "WARP angle factor duration  Turn ship towards angle then travel at Warp Factor provided for indicated number of seconds");
//		helpWindow.writeLn( "                            Warp factor 1 for 1 second will take you one sector.  Higher factors take more energy, travel further and get there faster.");
//		helpWindow.writeLn( "STOP                        apply maximum deceleration force until we have stopped moving");
//		helpWindow.writeLn( "SRS                         Short range scan, refreshes data about the current sector.");
//		helpWindow.writeLn( "LRS                         Long range scan, Gather information from adjoining sectors. This updates Galactic Map.");
//		helpWindow.writeLn( "SHUP                        Shields UP");
//		helpWindow.writeLn( "SHDOWN                      Shields DOWN");
//		helpWindow.writeLn( "EXIT");
//		helpWindow.writeLn("");
//		helpWindow.writeLn( "COMP command [parameters]   Ask computer to calculate something");
//		helpWindow.writeLn( "        NAV [BE]            Computer navigation angle (and range) to any Base or Enemy in current sector");
//		helpWindow.writeLn( "        TGT [BES]           Computer targeting information (angle and range) to Base, Enemy or Star in sector.");
//		helpWindow.writeLn( " ");
//		helpInitialised = true;
//	}
//	private void drawHelp() {
//		if (!helpInitialised) initHelp();
//		helpWindow.draw();
//	}
//

}
