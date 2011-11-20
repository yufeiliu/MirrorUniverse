package mirroruniverse.g5.dfa;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

public class DFA<V> {
	
	// TODO - should use a different generic for the node value and for the
	// transitions...
	
	ArrayList<State<V>> goalStates;
	ArrayList<State<V>> states;
	State<V> startState;

	// TODO
	public DFA(int[][] firstMap) {
		this();
	}
	
	public DFA() {
		goalStates = new ArrayList<State<V>>();
		states = new ArrayList<State<V>>();
	}
	
	private State<V> addState(V value, boolean isGoal) {
		State<V> s = new State<V>(value, isGoal);
		addState(s);
		return s;
	}
	private State<V> addState(State<V> s) {
		if (s.isGoal()) {
			goalStates.add(s);
		}
		states.add(s);
		return s;
	}
	
	public ArrayList<V> findShortestPath() {
		HashMap<State<V>, State<V>> used = new HashMap<State<V>, State<V>>();
		Queue<State<V>> q = new LinkedList<State<V>>();
		q.add(startState);
		State<V> currentState;
		while(!q.isEmpty()) {
			currentState = q.poll();
			if (currentState.isGoal()) {
				return recoverPath(currentState, used);
			}
			for (Transition<V> t : currentState.getTransitions()) {
				State<V> nextState = t.getEnd();
				if (used.get(nextState) != null) {
					q.add(nextState);
				}
			}
		}
		return null;
	}
	
	private ArrayList<V> recoverPath(State<V> currentState,
			HashMap<State<V>, State<V>> used) {
		ArrayList<V> path = new ArrayList<V>();
		while (currentState != null) {
			path.add(currentState.getValue());
			currentState = used.get(currentState);
		}
		Collections.reverse(path);
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
	public static DFA<String> intersect(DFA<String> first, DFA<String> other) {
		DFA<String> intersection = new DFA<String>();
		HashMap<String, State<String>> newStates =
				new HashMap<String, State<String>>();
		for (State<String> selfState : first.states) {
			for (State<String> otherState : other.states) {
				String key = selfState.getValue() + ", " + otherState.getValue();
				State<String> s = new State<String>(key,
						selfState.isGoal() && otherState.isGoal());
				newStates.put(key, s);
				intersection.addState(s);
			}
		}
		
		HashMap<String, Transition<String>> firstTransitionValues =
				new HashMap<String, Transition<String>>();
		for (State<String> selfState : first.states) {
			for (Transition<String> t : selfState.getTransitions()) {
				firstTransitionValues.put(t.getValue(), t);
			}
		}
		for (State<String> otherState : other.states) {
			for (Transition<String> t : otherState.getTransitions()) {
				Transition<String> firstTransition =
						firstTransitionValues.get(t.getValue());
				if (firstTransition != null) {
					State<String> start = newStates.get(
							firstTransition.getStart().getValue() + ", " +
							t.getStart().getValue());
					State<String> end= newStates.get(
							firstTransition.getEnd().getValue() + ", " +
							t.getEnd().getValue());
					start.addTransition(t.getValue(), end);
				}
			}
		}
		return intersection;
	}
	
	public boolean hasNonEmptyLanguage() {
		return !goalStates.isEmpty();
	}
	
	/*
	 * Returns a list of DFAs with the goals shifted one back.
	 */
	public ArrayList<DFA<V>> shiftGoals() {
		// TODO
		return null;
	}

}
