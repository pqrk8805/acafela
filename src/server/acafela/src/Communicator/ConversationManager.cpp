#include <stdio.h>
#include "communicator.h"
SocketGroup ConversationManager::ctrlStreamSocket;
std::thread * ConversationManager::rcvThread;
std::map<Participant *, Conversation *> ConversationManager::conversationMap;
std::vector<acafela::sip::SIPMessage> ConversationManager::ctrlMessageBuffer;
void ConversationManager::createControlServer() {
	createSocket();
	rcvThread = new std::thread([&] {
		while (1) {
			fflush(stdout);
			int recv_len;
			char * buf = new char[BUFLEN];
			memset(buf, NULL, BUFLEN);
			struct sockaddr_in si_other;
			int slen = sizeof(si_other);
			if ((recv_len = recvfrom(ctrlStreamSocket.server, buf, BUFLEN, 0, (struct sockaddr *) &si_other, &slen)) == SOCKET_ERROR)
			{
				printf("DATAPATH : recvfrom() failed with error code : %d\n", WSAGetLastError());
				//exit(EXIT_FAILURE);
			}
			acafela::sip::SIPMessage msg;
			msg.ParseFromArray(buf, recv_len);
			messageHandler(msg);
			delete buf;
		}
	});
}

void ConversationManager::createSocket() {
	ctrlStreamSocket.client = socket(AF_INET, SOCK_DGRAM, IPPROTO_UDP);
	if (ctrlStreamSocket.client == INVALID_SOCKET)
		printf("CTRL : Could not create socket : %d", WSAGetLastError());

	struct sockaddr_in server;
	server.sin_family = AF_INET;
	server.sin_addr.s_addr = INADDR_ANY;
	server.sin_port = htons(CTRLSERVERRCVPORT);

	if (bind(ctrlStreamSocket.server, (struct sockaddr *)&server, sizeof(server)) == SOCKET_ERROR)
	{
		printf("CTRL : Bind failed with error code : %d", WSAGetLastError());
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
		// or MAKECALL?
		case acafela::sip::ACCEPTCALL:
		{
			Conversation * conversation = new Conversation();
			conversation->makeConversation({
				std::make_tuple(from,PortHandler().getPortNumber()),
				std::make_tuple(to,PortHandler().getPortNumber())
				});
		}
		break;
		case acafela::sip::INVITE:
		{
			// should make Key
		}
		break;
		case acafela::sip::TERMINATE:
		{
			// should send bye and destruct conversation
		}
		break;
	}
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
		printf("sendto() failed with error code : %d\n", WSAGetLastError());
	delete buffer;
}