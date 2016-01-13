/*
 * Broker_Test4.cpp
 *
 *  Created on: Jan 13, 2016
 *      Author: ms-dev
 */
#include "Common.h";
#include "Broker_Test4.h";


//생성자
Broker_Test4::Broker_Test4()
{
	//socket option value
	optval = 1;

	//접속이 성공되었는지 여부를 표시
	greet[0] = 'O';
	no_greet[0] = 'X';
}

//소멸자
Broker_Test4::~Broker_Test4()
{
}

void Broker_Test4::initBroker()
{

	//1. socket setting
	brokerSockStart();

	//2. DB

	//3. etc..

}

void Broker_Test4::brokerSockStart()
{

	if((ssock = socket(AF_INET,SOCK_STREAM,IPPROTO_TCP))<0)
	{
				perror("socket error : ");
				exit(1);
	}

	//소켓 옵션을 설정(옵션 해석을 위한 커널 내 시스템 코드의 구분, 옵션이름, 옵션의 값 등--SO_SNDBUF,SO_BROADCAST,SO_KEEPALIVE)
	setsockopt(ssock,SOL_SOCKET,SO_REUSEADDR, &optval,sizeof(optval));

	//해당 변수를 초기화, 주소를 저장
	memset(add_num,0,sizeof(add_num));
	memset(&server_addr, 0, sizeof(server_addr));

	server_addr.sin_family = AF_INET;
	server_addr.sin_addr.s_addr = htonl(INADDR_ANY);
	server_addr.sin_port = htons(3333);

	//소켓을 해당 주소로 연결
	if(bind(ssock,(struct sockaddr *)&server_addr,sizeof(server_addr))<0)
	{
		perror("bind error: ");
		exit(1);
	}

	//클라이언트의 접속을 기다림
	if(listen(ssock,5)<0)
	{
		perror("listen error : ");
		exit(1);
	}

}

//select, accept
void Broker_Test4::acceptEP()
{
	clen=sizeof(client_addr);

	//FD_SET 디스크립터의 세팅
	FD_ZERO(&read_fds);
	FD_SET(ssock,&read_fds);

	maxfd= ssock;


	while(1)
	{
		//fd_set디스크립터 테이블은 일회성. 그렇기 때문에 해당값을 미리 옮겨 놓고 시작해야 한다. 그렇기 때문에 복사를 먼져 하고 시작해야 한다.
		tmp_fds=read_fds;

		//인터페이스 상에서 디바이스에 들어온 입력에 대한 즉각적인 대응이 필요.
		if(select(maxfd+1,&tmp_fds,0,0,(struct timeval *)0)<1)
		{
			perror("select error : ");
			exit(1);
		}

		for(fd=0;fd<maxfd+1;fd++)
		{
			if(FD_ISSET(fd,&tmp_fds))
			{
				if(fd==ssock)
				{
					if((csock = accept(ssock,(struct sockaddr *)&client_addr,&clen))<0)
					{
						perror("accept error : ");
						exit(0);
					}

					FD_SET(csock,&read_fds);

					printf("새로운 클라이언트 %d번 파일 디스크립터 접속\n",csock);

					for(index=0;index<MAX;index++)
					{
						if(add_num[index].anum==0)
						{
							add_num[index].anum=csock;
							maxfd++;

							break;
						}
					}

					if(csock>maxfd)
					{
						maxfd = csock;
					}
				}
				else
				{
					memset(&read_message,0,sizeof(read_message));

					//클라이언트로 부터 메세지를 수신받는다.
					data_len = read(fd,(struct message*)&read_message,sizeof(read_message));

					//클라이언트로부터 메시지가 들어왔다면 메시지 전송
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

						printf("클라이언트 %d번 파일 디스크립터 해제\n",fd);

					}
					else if(data_len<0)
					{
						perror("read error : ");
						exit(1);
					}
				}
			}
		}
	}

}

//메세지 전송 함수
//사용자로부터 받은 메시지를 해당 클라이언트에 전송해 주는 함수
void Broker_Test4::writeMessage(void *client_message,void *num,int basefd,int maxfd)
{
	int index;
	struct message *cl_message;
	struct add_num *index_num;
	char all[]="ALL";

	cl_message = (struct message*)client_message;
	index_num = (struct add_num*)num;

	//클라이언트가 접속했다고 메시지를 보냈을때
	if(strcmp(cl_message->sbuf,"")==0)
	{
		printf("등록 하겠습니다.\n");

		for(index=0;index<USER;index++)
		{
			if(((index_num+index)->anum)==basefd)
			{
				strcpy((index_num+index)->name,(cl_message->user));
			}
		}

		write(basefd,greet,sizeof(greet));


	}
	//클라이언트가 다른 클라이언트에 메시지를 전송 할 때
	else
	{
		//모든 사용자에게 메시지를 전송한다.
		all[strlen(all)]='\0';

		if(strcmp(cl_message->user,all)==0)
		{
			for(index=0;index<maxfd;index++)
			{
				if((index_num+index)->anum!=0)
					write(((index_num+index)->anum),cl_message->sbuf,MAXBUF);
			}
		}
		//지정된 사용자에게 메시지를 전송한다.
		else
		{
			for(index=0;index<USER;index++)
			{
				if(strcmp(((index_num+index)->name),cl_message->user)==0)
				{
					if(write(((index_num+index)->anum),cl_message->sbuf,MAXBUF)<0)
					{
						perror("write error : ");
						exit(1);
					}

					break;
				}

				//유저가 존재하지 않는다면
				if(index+1==USER)
					write(basefd,no_greet,sizeof(no_greet));

			}
		}
	}
}
