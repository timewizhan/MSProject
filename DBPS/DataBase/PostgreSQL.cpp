#include "PostgreSQL.h"


CPostgreSQL::CPostgreSQL() : pPGConn(NULL), pPGResult(NULL)
{
}

CPostgreSQL::~CPostgreSQL()
{
}

DWORD CPostgreSQL::_ConnectToDB(ST_DB_LOGIN_TOKEN &refstDBLoginToken)
{
	if (pPGConn)
		return 0;

	std::string strConnect;
	strConnect = "user=" + refstDBLoginToken.strUserName + " ";
	strConnect += "password=" + refstDBLoginToken.strPassword + " ";
	strConnect += "dbname=" + refstDBLoginToken.strDatabaseName + " ";
	strConnect += "hostaddr=" + refstDBLoginToken.strDatabaseIP + " ";
	strConnect += "port=" + refstDBLoginToken.strPort;

	pPGConn = PQconnectdb(strConnect.c_str());
	if (PQstatus(pPGConn) != CONNECTION_OK) {
		fprintf(stderr, "Connection to database failed: %s", PQerrorMessage(pPGConn));
		PQfinish(pPGConn);
		return E_RET_FAIL;
	}
	return E_RET_SUCCESS;
}

DWORD CPostgreSQL::_InsertToDB(ST_DB_SQL &refstDBSQL)
{
	pPGResult = PQexec(pPGConn, refstDBSQL.strSQL.c_str());
	if (PQresultStatus(pPGResult) != PGRES_COMMAND_OK) {
		fprintf(stderr, "Fail to insert to database: %s", PQerrorMessage(pPGConn));
		PQclear(pPGResult);
		PQfinish(pPGConn);
		return E_RET_FAIL;
	}
	return E_RET_SUCCESS;
}

DWORD CPostgreSQL::_UpdateToDB(ST_DB_SQL &refstDBSQL)
{
	pPGResult = PQexec(pPGConn, refstDBSQL.strSQL.c_str());
	DWORD dwPQresultStatus = PQresultStatus(pPGResult);
	if (dwPQresultStatus != PGRES_COMMAND_OK && dwPQresultStatus != PGRES_TUPLES_OK) {
		fprintf(stderr, "Fail to insert to database: %s", PQerrorMessage(pPGConn));
		PQclear(pPGResult);
		PQfinish(pPGConn);
		return E_RET_FAIL;
	}
	return E_RET_SUCCESS;
}

DWORD CPostgreSQL::_QueryFromDB(ST_DB_SQL &refstDBSQL, ST_DB_RESULT &refstDBResult)
{
	pPGResult = PQexec(pPGConn, refstDBSQL.strSQL.c_str());
	DWORD dwPQresultStatus = PQresultStatus(pPGResult);
	if (dwPQresultStatus != PGRES_COMMAND_OK && dwPQresultStatus != PGRES_TUPLES_OK) {
		fprintf(stderr, "Fail to query to database: %s", PQerrorMessage(pPGConn));
		PQclear(pPGResult);
		PQfinish(pPGConn);
		return E_RET_FAIL;
	}

	DWORD dwRow = PQntuples(pPGResult);
	DWORD dwCol = PQnfields(pPGResult);

	DWORD i = 0, j = 0;
	for (i = 0; i < dwRow; i++) {
		ST_DB_RESULT_LINE stDBResultLine;
		for (j = 0; j < dwCol; j++) {
			std::string strValue;
			strValue = PQgetvalue(pPGResult, i, j);
			stDBResultLine.vecstrResult.push_back(strValue);
		}
		refstDBResult.vecstDBResultLines.push_back(stDBResultLine);
	}
	return E_RET_SUCCESS;
}

DWORD CPostgreSQL::_QuitDB()
{
	PQfinish(pPGConn);
	return E_RET_SUCCESS;
}
