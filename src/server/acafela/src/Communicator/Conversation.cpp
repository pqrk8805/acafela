#include <stdio.h>
#include "communicator.h"
#include "../Hislog.h"
#define LOG_TAG "CONV"

Conversation::Conversation(std::vector<std::tuple<Participant *, int>> partList, bool isServerPassed) {
	InitializeCriticalSection(&critRoom);
	EnterCriticalSection(&critRoom);
	this->isServerPassed = isServerPassed;
	FUNC_LOGI("Make Conversation Room, %s", isServerPassed ? "P2P" : "P2S");
	for (auto partAndPort : partList) {
		FUNC_LOGI("%s join room", std::get<0>(partAndPort)->getIP().c_str());
		conversationRoom.push_back(partAndPort);
	}
	LeaveCriticalSection(&critRoom);
}

void Conversation::makeConversation() {
	EnterCriticalSection(&critRoom);
	for (auto partAndPort : conversationRoom) {
		Participant * part = std::get<0>(partAndPort);
		FUNC_LOGI("Create %s data path, RCVPort : %d", std::get<0>(partAndPort)->getIP().c_str(), std::get<1>(partAndPort));
		DataPath * dPath = new DataPath(part, this, part->getIP().c_str(), std::get<1>(partAndPort), isServerPassed);
		for (auto partAndPort_temp : conversationRoom) {
			Participant * part_temp = std::get<0>(partAndPort_temp);
			if (part == part_temp)
				continue;
			dPath->initParticipant(part_temp, std::get<1>(partAndPort_temp));
		}
		part->setDataPath(dPath);
		dPath->openDataPath();
	}
	LeaveCriticalSection(&critRoom);
}

void Conversation::terminateConversation() {
	EnterCriticalSection(&critRoom);
	for (auto partAndPort : conversationRoom) {
		Participant * part = std::get<0>(partAndPort);
		acafela::sip::SIPMessage msg;
		msg.set_cmd(acafela::sip::BYE);
		FUNC_LOGI("Send Bye to %s", part->getIP().c_str());
		msg.set_from("SERVER");
		msg.set_to(part->getIP());
		ConversationManager().sendCtrlMsg(part, msg, 0);
		if (part->getDataPath() != nullptr) {
			part->getDataPath()->terminateDataPath();
			delete part->getDataPath();
			part->clearDataPath();
		}
	}
	LeaveCriticalSection(&critRoom);
}

void Conversation::broadcast_Data(Participant * partSend, int len, char * data, bool isVideo) {
	EnterCriticalSection(&critRoom); 
	for (auto partAndPort : conversationRoom) {
		Participant * part = std::get<0>(partAndPort);
		if (part == partSend)
			continue;
		part->getDataPath()->addToSendData(partSend, len, data, isVideo);
	}
	LeaveCriticalSection(&critRoom);
}

void Conversation::boradcast_CtrlExceptMe(Participant * from, acafela::sip::SIPMessage msg) {
	EnterCriticalSection(&critRoom);
	for (auto partAndPort : conversationRoom) {
		Participant * part = std::get<0>(partAndPort);
		if (part == from)
			continue;
		ConversationManager().sendCtrlMsg(part, msg, 0);
	}
	LeaveCriticalSection(&critRoom);
}

void Conversation::startVideoConversation() {
	isVideoEnabled = true;
	EnterCriticalSection(&critRoom);
	for (auto partAndPort : conversationRoom) {
		Participant * part = std::get<0>(partAndPort);
		part->getDataPath()->startVideoDataPath();
	}
	LeaveCriticalSection(&critRoom);
}

void Conversation::stopVideoConversation() {
	isVideoEnabled = false;
	EnterCriticalSection(&critRoom);
	for (auto partAndPort : conversationRoom) {
		Participant * part = std::get<0>(partAndPort);
		part->getDataPath()->stopVideoDataPath();
	}
	LeaveCriticalSection(&critRoom);
}

void Conversation::addParticipant(Participant * part, int port) {
	FUNC_LOGI("%s join room", part->getIP().c_str());
	EnterCriticalSection(&critRoom); 
	for (auto partAndPort : conversationRoom)
		std::get<0>(partAndPort)->getDataPath()->addParticipant(part, port);
	
	FUNC_LOGI("Create %s data path, RCVPort : %d", part->getIP().c_str(), port);
	DataPath * dPath = new DataPath(part, this, part->getIP(), port, isServerPassed);
	for (auto partAndPort : conversationRoom)
		dPath->initParticipant(std::get<0>(partAndPort), std::get<1>(partAndPort));
	conversationRoom.push_back({ part, port });
	part->setDataPath(dPath);
	dPath->openDataPath();
	LeaveCriticalSection(&critRoom);
}

void Conversation::removeParticipant(Participant * part) {
	EnterCriticalSection(&critRoom); 
	part->getDataPath()->terminateDataPath();
	delete part->getDataPath();
	part->clearDataPath();

	conversationRoom.erase(std::remove_if(begin(conversationRoom), end(conversationRoom), [part](const auto& i)
	{
		return std::get<0>(i) == part;
	}), end(conversationRoom));

	for (auto partAndPort : conversationRoom)
		std::get<0>(partAndPort)->getDataPath()->removeParticipant(part);
	LeaveCriticalSection(&critRoom); 
	acafela::sip::SIPMessage msg;
	msg.set_cmd(acafela::sip::BYE);
	msg.set_from("SERVER");
	msg.set_to(part->getIP());
	ConversationManager().sendCtrlMsg(part, msg, 0);
}