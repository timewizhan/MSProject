#ifndef _SOCKET_
#define	_SOCKET_

#include "DataQueue.h"
#include "Common.h"
#include "SocketStruct.h"

#define MAX 100
#define ALL 1

class CSocket{

	WSADATA						wsaData;
	int							optval;
	//소켓옵션의 설정값
	int							ssock, csock;                       				//소켓
	struct sockaddr_in			server_addr, client_addr;        //IP와 Port값(즉 주소값)

	int							clen, data_len;
	fd_set						read_fds, tmp_fds;   				           //디스크립터 셋트(단일 비트 테이블)
	int							fd;

	struct add_num				add_num[USER]; 				    //서버에접속하는 클라이언트의 정보를 저장하는 객체
	int							index, maxfd;
	struct ST_MONITORING_RESULT read_message;				//클라이언트로 부터 받은 메세지 구조체
	char						greet[1];
	char						no_greet[1];

public:

	CSocket();
	~CSocket();

	void InitSocket();
	void CommSocket();
//	void SendMessage();
	void WriteMessage(void *client_message, void *num, int basefd, int maxfd);
};

#endif


