#pragma once

#include <string>

class EmailSender
{
private:
    EmailSender();

public:
    static void sendPasswordRecoveryMail(
                                    const std::string& address,
                                    const std::string& newPassword);
};
