#include "CryptoKey.h"
#include "Hislog.h"
#include "MD5Hash.h"
#include <chrono>
#include <sstream>

#define LOG_TAG "CRYPTO"

using namespace std;
CryptoKey::CryptoKey()
{
	mKeyMaker = make_unique<MD5>();
}

CryptoKey::~CryptoKey()
{
}

int CryptoKey::generateKey(
                    const string& sessionId,
                    const string& algorithm)
{
	time_t initValue = chrono::system_clock::to_time_t(chrono::system_clock::now());
	string strInitValue = sessionId + to_string(initValue);
	string hashKey = mKeyMaker->GetSecureData(strInitValue);

	vector<char> aesKey = {	-108, -110, -109,   -7, -33, 126,  75, 78,
							 110,  -25,  -40, -109, -12, 40,  -40, 96,
	};
	FUNC_LOGI("Get Crypto Key");

	if (hashKey.length() >= 32)
	{
		for (int i = 0; i < 16; i++)
		{
			stringstream ss;
			string num = hashKey.substr(2 * i, 2);
			ss << hex << num;
			int x;
			ss >> x;
			aesKey[i] = x;
		}
		mAESKeyMap.insert({ sessionId, aesKey});
		return 0;
	}
	else
	{
		return -1;
	}    
}

void CryptoKey::removeKey(
                    const string& sessionId)
{
	mAESKeyMap.erase(sessionId);
}

#include <iostream>
vector<char> CryptoKey::getKey(
                                const string sessionId)
{
	FUNC_LOGI("KeyRequest is entered : %s, key = ", sessionId.c_str());
	for (auto& i : mAESKeyMap[sessionId])
		printf("%d", i);
	std::cout << std::endl;
	return mAESKeyMap[sessionId];
}

