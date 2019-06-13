#pragma once

#include <string>

class IDirectoryService
{
protected:
        IDirectoryService() {}
        IDirectoryService(const IDirectoryService&) = delete;
        IDirectoryService& operator=(const IDirectoryService&) = delete;
public:
    virtual ~IDirectoryService() {}

    virtual int update(
                    const std::string& phoneNumber,
                    const std::string& password,
                    const std::string& ipAddress) = 0;
    virtual int query(
                    const std::string& phoneNumber,
                    std::string* ipAddrss) = 0;
    virtual int remove(
                    const std::string& phoneNumber) = 0;
};
