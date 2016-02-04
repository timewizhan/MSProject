/*
 * broker_server.cpp
 *
 *  Created on: Jan 20, 2016
 *      Author: ms-dev
 */

#include "common.h"
#include "broker_server.h"
#include "data_queue.h"
#include "database.h"

//////////////////////////////////////////////////////////////////////////////////////////////////////////////////
CBrokerServer::CBrokerServer(){

	optval = 1;							//socket option value
	greet[0] = 'O';	no_greet[0] = 'X';	//접속이 성공되었는지 여부를 표시
}

CBrokerServer::~CBrokerServer(){

}

void CBrokerServer::initThread(){

	printf("initThread \n");

	int thr_id;
	pthread_t thread_for_queue;

	thr_id = pthread_create(&thread_for_queue, NULL, preprocess_insert, NULL);
}

void CBrokerServer::initBroker(){

	printf("initBroker \n");

	//여기서 큐를 모니터링? 관리? 하는 스레드를 만들고
	//메인 스레드에서 데이터를 받으면 바로 거기서 큐에 푸쉬할텐데
	//여기서 만드는 큐 모니터링 스레드는 큐를 보고있다가 들어오면 바로 디비 처리하는 곳으로 넘긴다
	initThread();
	communicateWithEP();
}

void CBrokerServer::setSocket(){

	printf("setSocket \n");

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

void CBrokerServer::acceptEP(){

	printf("acceptEP \n");

	clen=sizeof(client_addr);

	//FD_SET 디스크립터의 세팅
	FD_ZERO(&read_fds);
	FD_SET(ssock,&read_fds);

	maxfd= ssock;

//	CDatabase::getDatabaseInstance()->initDB();
	db.initDB();

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

							printf("(add_num[%d].anum = %d) ", index, csock);

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
					data_len = read(fd,(struct message*)&read_message,sizeof(read_message));	//read의 리턴값 뭘까

					if(read_message.ep_num != 0){
						printf("\n[read test, fd=%d] \n", fd);
						printf("EP: %d, Side: %c \n",read_message.ep_num, read_message.side_flag);
						printf("cpu_util: %d, server-side traffic: %d \n",read_message.cpu_util, read_message.server_side_traffic);
						printf("user: %s, location: %s, timestamp: %d, user traffic: %d \n",read_message.user, read_message.location, read_message.timestamp, read_message.traffic);
					}

					//여기서 1. 큐 불러서 큐에 쌓으면 된다. 그리고 나서 2. 큐에서 꺼내면서 처리해주고 3. 디비에 저장
					//문제는 다른 종류의 데이터 두개를 받아야 되는데 어떻게 할것인가..
				//	printf("when data is queued, the data_queue address is : %x \n", &data_queue);
				//	data_queue.enqueue(read_message);

					CDataQueue::getDataQueue()->pushDataToQueue(read_message);

					/*
					스레드가 queue에 갔다가 LP처리까지 다하고 오면, 여기서 합쳐질 수 있게
					pthread_join을 여기서 호출하면, 다 처리된 LP결과를
					여기서 write 해줘서 EP들에게 보내줄 수 있겠다.
					*/

					//클라이언트로부터 메시지가 들어왔다면 메시지 전송
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

						printf("클라이언트 %d번 파일 디스크립터 해제\n",fd);

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


void CBrokerServer::communicateWithEP()	// parameter에 void 포인터를 넣었다는건, 데이터가 들어가도 안들어가도 된다는 의미?!
{
	printf("communicateWithEP \n");

//	bs.setSocket();
//	bs.acceptEP();
	setSocket();
	acceptEP();
}


void CBrokerServer::writeMessage(void *client_message,void *num,int basefd,int maxfd){

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

void* preprocess_insert(void *data)
{
	printf("preprocess_insert \n");

	while(1){


		if(!CDataQueue::getDataQueue()->getQueue().empty()){	//queue가 비어있지 않으면..
			//일단 큐에서 꺼내자

			monitoring_result poppedData = CDataQueue::getDataQueue()->popDataFromQueue();

			printf("\n[read test in preprocess of inserting data] \n");
			printf("EP: %d, Side: %c \n", poppedData.ep_num, poppedData.side_flag);
			printf("cpu_util: %d, server-side traffic: %d \n",poppedData.cpu_util, poppedData.server_side_traffic);
			printf("user: %s, location: %s, timestamp: %d, user traffic: %d \n",poppedData.user, poppedData.location
					,poppedData.timestamp, poppedData.traffic);

			/*
			//지역 별로 정리...
			if(strcmp(pop_out_data->location, "NY")){
				int ny_local_traffic = select * from local_traffic where location = "NY";
				ny_local_traffic += pop_out_data->traffic;
				테이블 수정 modify 였던가..
			}else if(strcmp(pop_out_data->location, "BS")){

			}
			*/

			//나머지 pop out data 요소들.. ep_num, flag, user, server side traffic, cpu util... 등등은 바로 broker table에 넣는다
		//	CDatabase::getDatabaseInstance()->initDB();
		//	CDatabase::getDatabaseInstance()->insertData(poppedData.user, poppedData.location
		//	,poppedData.timestamp, poppedData.traffic);
			CDatabase db;
			db.initDB();
			db.insertData(poppedData.user, poppedData.location
						,poppedData.timestamp, poppedData.traffic);

		}

		/*
		while(1)문을 탈출하는 조건이 있을텐데, 이를테면, 모니터링 한 결과를 EP로 부터 모두 받게되면 가장 마지막 데이터에 특수 기호 같은 걸 넣어서
		이게 마지막이다. 라는걸 알 수 있게 한다는 등이 될 것이다, 아마도?
		그러면 반복문을 나와서, local traffic 측정한 값을 여기서 넣는다.


		그리고 여기서 LP 알고리즘을 호출해야할듯.
		*/

	//	printf(". \n");
	//	sleep(1);

	}
}


