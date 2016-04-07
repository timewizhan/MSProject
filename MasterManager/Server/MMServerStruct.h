#ifndef _AY_SERVER_STRUCT_
#define _AY_SERVER_STRUCT_

#include "..\Common\Common.h"

#define COUNT_THREAD 3
struct ST_THREAD_MANAGER
{
	HANDLE hThread[COUNT_THREAD];
};

struct ST_THREADS_PARAM
{
	DWORD				dwPort;
	DWORD				dwCountOfManager;

	ST_THREADS_PARAM() : dwPort(0), dwCountOfManager(0) {}
};

#endif