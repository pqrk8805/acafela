#include "FileStorageAccessor.h"
#include <sys/stat.h>
#include <sys/types.h>

#include <filesystem>
#include "DBFile.h"

using namespace std;
FileStorageAccessor::FileStorageAccessor()
{
}

FileStorageAccessor::~FileStorageAccessor()
{
}

int FileStorageAccessor::registerUser(const string& emailAddress, const string& password, const string& phoneNumber)
{
	{
		lock_guard<mutex> lock(mPasswordLock);

		PasswordFile f(emailAddress, "wb");
		
		f.WriteFile(password);		
	}

	{
		lock_guard<mutex> lock(mPhoneNumberLock);

		PhoneNumberFile f(emailAddress, "wb");

		f.WriteFile(phoneNumber);
	}

	return 0;
}

int FileStorageAccessor::changePassword(const string& emailAddress, const string& password)
{
	lock_guard<mutex> lock(mPasswordLock);

	PasswordFile f(emailAddress, "wb");

	f.WriteFile(password);

	return 0;
}

bool FileStorageAccessor::confirmPassword(const std::string& emailAddress, const std::string& password)
{
	lock_guard<mutex> lock(mPasswordLock);

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

int FileStorageAccessor::restorePassword(const string& emailAddress, const string& phoneNumber)
{
	return -1;
}

int FileStorageAccessor::confirmPhoneNumber(const string& emailAddress, const string& phoneNumber)
{
	lock_guard<mutex> lock(mPhoneNumberLock);

	PhoneNumberFile f(emailAddress, "rb");

	if (f.GetPF() == nullptr)
	{
		return false;
	}

	string savedPhoneNumber = f.ReadFile();

	if (phoneNumber.compare(0, phoneNumber.length(), savedPhoneNumber, 0, phoneNumber.length()) == 0)
		return true;
	else
		return false;
}

int FileStorageAccessor::getUserNumber()
{
	lock_guard<mutex> lock(mUserNumberLock);

	PasswordFile f("UserNumber", "rb");

	if (f.GetPF())
	{
		string strUserNumber = f.ReadFile();
		int nUserNumber = stoi(strUserNumber);
		return nUserNumber;
	}
	else
	{
		return 0;
	}
}

int FileStorageAccessor::updateUserNumber(int userNumber)
{
	lock_guard<mutex> lock(mUserNumberLock);

	PasswordFile f("UserNumber", "wb");

	string userNumberString = to_string(userNumber);

	f.WriteFile(userNumberString);

	return 0;
}

bool FileStorageAccessor::isExistUser(const std::string& emailAddress)
{
	lock_guard<mutex> lock(mPasswordLock);

	PasswordFile f(emailAddress, "wb");

	if (f.GetSize() > 0)
	{
		return true;
	}
	else
	{
		return false;
	}
}