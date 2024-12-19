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
 * SubmissionResponse
 */
@JsonPropertyOrder({
    SubmissionResponse.JSON_PROPERTY_TENANT_ID,
    SubmissionResponse.JSON_PROPERTY_SENSOR_ID,
    SubmissionResponse.JSON_PROPERTY_REQUESTER_ID,
    SubmissionResponse.JSON_PROPERTY_GROUP_ID,
    SubmissionResponse.JSON_PROPERTY_EVENT_TIME
})
@javax.annotation.Generated(
        value = "org.openapitools.codegen.languages.JavaClientCodegen",
        date = "2024-12-03T15:49:05.899737Z[GMT]",
        comments = "Generator version: 7.6.0")
public class SubmissionResponse {
    public static final String JSON_PROPERTY_TENANT_ID = "tenant_id";
    private String tenantId;

    public static final String JSON_PROPERTY_SENSOR_ID = "sensor_id";
    private String sensorId;

    public static final String JSON_PROPERTY_REQUESTER_ID = "requester_id";
    private String requesterId;

    public static final String JSON_PROPERTY_GROUP_ID = "group_id";
    private String groupId;

    public static final String JSON_PROPERTY_EVENT_TIME = "event_time";
    private String eventTime;

    public SubmissionResponse() {}

    public SubmissionResponse tenantId(String tenantId) {
        this.tenantId = tenantId;
        return this;
    }

    /**
     * Get tenantId
     * @return tenantId
     **/
    @javax.annotation.Nonnull
    @JsonProperty(JSON_PROPERTY_TENANT_ID)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public String getTenantId() {
        return tenantId;
    }

    @JsonProperty(JSON_PROPERTY_TENANT_ID)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public SubmissionResponse sensorId(String sensorId) {
        this.sensorId = sensorId;
        return this;
    }

    /**
     * Get sensorId
     * @return sensorId
     **/
    @javax.annotation.Nonnull
    @JsonProperty(JSON_PROPERTY_SENSOR_ID)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public String getSensorId() {
        return sensorId;
    }

    @JsonProperty(JSON_PROPERTY_SENSOR_ID)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setSensorId(String sensorId) {
        this.sensorId = sensorId;
    }

    public SubmissionResponse requesterId(String requesterId) {
        this.requesterId = requesterId;
        return this;
    }

    /**
     * Get requesterId
     * @return requesterId
     **/
    @javax.annotation.Nonnull
    @JsonProperty(JSON_PROPERTY_REQUESTER_ID)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public String getRequesterId() {
        return requesterId;
    }

    @JsonProperty(JSON_PROPERTY_REQUESTER_ID)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setRequesterId(String requesterId) {
        this.requesterId = requesterId;
    }

    public SubmissionResponse groupId(String groupId) {
        this.groupId = groupId;
        return this;
    }

    /**
     * Get groupId
     * @return groupId
     **/
    @javax.annotation.Nonnull
    @JsonProperty(JSON_PROPERTY_GROUP_ID)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public String getGroupId() {
        return groupId;
    }

    @JsonProperty(JSON_PROPERTY_GROUP_ID)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public SubmissionResponse eventTime(String eventTime) {
        this.eventTime = eventTime;
        return this;
    }

    /**
     * Get eventTime
     * @return eventTime
     **/
    @javax.annotation.Nonnull
    @JsonProperty(JSON_PROPERTY_EVENT_TIME)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public String getEventTime() {
        return eventTime;
    }

    @JsonProperty(JSON_PROPERTY_EVENT_TIME)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setEventTime(String eventTime) {
        this.eventTime = eventTime;
    }

    /**
     * Return true if this SubmissionResponse object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SubmissionResponse submissionResponse = (SubmissionResponse) o;
        return Objects.equals(this.tenantId, submissionResponse.tenantId)
                && Objects.equals(this.sensorId, submissionResponse.sensorId)
                && Objects.equals(this.requesterId, submissionResponse.requesterId)
                && Objects.equals(this.groupId, submissionResponse.groupId)
                && Objects.equals(this.eventTime, submissionResponse.eventTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tenantId, sensorId, requesterId, groupId, eventTime);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class SubmissionResponse {\n");
        sb.append("    tenantId: ").append(toIndentedString(tenantId)).append("\n");
        sb.append("    sensorId: ").append(toIndentedString(sensorId)).append("\n");
        sb.append("    requesterId: ").append(toIndentedString(requesterId)).append("\n");
        sb.append("    groupId: ").append(toIndentedString(groupId)).append("\n");
        sb.append("    eventTime: ").append(toIndentedString(eventTime)).append("\n");
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

        // add `tenant_id` to the URL query string
        if (getTenantId() != null) {
            joiner.add(String.format(
                    "%stenant_id%s=%s",
                    prefix,
                    suffix,
                    URLEncoder.encode(String.valueOf(getTenantId()), StandardCharsets.UTF_8)
                            .replaceAll("\\+", "%20")));
        }

        // add `sensor_id` to the URL query string
        if (getSensorId() != null) {
            joiner.add(String.format(
                    "%ssensor_id%s=%s",
                    prefix,
                    suffix,
                    URLEncoder.encode(String.valueOf(getSensorId()), StandardCharsets.UTF_8)
                            .replaceAll("\\+", "%20")));
        }

        // add `requester_id` to the URL query string
        if (getRequesterId() != null) {
            joiner.add(String.format(
                    "%srequester_id%s=%s",
                    prefix,
                    suffix,
                    URLEncoder.encode(String.valueOf(getRequesterId()), StandardCharsets.UTF_8)
                            .replaceAll("\\+", "%20")));
        }

        // add `group_id` to the URL query string
        if (getGroupId() != null) {
            joiner.add(String.format(
                    "%sgroup_id%s=%s",
                    prefix,
                    suffix,
                    URLEncoder.encode(String.valueOf(getGroupId()), StandardCharsets.UTF_8)
                            .replaceAll("\\+", "%20")));
        }

        // add `event_time` to the URL query string
        if (getEventTime() != null) {
            joiner.add(String.format(
                    "%sevent_time%s=%s",
                    prefix,
                    suffix,
                    URLEncoder.encode(String.valueOf(getEventTime()), StandardCharsets.UTF_8)
                            .replaceAll("\\+", "%20")));
        }

        return joiner.toString();
    }
}
