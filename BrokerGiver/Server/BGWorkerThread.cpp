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

	m_stDBLoginToken.strDatabaseName	= "broker_table";
	m_stDBLoginToken.strDatabaseIP		= "165.132.122.243";
	m_stDBLoginToken.strPort			= "3306";
	m_stDBLoginToken.strUserName		= "root";
	m_stDBLoginToken.strPassword		= "cclab";
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
void CBGWorkerThread::ParseReqData(ST_RECV_DATA &refstRecvData, ST_CLIENT_REQ &refstReqClient)
{
	Json::Value JsonRoot;
	Json::Reader reader;
	bool bParsingRet = reader.parse(refstRecvData.strRecvData, JsonRoot);
	if (!bParsingRet) {
		ErrorLog("Fail to parse a received data [%s]", reader.getFormatedErrorMessages());
		std::cout << reader.getFormatedErrorMessages() << std::endl;
		return ;
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
void CBGWorkerThread::ExtractResData(ST_DB_RESULT &refstDBResult, ST_CLIENT_RES &refstResClient)
{
	Json::Value JsonRoot;

	if (refstDBResult.vecstDBResultLines.size() < 1) {
		return;
	}

	ST_DB_RESULT_LINE stDBResultLine;
	stDBResultLine = refstDBResult.vecstDBResultLines[0];

	if (stDBResultLine.vecstrResult.size() < 1) {
		return;
	}

	std::string strIPAddress = stDBResultLine.vecstrResult[0];
	refstResClient.strIPAddress = strIPAddress;
}

void CBGWorkerThread::RequestDataBase(ST_CLIENT_REQ &refstReqClient, ST_DB_RESULT &refstDBResult)
{
	HANDLE hDataBase = NULL;
	hDataBase = CreateDBInstance(E_DB_MYSQL);
	if (hDataBase == NULL) {
		ErrorLog("Fail to create DB instance");
		return ;
	}

	DWORD dwRet;
	dwRet = ConnectToDB(hDataBase, m_stDBLoginToken);
	if (dwRet != E_RET_SUCCESS) {
		ErrorLog("Fail to connet to DB");
		return ;
	}

	ST_DB_SQL stDBSql;
	stDBSql.strSQL = "SELECT ip from prev_matching_table WHERE user=\"" + refstReqClient.strDst + "\"";

	ST_DB_RESULT stDBResult;
	dwRet = QueryFromDB(hDataBase, stDBSql, stDBResult);
	if (dwRet != E_RET_SUCCESS) {
		ErrorLog("Fail to query data from DataBase");
		return;
	}

	dwRet = QuitDB(hDataBase);
	if (dwRet != E_RET_SUCCESS) {
		ErrorLog("Fail to quit data from DataBase");
		return;
	}

	refstDBResult = stDBResult;
}

///////////////////////////////////////////////////////////////////////////////////////////////////////
DWORD CBGWorkerThread::StartWorkerThread(char *pReceiveBuf, DWORD dwByteTransferred)
{
	DWORD dwRet;
	try
	{
		ST_RECV_DATA stRecvData;
		ReceiveDataFromClient(stRecvData, pReceiveBuf);

		ST_CLIENT_REQ stReqClient;
		ParseReqData(stRecvData, stReqClient);

		ST_DB_RESULT stDBResult;
		RequestDataBase(stReqClient, stDBResult);

		ST_CLIENT_RES stResClient;
		ExtractResData(stDBResult, stResClient);

		std::string strSendData;
		MakeJsonResData(stResClient, strSendData);
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