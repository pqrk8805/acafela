#include <stdio.h>
#include "communicator.h"
#include "../Hislog.h"
#include "../SipMessage/SipMessage.pb.h"
#define LOG_TAG "COMMPATH"
extern std::vector<std::thread *> additionalThreadList;
ICryptoKeyMgr * ConversationManager::keyManager;
SocketGroup ConversationManager::ctrlStreamSocket;
std::thread * ConversationManager::rcvThread;
std::map<Participant *, Conversation *> ConversationManager::conversationMap;
std::vector<acafela::sip::SIPMessage> ConversationManager::ctrlMessageBuffer;
void ConversationManager::createControlServer(ICryptoKeyMgr * keyManager_p) {
	keyManager = keyManager_p;
	createSocket();
	additionalThreadList.push_back(new std::thread([&] {
		while (1) {
			fflush(stdout);
			int recv_len;
			char * buf = new char[BUFLEN];
			memset(buf, NULL, BUFLEN);
			struct sockaddr_in si;
			int slen = sizeof(si);
			if ((recv_len = recvfrom(ctrlStreamSocket.server, buf, BUFLEN, 0, (struct sockaddr *) &si, &slen)) == SOCKET_ERROR)
				FUNC_LOGI("recvfrom() failed with error code : %d", WSAGetLastError());
			acafela::sip::SIPMessage msg;
			msg.ParseFromArray(buf, recv_len);
			char ipStr[INET_ADDRSTRLEN];
			inet_ntop(AF_INET, &(si.sin_addr), ipStr, INET_ADDRSTRLEN);
			messageHandler(std::string(ipStr), msg);
			delete buf;
		}
	}));
}

void ConversationManager::createSocket() {
	ctrlStreamSocket.client = socket(AF_INET, SOCK_DGRAM, IPPROTO_UDP);
	if (ctrlStreamSocket.client == INVALID_SOCKET)
		FUNC_LOGE("Could not create socket : %d", WSAGetLastError());
	
	if ((ctrlStreamSocket.server = socket(AF_INET, SOCK_DGRAM, IPPROTO_UDP)) == INVALID_SOCKET)
		FUNC_LOGE("Could not create socket : %d", WSAGetLastError());
	FUNC_LOGI("Server Socket created.");

	struct sockaddr_in server;
	server.sin_family = AF_INET;
	server.sin_addr.s_addr = INADDR_ANY;
	server.sin_port = htons(CTRLSERVERRCVPORT);

	if (bind(ctrlStreamSocket.server, (struct sockaddr *)&server, sizeof(server)) == SOCKET_ERROR)
	{
		FUNC_LOGE("Bind failed with error code : %d", WSAGetLastError());
		exit(EXIT_FAILURE);
	}
}

void ConversationManager::messageHandler(std::string IP, acafela::sip::SIPMessage msg) {
	Participant * from = ParticipantDirectory().getFromNumber(msg.from());
	Participant * to = ParticipantDirectory().getFromNumber(msg.to());
	Participant * sender = ParticipantDirectory().getFromIP(IP);
	if (to == nullptr) {
		acafela::sip::SIPMessage returnMessage;
		FUNC_LOGI("Cannot find user from directory : Say Good Bye~");
		returnMessage.set_cmd(acafela::sip::BYE);
		returnMessage.set_from("SERVER");
		returnMessage.set_to(IP);
		Participant * tmpPart = new Participant(IP);
		sendControlMessage(tmpPart, returnMessage);
		delete tmpPart;
		return;
	}
	switch (msg.cmd()) {
		case acafela::sip::ACCEPTCALL:
		{
			FUNC_LOGI("Request to Make Call");
			Conversation * conversation = conversationMap[sender];
			conversation->makeConversation();
		}
		break;
		case acafela::sip::INVITE:
		{
			FUNC_LOGI("Request to Make Key");
			keyManager->generateKey(msg.sessionid());
			Conversation * conversation = new Conversation({
				std::make_tuple(from,PortHandler().getPortNumber()),
				std::make_tuple(to,PortHandler().getPortNumber())
				}, false);
			conversationMap[from] = conversation;
			conversationMap[to] = conversation;
		}
		break;
		case acafela::sip::TERMINATE:
		{
			FUNC_LOGI("Request to Terminate");
			Conversation * conversation = conversationMap[sender];
			conversation->terminateConversation();
			for (auto iter = conversationMap.begin(); iter != conversationMap.end();) {
				if (std::get<1>(*iter) != conversation)
					iter++;
				else
					iter = conversationMap.erase(iter);
			}
			delete conversation;
			return;
		}
		break;
		case acafela::sip::STARTVIDEO:
		{
			FUNC_LOGI("Request to StartVideo");
			Conversation * conversation = conversationMap[sender];
			conversation->startVideoConversation();
			break;
		}
		case acafela::sip::STOPVIDEO:
		{
			FUNC_LOGI("Request to StartVideo");
			Conversation * conversation = conversationMap[sender];
			conversation->stopVideoConversation();
			break;
		}
		break;
		//case acafela::sip::LEAVE: 
		//{
		//	Conversation * conversation = conversationMap[from];
		//	conversation->removeParticipant(from);
		//	conversationMap.erase(from);
		//	return;
		//}
		break;
	}
	if (conversationMap[sender] == NULL)
		sendControlMessage(to, msg);
	else if (conversationMap[sender]->isP2P())
		conversationMap[sender]->boradcast_CtrlExceptMe(sender, msg);
}
void ConversationManager::sendControlMessage(
	Participant * to,
	acafela::sip::SIPMessage msg) {
	struct sockaddr_in server;
	server.sin_family = AF_INET;
	server.sin_port = htons(CTRLSERVERSNDPORT);
	inet_pton(AF_INET, to->getIP().c_str(), &(server.sin_addr));
	FUNC_LOGI("Send msg %s : \n----%s----\n%s", 
		to->getIP().c_str(), 
		acafela::sip::Command_descriptor()->FindValueByNumber(msg.cmd())->name().c_str(), 
		msg.DebugString().c_str()
	);
	size_t size = msg.ByteSizeLong();
	void *buffer = new char[size];
	msg.SerializeToArray(buffer, size);
	if (sendto(ctrlStreamSocket.client, (char *)buffer, size, 0, (struct sockaddr*) &server, sizeof(struct sockaddr_in)) == SOCKET_ERROR)
		FUNC_LOGE("sendto() failed with error code : %d", WSAGetLastError());
	delete buffer;
}