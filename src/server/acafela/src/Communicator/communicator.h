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
	static CRITICAL_SECTION waitAckCrit;
	static CRITICAL_SECTION consumeAckCrit;
	static std::thread * rcvThread;
	static SocketGroup ctrlStreamSocket;
	static std::map<Participant *,Conversation *> conversationMap;
	static std::vector<std::tuple<int, acafela::sip::SIPMessage>> consumedPacketList;
	// time, retry, ip, msg
	static std::vector<std::tuple<int, int, std::string, acafela::sip::SIPMessage>> waitAckPacketList;
	static ICryptoKeyMgr * keyManager;
	static void sayGoodbyeMsg(std::string IP);
	static void forwardMessageHandler(std::string IP, acafela::sip::SIPMessage msg);
	static bool consumeMessageHandler(std::string IP, acafela::sip::SIPMessage msg);
	static void rcvAckHandler(std::string IP, acafela::sip::SIPMessage msg);
	static bool isHandledMsgAndAck(Participant * part, bool isServerConsumed, acafela::sip::SIPMessage msg);
	static void setRetryCtrlMsg(std::string ip, acafela::sip::SIPMessage msg, int retryConut);
	static int getTime();
public:
	static void createSocket();
	static void createControlServer(ICryptoKeyMgr * keyManager);
	static void sendCtrlMsg(Participant * to, acafela::sip::SIPMessage msg, int retryCount);
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
	bool isVideoWorking;
	std::string clientIP;
	CRITICAL_SECTION crit;
	Participant * ownerPart;
	Conversation * conversation;
	SocketGroup dataStreamSocket;
	SocketGroup dataVideoStreamSocket;
	std::map<Participant *, int> sendPortDirectory;
	std::vector<std::tuple<Participant *, int, char *>> dataBuffer;
	std::vector<std::tuple<Participant *, int, char *>> dataVideoBuffer;
	std::vector<std::thread *> threadList;
	void createSocket(SocketGroup& streamSocket, bool isVideo);
	void createServerDataPath();
	void createVideoServerDataPath();
	void broadcastSessionControlMsg(acafela::sip::Command cmd, acafela::sip::SessionType sessionType);
	void sendSessionControlMsg(Participant * part, acafela::sip::Command cmd, acafela::sip::SessionType sessionType);
public:
	DataPath(Participant * ownerPart, Conversation * conversation, std::string clientIP, int receivePort, bool isServerPassed = false) {
		this->ownerPart = ownerPart;
		this->conversation = conversation;
		this->clientIP = clientIP;
		this->receivePort = receivePort;
		this->isServerPassed = isServerPassed;
		this->isWorking = true;
		this->isVideoWorking = false;
		if(isServerPassed)
			createServerDataPath();
	};
	void openDataPath();
	void startVideoDataPath();
	void terminateDataPath();
	void stopVideoDataPath();
	void listener(SocketGroup socket, bool isVideo);
	void sender(SocketGroup socket, bool isVideo);
	void initParticipant(Participant * part, int port);
	void addParticipant(Participant * part, int port);
	void removeParticipant(Participant * part);
	void addToSendData(Participant * part, int len, char * data, bool isVideo);
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
	bool isVideoEnabled;
public :
	Conversation(std::vector<std::tuple<Participant *, int>> partList, bool isServerPassed);
	bool isP2P() {
		return !isServerPassed;
	}
	bool isVideoComm() {
		return isVideoEnabled;
	}
	void enableVideoConversation() {
		isVideoEnabled = true;
	}
	size_t getPartCount() {
		return conversationRoom.size();
	}
	void startVideoConversation();
	void stopVideoConversation();
	void broadcast_Data(Participant * partSend, int len, char * data, bool isVideo);
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
		portNo += 2;
		return portNo;
	}
};

class RoomNoHandler {
private:
	static int roomNo;
public:
	static int getRoomNumber() {
		return ++roomNo;
	}
};
