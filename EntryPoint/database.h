/*
 * database.h
 *
 *  Created on: Feb 4, 2016
 *      Author: ms-dev
 */

#ifndef _DB_
#define _DB_

#include "Common.h"
#include "mysql.h"

//DB
#define DB_HOST "127.0.0.1"
#define DB_USER "root"
#define DB_PASS "cclab"
#define DB_NAME "EP1"

class CDatabase{
private:

	CDatabase* m_pDatabase;
	pthread_mutex_t m_mutex;

	MYSQL       *connection=NULL, conn;
	MYSQL_RES   *sql_result;
	MYSQL_ROW   sql_row;
	MYSQL_FIELD	*field;


public:

	CDatabase();
	~CDatabase();

	CDatabase* getDatabaseInstance(){
		if(m_pDatabase == NULL){
			m_pDatabase = new CDatabase();
		}

		return m_pDatabase;
	}

	int       query_stat;

	int initDB();
	int extractData();
//	void insertData(string name, string location, int timestamp, int traffic);

};

#endif



