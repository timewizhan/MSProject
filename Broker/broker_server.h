/*
 * main_broker.h
 *
 *  Created on: Jan 20, 2016
 *      Author: ms-dev
 */

#ifndef _BROKER_
#define _BROKER_

#include "common.h"
#include "mysql.h"
#include "database.h"


//클라이언트로부터 받은 메시지 구조체
typedef struct monitoring_result{

	//flag
	int ep_num;	//1, 2 or 3
	char side_flag; //server side: 's', client side: 'c'

	//server side monitoring result
	int cpu_util;
	int server_side_traffic;

	//client side monitoring result
	char user[20];
	char location[20];
	int timestamp;
	int traffic;

};


//BROKER
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
#define MAXBUF 1024
#define MAX 100
#define ALL 1
#define USER 20


//클라이언트가 보낸 메세지와 전송받을 클라이언트의 이름을 저장
typedef struct message {
	char user[USER];
	char sbuf[MAXBUF];
};

//서버에 접속한 클라이언트의 디스크립터와 거기에 매치되는 해당 클라이언트의 이름을 저장
struct add_num {
	int anum;
	char name[MAXBUF];
};


class CBrokerServer{

	//SOCKET Related
	int optval;                        					//소켓옵션의 설정값
	int ssock, csock;                       				//소켓
	struct sockaddr_in server_addr, client_addr;        //IP와 Port값(즉 주소값)

	unsigned int clen, data_len;
	fd_set read_fds, tmp_fds;   				           //디스크립터 셋트(단일 비트 테이블)
	int fd;

	struct add_num add_num[USER]; 				    //서버에접속하는 클라이언트의 정보를 저장하는 객체
	int index, maxfd;
	struct monitoring_result read_message;				//클라이언트로 부터 받은 메세지 구조체
	char greet[1];
	char no_greet[1];

public:

	CBrokerServer();
	~CBrokerServer();

	//Thread
	void initThread();

	//SOCKET Related
	void initBroker();								//broker의 각 기능들 시작점
	void setSocket();								//socket, bind, listen
	void acceptEP();								//select, accept
	void communicateWithEP();
	void writeMessage(void *client_message, void *num, int basefd, int maxfd); //메세지 전송 함수 //사용자로부터 받은 메시지를 해당 클라이언트에 전송해 주는 함수

	//DB
	CDatabase db;

};

void* preprocess_insert(void *data);

#endif

