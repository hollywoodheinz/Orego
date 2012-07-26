package orego.heuristic;

import orego.core.Board;

/** Adjusts the probability of playing a move using domain-specific knowledge. */
public abstract class Heuristic {

	/**
	 * The amount of weight given to the heuristic
	 */
	private double weight;
	
	public double getWeight() {
		return weight;
	}

	public void setWeight(double weight) {
		this.weight = weight;
	}

	/**
	 * Returns a positive value if p is a good move for the current player on
	 * board, a negative value if it's bad.
	 */
	public abstract int evaluate(int p, Board board);
	
	/** Allows external clients to optimize parameters. Subclasses
	 * should override if they have additional 'tunable' parameters.
	 * @param property The name of the property
	 * @param value The value of the property
	 */
	public void setProperty(String property, String value) {
		if (property.equals("weight")) {
			this.weight = Double.valueOf(weight);
		}
	}

}