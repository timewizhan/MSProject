package Type;

/**
 * The Class opType
 * 
 * Classifies the request type in operation number and operation name
 */
public class opType {
	
	public final static int mTWEET = 1;
	public final static int mREAD = 2;
	public final static int mREPLY = 3;
	public final static int mRETWEET = 4;
	
	public final static int mMONITOR = 5;
	public final static int mMOVEOUT = 6;	
	public final static int mRESTART = 7;
	public final static int mMOVEIN = 8;

	private final static String[] mOP_LIST = {"TWEET", "READ", "REPLY", "RETWEET",
											"MONITOR", "MOVEOUT", "RESTART", "MOVEIN"};	
	
	private final static String mINVALID_OP = "IVALID OPERATION";
	
	/**
	 * Additional variables to define the number of read and share operations
	 */
	public final static int mNUM_READ = 1;
	public final static int mNUM_SHARE = 5;				
	
	/**
	 * Returns the proper operation name according to the operation number
	 *  
	 * @param: reqType operation number
	 * @return the operation name corresponding to the operation number 
	 */
	public static String getOperationName(int reqType) {		
		String opname = null;
		int index = reqType - 1;
				
		switch(reqType) {
			case mTWEET:
				opname = mOP_LIST[index];
				break;				
			case mREAD:
				opname = mOP_LIST[index];
				break;				
			case mREPLY:
				opname = mOP_LIST[index];
				break;
			case mRETWEET:
				opname = mOP_LIST[index];
				break;
			case mMONITOR:
				opname = mOP_LIST[index];
				break;
			case mMOVEOUT:
				opname = mOP_LIST[index];
				break;
			case mRESTART:
				opname = mOP_LIST[index];
				break;
			case mMOVEIN:
				opname = mOP_LIST[index];
				break;
			default:
				opname = mINVALID_OP;
				break;
		}
		
		return opname;
	}
}