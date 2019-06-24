#include "EmailSenderCC.h"
#include <cstdlib>
#include <sstream>


EmailSenderCC::EmailSenderCC()
{
}

void EmailSenderCC::sendCCInvitation(
	const std::string& address,
	const std::string& roomNumber,
	const std::string& dataFrom,
	const std::string& dateTo)
{
	std::stringstream ss;
	ss << "EmailSenderCC.exe " << address << ' ' << roomNumber << ' ' << dataFrom << ' ' << dateTo;

	std::system(ss.str().c_str());
}
