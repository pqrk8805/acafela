#pragma once

#include <vector>
#include <string>

class ConferenceCallManager
{
public:
	ConferenceCallManager() {}
	int MakeConferenceCall(	const std::string& dateFrom,
							const std::string& dateTo,
							const std::vector<std::string>& participantsList);
};