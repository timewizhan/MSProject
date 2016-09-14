
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
					
					//Count �� �ʱ�ȭ
					Counter.GetInstance().setRecvCompletedCountZero();
					log.debug("# SET EP CONNECTIION COUNTER ZERO \r\n");
					
				//	System.out.println(Counter.GetInstance().getRecvCompletedCount());
					break;
				}
			}

			// ����ڿ� �����ͼ��� ���� ��Ī �˰���
			doMatchAlgorithm();
		}
	}
	
	private void initializeTables(){
	
		//Database�� EP���� ��ŭ Column ���� Table ����
		CDatabase databaseInstance = new CDatabase();
		databaseInstance.connectBrokerDatabase();
		databaseInstance.createTable(CBroker.NUM_OF_EP);
		databaseInstance.updateLocationIpTable();
		databaseInstance.disconnectBrokerDatabase();
		
	}
	
	public void doMatchAlgorithm(){
		
		log.info("[doMatchAlgorithm method] - Start \r\n");
		//�� ����(Factors: Server Traffic, Distance, Social Level, Cost) ����ȭ
		Normalization norm = new Normalization();
		norm.normalizeFactors();
		
		//�� ������ ���� ����ġ(weight) ���
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
		
		//���� ����Ʈ �о���� client_table����
		CDatabase databaseInstance = new CDatabase();
		databaseInstance.connectBrokerDatabase();
		
		//���� �� ��� ������ ����
		ArrayList<ClientData> userList = databaseInstance.getUserList();
		NUM_OF_USERS = userList.size();
		
		log.debug("	* number of user : " + NUM_OF_USERS);
		log.debug("	----------------------------------------------------------------------");
		//���� ���� �ݺ�
		UserWeight userWeight;
		for(int i=0; i<NUM_OF_USERS; i++){
			userWeight = new UserWeight(CBroker.NUM_OF_EP);
			
			//���� ���̵�
			userWeight.setUser(userList.get(i).getUserID());
			
			//������ ����ũ�� ��ȣ�� �Ű��ֱ�
			userWeight.setUser_no(i);
			
			//normalized_distance_table���� user�� �̿��ؼ� ep ������ �°� �� ����
			double NormDistValueArray[];
			NormDistValueArray = databaseInstance.getNormalizedDistanceValues(userList.get(i).getUserID(), CBroker.NUM_OF_EP);
			
			//normalized_social_level_table���� user�� �̿��ؼ� ep ������ �°� �� ����
			double NormSocialWeightValueArray[];
			NormSocialWeightValueArray = databaseInstance.getNormalizedSocialWeightValues(userList.get(i).getUserID(), CBroker.NUM_OF_EP);
			
			//ep������ �°� weight�� �༭ �ΰ��� ���� ��ħ
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
		//�ʱ�ȭ
		LpSolve lp;
		int Ncol, j, ret = 0;

		/* We will build the model row by row
            So we start with creating a model with 0 rows and 2 columns */
		//Ncol = 2; /* there are two variables in the model */
		Ncol = NUM_OF_USERS * CBroker.NUM_OF_EP;

		/* create space large enough for one row */
		int[] colno = new int[Ncol];		//colno�� �� ������ ������ �ǹ� colno���� �̿��ؼ� � ����(�� ��° ����)�� ������ ���� ������ �� �ִ�. ����, ���� ��ŭ ������ �Ҵ�������ϰ�, ����(�ε���)�� ��Ÿ���Ƿ� int
		double[] row = new double[Ncol];	//row�� �� ���� �տ� �ٴ� ��� ��, ���� ���� ��ŭ ������ �Ҵ������ �ϰ�, �Ҽ��� ���ü� �����Ƿ� double�� ó��������Ѵ�

		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		//make LP
		lp = LpSolve.makeLp(0, Ncol);

		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		//make LP ���� (����)
		if(lp.getLp() == 0)
			ret = 1; /* couldn't construct a new model... */

		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		//make LP �����ϸ�,
		//�� ������ �̸� �ٿ��ֱ�
		if(ret == 0) {
			/* let us name our variables. Not required, but can be useful for debugging */
			int p = 1;
			for (int i = 0; i < NUM_OF_USERS; i++) {		//user ���� ��ŭ �ݺ�

				for (int k = 0; k < CBroker.NUM_OF_EP; k++) {		//data center ���� ��ŭ �ݺ�

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
			//contraint ����   

			lp.setAddRowmode(true);  /* makes building the model faster if it is done rows by row */

			//contraint1 : �� ������ �ټ��� ������ ���� �߿����� �ϳ����� ��Ī �Ǿ��ϴ� ���� : x_11 + x_12 + x_13 <= 1

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

		//contraint2 : �� ������ ���Ͱ� �ټ��� �������� ��Ī�� �� �ִ� ���� : x_11 + x_21 + x_31 + x_n1 <= n (n�� user �� : numOfUsers)

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

		//contraint3 : ��Ī�� ��� ������ ���� �ּ� ������ ���ں��� Ŀ�ߵǴ� ���� (�� ������ �ּ� 1�� �̻��� ������ ���Ϳ� ��Ī �Ǿ��Ѵ�)

		if(ret == 0) {

			j = 0;
			for (int i = 0; i < NUM_OF_USERS*CBroker.NUM_OF_EP; i++){
				colno[j] = i + 1;
				row[j++] = 1;
			}

			/* add the row to lpsolve */
			lp.addConstraintex(j, row, colno, LpSolve.GE, NUM_OF_USERS);
		}

		//contraint4 : ��� ������ ���� 0���� ũ�ų� ���ƾ� �Ѵٴ� ����

		if (ret == 0){

			for (int i = 0; i < NUM_OF_USERS * CBroker.NUM_OF_EP; i++){

				j = 0;
				colno[j] = i + 1;
				row[j++] = 1;

				/* add the row to lpsolve */
				lp.addConstraintex(j, row, colno, LpSolve.GE, 0);
			}
		}

		//contraint5 : legislation ������ ����ٰ� �־�� �Ѵ�

		/**
		 * 	
		 */


		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////   
		//min �̳� max�� ������ �ϴ� objective function ������ִ� ��. 
		//���⿡ row �迭 �̿��ؼ� ����ġ �־����

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
		//���ϰ��� �ϴ� ���� min���� max���� ����, LP�� ���ư��� �ϴ� �κ�

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

			//1�ΰŸ� ����
		//	System.out.println("");
		//	System.out.println("[elements over 1]");
			for (j = 0; j < Ncol; j++){
				if (row[j] > 0){
					
					String matchResult = lp.getColName(j+1);
					String fullString = matchResult;
					int stringLength = matchResult.length();

					String userNo = fullString.substring(2, stringLength-1);					//���ʿ��� "x_" ��������, ���ʿ��� Data center�� �ǹ��ϴ� ���ڸ� ���� �ձ���
					String epNo = fullString.substring(stringLength-1, stringLength);			//���� ���ڸ� ���ڸ���
				//	log.debug(" - User number: " + userNo + ", EP number: " + epNo);

					//user id, user no, match result�� ArrayList�� �߰�
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
