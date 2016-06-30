import org.lwjgl.util.vector.Vector3f;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL11.glViewport;

/**
 * Created by Mark on 8/12/2015.
 */
public class Galaxy {

	/* Routine to build the two dimensional array that contains the sectors of the galaxy
	 *
	 */
	public Sector[][] allocate(Class<Sector> c,int row,int column){
		Sector[][] matrix = (Sector[][]) Array.newInstance(c, column, row);
		for (int x = 0; x < column; x++) {
			//matrix[x] = (Sector[]) Array.newInstance(c,column);
			for(int y = 0; y < row; y++) {
				matrix[x][y] = new Sector();		// Allocates and initialises a Sector
			}
		}
		return matrix;
	}

	public Sector[][] sectorArray;

	private int galaxyWidth, galaxyHeight;

	public Galaxy( int width, int height ) {
		galaxyWidth = width;
		galaxyHeight = height;
		sectorArray = allocate(Sector.class, width, height);
	}

	public Sector getSector(Vector3f loc) {
		return getSector((int)Math.floor(loc.x), (int)(Math.floor(loc.y)));
	}

	public Sector getSector( int sectorX, int sectorY) {
		assert(sectorX >= 0);
		assert(sectorY >= 0);
		assert(sectorX < galaxyWidth);
		assert(sectorY < galaxyHeight);
		Sector req = sectorArray[sectorX][sectorY];
		assert(req != null);
		return req;
	}

	// Galactic logic implemented here and passed down the classes
	public void doLogic(double secondsElapsed) {
		for (int gx = 0; gx < galaxyWidth; gx++) {
			for (int gy = 0; gy < galaxyHeight; gy++) {
				Sector aSector = sectorArray[gx][gy];
				aSector.doLogic(secondsElapsed);
			}
		}
	}
}

