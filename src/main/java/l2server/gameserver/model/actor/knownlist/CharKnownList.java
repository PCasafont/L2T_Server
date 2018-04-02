/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */

package l2server.gameserver.model.actor.knownlist;

import l2server.gameserver.model.WorldObject;
import l2server.gameserver.model.actor.Creature;
import l2server.gameserver.model.actor.Summon;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.util.Util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class CharKnownList extends ObjectKnownList {
	private Map<Integer, Player> knownPlayers;
	private Map<Integer, Summon> knownSummons;
	private Map<Integer, Integer> knownRelations;

	public CharKnownList(Creature activeChar) {
		super(activeChar);
	}

	@Override
	public boolean addKnownObject(WorldObject object) {
		if (!super.addKnownObject(object)) {
			return false;
		}
		if (object instanceof Player) {
			getKnownPlayers().put(object.getObjectId(), (Player) object);
			getKnownRelations().put(object.getObjectId(), -1);
		} else if (object instanceof Summon) {
			getKnownSummons().put(object.getObjectId(), (Summon) object);
		}

		return true;
	}

	/**
	 * Return True if the Player is in knownPlayer of the Creature.<BR><BR>
	 *
	 * @param player The Player to search in knownPlayer
	 */
	public final boolean knowsThePlayer(Player player) {
		return getActiveChar() == player || getKnownPlayers().containsKey(player.getObjectId());
	}

	/**
	 * Remove all WorldObject from knownObjects and knownPlayer of the Creature then cancel Attack or Cast and notify AI.
	 */
	@Override
	public final void removeAllKnownObjects() {
		super.removeAllKnownObjects();
		getKnownPlayers().clear();
		getKnownRelations().clear();
		getKnownSummons().clear();

		// Set target of the Creature to null
		// Cancel Attack or Cast
		getActiveChar().setTarget(null);

		// Cancel AI Task
		if (getActiveChar().hasAI()) {
			getActiveChar().setAI(null);
		}
	}

	@Override
	protected boolean removeKnownObject(WorldObject object, boolean forget) {
		if (!super.removeKnownObject(object, forget)) {
			return false;
		}

		if (!forget) // on forget objects removed by iterator
		{
			if (object instanceof Player) {
				getKnownPlayers().remove(object.getObjectId());
				getKnownRelations().remove(object.getObjectId());
			} else if (object instanceof Summon) {
				getKnownSummons().remove(object.getObjectId());
			}
		}

		// If object is targeted by the Creature, cancel Attack or Cast
		if (object == getActiveChar().getTarget()) {
			getActiveChar().setTarget(null);
		}

		return true;
	}

	@Override
	public void forgetObjects(boolean fullCheck) {
		if (!fullCheck) {
			final Collection<Player> plrs = getKnownPlayers().values();
			final Iterator<Player> pIter = plrs.iterator();
			Player player;
			//synchronized (getKnownPlayers())
			{
				while (pIter.hasNext()) {
					player = pIter.next();
					if (player == null) {
						pIter.remove();
					} else if (!player.isVisible() ||
							!Util.checkIfInShortRadius(getDistanceToForgetObject(player), getActiveObject(), player, true)) {
						pIter.remove();
						removeKnownObject(player, true);
						getKnownRelations().remove(player.getObjectId());
						getKnownObjects().remove(player.getObjectId());
					}
				}
			}

			final Collection<Summon> sums = getKnownSummons().values();
			final Iterator<Summon> sIter = sums.iterator();
			Summon summon;
			//synchronized (sums)
			{
				while (sIter.hasNext()) {
					summon = sIter.next();
					if (summon == null) {
						sIter.remove();
					} else if (!summon.isVisible() ||
							!Util.checkIfInShortRadius(getDistanceToForgetObject(summon), getActiveObject(), summon, true)) {
						sIter.remove();
						removeKnownObject(summon, true);
						getKnownObjects().remove(summon.getObjectId());
					}
				}
			}
			return;
		}
		// Go through knownObjects
		final Collection<WorldObject> objs = getKnownObjects().values();
		final Iterator<WorldObject> oIter = objs.iterator();
		WorldObject object;
		//synchronized (getKnownObjects())
		{
			while (oIter.hasNext()) {
				object = oIter.next();
				if (object == null) {
					oIter.remove();
				} else if (!object.isVisible() || !Util.checkIfInShortRadius(getDistanceToForgetObject(object), getActiveObject(), object, true)) {
					oIter.remove();
					removeKnownObject(object, true);

					if (object instanceof Player) {
						getKnownPlayers().remove(object.getObjectId());
						getKnownRelations().remove(object.getObjectId());
					} else if (object instanceof Summon) {
						getKnownSummons().remove(object.getObjectId());
					}
				}
			}
		}
	}

	public Creature getActiveChar() {
		return (Creature) super.getActiveObject();
	}

	public Collection<Creature> getKnownCharacters() {
		ArrayList<Creature> result = new ArrayList<>();

		final Collection<WorldObject> objs = getKnownObjects().values();
		//synchronized (getKnownObjects())
		{
			result.addAll(objs.stream().filter(obj -> obj instanceof Creature).map(obj -> (Creature) obj).collect(Collectors.toList()));
		}
		return result;
	}

	public Collection<Creature> getKnownCharactersInRadius(long radius) {
		ArrayList<Creature> result = new ArrayList<>();

		final Collection<WorldObject> objs = getKnownObjects().values();
		//synchronized (getKnownObjects())
		{
			result.addAll(objs.stream()
					.filter(obj -> obj instanceof Creature)
					.filter(obj -> Util.checkIfInRange((int) radius, getActiveChar(), obj, true))
					.map(obj -> (Creature) obj)
					.collect(Collectors.toList()));
		}
		return result;
	}

	public final Map<Integer, Player> getKnownPlayers() {
		if (knownPlayers == null) {
			knownPlayers = new ConcurrentHashMap<>();
		}
		return knownPlayers;
	}

	public final Map<Integer, Integer> getKnownRelations() {
		if (knownRelations == null) {
			knownRelations = new ConcurrentHashMap<>();
		}
		return knownRelations;
	}

	public final Map<Integer, Summon> getKnownSummons() {
		if (knownSummons == null) {
			knownSummons = new ConcurrentHashMap<>();
		}
		return knownSummons;
	}

	public final Collection<Player> getKnownPlayersInRadius(long radius) {
		ArrayList<Player> result = new ArrayList<>();

		final Collection<Player> plrs = getKnownPlayers().values();
		//synchronized (getKnownPlayers())
		{
			result.addAll(plrs.stream()
					.filter(player -> Util.checkIfInRange((int) radius, getActiveChar(), player, true))
					.collect(Collectors.toList()));
		}
		return result;
	}
}
