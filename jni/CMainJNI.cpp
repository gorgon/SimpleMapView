#include <jni.h>
#include "CMainJNI.h"
#include "CDiscCache.h"
#include <string.h>

CDiscCache file;

// -----------------------specifies "/sdcard" path on the device----------------------
void Java_com_gorgon_mapview_cache_DiscCache_setStoragePath(JNIEnv* jenv, jobject thiz, jstring path)
{
  const char *pstr = (const char *)jenv->GetStringUTFChars(path, 0); 
  
    if (!pstr) 
    {
        LOGE("setStoragePath() - incorrect path");
        return;
    }

    file.SetStoragePath(pstr);

    jenv->ReleaseStringUTFChars(path, pstr); 
}

// -----------------------checks if file presents on disk----------------------
jboolean Java_com_gorgon_mapview_cache_DiscCache_contains(JNIEnv* jenv, jobject thiz, jint x, jint y)
{
    return file.Contains(x, y);
}

// -----------------------saves data on disk----------------------
void Java_com_gorgon_mapview_cache_DiscCache_save(JNIEnv* jenv, jobject thiz, jint x, jint y, jbyteArray data)
{
    unsigned char* charData = (unsigned char*) jenv->GetByteArrayElements(data, JNI_FALSE);
    file.SaveToDisk(x, y, charData);
    jenv->ReleaseByteArrayElements(data, (jbyte*)charData, JNI_FALSE); 
}

// -----------------------reads tile cache from disk----------------------
jbyteArray Java_com_gorgon_mapview_cache_DiscCache_read(JNIEnv* jenv, jobject thiz, jint x, jint y)
{
    char* charData = file.ReadFromDisk(x, y);
    
    if(charData == NULL)
        return NULL;
    else
    {    
        jbyteArray result = jenv->NewByteArray(FILE_LENGTH);
        jenv->SetByteArrayRegion(result, 0, FILE_LENGTH, (jbyte*)charData);     
        return result;
    }
}
