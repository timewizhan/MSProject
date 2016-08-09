
public class ServerData {

	String epAddr;
	
	int serverTraffic;
	double normServerTraffic;
	
	static int minServerTraffic;
	static int maxServerTraffic;
	
	public ServerData(){
	
		epAddr = null;
		
		serverTraffic = 0;
		normServerTraffic = 0.0;
	}
	
	public String getEpAddr() {
		return epAddr;
	}

	public void setEpAddr(String epAddr) {
		this.epAddr = epAddr;
	}

	public int getServerTraffic() {
		return serverTraffic;
	}

	public void setServerTraffic(int serverTraffic) {
		this.serverTraffic = serverTraffic;
	}

	public double getNormServerTraffic() {
		return normServerTraffic;
	}

	public void setNormServerTraffic(double normServerTraffic) {
		this.normServerTraffic = normServerTraffic;
	}

	public static int getMinServerTraffic() {
		return minServerTraffic;
	}

	public static void setMinServerTraffic(int minServerTraffic) {
		ServerData.minServerTraffic = minServerTraffic;
	}

	public static int getMaxServerTraffic() {
		return maxServerTraffic;
	}

	public static void setMaxServerTraffic(int maxServerTraffic) {
		ServerData.maxServerTraffic = maxServerTraffic;
	}
}
