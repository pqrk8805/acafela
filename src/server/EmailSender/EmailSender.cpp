#include "pch.h"
#include <iostream>
#include <fstream>
#include <sstream>

int main(int argc, char** argv)
{
    std::ofstream outFile("mail.txt");
    outFile << "From: Acafela<���̵�@naver.com>" << std::endl;
    outFile << "To: Customer <"
            << argv[1]
            << ">" << std::endl;
    outFile << "Subject: [Acafela] password recovery" << std::endl;
    outFile << std::endl;
    outFile << "Dear Customer," << std::endl;
    outFile << "Your new password is '"
            << argv[2]
            << "'" << std::endl;
    outFile << std::endl << "BR" << std::endl;
    outFile.close();


	std::stringstream ss;
    ss << "curl -u ���̵�:�н�����" << ' '
       << "--ssl-reqd smtp://smtp.naver.com "
       << "--mail-from ���̵�@naver.com "
       << "--mail-rcpt "
	   << argv[1] << ' '
       << "--upload-file mail.txt";

	std::system(ss.str().c_str());

    return 0;
}
