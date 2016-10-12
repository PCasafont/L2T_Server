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

	private L2Summon _summon;
	private int _x, _y, _z, _heading;
	private boolean _isSummoned;
	private int _val;
	private int _mAtkSpd, _pAtkSpd;
	private int _runSpd, _walkSpd, _swimRunSpd, _swimWalkSpd, _flRunSpd, _flWalkSpd, _flyRunSpd, _flyWalkSpd;
	private int _maxHp, _maxMp;
	private int _maxFed, _curFed;
	private float _multiplier;

	/**
	 * rev 478  dddddddddddddddddddffffdddcccccSSdddddddddddddddddddddddddddhc
	 */
	public PetInfo(L2Summon summon, int val)
	{
		_summon = summon;
		_isSummoned = _summon.isShowSummonAnimation();
		_x = _summon.getX();
		_y = _summon.getY();
		_z = _summon.getZ();
		_heading = _summon.getHeading();
		_mAtkSpd = _summon.getMAtkSpd();
		_pAtkSpd = _summon.getPAtkSpd();
		_multiplier = _summon.getMovementSpeedMultiplier();
		_runSpd = Math.round(_summon.getTemplate().baseRunSpd);
		_walkSpd = Math.round(_summon.getTemplate().baseWalkSpd);
		_swimRunSpd = _flRunSpd = _flyRunSpd = _runSpd;
		_swimWalkSpd = _flWalkSpd = _flyWalkSpd = _walkSpd;
		_maxHp = _summon.getMaxVisibleHp();
		_maxMp = _summon.getMaxMp();
		_val = val;
		if (_summon instanceof L2PetInstance)
		{
			L2PetInstance pet = (L2PetInstance) _summon;
			_curFed = pet.getCurrentFed(); // how fed it is
			_maxFed = pet.getMaxFed(); //max fed it can be
		}
		else if (_summon instanceof L2SummonInstance)
		{
			L2SummonInstance sum = (L2SummonInstance) _summon;
			_curFed = sum.getTimeRemaining();
			_maxFed = sum.getTotalLifeTime();
		}
	}

	@Override
	protected final void writeImpl()
	{
		writeC(_summon.getSummonType());
		writeD(_summon.getObjectId());
		writeD(_summon.getTemplate().TemplateId + 1000000);

		writeD(_x);
		writeD(_y);
		writeD(_z);
		writeD(_heading);
		writeD(_mAtkSpd);
		writeD(_pAtkSpd);
		writeH(_runSpd);
		writeH(_walkSpd);
		writeH(_swimRunSpd);
		writeH(_swimWalkSpd);
		writeH(_flRunSpd);
		writeH(_flWalkSpd);
		writeH(_flyRunSpd);
		writeH(_flyWalkSpd);

		writeF(_multiplier); // movement multiplier
		writeF(_summon.getAttackSpeedMultiplier()); // attack speed multiplier
		writeF(_summon.getTemplate().fCollisionRadius);
		writeF(_summon.getTemplate().fCollisionHeight);
		writeD(_summon.getWeapon()); // right hand weapon
		writeD(_summon.getArmor()); // body armor
		writeD(0); // left hand weapon
		writeC(_isSummoned ? 2 : _val); //  0=teleported  1=default   2=summoned
		writeD(-1); // High Five NPCStringId
		writeS(_summon.getName()); // summon name
		writeD(-1); // High Five NPCStringId
		writeS(_summon.getTitle()); // owner name
		writeC(_summon.getOwner() != null ? _summon.getOwner().getPvpFlag() :
				0); //0 = white,2= purpleblink, if its greater then karma = purple
		writeD(_summon.getOwner() != null ? _summon.getOwner().getReputation() : 0); // karma
		writeD(_curFed); // how fed it is
		writeD(_maxFed); //max fed it can be
		writeD((int) _summon.getCurrentHp());//current hp
		writeD(_maxHp);// max hp
		writeD((int) _summon.getCurrentMp());//current mp
		writeD(_maxMp);//max mp
		writeQ(_summon.getStat().getSp()); //sp
		writeC(_summon.getLevel());// lvl
		writeQ(_summon.getStat().getExp());

		if (_summon.getExpForThisLevel() > _summon.getStat().getExp())
		{
			writeQ(_summon.getStat().getExp());// 0%  absolute value
		}
		else
		{
			writeQ(_summon.getExpForThisLevel());// 0%  absolute value
		}

		writeQ(_summon.getExpForNextLevel());// 100% absoulte value
		writeD(_summon instanceof L2PetInstance ? _summon.getInventory().getTotalWeight() : 0);//weight
		writeD(_summon.getMaxLoad());//max weight it can carry
		writeD(_summon.getPAtk(null));//patk
		writeD(_summon.getPDef(null));//pdef
		writeD(_summon.getAccuracy());//accuracy
		writeD(_summon.getEvasionRate(null));//evasion
		writeD(_summon.getCriticalHit(null, null));//critical
		writeD(_summon.getMAtk(null, null));//matk
		writeD(_summon.getMDef(null, null));//mdef
		writeD(_summon.getMAccuracy()); // M. Accuracy
		writeD(_summon.getMEvasionRate(null)); // M. Evasion
		//Log.info(_summon.getMEvasionRate(null)); // M. Evasion
		writeD(_summon.getMCriticalHit(null, null)); // M. Critical
		writeD((int) _summon.getStat().getMoveSpeed());//speed
		writeD(_summon.getPAtkSpd());//atkspeed
		writeD(_summon.getMAtkSpd());//casting speed

		int npcId = _summon.getTemplate().NpcId;
		writeC(_summon.isMountable() ? 1 : 0);//c2	ride button

		writeC(_summon.getOwner() != null ? _summon.getOwner().getTeam() : 0); // team aura (1 = blue, 2 = red)
		writeC(_summon.getSoulShotsPerHit()); // How many soulshots this servitor uses per hit
		writeC(_summon.getSpiritShotsPerHit()); // How many spiritshots this servitor uses per hit

		int form = 0;
		if (npcId == 16041 || npcId == 16042)
		{
			if (_summon.getLevel() > 84)
			{
				form = 3;
			}
			else if (_summon.getLevel() > 79)
			{
				form = 2;
			}
			else if (_summon.getLevel() > 74)
			{
				form = 1;
			}
		}
		else if (npcId == 16025 || npcId == 16037)
		{
			if (_summon.getLevel() > 69)
			{
				form = 3;
			}
			else if (_summon.getLevel() > 64)
			{
				form = 2;
			}
			else if (_summon.getLevel() > 59)
			{
				form = 1;
			}
		}
		writeD(form);//CT1.5 Pet form and skills

		writeH(0x00); // ???
		writeH(0x00); // ???

		writeC(_summon.getOwner() != null ? _summon.getOwner().getSpentSummonPoints() : 0); // Consumed summon points
		writeC(_summon.getOwner() != null ? _summon.getOwner().getMaxSummonPoints() : 0); // Maximum summon points

		Set<Integer> abnormal = _summon.getAbnormalEffect();
		if (_summon.getOwner().getAppearance().getInvisible())
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
