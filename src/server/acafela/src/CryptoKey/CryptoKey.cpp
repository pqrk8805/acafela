#include "CryptoKey.h"

 CryptoKey::CryptoKey()
 {
 }

CryptoKey::~CryptoKey()
{
}

int CryptoKey::generateKey(
                    const std::string& sessionId,
                    const std::string& algorithm)
{
    return -1;
}

void CryptoKey::removeKey(
                    const std::string& sessionId)
{
}

std::vector<char> CryptoKey::getKey(
                                const std::string sessionId)
{
    // temp
    //
    static const std::vector<char> aesKey {
                            -108, -110, -109,   -7, -33, 126,  75, 78,
                             110,  -25,  -40, -109, -12, 40,  -40, 96,
    };
    return aesKey;
}

