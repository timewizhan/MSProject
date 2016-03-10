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
	printf("socket value (ssock): %d \n", ssock);

	//소켓 옵션을 설정(옵션 해석을 위한 커널 내 시스템 코드의 구분, 옵션이름, 옵션의 값 등--SO_SNDBUF,SO_BROADCAST,SO_KEEPALIVE)
	setsockopt(ssock, SOL_SOCKET, SO_REUSEADDR, (const char*)&optval, sizeof(optval));

	//해당 변수를 초기화, 주소를 저장
	memset(add_num, 0, sizeof(add_num));
	memset(&server_addr, 0, sizeof(server_addr));

	server_addr.sin_family = AF_INET;
	server_addr.sin_addr.s_addr = htonl(INADDR_ANY);
	server_addr.sin_port = htons(3333);

	//소켓을 해당 주소로 연결
	if (bind(ssock, (struct sockaddr *)&server_addr, sizeof(server_addr))<0)
	{
		perror("bind error: ");
		exit(1);
	}

	//클라이언트의 접속을 기다림
	if (listen(ssock, 5)<0)
	{
		perror("listen error : ");
		exit(1);
	}
}

void CSocket::CommSocket(){

	printf("comm_socket \n");

	clen = sizeof(client_addr);

	printf("before FD_SET \n");
	//FD_SET 디스크립터의 세팅
	FD_ZERO(&read_fds);
	FD_SET(ssock, &read_fds);

	maxfd = ssock;

	int iECount = 0; 
	
	while (1)
	{
		printf("1.start while \n"); Sleep(1000);
		//fd_set디스크립터 테이블은 일회성. 그렇기 때문에 해당값을 미리 옮겨 놓고 시작해야 한다. 그렇기 때문에 복사를 먼져 하고 시작해야 한다.
		tmp_fds = read_fds;
		printf("1-1.tmp_fds = %d \n", tmp_fds);
		
		//인터페이스 상에서 디바이스에 들어온 입력에 대한 즉각적인 대응이 필요.
		printf("2.before select \n");
		if (select(maxfd + 1, &tmp_fds, 0, 0, (struct timeval *)0)<1)
		{
			perror("select error : ");
			exit(1);
		}
		printf("2-2.start select \n");
		for (fd = 0; fd<maxfd + 1; fd++)
		{
			if (FD_ISSET(fd, &tmp_fds))
			{
				///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
				if (fd == ssock)
				{
					printf("3.(s).before accept, fd(ssock) = %d \n", fd);
					if ((csock = accept(ssock, (struct sockaddr *)&client_addr, &clen))<0)
					{
						perror("accept error : ");
						exit(0);
					}
					printf("3-1.(s).after accept \n");

					FD_SET(csock, &read_fds);

					printf("3-2.(s).새로운 클라이언트 %d번 파일 디스크립터 접속\n", csock);

					for (index = 0; index<MAX; index++)
					{
						if (add_num[index].anum == 0)
						{
							add_num[index].anum = csock;
							maxfd++;

							printf("(add_num[%d].anum = %d) \n", index, csock);

							break;
						}
						printf("%d", index);
					}

					if (csock>maxfd)
					{
						maxfd = csock;
					}
				}
				///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////			
				else
				{
					printf("3.(c). fd(!ssock) = %d \n", fd);
					memset(&read_message, 0, sizeof(read_message));

					//클라이언트로 부터 메세지를 수신받는다.
					data_len = recv(fd, (char*)&read_message, sizeof(read_message), 0);	//read의 리턴값 뭘까
					/*
					if (read_message.ep_num != 0){
						printf("\n[read test, fd=%d] \n", fd);
						printf("EP: %d, Side: %s \n", read_message.ep_num, read_message.side_flag);
						printf("cpu_util: %d, server-side traffic: %d \n", read_message.cpu_util, read_message.server_side_traffic);
						printf("user: %s, location: %s, timestamp: %d, user traffic: %d \n", read_message.user, read_message.location, read_message.timestamp, read_message.traffic);
					}
					*/

					//여기서 1. 큐 불러서 큐에 쌓으면 된다. 그리고 나서 2. 큐에서 꺼내면서 처리해주고 3. 디비에 저장
					//각 EP로 부터 데이터 전송 종료 메세지를 받으면(형태는 뭐...특정 플래그를 보내준다든가..), 큐로 데이터 보내는 걸 중단하고
					//그동안 쌓인 데이터로 LP 계산을 하게 한다.
					
					CDataQueue::getDataQueue()->pushDataToQueue(read_message);
				//	int iECount = 0;
					if (!strcmp(read_message.side_flag, "e")){		
						iECount++;
						if (iECount == 2){		//3개의 EP에서 보낸 데이터 Broker 테이블 입력 완료
							//여기서 LP 계산 함수 호출
							CMatch match;
							match.NormalizeFactor();
							match.InsertWeightTable();
							match.CalculateLP();
							
							//여기서 EP에게 돌려주는 걸 하자
							printf("4. \n");
							printf("4-1. EP: %d, Side: %s \n", read_message.ep_num, read_message.side_flag);
						//	WriteMessage((void*)&read_message, (void*)add_num, fd, maxfd);
						//	send(basefd, greet, sizeof(greet), 0);
							send(fd, (char*)&read_message, sizeof(read_message), 0);

						}
					}

				}
			}

		//	printf("end for statement \n");
		}




	}
}

//void CSocket::send_message(){}
/*
void CSocket::WriteMessage(void *client_message, void *num, int basefd, int maxfd){

	int index = 0;
	struct message *cl_message;
	struct add_num *index_num;
	char all[] = "ALL";

	cl_message = (struct message*)client_message;
	index_num = (struct add_num*)num;

	//클라이언트가 접속했다고 메시지를 보냈을때
	if (strcmp(cl_message->sbuf, "") == 0)
	{
		printf("등록 하겠습니다.\n");

		for (index = 0; index<USER; index++)
		{
			if (((index_num + index)->anum) == basefd)
			{
				strcpy_s((index_num + index)->name, 40, (cl_message->user));
			}
		}
		send(basefd, greet, sizeof(greet), 0);


	}
	//클라이언트가 다른 클라이언트에 메시지를 전송 할 때
	else
	{
		int ret = 0;
		ret = send(((index_num + index)->anum), cl_message->sbuf, MAXBUF, 0);
		
		int errNumber = WSAGetLastError();
		if (ret < 0) {
			printf("=== %d\n", errNumber);
		}
	}

}
*/

