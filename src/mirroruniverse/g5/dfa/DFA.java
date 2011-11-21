package mirroruniverse.g5.dfa;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

import mirroruniverse.g5.Utils.Entity;
import mirroruniverse.g5.Utils.Move;

public class DFA<V, T> {
	
	// TODO - should use a different generic for the node value and for the
	// transitions...
	
	ArrayList<State<V, T>> goalStates;
	ArrayList<State<V, T>> states;
	State<V, T> startState;

	// TODO
	// Yufei: why is the int array used? I thought we defined the entities enum
	public DFA(int[][] firstMap) {
		this();
		
		int x_cap = firstMap.length;
		int y_cap = firstMap[0].length;
		
		for (int x = 0; x < x_cap; x++) {
			for (int y = 0; y < y_cap; y++) {

				State<String> node = new State<String>("Value", firstMap[x][y]==2);
				
				for (int dx = -1; dx <= 1; dx+=2) {
					
					for (int dy = -1; dy <= 1; dy+=2) {
						
						int new_x = x + dx, new_y = y + dy;
						
						//If out of bound, transition onto self
						if (new_x >= x_cap || new_x < 0 || new_y >= y_cap || new_y < 0) {
							
						}
					}
				}
				
			}
		}
		
	}
	
	public DFA() {
		goalStates = new ArrayList<State<V, T>>();
		states = new ArrayList<State<V, T>>();
	}
	
	private State<V, T> addState(V value, boolean isGoal) {
		State<V, T> s = new State<V, T>(value, isGoal);
		addState(s);
		return s;
	}
	private State<V, T> addState(State<V, T> s) {
		if (s.isGoal()) {
			goalStates.add(s);
		}
		states.add(s);
		return s;
	}
	
	public ArrayList<V> findShortestPath() {
		HashMap<State<V, T>, State<V, T>> used =
				new HashMap<State<V, T>, State<V, T>>();
		Queue<State<V, T>> q = new LinkedList<State<V, T>>();
		q.add(startState);
		State<V, T> currentState;
		while(!q.isEmpty()) {
			currentState = q.poll();
			if (currentState.isGoal()) {
				return recoverPath(currentState, used);
			}
			for (Transition<V, T> t : currentState.getTransitions()) {
				State<V, T> nextState = t.getEnd();
				if (used.get(nextState) != null) {
					q.add(nextState);
				}
			}
		}
		return null;
	}
	
	private ArrayList<V> recoverPath(State<V, T> currentState,
			HashMap<State<V, T>, State<V, T>> used) {
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
	public static DFA<Entity, Move> intersect(
			DFA<Entity, Move> first, DFA<Entity, Move> other) {
		DFA<Entity, Move> intersection = new DFA<Entity, Move>();
		
		HashMap<String, State<Entity, Move>> newStates =
				new HashMap<String, State<Entity, Move>>();
		for (State<Entity, Move> selfState : first.states) {
			for (State<Entity, Move> otherState : other.states) {
				if ((selfState.isGoal() || !otherState.isGoal()) ||
						(!selfState.isGoal() || otherState.isGoal())) {
					continue;
				}
				String key = selfState.getValue() + ", " + otherState.getValue();
				// TODO - does this matter?
				Entity e = selfState.getValue();
				State<Entity, Move> s = new State<Entity, Move>(
						selfState.getValue(),
						selfState.isGoal() && otherState.isGoal(), key);
				newStates.put(key, s);
				intersection.addState(s);
			}
		}
		
		HashMap<String, Transition<Entity, Move>> firstTransitionValues =
				new HashMap<String, Transition<Entity, Move>>();
		for (State<Entity, Move> selfState : first.states) {
			for (Transition<Entity, Move> t : selfState.getTransitions()) {
				firstTransitionValues.put(t.getId(), t);
			}
		}
		for (State<Entity, Move> otherState : other.states) {
			for (Transition<Entity, Move> t : otherState.getTransitions()) {
				Transition<Entity, Move> firstTransition =
						firstTransitionValues.get(t.getValue());
				if (firstTransition != null) {
					State<Entity, Move> start = newStates.get(
							firstTransition.getStart().getValue() + ", " +
							t.getStart().getValue());
					State<Entity, Move> end= newStates.get(
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
	public ArrayList<DFA<V, T>> shiftGoals() {
		// TODO
		return null;
	}

}
