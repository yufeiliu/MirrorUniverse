package mirroruniverse.g6.dfa;

import mirroruniverse.g6.Utils;
import mirroruniverse.g6.Utils.Entity;
import mirroruniverse.g6.Utils.Move;

public class State implements Comparable<State> {

	private Transition[] transList;

	private Entity value;
	private boolean goal;
	private String id;
	private boolean banned;
		
	public final static byte NUM_DIRECTIONS = 8;
	private static long idCounter = 0; 
	
	public State(Entity value) {
		this(value, false);
	}
	
	public State(Entity value, boolean goal) {
		this(value, goal, String.valueOf(idCounter++));
	}
	
	public State(Entity value, boolean goal, String id) {
		this.value = value;
		this.goal = goal;
		this.id = id;
		this.transList = new Transition[NUM_DIRECTIONS];
	}

	public void addTransition(Transition trans) {
		int index = Utils.moveToShen(trans.getValue()) - 1;
		transList[index] = trans;
	}
	
	public void addTransition(Move value, State end) {
		addTransition(new Transition(value, this, end));
	}
	
	// NOTE - this value is not meaningful for intersected DFAs
	public Entity getValue() {
		return value;
	}
	
	public boolean isGoal() {
		return goal;
	}
	
	public String getId() {
		return this.id;
	}
	
	// Can have null elements.
	public Transition[] getTransitions() {
		return transList;
	}
	
	public String toString() {
		return this.id + " " + this.goal + " " + this.value;
	}
	
	public void setGoal(boolean goal) {
		this.goal = goal;
	}
	
	public int hashCode() {
		return id.hashCode();
	}

	@Override
	public int compareTo(State other) {
		return id.compareTo(other.id);
	}
	
	public boolean isBanned() {
		return banned;
	}
	
	public void setBanned(boolean banned) {
		this.banned = banned;
	}
	
}
