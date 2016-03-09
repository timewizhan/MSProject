#ifndef _DB_
#define _DB_

#include "Socket.h"
#include "mysql.h"

//DB
#define DB_HOST "127.0.0.1"
#define DB_USER "root"
#define DB_PASS "cclab"
#define DB_NAME "broker_table"

struct client_data{

	string			sUser;
	string			sLocation;
	int				iTimestamp;
	int				iClientSideTraffic;
};
/*
struct norm_cst_data{

	string			sLocation;
	double			dNormCst;
};
*/
struct server_data{

	int				iServerSideTraffic;
	int				iCpuUtil;
};

class CDatabase{

private:
	MYSQL			 *connection = NULL, conn;
	MYSQL_RES		 *sql_result;
	MYSQL_ROW		 sql_row;
	MYSQL_FIELD		 *field;

public:
	int				 m_iQueryStat;
	CSocket			 m_cSocket;

	CDatabase();
	~CDatabase();

	int InitDB();
	vector<client_data> extractClientData(string sQuery, int iNumOfColumn);
	vector<server_data> extractServerData(string sQuery, int iNumOfColumn);
	vector<client_data> ExtractCstData(string sQuery);
	vector<string>		ExtractCstLocation();
	void DeleteDuplicateValues(string sQuery);
	void insertData(string name, string location, int timestamp, int client_side_traffic, int server_side_traffic, int cpu_util, int ep_num, string side_flag);
	void InsertNormServerTable(vector <double> vecNormalizedSST, string sFlag);
	void InsertNormCstTable(vector <double> vecNormalizedCST, vector <string> vecNormalizedCSTLocation);
	void updateLocation(int, int, int);
};

#endif
