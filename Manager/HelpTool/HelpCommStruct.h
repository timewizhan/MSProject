#ifndef _M_HELP_COMM_STRUCT_
#define _M_HELP_COMM_STRUCT_

#include "..\Common\Common.h"

struct ST_CLIENT_INFO
{
	SOCKET			hServerSock;
	SOCKADDR_IN		sockAddr;
};

struct ST_CLIENT_CONTEXT
{
	WSADATA			stWSAData;
	ST_CLIENT_INFO	stServerInfo;
};

struct ST_SERVER_ADDR
{
	std::string strIPAddress;
	DWORD dwPort;
};

struct ST_CLIENT_SOCKET
{
	SOCKET			hClientSock;
	SOCKADDR_IN		stClientAddrIn;
};

typedef std::vector<std::string> VecIPAddress;
struct ST_SERVER_BIND
{
	VecIPAddress	vecstrAddress;
	DWORD			dwPort;
};

struct ST_SERVER_INFO
{
	SOCKET			hServerSock;
	SOCKADDR_IN		stServerAddrIn;
	ST_SERVER_BIND	stServerBind;
};

struct ST_SERVER_CONTEXT
{
	WSADATA			stWSAData;
	ST_SERVER_INFO	stServerInfo;
};


#endif