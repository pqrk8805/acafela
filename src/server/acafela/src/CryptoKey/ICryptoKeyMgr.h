#pragma once

#include <string>

class ICryptoKeyMgr
{
protected:
        ICryptoKeyMgr() {}
        ICryptoKeyMgr(const ICryptoKeyMgr&) = delete;
        ICryptoKeyMgr& operator=(const ICryptoKeyMgr&) = delete;
public:
    virtual ~ICryptoKeyMgr() {}

    virtual int generateKey(
                        const std::string& sessionId,
                        const std::string& algorithm = "AES") = 0;
    virtual void removeKey(
                        const std::string& sessionId) = 0;
};

