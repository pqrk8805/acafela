#pragma once

#include <string>

class EmailSenderCC
{
private:
	EmailSenderCC();

public:
	static void sendCCInvitation(
		const std::string& address,
		const std::string& roomNumber,
		const std::string& dataFrom,
		const std::string& dateTo);
};

