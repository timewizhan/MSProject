import java.util.Calendar;

public class CEntryPoint {

	public final static int UNITSLEEP = 1000; 

	public static void main(String [] args){

		System.out.println("EntryPoint Start");

		while(true){
			sleepProcess();
		
			CNetworkClient cEP = new CNetworkClient();
		//	cEP.start();
			int keepGoing = cEP.start();

			if(keepGoing==1){

			}else if(keepGoing==0){
				break;
			}
		
		}
	}

	public static void sleepProcess(){

		while(true) {
			Calendar currentTime = Calendar.getInstance( );
			boolean bSharpTime = timeChecking(currentTime.get(Calendar.HOUR_OF_DAY), currentTime.get(Calendar.MINUTE));
			try {
				if (bSharpTime){
					System.out.println("time test : " + currentTime.get(Calendar.HOUR_OF_DAY) + "hour " + currentTime.get(Calendar.MINUTE) + "min");
					Thread.sleep(UNITSLEEP * 60);
					break;
				}
				else {
					Thread.sleep(UNITSLEEP * 60);	// 1 min
				}
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public static boolean timeChecking(int hour, int min){

		boolean bSharpTime = false;

		if (hour == 0 && min == 0){
			bSharpTime = true;
		}
		else if (hour == 1 && min == 30){
			bSharpTime = true;
		}
		else if (hour == 2 && min == 30){
			bSharpTime = true;
		}
		else if (hour == 3 && min == 30){
			bSharpTime = true;
		}
		else if (hour == 4 && min == 30){
			bSharpTime = true;
		}
		else if (hour == 5 && min == 30){
			bSharpTime = true;
		}
		else if (hour == 6 && min == 30){
			bSharpTime = true;
		}
		else if (hour == 7 && min == 30){
			bSharpTime = true;
		}
		else if (hour == 8 && min == 30){
			bSharpTime = true;
		}
		else if (hour == 9 && min == 30){
			bSharpTime = true;
		}
		else if (hour == 10 && min == 30){
			bSharpTime = true;
		}
		else if (hour == 11 && min == 30){
			bSharpTime = true;
		}
		else if (hour == 12 && min == 30){
			bSharpTime = true;
		}
		else if (hour == 13 && min == 30){
			bSharpTime = true;
		}
		else if (hour == 14 && min == 30){
			bSharpTime = true;
		}
		else if (hour == 15 && min == 30){
			bSharpTime = true;
		}
		else if (hour == 16 && min == 30){
			bSharpTime = true;
		}
		else if (hour == 17 && min == 30){
			bSharpTime = true;
		}
		else if (hour == 18 && min == 30){
			bSharpTime = true;
		}
		else if (hour == 19 && min == 30){
			bSharpTime = true;
		}
		else if (hour == 20 && min == 30){
			bSharpTime = true;
		}
		else if (hour == 21 && min == 30){
			bSharpTime = true;
		}
		else if (hour == 22 && min == 30){
			bSharpTime = true;
		}
		else if (hour == 23 && min == 30){
			bSharpTime = true;
		}

		return bSharpTime;
	}
}
