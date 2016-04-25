#include "BGLog.h"

#include <time.h>

CBGLog::CBGLog(DWORD dwFileNumber)
{
	const std::string strLogName = "BGLog" + std::to_string(dwFileNumber) + ".txt";
	std::string strCurrentDir;
	GetCurrentPath(strCurrentDir);

	m_stLogInfo.strFilePath = strCurrentDir + "\\" + strLogName;
	m_stLogInfo.pFile = _fsopen(m_stLogInfo.strFilePath.c_str(), "a+", _SH_DENYNO);
}

CBGLog::~CBGLog()
{
	::fclose(m_stLogInfo.pFile);
}

VOID CBGLog::GetCurrentPath(std::string &refstrCurrentDir)
{
	char szBuf[256] = { 0 };

	::GetCurrentDirectoryA(sizeof(szBuf), szBuf);
	refstrCurrentDir = szBuf;
}

VOID CBGLog::WriteLog(std::string &refstrLog, DWORD dwType)
{
	struct tm stTM;
	time_t timer = time(NULL);
	localtime_s(&stTM, &timer);

	char szBuf[20] = { 0 };
	sprintf_s(szBuf, "%d/%d/%d %d:%d:%d", stTM.tm_year + 1900, stTM.tm_mon + 1, stTM.tm_mday, stTM.tm_hour, stTM.tm_min, stTM.tm_sec);
	std::string strTime = szBuf;

	std::string strLog;
	if (dwType == 1) {
		strLog = "[Debug : " + strTime + "]" + refstrLog + "\n";
	}
	else {
		strLog = "[Error : " + strTime + "]" + refstrLog + "\n";
	}

	::fwrite(strLog.c_str(), strLog.size(), 1, m_stLogInfo.pFile);
	::fflush(m_stLogInfo.pFile);
}

VOID CBGLog::WriteDebugLog(std::string &refstrLog)
{
	WriteLog(refstrLog, 1);
}

VOID CBGLog::WriteDebugLog(const char *pstrLog)
{
	std::string strLogMsg = pstrLog;
	WriteLog(strLogMsg, 1);
}

VOID CBGLog::WriteErrorLog(std::string &refstrLog)
{
	WriteLog(refstrLog, 2);
}

VOID CBGLog::WriteErrorLog(const char *pstrLog)
{
	std::string strLogMsg = pstrLog;
	WriteLog(strLogMsg, 2);
}


