#include "UserAdmin.h"


UserAdmin::UserAdmin(
                IStorageAccessor& sa,
                IDirectoryService& ds)
  : mStorageAccessor(sa),
    mDirectoryService(ds)
{
}

UserAdmin::~UserAdmin()
{
}


std::vector<UserInfo> UserAdmin::getUserInfoList()
{
	return mStorageAccessor.getDSItems();
}

int UserAdmin::disableUser(
						const std::string& emailAddress,
						const std::string& phoneNumber)
{
	int err = mStorageAccessor.disableUser(emailAddress);
    if (!err) mDirectoryService.setEnable(phoneNumber, false);
    return err;
}

int UserAdmin::enableUser(
						const std::string& emailAddress,
						const std::string& phoneNumber)
{
	int err = mStorageAccessor.enableUser(emailAddress);
    if (!err) mDirectoryService.setEnable(phoneNumber, true);
    return err;
}

int UserAdmin::deleteUser(
						const std::string& emailAddress,
						const std::string& phoneNumber)
{
	return mStorageAccessor.deleteUser(emailAddress);
}
