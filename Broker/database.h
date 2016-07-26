#ifndef _DB_
#define _DB_

#include "Socket.h"
#include "mysql.h"
#include "SocketStruct.h"

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

	string			sEpNum;
	int				iServerSideTraffic;
	int				iCpuUtil;
};

struct norm_server_data{

	string			sEpNum;
	double			dServerSideTraffic;
	double			dCpuUtil;
};

struct norm_cst_data{

	string			sUser;
	double			dCst;
};

struct norm_dist_data{

	string			sUser;
	double			dEp1;
	double			dEp2;
	double			dEp3;
};

struct coord_value{

	double latitude;
	double longitude;
};

struct weight_data{

	string sUser;
	int iUserNo;
	double dEp1;
	double dEp2;
	double dEp3;
};

struct match_result_data{

//	string sUser;
//	char *pUser;
//	char cUser[128];
	char arrUser[40];
	int iPrevEp;
	int iCurrEP;
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
	void CloseDB();
	void EstimationResultFileOut(ofstream &insDRResFile);
	vector<client_data> extractClientData(string sQuery);
	vector<server_data> extractServerData(string sQuery, int iNumOfColumn);
	vector<client_data> ExtractCstData(string sQuery);
	vector<string>		ExtractCstLocation();
	coord_value			ExtractCoordValue(string sLocation);
//	vector<norm_server_data> ExtractNormServerData();
	norm_server_data ExtractNormServerData(string sQuery);
	norm_cst_data ExtractNormCstData(string sQuery);
	double ExtractNormDistData(string sQuery);
	double ExtractNormSocialLevelData(string sQuery);
	vector<weight_data> ExtractWeightData();
	vector <match_result_data> ExtractMatchResult();
	void DeleteDuplicateValues(string sQuery);
//	void insertData(string name, string location, int timestamp, int client_side_traffic, int server_side_traffic, int cpu_util, int ep_num, string side_flag);
	void InsertServerTable(int iEP, int server_side_traffic, int cpu_util);
	void InsertClientTable(string sUser, string sLocation, int iTimestamp, int iClientSideTraffic);
	void InsertNumOfReq(int iEP, int request_num);
	void InsertNormServerTable(vector <norm_server_data> vecNormalizedSST, string sFlag);
	void InsertNormCstTable(vector <double> vecNormalizedCST, vector <string> vecNormalizedCSTLocation);
	void InsertNormDistTable(string sUser, double dNormDistEp1, double dNormDistEp2, double dNormDistEp3);
	void InsertWeightTable(string sUser, int iUserNo, double dWeightEp1, double dWeightEp2, double dWeightEp3);
	void InsertMatchingTable(string sUserNo, string sEpNo);
	void InsertUpdateMatchingTable();
	boolean CheckPrevTableEmpty();
//	void updateLocation(int, int, int);
	void updateLocation(ST_CCT stCCT);
	void UpdatePrevMatchingTable(vector <match_result_data> vecMatchResult);
	void DeleteTables();
};

#endif
