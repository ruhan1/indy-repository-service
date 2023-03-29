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
package org.commonjava.indy.service.repository.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema( type = SchemaType.OBJECT, discriminatorProperty = "type",
         description = "Representation of a simple boolean result of query, like if the stores data is empty" )
public class SimpleBooleanResultDTO
{
    @JsonProperty
    @Schema( description = "The description for this boolean result", required = true )
    private String description;

    @JsonProperty
    @Schema( description = "The boolean result", required = true )
    private Boolean result;

    public String getDescription()
    {
        return description;
    }

    public void setDescription( String description )
    {
        this.description = description;
    }

    public Boolean getResult()
    {
        return result;
    }

    public void setResult( Boolean result )
    {
        this.result = result;
    }
}
