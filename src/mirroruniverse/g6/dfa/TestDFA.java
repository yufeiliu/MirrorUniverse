package mirroruniverse.g6.dfa;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.HashMap;

import mirroruniverse.g6.Utils.Entity;
import mirroruniverse.g6.Utils.Move;

import org.junit.Before;
import org.junit.Test;

public class TestDFA {

	private State endState;
	private DFA dfa;
	private State startState;
	
	private State otherEndState;
	private DFA otherDfa;
	private State otherStartState;

	@Before
	public void setUp() throws Exception {
		dfa = new DFA();
		startState = new State(Entity.PLAYER, false);
		endState = new State(Entity.EXIT, true);
		dfa.addStartState(startState);
		dfa.addState(endState);
	}
	
	public void setUpOtherDFA() {
		otherDfa = new DFA();
		otherStartState = new State(Entity.PLAYER, false);
		otherEndState = new State(Entity.EXIT, true);
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
			State startingPoint,
			State end) {
		for (int i = 0; i < num; i++) {
			State newState = new State(Entity.SPACE);
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
		State beforeEnd = new State(Entity.OBSTACLE);
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
		// Use E since that's indexed as 0 by Shen
		startState.addTransition(new Transition(Move.E,
				startState, endState));
		HashMap<State, Transition> used = 
				new HashMap<State, Transition>();
		used.put(endState, startState.getTransitions()[0]);
		ArrayList<Move> path = dfa.recoverPath(endState, used);
		assertNotNull(path);
		assertEquals(1, path.size());
		assertEquals(Move.E, path.get(0));
	}
	
	@Test
	public void testConstructorWithBigMaps() {
		int[][] map = new int[][] {{1, 1, 0, 0, 2}, {1, 1, 0, 1, 0}, {1, 1, 3, 0, 0}, {1, 1, 1, 1, 1}, {1, 1, 1, 1, 1}};
		DFA dfa = new DFA(map);
//		System.out.println(dfa);
//		int[][] map2 = new int[][] {{2, 0, 0, 1, 1}, {0, 1, 0, 1, 1}, {0, 0, 3, 1, 1}, {1, 1, 1, 1, 1}, {1, 1, 1, 1, 1}};
//		DFA dfa2 = new DFA(map2);
		ArrayList<Move> solution = dfa.findShortestPath();
		assertNotNull(solution.get(0));
	}
	
	@Test
	public void testConstructorWithMap() {
		int[][] map = new int[][] {{3, 0}, {0, 2}};
		DFA dfa = new DFA(map);
		ArrayList<Move> solution = dfa.findShortestPath();
		assertEquals(solution.size(), 1);
		assertEquals(solution.get(0), Move.SE);
	}
	
	@Test
	public void testIntersectOnTwoLengthPaths() {
		setUpOtherDFA();
		startState.addTransition(Move.N, endState);
		otherStartState.addTransition(Move.N, otherEndState);
		DFA intersection = DFA.intersect(dfa, otherDfa);
		ArrayList<Move> solution = intersection.findShortestPath();
		assertNotNull(solution);
		assertEquals(1, solution.size());
		assertEquals(Move.N, solution.get(0));
	}
	
	@Test
	public void testIntersectOnTwoAndThreeLengthPaths() {
		setUpOtherDFA();
		startState.addTransition(Move.N, endState);
		State noiseState = new State(Entity.SPACE);
		otherDfa.addState(noiseState);
		otherStartState.addTransition(Move.S, noiseState);
		otherStartState.addTransition(Move.N, otherEndState);
		DFA intersection = DFA.intersect(dfa, otherDfa);
		ArrayList<Move> solution = intersection.findShortestPath();
		assertNotNull(solution);
		assertEquals(1, solution.size());
		assertEquals(Move.N, solution.get(0));
	}
	
	@Test
	public void testIntersectOnNonEquivalentGraphs() {
		setUpOtherDFA();
		ArrayList<State> firstDfaStates =
				new ArrayList<State>();
		ArrayList<State> otherDfaStates =
				new ArrayList<State>();
		
		firstDfaStates.add(new State(Entity.SPACE));
		firstDfaStates.add(new State(Entity.SPACE));
		
		otherDfaStates.add(new State(Entity.SPACE));
		otherDfaStates.add(new State(Entity.SPACE));
		otherDfaStates.add(new State(Entity.SPACE));
		
		for (State s : firstDfaStates) {
			dfa.addState(s);
		}
		
		for (State s : otherDfaStates) {
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
		DFA intersection = DFA.intersect(dfa, otherDfa);
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
		DFA intersection = DFA.intersect(dfa, otherDfa);
		ArrayList<Move> solution = intersection.findShortestPath();
		assertEquals(solution, null);
	}
	
	// TODO - test for optimal > 0
	
}
