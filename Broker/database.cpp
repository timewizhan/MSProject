/*
 * database.cpp
 *
 *  Created on: Feb 4, 2016
 *      Author: alphahacker
 */

#include "common.h"
#include "mysql.h"
#include "broker_server.h"
#include "data_queue.h"
#include "database.h"
#include "socket.h"

CDatabase::CDatabase(){

	initDB();
}

CDatabase::~CDatabase(){}

int CDatabase::initDB(){

	printf("init db \n");

	mysql_init(&conn);

	connection = mysql_real_connect(&conn, DB_HOST,
									DB_USER, DB_PASS,
									DB_NAME, 3306,
									(char *)NULL, 0);

	if (connection == NULL){

		fprintf(stderr, "Mysql connection error : %s", mysql_error(&conn));
		return 1;

	}else if(connection){

	//	printf("connection success \n");
	}

	return 0;
}


int CDatabase::extractData(){

	return 0;
}


void CDatabase::insertData(string name, string location, int timestamp, int client_side_traffic, int server_side_traffic, int cpu_util, int ep_num, string side_flag){


	char query[255];

	sprintf(query, "insert into broker_table values ('%s', '%s', %d, %d, %d, %d, %d, '%s')",
	                   name.c_str(), location.c_str(), timestamp, client_side_traffic, server_side_traffic, cpu_util, ep_num, side_flag.c_str());

	query_stat = mysql_query(connection, query);

	if (query_stat != 0){

		fprintf(stderr, "Mysql query error : %s", mysql_error(&conn));
	}

}

void CDatabase::updateLocation(int l_ny_traffic, int l_bs_traffic, int l_chi_traffic){

	char query[255];

	//NY
	sprintf(query, "UPDATE broker_table SET CLIENT_SIDE_TRAFFIC = %d WHERE LOCATION = 'NY'", l_ny_traffic);

	query_stat = mysql_query(connection, query);
	if (query_stat != 0){

		fprintf(stderr, "Mysql query error : %s", mysql_error(&conn));
	}

	//BS
	sprintf(query, "UPDATE broker_table SET CLIENT_SIDE_TRAFFIC = %d WHERE LOCATION = 'BS'", l_bs_traffic);

	query_stat = mysql_query(connection, query);
	if (query_stat != 0){

		fprintf(stderr, "Mysql query error : %s", mysql_error(&conn));
	}

	//CHI
	sprintf(query, "UPDATE broker_table SET CLIENT_SIDE_TRAFFIC = %d WHERE LOCATION = 'CHI'", l_chi_traffic);

	query_stat = mysql_query(connection, query);
	if (query_stat != 0){

		fprintf(stderr, "Mysql query error : %s", mysql_error(&conn));
	}
}

void* preprocess_insert(void *data){

	printf("preprocess_insert \n");
	CDatabase *l_db = (CDatabase *)data;

	int l_ny_traffic = 0;
	int l_bs_traffic = 0;
	int l_chi_traffic = 0;

	while(1){

		if(!CDataQueue::getDataQueue()->getQueue().empty()){															//queue가 비어있지 않으면

			monitoring_result poppedData = CDataQueue::getDataQueue()->popDataFromQueue();

			printf("\n[read test in preprocess of inserting data] \n");
			printf("EP: %d, Side: %s \n", poppedData.ep_num, poppedData.side_flag);
			printf("cpu_util: %d, server-side traffic: %d \n", poppedData.cpu_util, poppedData.server_side_traffic);
			printf("user: %s, location: %s, timestamp: %d, user traffic: %d \n", poppedData.user, poppedData.location
					, poppedData.timestamp, poppedData.traffic);

			/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			//지역별로 트래픽 합치기
			if(!strcmp(poppedData.side_flag, "c")){

				if(!strcmp(poppedData.location, "NY")){

					l_ny_traffic += poppedData.traffic;
					l_db->insertData(poppedData.user, poppedData.location, poppedData.timestamp, poppedData.traffic
											, poppedData.server_side_traffic, poppedData.cpu_util, poppedData.ep_num, poppedData.side_flag);	//서버 부분 빼고 다 넣어주기

				}else if(!strcmp(poppedData.location, "BS")){

					l_bs_traffic += poppedData.traffic;
					l_db->insertData(poppedData.user, poppedData.location, poppedData.timestamp, poppedData.traffic
											, poppedData.server_side_traffic, poppedData.cpu_util, poppedData.ep_num, poppedData.side_flag);

				}else if(!strcmp(poppedData.location, "CHI")){

					l_chi_traffic += poppedData.traffic;
					l_db->insertData(poppedData.user, poppedData.location, poppedData.timestamp, poppedData.traffic
											, poppedData.server_side_traffic, poppedData.cpu_util, poppedData.ep_num, poppedData.side_flag);
				}

			}else if(!strcmp(poppedData.side_flag, "s")){

				l_db->insertData(poppedData.user, poppedData.location, poppedData.timestamp, poppedData.traffic
						, poppedData.server_side_traffic, poppedData.cpu_util, poppedData.ep_num, poppedData.side_flag);		//클라이언트 부분 빼고 다 넣어주기

			}else if(!strcmp(poppedData.side_flag, "e")){

				break;
			}
			/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		}

		::Sleep(1);
	}


	//여기서 client-side traffic 값 넣어주기
	printf("Total traffic test: NY TT = %d, BS TT = %d, CHI TT = %d", l_ny_traffic, l_bs_traffic, l_chi_traffic);
	l_db->updateLocation(l_ny_traffic, l_bs_traffic, l_chi_traffic);

	/*
		여기서 LP 알고리즘을 호출해야할듯.
	*/
}
