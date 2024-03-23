package ru.froggymonitor.rewardplugin;

import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.json.JSONComponentSerializer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;

public enum MessageFormatting {
    AMPERSAND("ampersand", (str) -> LegacyComponentSerializer.legacyAmpersand().deserialize(str)),
    SECTION("section", (str) -> LegacyComponentSerializer.legacySection().deserialize(str)),
    MINIMESSAGE("minimessage", (str) -> (TextComponent) MiniMessage.miniMessage().deserialize(str)),
    JSON("json", (str) -> (TextComponent) JSONComponentSerializer.json().deserialize(str));

    public interface MessageFormat {
        TextComponent format(String input);
    }

    private MessageFormat format_func;
    private String name;
    MessageFormatting(String name, MessageFormat format) {
        this.name = name;
        this.format_func = format;
    }

    public String getName() {
        return name;
    }

    public BaseComponent[] format(String str) {
        return ComponentSerializer.parse(JSONComponentSerializer.json().serialize(format_func.format(str)));
    }

    public static MessageFormatting getFormatting(String name) {
        for (MessageFormatting f : values()) {
            if (f.getName().startsWith(name.toLowerCase())) {
                return f;
            }
        }
        return SECTION;
    }
}
