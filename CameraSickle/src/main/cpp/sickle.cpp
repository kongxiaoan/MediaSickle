#include <jni.h>
#include <string>
#include <opencv2/opencv.hpp>
#include <android/bitmap.h>
#include <android/log.h>

using namespace cv;
#define LOG_TAG "AndroidScalingalgorithmforimage"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
extern "C" JNIEXPORT jstring JNICALL
Java_com_media_sickle_NativeLib_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}


/**
 * 创建目标位图
 * @param env
 * @param new_width 目标位图 宽
 * @param new_height 高
 * @return 目标位图
 */
jobject createBitmap(JNIEnv *env, int new_width, int new_height) {
    jobject outputBitmap;
    jclass bitmapCls = env->FindClass("android/graphics/Bitmap");
    jmethodID createBitmapMethod = env->GetStaticMethodID(bitmapCls, "createBitmap",
                                                          "(IILandroid/graphics/Bitmap$Config;)Landroid/graphics/Bitmap;");
    jstring configName = env->NewStringUTF("ARGB_8888");
    jclass bitmapConfigClass = env->FindClass("android/graphics/Bitmap$Config");
    jobject java_bitmap_config = env->CallStaticObjectMethod(bitmapConfigClass,
                                                             env->GetStaticMethodID(
                                                                     bitmapConfigClass, "valueOf",
                                                                     "(Ljava/lang/String;)Landroid/graphics/Bitmap$Config;"),
                                                             configName);
    outputBitmap = env->CallStaticObjectMethod(bitmapCls,
                                               createBitmapMethod,
                                               new_width,
                                               new_height,
                                               java_bitmap_config);
    return outputBitmap;
}


extern "C"
JNIEXPORT jobject JNICALL
Java_com_media_sickle_NativeLib_scaleBitmapByBilinear(JNIEnv *env, jobject thiz,
                                                      jobject input_bitmap, jfloat scale) {
    if (scale == 1.0) {
        return input_bitmap;
    }
    // 获取源位图和目标位图的信息
    AndroidBitmapInfo bitmapInfo;
    void *pixels = nullptr;
    //将Android中的Bitmap 对象转为OpenCV 的Mat 对象
    if (AndroidBitmap_getInfo(env, input_bitmap, &bitmapInfo) < 0) {
        return NULL;
    }
    //获取原位图的像素
    if (AndroidBitmap_lockPixels(env, input_bitmap, &pixels) < 0) {
        return NULL;
    }
    //解锁Bitmap的像素数据
    AndroidBitmap_unlockPixels(env, input_bitmap);
    LOGD("height = %d, width = %d", bitmapInfo.height, bitmapInfo.width);
    //按比例计算目标图像的宽高
    int newWidth = static_cast<int>(bitmapInfo.width * scale);
    int newHeight = static_cast<int>(bitmapInfo.height * scale);
    LOGD("newHeight = %d, newWidth = %d", newHeight, newWidth);
    // 原位图像素数组
    uint32_t *srcPixels = static_cast<uint32_t *>(pixels);
    // 目标位图像素数组
    uint32_t *dstPixels = new uint32_t[newWidth * newHeight];
    //位图的宽度和新位图的宽度都是从1开始计数的，而像素索引是从0开始计数的。因此，要计算像素索引之间的比率，需要将宽度减一。
    float xRatio = static_cast<float>(bitmapInfo.width - 1) / static_cast<float>(newWidth - 1);
    float yRatio = static_cast<float>(bitmapInfo.height - 1) / static_cast<float>(newHeight - 1);
    for (int y = 0; y < newHeight; ++y) {
        for (int x = 0; x < newWidth; ++x) {
            float gx = x * xRatio;
            float gy = y * yRatio;
            int gxi = static_cast<int>(gx);
            int gyi = static_cast<int>(gy);
            float fracx = gx - gxi;
            float fracy = gy - gyi;

            uint32_t c00 = srcPixels[gyi * bitmapInfo.width + gxi];
            uint32_t c10 = srcPixels[gyi * bitmapInfo.width + gxi + 1];
            uint32_t c01 = srcPixels[(gyi + 1) * bitmapInfo.width + gxi];
            uint32_t c11 = srcPixels[(gyi + 1) * bitmapInfo.width + gxi + 1];

            int r = static_cast<int>((1 - fracx) * (1 - fracy) * (c00 >> 16 & 0xff) +
                                     fracx * (1 - fracy) * (c10 >> 16 & 0xff) +
                                     (1 - fracx) * fracy * (c01 >> 16 & 0xff) +
                                     fracx * fracy * (c11 >> 16 & 0xff));

            int g = static_cast<int>((1 - fracx) * (1 - fracy) * (c00 >> 8 & 0xff) +
                                     fracx * (1 - fracy) * (c10 >> 8 & 0xff) +
                                     (1 - fracx) * fracy * (c01 >> 8 & 0xff) +
                                     fracx * fracy * (c11 >> 8 & 0xff));

            int b = static_cast<int>((1 - fracx) * (1 - fracy) * (c00 & 0xff) +
                                     fracx * (1 - fracy) * (c10 & 0xff) +
                                     (1 - fracx) * fracy * (c01 & 0xff) +
                                     fracx * fracy * (c11 & 0xff));
            dstPixels[y * newWidth + x] = 0xff000000 | (r << 16) | (g << 8) | b;
        }
    }
    jobject newBitmap = createBitmap(env, newWidth, newHeight);
    LOGD("将OpenCV的Mat对象转换成Java中的Bitmap对象完成");
    void *outputPixels;
    int result = AndroidBitmap_lockPixels(env, newBitmap, &outputPixels);
    if (result < 0) {
        LOGD("outputPixels执行失败 %d", result);
        return NULL;
    }
    LOGD("outputPixels执行完成");
    memcpy(outputPixels, dstPixels, newWidth * newHeight * 4);
    AndroidBitmap_unlockPixels(env, newBitmap);
    LOGD("执行完成");
    return newBitmap;
}