#include "MySQL.h"


CMySQL::CMySQL() : pMySQLConn(NULL), pMySQLResult(NULL)
{
}

CMySQL::~CMySQL()
{
}

DWORD CMySQL::_ConnectToDB(ST_DB_LOGIN_TOKEN &refstDBLoginToken)
{
	pMySQLConn = mysql_init(NULL);
	if (!pMySQLConn)
		return 0;

	std::string strConnect;

	DWORD dwPort = static_cast<DWORD>(::atoi(refstDBLoginToken.strPort.c_str()));
	if (mysql_real_connect(
		pMySQLConn,
		refstDBLoginToken.strDatabaseIP.c_str(),
		refstDBLoginToken.strUserName.c_str(),
		refstDBLoginToken.strPassword.c_str(),
		refstDBLoginToken.strDatabaseName.c_str(),
		dwPort,
		NULL,
		0) 
		== NULL) {
		fprintf(stderr, "Connection to database failed: %s", mysql_error(pMySQLConn));
		mysql_close(pMySQLConn);
		return E_RET_FAIL;
	}

	return E_RET_SUCCESS;
}

DWORD CMySQL::_InsertToDB(ST_DB_SQL &refstDBSQL)
{
	if (mysql_query(pMySQLConn, refstDBSQL.strSQL.c_str())) {
		fprintf(stderr, "Fail to insert to database: %s", mysql_error(pMySQLConn));
		mysql_close(pMySQLConn);
		return E_RET_FAIL;
	}
	return E_RET_SUCCESS;
}

DWORD CMySQL::_UpdateToDB(ST_DB_SQL &refstDBSQL)
{
	if (mysql_query(pMySQLConn, refstDBSQL.strSQL.c_str())) {
		fprintf(stderr, "Fail to update to database: %s", mysql_error(pMySQLConn));
		mysql_close(pMySQLConn);
		return E_RET_FAIL;
	}
	return E_RET_SUCCESS;
}

DWORD CMySQL::_QueryFromDB(ST_DB_SQL &refstDBSQL, ST_DB_RESULT &refstDBResult)
{
	if (mysql_query(pMySQLConn, refstDBSQL.strSQL.c_str())) {
		fprintf(stderr, "Fail to update to database: %s", mysql_error(pMySQLConn));
		mysql_close(pMySQLConn);
		return E_RET_FAIL;
	}

	pMySQLResult = mysql_store_result(pMySQLConn);
	if (!pMySQLConn) {
		fprintf(stderr, "Fail to query to database: %s", mysql_error(pMySQLConn));
		mysql_close(pMySQLConn);
		return E_RET_FAIL;
	}


	DWORD dwCol = mysql_num_fields(pMySQLResult);	
	MYSQL_ROW resultRow;
	MYSQL_FIELD *pResultCol = NULL;

	while ((resultRow = mysql_fetch_row(pMySQLResult))) {
		ST_DB_RESULT_LINE stDBResultLine;
		for (DWORD i = 0; i < dwCol; i++) {
			std::string strValue;
			strValue = resultRow[i];
			stDBResultLine.vecstrResult.push_back(strValue);
		}
		refstDBResult.vecstDBResultLines.push_back(stDBResultLine);
	}

	return E_RET_SUCCESS;
}

DWORD CMySQL::_QuitDB()
{
	mysql_close(pMySQLConn);
	return E_RET_SUCCESS;
}
