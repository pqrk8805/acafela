#pragma once

#include <map>
#include <mutex>
#include "IDirectoryService.h"

class DirectoryService : public IDirectoryService
{
private:
    std::map<std::string, std::string> mBook;
    std::mutex mLock;

public:
    DirectoryService();
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
