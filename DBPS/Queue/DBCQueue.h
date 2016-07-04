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
	CDBCQueue	*m_pCDBCQueue;
	CRITICAL_SECTION	m_CriticalSection;

	typedef std::queue<ST_DBConnection> queueDBConnection;
	queueDBConnection	m_queueDBConnection;

public:
	CDBCQueue();
	~CDBCQueue();

	CDBCQueue* getQueueInstance() {
		if (!m_pCDBCQueue) {
			m_pCDBCQueue = new CDBCQueue();
		}
		return m_pCDBCQueue;
	}

	/**
	* Push queue (db connection)
	*
	* @param :
		ST_DBConnection : DB Connection
	* @return : None
	* @exception : None
	*/
	void pushToQueue(const ST_DBConnection &refstDBConnection);

	/**
	* Pop queue (db connection)
	*
	* @param :
		ST_DBConnection : DB Connection
	* @return : None
	* @exception : None
	*/
	void popFromQueue(ST_DBConnection &refstDBConnection);
};

#endif