package mirroruniverse.g5.dfa;

public class Transition<V> {
	
	private State<V> end;
	
	public Transition(State<V> end) {
		this.end = end;
	}

	public State<V> getEnd() {
		return end;
	}
	
}
