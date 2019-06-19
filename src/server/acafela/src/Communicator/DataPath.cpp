#include <stdio.h>
#include "communicator.h"
#include "../Hislog.h"
#define LOG_TAG "DATAPATH"

void DataPath::initParticipant(Participant * part, int port) {
	sendPortDirectory[part] = port;
}

void DataPath::addParticipant(Participant * part, int port) {
	FUNC_LOGI("ADD OPENSESSION to %s", ownerPart->getIP().c_str()); 
	sendPortDirectory[part] = port;
	sendSessionControlMsg(part, acafela::sip::OPENSESSION);
}

void DataPath::removeParticipant(Participant * leavePart) {
	FUNC_LOGI("CLOSESESSION to %s", ownerPart->getIP().c_str());
	sendSessionControlMsg(leavePart, acafela::sip::CLOSESESSION);
	sendPortDirectory.erase(leavePart);
}

void DataPath::sendSessionControlMsg(Participant * part, acafela::sip::Command cmd) {
	acafela::sip::SIPMessage msg;
	msg.set_cmd(cmd);
	msg.set_from("SERVER");
	msg.set_to(ownerPart->getIP());
	acafela::sip::SessionInfo* sessionInfo = new acafela::sip::SessionInfo;
	msg.set_allocated_sessioninfo(sessionInfo);
	acafela::sip::Session* session = sessionInfo->add_sessions();
	session->set_sessiontype(acafela::sip::RECIEVEAUDIO);
	session->set_port(sendPortDirectory[part]);
	session->set_ip(isServerPassed ? "SERVER" : part->getIP());
	ConversationManager().sendControlMessage(ownerPart, msg);
}

void DataPath::broadcastSessionControlMsg(acafela::sip::Command cmd) {
	acafela::sip::SIPMessage msg;
	msg.set_cmd(cmd);
	msg.set_from("SERVER");
	msg.set_to(ownerPart->getIP()); 
	acafela::sip::SessionInfo* sessionInfo = new acafela::sip::SessionInfo;
	msg.set_allocated_sessioninfo(sessionInfo);
	acafela::sip::Session* session = sessionInfo->add_sessions();
	session->set_sessiontype(acafela::sip::SENDAUDIO);
	session->set_port(receivePort);
	session->set_ip(
		isServerPassed
		? "SERVER"
		: sendPortDirectory.begin()->first->getIP()
	);
	for (auto partAndPort : sendPortDirectory) {
		session = sessionInfo->add_sessions();
		session->set_sessiontype(acafela::sip::RECIEVEAUDIO);
		session->set_ip(
			isServerPassed
			? "SERVER"
			: std::get<0>(partAndPort)->getIP()
		);
		session->set_port(std::get<1>(partAndPort));
	}
	ConversationManager().sendControlMessage(ownerPart, msg);
}

void DataPath::openDataPath() {
	FUNC_LOGI("OPENSESSION to %s", ownerPart->getIP().c_str());
	broadcastSessionControlMsg(acafela::sip::OPENSESSION);
}

void DataPath::terminateDataPath() {
	//server close ½Ã ÀüÃ¼ Close Session ÇÒ °ÍÀÎ°¡? ¾Æ´Ô °Á Bye?
	FUNC_LOGI("TERMINATESESSION to %s", ownerPart->getIP().c_str());
	//broadcastSessionControlMsg(acafela::sip::CLOSESESSION);
	isWorking = false;
	if (isServerPassed) {
		closesocket(dataStreamSocket.client);
		closesocket(dataStreamSocket.server);
	}
}

void DataPath::createServerDataPath() {
	InitializeCriticalSection(&crit);
	createSocket();

	threadList.push_back(new std::thread([&] {
		while (isWorking) {
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
		while (isWorking) {
			fflush(stdout);
			int recv_len;
			char * buf = new char[BUFLEN];
			memset(buf, NULL, BUFLEN);
			struct sockaddr_in si_other;
			int slen = sizeof(si_other);
			if ((recv_len = recvfrom(dataStreamSocket.server, buf, BUFLEN, 0, (struct sockaddr *) &si_other, &slen)) == SOCKET_ERROR)
				printf("DATAPATH : recvfrom() failed with error code : %d, %s\n", WSAGetLastError(), clientIP.c_str());
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
		FUNC_LOGE("Could not create socket : %d", WSAGetLastError());
	FUNC_LOGI("DATAPATH : Client Socket created : %s.", clientIP.c_str());

	if ((dataStreamSocket.server = socket(AF_INET, SOCK_DGRAM, IPPROTO_UDP)) == INVALID_SOCKET)
		FUNC_LOGE("Could not create socket : %d", WSAGetLastError());
	FUNC_LOGI("DATAPATH : Server Socket created.");

	struct sockaddr_in server;
	server.sin_family = AF_INET;
	server.sin_addr.s_addr = INADDR_ANY;
	server.sin_port = htons(receivePort);

	if (bind(dataStreamSocket.server, (struct sockaddr *)&server, sizeof(server)) == SOCKET_ERROR)
		FUNC_LOGE("Bind failed with error code : %d", WSAGetLastError());
}
