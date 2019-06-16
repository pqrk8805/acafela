#include <stdio.h>
#include "communicator.h"

DataPath * Participant::getDataPath() {
	return dataPath;
}

std::string Participant::getIP() {
	return clientIP;
}

void Participant::setIP(std::string ip) {
	this->clientIP = ip;
}

void Participant::setDataPath(DataPath * dataPath) {
	this->dataPath = dataPath;
}

void Participant::joinConversation(Conversation * conversation) {
	this->conversation = conversation;
}