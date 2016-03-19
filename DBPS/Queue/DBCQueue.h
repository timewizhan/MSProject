#ifndef _DBPS_QUEUE_
#define _DBPS_QUEUE_

#include "..\Common\Common.h"

struct ST_DBConnection
{
	HANDLE hDataBase;

	ST_DBConnection() : hDataBase(NULL) {}
};

class CDBCQueue
{
	static CDBCQueue	*m_pCDBCQueue;
	CRITICAL_SECTION	m_CriticalSection;

	typedef std::queue<ST_DBConnection> queueDBConnection;
	queueDBConnection	m_queueDBConnection;

	CDBCQueue();
	//~CDBCQueue();
public:

	static CDBCQueue getQueueInstance() {
		if (m_pCDBCQueue) {
			m_pCDBCQueue = new CDBCQueue();
		}
		return *m_pCDBCQueue;
	}

	void pushToQueue(const ST_DBConnection &refstDBConnection);
	void popFromQueue(ST_DBConnection &refstDBConnection);
};

#endif