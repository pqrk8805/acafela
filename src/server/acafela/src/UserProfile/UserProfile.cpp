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

string UserProfile::generateUserPhoneNumber()
{
	int totalUserNumber = mSA->getUserNumber();

	string userPhoneNumber = to_string(totalUserNumber + 1111);

	string fillZero(4 - userPhoneNumber.size(), '0');

	userPhoneNumber = fillZero + userPhoneNumber;

	totalUserNumber += 1;

	mSA->updateUserNumber(totalUserNumber);

	return userPhoneNumber;
}

std::string UserProfile::registerUser(
                        const std::string& emailAddress,
                        const std::string& password)
{
    FUNC_LOGD("BEGIN");	
	
	string encPassword = mSP->GetSecureData(password);

	bool isExistUser = mSA->isExistUser(emailAddress);

	if (isExistUser == false)
	{
		string genPhoneNumber = generateUserPhoneNumber();
		mSA->registerUser(emailAddress, encPassword, genPhoneNumber);
		return genPhoneNumber;
	}
	else
	{
		return "0000";
	}
}

int UserProfile::changePassword(
                        const std::string& emailAddress,
                        const std::string& oldPassword,
                        const std::string& newPassword)
{
    FUNC_LOGD("BEGIN");	
	
	string encOldPassword = mSP->GetSecureData(oldPassword);
	bool confirmedPassword = mSA->confirmPassword(emailAddress, encOldPassword);

	string encNewPassword = mSP->GetSecureData(newPassword);
	if (confirmedPassword == true)
		return mSA->changePassword(emailAddress, encNewPassword);
	else
		return -1;
}

 int UserProfile::restorePassword(
                        const std::string& emailAddress,
                        const std::string& phoneNumber)
 {
    FUNC_LOGD("BEGIN");
	

	bool confirmedPhoneNumber = mSA->confirmPhoneNumber(emailAddress, phoneNumber);
	if (confirmedPhoneNumber == true)
	{
		const string tempPassword = "0000";
		mSA->changePassword(emailAddress, tempPassword);

		EmailSender::sendPasswordRecoveryMail(emailAddress, tempPassword);
		return 0;
	}
	else
	{
		return -1;
	}
 }

