package net.dumbcode.dumblibrary.server.utils;

import lombok.experimental.UtilityClass;
import net.minecraft.client.resources.I18n;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@UtilityClass
public class InlineTranslation {

    private static final Pattern ELEMENT_MATCHER = Pattern.compile("\\$\\$\\{(.+?)(?<!\\\\)}\\[(.+?)(?<!\\\\)]");

    public static String encode(String key, String... args) {
        String joined = Arrays.stream(args).map(s -> s.replaceAll("(?=[],])", "\\\\")).collect(Collectors.joining(","));
        return "$${" + key.replaceAll("(?=[{])", "\\\\") + "}[" + joined + "]";
    }

    public static String decode(String string) {
        Matcher matcher = ELEMENT_MATCHER.matcher(string);

        String out = string;
        int offset = 0;

        while (matcher.find()) {
            String key = matcher.group(1).replaceAll("\\\\(?=\\{)", "");
            Object[] args = Arrays.stream(matcher.group(2).split("(?<!\\\\),")).map(s -> decode(s.replaceAll("\\\\(?=[],])", ""))).toArray();

            String translated = I18n.format(key, args);

            out = out.substring(0, matcher.start() - offset) + translated + out.substring(matcher.end() - offset);
            offset += matcher.group(0).length() - translated.length();
        }

        return out;

    }

}
