#ifndef _CMAINJNI_H
#define _CMAINJNI_H

#include <jni.h>
#include "CUtils.h"

#ifdef __cplusplus
extern "C" {
#endif

void Java_com_gorgon_mapview_cache_DiscCache_setStoragePath(JNIEnv* jenv, jobject thiz, jstring path);

jboolean Java_com_gorgon_mapview_cache_DiscCache_contains(JNIEnv* jenv, jobject thiz, jint x, jint y);

void Java_com_gorgon_mapview_cache_DiscCache_save(JNIEnv* jenv, jobject thiz, jint x, jint y, jbyteArray data);

jbyteArray Java_com_gorgon_mapview_cache_DiscCache_read(JNIEnv* jenv, jobject thiz, jint x, jint y);

#ifdef __cplusplus
}
#endif

#endif // _CMAINJNI_H