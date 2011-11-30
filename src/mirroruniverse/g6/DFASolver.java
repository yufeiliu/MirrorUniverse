package mirroruniverse.g6;

import java.util.ArrayList;

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
		
		if (DFA.intersect(firstDFA, secondDFA).getStartState() == null) {
			System.err.println("DFA failed.");
			System.out.println(firstMap);
			System.out.println(firstDFA);
			System.out.println(";;;;;");
			System.out.println(secondMap);
			System.out.println(secondDFA);
			System.out.println(";;;;;");
			System.out.println(firstDFA.getStartState());
			System.out.println(secondDFA.getStartState());
			System.out.println(DFA.intersect(firstDFA, secondDFA).getStartState());
			System.exit(1);
			
			return null;
		}
		
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
