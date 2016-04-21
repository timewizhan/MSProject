#include "ControlThread.h"
#include "..\Queue\SharedCommand.h"

CMMControlThread::CMMControlThread()
{
}

CMMControlThread::~CMMControlThread()
{
}

VOID CMMControlThread::Usage()
{
	printf("Enter Command ([start] [Bot Count]) ([stop]) \n");
}

DWORD CMMControlThread::CheckInputData(std::string &refstrInputData, DWORD dwType)
{
	DWORD dwRet = E_RET_FAIL;

	// Type 1 is the start command
	if (dwType == 1) {
		DWORD dwSizeOfString = 5;
		if (refstrInputData.size() <= dwSizeOfString) {
			return dwRet;
		}

		DWORD dwPos = refstrInputData.find_first_of(" ");
		if (dwPos == std::string::npos) {
			return dwRet;
		}
		dwRet = E_RET_SUCCESS;
	}
	// Type 2 is the stop command
	else {
		DWORD dwSizeOfString = 4;
		if ((refstrInputData.size() < dwSizeOfString) || (refstrInputData.size() > dwSizeOfString)) {
			return dwRet;
		}
		dwRet = E_RET_SUCCESS;
	}

	return dwRet;
}

VOID CMMControlThread::InputCommand()
{
	DWORD dwRet;

	char szInputBuf[64] = { 0 };

	Usage();
	while (::gets_s(szInputBuf)) {
		std::string strInput = szInputBuf;
		if (strInput.size() < 5) {
			if (strInput.find("stop") || strInput.find("STOP")) {
				dwRet = CheckInputData(strInput, 2);
				if (dwRet != E_RET_SUCCESS) {
					::memset(szInputBuf, 0x00, sizeof(szInputBuf));
					continue;
				}
			}
			SetCommand(strInput);
		}
		else {
			if (strInput.find("start") || strInput.find("START")) {
				dwRet = CheckInputData(strInput, 1);
				if (dwRet != E_RET_SUCCESS) {
					::memset(szInputBuf, 0x00, sizeof(szInputBuf));
					continue;
				}
				SetCommand(strInput);
			}
		}
		Usage();
		::memset(szInputBuf, 0x00, sizeof(szInputBuf));
	}
}

DWORD CMMControlThread::StartThread()
{
	try
	{
		InputCommand();

		/*DebugLog("Command Input");
		std::string strInput = "start 1";
		SetCommand(strInput);*/
	}
	catch (std::exception &e) {
		ErrorLog("%s", e.what());
	}

	return E_RET_SUCCESS;
}
