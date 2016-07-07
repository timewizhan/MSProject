package Utility;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * The Class Talculator.
 * 
 * Provides a static method calculating the time
 */
public class Talculator {

	/**
	 * Gets the current time
	 * and the time one hour before the current time
	 *
	 * @return a list of time
	 */
	public static String[] getPeriod () {		
		Date date = new Date();			
		SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");						
		Calendar cal = Calendar.getInstance();
		
		cal.setTime(date);                                                                                        
		cal.add(Calendar.HOUR, -1);
		cal.add(Calendar.MINUTE, -10);
		String start = f.format(cal.getTime());
		
		cal.setTime(date);
		String end = f.format(cal.getTime());
	
		String[] period = new String[2];
		period[0] = start;
		period[1] = end;				
				
		return period;
	}
}