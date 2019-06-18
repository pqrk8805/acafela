#include <stdio.h>
#include "communicator.h"
#include "../Hislog.h"
#define LOG_TAG "DATAPATH"

void DataPath::addParticipant(Participant * part, int port) {
	sendPortDirectory[part] = port;
}

void DataPath::sendOpenDataPathMsg() {
	acafela::sip::SIPMessage msg;
	FUNC_LOGI("OPENSESSION to %s", ownerPart->getIP().c_str());
	msg.set_cmd(acafela::sip::OPENSESSION);
	msg.set_from("SERVER");
	msg.set_to(ownerPart->getIP()); 
	acafela::sip::SessionInfo* sessionInfo = new acafela::sip::SessionInfo;
	msg.set_allocated_sessioninfo(sessionInfo);
	acafela::sip::Session* session = sessionInfo->add_sessions();
	FUNC_LOGI("OPENSESSION SEND to %s, %d", ownerPart->getIP().c_str(), receivePort);
	session->set_sessiontype(acafela::sip::SENDAUDIO);
	session->set_port(receivePort);
	session->set_ip(
		isServerPassed
		? "SERVER"
		: sendPortDirectory.begin()->first->getIP()
	);
	for (auto partAndPort : sendPortDirectory) {
		FUNC_LOGI("OPENSESSION RCV to %s, %d", ownerPart->getIP().c_str(), std::get<1>(partAndPort));
		session = sessionInfo->add_sessions();
		session->set_sessiontype(acafela::sip::RECIEVEAUDIO);
		session->set_ip(
			isServerPassed
			? "SERVER"
			: std::get<0>(partAndPort)->getIP()
		);
		session->set_port(std::get<1>(partAndPort));
	}
	ConversationManager().sendControlMessage(ownerPart, msg);
}

void DataPath::createDataPath() {
	InitializeCriticalSection(&crit);
	createSocket();

	threadList.push_back(new std::thread([&] {
		while (1) {
			EnterCriticalSection(&crit);
			if (dataBuffer.size() == 0) {
				LeaveCriticalSection(&crit);
				continue;
			}
			Participant * targetPart = std::get<0>(dataBuffer.front());
			int recv_len = std::get<1>(dataBuffer.front());
			char * buf = std::get<2>(dataBuffer.front());
			dataBuffer.erase(dataBuffer.begin());
			LeaveCriticalSection(&crit);

			struct sockaddr_in server;
			server.sin_family = AF_INET;
			server.sin_port = htons(sendPortDirectory[targetPart]);
			inet_pton(AF_INET, clientIP.c_str(), &(server.sin_addr));
			if (sendto(dataStreamSocket.client, buf, recv_len, 0, (struct sockaddr*) &server, sizeof(struct sockaddr_in)) == SOCKET_ERROR)
				printf("sendto() failed with error code : %d\n", WSAGetLastError());
			delete buf;
		}
	}));
	threadList.push_back(new std::thread([&] {
		while (1) {
			fflush(stdout);
			int recv_len;
			char * buf = new char[BUFLEN];
			memset(buf, NULL, BUFLEN);
			struct sockaddr_in si_other;
			int slen = sizeof(si_other);
			if ((recv_len = recvfrom(dataStreamSocket.server, buf, BUFLEN, 0, (struct sockaddr *) &si_other, &slen)) == SOCKET_ERROR)
			{
				printf("DATAPATH : recvfrom() failed with error code : %d, %s\n", WSAGetLastError(), clientIP.c_str());
				//exit(EXIT_FAILURE);
			}
			conversation->broadcast_Data(ownerPart, recv_len, buf);
			delete buf;
		}
	}));
}

void DataPath::addToSendData(Participant * part, int len, char * data) {
	char * buf = new char[BUFLEN];
	memset(buf, NULL, BUFLEN);
	memcpy(buf, data, len + 1);
	EnterCriticalSection(&crit);
	dataBuffer.push_back(std::make_tuple(part, len, buf));
	LeaveCriticalSection(&crit);
}

void DataPath::createSocket() {
	dataStreamSocket.client = socket(AF_INET, SOCK_DGRAM, IPPROTO_UDP);
	if (dataStreamSocket.client == INVALID_SOCKET)
		printf("Could not create socket : %d", WSAGetLastError());
	printf("DATAPATH : Client Socket created : %s.\n", clientIP.c_str());

	if ((dataStreamSocket.server = socket(AF_INET, SOCK_DGRAM, IPPROTO_UDP)) == INVALID_SOCKET)
		printf("Could not create socket : %d", WSAGetLastError());
	printf("DATAPATH : Server Socket created.\n");

	struct sockaddr_in server;
	server.sin_family = AF_INET;
	server.sin_addr.s_addr = INADDR_ANY;
	server.sin_port = htons(receivePort);

	if (bind(dataStreamSocket.server, (struct sockaddr *)&server, sizeof(server)) == SOCKET_ERROR)
	{
		printf("Bind failed with error code : %d", WSAGetLastError());
		exit(EXIT_FAILURE);
	}
}
