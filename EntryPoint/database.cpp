#include "Common.h"
#include "EP_Test4.h"
#include "database.h"

CDatabase::CDatabase(){
	m_iQueryStat = 0;
//	m_socket.init_socket();
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

void CDatabase::CloseDB(){
	mysql_close(connection);
}

int CDatabase::extractData(CSocket cBrokerSocket)
{
	printf("extract data method \n");

	m_iQueryStat = mysql_query(connection, "select * from server_side_monitor");
	if (m_iQueryStat != 0){

		fprintf(stderr, "Mysql query error : %s", mysql_error(&conn));
		return 1;
	}
	sql_result = mysql_store_result(connection);

	memset(&cBrokerSocket.write_message, 0, sizeof(cBrokerSocket.write_message));
	while ((sql_row = mysql_fetch_row(sql_result)) != NULL){
		printf("cpu util: %s, server-side traffic: %s \n", sql_row[0], sql_row[1]);

		int _cpu_util = 0;
		int _server_side_traffic = 0;
		_cpu_util = atoi(sql_row[0]);
		_server_side_traffic = atoi(sql_row[1]);

		cBrokerSocket.write_message.cpu_util = _cpu_util;
		cBrokerSocket.write_message.server_side_traffic = _server_side_traffic;
		//		printf("test: %d %d", _cpu_util,_server_side_traffic);
		//����ü�� ������ �κ��� NULL �� ä���. flag�� �� �־������
		cBrokerSocket.write_message.ep_num = 2;
		//		m_socket.write_message.side_flag = "s";
		strcpy_s(cBrokerSocket.write_message.side_flag, 2, "s");
		memset(&cBrokerSocket.write_message.user, 0, sizeof(cBrokerSocket.write_message.user));
		memset(&cBrokerSocket.write_message.location, 0, sizeof(cBrokerSocket.write_message.location));
		cBrokerSocket.write_message.timestamp = 0;
		cBrokerSocket.write_message.traffic = 0;

		cBrokerSocket.send_message(); //�ƴϸ� �̰� �׳� static���� �����ع�����?
	}

	mysql_free_result(sql_result);

//	::Sleep(1);

//	printf("\n");

	/////////////////////////////////////////////////////////////////////////////////////////////////////
	m_iQueryStat = mysql_query(connection, "select * from client_side_monitor");

	if (m_iQueryStat != 0)
	{
		fprintf(stderr, "Mysql query error : %s", mysql_error(&conn));
		return 1;
	}

	sql_result = mysql_store_result(connection);

	memset(&cBrokerSocket.write_message, 0, sizeof(cBrokerSocket.write_message));
	while ((sql_row = mysql_fetch_row(sql_result)) != NULL){
		printf("user: %s, location: %s, timestamp: %s, client-side traffic: %s \n"
			, sql_row[0], sql_row[1], sql_row[2], sql_row[3]);

		strcpy_s(cBrokerSocket.write_message.user, 40, sql_row[0]);				//user name
		strcpy_s(cBrokerSocket.write_message.location, 40, sql_row[1]);			//location
		//m_socket.write_message.timestamp = atoi(sql_row[2]);		//timestamp
		cBrokerSocket.write_message.traffic = atoi(sql_row[3]);			//traffic

		//����ü�� ������ �κ��� NULL �� ä���. flag�� �� �־������
		cBrokerSocket.write_message.ep_num = 2;							//EP number
		strcpy_s(cBrokerSocket.write_message.side_flag, 2, "c");
		//	m_socket.write_message.side_flag = "c";						//server-side or client-side
		cBrokerSocket.write_message.cpu_util = 0;						//cpu utilization
		cBrokerSocket.write_message.server_side_traffic = 0;				//server-side traffic

		cBrokerSocket.send_message(); //�ƴϸ� �̰� �׳� static���� �����ع�����?
//		::Sleep(1);
	}

	/////////////////////////////////////////////////////////////////////////////////////////////////////
//	::Sleep(1);

	memset(&cBrokerSocket.write_message, 0, sizeof(cBrokerSocket.write_message));
	strcpy_s(cBrokerSocket.write_message.side_flag, 2, "e");											//������ ������ ���̻� ������ �˸��� �÷��� ����(e = end)
	memset(&cBrokerSocket.write_message.user, 0, sizeof(cBrokerSocket.write_message.user));			//������ ������ ��� ����
	memset(&cBrokerSocket.write_message.location, 0, sizeof(cBrokerSocket.write_message.location));
	cBrokerSocket.write_message.cpu_util = 0;
	cBrokerSocket.write_message.ep_num = 2;
	cBrokerSocket.write_message.server_side_traffic = 0;
	cBrokerSocket.write_message.timestamp = 0;
	cBrokerSocket.write_message.traffic = 0;

	cBrokerSocket.send_message();

	mysql_free_result(sql_result);

//	mysql_close(connection);

	return 0;
}

void CDatabase::StoreData(CSocket cBrokerSocket){

	int data_len = 0;

	while (1){
		//������ �޾Ƽ� ��� ����
		match_result_data stRecvedResData = cBrokerSocket.recv_message();
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

void CDatabase::DeleteTables(){

	string arrQuery[] = {
		"delete from server_side_monitor",
		"delete from client_side_monitor",
		"delete from match_result_table"
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