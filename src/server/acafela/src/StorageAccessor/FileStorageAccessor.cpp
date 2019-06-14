#include "FileStorageAccessor.h"
#include <sys/stat.h>
#include <sys/types.h>

#include <filesystem>
#include "DBFile.h"

using namespace std;
FileStorageAccessor::FileStorageAccessor()
{
	getTotalUserNumber();
}

FileStorageAccessor::~FileStorageAccessor()
{
	updateUserNumber();
}

void FileStorageAccessor::getTotalUserNumber()
{
	lock_guard<mutex> lock(mUserNumberLock);

	PasswordFile f("UserNumber", "rb");

	if (f.GetPF())
	{
		string userNumber;
		userNumber = f.ReadFile();
		mUserNumber = stoi(userNumber);
	}
	else
	{
		mUserNumber = 0;
	}
}

void FileStorageAccessor::updateUserNumber()
{
	lock_guard<mutex> lock(mUserNumberLock);

	PasswordFile f("UserNumber", "wb");

	string userNumberString = to_string(mUserNumber);
	
	f.WriteFile(userNumberString);
}

string FileStorageAccessor::generateUserPhoneNumber()
{
	getTotalUserNumber();

	int totalUserNumber = mUserNumber + 1111;

	string userPhoneNumber = to_string(totalUserNumber);

	string fillZero(4 - userPhoneNumber.size(), '0');

	userPhoneNumber = fillZero + userPhoneNumber;

	mUserNumber += 1;

	updateUserNumber();

	return userPhoneNumber;
}

string FileStorageAccessor::registerUser(const string& emailAddress, const string& password)
{
	lock_guard<mutex> lock(mUserProfileLock);	

	PasswordFile f(emailAddress, "wb");

	if (f.GetSize() > 0)
	{
		return "Registered User";
	}
	else
	{
		f.WriteFile(password);

		string phoneNumber = generateUserPhoneNumber();

		return phoneNumber;
	}	
}

int FileStorageAccessor::changePassword(const string& emailAddress, const string& oldPassword, const string& newPassword)
{
	lock_guard<mutex> lock(mUserProfileLock);

	if (confirmUser(emailAddress, oldPassword) == false)
	{
		return -1;
	}	

	PasswordFile f(emailAddress, "wb");

	f.WriteFile(emailAddress);

	return -1;
}

int FileStorageAccessor::restorePassword(const string& emailAddress, const string& phoneNumber)
{
	return -1;
}

int FileStorageAccessor::deleteUser(const std::string& emailAddress)
{
	return -1;
}

bool FileStorageAccessor::confirmUser(const std::string& emailAddress, const std::string& password)
{
	lock_guard<mutex> lock(mUserProfileLock);

	PasswordFile f(emailAddress, "rb");

	if (f.GetPF() == nullptr)
	{
		return false;
	}

	string savedPassword = f.ReadFile();	

	if (password.compare(0, password.length(), savedPassword, 0, password.length()) == 0)
		return true;
	else
		return false;
}