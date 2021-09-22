package com.life4ever.shadowsocks4j.proxy.util;

public class EnumUtil {

    private EnumUtil() {
    }

    public static <E extends Enum<E>> E getEnumIgnoreCase(Class<E> enumClass, String enumName, E defaultEnum) {
        if (!enumClass.isEnum() || enumName == null) {
            return defaultEnum;
        }

        for (final E element : enumClass.getEnumConstants()) {
            if (element.name().equalsIgnoreCase(enumName)) {
                return element;
            }
        }

        return defaultEnum;
    }

}
