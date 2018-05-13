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

import l2server.gameserver.model.actor.Summon;
import l2server.gameserver.model.actor.instance.PetInstance;
import l2server.gameserver.model.actor.instance.SummonInstance;
import l2server.gameserver.stats.VisualEffect;

import java.util.Set;

/**
 * This class ...
 *
 * @version $Revision: 1.6.2.5.2.12 $ $Date: 2005/03/31 09:19:16 $
 */
public class PetInfo extends L2GameServerPacket {
	//

	private Summon summon;
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
	public PetInfo(Summon summon, int val) {
		this.summon = summon;
		isSummoned = summon.isShowSummonAnimation();
		x = summon.getX();
		y = summon.getY();
		z = summon.getZ();
		heading = summon.getHeading();
		mAtkSpd = summon.getMAtkSpd();
		pAtkSpd = summon.getPAtkSpd();
		multiplier = summon.getMovementSpeedMultiplier();
		runSpd = Math.round(summon.getTemplate().getBaseRunSpd());
		walkSpd = Math.round(summon.getTemplate().getBaseWalkSpd());
		swimRunSpd = flRunSpd = flyRunSpd = runSpd;
		swimWalkSpd = flWalkSpd = flyWalkSpd = walkSpd;
		maxHp = summon.getMaxVisibleHp();
		maxMp = summon.getMaxMp();
		this.val = val;
		if (summon instanceof PetInstance) {
			PetInstance pet = (PetInstance) summon;
			curFed = pet.getCurrentFed(); // how fed it is
			maxFed = pet.getMaxFed(); //max fed it can be
		} else if (summon instanceof SummonInstance) {
			SummonInstance sum = (SummonInstance) summon;
			curFed = sum.getTimeRemaining();
			maxFed = sum.getTotalLifeTime();
		}
	}

	@Override
	protected final void writeImpl() {
		writeC(summon.getSummonType());
		writeD(summon.getObjectId());
		writeD(summon.getTemplate().TemplateId + 1000000);

		writeD(x);
		writeD(y);
		writeD(z);
		writeD(heading);
		writeD(mAtkSpd);
		writeD(pAtkSpd);
		writeH(runSpd);
		writeH(walkSpd);
		writeH(swimRunSpd);
		writeH(swimWalkSpd);
		writeH(flRunSpd);
		writeH(flWalkSpd);
		writeH(flyRunSpd);
		writeH(flyWalkSpd);

		writeF(multiplier); // movement multiplier
		writeF(summon.getAttackSpeedMultiplier()); // attack speed multiplier
		writeF(summon.getTemplate().getFCollisionRadius());
		writeF(summon.getTemplate().getFCollisionHeight());
		writeD(summon.getWeapon()); // right hand weapon
		writeD(summon.getArmor()); // body armor
		writeD(0); // left hand weapon
		writeC(isSummoned ? 2 : val); //  0=teleported  1=default   2=summoned
		writeD(-1); // High Five NPCStringId
		writeS(summon.getName()); // summon name
		writeD(-1); // High Five NPCStringId
		writeS(summon.getTitle()); // owner name
		writeC(summon.getOwner() != null ? summon.getOwner().getPvpFlag() : 0); //0 = white,2= purpleblink, if its greater then karma = purple
		writeD(summon.getOwner() != null ? summon.getOwner().getReputation() : 0); // karma
		writeD(curFed); // how fed it is
		writeD(maxFed); //max fed it can be
		writeD((int) summon.getCurrentHp());//current hp
		writeD(maxHp);// max hp
		writeD((int) summon.getCurrentMp());//current mp
		writeD(maxMp);//max mp
		writeQ(summon.getStat().getSp()); //sp
		writeC(summon.getLevel());// lvl
		writeQ(summon.getStat().getExp());

		if (summon.getExpForThisLevel() > summon.getStat().getExp()) {
			writeQ(summon.getStat().getExp());// 0%  absolute value
		} else {
			writeQ(summon.getExpForThisLevel());// 0%  absolute value
		}

		writeQ(summon.getExpForNextLevel());// 100% absoulte value
		writeD(summon instanceof PetInstance ? summon.getInventory().getTotalWeight() : 0);//weight
		writeD(summon.getMaxLoad());//max weight it can carry
		writeD(summon.getPAtk(null));//patk
		writeD(summon.getPDef(null));//pdef
		writeD(summon.getAccuracy());//accuracy
		writeD(summon.getEvasionRate(null));//evasion
		writeD(summon.getCriticalHit(null, null));//critical
		writeD(summon.getMAtk(null, null));//matk
		writeD(summon.getMDef(null, null));//mdef
		writeD(summon.getMAccuracy()); // M. Accuracy
		writeD(summon.getMEvasionRate(null)); // M. Evasion
		//log.info(summon.getMEvasionRate(null)); // M. Evasion
		writeD(summon.getMCriticalHit(null, null)); // M. Critical
		writeD((int) summon.getStat().getMoveSpeed());//speed
		writeD(summon.getPAtkSpd());//atkspeed
		writeD(summon.getMAtkSpd());//casting speed

		int npcId = summon.getTemplate().NpcId;
		writeC(summon.isMountable() ? 1 : 0);//c2	ride button

		writeC(summon.getOwner() != null ? summon.getOwner().getTeam() : 0); // team aura (1 = blue, 2 = red)
		writeC(summon.getSoulShotsPerHit()); // How many soulshots this servitor uses per hit
		writeC(summon.getSpiritShotsPerHit()); // How many spiritshots this servitor uses per hit

		int form = 0;
		if (npcId == 16041 || npcId == 16042) {
			if (summon.getLevel() > 84) {
				form = 3;
			} else if (summon.getLevel() > 79) {
				form = 2;
			} else if (summon.getLevel() > 74) {
				form = 1;
			}
		} else if (npcId == 16025 || npcId == 16037) {
			if (summon.getLevel() > 69) {
				form = 3;
			} else if (summon.getLevel() > 64) {
				form = 2;
			} else if (summon.getLevel() > 59) {
				form = 1;
			}
		}
		writeD(form);//CT1.5 Pet form and skills

		writeH(0x00); // ???
		writeH(0x00); // ???

		writeC(summon.getOwner() != null ? summon.getOwner().getSpentSummonPoints() : 0); // Consumed summon points
		writeC(summon.getOwner() != null ? summon.getOwner().getMaxSummonPoints() : 0); // Maximum summon points

		Set<Integer> abnormal = summon.getAbnormalEffect();
		if (summon.getOwner().getAppearance().getInvisible()) {
			abnormal.add(VisualEffect.STEALTH.getId());
		}
		writeH(abnormal.size());
		for (int abnormalId : abnormal) {
			writeH(abnormalId);
		}

		writeC(0x06); // ???
	}
}
