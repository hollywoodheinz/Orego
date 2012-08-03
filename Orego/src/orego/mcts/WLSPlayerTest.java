package orego.mcts;

import static orego.core.Colors.BLACK;
import static orego.core.Colors.WHITE;
import static orego.core.Coordinates.BOARD_WIDTH;
import static orego.core.Coordinates.NO_POINT;
import static orego.core.Coordinates.PASS;
import static orego.core.Coordinates.at;
import static orego.mcts.MctsPlayerTest.TABLE_SIZE;
import static org.junit.Assert.*;

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
		int[] topBlackResponses1 = player.getBestReplies()[BLACK][at("a1")][at("b1")].getTopResponses();
		int[] topBlackResponses2 = player.getBestReplies()[BLACK][at("a2")][at("b1")].getTopResponses();
		
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
	
	@Test
	public void testLifeOrDeath() {
		if (BOARD_WIDTH == 19) {
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
				player.bestMove();
				
				assertEquals(at("k19"), player.getBestReplies()[BLACK][at("a10")][at("j19")].getTopResponses()[0]);
				assertEquals(at("j19"), player.getBestReplies()[BLACK][at("a10")][at("k19")].getTopResponses()[0]);
		}
	}

}

