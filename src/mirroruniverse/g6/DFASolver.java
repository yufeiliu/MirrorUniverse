package mirroruniverse.g6;

import java.util.ArrayList;

import mirroruniverse.g6.Utils.Entity;
import mirroruniverse.g6.Utils.Move;
import mirroruniverse.g6.dfa.DFA;

public class DFASolver extends Solver {

	private static final int MAX_DISTANCE = 10; 
	
	@Override
	Solution solve(int[][] firstMap, int[][] secondMap) {
		ArrayList<Move> solution = null;
		int attempts = 0;
		DFA<Entity, Move> firstDFA, secondDFA, firstBack, secondBack;
		firstDFA = firstBack = new DFA<Entity, Move>(firstMap);
		secondDFA = secondBack = new DFA<Entity, Move>(secondMap);
		
		solution = DFA.intersect(firstDFA, secondDFA).findShortestPath();
		while (solution == null && attempts < MAX_DISTANCE) {
			secondBack = secondBack.shiftGoals();
			solution = DFA.intersect(firstDFA, secondBack).findShortestPath();
			if (solution == null) {
				firstBack = firstBack.shiftGoals();
				solution = DFA.intersect(firstBack, secondDFA).findShortestPath();
			}
			attempts++;
		}
		return solution == null ? null : new Solution(solution, attempts);
	}
	
}
