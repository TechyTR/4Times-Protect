# Keep JNI entry points and their class name stable for native symbol lookup.
-keep class com.nevruz.fourtimessecured.core.NativeSecurityEngine {
    private native <methods>;
}
