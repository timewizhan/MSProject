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

	//client side monitoring result
	char	user[20];
	char	location[20];
	int		timestamp;
	int		traffic;
};

struct ST_EP_INFO {
	int		iFDNum;
	int		iEpNum;
	string	sIpAddr;
	string	sPort;
};

#endif