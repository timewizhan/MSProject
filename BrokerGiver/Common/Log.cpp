#include "Log.h"

char *g_pszPath = NULL;
FILE	*pLogFile;
extern E_LOG_TYPE g_eLogtype = E_LOG_CONSOLE;

std::string strLogFileFullPath;

CRITICAL_SECTION CriticalSection;

////////////////////////////////////////////////////////////////////////////////////////
void _TimeToString(char *pBuf)
{
	time_t timer;
	struct tm stTM;

	timer = time(NULL);  
	localtime_s(&stTM, &timer);

	sprintf_s(pBuf, 512,"%04d-%02d-%02d %02d:%02d:%02d ",
		stTM.tm_year + 1900, stTM.tm_mon + 1, stTM.tm_mday,
		stTM.tm_hour, stTM.tm_min, stTM.tm_sec
		);
}
////////////////////////////////////////////////////////////////////////////////////////
DWORD InitLog(E_LOG_TYPE eLogType)
{
	g_eLogtype = eLogType;
	pLogFile = NULL;

	if (eLogType == E_LOG_CONSOLE)
		g_eLogtype = E_LOG_CONSOLE;
	else if (eLogType == E_LOG_FILE) {
		TCHAR lpwBuffer[512] = { 0 };
		GetCurrentDirectory(512, lpwBuffer);
		std::wstring strBuffer = lpwBuffer;
		std::wstring strFileName = L"AYServer.log";// LOG_FILE_NAME;
		strBuffer += L"\\" + strFileName;

		strLogFileFullPath.assign(strBuffer.begin(), strBuffer.end());
		int iRet;
		iRet = ::fopen_s(&pLogFile, strLogFileFullPath.c_str(), "a+");
		if (iRet != 0) {
			return E_RET_FAIL;
		}

		InitializeCriticalSection(&CriticalSection);
		::fclose(pLogFile);
	}

	return E_RET_SUCCESS;
}

////////////////////////////////////////////////////////////////////////////////////////
DWORD ErrorLog(const char *cformat, ...)
{
	//EnterCriticalSection(&CriticalSection);
	va_list arg;
	int iCount;
	char szBuf[MAX_BUF] = { 0 };
	char szTime[128] = { 0 };

	va_start(arg, cformat);
	iCount = vsnprintf_s(szBuf, sizeof(szBuf), cformat, arg);
	va_end(arg);

	switch (g_eLogtype)
	{
	case E_LOG_CONSOLE:
		printf("[ERROR] %s\n", szBuf);
		break;
	case E_LOG_FILE:
		int iRet;
		iRet = ::fopen_s(&pLogFile, strLogFileFullPath.c_str(), "a+");
		if (iRet != 0) {
			return E_RET_FAIL;
		}
		time_t timer;
		struct tm stTM;

		timer = time(NULL);
		localtime_s(&stTM, &timer);

		fprintf_s(pLogFile, "%04d-%02d-%02d %02d:%02d:%02d %s\n",
			stTM.tm_year + 1900, stTM.tm_mon + 1, stTM.tm_mday,
			stTM.tm_hour, stTM.tm_min, stTM.tm_sec, szBuf);
		//fputs(szBuf, pLogFile);
		//fputs("\n", pLogFile);
		::fclose(pLogFile);;
		break;
	default:
		break;
	}
	//LeaveCriticalSection(&CriticalSection);
	return E_RET_SUCCESS;
}

////////////////////////////////////////////////////////////////////////////////////////
DWORD DebugLog(const char *cformat, ...)
{
	//EnterCriticalSection(&CriticalSection);
	va_list arg;
	int iCount;
	char szBuf[MAX_BUF] = { 0 };

	va_start(arg, cformat);
	iCount = vsnprintf_s(szBuf, sizeof(szBuf), cformat, arg);
	va_end(arg);

	switch (g_eLogtype)
	{
	case E_LOG_CONSOLE:
		printf("[DEBUG] %s\n", szBuf);
		break;
	case E_LOG_FILE:
		int iRet;
		iRet = ::fopen_s(&pLogFile, strLogFileFullPath.c_str(), "a+");
		if (iRet != 0) {
			return E_RET_FAIL;
		}

		time_t timer;
		struct tm stTM;

		timer = time(NULL);
		localtime_s(&stTM, &timer);
		
		fprintf_s(pLogFile, "%04d-%02d-%02d %02d:%02d:%02d %s\n",
			stTM.tm_year + 1900, stTM.tm_mon + 1, stTM.tm_mday,
			stTM.tm_hour, stTM.tm_min, stTM.tm_sec, szBuf);
		//fputs(szBuf, pLogFile);
		//fputs("\n", pLogFile);
		::fclose(pLogFile);
		break;
	default:
		break;
	}
	//LeaveCriticalSection(&CriticalSection);
	return E_RET_SUCCESS;
}

////////////////////////////////////////////////////////////////////////////////////////
DWORD WarningLog(const char *cformat, ...)
{
	EnterCriticalSection(&CriticalSection);
	va_list arg;
	int iCount;
	char szBuf[MAX_BUF] = { 0 };
	char szTime[128] = { 0 };

	va_start(arg, cformat);
	iCount = vsnprintf_s(szBuf, sizeof(szBuf), cformat, arg);
	va_end(arg);

	switch (g_eLogtype)
	{
	case E_LOG_CONSOLE:
		printf("[WARNING] %s\n", szBuf);
		break;
	case E_LOG_FILE:
		int iRet;
		iRet = ::fopen_s(&pLogFile, strLogFileFullPath.c_str(), "a+");
		if (iRet != 0) {
			return E_RET_FAIL;
		}
		_TimeToString(szTime);
		fputs(szTime, pLogFile);

		fputs(szBuf, pLogFile);
		::fclose(pLogFile);

		break;
	default:
		break;
	}
	LeaveCriticalSection(&CriticalSection);
	return E_RET_SUCCESS;
}