#pragma once


enum LogLevel
{
    V = 0,  // VERBOSE
    D,      // DEBUG
    I,      // INFO
    W,      // WARNING
    E,      // ERROR
};


#ifdef __cplusplus
extern "C" {
#endif

extern void hislogRaw(
                    LogLevel level,
                    const char* tag,
                    const char* log);

extern void hislog(
                    LogLevel level,
                    const char* tag,
                    const char* fmt,
                    ...);

extern void setLogLevel(
                    LogLevel level);

#ifdef __cplusplus
}
#endif


#define FUNC_HISLOG(level, tag, fmt, ...)                               \
    do {                                                                \
        hislog(level, tag, "%s() " fmt, __FUNCTION__, ##__VA_ARGS__);   \
    } while(false);


#define LOGI(fmt, ...)              hislog(I, LOG_TAG, fmt, ##__VA_ARGS__)
#define LOGV(fmt, ...)              hislog(V, LOG_TAG, fmt, ##__VA_ARGS__)
#define LOGD(fmt, ...)              hislog(D, LOG_TAG, fmt, ##__VA_ARGS__)
#define LOGW(fmt, ...)              hislog(W, LOG_TAG, fmt, ##__VA_ARGS__)
#define LOGE(fmt, ...)              hislog(E, LOG_TAG, fmt, ##__VA_ARGS__)

#define FUNC_LOGI(fmt, ...)         FUNC_HISLOG(I, LOG_TAG, fmt, ##__VA_ARGS__)
#define FUNC_LOGV(fmt, ...)         FUNC_HISLOG(V, LOG_TAG, fmt, ##__VA_ARGS__)
#define FUNC_LOGD(fmt, ...)         FUNC_HISLOG(D, LOG_TAG, fmt, ##__VA_ARGS__)
#define FUNC_LOGW(fmt, ...)         FUNC_HISLOG(W, LOG_TAG, fmt, ##__VA_ARGS__)
#define FUNC_LOGE(fmt, ...)         FUNC_HISLOG(E, LOG_TAG, fmt, ##__VA_ARGS__)
