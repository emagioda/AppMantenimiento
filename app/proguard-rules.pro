# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# Gson models used for asset parsing
-keep class com.emagioda.myapp.data.datasource.AssetsDiagnosticDataSource$MachinesIndex { *; }
-keep class com.emagioda.myapp.data.datasource.AssetsDiagnosticDataSource$MachineMap { *; }
-keep class com.emagioda.myapp.data.datasource.AssetsDiagnosticDataSource$RawTree { *; }
-keep class com.emagioda.myapp.data.datasource.AssetsDiagnosticDataSource$RawNode { *; }
-keep class com.emagioda.myapp.data.datasource.AssetsDiagnosticDataSource$RawPartRef { *; }
-keep class com.emagioda.myapp.data.datasource.AssetsDiagnosticDataSource$PartsCatalog { *; }
-keep class com.emagioda.myapp.data.datasource.AssetsDiagnosticDataSource$PartDetailRaw { *; }
-keep class com.emagioda.myapp.data.datasource.AssetsDiagnosticDataSource$ContactRefRaw { *; }
-keep class com.emagioda.myapp.data.datasource.AssetsContactsDataSource$Envelope { *; }
-keep class com.emagioda.myapp.data.datasource.AssetsContactsDataSource$ContactRaw { *; }
