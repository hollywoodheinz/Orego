package edu.lclark.orego.feature;

import java.io.FileInputStream;
import java.io.ObjectInputStream;

import edu.lclark.orego.core.Board;
import edu.lclark.orego.core.Color;
import edu.lclark.orego.core.CoordinateSystem;
import static edu.lclark.orego.core.NonStoneColor.*;
import edu.lclark.orego.util.BitVector;
import edu.lclark.orego.util.ShortSet;
import static edu.lclark.orego.experiment.PropertyPaths.OREGO_ROOT;
	
@SuppressWarnings("serial")
public final class PatternSuggester implements Suggester {
	
	private static final float THRESHOLD = 0.8f;

	private final Board board;

	private final CoordinateSystem coords;

	private final HistoryObserver history;

	private final ShortSet moves;
	
	private BitVector goodPatterns;

	public PatternSuggester(Board board, HistoryObserver history) {
		this.board = board;
		coords = board.getCoordinateSystem();
		this.history = history;
		moves = new ShortSet(coords.getFirstPointBeyondBoard());
		try(ObjectInputStream objectInputStream = new ObjectInputStream(
				// TODO Rename this directory for consistency with config
				new FileInputStream(OREGO_ROOT + "PatternData/Pro3x3PatternData.data"));) {
			int[] fileRuns = (int[]) objectInputStream.readObject();
			int[] fileWins = (int[]) objectInputStream.readObject();
			goodPatterns = new BitVector(fileRuns.length);
			for(int i = 0; i < fileRuns.length; i++){
				goodPatterns.set(i, ((float)fileWins[i] / (float)fileRuns[i]) > THRESHOLD);
			}
			objectInputStream.close();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	@Override
	public ShortSet getMoves() {
		int turn = board.getTurn();
		if(turn==0){
			return moves;
		}
		moves.clear();
		short p = history.get(turn - 1);
		if(p == CoordinateSystem.PASS){
			return moves;
		}
		short[] neighbors = coords.getNeighbors(p);
		for (short n : neighbors) {
			if (board.getColorAt(n) == VACANT) {
				char hash = calculateHash(n);
				if(goodPatterns.get(hash)){
					moves.add(n);
				}
			}
		}
		return moves;
	}
	
	private char calculateHash(short n){
		char hash = 0;
		short[] neighbors = coords.getNeighbors(n);
		for (int i = 0; i < neighbors.length; i++) {
			Color color = board.getColorAt(neighbors[i]);
			if (color == board.getColorToPlay()) {
				// Friendly stone at this neighbor
				hash |= 1 << (i * 2);
			} else if (color != board.getColorToPlay().opposite()) {
				// neighbor is vacant or off board
				hash |= color.index() << (i * 2);
			} // else do nothing, no need to OR 0 with 0
		}
		return hash;
	}

}
