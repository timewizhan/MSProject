/*
* socket.h
*
*  Created on: Feb 4, 2016
*      Author: ms-dev
*/

#ifndef _SOCKETINEP_
#define	_SOCKETINEP_

struct monitoring_result{

	//flag
	int ep_num;	//1, 2 or 3
	char side_flag[10]; //server side: 's', client side: 'c'

	//server side monitoring result
	int cpu_util;
	int server_side_traffic;

	//client side monitoring result
	char user[40];
	char location[40];
	int timestamp;
	int traffic;
};

struct match_result_data{

//	string sUser;
	char arrUser[40];
	int iPrevEp;
	int iCurrEP;
};

class CSocket{

public:
	WSADATA wsaData;
	int ssock;
	int clen;
	struct sockaddr_in client_addr, server_addr;
	fd_set read_fds, tmp_fds;
	int fd;

	struct monitoring_result write_message;
	struct match_result_data read_message;
	
	CSocket();
	~CSocket();

	void init_socket();
	void send_message();
	match_result_data recv_message();
};

#endif


