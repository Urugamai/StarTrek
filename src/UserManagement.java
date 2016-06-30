/**
 * Created by Mark on 25/06/2016.
 */

import org.lwjgl.Sys;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.util.ArrayList;

public class UserManagement {
	private Alliance alliance = null;
	private Galaxy galaxy = null;

	private String				userInput 			= "";
	private String				cmd = "";
	private ArrayList<String> userInputHistory	= new ArrayList<>();
	private int					historyLines = 0, historyPosition = 0;
	private boolean				returnDown;
	private boolean				inKeyUp = false, inKeyDown = false;

	public UserManagement() {
		// grab the mouse, don't want a cursor when we're playing!
		Mouse.setGrabbed(true);
	}

	public boolean Update() {
		if (Keyboard.isKeyDown(Keyboard.KEY_ESCAPE)) return false;	// Game Over


		// Process the command history buffer
		if (Keyboard.isKeyDown(Keyboard.KEY_UP)) {
			if (!inKeyUp) {
				userInput = userInputHistory.get(historyPosition--);
				if (historyPosition < 0) historyPosition = 0;
				inKeyUp = true;
			}
		} else if (Keyboard.isKeyDown(Keyboard.KEY_DOWN)) {
			if (!inKeyDown) {
				historyPosition++;
				if (historyPosition >= userInputHistory.size()) historyPosition--;
				userInput = userInputHistory.get(historyPosition);
				inKeyDown = true;
			}
		} else {
			inKeyUp = false;
			inKeyDown = false;
		}

		char key = getCurrentKey();

		if (key != '\0') {
			// do something with this key
			if (key == 13) {
				userInputHistory.add(userInput);
				historyPosition = userInputHistory.size()-1;
				cmd = userInput;
				userInput = "";
			} else {
				if (key == 8 && userInput.length() > 0 ) { // Backspace
					userInput = userInput.substring(0, userInput.length() - 1);
				} else {
					userInput += key;
				}
			}
		}

		return true;
	}

	private char getCurrentKey() {
		boolean returnPressed = Keyboard.isKeyDown(Keyboard.KEY_RETURN);
		if (returnPressed) {
			if (returnDown) return '\0';  // Already processed this pressing of return
			returnDown = true;
			return Keyboard.getEventCharacter();
		}

		if (Keyboard.next()) {
			return Keyboard.getEventCharacter();
		}

		return '\0';
	}

	public String getUserInput() {
		return userInput;
	}

	public String getCommand() {
		return cmd;
	}

	public void clearCommand() {
		cmd = "";
	}

	public void setAlliance(Alliance a) { alliance = a; }

	public void setGalaxy(Galaxy g) {
		galaxy = g;
	}

}
