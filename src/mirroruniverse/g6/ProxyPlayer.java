package mirroruniverse.g6;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Random;

import mirroruniverse.sim.MUMap;
import mirroruniverse.sim.MUMapConfig;
import mirroruniverse.sim.Player;

public class ProxyPlayer implements Player
{
	private MUMap myMap;
	
	//knowledge of whats at each map
	int[][] leftMapKnowledge;
	int[][] rightMapKnowledge;
	boolean didInitialize = false;
	boolean keepExploringLeft = true;
	boolean keepExploringRight = true;
	
	boolean leftMapExitFound = false;
	boolean rightMapExitFound = false;
	
	Process process;
	BufferedReader reader;
	BufferedWriter writer;
	
	
	
	public ProxyPlayer() {
		try {
			ProcessBuilder builder = new ProcessBuilder("ruby", "src/mirroruniverse/g5/player.rb");
			builder.redirectErrorStream(true);
			process = builder.start();
			OutputStream stdin = process.getOutputStream();
			InputStream stdout = process.getInputStream();
			reader = new BufferedReader(new InputStreamReader(stdout));
			writer = new BufferedWriter(new OutputStreamWriter(stdin));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static String serialize(int[][] doubleArray) {
		String out = "[";
		
		for (int i=0; i<doubleArray.length; i++) {
			out+="[";
			
			for (int j=0; j<doubleArray[i].length; j++) {
				out+=doubleArray[i][j]+(j==doubleArray[i].length-1 ? "" : ",");
			}
			out+="]" + (i==doubleArray.length-1 ? "" : ",");
		}
		out+="]";
		return out;
	}
	
	public void initializeMapKnowledge(int[][] lMap, int[][] rMap) {
		//initialize the left map knowledge to all -1;
		//num of rows
		for(int i = 0; i < lMap.length; i++) {
			System.out.println("rows: " + lMap.length);
			//num of cols
			for(int j = 0; j < lMap[0].length; j++) {
				System.out.println("cols: " + lMap[0].length);
				leftMapKnowledge[i][j] = -1;
			}
		}
		
		//initalize the right map knowledge to all -1
		for(int i = 0; i < rMap.length; i++) {
			for(int j = 0; j < rMap[0].length; j++) {
				rightMapKnowledge[i][j] = -1;
			}
		}
		
		didInitialize = false;
	}
	
	//exploration strategy 
	public int[] Explore(int[][] leftMap, int[][] rightMap) {
		
		//stores which position a player should explore next
		//left map is index 0, right map is index 1
		int[] whichSpaceToMove = new int[2];
		
		//initialize explored arrays if empty
		if(!didInitialize)
			initializeMapKnowledge(leftMap, rightMap);
		
		//get the players current position
		//need to update this so that we can store accurate information of the exact x y location of a map
		//at the moment it refreshes and sees the boxes immediately adjacent to it and chooses which location to go
		//int[] currentPosition = myMap.getLocation();
		
		//check which squares havent been seen
		//to see the immediate adjacent square
		int[][] leftLocalView = new int[ 3 ][ 3 ];
		int intMidL = leftMap.length / 2;
		for ( int i = -1; i <= 1; i ++ )
		{
			for ( int j = -1; j <= 1; j ++ )
			{
				leftLocalView[ 1 + j ][ 1 + i ] = leftMap[ intMidL + j ][ intMidL + i ];
				//take note of whats on the adjacency squares
				//leftMapKnowledge[1+j][1+i] = leftMap[intMidL+j][intMidL+i];
			}
		}
		
		//find best spot to explore left map
		boolean lExplored = false;
		//traverse what spaces have been unexplored and chose best pick
		for(int i = 0; i < leftMapKnowledge.length; i++) {
			for(int j = 0; j < leftMapKnowledge[0].length; j++) {
				if(leftMapKnowledge[i][j] == 0) {
					lExplored = true;
					whichSpaceToMove[0] = MUMap.aintMToD[ j + 1 ][ i + 1 ];
				}
					
			}
		}
		
		//to see the immediate adjacent square
		int[][] rightLocalView = new int[ 3 ][ 3 ];
		int intMidR = rightMap.length / 2;
		for ( int i = -1; i <= 1; i ++ )
		{
			for ( int j = -1; j <= 1; j ++ )
			{
				rightLocalView[ 1 + j ][ 1 + i ] = rightMap[ intMidR + j ][ intMidR + i ];
				//take note of whats on the adjacency squares
				rightMapKnowledge[1+j][1+i] = rightMap[intMidR+j][intMidR+i];
			}
		}
		
		//find best spot to move right map
		boolean rExplored = false;
		//traverse what spaces have been unexplored and chose best pick
		for(int i = 0; i < rightMapKnowledge.length; i++) {
			for(int j = 0; j < rightMapKnowledge[0].length; j++) {
				if(rightMapKnowledge[i][j] == 0) {
					rExplored = true;
					whichSpaceToMove[1] = MUMap.aintMToD[ j + 1 ][ i + 1 ];
				}
					
			}
		}
		
		int rightUnknownSpaces = 0;
		int leftUnknownSpaces = 0;
		//check to see if we explored almost all unknowned squares
		for(int i = 0; i < leftMapKnowledge.length; i ++) {
			for(int j = 0; j < leftMapKnowledge.length; j++) {
				if(leftMapKnowledge[i][j] == -1)
					leftUnknownSpaces++;
			}
		}
		
		//if there is less than 20% left of unknown knowledge spaces than stop exploring
		//need to update this as this is not optimal
		if(leftUnknownSpaces <= (leftMap.length + leftMap[0].length) * .2)
			keepExploringLeft = false;
		
		for(int i = 0; i < rightMapKnowledge.length; i ++) {
			for(int j = 0; j < rightMapKnowledge.length; j++) {
				if(rightMapKnowledge[i][j] == -1) 
					rightUnknownSpaces++;
			}
		}
	
		if(rightUnknownSpaces <= (rightMap.length + rightMap[0].length) * .2)
			keepExploringRight = false;
		
		return whichSpaceToMove;
		
	}
	
	
	public int lookAndMove( int[][] aintViewL, int[][] aintViewR ) {
		int out = -1;
		
		int[] whichSpaceToMove = new int[2];
		
//		//run the explore strategy first
//		if(keepExploringRight || keepExploringLeft) {
//			whichSpaceToMove = Explore(aintViewL, aintViewR);
//			//return move for the left map
//			return whichSpaceToMove[0]; //need to fix this logic
//		}

		//to see the immediate adjacent square
		int[][] leftLocalView = new int[ 3 ][ 3 ];
		int intMid = aintViewL.length / 2;
		for ( int i = -1; i <= 1; i ++ )
		{
			for ( int j = -1; j <= 1; j ++ )
			{
				leftLocalView[ 1 + j ][ 1 + i ] = aintViewL[ intMid + j ][ intMid + i ];
				if ( leftLocalView[ 1 + j ][ 1 + i ] == 2 )
				{
					if ( i == 0 && j == 0 )
						continue;
					leftMapExitFound = true;
					return MUMap.aintMToD[ j + 1 ][ i + 1 ];
				}
			}
		}
		
		intMid = aintViewR.length / 2;
		int[][] rightLocalView = new int[ 3 ][ 3 ];
		for ( int i = -1; i <= 1; i ++ )
		{
			for ( int j = -1; j <= 1; j ++ )
			{
				rightLocalView[ 1 + j ][ 1 + i ] = aintViewR[ intMid + j ][ intMid + i ];
				if ( aintViewR[ intMid + j ][ intMid + i ] == 2 )
				{
					if ( i == 0 && j == 0 )
						continue;
					rightMapExitFound = true;
					return MUMap.aintMToD[ j + 1 ][ i + 1 ];
				}
			}
		}
		
		Random rdmTemp = new Random();
		int intD;
		int intDeltaX;
		int intDeltaY;
		if ( !leftMapExitFound )
		{
			do
			{
				intD = rdmTemp.nextInt( 8 ) + 1;
				intDeltaX = MUMap.aintDToM[ intD ][ 0 ];
				intDeltaY = MUMap.aintDToM[ intD ][ 1 ];
			} while ( leftLocalView[ 1 + intDeltaY ][ 1 + intDeltaX ] == 1 );
		}
		else
		{
			do
			{
				intD = rdmTemp.nextInt( 8 ) + 1;
				intDeltaX = MUMap.aintDToM[ intD ][ 0 ];
				intDeltaY = MUMap.aintDToM[ intD ][ 1 ];
			} while ( rightLocalView[ 1 + intDeltaY ][ 1 + intDeltaX ] == 1 );
		}
		return intD;
	}
		
}
