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

CDatabase::~CDatabase(){}

int CDatabase::InitDB(){

	printf("init db \n");

	mysql_init(&conn);

	connection = mysql_real_connect(&conn, DB_HOST, DB_USER, DB_PASS, DB_NAME, 3306, (char *)NULL, 0);

	if (connection == NULL){

		fprintf(stderr, "Mysql connection error : %s", mysql_error(&conn));
		return 1;

	}
	else if (connection){

		printf("connection success \n");
	}

	return 0;
}


vector<client_data> CDatabase::extractClientData(string sQuery, int iNumOfColumn){

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
		printf("test / location : %s \n", sResData.c_str());
	}

	mysql_free_result(sql_result);

	return vecClientData;
}

void CDatabase::insertData(string name, string location, int timestamp, int client_side_traffic, int server_side_traffic, int cpu_util, int ep_num, string side_flag){


	char query[255];

	sprintf_s(query, sizeof(query), "insert into broker_table values ('%s', '%s', %d, %d, %d, %d, %d, '%s')",
		name.c_str(), location.c_str(), timestamp, client_side_traffic, server_side_traffic, cpu_util, ep_num, side_flag.c_str());

	m_iQueryStat = mysql_query(connection, query);

	if (m_iQueryStat != 0){

		fprintf(stderr, "Mysql query error : %s", mysql_error(&conn));
	}

}

void CDatabase::InsertNormServerTable(vector <double> vecNormalizedData, string sFlag){

	char query[255];

	if (!strcmp(sFlag.c_str(), "SST")){
	
		sprintf_s(query, sizeof(query), "insert into normalized_server_table values ('EP1', '%f', '%f')", vecNormalizedData.at(0), 0);

		m_iQueryStat = mysql_query(connection, query);

		if (m_iQueryStat != 0){

			fprintf(stderr, "Mysql query error : %s", mysql_error(&conn));
		}

		sprintf_s(query, sizeof(query), "insert into normalized_server_table values ('EP2', '%f', '%f')", vecNormalizedData.at(1), 0);

		m_iQueryStat = mysql_query(connection, query);

		if (m_iQueryStat != 0){

			fprintf(stderr, "Mysql query error : %s", mysql_error(&conn));
		}

		sprintf_s(query, sizeof(query), "insert into normalized_server_table values ('EP3', '%f', '%f')", vecNormalizedData.at(2), 0);

		m_iQueryStat = mysql_query(connection, query);

		if (m_iQueryStat != 0){

			fprintf(stderr, "Mysql query error : %s", mysql_error(&conn));
		}
	}
	else if (!strcmp(sFlag.c_str(), "CPU")) {

		//EP1
		sprintf_s(query, sizeof(query), "UPDATE normalized_server_table SET cpu_util = %f WHERE EP = 'EP1'", vecNormalizedData.at(0));

		m_iQueryStat = mysql_query(connection, query);
		if (m_iQueryStat != 0){

			fprintf(stderr, "Mysql query error : %s", mysql_error(&conn));
		}

		//EP2
		sprintf_s(query, sizeof(query), "UPDATE normalized_server_table SET cpu_util = %f WHERE EP = 'EP2'", vecNormalizedData.at(1));

		m_iQueryStat = mysql_query(connection, query);
		if (m_iQueryStat != 0){

			fprintf(stderr, "Mysql query error : %s", mysql_error(&conn));
		}

		//EP3
		sprintf_s(query, sizeof(query), "UPDATE normalized_server_table SET cpu_util = %f WHERE EP = 'EP3'", vecNormalizedData.at(2));

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

void CDatabase::updateLocation(int l_ny_traffic, int l_bs_traffic, int l_chi_traffic){

	char query[255];

	//NY
	sprintf_s(query, sizeof(query), "UPDATE broker_table SET CLIENT_SIDE_TRAFFIC = %d WHERE LOCATION = 'NY'", l_ny_traffic);

	m_iQueryStat = mysql_query(connection, query);
	if (m_iQueryStat != 0){

		fprintf(stderr, "Mysql query error : %s", mysql_error(&conn));
	}

	//BS
	sprintf_s(query, sizeof(query), "UPDATE broker_table SET CLIENT_SIDE_TRAFFIC = %d WHERE LOCATION = 'BS'", l_bs_traffic);

	m_iQueryStat = mysql_query(connection, query);
	if (m_iQueryStat != 0){

		fprintf(stderr, "Mysql query error : %s", mysql_error(&conn));
	}

	//CHI
	sprintf_s(query, sizeof(query), "UPDATE broker_table SET CLIENT_SIDE_TRAFFIC = %d WHERE LOCATION = 'CHI'", l_chi_traffic);

	m_iQueryStat = mysql_query(connection, query);
	if (m_iQueryStat != 0){

		fprintf(stderr, "Mysql query error : %s", mysql_error(&conn));
	}
}
