#include "BGWorkerThread.h"
#include "BGServerError.h"
#include "BGException.h"

#include "..\Common\Log.h"
#include "..\Server\DBQueue.h"

#include "json\reader.h"
#include "json\json.h"

#define DEFAULT_RECV_DATA 32

extern CDBQueue* g_pCDBQueue;

///////////////////////////////////////////////////////////////////////////////////////////////////////
CBGWorkerThread::CBGWorkerThread()
{
}

///////////////////////////////////////////////////////////////////////////////////////////////////////
CBGWorkerThread::~CBGWorkerThread()
{
}

///////////////////////////////////////////////////////////////////////////////////////////////////////
VOID CBGWorkerThread::SetClientSocket(SOCKET ClientSocket)
{
	::memset(&m_stWorkerThread, 0x00, sizeof(ST_WORKER_THREAD));
	m_stWorkerThread.hClientSocket = ClientSocket;
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
			DebugLog("Success to send data to client");
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
void CBGWorkerThread::ParseReqData(ST_RECV_DATA &refstRecvData, ST_CLIENT_REQ &refstReqClient) throw(JSONException)
{
	Json::Value JsonRoot;
	Json::Reader reader;
	bool bParsingRet = reader.parse(refstRecvData.strRecvData, JsonRoot);
	if (!bParsingRet) {
		throw JSONException(reader.getFormatedErrorMessages());
	}

	refstReqClient.iType	= JsonRoot.get("TYPE", 0).asInt();
	refstReqClient.strSrc	= JsonRoot.get("SRC", 0).asString();
	refstReqClient.strDst	= JsonRoot.get("DST", 0).asString();
}

///////////////////////////////////////////////////////////////////////////////////////////////////////
void CBGWorkerThread::MakeJsonResData(ST_CLIENT_RES &refstResClient, std::string &refstrSendData)
{
	Json::Value JsonRoot;
	JsonRoot["RESPONSE"] = refstResClient.strIPAddress;

	Json::StyledWriter JsonWriter;
	std::string strSendData;
	strSendData = JsonWriter.write(JsonRoot);

	refstrSendData = strSendData;
}

///////////////////////////////////////////////////////////////////////////////////////////////////////
void CBGWorkerThread::ExtractResData(ST_DB_RESULT &refstDBResult, ST_CLIENT_RES &refstResClient) throw(std::exception)
{
	Json::Value JsonRoot;

	if (refstDBResult.vecstDBResultLines.size() < 1) {
		throw std::exception("There is no DB result");
	}

	ST_DB_RESULT_LINE stDBResultLine;
	stDBResultLine = refstDBResult.vecstDBResultLines[0];

	if (stDBResultLine.vecstrResult.size() < 1) {
		throw std::exception("There is no DB record result");
	}

	std::string strIPAddress = stDBResultLine.vecstrResult[0];
	refstResClient.strIPAddress = strIPAddress;
}

void CBGWorkerThread::RequestDataBase(ST_CLIENT_REQ &refstReqClient, ST_DB_RESULT &refstDBResult)
{
	ST_DBConnection stDBConnection;

	bool bContinue = true;
	while (bContinue) {
		g_pCDBQueue->popFromQueue(stDBConnection);
		if (stDBConnection.hDataBase == NULL) {
			::Sleep(1000);
			continue;
		}
		bContinue = false;
	}

	ST_DB_SQL stDBSql;
	stDBSql.strSQL = "SELECT ip from prev_matching_table WHERE user=\"" + refstReqClient.strDst + "\"";

	DWORD dwRet;
	ST_DB_RESULT stDBResult;
	dwRet = QueryFromDB(stDBConnection.hDataBase, stDBSql, stDBResult);
	if (dwRet != E_RET_SUCCESS) {
		throw DBException(stDBSql.strSQL);
	}

	g_pCDBQueue->pushToQueue(stDBConnection);
	refstDBResult = stDBResult;
}

///////////////////////////////////////////////////////////////////////////////////////////////////////
DWORD CBGWorkerThread::StartWorkerThread(char *pReceiveBuf, CBGLog *pBGLog)
{
	try
	{
		ST_RECV_DATA stRecvData;
		ReceiveDataFromClient(stRecvData, pReceiveBuf);
		pBGLog->WriteDebugLog(stRecvData.strRecvData);

		ST_CLIENT_REQ stReqClient;
		ParseReqData(stRecvData, stReqClient);

		ST_DB_RESULT stDBResult;
		RequestDataBase(stReqClient, stDBResult);

		ST_CLIENT_RES stResClient;
		ExtractResData(stDBResult, stResClient);

		std::string strSendData;
		MakeJsonResData(stResClient, strSendData);
		pBGLog->WriteDebugLog(strSendData);

		SendDataToClient(strSendData);
	}
	catch (std::exception &e)
	{
		std::string strErrMsg = e.what();
		pBGLog->WriteErrorLog(strErrMsg);

		::closesocket(m_stWorkerThread.hClientSocket);
		return E_RET_FAIL;
	}

	pBGLog->WriteDebugLog("Success to communicate with client");
	::closesocket(m_stWorkerThread.hClientSocket);
	return E_RET_SUCCESS;
} 