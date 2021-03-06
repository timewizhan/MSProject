#ifndef _BROKER_
#define _BROKER_

#include "DataQueue.h"
#include "SocketStruct.h"
#include "Database.h"
#include "Common.h"
#include "FileWrite.h"

unsigned WINAPI PreprocessInsert(void *data);

class CBroker{

public:
	HANDLE hThread;
	CDatabase m_cDatabase;

	CBroker();

	void InitBroker();
	void InitThread();
	void BridgeSocket(HANDLE hThread, ofstream &insDRResFile, ofstream &insWeightResFile);

};

#endif