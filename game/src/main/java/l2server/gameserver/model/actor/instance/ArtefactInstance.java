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

package l2server.gameserver.model.actor.instance;

import l2server.gameserver.model.Skill;
import l2server.gameserver.model.actor.Creature;
import l2server.gameserver.model.actor.Npc;
import l2server.gameserver.network.serverpackets.ActionFailed;
import l2server.gameserver.templates.chars.NpcTemplate;

/**
 * This class manages all Castle Siege Artefacts.<BR>
 * <BR>
 *
 * @version $Revision: 1.11.2.1.2.7 $ $Date: 2005/04/06 16:13:40 $
 */
public final class ArtefactInstance extends Npc {
	/**
	 * Constructor of ArtefactInstance (use Creature and NpcInstance
	 * constructor).<BR>
	 * <BR>
	 * <p>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Call the Creature constructor to set the template of the
	 * ArtefactInstance (copy skills from template to object and link
	 * calculators to NPC_STD_CALCULATOR)</li> <li>Set the name of the
	 * ArtefactInstance</li> <li>Create a RandomAnimation Task that will be
	 * launched after the calculated delay if the server allow it</li><BR>
	 * <BR>
	 *
	 * @param objectId Identifier of the object to initialized
	 */
	public ArtefactInstance(int objectId, NpcTemplate template) {
		super(objectId, template);
		setInstanceType(InstanceType.L2ArtefactInstance);
	}

	/**
	 * @see Npc#onSpawn()
	 */
	@Override
	public void onSpawn() {
		super.onSpawn();
		getCastle().registerArtefact(this);
	}

	/**
	 * Return False.<BR>
	 * <BR>
	 */
	@Override
	public boolean isAutoAttackable(Creature attacker) {
		return false;
	}

	@Override
	public boolean isAttackable() {
		return false;
	}

	@Override
	public void onForcedAttack(Player player) {
		// Send a Server->Client ActionFailed to the Player in order to avoid that the client wait another packet
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}

	@Override
	public void reduceCurrentHp(double damage, Creature attacker, Skill skill) {
	}

	@Override
	public void reduceCurrentHp(double damage, Creature attacker, boolean awake, boolean isDOT, Skill skill) {
	}
}
