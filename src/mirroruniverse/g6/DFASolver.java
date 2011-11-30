package mirroruniverse.g6;

import java.util.ArrayList;

import mirroruniverse.g6.Utils.Move;
import mirroruniverse.g6.dfa.DFA;

public class DFASolver extends Solver {

	private static final int DEFAULT_MIN_ATTEMPTS = 0;
	private static final int DEFAULT_MAX_ATTEMPTS = 100;
	private static final long DEFAULT_CUTOFF_TIME = 1000;
	
	@Override
	Solution solve(int[][] firstMap, int[][] secondMap) {
		return solve(firstMap, secondMap, DEFAULT_CUTOFF_TIME,
				DEFAULT_MIN_ATTEMPTS, DEFAULT_MAX_ATTEMPTS);
	}
	
	@Override
	Solution solve(int[][] firstMap, int[][] secondMap, long cutoffTime,
			int minAttempts, int maxAttempts) {
		DFA firstDFA, secondDFA, firstBack, secondBack;
		ArrayList<Move> steps = null;
		int attempts = 1;
		long startTime = System.currentTimeMillis();
		
		firstDFA = firstBack = new DFA(firstMap);
		secondDFA = secondBack = new DFA(secondMap);
		
		DFA intersection = DFA.intersect(firstDFA, secondDFA);
		
		if (intersection.getStartState() == null) {
			System.err.println("DFA failed.");
			return null;
		}
		
		steps = intersection.findShortestPath();

		while (steps == null && attempts < maxAttempts &&
				(System.currentTimeMillis() - startTime < cutoffTime
						&& attempts >= minAttempts)) {
			secondBack = secondBack.shiftGoals();
			steps = DFA.intersect(firstDFA, secondBack).findShortestPath();
			if (steps == null) {
				firstBack = firstBack.shiftGoals();
				steps = DFA.intersect(firstBack, secondDFA).findShortestPath();
			}
			attempts++;
		}
		return steps == null ? null : new Solution(steps, attempts - 1);
	}
	
	Solution solve(int[][] map) {
		DFA dfa = new DFA(map);
		ArrayList<Move> steps = dfa.findShortestPath();
		return steps == null ? null : new Solution(steps, 0);
	}
	
}
