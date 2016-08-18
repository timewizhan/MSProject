
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import lpsolve.*;

public class CLPCalculation implements Runnable {

	//165.132.122.244, 165.132.123.73, localhost
	static final int NUM_OF_EP = 3; // Number of clouds
	
	public void checkRecvComplete(){
		
		while(true) {
			
			if(NUM_OF_EP == Counter.GetInstance().getRecvCompletedCount()){
				System.out.println("all the data was received");
				break;
			}
		}
		
		// 사용자와 데이터센터 간의 매칭 알고리즘
		doMatchAlgorithm();
	}
	
	public void doMatchAlgorithm(){
		
		//각 팩터(Factors: Server Traffic, Distance, Social Level, Cost) 정규화
		Normalization norm = new Normalization();
		norm.normalizeFactors();

		System.out.println("!!!!!!!!!!!!!!!");
		
		//각 엣지에 대한 가중치(weight) 계산
		calculateWeight();
		
		/*
		try {
			calculateLP();
		} catch (LpSolveException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		CLBCalculation lbCalculation = new CLBCalculation();
		lbCalculation.lbMain();
		*/
	}
	
	public void calculateWeight(){
		
	}
	
	public void getWeight() {
		
	}
	
	public int calculateLP() throws LpSolveException{

		///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		//초기화
		LpSolve lp;
		int Ncol, j, ret = 0;

		/* We will build the model row by row
            So we start with creating a model with 0 rows and 2 columns */
		//Ncol = 2; /* there are two variables in the model */

		int numOfUsers = 0;
		/**
		 * numOfUsers = vecClientData.size(); //이 부분은 이런식으로, 데이터 베이스에서 사용자 수 받아온다
		 */
		Ncol = numOfUsers * NUM_OF_EP;

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
			for (int i = 0; i < numOfUsers; i++) {		//user 숫자 만큼 반복

				for (int k = 0; k < NUM_OF_EP; k++) {		//data center 숫자 만큼 반복

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
			for (int i= 0; i < numOfUsers * NUM_OF_EP; i++){

				colno[j] = i + 1;
				row[j++] = 1;

				if (((i + 1) % NUM_OF_EP) == 0){

					/* add the row to lpsolve */
					lp.addConstraintex(j, row, colno, LpSolve.LE, 1);
					j = 0;
				}
			}
		}

		//contraint2 : 각 데이터 센터가 다수의 유저에게 매칭될 수 있는 조건 : x_11 + x_21 + x_31 + x_n1 <= n (n은 user 수 : numOfUsers)

		if(ret == 0) {

			j = 0; int p = 1; int tmp = 0;
			for (int i = 0; i < NUM_OF_EP; i++){

				tmp = p;
				for (int k = 0; k < numOfUsers; k++){

					colno[j] = tmp;
					row[j++] = 1;

					tmp = tmp + NUM_OF_EP;
				}

				/* add the row to lpsolve */
				lp.addConstraintex(j, row, colno, LpSolve.LE, numOfUsers);
				p++; j = 0;
			}
		}

		//contraint3 : 매칭된 모든 엣지의 합이 최소 유저의 숫자보다 커야되는 조건 (각 유저는 최소 1개 이상의 데이터 센터와 매칭 되야한다)

		if(ret == 0) {

			j = 0;
			for (int i = 0; i < numOfUsers*NUM_OF_EP; i++){
				colno[j] = i + 1;
				row[j++] = 1;
			}

			/* add the row to lpsolve */
			lp.addConstraintex(j, row, colno, LpSolve.GE, numOfUsers);
		}

		//contraint4 : 모든 엣지의 값은 0보다 크거나 같아야 한다는 조건

		if (ret == 0){

			for (int i = 0; i < numOfUsers * NUM_OF_EP; i++){

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

		/**
		 *  여기에서 아래와 같이, 데이터베이스 인스턴스 만들고, weight값을 불러와야함
		 *	CDatabase	databaseInstance;
		 *	databaseInstance.InitDB();
		 *	vector<weight_data> vecWeightData = databaseInstance.ExtractWeightData();
		 */

		if(ret == 0) {
			lp.setAddRowmode(false); /* rowmode should be turned off again when done building the model */

			j = 0;
			/**
			 * 가중치가 적용된, 최소가 되야하는 objective 수식이 아래와 같이 들어가야 함
			 * 

			//colno[j] = 1 : 첫번째 column이라는 의미
			//row[j] = 143 : 계수에 143 대입
			//colno[j] = 2 : 두번째 column이라는 의미
			//row[j] = 60 : 계수에 60 대입


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
			System.out.println("Objective value: " + lp.getObjective());

			/* variable values */
			lp.getVariables(row);
			//  for(j = 0; j < Ncol; j++)
			//    System.out.println(lp.getColName(j + 1) + ": " + row[j]);

			//1인거만 뽑자
			System.out.println("");
			System.out.println("[elements over 1]");
			for (int q = 0; q < Ncol; q++){
				if (row[q] > 0){
					StringBuffer matchResult = new StringBuffer();
					matchResult.append(lp.getColName(q+1));

					StringBuffer fullString = new StringBuffer();
					fullString.append(matchResult.toString());
					int stringLength = matchResult.length();

					//Data Center 개수가 한 자리일때만!
					//두자리수 만큼 있으면 바꿔야함!
					String userNo = fullString.substring(2, stringLength-2).toString();						//앞쪽에서 "x_" 다음부터, 뒤쪽에서 Data center를 의미하는 한자리 숫자 앞까지
					String epNo = fullString.substring(stringLength-1, stringLength-1).toString();			//제일 끝자리 한자리만
					System.out.println("User number: " + userNo + ", EP number: " + epNo);

					/**
					 * 이 부분에, DB에 matching 결과 저장 코드 삽입 
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
		 * 이 부분에, 그 전 matching과 비교해서 다른건 업데이트 하는 과정이 들어간다
		 * (Prev 값이랑 다른 것만 추출해서 update_matching_table에 저장)
		 * 
		 * Example)
		 * user랑 EP랑 매칭할때 weight_table에 있는 user, user_no 값에 따라서 매칭 해야한다.
		 * databaseInstance.InsertUpdateMatchingTable();
		 * 
		 * 데이터 읽어와서 벡터에 저장한 다음에 리턴으로 돌려줌
		 * vector <match_result_data> vecMatchResult = databaseInstance.ExtractMatchResult();
		 * 
		 * 돌려주고 나서 prev 테이블 업데이트
		 * databaseInstance.UpdatePrevMatchingTable(vecMatchResult);
		 * 
		 * 데이터베이스 커넥션 끊기
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
		
		//Database에 EP개수 만큼 Column 만들어서 Table 생성
		CDatabase databaseInstance = new CDatabase();
		databaseInstance.connectBrokerDatabase();
		databaseInstance.createTable(NUM_OF_EP);
		databaseInstance.disconnectBrokerDatabase();
		
		//
		checkRecvComplete();
	}
}
