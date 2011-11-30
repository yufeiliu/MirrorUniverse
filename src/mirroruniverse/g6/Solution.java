
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
		if (G6Player.SID_DEBUG) {
			System.out.println(this);
		}
		if (currentStep >= steps.length) {
			// This can occur in a valid case. Consider the case in which the
			// map is fully explored and the best solution is of a huge size.
			// We solve one map and then the other. After we've solved one map
			// and before we solve the other, there's an extra step.
			if (currentStep == steps.length) {
//				System.err.println("Invalid solution found.");
			}
			return -1;
		} else {
			return steps[currentStep++];
		}
	}
	
	public int numTotalSteps() {
		return steps.length;
	}

	public boolean isCompleted() {
		return steps.length == currentStep;
	}

	public String toString() {
		String s = "";
		s += "Solution (" + diff + "): ";
		for (int i = 0; i < steps.length; i++) {
			s += Utils.shenToMove(steps[i]);
			if (i == currentStep) {
				s += "*";
			}
			s += "\t";
		}
		s += "";
		return s;
	}

}
