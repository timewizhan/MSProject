#include "DBPSWorkerThread.h"
#include "DBPSServerError.h"

#include "..\Common\Log.h"
#include "json\reader.h"
#include "json\json.h"

#include "..\Queue\DBCQueue.h"

#define DEFAULT_RECV_DATA 32

///////////////////////////////////////////////////////////////////////////////////////////////////////
CDBPSWorkerThread::CDBPSWorkerThread(SOCKET ClientSocket)
{
	::memset(&m_stWorkerThread, 0x00, sizeof(ST_WORKER_THREAD));
	m_stWorkerThread.hClientSocket = ClientSocket;
}

///////////////////////////////////////////////////////////////////////////////////////////////////////
CDBPSWorkerThread::~CDBPSWorkerThread()
{
	closesocket(m_stWorkerThread.hClientSocket);
}

///////////////////////////////////////////////////////////////////////////////////////////////////////
DWORD CDBPSWorkerThread::SendDataToClient(std::string &refstrSendData)
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
void CDBPSWorkerThread::ReceiveDataFromClient(ST_RECV_DATA &refstRecvData, char *pReceiveBuf)
{
	refstRecvData.strRecvData = pReceiveBuf;
}

///////////////////////////////////////////////////////////////////////////////////////////////////////
void CDBPSWorkerThread::MakeJsonResData(ST_DB_RESULT &refstDBResult, std::string &refstrSendData)
{
	Json::Value JsonRoot;

	std::string strListResult;
	for (unsigned int i = 0; i < refstDBResult.vecstDBResultLines.size(); i++) {
		ST_DB_RESULT_LINE stDBResultList;
		std::string strColume = stDBResultList.vecstrResult[0];
		strListResult += strColume + " ";
	}
	JsonRoot["RESPONSE"] = strListResult;

	Json::StyledWriter JsonWriter;
	std::string strSendData;
	strSendData = JsonWriter.write(JsonRoot);

	refstrSendData = strSendData;
}

void CDBPSWorkerThread::RequestDataBase(ST_RECV_DATA &refstRecvData, ST_DB_RESULT &refstDBResult)
{
	ST_DBConnection stDBConnection;
	CDBCQueue::getQueueInstance().popFromQueue(stDBConnection);

	ST_DB_SQL stDBSql;
	stDBSql.strSQL = refstRecvData.strRecvData;

	DWORD dwRet;
	ST_DB_RESULT stDBResult;
	dwRet = QueryFromDB(stDBConnection.hDataBase, stDBSql, stDBResult);
	if (dwRet != E_RET_SUCCESS) {
		ErrorLog("Fail to query data from DataBase");
		return;
	}
	refstDBResult = stDBResult;
}

///////////////////////////////////////////////////////////////////////////////////////////////////////
DWORD CDBPSWorkerThread::StartWorkerThread(char *pReceiveBuf, DWORD dwByteTransferred)
{
	DWORD dwRet;
	try
	{
		ST_RECV_DATA stRecvData;
		ReceiveDataFromClient(stRecvData, pReceiveBuf);

		ST_DB_RESULT stDBResult;
		RequestDataBase(stRecvData, stDBResult);

		std::string strSendData;
		MakeJsonResData(stDBResult, strSendData);
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