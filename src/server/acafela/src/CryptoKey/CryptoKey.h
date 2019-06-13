#pragma once

#include "ICryptoKeyMgr.h"
#include "ICryptoKeyRepo.h"

class CryptoKey : public ICryptoKeyMgr,
                  public ICryptoKeyRepo
{
public:
    CryptoKey();
    ~CryptoKey();

    int generateKey(
                const std::string& sessionId,
                const std::string& algorithm = "AES") override;
    void removeKey(
                const std::string& sessionId) override;

    virtual std::vector<char> getKey(
                                const std::string sessionId) override;
};
