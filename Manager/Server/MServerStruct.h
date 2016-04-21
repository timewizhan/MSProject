#ifndef _AY_SERVER_STRUCT_
#define _AY_SERVER_STRUCT_

#include "..\Common\Common.h"

#define COUNT_THREAD 3
struct ST_THREAD_MANAGER
{
	HANDLE hThread[COUNT_THREAD];
};

struct ST_SHARED_MEM_INFO
{
	HANDLE hOnlyReadPipe;
	HANDLE hOnlyWritePipe;
};

struct ST_THREADS_PARAM
{
	DWORD				dwPort;
	DWORD				dwManagerNumber;
	std::string			strIPAddress;
	std::string			strFilePath;
	ST_SHARED_MEM_INFO	stSharedMemInfo;
};

struct ST_BOT_CONNECTION
{
	DWORD dwBotNumber;
	DWORD dwBotPID;
};


#endif