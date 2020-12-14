import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.sound.midi.MidiChannel;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequencer;
import javax.sound.midi.Synthesizer;

import org.newdawn.easyogg.OggClip;

import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.PausablePlayer;

public class RadicalMidi {

	BufferedInputStream is;
	Sequencer sequencer;
	boolean paused;
	boolean loaded;
	boolean playing;
	boolean isMp3;
	boolean isOgg;
	String s;
	FileInputStream fi;
	File fl;
	OggClip ogg;

	PausablePlayer player;
	String filePath;

	/**
	 * Sets up the RadicalMidi for playback.
	 * Use load() to load the file;
	 * Use play() to play (and loop) the file;
	 * use setPaused(true/false) to pause/resume the file;
	 * Use unload() to unload the file, then set RadicalMidi to null;
	 * Use playMidi() or playMidi(int gain) or playMidi(int gain, int loops) to manuall play a midi file.
	 * 
	 * @param fn the file name of the file to load.
	 */
	public RadicalMidi(String fn) {
		loaded = false;
		playing = false;
		isMp3 = false;
		isOgg = false;
		if (fn.endsWith(".mp3")) { // is it an mp3?
			s = fn;
			isMp3 = true;
			isOgg = false;
			fl = new File(fn);
			try {
				fi = new FileInputStream(fl);
				player = new PausablePlayer(fi);
			} catch (JavaLayerException | FileNotFoundException ex) {
				System.out.println("Error loading Mp3!");
				ex.printStackTrace();
			}
		} else if (fn.endsWith(".ogg")) { //is it an ogg?
			s = fn;
			isMp3 = false;
			isOgg = true;
			try {
				ogg = new OggClip(fn);
			} catch (IOException e) {
				System.out.println("Error loading Ogg!");
				e.printStackTrace();
			}
		} else { //then it must be a midi!
			isMp3 = false;
			isOgg = false;
			s = fn;
			try {
				fi = new FileInputStream(new File(fn));
			} catch (java.io.FileNotFoundException ex) {
				System.out.println("Midi file \"" + fn + "\" not found!");
				ex.printStackTrace();
			}
			try {
				// Obtains the default Sequencer connected to a default device.
				sequencer = MidiSystem.getSequencer();

				// Opens the device, indicating that it should now acquire any
				// system resources it requires and become operational.
				sequencer.open();

			} catch (Exception ex) {
				System.out.println("Error loading Midi file \"" + fn + "\":");
				ex.printStackTrace();
			}
		}
	}

	/**
	 * Sets up the midi loader.
	 */
	public void load() {
		if (!isOgg && !isMp3) loadMidi();
	}

	public void play() {
		if (isMp3) playMp3();
		else if (isOgg) ogg.loop();
		else playMidi();
	}

	@Deprecated
	/**
	 * Pauses the midi/mp3/ogg playback.
	 * Deprecated: Use setPaused instead.
	 */
	public void resume() {
		if (isMp3) player.resume();
		else if (isOgg) ogg.resume();
		else resumeMidi();
	}

	@Deprecated
	/**
	 * Pauses the midi/mp3/ogg playback.
	 * Deprecated: Use setPaused instead.
	 */
	public void stop() {
		if (isMp3) player.pause();
		else if (isOgg) ogg.pause();
		else stopMidi();
	}

	/**
	 * Unloads the midi and forcefully stops playback.
	 */
	public void unload() {
		if (isMp3) player.close();
		else if (isOgg) unloadOgg();
		else unloadMidi();
	}

	/**
	 * Loads the midi file.
	 * Should never be used directly.
	 * Use load() instead.
	 */
	public void loadMidi() {
		try {
			// create a stream from a file
			is = new BufferedInputStream(fi);
			loaded = true;

		} catch (Exception ex) {
			System.out.println("Error buffering Midi file:");
			ex.printStackTrace();
		}
	}

	@Deprecated
	/**
	 * Resumes playback of the midi.
	 * 
	 * @param	gain	the sound volume in percent
	 * @param	loops	amount of times to loop the midi
	 * 
	 */
	public void resumeMidi(int gain, int loops) {
		try {
			fi = new FileInputStream(new File(s));
			is = new BufferedInputStream(fi);
		} catch (IOException ex) {
			System.out.println("Midi file not found!");
			ex.printStackTrace();
		} catch (Exception ex) {
			System.out.println("Error buffering Midi file:");
			ex.printStackTrace();
		}
		playMidi(gain, loops);
	}

	@Deprecated
	/**
	 * Resumes playback of the midi.
	 * 
	 * @param	gain	the sound volume in percent
	 * 
	 */
	public void resumeMidi(int gain) {
		try {
			fi = new FileInputStream(new File(s));
			is = new BufferedInputStream(fi);
		} catch (IOException ex) {
			System.out.println("Midi file not found!");
			ex.printStackTrace();
		} catch (Exception ex) {
			System.out.println("Error buffering Midi file:");
			ex.printStackTrace();
		}
		playMidi(gain);
	}

	@Deprecated
	/**
	 * Resumes playback of the midi.
	 */
	public void resumeMidi() {
		try {
			fi = new FileInputStream(new File(s));
			is = new BufferedInputStream(fi);
		} catch (IOException ex) {
			System.out.println("Midi file not found!");
			ex.printStackTrace();
		} catch (Exception ex) {
			System.out.println("Error buffering Midi file:");
			ex.printStackTrace();
		}
		playMidi();
	}

	/**
	 * Begins playing the midi.
	 * 
	 * @param	gain	the sound volume in percent
	 * @param	loops	amount of times to loop the midi
	 * 
	 */
	public void playMidi(int gain, int loops) {

		try {
			// Sets the current sequence on which the sequencer operates.
			// The stream must point to MIDI file data.
			sequencer.setSequence(is);

			sequencer.setLoopCount(loops);

			if (sequencer instanceof Synthesizer) {
				Synthesizer synthesizer = (Synthesizer) sequencer;
				MidiChannel[] channels = synthesizer.getChannels();

				// gain is a value between 0 and 1 (loudest)
				for (int i = 0; i < channels.length; i++) {
					channels[i].controlChange(7, (int)((float)gain * 1.27));
				}
			}

			// Starts playback of the MIDI data in the currently loaded sequence.
			sequencer.start();

			playing = true;
		} catch (IllegalArgumentException ex) {
			System.out.println("There is a mistake in your Midi code,");
			System.out.println("please re-check!");
			ex.printStackTrace();
		} catch (java.lang.IllegalStateException ex) {
			System.out.println("Error playing Midi file " + s + ", check if the file exists!");
			ex.printStackTrace();
		} catch (Exception ex) {
			System.out.println("Error playing Midi file:");
			ex.printStackTrace();
		}
	}

	/**
	 * Begins playing the midi.
	 * 
	 * @param	gain	the sound volume in percent
	 * 
	 */
	public void playMidi(int gain) {

		try {
			// Sets the current sequence on which the sequencer operates.
			// The stream must point to MIDI file data.
			sequencer.setSequence(is);

			// loop forever
			sequencer.setLoopCount(9999);

			if (sequencer instanceof Synthesizer) {
				Synthesizer synthesizer = (Synthesizer) sequencer;
				MidiChannel[] channels = synthesizer.getChannels();

				// gain is a value between 0 and 1 (loudest)
				for (int i = 0; i < channels.length; i++) {
					channels[i].controlChange(7, (int) ((float)gain * 1.27));
				}
			}

			// Starts playback of the MIDI data in the currently loaded
			// sequence.
			sequencer.start();

			playing = true;
		} catch (IllegalArgumentException ex) {
			System.out.println("There is a mistake in your Midi code,");
			System.out.println("please re-check!");
			ex.printStackTrace();
		} catch (java.lang.IllegalStateException ex) {
			System.out.println("Error playing Midi file " + s + ", check if the file exists!");
			ex.printStackTrace();
		} catch (Exception ex) {
			System.out.println("Error playing Midi file:");
			ex.printStackTrace();
		}
	}
	
	/**
	 * Begins playing the midi.
	 */
	public void playMidi() {

		try {
			// Sets the current sequence on which the sequencer operates.
			// The stream must point to MIDI file data.
			sequencer.setSequence(is);

			// loop forever
			sequencer.setLoopCount(9999);

			// Starts playback of the MIDI data in the currently loaded
			// sequence.
			sequencer.start();

			playing = true;
		} catch (IllegalArgumentException ex) {
			System.out.println("There is a mistake in your Midi code,");
			System.out.println("please re-check!");
			ex.printStackTrace();
		} catch (java.lang.IllegalStateException ex) {
			System.out.println("Error playing Midi file " + s + ", check if the file exists!");
			ex.printStackTrace();
		} catch (Exception ex) {
			System.out.println("Error playing Midi file:");
			ex.printStackTrace();
		}
	}

	/**
	 * Sets the paused state. Music may not immediately pause.
	 */
	public void setPaused(boolean paused) {
		if (isOgg || isMp3) {
			if (paused) {
				if (isMp3) player.pause();
				else if (isOgg) ogg.pause();
			} else {
				if (isMp3) player.resume();
				else if (isOgg) ogg.resume();
			}
		} else if (this.paused != paused && sequencer != null && sequencer.isOpen()) {
			this.paused = paused;
			if (paused) {
				sequencer.stop();
			} else {
				sequencer.start();
			}
		}
	}

	/**
	 * Returns the paused state.
	 */
	public boolean isPaused() {
		return paused;
	}

	/**
	 * Stops the midi sequencer.
	 */
	public void stopMidi() {
		System.out.println("Stopping Midi file...");
		try {
			sequencer.stop();
			playing = false;
		} catch (Exception ex) {
			System.out.println("Error stopping Midi file:");
			ex.printStackTrace();
		}
	}
	
	/**
	 * Closes the midi sequencer.
	 */
	public void unloadMidi() {
		try {
			is.close();
			loaded = false;
		} catch (Exception ex) {
			System.out.println("Error unloading Midi file:");
			ex.printStackTrace();
		}
	}
	
	/**
	 * Begins playing an MP3.
	 */
	public void playMp3() {
		try {
			player.play();
		} catch (JavaLayerException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Closes the OGG player.
	 */
	public void unloadOgg() {
		ogg.stop();
		ogg.close();
	}

}		