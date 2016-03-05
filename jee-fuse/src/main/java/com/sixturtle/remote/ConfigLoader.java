package com.sixturtle.remote;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URL;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * A helper class to load JSON based configuration files and convert them to a Java Type using Jackson Mapper API
 * Most helpful in loading configuration files that are relative to the .class file, not absolute.
 *
 * @author Anurag Sharma
 */
public final class ConfigLoader {
    private static final int BUFF_SIZE = 1024;
    private static final Logger log = LoggerFactory.getLogger(ConfigLoader.class);

    /**
     * Private constructor for utility class.
     */
    private ConfigLoader() {
        // utility class with all static methods, hide constructor
    }

    /**
     * Loads a JSON file and converts to the provided clazz type.
     *
     * @param clazz
     *            The returned class type
     * @param jsonFilepath
     *            The filepath of the JSON file
     * @param <T>
     *            Generic Type
     * @return An instance of clazz loaded from the JSON file in case of
     *         success, null otherwise
     */
    public static <T> T loadJsonFileContent(final Class<T> clazz, final String jsonFilepath) {
        return resolveJsonFileTemplate(clazz, jsonFilepath, null, null);
    }

    /**
     * Given a JSON template filepath, it creates an instance of clazz after
     * replacing all the tokens with provided values.
     *
     * @param clazz
     *            class <T>
     * @param templatePath
     *            The JSON template filepath
     * @param tokens
     *            An array of token names available in the template file
     * @param values
     *            An array of values in the same order and token names
     * @param <T>
     *            Generic Type
     *
     * @return an instance of clazz in case of success, null otherwise
     */
    public static <T> T resolveJsonFileTemplate(
            final Class<T> clazz,
            final String   templatePath,
            final String[] tokens,
            final String[] values) {

        T t = null;
        try {
            String content = loadFileContent(templatePath);
            if (StringUtils.isNotBlank(content)) {

                // replace tokens with values if tokens are provided
                if (tokens != null) {
                    content = StringUtils.replaceEach(content, tokens, values);
                }

                log.trace("resolved content of template {}: \n{}", templatePath, content);

                ObjectMapper jsonMapper = new ObjectMapper();
                t = jsonMapper.readValue(content, clazz);
            } else {
                log.error("empty content loaded from: {}", templatePath);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return t;
    }

    /**
     * Loads a file content to String.  Will first try to be loaded from the
     * OS filesystem.  If the file is not found, then the file will try to
     * be loaded relative to the class file as a class resource.
     *
     * @param filepath  The filename with full path
     *
     * @return content of the file in case of success, exception otherwise
     * @throws Exception in case of error
     */
    public static String loadFileContent(final String filepath) throws Exception {
        InputStream in = null;
        try {

            try {
                in = new FileInputStream(filepath);
                log.trace("loading system file: {}", filepath);
            } catch (final FileNotFoundException e) {
                in = ConfigLoader.class.getClassLoader().getResourceAsStream(filepath);
                log.trace("loading class loader resource file: {}", filepath);
            }
            if (in == null) {
                URL url = ConfigLoader.class.getResource(filepath);
                log.trace("loading class resource url: {}", url);
                if (url != null) {
                    in = new FileInputStream(url.getPath());
                }
            }

            if (in != null) {
                StringBuilder sb = new StringBuilder();
                byte[] buffer = new byte[BUFF_SIZE];
                int len = 0;
                while ((len = in.read(buffer)) != -1) {
                    sb.append(new String(buffer, 0, len));
                    buffer = new byte[BUFF_SIZE];
                }
                log.trace("file content: \n{}", sb);
                return sb.toString();
            } else {
                throw new FileNotFoundException(filepath);
            }
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (Exception e) {
                    log.error("Error Closing File stream {}", e.getMessage());
                }
            }
        }
    }
}
