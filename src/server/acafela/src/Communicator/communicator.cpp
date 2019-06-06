#include <stdio.h>
#include "communicator.h"
#pragma comment(lib,"ws2_32.lib")
extern std::vector<std::thread *> additionalThreadList;
void pingpongCommunicator_init() {
	WSADATA wsa;
	//Initialise winsock
	printf("\nInitialising Winsock...\n");
	if (WSAStartup(MAKEWORD(2, 2), &wsa) != 0)
	{
		printf("Failed.Error Code : %d", WSAGetLastError());
		exit(EXIT_FAILURE);
	}
	Conversation * conversation = new Conversation();
	conversation->addCommunicator("10.0.1.151", { 0,0 }, { 5000,5001 });
	conversation->addCommunicator("10.0.1.152", { 0,0 }, { 5002,5003 });
}

void Conversation::broadcast(int len, char * data) {
	for (auto * co : conversationRoom) {
		co->dataStream_addToSend(len, data);
	}
}

void Conversation::broadcast_exceptMe(Communicator * me, int len, char * data) {
	for (auto * co : conversationRoom) {
		if (co == me)
			continue;
		co->dataStream_addToSend(len, data);
	}
}

void Conversation::addCommunicator(std::string clientIP, PortGroup controlStreamPort, PortGroup dataStreamPort) {
	conversationRoom.push_back(
		new Communicator(this, clientIP, controlStreamPort, dataStreamPort)
		);
}


Communicator::Communicator(Conversation * conversationRoom, std::string clientIP, PortGroup controlStreamPort = { 0,0 }, PortGroup dataStreamPort = { 0,0 }) {
	this->conversation = conversationRoom;
	this->clientIP = clientIP;
	this->controlStreamPort = controlStreamPort;
	this->dataStreamPort = dataStreamPort;
	InitializeCriticalSection(&crit);
	controlStream_create();
	dataStream_create();
};

void Communicator::controlStream_create() {
	;
}

void Communicator::dataStream_create() {
	dataStreamSocket.server = SocketCreator().createSocket_serverSocket(dataStreamPort.receive, IPPROTO_UDP);
	dataStreamSocket.client = SocketCreator().createSocket_clientSocket(clientIP, dataStreamPort.send, IPPROTO_UDP);

	// send
	struct sockaddr_in server;
	server.sin_family = AF_INET;
	inet_pton(AF_INET, clientIP.c_str(), &(server.sin_addr));
	server.sin_port = htons(dataStreamPort.send);
	threadList.push_back(new std::thread([&] { 
		while (1) {
			EnterCriticalSection(&crit);
			if (dataBuffer.size() == 0) {
				LeaveCriticalSection(&crit);
				continue;
			}
			int recv_len = std::get<0>(dataBuffer.front());
			char * buf = std::get<1>(dataBuffer.front());
			dataBuffer.erase(dataBuffer.begin());
			LeaveCriticalSection(&crit);

			if (sendto(dataStreamSocket.client, buf, recv_len, 0, (struct sockaddr*) &server, sizeof(server)) == SOCKET_ERROR)
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
				printf("recvfrom() failed with error code : %d\n", WSAGetLastError());
				//exit(EXIT_FAILURE);
			}
			conversation->broadcast_exceptMe(this, recv_len, buf);
		}
	}));
	for (auto * th : threadList)
		additionalThreadList.push_back(th);
}

void Communicator::dataStream_addToSend(int len, char * data) {
	char * buf = new char[BUFLEN];
	EnterCriticalSection(&crit);
	dataBuffer.push_back(std::make_tuple(len, buf));
	LeaveCriticalSection(&crit);
}

SOCKET SocketCreator::createSocket_clientSocket(std::string clientIP, int port, IPPROTO socketType) {
	SOCKET s;
	if ((s = socket(AF_INET, SOCK_DGRAM, socketType)) == INVALID_SOCKET)
		printf("Could not create socket : %d", WSAGetLastError());
	
	printf("Client Socket created.\n");
	struct sockaddr_in server;
	server.sin_family = AF_INET;
	inet_pton(AF_INET, clientIP.c_str(), &(server.sin_addr));
	server.sin_port = htons(port);

	//need to add connect if TCP
	if (socketType == IPPROTO_TCP);
	
	return s;
}


SOCKET SocketCreator::createSocket_serverSocket(int port, IPPROTO socketType) {
	SOCKET s;
	struct sockaddr_in server;

	//Create a socket
	if ((s = socket(AF_INET, SOCK_DGRAM, IPPROTO_UDP)) == INVALID_SOCKET)
	{
		printf("Could not create socket : %d", WSAGetLastError());
	}
	printf("Server Socket created.\n");

	server.sin_family = AF_INET;
	server.sin_addr.s_addr = INADDR_ANY;
	server.sin_port = htons(port);

	if (bind(s, (struct sockaddr *)&server, sizeof(server)) == SOCKET_ERROR)
	{
		printf("Bind failed with error code : %d", WSAGetLastError());
		exit(EXIT_FAILURE);
	}
	printf("Bind done\n");

	return s;
}
