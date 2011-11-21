package mirroruniverse.g5.test;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

import mirroruniverse.g5.Utils.*;
import mirroruniverse.g5.dfa.*;

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
		DFA<Entity, Move> dfa = new DFA<Entity, Move>();
		State<Entity, Move> state1 = new State<Entity, Move>(Entity.PLAYER, false);
		State<Entity, Move> state2 = new State<Entity, Move>(Entity.EXIT, true);
		state1.addTransition(new Transition<Entity, Move>(Move.N, state1, state2));
		
		dfa.addStartState(state1);
		dfa.addState(state2);
		ArrayList<Move> solution = dfa.findShortestPath();
		assertNotNull(solution.get(0));
	}
	
	@Test
	public void testConstructorWithMap() {
		int[][] map1 = new int[][] {{3, 0}, {0, 2}};
		DFA<Entity, Move> dfa = new DFA<Entity, Move>(map1);
		ArrayList<Move> solution = dfa.findShortestPath();
		assertNotNull(solution.get(0));
	}

}
