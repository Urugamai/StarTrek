import java.awt.*;

import static java.awt.Font.PLAIN;

/**
 * Created by Mark on 9/12/2015.
 */
public class Constants {
	// Graphics
	public static final String		FILE_IMG_ENTERPRISE	= "res/ST_Enterprise.gif";
	public static final String		FILE_IMG_TORPEDO	= "res/ST_Torpedo.gif";
	public static final String		FILE_IMG_PHASER		= "res/ST_Phaser.gif";
	public static final String 		FILE_IMG_ROMULAN 	= "res/ST_Romulan.gif";
	public static final String		FILE_IMG_STARBASE	= "res/ST_StarBase.gif";
	public static final String		FILE_IMG_STAR		= "res/ST_Star.gif";
	public static final String		FILE_IMG_PLANET		= "res/ST_Planet.gif";

	// Audio
	public static final String		FILE_SND_TORPEDO	= "res/ST_Torpedo.wav";

	// Originals	// TODO Replace sound with one of my own
	public static final String		FILE_SND_HIT		= "res/hit.wav";
	public static final String		FILE_SND_START		= "res/start.wav";
	public static final String		FILE_SND_WIN		= "res/win.wav";
	public static final String		FILE_SND_LOSE		= "res/ST_lose.wav";

	/** Game Parameters */
	public static final String		WINDOW_TITLE		= "Alliance";
	public static final int			FramesPerSecond		= 60;
	public static final int			viewSector = 0, viewStatus = 1, viewComputer = 2, viewGalaxy = 3;
	public static final double 		sectorWindowPercentW = 0.8;	// percentage of viewable screen space to allocate to sector view box
	public static final double 		sectorWindowPercentH = 0.8;

	// PRODUCTION PARAMETERS
	public static final int			maxEnemy			= 9;		// Per sector
	public static final float		starbaseProbability = 0.1f;
	public static final int			maxPlanets			= 9;		// Per Sector
	public static final int			startDate			= 20210703;

	// LIMITS
	public static final float		maxWarpSpeed		= 10.0f;
	public static final float 		maxImpulseSpeed		= 100f;		// pixels per second?

	public static final int 		maxTorpedoes		= 15;
	public static final int 		maxStarbaseTorpedoes = 150;
	public static final float		torpedoSpeed		= 100f;

	// ENERGY
	public static final float		starEnergy 			= 1000000000f;
	public static final float		planetEnergy		= 100000000f;
	public static final float		starbaseEnergy		= 1000000f;
	public static final float		shipEnergy			= 10000f;
	public static final float		dockedEnergy		= 100f;
	public static final float		shieldRunningCost	= 0.01f;		// fraction of shield energy that leaks


	public static enum DisplayMode {HELP_SCREEN, DISPLAY_SECTOR, GALACTIC_MAP, SHIP_STATUS};
	public static enum sectorDirection { LeftTop, Top, RightTop, Left, here, Right, LeftBottom, Bottom, RightBottom };
	public static enum LRSItems {
		Enemy(0), Starbases(1), Planets(2);
		private final int value;
		LRSItems(int value) {this.value = value; }
		public int value() { return value; }
		public static final int Size = LRSItems.values().length;
	};

	public static double sectorXScale = 1, sectorYScale = 1;

	// Prevent construction call
	private Constants(){ }
}
