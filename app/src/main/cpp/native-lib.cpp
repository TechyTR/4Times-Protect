#include <jni.h>
#include <unistd.h>
#include <cerrno>
#include <string>
#include <algorithm>
#include <cctype>

namespace {
constexpr jint ELF = 1 << 0;
constexpr jint PE = 1 << 1;
constexpr jint DEX = 1 << 2;
constexpr jint ZIP = 1 << 3;
constexpr jint SCRIPT = 1 << 4;
constexpr jint EXTENSION_MISMATCH = 1 << 5;
constexpr jint EMPTY = 1 << 6;

std::string lowerAscii(std::string value) {
    std::transform(value.begin(), value.end(), value.begin(),
        [](unsigned char c) { return static_cast<char>(std::tolower(c)); });
    return value;
}
bool endsWith(const std::string& value, const char* suffix) {
    const std::string tail(suffix);
    return value.size() >= tail.size() &&
        value.compare(value.size() - tail.size(), tail.size(), tail) == 0;
}
jint inspectHeader(const unsigned char* b, ssize_t n, const std::string& name) {
    if (n < 0) return -1;
    if (n == 0) return EMPTY;
    const auto has = [b, n](size_t i, unsigned char c) {
        return i < static_cast<size_t>(n) && b[i] == c;
    };
    jint flags = 0;
    if (n >= 4 && has(0, 0x7f) && has(1, 'E') && has(2, 'L') && has(3, 'F')) flags |= ELF;
    if (n >= 2 && has(0, 'M') && has(1, 'Z')) flags |= PE;
    if (n >= 4 && has(0, 'd') && has(1, 'e') && has(2, 'x') && has(3, '\n')) flags |= DEX;
    if (n >= 4 && has(0, 'P') && has(1, 'K') &&
        ((has(2, 3) && has(3, 4)) || (has(2, 5) && has(3, 6)) ||
         (has(2, 7) && has(3, 8)))) flags |= ZIP;
    if (n >= 2 && has(0, '#') && has(1, '!')) flags |= SCRIPT;

    const bool executable = (flags & (ELF | PE | DEX | SCRIPT)) != 0;
    const std::string ext = lowerAscii(name);
    const bool documentOrMedia =
        endsWith(ext, ".txt") || endsWith(ext, ".jpg") || endsWith(ext, ".jpeg") ||
        endsWith(ext, ".png") || endsWith(ext, ".gif") || endsWith(ext, ".webp") ||
        endsWith(ext, ".pdf") || endsWith(ext, ".mp3") || endsWith(ext, ".mp4") ||
        endsWith(ext, ".doc") || endsWith(ext, ".docx");
    if (executable && documentOrMedia) flags |= EXTENSION_MISMATCH;
    return flags;
}
}

extern "C" JNIEXPORT jint JNICALL
Java_com_nevruz_fourtimessecured_core_NativeSecurityEngine_nativeInspectFileDescriptor(
    JNIEnv* env, jobject /* thiz */, jobject fileDescriptor, jstring displayName) {
    if (fileDescriptor == nullptr || displayName == nullptr) return -1;
    jclass fdClass = env->FindClass("java/io/FileDescriptor");
    if (fdClass == nullptr) return -1;
    jfieldID field = env->GetFieldID(fdClass, "descriptor", "I");
    if (field == nullptr) {
        if (env->ExceptionCheck()) env->ExceptionClear();
        return -1;
    }
    const jint fd = env->GetIntField(fileDescriptor, field);
    if (fd < 0) return -1;
    const char* chars = env->GetStringUTFChars(displayName, nullptr);
    if (chars == nullptr) return -1;
    const std::string name(chars);
    env->ReleaseStringUTFChars(displayName, chars);
    unsigned char header[4096];
    ssize_t count;
    do { count = pread(fd, header, sizeof(header), 0); }
    while (count < 0 && errno == EINTR);
    return inspectHeader(header, count, name);
}