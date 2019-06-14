#include "DirectoryServiceProvider.h"
using namespace std;

int DirectoryServiceProvider::saveDSItems(const string& phoneNumber, const string& ipAddress)
{
	lock_guard<mutex> lock(mDSMutex);

	PasswordFile f("UserNumber", "wb");

	string userNumberString = to_string(mUserNumber);

	f.WriteFile(userNumberString);
}

const map<string, string>& DirectoryServiceProvider::getDSItems()
{
	return mDSStorage;
}
