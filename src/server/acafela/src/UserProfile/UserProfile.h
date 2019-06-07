#pragma once

#include "IUserProfile.h"
#include <memory>

class IStorageAccessor;
class UserProfile : public IUserProfile
{
public:
    UserProfile();
    ~UserProfile();


    std::string registerUser(
                const std::string& emailAddress,
                const std::string& password) override;

    int changePassword(
                const std::string& emailAddress,
                const std::string& oldPassword,
                const std::string& newPassword) override;

    int restorePassword(
                const std::string& emailAddress,
                const std::string& phoneNumber) override;

private:
	std::unique_ptr<IStorageAccessor> mSA;
};
