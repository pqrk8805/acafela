#include "UserProfile.h"
#include "EmailSender.h"
#include "Hislog.h"
#include "StorageAccessor/FileStorageAccessor.h"
#include "MD5Hash.h"

#define LOG_TAG "UserProfile"

using namespace std;
UserProfile::UserProfile()
{
	mSA = make_unique<FileStorageAccessor>();
	mSP = make_unique<MD5>();
}

UserProfile::~UserProfile()
{
}

std::string UserProfile::registerUser(
                        const std::string& emailAddress,
                        const std::string& password)
{
    FUNC_LOGD("BEGIN");	
	
	string encEmailAddress = mSP->GetSecureData(emailAddress);
	encEmailAddress = mSP->GetSecureData(emailAddress);
	encEmailAddress = mSP->GetSecureData(emailAddress);
	encEmailAddress = mSP->GetSecureData(emailAddress);
	string encPassword = mSP->GetSecureData(password);
    return mSA->registerUser(encEmailAddress, encPassword);
}

int UserProfile::changePassword(
                        const std::string& emailAddress,
                        const std::string& oldPassword,
                        const std::string& newPassword)
{
    FUNC_LOGD("BEGIN");	
	
	string encEmailAddress = mSP->GetSecureData(emailAddress);
	string encOldPassword = mSP->GetSecureData(oldPassword);
	string encNewPassword = mSP->GetSecureData(newPassword);
    return mSA->changePassword(encEmailAddress, encOldPassword, encNewPassword);
}

 int UserProfile::restorePassword(
                        const std::string& emailAddress,
                        const std::string& phoneNumber)
 {
    FUNC_LOGD("BEGIN");
	
	string encEmailAddress = mSP->GetSecureData(emailAddress);	
#if 0
    EmailSender::sendPasswordRecoveryMail(
                                    emailAddress,
                                    "new password");
#endif
	return restorePassword(encEmailAddress, phoneNumber);
 }

