/*
 * database.cpp
 *
 *  Created on: Feb 4, 2016
 *      Author: ms-dev
 */

#include "Common.h"
#include "EP_Test4.h"
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


/*
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
*/

int CDatabase::extractData()
{

		//select * from server_side_monitor
		query_stat = mysql_query(connection, "select * from server_side_monitor");

	    if (query_stat != 0)
	    {
	        fprintf(stderr, "Mysql query error : %s", mysql_error(&conn));
	        return 1;
	    }

	    sql_result = mysql_store_result(connection);

		int num_rows =0;
		int num_fields =0;
		int num_res_fields =0;
		if(sql_result){
		num_rows = mysql_num_rows(sql_result);
		num_fields = mysql_field_count(connection);
		num_res_fields = mysql_num_fields(sql_result);
		printf("mysql_num_rows: %d, mysql_field_count: %d, mysql_num_fields: %d \n", num_rows, num_fields, num_res_fields);
		}


		//print column headers
		MYSQL_FIELD	*field;
		field = mysql_fetch_fields(sql_result);
		for(int i=0; i<num_fields; i++){

			printf("field %u is %s \n", i, field[i].name);
		}


		printf("\n\n");
		////////////////////////////////////////////////////////////////////////////
		EP_Test4 ep;
	    memset(&ep.write_message, 0, sizeof(ep.write_message));

	    //num_fields = mysql_field_count(connection);
	    while(sql_row = mysql_fetch_row(sql_result)){
	    		printf("cpu util: %s, server-side traffic: %s \n", sql_row[0], sql_row[1]);

	    		int _cpu_util =0;
	    		int _server_side_traffic =0;
	    		_cpu_util = atoi(sql_row[0]);
	    		_server_side_traffic = atoi(sql_row[1]);

	    		ep.write_message.cpu_util = _cpu_util;
	    		ep.write_message.server_side_traffic = _server_side_traffic;

				//구조체의 나머지 부분은 NULL 로 채운다. flag도 값 넣어줘야함
	    		ep.write_message.ep_num = 1;
	    		ep.write_message.side_flag = 's';

				memset(&ep.write_message.user, 0, sizeof(ep.write_message.user));
				memset(&ep.write_message.location, 0, sizeof(ep.write_message.location));
				ep.write_message.timestamp = 0;
				ep.write_message.traffic = 0;


				EP_Test4 ep;
				ep.sendMessage(); //아니면 이걸 그냥 static으로 선언해버릴까?
	    }

	    mysql_free_result(sql_result);


	    //select * from client_side_monitor
		query_stat = mysql_query(connection, "select * from client_side_monitor");

	    if (query_stat != 0)
	    {
	        fprintf(stderr, "Mysql query error : %s", mysql_error(&conn));
	        return 1;
	    }

	    sql_result = mysql_store_result(connection);

	    while ( (sql_row = mysql_fetch_row(sql_result)) != NULL )
		{
	    	sleep(1);
			printf("user: %s, location: %s, timestamp: %d, client-side traffic: %d \n", sql_row[0], sql_row[1], sql_row[2], sql_row[3]);

			strcpy(ep.write_message.user,sql_row[0]);
			strcpy(ep.write_message.location,sql_row[1]);
	//		write_message.user = sql_row[0];
	//		write_message.location = sql_row[1];
		//	write_message.timestamp = (int)sql_row[2];
			ep.write_message.timestamp = atoi(sql_row[2]);
		//	write_message.traffic = (int)sql_row[3];
			ep.write_message.timestamp = atoi(sql_row[3]);

			//구조체의 나머지 부분은 NULL 로 채운다. flag도 값 넣어줘야함
			ep.write_message.ep_num = 1;
			ep.write_message.side_flag = 'c';

			ep.write_message.cpu_util = 0;
			ep.write_message.server_side_traffic = 0;

			EP_Test4 ep;
			ep.sendMessage(); //아니면 이걸 그냥 static으로 선언해버릴까?
		}

		mysql_free_result(sql_result);



//	    mysql_close(connection); <-- 이거 적절한 곳에 넣어줘야 함


	return 0;
}

