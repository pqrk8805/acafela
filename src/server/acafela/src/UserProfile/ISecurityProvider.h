#pragma once
#include <string>

class ISecurityProvider
{
public:
	virtual std::string GetSecureData(const std::string& data) = 0;
};