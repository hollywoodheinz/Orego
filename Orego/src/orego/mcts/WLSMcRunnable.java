package orego.mcts;

import static orego.core.Board.PLAY_OK;
import static orego.core.Colors.VACANT;
import orego.core.Board;
import orego.core.Coordinates;
import orego.heuristic.HeuristicList;
import ec.util.MersenneTwisterFast;

public class WLSMcRunnable extends McRunnable {
	/** A *reference* to the WLSPlayer's copy of the level two response table*/
	private WLSResponseMoveList[][][] bestReplies;
	
	
	public WLSMcRunnable(McPlayer player, HeuristicList heuristics, WLSResponseMoveList[][][] bestReplies) {
		super(player, heuristics);
		
		this.bestReplies = bestReplies;
	}
	
	@Override
	public int selectAndPlayOneMove(MersenneTwisterFast random, Board board) {
		int antepenultimate = board.getMove(board.getTurn() - 2);
		int previous 	    = board.getMove(board.getTurn() - 1);
		WLSResponseMoveList list = bestReplies[board.getColorToPlay()][antepenultimate][previous];
		
		if (list != null) {
			// work through our best moves (from best to worst) and test legality
			for (int responseMove : list.getTopResponses()) {
				// Try a level 2 reply
				if (responseMove != Coordinates.NO_POINT &&
					(board.getColor(responseMove) == VACANT) && 
				     board.isFeasible(responseMove) 		 && 
				     (board.playFast(responseMove) == PLAY_OK)) {
					return responseMove;
				}
			}
		}
		
		// No good replies stored; proceed normally
		return super.selectAndPlayOneMove(random, board);
	}
}
