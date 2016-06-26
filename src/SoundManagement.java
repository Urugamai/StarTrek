
import java.util.ArrayList;

/**
 * Created by Mark on 25/06/2016.
 */
public class SoundManagement {
	private class SoundCollection extends java.awt.List {
		private class SoundSet {
			String soundName;
			int soundID;

			public SoundSet(String name, int ID) {
				soundName = name;
				soundID = ID;
			}
		}

		private ArrayList<SoundSet> soundList = new ArrayList<SoundSet>();

		public SoundCollection() {
		}

		public void add(String soundName, int soundID) {
			SoundSet aSound;

			aSound = find(soundName);
			aSound.soundID = soundID;

			soundList.add(aSound);
		}

		public SoundSet find(String soundName) {
			SoundSet aSound = null;

			for(SoundSet aSnd : soundList) {
				if (aSnd.soundName.equalsIgnoreCase(soundName)) return aSnd;
			}

			aSound = new SoundSet(soundName, -1);
			return aSound;
		}

		public int get(String soundName) {
			for(SoundSet aSnd : soundList) {
				if (aSnd.soundName.equalsIgnoreCase(soundName)) return aSnd.soundID;
			}
			return -1;
		}
	}

	private static SoundManager soundManager;
	private SoundCollection sounds = new SoundCollection();

	public SoundManagement() {
		// create our sound manager, and initialize it with 8 channels
		// Increase this if we need more sounds to be playing simultaneously?
		soundManager = new SoundManager();
		soundManager.initialize(8);
	}

	public void loadSound(String soundName, String soundFile) {
		int soundID;

		soundID = soundManager.addSound(soundFile);

		sounds.add(soundName, soundID);
	}

	public void playSound(String soundName) {
		int aSound = sounds.get(soundName);
		if (aSound == -1) return;

		// play sound
		soundManager.playEffect(aSound);
	}

	public void Destroy() {
		soundManager.destroy();
	}
}
