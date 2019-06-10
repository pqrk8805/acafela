#pragma once

#include "IStorageAccessor.h"
#include <mutex>

class FileStorageAccessor : public IStorageAccessor
{
public:
	FileStorageAccessor() {}

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
private:
	std::mutex			mFileLock;
};