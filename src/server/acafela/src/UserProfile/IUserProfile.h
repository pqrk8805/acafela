#pragma once

#include <string>


class IUserProfile
{
protected:
        IUserProfile() {}
        IUserProfile(const IUserProfile&) = delete;
        IUserProfile& operator=(const IUserProfile&) = delete;
public:
    virtual ~IUserProfile() {}

    /**
     * @returns      new Phone number
     */
    virtual std::string registerUser(
                        const std::string& emailAddress,
                        const std::string& password) = 0;

    virtual int changePassword(
                        const std::string& emailAddress,
                        const std::string& oldPassword,
                        const std::string& newPassword) = 0;

    virtual int restorePassword(
                        const std::string& emailAddress,
                        const std::string& phoneNumber) = 0;

};

