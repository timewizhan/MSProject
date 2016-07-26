/*
* socket.cpp
*
*  Created on: Feb 5, 2016
*      Author: alphahacker
*/
#define _WINSOCK_DEPRECATED_NO_WARNINGS

#include "DataQueue.h"
#include "Socket.h"
#include "Matching.h"

CSocket::CSocket(){

	optval = 1;
	InitSocket();
}
CSocket::~CSocket(){}

void CSocket::InitSocket(){

//	printf("init_socket \n");

	if (WSAStartup(MAKEWORD(2, 2), &wsaData) != 0) {
		printf("error\r\n");
	}
	
	if ((ssock = socket(AF_INET, SOCK_STREAM, IPPROTO_TCP))<0) {

		perror("socket error : ");
		exit(1);
	}

	setsockopt(ssock, SOL_SOCKET, SO_REUSEADDR, (const char*)&optval, sizeof(optval));

	memset(stEpInfo, 0, sizeof(stEpInfo));
	memset(&server_addr, 0, sizeof(server_addr));

	server_addr.sin_family = AF_INET;
	server_addr.sin_addr.s_addr = htonl(INADDR_ANY);
	server_addr.sin_port = htons(3333);

	if (bind(ssock, (struct sockaddr *)&server_addr, sizeof(server_addr)) < 0)
	{
		perror("bind error: ");
		exit(1);
	}

	if (listen(ssock, 5)<0)
	{
		perror("listen error : ");
		exit(1);
	}
}

void CSocket::InitBrokerGiverSocket(){

	if (WSAStartup(MAKEWORD(2, 2), &wsaData) != 0){
		printf("error\r\n");
	}

	if ((BrokerGiverSock = socket(AF_INET, SOCK_STREAM, 0))<0){
		perror("socket error : ");
		exit(1);
	}

	//변수 초기화
	memset(&server_addr, 0, sizeof(server_addr));
	server_addr.sin_family = AF_INET;
	server_addr.sin_addr.s_addr = inet_addr("165.132.122.243");
	server_addr.sin_port = htons(8888);

	clen = sizeof(server_addr);

	//socket이라는 파일 서술자를 이용하여 소켓에 연결을 하게 된다.
	if (connect(BrokerGiverSock, (struct sockaddr *)&server_addr, clen)<0){
		perror("connect error : ");
	}
}

void CSocket::SendStopMsg(){
	char szSendStopMsg[2] = "1";

	//send message
	printf("[Send stop msg to broker] \n");
	if (send(BrokerGiverSock, szSendStopMsg, sizeof(szSendStopMsg), 0)<0){
		perror("write error : ");
		exit(1);
	}
}

void CSocket::SendResumeMsg(){
	char szSendResumeMsg[2] = "0";

	//send message
	printf("[Send resume msg to broker] \n");
	if (send(BrokerGiverSock, szSendResumeMsg, sizeof(szSendResumeMsg), 0)<0){
		perror("write error : ");
		exit(1);
	}
}

void CSocket::CloseSocket(){
	closesocket(ssock);
	WSACleanup();
}

void CSocket::CommSocket(HANDLE hThread, ofstream &insDRResFile, ofstream &insWeightResFile){

//	printf("comm_socket \n");

	clen = sizeof(client_addr);

	FD_ZERO(&read_fds);
	FD_SET(ssock, &read_fds);

	maxfd = ssock;

	int iECount = 0;	// counting the number of the sending data end signal from each EP
	int iEpCount = 0;	// counting the number of connected EPs
	while (1)
	{
		tmp_fds = read_fds;
		
		printf("[Waiting Select Signal] \n");
		if (select(maxfd + 1, &tmp_fds, 0, 0, (struct timeval *)0)<1)
		{
			perror("select error : ");
			exit(1);
		}
		
		for (fd = 0; fd<maxfd + 1; fd++)
		{
			if (FD_ISSET(fd, &tmp_fds))
			{
				
				///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
				if (fd == ssock)
				{
					if ((csock = accept(ssock, (struct sockaddr *)&client_addr, &clen))<0)
					{
						perror("accept error : ");
						exit(0);
					}

					FD_SET(csock, &read_fds);

					printf(" - New client is connected = %d \n", csock);
					stEpInfo[iEpCount].iFDNum = csock;
					stEpInfo[iEpCount].sIpAddr = inet_ntoa(client_addr.sin_addr);
					iEpCount++;

					if (csock>maxfd)
					{
						maxfd = csock;
					}
				}
				////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////			
				else
				{
					struct ST_MONITORING_RESULT read_message;
					printf(" - Received Client Socket Signal (FD = %d) \n", fd);
					memset(&read_message, 0, sizeof(struct ST_MONITORING_RESULT));

					//클라이언트로 부터 메세지를 수신받는다.
					data_len = recv(fd, (char*)&read_message, sizeof(struct ST_MONITORING_RESULT), 0);
					
					CDataQueue::getDataQueue()->pushDataToQueue(read_message);
					
					if (!strcmp(read_message.side_flag, "e")){		
					
						iECount++;
						if (iECount == 3){		//3개의 EP에서 보낸 데이터 Broker 테이블 입력 완료
						
							iECount = 0;

							WaitForSingleObject(hThread, INFINITE);
				
							printf("[Sending Stop Signal to BrokerGiver] \n");
							/////////////////////////////////////////////////////
							//여기에 Broker Giver에게 멈추라는 코드 넣어야함
							InitBrokerGiverSocket();
							SendStopMsg();
							/////////////////////////////////////////////////////

							////////// Data Replacement 시간 출력 //////////
							CFileWrite cFileWrite;
							cFileWrite.InputTimeIntoDRFile(insDRResFile);
							///////////////////////////////////////////////

							printf("[Calculatiing LP] \n");
							CMatch match;
							match.NormalizeFactor(insDRResFile);
							match.InsertWeightTable();
							
							vector <match_result_data> vecMatchResult = match.CalculateLP();	//user, prev_ep, curr_ep 있는 데이터
						
							int EP1_FD = 0;
							int EP2_FD = 0;
							int EP3_FD = 0;
							for (int i = 0; i < NUM_OF_EP; i++){

								if (!strcmp(stEpInfo[i].sIpAddr.c_str(), "165.132.122.244")){	//EP1: 165.132.123.85 / Host: 165.132.122.244 
								
									EP1_FD = stEpInfo[i].iFDNum;
								}
								else if (!strcmp(stEpInfo[i].sIpAddr.c_str(), "165.132.122.245")) {	//EP2: 165.132.123.86 / Host : 165.132.122.245
								
									EP2_FD = stEpInfo[i].iFDNum;
								}
								else if (!strcmp(stEpInfo[i].sIpAddr.c_str(), "165.132.123.73")) {	//EP3: 165.132.123.87 / Host : 165.132.123.73
								
									EP3_FD = stEpInfo[i].iFDNum;
								}
							}
							
							printf("[Sending Matching Result to EPs] \n");
							match_result_data stMatchResData;
						//	char cUser[128];

							for (int i = 0; i < vecMatchResult.size(); i++){

								if (vecMatchResult.at(i).iPrevEp == 1){
									
									if (vecMatchResult.at(i).iPrevEp != vecMatchResult.at(i).iCurrEP){
									
										memset(&stMatchResData, 0, sizeof(stMatchResData));
										strcpy_s(stMatchResData.arrUser, 40, vecMatchResult.at(i).arrUser);
									
										stMatchResData.iPrevEp = vecMatchResult.at(i).iPrevEp;
										stMatchResData.iCurrEP = vecMatchResult.at(i).iCurrEP;

										send(EP1_FD, (char*)&stMatchResData, sizeof(stMatchResData), 0);
										
										////////////////// Data Replacement 유저 출력 ///////////////////
									//	CFileWrite cFileWrite;
										cFileWrite.WriteDRFile(stMatchResData.arrUser, insDRResFile);
										cFileWrite.WriteDRFile(stMatchResData.iPrevEp, insDRResFile);
										cFileWrite.WriteDRFile(stMatchResData.iCurrEP, insDRResFile);
										cFileWrite.WriteNewLine(insDRResFile);
										////////////////////////////////////////////////////////////////
									}
										
								}
								else if (vecMatchResult.at(i).iPrevEp == 2){

									if (vecMatchResult.at(i).iPrevEp != vecMatchResult.at(i).iCurrEP){

										memset(&stMatchResData, 0, sizeof(stMatchResData));
										strcpy_s(stMatchResData.arrUser, 40, vecMatchResult.at(i).arrUser);
									
										stMatchResData.iPrevEp = vecMatchResult.at(i).iPrevEp;
										stMatchResData.iCurrEP = vecMatchResult.at(i).iCurrEP;

										send(EP2_FD, (char*)&stMatchResData, sizeof(stMatchResData), 0);
										////////////////// Data Replacement 유저 출력 ///////////////////
										//	CFileWrite cFileWrite;
										cFileWrite.WriteDRFile(stMatchResData.arrUser, insDRResFile);
										cFileWrite.WriteDRFile(stMatchResData.iPrevEp, insDRResFile);
										cFileWrite.WriteDRFile(stMatchResData.iCurrEP, insDRResFile);
										cFileWrite.WriteNewLine(insDRResFile);
										////////////////////////////////////////////////////////////////
									}
								}
								else if (vecMatchResult.at(i).iPrevEp == 3){
									
									if (vecMatchResult.at(i).iPrevEp != vecMatchResult.at(i).iCurrEP){

										memset(&stMatchResData, 0, sizeof(stMatchResData));
										strcpy_s(stMatchResData.arrUser, 40, vecMatchResult.at(i).arrUser);
									
										stMatchResData.iPrevEp = vecMatchResult.at(i).iPrevEp;
										stMatchResData.iCurrEP = vecMatchResult.at(i).iCurrEP;

										send(EP3_FD, (char*)&stMatchResData, sizeof(stMatchResData), 0);
										////////////////// Data Replacement 유저 출력 ///////////////////
										//	CFileWrite cFileWrite;
										cFileWrite.WriteDRFile(stMatchResData.arrUser, insDRResFile);
										cFileWrite.WriteDRFile(stMatchResData.iPrevEp, insDRResFile);
										cFileWrite.WriteDRFile(stMatchResData.iCurrEP, insDRResFile);
										cFileWrite.WriteNewLine(insDRResFile);
										////////////////////////////////////////////////////////////////
									}
								}
							}

							printf("[Sending End Transmission Signal of Match Result to EPs] \n");
							//EP에게 전송 끝을 알림
							memset(&stMatchResData, 0, sizeof(stMatchResData));
							char arrEndSignal [40] = "end_match_result_transmission";
							strcpy_s(stMatchResData.arrUser, 40, arrEndSignal);
							stMatchResData.iCurrEP = 0;
							stMatchResData.iPrevEp = 0;

					
							char arrDRComplete[50];
					//		printf("1 \n");
							send(EP1_FD, (char*)&stMatchResData, sizeof(stMatchResData), 0);
					//		cout << "1-2 : " << stMatchResData.arrUser << endl;
					//		data_len = recv(EP1_FD, (char*)&arrDRComplete, sizeof(arrDRComplete), 0);
					//		if (data_len < 0){
					//			printf("Couldn't Receive Data Replacement Complete Message From EP1 \n");
					//		}
					//		printf("2 \n");
							send(EP2_FD, (char*)&stMatchResData, sizeof(stMatchResData), 0);
					//		cout << "2-1:" << stMatchResData.arrUser <<endl;
					//		data_len = recv(EP2_FD, (char*)&arrDRComplete, sizeof(arrDRComplete), 0);
					//		if (data_len < 0){
					//			printf("Couldn't Receive Data Replacement Complete Message From EP2 \n");
					//		}
					//		printf("3 \n");
							send(EP3_FD, (char*)&stMatchResData, sizeof(stMatchResData), 0);
					//		cout << "3-1:" << stMatchResData.arrUser << endl;
					//		data_len = recv(EP3_FD, (char*)&arrDRComplete, sizeof(arrDRComplete), 0);
					//		if (data_len < 0){
					//			printf("Couldn't Receive Data Replacement Complete Message From EP3 \n");
					//		}

							//9. 1시간마다 EP가 동시에 돌아가게 하기 위해 동기화 메세지 다시 보내기
					//		printf("[Sending Sync Signal to EPs] \n");
					//		char arrSyncMsg[10];
					//		memset(&arrSyncMsg, 0, sizeof(arrSyncMsg));
					//		strcpy_s(arrSyncMsg, 9, "SyncMsg");

					//		send(EP1_FD, (char*)&arrSyncMsg, sizeof(arrSyncMsg), 0);
					//		send(EP2_FD, (char*)&arrSyncMsg, sizeof(arrSyncMsg), 0);
					//		send(EP3_FD, (char*)&arrSyncMsg, sizeof(arrSyncMsg), 0);

							//Broker Giver에게 재시작하라는 메세지 전송
							InitBrokerGiverSocket();
							SendResumeMsg();
							

							goto ProcessEnd;
						}
					}
				}
				////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			} // end if statement (FD_ISSET)
		
		} // end for statement

	} // end while statement
ProcessEnd:
	printf("[ProcessEnd] \n");
}


