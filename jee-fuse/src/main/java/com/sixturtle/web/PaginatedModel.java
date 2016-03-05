package com.sixturtle.web;

import java.util.List;

/**
 * A resource facade to encapsulate pagination information.
 *
 * @author Anurag Sharma
 *
 * @param <T>
 */
public class PaginatedModel<T> {
    private int offset;
    private int limit;
    private Long count;
    private List<T> data;

    /**
     * Construct paginated wrapper of a list data.
     *
     * @param offset
     *            The starting index requested
     * @param limit
     *            Max number of elements requested
     * @param count
     *            Total number of the elements available
     * @param data
     *            Actual list of elements
     */
    public PaginatedModel(int offset, int limit, Long count, List<T> data) {
        this.offset = offset;
        this.limit  = limit;
        this.count  = count;
        this.data   = data;
    }

    /**
     * @return the offset
     */
    public int getOffset() {
        return offset;
    }
    /**
     * @return the limit
     */
    public int getLimit() {
        return limit;
    }
    /**
     * @return the count
     */
    public Long getCount() {
        return count;
    }
    /**
     * @return the data
     */
    public List<T> getData() {
        return data;
    }

    /**
     * @return true if previous exists, false otherwise
     */
    public boolean hasPrev() {
        return (offset > 0);
    }
    /**
     * @return true if next exists, false otherwise
     */
    public boolean hasNext() {
        return ((offset + limit) < count);
    }
    /**
     * @return offset of the first position
     */
    public int first() {
        return 0;
    }
    /**
     * @return offset of the previous position
     */
    public int prev() {
        return ((offset - limit) < 0) ? 0 : (offset - limit);
    }
    /**
     * @return offset of the next position
     */
    public int next() {
        return (hasNext() ? (offset + limit) : offset);
    }
    /**
     * @return offset of the last position
     */
    public int last() {
        return (int) ((count % limit > 0)
                ? (limit * (count / limit) + (offset % limit))
                : (limit * ((count / limit) - 1)));
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("PaginatedModel {")
               .append("offset:").append(offset).append(",")
               .append("limit:").append(limit).append(",")
               .append("count:").append(count).append(",")
               .append("data:").append(data)
               .append("}");
        return builder.toString();
    }
}
