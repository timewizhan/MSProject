#include "BGWorkerThread.h"
#include "BGServerError.h"

#include "..\Common\Log.h"
#include "json\reader.h"
#include "json\json.h"


#define DEFAULT_RECV_DATA 32

///////////////////////////////////////////////////////////////////////////////////////////////////////
CBGWorkerThread::CBGWorkerThread(SOCKET ClientSocket)
{
	::memset(&m_stWorkerThread, 0x00, sizeof(ST_WORKER_THREAD));
	m_stWorkerThread.hClientSocket = ClientSocket;
}

///////////////////////////////////////////////////////////////////////////////////////////////////////
CBGWorkerThread::~CBGWorkerThread()
{
	closesocket(m_stWorkerThread.hClientSocket);
}

///////////////////////////////////////////////////////////////////////////////////////////////////////
DWORD CBGWorkerThread::SendDataToClient(std::string &refstrSendData)
{
	int nSizeOfData = refstrSendData.size();
	int nSent = 0, nRet;
	BOOL bContinue = TRUE;
	while (bContinue) {
		nRet = send(m_stWorkerThread.hClientSocket, refstrSendData.c_str() + nSent, nSizeOfData - nSent, 0);
		if (nSent == SOCKET_ERROR) {
			int nRet = WSAGetLastError();
			ShowErrorSend(nRet);
			bContinue = FALSE;
			continue;
		}
		else if (nRet == nSizeOfData) {
			DebugLog("Success to send data to client [%s]", refstrSendData.c_str());
			bContinue = FALSE;
			continue;
		}

		/*
		if all data is not sent to client yet, continue to send to rest of data
		*/
		nSent = nRet;
	}
	return E_RET_SUCCESS;
}

///////////////////////////////////////////////////////////////////////////////////////////////////////
void CBGWorkerThread::ReceiveDataFromClient(ST_RECV_DATA &refstRecvData, char *pReceiveBuf)
{
	refstRecvData.strRecvData = pReceiveBuf;
}

///////////////////////////////////////////////////////////////////////////////////////////////////////
void CBGWorkerThread::ParseReceivedData(ST_RECV_DATA &refstRecvData, ST_REQ_CLIENT &refstReqClient)
{
	Json::Value JsonRoot;
	Json::Reader reader;
	bool bParsingRet = reader.parse(refstRecvData.strRecvData, JsonRoot);
	if (!bParsingRet) {
		ErrorLog("Fail to parse a received data [%s]", reader.getFormatedErrorMessages());
		std::cout << reader.getFormatedErrorMessages() << std::endl;
		return ;
	}
}

///////////////////////////////////////////////////////////////////////////////////////////////////////
DWORD CBGWorkerThread::StartWorkerThread(char *pReceiveBuf, DWORD dwByteTransferred)
{
	DWORD dwRet;
	try
	{
		ST_RECV_DATA stRecvData;
		ReceiveDataFromClient(stRecvData, pReceiveBuf);

		ST_REQ_CLIENT stReqClient;
		ParseReceivedData(stRecvData, stReqClient);

		/*
			TODO :
			Implement a communication with Broker (MySQL) and data to send to bot
		*/
		std::string strSendData = "hello, I'am Server";
		dwRet = SendDataToClient(strSendData);
	}
	catch (std::exception &e)
	{
		ErrorLog(e.what());
		return E_RET_FAIL;
	}
	DebugLog("Success to communicate with client");
	return E_RET_SUCCESS;
} 