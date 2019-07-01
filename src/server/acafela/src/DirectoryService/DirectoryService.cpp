#include "DirectoryService.h"
#include "Hislog.h"
#include "Communicator/communicator.h"

#define LOG_TAG "DirService"

DirectoryService::DirectoryService(IStorageAccessor& sa)
  : mStorageAccessor(sa)
{
	for (const auto& item : sa.getDSItems()) {
		mBook.emplace(item.phoneNumber, item);
        ParticipantDirectory().notify_update(
                                        item.phoneNumber,
                                        item.enabled ? item.ipAddress : "");
	}
}

DirectoryService::~DirectoryService()
{
}

int DirectoryService::update(
                        const std::string& phoneNumber,
                        const std::string& password,
                        const std::string& ipAddress)
{
    FUNC_LOGI("%s : %s", phoneNumber.c_str(), ipAddress.c_str());
#if 0
    int err = mStorageAccessor.confirmPhoneNumberPassword(
                                                    phoneNumber,
                                                    password);
    if (err) {
        FUNC_LOGE("ERROR: Password mismatched.");
        return -999;
    }

    std::lock_guard<std::mutex> guard(mLock);
    auto iter = mBook.find(phoneNumber);
    if (iter == mBook.end()) {
        FUNC_LOGE("ERROR: There is no entry: %s", phoneNumber.c_str());
        return -998;
    } else {
        UserInfo& info = iter->second;
        info.ipAddress = ipAddress;

        mStorageAccessor.saveDSItem(info);
        ParticipantDirectory().notify_update(
                                        phoneNumber,
                                        info.enabled ? ipAddress : "");
    }
#endif
	if (phoneNumber.empty() || phoneNumber.compare("") == 0 || phoneNumber.compare("0000") == 0)
		return 0;
	UserInfo info = { "", phoneNumber, ipAddress, true };
	mStorageAccessor.saveDSItem(info);
	ParticipantDirectory().notify_update(
		phoneNumber,
		info.enabled ? ipAddress : "");
    return 0;
}

int DirectoryService::query(
                        const std::string& phoneNumber,
                        std::string* ipAddress)
{
    std::lock_guard<std::mutex> guard(mLock);
    const auto iter = mBook.find(phoneNumber);
    if (iter == mBook.cend())
        return -1;
    if (!iter->second.enabled)
        return -2;
    if (iter->second.ipAddress.empty())
        return -3;
    *ipAddress = iter->second.ipAddress;
    return 0;
}

int DirectoryService::remove(
                        const std::string& phoneNumber)
{
    std::lock_guard<std::mutex> guard(mLock);
	ParticipantDirectory().notify_remove(phoneNumber);

    return mBook.erase(phoneNumber) ? 0 : -1;
}

int DirectoryService::setEnable(
                        const std::string& phoneNumber,
                        bool enable)
{
    std::lock_guard<std::mutex> guard(mLock);
    auto iter = mBook.find(phoneNumber);
    if (iter != mBook.end()) {
        UserInfo& info = iter->second;
        info.enabled = enable;
        ParticipantDirectory().notify_update(
                                        phoneNumber,
                                        enable ? info.ipAddress : "");
    }
    return 0;
}

