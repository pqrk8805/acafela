#pragma once

#include "ICryptoKeyMgr.h"
#include "ICryptoKeyRepo.h"
#include <memory>
#include <vector>

class ISecurityProvider;
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
private:
	std::unique_ptr<ISecurityProvider>	mKeyMaker;
	std::vector<char>					mAESKey;
};
