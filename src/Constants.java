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
	public static final String		FILE_SND_LOSE		= "res/ST_lose.wav";	// TODO Replace sound with one of my own

	// Audio
	public static final String		FILE_SND_TORPEDO	= "res/ST_Torpedo.wav";

	// Originals - to be replaced
	public static final String		FILE_SND_HIT		= "res/hit.wav";
	public static final String		FILE_SND_START		= "res/start.wav";
	public static final String		FILE_SND_WIN		= "res/win.wav";

	/** Game Parameters */
	public static final String		WINDOW_TITLE		= "Star Trek - TNG";
	public static final int			FramesPerSecond		= 60;
	public static final int			screenLines			= 40;		// Number of text lines to provide on the screen

	public static final int			maxEnemy			= 5;		// Per sector
	public static final float		starbaseProbability = 0.1f;
	public static final int			startEnemyCount		= 100;		// Per Galaxy
	public static final int			maxPlanets			= 9;		// Per Sector

	// All speeds are % of lightspeed?
	public static final float 		PHASER_SPEED 		= 1000.0f;		// pixels per second
	public static final float		warpSpeedMax		= 10.0f;		// multiple of light speed
	public static final float 		IMPULSE_MAX 		= 0.2f;		// multiple of light speed
	public static final float 		TORPEDO_SPEED 		= 100.0f;		// pixels per second
	public static final float		c					= 500.0f;	// The speed of light, in pixels per second

	public static final float		maxEnergy			= 3000f;
	public static final int 		maxTorpedoes		= 15;

	public static final float		maxStarbaseEnergy	= 300000f;
	public static final int 		maxStarbaseTorpedoes = 1500;

	public static enum DisplayMode {HELP_SCREEN, DISPLAY_SECTOR, GALACTIC_MAP, SHIP_STATUS};
	public static enum sectorDirection { LeftTop, Top, RightTop, Left, here, Right, LeftBottom, Bottom, RightBottom };
	public static enum LRSItems {
		Enemy(0), Starbases(1), Planets(2);
		private final int value;
		LRSItems(int value) {this.value = value; }
		public int value() { return value; }
		public static final int Size = LRSItems.values().length;
	};

	// Prevent construction call
	private Constants(){ }
}
