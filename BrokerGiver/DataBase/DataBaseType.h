#ifndef _DATABASE_TYPE_
#define _DATABASE_TYPE_

#include "..\Common\Common.h"

enum E_DB_TYPE
{
	E_DB_POSTGRES = 0,
	E_DB_MYSQL,
	E_DB_ORACLE,

	E_DB_UNKNOWN
};

/*
	Usage : below example is postgresql
	[user = postgres password = xeros dbname = postgres hostaddr = 165.132.120.152 port = 5432]
*/

struct ST_DB_LOGIN_TOKEN
{
	std::string strUserName;
	std::string strPassword;
	std::string strDatabaseName;
	std::string strDatabaseIP;
	std::string strPort;
};

struct ST_DB_SQL
{
	std::string strSQL;
};

struct ST_DB_RESULT_LINE
{
	std::vector<std::string> vecstrResult;
};

struct ST_DB_RESULT
{
	std::vector<ST_DB_RESULT_LINE>	vecstDBResultLines;
};

struct ST_PROCESSED_VALUE
{
	std::vector<ST_DB_RESULT_LINE>	vecstDBResultLines;
};

#endif