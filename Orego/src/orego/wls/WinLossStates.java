package orego.wls;

/**
 LICENSE:
 ========

 Copyright (c) 2011 Jacques Basald�a.
 All rights reserved.

 Redistribution and use in source and binary forms are permitted
 provided that the above copyright notice and this paragraph are
 duplicated in all such forms and that any documentation,
 advertising materials, and other materials related to such
 distribution and use acknowledge that the software was developed
 by the <organization>.  The name of the author may not be used to
 endorse or promote products derived from this software without
 specific prior written permission.

 THIS SOFTWARE IS PROVIDED ``AS IS'' AND WITHOUT ANY EXPRESS OR
 IMPLIED WARRANTIES, INCLUDING, WITHOUT LIMITATION, THE IMPLIED
 WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE.

 */

import java.util.Arrays;

public class WinLossStates {


	public static double CONFIDENCE_LEVEL = .95; // confidence level for interval estimation
	
	public static int END_SCALE = 21;
	
	// compute the total number of states which is sum(1...[e+1]).
	// we write it in closed form
	public static int NUM_STATES = Integer.MIN_VALUE;
	
	public static double WIN_THRESHOLD = 0.5000;
	
	// constant used when jumping. Tuned empirically for END_SCALE = 21
	// Can we do this automatically in the future?
	public static double JUMP_CONSTANT_K = 1.3; 
	
	public int oneOfTwo; // Binary value of 1/2 (0/1 if endscale < 2)
	
	public int[] WIN;

	public int[] LOSS;

	private State[] states;
	
	// TODO: configuration of properties such as END_SCALE, WIN_THRESHOLD, etc
	public WinLossStates(double confidence, int end_scale) {
		CONFIDENCE_LEVEL = confidence;
		END_SCALE = end_scale;
		
		computeNumberOfStates();
		
		oneOfTwo = 0;


		WIN  = new int[NUM_STATES];
		LOSS = new int[NUM_STATES];
		states = new State[NUM_STATES];
		
		for (int i = 0; i < NUM_STATES; i++) {
			states[i] = new State(0, 0);
		}
		
		buildTables();
	}
	
	public WinLossStates() {
		this(.95, 21);
	}
	private void computeNumberOfStates() {
		NUM_STATES = (END_SCALE * (END_SCALE + 1)) / 2;
	}
	
	private void buildTables() {
		
		int curState = 0;
		// Initializing states
		// loop through all the different number of "denominators" or 
		// number of runs.
		for (int i = 0; i <= END_SCALE; i++) {
			
			// For each "level" in the tree (see figure 5 in the paper)
			// create a series of fractions with the number of wins increasing
			for (int j = 0; j <= i; j++) {
				State state = states[curState];
				
				state.setWins(j);
				state.setRuns(i);
				
				if (i == 0) {
					// undefined for first state (lowest)
					state.setConfidence(Double.MIN_VALUE);
					continue;
				}
				
				// compute the statistically "strength" of this proportion
				state.computeConfidence(WIN_THRESHOLD);
				
				curState++;
			}
		}
		
		Arrays.sort(states); // sort according to confidence
		
		
		// build the "directed graph" of win/loss state transitions
		// we start at 0 and chain together the appropriate states
		for (int stateActionIndex = 0; stateActionIndex < NUM_STATES; stateActionIndex++) {
			
			// where do we go after a win?
			WIN[stateActionIndex] = findStateIndex(states[stateActionIndex].getWins() + 1, 
								   				   states[stateActionIndex].getRuns() + 1,
								   				   true);
			
			// where do we go after a loss?
			LOSS[stateActionIndex] = findStateIndex(states[stateActionIndex].getWins(),
													states[stateActionIndex].getRuns() + 1,
													false);
					
		}
		
	}

	private int findStateIndex(int wins, int runs, boolean didWin) {
		for (int i = NUM_STATES - 1; i >= 0; i--) {
			if (states[i].getWins() == wins && 
				states[i].getRuns() == runs   )
				return i;
		}
		
		// doesn't exist in the table? the value is saturated, we're going to need to jump
		// WIN[i] = SaturatedIdx(states[i].wins, states[i].runs, 1);
		return saturatedJumpIndex(wins, didWin);
	}

	/**
	 * Returns the index to jump to when we have a "saturated" proportion: n/m where m = e (end of scale).
	 * This function should not be called *unless* m = e (the proportion is saturated).
	 * When a proportion is saturated the number of runs have surpassed the END_SCALE and hence have grown
	 * beyond our encoding. We also begin to lose useful information because we are literally "off the charts".
	 * Hence, we perform a small re-adjustment which allows the proportion to jump back and "churn" back up to the current state.
	 * This guarantees that any truly saturated states are extremely "rich" with information and hence become stationary.
	 * We continue to jump backwards in history until the win or loss hits the ceiling or floor. Once this happens,
	 * we have as much information as we'll ever use as both the number of runs *and* the number of wins/losses have maxed out the scale.
	 * @param wins The number of wins
	 * @param runs The number of runs 
	 * @param didWin Did we win in the last run?
	 * @return int The index to which we will jump to force a sort of "confirmation"
	 */
	private int saturatedJumpIndex(int wins, boolean didWin) {

		// number of runs we are going to jump to.
		// In effect we are changing the denominator in proportion to deviation from 1/2.
		// This performs a "jump" to an earlier state and has the side of increasingly our side effects.
		// See the original paper for the jump formula
		
		// TODO: we might have to change 1/2 for binary values
		int jumpRuns = (int)(END_SCALE - Math.round(JUMP_CONSTANT_K * END_SCALE * Math.abs(wins / END_SCALE - 1/2)));

		// need to build a temporary state for our current state
		State curState = states[findStateIndex(wins, END_SCALE, didWin)];
		
		if (didWin) {
			
			// pick the smallest proportion (with appropriate run count "jumpRuns").
			// The proportion's confidence should be better than our current proportion.
			// We effectively punish by reducing the number of wins and runs but we increase our confidence.
			
			int smallestProportionStateIndex = 0; // we are guaranteed to find a better state
			for (int i = 0; i < NUM_STATES - 1; i++) {
				
				if (curState.getRuns() == jumpRuns) { // break early if no match!
					
					if (curState.getWinRunsProportion() > states[smallestProportionStateIndex].getWinRunsProportion() &&
						states[i].getConfidence() > curState.getConfidence()) {
						
						smallestProportionStateIndex = i;
					}
				}
				
				return smallestProportionStateIndex;
			}
		} else {
			if (wins == 0) return findStateIndex(0, END_SCALE, didWin); // fully saturated (lost as many times as possible)
			else {
				// pick the largest proportion (with appropriate run count)
				// with a "confidence" smaller than our current proportion.
				// We are effectively punishing by reducing the lose count and
				// reducing our confidence level
				
				int biggestProportionStateIndex = 0; // we are guaranteed to find a better state
				for (int i = 0; i < NUM_STATES - 1; i++) {
					
					if (curState.getRuns() == jumpRuns) { // break early if no match!
						
						if (curState.getWinRunsProportion() > states[biggestProportionStateIndex].getWinRunsProportion() &&
							states[i].getConfidence() < curState.getConfidence()											) {
							
							biggestProportionStateIndex = i;
						}						
					}
				}
				
				return biggestProportionStateIndex;
			}
			
		}
		

		return Integer.MIN_VALUE; 
	};
};
