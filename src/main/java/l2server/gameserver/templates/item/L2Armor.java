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

package l2server.gameserver.templates.item;

import gnu.trove.TIntObjectHashMap;
import l2server.gameserver.model.L2ItemInstance;
import l2server.gameserver.model.L2Skill;
import l2server.gameserver.stats.SkillHolder;
import l2server.gameserver.stats.funcs.Func;
import l2server.gameserver.stats.funcs.FuncTemplate;
import l2server.gameserver.templates.StatsSet;
import l2server.log.Log;
import l2server.util.StringUtil;

import java.util.ArrayList;

/**
 * This class is dedicated to the management of armors.
 *
 * @version $Revision: 1.2.2.1.2.6 $ $Date: 2005/03/27 15:30:10 $
 */
public final class L2Armor extends L2Item
{
	public static final int MAX_ENCHANT_SKILL = 10;

	private TIntObjectHashMap<SkillHolder> enchantSkills = new TIntObjectHashMap<>();
	// skill that activates when armor is enchanted +X
	// private final String[] skill;
	private L2ArmorType type;
	private int[] armorSet;

	/**
	 * Constructor for Armor.<BR><BR>
	 * <U><I>Variables filled :</I></U><BR>
	 * <LI>avoidModifier</LI>
	 * <LI>pDef & mDef</LI>
	 * <LI>mpBonus & hpBonus</LI>
	 * <LI>enchant4Skill</LI>
	 *
	 * @param set : StatsSet designating the set of couples (key,value) caracterizing the armor
	 * @see L2Item constructor
	 */
	public L2Armor(StatsSet set)
	{
		super(set);
		type = L2ArmorType.valueOf(set.getString("armorType", "none").toUpperCase());

		int bodyPart = getBodyPart();
		if (bodyPart == L2Item.SLOT_NECK || bodyPart == L2Item.SLOT_HAIR || bodyPart == L2Item.SLOT_HAIR2 ||
				bodyPart == L2Item.SLOT_HAIRALL || (bodyPart & L2Item.SLOT_L_EAR) != 0 ||
				(bodyPart & L2Item.SLOT_L_FINGER) != 0 || (bodyPart & L2Item.SLOT_R_BRACELET) != 0 ||
				(bodyPart & L2Item.SLOT_L_BRACELET) != 0 || (bodyPart & L2Item.SLOT_BACK) != 0 ||
				(bodyPart & L2Item.SLOT_BROOCH) != 0)
		{
			type1 = L2Item.TYPE1_WEAPON_RING_EARRING_NECKLACE;
			type2 = L2Item.TYPE2_ACCESSORY;
		}
		else
		{
			if (type == L2ArmorType.NONE && getBodyPart() == L2Item.SLOT_L_HAND) // retail define shield as NONE
			{
				type = L2ArmorType.SHIELD;
			}
			type1 = L2Item.TYPE1_SHIELD_ARMOR;
			type2 = L2Item.TYPE2_SHIELD_ARMOR;
		}

		String sets = set.getString("armorSet", null);
		if (sets != null)
		{
			String[] setsSplit = sets.split(";");
			armorSet = new int[setsSplit.length];
			int used = 0;

			for (String element : setsSplit)
			{
				try
				{
					armorSet[used] = Integer.parseInt(element);
					used++;
				}
				catch (Exception e)
				{
					Log.warning(
							StringUtil.concat("Failed to parse armorSet(", element, ") for item ", toString(), "!"));
				}
			}
		}

		for (int enchant = 1; enchant <= 10; enchant++)
		{
			String skill = set.getString("enchant" + enchant + "Skill", null);
			if (skill != null)
			{
				String[] info = skill.split("-");

				if (info != null && info.length == 2)
				{
					int id = 0;
					int level = 0;
					try
					{
						id = Integer.parseInt(info[0]);
						level = Integer.parseInt(info[1]);
					}
					catch (Exception nfe)
					{
						// Incorrect syntax, dont add new skill
						Log.info(StringUtil
								.concat("> Couldnt parse ", skill, " in armor enchant skills! item ", toString()));
					}
					if (id > 0 && level > 0)
					{
						enchantSkills.put(enchant, new SkillHolder(id, level));
					}
				}
			}
		}
	}

	/**
	 * Returns the type of the armor.
	 *
	 * @return L2ArmorType
	 */
	@Override
	public L2ArmorType getItemType()
	{
		return type;
	}

	/**
	 * Returns the ID of the item after applying the mask.
	 *
	 * @return int : ID of the item
	 */
	@Override
	public final int getItemMask()
	{
		return getItemType().mask();
	}

	/**
	 * Returns skill that player get when has equiped armor +4  or more
	 *
	 * @return
	 */
	public L2Skill getEnchantSkill(int enchant)
	{
		SkillHolder sh = enchantSkills.get(enchant);
		if (sh == null)
		{
			return null;
		}

		return sh.getSkill();
	}

	public int[] getArmorSet()
	{
		return armorSet;
	}

	public boolean isArmorSetPart(int armorSet)
	{
		if (this.armorSet == null)
		{
			return false;
		}

		for (int set : this.armorSet)
		{
			if (set == armorSet)
			{
				return true;
			}
		}

		return false;
	}

	/**
	 * Returns array of Func objects containing the list of functions used by the armor
	 *
	 * @param instance : L2ItemInstance pointing out the armor
	 * @return Func[] : array of functions
	 */
	@Override
	public Func[] getStatFuncs(L2ItemInstance instance)
	{
		if (funcTemplates == null || funcTemplates.length == 0)
		{
			return emptyFunctionSet;
		}

		ArrayList<Func> funcs = new ArrayList<>(funcTemplates.length);

		Func f;
		for (FuncTemplate t : funcTemplates)
		{
			f = t.getFunc(instance);
			if (f != null)
			{
				funcs.add(f);
			}
		}

		return funcs.toArray(new Func[funcs.size()]);
	}
}
