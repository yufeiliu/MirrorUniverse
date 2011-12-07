package mirroruniverse.g6;

import static org.junit.Assert.*;

import java.util.ArrayList;

import mirroruniverse.g6.Utils.Move;

import org.junit.Test;

public class SolutionTest {

	//example move
	ArrayList<Move> moves = new ArrayList<Move>();
	int diff;
	
	public void initVars() {
		//initialize
		for(int i = 0; i < 3; i++) {
			moves.add(Move.E);
			moves.add(Move.N);
		}
		//moves is E, N, E, N, E, N
		
		diff = 0;
		
	}

	@Test
	public void testMovesToInts() {
		initVars();
		//make sure its set correctly
		@SuppressWarnings("unused")
		Solution tester = new Solution(moves, diff, false);
		assertNotNull("moves to shen results:", Solution.movesToInts(moves));
		
	}

	@Test
	public void testGetDiff() {
		initVars();
		Solution tester = new Solution(moves, diff, false);
		assertEquals("get diff:", 0, tester.getDiff());
	}

	@Test
	public void testGetNextStep() {
		initVars();
		Solution tester = new Solution(moves, diff, false);
		assertNotNull("get next step:", tester.getNextStep());
	}

	@Test
	public void testNumTotalSteps() {
		initVars();
		Solution tester = new Solution(moves, diff, false);
		assertEquals("get total steps:", 6, tester.numTotalSteps());
	}

	@Test
	public void testIsCompleted() {
		initVars();
		Solution tester = new Solution(moves, diff, false);
		assertNotNull("isCompleted:", tester.isCompleted());
	}

	@Test
	public void testToString() {
		initVars();
		Solution tester = new Solution(moves, diff, false);
		assertNotNull("path to string:", tester.toString());
	}

}
