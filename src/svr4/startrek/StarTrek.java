package svr4.startrek;

/**
 * Created by Mark on 29/11/2015.
 */
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;

public class StarTrek {
	public StarTrek() {
		try {
			Display.setDisplayMode(new DisplayMode(700, 1000));
			Display.create();

			while(!Display.isCloseRequested()) {
				Display.update();
			}

			Display.destroy();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		new StarTrek();
	}
}
