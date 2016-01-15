/*
 * EP_Test4.cpp
 *
 *  Created on: Jan 13, 2016
 *      Author: ms-dev
 */

#include "Common.h"
#include "EP_Test4.h"
#include "mysql.h"

EP_Test4::EP_Test4(){}
EP_Test4::~EP_Test4(){}

void EP_Test4::initEntryPoint()
{

	//socket
	entrySockStart();

	//DB
	initDB();

	//etc..

}

int EP_Test4::entrySockStart()
{

	if((ssock = socket(AF_INET,SOCK_STREAM,0))<0){
			perror("soket error : ");
			exit(1);
		}

		//변수 초기화
		memset(&server_addr,0,sizeof(server_addr));
		server_addr.sin_family = AF_INET;
		server_addr.sin_addr.s_addr = inet_addr("127.0.0.1");
		server_addr.sin_port = htons(3333);

		clen = sizeof(server_addr);

		//사용자를 입력받는다.
		printf("사용자를 입력하시오 : ");
		fgets(name,MAXBUF,stdin);
		*(name+(strlen(name)-1))='\0';

		//socket이라는 파일 서술자를 이용하여 소켓에 연결을 하게 된다.
		if(connect(ssock,(struct sockaddr *)&server_addr, clen)<0){
			perror("connect error : ");
			return -1;
		}

}

void EP_Test4::sendMessage()
{
	memset(&write_message,0,sizeof(write_message));
	strcpy(write_message.user,name);
	strcpy(write_message.sbuf,"");

	//해당 메시지를 전송한다.
	if(write(ssock,(struct message*)&write_message,sizeof(write_message))<0){
		perror("write error : ");
		exit(1);
	}

	//파일 디스크립트 테이블을 새로 셋팅
	FD_ZERO(&read_fds);
	FD_SET(ssock,&read_fds);
	FD_SET(0,&read_fds);

	fd=ssock;

	while(1){
		//일회성 복사
		tmp_fds=read_fds;

		//변화를 감지한다.
		if(select(fd+1,&tmp_fds,0,0,0)<0){
			perror("select error : ");
			exit(1);
		}

		//클라이언트 소켓에 변화가 있다는 것을 확인한다.
		if(FD_ISSET(ssock,&tmp_fds)){
			memset(buf,0,MAXBUF);
			re = read(ssock,buf,MAXBUF);
			if(re<0){
				perror("read error : ");
				exit(1);
			}
			//클라이언트 서버가 접속이 끊켰다는 것을 확인한다.
			else if(re==0){
				printf("서버와의 접속이 끊켰습니다.\n");
				break;
			}
			printf("%s\n",buf);
			//printf("전송할 메시지를 입력해 주세요 : ");
		}
		//입력에 대한 변화가 있다는 것을 확인한다.
		else if(FD_ISSET(0,&tmp_fds)){
			memset(&write_message,0,sizeof(write_message));
			memset(buf,0,MAXBUF);

			//보낼 user를 입력
			fgets(write_message.user,USER,stdin);
			*(write_message.user+(strlen(write_message.user)-1))='\0';
			//보낼 메시지를 입력
			printf("전송할 메시지를 입력해 주세요 : ");
			fgets(buf,MAXBUF,stdin);
			*(buf +(strlen(buf)-1))='\0';
			sprintf(write_message.sbuf,"[%s]:%s",name,buf);
			if(write(ssock,(struct message*)&write_message,sizeof(write_message))<0){
			perror("write error : ");
			exit(1);

			}
		}
	}
	close(ssock);
}

int EP_Test4::initDB(){

	mysql_init(&conn);

	connection = mysql_real_connect(&conn, DB_HOST,
									DB_USER, DB_PASS,
									DB_NAME, 3306,
									(char *)NULL, 0);

	if (connection == NULL)
	{
		fprintf(stderr, "Mysql connection error : %s", mysql_error(&conn));
		return 1;
	}

	return 0;
}


int EP_Test4::extractData()
{

		query_stat = mysql_query(connection, "select * from address");
	    if (query_stat != 0)
	    {
	        fprintf(stderr, "Mysql query error : %s", mysql_error(&conn));
	        return 1;
	    }

	    sql_result = mysql_store_result(connection);

	    printf("%+11s   %-30s   %-10s", "이름", "주소", "전화번호");
	    while ( (sql_row = mysql_fetch_row(sql_result)) != NULL )
	    {
	        printf("%+11s   %-30s   %-10s", sql_row[0], sql_row[1], sql_row[2]);
	    }

	    mysql_free_result(sql_result);

	    printf("이름 :");
	    fgets(_name, 12, stdin);
	    CHOP(_name);

	    printf("주소 :");
	    fgets(address, 80, stdin);
	    CHOP(address);

	    printf("전화 :");
	    fgets(tel, 12, stdin);
	    CHOP(tel);

	    sprintf(query, "insert into address values "
	                   "('%s', '%s', '%s')",
	                   _name, address, tel);

	    query_stat = mysql_query(connection, query);
	    if (query_stat != 0)
	    {
	        fprintf(stderr, "Mysql query error : %s", mysql_error(&conn));
	        return 1;
	    }

//	    mysql_close(connection); <-- 이거 적절한 곳에 넣어줘야 함


	return 0;
}
int EP_Test4::storeData()
{

	return 0;
}
