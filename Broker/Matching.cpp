#include "Matching.h"

CMatch::CMatch(){
	/* We will build the model row by row
	So we start with creating a model with 0 rows and 2 columns */
	CDatabase	databaseInstance;
	databaseInstance.InitDB();
	vector<client_data> vecClientData = databaseInstance.extractClientData("select * from client_table");
	
	user_no = vecClientData.size();;
	cloud_no = 3;

	Ncol = user_no * cloud_no;
	lp = make_lp(0, Ncol);

	databaseInstance.CloseDB();
	databaseInstance.~CDatabase();
}

CMatch::~CMatch(){

}

void CMatch::NormalizeFactor(){
	
	CDatabase	databaseInstance;
	databaseInstance.InitDB();
	string sQuery;
	


	//normalizing >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> server side traffic
	sQuery = "select * from server_table";
	vector <server_data> vecDataList_server = databaseInstance.extractServerData(sQuery, 3);

	vector <int> vec_server_side_traffic_list;
	for (int i = 0; i < vecDataList_server.size(); i++){
		int iEachSST = vecDataList_server.at(i).iServerSideTraffic;
		vec_server_side_traffic_list.push_back(iEachSST);
	}

	int iMaxValue = FindMax(vec_server_side_traffic_list);
	int iMinValue = FindMin(vec_server_side_traffic_list);

	double dRange = 1.0 / (iMaxValue - iMinValue);
	
	norm_server_data stNormServerData;
	vector <norm_server_data> vecNormalizedSST;
	for (int i = 0; i < vec_server_side_traffic_list.size(); i++){

		double dNormalizedVal = (vec_server_side_traffic_list.at(i) - iMinValue)*dRange;
		
		stNormServerData.sEpNum = vecDataList_server.at(i).sEpNum;
		stNormServerData.dServerSideTraffic = dNormalizedVal;
		
		vecNormalizedSST.push_back(stNormServerData);
	}

	databaseInstance.InsertNormServerTable(vecNormalizedSST, "SST");



	//normalizing >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> cpu utilization
	sQuery = "select * from server_table";
	vecDataList_server = databaseInstance.extractServerData(sQuery, 3);

	vector <int> vec_cpu_util_list;
	for (int i = 0; i < vecDataList_server.size(); i++){
		int iEachCpuUtil = vecDataList_server.at(i).iCpuUtil;
		vec_cpu_util_list.push_back(iEachCpuUtil);
	}

	iMaxValue = FindMax(vec_cpu_util_list);
	iMinValue = FindMin(vec_cpu_util_list);
	 
	dRange = 1.0 / (iMaxValue - iMinValue);

//	norm_server_data stNormServerData;
	vector <norm_server_data> vecNormalizedCpuUtil;
	for (int i = 0; i < vec_cpu_util_list.size(); i++){

		double dNormalizedVal = (vec_cpu_util_list.at(i) - iMinValue)*dRange;
		
		stNormServerData.sEpNum = vecDataList_server.at(i).sEpNum;
		stNormServerData.dCpuUtil = dNormalizedVal;

		vecNormalizedCpuUtil.push_back(stNormServerData);
	}

	databaseInstance.InsertNormServerTable(vecNormalizedCpuUtil, "CPU");



	//normalizing >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> client side traffic 
	sQuery = "insert into cst_table (client_side_traffic, location) select client_side_traffic, location from client_table group by location";
	databaseInstance.DeleteDuplicateValues(sQuery);
	
	sQuery = "select * from cst_table";
	vector <client_data> vecDataList_client = databaseInstance.ExtractCstData(sQuery);
		
	vector <int> vec_client_side_traffic_list;
	for (int i = 0; i < vecDataList_client.size(); i++){
	
		int iEachCST = vecDataList_client.at(i).iClientSideTraffic;
		vec_client_side_traffic_list.push_back(iEachCST);
	}

	iMaxValue = FindMax(vec_client_side_traffic_list);
	iMinValue = FindMin(vec_client_side_traffic_list);

	dRange = 1.0 / (iMaxValue - iMinValue);
	vector <double> vecNormalizedCST;
	for (int i = 0; i < vec_client_side_traffic_list.size(); i++){

		double dNormalizedVal = (vec_client_side_traffic_list.at(i) - iMinValue)*dRange;
		vecNormalizedCST.push_back(dNormalizedVal);
	}

	vector <string> vecNormalizedCSTLocation = databaseInstance.ExtractCstLocation();

	databaseInstance.InsertNormCstTable(vecNormalizedCST, vecNormalizedCSTLocation);
	


	//normalizing >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> distance 
	sQuery = "select * from client_table";
	vecDataList_client = databaseInstance.extractClientData(sQuery);

	vector <int> vec_distance_list;
	for (int i = 0; i < vecDataList_client.size(); i++){
		string sEachLocation = vecDataList_client.at(i).sLocation;
		
		//여기에 세개의 데이터센터와 거리재는 함수
		//EP1(Washington : 38.9071923, -77.0368707)
		//EP2(Texas		 : 31.9685988, -99.9018131)
		//EP3(Newyork	 : 40.7127837, -74.0059413)

		coord_value stUserCoordValue = databaseInstance.ExtractCoordValue(sEachLocation);

//		printf("[%s] stUserCoordValue: lat- %f, lon- %f \n", sEachLocation.c_str(), stUserCoordValue.latitude, stUserCoordValue.longitude);

		double iDistFromEP1 = CalculateDistEp1(stUserCoordValue); //EP1과의 거리
		double iDistFromEP2 = CalculateDistEp2(stUserCoordValue); //EP2와의 거리
		double iDistFromEP3 = CalculateDistEp3(stUserCoordValue); //EP3와의 거리
		
//		printf("location: %s, EP1 Dist: %f, EP2 Dist: %f, EP3 Dist: %f \n"
//			, sEachLocation.c_str(), iDistFromEP1, iDistFromEP2, iDistFromEP3);

		double arrDists[3] = {iDistFromEP1, iDistFromEP2, iDistFromEP3};

		//세개 중에서 최대값 최소값 구하는 함수 호출
		double iMaxValue = FindMaxDist(arrDists);
		double iMinValue = FindMinDist(arrDists);

		//normalize
		double dRange = 1.0 / (iMaxValue - iMinValue);
		for (int i = 0; i < sizeof(arrDists)/sizeof(double); i++){

			double dNormalizedVal = (arrDists[i] - iMinValue)*dRange;
			arrDists[i] = dNormalizedVal;
		}

		//여기서 노멀라이즈한 값 테이블에 없데이트.. 
		databaseInstance.InsertNormDistTable(vecDataList_client.at(i).sUser, arrDists[0], arrDists[1], arrDists[2]);
	}

	databaseInstance.CloseDB();
	databaseInstance.~CDatabase();
	//CalculateLP();
}

double CMatch::CalculateDistEp1(coord_value stUserCoordValue){
	//EP1(Washington : 38.9071923, -77.0368707)

	double theta, dist;
	theta = -77.0368707 - stUserCoordValue.longitude;
	dist = sin(DegToRad(38.9071923)) * sin(DegToRad(stUserCoordValue.latitude)) + cos(DegToRad(38.9071923))
		* cos(DegToRad(stUserCoordValue.latitude)) * cos(DegToRad(theta));
	dist = acos(dist);
	dist = RadToDeg(dist);

	dist = dist * 60 * 1.1515;
	dist = dist * 1.609344;    // 단위 mile 에서 km 변환.  
	dist = dist * 1000.0;      // 단위  km 에서 m 로 변환  

	return dist;
}

double CMatch::CalculateDistEp2(coord_value stUserCoordValue){
	//EP2(Texas		 : 31.9685988, -99.9018131)

	double theta, dist;
	theta = -99.9018131 - stUserCoordValue.longitude;
	dist = sin(DegToRad(31.9685988)) * sin(DegToRad(stUserCoordValue.latitude)) + cos(DegToRad(31.9685988))
		* cos(DegToRad(stUserCoordValue.latitude)) * cos(DegToRad(theta));
	dist = acos(dist);
	dist = RadToDeg(dist);

	dist = dist * 60 * 1.1515;
	dist = dist * 1.609344;    // 단위 mile 에서 km 변환.  
	dist = dist * 1000.0;      // 단위  km 에서 m 로 변환  

	return dist;
}

double CMatch::CalculateDistEp3(coord_value stUserCoordValue){
	//EP3(Newyork	 : 40.7127837, -74.0059413)

	double theta, dist;
	theta = -74.0059413 - stUserCoordValue.longitude;
	dist = sin(DegToRad(40.7127837)) * sin(DegToRad(stUserCoordValue.latitude)) + cos(DegToRad(40.7127837))
		* cos(DegToRad(stUserCoordValue.latitude)) * cos(DegToRad(theta));
	dist = acos(dist);
	dist = RadToDeg(dist);

	dist = dist * 60 * 1.1515;
	dist = dist * 1.609344;    // 단위 mile 에서 km 변환.  
	dist = dist * 1000.0;      // 단위  km 에서 m 로 변환  

	return dist;
}

double CMatch::DegToRad(double dDeg){

	return (double)(dDeg * M_PI / (double)180);
}

double CMatch::RadToDeg(double dRad){

	return (double)(dRad * (double)180 / M_PI);
}

void CMatch::InsertWeightTable(){

	// Weight 계산
	// [a: normalized cpu, b: normalized server-side traffic, c: normalized client-side traffic, d: distance, e: social level] = 가중치
	double a = 0.5;
	double b = 0.5;
	double c = 1.0;
	double d = 1.0;
	double e = 1.0;
	CDatabase	databaseInstance;
	databaseInstance.InitDB();
//	vector <norm_server_data> vecNormServData = databaseInstance.ExtractNormServerData();
//	vector <norm_cst_data> vecNormCstData = databaseInstance.ExtractNormCstData();
//	vector <norm_dist_data> vecNormDistData = databaseInstance.ExtractNormDistData();
	string sQuery;
	sQuery = "select * from client_table";
	vector<client_data> vecClientData = databaseInstance.extractClientData(sQuery);

	double dCpuUtil = 0;
	double dSst = 0;
	double dCst = 0;
	double dEp1Dist = 0;
	double dEp2Dist = 0;
	double dEp3Dist = 0;
	double dEp1SocialLvl = 0;
	double dEp2SocialLvl = 0;
	double dEp3SocialLvl = 0;
	double dWeight_EP1 = 0;
	double dWeight_EP2 = 0;
	double dWeight_EP3 = 0;
	norm_server_data stNormServerData;
	norm_cst_data stNormCstData;

	for (int i = 0; i < vecClientData.size(); i++){		//모든 유저에 대해서...

	string sUser;
	sUser = vecClientData.at(i).sUser;
	//user1, ep1
		
		//SST, CPU
		sQuery = "select * from normalized_server_table where EP = 'EP1'";
		stNormServerData = databaseInstance.ExtractNormServerData(sQuery);
		dCpuUtil = stNormServerData.dCpuUtil;
		dSst = stNormServerData.dServerSideTraffic;

		//CST
		sQuery = "select A.user, A.location, B.client_side_traffic from client_table A join normalized_cst_table B on A.location = B.location where A.user ='" + sUser + "'";
		stNormCstData = databaseInstance.ExtractNormCstData(sQuery);
		dCst = stNormCstData.dCst;

		//DIST
		sQuery = "select user, ep1 from normalized_distance_table where user ='" + sUser + "'";
		dEp1Dist = databaseInstance.ExtractNormDistData(sQuery);

		//Social Level
	//	sQuery = "select user, ep1 from normalized_social_level_table where user ='" + sUser + "'";
	//	dEp1SocialLvl = databaseInstance.ExtractNormSocialLevelData(sQuery);

		//weight
	//	dWeight_EP1 = a*dCpuUtil + b*dSst + c*dCst + d*dEp1Dist + e*dEp1SocialLvl;
		dWeight_EP1 = a*dCpuUtil + b*dSst + c*dCst + d*dEp1Dist;


	//user1, ep2
	
		//SST, CPU
		sQuery = "select * from normalized_server_table where EP = 'EP2'";
		stNormServerData = databaseInstance.ExtractNormServerData(sQuery);
		dCpuUtil = stNormServerData.dCpuUtil;
		dSst = stNormServerData.dServerSideTraffic;

		//CST
		sQuery = "select A.user, A.location, B.client_side_traffic from client_table A join normalized_cst_table B on A.location = B.location where A.user ='" + sUser + "'";
		stNormCstData = databaseInstance.ExtractNormCstData(sQuery);
		dCst = stNormCstData.dCst;

		//DIST
		sQuery = "select user, ep2 from normalized_distance_table where user ='" + sUser + "'";
		dEp2Dist = databaseInstance.ExtractNormDistData(sQuery);

		//Social Level
	//	sQuery = "select user, ep2 from normalized_social_level_table where user ='" + sUser + "'";
	//	dEp2SocialLvl = databaseInstance.ExtractNormSocialLevelData(sQuery);

		//weight
	//	dWeight_EP2 = a*dCpuUtil + b*dSst + c*dCst + d*dEp2Dist + e*dEp2SocialLvl;
		dWeight_EP2 = a*dCpuUtil + b*dSst + c*dCst + d*dEp2Dist;
	

	//user1, ep3
	
		//SST, CPU
		sQuery = "select * from normalized_server_table where EP = 'EP3'";
		stNormServerData = databaseInstance.ExtractNormServerData(sQuery);
		dCpuUtil = stNormServerData.dCpuUtil;
		dSst = stNormServerData.dServerSideTraffic;

		//CST
		sQuery = "select A.user, A.location, B.client_side_traffic from client_table A join normalized_cst_table B on A.location = B.location where A.user ='" + sUser + "'";
		stNormCstData = databaseInstance.ExtractNormCstData(sQuery);
		dCst = stNormCstData.dCst;

		//DIST
		sQuery = "select user, ep3 from normalized_distance_table where user ='" + sUser + "'";
		dEp3Dist = databaseInstance.ExtractNormDistData(sQuery);

		//Social Level
	//	sQuery = "select user, ep3 from normalized_social_level_table where user ='" + sUser + "'";
	//	dEp3SocialLvl = databaseInstance.ExtractNormSocialLevelData(sQuery);

		//weight
	//	dWeight_EP3 = a*dCpuUtil + b*dSst + c*dCst + d*dEp3Dist + e*dEp3SocialLvl;
		dWeight_EP3 = a*dCpuUtil + b*dSst + c*dCst + d*dEp3Dist;




//		if (i == 0){
//			printf("ep1 - sst: %f, cpu: %f, cst: %f, dis: %f", 
//			vecNormServData.at(0).dServerSideTraffic, vecNormServData.at(0).dCpuUtil, vecNormCstData.at(i).dCst, vecNormDistData.at(i).dEp1);
//		}
		//user1의 ep1, ep2, ep3에 대한 가중치 DB에 넣기
		databaseInstance.InsertWeightTable(sUser, i, dWeight_EP1, dWeight_EP2, dWeight_EP3);
		
	}

	databaseInstance.CloseDB();
	databaseInstance.~CDatabase();
}

vector <match_result_data> CMatch::CalculateLP(){
	
//	printf("CalculateLP method \n");

	if (lp == NULL)		ret = 1; 

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	//각 변수에 이름 붙여주고, 변수에 대한 메모리 할당
	if (ret == 0) {
		
		int p = 1;
		for (int i = 0; i < user_no; i++) {		//user 숫자 만큼 반복

			for (int j = 0; j < cloud_no; j++) {		//data center 숫자 만큼 반복

				string base = "x_";

				string user_index = to_string(i);
				base.append(user_index);

				string cloud_index = to_string(j + 1);
				base.append(cloud_index);

				set_col_name(lp, p++, const_cast<char *>(base.c_str()));
			}
		}

		colno = (int *)malloc(Ncol * sizeof(*colno));
		row = (REAL *)malloc(Ncol * sizeof(*row));

		if ((colno == NULL) || (row == NULL))		ret = 2;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	//각 유저가 다수의 데이터 센터 중에서도 하나에만 매칭 되야하는 조건 : x_11 + x_12 + x_13 <= 1
	if (ret == 0) {
		set_add_rowmode(lp, TRUE);

		j = 0;
		for (int i = 0; i < user_no*cloud_no; i++){

			//			printf("colno[%d] = %d ", j, i + 1);
			//			printf("row[%d] = %d \n", j, 1);

			colno[j] = i + 1;
			row[j++] = 1;

			if (((i + 1) % cloud_no) == 0){
				if (!add_constraintex(lp, j, row, colno, LE, 1))
					ret = 3;

				j = 0;
			}
		}
	}

	//각 데이터 센터가 다수의 유저에게 매칭될 수 있는 조건 : x_11 + x_21 + x_31 + x_n1 <= n (n은 user 수 : user_no)
	j = 0; int p = 1; int tmp = 0;
	for (int i = 0; i < cloud_no; i++){

		tmp = p;
		for (int k = 0; k < user_no; k++){

			colno[j] = tmp;
			row[j++] = 1;

			tmp = tmp + cloud_no;
		}

		if (!add_constraintex(lp, j, row, colno, LE, user_no))
			ret = 3;

		p++; j = 0;
	}

	//매칭된 모든 엣지의 합이 최소 유저의 숫자보다 커야되는 조건 (각 유저는 최소 1개 이상의 데이터 센터와 매칭 되야한다)
	if (ret == 0) {

		j = 0;
		for (int i = 0; i < user_no*cloud_no; i++){
			colno[j] = i + 1;
			row[j++] = 1;
		}
		if (!add_constraintex(lp, j, row, colno, GE, user_no))
			ret = 3;
	}

	//모든 엣지의 값은 0보다 크거나 같아야 한다는 조건
	if (ret == 0){

		for (int i = 0; i < user_no * cloud_no; i++){

			j = 0;
			colno[j] = i + 1;
			row[j++] = 1;

			if (!add_constraintex(lp, j, row, colno, GE, 0))
				ret = 3;
		}
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	//min 이나 max로 만들어야 하는 objective function 만들어주는 곳. 
	//여기에 row 배열 이용해서 가중치 넣어야함
	//나중에 상황에 맞게 코드 수정해야함
	CDatabase	databaseInstance;
	databaseInstance.InitDB();
	vector<weight_data> vecWeightData = databaseInstance.ExtractWeightData();

	if (ret == 0) {
		set_add_rowmode(lp, FALSE); 

		/*******************************************/
/*		j = 0;
		for (int i = 0; i < user_no*cloud_no; i++){
			printf("\n j= %d, i= %d, colno[%d] = %d, row[%d] = 1", j, i, j, i+1, j);
			colno[j] = i + 1;
			row[j++] = 1;
		}
*/		/*******************************************/
		j = 0;
		for (int i = 0; i < user_no; i++){
			for (int p = 0; p < cloud_no; p++){

				if ((p + 1 % cloud_no) == 1){
		//			printf("\n j= %d, i= %d, p= %d, colno[%d] = %d, row[%d] = %f", j, i, p, j, j + 1, j, vecWeightData.at(i).dEp1);
					colno[j] = j + 1;
					row[j] = vecWeightData.at(i).dEp1;
					j++;
				}
				else if ((p + 1 % cloud_no) == 2){
		//			printf("\n j= %d, i= %d, p= %d, colno[%d] = %d, row[%d] = %f", j, i, p, j, j + 1, j, vecWeightData.at(i).dEp2);
					colno[j] = j + 1;
					row[j] = vecWeightData.at(i).dEp2;
					j++;
				}
				else if ((p + 1 % cloud_no) == 3){
		//			printf("\n j= %d, i= %d, p= %d, colno[%d] = %d, row[%d] = %f", j, i, p, j, j + 1, j, vecWeightData.at(i).dEp3);
					colno[j] = j + 1;
					row[j] = vecWeightData.at(i).dEp3;
					j++;
				}
			}
		}

		if (!set_obj_fnex(lp, j, row, colno))
			ret = 4;

	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	if (ret == 0) {
	
		set_minim(lp);

		write_LP(lp, stdout);
	
		set_verbose(lp, IMPORTANT);

		ret = solve(lp);
		if (ret == OPTIMAL)
			ret = 0;
		else
			ret = 5;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	if (ret == 0) {
		printf("\n");
		printf("Objective value: %f \n", get_objective(lp));

		get_variables(lp, row);
	//	for (j = 0; j < Ncol; j++)
	//		printf("%s: %f\n", get_col_name(lp, j + 1), row[j]);

		//1인거만 뽑자
		printf("\n[elements over 1] \n");
		for (int j = 0; j < Ncol; j++){
			if (row[j] > 0){
				string sMatchResult = get_col_name(lp, j + 1);
			//	printf("%s, length: %d \n", sMatchResult.c_str(), sMatchResult.length());
			//	printf("%s \n", get_col_name(lp, j + 1));

				//string 잘라내서 (substr) 매치 결과 뽑아내기
				string sFullString = sMatchResult;
				int iStringLength = sMatchResult.length();
				string sUserNo = sFullString.substr(2, iStringLength-3);
				string sEpNo = sFullString.substr(iStringLength-1, 1);
				printf("User number : %s, ", sUserNo.c_str());
				printf("EP number : %s \n", sEpNo.c_str());

				//DB에 matching 결과 저장
				databaseInstance.InsertMatchingTable(sUserNo, sEpNo);
			}
		}
	}

	if (row != NULL)	free(row);
	if (colno != NULL)	free(colno);
	if (lp != NULL)		delete_lp(lp); 


	//여기서 Prev 값이랑 다른 것만 추출해서 update_matching_table에 저장
	//user랑 EP랑 매칭할때 weight_table에 있는 user, user_no 값에 따라서 매칭 해야한다.
	databaseInstance.InsertUpdateMatchingTable();
	
	//데이터 읽어와서 벡터에 저장한 다음에 리턴으로 돌려줌
	vector <match_result_data> vecMatchResult = databaseInstance.ExtractMatchResult();

	//돌려주고 나서 prev 테이블 업데이트
	databaseInstance.UpdatePrevMatchingTable(vecMatchResult);

	databaseInstance.DeleteTables();
	databaseInstance.CloseDB();
	databaseInstance.~CDatabase();

	return vecMatchResult;
}

int CMatch::FindMax(vector <int> vec){

	int iMax = vec.at(0);
	for (int i = 0; i < vec.size(); i++){

		if (iMax <= vec.at(i))
			iMax = vec.at(i);
	}

	return iMax;
}

int CMatch::FindMin(vector <int> vec){
	
	int iMin = vec.at(0);
	for (int i = 0; i < vec.size(); i++){

		if (iMin >= vec.at(i))
			iMin = vec.at(i);
	}

	return iMin;
}

double CMatch::FindMaxDist(double *arrDists){

	double iMax = *arrDists;
	for (int i = 0; i < 3; i++){		//EP 개수만큼 반복

		if (iMax <= *(arrDists + i))
			iMax = *(arrDists + i);
	}

	return iMax;
}

double CMatch::FindMinDist(double *arrDists){

	double iMin = *arrDists;
	for (int i = 0; i < 3; i++){		//EP 개수만큼 반복

		if (iMin >= *(arrDists + i))
			iMin = *(arrDists + i);
	}

	return iMin;
}