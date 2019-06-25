#pragma once

#include "IConferenceCall.h"
#include "IStorageAccessor.h"
#include "../Communicator/communicator.h"
#include <memory>
#include <map>

class Conversation;
class ConferenceCallManager : public IConferenceCall
{
public:
	ConferenceCallManager();
	int MakeConferenceCall(	const std::string& dateFrom,
							const std::string& dateTo,
							const std::vector<std::string>& participantsList) override;
	Conversation * openConversationRoom(std::string ccNo, bool isVideo);
	void removeConversationRoom(std::string ccNo);
	Conversation * getConversationRoom(std::string ccNo);
private:
	std::string generateNewCCNumber();
	std::unique_ptr<IStorageAccessor> mSA;
	std::map<std::string, Conversation *> conversationMap;
};