#include "UserAdmin.h"


UserAdmin::UserAdmin(IStorageAccessor& sa)
  : mStorageAccessor(sa)
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
						const std::string& emailAddress)
{
	return mStorageAccessor.disableUser(emailAddress);
}

int UserAdmin::enableUser(
						const std::string& emailAddress)
{
	return mStorageAccessor.enableUser(emailAddress);
}

int UserAdmin::deleteUser(
						const std::string& emailAddress)
{
	return mStorageAccessor.deleteUser(emailAddress);
}
