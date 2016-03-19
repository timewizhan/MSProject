#include "Common.h"
#include "EP_Test4.h"
#include "database.h"

CDatabase::CDatabase(){
	m_iQueryStat = 0;
	m_socket.init_socket();
}

CDatabase::~CDatabase(){

}

int CDatabase::initDB(){

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

int CDatabase::extractData()
{
	printf("extract data method \n");

	m_iQueryStat = mysql_query(connection, "select * from server_side_monitor");
	if (m_iQueryStat != 0){

		fprintf(stderr, "Mysql query error : %s", mysql_error(&conn));
		return 1;
	}
	sql_result = mysql_store_result(connection);

	memset(&m_socket.write_message, 0, sizeof(m_socket.write_message));
	while ((sql_row = mysql_fetch_row(sql_result)) != NULL){
		printf("cpu util: %s, server-side traffic: %s \n", sql_row[0], sql_row[1]);

		int _cpu_util = 0;
		int _server_side_traffic = 0;
		_cpu_util = atoi(sql_row[0]);
		_server_side_traffic = atoi(sql_row[1]);

		m_socket.write_message.cpu_util = _cpu_util;
		m_socket.write_message.server_side_traffic = _server_side_traffic;
		//		printf("test: %d %d", _cpu_util,_server_side_traffic);
		//구조체의 나머지 부분은 NULL 로 채운다. flag도 값 넣어줘야함
		m_socket.write_message.ep_num = 2;
		//		m_socket.write_message.side_flag = "s";
		strcpy_s(m_socket.write_message.side_flag, 2, "s");
		memset(&m_socket.write_message.user, 0, sizeof(m_socket.write_message.user));
		memset(&m_socket.write_message.location, 0, sizeof(m_socket.write_message.location));
		m_socket.write_message.timestamp = 0;
		m_socket.write_message.traffic = 0;

		m_socket.send_message(); //아니면 이걸 그냥 static으로 선언해버릴까?
	}

	mysql_free_result(sql_result);

//	::Sleep(1);

	printf("\n");

	/////////////////////////////////////////////////////////////////////////////////////////////////////
	m_iQueryStat = mysql_query(connection, "select * from client_side_monitor");

	if (m_iQueryStat != 0)
	{
		fprintf(stderr, "Mysql query error : %s", mysql_error(&conn));
		return 1;
	}

	sql_result = mysql_store_result(connection);

	memset(&m_socket.write_message, 0, sizeof(m_socket.write_message));
	while ((sql_row = mysql_fetch_row(sql_result)) != NULL){
		printf("user: %s, location: %s, timestamp: %s, client-side traffic: %s \n"
			, sql_row[0], sql_row[1], sql_row[2], sql_row[3]);

		strcpy_s(m_socket.write_message.user, 40, sql_row[0]);				//user name
		strcpy_s(m_socket.write_message.location, 40, sql_row[1]);			//location
		m_socket.write_message.timestamp = atoi(sql_row[2]);		//timestamp
		m_socket.write_message.traffic = atoi(sql_row[3]);			//traffic

		//구조체의 나머지 부분은 NULL 로 채운다. flag도 값 넣어줘야함
		m_socket.write_message.ep_num = 1;							//EP number
		strcpy_s(m_socket.write_message.side_flag, 2, "c");
		//	m_socket.write_message.side_flag = "c";						//server-side or client-side
		m_socket.write_message.cpu_util = 0;						//cpu utilization
		m_socket.write_message.server_side_traffic = 0;				//server-side traffic

		m_socket.send_message(); //아니면 이걸 그냥 static으로 선언해버릴까?
	//	::Sleep(1);
	}

	/////////////////////////////////////////////////////////////////////////////////////////////////////
//	::Sleep(1);

	memset(&m_socket.write_message, 0, sizeof(m_socket.write_message));
	strcpy_s(m_socket.write_message.side_flag, 2, "e");											//전송할 내용이 더이상 없음을 알리는 플래그 전송(e = end)
	memset(&m_socket.write_message.user, 0, sizeof(m_socket.write_message.user));			//나머지 값들은 허수 전송
	memset(&m_socket.write_message.location, 0, sizeof(m_socket.write_message.location));
	m_socket.write_message.cpu_util = 0;
	m_socket.write_message.ep_num = 1;
	m_socket.write_message.server_side_traffic = 0;
	m_socket.write_message.timestamp = 0;
	m_socket.write_message.traffic = 0;

	m_socket.send_message(); 

	mysql_free_result(sql_result);

//	mysql_close(connection);

	return 0;
}
/*
void CDatabase::StoreData(){

	int data_len = 0;

	while (1){
		//데이터 받아서 디비에 저장
		match_result_data stRecvedResData = m_socket.recv_message();
		if (stRecvedResData.sUser != ""){
			InsertMatchResultTable(stRecvedResData);
		}
		else if (stRecvedResData.sUser == "end_match_result"){
			break;
		}
		
	}
}

void CDatabase::InsertMatchResultTable(match_result_data stRecvedResData){

	char query[255];

	sprintf_s(query, sizeof(query), "insert into match_result_table values ('%s', '%d', '%d')",
		stRecvedResData.sUser.c_str(), stRecvedResData.iPrevEp, stRecvedResData.iCurrEP);

	m_iQueryStat = mysql_query(connection, query);

	if (m_iQueryStat != 0){

		fprintf(stderr, "Mysql query error : %s", mysql_error(&conn));
	}
}*/


void CDatabase::StoreData(){

	int data_len = 0;

	while (1){
		//데이터 받아서 디비에 저장
		match_result_data stRecvedResData = m_socket.recv_message();
		if (!strcmp(stRecvedResData.arrUser, "end_match_result_transmission")){
			break;
		}
		else if (strcmp(stRecvedResData.arrUser, "")){
			InsertMatchResultTable(stRecvedResData);
		}
	}
}

void CDatabase::InsertMatchResultTable(match_result_data stRecvedResData){

	char query[255];

	sprintf_s(query, sizeof(query), "insert into match_result_table values ('%s', '%d', '%d')",
		stRecvedResData.arrUser, stRecvedResData.iPrevEp, stRecvedResData.iCurrEP);

	m_iQueryStat = mysql_query(connection, query);

	if (m_iQueryStat != 0){

		fprintf(stderr, "Mysql query error : %s", mysql_error(&conn));
	}
}