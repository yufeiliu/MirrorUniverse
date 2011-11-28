package mirroruniverse.g6;

import java.util.ArrayList;

import mirroruniverse.g6.Utils.Entity;
import mirroruniverse.g6.Utils.Move;
import mirroruniverse.g6.dfa.DFA;

public class DFASolver extends Solver {

	private static final int MAX_DISTANCE = 0; 
	
	@Override
	ArrayList<Move> solveInternal(int[][] firstMap, int[][] secondMap) {
		ArrayList<Move> solution = null;
		int attempts = 0;
		while (solution == null && attempts <= MAX_DISTANCE) {
			// TODO - this doesn't step backwards yet when the actual
			// goal doesn't work
			// Probably add multithreading
			solution = DFA.intersect(new DFA<Entity, Move>(firstMap),
							new DFA<Entity, Move>(secondMap))
					.findShortestPath();
			attempts++;
		}
		return solution;
	}
	
}
