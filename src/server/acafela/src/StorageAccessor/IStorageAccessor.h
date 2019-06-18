#pragma once

#include <string>
#include <map>

class IStorageAccessor
{
protected:
	IStorageAccessor() {}
	IStorageAccessor(const IStorageAccessor&) = delete;
	IStorageAccessor& operator=(const IStorageAccessor&) = delete;
public:
	virtual ~IStorageAccessor() {}

	virtual int registerUser(
		const std::string& emailAddress,
		const std::string& password,
		const std::string& phoneNumber) = 0;

	virtual bool confirmPassword(
		const std::string& emailAddress,
		const std::string& password) = 0;

	virtual int changePassword(
		const std::string& emailAddress,
		const std::string& password) = 0;

	virtual int confirmPhoneNumber(
		const std::string& emailAddress,
		const std::string& phoneNumber) = 0;

	virtual std::string getTempPassword(
		const std::string& emailAddress) = 0;

	virtual int getUserNumber() = 0;
	virtual int updateUserNumber(int userNumber) = 0;

	virtual bool isExistUser(const std::string& emailAddress) = 0;

	virtual int saveDSItems(const std::string& phoneNumber, const std::string& ipAddress) = 0;
	virtual std::map<std::string, std::string> getDSItems() = 0;
private:
	static IStorageAccessor* mInst;
};