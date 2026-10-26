package com.naizo.elementals.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.naizo.elementals.compat.Compat;
import com.naizo.elementals.core.BuildCode;
import com.naizo.elementals.core.PowerProfile;
import com.naizo.elementals.core.Progression;
import com.naizo.elementals.core.School;
import com.naizo.elementals.core.SkillNode;
import com.naizo.elementals.integration.RasBridge;
import com.naizo.elementals.power.PowerManager;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;

/** /powers: player info and admin tools. */
public final class PowersCommands {
    private PowersCommands() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal("powers")
                .executes(ctx -> info(ctx.getSource(), ctx.getSource().getPlayerOrException()))
                .then(Commands.literal("info")
                        .executes(ctx -> info(ctx.getSource(), ctx.getSource().getPlayerOrException()))
                        .then(Commands.argument("player", EntityArgument.player()).requires(s -> Compat.hasPermission(s, 2))
                                .executes(ctx -> info(ctx.getSource(), EntityArgument.getPlayer(ctx, "player")))))
                .then(Commands.literal("build")
                        .executes(ctx -> build(ctx.getSource(), ctx.getSource().getPlayerOrException()))
                        .then(Commands.argument("player", EntityArgument.player()).requires(s -> Compat.hasPermission(s, 2))
                                .executes(ctx -> build(ctx.getSource(), EntityArgument.getPlayer(ctx, "player")))))
                .then(Commands.literal("points").requires(s -> Compat.hasPermission(s, 2))
                        .then(amount("add", (p, n) -> PowerManager.profile(p).bonusPoints += n))
                        .then(amount("remove", (p, n) -> PowerManager.profile(p).bonusPoints -= n))
                        .then(amount("set", (p, n) -> {
                            PowerProfile profile = PowerManager.profile(p);
                            profile.bonusPoints += n - Progression.availablePoints(PowerManager.CONFIG, PowerManager.TREE, profile, RasBridge.isLoaded());
                        })))
                .then(Commands.literal("level").requires(s -> Compat.hasPermission(s, 2))
                        .then(amount("add", (p, n) -> PowerManager.profile(p).level = Math.min(PowerManager.CONFIG.maxLevel, Math.max(0, PowerManager.profile(p).level + n))))
                        .then(amount("set", (p, n) -> PowerManager.profile(p).level = Math.min(PowerManager.CONFIG.maxLevel, Math.max(0, n)))))
                .then(Commands.literal("xp").requires(s -> Compat.hasPermission(s, 2))
                        .then(amount("add", PowerManager::addXp)))
                .then(Commands.literal("reset").requires(s -> Compat.hasPermission(s, 2))
                        .then(Commands.argument("targets", EntityArgument.players())
                                .executes(ctx -> each(ctx, "targets", p -> PowerManager.reset(p, true), "commands.elementals.reset"))))
                .then(Commands.literal("cooldowns").requires(s -> Compat.hasPermission(s, 2))
                        .then(Commands.argument("targets", EntityArgument.players())
                                .executes(ctx -> each(ctx, "targets", p -> {
                                    PowerProfile profile = PowerManager.profile(p);
                                    profile.cooldowns.clear();
                                    profile.essence = PowerManager.maxEssence(p);
                                }, "commands.elementals.cooldowns"))))
                .then(Commands.literal("origin").requires(s -> Compat.hasPermission(s, 2))
                        .then(Commands.argument("targets", EntityArgument.players())
                                .then(Commands.argument("school", StringArgumentType.word())
                                        .suggests((ctx, builder) -> SharedSuggestionProvider.suggest(Arrays.stream(School.playable()).map(s -> s.id), builder))
                                        .executes(ctx -> origin(ctx)))))
                .then(Commands.literal("grant").requires(s -> Compat.hasPermission(s, 2))
                        .then(Commands.argument("targets", EntityArgument.players())
                                .then(Commands.argument("node", StringArgumentType.string())
                                        .suggests((ctx, builder) -> SharedSuggestionProvider.suggest(PowerManager.TREE.nodes().stream().map(n -> n.id), builder))
                                        .executes(PowersCommands::grant))))
                .then(Commands.literal("cast").requires(s -> Compat.hasPermission(s, 2))
                        .then(Commands.argument("targets", EntityArgument.players())
                                .then(Commands.argument("ability", StringArgumentType.word())
                                        .suggests((ctx, builder) -> SharedSuggestionProvider.suggest(com.naizo.elementals.core.AbilityCatalog.all().keySet(), builder))
                                        .executes(ctx -> {
                                            String ability = StringArgumentType.getString(ctx, "ability");
                                            int cast = 0;
                                            for (ServerPlayer player : EntityArgument.getPlayers(ctx, "targets")) {
                                                if (com.naizo.elementals.ability.AbilityRunner.forceCast(player, ability)) {
                                                    cast++;
                                                }
                                            }
                                            int count = cast;
                                            ctx.getSource().sendSuccess(() -> Component.translatable("commands.elementals.cast", ability, count), true);
                                            return cast;
                                        }))))
                .then(Commands.literal("reload").requires(s -> Compat.hasPermission(s, 2))
                        .executes(ctx -> {
                            List<String> warnings = PowerManager.reload();
                            ctx.getSource().sendSuccess(() -> Component.translatable("commands.elementals.reloaded", PowerManager.TREE.size()), true);
                            for (String warning : warnings) {
                                ctx.getSource().sendFailure(Component.literal(warning));
                            }
                            return 1;
                        }));
        dispatcher.register(root);
        dispatcher.register(Commands.literal("skills")
                .executes(ctx -> info(ctx.getSource(), ctx.getSource().getPlayerOrException()))
                .redirect(dispatcher.getRoot().getChild("powers")));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> amount(String name, BiConsumer<ServerPlayer, Integer> action) {
        return Commands.literal(name).then(Commands.argument("targets", EntityArgument.players())
                .then(Commands.argument("amount", IntegerArgumentType.integer(0))
                        .executes(ctx -> {
                            int amount = IntegerArgumentType.getInteger(ctx, "amount");
                            return each(ctx, "targets", p -> action.accept(p, amount), "commands.elementals.updated");
                        })));
    }

    private interface PlayerAction {
        void run(ServerPlayer player) throws CommandSyntaxException;
    }

    private static int each(CommandContext<CommandSourceStack> ctx, String argument, PlayerAction action, String message) throws CommandSyntaxException {
        Collection<ServerPlayer> players = EntityArgument.getPlayers(ctx, argument);
        for (ServerPlayer player : players) {
            action.run(player);
            PowerManager.refresh(player);
            PowerManager.sendProfile(player);
            PowerManager.save(player);
        }
        ctx.getSource().sendSuccess(() -> Component.translatable(message, players.size()), true);
        return players.size();
    }

    private static int origin(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        School school = School.byId(StringArgumentType.getString(ctx, "school"));
        if (school == School.NONE) {
            ctx.getSource().sendFailure(Component.translatable("commands.elementals.unknown_school"));
            return 0;
        }
        int changed = 0;
        for (ServerPlayer player : EntityArgument.getPlayers(ctx, "targets")) {
            PowerProfile profile = PowerManager.profile(player);
            if (!profile.allocated.isEmpty()) {
                PowerManager.reset(player, true);
            }
            if (PowerManager.grantOrigin(profile, school)) {
                changed++;
                PowerManager.refresh(player);
                PowerManager.sendProfile(player);
                PowerManager.save(player);
            }
        }
        int count = changed;
        ctx.getSource().sendSuccess(() -> Component.translatable("commands.elementals.origin", school.displayName, count), true);
        return changed;
    }

    private static int grant(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        String node = StringArgumentType.getString(ctx, "node");
        if (!PowerManager.TREE.contains(node)) {
            ctx.getSource().sendFailure(Component.translatable("message.elementals.unknown_node", node));
            return 0;
        }
        int granted = 0;
        for (ServerPlayer player : EntityArgument.getPlayers(ctx, "targets")) {
            PowerProfile profile = PowerManager.profile(player);
            List<String> path = PowerManager.TREE.pathTo(profile.allocated, node);
            if (path.isEmpty()) {
                continue;
            }
            profile.bonusPoints += PowerManager.TREE.cost(path);
            List<String> target = new ArrayList<>(profile.allocated);
            target.addAll(path);
            PowerManager.commit(player, target);
            granted++;
        }
        int count = granted;
        ctx.getSource().sendSuccess(() -> Component.translatable("commands.elementals.granted", node, count), true);
        return granted;
    }

    private static int info(CommandSourceStack source, ServerPlayer player) {
        PowerProfile profile = PowerManager.profile(player);
        boolean ras = RasBridge.isLoaded();
        int earned = Progression.earnedPoints(PowerManager.CONFIG, profile, ras);
        int spent = Progression.spentPoints(PowerManager.TREE, profile);
        MutableComponent text = Component.literal("").append(Component.literal(player.getScoreboardName()).withStyle(ChatFormatting.GOLD))
                .append(Component.literal(" | " + (profile.origin.isEmpty() ? "No origin" : profile.originSchool().displayName)).withStyle(ChatFormatting.YELLOW));
        source.sendSuccess(() -> text, false);
        String mode = PowerManager.CONFIG.effectiveRasMode(ras);
        if (!"replace".equals(mode)) {
            source.sendSuccess(() -> Component.translatable("commands.elementals.info.level", profile.level, Math.round(profile.xp),
                    Math.round(Progression.xpToNext(PowerManager.CONFIG, profile.level))), false);
        }
        if (ras) {
            source.sendSuccess(() -> Component.translatable("commands.elementals.info.ras", profile.rasLevel, mode), false);
        }
        source.sendSuccess(() -> Component.translatable("commands.elementals.info.points", earned - spent, spent, earned), false);
        Set<String> abilities = new LinkedHashSet<>(PowerManager.sheet(player).abilities);
        source.sendSuccess(() -> Component.translatable("commands.elementals.info.abilities", abilities.isEmpty() ? "-" : String.join(", ", abilities)), false);
        source.sendSuccess(() -> Component.translatable("commands.elementals.info.keystones",
                PowerManager.sheet(player).keystones.isEmpty() ? "-" : String.join(", ", PowerManager.sheet(player).keystones)), false);
        return 1;
    }

    private static int build(CommandSourceStack source, ServerPlayer player) {
        String code = BuildCode.encode(PowerManager.TREE, PowerManager.profile(player).allocated);
        source.sendSuccess(() -> Component.translatable("commands.elementals.build", player.getScoreboardName()).append(" ")
                .append(Compat.copyable(code, code)), false);
        return 1;
    }

    static String describe(SkillNode node) {
        return node.name + " (" + node.id + ")";
    }
}
