#include "DataBase.h"

IDataBase::IDataBase()
{
}

IDataBase::~IDataBase()
{
}

/*
	This is common api method for user
*/

HANDLE CreateDBInstance(E_DB_TYPE eDBType)
{
	switch (eDBType)
	{
	case E_DB_MYSQL:
	case E_DB_ORACLE:
		/*
		nothing to implement
		*/
		break;
	case E_DB_POSTGRES:
		break;
	default:
		return NULL;
		break;
	}
	return NULL;
}

DWORD ConnectToDB(HANDLE hHandle, ST_DB_LOGIN_TOKEN &refstDBLoginToken)
{
	IDataBase *pIDB = NULL;
	pIDB = (IDataBase *)hHandle;

	DWORD dwRet;
	dwRet = pIDB->_ConnectToDB(refstDBLoginToken);
	return dwRet;
}

DWORD InsertToDB(HANDLE hHandle, ST_DB_SQL &refstDBSQL)
{
	IDataBase *pIDB = NULL;
	pIDB = (IDataBase *)hHandle;

	DWORD dwRet;
	dwRet = pIDB->_InsertToDB(refstDBSQL);
	return dwRet;
}

DWORD UpdateToDB(HANDLE hHandle, ST_DB_SQL &refstDBSQL)
{
	IDataBase *pIDB = NULL;
	pIDB = (IDataBase *)hHandle;

	DWORD dwRet;
	dwRet = pIDB->_UpdateToDB(refstDBSQL);
	return dwRet;
}

DWORD QueryFromDB(HANDLE hHandle, ST_DB_SQL &refstDBSQL, ST_DB_RESULT &refstDBResult)
{
	IDataBase *pIDB = NULL;
	pIDB = (IDataBase *)hHandle;

	DWORD dwRet;
	dwRet = pIDB->_QueryFromDB(refstDBSQL, refstDBResult);
	return dwRet;
}

DWORD QuitDB(HANDLE hHandle)
{
	IDataBase *pIDB = NULL;
	pIDB = (IDataBase *)hHandle;

	DWORD dwRet;
	dwRet = pIDB->_QuitDB();
	return dwRet;
}

