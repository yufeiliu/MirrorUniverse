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
	
	public static Move reverseMove(Move m) {
		switch(m) {
			case E:
				return Move.W;
			case NE:
				return Move.SW;
			case N:
				return Move.S;
			case NW:
				return Move.SE;
			case W:
				return Move.E;
			case SW:
				return Move.NE;
			case S:
				return Move.N;
			case SE:
				return Move.NW;
			default:
				return null;
		}
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
	
	public static int distFromExit(int[][] map) {
		int playerX = 0, playerY = 0, exitX = 0, exitY = 0;
		for (int i = 0; i < map.length; i++) {
			for (int j = 0; j < map[0].length; j++) {
				if (map[i][j] == Utils.entitiesToShen(Entity.EXIT)) {
					exitX = j;
					exitY = i;
				} else if (map[i][j] == Utils.entitiesToShen(Entity.PLAYER)) {
					playerX = j;
					playerY = i;
				}
			}
		}
		int deltaX = Math.abs(playerX - exitX);
		int deltaY = Math.abs(playerY - exitY);
		return Math.max(deltaX, deltaY);
	}
	
	
	
	/*
	 * Given a map, returns a new map that has at most maxEntities players +
	 * exits + spaces + obstacles. It should not affect the number of players
	 * and exits. In other words, it replaces up some spaces and obstacles with
	 * unknowns.
	 * 
	 *  It should try to include the most relevant squares, so that the DFA
	 *  solution will be as accurate as possible.
	 *  
	 *  NOTE: DO NOT MUTATE THE ORIGINAL MAP.
	 */
	public static int[][] capMap(int[][] map, int maxEntities) {
		int[][] newMap = new int[map.length][map[0].length];
		for (int i = 0; i < map.length; i++) {
			for (int j = 0; j < map[0].length; j++) {
				if(countNeighbors(map, i, j, Entity.OBSTACLE) >= 7) {
					newMap[i][j] = entitiesToShen(Entity.UNKNOWN);
				} else if(countNeighbors(map, i, j, Entity.SPACE) == 8) {
					newMap[i][j] = entitiesToShen(Entity.UNKNOWN);
				} else {
					newMap[i][j] = map[i][j];
				}
			}
		}
		return newMap;
	}
	
	private static int countNeighbors(int[][] map, int a, int b, Entity ent) {
		int count = 0;
		for(int i = (a > 0) ? a - 1 : 0; i < ((a < map.length - 1) ? a + 1 : map.length); i++) {
			for(int j = (b > 0) ? b - 1 : 0; j < ((b < map[0].length - 1) ? b + 1 : map[0].length); j++) {
				if(map[i][j] == entitiesToShen(ent)) {
					count++;
				}
			}
		}
		return count;
	}
	
}
