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
	
	public void addManyStates(Move m, int num,
			State<Entity, Move> startingPoint,
			State<Entity, Move> end) {
		for (int i = 0; i < num; i++) {
			State<Entity, Move> newState = new State<Entity, Move>(Entity.SPACE);
			dfa.addState(newState);
			startingPoint.addTransition(m, newState);
			startingPoint = newState;
		}
		startingPoint.addTransition(m, end);
	}

	/*
	 * Pick between two possible paths. 
	 */
	@Test
	public void testShortestPathOfTwo() {
		dfa.addStartState(startState);
		addManyStates(Move.N, 3, startState, endState);
		addManyStates(Move.S, 2, startState, endState);
		ArrayList<Move> solution = dfa.findShortestPath();
		assertEquals(3, solution.size());
		for (int i = 0; i < solution.size(); i++) {
			assertEquals(solution.get(i), Move.S);
		}
	}
	
	@Test
	public void testShortestPathOnImpossible() {
		dfa.addStartState(startState);
		State<Entity, Move> beforeEnd = new State<Entity, Move>(Entity.OBSTACLE);
		addManyStates(Move.N, 10, startState, beforeEnd);
		beforeEnd.addTransition(Move.N, beforeEnd);
		ArrayList<Move> solution = dfa.findShortestPath();
		assertNull(solution);
	}
	
	@Test
	public void testShortestPathOnLong() {
		dfa.addStartState(startState);
		dfa.addState(endState);
		addManyStates(Move.N, 10, startState, endState);
		ArrayList<Move> solution = dfa.findShortestPath();
		assertEquals(11, solution.size());
		for (int i = 0; i < solution.size(); i++) {
			assertEquals(solution.get(i), Move.N);
		}
	}
	
	/*
	 * We just do a basic sanity check. We don't test a bunch of cases since
	 * if recover path breaks, the shortest path test should let us know.
	 */
	@Test
	public void testRecoverPath() {
		startState.addTransition(new Transition<Entity, Move>(Move.N,
				startState, endState));
		dfa.addStartState(startState);
		dfa.addState(endState);
		
		HashMap<State<Entity, Move>, Transition<Entity, Move>> used = 
				new HashMap<State<Entity, Move>, Transition<Entity, Move>>();
		used.put(endState, startState.getTransitions().get(0));
		
		ArrayList<Move> path = dfa.recoverPath(endState, used);
		assertNotNull(path);
		assertEquals(1, path.size());
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
