#include "UserProfile.h"
#include "Hislog.h"
#include "StorageAccessor/FileStorageAccessor.h"
#define LOG_TAG "UserProfile"

using namespace std;
UserProfile::UserProfile()
{
	mSA = make_unique<FileStorageAccessor>();
}

UserProfile::~UserProfile()
{
}

std::string UserProfile::registerUser(
                        const std::string& emailAddress,
                        const std::string& password)
{
    FUNC_LOGD("BEGIN");	
    return mSA->registerUser(emailAddress, password);
}

int UserProfile::changePassword(
                        const std::string& emailAddress,
                        const std::string& oldPassword,
                        const std::string& newPassword)
{
    FUNC_LOGD("BEGIN");	
    return mSA->changePassword(emailAddress, oldPassword, newPassword);
}

 int UserProfile::restorePassword(
                        const std::string& emailAddress,
                        const std::string& phoneNumber)
 {
    FUNC_LOGD("BEGIN");
	return restorePassword(emailAddress, phoneNumber);
 }

