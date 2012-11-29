#include "CDiscCache.h"

// -----------------------sets /mnt/sdcard location specific to a device----------------------
void CDiscCache::SetStoragePath(const char* path)
{
    strcpy(mStoragePath, path);
    strcat(mStoragePath, STORAGE_DIR);
    LOGV("Setting storage to: %s", mStoragePath);
}

// -----------------------checks if file presents on disk----------------------
bool CDiscCache::Contains(int x, int y)
{
    UpdateFileFullPath(x, y);
    
   	FILE* file = 0;
    if (file = fopen(mFileFullPath, "r")) {
        fclose(file);
        return true;
    }
        
    return false;
}

// -----------------------writes cache file to disk----------------------
void CDiscCache::SaveToDisk(int x, int y, const unsigned char* data)
{
    // Create a directory if needed
    mkdir(mStoragePath, FILE_ACCESS_MODE);
    UpdateFileFullPath(x, y);
    
   	FILE* file = 0;
    if (file = fopen(mFileFullPath, "w"))
    {
        fputs((char*)data, file);
        fclose(file);
    } else
        LOGE("CDiscCache::SaveToDisk() - Error opening file to write: %s", mFileFullPath);
}

// -----------------------reads cache from disk----------------------
char* CDiscCache::ReadFromDisk(int x, int y)
{
   	FILE* file = 0;

    UpdateFileFullPath(x, y);
    
    if (file = fopen(mFileFullPath, "r"))    
    {
        fgets(mFileData, FILE_LENGTH, file);
        fclose(file);

        //LOGV("JNI Read BYTES: %d - %d", mFileData[123], mFileData[124]);

        return mFileData;
    }
    else
    {
        LOGE("CDiscCache::ReadFromDisk() - Error opening file to read: %s", mFileFullPath);
        return NULL;
    }        
}

// -----------------------prepares mFileFullPath for current file----------------------
void CDiscCache::UpdateFileFullPath(int x, int y)
{
    // Reseting mFileFullPath
    strcpy(mFileFullPath, mStoragePath);

    // Filling mFileFullPath with new filename
    char filename[CACHE_FILENAME_SIZE];
    sprintf(filename, CACHE_FILENAME_FORMAT, x, y);
    strcat(mFileFullPath, filename);

    LOGV("Updated file full path to: %s", mFileFullPath);
}