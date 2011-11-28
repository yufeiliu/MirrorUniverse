package mirroruniverse.g6;

import java.util.ArrayList;

import mirroruniverse.g6.Utils.Move;

public abstract class Solver {
	
	
	public int[] solve(int[][] firstMap, int[][] secondMap) {
		ArrayList<Move> moves = solveInternal(firstMap, secondMap);
		return moves == null ? null : movesToInt(moves);
	}
	
	private int[] movesToInt(ArrayList<Move> solution) {
		int[] convertedSolution = new int[solution.size()];
		for (int i = 0; i < solution.size(); i++) {
			convertedSolution[i] = Utils.moveToShen(solution.get(i));
		}
		return convertedSolution;
	}
	
	abstract ArrayList<Move> solveInternal(int[][] firstMap, int[][] secondMap);

}
