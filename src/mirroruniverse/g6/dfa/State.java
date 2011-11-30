package mirroruniverse.g6.dfa;

import java.util.ArrayList;

import mirroruniverse.g6.Utils;
import mirroruniverse.g6.Utils.Move;

public class State<V, T> {
	
	private V value;
	private ArrayList<Transition<V, T>> transitions;
	private boolean goal;
	private String id;
	
	private Transition<V, T>[] transList;
	public final static int NUM_DIRECTIONS = 8;
	
	private static int idCounter = 0; 
	
	public State(V value) {
		this(value, false);
	}
	
	public State(V value, boolean goal) {
		this(value, goal, String.valueOf(idCounter++));
	}
	
	@SuppressWarnings("unchecked")
	public State(V value, boolean goal, String id) {
		this.value = value;
		this.goal = goal;
		this.id = id;
		this.transitions = new ArrayList<Transition<V, T>>();
		this.transList = new Transition[NUM_DIRECTIONS];
	}

	public void addTransition(Transition<V, T> trans) {
		@SuppressWarnings("unchecked")
		Transition<V, Move> typedTrans = (Transition<V, Move>) trans;
		int index = Utils.moveToShen(typedTrans.getValue()) - 1;
		transList[index] = trans;
		transitions.add(trans);
	}
	
	public void addTransition(T value, State<V, T> end) {
		addTransition(new Transition<V, T>(value, this, end));
	}
	
	// NOTE - this value is not meaningful for intersected DFAs
	public V getValue() {
		return value;
	}
	
	public boolean isGoal() {
		return goal;
	}
	
	public String getId() {
		return this.id;
	}

	public ArrayList<Transition<V, T>> getTransitions() {
		return transitions;
	}
	
	public String toString() {
		return this.id + " " + this.goal + " " + this.value;
	}
	
	public void setGoal(boolean goal) {
		this.goal = goal;
	}
	
	public Transition<V, T> getTransition(int i) {
		return transList[i];
	}
	
	public Transition<V, T>[] getTransList() {
		return transList;
	}
	
}
