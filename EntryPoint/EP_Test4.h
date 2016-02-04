/*
 * EP_Test4.h
 *
 *  Created on: Jan 23, 2016
 *      Author: alphahacker
 */

#ifndef _ENTRYPOINT_
#define _ENTRYPOINT_
#include "mysql.h"
#include "database.h"

//SOCKET
#define MAXBUF 1024
#define USER 20

//보낼 메시지 구조체를 하나로 합치고, 어디서(어느 EP에서) 보냈는지 구분할수있는 flag가 하나있어야한다.
struct monitoring_result{

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

class EP_Test4{

	//SOCKET//////////////////////////////////////////////////////////////////////////////////////////////////////
	int ssock;
	int clen;
	struct sockaddr_in client_addr, server_addr;
	char buf[MAXBUF],pre[MAXBUF];
	fd_set read_fds,tmp_fds;
	int fd;
	char name[20];
//	struct message write_message;
	int re;



public:

	struct monitoring_result write_message;

	//DB
	CDatabase db;

	EP_Test4();
	~EP_Test4();

	//SOCKET
	void initEntryPoint();
	int entrySockStart();
	void sendMessage();

};

#endif


