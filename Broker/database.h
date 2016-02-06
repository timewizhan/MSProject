/*
 * database.h
 *
 *  Created on: Feb 4, 2016
 *      Author: alphahacker
 */

#ifndef _DB_
#define _DB_

#include "common.h"
#include "mysql.h"
#include "database.h"
#include "socket.h"

//DB
#define DB_HOST "127.0.0.1"
#define DB_USER "root"
#define DB_PASS "cclab"
#define DB_NAME "broker_table"

class CDatabase{

private:

	MYSQL       *connection=NULL, conn;
	MYSQL_RES   *sql_result;
	MYSQL_ROW   sql_row;
	MYSQL_FIELD	*field;

public:

	int query_stat;
	CSocket m_socket;

	CDatabase();
	~CDatabase();

	void init_thread();
	int initDB();
	int extractData();
	void insertData(string name, string location, int timestamp, int client_side_traffic, int server_side_traffic, int cpu_util, int ep_num, string side_flag);
	void updateLocation(int, int, int);

};

#endif





