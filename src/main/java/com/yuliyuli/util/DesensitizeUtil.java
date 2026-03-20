package com.yuliyuli.util;

import com.yuliyuli.enums.DesensitizeType;
import org.apache.commons.lang3.StringUtils;

public class DesensitizeUtil {

  private static final String DEFAULT_MASK = "***";

  public static String desensitize(String value, DesensitizeType type) {
    if (StringUtils.isBlank(value)) {
      return value;
    }

    switch (type) {
      case PHONE:
        return phone(value);
      case EMAIL:
        return email(value);
      case ID_CARD:
        return idCard(value);
      case NAME:
        return name(value);
      case PASSWORD:
        return password();
      case BANK_CARD:
        return bankCard(value);
      case ADDRESS:
        return address(value);
      default:
        return defaultDesensitize(value);
    }
  }

  public static String desensitize(String value, int prefixKeep, int suffixKeep) {
    if (StringUtils.isBlank(value)) {
      return value;
    }
    return custom(value, prefixKeep, suffixKeep);
  }

  public static String phone(String phone) {
    if (StringUtils.isBlank(phone)) {
      return phone;
    }
    if (phone.length() == 11) {
      return phone.substring(0, 3) + "****" + phone.substring(7);
    }
    return defaultDesensitize(phone);
  }

  public static String email(String email) {
    if (StringUtils.isBlank(email)) {
      return email;
    }
    int atIndex = email.indexOf('@');
    if (atIndex > 0) {
      String prefix = email.substring(0, atIndex);
      String suffix = email.substring(atIndex);
      if (prefix.length() <= 2) {
        return prefix.charAt(0) + "***" + suffix;
      }
      return prefix.substring(0, 2) + "***" + suffix;
    }
    return defaultDesensitize(email);
  }

  public static String idCard(String idCard) {
    if (StringUtils.isBlank(idCard)) {
      return idCard;
    }
    if (idCard.length() == 18) {
      return idCard.substring(0, 6) + "********" + idCard.substring(14);
    }
    if (idCard.length() == 15) {
      return idCard.substring(0, 6) + "*****" + idCard.substring(11);
    }
    return defaultDesensitize(idCard);
  }

  public static String name(String name) {
    if (StringUtils.isBlank(name)) {
      return name;
    }
    if (name.length() == 1) {
      return name;
    }
    if (name.length() == 2) {
      return name.charAt(0) + "*";
    }
    return name.charAt(0) + "*" + name.substring(name.length() - 1);
  }

  public static String password() {
    return "******";
  }

  public static String bankCard(String bankCard) {
    if (StringUtils.isBlank(bankCard)) {
      return bankCard;
    }
    if (bankCard.length() >= 8) {
      return bankCard.substring(0, 4) + "****" + bankCard.substring(bankCard.length() - 4);
    }
    return defaultDesensitize(bankCard);
  }

  public static String address(String address) {
    if (StringUtils.isBlank(address)) {
      return address;
    }
    if (address.length() <= 6) {
      return address.charAt(0) + "******";
    }
    return address.substring(0, 6) + "******";
  }

  public static String custom(String value, int prefixKeep, int suffixKeep) {
    if (StringUtils.isBlank(value)) {
      return value;
    }
    if (prefixKeep < 0) {
      prefixKeep = 0;
    }
    if (suffixKeep < 0) {
      suffixKeep = 0;
    }
    if (prefixKeep + suffixKeep >= value.length()) {
      return DEFAULT_MASK;
    }
    String prefix = value.substring(0, prefixKeep);
    String suffix = value.substring(value.length() - suffixKeep);
    int maskLength = value.length() - prefixKeep - suffixKeep;
    StringBuilder mask = new StringBuilder();
    for (int i = 0; i < maskLength; i++) {
      mask.append("*");
    }
    return prefix + mask + suffix;
  }

  public static String defaultDesensitize(String value) {
    if (StringUtils.isBlank(value)) {
      return value;
    }
    if (value.length() <= 2) {
      return value.charAt(0) + "*";
    }
    if (value.length() <= 4) {
      return value.substring(0, 1) + "**" + value.substring(value.length() - 1);
    }
    return value.substring(0, 2) + "****" + value.substring(value.length() - 2);
  }
}
