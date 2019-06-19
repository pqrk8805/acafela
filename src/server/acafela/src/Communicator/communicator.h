#pragma once
#include <vector>
#include <thread>
#include <map>
#include <mutex>
#include <winsock2.h>
#include <ws2tcpip.h>
#include <windows.h>
#include "../SipMessage/SipMessage.pb.h"
#include "../CryptoKey/ICryptoKeyMgr.h"
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
	static void messageHandler(std::string IP, acafela::sip::SIPMessage msg);
	static ICryptoKeyMgr * keyManager;
public:
	static void createSocket();
	static void createControlServer(ICryptoKeyMgr * keyManager);
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
	static Participant * getFromNumber(std::string phoneNumber);
	static Participant * getFromIP(std::string IP);
};

class DataPath {
private:
	int receivePort;
	bool isServerPassed;
	bool isWorking;
	Participant * ownerPart;
	SocketGroup dataStreamSocket;
	Conversation * conversation;
	std::map<Participant *, int> sendPortDirectory;
	std::vector<std::tuple<Participant *, int, char *>> dataBuffer;
	CRITICAL_SECTION crit;
	std::string clientIP;
	std::vector<std::thread *> threadList;
	void createSocket();
	void createServerDataPath();
	void broadcastSessionControlMsg(acafela::sip::Command cmd);
	void sendSessionControlMsg(Participant * part, acafela::sip::Command cmd);
public:
	DataPath(Participant * ownerPart, Conversation * conversation, std::string clientIP, int receivePort, bool isServerPassed = false) {
		this->ownerPart = ownerPart;
		this->conversation = conversation;
		this->clientIP = clientIP;
		this->receivePort = receivePort;
		this->isServerPassed = isServerPassed;
		this->isWorking = true;
		if(isServerPassed)
			createServerDataPath();
	}
	void openDataPath();
	void terminateDataPath();
	void initParticipant(Participant * part, int port);
	void addParticipant(Participant * part, int port);
	void removeParticipant(Participant * part);
	void addToSendData(Participant * part, int len, char * data);
};

class Participant {
private:
	std::vector<std::tuple<int, char *>> controlBuffer; // is it needed?
	DataPath * dataPath;
	std::string clientIP;
public:
	Participant(std::string clientIP) {
		this->clientIP = clientIP;
		dataPath = nullptr;
	}
	void clearDataPath() {
		dataPath = nullptr;
	}
	DataPath * getDataPath();
	std::string getIP();
	void setIP(std::string ip);
	void setDataPath(DataPath * dataPath);
};

class Conversation {
private :
	std::vector<std::tuple<Participant *, int>> conversationRoom;
	bool isServerPassed;
	int sessionId;
public :
	Conversation(std::vector<std::tuple<Participant *, int>> partList, bool isServerPassed);
	bool isP2P() {
		return !isServerPassed;
	}
	void setSessionId(int sessionId) {
		this->sessionId = sessionId;
	}
	void broadcast_Data(Participant * partSend, int len, char * data);
	void boradcast_CtrlExceptMe(Participant * from, acafela::sip::SIPMessage msg);
	void addParticipant(Participant * part, int port);
	void removeParticipant(Participant * part);
	void makeConversation();
	void terminateConversation();
};

class PortHandler {
private:
	static int portNo;
public:
	static int getPortNumber() {
		return ++portNo;
	}
};
