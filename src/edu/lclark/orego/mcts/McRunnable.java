package edu.lclark.orego.mcts;

import ec.util.MersenneTwisterFast;
import edu.lclark.orego.core.Board;
import edu.lclark.orego.core.Color;
import edu.lclark.orego.feature.*;
import static edu.lclark.orego.core.NonStoneColor.*;
import edu.lclark.orego.core.CoordinateSystem;
import edu.lclark.orego.core.Legality;
import static edu.lclark.orego.core.Legality.*;
import edu.lclark.orego.move.Mover;
import edu.lclark.orego.score.Scorer;

/**
 * Players use this class to perform multiple Monte Carlo runs in different
 * threads.
 */
public class McRunnable implements Runnable {

	/** The board on which this McRunnable plays its moves. */
	private Board board;

	/** @see #getFancyHashes() */
	private final long[] fancyHashes;
	
	/** Keeps track of moves played. */
	private HistoryObserver historyObserver;

	/** The Player that launches the thread wrapped around this McRunnable. */
	private Player player;

	/** Number of playouts completed. */
	private long playoutsCompleted;

	/** Generates moves beyond the tree. */
	private Mover mover;

	private CoordinateSystem coords;

	/** Determines winners of playouts. */
	private Scorer scorer;

	/** Counts stones for fast mercy cutoffs of playouts. */
	private StoneCounter mercyObserver;

	// TODO If this is seeded with the time, do we have to worry about different
	// McRunnables having identical generators?
	/** Random number generator. */
	private final MersenneTwisterFast random;

	public McRunnable(Player player, CopiableStructure stuff) {
		CopiableStructure copy = stuff.copy();
		board = copy.get(Board.class);
		coords = board.getCoordinateSystem();
		this.player = player;
		random = new MersenneTwisterFast();
		mover = copy.get(Mover.class);
		scorer = copy.get(Scorer.class);
		mercyObserver = copy.get(StoneCounter.class);
		historyObserver = copy.get(HistoryObserver.class);
		fancyHashes = new long[coords.getMaxMovesPerGame() + 1];
	}

	/**
	 * Accepts (plays on on this McRunnable's own board) the given move.
	 * 
	 * @see edu.lclark.orego.core.Board#play(int)
	 */
	public void acceptMove(short p) {
		Legality legality = board.play(p);
		assert legality == OK : "Legality " + legality + " for move "
				+ coords.toString(p) + "\n" + board;
		fancyHashes[board.getTurn()] = board.getFancyHash();
	}

	/** Copies data from that (the player's real board) to the local board. */
	public void copyDataFrom(Board that) {
		board.copyDataFrom(that);
	}

	/** Returns the board associated with this runnable. */
	public Board getBoard() {
		return board;
	}

	/** @return the player associated with this runnable */
	public Player getPlayer() {
		return player;
	}

	/** Returns the number of playouts completed by this runnable. */
	public long getPlayoutsCompleted() {
		return playoutsCompleted;
	}

	/** Returns the random number generator associated with this runnable. */
	public MersenneTwisterFast getRandom() {
		return random;
	}

	/** Returns the current turn number on this runnable's board. */
	public int getTurn() {
		return board.getTurn();
	}

	/**
	 * Performs a single Monte Carlo run and incorporates it into player's
	 * search tree. The player should generate moves to the frontier of the
	 * known tree and then return. The McRunnable performs the actual playout
	 * beyond the tree, then calls incorporateRun on the player.
	 * 
	 * @return The winning color, although this is only used in tests.
	 */
	public Color performMcRun() {
		copyDataFrom(player.getBoard());
		player.generateMovesToFrontier(this);
		Color winner;
		if (board.getPasses() == 2) {
			winner = scorer.winner();
		} else {
			winner = playout();
		}
		player.incorporateRun(winner, this);
		playoutsCompleted++;
		return winner;
	}

	/**
	 * Plays moves to the end of the game and returns the winner: BLACK, WHITE,
	 * or (in rare event where the playout is canceled because it hits the
	 * maximum number of moves) VACANT.
	 */
	public Color playout() {
		do {
			if (board.getTurn() >= coords.getMaxMovesPerGame()) {
				// Playout ran out of moves, probably due to superko
				return VACANT;
			}
			if (board.getPasses() < 2) {
				selectAndPlayOneMove();
			}
			if (board.getPasses() >= 2) {
				// Game ended
				return scorer.winner();
			}
			Color mercyWinner = mercyObserver.mercyWinner();
			if (mercyWinner != null) {
				// One player has far more stones on the board
				return mercyWinner;
			}
		} while (true);
	}

	/**
	 * Performs runs and incorporate them into player's search tree until this
	 * thread is interrupted.
	 */
	@Override
	public void run() {
		// TODO Allow for limiting by playouts instead of time
		while (getPlayer().shouldKeepRunning()) {
			performMcRun();
		}
	}

	public short selectAndPlayOneMove() {
		return mover.selectAndPlayOneMove(random);
	}

	public HistoryObserver getHistoryObserver() {
		return historyObserver;
	}

	/**
	 * Returns the sequence of fancy hashes for search nodes visited during this
	 * run. Only the elements between the real board's turn (inclusive) and this
	 * McRunnable's turn (exclusive) are valid.
	 */
	public long[] getFancyHashes() {
		return fancyHashes;
	}

}
