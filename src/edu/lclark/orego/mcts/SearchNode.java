package edu.lclark.orego.mcts;

import edu.lclark.orego.core.CoordinateSystem;
import edu.lclark.orego.util.ListNode;
import edu.lclark.orego.util.ShortSet;

/** A node in the search "tree". */
public interface SearchNode {

	/**
	 * Returns a human-readable String giving statistics on the move with the
	 * most wins.
	 */
	public String bestWinCountReport(CoordinateSystem coords);

	/** Returns the win rate of the best move. */
	public float bestWinRate(CoordinateSystem coords);

	/**
	 * Marks move p (e.g., an illegal move) as being horrible, so it will never
	 * be tried again.
	 */
	public void exclude(short p);

	/** Mark this node as unused until the next time it is reset. */
	public void free();

	/** Returns the (beginning of the linked list of) children of this node. */
	public ListNode<SearchNode> getChildren();

	/**
	 * Returns the fancy Zobrist hash of the board situation stored in this
	 * node.
	 */
	public long getFancyHash();

	/** Returns the move with the most wins from this node. */
	public short getMoveWithMostWins(CoordinateSystem coords);

	/** Returns the number of runs through move p. */
	public int getRuns(short p);

	/** Returns the total number of runs through this node. */
	public int getTotalRuns();

	/**
	 * Returns the last move played from this node if it resulted in a win,
	 * otherwise NO_POINT.
	 */
	public short getWinningMove();

	/** Returns the win rate through this node for move p. */
	public float getWinRate(short p);

	/** Returns the number of wins through move p. */
	public float getWins(short p);

	/**
	 * Returns true for those moves through which another node has already been
	 * created.
	 */
	public boolean hasChild(short p);

	/**
	 * Returns true if this node has not yet experienced any playouts (other
	 * than initial bias playouts).
	 */
	public boolean isFresh(CoordinateSystem coords);

	/** Returns true if this node is in use (i.e., has been reset since the last time it was freed). */
	public boolean isInUse();

	/**
	 * True if this node is marked. Used in garbage collection.
	 */
	public boolean isMarked();

	/**
	 * Returns the total ratio of wins to runs for moves from this node. This is
	 * slow.
	 */
	public float overallWinRate(CoordinateSystem coords);

	/**
	 * Increments the counts for a move sequence resulting from a playout.
	 * 
	 * NOTE: Since this method is not synchronized, two simultaneous calls on
	 * the same node might result in a race condition affecting which one sets
	 * the winningMove field.
	 * 
	 * @param winProportion
	 *            1.0 if this is a winning playout for the player to play at
	 *            this node, 0.0 otherwise.
	 * @param moves
	 *            Sequence of moves made in this playout, including two final
	 *            passes.
	 * @param t
	 *            Index of the first move (the one made from this node).
	 * @param turn
	 *            Index right after the last move played.
	 * @param playedPoints
	 *            For keeping track of points played to avoid counting
	 *            already-played points.
	 */
	public void recordPlayout(float winProportion, short[] moves,
			int t, int turn, ShortSet playedPoints);

	/**
	 * Resets this node as a "new" node for the board situation represented by
	 * boardHash.
	 */
	public void reset(long fancyHash, CoordinateSystem coords);

	/** Sets the child list for this node. */
	public void setChildren(ListNode<SearchNode> children);

	/** Marks move p as visited. */
	public void setHasChild(short p);

	/** Sets the mark of this node for garbage collection. */
	public void setMarked(boolean marked);

	/** Returns a human-readable representation of this node. */
	public String toString(CoordinateSystem coords);

	/**
	 * Returns a human-readable representation of the information stored for
	 * move p.
	 */
	public String toString(short p, CoordinateSystem coords);

	/**
	 * Update the win rate for p, by adding the specified number of wins and n
	 * runs. Also updates the counts of total runs and runs.
	 */
	public void update(short p, int n, float wins);

}