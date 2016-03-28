package Type;

public class opType {
	public final static int tweet = 1;
	public final static int read = 2;
	public final static int reply = 3;
	public final static int retweet = 4;
	
	public final static int monitor = 5;
	public final static int moveout = 6;	
	public final static int restart = 7;
	public final static int movein = 8;

	private final static String[] op_list = {"TWEET", "READ", "REPLY", "RETWEET",
											"MONITOR", "MOVEOUT", "RESTART", "MOVEIN"};	
	private final static String invalid_op = "IVALID OPERATION";
	
	public final static int num_read = 10;
	public final static int num_share = 5;
		
	public static String getOperationName(int index) {		
		String opname = null;
		
		switch(index) {
			case tweet:
				opname = op_list[tweet];
				break;				
			case read:
				opname = op_list[read];
				break;				
			case reply:
				opname = op_list[reply];
				break;
			case retweet:
				opname = op_list[retweet];
				break;
			case monitor:
				opname = op_list[monitor];
				break;
			case moveout:
				opname = op_list[moveout];
				break;
			case restart:
				opname = op_list[restart];
				break;
			case movein:
				opname = op_list[movein];
				break;
			default:
				opname = invalid_op;
				break;
		}
		
		return opname;
	}
}
