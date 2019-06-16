#pragma once
#include <vector>
#include <thread>
#include <map>
#include <mutex>
#include <winsock2.h>
#include <ws2tcpip.h>
#include <windows.h>
#include "../SipMessage/SipMessage.pb.h"
#define BUFLEN 512
#define CTRLSERVERRCVPORT 5000
#define CTRLSERVERSNDPORT 5001
#pragma comment(lib,"ws2_32.lib")

typedef struct {
	SOCKET server;
	SOCKET client;
} SocketGroup;

class Conversation;
class Participant;
class ConversationManager {
private:
	static std::thread * rcvThread;
	static SocketGroup ctrlStreamSocket;
	static std::map<Participant *,Conversation *> conversationMap;
	static std::vector<acafela::sip::SIPMessage> ctrlMessageBuffer;
	static void messageHandler(acafela::sip::SIPMessage msg);
public:
	static void createSocket();
	static void createControlServer();
	static void sendControlMessage(
		Participant * to, 
		acafela::sip::SIPMessage msg
	);
};

class ParticipantDirectory {
private:
	static std::mutex mLock;
	static std::map<std::string, Participant *> participantDirectory;
public:
	static void notify_update(std::string phoneNumber, std::string ip);
	static void notify_remove(std::string phoneNumber);
	static Participant * get(std::string phoneNumber);
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
	std::string clientIP;
	Conversation * conversation;
public:
	Participant(std::string clientIP) {
		this->clientIP = clientIP;
	}
	DataPath * getDataPath();
	std::string getIP();
	void setIP(std::string ip);
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
