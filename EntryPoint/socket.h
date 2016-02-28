/*
* socket.h
*
*  Created on: Feb 4, 2016
*      Author: ms-dev
*/

#ifndef _SOCKET_
#define	_SOCKET_

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

public:
	WSADATA wsaData;
	int ssock;
	int clen;
	struct sockaddr_in client_addr, server_addr;
	fd_set read_fds, tmp_fds;
	int fd;

	struct monitoring_result write_message;

	CSocket();
	~CSocket();

	void init_socket();
	void send_message();
};

#endif


