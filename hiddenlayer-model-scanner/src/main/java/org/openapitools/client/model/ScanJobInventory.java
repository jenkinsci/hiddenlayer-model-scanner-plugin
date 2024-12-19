/*
 * HiddenLayer ModelScan V2
 * HiddenLayer ModelScan API for scanning of models
 *
 * The version of the OpenAPI document: 1
 *
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */

package org.openapitools.client.model;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import hiddenlayer.sdk.JSON;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.StringJoiner;
import java.util.logging.Level;
import java.util.logging.Logger;

@javax.annotation.Generated(
        value = "org.openapitools.codegen.languages.JavaClientCodegen",
        date = "2024-12-03T15:49:05.899737Z[GMT]",
        comments = "Generator version: 7.6.0")
@JsonDeserialize(using = ScanJobInventory.ScanJobInventoryDeserializer.class)
@JsonSerialize(using = ScanJobInventory.ScanJobInventorySerializer.class)
public class ScanJobInventory extends AbstractOpenApiSchema {
    private static final Logger log = Logger.getLogger(ScanJobInventory.class.getName());

    public static class ScanJobInventorySerializer extends StdSerializer<ScanJobInventory> {
        public ScanJobInventorySerializer(Class<ScanJobInventory> t) {
            super(t);
        }

        public ScanJobInventorySerializer() {
            this(null);
        }

        @Override
        public void serialize(ScanJobInventory value, JsonGenerator jgen, SerializerProvider provider)
                throws IOException, JsonProcessingException {
            jgen.writeObject(value.getActualInstance());
        }
    }

    public static class ScanJobInventoryDeserializer extends StdDeserializer<ScanJobInventory> {
        public ScanJobInventoryDeserializer() {
            this(ScanJobInventory.class);
        }

        public ScanJobInventoryDeserializer(Class<?> vc) {
            super(vc);
        }

        @Override
        public ScanJobInventory deserialize(JsonParser jp, DeserializationContext ctxt)
                throws IOException, JsonProcessingException {
            JsonNode tree = jp.readValueAsTree();
            Object deserialized = null;
            boolean typeCoercion = ctxt.isEnabled(MapperFeature.ALLOW_COERCION_OF_SCALARS);
            int match = 0;
            JsonToken token = tree.traverse(jp.getCodec()).nextToken();
            // deserialize ScanModelDetailsV3
            try {
                boolean attemptParsing = true;
                // ensure that we respect type coercion as set on the client ObjectMapper
                if (ScanModelDetailsV3.class.equals(Integer.class)
                        || ScanModelDetailsV3.class.equals(Long.class)
                        || ScanModelDetailsV3.class.equals(Float.class)
                        || ScanModelDetailsV3.class.equals(Double.class)
                        || ScanModelDetailsV3.class.equals(Boolean.class)
                        || ScanModelDetailsV3.class.equals(String.class)) {
                    attemptParsing = typeCoercion;
                    if (!attemptParsing) {
                        attemptParsing |= ((ScanModelDetailsV3.class.equals(Integer.class)
                                        || ScanModelDetailsV3.class.equals(Long.class))
                                && token == JsonToken.VALUE_NUMBER_INT);
                        attemptParsing |= ((ScanModelDetailsV3.class.equals(Float.class)
                                        || ScanModelDetailsV3.class.equals(Double.class))
                                && token == JsonToken.VALUE_NUMBER_FLOAT);
                        attemptParsing |= (ScanModelDetailsV3.class.equals(Boolean.class)
                                && (token == JsonToken.VALUE_FALSE || token == JsonToken.VALUE_TRUE));
                        attemptParsing |=
                                (ScanModelDetailsV3.class.equals(String.class) && token == JsonToken.VALUE_STRING);
                    }
                }
                if (attemptParsing) {
                    deserialized = tree.traverse(jp.getCodec()).readValueAs(ScanModelDetailsV3.class);
                    // TODO: there is no validation against JSON schema constraints
                    // (min, max, enum, pattern...), this does not perform a strict JSON
                    // validation, which means the 'match' count may be higher than it should be.
                    match++;
                    log.log(Level.FINER, "Input data matches schema 'ScanModelDetailsV3'");
                }
            } catch (java.lang.Exception e) {
                // deserialization failed, continue
                log.log(Level.FINER, "Input data does not match schema 'ScanModelDetailsV3'", e);
            }

            // deserialize ScanModelIdsV3
            try {
                boolean attemptParsing = true;
                // ensure that we respect type coercion as set on the client ObjectMapper
                if (ScanModelIdsV3.class.equals(Integer.class)
                        || ScanModelIdsV3.class.equals(Long.class)
                        || ScanModelIdsV3.class.equals(Float.class)
                        || ScanModelIdsV3.class.equals(Double.class)
                        || ScanModelIdsV3.class.equals(Boolean.class)
                        || ScanModelIdsV3.class.equals(String.class)) {
                    attemptParsing = typeCoercion;
                    if (!attemptParsing) {
                        attemptParsing |=
                                ((ScanModelIdsV3.class.equals(Integer.class) || ScanModelIdsV3.class.equals(Long.class))
                                        && token == JsonToken.VALUE_NUMBER_INT);
                        attemptParsing |=
                                ((ScanModelIdsV3.class.equals(Float.class) || ScanModelIdsV3.class.equals(Double.class))
                                        && token == JsonToken.VALUE_NUMBER_FLOAT);
                        attemptParsing |= (ScanModelIdsV3.class.equals(Boolean.class)
                                && (token == JsonToken.VALUE_FALSE || token == JsonToken.VALUE_TRUE));
                        attemptParsing |=
                                (ScanModelIdsV3.class.equals(String.class) && token == JsonToken.VALUE_STRING);
                    }
                }
                if (attemptParsing) {
                    deserialized = tree.traverse(jp.getCodec()).readValueAs(ScanModelIdsV3.class);
                    // TODO: there is no validation against JSON schema constraints
                    // (min, max, enum, pattern...), this does not perform a strict JSON
                    // validation, which means the 'match' count may be higher than it should be.
                    match++;
                    log.log(Level.FINER, "Input data matches schema 'ScanModelIdsV3'");
                }
            } catch (java.lang.Exception e) {
                // deserialization failed, continue
                log.log(Level.FINER, "Input data does not match schema 'ScanModelIdsV3'", e);
            }

            if (match == 1) {
                ScanJobInventory ret = new ScanJobInventory();
                ret.setActualInstance(deserialized);
                return ret;
            }
            throw new IOException(String.format(
                    "Failed deserialization for ScanJobInventory: %d classes match result, expected 1", match));
        }

        /**
         * Handle deserialization of the 'null' value.
         */
        @Override
        public ScanJobInventory getNullValue(DeserializationContext ctxt) throws JsonMappingException {
            throw new JsonMappingException(ctxt.getParser(), "ScanJobInventory cannot be null");
        }
    }

    // store a list of schema names defined in oneOf
    public static final Map<String, Class<?>> schemas = new HashMap<>();

    public ScanJobInventory() {
        super("oneOf", Boolean.FALSE);
    }

    public ScanJobInventory(ScanModelDetailsV3 o) {
        super("oneOf", Boolean.FALSE);
        setActualInstance(o);
    }

    public ScanJobInventory(ScanModelIdsV3 o) {
        super("oneOf", Boolean.FALSE);
        setActualInstance(o);
    }

    static {
        schemas.put("ScanModelDetailsV3", ScanModelDetailsV3.class);
        schemas.put("ScanModelIdsV3", ScanModelIdsV3.class);
        JSON.registerDescendants(ScanJobInventory.class, Collections.unmodifiableMap(schemas));
    }

    @Override
    public Map<String, Class<?>> getSchemas() {
        return ScanJobInventory.schemas;
    }

    /**
     * Set the instance that matches the oneOf child schema, check
     * the instance parameter is valid against the oneOf child schemas:
     * ScanModelDetailsV3, ScanModelIdsV3
     *
     * It could be an instance of the 'oneOf' schemas.
     * The oneOf child schemas may themselves be a composed schema (allOf, anyOf, oneOf).
     */
    @Override
    public void setActualInstance(Object instance) {
        if (JSON.isInstanceOf(ScanModelDetailsV3.class, instance, new HashSet<Class<?>>())) {
            super.setActualInstance(instance);
            return;
        }

        if (JSON.isInstanceOf(ScanModelIdsV3.class, instance, new HashSet<Class<?>>())) {
            super.setActualInstance(instance);
            return;
        }

        throw new RuntimeException("Invalid instance type. Must be ScanModelDetailsV3, ScanModelIdsV3");
    }

    /**
     * Get the actual instance, which can be the following:
     * ScanModelDetailsV3, ScanModelIdsV3
     *
     * @return The actual instance (ScanModelDetailsV3, ScanModelIdsV3)
     */
    @Override
    public Object getActualInstance() {
        return super.getActualInstance();
    }

    /**
     * Get the actual instance of `ScanModelDetailsV3`. If the actual instance is not `ScanModelDetailsV3`,
     * the ClassCastException will be thrown.
     *
     * @return The actual instance of `ScanModelDetailsV3`
     * @throws ClassCastException if the instance is not `ScanModelDetailsV3`
     */
    public ScanModelDetailsV3 getScanModelDetailsV3() throws ClassCastException {
        return (ScanModelDetailsV3) super.getActualInstance();
    }

    /**
     * Get the actual instance of `ScanModelIdsV3`. If the actual instance is not `ScanModelIdsV3`,
     * the ClassCastException will be thrown.
     *
     * @return The actual instance of `ScanModelIdsV3`
     * @throws ClassCastException if the instance is not `ScanModelIdsV3`
     */
    public ScanModelIdsV3 getScanModelIdsV3() throws ClassCastException {
        return (ScanModelIdsV3) super.getActualInstance();
    }

    /**
     * Convert the instance into URL query string.
     *
     * @return URL query string
     */
    public String toUrlQueryString() {
        return toUrlQueryString(null);
    }

    /**
     * Convert the instance into URL query string.
     *
     * @param prefix prefix of the query string
     * @return URL query string
     */
    public String toUrlQueryString(String prefix) {
        String suffix = "";
        String containerSuffix = "";
        String containerPrefix = "";
        if (prefix == null) {
            // style=form, explode=true, e.g. /pet?name=cat&type=manx
            prefix = "";
        } else {
            // deepObject style e.g. /pet?id[name]=cat&id[type]=manx
            prefix = prefix + "[";
            suffix = "]";
            containerSuffix = "]";
            containerPrefix = "[";
        }

        StringJoiner joiner = new StringJoiner("&");

        if (getActualInstance() instanceof ScanModelDetailsV3) {
            if (getActualInstance() != null) {
                joiner.add(((ScanModelDetailsV3) getActualInstance()).toUrlQueryString(prefix + "one_of_0" + suffix));
            }
            return joiner.toString();
        }
        if (getActualInstance() instanceof ScanModelIdsV3) {
            if (getActualInstance() != null) {
                joiner.add(((ScanModelIdsV3) getActualInstance()).toUrlQueryString(prefix + "one_of_1" + suffix));
            }
            return joiner.toString();
        }
        return null;
    }
}
