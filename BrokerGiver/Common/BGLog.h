#ifndef _BGLOG_
#define _BGLOG_

#include "Common.h"

struct ST_LOG_INFO
{
	std::string strFilePath;
	FILE		*pFile;
};

class CBGLog
{
	ST_LOG_INFO	m_stLogInfo;

	VOID GetCurrentPath(std::string &refstrCurrentDir);
	VOID WriteLog(std::string &refstrLog, DWORD dwType);
public:
	CBGLog(DWORD dwFileNumber);
	~CBGLog();

	VOID WriteDebugLog(std::string &refstrLog);
	VOID WriteDebugLog(const char *pstrLog);

	VOID WriteErrorLog(std::string &refstrLog);
	VOID WriteErrorLog(const char *pstrLog);
};

#endif