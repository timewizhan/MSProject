#ifndef _SOCKETSTRUCT_
#define _SOCKETSTRUCT_

#define USER 20
#define MAXBUF 1024

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

//클라이언트가 보낸 메세지와 전송받을 클라이언트의 이름을 저장
typedef struct message {
	char user[USER];
	char sbuf[MAXBUF];
};

//서버에 접속한 클라이언트의 디스크립터와 거기에 매치되는 해당 클라이언트의 이름을 저장
struct add_num {
	int anum;
	char name[MAXBUF];
};

#endif