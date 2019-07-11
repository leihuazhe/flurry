package com.yunji.json.serializer;


import java.io.IOException;

/**
 * @author zxwang
 */
public interface JsonCallback {

    /**
     * Called at start of Json object, typical handle the '{'
     *
     * @throws IOException
     */
    void onStartObject() throws IOException;

    /**
     * Called at end of Json object, typical handle the '}'
     *
     * @throws IOException
     */
    void onEndObject() throws IOException;

    /**
     * Called at start of Json array, typical handle the '['
     *
     * @throws IOException
     */
    void onStartArray() throws IOException;

    /**
     * Called at end of Json array, typical handle the ']'
     *
     * @throws IOException
     */
    void onEndArray() throws IOException;

    /**
     * Called at start of Json field, such as: "orderId":130
     *
     * @param name name of the filed, as for the example above, that is "orderId"
     * @throws IOException
     */
    void onStartField(String name) throws IOException;

    /**
     * called begin an array element
     *
     * @param index
     * @throws IOException
     */
    void onStartField(int index) throws IOException;

    /**
     * Called at end of Json field
     *
     * @throws IOException
     */
    void onEndField() throws IOException;

    /**
     * Called when a boolean value is met,
     * as to given field: <pre>"expired":false</pre>
     * First onStartField("expired") is called, followed by a call onBoolean(false) and a call onEndField()
     *
     * @param value
     * @throws IOException
     */
    void onBoolean(boolean value) throws IOException;

    /**
     * Called when a double value is met.
     *
     * @param value
     * @throws IOException
     */
    void onNumber(double value) throws IOException;

    /**
     * Called when a long/int value is met.
     *
     * @param value
     * @throws IOException
     */
    void onNumber(long value) throws IOException;

    /**
     * Called when a null value is met.
     * Such as: "subItemId":null
     *
     * @throws IOException
     */
    void onNull() throws IOException;

    /**
     * Called when a String value is met.
     * Such as: "name": "Walt"
     *
     * @param value
     * @throws IOException
     */
    void onString(String value) throws IOException;

    /**
     * onColon ":"
     *
     * @throws IOException
     */
    void onColon() throws IOException;
}

