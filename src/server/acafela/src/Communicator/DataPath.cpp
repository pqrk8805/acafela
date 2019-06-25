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
	sendSessionControlMsg(part, acafela::sip::OPENSESSION, acafela::sip::RECIEVEAUDIO);
	if(conversation->isVideoComm())
		sendSessionControlMsg(part, acafela::sip::OPENSESSION, acafela::sip::RECIEVEVIDEO);
}

void DataPath::removeParticipant(Participant * leavePart) {
	FUNC_LOGI("CLOSESESSION to %s", ownerPart->getIP().c_str());
	sendSessionControlMsg(leavePart, acafela::sip::CLOSESESSION, acafela::sip::RECIEVEAUDIO);
	if (conversation->isVideoComm())
		sendSessionControlMsg(leavePart, acafela::sip::CLOSESESSION, acafela::sip::RECIEVEVIDEO);
	sendPortDirectory.erase(leavePart);
}

void DataPath::sendSessionControlMsg(Participant * part, acafela::sip::Command cmd, acafela::sip::SessionType sessionType) {
	acafela::sip::SIPMessage msg;
	msg.set_cmd(cmd);
	msg.set_from("SERVER");
	msg.set_to(ownerPart->getIP());
	acafela::sip::SessionInfo* sessionInfo = new acafela::sip::SessionInfo;
	msg.set_allocated_sessioninfo(sessionInfo);
	acafela::sip::Session* session = sessionInfo->add_sessions();
	session->set_sessiontype(sessionType);
	int port = sendPortDirectory[part];
	if (sessionType > acafela::sip::RECIEVEAUDIO)
		port += 1;
	session->set_port(port);
	session->set_ip(isServerPassed ? SERVER_IP : part->getIP());
	ConversationManager().sendCtrlMsg(ownerPart, msg, 0);
}

void DataPath::broadcastSessionControlMsg(acafela::sip::Command cmd, acafela::sip::SessionType sessionType) {
	acafela::sip::SIPMessage msg;
	msg.set_cmd(cmd);
	msg.set_from("SERVER");
	msg.set_to(ownerPart->getIP()); 
	acafela::sip::SessionInfo* sessionInfo = new acafela::sip::SessionInfo;
	msg.set_allocated_sessioninfo(sessionInfo);
	acafela::sip::Session* session = sessionInfo->add_sessions();
	session->set_sessiontype(sessionType);
	int listenPort = receivePort;
	if (sessionType > acafela::sip::RECIEVEAUDIO)
		listenPort += 1;
	session->set_port(listenPort);
	session->set_ip(
		isServerPassed
		? SERVER_IP
		: sendPortDirectory.begin()->first->getIP()
	);
	for (auto partAndPort : sendPortDirectory) {
		session = sessionInfo->add_sessions();
		session->set_sessiontype((acafela::sip::SessionType)(sessionType+1));
		session->set_ip(
			isServerPassed
			? SERVER_IP
			: std::get<0>(partAndPort)->getIP()
		);
		int port = std::get<1>(partAndPort);
		if (sessionType > acafela::sip::RECIEVEAUDIO)
			port += 1;
		session->set_port(port);
	}
	ConversationManager().sendCtrlMsg(ownerPart, msg, 0);
}

void DataPath::startVideoDataPath() {
	FUNC_LOGI("OPENSESSION VIDEO to %s", ownerPart->getIP().c_str());
	isVideoWorking = true;
	if (isServerPassed)
		createVideoServerDataPath();
	broadcastSessionControlMsg(acafela::sip::OPENSESSION, acafela::sip::SENDVIDEO);
}

void DataPath::stopVideoDataPath() {
	FUNC_LOGI("OPENSESSION VIDEO to %s", ownerPart->getIP().c_str());
	isVideoWorking = false;
	broadcastSessionControlMsg(acafela::sip::CLOSESESSION, acafela::sip::SENDVIDEO);
}

void DataPath::openDataPath() {
	FUNC_LOGI("OPENSESSION to %s", ownerPart->getIP().c_str());
	if (isServerPassed)
		createServerDataPath();
	broadcastSessionControlMsg(acafela::sip::OPENSESSION, acafela::sip::SENDAUDIO);
}

void DataPath::terminateDataPath() {
	isWorking = false;
	isVideoWorking = false;
	if (isServerPassed) {
		closesocket(dataStreamSocket.client);
		closesocket(dataStreamSocket.server);
	}
}

void DataPath::listener(SocketGroup socket, bool isVideo) {
	bool * workFlag = &isWorking;
	if (isVideo) workFlag = &isVideoWorking;
	while (*workFlag) {
		fflush(stdout);
		int recv_len;
		char * buf = new char[BUFLEN];
		memset(buf, NULL, BUFLEN);
		struct sockaddr_in si;
		int slen = sizeof(si);
		if ((recv_len = recvfrom(socket.server, buf, BUFLEN, 0, (struct sockaddr *) &si, &slen)) == SOCKET_ERROR)
			printf("DATAPATH : recvfrom() failed with error code : %d, %s\n", WSAGetLastError(), clientIP.c_str());
		conversation->broadcast_Data(ownerPart, recv_len, buf, isVideo);
		delete buf;
	}
}

void DataPath::sender(SocketGroup socket, bool isVideo) {
	bool * workFlag = &isWorking;
	if(isVideo) workFlag = &isVideoWorking;
	while (*workFlag) {
		EnterCriticalSection(&crit);
		std::vector<std::tuple<Participant *, int, char *>> * dBuffer;
		if (!isVideo)
			dBuffer = &dataBuffer;
		else
			dBuffer = &dataVideoBuffer;
		if (dBuffer->size() == 0) {
			LeaveCriticalSection(&crit);
			continue;
		}
		Participant * targetPart = std::get<0>(dBuffer->front());
		int recv_len = std::get<1>(dBuffer->front());
		char * buf = std::get<2>(dBuffer->front());
		dBuffer->erase(dBuffer->begin());
		LeaveCriticalSection(&crit);

		struct sockaddr_in server;
		server.sin_family = AF_INET;
		int port = sendPortDirectory[targetPart];
		if (isVideo) port += 1;
		server.sin_port = htons(port);
		inet_pton(AF_INET, clientIP.c_str(), &(server.sin_addr));
		if (sendto(socket.client, buf, recv_len, 0, (struct sockaddr*) &server, sizeof(struct sockaddr_in)) == SOCKET_ERROR)
			printf("sendto() failed with error code : %d\n", WSAGetLastError());
		delete buf;
	}
}

void DataPath::createServerDataPath() {
	InitializeCriticalSection(&crit);
	createSocket(dataStreamSocket, false);
	threadList.push_back(new std::thread(&DataPath::listener, this, dataStreamSocket, false));
	threadList.push_back(new std::thread(&DataPath::sender, this, dataStreamSocket, false));
}

void DataPath::createVideoServerDataPath() {
	createSocket(dataVideoStreamSocket, true);
	isVideoWorking = true;
	threadList.push_back(new std::thread(&DataPath::listener, this, dataVideoStreamSocket, true));
	threadList.push_back(new std::thread(&DataPath::sender, this, dataVideoStreamSocket, true));
}

void DataPath::addToSendData(Participant * part, int len, char * data, bool isVideo) {
	char * buf = new char[BUFLEN];
	memset(buf, NULL, BUFLEN);
	memcpy(buf, data, len + 1);
	EnterCriticalSection(&crit);
	if(!isVideo)
		dataBuffer.push_back(std::make_tuple(part, len, buf));
	else
		dataVideoBuffer.push_back(std::make_tuple(part, len, buf));
	LeaveCriticalSection(&crit);
}

void DataPath::createSocket(SocketGroup& streamSocket, bool isVideo) {
	streamSocket.client = socket(AF_INET, SOCK_DGRAM, IPPROTO_UDP);
	if (streamSocket.client == INVALID_SOCKET)
		FUNC_LOGE("Could not create socket : %d", WSAGetLastError());
	FUNC_LOGI("DATAPATH : Client Socket created : %s.", clientIP.c_str());

	if ((streamSocket.server = socket(AF_INET, SOCK_DGRAM, IPPROTO_UDP)) == INVALID_SOCKET)
		FUNC_LOGE("Could not create socket : %d", WSAGetLastError());
	FUNC_LOGI("DATAPATH : Server Socket created.");

	struct sockaddr_in server;
	server.sin_family = AF_INET;
	server.sin_addr.s_addr = INADDR_ANY;
	int port = receivePort;
	if (isVideo) port += 1;
	server.sin_port = htons(port);

	if (bind(streamSocket.server, (struct sockaddr *)&server, sizeof(server)) == SOCKET_ERROR)
		FUNC_LOGE("Bind failed with error code : %d", WSAGetLastError());
}
