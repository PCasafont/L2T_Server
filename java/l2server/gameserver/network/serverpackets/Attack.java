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

import l2server.gameserver.model.L2ItemInstance;
import l2server.gameserver.model.L2Object;
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.instance.L2PcInstance;

/**
 * sample
 * 06 8f19904b 2522d04b 00000000 80 950c0000 4af50000 08f2ffff 0000	- 0 damage (missed 0x80)
 * 06 85071048 bc0e504b 32000000 10 fc41ffff fd240200 a6f5ffff 0100 bc0e504b 33000000 10									 3....
 * <p>
 * format
 * dddc dddh (ddc)
 *
 * @version $Revision: 1.3.2.1.2.4 $ $Date: 2005/03/27 15:29:39 $
 */
public class Attack extends L2GameServerPacket
{
	/*public static final int HITFLAG_USESS = 0x10;
	public static final int HITFLAG_CRIT = 0x20;
	public static final int HITFLAG_SHLD = 0x40;
	public static final int HITFLAG_MISS = 0x80;
	 */
	public static final int HITFLAG_MISS = 0x01;
	public static final int HITFLAG_SHLD = 0x02;
	public static final int HITFLAG_CRIT = 0x04;
	public static final int HITFLAG_USESS = 0x08;

	public class Hit
	{
		protected final int targetId;
		protected final int damage;
		protected int flags;
		protected int ssGrade;

		Hit(L2Object target, int damage, boolean miss, boolean crit, byte shld)
		{
			this.targetId = target.getObjectId();
			this.damage = damage;
			if (miss)
			{
				this.flags = HITFLAG_MISS;
				return;
			}
			if (crit)
			{
				this.flags |= HITFLAG_CRIT;
			}
			if (soulshotCharge > L2ItemInstance.CHARGED_NONE)
			{
				this.flags |= HITFLAG_USESS;
				this.ssGrade = Attack.this.ssGrade;
			}
			// dirty fix for lags on olympiad
			if (shld > 0 && !(target instanceof L2PcInstance && ((L2PcInstance) target).isInOlympiadMode()))
			{
				this.flags |= HITFLAG_SHLD;
			}
			//			if (shld > 0)
			//				this.flags |= HITFLAG_SHLD;
		}
	}

	private final int attackerObjId;
	private final int targetObjId;
	public final double soulshotCharge;
	public final int ssGrade;
	private final int x;
	private final int y;
	private final int z;
	private final int tx;
	private final int ty;
	private final int tz;
	private Hit[] hits;

	/**
	 * @param attacker: the attacking L2Character<br>
	 * @param target:   the target L2Object<br>
	 * @param ssCharge: true if soulshots used
	 * @param ssGrade:  the grade of the soulshots
	 */
	public Attack(L2Character attacker, L2Object target, double ssCharge, int ssGrade)
	{
		this.attackerObjId = attacker.getObjectId();
		this.targetObjId = target.getObjectId();
		soulshotCharge = ssCharge;
		this.ssGrade = ssGrade > 6 ? 6 : ssGrade;
		this.x = attacker.getX();
		this.y = attacker.getY();
		this.z = attacker.getZ();
		this.tx = target.getX();
		this.ty = target.getY();
		this.tz = target.getZ();
	}

	public Hit createHit(L2Object target, int damage, boolean miss, boolean crit, byte shld)
	{
		return new Hit(target, damage, miss, crit, shld);
	}

	public void hit(Hit... hits)
	{
		if (this.hits == null)
		{
			this.hits = hits;
			return;
		}

		// this will only happen with pole attacks
		Hit[] tmp = new Hit[hits.length + this.hits.length];
		System.arraycopy(this.hits, 0, tmp, 0, this.hits.length);
		System.arraycopy(hits, 0, tmp, this.hits.length, hits.length);
		this.hits = tmp;
	}

	/**
	 * Return True if the Server-Client packet Attack contains at least 1 hit.<BR><BR>
	 */
	public boolean hasHits()
	{
		return this.hits != null;
	}

	@Override
	protected final void writeImpl()
	{
		writeD(this.attackerObjId);
		writeD(this.targetObjId);
		writeD(0); // ???
		writeD(this.hits[0].damage);
		writeD(this.hits[0].flags); // GoD ??? 0 normal, 1 miss, 2 move a bit, 4 crit
		writeD(this.hits[0].ssGrade); // Soulshot ID
		writeD(this.x);
		writeD(this.y);
		writeD(this.z);

		writeH(this.hits.length - 1);
		// prevent sending useless packet while there is only one target.
		if (this.hits.length > 1)
		{
			for (int i = 1; i < this.hits.length; i++)
			{
				writeD(this.hits[i].targetId);
				writeD(this.hits[i].damage);
				writeD(this.hits[0].flags); // GoD ??? 0 normal, 1 miss, 2 move a bit, 4 crit
				writeD(this.hits[0].ssGrade); // Soulshot ID
			}
		}

		writeD(this.tx);
		writeD(this.ty);
		writeD(this.tz);
	}
}
