#include "Common\Common.h"
#include "Common\Log.h"

#include "Server\MServer.h"

int main(int argc, char **argv)
{
	DWORD dwRet = E_RET_FAIL;
	dwRet = InitLog(E_LOG_CONSOLE);

	if (argc < 6) {
		DebugLog("[Usage] : [Master Manager Port] [Manager Port] [Health Port] [Manager Number] [Bot Generator File Path]");
		return E_RET_FAIL;
	}

	DebugLog("****************************************");
	DebugLog("******** Manager Server Init **********");
	DebugLog("****************************************");

	/*
		you have to configure argument for server
		argv[1] : Master Manager Port
		argv[2] : Health Port
		argv[3] : Manager Port
		argv[4] : Manager Number
		argv[5] : Bot Generator File Path
	*/

	ST_INIT_ARG stInitArg;
	stInitArg.dwMMPort			= ::atoi(argv[1]);
	stInitArg.dwHealthPort		= ::atoi(argv[2]);
	stInitArg.dwMPort			= ::atoi(argv[3]);
	stInitArg.dwManagerNumber	= ::atoi(argv[4]);
	stInitArg.strBotGenFilePath	= argv[5];

	CMServer *pMServer = new CMServer();
	if (!pMServer) {
		ErrorLog("Fail to get memory of AYServer");
		return dwRet;
	}

	try
	{
		DebugLog("****************************************");
		DebugLog("******** Manager Server Start **********");
		DebugLog("****************************************");

		dwRet = pMServer->StartServer(stInitArg);
		if (dwRet != E_RET_SUCCESS) {
			throw std::exception("Fail to start server");
		}
	}
	catch (std::exception &e)
	{
		ErrorLog("****************************************");
		ErrorLog("******** Manager Server Exit  **********");
		ErrorLog("****************************************");
		ErrorLog("%s", e.what());
	}

	DebugLog("****************************************");
	DebugLog("******** Manager Server Exit  **********");
	DebugLog("****************************************");

	if (pMServer)
		delete pMServer;

	return dwRet;
}