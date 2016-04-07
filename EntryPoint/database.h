/*
* database.h
*
*  Created on: Feb 4, 2016
*      Author: ms-dev
*/

#ifndef _DBINEP_
#define _DBINEP_

#include "Common.h"
#include "mysql.h"
#include "socket.h"

//DB
#define DB_HOST "127.0.0.1"
#define DB_USER "root"
#define DB_PASS "cclabj0gg00"
#define DB_NAME "snsdb"
//#define DB_NAME "ep1"

class CDatabase{

private:

	MYSQL       *connection = NULL, conn;
	MYSQL_RES   *sql_result;
	MYSQL_ROW   sql_row;
	MYSQL_FIELD	*field;


public:

//	int query_stat;
	int m_iQueryStat;
//	CSocket m_socket;

	CDatabase();
	~CDatabase();
	int initDB();
	void CloseDB();
	int extractData(CSocket cBrokerSocket);
	void StoreData(CSocket cBrokerSocket);
	void InsertMatchResultTable(match_result_data stRecvedResData);
	//	void insertData(string name, string location, int timestamp, int traffic);
	void DeleteTables();

};

#endif



