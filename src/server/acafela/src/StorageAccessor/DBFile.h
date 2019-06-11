#pragma once
#include <string>

class DBFile
{
public:
	DBFile() {}
	DBFile(const std::string& filename, const std::string& access_mode);
	~DBFile();
	FILE* GetPF() const { return mFile; }
	int GetSize() const { return mSize; }
	void OpenFile(const std::string& filename, const std::string& access_mode);
	void CloseFile();
	void WriteFile(const std::string& data);
	std::string ReadFile() const;
private:
	FILE* mFile = nullptr;
	int mSize = -1;
};

class PasswordFile : public DBFile
{
public:
	PasswordFile(const std::string& filename, const std::string& access_mode) :
		DBFile("./pw/" + filename, access_mode)
	{
	}
};

class DerectoryServiceFile : public DBFile
{
public:
	DerectoryServiceFile(const std::string& filename, const std::string& access_mode) :
		DBFile("./ds/" + filename, access_mode)
	{
	}
};
