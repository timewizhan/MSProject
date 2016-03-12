#ifndef _SOCKET_
#define	_SOCKET_

#include "DataQueue.h"
#include "Common.h"
#include "SocketStruct.h"

class CSocket{

	WSADATA						wsaData;
	int							optval;
	//家南可记狼 汲沥蔼
	int							ssock, csock;             
	struct sockaddr_in			server_addr, client_addr; 

	int							clen, data_len;
	fd_set						read_fds, tmp_fds;   		
	int							fd;

	struct ST_EP_INFO			stEpInfo[NUM_OF_EP];
	int							index, maxfd;
	struct ST_MONITORING_RESULT read_message;	

public:

	CSocket();
	~CSocket();

	void InitSocket();
	void CommSocket();
//	void SendMessage();
	void WriteMessage(void *client_message, void *num, int basefd, int maxfd);
};

#endif


