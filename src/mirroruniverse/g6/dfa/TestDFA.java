package mirroruniverse.g6.dfa;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;

import mirroruniverse.g6.Utils.Entity;
import mirroruniverse.g6.Utils.Move;

public class TestDFA {

	private State<Entity, Move> endState;
	private DFA<Entity, Move> dfa;
	private State<Entity, Move> startState;

	@Before
	public void setUp() throws Exception {
		dfa = new DFA<Entity, Move>();
		startState = new State<Entity, Move>(Entity.PLAYER, false);
		endState = new State<Entity, Move>(Entity.EXIT, true);
	}

	@Test
	public void testFindShortestPathOnTwoCell() {
		startState.addTransition(Move.N, endState);
		dfa.addStartState(startState);
		dfa.addState(endState);
		ArrayList<Move> solution = dfa.findShortestPath();
		assertEquals(solution.get(0), Move.N);
	}
	
	@Test
	public void testShortestPathOnLong() {
		startState.addTransition(
				new Transition<Entity, Move>(Move.N, startState, endState));
		dfa.addStartState(startState);
		dfa.addState(endState);
		State<Entity, Move> previousState = startState;
		for (int i = 0; i < 10; i++) {
			State<Entity, Move> newState = new State<Entity, Move>(Entity.SPACE);
			dfa.addState(newState);
			previousState.addTransition(Move.N, newState);
			previousState = newState;
		}
		previousState.addTransition(Move.N, endState);
		ArrayList<Move> solution = dfa.findShortestPath();
		assertEquals(10, solution.size());
		for (int i = 0; i < solution.size(); i++) {
			assertEquals(solution.get(i), Move.N);
		}
	}
	
	@Test
	public void testRecoverPath() {
		startState.addTransition(new Transition<Entity, Move>(Move.N, startState, endState));
		dfa.addStartState(startState);
		dfa.addState(endState);
		
		HashMap<State<Entity, Move>, Transition<Entity, Move>> used = 
				new HashMap<State<Entity, Move>, Transition<Entity, Move>>();
		used.put(endState, startState.getTransitions().get(0));
		
		ArrayList<Move> path = dfa.recoverPath(endState, used);
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
	
	@Test
	public void testIntersect() {
		fail("Sid, please write testIntersect.");
	}
}
