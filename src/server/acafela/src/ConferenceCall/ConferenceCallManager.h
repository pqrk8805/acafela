#pragma once

#include "IConferenceCall.h"
#include "IStorageAccessor.h"
#include <memory>

class ConferenceCallManager : public IConferenceCall
{
public:
	ConferenceCallManager();
	int MakeConferenceCall(	const std::string& dateFrom,
							const std::string& dateTo,
							const std::vector<std::string>& participantsList) override;
private:
	std::string generateNewCCNumber();
private:
	std::unique_ptr<IStorageAccessor> mSA;
};