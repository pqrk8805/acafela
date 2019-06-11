#pragma once
#include <vector>
#include <thread>
#include <map>
#include <winsock2.h>
#include <ws2tcpip.h>
#include <windows.h>
#define BUFLEN 512
#define CTRLSERVERPORT 5000
#pragma comment(lib,"ws2_32.lib")

typedef struct {
	SOCKET server;
	SOCKET client;
} SocketGroup;

class Conversation;
class Participant;
class ConversationManager {
private:
	SOCKET ctrlPathServer;
	std::map<Participant *,Conversation *> conversationMap;
public:
	void createControlServer();
	void createClientCtrlPath();
	Participant * createParticipant(std::string clientIP);
	void request_CreateControlPath(Participant *);
	void request_CreateDataPath(Participant *);
};

class ControlPath {
private:
	SOCKET ctrlPathClient;
	std::thread * threadList;
public:
	void initiateClientSocket(std::string clientIP, int port);
};

class DataPath {
private:
	int receivePort;
	Participant * ownerPart;
	SocketGroup dataStreamSocket;
	Conversation * conversation;
	std::map<Participant *, int> sendPortDirectory;
	std::vector<std::tuple<Participant *, int, char *>> dataBuffer;
	CRITICAL_SECTION crit;
	std::string clientIP;
	std::vector<std::thread *> threadList;
	void createSocket();
public:
	DataPath(Participant * ownerPart, Conversation * conversation, std::string clientIP, int receivePort) {
		this->ownerPart = ownerPart;
		this->conversation = conversation;
		this->clientIP = clientIP;
		this->receivePort = receivePort;
		createDataPath();
	}
	void createDataPath();
	void addParticipant(Participant * part, int port);
	void addToSendData(Participant * part, int len, char * data);
};

class Participant {
private:
	std::vector<std::tuple<int, char *>> controlBuffer; // is it needed?
	DataPath * dataPath;
	ControlPath * ctrlPath;
	std::string clientIP;
	Conversation * conversation;
public:
	Participant(std::string clientIP) {
		this->clientIP = clientIP;
	}
	DataPath * getDataPath();
	std::string getIP();
	void setDataPath(DataPath * dataPath);
	void joinConversation(Conversation * conversation);
};

class Conversation {
private :
	std::vector<std::tuple<Participant *, int>> conversationRoom;
public :
	void broadcast_Data(Participant * partSend, int len, char * data);
	void boradcast_Ctrl(std::string msg);
	void addParticipant(Participant * part, int port);
	void makeConversation(std::vector<std::tuple<Participant *, int>> partList);
};

class PortHandler {
private:
	static int portNo;
public:
	static int getPortNumber() {
		return ++portNo;
	}
};
