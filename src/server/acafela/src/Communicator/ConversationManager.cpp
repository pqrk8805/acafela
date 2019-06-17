#include <stdio.h>
#include "communicator.h"
#include "../Hislog.h"
#define LOG_TAG "COMMPATH"
extern std::vector<std::thread *> additionalThreadList;
SocketGroup ConversationManager::ctrlStreamSocket;
std::thread * ConversationManager::rcvThread;
std::map<Participant *, Conversation *> ConversationManager::conversationMap;
std::vector<acafela::sip::SIPMessage> ConversationManager::ctrlMessageBuffer;
void ConversationManager::createControlServer() {
	createSocket();
	additionalThreadList.push_back(new std::thread([&] {
		while (1) {
			fflush(stdout);
			int recv_len;
			char * buf = new char[BUFLEN];
			memset(buf, NULL, BUFLEN);
			struct sockaddr_in si_other;
			int slen = sizeof(si_other);
			if ((recv_len = recvfrom(ctrlStreamSocket.server, buf, BUFLEN, 0, (struct sockaddr *) &si_other, &slen)) == SOCKET_ERROR)
				FUNC_LOGI("recvfrom() failed with error code : %d", WSAGetLastError());
			acafela::sip::SIPMessage msg;
			msg.ParseFromArray(buf, recv_len);
			messageHandler(msg);
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

void ConversationManager::messageHandler(acafela::sip::SIPMessage msg) {
	Participant * from = ParticipantDirectory().get(msg.from());
	Participant * to = ParticipantDirectory().get(msg.to());
	if (to == nullptr) {
		acafela::sip::SIPMessage returnMessage;
		returnMessage.set_cmd(acafela::sip::BYE);
		returnMessage.set_from("SERVER");
		returnMessage.set_to(from->getIP());
		sendControlMessage(from, msg);
		return;
	}
	switch (msg.cmd()) {
		case acafela::sip::ACCEPTCALL:
		{
			FUNC_LOGI("Request to Make Call");
			Conversation * conversation = new Conversation();
			conversation->makeConversation({
				std::make_tuple(from,PortHandler().getPortNumber()),
				std::make_tuple(to,PortHandler().getPortNumber())
				}, false);
		}
		break;
		case acafela::sip::INVITE:
		{
			FUNC_LOGI("Request to Make Key");
			// should make Key
		}
		break;
		case acafela::sip::TERMINATE:
		{
			FUNC_LOGI("Request to Terminate");
			// should send bye and destruct conversation
		}
		break;
	}
	FUNC_LOGI("Send msg %d to %s", msg.cmd(), to->getIP().c_str());
	sendControlMessage(to, msg);
}
void ConversationManager::sendControlMessage(
	Participant * to,
	acafela::sip::SIPMessage msg) {
	struct sockaddr_in server;
	server.sin_family = AF_INET;
	server.sin_port = htons(CTRLSERVERSNDPORT);
	inet_pton(AF_INET, to->getIP().c_str(), &(server.sin_addr));
	size_t size = msg.ByteSizeLong();
	void *buffer = malloc(size);
	msg.SerializeToArray(buffer, size);
	if (sendto(ctrlStreamSocket.client, (char *)buffer, size, 0, (struct sockaddr*) &server, sizeof(struct sockaddr_in)) == SOCKET_ERROR)
		FUNC_LOGE("sendto() failed with error code : %d", WSAGetLastError());
	delete buffer;
}