#include "UserProfile.h"
#include "Hislog.h"

#define LOG_TAG "UserProfile"


UserProfile::UserProfile()
{
}

UserProfile::~UserProfile()
{
}

std::string UserProfile::registerUser(
                        const std::string& emailAddress,
                        const std::string& password)
{
    FUNC_LOGD("BEGIN");
    return "";
}

int UserProfile::changePassword(
                        const std::string& emailAddress,
                        const std::string& oldPassword,
                        const std::string& newPassword)
{
    FUNC_LOGD("BEGIN");
    return -1;
}

 int UserProfile::restorePassword(
                        const std::string& emailAddress,
                        const std::string& phoneNumber)
 {
    FUNC_LOGD("BEGIN");
    return -1;
 }

