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

import l2server.gameserver.model.actor.L2Summon;
import l2server.gameserver.model.actor.instance.L2PetInstance;
import l2server.gameserver.model.actor.instance.L2SummonInstance;
import l2server.gameserver.stats.VisualEffect;

import java.util.Set;

/**
 * This class ...
 *
 * @version $Revision: 1.6.2.5.2.12 $ $Date: 2005/03/31 09:19:16 $
 */
public class PetInfo extends L2GameServerPacket
{
	//

	private L2Summon summon;
	private int x, y, z, heading;
	private boolean isSummoned;
	private int val;
	private int mAtkSpd, pAtkSpd;
	private int runSpd, walkSpd, swimRunSpd, swimWalkSpd, flRunSpd, flWalkSpd, flyRunSpd, flyWalkSpd;
	private int maxHp, maxMp;
	private int maxFed, curFed;
	private float multiplier;

	/**
	 * rev 478  dddddddddddddddddddffffdddcccccSSdddddddddddddddddddddddddddhc
	 */
	public PetInfo(L2Summon summon, int val)
	{
		this.summon = summon;
		this.isSummoned = this.summon.isShowSummonAnimation();
		this.x = this.summon.getX();
		this.y = this.summon.getY();
		this.z = this.summon.getZ();
		this.heading = this.summon.getHeading();
		this.mAtkSpd = this.summon.getMAtkSpd();
		this.pAtkSpd = this.summon.getPAtkSpd();
		this.multiplier = this.summon.getMovementSpeedMultiplier();
		this.runSpd = Math.round(this.summon.getTemplate().baseRunSpd);
		this.walkSpd = Math.round(this.summon.getTemplate().baseWalkSpd);
		this.swimRunSpd = this.flRunSpd = this.flyRunSpd = this.runSpd;
		this.swimWalkSpd = this.flWalkSpd = this.flyWalkSpd = this.walkSpd;
		this.maxHp = this.summon.getMaxVisibleHp();
		this.maxMp = this.summon.getMaxMp();
		this.val = val;
		if (this.summon instanceof L2PetInstance)
		{
			L2PetInstance pet = (L2PetInstance) this.summon;
			this.curFed = pet.getCurrentFed(); // how fed it is
			this.maxFed = pet.getMaxFed(); //max fed it can be
		}
		else if (this.summon instanceof L2SummonInstance)
		{
			L2SummonInstance sum = (L2SummonInstance) this.summon;
			this.curFed = sum.getTimeRemaining();
			this.maxFed = sum.getTotalLifeTime();
		}
	}

	@Override
	protected final void writeImpl()
	{
		writeC(this.summon.getSummonType());
		writeD(this.summon.getObjectId());
		writeD(this.summon.getTemplate().TemplateId + 1000000);

		writeD(this.x);
		writeD(this.y);
		writeD(this.z);
		writeD(this.heading);
		writeD(this.mAtkSpd);
		writeD(this.pAtkSpd);
		writeH(this.runSpd);
		writeH(this.walkSpd);
		writeH(this.swimRunSpd);
		writeH(this.swimWalkSpd);
		writeH(this.flRunSpd);
		writeH(this.flWalkSpd);
		writeH(this.flyRunSpd);
		writeH(this.flyWalkSpd);

		writeF(this.multiplier); // movement multiplier
		writeF(this.summon.getAttackSpeedMultiplier()); // attack speed multiplier
		writeF(this.summon.getTemplate().fCollisionRadius);
		writeF(this.summon.getTemplate().fCollisionHeight);
		writeD(this.summon.getWeapon()); // right hand weapon
		writeD(this.summon.getArmor()); // body armor
		writeD(0); // left hand weapon
		writeC(this.isSummoned ? 2 : this.val); //  0=teleported  1=default   2=summoned
		writeD(-1); // High Five NPCStringId
		writeS(this.summon.getName()); // summon name
		writeD(-1); // High Five NPCStringId
		writeS(this.summon.getTitle()); // owner name
		writeC(this.summon.getOwner() != null ? this.summon.getOwner().getPvpFlag() :
				0); //0 = white,2= purpleblink, if its greater then karma = purple
		writeD(this.summon.getOwner() != null ? this.summon.getOwner().getReputation() : 0); // karma
		writeD(this.curFed); // how fed it is
		writeD(this.maxFed); //max fed it can be
		writeD((int) this.summon.getCurrentHp());//current hp
		writeD(this.maxHp);// max hp
		writeD((int) this.summon.getCurrentMp());//current mp
		writeD(this.maxMp);//max mp
		writeQ(this.summon.getStat().getSp()); //sp
		writeC(this.summon.getLevel());// lvl
		writeQ(this.summon.getStat().getExp());

		if (this.summon.getExpForThisLevel() > summon.getStat().getExp())
		{
			writeQ(this.summon.getStat().getExp());// 0%  absolute value
		}
		else
		{
			writeQ(this.summon.getExpForThisLevel());// 0%  absolute value
		}

		writeQ(this.summon.getExpForNextLevel());// 100% absoulte value
		writeD(this.summon instanceof L2PetInstance ? this.summon.getInventory().getTotalWeight() : 0);//weight
		writeD(this.summon.getMaxLoad());//max weight it can carry
		writeD(this.summon.getPAtk(null));//patk
		writeD(this.summon.getPDef(null));//pdef
		writeD(this.summon.getAccuracy());//accuracy
		writeD(this.summon.getEvasionRate(null));//evasion
		writeD(this.summon.getCriticalHit(null, null));//critical
		writeD(this.summon.getMAtk(null, null));//matk
		writeD(this.summon.getMDef(null, null));//mdef
		writeD(this.summon.getMAccuracy()); // M. Accuracy
		writeD(this.summon.getMEvasionRate(null)); // M. Evasion
		//Log.info(this.summon.getMEvasionRate(null)); // M. Evasion
		writeD(this.summon.getMCriticalHit(null, null)); // M. Critical
		writeD((int) this.summon.getStat().getMoveSpeed());//speed
		writeD(this.summon.getPAtkSpd());//atkspeed
		writeD(this.summon.getMAtkSpd());//casting speed

		int npcId = this.summon.getTemplate().NpcId;
		writeC(this.summon.isMountable() ? 1 : 0);//c2	ride button

		writeC(this.summon.getOwner() != null ? this.summon.getOwner().getTeam() : 0); // team aura (1 = blue, 2 = red)
		writeC(this.summon.getSoulShotsPerHit()); // How many soulshots this servitor uses per hit
		writeC(this.summon.getSpiritShotsPerHit()); // How many spiritshots this servitor uses per hit

		int form = 0;
		if (npcId == 16041 || npcId == 16042)
		{
			if (this.summon.getLevel() > 84)
			{
				form = 3;
			}
			else if (this.summon.getLevel() > 79)
			{
				form = 2;
			}
			else if (this.summon.getLevel() > 74)
			{
				form = 1;
			}
		}
		else if (npcId == 16025 || npcId == 16037)
		{
			if (this.summon.getLevel() > 69)
			{
				form = 3;
			}
			else if (this.summon.getLevel() > 64)
			{
				form = 2;
			}
			else if (this.summon.getLevel() > 59)
			{
				form = 1;
			}
		}
		writeD(form);//CT1.5 Pet form and skills

		writeH(0x00); // ???
		writeH(0x00); // ???

		writeC(this.summon.getOwner() != null ? this.summon.getOwner().getSpentSummonPoints() : 0); // Consumed summon points
		writeC(this.summon.getOwner() != null ? this.summon.getOwner().getMaxSummonPoints() : 0); // Maximum summon points

		Set<Integer> abnormal = this.summon.getAbnormalEffect();
		if (this.summon.getOwner().getAppearance().getInvisible())
		{
			abnormal.add(VisualEffect.STEALTH.getId());
		}
		writeH(abnormal.size());
		for (int abnormalId : abnormal)
		{
			writeH(abnormalId);
		}

		writeC(0x06); // ???
	}
}
