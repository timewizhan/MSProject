import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.management.ManagementFactory;
import java.net.Socket;
import java.util.ArrayList;

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
					socket.getInputStream(), "UTF-8"));
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

	public static long calRTT(String user_loc) {
		String server_loc = "KR";
		long RTT = 0;
		float distance = 0;
	
		// if same region, RTT = 20
		if (user_loc == server_loc)
			RTT = 20;
		// RTT(ms) = 0.02 * Distance(km) + 5
		else
			RTT = Math.round(0.02 * distance + 5);
		
		// maximal average response delay = 150
		// since latency up to 200
		// will deteriorate the user experience significantly
		
		return RTT;
	}
	
	//OperatingSystemMXBean class = CPU utilization
	public static void getCpuLoad(ArrayList<Double> cpu_log) {
		final OperatingSystemMXBean osBean = (com.sun.management.OperatingSystemMXBean)ManagementFactory.getOperatingSystemMXBean();
		double load = 0;
		
		cpu_log = new ArrayList<>();
		
		while(true) {
			load = osBean.getSystemCpuLoad();
			
			if (load < 0.0)
				continue;
			
			cpu_log.add(load * 100);						
			System.out.println("CPU Usage: " + load * 100.0 + "%" + " / " + cpu_log.size());			
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				System.out.println("[getCpuLoad]InterruptedException e: " + e.getMessage());
			}
		}
	}
}
