package Utility;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;

import Wrapper.coordInfo;

/**
 * The Class CoordHandler.
 * Provides useful static methods associated with the location information
 */
public class CoordHandler {
	
	/**
	 * Gets the user's input
	 * and sets the location of service server
	 *
	 * @return the location name in upper case
	 */
	public static String setLocation() {				
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));		
		String loc = null;
		boolean isValid = false;		
		try {
			do {
				System.out.print("Enter a location: ");											
				loc = in.readLine();
								
				if (checkCoord(loc.toUpperCase()))
					isValid = true;
				else
					System.out.println("Please enter a correct location!");
				
			} while (!isValid);
			in.close();
		} catch (IOException e) {
			System.out.println("[setLocation]IOException: " + e.getMessage());
		}		
		return loc.toUpperCase();
	}
	
	/**
	 * Check the location name is valid or not
	 *
	 * @param loc the location name
	 * @return true, if successful
	 */
	public static boolean checkCoord(String loc) {
		boolean isValid = false;		
		try {
			File csv = new File("rsc/coord_list.csv");
			BufferedReader in = new BufferedReader(new FileReader(csv));
			
			String inline = "";		
			while ((inline = in.readLine()) != null) {
				String[] token = inline.split(",", -1);				
				if(loc.equals(token[0])) {
					isValid = true;
					break;
				}
			}
			in.close();
		} catch (FileNotFoundException e) {
			System.out.println("[checkCoord]FileNotFoundException: " + e.getMessage());			
		} catch (IOException e) {
			System.out.println("[checkCoord]IOException: " + e.getMessage());
		}
		return isValid;
	}
	
	/**
	 * Reads the coordinate data from the csv file
	 *
	 * @param coord Struct coordInfo storing coordinate data
	 * @param loc the location name
	 */
	public static void readCoord(coordInfo coord, String loc) {
		try {
			File csv = new File("rsc/coord_list.csv");
			BufferedReader in = new BufferedReader(new FileReader(csv));
			
			String inline = "";		
			while ((inline = in.readLine()) != null) {
				String[] token = inline.split(",", -1);					
				coord.setCoord(token[0], Double.parseDouble(token[1]), Double.parseDouble(token[2]));								
			}
			in.close();
		} catch (FileNotFoundException e) {
			System.out.println("[readCoord]FileNotFoundException: " + e.getMessage());			
		} catch (IOException e) {
			System.out.println("[readCoord]IOException: " + e.getMessage());
		}
		coord.setServerCoord(loc);
	}
	
	/**
	 * Calculates the RTT
	 * and returns the result
	 *
	 * @param coord Struct coordInfo storing coordinate data
	 * @param user_loc the name of user's location
	 * @return the calculated RTT
	 */
	public static long calRTT(coordInfo coord, String user_loc) {		
		long RTT = 0;
		double R = 6371000;
		
		double server_lat = coord.getServerLat();
		double server_long = coord.getServerLong();				
		
		double user_lat = coord.getLat(user_loc);
		double user_long = coord.getLong(user_loc);
		
		double server_rx = Math.toRadians(server_lat);
		double server_ry = Math.toRadians(server_long);
		double user_rx = Math.toRadians(user_lat);
		double user_ry = Math.toRadians(user_long);
		
		double x = (server_ry - user_ry) * Math.cos((server_rx + user_rx)/2);
		double y = server_rx - user_rx;
		double distance = (Math.sqrt(x*x + y*y) * R) / 1000;
									
		// if same region, RTT = 20
		if (coord.getServerLoc().equals(user_loc))
			RTT = 20;
		// RTT(ms) = 0.02 * Distance(km) + 5
		else {
			RTT = Math.round(0.02 * distance + 5);
			
			// minimum RTT should be 20
			if (RTT < 20)
				RTT = 20;
		}
		return RTT;
	}
}