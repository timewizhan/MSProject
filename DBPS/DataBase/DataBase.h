#ifndef _DATABASE_
#define _DATABASE_

#include "DataBaseType.h"

class IDataBase
{
public:
	IDataBase();
	virtual ~IDataBase();

	virtual DWORD _ConnectToDB(ST_DB_LOGIN_TOKEN &refstDBLoginToken)				= 0;
	virtual DWORD _InsertToDB(ST_DB_SQL &refstDBSQL)								= 0;
	virtual DWORD _UpdateToDB(ST_DB_SQL &refstDBSQL)								= 0;
	virtual DWORD _QueryFromDB(ST_DB_SQL &refstDBSQL, ST_DB_RESULT &refstDBResult)	= 0;
	virtual DWORD _QuitDB()															= 0;
};

/*
	This is common api method for user
*/

HANDLE CreateDBInstance(E_DB_TYPE eDBType);
DWORD ConnectToDB(HANDLE hHandle, ST_DB_LOGIN_TOKEN &refstDBLoginToken);
DWORD InsertToDB(HANDLE hHandle, ST_DB_SQL &refstDBSQL);
DWORD UpdateToDB(HANDLE hHandle, ST_DB_SQL &refstDBSQL);
DWORD QueryFromDB(HANDLE hHandle, ST_DB_SQL &refstDBSQL, ST_DB_RESULT &refstDBResult);
DWORD QuitDB(HANDLE hHandle);

#endif _DATABASE_