/*
* socket.cpp
*
*  Created on: Feb 5, 2016
*      Author: alphahacker
*/
#define _WINSOCK_DEPRECATED_NO_WARNINGS

#include "DataQueue.h"
#include "Socket.h"
#include "Matching.h"

CSocket::CSocket(){

	optval = 1;
	InitSocket();
}
CSocket::~CSocket(){}

void CSocket::InitSocket(){

	printf("init_socket \n");

	if (WSAStartup(MAKEWORD(2, 2), &wsaData) != 0) {
		printf("error\r\n");
	}

	if ((ssock = socket(AF_INET, SOCK_STREAM, IPPROTO_TCP))<0) {

		perror("socket error : ");
		exit(1);
	}
	printf("socket ��ȯ��(ssock): %d \n", ssock);

	//���� �ɼ��� ����(�ɼ� �ؼ��� ���� Ŀ�� �� �ý��� �ڵ��� ����, �ɼ��̸�, �ɼ��� �� ��--SO_SNDBUF,SO_BROADCAST,SO_KEEPALIVE)
	setsockopt(ssock, SOL_SOCKET, SO_REUSEADDR, (const char*)&optval, sizeof(optval));

	//�ش� ������ �ʱ�ȭ, �ּҸ� ����
	memset(add_num, 0, sizeof(add_num));
	memset(&server_addr, 0, sizeof(server_addr));

	server_addr.sin_family = AF_INET;
	server_addr.sin_addr.s_addr = htonl(INADDR_ANY);
	server_addr.sin_port = htons(3333);

	//������ �ش� �ּҷ� ����
	if (bind(ssock, (struct sockaddr *)&server_addr, sizeof(server_addr))<0)
	{
		perror("bind error: ");
		exit(1);
	}

	//Ŭ���̾�Ʈ�� ������ ��ٸ�
	if (listen(ssock, 5)<0)
	{
		perror("listen error : ");
		exit(1);
	}
}

void CSocket::CommSocket(){

	printf("comm_socket \n");

	clen = sizeof(client_addr);

	//FD_SET ��ũ������ ����
	FD_ZERO(&read_fds);
	FD_SET(ssock, &read_fds);

	maxfd = ssock;

	int iECount = 0;
	
	while (1)
	{
		printf("1"); Sleep(1000);
		//fd_set��ũ���� ���̺��� ��ȸ��. �׷��� ������ �ش簪�� �̸� �Ű� ���� �����ؾ� �Ѵ�. �׷��� ������ ���縦 ���� �ϰ� �����ؾ� �Ѵ�.
		tmp_fds = read_fds;

		//�������̽� �󿡼� ����̽��� ���� �Է¿� ���� �ﰢ���� ������ �ʿ�.
		if (select(maxfd + 1, &tmp_fds, 0, 0, (struct timeval *)0)<1)
		{
			perror("select error : ");
			exit(1);
		}

		for (fd = 0; fd<maxfd + 1; fd++)
		{
			if (FD_ISSET(fd, &tmp_fds))
			{
				if (fd == ssock)
				{
					if ((csock = accept(ssock, (struct sockaddr *)&client_addr, &clen))<0)
					{
						perror("accept error : ");
						exit(0);
					}

					FD_SET(csock, &read_fds);

					printf("���ο� Ŭ���̾�Ʈ %d�� ���� ��ũ���� ����\n", csock);

					for (index = 0; index<MAX; index++)
					{
						if (add_num[index].anum == 0)
						{
							add_num[index].anum = csock;
							maxfd++;

							printf("(add_num[%d].anum = %d) ", index, csock);

							break;
						}
					}

					if (csock>maxfd)
					{
						maxfd = csock;
					}
				}
				else
				{
					memset(&read_message, 0, sizeof(read_message));

					//Ŭ���̾�Ʈ�� ���� �޼����� ���Ź޴´�.
					data_len = recv(fd, (char*)&read_message, sizeof(read_message), 0);	//read�� ���ϰ� ����

					if (read_message.ep_num != 0){
						printf("\n[read test, fd=%d] \n", fd);
						printf("EP: %d, Side: %s \n", read_message.ep_num, read_message.side_flag);
						printf("cpu_util: %d, server-side traffic: %d \n", read_message.cpu_util, read_message.server_side_traffic);
						printf("user: %s, location: %s, timestamp: %d, user traffic: %d \n", read_message.user, read_message.location, read_message.timestamp, read_message.traffic);
					}

					//���⼭ 1. ť �ҷ��� ť�� ������ �ȴ�. �׸��� ���� 2. ť���� �����鼭 ó�����ְ� 3. ��� ����
					//�� EP�� ���� ������ ���� ���� �޼����� ������(���´� ��...Ư�� �÷��׸� �����شٵ簡..), ť�� ������ ������ �� �ߴ��ϰ�
					//�׵��� ���� �����ͷ� LP ����� �ϰ� �Ѵ�.
					
					CDataQueue::getDataQueue()->pushDataToQueue(read_message);
				//	int iECount = 0;
					if (!strcmp(read_message.side_flag, "e")){		
						iECount++;
						if (iECount == 1){		//3���� EP���� ���� ������ Broker ���̺� �Է� �Ϸ�
							//���⼭ LP ��� �Լ� ȣ��
							CMatch match;
							match.NormalizeFactor();

						}
					}
			//		WaitForSingleObject(hThread, INFINITE);
					/*
					�����尡 queue�� ���ٰ� LPó������ ���ϰ� ����, ���⼭ ������ �� �ְ�
					pthread_join�� ���⼭ ȣ���ϸ�, �� ó���� LP�����
					���⼭ write ���༭ EP�鿡�� ������ �� �ְڴ�.
					*/

					//Ŭ���̾�Ʈ�κ��� �޽����� ���Դٸ� �޽��� ����
					/*
					if(data_len>0)
					{
					writeMessage((void*)&read_message,(void*)add_num,fd,maxfd);

					}
					else if(data_len==0)
					{
					for(index=0;index<USER;index++)
					{
					if(add_num[index].anum==fd)
					{
					add_num[index].anum=0;
					strcpy(add_num[index].name,"");
					break;
					}
					}

					close(fd);

					FD_CLR(fd,&read_fds);

					if(maxfd==fd)
					maxfd--;

					printf("Ŭ���̾�Ʈ %d�� ���� ��ũ���� ����\n",fd);

					}
					else if(data_len<0)
					{
					perror("read error : ");
					exit(1);
					}
					*/

				}
			}
		}




	}
}

//void CSocket::send_message(){}

void CSocket::WriteMessage(void *client_message, void *num, int basefd, int maxfd){

	int index;
	struct message *cl_message;
	struct add_num *index_num;
	char all[] = "ALL";

	cl_message = (struct message*)client_message;
	index_num = (struct add_num*)num;

	//Ŭ���̾�Ʈ�� �����ߴٰ� �޽����� ��������
	if (strcmp(cl_message->sbuf, "") == 0)
	{
		printf("��� �ϰڽ��ϴ�.\n");

		for (index = 0; index<USER; index++)
		{
			if (((index_num + index)->anum) == basefd)
			{
				strcpy_s((index_num + index)->name, 40, (cl_message->user));
			}
		}

		send(basefd, greet, sizeof(greet), 0);


	}
	//Ŭ���̾�Ʈ�� �ٸ� Ŭ���̾�Ʈ�� �޽����� ���� �� ��
	else
	{
		//��� ����ڿ��� �޽����� �����Ѵ�.
		all[strlen(all)] = '\0';

		if (strcmp(cl_message->user, all) == 0)
		{
			for (index = 0; index<maxfd; index++)
			{
				if ((index_num + index)->anum != 0)
					send(((index_num + index)->anum), cl_message->sbuf, MAXBUF, 0);
			}
		}
		//������ ����ڿ��� �޽����� �����Ѵ�.
		else
		{
			for (index = 0; index<USER; index++)
			{
				if (strcmp(((index_num + index)->name), cl_message->user) == 0)
				{
					if (send(((index_num + index)->anum), cl_message->sbuf, MAXBUF, 0)<0)
					{
						perror("write error : ");
						exit(1);
					}

					break;
				}

				//������ �������� �ʴ´ٸ�
				if (index + 1 == USER)
					send(basefd, no_greet, sizeof(no_greet), 0);

			}
		}
	}

}


