/*
 * EP_Test4.h
 *
 *  Created on: Jan 13, 2016
 *      Author: ms-dev
 */

#ifndef _ENTRYPOINT_
#define _ENTRYPOINT_


//SOCKET
#define MAXBUF 1024
#define USER 20

//DB
#define DB_HOST "127.0.0.1"
#define DB_USER "root"
#define DB_PASS "cclab"
#define DB_NAME "test"
#define CHOP(x) x[strlen(x) - 1] = ' '


//서버에 보낼 메시지
struct message{
	char user[USER];
	char sbuf[MAXBUF];
};

class EP_Test4{

	//SOCKET//////////////////////////////////////////////////////////////////////////////////////////////////////
	int ssock;
	int clen;
	struct sockaddr_in client_addr, server_addr;
	char buf[MAXBUF],pre[MAXBUF];
	fd_set read_fds,tmp_fds;
	int fd;
	char name[20];
	struct message write_message;
	int re;

	//DATABASE//////////////////////////////////////////////////////////////////////////////////////////////////////
	MYSQL       *connection=NULL, conn;
	MYSQL_RES   *sql_result;
	MYSQL_ROW   sql_row;
	int       query_stat;

	char _name[12];
	char address[80];
	char tel[12];
	char query[255];


public:
	EP_Test4();
	~EP_Test4();

	//SOCKET
	void initEntryPoint();
	int entrySockStart();
	void sendMessage();

	//DB
	int initDB();
	int extractData();
	int storeData();
};

#endif
