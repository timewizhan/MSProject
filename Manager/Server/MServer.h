#ifndef _AY_SERVER_
#define _AY_SERVER_

#include "..\Common\Common.h"
#include "..\HelpTool\HelpTool.h"
#include "MServerStruct.h"

struct ST_INIT_ARG
{
	DWORD dwMMPort;
	DWORD dwMPort;
	DWORD dwHealthPort;
	DWORD dwManagerNumber;
	std::string strBotGenFilePath;
};

class CMServer
{	
	ST_THREAD_MANAGER	m_stThreadManager;
	ST_SHARED_MEM_INFO	m_stSharedMemInfo;

	VOID InitMMThread(DWORD dwPort, DWORD dwManagerNumber);
	VOID InitBotsThread(DWORD dwPort, std::string &refstrBotGenFilePath);
	VOID InitHealthThread(DWORD dwPort, DWORD dwManagerNumber);
	VOID InitAnonymousPipe();

	VOID HandleError(DWORD dwRet) throw(std::exception);

	BOOL IsBotGenFile(std::string &refstrFilePath);
	DWORD ExecuteBotGen();

public:
	CMServer();
	~CMServer();

	DWORD StartServer(ST_INIT_ARG &refstInitArg);
};

unsigned int WINAPI WorkerMMThread(void *pData);
unsigned int WINAPI WorkerBotsThread(void *pData);
unsigned int WINAPI WorkerHealthThread(void *pData);


#endif