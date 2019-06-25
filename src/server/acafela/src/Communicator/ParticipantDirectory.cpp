#include "communicator.h"
#include "../Hislog.h"
#define LOG_TAG "COMMDIR"

std::map<std::string, Participant *> ParticipantDirectory::participantDirectory;
std::mutex ParticipantDirectory::mLock;

void ParticipantDirectory::notify_update(std::string phoneNumber, std::string ip) {
	FUNC_LOGI("%s : %s", phoneNumber.c_str(), ip.c_str());
	std::lock_guard<std::mutex> guard(mLock);
	const auto iter = participantDirectory.find(phoneNumber);
	if (iter == participantDirectory.cend() || iter->second == nullptr) {
		participantDirectory[phoneNumber] = new Participant(ip);
	}
	participantDirectory[phoneNumber]->setIP(ip);
}

void ParticipantDirectory::notify_remove(std::string phoneNumber) {
	FUNC_LOGI("%s", phoneNumber.c_str());
	std::lock_guard<std::mutex> guard(mLock);
	participantDirectory[phoneNumber] = nullptr;
}

Participant * ParticipantDirectory::getFromNumber(std::string phoneNumber) {
	std::lock_guard<std::mutex> guard(mLock);
	const auto iter = participantDirectory.find(phoneNumber);
	if (iter == participantDirectory.cend() || iter->second == nullptr) {
		FUNC_LOGI("Cannot Find %s", phoneNumber.c_str());
		return nullptr;
	}
	return participantDirectory[phoneNumber];
}

Participant * ParticipantDirectory::getFromIP(std::string IP) {
	std::lock_guard<std::mutex> guard(mLock);
	for (auto numAndPart : participantDirectory) {
		Participant * part = std::get<1>(numAndPart);
		if (!strcmp(IP.c_str(), part->getIP().c_str()))
			return part;
	}
	FUNC_LOGI("Cannot Find %s", IP.c_str());
	return nullptr;
}