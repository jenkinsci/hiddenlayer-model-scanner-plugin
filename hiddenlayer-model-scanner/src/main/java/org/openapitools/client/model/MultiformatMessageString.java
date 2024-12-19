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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.StringJoiner;

/**
 * A message string or message format string rendered in multiple formats.
 */
@JsonPropertyOrder({
    MultiformatMessageString.JSON_PROPERTY_TEXT,
    MultiformatMessageString.JSON_PROPERTY_MARKDOWN,
    MultiformatMessageString.JSON_PROPERTY_PROPERTIES
})
@javax.annotation.Generated(
        value = "org.openapitools.codegen.languages.JavaClientCodegen",
        date = "2024-12-03T15:49:05.899737Z[GMT]",
        comments = "Generator version: 7.6.0")
public class MultiformatMessageString {
    public static final String JSON_PROPERTY_TEXT = "text";
    private String text;

    public static final String JSON_PROPERTY_MARKDOWN = "markdown";
    private String markdown;

    public static final String JSON_PROPERTY_PROPERTIES = "properties";
    private PropertyBag properties;

    public MultiformatMessageString() {}

    public MultiformatMessageString text(String text) {
        this.text = text;
        return this;
    }

    /**
     * A plain text message string or format string.
     * @return text
     **/
    @javax.annotation.Nonnull
    @JsonProperty(JSON_PROPERTY_TEXT)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public String getText() {
        return text;
    }

    @JsonProperty(JSON_PROPERTY_TEXT)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setText(String text) {
        this.text = text;
    }

    public MultiformatMessageString markdown(String markdown) {
        this.markdown = markdown;
        return this;
    }

    /**
     * A Markdown message string or format string.
     * @return markdown
     **/
    @javax.annotation.Nullable
    @JsonProperty(JSON_PROPERTY_MARKDOWN)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getMarkdown() {
        return markdown;
    }

    @JsonProperty(JSON_PROPERTY_MARKDOWN)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setMarkdown(String markdown) {
        this.markdown = markdown;
    }

    public MultiformatMessageString properties(PropertyBag properties) {
        this.properties = properties;
        return this;
    }

    /**
     * Get properties
     * @return properties
     **/
    @javax.annotation.Nullable
    @JsonProperty(JSON_PROPERTY_PROPERTIES)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public PropertyBag getProperties() {
        return properties;
    }

    @JsonProperty(JSON_PROPERTY_PROPERTIES)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setProperties(PropertyBag properties) {
        this.properties = properties;
    }

    /**
     * Return true if this multiformatMessageString object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MultiformatMessageString multiformatMessageString = (MultiformatMessageString) o;
        return Objects.equals(this.text, multiformatMessageString.text)
                && Objects.equals(this.markdown, multiformatMessageString.markdown)
                && Objects.equals(this.properties, multiformatMessageString.properties);
    }

    @Override
    public int hashCode() {
        return Objects.hash(text, markdown, properties);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class MultiformatMessageString {\n");
        sb.append("    text: ").append(toIndentedString(text)).append("\n");
        sb.append("    markdown: ").append(toIndentedString(markdown)).append("\n");
        sb.append("    properties: ").append(toIndentedString(properties)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private String toIndentedString(Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
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

        // add `text` to the URL query string
        if (getText() != null) {
            joiner.add(String.format(
                    "%stext%s=%s",
                    prefix,
                    suffix,
                    URLEncoder.encode(String.valueOf(getText()), StandardCharsets.UTF_8)
                            .replaceAll("\\+", "%20")));
        }

        // add `markdown` to the URL query string
        if (getMarkdown() != null) {
            joiner.add(String.format(
                    "%smarkdown%s=%s",
                    prefix,
                    suffix,
                    URLEncoder.encode(String.valueOf(getMarkdown()), StandardCharsets.UTF_8)
                            .replaceAll("\\+", "%20")));
        }

        // add `properties` to the URL query string
        if (getProperties() != null) {
            joiner.add(String.format(
                    "%sproperties%s=%s",
                    prefix,
                    suffix,
                    URLEncoder.encode(String.valueOf(getProperties()), StandardCharsets.UTF_8)
                            .replaceAll("\\+", "%20")));
        }

        return joiner.toString();
    }
}
