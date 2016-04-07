#ifndef _AY_SERVER_
#define _AY_SERVER_

#include "..\Common\Common.h"
#include "..\HelpTool\HelpTool.h"

#include "MMServerStruct.h"

struct ST_INIT_ARG
{
	DWORD dwMMPort;
	DWORD dwHealthPort;
	DWORD dwCountOfManager;
};

class CMMServer
{	
	ST_THREAD_MANAGER	m_stThreadManager;

	VOID InitControlThread();
	VOID InitManagerThread(DWORD dwPort, DWORD dwCountOfManager);
	VOID InitHealthThread(DWORD dwPort);

	VOID HandleError(DWORD dwRet) throw(std::exception);
public:
	CMMServer();
	~CMMServer();

	DWORD StartServer(ST_INIT_ARG &refstInitArg);
};

unsigned int WINAPI WorkerControlThread(void *pData);
unsigned int WINAPI WorkerManagerThread(void *pData);
unsigned int WINAPI WorkerHealthThread(void *pData);


#endif