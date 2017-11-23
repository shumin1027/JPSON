package com.jpson;

import java.io.ByteArrayInputStream;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.jpson.internal.*;

/**
 * A high-level PSON decoder that maintains a dictionary.
 */
class PsonDecoder {

    private ByteArrayInputStream input;

    private List<String> dictionary;

    private PsonOptions options;

    private int allocationLimit;

    private byte[] convertArray = new byte[8];


    public static Object decode(byte[] buffer, List<String> initialDictionary, PsonOptions options, int allocationLimit) throws Exception {
        final ByteArrayInputStream input = new ByteArrayInputStream(buffer);
        final PsonDecoder decoder = new PsonDecoder(input, initialDictionary, options, allocationLimit);
        return decoder.read();
    }

    public PsonDecoder(ByteArrayInputStream input, List<String> initialDictionary, PsonOptions options, int allocationLimit) {
        if (input == null) {
            throw new IllegalArgumentException("input");
        }
        this.input = input;

        if (options == null) {
            this.options = PsonOptions.None;
        } else {
            this.options = options;
        }

        if (initialDictionary == null) {
            dictionary = null;
        } else {
            dictionary = new ArrayList<>(initialDictionary);
        }

        this.allocationLimit = allocationLimit;
    }

    public PsonDecoder(ByteArrayInputStream input, List<String> initialDictionary, PsonOptions options) {
        new PsonDecoder(input, initialDictionary, options, -1);
    }

    public PsonDecoder(ByteArrayInputStream input, List<String> initialDictionary) {
        new PsonDecoder(input, initialDictionary, PsonOptions.None);
    }

    public PsonDecoder(ByteArrayInputStream input) {
        new PsonDecoder(input, null, PsonOptions.None);
    }

    private Object read() throws Exception {
        return decodeValue();
    }

    private Object decodeValue() throws Exception {
        int token = (byte) input.read();
        if (token <= Token.MAX)
            return token;
        switch (token) {
            case Token.NULL:
                return null;
            case Token.TRUE:
                return true;
            case Token.FALSE:
                return false;
            case Token.EOBJECT:
                return new HashMap<String, Object>();
            case Token.EARRAY:
                return new ArrayList<>();
            case Token.ESTRING:
                return "";
            case Token.OBJECT:
                return decodeObject();
            case Token.ARRAY:
                return decodeArray();
            case Token.INTEGER:
                return ZigZag.decode(ZigZag.readVarint32(input));
            case Token.LONG:
                return ZigZag.decode(ZigZag.readVarint64(input));
            case Token.FLOAT:
                if (input.read(convertArray, 0, 4) != 4)
                    throw new PsonException("stream ended prematurely");
                if (ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN) {
                    ArrayUtils.reverse(convertArray, 0, 4);
                }
                return BitConverter.toFloat(convertArray, 0);
            case Token.DOUBLE:
                if (input.read(convertArray, 0, 8) != 8)
                    throw new PsonException("stream ended prematurely");
                if (ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN) {
                    ArrayUtils.reverse(convertArray);
                }
                return BitConverter.toDouble(convertArray, 0);
            case Token.STRING_ADD:
            case Token.STRING:
                return decodeString((byte) token, false);
            case Token.STRING_GET:
                return getString(ZigZag.readVarint32(input));
            case Token.BINARY:
                return decodeBinary();
            default:
//                throw new PsonException("illegal token: 0x" + token.ToString("x2")); // should never happen
                return null;
        }
    }

    private List<Object> decodeArray() throws Exception {
        int count = ZigZag.readVarint32(input);
        if (allocationLimit > -1 && count > allocationLimit)
            throw new PsonException("allocation limit exceeded:" + count);
        ArrayList list = new ArrayList();
        while (count-- > 0)
            list.add(decodeValue());
        return list;
    }

    private Map<String, Object> decodeObject() throws Exception {
        int count = ZigZag.readVarint32(input);
        if (allocationLimit > -1 && count > allocationLimit)
            throw new PsonException("allocation limit exceeded:" + count);
        Map obj = new HashMap();
        while (count-- > 0) {
            byte strToken = (byte) input.read();
            switch (strToken) {
                case Token.STRING_ADD:
                case Token.STRING:
                    obj.put(decodeString((byte) strToken, true), decodeValue());
                    break;

                case Token.STRING_GET:
                    obj.put(getString(ZigZag.readVarint32(input)), decodeValue());
                    break;

                default:
                    throw new PsonException("string token expected");
            }
        }
        return obj;
    }

    private String decodeString(byte token, boolean isKey) throws Exception {
        int count = ZigZag.readVarint32(input);
        if (allocationLimit > -1 && count > allocationLimit)
            throw new PsonException("allocation limit exceeded: " + count);
        byte[] buffer = new byte[count];
        if (input.read(buffer, 0, count) != count)
            throw new PsonException("stream ended prematurely");
        String value = new String(buffer, "UTF-8");
        if (token == Token.STRING_ADD) {
            if (isKey) {
                if (options == PsonOptions.ProgressiveKeys)
                    throw new PsonException("illegal progressive key");
            } else {
                if (options == PsonOptions.ProgressiveValues)
                    throw new PsonException("illegal progressive value");
            }
            dictionary.add(value);
        }
        return value;
    }

    private String getString(int index) throws Exception {
        if (index >= dictionary.size())
            throw new PsonException("dictionary index out of bounds: " + index);
        return dictionary.get(index);
    }

    private byte[] decodeBinary() throws Exception {
        int count = ZigZag.readVarint32(input);
        if (allocationLimit > -1 && count > allocationLimit)
            throw new PsonException("allocation limit exceeded: " + count);
        byte[] bytes = new byte[count];
        if (input.read(bytes, 0, count) != count)
            throw new PsonException("stream ended prematurely");
        return bytes;
    }
}