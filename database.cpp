/*
 * database.cpp
 *
 *  Created on: Feb 4, 2016
 *      Author: alphahacker
 */

#include "common.h"
#include "broker_server.h"
#include "database.h"

//CDatabase* CDatabase::m_pDatabase = NULL;

CDatabase::CDatabase(){

	m_mutex = PTHREAD_MUTEX_INITIALIZER;
}

CDatabase::~CDatabase(){

}

//////////////////////////////////////////////////////////////////////////////////////////////

int CDatabase::initDB(){

	mysql_init(&conn);

	connection = mysql_real_connect(&conn, DB_HOST,
									DB_USER, DB_PASS,
									DB_NAME, 3306,
									(char *)NULL, 0);

	if (connection == NULL)
	{
		fprintf(stderr, "Mysql connection error : %s", mysql_error(&conn));
		return 1;
	}else if(connection){
		printf("connection success \n");
	}

	//test
//	mysql_query(connection, "insert into server_side_monitor(cpu_util, server_side_traffic) values(1, 1)");

	return 0;

}


int CDatabase::extractData()
{
	return 0;
}


void CDatabase::insertData(string name, string location, int timestamp, int traffic)
{
	char query[255];

	sprintf(query, "insert into broker_table values ('%s', '%s', %d, %d)",
	                   name.c_str(), location.c_str(), timestamp, traffic);

	query_stat = mysql_query(connection, query);

	if (query_stat != 0)
	{
		fprintf(stderr, "Mysql query error : %s", mysql_error(&conn));
	}

}
