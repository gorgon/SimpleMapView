#ifndef _CUTILS_H
#define _CUTILS_H

#include <stdbool.h>
#include <string.h>
#include <android/log.h>
#include <stdio.h>
#include <stdlib.h>

static const int MAX_FILENAME_LENGTH = 256;
static const int FILE_LENGTH = 256 * 256 * 4;
static const int FILE_ACCESS_MODE = 0565;
static const char* STORAGE_DIR = "/SimpleMapView";

#define LOG_TAG "Simple_MapView"
#define LOGV(...) __android_log_print(ANDROID_LOG_VERBOSE, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

#endif // _CUTILS_H