package com.example.cloudfilestorage.core.utilities;

public class Utilities {

    public static String buildPath(String... parts) {
        return String.join("/", parts);
    }

    public static String getParentPath(String fullPath) {
        if (fullPath == null || fullPath.lastIndexOf('/') == -1) {
            return "";
        }

        int lastSlashIndex = fullPath.lastIndexOf('/');
        return fullPath.substring(0, lastSlashIndex + 1);
    }

    public static String getResourceName(String fullPath) {
        if (fullPath == null || fullPath.lastIndexOf('/') == -1) {
            return "";
        }

        int lastSlashIndex = fullPath.lastIndexOf('/');
        return fullPath.substring(lastSlashIndex + 1);
    }
}
