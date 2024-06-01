package com.tngtech.archunit.testutil;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.Properties;
import java.util.Random;
import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.tngtech.archunit.core.domain.properties.HasName;
import org.assertj.core.util.Files;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Comparator.comparing;
import static java.util.Objects.requireNonNull;
import static org.assertj.core.util.Files.temporaryFolderPath;
import static org.assertj.core.util.Strings.concat;

public class TestUtils {
    private static final Random random = new Random();

    /**
     * NOTE: The resolution of {@link Files#newTemporaryFolder()}, using {@link System#currentTimeMillis()}
     * is not good enough and makes tests flaky.
     */
    public static File newTemporaryFolder() {
        String folderName = "archtmp" + System.nanoTime() + random.nextLong();
        File folder = new File(concat(temporaryFolderPath(), folderName));
        if (folder.exists()) {
            Files.delete(folder);
        }
        checkArgument(folder.mkdirs(), "Folder %s already exists", folder.getAbsolutePath());
        folder.deleteOnExit();
        return folder;
    }

    public static Object invoke(Method method, Object owner, Object... params) {
        try {
            method.setAccessible(true);
            return method.invoke(owner, params);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Properties singleProperty(String key, String value) {
        Properties result = new Properties();
        result.setProperty(key, value);
        return result;
    }

    public static Properties properties(String... keyValues) {
        Properties result = new Properties();
        for (int i = 0; i < keyValues.length; i += 2) {
            result.setProperty(keyValues[i], keyValues[i + 1]);
        }
        return result;
    }

    public static <T extends HasName> void sortByName(T[] result) {
        Arrays.sort(result, comparing(HasName::getName));
    }

    public static URI toUri(URL url) {
        try {
            return url.toURI();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public static URL urlOf(Class<?> clazz) {
        return requireNonNull(clazz.getResource("/" + clazz.getName().replace('.', '/') + ".class"),
                String.format("Can't determine url of %s", clazz.getName()));
    }

    public static URI uriOf(Class<?> clazz) {
        return toUri(urlOf(clazz));
    }

    public static URI relativeResourceUri(Class<?> relativeToClass, String resourceName) {
        try {
            return relativeToClass.getResource("/" + relativeToClass.getPackage().getName().replace(".", "/") + "/" + resourceName).toURI();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    @SafeVarargs
    public static <T> Set<T> union(Set<T>... sets) {
        ImmutableSet.Builder<T> result = ImmutableSet.builder();
        for (Set<T> set : sets) {
            result = result.addAll(set);
        }
        return result.build();
    }

    public static void unchecked(ThrowingRunnable action) {
        unchecked(() -> {
            action.run();
            return null;
        });
    }

    public static <T> T unchecked(ThrowingSupplier<T> action) {
        try {
            return action.get();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @FunctionalInterface
    public interface ThrowingRunnable {
        void run() throws Exception;
    }

    @FunctionalInterface
    public interface ThrowingSupplier<T> {
        T get() throws Exception;
    }
}
