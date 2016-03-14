/*
* socket.cpp
*
*  Created on: Feb 4, 2016
*      Author: ms-dev
*/
#define _WINSOCK_DEPRECATED_NO_WARNINGS

#include "Common.h"
#include "socket.h"

CSocket::CSocket(){}
CSocket::~CSocket(){}

void CSocket::init_socket(){

	if (WSAStartup(MAKEWORD(2, 2), &wsaData) != 0){
		printf("error\r\n");
	}

	if ((ssock = socket(AF_INET, SOCK_STREAM, 0))<0){
		perror("socket error : ");
		exit(1);
	}

	//변수 초기화
	memset(&server_addr, 0, sizeof(server_addr));
	server_addr.sin_family = AF_INET;
	server_addr.sin_addr.s_addr = inet_addr("127.0.0.1");
	server_addr.sin_port = htons(3333);

	clen = sizeof(server_addr);

	//socket이라는 파일 서술자를 이용하여 소켓에 연결을 하게 된다.
	if (connect(ssock, (struct sockaddr *)&server_addr, clen)<0){
		perror("connect error : ");
	}
}

void CSocket::send_message(){

	//send message
	if (send(ssock, (char*)&write_message, sizeof(write_message), 0)<0){
		perror("write error : ");
		exit(1);
	}
}

match_result_data CSocket::recv_message(){

	//recv
	memset(&read_message, 0, sizeof(read_message));
	int data_len = recv(ssock, (char*)&read_message, sizeof(read_message), 0);
	printf("User: %s, Prev: %d, Curr: %d \n", read_message.sUser.c_str(), read_message.iPrevEp, read_message.iCurrEP);

	return read_message;
}