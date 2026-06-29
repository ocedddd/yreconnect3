package com.yreconnect;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

public class YReconnectCommand {

    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
            dispatcher.register(
                ClientCommandManager.literal("yreconnect")

                    // /yreconnect status
                    .then(ClientCommandManager.literal("status")
                        .executes(ctx -> {
                            YReconnectConfig cfg = YReconnectConfig.get();
                            String dir = cfg.triggerAbove ? "above" : "below";
                            ctx.getSource().sendFeedback(Text.literal(
                                "§b[YReconnect] §fEnabled=§e" + cfg.enabled +
                                " §f| Y=§e" + cfg.triggerY +
                                " §f| Direction=§e" + dir +
                                " §f| Delay=§e" + cfg.reconnectDelayTicks + " ticks"
                            ));
                            return 1;
                        })
                    )

                    // /yreconnect config  → opens the GUI
                    .then(ClientCommandManager.literal("config")
                        .executes(ctx -> {
                            MinecraftClient client = MinecraftClient.getInstance();
                            client.send(() -> client.setScreen(new YReconnectConfigScreen(null)));
                            return 1;
                        })
                    )

                    // /yreconnect set <y> [above]
                    .then(ClientCommandManager.literal("set")
                        .then(ClientCommandManager.argument("y", DoubleArgumentType.doubleArg())
                            .executes(ctx -> {
                                double y = DoubleArgumentType.getDouble(ctx, "y");
                                YReconnectConfig cfg = YReconnectConfig.get();
                                cfg.triggerY = y;
                                cfg.triggerAbove = false;
                                YReconnectConfig.save();
                                ctx.getSource().sendFeedback(Text.literal(
                                    "§a[YReconnect] §fWill disconnect when Y < §e" + y
                                ));
                                return 1;
                            })
                            .then(ClientCommandManager.argument("above", BoolArgumentType.bool())
                                .executes(ctx -> {
                                    double y = DoubleArgumentType.getDouble(ctx, "y");
                                    boolean above = BoolArgumentType.getBool(ctx, "above");
                                    YReconnectConfig cfg = YReconnectConfig.get();
                                    cfg.triggerY = y;
                                    cfg.triggerAbove = above;
                                    YReconnectConfig.save();
                                    ctx.getSource().sendFeedback(Text.literal(
                                        "§a[YReconnect] §fWill disconnect when Y is " +
                                        (above ? "above" : "below") + " §e" + y
                                    ));
                                    return 1;
                                })
                            )
                        )
                    )

                    // /yreconnect delay <ticks>
                    .then(ClientCommandManager.literal("delay")
                        .then(ClientCommandManager.argument("ticks", IntegerArgumentType.integer(0, 200))
                            .executes(ctx -> {
                                int ticks = IntegerArgumentType.getInteger(ctx, "ticks");
                                YReconnectConfig cfg = YReconnectConfig.get();
                                cfg.reconnectDelayTicks = ticks;
                                YReconnectConfig.save();
                                ctx.getSource().sendFeedback(Text.literal(
                                    "§a[YReconnect] §fDelay set to §e" + ticks +
                                    " ticks §7(~" + (ticks / 20.0) + "s)"
                                ));
                                return 1;
                            })
                        )
                    )

                    // /yreconnect toggle
                    .then(ClientCommandManager.literal("toggle")
                        .executes(ctx -> {
                            YReconnectConfig cfg = YReconnectConfig.get();
                            cfg.enabled = !cfg.enabled;
                            YReconnectConfig.save();
                            ctx.getSource().sendFeedback(Text.literal(
                                cfg.enabled
                                    ? "§a[YReconnect] §fEnabled."
                                    : "§c[YReconnect] §fDisabled."
                            ));
                            return 1;
                        })
                    )

                    // /yreconnect help
                    .then(ClientCommandManager.literal("help")
                        .executes(ctx -> {
                            ctx.getSource().sendFeedback(Text.literal(
                                "§b[YReconnect] §fCommands:\n" +
                                "§e/yreconnect config §f- Open GUI config screen\n" +
                                "§e/yreconnect status §f- Show current settings\n" +
                                "§e/yreconnect set <y> [above] §f- Set trigger Y\n" +
                                "§e/yreconnect delay <ticks> §f- Set reconnect delay\n" +
                                "§e/yreconnect toggle §f- Enable/disable the mod\n" +
                                "§8Press §7K §8in-game to open the config GUI."
                            ));
                            return 1;
                        })
                    )
            )
        );
    }
}
