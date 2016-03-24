#include "DBCQueue.h"

/////////////////////////////////////////////////////////////////////////////////////////////////////
CDBCQueue::CDBCQueue()
{
	InitializeCriticalSection(&m_CriticalSection);
}

///////////////////////////////////////////////////////////////////////////////////////////////////////
CDBCQueue::~CDBCQueue()
{
	DeleteCriticalSection(&m_CriticalSection);
}

///////////////////////////////////////////////////////////////////////////////////////////////////////
void CDBCQueue::pushToQueue(const ST_DBConnection &refstDBConnection)
{
	EnterCriticalSection(&m_CriticalSection);

	m_queueDBConnection.push(refstDBConnection);

	LeaveCriticalSection(&m_CriticalSection);
}

///////////////////////////////////////////////////////////////////////////////////////////////////////
void CDBCQueue::popFromQueue(ST_DBConnection &refstDBConnection)
{
	EnterCriticalSection(&m_CriticalSection);

	ST_DBConnection stDBConnection;
	if (m_queueDBConnection.size() != 0) {
		stDBConnection = m_queueDBConnection.front();
		m_queueDBConnection.pop();
	}
	refstDBConnection = stDBConnection;

	LeaveCriticalSection(&m_CriticalSection);
}