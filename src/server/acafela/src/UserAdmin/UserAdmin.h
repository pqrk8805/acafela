#pragma once

#include "IUserAdmin.h"
#include "IStorageAccessor.h"
#include "IDirectoryService.h"


class UserAdmin : public IUserAdmin
{
private:
	IStorageAccessor& mStorageAccessor;
    IDirectoryService& mDirectoryService;

public:
	UserAdmin(
            IStorageAccessor& sa,
            IDirectoryService& ds);
	~UserAdmin();

	std::vector<UserInfo> getUserInfoList() override;
	int disableUser(
				const std::string& emailAddress,
				const std::string& phoneNumber) override;
	int enableUser(
				const std::string& emailAddress,
				const std::string& phoneNumber) override;
	int deleteUser(
				const std::string& emailAddress,
				const std::string& phoneNumber) override;
};
