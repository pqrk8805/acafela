#include <stdio.h>
#include "communicator.h"
extern std::vector<std::thread *> additionalThreadList;


void DataPath::addParticipant(Participant * part, int port) {
	sendPortDirectory[part] = port;
	acafela::sip::SIPMessage msg;
	msg.set_allocated_sessioninfo();
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
