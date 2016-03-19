#ifndef _POSTGRESQL_
#define _POSTGRESQL_

#include "DataBase.h"

#define POSTGRESQL

#ifdef POSTGRESQL
#include <libpq-fe.h>
#endif

class CPostgreSQL : public IDataBase
{
	PGconn		*pPGConn;
	PGresult	*pPGResult;
public:
	CPostgreSQL();
	~CPostgreSQL();

	DWORD _ConnectToDB(ST_DB_LOGIN_TOKEN &refstDBLoginToken);
	DWORD _InsertToDB(ST_DB_SQL &refstDBSQL);
	DWORD _UpdateToDB(ST_DB_SQL &refstDBSQL);
	DWORD _QueryFromDB(ST_DB_SQL &refstDBSQL, ST_DB_RESULT &refstDBResult);
	DWORD _QuitDB();
};


#endif