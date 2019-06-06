#pragma once
#include <vector>
#include <thread>
#include <winsock2.h>
#include <ws2tcpip.h>
#include <windows.h>
#define BUFLEN 512
typedef struct {
	int send;
	int receive;
} PortGroup;

typedef struct {
	SOCKET server;
	SOCKET client;
} SocketGroup;

class Conversation;

class Communicator {
private:
	std::vector<std::thread *> threadList;
	std::vector<std::tuple<int, char *>> dataBuffer;
	std::vector<std::tuple<int, char *>> controlBuffer; // is it needed?
	PortGroup controlStreamPort;
	PortGroup dataStreamPort;
	SocketGroup controlStreamSocket;
	SocketGroup dataStreamSocket;
	std::string clientIP;
	CRITICAL_SECTION crit;
	Conversation * conversation;
public:
	Communicator(Conversation * conversation, std::string clientIP, PortGroup controlStreamPort, PortGroup dataStreamPort);
	void controlStream_create();
	void dataStream_create();
	void dataStream_addToSend(int len, char * data);
};

class Conversation {
private :
	std::vector<Communicator *> conversationRoom;
public :
	void broadcast(int len, char * data);
	void broadcast_exceptMe(Communicator * me, int len, char * data);
	void addCommunicator(std::string clientIP, PortGroup controlStreamPort, PortGroup dataStreamPort);
};

//need refactor
class SocketCreator {
public:
	SOCKET createSocket_serverSocket(int port, IPPROTO socketType);
	SOCKET createSocket_clientSocket(std::string clientIP, int port, IPPROTO socketType);
};