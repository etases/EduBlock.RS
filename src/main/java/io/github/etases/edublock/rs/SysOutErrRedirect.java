package io.github.etases.edublock.rs;

import lombok.experimental.UtilityClass;
import org.tinylog.Logger;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import java.io.OutputStream;
import java.io.PrintStream;

@UtilityClass
public final class SysOutErrRedirect {
    static void init() {
        System.setOut(new SysPrintStream(System.out, false, "[SYSOUT] "));
        System.setErr(new SysPrintStream(System.err, true, "[SYSERR] "));
    }

    private static class SysPrintStream extends PrintStream {
        private final boolean isError;
        private final String prefix;

        private SysPrintStream(@NotNull OutputStream out, boolean isError, String prefix) {
            super(out);
            this.isError = isError;
            this.prefix = prefix;
        }

        @Override
        public void println(@Nullable String x) {
            if (x == null) {
                x = "null";
            }
            String format = prefix + x;
            if (isError) {
                Logger.error(format);
            } else {
                Logger.info(format);
            }
        }
    }
}
