
import java.lang.management.ManagementFactory;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.sun.management.OperatingSystemMXBean;

public class CpuMonitor {	
	public static void startCpuMonitor() {
		ServiceServer.mScheduler = Executors.newSingleThreadScheduledExecutor();
		try {
			monitorCpuLoad();
		} catch (InterruptedException e) {
			System.out.println("[startCpuMonitor]e: " + e.getMessage());
		}
	}
	
	public static void monitorCpuLoad() throws InterruptedException {
		final OperatingSystemMXBean osBean = 
				(com.sun.management.OperatingSystemMXBean)ManagementFactory.getOperatingSystemMXBean();
		
		ServiceServer.mCPU_Log.clear();
		ServiceServer.mAVG_CPU_Log.clear();
		
		Runnable monitor = new Runnable() {
			
			@Override
			public void run() {
				double load = osBean.getSystemCpuLoad();
				if (load > 0.0) {
					if (ServiceServer.mCPU_Log.size() < Integer.MAX_VALUE)
						ServiceServer.mCPU_Log.add(load * 100);
					else {
						double total = 0;
						for (int i = 0; i < ServiceServer.mCPU_Log.size(); i ++)
							total = total + ServiceServer.mCPU_Log.get(i);
						
						ServiceServer.mAVG_CPU_Log.add(total / ServiceServer.mCPU_Log.size());
						ServiceServer.mCPU_Log.clear();
						ServiceServer.mCPU_Log.add(load);
					}					
					//System.out.println("[CPU Usage] " + load * 100);
				}
			}
		};		
		ServiceServer.mScheduler.scheduleAtFixedRate(monitor, 0, 1, TimeUnit.SECONDS);								
	}
	
	public static void stopScheduler(ScheduledExecutorService scheduler) {
		ServiceServer.mScheduler.shutdown();		
	}
}
