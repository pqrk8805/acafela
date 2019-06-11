#pragma once

#include "IStorageAccessor.h"
#include <mutex>

class FileStorageAccessor : public IStorageAccessor
{
public:
	FileStorageAccessor();
	~FileStorageAccessor();

	std::string registerUser(
		const std::string& emailAddress,
		const std::string& password) override;

	int changePassword(
		const std::string& emailAddress,
		const std::string& oldPassword,
		const std::string& newPassword) override;

	bool confirmUser(
		const std::string& emailAddress,
		const std::string& password);

	int restorePassword(
		const std::string& emailAddress,
		const std::string& phoneNumber) override;

	int deleteUser(const std::string& emailAddress);

	void getTotalUserNumber();
	void updateUserNumber();	

	std::string generateUserPhoneNumber();
private:
	int			mUserNumber = 0;
	std::mutex	mUserNumberLock;
	std::mutex	mUserProfileLock;
};