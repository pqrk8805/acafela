#pragma once

#include <string>
#include <vector>
#include "IStorageAccessor.h"

class IUserAdmin
{
protected:
        IUserAdmin() {}
        IUserAdmin(const IUserAdmin&) = delete;
        IUserAdmin& operator=(const IUserAdmin&) = delete;
public:
    virtual ~IUserAdmin() {}

    virtual std::vector<UserInfo> getUserInfoList() = 0;

	virtual int disableUser(
						const std::string& emailAddress,
                        const std::string& phoneNumber) = 0;

	virtual int enableUser(
						const std::string& emailAddress,
                        const std::string& phoneNumber) = 0;

	virtual int deleteUser(
						const std::string& emailAddress,
                        const std::string& phoneNumber) = 0;
};
