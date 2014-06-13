package edu.lclark.orego.mcts;

import edu.lclark.orego.core.*;
import static edu.lclark.orego.core.StoneColor.*;
import static edu.lclark.orego.core.NonStoneColor.*;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

public class McRunnableTest {

	private Player player;
	
	private McRunnable runnable;
	
	@Before
	public void setUp() throws Exception {
		player = new StubPlayer();
		player.reset();
		runnable = player.getMcRunnable(0);
	}

	@Test
	public void testPlayoutAfterTwoPasses() {
		runnable.getBoard().pass();
		runnable.getBoard().pass();
		Color winner = runnable.playout();
		assertEquals(2, runnable.getTurn());
		assertEquals(WHITE, winner);
	}

//	@Test
//	public void testPlayoutMaxMoves() {
//		for (int i = 0; i < MAX_MOVES_PER_GAME - 1; i++) {
//			runnable.acceptMove(PASS);
//			runnable.getBoard().setPasses(0);
//		}
//		// Playing out from here should run up against the max game length
//		assertEquals(VACANT, runnable.playout());
//	}
//	
//	@Test
//	public void testDebug1() {
//			String[] problem = {
//					"#########OOOOOOOOOO",//19
//					"#########OOOOOOOOOO",//18
//					"#########OOOOOOOOOO",//17
//					"#########OOOOOOOOOO",//16
//					"#########OOOOOOOOOO",//15
//					"##########OOOOOOOOO",//14
//					"##########OOOOOOOOO",//13
//					"##########OOOOOOOOO",//12
//					"##########OOOOOOOOO",//11
//					"##########OOOOOOOOO",//10
//					"..##.#..###########",//9
//					"..#O.#.#.##########",//8
//					"..#O##.##OOOOOOOOOO",//7
//					"...#.##O########OOO",//6
//					"..###OOOOOOOOO#####",//5
//					"###OOOOOOOOO#######",//4
//					"OOOO.O#############",//3
//					"#OOOO##OOOOOOOOO###",//2
//					".O###.#O.##########" //1
//			       //ABCDEFGHJKLMNOPQRST
//			};
//			player.getBoard().setUpProblem(BLACK, problem);
//			player.acceptMove(PASS);
//			for (int i = 0; i < 10000; i++) {
//				runnable.performMcRun();
//			}
//			assertEquals(10000, runnable.getPlayoutsCompleted());
//	}
//
//	@Test
//	public void testUpdatePriors() {
//		player.setHeuristics(new HeuristicList("Pattern@5"));
//		player.reset();
//		runnable = (McRunnable)(player.getRunnable(0));
//		assertTrue(runnable.getHeuristics().get(0) instanceof PatternHeuristic);
//		String[] problem = { 
//				"...................",// 19
//				"...................",// 18
//				"...................",// 17
//				"...................",// 16
//				"..#.#..............",// 15
//				"...................",// 14
//				"...................",// 13
//				"...................",// 12
//				"...................",// 11
//				"...................",// 10
//				"...................",// 9
//				"...................",// 8
//				"...................",// 7
//				"...................",// 6
//				"...................",// 5
//				"...................",// 4
//				"...................",// 3
//				"...................",// 2
//				"..................."// 1
//		// 		 ABCDEFGHJKLMNOPQRST
//		};
//		Board board = runnable.getBoard();
//		board.setUpProblem(WHITE, problem);
//		board.play("d14");
//		SearchNode node = new SearchNode();
//		node.reset(board.getHash());
//		runnable.updatePriors(node, board);
//		assertEquals(6, node.getWins(at("d15")), 0.001);
//		assertEquals(7, node.getRuns(at("d15")));
//	}

}