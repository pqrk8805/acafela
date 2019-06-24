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

	bool confirmPhoneNumber(
		const std::string& emailAddress,
		const std::string& phoneNumber) override;

	int confirmPhoneNumberPassword(
		const std::string& phoneNumber,
		const std::string& password) override;

	std::string getTempPassword(
		const std::string& emailAddress) override;

	int deleteUser(const std::string& emailAddress) override;
	
	int getUserNumber() override;
	int updateUserNumber(int userNumber) override;

	bool isExistUser(const std::string& emailAddress) override;

	int saveDSItem(UserInfo& userInfo) override;
	std::vector<UserInfo> getDSItems() override;
	int disableUser(const std::string& emailAddress) override;
	int enableUser(const std::string& emailAddress) override;

	int saveCCItem( const std::string& roomNumber,
					const std::string& dateFrom,
					const std::string& dateTo,
					const std::vector<std::string>& phoneNumberList) override;

	bool confirmCCUser( const std::string& roomNumber, 
						const std::string& phoneNumber) override;
private:
	std::string FindEmailAddress(const std::string& phoneNumber);

	int getDSItem(const std::string& emailAddress, UserInfo& ui);
private:
	std::mutex	mUserNumberLock;
	std::mutex	mPasswordLock;
	std::mutex	mPhoneNumberLock;
	std::mutex	mDSLock;
	std::mutex	mCCLock;
};