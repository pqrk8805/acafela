#include "FileStorageAccessor.h"
#include <sys/stat.h>
#include <sys/types.h>
#include <fstream>

using namespace std;

class SmartFile
{
public:
	SmartFile() {}
	SmartFile(const string& filename, const string& access_mode)
	{
		string binfile = filename + ".bin";

		ifstream file;
		file.open(binfile.c_str(), ios::in | ios::binary);		
		if (file.is_open())
		{
			file.seekg(0, std::ios_base::end);
			mSize = file.tellg();
		}
		file.close();

		fopen_s(&mFile, binfile.c_str(), access_mode.c_str());
	}
	~SmartFile()
	{
		FileClose();
	}
	FILE* GetPF() const { return mFile; }
	int GetSize() { return mSize; }
	void FileClose()
	{
		if (mFile != nullptr)
			fclose(mFile);
	}
private:
	FILE* mFile = nullptr;
	int mSize = -1;
};

#include "MD5Encryption.h"
string GetHashData(const string& data)
{
	//std::string data = "Hello World";
	std::string data_hex_digest;

	MD5 hash;
	hash.update(data.begin(), data.end());
	hash.hex_digest(data_hex_digest);

	return data_hex_digest;
}

string FileStorageAccessor::registerUser(const string& emailAddress, const string& password)
{
	lock_guard<mutex> lock(mFileLock);

	string encEmailAddress = GetHashData(emailAddress);
	SmartFile f(encEmailAddress.c_str(), "wb");

	string encPassword = GetHashData(password);
	fwrite(encPassword.c_str(), encPassword.length(), 1, f.GetPF());

	return string("TBD");
}

int FileStorageAccessor::changePassword(const string& emailAddress, const string& oldPassword, const string& newPassword)
{
	if (confirmUser(emailAddress, oldPassword) == false)
	{
		return -1;
	}
	
	lock_guard<mutex> lock(mFileLock);	
	string encEmailAddress = GetHashData(emailAddress);
	SmartFile f(encEmailAddress.c_str(), "wb");

	string encPassword = GetHashData(newPassword);
	fwrite(encPassword.c_str(), encPassword.length(), 1, f.GetPF());

	return -1;
}

int FileStorageAccessor::restorePassword(const string& emailAddress, const string& phoneNumber)
{	
	lock_guard<mutex> lock(mFileLock);
	SmartFile f(emailAddress.c_str(), "rb");

	return -1;
}

bool FileStorageAccessor::confirmUser(const std::string& emailAddress, const std::string& password)
{
	lock_guard<mutex> lock(mFileLock);

	string encEmailAddress = GetHashData(emailAddress);
	SmartFile f(encEmailAddress.c_str(), "rb");

	if (f.GetPF() == nullptr)
	{
		return false;
	}

	string savedPassword(80, '\0');
	fread( &savedPassword[0], f.GetSize(), 1, f.GetPF());

	string encPassword = GetHashData(password);
	if (encPassword.compare(0, encPassword.length(), savedPassword, 0, encPassword.length()) == 0)
		return true;
	else
		return false;
}