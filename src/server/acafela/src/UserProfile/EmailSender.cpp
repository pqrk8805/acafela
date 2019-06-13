#include "EmailSender.h"
#include <cstdlib>
#include <sstream>


EmailSender::EmailSender()
{
}

void EmailSender::sendPasswordRecoveryMail(
                                    const std::string& address,
                                    const std::string& newPassword)
{
    std::stringstream ss;
    ss << "EmailSender.exe " << address << ' ' << newPassword;
    
    std::system(ss.str().c_str());
}
