package io.vertx.ext.asyncsql;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

/**
 * @author <a href="http://www.campudus.com">Joern Bernhardt</a>.
 */
@DataObject
public class SelectOptions {
  private JsonArray fields;
  private Integer limit;
  private Integer offset;

  public SelectOptions() {
    this.fields = new JsonArray();
  }

  public SelectOptions(SelectOptions other) {
    this.fields = other.fields;
    this.limit = other.limit;
    this.offset = other.offset;
  }

  public SelectOptions(JsonObject json) {
    this.fields = json.getJsonArray("fields", new JsonArray());
    this.limit = json.getInteger("limit");
    this.offset = json.getInteger("offset");
  }

  public JsonArray getFields() {
    return fields;
  }

  public SelectOptions setFields(JsonArray fields) {
    this.fields = fields;
    return this;
  }

  public Integer getLimit() {
    return limit;
  }

  public SelectOptions setLimit(Integer limit) {
    this.limit = limit;
    return this;
  }

  public Integer getOffset() {
    return offset;
  }

  public SelectOptions setOffset(Integer offset) {
    this.offset = offset;
    return this;
  }

  public JsonObject toJson() {
    JsonObject json = new JsonObject();
    json.put("fields", fields);

    if (limit != null) {
      json.put("limit", limit);
    }
    if (offset != null) {
      json.put("offset", offset);
    }

    return json;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    SelectOptions that = (SelectOptions) o;

    if (!fields.equals(that.fields)) return false;
    if (limit != null ? !limit.equals(that.limit) : that.limit != null) return false;
    if (offset != null ? !offset.equals(that.offset) : that.offset != null) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = fields.hashCode();
    result = 31 * result + (limit != null ? limit.hashCode() : 0);
    result = 31 * result + (offset != null ? offset.hashCode() : 0);
    return result;
  }
}
