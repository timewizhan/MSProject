#include "DBCQueue.h"
#include "..\Common\Log.h"

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

	try
	{
		m_queueDBConnection.push(refstDBConnection);
	}
	catch (std::exception &e) {
		ErrorLog("%s", e.what());
	}
	

	LeaveCriticalSection(&m_CriticalSection);
}

///////////////////////////////////////////////////////////////////////////////////////////////////////
void CDBCQueue::popFromQueue(ST_DBConnection &refstDBConnection)
{
	EnterCriticalSection(&m_CriticalSection);

	try
	{
		ST_DBConnection stDBConnection;
		if (m_queueDBConnection.size() != 0) {
			stDBConnection = m_queueDBConnection.front();
			m_queueDBConnection.pop();
		}
		refstDBConnection = stDBConnection;
	}
	catch (std::exception &e) {
		ErrorLog("%s", e.what());
	}

	LeaveCriticalSection(&m_CriticalSection);
}