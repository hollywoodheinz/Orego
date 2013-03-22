package orego.mcts;

import static orego.core.Colors.BLACK;
import static orego.core.Coordinates.PASS;
import static orego.core.Coordinates.at;
import static orego.mcts.MctsPlayerTest.TABLE_SIZE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

public class WLSPlayerTest {

	private WLSPlayer player;

	private McRunnable runnable;

	@Before
	public void setUp() throws Exception {
		player = new WLSPlayer();
		player.setProperty("pool", "" + TABLE_SIZE);
		player.setProperty("threads", "1");
		player.setProperty("heuristics", "Pattern:Capture");
		player.setPlayoutLimit(1000);
		player.reset();
		runnable = (McRunnable) (player.getRunnable(0));
	}

	/**
	 * Incorporates the indicates moves as if they had been generated by a real
	 * playout. Two passes are added to the end.
	 */
	protected void fakeRun(int winner, String... labels) {
		int[] moves = new int[labels.length + 2];
		int i;
		for (i = 0; i < labels.length; i++) {
			moves[i] = at(labels[i]);
		}
		moves[i] = PASS;
		moves[i + 1] = PASS;
		player.fakeGenerateMovesToFrontierOfTree(runnable, moves);
		runnable.copyDataFrom(player.getBoard());
		for (int p : moves) {
			runnable.acceptMove(p);
		}
		player.incorporateRun(winner, runnable);
	}

	@Test
	public void testIncorporateRun2() {
		fakeRun(BLACK, "a1", "b1", "b2", "c2", "c1", "d1", "a2", "b1", "b3");
		int[] topBlackResponses1 = player.getBestReplies().get(WLSPlayer.levelTwoEncodedIndex(at("a1"), at("b1"), BLACK)).getTopResponses();
		int[] topBlackResponses2 = player.getBestReplies().get(WLSPlayer.levelTwoEncodedIndex(at("a2"), at("b1"), BLACK)).getTopResponses();
		
		assertTrue(topBlackResponses1.length > 0);
		assertTrue(topBlackResponses2.length > 0);
		
		assertEquals(at("b2"), topBlackResponses1[0]);
		assertEquals(at("b3"), topBlackResponses2[0]);
	}


	@Test
	public void testPreviousMoves() {
		fakeRun(BLACK, "a1", "b1", "b2", "c2", "c1", "d1", "a2", "b1", "b3");
		player.acceptMove(at("a1"));
		player.acceptMove(at("b1"));
		runnable.copyDataFrom(player.getBoard());
		runnable.playout();
		assertEquals(at("b2"), runnable.getMove(2));
	}
	
	// TODO This test seems to fail sometimes
	@Test
	public void testLifeOrDeath()  throws Exception{
			player.reset();
			String[] diagram = { 
					"#######....########",// 19
					"###################",// 18
					"###################",// 17
					"###################",// 16
					"###################",// 15
					"###################",// 14
					"###################",// 13
					"###################",// 12
					"###################",// 11
					".##################",// 10
					"OOOOOOOOOOOOOOOOOOO",// 9
					"OOOOOOOOOOOOOOOOOOO",// 8
					"OOOOOOOOOOOOOOOOOOO",// 7
					"OOOOOOOOOOOOOOOOOOO",// 6
					"OOOOOOOOOOOOOOOOOOO",// 5
					"OOOOOOOOOOOOOOOOOOO",// 4
					"OOOOOOOOOOOOOOOOOOO",// 3
					"OOOOOOOOOOOOOOOOOOO",// 2
					".OOOOOOOOOOOOOOOOO." // 1
				  // ABCDEFGHJKLMNOPQRST
			};
			player.setUpProblem(BLACK, diagram);
			player.getBoard().play(at("a10"));
			player.setProperty("msec", "5000");
			player.bestMove();
			
			assertEquals(at("k19"), player.getBestReplies().get(WLSPlayer.levelTwoEncodedIndex(at("a10"), at("j19"), BLACK)).getTopResponses()[0]);
			assertEquals(at("j19"), player.getBestReplies().get(WLSPlayer.levelTwoEncodedIndex(at("a10"), at("k19"), BLACK)).getTopResponses()[0]);
	}

	@Test
	public void testSetMaxIllegalityCap() throws Exception {
		assertEquals(5, WLSPlayer.MAX_ILLEGALITY_CAP);
		
		player.setProperty("maxIllegalityThreshold", "6");
		
		assertEquals(6, WLSPlayer.MAX_ILLEGALITY_CAP);
	}
	
	@Test
	public void testSetMinWLSThreshold() throws Exception {
		assertEquals(.55, WLSPlayer.MIN_WLS_THRESHOLD, .000001);
		
		player.setProperty("minWlsThreshold", ".634");
		
		assertEquals(.634, WLSPlayer.MIN_WLS_THRESHOLD, .000001);
	}
	
	@Test
	public void testResizeTopResponseList() throws Exception {
		// pick a random response list
		WLSResponseMoveList topMoves = player.getBestReplies().get(WLSPlayer.levelTwoEncodedIndex(at("e11"), at("b4"), BLACK));
		
		assertEquals(8, topMoves.getTopResponses().length);
		assertEquals(8, topMoves.getTopResponsesLength());
		
		// another random move sequence
		topMoves = player.getBestReplies().get(WLSPlayer.levelTwoEncodedIndex(at("h17"), at("d9"), BLACK));
		
		assertEquals(8, topMoves.getTopResponses().length);
		assertEquals(8, topMoves.getTopResponsesLength());
		
		// now resize the length of top responses
		player.setProperty("topResultsLength", "12");
		
		assertEquals(12, WLSPlayer.TOP_RESPONSES_CAP);
		assertEquals(12, topMoves.getTopResponsesLength());
		
		// pick a random response list
		topMoves = player.getBestReplies().get(WLSPlayer.levelTwoEncodedIndex(at("e11"), at("b4"), BLACK));
		
		assertEquals(12, topMoves.getTopResponses().length);
		assertEquals(12, topMoves.getTopResponsesLength());
		
		// another random move sequence
		topMoves = player.getBestReplies().get(WLSPlayer.levelTwoEncodedIndex(at("h17"), at("d9"), BLACK));
		
		assertEquals(12, topMoves.getTopResponses().length);
		assertEquals(12, topMoves.getTopResponsesLength());
		
		// another random move sequence
		topMoves = player.getBestReplies().get(WLSPlayer.levelTwoEncodedIndex(at("h6"), at("d5"), BLACK));
				
		assertEquals(12, topMoves.getTopResponses().length);
		assertEquals(12, topMoves.getTopResponsesLength());
		
	}
}

