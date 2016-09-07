public class Counter {
	
	int RECV_COMPLETED_CNT = 0;
	private static Counter counter;
	private Counter () {}
	
	public static synchronized Counter GetInstance () {
		if (counter == null)
			counter = new Counter();
		return counter;
	}
	
	public void addRecvCompletedCount(){
		RECV_COMPLETED_CNT++;
	}
	
	public void setRecvCompletedCountZero(){
		RECV_COMPLETED_CNT = 0;
	}
	
	public int getRecvCompletedCount(){
		return RECV_COMPLETED_CNT;
	}
}
