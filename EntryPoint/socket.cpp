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
	server_addr.sin_addr.s_addr = inet_addr("165.132.122.243");	//2번 PC - BROKER
//	server_addr.sin_addr.s_addr = inet_addr("165.132.120.144");
	server_addr.sin_port = htons(3333);

	clen = sizeof(server_addr);

	//socket이라는 파일 서술자를 이용하여 소켓에 연결을 하게 된다.
	if (connect(ssock, (struct sockaddr *)&server_addr, clen)<0){
		perror("connect error : ");
	}
}

void CSocket::InitSocketWithSNSServer(){

	if (WSAStartup(MAKEWORD(2, 2), &wsaData) != 0){
		printf("error\r\n");
	}

	if ((SNSServerCsocket = socket(AF_INET, SOCK_STREAM, 0))<0){
		perror("socket error : ");
		exit(1);
	}

	//변수 초기화
	memset(&server_addr, 0, sizeof(server_addr));
	server_addr.sin_family = AF_INET;
	server_addr.sin_addr.s_addr = inet_addr("127.0.0.1");
	server_addr.sin_port = htons(7777);

	clen = sizeof(server_addr);

	//socket이라는 파일 서술자를 이용하여 소켓에 연결을 하게 된다.
	if (connect(SNSServerCsocket, (struct sockaddr *)&server_addr, clen)<0){
		perror("connect error : ");
	}
}

void CSocket::SendStoreCmdMessage(){

	string sJSONMessage;
	sJSONMessage = "{\"TYPE\":\"5\"}\r\n";
	//send message
	if (send(SNSServerCsocket, (char*)&sJSONMessage, sizeof(sJSONMessage), 0)<0){
		perror("write error : ");
		exit(1);
	}
}

void CSocket::RecvStoreCompleteMessage(){
	char arrRecvCompleteMsg[50];
	memset(&arrRecvCompleteMsg, 0, sizeof(arrRecvCompleteMsg));

	while (1){
		int data_len = recv(SNSServerCsocket, (char*)&arrRecvCompleteMsg, sizeof(arrRecvCompleteMsg), 0);

		cout << "RECIEVED: " << arrRecvCompleteMsg;

		if (!strcmp(arrRecvCompleteMsg, "store_complete\r\n")){
			break;
		}
	}
//	printf("User: %s, Prev: %d, Curr: %d \n", read_message.arrUser, read_message.iPrevEp, read_message.iCurrEP);
}

void CSocket::SendMatchStoreCompleteMessage(){

	string sJSONMessage;
	sJSONMessage = "{\"TYPE\":\"6\"}\r\n";
	//send message
	if (send(SNSServerCsocket, (char*)&sJSONMessage, sizeof(sJSONMessage), 0)<0){
		perror("write error : ");
		exit(1);
	}
}

void CSocket::RecvDRCompleteMessage(){

	char arrRecvCompleteMsg[50];
	memset(&arrRecvCompleteMsg, 0, sizeof(arrRecvCompleteMsg));

	while (1){
		int data_len = recv(SNSServerCsocket, (char*)&arrRecvCompleteMsg, sizeof(arrRecvCompleteMsg), 0);

		if (!strcmp(arrRecvCompleteMsg, "data_replacement_complete\r\n")){
			break;
		}
	}
	//	printf("User: %s, Prev: %d, Curr: %d \n", read_message.arrUser, read_message.iPrevEp, read_message.iCurrEP);
}

void CSocket::CloseSNSServerSocket(){

	closesocket(SNSServerCsocket);
	WSACleanup();
}

void CSocket::CloseBrokerSocket(){

	closesocket(ssock);
	WSACleanup();
}

void CSocket::SendDRCompleteMessage(){	//broker에게 보내는 메세지

	char arrDRCompleteMessage[50];
	strcpy_s(arrDRCompleteMessage, 49, "data_replacement_complete");
	if (send(ssock, (char*)&arrDRCompleteMessage, sizeof(arrDRCompleteMessage), 0)<0){
		perror("write error : ");
		exit(1);
	}
}

void CSocket::RecvSyncMessage(){	//broker로 부터 받는 메세지

	char arrSyncMsg[10];
	memset(&arrSyncMsg, 0, sizeof(arrSyncMsg));

	int data_len = recv(ssock, (char*)&arrSyncMsg, sizeof(arrSyncMsg), 0);
	if (data_len < 0){
		printf("Couldn't Receive Sync Message From BROKER \n");
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
	
	printf("User: %s, Prev: %d, Curr: %d \n", read_message.arrUser, read_message.iPrevEp, read_message.iCurrEP);

	return read_message;
}