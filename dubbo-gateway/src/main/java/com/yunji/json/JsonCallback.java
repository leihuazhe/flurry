package com.yunji.json;


/**
 * @author zxwang
 */
public interface JsonCallback {

    /**
     * Called at start of Json object, typical handle the '{'
     *
     * @throws JException
     */
    void onStartObject() throws JException;

    /**
     * Called at end of Json object, typical handle the '}'
     *
     * @throws JException
     */
    void onEndObject() throws JException;

    /**
     * Called at start of Json array, typical handle the '['
     *
     * @throws JException
     */
    void onStartArray() throws JException;

    /**
     * Called at end of Json array, typical handle the ']'
     *
     * @throws JException
     */
    void onEndArray() throws JException;

    /**
     * Called at start of Json field, such as: "orderId":130
     *
     * @param name name of the filed, as for the example above, that is "orderId"
     * @throws JException
     */
    void onStartField(String name) throws JException;

    /**
     * called begin an array element
     *
     * @param index
     * @throws JException
     */
    void onStartField(int index) throws JException;

    /**
     * Called at end of Json field
     *
     * @throws JException
     */
    void onEndField() throws JException;

    /**
     * Called when a boolean value is met,
     * as to given field: <pre>"expired":false</pre>
     * First onStartField("expired") is called, followed by a call onBoolean(false) and a call onEndField()
     *
     * @param value
     * @throws JException
     */
    void onBoolean(boolean value) throws JException;

    /**
     * Called when a double value is met.
     *
     * @param value
     * @throws JException
     */
    void onNumber(double value) throws JException;

    /**
     * Called when a long/int value is met.
     *
     * @param value
     * @throws JException
     */
    void onNumber(long value) throws JException;

    /**
     * Called when a null value is met.
     * Such as: "subItemId":null
     *
     * @throws JException
     */
    void onNull() throws JException;

    /**
     * Called when a String value is met.
     * Such as: "name": "Walt"
     *
     * @param value
     * @throws JException
     */
    void onString(String value) throws JException;
}

