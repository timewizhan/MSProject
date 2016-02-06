/*
 * socket.h
 *
 *  Created on: Feb 5, 2016
 *      Author: alphahacker
 */

#ifndef _SOCKET_
#define	_SOCKET_

#include "common.h"

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

struct monitoring_result{

	//flag
	int ep_num;	//1, 2 or 3
	char side_flag[10]; //server side: 's', client side: 'c'

	//server side monitoring result
	int cpu_util;
	int server_side_traffic;

	//client side monitoring result
	char user[20];
	char location[20];
	int timestamp;
	int traffic;
};

class CSocket{

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

	CSocket();
	~CSocket();

	void init_socket();
	void comm_socket();
	void send_message();
	void write_message(void *client_message,void *num,int basefd,int maxfd);
};

#endif


