#include "FileStorageAccessor.h"
#include <sys/stat.h>
#include <sys/types.h>

#include <windows.h>
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

#include <regex>
string extractIntegerWords(string str)
{
	string output;
	for (int i = 0; i < str.length(); i++)
	{
		if (isdigit(str[i]))
			output += str[i];
	}
	return output;
}

string FileStorageAccessor::getTempPassword(const string& emailAddress)
{
	lock_guard<mutex> lock(mPasswordLock);

	PasswordFile f(emailAddress, "rb");

	if (f.GetPF() == nullptr)
	{
		return "0000";
	}

	string savedPassword = f.ReadFile();

	string numPassword = extractIntegerWords(savedPassword);

	return numPassword.substr(0, 4);
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

	PasswordFile f(emailAddress, "rb");

	if (f.GetSize() > 0)
	{
		return true;
	}
	else
	{
		return false;
	}
}

int FileStorageAccessor::saveDSItems(const string& phoneNumber, const string& ipAddress)
{
	lock_guard<mutex> lock(mDSLock);

	DerectoryServiceFile f(phoneNumber, "wb");

	f.WriteFile(ipAddress);

	return 0;
}

map<string, string> FileStorageAccessor::getDSItems()
{
	lock_guard<mutex> lock(mDSLock);

	WIN32_FIND_DATA FindFileData;
	HANDLE hFind;
	wstring path = L"./storage/directoryservice/";

	map<string, string> DSMap;
	hFind = FindFirstFile(path.c_str(), &FindFileData);
	while (hFind != INVALID_HANDLE_VALUE)
	{
		wstring wfilename = FindFileData.cFileName;
		string phoneNumber(wfilename.begin(), wfilename.end());
		DerectoryServiceFile f(phoneNumber, "rb");
		
		string ipAddress = f.ReadFile();
		DSMap[phoneNumber] = ipAddress;

		if (!FindNextFile(hFind, &FindFileData))
			break;
	}
	FindClose(hFind);
	
	return DSMap;
}