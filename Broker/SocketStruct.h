#ifndef _SOCKETSTRUCT_
#define _SOCKETSTRUCT_

#define NUM_OF_EP 3

#include "Common.h"

struct ST_MONITORING_RESULT{

	//flag
	int		ep_num;	//1, 2 or 3
	char	side_flag[10]; //server side: 's', client side: 'c'

	//server side monitoring result
	int		cpu_util;
	int		server_side_traffic;
	int		request_num;

	//client side monitoring result
	char	user[40];
	char	location[40];
	int		timestamp;
	int		traffic;
};

struct ST_EP_INFO {
	int		iFDNum;
	int		iEpNum;
	string	sIpAddr;
	string	sPort;
};

struct ST_CCT{
	int iNyTraffic = 0;
	int iBsTraffic = 0;
	int iChiTraffic = 0;
	int iTexTraffic = 0;
	int iWhaTraffic = 0;

	int WASHINGTON_TRAFFIC = 0;
	int MONTANA_TRAFFIC = 0;
	int NORTHDAKOTA_TRAFFIC = 0;
	int OREGON_TRAFFIC = 0;
	int IDAHO_TRAFFIC = 0;
	int WYOMING_TRAFFIC = 0;
	int SOUTHDAKOTA_TRAFFIC = 0;
	int NEBRASKA_TRAFFIC = 0;
	int ALASKA_TRAFFIC = 0;
	int CALIFORNIA_TRAFFIC = 0;
	int NEVADA_TRAFFIC = 0;
	int UTAH_TRAFFIC = 0;
	int COLORADO_TRAFFIC = 0;
	int KANSAS_TRAFFIC = 0;
	int MISSOURI_TRAFFIC = 0;
	int ARIZONA_TRAFFIC = 0;
	int NEWMEXICO_TRAFFIC = 0;
	int TEXAS_TRAFFIC = 0;
	int OKLAHOMA_TRAFFIC = 0;
	int ARKANSAS_TRAFFIC = 0;
	int LOUISIANA_TRAFFIC = 0;
	int HAWAII_TRAFFIC = 0;
	int MINNESOTA_TRAFFIC = 0;
	int WISCONSIN_TRAFFIC = 0;
	int MICHIGAN_TRAFFIC = 0;
	int IOWA_TRAFFIC = 0;
	int ILLINOIS_TRAFFIC = 0;
	int INDIANA_TRAFFIC = 0;
	int OHIO_TRAFFIC = 0;
	int PENNSYLVANIA_TRAFFIC = 0;
	int NEWYORK_TRAFFIC = 0;
	int VERMONT_TRAFFIC = 0;
	int NEWHAMPSHIRE_TRAFFIC = 0;
	int MAINE_TRAFFIC = 0;
	int MASSACHUSETTS_TRAFFIC = 0;
	int RHODE_TRAFFIC = 0;
	int CONNECTICUT_TRAFFIC = 0;
	int NEWJERSY_TRAFFIC = 0;
	int DELAWARE_TRAFFIC = 0;
	int MARYLAND_TRAFFIC = 0;
	int KENTUCKY_TRAFFIC = 0;
	int WESTVIRGINIA_TRAFFIC = 0;
	int VIRGINIA_TRAFFIC = 0;
	int TENNESSEE_TRAFFIC = 0;
	int NORTHCAROLINA_TRAFFIC = 0;
	int MISSISSIPPI_TRAFFIC = 0;
	int ALABAMA_TRAFFIC = 0;
	int GEORGIA_TRAFFIC = 0;
	int SOUTHCAROLINA_TRAFFIC = 0;
	int FLORIDA_TRAFFIC = 0;
	int GUAM_TRAFFIC = 0;

};

#endif