#pragma once

#include <map>
#include <mutex>
#include <memory>
#include "IDirectoryService.h"

class IStorageAccessor;
class DirectoryService : public IDirectoryService
{
private:
    std::map<std::string, std::string> mBook;
    std::mutex mLock;
	IStorageAccessor& mStorageAccessor;

public:
    DirectoryService(IStorageAccessor& sa);
    ~DirectoryService();

    int update(
            const std::string& phoneNumber,
            const std::string& password,
            const std::string& ipAddress) override;
    int query(
            const std::string& phoneNumber,
            std::string* ipAddress) override;
    int remove(
            const std::string& phoneNumber) override;
};
