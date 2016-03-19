/*
* database.cpp
*
*  Created on: Feb 4, 2016
*      Author: alphahacker
*/

#include "Database.h"

CDatabase::CDatabase(){

	InitDB();
}

CDatabase::~CDatabase(){
//	printf("\n DB 소멸 \n");
}

int CDatabase::InitDB(){

//	printf("init db \n");

	mysql_init(&conn);

	connection = mysql_real_connect(&conn, DB_HOST, DB_USER, DB_PASS, DB_NAME, 3306, (char *)NULL, 0);

	if (connection == NULL){

		fprintf(stderr, "Mysql connection error : %s", mysql_error(&conn));
		return 1;

	}
	else if (connection){

	//	printf("connection success \n");
	}

	return 0;
}


vector<client_data> CDatabase::extractClientData(string sQuery){

	m_iQueryStat = mysql_query(connection, sQuery.c_str());
	if (m_iQueryStat != 0) {

		fprintf(stderr, "Mysql query error : %s", mysql_error(&conn));
	}

	sql_result = mysql_store_result(connection);

	client_data stResData;
	vector <client_data> vecClientData;
	while ((sql_row = mysql_fetch_row(sql_result)) != NULL) {

		stResData.sUser = sql_row[0];
		stResData.sLocation = sql_row[1];
		stResData.iTimestamp = atoi(sql_row[2]);
		stResData.iClientSideTraffic = atoi(sql_row[3]);

		vecClientData.push_back(stResData);
		//		printf("EP: %s, Server-side traffic: %s, CPU_UTIL: %s \n", sql_row[0], sql_row[1], sql_row[2]);
	}

	mysql_free_result(sql_result);

	return vecClientData;
}

vector<server_data> CDatabase::extractServerData(string sQuery, int iNumOfColumn){

	m_iQueryStat = mysql_query(connection, sQuery.c_str());
	if (m_iQueryStat != 0) {

		fprintf(stderr, "Mysql query error : %s", mysql_error(&conn));
	}

	sql_result = mysql_store_result(connection);

	server_data stResData;
	vector <server_data> vecServerData;
	while ((sql_row = mysql_fetch_row(sql_result)) != NULL) {

		stResData.sEpNum = sql_row[0];
		stResData.iServerSideTraffic = atoi(sql_row[1]);
		stResData.iCpuUtil = atoi(sql_row[2]);

		vecServerData.push_back(stResData);
		//		printf("EP: %s, Server-side traffic: %s, CPU_UTIL: %s \n", sql_row[0], sql_row[1], sql_row[2]);
	}

	mysql_free_result(sql_result);

	return vecServerData;
}

void CDatabase::DeleteDuplicateValues(string sQuery) {

	m_iQueryStat = mysql_query(connection, sQuery.c_str());
	if (m_iQueryStat != 0) {

		fprintf(stderr, "Mysql query error : %s", mysql_error(&conn));
	}

	sql_result = mysql_store_result(connection);

//	sql_row = mysql_fetch_row(sql_result);
	
	mysql_free_result(sql_result);

}

vector<client_data> CDatabase::ExtractCstData(string sQuery){

	m_iQueryStat = mysql_query(connection, sQuery.c_str());
	if (m_iQueryStat != 0) {

		fprintf(stderr, "Mysql query error : %s", mysql_error(&conn));
	}

	sql_result = mysql_store_result(connection);

	client_data stResData;
	vector <client_data> vecClientData;
	while ((sql_row = mysql_fetch_row(sql_result)) != NULL) {

		stResData.iClientSideTraffic = atoi(sql_row[0]);
		stResData.sLocation = sql_row[1];
	
		vecClientData.push_back(stResData);
		//		printf("EP: %s, Server-side traffic: %s, CPU_UTIL: %s \n", sql_row[0], sql_row[1], sql_row[2]);
	}

	mysql_free_result(sql_result);

	return vecClientData;

}

vector <string> CDatabase::ExtractCstLocation(){

	m_iQueryStat = mysql_query(connection, "select location from cst_table");
	if (m_iQueryStat != 0) {

		fprintf(stderr, "Mysql query error : %s", mysql_error(&conn));
	}

	sql_result = mysql_store_result(connection);

	vector <string> vecClientData;
	string			sResData;
	while ((sql_row = mysql_fetch_row(sql_result)) != NULL) {

		sResData = sql_row[0];
		vecClientData.push_back(sResData);
	//	printf("test / location : %s \n", sql_row[0]);
	//	printf("test / location : %s \n", sResData.c_str());
	}

	mysql_free_result(sql_result);

	return vecClientData;
}

coord_value	CDatabase::ExtractCoordValue(string sLocation){

	string sQuery = "select latitude, longitude from coord_list_table";
	string cond_open = " where state = '";
	string cond_close = "'";

	sQuery = sQuery + cond_open + sLocation + cond_close;
//	printf("query [%s]: %s", sLocation.c_str(), sQuery.c_str());

	m_iQueryStat = mysql_query(connection, sQuery.c_str());
	if (m_iQueryStat != 0) {

		fprintf(stderr, "Mysql query error : %s", mysql_error(&conn));
	}

	sql_result = mysql_store_result(connection);

	coord_value stCoordValue;
	while ((sql_row = mysql_fetch_row(sql_result)) != NULL) {

		stCoordValue.latitude = stod(sql_row[0]);
		stCoordValue.longitude = stod(sql_row[1]);
		//	printf("test / location : %s \n", sql_row[0]);
	}

	mysql_free_result(sql_result);

	return stCoordValue;
}

norm_server_data CDatabase::ExtractNormServerData(string sQuery){
	
	m_iQueryStat = mysql_query(connection, sQuery.c_str());
	if (m_iQueryStat != 0) {

		fprintf(stderr, "Mysql query error : %s", mysql_error(&conn));
	}

	sql_result = mysql_store_result(connection);

	norm_server_data stResValue;
	while ((sql_row = mysql_fetch_row(sql_result)) != NULL) {

		stResValue.sEpNum = sql_row[0];
		stResValue.dServerSideTraffic = stod(sql_row[1]);
		stResValue.dCpuUtil = stod(sql_row[2]);
	}

	mysql_free_result(sql_result);

	return stResValue;
}

norm_cst_data CDatabase::ExtractNormCstData(string sQuery){

	m_iQueryStat = mysql_query(connection, sQuery.c_str());
	if (m_iQueryStat != 0) {

		fprintf(stderr, "Mysql query error : %s", mysql_error(&conn));
	}

	sql_result = mysql_store_result(connection);

	norm_cst_data stResValue;
	while ((sql_row = mysql_fetch_row(sql_result)) != NULL) {

		stResValue.sUser = sql_row[0];	//user
	//	stResValue.		= sql_row[1] // <-- location
		stResValue.dCst = stod(sql_row[2]);	//client_side_traffic
	}

	mysql_free_result(sql_result);

	return stResValue;
}

double CDatabase::ExtractNormDistData(string sQuery){

	m_iQueryStat = mysql_query(connection, sQuery.c_str());
	if (m_iQueryStat != 0) {

		fprintf(stderr, "Mysql query error : %s", mysql_error(&conn));
	}

	sql_result = mysql_store_result(connection);

	double dResValue = 0;
	while ((sql_row = mysql_fetch_row(sql_result)) != NULL) {

	//	stResValue.sUser = sql_row[0];
		dResValue = stod(sql_row[1]);
	//	stResValue.dEp2 = stod(sql_row[2]);
	//	stResValue.dEp3 = stod(sql_row[3]);
	}

	mysql_free_result(sql_result);

	return dResValue;
}

vector<weight_data> CDatabase::ExtractWeightData(){

	string sQuery = "select * from weight_table";

	m_iQueryStat = mysql_query(connection, sQuery.c_str());
	if (m_iQueryStat != 0) {

		fprintf(stderr, "Mysql query error : %s", mysql_error(&conn));
	}

	sql_result = mysql_store_result(connection);

	weight_data stResValue;
	vector <weight_data> vecWeightData;
	while ((sql_row = mysql_fetch_row(sql_result)) != NULL) {

		stResValue.sUser = sql_row[0];
		stResValue.iUserNo = atoi(sql_row[1]);
		stResValue.dEp1 = stod(sql_row[2]);
		stResValue.dEp2 = stod(sql_row[3]);
		stResValue.dEp3 = stod(sql_row[4]);

		vecWeightData.push_back(stResValue);
	}

	mysql_free_result(sql_result);

	return vecWeightData;
}

vector <match_result_data> CDatabase::ExtractMatchResult(){

	string sQuery = "select * from update_matching_table";

	m_iQueryStat = mysql_query(connection, sQuery.c_str());
	if (m_iQueryStat != 0) {

		fprintf(stderr, "Mysql query error : %s", mysql_error(&conn));
	}

	sql_result = mysql_store_result(connection);

	match_result_data stResValue;
	vector <match_result_data> vecMatchResult;
	while ((sql_row = mysql_fetch_row(sql_result)) != NULL) {

	//	stResValue.sUser = sql_row[0];
		strcpy_s(stResValue.arrUser, 40, sql_row[0]);
		stResValue.iPrevEp = atoi(sql_row[2]);
		stResValue.iCurrEP = atoi(sql_row[3]);

		vecMatchResult.push_back(stResValue);
	}

	mysql_free_result(sql_result);

	return vecMatchResult;
}
/*
void CDatabase::insertData(string name, string location, int timestamp, int client_side_traffic, int server_side_traffic, int cpu_util, int ep_num, string side_flag){


	char query[255];

	sprintf_s(query, sizeof(query), "insert into broker_table values ('%s', '%s', %d, %d, %d, %d, %d, '%s')",
		name.c_str(), location.c_str(), timestamp, client_side_traffic, server_side_traffic, cpu_util, ep_num, side_flag.c_str());

	m_iQueryStat = mysql_query(connection, query);

	if (m_iQueryStat != 0){

		fprintf(stderr, "Mysql query error : %s", mysql_error(&conn));
	}

}
*/
void CDatabase::InsertServerTable(int iEP, int server_side_traffic, int cpu_util){

	string sEP; 

	if (iEP == 1){
		sEP = "EP1";
	}
	else if (iEP == 2){
		sEP = "EP2";
	}
	else if (iEP == 3){
		sEP = "EP3";
	}


	char query[255];

	sprintf_s(query, sizeof(query), "insert into server_table values ('%s', %d, %d)",
		sEP.c_str(), server_side_traffic, cpu_util);

	m_iQueryStat = mysql_query(connection, query);

	if (m_iQueryStat != 0){

		fprintf(stderr, "Mysql query error : %s", mysql_error(&conn));
	}

}

void CDatabase::InsertClientTable(string sUser, string sLocation, int iTimestamp, int iClientSideTraffic){


	char query[255];

	sprintf_s(query, sizeof(query), "insert into client_table values ('%s', '%s', %d, %d)",
		sUser.c_str(), sLocation.c_str(), iTimestamp, iClientSideTraffic);

	m_iQueryStat = mysql_query(connection, query);

	if (m_iQueryStat != 0){

		fprintf(stderr, "Mysql query error : %s", mysql_error(&conn));
	}

}

void CDatabase::InsertNormServerTable(vector <norm_server_data> vecNormalizedData, string sFlag){

	char query[255];

	if (!strcmp(sFlag.c_str(), "SST")){
	
		sprintf_s(query, sizeof(query), "insert into normalized_server_table values ('%s', '%f', '%f')", vecNormalizedData.at(0).sEpNum.c_str(), vecNormalizedData.at(0).dServerSideTraffic, 0);

		m_iQueryStat = mysql_query(connection, query);

		if (m_iQueryStat != 0){

			fprintf(stderr, "Mysql query error : %s", mysql_error(&conn));
		}
		
		sprintf_s(query, sizeof(query), "insert into normalized_server_table values ('%s', '%f', '%f')", vecNormalizedData.at(1).sEpNum.c_str(), vecNormalizedData.at(1).dServerSideTraffic, 0);

		m_iQueryStat = mysql_query(connection, query);

		if (m_iQueryStat != 0){

			fprintf(stderr, "Mysql query error : %s", mysql_error(&conn));
		}

		sprintf_s(query, sizeof(query), "insert into normalized_server_table values ('%s', '%f', '%f')", vecNormalizedData.at(2).sEpNum.c_str(), vecNormalizedData.at(2).dServerSideTraffic, 0);

		m_iQueryStat = mysql_query(connection, query);

		if (m_iQueryStat != 0){

			fprintf(stderr, "Mysql query error : %s", mysql_error(&conn));
		}
		
	}
	else if (!strcmp(sFlag.c_str(), "CPU")) {

		//EP1
		sprintf_s(query, sizeof(query), "UPDATE normalized_server_table SET cpu_util = %f WHERE EP = '%s'", vecNormalizedData.at(0).dCpuUtil, vecNormalizedData.at(0).sEpNum.c_str());

		m_iQueryStat = mysql_query(connection, query);
		if (m_iQueryStat != 0){

			fprintf(stderr, "Mysql query error : %s", mysql_error(&conn));
		}

		
		//EP2
		sprintf_s(query, sizeof(query), "UPDATE normalized_server_table SET cpu_util = %f WHERE EP = '%s'", vecNormalizedData.at(1).dCpuUtil, vecNormalizedData.at(1).sEpNum.c_str());

		m_iQueryStat = mysql_query(connection, query);
		if (m_iQueryStat != 0){

			fprintf(stderr, "Mysql query error : %s", mysql_error(&conn));
		}

		//EP3
		sprintf_s(query, sizeof(query), "UPDATE normalized_server_table SET cpu_util = %f WHERE EP = '%s'", vecNormalizedData.at(2).dCpuUtil, vecNormalizedData.at(2).sEpNum.c_str());

		m_iQueryStat = mysql_query(connection, query);
		if (m_iQueryStat != 0){

			fprintf(stderr, "Mysql query error : %s", mysql_error(&conn));
		}
		
	}
}

void CDatabase::InsertNormCstTable(vector <double> vecNormalizedCST, vector <string> vecNormalizedCSTLocation){

	char query[255];

	for (int i = 0; i < vecNormalizedCST.size(); i++){

		sprintf_s(query, sizeof(query), "insert into normalized_cst_table values ('%f', '%s')", vecNormalizedCST.at(i), vecNormalizedCSTLocation.at(i).c_str());

		m_iQueryStat = mysql_query(connection, query);

		if (m_iQueryStat != 0){

			fprintf(stderr, "Mysql query error : %s", mysql_error(&conn));
		}
	}
}

void CDatabase::InsertNormDistTable(string sUser, double dNormDistEp1, double dNormDistEp2, double dNormDistEp3){

	char query[255];

	sprintf_s(query, sizeof(query), "insert into normalized_distance_table values ('%s', '%f', '%f', '%f')", sUser.c_str(), dNormDistEp1, dNormDistEp2, dNormDistEp3);

	m_iQueryStat = mysql_query(connection, query);

	if (m_iQueryStat != 0){

		fprintf(stderr, "Mysql query error : %s", mysql_error(&conn));
	}
}

void CDatabase::InsertWeightTable(string sUser, int iUserNo, double dWeightEp1, double dWeightEp2, double dWeightEp3){

	char query[255];

	sprintf_s(query, sizeof(query), "insert into weight_table values ('%s', '%d', '%f', '%f', '%f')", sUser.c_str(), iUserNo, dWeightEp1, dWeightEp2, dWeightEp3);

	m_iQueryStat = mysql_query(connection, query);

	if (m_iQueryStat != 0){

		fprintf(stderr, "Mysql query error : %s", mysql_error(&conn));
	}
}

void CDatabase::InsertMatchingTable(string sUserNo, string sEpNo){
	
	//weight_table에서 user_no로 user, user_no 가지고 오기
	string sQuery = "select user from weight_table where user_no = '";
	string sCloseQuery = "'";

	sQuery = sQuery + sUserNo.c_str() + sCloseQuery;

	m_iQueryStat = mysql_query(connection, sQuery.c_str());
	if (m_iQueryStat != 0) {

		fprintf(stderr, "Mysql query error : %s", mysql_error(&conn));
	}

	sql_result = mysql_store_result(connection);

	string sUserName;
	while ((sql_row = mysql_fetch_row(sql_result)) != NULL) {

		sUserName = sql_row[0];
	}

	mysql_free_result(sql_result);

	
	//user, user_no 값과 전달 받은 sEpNo 값으로 matching_table에 insert
	char query[255];

	sprintf_s(query, sizeof(query), "insert into matching_table values ('%s', '%s', '%s')", sUserName.c_str(), sUserNo.c_str(), sEpNo.c_str());

	m_iQueryStat = mysql_query(connection, query);

	if (m_iQueryStat != 0){

		fprintf(stderr, "Mysql query error : %s", mysql_error(&conn));
	}
}

void CDatabase::InsertUpdateMatchingTable(){

	boolean bEmptyCheck = CheckPrevTableEmpty();

	if (bEmptyCheck){		//prev_matching_table에 아무 값이 없을때. 즉, 최초 Data replacement 일 때.


		string sQuery = "select user, user_no, curr_ep from matching_table";

		m_iQueryStat = mysql_query(connection, sQuery.c_str());
		if (m_iQueryStat != 0) {

			fprintf(stderr, "Mysql query error : %s", mysql_error(&conn));
		}

		sql_result = mysql_store_result(connection);

		while ((sql_row = mysql_fetch_row(sql_result)) != NULL) {




			char query[255];

			//update_matching_table 값 입력
			sprintf_s(query, sizeof(query), "insert into update_matching_table values ('%s', '%s', '%s', '%s')", sql_row[0], sql_row[1], "0", sql_row[2]);

			m_iQueryStat = mysql_query(connection, query);

			if (m_iQueryStat != 0){

				fprintf(stderr, "Mysql query error : %s", mysql_error(&conn));
			}

			//prev_matching_table 값 0 으로 입력
			sprintf_s(query, sizeof(query), "insert into prev_matching_table values ('%s', '%s')", sql_row[0], "0");

			m_iQueryStat = mysql_query(connection, query);

			if (m_iQueryStat != 0){

				fprintf(stderr, "Mysql query error : %s", mysql_error(&conn));
			}

		}

		mysql_free_result(sql_result);


	}
	else {

		string sQuery = "select A.user, A.user_no, B.prev_ep, A.curr_ep from matching_table A join prev_matching_table B on A.user = B.user";

		m_iQueryStat = mysql_query(connection, sQuery.c_str());
		if (m_iQueryStat != 0) {

			fprintf(stderr, "Mysql query error : %s", mysql_error(&conn));
		}

		sql_result = mysql_store_result(connection);

		while ((sql_row = mysql_fetch_row(sql_result)) != NULL) {




			char query[255];

			sprintf_s(query, sizeof(query), "insert into update_matching_table values ('%s', '%s', '%s', '%s')", sql_row[0], sql_row[1], sql_row[2], sql_row[3]);

			m_iQueryStat = mysql_query(connection, query);

			if (m_iQueryStat != 0){

				fprintf(stderr, "Mysql query error : %s", mysql_error(&conn));
			}
		
		
		
		}

		mysql_free_result(sql_result);
	}
}

boolean CDatabase::CheckPrevTableEmpty(){

	boolean bEmptyCheck;
	string sQuery = "select * from prev_matching_table";
	
	m_iQueryStat = mysql_query(connection, sQuery.c_str());
	if (m_iQueryStat != 0) {

		fprintf(stderr, "Mysql query error : %s", mysql_error(&conn));
	}

	sql_result = mysql_store_result(connection);

	if ((sql_row = mysql_fetch_row(sql_result)) == NULL){		//prev_matching_table에 아무 값이 없을때. 즉, 최초 Data replacement 일 때.
		bEmptyCheck = true;
	}
	else {
		bEmptyCheck = false;
	}

	mysql_free_result(sql_result);

	return bEmptyCheck;
}

//void CDatabase::updateLocation(int l_ny_traffic, int l_bs_traffic, int l_chi_traffic){
void CDatabase::updateLocation(ST_CCT stCCT){

	char query[255];

	//NY
	sprintf_s(query, sizeof(query), "UPDATE client_table SET CLIENT_SIDE_TRAFFIC = %d WHERE LOCATION = 'NY'", stCCT.iNyTraffic);

	m_iQueryStat = mysql_query(connection, query);
	if (m_iQueryStat != 0){

		fprintf(stderr, "Mysql query error : %s", mysql_error(&conn));
	}

	//BS
	sprintf_s(query, sizeof(query), "UPDATE client_table SET CLIENT_SIDE_TRAFFIC = %d WHERE LOCATION = 'BS'", stCCT.iBsTraffic);

	m_iQueryStat = mysql_query(connection, query);
	if (m_iQueryStat != 0){

		fprintf(stderr, "Mysql query error : %s", mysql_error(&conn));
	}

	//CHI
	sprintf_s(query, sizeof(query), "UPDATE client_table SET CLIENT_SIDE_TRAFFIC = %d WHERE LOCATION = 'CHI'", stCCT.iChiTraffic);

	m_iQueryStat = mysql_query(connection, query);
	if (m_iQueryStat != 0){

		fprintf(stderr, "Mysql query error : %s", mysql_error(&conn));
	}

	//CHI
	sprintf_s(query, sizeof(query), "UPDATE client_table SET CLIENT_SIDE_TRAFFIC = %d WHERE LOCATION = 'TEX'", stCCT.iTexTraffic);

	m_iQueryStat = mysql_query(connection, query);
	if (m_iQueryStat != 0){

		fprintf(stderr, "Mysql query error : %s", mysql_error(&conn));
	}

	//CHI
	sprintf_s(query, sizeof(query), "UPDATE client_table SET CLIENT_SIDE_TRAFFIC = %d WHERE LOCATION = 'WA'", stCCT.iWhaTraffic);

	m_iQueryStat = mysql_query(connection, query);
	if (m_iQueryStat != 0){

		fprintf(stderr, "Mysql query error : %s", mysql_error(&conn));
	}






	//CHI
	sprintf_s(query, sizeof(query), "UPDATE client_table SET CLIENT_SIDE_TRAFFIC = %d WHERE LOCATION = 'WASHINGTON'", stCCT.WASHINGTON_TRAFFIC);

	m_iQueryStat = mysql_query(connection, query);
	if (m_iQueryStat != 0){

		fprintf(stderr, "Mysql query error : %s", mysql_error(&conn));
	}

	//CHI
	sprintf_s(query, sizeof(query), "UPDATE client_table SET CLIENT_SIDE_TRAFFIC = %d WHERE LOCATION = 'MONTANA'", stCCT.MONTANA_TRAFFIC);

	m_iQueryStat = mysql_query(connection, query);
	if (m_iQueryStat != 0){

		fprintf(stderr, "Mysql query error : %s", mysql_error(&conn));
	}

	//CHI
	sprintf_s(query, sizeof(query), "UPDATE client_table SET CLIENT_SIDE_TRAFFIC = %d WHERE LOCATION = 'NORTHDAKOTA'", stCCT.NORTHDAKOTA_TRAFFIC);

	m_iQueryStat = mysql_query(connection, query);
	if (m_iQueryStat != 0){

		fprintf(stderr, "Mysql query error : %s", mysql_error(&conn));
	}

	//CHI
	sprintf_s(query, sizeof(query), "UPDATE client_table SET CLIENT_SIDE_TRAFFIC = %d WHERE LOCATION = 'OREGON'", stCCT.OREGON_TRAFFIC);

	m_iQueryStat = mysql_query(connection, query);
	if (m_iQueryStat != 0){

		fprintf(stderr, "Mysql query error : %s", mysql_error(&conn));
	}

	//CHI
	sprintf_s(query, sizeof(query), "UPDATE client_table SET CLIENT_SIDE_TRAFFIC = %d WHERE LOCATION = 'IDAHO'", stCCT.IDAHO_TRAFFIC);

	m_iQueryStat = mysql_query(connection, query);
	if (m_iQueryStat != 0){

		fprintf(stderr, "Mysql query error : %s", mysql_error(&conn));
	}

	//CHI
	sprintf_s(query, sizeof(query), "UPDATE client_table SET CLIENT_SIDE_TRAFFIC = %d WHERE LOCATION = 'WYOMING'", stCCT.WYOMING_TRAFFIC);

	m_iQueryStat = mysql_query(connection, query);
	if (m_iQueryStat != 0){

		fprintf(stderr, "Mysql query error : %s", mysql_error(&conn));
	}

	//CHI
	sprintf_s(query, sizeof(query), "UPDATE client_table SET CLIENT_SIDE_TRAFFIC = %d WHERE LOCATION = 'SOUTHDAKOTA'", stCCT.SOUTHDAKOTA_TRAFFIC);

	m_iQueryStat = mysql_query(connection, query);
	if (m_iQueryStat != 0){

		fprintf(stderr, "Mysql query error : %s", mysql_error(&conn));
	}

	//CHI
	sprintf_s(query, sizeof(query), "UPDATE client_table SET CLIENT_SIDE_TRAFFIC = %d WHERE LOCATION = 'NEBRASKA'", stCCT.NEBRASKA_TRAFFIC);

	m_iQueryStat = mysql_query(connection, query);
	if (m_iQueryStat != 0){

		fprintf(stderr, "Mysql query error : %s", mysql_error(&conn));
	}

	//CHI
	sprintf_s(query, sizeof(query), "UPDATE client_table SET CLIENT_SIDE_TRAFFIC = %d WHERE LOCATION = 'ALASKA'", stCCT.ALASKA_TRAFFIC);

	m_iQueryStat = mysql_query(connection, query);
	if (m_iQueryStat != 0){

		fprintf(stderr, "Mysql query error : %s", mysql_error(&conn));
	}

	//CHI
	sprintf_s(query, sizeof(query), "UPDATE client_table SET CLIENT_SIDE_TRAFFIC = %d WHERE LOCATION = 'CALIFORNIA'", stCCT.CALIFORNIA_TRAFFIC);

	m_iQueryStat = mysql_query(connection, query);
	if (m_iQueryStat != 0){

		fprintf(stderr, "Mysql query error : %s", mysql_error(&conn));
	}

	//CHI
	sprintf_s(query, sizeof(query), "UPDATE client_table SET CLIENT_SIDE_TRAFFIC = %d WHERE LOCATION = 'NEVADA'", stCCT.NEVADA_TRAFFIC);

	m_iQueryStat = mysql_query(connection, query);
	if (m_iQueryStat != 0){

		fprintf(stderr, "Mysql query error : %s", mysql_error(&conn));
	}

	//CHI
	sprintf_s(query, sizeof(query), "UPDATE client_table SET CLIENT_SIDE_TRAFFIC = %d WHERE LOCATION = 'UTAH'", stCCT.UTAH_TRAFFIC);

	m_iQueryStat = mysql_query(connection, query);
	if (m_iQueryStat != 0){

		fprintf(stderr, "Mysql query error : %s", mysql_error(&conn));
	}

	//CHI
	sprintf_s(query, sizeof(query), "UPDATE client_table SET CLIENT_SIDE_TRAFFIC = %d WHERE LOCATION = 'COLORADO'", stCCT.COLORADO_TRAFFIC);

	m_iQueryStat = mysql_query(connection, query);
	if (m_iQueryStat != 0){

		fprintf(stderr, "Mysql query error : %s", mysql_error(&conn));
	}

	//CHI
	sprintf_s(query, sizeof(query), "UPDATE client_table SET CLIENT_SIDE_TRAFFIC = %d WHERE LOCATION = 'KANSAS'", stCCT.KANSAS_TRAFFIC);

	m_iQueryStat = mysql_query(connection, query);
	if (m_iQueryStat != 0){

		fprintf(stderr, "Mysql query error : %s", mysql_error(&conn));
	}

	//CHI
	sprintf_s(query, sizeof(query), "UPDATE client_table SET CLIENT_SIDE_TRAFFIC = %d WHERE LOCATION = 'MISSOURI'", stCCT.MISSOURI_TRAFFIC);

	m_iQueryStat = mysql_query(connection, query);
	if (m_iQueryStat != 0){

		fprintf(stderr, "Mysql query error : %s", mysql_error(&conn));
	}

	//CHI
	sprintf_s(query, sizeof(query), "UPDATE client_table SET CLIENT_SIDE_TRAFFIC = %d WHERE LOCATION = 'ARIZONA'", stCCT.ARIZONA_TRAFFIC);

	m_iQueryStat = mysql_query(connection, query);
	if (m_iQueryStat != 0){

		fprintf(stderr, "Mysql query error : %s", mysql_error(&conn));
	}

	//CHI
	sprintf_s(query, sizeof(query), "UPDATE client_table SET CLIENT_SIDE_TRAFFIC = %d WHERE LOCATION = 'NEWMEXICO'", stCCT.NEWMEXICO_TRAFFIC);

	m_iQueryStat = mysql_query(connection, query);
	if (m_iQueryStat != 0){

		fprintf(stderr, "Mysql query error : %s", mysql_error(&conn));
	}

	//CHI
	sprintf_s(query, sizeof(query), "UPDATE client_table SET CLIENT_SIDE_TRAFFIC = %d WHERE LOCATION = 'TEXAS'", stCCT.TEXAS_TRAFFIC);

	m_iQueryStat = mysql_query(connection, query);
	if (m_iQueryStat != 0){

		fprintf(stderr, "Mysql query error : %s", mysql_error(&conn));
	}

	//CHI
	sprintf_s(query, sizeof(query), "UPDATE client_table SET CLIENT_SIDE_TRAFFIC = %d WHERE LOCATION = 'OKLAHOMA'", stCCT.OKLAHOMA_TRAFFIC);

	m_iQueryStat = mysql_query(connection, query);
	if (m_iQueryStat != 0){

		fprintf(stderr, "Mysql query error : %s", mysql_error(&conn));
	}

	//CHI
	sprintf_s(query, sizeof(query), "UPDATE client_table SET CLIENT_SIDE_TRAFFIC = %d WHERE LOCATION = 'ARKANSAS'", stCCT.ARKANSAS_TRAFFIC);

	m_iQueryStat = mysql_query(connection, query);
	if (m_iQueryStat != 0){

		fprintf(stderr, "Mysql query error : %s", mysql_error(&conn));
	}

	//CHI
	sprintf_s(query, sizeof(query), "UPDATE client_table SET CLIENT_SIDE_TRAFFIC = %d WHERE LOCATION = 'LOUISIANA'", stCCT.LOUISIANA_TRAFFIC);

	m_iQueryStat = mysql_query(connection, query);
	if (m_iQueryStat != 0){

		fprintf(stderr, "Mysql query error : %s", mysql_error(&conn));
	}

	//CHI
	sprintf_s(query, sizeof(query), "UPDATE client_table SET CLIENT_SIDE_TRAFFIC = %d WHERE LOCATION = 'HAWAII'", stCCT.HAWAII_TRAFFIC);

	m_iQueryStat = mysql_query(connection, query);
	if (m_iQueryStat != 0){

		fprintf(stderr, "Mysql query error : %s", mysql_error(&conn));
	}

	//CHI
	sprintf_s(query, sizeof(query), "UPDATE client_table SET CLIENT_SIDE_TRAFFIC = %d WHERE LOCATION = 'MINNESOTA'", stCCT.MINNESOTA_TRAFFIC);

	m_iQueryStat = mysql_query(connection, query);
	if (m_iQueryStat != 0){

		fprintf(stderr, "Mysql query error : %s", mysql_error(&conn));
	}

	//CHI
	sprintf_s(query, sizeof(query), "UPDATE client_table SET CLIENT_SIDE_TRAFFIC = %d WHERE LOCATION = 'WISCONSIN'", stCCT.WISCONSIN_TRAFFIC);

	m_iQueryStat = mysql_query(connection, query);
	if (m_iQueryStat != 0){

		fprintf(stderr, "Mysql query error : %s", mysql_error(&conn));
	}

	//CHI
	sprintf_s(query, sizeof(query), "UPDATE client_table SET CLIENT_SIDE_TRAFFIC = %d WHERE LOCATION = 'MICHIGAN'", stCCT.MICHIGAN_TRAFFIC);

	m_iQueryStat = mysql_query(connection, query);
	if (m_iQueryStat != 0){

		fprintf(stderr, "Mysql query error : %s", mysql_error(&conn));
	}

	//CHI
	sprintf_s(query, sizeof(query), "UPDATE client_table SET CLIENT_SIDE_TRAFFIC = %d WHERE LOCATION = 'IOWA'", stCCT.IOWA_TRAFFIC);

	m_iQueryStat = mysql_query(connection, query);
	if (m_iQueryStat != 0){

		fprintf(stderr, "Mysql query error : %s", mysql_error(&conn));
	}

	//CHI
	sprintf_s(query, sizeof(query), "UPDATE client_table SET CLIENT_SIDE_TRAFFIC = %d WHERE LOCATION = 'ILLINOIS'", stCCT.ILLINOIS_TRAFFIC);

	m_iQueryStat = mysql_query(connection, query);
	if (m_iQueryStat != 0){

		fprintf(stderr, "Mysql query error : %s", mysql_error(&conn));
	}

	//CHI
	sprintf_s(query, sizeof(query), "UPDATE client_table SET CLIENT_SIDE_TRAFFIC = %d WHERE LOCATION = 'INDIANA'", stCCT.INDIANA_TRAFFIC);

	m_iQueryStat = mysql_query(connection, query);
	if (m_iQueryStat != 0){

		fprintf(stderr, "Mysql query error : %s", mysql_error(&conn));
	}

	//CHI
	sprintf_s(query, sizeof(query), "UPDATE client_table SET CLIENT_SIDE_TRAFFIC = %d WHERE LOCATION = 'OHIO'", stCCT.OHIO_TRAFFIC);

	m_iQueryStat = mysql_query(connection, query);
	if (m_iQueryStat != 0){

		fprintf(stderr, "Mysql query error : %s", mysql_error(&conn));
	}

	//CHI
	sprintf_s(query, sizeof(query), "UPDATE client_table SET CLIENT_SIDE_TRAFFIC = %d WHERE LOCATION = 'PENNSYLVANIA'", stCCT.PENNSYLVANIA_TRAFFIC);

	m_iQueryStat = mysql_query(connection, query);
	if (m_iQueryStat != 0){

		fprintf(stderr, "Mysql query error : %s", mysql_error(&conn));
	}

	//CHI
	sprintf_s(query, sizeof(query), "UPDATE client_table SET CLIENT_SIDE_TRAFFIC = %d WHERE LOCATION = 'NEWYORK'", stCCT.NEWYORK_TRAFFIC);

	m_iQueryStat = mysql_query(connection, query);
	if (m_iQueryStat != 0){

		fprintf(stderr, "Mysql query error : %s", mysql_error(&conn));
	}

	//CHI
	sprintf_s(query, sizeof(query), "UPDATE client_table SET CLIENT_SIDE_TRAFFIC = %d WHERE LOCATION = 'VERMONT'", stCCT.VERMONT_TRAFFIC);

	m_iQueryStat = mysql_query(connection, query);
	if (m_iQueryStat != 0){

		fprintf(stderr, "Mysql query error : %s", mysql_error(&conn));
	}

	//CHI
	sprintf_s(query, sizeof(query), "UPDATE client_table SET CLIENT_SIDE_TRAFFIC = %d WHERE LOCATION = 'NEWHAMPSHIRE'", stCCT.NEWHAMPSHIRE_TRAFFIC);

	m_iQueryStat = mysql_query(connection, query);
	if (m_iQueryStat != 0){

		fprintf(stderr, "Mysql query error : %s", mysql_error(&conn));
	}

	//CHI
	sprintf_s(query, sizeof(query), "UPDATE client_table SET CLIENT_SIDE_TRAFFIC = %d WHERE LOCATION = 'MAINE'", stCCT.MAINE_TRAFFIC);

	m_iQueryStat = mysql_query(connection, query);
	if (m_iQueryStat != 0){

		fprintf(stderr, "Mysql query error : %s", mysql_error(&conn));
	}

	//CHI
	sprintf_s(query, sizeof(query), "UPDATE client_table SET CLIENT_SIDE_TRAFFIC = %d WHERE LOCATION = 'MASSACHUSETTS'", stCCT.MASSACHUSETTS_TRAFFIC);

	m_iQueryStat = mysql_query(connection, query);
	if (m_iQueryStat != 0){

		fprintf(stderr, "Mysql query error : %s", mysql_error(&conn));
	}

	//CHI
	sprintf_s(query, sizeof(query), "UPDATE client_table SET CLIENT_SIDE_TRAFFIC = %d WHERE LOCATION = 'RHODE'", stCCT.RHODE_TRAFFIC);

	m_iQueryStat = mysql_query(connection, query);
	if (m_iQueryStat != 0){

		fprintf(stderr, "Mysql query error : %s", mysql_error(&conn));
	}

	//CHI
	sprintf_s(query, sizeof(query), "UPDATE client_table SET CLIENT_SIDE_TRAFFIC = %d WHERE LOCATION = 'CONNECTICUT'", stCCT.CONNECTICUT_TRAFFIC);

	m_iQueryStat = mysql_query(connection, query);
	if (m_iQueryStat != 0){

		fprintf(stderr, "Mysql query error : %s", mysql_error(&conn));
	}

	//CHI
	sprintf_s(query, sizeof(query), "UPDATE client_table SET CLIENT_SIDE_TRAFFIC = %d WHERE LOCATION = 'NEWJERSY'", stCCT.NEWJERSY_TRAFFIC);

	m_iQueryStat = mysql_query(connection, query);
	if (m_iQueryStat != 0){

		fprintf(stderr, "Mysql query error : %s", mysql_error(&conn));
	}

	//CHI
	sprintf_s(query, sizeof(query), "UPDATE client_table SET CLIENT_SIDE_TRAFFIC = %d WHERE LOCATION = 'DELAWARE'", stCCT.DELAWARE_TRAFFIC);

	m_iQueryStat = mysql_query(connection, query);
	if (m_iQueryStat != 0){

		fprintf(stderr, "Mysql query error : %s", mysql_error(&conn));
	}

	//CHI
	sprintf_s(query, sizeof(query), "UPDATE client_table SET CLIENT_SIDE_TRAFFIC = %d WHERE LOCATION = 'MARYLAND'", stCCT.MARYLAND_TRAFFIC);

	m_iQueryStat = mysql_query(connection, query);
	if (m_iQueryStat != 0){

		fprintf(stderr, "Mysql query error : %s", mysql_error(&conn));
	}

	//CHI
	sprintf_s(query, sizeof(query), "UPDATE client_table SET CLIENT_SIDE_TRAFFIC = %d WHERE LOCATION = 'KENTUCKY'", stCCT.KENTUCKY_TRAFFIC);

	m_iQueryStat = mysql_query(connection, query);
	if (m_iQueryStat != 0){

		fprintf(stderr, "Mysql query error : %s", mysql_error(&conn));
	}

	//CHI
	sprintf_s(query, sizeof(query), "UPDATE client_table SET CLIENT_SIDE_TRAFFIC = %d WHERE LOCATION = 'WESTVIRGINIA'", stCCT.WESTVIRGINIA_TRAFFIC);

	m_iQueryStat = mysql_query(connection, query);
	if (m_iQueryStat != 0){

		fprintf(stderr, "Mysql query error : %s", mysql_error(&conn));
	}

	//CHI
	sprintf_s(query, sizeof(query), "UPDATE client_table SET CLIENT_SIDE_TRAFFIC = %d WHERE LOCATION = 'VIRGINIA'", stCCT.VIRGINIA_TRAFFIC);

	m_iQueryStat = mysql_query(connection, query);
	if (m_iQueryStat != 0){

		fprintf(stderr, "Mysql query error : %s", mysql_error(&conn));
	}

	//CHI
	sprintf_s(query, sizeof(query), "UPDATE client_table SET CLIENT_SIDE_TRAFFIC = %d WHERE LOCATION = 'TENNESSEE'", stCCT.TENNESSEE_TRAFFIC);

	m_iQueryStat = mysql_query(connection, query);
	if (m_iQueryStat != 0){

		fprintf(stderr, "Mysql query error : %s", mysql_error(&conn));
	}

	//CHI
	sprintf_s(query, sizeof(query), "UPDATE client_table SET CLIENT_SIDE_TRAFFIC = %d WHERE LOCATION = 'NORTHCAROLINA'", stCCT.NORTHCAROLINA_TRAFFIC);

	m_iQueryStat = mysql_query(connection, query);
	if (m_iQueryStat != 0){

		fprintf(stderr, "Mysql query error : %s", mysql_error(&conn));
	}

	//CHI
	sprintf_s(query, sizeof(query), "UPDATE client_table SET CLIENT_SIDE_TRAFFIC = %d WHERE LOCATION = 'MISSISSIPPI'", stCCT.MISSISSIPPI_TRAFFIC);

	m_iQueryStat = mysql_query(connection, query);
	if (m_iQueryStat != 0){

		fprintf(stderr, "Mysql query error : %s", mysql_error(&conn));
	}

	//CHI
	sprintf_s(query, sizeof(query), "UPDATE client_table SET CLIENT_SIDE_TRAFFIC = %d WHERE LOCATION = 'ALABAMA'", stCCT.ALABAMA_TRAFFIC);

	m_iQueryStat = mysql_query(connection, query);
	if (m_iQueryStat != 0){

		fprintf(stderr, "Mysql query error : %s", mysql_error(&conn));
	}

	//CHI
	sprintf_s(query, sizeof(query), "UPDATE client_table SET CLIENT_SIDE_TRAFFIC = %d WHERE LOCATION = 'GEORGIA'", stCCT.GEORGIA_TRAFFIC);

	m_iQueryStat = mysql_query(connection, query);
	if (m_iQueryStat != 0){

		fprintf(stderr, "Mysql query error : %s", mysql_error(&conn));
	}

	//CHI
	sprintf_s(query, sizeof(query), "UPDATE client_table SET CLIENT_SIDE_TRAFFIC = %d WHERE LOCATION = 'SOUTHCAROLINA'", stCCT.SOUTHCAROLINA_TRAFFIC);

	m_iQueryStat = mysql_query(connection, query);
	if (m_iQueryStat != 0){

		fprintf(stderr, "Mysql query error : %s", mysql_error(&conn));
	}

	//CHI
	sprintf_s(query, sizeof(query), "UPDATE client_table SET CLIENT_SIDE_TRAFFIC = %d WHERE LOCATION = 'FLORIDA'", stCCT.FLORIDA_TRAFFIC);

	m_iQueryStat = mysql_query(connection, query);
	if (m_iQueryStat != 0){

		fprintf(stderr, "Mysql query error : %s", mysql_error(&conn));
	}

	//CHI
	sprintf_s(query, sizeof(query), "UPDATE client_table SET CLIENT_SIDE_TRAFFIC = %d WHERE LOCATION = 'GUAM'", stCCT.GUAM_TRAFFIC);

	m_iQueryStat = mysql_query(connection, query);
	if (m_iQueryStat != 0){

		fprintf(stderr, "Mysql query error : %s", mysql_error(&conn));
	}
}

void CDatabase::UpdatePrevMatchingTable(vector <match_result_data> vecMatchResult) {

	char query[255];

	for (int i = 0; i < vecMatchResult.size(); i++){

		char arrUser[40];
		memset(arrUser, 0x00, 40);
		strcpy_s(arrUser, 40, vecMatchResult.at(i).arrUser);

		sprintf_s(query, sizeof(query), "UPDATE prev_matching_table SET prev_ep = %d WHERE user = '%s'", vecMatchResult.at(i).iCurrEP, vecMatchResult.at(i).arrUser);

		m_iQueryStat = mysql_query(connection, query);
		if (m_iQueryStat != 0){

			fprintf(stderr, "Mysql query error : %s", mysql_error(&conn));
		}
	}
}

void CDatabase::DeleteTables(){

	string arrQuery[] = {
		"delete from server_table",
		"delete from client_table",
		"delete from cst_table",
		"delete from distance_table",
		"delete from normalized_cst_table",
		"delete from normalized_distance_table",
		"delete from normalized_server_table",
		"delete from weight_table",
		"delete from matching_table",
		"delete from update_matching_table"
	};

	for (int i = 0; i < sizeof(arrQuery) / sizeof(string); i++){
	
		m_iQueryStat = mysql_query(connection, arrQuery[i].c_str());
		if (m_iQueryStat != 0) {

			fprintf(stderr, "Mysql query error : %s", mysql_error(&conn));
		}

		sql_result = mysql_store_result(connection);

		mysql_free_result(sql_result);
	}
}