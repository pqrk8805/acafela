#include <stdio.h>
#include "communicator.h"

void Conversation::broadcast_Data(Participant * partSend, int len, char * data) {
	for (auto partAndPort : conversationRoom) {
		Participant * part = std::get<0>(partAndPort);
		if (part == partSend)
			continue;
		part->getDataPath()->addToSendData(partSend, len, data);
	}
}
void Conversation::boradcast_Ctrl(std::string msg) {
	//tbd
	;
}
void Conversation::addParticipant(Participant * part, int port) {
	for(auto partAndPort : conversationRoom) 
		std::get<0>(partAndPort)->getDataPath()->addParticipant(part, port);
	DataPath * dPath = new DataPath(part, this, part->getIP(), port);
	for (auto partAndPort : conversationRoom)
		dPath->addParticipant(std::get<0>(partAndPort), std::get<1>(partAndPort));
	conversationRoom.push_back(std::make_tuple(part, port));
	part->setDataPath(dPath);
	part->joinConversation(this);
}

void Conversation::makeConversation(std::vector<std::tuple<Participant *, int>> partList) {
	for (auto part : partList)
		conversationRoom.push_back(part);
	for (auto partAndPort : partList) {
		Participant * part = std::get<0>(partAndPort);
		DataPath * dPath = new DataPath(part, this, part->getIP(), std::get<1>(partAndPort));
		for (auto partAndPort_temp : partList) {
			Participant * part_temp = std::get<0>(partAndPort_temp);
			if (part == part_temp)
				continue;
			dPath->addParticipant(part_temp, std::get<1>(partAndPort_temp));
		}
		part->setDataPath(dPath);
		part->joinConversation(this);
	}
}