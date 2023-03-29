/**
 * Copyright (C) 2022-2023 Red Hat, Inc. (https://github.com/Commonjava/indy-repository-service)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.commonjava.indy.service.repository.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.BeforeAll;

import java.util.Map;

public class AbstractSerializatinTest
{
    protected static ObjectMapper mapper;

    @BeforeAll
    protected static void beforeSuite()
    {
        JsonMapper.Builder builder = JsonMapper.builder();
        builder.serializationInclusion( JsonInclude.Include.NON_EMPTY )
               .configure( JsonGenerator.Feature.AUTO_CLOSE_JSON_CONTENT, true )
               .configure( DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true )
               .enable( MapperFeature.AUTO_DETECT_FIELDS )
               .enable( SerializationFeature.INDENT_OUTPUT, SerializationFeature.USE_EQUALITY_FOR_OBJECT_ID )
               .disable( SerializationFeature.WRITE_NULL_MAP_VALUES, SerializationFeature.WRITE_EMPTY_JSON_ARRAYS )
               .disable( DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES );

        mapper = builder.build();
    }
}
