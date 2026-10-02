package com.hybridash.haunteditems;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.network.packet.s2c.play.UpdateSelectedSlotS2CPacket;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Every creepy thing that can happen. Sounds are sent only to the haunted player, so nobody else hears them.
 */
public enum Haunting {
	/** A random item in your inventory "whispers". */
	WHISPER("whisper", false, p -> {
		ItemStack stack = randomItem(p, false);
		if (stack == null) return;
		play(p, SoundEvents.AMBIENT_CAVE, p.getPos(), 0.35f, 1.4f);
		actionBar(p, "Your " + stack.getName().getString() + " whispers something you can't quite hear...");
	}),

	/** Two items in your inventory quietly trade places. */
	SWAP("swap", true, p -> {
		List<Integer> slots = filledSlots(p, false);
		if (slots.size() < 2) return;
		int a = slots.remove(HauntedItems.RANDOM.nextInt(slots.size()));
		int b = slots.get(HauntedItems.RANDOM.nextInt(slots.size()));
		PlayerInventory inv = p.getInventory();
		ItemStack tmp = inv.main.get(a);
		inv.main.set(a, inv.main.get(b));
		inv.main.set(b, tmp);
		inv.markDirty();
		play(p, SoundEvents.ENTITY_ITEM_FRAME_ROTATE_ITEM, p.getPos(), 0.4f, 0.6f);
		if (HauntedItems.RANDOM.nextInt(3) == 0) actionBar(p, "Did something just move?");
	}),

	/** Your hand slides to a different hotbar slot on its own. */
	NUDGE("nudge", true, p -> {
		PlayerInventory inv = p.getInventory();
		int slot = (inv.selectedSlot + (HauntedItems.RANDOM.nextBoolean() ? 1 : 8)) % 9;
		inv.selectedSlot = slot;
		p.networkHandler.sendPacket(new UpdateSelectedSlotS2CPacket(slot));
		play(p, SoundEvents.ENTITY_ITEM_PICKUP, p.getPos(), 0.2f, 0.5f);
	}),

	/** You hear an item get picked up. You didn't pick anything up. */
	PHANTOM_PICKUP("phantom_pickup", false, p ->
			play(p, SoundEvents.ENTITY_ITEM_PICKUP, behind(p, 1.5), 0.5f, 0.8f)),

	/** Footsteps behind you that stop when you'd turn around. */
	FOOTSTEPS("footsteps", false, p -> {
		for (int i = 0; i < 4; i++) {
			int step = i;
			Scheduler.later(step * 9, () -> {
				if (!p.isAlive() || p.isDisconnected()) return;
				play(p, SoundEvents.BLOCK_STONE_STEP, behind(p, 4.0 - step * 0.6), 0.45f, 0.8f);
			});
		}
	}),

	/** A chest opens and closes nearby. There's no chest. */
	CHEST("chest", false, p -> {
		Vec3d spot = behind(p, 3.5);
		play(p, SoundEvents.BLOCK_CHEST_OPEN, spot, 0.5f, 0.9f);
		Scheduler.later(30, () -> {
			if (p.isAlive() && !p.isDisconnected()) play(p, SoundEvents.BLOCK_CHEST_CLOSE, spot, 0.5f, 0.9f);
		});
	}),

	/** The item in your hand gets cold for a moment. */
	COLD("cold", false, p -> {
		ItemStack held = p.getMainHandStack();
		if (held.isEmpty()) return;
		play(p, SoundEvents.BLOCK_GLASS_HIT, p.getPos(), 0.3f, 1.8f);
		actionBar(p, "Your " + held.getName().getString() + " feels ice cold.");
	});

	public final String id;
	private final boolean movesItems;
	private final Consumer<ServerPlayerEntity> action;

	Haunting(String id, boolean movesItems, Consumer<ServerPlayerEntity> action) {
		this.id = id;
		this.movesItems = movesItems;
		this.action = action;
	}

	public void run(ServerPlayerEntity player) {
		HauntedItems.LOGGER.debug("Haunting {} with {}", player.getName().getString(), id);
		action.accept(player);
	}

	public static void random(ServerPlayerEntity player) {
		List<Haunting> allowed = new ArrayList<>();
		for (Haunting h : values()) {
			if (!h.movesItems || HauntedItems.config().allowItemMoving) allowed.add(h);
		}
		allowed.get(HauntedItems.RANDOM.nextInt(allowed.size())).run(player);
	}

	public static Haunting byId(String id) {
		for (Haunting h : values()) {
			if (h.id.equalsIgnoreCase(id)) return h;
		}
		return null;
	}

	// ---- helpers --------------------------------------------------------------------------

	/** Main inventory slots (hotbar + backpack) that have something in them. */
	private static List<Integer> filledSlots(ServerPlayerEntity p, boolean hotbarOnly) {
		List<Integer> out = new ArrayList<>();
		PlayerInventory inv = p.getInventory();
		int end = hotbarOnly ? 9 : inv.main.size();
		for (int i = 0; i < end; i++) {
			if (!inv.main.get(i).isEmpty()) out.add(i);
		}
		return out;
	}

	private static ItemStack randomItem(ServerPlayerEntity p, boolean hotbarOnly) {
		List<Integer> slots = filledSlots(p, hotbarOnly);
		if (slots.isEmpty()) return null;
		return p.getInventory().main.get(slots.get(HauntedItems.RANDOM.nextInt(slots.size())));
	}

	/** A point behind the player's back. */
	private static Vec3d behind(ServerPlayerEntity p, double distance) {
		Vec3d look = p.getRotationVector().multiply(1, 0, 1);
		if (look.lengthSquared() < 1.0E-4) look = new Vec3d(0, 0, 1);
		return p.getPos().subtract(look.normalize().multiply(distance));
	}

	private static void play(ServerPlayerEntity p, SoundEvent sound, Vec3d at, float volume, float pitch) {
		play(p, Registries.SOUND_EVENT.getEntry(sound), at, volume, pitch);
	}

	private static void play(ServerPlayerEntity p, RegistryEntry<SoundEvent> sound, Vec3d at, float volume, float pitch) {
		p.networkHandler.sendPacket(new PlaySoundS2CPacket(sound, SoundCategory.AMBIENT,
				at.x, at.y, at.z, volume, pitch, HauntedItems.RANDOM.nextLong()));
	}

	private static void actionBar(ServerPlayerEntity p, String message) {
		p.sendMessage(Text.literal(message).formatted(Formatting.DARK_GRAY, Formatting.ITALIC), true);
	}
}
