package mirroruniverse.g5;

import mirroruniverse.g5.Utils.Move;

public abstract class Solver {
	
	
	public int[] solve(int[][] firstMap, int[][] secondMap) {
		Move[] moves = solveInternal(firstMap, secondMap);
		return movesToInt(moves);
	}
	
	private int[] movesToInt(Move[] solution) {
		int[] convertedSolution = new int[solution.length];
		for (int i = 0; i < solution.length; i++) {
			convertedSolution[i] = Utils.moveToShen(solution[i]);
		}
		return convertedSolution;
	}
	
	abstract Move[] solveInternal(int[][] firstMap, int[][] secondMap);

}
