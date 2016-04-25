#include "Common\Common.h"
#include "Server\BGServer.h"
#include "Common\Log.h"

int main(int argc, char **argv)
{
	DWORD dwRet;
	dwRet = InitLog(E_LOG_CONSOLE);
	//dwRet = InitLog(E_LOG_FILE);

	if (argc < 2) {
		DebugLog("[Usage] : Server Port, Server DB Queue [Default 4]");
		return E_RET_FAIL;
	}

	DebugLog("******************************************");
	DebugLog("******* Broker Giver Server Init *********");
	DebugLog("******************************************");

	/*
		you have to configure argument for server
		argv[1] : Port
		argv[2] : Backlog
	*/
	std::string strPort, strDBQueue;
	DWORD dwPort, dwDBQueue;
	strPort = argv[1];
	dwPort = ::atoi(strPort.c_str());

	if (argc == 2) {
#define DEFAULT_QUEUE_SIZE 4
		dwDBQueue = DEFAULT_QUEUE_SIZE;
	}
	else {
		strDBQueue = argv[2];
		dwDBQueue = ::atoi(strDBQueue.c_str());
	}

	CBGServer *BGServer = NULL;
	BGServer = new CBGServer();
	if (!BGServer) {
		ErrorLog("Fail to get memory of AYServer");
		return E_RET_FAIL;
	}

	DebugLog("******************************************");
	DebugLog("****** Broker Giver Server Start *********");
	DebugLog("******************************************");

	try
	{
		dwRet = BGServer->StartServer(dwPort, dwDBQueue);
		if (dwRet != E_RET_SUCCESS) {
			throw std::exception("Fail to start server");
		}
	}
	catch (std::exception &e)
	{
		ErrorLog("******************************************");
		ErrorLog("****** Broker Giver Server Exit  *********");
		ErrorLog("******************************************");
		ErrorLog("%s", e.what());

		if (BGServer)
			delete BGServer;

		DeleteCriticalSection(&CriticalSection);
		return E_RET_FAIL;
	}

	DebugLog("******************************************");
	DebugLog("****** Broker Giver Server Exit  *********");
	DebugLog("******************************************");

	if (BGServer)
		delete BGServer;

	DeleteCriticalSection(&CriticalSection);
	return E_RET_SUCCESS;
}