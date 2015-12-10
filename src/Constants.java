/**
 * Created by Mark on 9/12/2015.
 */
public class Constants {
	/** Game Parameters */
	public static final String		WINDOW_TITLE		= "Star Trek - TNG";

	// Graphics
	public static final String		FILE_IMG_ENTERPRISE	= "res/ST_Enterprise.gif";
	public static final String		FILE_IMG_TORPEDO	= "res/ST_Torpedo.gif";
	public static final String 		FILE_IMG_ROMULAN 	= "res/ST_Romulan.gif";
	public static final String		FILE_IMG_STARBASE	= "res/ST_StarBase.gif";
	public static final String		FILE_IMG_STAR		= "res/ST_Star.gif";

	// Audio
	public static final String		FILE_SND_TORPEDO	= "res/ST_Torpedo.wav";

	// Originals
	public static final String		FILE_IMG_START		= "res/pressanykey.gif";
	public static final String		FILE_IMG_LOSE		= "res/gotyou.gif";
	public static final String		FILE_IMG_WIN		= "res/youwin.gif";

	public static final String		FILE_SND_HIT		= "res/hit.wav";
	public static final String		FILE_SND_LOSE		= "res/loose.wav";
	public static final String		FILE_SND_START		= "res/start.wav";
	public static final String		FILE_SND_WIN		= "res/win.wav";

	public static final int			maxEnemy			= 5;	// Per sector
	public static final float		maxGravity			= 20;
	public static final float		starbaseProbability = 0.2f;
	public static final int			startEnemyCount		= 100;	// Per Galaxy
	public static final int			maxPlanets			= 9;

	// All speeds are % of lightspeed
	public static final float		phaserSpeed			= 1.0f;
	public static final float		warpSpeedMax		= 8.0f;
	public static final float		impulseSpeedMax		= 0.1f;
	public static final float		torpedoSpeed		= 0.5f;
	public static final float		c					= 200.0f;	// The speed of light, in pixels per second

	public static enum DisplayMode { Sector, GalacticMap };
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
