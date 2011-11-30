package mirroruniverse.g6.dfa;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

import mirroruniverse.g6.G6Player;
import mirroruniverse.g6.Utils;
import mirroruniverse.g6.Utils.Entity;
import mirroruniverse.g6.Utils.Move;

public class DFA {
	
	ArrayList<State> goalStates;
	ArrayList<State> states;
	State startState;

	public DFA(int[][] map) {
		this();
		HashMap<String, State> allStates =
				new HashMap<String, State>(); 
		int xCap = map.length;
		int yCap = map[0].length;
		
		addStates(map, allStates, xCap, yCap);

		for (int x = 0; x < xCap; x++) {
			for (int y = 0; y < yCap; y++) {
				if (allStates.containsKey(makeKey(x, y))) {
					State node = allStates.get(makeKey(x, y));
					addTransitions(map, allStates, x, y, node);
				}
			}
		}
	}

	private void addStates(int[][] map,
			HashMap<String, State> allStates, int xCap, int yCap) {
		for (int x = 0; x < xCap; x++) {
			for (int y = 0; y < yCap; y++) {
				if (map[x][y] == Utils.entitiesToShen(Entity.OBSTACLE))
					continue;
				
				Entity entity = Utils.shenToEntities(map[x][y]);
				boolean isStart = (entity == Entity.PLAYER);
				boolean isGoal = (entity == Entity.EXIT);
				boolean isKnown = (entity != Entity.UNKNOWN);
				if (isKnown) {
					State node = new State(entity, isGoal);
					allStates.put(makeKey(x, y), node);
					if (isStart) {
						startState = (State) node;
					}
					if (node.isGoal()) {
						goalStates.add((State) node);
					}
					states.add((State) node);
				}
			}
		}
	}

	private void addTransitions(int[][] map,
			HashMap<String, State> allStates,
			int x, int y, State node) {
		for (int dx = -1; dx <= 1; dx++) {
			for (int dy = -1; dy <= 1; dy++) {
				if (dx == 0 && dy == 0) {
					continue;
				}
				String key = makeKey(x+dy, y+dx);
				if (!allStates.containsKey(key)) {
					node.addTransition(Utils.dxdyToMove(dx, dy), node);
				} else {
					State neighbor = allStates.get(key);
					node.addTransition(Utils.dxdyToMove(dx, dy), neighbor);
				}
			}
		}
	}
	
	private String makeKey(int x, int y) {
		return x + "," + y;
	}

	public DFA() {
		goalStates = new ArrayList<State>();
		states = new ArrayList<State>();
	}
	
	public State addStartState(State s) {
		startState = s;
		addState(s);
		return s;
	}
	
	public State addState(State s) {
		if (s.isGoal()) {
			goalStates.add(s);
		}
		states.add(s);
		return s;
	}
	
	public ArrayList<Move> findShortestPath() {
		HashMap<State, Transition> used =
				new HashMap<State, Transition>();
		Queue<State> q = new LinkedList<State>();
		used.put(startState, null);
		q.add(startState);
		State currentState;
		while(!q.isEmpty()) {
			currentState = q.poll();
			if (currentState.isGoal()) {
				return recoverPath(currentState, used);
			}
			for (Transition t : currentState.getTransitions()) {				
				// self-transitions should not be part of shortest path
				if(t.getStart().equals(t.getEnd())) {
					continue;
				}
				State nextState = t.getEnd();
				if (!used.containsKey(nextState)) {
					used.put(nextState, t);
					q.add(nextState);
				}
			}
		}
		return null;
	}
	
	protected ArrayList<Move> recoverPath(State currentState,
			HashMap<State, Transition> used) {
		ArrayList<Move> path = new ArrayList<Move>();
		ArrayList<Transition> transPath = new ArrayList<Transition>();
		
		Transition trans = used.get(currentState);
		while (trans != null) {
			path.add(trans.getValue());
			currentState = trans.getStart();
			transPath.add(trans);
			trans = used.get(currentState);
		}
		Collections.reverse(path);
		Collections.reverse(transPath);
		if (G6Player.SID_DEBUG) {
			System.out.println(transPath);
		}
		return path;
	}

	/*
	 * Creates a DFA with new states, one for every pair of states in the two
	 * original DFAs. A state is a goal state in the new DFA if and only if 
	 * both states in the pair were goal states.
	 * 
	 * There is a transition from state (A, B) to (C, D) on symbol x if and
	 * only if there was a transition on x from A to C and a transition x from
	 * C to D.
	 * 
	 * The result is a DFA that accepts the intersection of the two original
	 * DFA.
	 */
	public static DFA intersect(
			DFA first, DFA other) {
		DFA intersection = new DFA();
		HashMap<String, State> newStates =
				new HashMap<String, State>();
		
		long start = System.currentTimeMillis();
		long stateEnd;
		long transEnd;
		
		addIntersectionStates(first, other, intersection, newStates);
		stateEnd = System.currentTimeMillis();
		addIntersectionTransitions(first, other, newStates);
		transEnd = System.currentTimeMillis();
		
		if (G6Player.DEBUG) {
			System.out.println("Intersection: " + (stateEnd - start));
			System.out.println("Transitions: " + (transEnd - stateEnd));
		}
		
		if (G6Player.SID_DEBUG) {
			System.out.println(other);
		}
		
		return intersection;
	}

	private static void addIntersectionStates(DFA first,
			DFA other, DFA intersection,
			HashMap<String, State> newStates) {
		for (State selfState : first.states) {
			for (State otherState : other.states) {
				// Don't accidentally step on an exit - these states cannot
				// be part of our solution
				// TODO - handle cases where these aren't real exits
				if ((selfState.isGoal() && !otherState.isGoal() &&
						selfState != first.startState) ||
							(!selfState.isGoal() && otherState.isGoal() &&
							otherState != other.startState)) {
					continue;
				}
				String key = makeKey(selfState, otherState);
				// This value 
				Entity e = selfState.getValue();
				State s = new State(
						e,
						selfState.isGoal() && otherState.isGoal(), key);
				newStates.put(key, s);
				
				// Add the state to the DFA
				if (selfState == first.startState && otherState == other.startState) {
					intersection.addStartState(s);
				} else {
					intersection.addState(s);
				}
			}
		}
	}

	private static void addIntersectionTransitions(DFA first,
			DFA other,
			HashMap<String, State> newStates) {
		for (State selfState : first.states) {
			for (State otherState : other.states) {
				String startKey = makeKey(selfState, otherState);
				State source = newStates.get(startKey);
				// null if it's an exit for one of them
				if (source == null) {
					continue;
				}
				// TODO - hash transitions by value. That would make this loop
				// O(T) instead of O(T^2); T is always 9 for this project
				for (int i = 0; i < State.NUM_DIRECTIONS; i++) {
					Transition selfTrans = selfState.getTransition(i);
					Transition otherTrans = otherState.getTransition(i);
					if (selfTrans == null || otherTrans == null) {
						continue;
					}
					Move m = selfTrans.getValue();
					if (otherTrans.getValue() != m) {
						System.err.println("Oopps");
						continue;
					};
					String endKey = makeKey(
							selfTrans.getEnd(), otherTrans.getEnd());
					State dest = newStates.get(endKey);
					// dest is null if it would have one exit
					if (dest != null) {
						source.addTransition(m, dest);
					}
				}
			}
		}
	}

	private static String makeKey(State selfState, State otherState) {
		String key = selfState.getValue() + selfState.getId() + "; " +
				otherState.getValue() + otherState.getId();
		return key;
	}
	
	public boolean hasNonEmptyLanguage() {
		return !goalStates.isEmpty();
	}
	
	/*
	 * Returns a list of DFAs with the goals shifted one back.
	 */
	public DFA shiftGoals() {
		DFA other = new DFA();
		HashMap<State, State> copiedStates =
				new HashMap<State, State>();

		for (State s : states) {
			boolean isStart = (s == startState);			
			State newS = new State(s.getValue(), false);
			copiedStates.put(s, newS);
			if (isStart) {
				other.addStartState(newS);
			} else {
				other.addState(newS);
			}
		}
		
		for (State s : states) {
			State newS = copiedStates.get(s);
			for (Transition t : s.getTransitions()) {
				State dest = copiedStates.get(t.getEnd());
				newS.addTransition(t.getValue(), dest);
				// can probably add !s.isGoal() for efficiency
				if (!s.isGoal() && t.getEnd().isGoal() && !newS.isGoal()) {
					newS.setGoal(true);
					other.goalStates.add(newS);
				}
			}
		}
		return other;
	}
	
	public String toString() {
		String s = "";
		s += "=====START STATE=====\n";
		s += startState + "\n";
		s += "\n=====GOAL STATES=====\n";
		for (State state : goalStates) {
			s += state + "\n";
		}
		s += "\n=====ALL STATES=====\n";
		for (State state : states) {
			s += state + "\n";
			for (Transition t : state.getTransitions()) {
				s += "\t" + t;
			}
		}
		return s;
	}
	
	public State getStartState() {
		return startState;
	}

}
