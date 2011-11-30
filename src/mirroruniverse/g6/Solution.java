package mirroruniverse.g6;

import java.util.ArrayList;

import mirroruniverse.g6.Utils.Move;

public class Solution {
	
	private int[] steps;
	private int diff;
	int currentStep;
	
	public Solution(ArrayList<Move> moves, int diff) {
		this.diff = diff;
		this.steps = movesToInts(moves);
	}
	
	
	public static int[] movesToInts(ArrayList<Move> solution) {
		if (solution == null) {
			return null;
		}
		int[] convertedSolution = new int[solution.size()];
		for (int i = 0; i < solution.size(); i++) {
			convertedSolution[i] = Utils.moveToShen(solution.get(i));
		}
		return convertedSolution;
	}
	
	public int getDiff() {
		return diff;
	}
	
	public int getNextStep() {
		return steps[currentStep++];
	}
	
	public int numTotalSteps() {
		return steps.length;
	}


	public void printSteps() {
		if (G6Player.DEBUG) {
			for (int i : steps) {
				System.out.print("Solution: ");
				System.out.print(Utils.shenToMove(i) + "\t");
				System.out.println("");
			}
		}
	}

}
