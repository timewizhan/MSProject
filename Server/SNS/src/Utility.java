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
import java.util.HashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.sun.management.OperatingSystemMXBean;

public class Utility {
	public static JSONObject msgParser(Socket socket) throws UnsupportedEncodingException, IOException, ParseException {		
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

	// http://www.movable-type.co.uk/scripts/latlong.html
	public static long calRTT(String user_loc) {
		double R = 6371000;
		double x1 = 12.2;
		double y1 = 2.2;
		double x2 = 12.2;
		double y2 = 2.2;
		
		double rx_1 = Math.toRadians(x1);
		double ry_1 = Math.toRadians(y1);
		double rx_2 = Math.toRadians(x2);
		double ry_2 = Math.toRadians(y2);
		
		double x = (ry_2 - ry_1) * Math.cos((rx_1 + rx_2)/2);
		double y = rx_2 - rx_1;
		double distance = Math.sqrt(x*x + y*y) * R;
		
		long RTT = 0;
	
		// if same region, RTT = 20
		if (user_loc == "KR")
			RTT = 20;
		// RTT(ms) = 0.02 * Distance(km) + 5
		else
			RTT = Math.round(0.02 * distance + 5);
		
		// maximal average response delay = 150
		// since latency up to 200
		// will deteriorate the user experience significantly
		
		return RTT;
	}
	
	public static void monitorCpuLoad(ScheduledExecutorService scheduler, ArrayList<Double> cpu_log) throws InterruptedException {
		final OperatingSystemMXBean osBean = (com.sun.management.OperatingSystemMXBean)ManagementFactory.getOperatingSystemMXBean();				
		Runnable monitor = new Runnable() {
			
			@Override
			public void run() {
				double load = osBean.getSystemCpuLoad();
				if (load > 0.0) {
					cpu_log.add(load * 100);					
				}
			}
		};		
		scheduler.scheduleAtFixedRate(monitor, 0, 1, TimeUnit.SECONDS);								
	}
	
	public static void stopScheduler(ScheduledExecutorService scheduler) {
		scheduler.shutdown();
	}
	
	public static void readCord(HashMap<String, Double> xcoord, HashMap<String, Double> ycoord) {
		try {
			File csv = new File("rsc/coord_list.csv");
			BufferedReader in = new BufferedReader(new FileReader(csv));
			
			String inline = "";		
			while ((inline = in.readLine()) != null) {
				String[] token = inline.split(",", -1);
				
				xcoord.put(token[0], Double.parseDouble(token[1]));
				ycoord.put(token[0], Double.parseDouble(token[2]));
				
				System.out.println("X: " + xcoord.get(token[0]) + " " + "Y: " + ycoord.get(token[0]));
			}
			in.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}