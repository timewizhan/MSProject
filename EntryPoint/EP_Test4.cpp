/*
 * EP_Test4.cpp
 *
 *  Created on: Jan 23, 2016
 *      Author: alphahacker
 */

#include "Common.h"
#include "database.h"
#include "EP_Test4.h"
#include "mysql.h"

EP_Test4::EP_Test4(){}
EP_Test4::~EP_Test4(){}

void EP_Test4::initEntryPoint()
{
	//socket
	entrySockStart();
}

int EP_Test4::entrySockStart()
{

	if((ssock = socket(AF_INET,SOCK_STREAM,0))<0){
			perror("soket error : ");
			exit(1);
		}

		//변수 초기화
		memset(&server_addr,0,sizeof(server_addr));
		server_addr.sin_family = AF_INET;
		server_addr.sin_addr.s_addr = inet_addr("127.0.0.1");
		server_addr.sin_port = htons(3333);

		clen = sizeof(server_addr);

		//사용자를 입력받는다.
		printf("사용자를 입력하시오 : ");
		fgets(name,MAXBUF,stdin);
		*(name+(strlen(name)-1))='\0';

		//socket이라는 파일 서술자를 이용하여 소켓에 연결을 하게 된다.
		if(connect(ssock,(struct sockaddr *)&server_addr, clen)<0){
			perror("connect error : ");
			return -1;
		}

		//이부분에 디비연결하고, 디비에서 데이터 읽어서 변수에 저장한다
		db.initDB();
		db.extractData();

}


void EP_Test4::sendMessage()
{
	//해당 메시지를 전송한다.
	if(write(ssock,(struct message*)&write_message,sizeof(write_message))<0){
		perror("write error : ");
		exit(1);
	}
	/*
	//파일 디스크립트 테이블을 새로 셋팅
	FD_ZERO(&read_fds);
	FD_SET(ssock,&read_fds);
	FD_SET(0,&read_fds);

	fd=ssock;

	while(1){
		//일회성 복사
		tmp_fds=read_fds;

		//변화를 감지한다.
		if(select(fd+1,&tmp_fds,0,0,0)<0){
			perror("select error : ");
			exit(1);
		}

		//클라이언트 소켓에 변화가 있다는 것을 확인한다.
		if(FD_ISSET(ssock,&tmp_fds)){
			memset(buf,0,MAXBUF);
			re = read(ssock,buf,MAXBUF);
			if(re<0){
				perror("read error : ");
				exit(1);
			}
			//클라이언트 서버가 접속이 끊켰다는 것을 확인한다.
			else if(re==0){
				printf("서버와의 접속이 끊켰습니다.\n");
				break;
			}
			printf("%s\n",buf);
			//printf("전송할 메시지를 입력해 주세요 : ");
		}
		//입력에 대한 변화가 있다는 것을 확인한다.
		else if(FD_ISSET(0,&tmp_fds)){


			memset(&write_message,0,sizeof(write_message));
			memset(buf,0,MAXBUF);

			//보낼 user를 입력
			fgets(write_message.user,USER,stdin);
			*(write_message.user+(strlen(write_message.user)-1))='\0';
			//보낼 메시지를 입력
			printf("전송할 메시지를 입력해 주세요 : ");
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

