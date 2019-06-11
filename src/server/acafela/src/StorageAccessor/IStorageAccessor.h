#pragma once

#include <string>

class IStorageAccessor
{
protected:
	IStorageAccessor() {}
	IStorageAccessor(const IStorageAccessor&) = delete;
	IStorageAccessor& operator=(const IStorageAccessor&) = delete;
public:
	virtual ~IStorageAccessor() {}

	virtual std::string registerUser(
		const std::string& emailAddress,
		const std::string& password) = 0;

	virtual int changePassword(
		const std::string& emailAddress,
		const std::string& oldPassword,
		const std::string& newPassword) = 0;

	virtual int restorePassword(
		const std::string& emailAddress,
		const std::string& phoneNumber) = 0;

	//virtual int saveDSItems(const string& phoneNumber, const string& ipAddress) = 0;
	//virtual std::map<std::string, std::string> getDSItems() = 0;
private:
	static IStorageAccessor* mInst;
};