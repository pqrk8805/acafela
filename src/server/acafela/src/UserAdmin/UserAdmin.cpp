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
	return 0;
}

int UserAdmin::enableUser(
						const std::string& emailAddress)
{
	return 0;
}
