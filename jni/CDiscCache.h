#ifndef _CDISCCACHE_H
#define _CDISCCACHE_H

#include "CUtils.h"
#include <dirent.h>
#include <sys/stat.h>

static const char* CACHE_FILENAME_FORMAT = "/CachedTile_%d_%d.raw";
static const int CACHE_FILENAME_SIZE = 22; // length of CACHE_FILENAME_FORMAT + 6 characters for X, Y tiles indexes

class CDiscCache {
    private:
        char mStoragePath[MAX_FILENAME_LENGTH];
        char mFileFullPath[MAX_FILENAME_LENGTH];
        char mFileData[FILE_LENGTH];
        
        void UpdateFileFullPath(int x, int y);
        
    public:
        void SetStoragePath(const char* path);
        bool Contains(int x, int y);
        void SaveToDisk(int x, int y, const unsigned char* data);
        char* ReadFromDisk(int x, int y);
};

#endif // _CDISCCACHE_H