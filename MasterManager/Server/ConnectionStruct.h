#ifndef _CONNECTION_STRUCT_
#define _CONNECTION_STRUCT_

#include "..\Common\Common.h"

enum E_CONNECTION_STATE
{
	E_CONN_ESTABLISHED = 1,
	E_CONN_COMPLETE_READY,
	E_CONN_ALIVE,
	E_CONN_NOT_ALIVE,

	E_CONN_UNKNOWN
};

struct ST_CONNECTION_STATE
{
	DWORD dwConnectionNumber;
	DWORD dwState;
	time_t llTime;


	ST_CONNECTION_STATE() : dwConnectionNumber(0), dwState(E_CONN_ESTABLISHED), llTime(0) {}
};

struct ST_CONNECTION_INFO
{
	DWORD				dwConnectionNumber;
	ST_CONNECTION_STATE	stConnectionState;
	ST_CLIENT_SOCKET	stClientSocket;
};

typedef std::vector<ST_CONNECTION_INFO> VecConnection;

struct ST_MANAGER_CONNECTION
{
	DWORD dwManagerNumber;
};

#endif