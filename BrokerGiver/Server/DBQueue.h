#ifndef _DB_QUEUE_
#define _DB_QUEUE_

#include "..\Common\Common.h"

struct ST_DBConnection
{
	HANDLE hDataBase;

	ST_DBConnection() : hDataBase(NULL) {}
};

class CDBQueue
{
	CDBQueue	*m_pCDBQueue;
	CRITICAL_SECTION	m_CriticalSection;

	typedef std::queue<ST_DBConnection> queueDBConnection;
	queueDBConnection	m_queueDBConnection;

public:
	CDBQueue();
	~CDBQueue();

	CDBQueue* getQueueInstance() {
		if (!m_pCDBQueue) {
			m_pCDBQueue = new CDBQueue();
		}
		return m_pCDBQueue;
	}

	void pushToQueue(const ST_DBConnection &refstDBConnection);
	void popFromQueue(ST_DBConnection &refstDBConnection);
};

#endif