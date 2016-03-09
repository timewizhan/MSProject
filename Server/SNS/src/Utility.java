import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.management.ManagementFactory;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.sun.management.OperatingSystemMXBean;

import Wrapper.coordInfo;

public class Utility {
	public static JSONObject msgParser(Socket socket) {		
		String result = "";	
		BufferedReader input = null;
		JSONObject msg = null;
						
		try {
			input = new BufferedReader(new InputStreamReader(
					socket.getInputStream()));
			result = input.readLine();		
						
			JSONParser parser = new JSONParser();
			msg = (JSONObject) parser.parse(result);			
		} catch (UnsupportedEncodingException e) {
			System.out.println("[msgParser]UnsupportedEncodingException e: " + e.getMessage());			
		} catch (IOException e) {
			System.out.println("[msgParser]IOException e: " + e.getMessage());
		} catch (ParseException e) {
			System.out.println("[msgParser]ParseException e: " + e.getMessage());
		}	
		return msg;
	}
	
	@SuppressWarnings("unchecked")
	public static String msgGenerator(int result) {
		JSONObject response = new JSONObject();
		response.put("RESPONSE", result);
		
		return response.toString();
	}
		
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
			System.out.println("[checkCoord]FileNotFoundException e: " + e.getMessage());			
		} catch (IOException e) {
			System.out.println("[checkCoord]IOException e: " + e.getMessage());
		}
		return isValid;
	}
	
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
			System.out.println("[readCoord]FileNotFoundException e: " + e.getMessage());			
		} catch (IOException e) {
			System.out.println("[readCoord]IOException e: " + e.getMessage());
		}
		coord.setServerCoord(loc);
	}
	
	public static String setLocation() {				
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));		
		String loc = null;
		boolean isValid = false;		
		try {
			do {
				System.out.print("Enter a location: ");											
				loc = in.readLine();
								
				if (Utility.checkCoord(loc.toUpperCase()))
					isValid = true;
				else
					System.out.println("Please enter a correct location!");
				
			} while (!isValid);
			in.close();
		} catch (IOException e) {
			System.out.println("[setLocation]IOException e: " + e.getMessage());
		}		
		return loc.toUpperCase();
	}
	
	public static void monitorCpuLoad(ScheduledExecutorService scheduler, ArrayList<Double> cpu_log, ArrayList<Double> avg_cpu_log) throws InterruptedException {
		final OperatingSystemMXBean osBean = 
				(com.sun.management.OperatingSystemMXBean)ManagementFactory.getOperatingSystemMXBean();				
		Runnable monitor = new Runnable() {
			
			@Override
			public void run() {
				double load = osBean.getSystemCpuLoad();
				if (load > 0.0) {
					if (cpu_log.size() < Integer.MAX_VALUE)
						cpu_log.add(load * 100);
					else {
						double total = 0;
						for (int i = 0; i < cpu_log.size(); i ++)
							total = total + cpu_log.get(i);
						
						avg_cpu_log.add(total / cpu_log.size());
						cpu_log.clear();
						cpu_log.add(load);
					}
				}
			}
		};		
		scheduler.scheduleAtFixedRate(monitor, 0, 1, TimeUnit.SECONDS);								
	}
	
	public static void stopScheduler(ScheduledExecutorService scheduler) {
		scheduler.shutdown();
	}
	
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