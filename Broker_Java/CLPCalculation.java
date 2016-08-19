
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import lpsolve.*;

public class CLPCalculation implements Runnable {

	//165.132.122.244, 165.132.123.73, localhost
	static final int NUM_OF_EP = 3; // Number of clouds
	private int NUM_OF_USERS;
	ArrayList<UserWeight> userWeightList;
	
	public CLPCalculation(){
		userWeightList = new ArrayList<UserWeight>();
	}
	
	public void checkRecvComplete(){
		
		while(true) {
			
			if(NUM_OF_EP == Counter.GetInstance().getRecvCompletedCount()){
				System.out.println("all the data was received");
				break;
			}
		}
		
		// ����ڿ� �����ͼ��� ���� ��Ī �˰���
		doMatchAlgorithm();
	}
	
	public void doMatchAlgorithm(){
		
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
		
		/*
		CLBCalculation lbCalculation = new CLBCalculation();
		lbCalculation.lbMain();
		*/
	}
	
	public void calculateWeight(){
		
		//���� ����Ʈ �о���� client_table����
		CDatabase databaseInstance = new CDatabase();
		databaseInstance.connectBrokerDatabase();
		
		//���� �� ��� ������ ����
		ArrayList<ClientData> userList = databaseInstance.getUserList();
		NUM_OF_USERS = userList.size();
		
		//���� ���� �ݺ�
		UserWeight userWeight;
		for(int i=0; i<NUM_OF_USERS; i++){
			userWeight = new UserWeight(NUM_OF_EP);
			
			//���� ���̵�
			userWeight.setUser(userList.get(i).getUserID());
			
			//������ ����ũ�� ��ȣ�� �Ű��ֱ�
			userWeight.setUser_no(i);
			
			//normalized_distance_table���� user�� �̿��ؼ� ep ������ �°� �� ����
			double NormDistValueArray[];
			NormDistValueArray = databaseInstance.getNormalizedDistanceValues(userList.get(i).getUserID(), NUM_OF_EP);
		//	System.out.println("[test0] " + userList.get(i).getUserID());
			
			//normalized_social_level_table���� user�� �̿��ؼ� ep ������ �°� �� ����
			double NormSocialWeightValueArray[];
			NormSocialWeightValueArray = databaseInstance.getNormalizedSocialWeightValues(userList.get(i).getUserID(), NUM_OF_EP);
			
			//ep������ �°� weight�� �༭ �ΰ��� ���� ��ħ
			int a = 1;
			int b = 1;
			double tmpWeightValues [] = new double [NUM_OF_EP];
			for(int j=0; j<NUM_OF_EP; j++){
				tmpWeightValues[j] = a*NormDistValueArray[j] + b*NormSocialWeightValueArray[j];
			}
			userWeight.setWeightValues(tmpWeightValues);
			System.out.println("[Test] user name : " + userWeight.getUser() + ", " + userWeight.getUser_no()
							+ ", " + userWeight.getWeightValues()[0]
									+ ", " + userWeight.getWeightValues()[1]
											+ ", " + userWeight.getWeightValues()[2]); 
			userWeightList.add(userWeight);
		}
		
		//�׽�Ʈ
		for(int p=0; p<userWeightList.size(); p++){
			System.out.println("[Debug] print the weight values of a user: " 
								+ userWeightList.get(p).getUser() + ", " 
									+ userWeightList.get(p).getWeightValues()[0] + ", "
										+ userWeightList.get(p).getWeightValues()[1] + ", "
											+ userWeightList.get(p).getWeightValues()[2]);
		}
		
		databaseInstance.disconnectBrokerDatabase();
	}
	
	public void getWeight() {
		
	}
	
	public int calculateLP() throws LpSolveException{

		///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		//�ʱ�ȭ
		LpSolve lp;
		int Ncol, j, ret = 0;

		/* We will build the model row by row
            So we start with creating a model with 0 rows and 2 columns */
		//Ncol = 2; /* there are two variables in the model */
		Ncol = NUM_OF_USERS * NUM_OF_EP;

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

				for (int k = 0; k < NUM_OF_EP; k++) {		//data center ���� ��ŭ �ݺ�

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
			for (int i= 0; i < NUM_OF_USERS * NUM_OF_EP; i++){

				colno[j] = i + 1;
				row[j++] = 1;

				if (((i + 1) % NUM_OF_EP) == 0){

					/* add the row to lpsolve */
					lp.addConstraintex(j, row, colno, LpSolve.LE, 1);
					j = 0;
				}
			}
		}

		//contraint2 : �� ������ ���Ͱ� �ټ��� �������� ��Ī�� �� �ִ� ���� : x_11 + x_21 + x_31 + x_n1 <= n (n�� user �� : numOfUsers)

		if(ret == 0) {

			j = 0; int p = 1; int tmp = 0;
			for (int i = 0; i < NUM_OF_EP; i++){

				tmp = p;
				for (int k = 0; k < NUM_OF_USERS; k++){

					colno[j] = tmp;
					row[j++] = 1;

					tmp = tmp + NUM_OF_EP;
				}

				/* add the row to lpsolve */
				lp.addConstraintex(j, row, colno, LpSolve.LE, NUM_OF_USERS);
				p++; j = 0;
			}
		}

		//contraint3 : ��Ī�� ��� ������ ���� �ּ� ������ ���ں��� Ŀ�ߵǴ� ���� (�� ������ �ּ� 1�� �̻��� ������ ���Ϳ� ��Ī �Ǿ��Ѵ�)

		if(ret == 0) {

			j = 0;
			for (int i = 0; i < NUM_OF_USERS*NUM_OF_EP; i++){
				colno[j] = i + 1;
				row[j++] = 1;
			}

			/* add the row to lpsolve */
			lp.addConstraintex(j, row, colno, LpSolve.GE, NUM_OF_USERS);
		}

		//contraint4 : ��� ������ ���� 0���� ũ�ų� ���ƾ� �Ѵٴ� ����

		if (ret == 0){

			for (int i = 0; i < NUM_OF_USERS * NUM_OF_EP; i++){

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
       		 for (int p = 0; p < NUM_OF_EP; p++){

   			 	if ((p + 1 % NUM_OF_EP) == 1){

   			 		colno[j] = j + 1;
						row[j] = userWeightList.get(i).getWeightValues()[0];
						j++;

   			 	} else if ((p + 1 % NUM_OF_EP) == 2){

   			 		colno[j] = j + 1;
						row[j] = userWeightList.get(i).getWeightValues()[1];
						j++;

   			 	} else if ((p + 1 % NUM_OF_EP) == 3){

						colno[j] = j + 1;
						row[j] = userWeightList.get(i).getWeightValues()[2];
						j++;
					}
				}
			}
			
			/*
			 ����ġ�� �����, �ּҰ� �Ǿ��ϴ� objective ������ �Ʒ��� ���� ���� ��
			//colno[j] = 1 : ù��° column�̶�� �ǹ�
			//row[j] = 143 : ����� 143 ����
			//colno[j] = 2 : �ι�° column�̶�� �ǹ�
			//row[j] = 60 : ����� 60 ����


        	 for (int i = 0; i < numOfUsers; i++){
        		 for (int p = 0; p < NUM_OF_EP; p++){

    			 	if ((p + 1 % cloud_no) == 1){

    			 		colno[j] = j + 1;
						row[j] = vecWeightData.at(i).dEp1;
						j++;

    			 	} else if ((p + 1 % NUM_OF_EP) == 2){

    			 		colno[j] = j + 1;
						row[j] = vecWeightData.at(i).dEp2;
						j++;

    			 	} else if ((p + 1 % NUM_OF_EP) == 3){

						colno[j] = j + 1;
						row[j] = vecWeightData.at(i).dEp3;
						j++;
					}
				}
			}
			*/

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
			System.out.println("Objective value: " + lp.getObjective());

			/* variable values */
			lp.getVariables(row);
			for(j = 0; j < Ncol; j++)
				System.out.println(lp.getColName(j + 1) + ": " + row[j]);

			//1�ΰŸ� ����
			System.out.println("");
			System.out.println("[elements over 1]");
			for (j = 0; j < Ncol; j++){
				if (row[j] > 0){
					
					String matchResult = lp.getColName(j+1);
					String fullString = matchResult;
					int stringLength = matchResult.length();

					String userNo = fullString.substring(2, stringLength-1);						//���ʿ��� "x_" ��������, ���ʿ��� Data center�� �ǹ��ϴ� ���ڸ� ���� �ձ���
					String epNo = fullString.substring(stringLength-1, stringLength);			//���� ���ڸ� ���ڸ���
					System.out.println("User number: " + userNo + ", EP number: " + epNo);
/*
					
					StringBuffer matchResult = new StringBuffer();
					matchResult.append(lp.getColName(j+1));

					StringBuffer fullString = new StringBuffer();
					fullString.append(matchResult.toString());
					int stringLength = matchResult.length();

					//Data Center ������ �� �ڸ��϶���!
					//���ڸ��� ��ŭ ������ �ٲ����!
					String userNo = fullString.substring(2, stringLength-2).toString();						//���ʿ��� "x_" ��������, ���ʿ��� Data center�� �ǹ��ϴ� ���ڸ� ���� �ձ���
					String epNo = fullString.substring(stringLength-1, stringLength-1).toString();			//���� ���ڸ� ���ڸ���
					System.out.println("User number: " + userNo + ", EP number: " + epNo);
*/
					/**
					 * �� �κп�, DB�� matching ��� ���� �ڵ� ���� 
					 * Example) databaseInstance.InsertMatchingTable(sUserNo, sEpNo);
					 */
				}
			}

			/* we are done now */
		}

		/* clean up such that all used memory by lpsolve is freed */
		if(lp.getLp() != 0)
			lp.deleteLp();


		/**
		 * �� �κп�, �� �� matching�� ���ؼ� �ٸ��� ������Ʈ �ϴ� ������ ����
		 * (Prev ���̶� �ٸ� �͸� �����ؼ� update_matching_table�� ����)
		 * 
		 * Example)
		 * user�� EP�� ��Ī�Ҷ� weight_table�� �ִ� user, user_no ���� ���� ��Ī �ؾ��Ѵ�.
		 * databaseInstance.InsertUpdateMatchingTable();
		 * 
		 * ������ �о�ͼ� ���Ϳ� ������ ������ �������� ������
		 * vector <match_result_data> vecMatchResult = databaseInstance.ExtractMatchResult();
		 * 
		 * �����ְ� ���� prev ���̺� ������Ʈ
		 * databaseInstance.UpdatePrevMatchingTable(vecMatchResult);
		 * 
		 * �����ͺ��̽� Ŀ�ؼ� ����
		 * databaseInstance.DeleteTables();
		 * databaseInstance.CloseDB(); 
		 * databaseInstance.~CDatabase();
		 */

		return(ret);
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		System.out.println("LP thread running..");
		
		//Database�� EP���� ��ŭ Column ���� Table ����
		CDatabase databaseInstance = new CDatabase();
		databaseInstance.connectBrokerDatabase();
		databaseInstance.createTable(NUM_OF_EP);
		databaseInstance.disconnectBrokerDatabase();
		
		//
		checkRecvComplete();
	}
}
