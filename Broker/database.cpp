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


void CDatabase::insertData(string name, string location, int timestamp, int traffic){


	char query[255];

	sprintf(query, "insert into broker_table values ('%s', '%s', %d, %d)",
	                   name.c_str(), location.c_str(), timestamp, traffic);

	query_stat = mysql_query(connection, query);

	if (query_stat != 0){

		fprintf(stderr, "Mysql query error : %s", mysql_error(&conn));
	}

}

void* preprocess_insert(void *data){

	printf("preprocess_insert \n");

	while(1){

		if(!CDataQueue::getDataQueue()->getQueue().empty()){							//queue가 비어있지 않으면

			monitoring_result poppedData = CDataQueue::getDataQueue()->popDataFromQueue();

			printf("\n[read test in preprocess of inserting data] \n");
			printf("EP: %d, Side: %c \n", poppedData.ep_num, poppedData.side_flag);
			printf("cpu_util: %d, server-side traffic: %d \n",poppedData.cpu_util, poppedData.server_side_traffic);
			printf("user: %s, location: %s, timestamp: %d, user traffic: %d \n",poppedData.user, poppedData.location
					,poppedData.timestamp, poppedData.traffic);

			/*
			//지역 별로 정리...
			if(strcmp(pop_out_data->location, "NY")){
				int ny_local_traffic = select * from local_traffic where location = "NY";
				ny_local_traffic += pop_out_data->traffic;
				테이블 수정 modify 였던가..
			}else if(strcmp(pop_out_data->location, "BS")){

			}
			*/

			//처리 후 Broker database에 저장
			//아래는 예시
			CDatabase *l_db = (CDatabase *)data;
			l_db->insertData(poppedData.user, poppedData.location
					,poppedData.timestamp, poppedData.traffic);
		}

		/*
		while(1)문을 탈출하는 조건이 있을텐데, 이를테면, 모니터링 한 결과를 EP로 부터 모두 받게되면 가장 마지막 데이터에 특수 기호 같은 걸 넣어서
		이게 마지막이다. 라는걸 알 수 있게 한다는 등이 될 것이다, 아마도?
		그러면 반복문을 나와서, local traffic 측정한 값을 여기서 넣는다.


		그리고 여기서 LP 알고리즘을 호출해야할듯.
		*/
		sleep(1);
	}
}
