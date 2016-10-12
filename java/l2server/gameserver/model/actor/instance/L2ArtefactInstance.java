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

import l2server.gameserver.model.L2Skill;
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.L2Npc;
import l2server.gameserver.network.serverpackets.ActionFailed;
import l2server.gameserver.templates.chars.L2NpcTemplate;

/**
 * This class manages all Castle Siege Artefacts.<BR>
 * <BR>
 *
 * @version $Revision: 1.11.2.1.2.7 $ $Date: 2005/04/06 16:13:40 $
 */
public final class L2ArtefactInstance extends L2Npc
{
	/**
	 * Constructor of L2ArtefactInstance (use L2Character and L2NpcInstance
	 * constructor).<BR>
	 * <BR>
	 * <p>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Call the L2Character constructor to set the _template of the
	 * L2ArtefactInstance (copy skills from template to object and link
	 * _calculators to NPC_STD_CALCULATOR)</li> <li>Set the name of the
	 * L2ArtefactInstance</li> <li>Create a RandomAnimation Task that will be
	 * launched after the calculated delay if the server allow it</li><BR>
	 * <BR>
	 *
	 * @param objectId Identifier of the object to initialized
	 */
	public L2ArtefactInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
		setInstanceType(InstanceType.L2ArtefactInstance);
	}

	/**
	 * @see l2server.gameserver.model.actor.L2Npc#onSpawn()
	 */
	@Override
	public void onSpawn()
	{
		super.onSpawn();
		getCastle().registerArtefact(this);
	}

	/**
	 * Return False.<BR>
	 * <BR>
	 */
	@Override
	public boolean isAutoAttackable(L2Character attacker)
	{
		return false;
	}

	@Override
	public boolean isAttackable()
	{
		return false;
	}

	@Override
	public void onForcedAttack(L2PcInstance player)
	{
		// Send a Server->Client ActionFailed to the L2PcInstance in order to avoid that the client wait another packet
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}

	@Override
	public void reduceCurrentHp(double damage, L2Character attacker, L2Skill skill)
	{
	}

	@Override
	public void reduceCurrentHp(double damage, L2Character attacker, boolean awake, boolean isDOT, L2Skill skill)
	{
	}
}
