package mirroruniverse.g5;

import java.util.ArrayList;

import mirroruniverse.g5.Utils.Move;
import mirroruniverse.g5.dfa.DFA;

public class DFASolver extends Solver {

	private static final int MAX_DISTANCE = 10; 
	
	@Override
	ArrayList<Move> solveInternal(int[][] firstMap, int[][] secondMap) {
		ArrayList<Move> solution = null;
		int attempts = 0;
		while (solution == null && attempts < MAX_DISTANCE) {
			// TODO(yufei) - this doesn't step backwards yet when the actual
			// goal doesn't work
			// Probably add multithreading
			solution = new DFA<Move>(firstMap)
					.intersect(new DFA<Move>(secondMap))
					.findShortestPath();
		}
		return solution;
	}

}
