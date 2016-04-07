#include "..\Common\Log.h"

#include "MServer.h"
#include "BotsThread.h"
#include "MMThread.h"
#include "HealthThread.h"
#include "MServerError.h"

///////////////////////////////////////////////////////////////////////////////////////////////////////
CMServer::CMServer()
{
	/*
		Server initializer has a role only for initializing variable
	*/
	::memset(&m_stThreadManager, 0x00, sizeof(m_stThreadManager));
}

///////////////////////////////////////////////////////////////////////////////////////////////////////
CMServer::~CMServer()
{
}

///////////////////////////////////////////////////////////////////////////////////////////////////////
VOID CMServer::InitMMThread(DWORD dwPort, DWORD dwManagerNumber)
{
	ST_THREADS_PARAM stThreadsParam;
	stThreadsParam.dwPort = dwPort;
	stThreadsParam.dwManagerNumber = dwManagerNumber;
	stThreadsParam.stSharedMemInfo = m_stSharedMemInfo;

	HANDLE hThread = NULL;
	hThread = (HANDLE)_beginthreadex(NULL, 0, WorkerMMThread, (void *)&stThreadsParam, 0, NULL);
	::Sleep(1000);
	if (!hThread) {
		DebugLog("Master Manager Thread is not created");
		return;
	}

	m_stThreadManager.hThread[0] = hThread;
	DebugLog("Master Manager Thread is created");
}

///////////////////////////////////////////////////////////////////////////////////////////////////////
VOID CMServer::InitBotsThread(DWORD dwPort, std::string &refstrBotGenFilePath)
{
	ST_THREADS_PARAM stThreadsParam;
	stThreadsParam.dwPort = dwPort;
	stThreadsParam.strFilePath = refstrBotGenFilePath;
	stThreadsParam.stSharedMemInfo = m_stSharedMemInfo;

	HANDLE hThread = NULL;
	hThread = (HANDLE)_beginthreadex(NULL, 0, WorkerBotsThread, (void *)&stThreadsParam, 0, NULL);
	::Sleep(1000);
	if (!hThread) {
		DebugLog("Bots Thread is not created");
		return;
	}

	m_stThreadManager.hThread[1] = hThread;
	DebugLog("Bots Thread is created");
}

///////////////////////////////////////////////////////////////////////////////////////////////////////
VOID CMServer::InitHealthThread(DWORD dwPort, DWORD dwManagerNumber)
{
	ST_THREADS_PARAM stThreadsParam;
	stThreadsParam.dwPort = dwPort;
	stThreadsParam.dwManagerNumber = dwManagerNumber;

	HANDLE hThread = NULL;
	hThread = (HANDLE)_beginthreadex(NULL, 0, WorkerHealthThread, (void *)&stThreadsParam, 0, NULL);
	::Sleep(1000);
	if (!hThread) {
		DebugLog("Bots Thread is not created");
		return;
	}

	m_stThreadManager.hThread[2] = hThread;
	DebugLog("Health Thread is created");
}

///////////////////////////////////////////////////////////////////////////////////////////////////////
VOID CMServer::HandleError(DWORD dwRet) throw(std::exception)
{
	if (dwRet == WAIT_OBJECT_0) {
		return;
	}
	else if (dwRet == WAIT_TIMEOUT) {
		throw std::exception("Occured Timeout");
	}
	else {
		throw std::exception("Unknown Error");
	}
}

///////////////////////////////////////////////////////////////////////////////////////////////////////
BOOL CMServer::IsBotGenFile(std::string &refstrFilePath)
{
	DWORD dwRet = ::GetFileAttributesA(refstrFilePath.c_str());
	if (dwRet == INVALID_FILE_ATTRIBUTES)
		return FALSE;

	if (dwRet & FILE_ATTRIBUTE_ARCHIVE)
		return TRUE;

	return FALSE;
}

///////////////////////////////////////////////////////////////////////////////////////////////////////
VOID CMServer::InitAnonymousPipe()
{
	// We use default buffering size, thus nSize is 0

	DWORD dwRet;
	dwRet = ::CreatePipe(&m_stSharedMemInfo.hOnlyReadPipe, &m_stSharedMemInfo.hOnlyWritePipe, NULL, 0);
	if (dwRet == 0) {
		dwRet = ::GetLastError();
		ErrorLog("Fail to create pipe [%s]", dwRet);
	}
}

///////////////////////////////////////////////////////////////////////////////////////////////////////
DWORD CMServer::StartServer(ST_INIT_ARG &refstInitArg)
{
	
	BOOL bExist;
	bExist = IsBotGenFile(refstInitArg.strBotGenFilePath);
	if (!bExist) {
		return E_RET_FAIL;
	}
	

	InitAnonymousPipe();
	InitMMThread(refstInitArg.dwMMPort, refstInitArg.dwManagerNumber);
	InitBotsThread(refstInitArg.dwMPort, refstInitArg.strBotGenFilePath);
	InitHealthThread(refstInitArg.dwHealthPort, refstInitArg.dwManagerNumber);

	BOOL bStartServer = TRUE;
	while (bStartServer)
	{
		try
		{	
			/*
				Main Thread is waiting for all thread (Master Manager Thread, bots Thread)
			*/
			DWORD dwRet;
			dwRet = ::WaitForMultipleObjects(COUNT_THREAD, m_stThreadManager.hThread, true, INFINITE);
			HandleError(dwRet);
		}
		catch (std::exception &e)
		{
			ErrorLog("%s", e.what());
		}

		bStartServer = FALSE;
	}

	return E_RET_SUCCESS;
}

/*
	Worker threads are used for receving data from MM and bots
*/
///////////////////////////////////////////////////////////////////////////////////////////////////////
unsigned int WINAPI WorkerMMThread(void *pData)
{
	CMMThread *pMMThread;
	pMMThread = new (std::nothrow) CMMThread();

	ST_THREADS_PARAM *pstThreadsParam = (ST_THREADS_PARAM *)pData;
	try
	{
		pMMThread->StartThread(pstThreadsParam);
	}
	catch (std::exception &e)
	{
		ErrorLog("%s", e.what());
	}

	delete pMMThread;
	return E_RET_SUCCESS;
}

///////////////////////////////////////////////////////////////////////////////////////////////////////
unsigned int WINAPI WorkerBotsThread(void *pData)
{
	CBotsThread *pBotsThread;
	pBotsThread = new (std::nothrow) CBotsThread();

	ST_THREADS_PARAM *pstThreadsParam = (ST_THREADS_PARAM *)pData;
	try 
	{
		pBotsThread->StartThread(pstThreadsParam);
	}
	catch (std::exception &e) 
	{
		ErrorLog("%s", e.what());
	}

	delete pBotsThread;
	return E_RET_SUCCESS;
}

///////////////////////////////////////////////////////////////////////////////////////////////////////
unsigned int WINAPI WorkerHealthThread(void *pData)
{
	CHealthThread *pHealthThread;
	pHealthThread = new (std::nothrow) CHealthThread();

	ST_THREADS_PARAM *pstThreadsParam = (ST_THREADS_PARAM *)pData;
	try
	{
		pHealthThread->StartThread(pstThreadsParam);
	}
	catch (std::exception &e)
	{
		ErrorLog("%s", e.what());
	}

	delete pHealthThread;
	return E_RET_SUCCESS;
}