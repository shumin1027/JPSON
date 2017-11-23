Working Draft                                                 D. Wirtz

Version 2                                                    July 2013

                      Protocol JSON - PSON

Status of This Memo

   This memo provides information for the Internet community.  It does
   not specify an Internet standard of any kind.  Distribution of this
   memo is unlimited.

Copyright Notice

   Copyright (c) 2013 Daniel Wirtz

Abstract

   Protocol JSON (PSON) is a lightweight, binary, language-independent
   data interchange format. PSON defines a set of encoding rules for
   the portable representation of structured data.

1.  Introduction

   Protocol JSON (PSON) is a binary format for the serialization of
   structured data. It is derived from JavaScript Object Notation
   (JSON), as defined in [RFC4627].

   PSON can represent five primitive types (strings, numbers, booleans,
   null and raw bytes) and two structured types (objects and arrays).

   A string is a sequence of zero or more UTF-8 characters [UNICODE].

   An object is an unordered collection of zero or more name/value
   pairs, where a name is a string and a value is a string, number,
   boolean, null, object or array.

   An array is an ordered sequence of zero or more values.

   The terms "object" and "array" come from the conventions of
   JavaScript.

   A variable length integer (varint) is a base 128 variable length
   integer as described in the Encoding section of the Protocol Buffers
   (protobuf) developer guide.

   PSON's design goals were for it to be small, portable, binary and
   a superset of JSON.

1.1.  Conventions Used in This Document

   The key words "MUST", "MUST NOT", "REQUIRED", "SHALL", "SHALL NOT",
   "SHOULD", "SHOULD NOT", "RECOMMENDED", "MAY", and "OPTIONAL" in this
   document are to be interpreted as described in [RFC2119].

   The grammatical rules in this document are to be interpreted as
   described in [RFC4234].

2.  PSON Grammar

   PSON data is a sequence of tokens, varints and arbitrary bytes.

2.1 Values

   A PSON value MUST start with a token and MAY be followed by

      a zig-zag encoded varint

   or

      an unsigned varint determining the length of arbitrary byte data
      following

   or

      an unsigned varint determining the number of PSON values
      following.

   No other combinations are allowed.

   There are 256 tokens:

      ZERO       = %x00 ;  0
      NEGONE     = %x01 ; -1
      ONE        = %x02 ; +1
              ...
      MAX        = %xEF ; -120

      NULL       = %xF0
      TRUE       = %xF1
      FALSE      = %xF2
      EOBJECT    = %xF3
      EARRAY     = %xF4
      ESTRING    = %xF5
      OBJECT     = %xF6 ; + varint32 + key/values
      ARRAY      = %xF7 ; + varint32 + values
      INTEGER    = %xF8 ; + varint32
      LONG       = %xF9 ; + varint64
      FLOAT      = %xFA ; + float32
      DOUBLE     = %xFB ; + float64
      STRING     = %xFC ; + varint32 + bytes
      STRING_ADD = %xFD ; + varint32 + bytes
      STRING_GET = %xFE ; + varint32
      BINARY     = %xFF ; + varint32 + bytes

2.2.  Boolean

   A boolean value evaluating to true MUST be encoded as the token:

      TRUE = %xF1

   A boolean value evaluating to false MUST be encoded as the token:

      FALSE = %xF2

2.2.  Numbers

2.2.1.  Integer

   Integer values greater than or equal -120 and less than or equal
   119 SHOULD be encoded as a token / single byte beginning at

      ZERO = %x00 ; 0

   and ending at

      MAX = %0xEF ; -120

   corresponding to the value's zig-zag encoded varint representation.

   Otherwise and values less than -120 or greater than 119 MUST be
   encoded as the token

      INTEGER = %xF8

   followed by its value as a zig-zag encoded 32 bit varint.

   If an integer value exceeds 32 bits of information and thus does not
   fit into a zig-zag encoded 32 bit varint, it SHOULD be encoded as
   the token

      LONG = %xF9

   followed by its value as a zig-zag encoded 64 bit varint or MUST be
   reduced to 32 bits otherwise which MAY rise a warning.

2.2.2.  Floating point

   A 32 bit float SHOULD be encoded as the token

      FLOAT = %xFA

   followed by the little endian 32 bit float value.

   Otherwise and a 64 bit double precision float MUST be encoded as
   the token

      DOUBLE = %xFB

   followed by the little endian 64 bit float value.

   If a 64 bit float can be converted to a 32 bit float without losing
   any information, it SHOULD be encoded as a 32 bit float instead.

   If a float can be converted to an integer without losing any
   information, it SHOULD be encoded as an integer.

2.2.  Arrays

  An array with zero elements SHOULD be encoded as the token

     EARRAY = %xF4

  Otherwise and arrays with one or more elements MUST be encoded as
  the token

     ARRAY = %xF7

  followed by the number of elements as an unsigned 32 bit varint
  followed by all elements as a PSON encoded value.

  If a value evaluates to the JavaScript constant

     undefined

  it must instead be encoded as the token:

     NULL = %xF0

2.3.  Objects

   An object evaluating to the JavaScript constant

      null

   MUST be encoded as the token:

      NULL = %xF0

   An object with zero key/value pairs SHOULD be encoded as the token:

      EOBJECT = %xF3

   Otherwise it and objects with one or more key/value pairs MUST be
   encoded as the token

      OBJECT = %xF6

   followed by the number of key/value pairs as an unsigned 32 bit
   varint followed by the alternating keys and values as PSON encoded
   values.

   If a value inside of an object evaluates to the JavaScript constant

      undefined

   the corresponding key/value pair MUST be omitted.

   Order of key/value pairs SHOULD be preserved if supported by the
   language runtime.

2.4.  Strings

   A string with zero characters SHOULD be encoded as the token:

      ESTRING = %xF5

   Otherwise it and strings with one or more characters MUST be encoded
   as the token

      STRING = %xFC

   followed by the number of raw bytes as an unsigned 32 bit varint
   followed by the UTF-8 encoded raw bytes.

2.5.  Binary data

   Binary data MUST be encoded as the token

      BINARY = %xFF

   followed by the number of raw bytes as an unsigned 32 bit varint
   followed by the raw bytes.

2.6.  undefined

   In PSON there is no token for a value that equals the JavaScript
   constant

      undefined

   and a value evaluating to undefined MUST either be skipped if it is
   a value inside of an object or, otherwise, be encoded as if it would
   equal the JavaScript constant:

      null

3.  Dictionaries

3.1. Progressive substitution

   In addition to encoding strings as defined in 2.4, strings SHOULD
   also be stored in a dictionary if requested by the application on
   the encoding side.

   A string that is not yet present in the dictionary SHOULD be added
   to the dictionary on the encoding side. If a key is added to the
   dictionary on the encoding side, it MUST be assigned the value of
   the number of elements contained in the dictionary before the value
   has been added (index) and, instead of being encoded like in 2.4,
   be encoded as the token

      STRING_ADD = %0xFD

   followed by the number of raw bytes as an unsigned 32 bit varint
   followed by the UTF-8 encoded raw bytes.

   When the decoding side decodes a string that has been encoded in
   this way, it MUST add the value to its dictionary and assign it
   the value of the number of elements contained in the dictionary
   before the value has been added (index).

   A string that has previously been added to the dictionary SHOULD,
   instead of being encoded as in 2.4, be encoded as the token

     STRING_GET = %0xFE

   followed by the previously assigned index as an unsigned 32 bit
   varint.

   When the decoding side decodes a string that has been encoded
   in this way, it MUST look up the index in the dictionary and
   return the remembered string value instead.

3.2.  Static substitution

  In addition to adding string values to the dictionary as defined in
  3.1, the initial dictionary MAY be negotiated between the encoding
  and the decoding side prior to encoding/decoding any values.

  If the encoding side uses static substitution, the decoding side MUST
  use the same dictionary entries in the same order.

4.  Encoding

  All floating point values MUST be encoded in little endian byte
  order.

  All string values MUST be encoded as UTF-8.

5.  Decoding

  A decoder MUST be able to process all data types defined in this
  document. It SHOULD return the corresponding values if available in
  the language runtime and MAY rise a warning otherwise.

6.  MIME media type

   The MIME media type for PSON is application/octet-stream.

Author's Address

   Daniel Wirtz
   dcode.io
   EMail: dcode@dcode.io

Full Copyright Statement

   Copyright 2013 Daniel Wirtz <dcode@dcode.io>

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.