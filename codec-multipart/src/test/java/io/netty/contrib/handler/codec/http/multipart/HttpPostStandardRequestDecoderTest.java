/*
 * Copyright 2022 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package io.netty.contrib.handler.codec.http.multipart;

import io.netty5.buffer.Buffer;
import io.netty5.buffer.DefaultBufferAllocators;
import io.netty5.handler.codec.http.DefaultHttpRequest;
import io.netty5.handler.codec.http.DefaultLastHttpContent;
import io.netty5.handler.codec.http.HttpMethod;
import io.netty5.handler.codec.http.HttpRequest;
import io.netty5.handler.codec.http.HttpVersion;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(GCExtension.class)
class HttpPostStandardRequestDecoderTest {

    @Test
    void testDecodeAttributes() {
        String requestBody = "key1=value1&key2=value2";

        HttpRequest request = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, "/upload");

        HttpPostStandardRequestDecoder decoder = new HttpPostStandardRequestDecoder(httpDiskDataFactory(), request);
        Buffer buf = DefaultBufferAllocators.preferredAllocator().copyOf(requestBody.getBytes(StandardCharsets.UTF_8));
        try (DefaultLastHttpContent httpContent = new DefaultLastHttpContent(buf)) {
            decoder.offer(httpContent);

            assertEquals(2, decoder.getBodyHttpDatas().size());
            assertMemoryAttribute(decoder.getBodyHttpData("key1"), "value1");
            assertMemoryAttribute(decoder.getBodyHttpData("key2"), "value2");
            decoder.destroy();
        }
    }

    @Test
    void testDecodeAttributesWithAmpersandPrefixSkipsNullAttribute() {
        String requestBody = "&key1=value1";

        HttpRequest request = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, "/upload");

        HttpPostStandardRequestDecoder decoder = new HttpPostStandardRequestDecoder(httpDiskDataFactory(), request);
        Buffer buf = DefaultBufferAllocators.preferredAllocator().copyOf(requestBody.getBytes(StandardCharsets.UTF_8));
        try (DefaultLastHttpContent httpContent = new DefaultLastHttpContent(buf)) {
            decoder.offer(httpContent);
        }

        assertEquals(1, decoder.getBodyHttpDatas().size());
        assertMemoryAttribute(decoder.getBodyHttpData("key1"), "value1");
        decoder.destroy();
    }

    @Test
    void testDecodeZeroAttributesWithAmpersandPrefix() {
        String requestBody = "&";

        HttpRequest request = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, "/upload");

        HttpPostStandardRequestDecoder decoder = new HttpPostStandardRequestDecoder(httpDiskDataFactory(), request);
        Buffer buf = DefaultBufferAllocators.preferredAllocator().copyOf(requestBody.getBytes(StandardCharsets.UTF_8));
        try (DefaultLastHttpContent httpContent = new DefaultLastHttpContent(buf)) {
            decoder.offer(httpContent);
        }

        assertEquals(0, decoder.getBodyHttpDatas().size());
        decoder.destroy();
    }

    private static DefaultHttpDataFactory httpDiskDataFactory() {
        return new DefaultHttpDataFactory(false);
    }

    private static void assertMemoryAttribute(InterfaceHttpData data, String expectedValue) {
        assertEquals(InterfaceHttpData.HttpDataType.Attribute, data.getHttpDataType());
        assertEquals(((MemoryAttribute) data).getValue(), expectedValue);
    }

}
