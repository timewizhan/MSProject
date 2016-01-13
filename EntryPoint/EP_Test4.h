/*
 * EP_Test4.h
 *
 *  Created on: Jan 13, 2016
 *      Author: ms-dev
 */

#ifndef _ENTRYPOINT_
#define _ENTRYPOINT_

#define MAXBUF 1024
#define USER 20

//서버에 보낼 메시지
struct message{
	char user[USER];
	char sbuf[MAXBUF];
};

class EP_Test4{

	int ssock;
	int clen;
	struct sockaddr_in client_addr, server_addr;
	char buf[MAXBUF],pre[MAXBUF];
	fd_set read_fds,tmp_fds;
	int fd;
	char name[20];
	struct message write_message;
	int re;

public:
	EP_Test4();
	~EP_Test4();

	void initEntryPoint();
	int entrySockStart();
	void sendMessage();

};

#endif
