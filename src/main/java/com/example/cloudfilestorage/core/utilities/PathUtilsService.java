package com.example.cloudfilestorage.core.utilities;

import com.example.cloudfilestorage.core.exception.ResourceException.InvalidResourceOperationException;
import com.example.cloudfilestorage.core.model.User;
import org.springframework.beans.factory.annotation.Value;

public class PathUtilsService {

    @Value("${minio.buckets.user-folder-pattern}")
    public static String userFolderPattern;


    public static String getUserFolder(User authenticatedUser) {
        return String.format(userFolderPattern, authenticatedUser.getId());
    }

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

    public static String getAbsolutPath(String fullPath, User user) {
        return buildPath(getUserFolder(user), fullPath);
    }

    public static boolean isRename(String from, String to) throws InvalidResourceOperationException {
        String resourceNameFrom = getResourceName(from);
        String parentPathFrom = getParentPath(from);

        String resourceNameTo = getResourceName(to);
        String parentPathTo = getParentPath(to);
        if (parentPathFrom.equals(parentPathTo) && !resourceNameFrom.equals(resourceNameTo)) {
            return true;
        }
        else if (!parentPathFrom.equals(parentPathTo) && resourceNameFrom.equals(resourceNameTo)) {
            return false;
        }
        throw new InvalidResourceOperationException(
                """
                   Невалидная операция с ресурсом.
                   Доступна только либо переименование либо только перемещение.
                """
        );
    }
}
