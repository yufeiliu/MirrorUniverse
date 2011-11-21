package mirroruniverse.g5.dfa;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;

import mirroruniverse.g5.Utils.*;
import mirroruniverse.g5.dfa.*;

public class TestDFA {

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
		assertEquals(solution.get(0), Move.N);
	}
	
	@Test
	public void testRecoverPath() {
		DFA<Entity, Move> dfa = new DFA<Entity, Move>();
		State<Entity, Move> state1 = new State<Entity, Move>(Entity.PLAYER, false);
		State<Entity, Move> state2 = new State<Entity, Move>(Entity.EXIT, true);
		state1.addTransition(new Transition<Entity, Move>(Move.N, state1, state2));
		dfa.addStartState(state1);
		dfa.addState(state2);
		
		HashMap<State<Entity, Move>, Transition<Entity, Move>> used = 
				new HashMap<State<Entity, Move>, Transition<Entity, Move>>();
		used.put(state2, state1.getTransitions().get(0));
		
		ArrayList<Move> path = dfa.recoverPath(state2, used);
		assertNotNull(path);
		assertEquals(path.get(0), Move.N);
	}
	
	@Test
	public void testConstructorWithMap() {
		int[][] map1 = new int[][] {{3, 0}, {0, 2}};
		DFA<Entity, Move> dfa = new DFA<Entity, Move>(map1);
		ArrayList<Move> solution = dfa.findShortestPath();
		assertNotNull(solution.get(0));
		assertEquals(solution.size(), 1);
		assertEquals(solution.get(0), Move.SE);
	}

}
