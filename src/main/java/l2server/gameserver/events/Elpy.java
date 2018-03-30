package l2server.gameserver.events;

import l2server.gameserver.Announcements;
import l2server.gameserver.ThreadPoolManager;
import l2server.gameserver.instancemanager.TransformationManager;
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.util.Rnd;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

/**
 * Created by Inia on 18/02/2017.
 */
public class Elpy {
	public static enum State {
		INNACTIVE,
		REGISTRATION,
		ACTIVE
	}

	public static State state = State.INNACTIVE;
	public static Vector<L2PcInstance> registered = new Vector<>();
	public static Map<Integer, Integer> elpy = new HashMap<Integer, Integer>();
	public static int runEach = 1; //Min
	public static int registTime = 60; //Seconds
	public static int time = 5; //Min
	public static int minPlayers = 2;
	public static int life = 6;
	
	public void openRegistration() {
		if (state != State.INNACTIVE) {
			return;
		}
		state = State.REGISTRATION;
		Announcements.getInstance().announceToAll("The registrations for Elpy Event are open.");
		Announcements.getInstance().announceToAll("Write .elpy to register.");
		ThreadPoolManager.getInstance().scheduleGeneral(new Run(), 1000 * registTime);
	}
	
	public void addPlayer(L2PcInstance player) {
		if (player == null) {
			return;
		}
		if (state != State.REGISTRATION) {
			player.sendMessage("You can't register for the moment.");
			return;
		}
		registered.add(player);
		elpy.put(player.getObjectId(), life);
		player.setTitle("Life : " + life);
		player.broadcastTitleInfo();
		player.broadcastUserInfo();
		player.sendMessage("You're now registered for the event!");
		return;
	}
	
	public void removePlayer(L2PcInstance player) {
		if (player == null || state != State.REGISTRATION) {
			return;
		}
		if (!registered.contains(player)) {
			player.sendMessage("You're not registered!");
			return;
		}
		registered.remove(player);
		elpy.remove(player.getObjectId());
		
		player.sendMessage("You left the event.");
		return;
	}
	
	public void runEvent() {
		if (state != State.REGISTRATION) {
			return;
		}
		if (registered.size() < minPlayers) {
			Announcements.getInstance().announceToAll("Event aborted, not enough players registered");
			state = State.INNACTIVE;
			registered.clear();
			return;
		}
		
		state = State.ACTIVE;
		Announcements.getInstance().announceToAll("Participants : " + registered.size());
		for (L2PcInstance player : registered) {
			if (player == null) {
				continue;
			}
			TransformationManager.getInstance().transformPlayer(105, player);
			player.heal();
			player.setPvpFlag(1);
			player.setPvpFlagLasts(30);
			player.setTitle("Life : " + life);
			player.broadcastUserInfo();
			player.broadcastTitleInfo();
			player.teleToLocation(-88082 + Rnd.get(400), -252843 + Rnd.get(400), -3336);
		}
		ThreadPoolManager.getInstance().scheduleGeneral(new End(), 300000);
	}
	
	public void stopEvent() {
		if (state != State.ACTIVE) {
			return;
		}
		Announcements.getInstance().announceToAll("Event finished.");
		for (L2PcInstance player : registered) {
			if (player == null) {
				continue;
			}
			player.unTransform(true);
			player.teleToLocation(-114435, 253417, -1546);
			player.setPvpFlag(0);
			player.heal();
			player.setTitle("");
			player.broadcastTitleInfo();
			player.broadcastUserInfo();
		}
		registered.clear();
		elpy.clear();
		state = State.INNACTIVE;
	}
	
	public void onAttack(L2PcInstance attacker, L2PcInstance target) {
		int hit = elpy.get(target.getObjectId());
		int atLife = elpy.get(attacker.getObjectId());
		
		L2Character one = (L2Character) attacker;
		
		if (!one.isTransformed() || !attacker.isTransformed()) {
			attacker.sendMessage("You're not an Elpy.");
			elpy.remove(attacker);
			registered.remove(attacker);
			attacker.doDie(target);
			one.doDie(target);
			attacker.teleToLocation(-114435, 253417, -1546);
			attacker.setPvpFlag(0);
			return;
		}
		
		elpy.put(target.getObjectId(), hit - 1);
		target.setTitle("Life : " + elpy.get(target.getObjectId()));
		target.broadcastUserInfo();
		target.broadcastTitleInfo();
		
		attacker.setTitle("Life : " + elpy.get(attacker.getObjectId()));
		attacker.broadcastUserInfo();
		attacker.broadcastTitleInfo();
		
		if (elpy.get(target.getObjectId()) <= 0) {
			onDie(attacker, target);
			attacker.sendMessage("You killed " + target.getName());
			int amount = (int) ((12 / elpy.size()) + 1);
			attacker.addItem("Kail's Coin", 5899, amount, attacker, true);
			if (atLife + 2 <= life) {
				elpy.put(attacker.getObjectId(), atLife + 2);
				attacker.setTitle("Life : " + elpy.get(attacker.getObjectId()));
				attacker.broadcastUserInfo();
				attacker.broadcastTitleInfo();
			} else if (atLife + 1 <= life) {
				elpy.put(attacker.getObjectId(), atLife + 1);
				attacker.setTitle("Life : " + elpy.get(attacker.getObjectId()));
				attacker.broadcastUserInfo();
				attacker.broadcastTitleInfo();
			}
			if (elpy.size() <= 1 || registered.size() <= 1) {
				stopEvent();
			}
		}
	}
	
	public void onDie(L2PcInstance attacker, L2PcInstance target) {
		target.setTitle("");
		target.doDie(attacker);
		target.unTransform(true);
		target.teleToLocation(-114435, 253417, -1546);
		target.doRevive();
		target.heal();
		target.broadcastTitleInfo();
		target.broadcastUserInfo();
		registered.remove(target);
		elpy.remove(target.getObjectId());
		if (elpy.size() <= 1) {
			Announcements.getInstance().announceToAll("King of elpies -> " + attacker.getName());
			stopEvent();
		}
	}
	
	protected Elpy() {
		if (state != State.INNACTIVE) {
			return;
		}
		openRegistration();
	}
	
	protected class Event implements Runnable {
		@Override
		public void run() {
			if (state != State.ACTIVE) {
				openRegistration();
			}
		}
	}
	
	protected class Run implements Runnable {
		@Override
		public void run() {
			runEvent();
		}
	}
	
	protected class End implements Runnable {
		@Override
		public void run() {
			if (state != State.ACTIVE) {
				return;
			}
			stopEvent();
		}
	}
	
	public static Elpy getInstance() {
		return SingletonHolder.instance;
	}
	
	private static class SingletonHolder {
		protected static final Elpy instance = new Elpy();
	}
}
