#include "Matching.h"

CMatch::CMatch(){
	/* We will build the model row by row
	So we start with creating a model with 0 rows and 2 columns */
	user_no = 10;
	cloud_no = 3;

	Ncol = user_no * cloud_no;
	lp = make_lp(0, Ncol);
}

CMatch::~CMatch(){

}

void CMatch::NormalizeFactor(){
	
	CDatabase	databaseInstance;
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
	vector <double> vecNormalizedSST;
	for (int i = 0; i < vec_server_side_traffic_list.size(); i++){

		double dNormalizedVal = (vec_server_side_traffic_list.at(i) - iMinValue)*dRange;
		vecNormalizedSST.push_back(dNormalizedVal);
	}

	printf("\n");
	for (int i = 0; i < vecNormalizedSST.size(); i++){
		double dValue = vecNormalizedSST.at(i);
		printf("[%d] normalized server side traffic: %f \n", i, (double)dValue);
	}

	databaseInstance.InsertNormServerTable(vecNormalizedSST, "SST");



	//normalizing >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> cpu utilization
	sQuery = "select * from server_table";
	//vector <server_data> vecDataList_server = databaseInstance.extractServerData(sQuery, 3);
	vecDataList_server = databaseInstance.extractServerData(sQuery, 3);

	vector <int> vec_cpu_util_list;
	for (int i = 0; i < vecDataList_server.size(); i++){
		int iEachCpuUtil = vecDataList_server.at(i).iCpuUtil;
		vec_cpu_util_list.push_back(iEachCpuUtil);
	}

	iMaxValue = FindMax(vec_cpu_util_list);
	iMinValue = FindMin(vec_cpu_util_list);
	 
	dRange = 1.0 / (iMaxValue - iMinValue);
	vector <double> vecNormalizedCpuUtil;
	for (int i = 0; i < vec_cpu_util_list.size(); i++){

		double dNormalizedVal = (vec_cpu_util_list.at(i) - iMinValue)*dRange;
		vecNormalizedCpuUtil.push_back(dNormalizedVal);
	}

	printf("\n");
	for (int i = 0; i < vecNormalizedCpuUtil.size(); i++){
		double dValue = vecNormalizedCpuUtil.at(i);
		printf("[%d] normalized cpu utilization: %f \n", i, (double)dValue);
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

	printf("\n");
	for (int i = 0; i < vecNormalizedCST.size(); i++){
	double dValue = vecNormalizedCST.at(i);
	printf("[%d] normalized client side taffic/location: %f/%s \n", i, (double)dValue, vecNormalizedCSTLocation.at(i).c_str());
	}

	databaseInstance.InsertNormCstTable(vecNormalizedCST, vecNormalizedCSTLocation);
//	databaseInstance.InsertNormCstTable(vecNormalizedCSTLocation, "LOCATION");
	


	//normalizing >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> distance 
	sQuery = "select * from client_table";
	vecDataList_client = databaseInstance.extractClientData(sQuery, 4);

	vector <int> vec_distance_list;
	for (int i = 0; i < vecDataList_client.size(); i++){
		string sEachLocation = vecDataList_client.at(i).sLocation;

		// 여기에 세개의 데이터센터와 거리재는 함수
			//int iDistFromEP1 = EP1과의 거리;
			//int iDistFromEP2 = EP2와의 거리;
			//int iDistFromEP3 = EP3와의 거리;
			//int arrDists[3] = {iDistFromEP1, iDistFromEP2, iDistFromEP3};

		// 세개 중에서 최대값 최소값 구하는 함수 호출
			//int iMaxValue = FindMaxDist(arrDists);
			//int iMinValue = FindMinDist(arrDists);

		//normalize
			//double dRange = 1.0 / (iMaxValue - iMinValue);
			//for (int i = 0; i < arrDists.size(); i++){

			//	double dNormalizedVal = (arrDists[i] - iMinValue)*dRange;
			//	arrDists[i] = dNormalizedVal;
			//}

		// 여기서 노멀라이즈한 값 테이블에 없데이트.. 
		//테이블은 User, EP1, EP2, EP3 컬럼을 가지고 각 ROW에는 USER 아이디와 각 EP에 대한 거리의 NORMALIZED 값이 들어감
		//테이블 명은 distance_table
	}

	CalculateLP();
}

void CMatch::CalculateLP(){

	if (lp == NULL)		ret = 1; 

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	//각 변수에 이름 붙여주고, 변수에 대한 메모리 할당
	if (ret == 0) {
		
		int p = 1;
		for (int i = 0; i < user_no; i++) {		//user 숫자 만큼 반복

			for (int j = 0; j < cloud_no; j++) {		//data center 숫자 만큼 반복

				string base = "x_";

				string user_index = to_string(i + 1);
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
	if (ret == 0) {
		set_add_rowmode(lp, FALSE); 


		j = 0;

		for (int i = 0; i < user_no*cloud_no; i++){

			colno[j] = i + 1;
			row[j++] = 1;
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
		printf("Objective value: %f\n", get_objective(lp));

		get_variables(lp, row);
		for (j = 0; j < Ncol; j++)
			printf("%s: %f\n", get_col_name(lp, j + 1), row[j]);

		//1인거만 뽑자
		printf("\n[elements over 1] \n");
		for (int j = 0; j < Ncol; j++){
			if (row[j] > 0){
				printf("%s \n", get_col_name(lp, j + 1));
			}
		}
	}

	if (row != NULL)
		free(row);
	if (colno != NULL)
		free(colno);

	if (lp != NULL) {
		delete_lp(lp);
	}

	
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