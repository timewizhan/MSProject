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

	//���� �ʱ�ȭ
	memset(&server_addr, 0, sizeof(server_addr));
	server_addr.sin_family = AF_INET;
	server_addr.sin_addr.s_addr = inet_addr("127.0.0.1");
	server_addr.sin_port = htons(3333);

	clen = sizeof(server_addr);

	//socket�̶�� ���� �����ڸ� �̿��Ͽ� ���Ͽ� ������ �ϰ� �ȴ�.
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

	/*
	//���� ��ũ��Ʈ ���̺��� ���� ����
	FD_ZERO(&read_fds);
	FD_SET(ssock,&read_fds);
	FD_SET(0,&read_fds);

	fd=ssock;

	while(1){
	//��ȸ�� ����
	tmp_fds=read_fds;

	//��ȭ�� �����Ѵ�.
	if(select(fd+1,&tmp_fds,0,0,0)<0){
	perror("select error : ");
	exit(1);
	}

	//Ŭ���̾�Ʈ ���Ͽ� ��ȭ�� �ִٴ� ���� Ȯ���Ѵ�.
	if(FD_ISSET(ssock,&tmp_fds)){
	memset(buf,0,MAXBUF);
	re = read(ssock,buf,MAXBUF);
	if(re<0){
	perror("read error : ");
	exit(1);
	}
	//Ŭ���̾�Ʈ ������ ������ ���״ٴ� ���� Ȯ���Ѵ�.
	else if(re==0){
	printf("�������� ������ ���׽��ϴ�.\n");
	break;
	}
	printf("%s\n",buf);
	//printf("������ �޽����� �Է��� �ּ��� : ");
	}
	//�Է¿� ���� ��ȭ�� �ִٴ� ���� Ȯ���Ѵ�.
	else if(FD_ISSET(0,&tmp_fds)){


	memset(&write_message,0,sizeof(write_message));
	memset(buf,0,MAXBUF);

	//���� user�� �Է�
	fgets(write_message.user,USER,stdin);
	*(write_message.user+(strlen(write_message.user)-1))='\0';
	//���� �޽����� �Է�
	printf("������ �޽����� �Է��� �ּ��� : ");
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
	*/
}
