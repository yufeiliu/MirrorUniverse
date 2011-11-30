package mirroruniverse.g6;

import java.util.ArrayList;

import mirroruniverse.g6.Utils.Entity;
import mirroruniverse.g6.Utils.Move;
import mirroruniverse.g6.dfa.DFA;

public class DFASolver extends Solver {

	private static final int MAX_DISTANCE = 3; 
	
	@Override
	Solution solve(int[][] firstMap, int[][] secondMap) {
		DFA<Entity, Move> firstDFA, secondDFA, firstBack, secondBack;
		ArrayList<Move> steps = null;
		int attempts = 0;
		
		firstDFA = firstBack = new DFA<Entity, Move>(firstMap);
		secondDFA = secondBack = new DFA<Entity, Move>(secondMap);
		
		DFA<Entity, Move> intersection = DFA.intersect(firstDFA, secondDFA);
		
		if (intersection.getStartState() == null) {
			System.err.println("DFA failed.");
			return null;
		}
		
		steps = intersection.findShortestPath();

		while (steps == null && attempts < MAX_DISTANCE) {
			secondBack = secondBack.shiftGoals();
			steps = DFA.intersect(firstDFA, secondBack).findShortestPath();
			if (steps == null) {
				firstBack = firstBack.shiftGoals();
				steps = DFA.intersect(firstBack, secondDFA).findShortestPath();
			}
			attempts++;
		}
		return steps == null ? null : new Solution(steps, attempts);
	}
	
	Solution solve(int[][] firstMap) {
		DFA<Entity, Move> dfa = new DFA<Entity, Move>(firstMap);
		ArrayList<Move> steps = dfa.findShortestPath();
		return steps == null ? null : new Solution(steps, 0);
	}
	
}
