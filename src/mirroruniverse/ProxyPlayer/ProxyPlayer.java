package mirroruniverse.ProxyPlayer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Random;

import mirroruniverse.sim.MUMap;
import mirroruniverse.sim.Player;

public class ProxyPlayer implements Player
{
	Process process;
	BufferedReader reader;
	BufferedWriter writer;
	
	public ProxyPlayer() {
		try {
			ProcessBuilder builder = new ProcessBuilder("ruby", "src/mirroruniverse/ProxyPlayer/player.rb");
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
	
	public int lookAndMove( int[][] aintViewL, int[][] aintViewR ) {
		int out = -1;
		
		//System.out.println(serialize(aintViewL));
		//System.out.println(serialize(aintViewR));
		
		try { 
			writer.write(serialize(aintViewL) + ";" + serialize(aintViewR) + "\n");
			writer.flush();
			
			String line = "";
			
			boolean errored;
			do {
				errored = false;
				try {
					line = reader.readLine().replace("\n", "");
					out = Integer.parseInt(line);
				} catch (Exception e) {
					//it was just a debugging message
					System.out.println(line);
					errored = true;
				}
			} while (errored);
		} catch (Exception e) {
			e.printStackTrace();
		}
		//System.out.println(" "  + out);
		return out;
	}
}
