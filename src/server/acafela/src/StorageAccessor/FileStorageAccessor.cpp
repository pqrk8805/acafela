#include "FileStorageAccessor.h"
using namespace std;

class SmartFile
{
public:
	SmartFile() {}
	SmartFile(const string& filename)
	{
		string binfile = filename + ".bin";
		fopen_s(&mFile, binfile.c_str(), "a+");
	}
	~SmartFile()
	{
		fclose(mFile);
	}
	FILE* GetPF() const { return mFile; }
private:
	FILE* mFile = nullptr;
};

string FileStorageAccessor::registerUser(const string& emailAddress, const string& password)
{
	lock_guard<mutex> lock(mFileLock);
	SmartFile f(emailAddress.c_str());	
	fwrite(password.c_str(), password.length(), 1, f.GetPF());

	return string("TBD");
}

int FileStorageAccessor::changePassword(const string& emailAddress, const string& oldPassword, const string& newPassword)
{
	lock_guard<mutex> lock(mFileLock);
	SmartFile f(emailAddress.c_str());
	fwrite(newPassword.c_str(), newPassword.length(), 1, f.GetPF());

	return -1;
}

int FileStorageAccessor::restorePassword(const string& emailAddress, const string& phoneNumber)
{	
	lock_guard<mutex> lock(mFileLock);
	SmartFile f(emailAddress.c_str());	

	return -1;
}