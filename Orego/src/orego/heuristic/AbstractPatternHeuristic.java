package orego.heuristic;

import static orego.core.Colors.BLACK;
import static orego.core.Colors.OFF_BOARD_COLOR;
import static orego.core.Colors.VACANT;
import static orego.core.Colors.WHITE;
import static orego.patterns.Pattern.diagramToNeighborhood;
import orego.patterns.ColorSpecificPattern;
import orego.patterns.Cut1Pattern;
import orego.patterns.Pattern;
import orego.patterns.SimplePattern;
import orego.util.BitVector;

public abstract class AbstractPatternHeuristic extends Heuristic {

	/**
	 * The number of total patterns, including impossible ones.
	 */
	public static final int NUMBER_OF_NEIGHBORHOODS = Character.MAX_VALUE + 1;
	
	public static final BitVector[] BAD_NEIGHBORHOODS = {
		new BitVector(NUMBER_OF_NEIGHBORHOODS),
		new BitVector(NUMBER_OF_NEIGHBORHOODS) };
	
	public static final BitVector[] GOOD_NEIGHBORHOODS = {
		new BitVector(NUMBER_OF_NEIGHBORHOODS),
		new BitVector(NUMBER_OF_NEIGHBORHOODS) };
	
	/**
	 * Set of 3x3 patterns taken from Gelly et al,
	 * "Modification of UCT with Patterns in Monte-Carlo Go"
	 */
	/**
	 * Used by isPossibleNeighborhood().
	 */
	public static final char[] VALID_OFF_BOARD_PATTERNS = {
				diagramToNeighborhood("...\n. .\n..."),
				diagramToNeighborhood("*..\n* .\n*.."),
				diagramToNeighborhood("..*\n. *\n..*"),
				diagramToNeighborhood("***\n. .\n..."),
				diagramToNeighborhood("...\n. .\n***"),
				diagramToNeighborhood("***\n* .\n*.."),
				diagramToNeighborhood("***\n. *\n..*"),
				diagramToNeighborhood("*..\n* .\n***"),
				diagramToNeighborhood("..*\n. *\n***") };

	static {
		Pattern[] BLACK_GOOD_PATTERNS = {
				// BLACK SPECIFIC PATTERNS
				new ColorSpecificPattern("O...#O??", BLACK), // Hane4
				new ColorSpecificPattern("#??*?O**", BLACK), // Edge3
				new ColorSpecificPattern("O?+*?#**", BLACK), // Edge4
				new ColorSpecificPattern("O#O*?#**", BLACK) // Edge5
		};
		
		Pattern[] WHITE_GOOD_PATTERNS = {
				// WHITE SPECIFIC PATTERNS
				new ColorSpecificPattern("O...#O??", WHITE), // Hane4
				new ColorSpecificPattern("#??*?O**", WHITE), // Edge3
				new ColorSpecificPattern("O?+*?#**", WHITE), // Edge4
				new ColorSpecificPattern("O#O*?#**", WHITE) // Edge5	
		};
		
		Pattern[] INDEPENDENT_GOOD_PATTERNS = {
			// Color independent patterns
			new SimplePattern("O..?##??"), // Hane1
			new SimplePattern("O...#.??"), // Hane2
			new SimplePattern("O#..#???"), // Hane3
			new Cut1Pattern(), // Cut1
			new SimplePattern("#OO+??++"), // Cut2
			new SimplePattern(".O?*#?**"), // Edge1
			new SimplePattern("#oO*??**") // Edge2
		};
		
		Pattern[] BLACK_BAD_PATTERNS = {
				// BLACK SPECIFIC PATTERNS
				new ColorSpecificPattern("O.OO?oo?", BLACK), // Ponnuki 
				new ColorSpecificPattern(".#..#.?.", BLACK), // Empty Triangle
				new ColorSpecificPattern(".OO?OO??", BLACK) // Push through bamboo
		};
		
		Pattern[] WHITE_BAD_PATTERNS = {
				// WHITE SPECIFIC PATTERNS
				new ColorSpecificPattern("O.OO?oo?", WHITE), // Ponnuki 
				new ColorSpecificPattern(".#..#.?.", WHITE), // Empty Triangle
				new ColorSpecificPattern(".OO?OO??", WHITE) // Push through bamboo
		};
		
		// Find all good neighborhoods, i.e., neighborhoods where a player
		// should play.
		// Note that i has to be an int, rather than a char, because
		// otherwise incrementing it after Character.MAX_VALUE would
		// return it to 0, resulting in an infinite loop.
		for (int i = 0; i < NUMBER_OF_NEIGHBORHOODS; i++) {
			if (!isPossibleNeighborhood((char) i)) {
				continue;
			}
			for (Pattern pattern : BLACK_GOOD_PATTERNS) {
				if (pattern.matches((char) i)) {
					GOOD_NEIGHBORHOODS[BLACK].set(i, true);
				}
			}
			
			for (Pattern pattern : WHITE_GOOD_PATTERNS) {
				if (pattern.matches((char) i)) {
					GOOD_NEIGHBORHOODS[WHITE].set(i, true);
				}
			}
			
			for (Pattern pattern : INDEPENDENT_GOOD_PATTERNS) {
				if (pattern.matches((char) i)) {
					GOOD_NEIGHBORHOODS[BLACK].set(i, true);
					GOOD_NEIGHBORHOODS[WHITE].set(i, true);
				}
			}
			
			for (Pattern pattern : BLACK_BAD_PATTERNS) {
				if (pattern.matches((char) i)) {
					BAD_NEIGHBORHOODS[BLACK].set(i, true);
				}
			}
			
			for (Pattern pattern : WHITE_BAD_PATTERNS) {
				if (pattern.matches((char) i)) {
					BAD_NEIGHBORHOODS[WHITE].set(i, true);
				}
			}
			
		}
	}

	/**
	 * Returns true if the the specified 3x3 neighborhood can possibly occur.
	 * Neighborhoods are impossible if, for example, there are non-contiguous
	 * off-board points.
	 */
	public static boolean isPossibleNeighborhood(char neighborhood) {
		int mask = 0x3;
		// Replace black and white with vacant, leaving only
		// vacant and off-board colors
		for (int i = 0; i < 16; i += 2) {
			if ((neighborhood >>> i & mask) != OFF_BOARD_COLOR) {
				neighborhood &= ~(mask << i);
				neighborhood |= VACANT << i;
			}
		}
		// Verify that the resulting pattern is valid
		assert VALID_OFF_BOARD_PATTERNS != null;
		for (char v : VALID_OFF_BOARD_PATTERNS) {
			if (neighborhood == v) {
				return true;
			}
		}
		return false;
	}

	public AbstractPatternHeuristic(int weight) {
		super(weight);
	}

}