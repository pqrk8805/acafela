#pragma once

#include <string>

class ConferenceCallManager
{
public:
	int MakeConferenceCall(	const std::string& dateFrom,
							const std::string& dateTo,
							const std::string& participantsList);
};