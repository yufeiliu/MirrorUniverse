package mirroruniverse.g6;

import java.util.ArrayList;
import mirroruniverse.g6.Utils.Move;
import mirroruniverse.g6.dfa.DFA;

public class DFASolver extends Solver {
	
	@Override
	Solution solve(int[][] firstMap, int[][] secondMap) {
		return solve(firstMap, secondMap, DEFAULT_CUTOFF_TIME,
				DEFAULT_MIN_DISTANCE, DEFAULT_MAX_DISTANCE);
	}
	
	
	// TODO: can this fail if we step back before the start state? This might
	// mean that we generate a DFA with no exit state.
	@Override
	Solution solve(int[][] firstMap, int[][] secondMap, long cutoffTime,
			int minAttempts, int maxAttempts) {
		
		if (G6Player.SID_DEBUG) {
			System.out.println("\nSolving");
		}
		
		DFA firstDFA, secondDFA, firstBack, secondBack;
		Solution steps = null;
		int attempts = 1;
		long startTime = System.currentTimeMillis();
		
		firstDFA = firstBack = new DFA(firstMap);
		secondDFA = secondBack = new DFA(secondMap);
		
		if (G6Player.SID_DEBUG) {
			System.out.println("DFAs Created");
		}
		
		steps = DFA.findShortestPathBFS(firstDFA, secondDFA, 0);
//		steps = DFA.intersect(firstDFA, secondDFA).findShortestPath();
		
		// This will try until we've found a solution, or have hit our time
		// limit or max number of attempts. We can guarantee to try some min
		// number of attempts before we stop because of time.
		while (steps == null && attempts < maxAttempts &&
				(System.currentTimeMillis() - startTime < cutoffTime
						|| attempts < minAttempts)) {
			
			if (G6Player.SID_DEBUG) {
				System.out.println("Attempt " + attempts);
			}
			
			secondBack = secondBack.shiftGoals();
			
			steps = DFA.findShortestPathBFS(firstDFA, secondBack, attempts - 1);
			if (steps == null) {
				firstBack = firstBack.shiftGoals();
				steps = DFA.findShortestPathBFS(firstBack, secondDFA, attempts - 1);
			}
			
			attempts++;
		}
		return steps;
	}
	
	Solution solve(int[][] map) {
		if (G6Player.SID_DEBUG) {
			System.out.println("\nSolving");
		}
		
		DFA dfa = new DFA(map);
		ArrayList<Move> steps = dfa.findShortestPath();
		return steps == null ? null : new Solution(steps, 0, false);
	}
	
}
