package mirroruniverse.g5.test;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

import mirroruniverse.g5.Utils.*;
import mirroruniverse.g5.dfa.DFA;

public class DFATest {

	/*
	@Before
	public void setUp() throws Exception {
	
	}
	*/

	@Test
	public void testConstructor() {
		DFA<Entity, Move> dfa = new DFA<Entity, Move>();
		assertNotNull(dfa);
	}
	
	@Test
	public void testFindShortestPath() {
		int[][] map1 = new int[][] {{0, 0}, {0, 2}};
		DFA<Entity, Move> dfa = new DFA<Entity, Move>(map1);
		ArrayList<Move> solution = dfa.findShortestPath();
		assertNotNull(solution);
	}

}
