/*
 * Broker_Test.h
 *
 *  Created on: Jan 13, 2016
 *      Author: ms-dev
 */

#ifndef _BROKER_
#define _BROKER_

#define MAXBUF 1024
#define MAX 100
#define ALL 1
#define USER 20

//이건 왜 굳이 밖으로?
//char greet[]="접속 되었습니다";
//char no_greet[]="접속 되지 않았습니다";

//클라이언트가 보낸 메세지와 전송받을 클라이언트의 이름을 저장
struct message{
	char user[USER];
	char sbuf[MAXBUF];
};


//서버에 접속한 클라이언트의 디스크립터와 거기에 매치되는 해당 클라이언트의 이름을 저장
struct add_num{
	int anum;
	char name[MAXBUF];
};

class Broker_Test4{

	//SOCKET//////////////////////////////////////////////////////////////////////////////////////////////////////
	int optval;                        					//소켓옵션의 설정값
	int ssock,csock;                       				//소켓
	struct sockaddr_in server_addr, client_addr;        //IP와 Port값(즉 주소값)

	unsigned int clen,data_len;
	fd_set read_fds,tmp_fds;   				            //디스크립터 셋트(단일 비트 테이블)
	int fd;

	struct add_num add_num[USER]; 				        //서버에접속하는 클라이언트의 정보를 저장하는 객체
	int index,maxfd;
	struct message read_message;        			    //클라이언트로 부터 받은 메세지 구조체

	char greet[1];
	char no_greet[1];

	//DATABASE//////////////////////////////////////////////////////////////////////////////////////////////////////


public:
	Broker_Test4();
	~Broker_Test4();


	//Broker의 각 기능들 시작점..
	void initBroker();

	//socket, bind, listen
	void brokerSockStart();

	//select, accept
	void acceptEP();

	//메세지 전송 함수
	//사용자로부터 받은 메시지를 해당 클라이언트에 전송해 주는 함수
	void writeMessage(void *client_message,void *num,int basefd,int maxfd);

};

#endif


