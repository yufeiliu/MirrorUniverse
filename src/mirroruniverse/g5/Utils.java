package mirroruniverse.g5;

public class Utils {
	
	public static int moveToShen(Move m) {
		switch(m) {
			case E:
				return 1;
			case NE:
				return 2;
			case N:
				return 3;
			case NW:
				return 4;
			case W:
				return 5;
			case SW:
				return 6;
			case S:
				return 7;
			case SE:
				return 8;
			default:
				return -1;
		}
	}
	
	public static enum Move {
		E, NE, N, NW, W, SW, S, SE
	}
	
	public static int entitiesToShen(Entity e) {
		switch(e) {
			case SPACE:
				return 0;
			case OBSTACLE:
				return 1;
			case PLAYER:
				return 3;
			case EXIT:
				return 2;
			default:
				return -1;
		}
	}
	
	public static enum Entity {
		PLAYER, EXIT, OBSTACLE, SPACE, UNKNOWN;
	}

}
