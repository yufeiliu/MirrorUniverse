package mirroruniverse.g6;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.junit.Test;

public class DFASolverTest {
	int[][]leftMap = new int[4][4]; //change to correct size
	int[][]rightMap = new int[4][4]; //change to correct size
	
	public void initializeMaps() {
		//can use any map

		//left map
	    int x = 0, y = 0;
	    int Lvalue;
	    
	    try {
	    	//***change to your local path for the map you want to test
	    	BufferedReader input = new BufferedReader(new FileReader("/Users/NeeCee/Classes/4444/MirrorUniverse/maps/g6maps/lessEasyLeft.txt"));
	    	String line;
	    	while((line = input.readLine()) != null) {
	    		String[] vals = line.split(" ");
	    		for(String str : vals) {
	    			Lvalue = Integer.parseInt(str);
	    			leftMap[x][y] = Lvalue;
	    			++y;
	    		}
	    		
	    		++x;
	    		y = 0;
	    	}
	    	
	    	input.close();
	    } catch (IOException ioex) {
	    	System.out.println("error reading in map for testing");
	    }
	    
	    //right map
	    x = 0;
	    y = 0;
	    int Rvalue;
	    
	    try {
	    	//***change to your local path for the map you want to test
	    	//change to map you want to test
	    	BufferedReader input = new BufferedReader(new FileReader("/Users/NeeCee/Classes/4444/MirrorUniverse/maps/g6maps/lessEasyRight.txt"));
	    	String line;
	    	while((line = input.readLine()) != null) {
	    		String[] vals = line.split(" ");
	    		for(String str : vals) {
	    			Rvalue = Integer.parseInt(str);
	    			rightMap[x][y] = Rvalue;
	    			++y;
	    		}
	    		++x;
	    		y = 0;
	    	}
	    	
	    	input.close();
	    } catch (IOException ioex) {
	    	System.out.println("error reading in map for testing");
	    }
	    
	    //print each map for testing
	    for(int i = 0; i < leftMap.length; i++ )
	    	for(int j = 0; j < leftMap[i].length; j++ )
	    		System.out.println(leftMap[i][j]);
	    System.out.println("*-----------------*");
	    for(int i = 0; i < rightMap.length; i++ )
	    	for(int j = 0; j < rightMap[i].length; j++ )
	    		System.out.println(rightMap[i][j]);
	}
	
	
	@Test
	public void testSolveIntArrayArrayIntArrayArray() {
	
		//init maps
		initializeMaps();
	    //tester 
	    DFASolver tester = new DFASolver();
	    //change to assertEquals and fill with expected results
	    assertNotNull("dfa solve (2 maps) result:", tester.solve(leftMap, rightMap));
	    
	    	
	}

	@Test
	public void testSolveIntArrayArray() {
		initializeMaps();
		//tester 
	    DFASolver tester = new DFASolver();
	  //change to assertEquals and fill with expected results
	    assertNotNull("dfa solve (2maps/cutofftime/min attempts/max attempts) result:", tester.solve(leftMap, rightMap, Solver.MAX_CUTOFF_TIME, Solver.DEFAULT_MIN_DISTANCE, Solver.MAX_DISTANCE));
		fail("Not yet implemented");
	}

	@Test
	public void testSolveIntArrayArrayIntArrayArrayLongIntInt() {
		initializeMaps();
		//tester 
	    DFASolver tester = new DFASolver();
	  //change to assertEquals and fill with expected results
	    assertNotNull("dfa solve (1 map) result:", tester.solve(leftMap));
		fail("Not yet implemented");
	}

}
