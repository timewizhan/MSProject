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

//Ŭ���̾�Ʈ�� ���� �޼����� ���۹��� Ŭ���̾�Ʈ�� �̸��� ����
typedef struct message {
	char user[USER];
	char sbuf[MAXBUF];
};

//������ ������ Ŭ���̾�Ʈ�� ��ũ���Ϳ� �ű⿡ ��ġ�Ǵ� �ش� Ŭ���̾�Ʈ�� �̸��� ����
struct add_num {
	int anum;
	char name[MAXBUF];
};

#endif