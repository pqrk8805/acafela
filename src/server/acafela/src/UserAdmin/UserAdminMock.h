#pragma once

#include <map>
#include "IUserAdmin.h"

class UserAdminMock : public IUserAdmin
{
private:
	std::map<std::string, UserInfo> mEntry;

public:
	UserAdminMock()
	  : mEntry {
		{"bartkim@gmail.com",		{"bartkim@gmail.com",	"1111", "", true} },
		{"isutar84@gmail.com",		{"isutar84@gmail.com",	"2222", "", true} },
		{"hakbun04@gmail.com",		{"hakbun04@gmail.com",	"3333", "", false} },
		{"jwleemailaddr@gmail.com", {"jwleemailaddr@gmail.com", "4444", "", true} },
		{"minuse80@gmail.com",		{"minuse80@gmail.com",	"4444", "", true} },
		{"rooky.lee@gmail.com",		{"rooky.lee@gmail.com", "4444", "", true} },
		{"pqrk8805@gmail.com",		{"pqrk8805@gmail.com",	"4444", "", false} },
	  }
	{
	}

	~UserAdminMock()
	{}

	std::vector<UserInfo> getUserInfoList() override
	{
		std::vector<UserInfo> list;
		for (const auto& kv : mEntry) {
			list.push_back(kv.second);
		}
		return list;
	}

	static int setEnable(
					std::map<std::string, UserInfo> entry,
					const std::string& email,
					bool enabled)
	{
		auto iter = entry.find(email);
		if (iter == entry.end()) {
			return -1;
		}
		iter->second.enabled = enabled;
		return 0;
	}

	int disableUser(
				const std::string& emailAddress) override
	{
		return setEnable(mEntry, emailAddress, false);
	}

	int enableUser(
				const std::string& emailAddress) override
	{
		return setEnable(mEntry, emailAddress, true);
	}
};
