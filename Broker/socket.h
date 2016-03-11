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
	//���Ͽɼ��� ������
	int							ssock, csock;                       				//����
	struct sockaddr_in			server_addr, client_addr;        //IP�� Port��(�� �ּҰ�)

	int							clen, data_len;
	fd_set						read_fds, tmp_fds;   				           //��ũ���� ��Ʈ(���� ��Ʈ ���̺�)
	int							fd;

	struct add_num				add_num[USER]; 				    //�����������ϴ� Ŭ���̾�Ʈ�� ������ �����ϴ� ��ü
	int							index, maxfd;
	struct ST_MONITORING_RESULT read_message;				//Ŭ���̾�Ʈ�� ���� ���� �޼��� ����ü
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


