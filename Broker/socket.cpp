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

	if (bind(ssock, (struct sockaddr *)&server_addr, sizeof(server_addr))<0)
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

void CSocket::CloseSocket(){
	closesocket(ssock);
	WSACleanup();
}

void CSocket::CommSocket(HANDLE	hThread){

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
					printf(" - Received Client Socket Signal (FD = %d) \n", fd);
					memset(&read_message, 0, sizeof(read_message));

					//클라이언트로 부터 메세지를 수신받는다.
					data_len = recv(fd, (char*)&read_message, sizeof(read_message), 0);
					
					CDataQueue::getDataQueue()->pushDataToQueue(read_message);
					
				 
					if (!strcmp(read_message.side_flag, "e")){		
					
						iECount++;
						if (iECount == 3){		//3개의 EP에서 보낸 데이터 Broker 테이블 입력 완료
						
							iECount = 0;

							WaitForSingleObject(hThread, INFINITE);
				//			ResumeThread(hThread);

							CMatch match;
							match.NormalizeFactor();
							match.InsertWeightTable();
							
							//여기서 LP 결과 리턴 값으로 받는걸로, EP 1에 보내야 되는거 2에 보내야되는거 3에보내야 되는거 구분
							vector <match_result_data> vecMatchResult = match.CalculateLP();	//user, prev_ep, curr_ep 있는 데이터
						
							
							int EP1_FD = 0;
							int EP2_FD = 0;
							int EP3_FD = 0;
							for (int i = 0; i < NUM_OF_EP; i++){

								if (!strcmp(stEpInfo[i].sIpAddr.c_str(), "165.132.122.244")){	//EP1: 165.132.123.85  
								
									EP1_FD = stEpInfo[i].iFDNum;
								}
								else if (!strcmp(stEpInfo[i].sIpAddr.c_str(), "165.132.122.245")) {	//EP2: 165.132.123.86
								
									EP2_FD = stEpInfo[i].iFDNum;
								}
								else if (!strcmp(stEpInfo[i].sIpAddr.c_str(), "165.132.123.73")) {	//EP3: 165.132.123.87
								
									EP3_FD = stEpInfo[i].iFDNum;
								}
							}
							

							match_result_data stMatchResData;
							char cUser[128];

							for (int i = 0; i < vecMatchResult.size(); i++){

								if (vecMatchResult.at(i).iPrevEp == 1){
									
									if (vecMatchResult.at(i).iPrevEp != vecMatchResult.at(i).iCurrEP){
									
										memset(&stMatchResData, 0, sizeof(stMatchResData));
										strcpy_s(stMatchResData.arrUser, 40, vecMatchResult.at(i).arrUser);
									
										stMatchResData.iPrevEp = vecMatchResult.at(i).iPrevEp;
										stMatchResData.iCurrEP = vecMatchResult.at(i).iCurrEP;

										send(EP1_FD, (char*)&stMatchResData, sizeof(stMatchResData), 0);
									}
										
								}
								else if (vecMatchResult.at(i).iPrevEp == 2){

									if (vecMatchResult.at(i).iPrevEp != vecMatchResult.at(i).iCurrEP){

										memset(&stMatchResData, 0, sizeof(stMatchResData));
										strcpy_s(stMatchResData.arrUser, 40, vecMatchResult.at(i).arrUser);
									
										stMatchResData.iPrevEp = vecMatchResult.at(i).iPrevEp;
										stMatchResData.iCurrEP = vecMatchResult.at(i).iCurrEP;

										send(EP2_FD, (char*)&stMatchResData, sizeof(stMatchResData), 0);
									}
								}
								else if (vecMatchResult.at(i).iPrevEp == 3){
									
									if (vecMatchResult.at(i).iPrevEp != vecMatchResult.at(i).iCurrEP){

										memset(&stMatchResData, 0, sizeof(stMatchResData));
										strcpy_s(stMatchResData.arrUser, 40, vecMatchResult.at(i).arrUser);
									
										stMatchResData.iPrevEp = vecMatchResult.at(i).iPrevEp;
										stMatchResData.iCurrEP = vecMatchResult.at(i).iCurrEP;

										send(EP3_FD, (char*)&stMatchResData, sizeof(stMatchResData), 0);
									}
								}
							}

							//EP에게 전송 끝을 알림
							memset(&stMatchResData, 0, sizeof(stMatchResData));
							char arrEndSignal [40] = "end_match_result_transmission";
							strcpy_s(stMatchResData.arrUser, 40, arrEndSignal);
							stMatchResData.iCurrEP = 0;
							stMatchResData.iPrevEp = 0;

					
							send(EP1_FD, (char*)&stMatchResData, sizeof(stMatchResData), 0);
							send(EP2_FD, (char*)&stMatchResData, sizeof(stMatchResData), 0);
							send(EP3_FD, (char*)&stMatchResData, sizeof(stMatchResData), 0);
						
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


/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


