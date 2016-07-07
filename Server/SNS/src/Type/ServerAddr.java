package Type;
 
/**
 * The Class ServerAddr.
 */
public class ServerAddr {	
	
	private final static String[] mIP_LIST = {"165.132.123.85", "165.132.123.86", "165.132.123.87",
											"165.132.123.88", "165.132.123.89"};		
		
	private final static String mLocal = "localhost";
	
	private final static int mPORT = 7777;
	
	/**
	 * Gets the address of the service server according to the entry point number
	 *
	 * @param ep_num the entry point number
	 * @return the IP address of the service server
	 */
	public static String getServerAddr(int ep_num) {		
		String addr = null;
		int index = ep_num - 1;
				
		switch(ep_num) {
			case 1:
				addr = mIP_LIST[index];
				break;
			case 2:
				addr = mIP_LIST[index];
				break;
			case 3:
				addr = mIP_LIST[index];
				break;
			case 4:
				addr = mIP_LIST[index];
				break;				
			case 5:
				addr = mIP_LIST[index];
				break;			
		}		
		return addr;
	}
	
	/**
	 * Gets the port number of the service server
	 *
	 * @return the service server's port number
	 */
	public static int getServerPort() {
		return mPORT;
	}
	
	/**
	 * Gets the local address of the service server
	 *
	 * @return the service server's local address
	 */
	public static String getLocalAddr() {
		return mLocal;
	}
}