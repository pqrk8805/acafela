#include "Hislog.h"

#include <stdio.h>
#include <mutex>
#include <iostream>
#include <fstream>
#include <sstream>
#include <atlstr.h>
#include <tlhelp32.h>
#include <time.h>
#include <Windows.h>


#define LOG_FMT_LEVEL       "%c"
#define LOG_FMT_TIMESTAMP   "%d-%02d-%02d %02d:%02d:%02d.%03d"
#define LOG_FMT_PROCESS     "%6d"
#define LOG_FMT_THREAD      "%6d"
#define LOG_FMT_TAG         "%-10s:"
#define LOG_FMT_FUNC        "%s()"

#define LOG_PRE_FMT                 \
            LOG_FMT_TIMESTAMP       \
            " " LOG_FMT_PROCESS     \
            " " LOG_FMT_THREAD      \
            " " LOG_FMT_LEVEL       \
            " " LOG_FMT_TAG

#define COLOR_LOG

static const int PROCESS_ID = ::GetCurrentProcessId();
static LogLevel sLogLevel = V;

#ifdef COLOR_LOG
static std::mutex sConsolColorLock;
#endif


static
int vasprintf(char** strp, const char* fmt, va_list ap)
{
    // _vscprintf tells you how big the buffer needs to be
    int len = _vscprintf(fmt, ap);
    if (len == -1) {
        return -1;
    }
    size_t size = (size_t)len + 1;
    char *str = (char*)malloc(size);
    if (!str) {
        return -1;
    }
    // _vsprintf_s is the "secure" version of vsprintf
    int r = vsnprintf(str, len + 1, fmt, ap);
    if (r == -1) {
        free(str);
        return -1;
    }
    *strp = str;
    return r;
}

static
int asprintf(char** strp, const char* fmt, ...)
{
    va_list ap;
    va_start(ap, fmt);
    int r = vasprintf(strp, fmt, ap);
    va_end(ap);
    return r;
}

static inline
char toChar(LogLevel level)
{
    return "VDIWE"[level];
}

static const WORD LOG_COLOR[] = {
    FOREGROUND_INTENSITY,
    FOREGROUND_GREEN,
    FOREGROUND_BLUE | FOREGROUND_GREEN,
    FOREGROUND_GREEN | FOREGROUND_RED,
    FOREGROUND_RED,
};

static
void getTimestamp(
                tm& tt,                 // out
                unsigned int& milliSec) // out
{
    FILETIME ft;
    ::GetSystemTimeAsFileTime(&ft);
    time_t now = ft.dwHighDateTime;
    now <<= 32;
    now |= ft.dwLowDateTime;
    now /= 10;
    now -= 11644473600000000Ui64;
    now /= 1000;
    auto time = now / 1000;
    milliSec = now % 1000;

    if (localtime_s(&tt, &time)) {
        memset(&tt, 0, sizeof(tt));
    }
}

static
void writeLogToConsol(
                    LogLevel level,
                    const char* log)
{
#ifdef COLOR_LOG
    sConsolColorLock.lock();
    HANDLE hStd = ::GetStdHandle(STD_OUTPUT_HANDLE);
    CONSOLE_SCREEN_BUFFER_INFO oldInfo;
    ::GetConsoleScreenBufferInfo(hStd, &oldInfo);
    ::SetConsoleTextAttribute(hStd, LOG_COLOR[static_cast<int>(level)]);
#endif

    std::cout << log;
    std::cout.flush();

#ifdef COLOR_LOG
    ::SetConsoleTextAttribute(hStd, oldInfo.wAttributes);
    sConsolColorLock.unlock();
#endif
}


static
void writeLog(
        LogLevel level, const char* log)
{
    if (level < sLogLevel) {
        return;
    }

    writeLogToConsol(level, log);
}

extern
void hislogRaw(
        LogLevel level, const char* tag, const char* log)
{
    hislog(level, tag, log);
}



extern
void hislog(
        LogLevel level, const char* tag, const char* fmt, ...)
{
    tm tt;
    unsigned int milliSec;
    getTimestamp(tt, milliSec);

    char* logInfo {nullptr};
    asprintf(
            &logInfo,
            LOG_PRE_FMT " ",
            tt.tm_year + 1900, tt.tm_mon + 1, tt.tm_mday,
            tt.tm_hour, tt.tm_min, tt.tm_sec, milliSec,
            PROCESS_ID,
            ::GetCurrentThreadId(),
            toChar(level),
            tag);

    va_list ap;
    va_start(ap, fmt);
    char* logContent {nullptr};
    int length = vasprintf(&logContent, fmt, ap);
    va_end(ap);

    std::stringstream ss;
    ss << logInfo << logContent << std::endl;
    writeLog(level, ss.str().c_str());

    free(logInfo);
    free(logContent);
}

extern
void setLogLevel(
                LogLevel level)
{
    sLogLevel = level;
}

