package orego.mcts;

import static orego.core.Coordinates.*;
import static orego.core.Colors.*;
import static org.junit.Assert.*;
import static orego.core.Board.*;
import java.util.StringTokenizer;
import org.junit.Before;
import org.junit.Test;
import ec.util.MersenneTwisterFast;
import orego.core.*;
import orego.heuristic.HeuristicList;
import orego.play.UnknownPropertyException;

public class MctsPlayerTest {

	/**
	 * We would want more than this in a real game, but allocating more for
	 * tests makes the system thrash.
	 */
	public static final int TABLE_SIZE = 1024;

	protected MctsPlayer player;

	protected Board board;

	@Before
	public void setUp() throws Exception {
		player = new MctsPlayer();
		player.setProperty("pool", "" + TABLE_SIZE);
		player.setProperty("threads", "1");
		player.setPlayoutLimit(1000);
		player.reset();
		board = new Board();
	}

	/** General method for indexing into 
	 * the board. Subclasses should override
	 * for a different hashing method if the subclass
	 * uses a different TranspositionTable.
	 * @param board
	 * @return the hashcode for the table
	 */
	public long indexOfBoard(Board board) {
		return board.getHash();
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
		McRunnable runnable = new McRunnable(player, new HeuristicList());
		player.fakeGenerateMovesToFrontierOfTree(runnable, moves);
		runnable.copyDataFrom(player.getBoard());
		for (int p : moves) {
			runnable.acceptMove(p);
		}
		player.incorporateRun(winner, runnable);
	}

	@Test
	public void testBestCleanupMove() {
		String[] problem1 = new String[] {
				"...................",// 19
				"...................",// 18
				"...................",// 17
				"...................",// 16
				"...................",// 15
				"...................",// 14
				"...................",// 13
				"...................",// 12
				"...................",// 11
				"...................",// 10
				"...................",// 9
				"...................",// 8
				"...................",// 7
				"...................",// 6
				"...................",// 5
				"...................",// 4
				".##................",// 3
				"OO#................",// 2
				".O#................" // 1
			  // ABCDEFGHJKLMNOPQRST
		};
		int successes1 = 0;
		for (int i = 0; i < 10; i++) {
			player.setPlayoutLimit(1000);
			player.reset();
			player.setUpProblem(BLACK, problem1);
			player.bestMove();
			int move1 = player.bestCleanupMove();
			System.err.print(pointToString(move1) + " ");
			assertTrue(move1 != PASS);
			if (move1 == at("a3")) {
				successes1++;
			}
		}
		System.err.println();
		assertTrue(successes1 >= 5);

		// another test
		String[] problem2 = new String[] {
				".........#O........",// 19
				".........#O........",// 18
				".........#O........",// 17
				".........#O........",// 16
				"#######..#O........",// 15
				"#.....#..#O........",// 14
				"#.....#..#O........",// 13
				"#..O..#..#O........",// 12
				"#.....#..#O........",// 11
				"#.....#..#O........",// 10
				"#######..#O........",// 9
				".........#O........",// 8
				".........#O........",// 7
				".........#O........",// 6
				".........#O........",// 5
				".........#O........",// 4
				".........#O........",// 3
				".........#O........",// 2
				".........#O........" // 1
			  // ABCDEFGHJKLMNOPQRST
		};
		int successes2 = 0;
		for (int i = 0; i < 10; i++) {
			player.reset();
			player.setUpProblem(BLACK, problem2);
			player.bestMove();
			int move2 = player.bestCleanupMove();
			System.err.print(pointToString(move2) + " ");
			assertTrue(move2 != PASS);
			if (move2 == at("C12") || move2 == at("D11") || move2 == at("D13") || move2 == at("E12")) {
				successes2++;
			}
		}
		System.err.println();
		assertTrue(successes2 >= 5);
	}
	
	@Test
	public void testCoupDeGrace() throws UnknownPropertyException {
		player.setProperty("grace", "true");
		assertTrue(player.isGrace());
		String[] problem = new String[] {
				"..O.O.#..#O#######.",// 19
				".OO.O#####O#######.",// 18
				"O.O.O#.#.#O########",// 17
				".OOOO#####O##.###.#",// 16
				"OOOOOOOO##O#######.",// 15
				".O.....OOOO####....",// 14
				".OOOOOOO..O#####.#.",// 13
				"OO.O..O.OOO##.#.#..",// 12
				".OOO.O..O.O#.#.#.#.",// 11
				"..O.OOOOOOO#..##...",// 10
				".OOOO.O..#O#####.#.",// 9
				"...O.OO######.#.#..",// 8
				"OOOO.O.#OOO##....#.",// 7
				"..O.OO##O########..",// 6
				"..O.O.#OO#OOOO##.#.",// 5
				"..O.O.#O.#O.O.O#.#.",// 4
				".OO.O.#O##OOOOO##..",// 3
				"O.O.O.#OOO##O######",// 2
				"..O.O.##O.#.######." // 1
		      // ABCDEFGHJKLMNOPQRST
		};
		int successes = 0;
		int failures = 0;
		for (int i = 0; i < 10; i++) {
			player.reset();
			player.setUpProblem(BLACK, problem);
			player.bestMove();
			assertTrue(player.isCoupDeGraceActive());
			int move = player.bestMove();
			if (move == at("J4")) {
				successes++;
			} else if (move == at("K1")) {
				failures ++;
			}
		}
		assertEquals(0,failures );
		assertTrue(successes >= 5);
	}

	@Test
	public void testIncorporateRun() {
		SearchNode root = player.getRoot();
		board.play(at("a1"));
		fakeRun(BLACK, "a1", "a2", "a3", "a4");
		fakeRun(WHITE, "a1", "a2", "a3", "a4");
		fakeRun(BLACK, "a1", "a2", "a3", "a4");
		SearchNode child = player.getTable().findIfPresent(indexOfBoard(board));
		assertEquals(3.0 / 5, root.getWinRate(at("a1")), 0.01);
		assertEquals(2.0 / 4, child.getWinRate(at("a2")), 0.01);
	}

	@Test
	public void testIncorporateRun2() {
		player.setColorToPlay(WHITE);
		fakeRun(WHITE, "a1", "a2", "a3", "a4");
		fakeRun(BLACK, "a1", "a2", "a3", "a4");
		board.copyDataFrom(player.getBoard());
		board.play(at("a1"));
		SearchNode child = player.getTable().findIfPresent(indexOfBoard(board));
		assertEquals(2.0 / 4, player.getWinRate(at("a1")), 0.01);
		assertEquals(2.0 / 3, child.getWinRate(at("a2")), 0.01);
	}

	@Test
	public void testTreeGrowth() {
		for (int i = 0; i < 5; i++) {
			// player.fakeGenerateMovesToFrontierOfTree(runnable, at("a1"),
			// at("b1"), at("c1"), at("d1"), at("e1"));
			fakeRun(BLACK, "a1", "b1", "c1", "d1", "e1");
		}
		assertEquals(5, player.dagSize());
		fakeRun(BLACK, "a1", "b1", "c1", "d1", "f1");
		assertEquals(5, player.dagSize());
	}

	@Test
	public void testOnlyMovesAfterNodeCounted() {
		board.play(at("a1"));
		fakeRun(BLACK, "a1", "a2", "a3", "a4");
		fakeRun(BLACK, "a1", "a2", "a3", "a4");
		board.play(at("a2"));
		fakeRun(BLACK, "a1", "a2", "a3", "a4");
		assertEquals(5, player.getPlayouts(at("a1")));
		SearchNode grandChild = player.getTable().findIfPresent(
				indexOfBoard(board));
		assertNotNull(grandChild);
		assertEquals(1.0 / 2, grandChild.getWinRate(at("a1")), 0.01);
		assertEquals(1.0 / 2, grandChild.getWinRate(at("a2")), 0.01);
		assertEquals(2.0 / 3, grandChild.getWinRate(at("a3")), 0.01);
		assertEquals(
				"Hash: 0 Total runs: 735\nA1:       4/      5 (0.8000)\n  Hash: -8482843705321423540 Total runs: 734\n  A2:       1/      4 (0.2500)\n",
				player.toString(1));
		assertEquals(
				"Hash: 0 Total runs: 735\nA1:       4/      5 (0.8000)\n  Hash: -8482843705321423540 Total runs: 734\n  A2:       1/      4 (0.2500)\n    Hash: 7612148777347051953 Total runs: 733\n    A3:       2/      3 (0.6667)\n",
				player.toString(2));
		board.clear();
		board.play(PASS);
		fakeRun(BLACK); // Just passes
		assertEquals(
				"Hash: 0 Total runs: 736\nA1:       4/      5 (0.8000)\n  Hash: -8482843705321423540 Total runs: 734\n  A2:       1/      4 (0.2500)\nPASS:       2/     11 (0.1818)\n  Hash: -1 Total runs: 733\n  PASS:       1/     11 (0.0909)\n",
				player.toString(1));
	}

//	@Test
//	public void testCoupDeGrace() throws UnknownPropertyException {
//		player.setProperty("threads", "4");
//		player.setProperty("grace", "true");
//		player.reset();
//		String[] problem;
//			problem = new String[] { "OOO................",// 19
//					"OOO................",// 18
//					"###################",// 17
//					"OOO................",// 16
//					"OOO................",// 15
//					"###################",// 14
//					"OOO................",// 13
//					"OOO................",// 12
//					"###################",// 11
//					"OOO................",// 10
//					"OOO................",// 9
//					"###################",// 8
//					"OOO................",// 7
//					"OOO................",// 6
//					"###################",// 5
//					"OOO................",// 4
//					"OOO................",// 3
//					"###################",// 2
//					"OOO................" // 1
//			// ABCDEFGHJKLMNOPQRST
//			};
//		

	@Test
	public void testMultipleMovesAtSamePointNotCounted() {
			String[] problem = { "...................",// 19
					"...................",// 18
					"...................",// 17
					"...................",// 16
					"...................",// 15
					"...................",// 14
					"...................",// 13
					"...................",// 12
					"...................",// 11
					"...................",// 10
					"...................",// 9
					"...................",// 8
					"...................",// 7
					"...................",// 6
					"...................",// 5
					"...................",// 4
					"...................",// 3
					"OO.................",// 2
					".#................." // 1
			// ABCDEFGHJKLMNOPQRST
			};
			player.setUpProblem(BLACK, problem);
			fakeRun(BLACK, "a1", "c1", "a1");
			SearchNode root = player.getRoot();
			assertEquals(2.0 / 3, root.getWinRate(at("a1")), 0.01);
	}

	@Test
	public void testMultipleMovesAtSamePointNotCounted2() {
			String[] problem = { "...................",// 19
					"...................",// 18
					"...................",// 17
					"...................",// 16
					"...................",// 15
					"...................",// 14
					"...................",// 13
					"...................",// 12
					"...................",// 11
					"...................",// 10
					"...................",// 9
					"...................",// 8
					"...................",// 7
					"...................",// 6
					"...................",// 5
					"...................",// 4
					"...................",// 3
					"OO.................",// 2
					".#................." // 1
			// ABCDEFGHJKLMNOPQRST
			};
			player.setUpProblem(BLACK, problem);
			board.setUpProblem(BLACK, problem);
			board.play(at("c5"));
			fakeRun(WHITE, "c5");
			fakeRun(WHITE, "c5", "d7", "a1", "c1", "a5", "a1");
			SearchNode child = player.getTable().findIfPresent(indexOfBoard(board));
			assertEquals(2.0 / 3, child.getWinRate(at("d7")), 0.01);
	}

	@Test
	public void testPassAbortsTreeTraversal() {
		fakeRun(BLACK, "c3", "d1");
		SearchNode root = player.getRoot();
		assertEquals(1.0 / 2, root.getWinRate(at("d1")), 0.01);
	}

	@Test
	public void testBestStoredMove() {
		fakeRun(BLACK, "c4", "c5", "c6", "c7");
		fakeRun(BLACK, "c4", "c5", "c6", "c7");
		fakeRun(WHITE, "c5", "c4", "c6", "c7");
		assertEquals(at("c4"), player.bestStoredMove());
	}

	@Test
	public void testInitialTree() {
		assertEquals(1, player.dagSize());
	}

	@Test
	public void testMcRunIncorporation() {
		McRunnable runnable = (McRunnable) player.getRunnable(0);
		int runs = 100;
		for (int i = 0; i < runs; i++) {
			runnable.performMcRun();
		}
		assertEquals(runs + (2 * getBoardArea() + 10), player.getRoot()
				.getTotalRuns());
	}

	@Test
	public void testNoIllegalMovesInTree() {
			String[] problem = { ".########OOOOOOOOO.",// 19
					"#########OOOOOOOOOO",// 18
					"#########OOOOOOOOOO",// 17
					"#########OOOOOOOOOO",// 16
					"#########OOOOOOOOOO",// 15
					"#########OOOOOOOOOO",// 14
					"#########OOOOOOOOOO",// 13
					"#########OOOOOOOOOO",// 12
					"#########OOOOOOOOOO",// 11
					"#########OOOOOOOOOO",// 10
					"#########OOOOOOOOOO",// 9
					"#########OOOOOOOOOO",// 8
					"#########OOOOOOOOOO",// 7
					"#########OOOOOOOOOO",// 6
					"#########OOOOOOOOOO",// 5
					"#########OOOOOOOOOO",// 4
					"#########OOOOOOOOOO",// 3
					"#########OOOOOOOOOO",// 2
					".########OOOOOOOOO." // 1
			// ABCDEFGHJKLMNOPQRST
			};
			player.setUpProblem(WHITE, problem);
			int move = player.bestMove();
			assertEquals(PASS, move);
	}

	@Test
	public void testPassInSeki() {
			String[] problem = { "#.#########OOOOOOOO",// 19
					".##########OOOOOOO.",// 18
					"###########OOOOOOOO",// 17
					"###########OOOOOOO.",// 16
					"###########OOOOOOOO",// 15
					"#########OOOOOOOOOO",// 14
					"#########OOOOOOOOOO",// 13
					"#########OOOOOOOOOO",// 12
					"#########OOOOOOOOOO",// 11
					"#########OOOOOOOOOO",// 10
					"#########OOOOOOOOOO",// 9
					"#########OOOOOOOOOO",// 8
					"#########OOOOOOOOOO",// 7
					"#########OOOOOOOOOO",// 6
					"#########OOOOOOOOOO",// 5
					"##OO###OOOO######OO",// 4
					"OOO#OO#########OOOO",// 3
					".OO#.O#.######OOOOO",// 2
					"O.O#.O##.######OOOO" // 1
			// ABCDEFGHJKLMNOPQRST
			};
			player.setUpProblem(BLACK, problem);
			int move = player.bestMove();
			assertEquals(PASS, move);
	}

	@Test
	public void testReclaimOldNodes() {
		SearchNode node = player.getTable().findIfPresent(0L);
		node.incrementTotalRuns();
		assertEquals(getBoardArea() * 2 + 10 + 1, node.getTotalRuns());
		player.acceptMove(at("a1"));
		long hash = indexOfBoard(player.getBoard());
		player.acceptMove(at("a2"));
		player.acceptMove(at("a3"));
		assertNull(player.getTable().findIfPresent(hash));
		assertNotNull(player.getTable().findOrAllocate(hash));
	}

	@Test
	public void testAcceptMoveWhenNoBranchExists() {
		player.acceptMove(at("e5"));
		board.copyDataFrom(player.getBoard());
		assertEquals(indexOfBoard(board), indexOfBoard(player.getBoard()));
	}

	@Test
	public void testPassCountMaintenance2() {
			String[] problem = { "OOOOOOOOOOOOOOOOOO.",// 19
					"OOOOOOOOOOOOOOOOO.O",// 18
					"OOOOOOOOOOOOOOOOOOO",// 17
					"OOOOOOOOOOOOOOOOOOO",// 16
					"OOOOOOOOOOOOOOOOOOO",// 15
					"OOOOOOOOOOOOOOOOOOO",// 14
					"OOOOOOOOOOOOOOOOOOO",// 13
					"OOOOOOOOOOOOOOOOOOO",// 12
					"OOOOOOOOOOOOOOOOOOO",// 11
					"OOOOOOOOOOOOOOOOOOO",// 10
					"OOOOOOOOOOOOOOOOOOO",// 9
					"OOOOOOOOOOOOOOOOOOO",// 8
					"OOOOOOOOOOOOOOOOOOO",// 7
					"OOOOOOOOOOOOOOOOOOO",// 6
					"#########OOOOOOOOOO",// 5
					"#########OOOOOOOOOO",// 4
					"#########OOOOOOOOOO",// 3
					"#########OOOOOOOOOO",// 2
					"...######OOOOOOOOOO" // 1
			// ABCDEFGHJKLMNOPQRST
			};
			player.getBoard().setUpProblem(BLACK, problem);
			player.acceptMove(PASS);
			player.acceptMove(PASS);
			player.acceptMove(PASS);
			player.undo();
			player.bestMove();
			player.undo();
			player.acceptMove(at("b1"));
			player.acceptMove(PASS);
			player.bestMove();
	}

	@Test
	public void testPondering() throws UnknownPropertyException {
		try {
			player.setProperty("ponder", null);
			player.acceptMove(at("c2"));
			Thread.sleep(100);
			assertTrue(player.getRoot().getTotalRuns() > 0);
		} catch (InterruptedException e) {
			e.printStackTrace();
			assertTrue(false);
		}
	}

//	@Test
//	public void testKillDeadStonesToOvercomeLargeTerritory() {
//			String[] problem = { 
//					"#..................",// 19
//					"O..................",// 18
//					"O..................",// 17
//					"O..................",// 16
//					"O..................",// 15
//					"O..................",// 14
//					"OOOOOOOOOOOOOOOOOOO",// 13
//					"OOOOOOOOOOOOOOOOOOO",// 12
//					"OOOOOOOOOOOOOOOOOOO",// 11
//					"OOOOOOOOOOOOOOOOOOO",// 10
//					"###################",// 9
//					"#..................",// 8
//					"#..................",// 7
//					"#..................",// 6
//					"#..................",// 5
//					"#..................",// 4
//					"#..................",// 3
//					"####...............",// 2
//					".#.#..............." // 1
//			// ABCDEFGHJKLMNOPQRST
//			};
//			player.getBoard().setUpProblem(BLACK, problem);
//			player.getBoard().play(PASS);
//			int move = player.bestMove();
//			// White must capture the dead black stone to win
//			assertFalse(PASS == move);
//	}
//
//	@Test
//	public void testKillDeadStonesToOvercomeLargeTerritory2() {
//			// Orego must realize that its own stone is dead.
//			String[] problem = { "##.O.OOOOOOOOOOOOO.",// 19
//					"OOOOOOOOOOOOOOOOOOO",// 18
//					"OOOOOOOOOOOOOOOOOOO",// 17
//					"OOOOOOOOOOOOOOOOOOO",// 16
//					"OOOOOOOOOOOOOOOOOOO",// 15
//					"OOOOOOOOOOOOOOOOOOO",// 14
//					"OOOOOOOOOOOOOOOOOOO",// 13
//					"OOOOOOOOOOOOOOOOOOO",// 12
//					"OOOOOOOOOOOOOOOOOOO",// 11
//					"OOOOOO#############",// 10
//					"###################",// 9
//					"###################",// 8
//					"###################",// 7
//					"###################",// 6
//					"###################",// 5
//					"###################",// 4
//					"####.O#############",// 3
//					"#######.###########",// 2
//					".#.##..############" // 1
//			// ABCDEFGHJKLMNOPQRST
//			};
//			player.getBoard().setUpProblem(BLACK, problem);
//			player.acceptMove(PASS);
//			int move = player.bestMove();
//			// White must capture the dead black stones to win
//			// assertEquals(at("c19"), move);
//			assertFalse(PASS == move);
//			player.getBoard().play(move);
//	}
	
	@Test
	public void testPassToWin() {
			// Orego must realize that its own stone is dead.
			String[] problem = { 
				// 	 ABCDEFGHJKLMNOPQRST
					"...................",// 19
					"...OOOOOOOOOOOO....",// 18
					"...O##########O....",// 17
					"...O##########O....",// 16
					"...O##########O....",// 15
					"...O##########O....",// 14
					"...O##########O....",// 13
					"...OOOOO.OOOO.O....",// 12
					"...................",// 11
					"...................",// 10
					"OOOOOOOOOOOOOOOOOOO",// 9
					"###################",// 8
					"...................",// 7
					"....###########....",// 6
					".....OOOOOOOOO#....",// 5
					"....#OOOOOOOOO#....",// 4
					".....OOOOOOOOO#....",// 3
					"....###########....",// 2
					"..................." // 1
			// 		 ABCDEFGHJKLMNOPQRST
			};
			player.getBoard().setUpProblem(BLACK, problem);
			player.acceptMove(PASS);
			int move = player.bestMove();
			// White should pass to win
			assertTrue(PASS == move);
			player.getBoard().play(move);
	}

	@Test
	public void testSecondPassWouldLoseGame() {
		String[] problem;
			problem = new String[] { 
					"OOOOOOOOOOOOOOOOOOO",// 19
					"O..OOOOOOO#OOOOOOOO",// 18
					"O...OO############O",// 17
					"OO.OOOOOOO#OOOOOOOO",// 16
					"OOOOOOOOOO#OOOOOOOO",// 15
					"#####OOOOO#OOOOOOOO",// 14
					"###################",// 13
					"##.#############.##",// 12
					"###################",// 11
					"###################",// 10
					"###################",// 9
					"###################",// 8
					"OOOOOOOOOOOOOOOOOOO",// 7
					"OOOOOOOOOOOOOOOOOOO",// 6
					"#OOOOOOOOOOOOOOOOOO",// 5
					"#####O#####OOOOOOOO",// 4
					"#####O#####OO.OOO.O",// 3
					"#####O#..##OOOOOOOO",// 2
					"#######..##########" // 1
			// 		 ABCDEFGHJKLMNOPQRST
			};
		player.getBoard().setUpProblem(WHITE, problem);
		player.acceptMove(PASS);
		assertEquals(BLACK,player.getBoard().getColorToPlay());
		// Black has to kill the top white group to win, but passing
		// to end the game appears to win
		player.getRoot().addWins(PASS, 100);
		// System.out.println(pointToString(player.bestStoredMove()));
		
		int move = player.bestStoredMove();
		System.out.println(pointToString(move));
		assertFalse(PASS == move);
		assertEquals("C17",pointToString(move));
	}
	
	@Test
	public void testSecondPassWouldWinGame() {
		String[] problem;
			problem = new String[] { 
					"OOOOOOOOOOOOOOOOOOO",// 19
					"O..OOOOOOO#OOOOOOOO",// 18
					"O...OO############O",// 17
					"OO.OOOOOOO#OOOOOOOO",// 16
					"OOOOOOOOOO#OOOOOOOO",// 15
					"#####OOOOO#OOOOOOOO",// 14
					"###################",// 13
					"##.#############.##",// 12
					"###################",// 11
					"###################",// 10
					"OOOOOOOOOOOOOOOOOOO",// 9
					"OOOOOOOOOOOOOOOOOOO",// 8
					"OOOOOOOOOOOOOOOOOOO",// 7
					"OOOOOOOOOOOOOOOOOOO",// 6
					"#OOOOOOOOOOOOOOOOOO",// 5
					"#####O#####OOOOOOOO",// 4
					"#####O#####OO.OOO.O",// 3
					"#####O#..##OOOOOOOO",// 2
					".######..##########" // 1
			// 		 ABCDEFGHJKLMNOPQRST
			};
		player.getBoard().setUpProblem(WHITE, problem);
		player.acceptMove(PASS);
		assertEquals(BLACK,player.getBoard().getColorToPlay());
		//pass by black will win game, but only if Orego notices the white stones are dead.
		// System.out.println(pointToString(player.bestStoredMove()));
		assertEquals(PASS, player.bestStoredMove());
	}

	@Test
	public void testDebug1() {
			String[] problem = { "#########OOOOOOOOOO",// 19
					"#########OOOOOOOOOO",// 18
					"#########OOOOOOOOOO",// 17
					"#########OOOOOOOOOO",// 16
					"#########OOOOOOOOOO",// 15
					"##########OOOOOOOOO",// 14
					"##########OOOOOOOOO",// 13
					"##########OOOOOOOOO",// 12
					"##########OOOOOOOOO",// 11
					"##########OOOOOOOOO",// 10
					"..##.#..###########",// 9
					"..#O.#.#.##########",// 8
					"..#O##.##OOOOOOOOOO",// 7
					"...#.##O########OOO",// 6
					"..###OOOOOOOOO#####",// 5
					"###OOOOOOOOO#######",// 4
					"OOOO.O#############",// 3
					"#OOOO##OOOOOOOOO###",// 2
					".O###.#O.##########" // 1
			// ABCDEFGHJKLMNOPQRST
			};
			player.getBoard().setUpProblem(BLACK, problem);
			player.acceptMove(PASS);
			player.bestMove();
	}

	@Test
	public void testDebug2() {
			String[] problem = { "#########OOOOOOOOOO",// 19
					"#########OOOOOOOOOO",// 18
					"#########OOOOOOOOOO",// 17
					"#########OOOOOOOOOO",// 16
					"#########OOOOOOOOOO",// 15
					"##########OOOOOOOOO",// 14
					"##########OOOOOOOOO",// 13
					"##########OOOOOOOOO",// 12
					"##########OOOOOOOOO",// 11
					"##########OOOOOOOOO",// 10
					".O#.#OO..OOOOOOOOOO",// 9
					"OO####OOOOOOOOOOOOO",// 8
					".###OOO############",// 7
					"####OO#.###########",// 6
					"OO#OO##O###########",// 5
					"OOOOO##############",// 4
					".O###.#..OOOOOOOOOO",// 3
					"##O##..#OOOOOOOOOOO",// 2
					".#O.#.#OOOOOOOOOOOO" // 1
			// ABCDEFGHJKLMNOPQRST
			};
			player.getBoard().setUpProblem(WHITE, problem);
			player.acceptMove(PASS);
			player.bestMove();
	}

	@Test
	public void testConnect() throws UnknownPropertyException {
			String[] problem = { "##########OOOOOOOOO",// 19
					"##.#######OOOOOO.OO",// 18
					"#...######OOOOO...O",// 17
					"##.#######OOOOOO.OO",// 16
					"##########OOOOOOOOO",// 15
					"##########OOOOOOOOO",// 14
					"##########OOOOOOOOO",// 13
					"##########OOOOOOOOO",// 12
					"##########OOOOOOOOO",// 11
					"OOOOOOOOO.OOOOOOOOO",// 10
					"OOOOOOOOO##########",// 9
					"OOOOOOOOO##########",// 8
					"OOOOOOOOO##########",// 7
					"OOOOOOOOO##########",// 6
					"OOOO.OOOO##########",// 5
					"OOO...OOO######.###",// 4
					"OOOO.OOOO#####...##",// 3
					"OOOOOOOOO######.###",// 2
					"OOOOOOOOO##########" // 1
			// ABCDEFGHJKLMNOPQRST
			};
			player.setUpProblem(BLACK, problem);
			player.setProperty("playouts", "10000");
			int move = player.bestMove();
			assertEquals(at("k10"), move);
	}

	@Test
	public void testSuperko() {
		String[] problem;
			problem = new String[] { "...................",// 19
					"...................",// 18
					"...................",// 17
					"...................",// 16
					"...................",// 15
					"...................",// 14
					"...................",// 13
					"...................",// 12
					"...................",// 11
					"...................",// 10
					"...................",// 9
					"...................",// 8
					"......#............",// 7
					"OOO................",// 6
					"##O................",// 5
					".#O................",// 4
					"##OOO..............",// 3
					"..##O..............",// 2
					"##.O.O............." // 1
			// ABCDEFGHJKLMNOPQRST
			};
		player.setUpProblem(WHITE, problem);
		player.acceptMove(at("c1"));
		player.acceptMove(at("e1"));
		// D1 is now a superko violation
		// Add some wins so the move looks good
		player.getRoot().addWins(at("d1"), 1000);
		player.bestMove();
		assertEquals(Integer.MIN_VALUE, player.getWins(at("d1")), 0.001);
	}

	@Test
	public void testTransposition() throws UnknownPropertyException {
		fakeRun(BLACK, "c3");
		fakeRun(BLACK, "c3", "c4", "c5", "c6");
		fakeRun(BLACK, "c3", "c4", "c5", "c6");
		fakeRun(BLACK, "c3", "c4", "c5", "c6");
		board.copyDataFrom(player.getBoard());
		board.play(at("c3"));
		board.play(at("c4"));
		board.play(at("c5"));
		long greatGrandchild = indexOfBoard(board);
		SearchNode node = player.getTable().findIfPresent(greatGrandchild);
		assertEquals(getBoardArea() * 2 + 11, node.getTotalRuns());
		board.copyDataFrom(player.getBoard()); // Create other child
		board.play(at("c5"));
		fakeRun(BLACK, "c5");
		fakeRun(BLACK, "c5", "c4", "c3", "c6");
		board.play(at("c4"));
		fakeRun(BLACK, "c5", "c4", "c3", "c6");
		assertEquals(getBoardArea() * 2 + 12, node.getTotalRuns());
	}

	@Test
	public void testSetMillisecondsPerMove() {
		player.setMillisecondsPerMove(314);
		assertEquals(314, player.getMillisecondsPerMove());
		assertEquals(-1, player.getPlayoutLimit());
	}

	@Test
	public void testSetPlayoutLimit() {
		player.setPlayoutLimit(1000);
		assertEquals(1000, player.getPlayoutLimit());
		assertEquals(-1, player.getMillisecondsPerMove());
	}

	@Test
	public void testSetPropertyPlayouts() throws UnknownPropertyException {
		player.setProperty("playouts", "1000");
		assertEquals(player.getPlayoutLimit(), 1000);
		assertEquals(-1, player.getMillisecondsPerMove());
	}

	@Test
	/** This test is necessary because of the overridden setMillisecondsPerMove(). */
	public void testSetPropertyMillisecondsPerMove()
			throws UnknownPropertyException {
		player.setProperty("msec", "314");
		assertEquals(314, player.getMillisecondsPerMove());
		assertEquals(-1, player.getPlayoutLimit());
	}

	@Test
	public void testResetClearsTranspositionTable() {
		player.setPlayoutLimit(5000);
		int move = player.bestMove();
		player.acceptMove(move);
		player.bestMove();
		player.reset();
		player.bestMove();
	}

	@Test
	public void testPriorsAtRoot() throws UnknownPropertyException {
		player.setHeuristics(new HeuristicList("SpecificPoint@1"));
		player.reset();
		player.acceptMove(at("b1"));
		SearchNode root = player.getRoot();
		assertEquals(2, root.getWins(at("c5")), 0.001);
		assertEquals(3, root.getRuns(at("c5")), 0.001);
		player.acceptMove(at("b2"));
		player.acceptMove(at("b3"));
		player.acceptMove(at("b4"));
		root = player.getRoot();
		assertEquals(2, root.getWins(at("c5")), 0.001);
		assertEquals(3, root.getRuns(at("c5")), 0.001);
	}

	@Test
	public void testResign() {
		SearchNode root = player.getRoot();
		for (int p : getAllPointsOnBoard()) {
			root.addLosses(p, 100);
		}
		root.addWins(PASS, 1);
		assertEquals(PASS, player.bestPlayMove(root));
		root.addLosses(PASS, 99);
		assertEquals(RESIGN, player.bestPlayMove(root));
	}

	@Test
	public void testFinalStatusList() {
		String[] problem;
			problem = new String[] { 
					"OO.##.OOOOOOOOOOOOO",// 19
					"OOOOOOOOOOOOOOOOOOO",// 18
					"###################",// 17
					"###################",// 16
					"###################",// 15
					"##.############.###",// 14
					"###################",// 13
					"OOOOOOOOOOOOOOOOOOO",// 12
					"OO.OOOOOOOOOOOO.OOO",// 11
					"OOOOOOOOOOOOOOOOOOO",// 10
					"OOOOOOOOOOOOOOOOOOO",// 9
					"OOOOOOOOOOOOOOOOOOO",// 8
					"OOOOOOOOOOOOOOOOOOO",// 7
					"OOOOOOOOOOOOOOOOOOO",// 6
					"OOOOOOO############",// 5
					"OOOOOOO#.#..#######",// 4
					"OOOOOOO##.O.#######",// 3
					"OOOOOOO############",// 2
					"OOOOOOO############" // 1
			//       ABCDEFGHJKLMNOPQRST
			};
			player.getBoard().setUpProblem(BLACK, problem);
			// We currently don't report points in seki
			assertEquals("", player.handleCommand("final_status_list",
					new StringTokenizer("seki")));
			assertEquals(BLACK,player.getBoard().getColorToPlay());
			System.out.println(player.getBoard());
			assertEquals(
					"A19 B19 G19 H19 J19 K19 L19 M19 N19 O19 P19 Q19 R19 S19 T19 A18 B18 C18 D18 E18 F18 G18 H18 J18 K18 L18 M18 N18 O18 P18 Q18 R18 S18 T18 A17 B17 C17 D17 E17 F17 G17 H17 J17 K17 L17 M17 N17 O17 P17 Q17 R17 S17 T17 A16 B16 C16 D16 E16 F16 G16 H16 J16 K16 L16 M16 N16 O16 P16 Q16 R16 S16 T16 A15 B15 C15 D15 E15 F15 G15 H15 J15 K15 L15 M15 N15 O15 P15 Q15 R15 S15 T15 A14 B14 D14 E14 F14 G14 H14 J14 K14 L14 M14 N14 O14 P14 R14 S14 T14 A13 B13 C13 D13 E13 F13 G13 H13 J13 K13 L13 M13 N13 O13 P13 Q13 R13 S13 T13 A12 B12 C12 D12 E12 F12 G12 H12 J12 K12 L12 M12 N12 O12 P12 Q12 R12 S12 T12 A11 B11 D11 E11 F11 G11 H11 J11 K11 L11 M11 N11 O11 P11 R11 S11 T11 A10 B10 C10 D10 E10 F10 G10 H10 J10 K10 L10 M10 N10 O10 P10 Q10 R10 S10 T10 A9 B9 C9 D9 E9 F9 G9 H9 J9 K9 L9 M9 N9 O9 P9 Q9 R9 S9 T9 A8 B8 C8 D8 E8 F8 G8 H8 J8 K8 L8 M8 N8 O8 P8 Q8 R8 S8 T8 A7 B7 C7 D7 E7 F7 G7 H7 J7 K7 L7 M7 N7 O7 P7 Q7 R7 S7 T7 A6 B6 C6 D6 E6 F6 G6 H6 J6 K6 L6 M6 N6 O6 P6 Q6 R6 S6 T6 A5 B5 C5 D5 E5 F5 G5 A4 B4 C4 D4 E4 F4 G4 A3 B3 C3 D3 E3 F3 G3 A2 B2 C2 D2 E2 F2 G2 A1 B1 C1 D1 E1 F1 G1 ",
					player.finalStatusList("alive"));
	}

	@Test
	public void testHandleCommand() throws UnknownPropertyException {
		player.setProperty("ponder", "true");
		player.acceptMove(at("b4"));
		assertEquals("", player.handleCommand("final_status_list",
				new StringTokenizer("seki")));
		player.setProperty("ponder", "false");
		player.acceptMove(at("c2"));
		assertNull(player.handleCommand("destroy_all_humans",
				new StringTokenizer("")));
	}

	@Test
	public void testForcedPassAtGameLengthLimits() {
		for (int i = 0; i < MAX_MOVES_PER_GAME - 3; i++) {
			player.acceptMove(PASS);
		}
		McRunnable runnable = (McRunnable) player.getRunnable(0);
		player.acceptMove(at("b3"));
		runnable.copyDataFrom(player.getBoard());
		int move = player.selectAndPlayMove(player.getRoot(), runnable);
		assertEquals(PASS, move);
	}

	@Test
	public void testSetPropertyGrace() throws UnknownPropertyException {
		assertEquals(false, player.isGrace());
		player.setProperty("grace", "true");
		assertEquals(true, player.isGrace());
	}

	@Test
	public void testManyThreads() throws UnknownPropertyException {
		player.setProperty("threads", "12");
		player.setPlayoutLimit(1000);
		player.reset();
		// If the threads are interfering, this would cause a problems
		player.bestMove();
	}

	@Test
	public void testRepeatWinningMove() {
		SearchNode node = player.getRoot();
			fakeRun(BLACK, "c3");
		assertEquals(at("c3"),
				player.bestSearchMove(node, board, new MersenneTwisterFast()));
		node.addWins(at("c4"), 100);
		assertEquals(at("c3"),
				player.bestSearchMove(node, board, new MersenneTwisterFast()));
	}

}
/*
 * 
O . O . . O O O O   O . O . . O O O O   O . O . . O O O O
O O O O O O O O O   O O O O O O O O O   O O O O O O O O O
# # # # # O . O O   # # # # # O # O O   # # # # # O . O O
# # # # # # O # .   # # # # # # . # .   # # # # # # O # .
# # # # # O O # #   # # # # # . . # #   # # # # # . . # #
O O O O O O O O O   . . . . . . . . .   . . . . . . . . .

 */



