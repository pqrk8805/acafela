#pragma once

#include "IUserAdmin.h"
#include "IStorageAccessor.h"


class UserAdmin : public IUserAdmin
{
private:
	IStorageAccessor& mStorageAccessor;

public:
	UserAdmin(IStorageAccessor& sa);
	~UserAdmin();

	std::vector<UserInfo> getUserInfoList() override;
	int disableUser(
				const std::string& emailAddress) override;
	int enableUser(
				const std::string& emailAddress) override;
	int deleteUser(
				const std::string& emailAddress) override;
};
