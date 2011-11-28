package mirroruniverse.g6;

import mirroruniverse.sim.MUMap;

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
	
	public static Move shenToMove(int m) {
		switch(m) {
		case 1:
			return Move.E;
		case 2:
			return Move.NE;
		case 3:
			return Move.N;
		case 4:
			return Move.NW;
		case 5:
			return Move.W;
		case 6:
			return Move.SW;
		case 7:
			return Move.S;
		case 8:
			return Move.SE;
		default:
			return Move.E;
		}
	}
	
	public static Move dxdyToMove(int dx, int dy) {
		int counter = 0;
		for (int[] elem : MUMap.aintDToM) {
			if (elem[0]==dx && elem[1]==dy) {
				return Utils.shenToMove(counter); 
			}
			counter++;
		}
		return null;
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
	
	public static Entity shenToEntities(int e) {
		switch(e) {
		case 0:
			return Entity.SPACE;
		case 1:
			return Entity.OBSTACLE;
		case 3:
			return Entity.PLAYER;
		case 2:
			return Entity.EXIT;
		case -1:
			return Entity.UNKNOWN;
		default:
			return Entity.SPACE;
		}
	}
	
	public static enum Entity {
		PLAYER, EXIT, OBSTACLE, SPACE, UNKNOWN;
	}

	public static void print2DArray(int[][] arr) {
		for (int i = 0; i < arr.length; i++) {
			for (int j = 0; j < arr[0].length; j++) {
				System.out.print(arr[i][j] + " ");
			}
			System.out.println();
		}
	}
}
