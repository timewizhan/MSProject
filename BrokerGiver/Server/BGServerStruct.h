#ifndef _AY_SERVER_STRUCT_
#define _AY_SERVER_STRUCT_

#include "..\Common\Common.h"

struct ST_WORKER_THREAD
{
	SOCKET		hClientSocket;
};

struct ST_CLIENT_SOCKET
{
	SOCKET			hClientSock;
	SOCKADDR_IN		stClientAddrIn;
};

struct ST_SERVER_INIT
{
	WSADATA			stWSAData;
	SOCKET			hServerSock;
	SOCKADDR_IN		stServerAddrIn;

	std::vector<std::string> vecstrAddress;
	DWORD dwPort;
};

struct ST_SERVER_WORKER_THREAD
{
	HANDLE *phWorkerThread;
	HANDLE hBrokerThread;
	DWORD	dwNumberOfThread;
};

struct ST_SERVER_IOCP_DATA
{
	HANDLE	hCompletionPort;
};

#define MAX_CLIENT_MSG_BUF 1024
struct ST_SERVER_CONNECTION
{
	OVERLAPPED stOverLapped;
	SOCKET	ClientSock;
	SOCKADDR_IN		stClientAddrIn;
	char	szBuf[MAX_CLIENT_MSG_BUF];
	WSABUF	stWSABuf;
};

struct ST_SERVER_STATUS
{
	DWORD dwNumberOfCurrentSocket;
};

#endif