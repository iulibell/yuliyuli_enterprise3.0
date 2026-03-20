package com.yuliyuli.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import com.yuliyuli.annotation.Desensitize;
import com.yuliyuli.enums.DesensitizeType;
import com.yuliyuli.util.DesensitizeUtil;
import java.io.IOException;

public class DesensitizeSerializer extends JsonSerializer<String> implements ContextualSerializer {

  private DesensitizeType type;
  private int prefixKeep;
  private int suffixKeep;

  public DesensitizeSerializer() {
    this.type = DesensitizeType.DEFAULT;
    this.prefixKeep = 0;
    this.suffixKeep = 0;
  }

  public DesensitizeSerializer(DesensitizeType type, int prefixKeep, int suffixKeep) {
    this.type = type;
    this.prefixKeep = prefixKeep;
    this.suffixKeep = suffixKeep;
  }

  @Override
  public void serialize(String value, JsonGenerator gen, SerializerProvider serializers)
      throws IOException {
    if (value == null) {
      gen.writeNull();
      return;
    }

    String desensitizedValue;
    if (type != DesensitizeType.DEFAULT) {
      desensitizedValue = DesensitizeUtil.desensitize(value, type);
    } else if (prefixKeep > 0 || suffixKeep > 0) {
      desensitizedValue = DesensitizeUtil.desensitize(value, prefixKeep, suffixKeep);
    } else {
      desensitizedValue = DesensitizeUtil.defaultDesensitize(value);
    }

    gen.writeString(desensitizedValue);
  }

  @Override
  public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property)
      throws JsonMappingException {
    if (property != null) {
      Desensitize annotation = property.getAnnotation(Desensitize.class);
      if (annotation != null) {
        return new DesensitizeSerializer(annotation.value(), annotation.prefixKeep(), annotation.suffixKeep());
      }
    }
    return this;
  }
}
