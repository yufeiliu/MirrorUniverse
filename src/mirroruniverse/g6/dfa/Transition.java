package mirroruniverse.g6.dfa;

import mirroruniverse.g6.Utils.Move;

public class Transition {
	
	protected State end;
	protected Move value;
	protected State start;
	private String id;
	
	private static short idCounter = 0;
	
	public Transition(Move value, State start, State end) {
		this.end = end;
		this.start = start;
		this.value = value;
		this.id = String.valueOf(idCounter++);
	}
	
	public Move getValue() {
		return value;
	}
	
	public State getStart() {
		return start;
	}

	public State getEnd() {
		return end;
	}
	
	public String getId() {
		return id;
	}
	
	public String toString() {
		return "Go " + value + " from (" + start + ") to (" + end + ")\n";
	}
	
}
