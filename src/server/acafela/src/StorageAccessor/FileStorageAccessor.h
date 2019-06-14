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

	int restorePassword(
		const std::string& emailAddress,
		const std::string& phoneNumber) override;

	int confirmPhoneNumber(
		const std::string& emailAddress,
		const std::string& phoneNumber) override;

	int getUserNumber() override;
	int updateUserNumber(int userNumber) override;

	bool isExistUser(const std::string& emailAddress) override;
private:
	std::mutex	mUserNumberLock;
	std::mutex	mPasswordLock;
	std::mutex	mPhoneNumberLock;
};