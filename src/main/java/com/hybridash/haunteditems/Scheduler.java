package com.hybridash.haunteditems;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/** Runs things a few ticks later (for footsteps and chest sounds that play one after another). */
public final class Scheduler {
	private static final List<Task> TASKS = new ArrayList<>();

	private Scheduler() {}

	public static void later(int ticks, Runnable action) {
		TASKS.add(new Task(ticks, action));
	}

	static void tick() {
		if (TASKS.isEmpty()) return;
		List<Runnable> due = new ArrayList<>();
		for (Iterator<Task> it = TASKS.iterator(); it.hasNext(); ) {
			Task t = it.next();
			if (--t.ticksLeft <= 0) {
				due.add(t.action);
				it.remove();
			}
		}
		due.forEach(Runnable::run);
	}

	private static final class Task {
		int ticksLeft;
		final Runnable action;

		Task(int ticksLeft, Runnable action) {
			this.ticksLeft = ticksLeft;
			this.action = action;
		}
	}
}
