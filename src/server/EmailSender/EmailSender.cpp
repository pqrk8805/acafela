#include "pch.h"
#include <iostream>
#include <fstream>
#include <sstream>

int main(int argc, char** argv)
{
    std::ofstream outFile("mail.txt");
    outFile << "From: Acafela<eldian@naver.com>" << std::endl;
    outFile << "To: Customer <"
            << argv[1]
            << ">" << std::endl;
    outFile << "Subject: [Acafela] conference call invitation" << std::endl;
    outFile << std::endl;
    outFile << "Dear Customer," << std::endl;
	outFile << "You are invited to the conference call." << std::endl;
	outFile << "Room number : " << argv[2] << std::endl;
	outFile << "Date : " << argv[3] << " ~ " << argv[4] << std::endl;            
    outFile << std::endl << "BR" << std::endl;
    outFile.close();


	std::stringstream ss;
    ss << "curl -u eldian:dpfeldks=84" << ' '
       << "--ssl-reqd smtp://smtp.naver.com "
       << "--mail-from eldian@naver.com "
       << "--mail-rcpt "
	   << argv[1] << ' '
       << "--upload-file mail.txt";

	std::system(ss.str().c_str());

    return 0;
}
