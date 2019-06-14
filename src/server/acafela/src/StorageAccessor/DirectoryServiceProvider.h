#pragma once
#include <string>
#include <map>
#include <mutex>
class DirectoryServiceProvider
{
public:
	int saveDSItems(const std::string& phoneNumber, const std::string& ipAddress);
	const std::map<std::string, std::string>& getDSItems();
private:
	std::map<std::string, std::string>	mDSStorage;
	std::mutex							mDSMutex;
};