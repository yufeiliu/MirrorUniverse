package mirroruniverse.g6;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import mirroruniverse.g6.Utils.Entity;
import mirroruniverse.sim.MUMap;
import mirroruniverse.sim.Player;

public class G6Player implements Player {
	
	//knowledge of whats at each map
	private int[][] left = new int[200][200];
	private int[][] right = new int[200][200];
	
	
	
	private int x1 = 0;
	private int y1 = 0;
	private int x2 = 0;
	private int y2 = 0;
	
	
	private int r1 = -1;
	private int r2 = -1;
	

	private int[] solution;
	private Solver solver;
	private int solutionStep;

	public G6Player() {
		for (int i = 0; i < 200; i++) {
			for (int j = 0; j < 200; j++) {
				left[i][j]=-1;
				right[i][j]=-1;
			}
		}
		
		left[x1][y1]=0;
		right[x2][y2]=0;
		
		solution = null;
		solver = new DFASolver();
	}

	//exploration strategy 
	public int[] Explore(int[][] leftMap, int[][] rightMap) {
		
		
		return null;
	}
	
	@Override
	public int lookAndMove(int[][] leftView, int[][] rightView) {
		
		if (r1 == -1 || r2 == -1) {
			r1 = (leftView.length-1) / 2;
			r2 = (rightView.length-1) / 2;
		}
		
		updateKnowledge(left, x1, y1, leftView);
		updateKnowledge(right, x2, y2, rightView);
		
		
		int dir = 0;
		
		List<Pair<Integer, Integer>> possibilities = new ArrayList<Pair<Integer, Integer>>(); 
		
		//TODO: if no direction exists that uncovers squares, go to direction with most/least space
		//Loop over directions
		for (int i = 1; i <= 8; i++) {
			int[] curDir = MUMap.aintDToM[i];
			int dx = curDir[0];
			int dy = curDir[1];
			
			if (leftView[x1+dx][y1+dy] == Utils.entitiesToShen(Entity.SPACE) && 
					rightView[x2+dx][y2+dy] == Utils.entitiesToShen(Entity.SPACE)) {
				possibilities.add(new Pair<Integer, Integer>(
						squaresUncovered(x1 + dx, y1 + dy, r1, left) + 
						squaresUncovered(x2 + dx, y2 + dy, r2, right)
						, i));
			}
		}
		
		Collections.sort(possibilities);
		dir = possibilities.get(0).getBack();
		
		int[] dirArray = MUMap.aintDToM[dir];
		x1 += dirArray[0];
		x2 += dirArray[0];
		y1 += dirArray[1];
		y2 += dirArray[1];
		
		return dir;
		
		//TODO: add logic for re-recomputing solution upon uncovering more fogged area
		/*
		if (solution==null) {
			solution = solver.solve(left, right);
			solutionStep = 0;
		}
		
		return solution[solutionStep++];
		
		*/
	}
	
	private void updateKnowledge(int[][] knowledge, int x, int y, int[][] view) {
		for (int i = 0; i < view.length; i++) {
			for (int j = 0; j < view[0].length; j++) {
				knowledge[i- (view.length - 1)/2][j - (view[0].length-1)/2] = view[i][j];
			}
		}
	}
	
	private int squaresUncovered(int newX, int newY, int r, int[][] knowledge) {
		int counter = 0;
		
		for (int i = newX - r; i <= newX + r; i++) {
			for (int j = newY - r; j <= newY + r; j++) {
				counter += (knowledge[i][j] == -1 ? 1 : 0);
			}
		}
		
		return counter;
	}

}
