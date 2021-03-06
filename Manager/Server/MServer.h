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
	std::string strMMIPAddress;
	std::string strBotGenFilePath;

	ST_INIT_ARG() : dwMMPort(0), dwMPort(0), dwHealthPort(0), dwManagerNumber(0), strMMIPAddress(""), strBotGenFilePath("") {}
};

class CMServer
{	
	ST_THREAD_MANAGER	m_stThreadManager;
	ST_SHARED_MEM_INFO	m_stSharedMemInfo;

	VOID InitMMThread(DWORD dwPort, DWORD dwManagerNumber, std::string &refstrIPAddress);
	VOID InitBotsThread(DWORD dwPort, DWORD dwManagerNumber, std::string &refstrBotGenFilePath);
	VOID InitHealthThread(DWORD dwPort, DWORD dwManagerNumber, std::string &refstrIPAddress);
	VOID InitAnonymousPipe();

	VOID HandleError(DWORD dwRet);

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