
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import lpsolve.*;

public class CLPCalculation implements Runnable {

	static Logger log = Logger.getLogger(CBroker.class.getName());		//initiate logger
	//165.132.122.244, 165.132.123.73, localhost
//	static final int NUM_OF_EP = 3; // Number of clouds
	private int NUM_OF_USERS;
	ArrayList<UserWeight> userWeightList;
	ArrayList<LpMatchResult> lpMatchResult;
	
	public CLPCalculation(){}
	
	public void checkRecvComplete(){
		
		log.info("check the number of receiving monitored data \r\n");
		
		while(true){
			log.info("****************************************************************************************");
			log.info("********************************* Broker routine START *********************************");
			log.info("**************************************************************************************** \r\n");
			
			userWeightList = new ArrayList<UserWeight>();
			lpMatchResult =  new ArrayList<LpMatchResult>();
			
			log.info("Initialize dynamic table (normalized_distance_table) \r\n");
			initializeTables();
			
			while(true) {
				
				if(CBroker.NUM_OF_EP == Counter.GetInstance().getRecvCompletedCount()){
					
					log.debug("# All The Monitored Data Was Received");
					
					//Count 값 초기화
					Counter.GetInstance().setRecvCompletedCountZero();
					log.debug("# SET EP CONNECTIION COUNTER ZERO \r\n");
					
				//	System.out.println(Counter.GetInstance().getRecvCompletedCount());
					break;
				}
			}

			// 사용자와 데이터센터 간의 매칭 알고리즘
			doMatchAlgorithm();
		}
	}
	
	private void initializeTables(){
	
		//Database에 EP개수 만큼 Column 만들어서 Table 생성
		CDatabase databaseInstance = new CDatabase();
		databaseInstance.connectBrokerDatabase();
		databaseInstance.createTable(CBroker.NUM_OF_EP);
		databaseInstance.updateLocationIpTable();
		databaseInstance.disconnectBrokerDatabase();
		
	}
	
	public void doMatchAlgorithm(){
		
		log.info("[doMatchAlgorithm method] - Start \r\n");
		//각 팩터(Factors: Server Traffic, Distance, Social Level, Cost) 정규화
		Normalization norm = new Normalization();
		norm.normalizeFactors();
		
		//각 엣지에 대한 가중치(weight) 계산
		calculateWeight();
		
		
		try {
			calculateLP();
		} catch (LpSolveException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		CLBCalculation lbCalculation = new CLBCalculation(userWeightList);
		lbCalculation.lbMain(lpMatchResult);
		
		log.info("[doMatchAlgorithm method] - End \r\n");
	}
	
	public void calculateWeight(){
		
		log.info("[calculateWeight method] - Start");
		
		//유저 리스트 읽어오기 client_table에서
		CDatabase databaseInstance = new CDatabase();
		databaseInstance.connectBrokerDatabase();
		
		//유저 수 멤버 변수에 저장
		ArrayList<ClientData> userList = databaseInstance.getUserList();
		NUM_OF_USERS = userList.size();
		
		log.debug("	* number of user : " + NUM_OF_USERS);
		log.debug("	----------------------------------------------------------------------");
		//유저 별로 반복
		UserWeight userWeight;
		for(int i=0; i<NUM_OF_USERS; i++){
			userWeight = new UserWeight(CBroker.NUM_OF_EP);
			
			//유저 아이디
			userWeight.setUser(userList.get(i).getUserID());
			
			//유저에 유니크한 번호값 매겨주기
			userWeight.setUser_no(i);
			
			//normalized_distance_table에서 user명 이용해서 ep 개수에 맞게 값 추출
			double NormDistValueArray[];
			NormDistValueArray = databaseInstance.getNormalizedDistanceValues(userList.get(i).getUserID(), CBroker.NUM_OF_EP);
			
			//normalized_social_level_table에서 user명 이용해서 ep 개수에 맞게 값 추출
			double NormSocialWeightValueArray[];
			NormSocialWeightValueArray = databaseInstance.getNormalizedSocialWeightValues(userList.get(i).getUserID(), CBroker.NUM_OF_EP);
			
			//ep개수에 맞게 weight를 줘서 두개의 값을 합침
			int a = 1;
			int b = 1;
			double tmpWeightValues [] = new double [CBroker.NUM_OF_EP];
			for(int j=0; j<CBroker.NUM_OF_EP; j++){
				log.debug("	user id : " + userList.get(i).getUserID() + ", norm dist (ep"+ (int)(j+1) + ") : " + NormDistValueArray[j] 
						+ ", norm social weight (ep" + (int)(j+1) + ") :" + NormSocialWeightValueArray[j]);
				tmpWeightValues[j] = a*NormDistValueArray[j] + b*NormSocialWeightValueArray[j];
			}
			userWeight.setWeightValues(tmpWeightValues);
			userWeightList.add(userWeight);
		}
		log.debug("	----------------------------------------------------------------------");

		databaseInstance.disconnectBrokerDatabase();
		
		log.info("[calculateWeight method] - End \r\n");
	}
	
	public void getWeight() {
		
	}
	
	public int calculateLP() throws LpSolveException{
		
		log.info("[calculateLP method] - Start");
		
		///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		//초기화
		LpSolve lp;
		int Ncol, j, ret = 0;

		/* We will build the model row by row
            So we start with creating a model with 0 rows and 2 columns */
		//Ncol = 2; /* there are two variables in the model */
		Ncol = NUM_OF_USERS * CBroker.NUM_OF_EP;

		/* create space large enough for one row */
		int[] colno = new int[Ncol];		//colno는 각 변수의 순서를 의미 colno값을 이용해서 어떤 변수(몇 번째 변수)에 접근한 건지 결정할 수 있다. 따라서, 변수 만큼 공간을 할당해줘야하고, 순서(인덱스)를 나타내므로 int
		double[] row = new double[Ncol];	//row는 각 변수 앞에 붙는 계수 값, 따라서 변수 만큼 공간을 할당해줘야 하고, 소수가 나올수 있으므로 double로 처리해줘야한다

		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		//make LP
		lp = LpSolve.makeLp(0, Ncol);

		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		//make LP 실패 (에러)
		if(lp.getLp() == 0)
			ret = 1; /* couldn't construct a new model... */

		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		//make LP 성공하면,
		//각 변수에 이름 붙여주기
		if(ret == 0) {
			/* let us name our variables. Not required, but can be useful for debugging */
			int p = 1;
			for (int i = 0; i < NUM_OF_USERS; i++) {		//user 숫자 만큼 반복

				for (int k = 0; k < CBroker.NUM_OF_EP; k++) {		//data center 숫자 만큼 반복

					String str = "x_";
					StringBuffer base = new StringBuffer(str);

					String userIndex = Integer.toString(i);
					base.append(userIndex);

					String cloudIndex = Integer.toString(k+1);
					base.append(cloudIndex);

					String colName = base.toString();

					lp.setColName(p++, colName);
				}
			}

			/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////   
			//contraint 설정   

			lp.setAddRowmode(true);  /* makes building the model faster if it is done rows by row */

			//contraint1 : 각 유저가 다수의 데이터 센터 중에서도 하나에만 매칭 되야하는 조건 : x_11 + x_12 + x_13 <= 1

			j= 0;
			for (int i= 0; i < NUM_OF_USERS * CBroker.NUM_OF_EP; i++){

				colno[j] = i + 1;
				row[j++] = 1;

				if (((i + 1) % CBroker.NUM_OF_EP) == 0){

					/* add the row to lpsolve */
					lp.addConstraintex(j, row, colno, LpSolve.LE, 1);
					j = 0;
				}
			}
		}

		//contraint2 : 각 데이터 센터가 다수의 유저에게 매칭될 수 있는 조건 : x_11 + x_21 + x_31 + x_n1 <= n (n은 user 수 : numOfUsers)

		if(ret == 0) {

			j = 0; int p = 1; int tmp = 0;
			for (int i = 0; i < CBroker.NUM_OF_EP; i++){

				tmp = p;
				for (int k = 0; k < NUM_OF_USERS; k++){

					colno[j] = tmp;
					row[j++] = 1;

					tmp = tmp + CBroker.NUM_OF_EP;
				}

				/* add the row to lpsolve */
				lp.addConstraintex(j, row, colno, LpSolve.LE, NUM_OF_USERS);
				p++; j = 0;
			}
		}

		//contraint3 : 매칭된 모든 엣지의 합이 최소 유저의 숫자보다 커야되는 조건 (각 유저는 최소 1개 이상의 데이터 센터와 매칭 되야한다)

		if(ret == 0) {

			j = 0;
			for (int i = 0; i < NUM_OF_USERS*CBroker.NUM_OF_EP; i++){
				colno[j] = i + 1;
				row[j++] = 1;
			}

			/* add the row to lpsolve */
			lp.addConstraintex(j, row, colno, LpSolve.GE, NUM_OF_USERS);
		}

		//contraint4 : 모든 엣지의 값은 0보다 크거나 같아야 한다는 조건

		if (ret == 0){

			for (int i = 0; i < NUM_OF_USERS * CBroker.NUM_OF_EP; i++){

				j = 0;
				colno[j] = i + 1;
				row[j++] = 1;

				/* add the row to lpsolve */
				lp.addConstraintex(j, row, colno, LpSolve.GE, 0);
			}
		}

		//contraint5 : legislation 조건을 여기다가 넣어야 한다

		/**
		 * 	
		 */


		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////   
		//min 이나 max로 만들어야 하는 objective function 만들어주는 곳. 
		//여기에 row 배열 이용해서 가중치 넣어야함

		if(ret == 0) {
			lp.setAddRowmode(false); /* rowmode should be turned off again when done building the model */
			j = 0;
			
			for (int i = 0; i < NUM_OF_USERS; i++){
       		 for (int p = 0; p < CBroker.NUM_OF_EP; p++){

   			 	if ((p + 1 % CBroker.NUM_OF_EP) == 1){

   			 		colno[j] = j + 1;
						row[j] = userWeightList.get(i).getWeightValues()[0];
						j++;

   			 	} else if ((p + 1 % CBroker.NUM_OF_EP) == 2){

   			 		colno[j] = j + 1;
						row[j] = userWeightList.get(i).getWeightValues()[1];
						j++;

   			 	} else if ((p + 1 % CBroker.NUM_OF_EP) == 3){

						colno[j] = j + 1;
						row[j] = userWeightList.get(i).getWeightValues()[2];
						j++;
					}
				}
			}

			/* set the objective in lpsolve */
			lp.setObjFnex(j, row, colno);
		}


		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////   
		//구하고자 하는 것이 min인지 max인지 고르고, LP가 돌아가게 하는 부분

		if(ret == 0) {
			/* set the object direction to maximize */
			lp.setMinim();

			/* just out of curioucity, now generate the model in lp format in file model.lp */
			lp.writeLp("model.lp");

			/* I only want to see important messages on screen while solving */
			lp.setVerbose(LpSolve.IMPORTANT);

			/* Now let lpsolve calculate a solution */
			ret = lp.solve();
			if(ret == LpSolve.OPTIMAL)
				ret = 0;
			else
				ret = 5;
		}

		if(ret == 0) {
			/* a solution is calculated, now lets get some results */

			/* objective value */
		//	System.out.println("Objective value: " + lp.getObjective());

			/* variable values */
			lp.getVariables(row);
		//	for(j = 0; j < Ncol; j++)
		//		System.out.println(lp.getColName(j + 1) + ": " + row[j]);

			//1인거만 뽑자
		//	System.out.println("");
		//	System.out.println("[elements over 1]");
			for (j = 0; j < Ncol; j++){
				if (row[j] > 0){
					
					String matchResult = lp.getColName(j+1);
					String fullString = matchResult;
					int stringLength = matchResult.length();

					String userNo = fullString.substring(2, stringLength-1);					//앞쪽에서 "x_" 다음부터, 뒤쪽에서 Data center를 의미하는 한자리 숫자 앞까지
					String epNo = fullString.substring(stringLength-1, stringLength);			//제일 끝자리 한자리만
				//	log.debug(" - User number: " + userNo + ", EP number: " + epNo);

					//user id, user no, match result를 ArrayList에 추가
					makeLpMatchList(userNo, epNo);
				}
			}

			/* we are done now */
			//test
			System.out.println();
			for(int i=0; i<lpMatchResult.size(); i++){
				log.debug("	* ID:" + lpMatchResult.get(i).getUserId()
						+ ", No.:" + lpMatchResult.get(i).getUserNo()
						+ ", Cloud No.:" + lpMatchResult.get(i).getCloudNo());
			}
			System.out.println();
		}

		/* clean up such that all used memory by lpsolve is freed */
		if(lp.getLp() != 0)
			lp.deleteLp();

		log.info("[calculateLP method] - End \r\n");
		
		return(ret);
	}
	
	public void makeLpMatchList(String userNo, String epNo){
	
		LpMatchResult eachUserMatch = new LpMatchResult();
		
		for(int i=0; i<userWeightList.size(); i++){
			if(userWeightList.get(i).getUser_no() == Integer.parseInt(userNo)){
				eachUserMatch.setUserId(userWeightList.get(i).getUser());
			}
		}
		eachUserMatch.setUserNo(Integer.parseInt(userNo));
		eachUserMatch.setCloudNo(Integer.parseInt(epNo));
		lpMatchResult.add(eachUserMatch);
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		log.info("LP Thread is Running Now..");
				
		checkRecvComplete();
		
		log.info("=================== END BROKER =================== \r\n");
	}
}
