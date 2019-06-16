#include "communicator.h"

std::map<std::string, Participant *> ParticipantDirectory::participantDirectory;
std::mutex ParticipantDirectory::mLock;

void ParticipantDirectory::notify_update(std::string phoneNumber, std::string ip) {
	std::lock_guard<std::mutex> guard(mLock);
	const auto iter = participantDirectory.find(phoneNumber);
	if (iter == participantDirectory.cend() || iter->second == nullptr) {
		participantDirectory[phoneNumber] = new Participant(ip);
	}
	participantDirectory[phoneNumber]->setIP(ip);
}
void ParticipantDirectory::notify_remove(std::string phoneNumber) {
	std::lock_guard<std::mutex> guard(mLock);
	participantDirectory[phoneNumber] = nullptr;
}

Participant * ParticipantDirectory::get(std::string phoneNumber) {
	std::lock_guard<std::mutex> guard(mLock);
	const auto iter = participantDirectory.find(phoneNumber);
	if (iter == participantDirectory.cend() || iter->second == nullptr)
		return nullptr;
	return participantDirectory[phoneNumber];
}