#include "..\Common\Log.h"

#include "MMServer.h"
#include "MMServerError.h"

#include "ControlThread.h"
#include "ManagerThread.h"
#include "HealthThread.h"

///////////////////////////////////////////////////////////////////////////////////////////////////////
CMMServer::CMMServer()
{
	/*
		Server initializer has a role only for initializing variable
	*/
	::memset(&m_stThreadManager, 0x00, sizeof(m_stThreadManager));
}

///////////////////////////////////////////////////////////////////////////////////////////////////////
CMMServer::~CMMServer()
{
}

///////////////////////////////////////////////////////////////////////////////////////////////////////
VOID CMMServer::InitControlThread()
{
	HANDLE hThread = NULL;
	hThread = (HANDLE)_beginthreadex(NULL, 0, WorkerControlThread, NULL, 0, NULL);
	::Sleep(1000);
	if (!hThread) {
		DebugLog("Master Manager Thread is not created");
		return;
	}

	m_stThreadManager.hThread[0] = hThread;
	DebugLog("Control Thread is created");
}

///////////////////////////////////////////////////////////////////////////////////////////////////////
VOID CMMServer::InitManagerThread(DWORD dwPort, DWORD dwCountOfManager)
{
	ST_THREADS_PARAM stThreadsParam;
	stThreadsParam.dwPort = dwPort;
	stThreadsParam.dwCountOfManager = dwCountOfManager;

	HANDLE hThread = NULL;
	hThread = (HANDLE)_beginthreadex(NULL, 0, WorkerManagerThread, (void *)&stThreadsParam, 0, NULL);
	::Sleep(1000);
	if (!hThread) {
		DebugLog("Bots Thread is not created");
		return;
	}

	m_stThreadManager.hThread[1] = hThread;
	DebugLog("Bots Thread is created");
}

///////////////////////////////////////////////////////////////////////////////////////////////////////
VOID CMMServer::InitHealthThread(DWORD dwPort)
{
	ST_THREADS_PARAM stThreadsParam;
	stThreadsParam.dwPort = dwPort;

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
VOID CMMServer::HandleError(DWORD dwRet) throw(std::exception)
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
DWORD CMMServer::StartServer(ST_INIT_ARG &refstInitArg)
{
	//InitControlThread();
	InitManagerThread(refstInitArg.dwMMPort, refstInitArg.dwCountOfManager);
	//InitHealthThread(refstInitArg.dwHealthPort);

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

	WSACleanup();
	return E_RET_SUCCESS;
}

/*
	Worker threads are used for receving data from MM and bots
*/
///////////////////////////////////////////////////////////////////////////////////////////////////////
unsigned int WINAPI WorkerControlThread(void *pData)
{
	CMMControlThread *pMMControlThread;
	pMMControlThread = new (std::nothrow) CMMControlThread();

	ST_THREADS_PARAM *pstThreadsParam = (ST_THREADS_PARAM *)pData;
	try
	{
		pMMControlThread->StartThread();
	}
	catch (std::exception &e)
	{
		ErrorLog("%s", e.what());
	}

	delete pMMControlThread;
	return E_RET_SUCCESS;
}

///////////////////////////////////////////////////////////////////////////////////////////////////////
unsigned int WINAPI WorkerManagerThread(void *pData)
{
	CManagerThread *pManagerThread;
	pManagerThread = new (std::nothrow) CManagerThread();

	ST_THREADS_PARAM *pstThreadsParam = (ST_THREADS_PARAM *)pData;
	try 
	{
		pManagerThread->StartThread(pstThreadsParam);
	}
	catch (std::exception &e) 
	{
		ErrorLog("%s", e.what());
	}

	delete pManagerThread;
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