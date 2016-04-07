#include "Common\Common.h"
#include "Common\Log.h"

#include "Server\MMServer.h"

int main(int argc, char **argv)
{
	DWORD dwRet = E_RET_FAIL;
	dwRet = InitLog(E_LOG_CONSOLE);

	if (argc < 4) {
		DebugLog("[Usage] : [Master Manager Port] [Health Port] [Manager Total Count]");
		return E_RET_FAIL;
	}

	DebugLog("****************************************");
	DebugLog("******** Manager Server Init **********");
	DebugLog("****************************************");

	/*
		you have to configure argument for server
		argv[1] : Master Manager Port
		argv[2] : Health Port
		argv[3] : Manager Total Count
	*/

	ST_INIT_ARG stInitArg;
	stInitArg.dwMMPort			= ::atoi(argv[1]);
	stInitArg.dwHealthPort		= ::atoi(argv[2]);
	stInitArg.dwCountOfManager	= ::atoi(argv[3]);

	CMMServer *pMMServer = new CMMServer();
	if (!pMMServer) {
		ErrorLog("Fail to get memory of CMMServer");
		return dwRet;
	}

	try
	{
		DebugLog("***********************************************");
		DebugLog("******** Master Manager Server Start **********");
		DebugLog("***********************************************");

		dwRet = pMMServer->StartServer(stInitArg);
		if (dwRet != E_RET_SUCCESS) {
			throw std::exception("Fail to start server");
		}
	}
	catch (std::exception &e)
	{
		ErrorLog("***********************************************");
		ErrorLog("******** Master Manager Server Exit  **********");
		ErrorLog("***********************************************");
		ErrorLog("%s", e.what());
	}

	DebugLog("***********************************************");
	DebugLog("******** Master Manager Server Exit  **********");
	DebugLog("***********************************************");

	if (pMMServer)
		delete pMMServer;

	return dwRet;
}