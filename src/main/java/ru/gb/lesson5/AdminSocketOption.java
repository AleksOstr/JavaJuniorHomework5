package ru.gb.lesson5;

import java.net.SocketOption;

public final class AdminSocketOption {
    private AdminSocketOption() {}

    public static final SocketOption<Boolean> ADMIN_OPTION = new AdmnSocketOption<>("Admin", Boolean.class);

    private static class AdmnSocketOption<T> implements SocketOption<T> {
        private final String name;
        private final Class<T> type;

        public AdmnSocketOption(String name, Class<T> type) {
            this.name = name;
            this.type = type;
        }

        @Override
        public String name() {
            return name;
        }

        @Override
        public Class<T> type() {
            return type;
        }

        @Override
        public String toString() {
            return name;
        }
}


}
