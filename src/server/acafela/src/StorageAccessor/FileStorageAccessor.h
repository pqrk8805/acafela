#pragma once

#include "IStorageAccessor.h"
#include <mutex>

class FileStorageAccessor : public IStorageAccessor
{
public:
	FileStorageAccessor();
	~FileStorageAccessor();

	int registerUser(
		const std::string& emailAddress,
		const std::string& password,
		const std::string& phoneNumber) override;

	int changePassword(
		const std::string& emailAddress,
		const std::string& password) override;

	bool confirmPassword(
		const std::string& emailAddress,
		const std::string& password) override;

	int confirmPhoneNumber(
		const std::string& emailAddress,
		const std::string& phoneNumber) override;

	std::string getTempPassword(
		const std::string& emailAddress) override;

	int getUserNumber() override;
	int updateUserNumber(int userNumber) override;

	bool isExistUser(const std::string& emailAddress) override;

	virtual int saveDSItems(
		const std::string& phoneNumber, 
		const std::string& ipAddress) override;
	virtual std::map<std::string, std::string> getDSItems() override;
private:
	std::mutex	mUserNumberLock;
	std::mutex	mPasswordLock;
	std::mutex	mPhoneNumberLock;
	std::mutex	mDSLock;
};