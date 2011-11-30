package mirroruniverse.g6;

import java.util.ArrayList;
import java.util.Arrays;

import mirroruniverse.g6.Utils.Entity;
import mirroruniverse.g6.Utils.Move;
import mirroruniverse.g6.dfa.DFA;

public class DFASolver extends Solver {

	private static final int MAX_DISTANCE = 5; 
	
	@Override
	Solution solve(int[][] firstMap, int[][] secondMap) {
		DFA<Entity, Move> firstDFA, secondDFA, firstBack, secondBack;
		ArrayList<Move> solution = null;
		int attempts = 0;
		
		firstDFA = firstBack = new DFA<Entity, Move>(firstMap);
		secondDFA = secondBack = new DFA<Entity, Move>(secondMap);
		
		DFA<Entity, Move> intersection = DFA.intersect(firstDFA, secondDFA);
		
		if (intersection.getStartState() == null) {
			System.err.println("DFA failed.");
			return null;
		}
		
		solution = intersection.findShortestPath();

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
