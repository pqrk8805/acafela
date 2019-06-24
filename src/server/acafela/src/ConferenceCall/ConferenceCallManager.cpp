#include "ConferenceCallManager.h"
#include "EmailSenderCC.h"

using namespace std;
int ConferenceCallManager::MakeConferenceCall(const string& dateFrom, const string& dateTo, const vector<string>& participantsList)
{
	EmailSenderCC::sendCCInvitation("address", "roomNumber", dateFrom, dateTo);
	return 0;
}