#pragma once

#include <string>
#include <vector>

class IConferenceCall
{
protected:
	IConferenceCall() {}
	IConferenceCall(const IConferenceCall&) = delete;
	IConferenceCall& operator=(const IConferenceCall&) = delete;

public:
	~IConferenceCall() {}
	virtual int MakeConferenceCall(	const std::string& dateFrom,
									const std::string& dateTo,
									const std::vector<std::string>& participantsList) = 0;
};