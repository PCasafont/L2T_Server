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

package l2server.gameserver.network.serverpackets;

import l2server.Config;
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.L2Npc;
import l2server.gameserver.model.actor.L2Trap;
import l2server.gameserver.model.actor.instance.L2CloneInstance;
import l2server.gameserver.model.actor.instance.L2MonsterInstance;
import l2server.gameserver.stats.VisualEffect;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Set;

/**
 * @author Pere
 */
public final class NpcInfo extends L2GameServerPacket
{
	private int _objectId;
	private int _val;
	private byte[] _data1;
	private byte[] _data2;
	private Set<Integer> _abnormals;

	public NpcInfo(L2Npc npc, L2Character attacker)
	{
		if (npc.getClonedPlayer() != null)
		{
			attacker.sendPacket(new ClonedPlayerInfo(npc, npc.getClonedPlayer()));
			return;
		}

		_objectId = npc.getObjectId();
		_val = npc.isShowSummonAnimation() ? 2 : 0;

		ByteBuffer buffer = ByteBuffer.allocate(200).order(ByteOrder.LITTLE_ENDIAN);

		buffer.put((byte) (npc.isAttackable() ? 1 : 0));
		buffer.putInt(0);
		String title = npc.getTitle();
		if (Config.SHOW_NPC_LVL && npc instanceof L2MonsterInstance)
		{
			String t = "Lv " + npc.getLevel() + (npc.getAggroRange() > 0 ? "*" : "");

			title = npc.getTitle();

			if (title != null)
			{
				t += " " + title;
			}

			title = t;
		}
		else if (Config.L2JMOD_CHAMPION_ENABLE && npc.isChampion())
		{
			title = Config.L2JMOD_CHAMP_TITLE;
		}
		else if (npc.getTemplate().ServerSideTitle)
		{
			title = npc.getTemplate().Title;
		}
		if (title != null)
		{
			for (char c : title.toCharArray())
			{
				buffer.putShort((short) c);
			}
		}
		buffer.putShort((short) 0);

		int size = buffer.position();
		buffer.position(0);
		_data1 = new byte[size];
		buffer.get(_data1, 0, size);

		buffer = ByteBuffer.allocate(500).order(ByteOrder.LITTLE_ENDIAN);

		// Write data to the buffer
		buffer.putInt(npc.getTemplate().TemplateId + 1000000);
		buffer.putInt(npc.getX());
		buffer.putInt(npc.getY());
		buffer.putInt(npc.getZ());
		buffer.putInt(npc.getHeading());
		buffer.putInt(0x00); // ???
		buffer.putInt(npc.getPAtkSpd());
		buffer.putInt(npc.getMAtkSpd());
		buffer.putFloat(npc.getMovementSpeedMultiplier());
		buffer.putFloat(npc.getAttackSpeedMultiplier());

		buffer.putInt(npc.getRightHandItem());
		buffer.putInt(0); //chest
		buffer.putInt(npc.getLeftHandItem());

		if (npc.getNpcId() == 18672)
		{
			buffer.put((byte) 0); // To make the cube stop jumping
		}
		else
		{
			buffer.put((byte) 1);
		}
		buffer.put((byte) (npc.isRunning() ? 1 : 0));
		buffer.put((byte) 0); // If not 0, mobs fall inside the ground (swimming?)

		buffer.put((byte) 0x00); // Team
		buffer.putInt(0x00); // Weapon enchant level

		buffer.putInt(npc.isFlying() ? 0x01 : 0x00);
		buffer.putInt(npc.getNpcId() >= 13302 && npc.getNpcId() <= 13305 ?
				npc.getOwner() != null ? npc.getOwner().getObjectId() : 0x01 : 0x00); // Cloned player
		buffer.putInt(0x01); // ???
		buffer.putInt(npc.getDisplayEffect());
		buffer.putInt(0x00); // Transform id

		buffer.putInt((int) Math.round(npc.getCurrentHp()));
		buffer.putInt((int) Math.round(npc.getCurrentMp()));
		buffer.putInt(npc.getMaxHp());
		buffer.putInt(npc.getMaxMp());

		buffer.putInt(0x00); // ???
		buffer.putInt(0x00); // ???

		buffer.put((byte) 0); // ???

		String name = null;
		if (npc.getTemplate().ServerSideName)
		{
			name = npc.getName();
		}
		if (name != null)
		{
			for (char c : name.toCharArray())
			{
				buffer.putShort((short) c);
			}
		}
		buffer.putShort((short) 0);

		buffer.putInt(-1); // Name NpcStringId
		buffer.putInt(-1); // Title NpcStringId
		buffer.put((byte) 0); // PvP Flag
		buffer.putInt(0); // Reputation
		/*if (npc.getOwner() != null)
        {
			buffer.putInt(npc.getOwner().getClanId());
			buffer.putInt(npc.getOwner().getClanCrestId());
			buffer.putInt(npc.getOwner().getClanCrestLargeId());
		}
		else*/
		{
			buffer.putInt(0);
			buffer.putInt(0);
			buffer.putInt(0);
		}
		buffer.putInt(-1); // ???
		buffer.putInt(0); // ???

		// Flag with bools
		// 0x00000001 unk
		// 0x00000002 dead
		// 0x00000004 targetable
		// 0x00000008 show name
		// 0x00000010 unk
		// 0x00000020 unk
		// 0x00000040 unk
		byte flag = 0x00;
		if (npc.isAlikeDead())
		{
			flag |= 0x02;
		}
		if (npc.getTemplate().Targetable)
		{
			flag |= 0x04;
		}
		if (npc.getTemplate().ShowName)
		{
			flag |= 0x08;
		}
		buffer.put(flag);

		size = buffer.position();
		buffer.position(0);
		_data2 = new byte[size];
		buffer.get(_data2, 0, size);

		_abnormals = npc.getAbnormalEffect();
		if (npc.isChampion())
		{
			_abnormals.add(VisualEffect.AQUA_BIG_BODY.getId());
		}
		if (npc.getNpcId() >= 40000 && npc.getNpcId() < 40006 && npc.getInstanceId() == 0)
		{
			_abnormals.add(VisualEffect.BIG_BODY.getId());
		}
	}

	public NpcInfo(L2CloneInstance npc)
	{
		_objectId = npc.getObjectId();
		_val = npc.isShowSummonAnimation() ? 2 : 0;

		ByteBuffer buffer = ByteBuffer.allocate(200).order(ByteOrder.LITTLE_ENDIAN);

		buffer.put((byte) (npc.isAttackable() ? 1 : 0));
		buffer.putInt(0);
		String title = "";
		for (char c : title.toCharArray())
		{
			buffer.putShort((short) c);
		}

		buffer.putShort((short) 0);

		int size = buffer.position();
		buffer.position(0);
		_data1 = new byte[size];
		buffer.get(_data1, 0, size);

		buffer = ByteBuffer.allocate(500).order(ByteOrder.LITTLE_ENDIAN);

		// Write data to the buffer
		buffer.putInt(npc.getTemplate().TemplateId + 1000000);
		buffer.putInt(npc.getX());
		buffer.putInt(npc.getY());
		buffer.putInt(npc.getZ());
		buffer.putInt(npc.getHeading());
		buffer.putInt(0x00); // ???
		buffer.putInt(npc.getPAtkSpd());
		buffer.putInt(npc.getMAtkSpd());
		buffer.putFloat(npc.getMovementSpeedMultiplier());
		buffer.putFloat(npc.getAttackSpeedMultiplier());

		buffer.putInt(0);
		buffer.putInt(0); //chest
		buffer.putInt(0);

		buffer.put((byte) 1);
		buffer.put((byte) (npc.isRunning() ? 1 : 0));
		buffer.put((byte) 0); // If not 0, mobs fall inside the ground (swimming?)

		buffer.put((byte) 0x00); // Team
		buffer.putInt(0x00); // Weapon enchant level

		buffer.putInt(npc.isFlying() ? 0x01 : 0x00);
		buffer.putInt(npc.getNpcId() >= 13302 && npc.getNpcId() <= 13305 ?
				npc.getOwner() != null ? npc.getOwner().getObjectId() : 0x01 : 0x00); // Cloned player
		buffer.putInt(0x00); // ???
		buffer.putInt(0x00);
		buffer.putInt(0x00); // Transform id

		buffer.putInt((int) Math.round(npc.getCurrentHp()));
		buffer.putInt((int) Math.round(npc.getCurrentMp()));
		buffer.putInt(npc.getMaxHp());
		buffer.putInt(npc.getMaxMp());

		buffer.putInt(0x00); // ???
		buffer.putInt(0x00); // ???

		buffer.put((byte) 0); // ???

		String name = npc.getOwner().getName();
		if (name != null)
		{
			for (char c : name.toCharArray())
			{
				buffer.putShort((short) c);
			}
		}
		buffer.putShort((short) 0);

		buffer.putInt(-1); // Name NpcStringId
		buffer.putInt(-1); // Title NpcStringId
		buffer.put(npc.getOwner().getPvpFlag()); // PvP Flag
		buffer.putInt(0); // Reputation
        /*if (npc.getOwner() != null)
		{
			buffer.putInt(npc.getOwner().getClanId());
			buffer.putInt(npc.getOwner().getClanCrestId());
			buffer.putInt(npc.getOwner().getClanCrestLargeId());
		}
		else*/
		{
			buffer.putInt(0);
			buffer.putInt(0);
			buffer.putInt(0);
		}
		buffer.putInt(-1); // ???
		buffer.putInt(0); // ???

		// Flag with bools
		// 0x00000001 unk
		// 0x00000002 dead
		// 0x00000004 targetable
		// 0x00000008 show name
		// 0x00000010 unk
		// 0x00000020 unk
		// 0x00000040 unk
		byte flag = 0x00;
		if (npc.isAlikeDead())
		{
			flag |= 0x02;
		}
		if (npc.getTemplate().Targetable)
		{
			flag |= 0x04;
		}
		if (npc.getTemplate().ShowName)
		{
			flag |= 0x08;
		}
		buffer.put(flag);

		size = buffer.position();
		buffer.position(0);
		_data2 = new byte[size];
		buffer.get(_data2, 0, size);

		_abnormals = npc.getAbnormalEffect();
	}

	public NpcInfo(L2Trap trap)
	{
		_objectId = trap.getObjectId();

		_data1 = new byte[7];

		ByteBuffer buffer = ByteBuffer.allocate(500).order(ByteOrder.LITTLE_ENDIAN);

		// Write data to the buffer
		buffer.putInt(trap.getTemplate().TemplateId + 1000000);
		buffer.putInt(trap.getX());
		buffer.putInt(trap.getY());
		buffer.putInt(trap.getZ());
		buffer.putInt(trap.getHeading());
		buffer.putInt(0x00); // ???
		buffer.putInt(trap.getPAtkSpd());
		buffer.putInt(trap.getMAtkSpd());
		buffer.putFloat(trap.getMovementSpeedMultiplier());
		buffer.putFloat(trap.getAttackSpeedMultiplier());

		buffer.putInt(0);
		buffer.putInt(0); //chest
		buffer.putInt(0);

		buffer.put((byte) 1);

		buffer.put((byte) 1);
		buffer.put((byte) 0); // If not 0, mobs fall inside the ground (swimming/flying?)

		buffer.put((byte) 0x00); // Team
		buffer.putInt(0x00); // Weapon enchant level

		buffer.putInt(0x00); // If not 0, mobs are half underground
		buffer.putInt(0x00); // If positive, npcs are not visible at all
		buffer.putInt(0x01); // ???
		buffer.putInt(0x00); // Display Effect
		buffer.putInt(0x00); // Transform id
		buffer.putInt(0x00); // ???

		buffer.putInt(0x00); // ???
		buffer.putInt((int) Math.round(trap.getCurrentHp()));
		buffer.putInt(trap.getMaxHp());

		buffer.putInt(0x00); // ???
		buffer.putInt(0x00); // ???

		buffer.put((byte) 0); // ???

		String name = null;
		if (trap.getTemplate().ServerSideName)
		{
			name = trap.getTemplate().Name;
		}
		if (name != null)
		{
			for (char c : name.toCharArray())
			{
				buffer.putShort((short) c);
			}
		}
		buffer.putShort((short) 0);

		buffer.putInt(-1); // Name NpcStringId

		buffer.putInt(-1); // Title NpcStringId
		buffer.put((byte) 0); // ???
		buffer.putInt(0); // Reputation
		buffer.putInt(0); // ???
		buffer.putInt(0); // ???
		buffer.put((byte) 0); // ???
		buffer.put((byte) 0); // ???
		buffer.put((byte) 0); // ???

		buffer.putInt(-1); // ???

		buffer.put((byte) -1); // ???

		buffer.putInt(0); // ???

		buffer.put((byte) 3);

		int size = buffer.position();
		buffer.position(0);
		_data2 = new byte[size];
		buffer.get(_data2, 0, size);

		_abnormals = trap.getAbnormalEffect();
	}

	@Override
	protected final void writeImpl()
	{
		if (_data1 == null)
		{
			return;
		}

		writeD(_objectId);
		writeC(_val); // 0=teleported 1=default 2=summoned
		writeH(0x25);
		writeC(0xff);
		writeC(0xff);
		writeC(0xff);
		writeC(0xff);
		writeC(0xff);

		writeC(_data1.length);
		writeB(_data1);

		writeH(_data2.length);
		writeB(_data2);

		writeH(_abnormals.size());
		for (int abnormal : _abnormals)
		{
			writeH(abnormal);
		}
	}
}
