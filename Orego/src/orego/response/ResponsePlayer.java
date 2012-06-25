package orego.response;

import ec.util.MersenneTwisterFast;
import orego.mcts.McPlayer;
import orego.mcts.McRunnable;
import orego.util.IntSet;
import orego.core.Board;
import orego.core.Coordinates;

public class ResponsePlayer extends McPlayer {
	
	public static final int THRESHOLD = 100;
	public static final int TEST_THRESHOLD = 1;
	
	private ResponseList responseZero;
	private ResponseList[] responseOne;
	private ResponseList[][] responseTwo;
	private boolean testing = false;
	
	public ResponsePlayer(){
		super();
		int arrayLength = Coordinates.FIRST_POINT_BEYOND_BOARD;
		responseZero = new ResponseList();
		responseOne = new ResponseList[arrayLength];
		responseTwo = new ResponseList[arrayLength][arrayLength];
		for (int i = 0; i < arrayLength; i++) {
			responseOne[i] = new ResponseList();
			responseTwo[i] = new ResponseList[arrayLength];
			for (int j = 0; j < arrayLength; j++) {
				responseTwo[i][j] = new ResponseList();
			}
		}
	}
	
	/**
	 * toggle testing flag to change threshold value
	 * @param setting new setting for testing flag
	 */
	public void setTesting(boolean setting) {
		testing = setting;
	}

	public void generateMovesToFrontier(McRunnable runnable) {
		MersenneTwisterFast random = runnable.getRandom();
		runnable.getBoard().copyDataFrom(getBoard());
		Board board = runnable.getBoard();
		//play the first move in our list(s)
		if (board.getTurn() >= 2 && responseTwo[(board.getTurn()-2)][board.getTurn()-1].getTotalRuns() >= (testing ? TEST_THRESHOLD: THRESHOLD)) {
			int counter = 1;
			int move = responseTwo[(board.getTurn()-2)][board.getTurn()-1].getMoves()[0];
			while (! board.isLegal(responseTwo[(board.getTurn()-2)][board.getTurn()-1].getMoves()[move])) {
				move = responseTwo[(board.getTurn()-2)][board.getTurn()-1].getMoves()[counter];
				counter++;
			}
			board.play(move);
		}
		else if (board.getTurn() >= 1 && responseOne[board.getTurn()-1].getTotalRuns() >= (testing ? TEST_THRESHOLD: THRESHOLD)) {
			int counter = 1;
			int move = responseOne[board.getTurn()-1].getMoves()[0];
			while (! board.isLegal(responseOne[board.getTurn()-1].getMoves()[move])) {
				move = responseOne[board.getTurn()-1].getMoves()[counter];
				counter++;
			}
			board.play(move);
		}
		else {
			int counter = 1;
			int move = responseZero.getMoves()[0];
			while (! board.isLegal(responseZero.getMoves()[move])) {
				move = responseZero.getMoves()[counter];
				counter++;
			}
			board.play(move);
		}
		while (board.getPasses() < 2) {
			IntSet vacantPoints = runnable.getBoard().getVacantPoints();
			int move = random.nextInt(vacantPoints.size());
			if (board.isLegal(move) && board.isFeasible(move)){
				board.play(move);
			}
		}
	}
	
	public int getPlayouts(int p) {
		return responseZero.getTotalRuns();
	}

	public double getWinRate(int p) {
		return responseZero.getWinRate(p);
	}

	public int getWins(int p) {
		return responseZero.getWins(p);
	}

	public void incorporateRun(int winner, McRunnable runnable) {
		Board board = runnable.getBoard();
		if(winner==board.getColorToPlay()) {
			responseZero.addWin(board.getMove(board.getTurn()));
			responseOne[board.getMove(board.getTurn()-1)].addWin(board.getMove(board.getTurn()));
			responseTwo[board.getMove(board.getTurn()-2)][board.getMove(board.getTurn()-1)].addWin(board.getMove(board.getTurn()));
		}
		else {
			responseZero.addLoss(board.getMove(board.getTurn()));
			responseOne[board.getMove(board.getTurn()-1)].addLoss(board.getMove(board.getTurn()));
			responseTwo[board.getMove(board.getTurn()-2)][board.getMove(board.getTurn()-1)].addLoss(board.getMove(board.getTurn()));
		}
	}

	@Override
	protected String winRateReport() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void beforeStartingThreads() {
		// TODO Auto-generated method stub

	}

	@Override
	public int bestStoredMove() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void updateForAcceptMove(int p) {
		// TODO Auto-generated method stub

	}

	public ResponseList getResponseZero() {
		return responseZero;
	}

	public ResponseList[] getResponseOne() {
		return responseOne;
	}

	public ResponseList[][] getResponseTwo() {
		return responseTwo;
	}
}
