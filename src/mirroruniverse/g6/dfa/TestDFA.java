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
	
	private State<Entity, Move> otherEndState;
	private DFA<Entity, Move> otherDfa;
	private State<Entity, Move> otherStartState;

	@Before
	public void setUp() throws Exception {
		dfa = new DFA<Entity, Move>();
		startState = new State<Entity, Move>(Entity.PLAYER, false);
		endState = new State<Entity, Move>(Entity.EXIT, true);
		dfa.addStartState(startState);
		dfa.addState(endState);
	}
	
	public void setUpOtherDFA() {
		otherDfa = new DFA<Entity, Move>();
		otherStartState = new State<Entity, Move>(Entity.PLAYER, false);
		otherEndState = new State<Entity, Move>(Entity.EXIT, true);
		otherDfa.addStartState(otherStartState);
		otherDfa.addState(otherEndState);
	}
	
	public void testConstructor() {
		
	}

	@Test
	public void testFindShortestPathOnTwoCell() {
		startState.addTransition(Move.N, endState);
		ArrayList<Move> solution = dfa.findShortestPath();
		assertEquals(solution.size(), 1);
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
		State<Entity, Move> beforeEnd = new State<Entity, Move>(Entity.OBSTACLE);
		addManyStates(Move.N, 10, startState, beforeEnd);
		beforeEnd.addTransition(Move.N, beforeEnd);
		ArrayList<Move> solution = dfa.findShortestPath();
		assertNull(solution);
	}
	
	@Test
	public void testShortestPathOnLong() {
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
		
		HashMap<State<Entity, Move>, Transition<Entity, Move>> used = 
				new HashMap<State<Entity, Move>, Transition<Entity, Move>>();
		used.put(endState, startState.getTransitions().get(0));
		
		ArrayList<Move> path = dfa.recoverPath(endState, used);
		assertNotNull(path);
		assertEquals(1, path.size());
		assertEquals(path.get(0), Move.N);
	}
	
	@Test
	public void testConstructorWithBigMaps() {
		int[][] map = new int[][] {{1, 1, 0, 0, 2}, {1, 1, 0, 1, 0}, {1, 1, 3, 0, 0}, {1, 1, 1, 1, 1}, {1, 1, 1, 1, 1}};
		DFA<Entity, Move> dfa = new DFA<Entity, Move>(map);
//		System.out.println(dfa);
//		int[][] map2 = new int[][] {{2, 0, 0, 1, 1}, {0, 1, 0, 1, 1}, {0, 0, 3, 1, 1}, {1, 1, 1, 1, 1}, {1, 1, 1, 1, 1}};
//		DFA<Entity, Move> dfa2 = new DFA<Entity, Move>(map2);
		ArrayList<Move> solution = dfa.findShortestPath();
		assertNotNull(solution.get(0));
	}
	
	@Test
	public void testConstructorWithMap() {
		int[][] map = new int[][] {{3, 0}, {0, 2}};
		DFA<Entity, Move> dfa = new DFA<Entity, Move>(map);
		ArrayList<Move> solution = dfa.findShortestPath();
		assertEquals(solution.size(), 1);
		assertEquals(solution.get(0), Move.SE);
	}
	
	@Test
	public void testIntersectOnTwoLengthPaths() {
		setUpOtherDFA();
		startState.addTransition(Move.N, endState);
		otherStartState.addTransition(Move.N, otherEndState);
		DFA<Entity, Move> intersection = DFA.intersect(dfa, otherDfa);
		ArrayList<Move> solution = intersection.findShortestPath();
		assertNotNull(solution);
		assertEquals(1, solution.size());
		assertEquals(Move.N, solution.get(0));
	}
	
	@Test
	public void testIntersectOnTwoAndThreeLengthPaths() {
		setUpOtherDFA();
		startState.addTransition(Move.N, endState);
		State<Entity, Move> noiseState = new State<Entity, Move>(Entity.SPACE);
		otherDfa.addState(noiseState);
		otherStartState.addTransition(Move.S, noiseState);
		otherStartState.addTransition(Move.N, otherEndState);
		DFA<Entity, Move> intersection = DFA.intersect(dfa, otherDfa);
		ArrayList<Move> solution = intersection.findShortestPath();
		assertNotNull(solution);
		assertEquals(1, solution.size());
		assertEquals(Move.N, solution.get(0));
	}
	
	@Test
	public void testIntersectOnNonEquivalentGraphs() {
		setUpOtherDFA();
		ArrayList<State<Entity, Move>> firstDfaStates =
				new ArrayList<State<Entity, Move>>();
		ArrayList<State<Entity, Move>> otherDfaStates =
				new ArrayList<State<Entity, Move>>();
		
		firstDfaStates.add(new State<Entity, Move>(Entity.SPACE));
		firstDfaStates.add(new State<Entity, Move>(Entity.SPACE));
		
		otherDfaStates.add(new State<Entity, Move>(Entity.SPACE));
		otherDfaStates.add(new State<Entity, Move>(Entity.SPACE));
		otherDfaStates.add(new State<Entity, Move>(Entity.SPACE));
		
		for (State<Entity, Move> s : firstDfaStates) {
			dfa.addState(s);
		}
		
		for (State<Entity, Move> s : otherDfaStates) {
			otherDfa.addState(s);
		}
		
		/*
		 * First graph has some shorter paths to exit, but it shouldn't be
		 * taken.
		 */
		startState.addTransition(Move.E, firstDfaStates.get(0));
		startState.addTransition(Move.W, endState);
		firstDfaStates.get(0).addTransition(Move.N, firstDfaStates.get(0));
		firstDfaStates.get(0).addTransition(Move.S, endState);
		firstDfaStates.get(0).addTransition(Move.E, firstDfaStates.get(1));
		firstDfaStates.get(1).addTransition(Move.E, endState);
		
		otherStartState.addTransition(Move.E, otherDfaStates.get(0));
		otherDfaStates.get(0).addTransition(Move.N, otherDfaStates.get(1));
		otherDfaStates.get(1).addTransition(Move.E, otherDfaStates.get(2));
		otherDfaStates.get(2).addTransition(Move.E, otherEndState);
		DFA<Entity, Move> intersection = DFA.intersect(dfa, otherDfa);
		ArrayList<Move> solution = intersection.findShortestPath();
		// E, N, E, E
		assertNotNull(solution);
		assertEquals(4, solution.size());
		assertEquals(Move.E, solution.get(0));
		assertEquals(Move.N, solution.get(1));
		assertEquals(Move.E, solution.get(2));
		assertEquals(Move.E, solution.get(3));
	}
	
	@Test
	public void testNoSolution() {
		setUpOtherDFA();
		startState.addTransition(Move.N, endState);
		otherStartState.addTransition(Move.S, otherEndState);
		DFA<Entity, Move> intersection = DFA.intersect(dfa, otherDfa);
		ArrayList<Move> solution = intersection.findShortestPath();
		assertEquals(solution, null);
	}
	
	// TODO - test for optimal > 0
	
}
