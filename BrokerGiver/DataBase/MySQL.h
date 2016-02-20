#ifndef _MYSQL_
#define _MYSQL_

#include "DataBase.h"

#define MY_SQL

#ifdef MY_SQL
#include <mysql.h>
#include <my_global.h>
#endif

class CMySQL : public IDataBase
{
	MYSQL		*pMySQLConn;
	MYSQL_RES	*pMySQLResult;
public:
	CMySQL();
	~CMySQL();

	DWORD _ConnectToDB(ST_DB_LOGIN_TOKEN &refstDBLoginToken);
	DWORD _InsertToDB(ST_DB_SQL &refstDBSQL);
	DWORD _UpdateToDB(ST_DB_SQL &refstDBSQL);
	DWORD _QueryFromDB(ST_DB_SQL &refstDBSQL, ST_DB_RESULT &refstDBResult);
	DWORD _QuitDB();
};


#endif