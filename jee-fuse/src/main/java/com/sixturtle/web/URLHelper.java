package com.sixturtle.web;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;

import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A helper class for building URLs for various use cases including
 * but not limited to link headers.
 *
 * @author      Anurag Sharma
 */
public final class URLHelper {
    private static final Logger log = LoggerFactory.getLogger(URLHelper.class);

    public static final String URI_CURRENT = ".";

    public static final String PARAM_OFFSET  = "offset";
    public static final String PARAM_LIMIT   = "limit";
    public static final String PARAM_SORT    = "sort";

    public static final String DEFAULT_OFFSET = "0";
    public static final String DEFAULT_LIMIT = "50";

    public static final String HEADER_LINK_FIRST = "first";
    public static final String HEADER_LINK_PREV  = "prev";
    public static final String HEADER_LINK_NEXT  = "next";
    public static final String HEADER_LINK_LAST  = "last";
    public static final String HEADER_TOTAL_COUNT = "X-total-count";

    /**
     * Utility class, prohibit construction.
     */
    private URLHelper() {
        // no constructor
    }

    /**
     * Generate a link to the resource.
     *
     * @param uriInfo
     *            uriInfo
     * @param id
     *            The resource ID
     * @param clazz
     *            class<T>
     * @return {@link URI} to the resource in case of success, null otherwise
     */
    public static URI selfLink(UriInfo uriInfo, final String id, Class< ? > clazz) {
        URI uri = null;
        if (uriInfo == null) {
            try {
                uri = new URI("/" + id.toString());
            } catch (URISyntaxException e) {
                log.error("URI Syntax error {}", e.getMessage());
            }
        } /*else {
            uri = uriInfo.getBaseUriBuilder()
                          .path(clazz)
                          .path(id.toString())
                          .build();
        }*/
        return uri;
    }

    /**
     * Builds headers for a GET request to a collection of resource.
     *
     * @param builder
     *            An instance of {@link ResponseBuilder} creating the response
     * @param uriInfo
     *            Base URI of the resource collection
     * @param <T>
     *            Generic Type
     * @param list
     *            An instance of {@link PaginatedModel} serving the request
     */
    public static <T> void addNavHeaders(
            final ResponseBuilder   builder,
            final UriInfo           uriInfo,
            final PaginatedModel<T>  list) {
        String urlTemplate = buildUrlTemplate(uriInfo);

        // Add total count
        builder.header(HEADER_TOTAL_COUNT, list.getCount());

        // Add navigation links
        if (list.hasPrev()) {
            String first = String.format(urlTemplate.toString(), list.first(), list.getLimit());
            String prev = String.format(urlTemplate.toString(), list.prev(), list.getLimit());
            if (first.equals(prev)) {
                builder.link(URI.create(first), "first prev");
            } else {
                builder.link(URI.create(first), "first");
                builder.link(URI.create(prev), "prev");
            }
        } else {
            if (list.hasNext()) {
                builder.link(URI.create(URI_CURRENT), "first");
            } else {
                builder.link(URI.create(URI_CURRENT), "first last");
            }
        }
        if (list.hasNext()) {
            String next = String.format(urlTemplate.toString(), list.next(), list.getLimit());
            String last = String.format(urlTemplate.toString(), list.last(), list.getLimit());
            if (last.equals(next)) {
                builder.link(URI.create(last), "next last");
            } else {
                builder.link(URI.create(next), "next");
                builder.link(URI.create(last), "last");
            }
        } else {
            if (list.hasPrev()) {
                builder.link(URI.create(URI_CURRENT), "last");
            }
        }
    }

    /**
     * Converts existing URL into a template where offset and limit
     * values can be updated.
     *
     * @param uriInfo   The {@link UriInfo} for the current request
     * @return          The relative URL for the resource with offset=%d&limit=%d
     */
    protected static String buildUrlTemplate(final UriInfo uriInfo) {
        String urlTemplate = null;

        String url;
        try {
            url = URLDecoder.decode(uriInfo.getRequestUri().toASCIIString(), "UTF-8");
            int pos = url.indexOf(uriInfo.getBaseUri().toASCIIString());
            if (pos >= 0) {
                urlTemplate = url.substring(pos);
            } else {
                urlTemplate = url;
            }
        } catch (UnsupportedEncodingException e) {
            log.error("Error in decoding the Link header", e);
        }


        urlTemplate = updateQueryParam(urlTemplate, URLHelper.PARAM_OFFSET, "%d");
        urlTemplate = updateQueryParam(urlTemplate, URLHelper.PARAM_LIMIT, "%d");
        return urlTemplate;
    }

    /**
     * Given a URL with query parameter, it adds or updates given param/value.
     *
     * @param url       Existing URL with query parameters
     * @param param     The parameter name whose value needs to be added/updated
     * @param value     The value of the parameter
     * @return          Updated URL containing updated/added param/value
     */
    protected static String updateQueryParam(
                        final String url,
                        final String param,
                        final String value) {

        /*
         * Locate the param and update the value.
         * If not found then append as new param/value.
         *
         * Example URL:
         * "/resources?JSESSIONID=123234&sort='field1,-field2'&q='field1=value1,field2=value2'&offset=0&limit=50"
         */

        // Find the boundaries of "param=value"
        int beginIndex = url.indexOf(param);
        int endIndex   = -1;
        if (beginIndex >= 0) { // param found
            endIndex   = url.indexOf('&', beginIndex + 1);
            if (endIndex < 0) { // no other params
                endIndex = url.length();
            }
        } else { // param not found, need to append
            beginIndex = url.length();
            endIndex   = url.length();
        }

        // Update the value in "param=value"
        String paramValue = url.substring(beginIndex, endIndex);
        String[] fields = null;
        if (StringUtils.isNotBlank(paramValue)) {
            fields    = paramValue.split("=");
            if (fields.length == 1) { // param without value
                fields = null; // will construct in later step
            } else {
                if (fields[1].equals(value)) {
                    return url; // done! no change needed
                }
                fields[1] = value; // update with new value
            }
        }
        if (fields == null) {
            fields    = new String[2];
            fields[0] = param;
            fields[1] = value;
        }

        // Split url into two parts - before and after "param=value"
        StringBuilder builder = new StringBuilder(url.substring(0, beginIndex));

        // if it is the last param, make sure to put ? or &
        int pos = url.indexOf('?');
        if (pos < 0) {
            builder.append('?');
        }
        if (beginIndex == url.length() && url.indexOf('?') >= 0) {
            builder.append('&');
        }
        // insert the updated "param=value"
        builder.append(fields[0]).append("=").append(fields[1]);
        builder.append(url.substring(endIndex, url.length()));

        return builder.toString();
    }
}
