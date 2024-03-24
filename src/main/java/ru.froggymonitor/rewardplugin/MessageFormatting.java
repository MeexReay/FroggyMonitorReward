package ru.froggymonitor.rewardplugin;

import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.json.JSONComponentSerializer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;

public enum MessageFormatting {
    AMPERSAND("ampersand", (str) -> TextComponent.fromLegacyText(str.replace("&", "§"))),
    SECTION("section", (str) -> TextComponent.fromLegacyText(str)),
    MINIMESSAGE("minimessage", (str) -> kyoriToBungee((net.kyori.adventure.text.TextComponent) MiniMessage.miniMessage().deserialize(str))),
    JSON("json", (str) -> ComponentSerializer.parse(str));

    public interface MessageFormat {
        BaseComponent[] format(String input);
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
        return format_func.format(str);
    }

    public static MessageFormatting getFormatting(String name) {
        for (MessageFormatting f : values()) {
            if (f.getName().startsWith(name.toLowerCase())) {
                return f;
            }
        }
        return SECTION;
    }

    public static BaseComponent[] kyoriToBungee(net.kyori.adventure.text.TextComponent text) {
        return ComponentSerializer.parse(JSONComponentSerializer.json().serialize(text));
    }

    public static net.kyori.adventure.text.TextComponent bungeeToKyori(BaseComponent[] text) {
        return (net.kyori.adventure.text.TextComponent) JSONComponentSerializer.json().deserialize(ComponentSerializer.toString(text));
    }
}
