package com.hybridash.haunteditems;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.random.Random;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Once per second, each player has a small chance of something creepy happening to their inventory.
 * Everything runs on the server using vanilla sounds, so it works on a server without players installing it.
 */
public class HauntedItems implements ModInitializer {
	public static final String MOD_ID = "haunteditems";
	public static final Logger LOGGER = LoggerFactory.getLogger("HauntedItems");
	public static final Random RANDOM = Random.create();

	private static HauntConfig config;
	private int ticks = 0;

	@Override
	public void onInitialize() {
		config = HauntConfig.load();
		ServerTickEvents.END_SERVER_TICK.register(this::onServerTick);

		// /haunt [player] [event] - ops can trigger a haunting to test it (or to mess with a friend)
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
				dispatcher.register(CommandManager.literal("haunt")
						.requires(source -> source.hasPermissionLevel(2))
						.executes(ctx -> {
							Haunting.random(ctx.getSource().getPlayerOrThrow());
							return 1;
						})
						.then(CommandManager.argument("target", EntityArgumentType.player())
								.executes(ctx -> {
									Haunting.random(EntityArgumentType.getPlayer(ctx, "target"));
									return 1;
								})
								.then(CommandManager.argument("event", StringArgumentType.word())
										.suggests((ctx, builder) -> {
											for (Haunting h : Haunting.values()) builder.suggest(h.id);
											return builder.buildFuture();
										})
										.executes(ctx -> {
											String id = StringArgumentType.getString(ctx, "event");
											Haunting h = Haunting.byId(id);
											if (h == null) {
												ctx.getSource().sendError(Text.literal("Unknown haunting: " + id));
												return 0;
											}
											h.run(EntityArgumentType.getPlayer(ctx, "target"));
											return 1;
										})))));

		LOGGER.info("HauntedItems loaded. Your items are listening. (1 in {} per second)", config.oddsPerSecond);
	}

	private void onServerTick(MinecraftServer server) {
		Scheduler.tick();
		if (++ticks < 20) return; // 20 ticks = 1 second
		ticks = 0;

		for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
			if (player.isSpectator() || player.isCreative() || !player.isAlive()) continue;
			if (RANDOM.nextInt(config.oddsPerSecond) == 0) {
				Haunting.random(player);
			}
		}
	}

	public static HauntConfig config() {
		return config;
	}
}
