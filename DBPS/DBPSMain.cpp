#include "Common\Common.h"
#include "Server\DBPSServer.h"
#include "Common\Log.h"

int main(int argc, char **argv)
{
	DWORD dwRet;
	dwRet = InitLog(E_LOG_CONSOLE);
	//dwRet = InitLog(E_LOG_FILE);

	if (argc < 2) {
		DebugLog("[Usage] : Server Port, Server Connection");
		return E_RET_FAIL;
	}

	DebugLog("****************************************");
	DebugLog("******* Around You Server Init *********");
	DebugLog("****************************************");

	/*
	you have to configure argument for server
	argv[1] : Port
	argv[2] : Backlog
	*/
	std::string strPort, strConnectionLog;
	DWORD dwPort, dwConnection;
	strPort = argv[1];
	dwPort = ::atoi(strPort.c_str());

	strConnectionLog = argv[2];
	dwConnection = ::atoi(strConnectionLog.c_str());

	CDBPSServer *pCDBPSServer = NULL;
	pCDBPSServer = new CDBPSServer();
	if (!pCDBPSServer) {
		ErrorLog("Fail to get memory of AYServer");
		return E_RET_FAIL;
	}

	try
	{
		if (dwRet != E_RET_SUCCESS) {
			std::exception("Fail to init log");
		}

		DebugLog("****************************************");
		DebugLog("****** Around You Server Start *********");
		DebugLog("****************************************");

		dwRet = pCDBPSServer->StartServer(dwPort, dwConnection);
		if (dwRet != E_RET_SUCCESS) {
			throw std::exception("Fail to start server");
		}

		dwRet = pCDBPSServer->StopServer();
		if (dwRet != E_RET_SUCCESS) {
			throw std::exception("Fail to stop server");
		}

	}
	catch (std::exception &e)
	{
		ErrorLog("****************************************");
		ErrorLog("****** Around You Server Exit  *********");
		ErrorLog("****************************************");
		ErrorLog("%s", e.what());

		if (pCDBPSServer)
			delete pCDBPSServer;

		DeleteCriticalSection(&CriticalSection);
		return E_RET_FAIL;
	}

	DebugLog("****************************************");
	DebugLog("****** Around You Server Exit  *********");
	DebugLog("****************************************");

	if (pCDBPSServer)
		delete pCDBPSServer;

	DeleteCriticalSection(&CriticalSection);
	return E_RET_SUCCESS;
}