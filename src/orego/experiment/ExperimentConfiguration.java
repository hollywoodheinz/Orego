package orego.experiment;

import static orego.experiment.Debug.OREGO_ROOT_DIRECTORY;
import static java.io.File.separator;
import static orego.core.Coordinates.getBoardWidth;
import java.util.Properties;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;


/** Defines some system-dependent constants for experiments. */
public class ExperimentConfiguration {

	/** Directory where game files are stored. */
	public static final String RESULTS_DIRECTORY;

	/** Command to start Java Virtual Machine with Orego's classpath. */
	public static final String JAVA_WITH_OREGO_CLASSPATH;

	/**
	 * The host from which commands are given must be listed first for
	 * KillExperiment to work.
	 */
	public static final String[] HOSTS;
	
	static {
		Properties defaultProp = new Properties();
		try {
			defaultProp.load(new FileInputStream(OREGO_ROOT_DIRECTORY + separator + "config.properties"));
		} catch (FileNotFoundException e1) {
			System.err.println("config.properties not found.");
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		Properties userProp = new Properties(defaultProp);
		try {
			userProp.load(new FileInputStream(OREGO_ROOT_DIRECTORY + separator + "user.properties"));
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
			e.printStackTrace();
		}
		RESULTS_DIRECTORY = userProp.getProperty("resultsdirectory");
		JAVA_WITH_OREGO_CLASSPATH = userProp.getProperty("oregoclasspath");
		GNUGO = userProp.getProperty("gnugoclasspath") + " --boardsize " + getBoardWidth() + " --mode gtp --quiet --chinese-rules --capture-all-dead --positional-superko --komi 7.5";
		String s = userProp.getProperty("hosts");
		HOSTS = s.trim().split("\\s+");
	}
	
	/**
	 * Number of games to run simultaneously on each host. This should be no
	 * more than the number of processor cores on each host. If Orego is being
	 * run with multiple threads, it might be even smaller.
	 */
	public static final int GAMES_PER_HOST = 6;

	/** Total number of games desired per condition. */
	public static final int GAMES_PER_CONDITION = 480;

	/**
	 * Number of games to play with Orego as each color. The total number of
	 * games will be 2 * <# of hosts> * GAMES_PER_HOST * GAMES_PER_COLOR.
	 */
	public static final int GAMES_PER_COLOR = GAMES_PER_CONDITION
			/ (2 * HOSTS.length * GAMES_PER_HOST);

	/** The amount of time each player is allocated for each game. */
	public static final int GAME_TIME_IN_SECONDS = 500;
	
	static {
		assert 2 * HOSTS.length * GAMES_PER_HOST * GAMES_PER_COLOR == GAMES_PER_CONDITION : "Games per condition must be a multiple of 2 * <# of hosts> * <games per host>";
	}

	/**
	 * Command line arguments to Orego for the various conditions in the
	 * experiment.
	 */
	public static final String[] CONDITIONS = new String[9];

	static {
		CONDITIONS[0] = "threads=2 book=LateOpeningBook compare-rest compare-rest-conf=0.999";
		CONDITIONS[1] = "threads=2 book=LateOpeningBook compare-rest compare-rest-conf=0.9999";
		CONDITIONS[2] = "threads=2 book=LateOpeningBook compare-rest compare-rest-conf=0.99999";
		CONDITIONS[3] = "threads=2 book=LateOpeningBook compare-rest comare-rest-unconf=0.60";
		CONDITIONS[4] = "threads=2 book=LateOpeningBook compare-rest comare-rest-unconf=0.70";
		CONDITIONS[5] = "threads=2 book=LateOpeningBook compare-rest comare-rest-unconf=0.80";
		CONDITIONS[6] = "threads=2 book=LateOpeningBook compare-second comare-second-unconf=0.40";
		CONDITIONS[7] = "threads=2 book=LateOpeningBook compare-second comare-second-unconf=0.50";
		CONDITIONS[8] = "threads=2 book=LateOpeningBook compare-second comare-second-unconf=0.60";
	}
	
	/** Path to run gnugo on your machine */
	public static final String GNUGO;

}
