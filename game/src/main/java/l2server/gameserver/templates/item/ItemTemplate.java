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

import l2server.Config;
import l2server.gameserver.datatables.ItemTable;
import l2server.gameserver.model.*;
import l2server.gameserver.model.actor.Creature;
import l2server.gameserver.model.actor.Summon;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.gameserver.stats.Env;
import l2server.gameserver.stats.SkillHolder;
import l2server.gameserver.stats.conditions.Condition;
import l2server.gameserver.stats.conditions.ConditionLogicOr;
import l2server.gameserver.stats.conditions.ConditionPetType;
import l2server.gameserver.stats.funcs.Func;
import l2server.gameserver.stats.funcs.FuncTemplate;
import l2server.gameserver.templates.StatsSet;
import l2server.gameserver.templates.skills.AbnormalTemplate;

import java.util.ArrayList;
import java.util.List;

/**
 * This class contains all informations concerning the item (weapon, armor, etc).<BR>
 * Mother class of :
 * <LI>ArmorTemplate</LI>
 * <LI>EtcItemTemplate</LI>
 * <LI>WeaponTemplate</LI>
 *
 * @version $Revision: 1.7.2.2.2.5 $ $Date: 2005/04/06 18:25:18 $
 */
public abstract class ItemTemplate {
	public static final int TYPE1_WEAPON_RING_EARRING_NECKLACE = 0;
	public static final int TYPE1_SHIELD_ARMOR = 1;
	public static final int TYPE1_ITEM_QUESTITEM_ADENA = 4;

	public static final int TYPE2_WEAPON = 0;
	public static final int TYPE2_SHIELD_ARMOR = 1;
	public static final int TYPE2_ACCESSORY = 2;
	public static final int TYPE2_QUEST = 3;
	public static final int TYPE2_MONEY = 4;
	public static final int TYPE2_OTHER = 5;

	public static final int WOLF = 0x1;
	public static final int HATCHLING = 0x2;
	public static final int STRIDER = 0x4;
	public static final int BABY = 0x8;
	public static final int IMPROVED_BABY = 0x10;
	public static final int GROWN_WOLF = 0x20;
	public static final int ALL_WOLF = 0x21;
	public static final int ALL_PET = 0x3F;

	public static final int SLOT_NONE = 0x0000;
	public static final int SLOT_UNDERWEAR = 0x0001;
	public static final int SLOT_R_EAR = 0x0002;
	public static final int SLOT_L_EAR = 0x0004;
	public static final int SLOT_LR_EAR = 0x00006;
	public static final int SLOT_NECK = 0x0008;
	public static final int SLOT_R_FINGER = 0x0010;
	public static final int SLOT_L_FINGER = 0x0020;
	public static final int SLOT_LR_FINGER = 0x0030;
	public static final int SLOT_HEAD = 0x0040;
	public static final int SLOT_R_HAND = 0x0080;
	public static final int SLOT_L_HAND = 0x0100;
	public static final int SLOT_GLOVES = 0x0200;
	public static final int SLOT_CHEST = 0x0400;
	public static final int SLOT_LEGS = 0x0800;
	public static final int SLOT_FEET = 0x1000;
	public static final int SLOT_BACK = 0x2000;
	public static final int SLOT_LR_HAND = 0x4000;
	public static final int SLOT_FULL_ARMOR = 0x8000;
	public static final int SLOT_HAIR = 0x010000;
	public static final int SLOT_ALLDRESS = 0x020000;
	public static final int SLOT_HAIR2 = 0x040000;
	public static final int SLOT_HAIRALL = 0x080000;
	public static final int SLOT_R_BRACELET = 0x100000;
	public static final int SLOT_L_BRACELET = 0x200000;
	public static final int SLOT_DECO = 0x400000;
	public static final int SLOT_BELT = 0x10000000;
	public static final int SLOT_BROOCH = 0x20000000;
	public static final int SLOT_JEWELRY = 0x40000000;
	public static final int SLOT_WOLF = -100;
	public static final int SLOT_HATCHLING = -101;
	public static final int SLOT_STRIDER = -102;
	public static final int SLOT_BABYPET = -103;
	public static final int SLOT_GREATWOLF = -104;

	public static final int SLOT_MULTI_ALLWEAPON = SLOT_LR_HAND | SLOT_R_HAND;

	public static final int CRYSTAL_NONE = 0x00;
	public static final int CRYSTAL_D = 0x01;
	public static final int CRYSTAL_C = 0x02;
	public static final int CRYSTAL_B = 0x03;
	public static final int CRYSTAL_A = 0x04;
	public static final int CRYSTAL_S = 0x05;
	public static final int CRYSTAL_S80 = 0x06;
	public static final int CRYSTAL_S84 = 0x07;
	public static final int CRYSTAL_R = 0x08;
	public static final int CRYSTAL_R95 = 0x09;
	public static final int CRYSTAL_R99 = 0x0a;

	private static final int[] crystalItemId = {0, 1458, 1459, 1460, 1461, 1462, 1462, 1462, 17371, 17371, 17371};
	/*private static final int[] crystalEnchantBonusArmor =
    {
		0, 11, 6, 11, 19, 25, 25, 25, 35, 35, 35
	};
	private static final int[] crystalEnchantBonusWeapon =
	{
		0, 90, 45, 67, 144, 250, 250, 250, 400, 400, 400
	};*/

	private final int itemId;
	private final String name;
	private final String icon;
	private final int weight;
	private final boolean stackable;
	private final int crystalType;
	private final int duration;
	private final int time;
	private final int autoDestroyTime;
	private final int bodyPart;
	private final int referencePrice;
	private final int crystalCount;
	private final int boundItem;
	private final int standardItem;
	private final int blessedItem;
	private final boolean sellable;
	private final boolean dropable;
	private final boolean destroyable;
	private final boolean tradeable;
	private final boolean depositable;
	private final boolean questItem;
	private final boolean common;
	private final boolean heroItem;
	private final boolean pvpItem;
	private final boolean _ex_immediate_effect;
	private final boolean vitality;
	private final boolean isOlyRestricted;
	private final boolean isBlessed;
	private final boolean isEnchantable;
	private final boolean isAttributable;
	private final boolean isAugmentable;
	private final boolean canBeUsedAsApp;
	private final boolean isEpic;
	private final ActionType defaultAction;
	private boolean isForPet = false;
	private final String bodyPartName;

	protected int type1; // needed for item list (inventory)
	protected int type2; // different lists for armor, weapon, etc
	protected Elementals[] elementals = null;
	protected FuncTemplate[] funcTemplates;
	protected AbnormalTemplate[] effectTemplates;
	protected List<Condition> preConditions;
	private SkillHolder[] skillHolder;
	private L2CrystallizeReward[] crystallizeRewards;

	protected static final Func[] emptyFunctionSet = new Func[0];
	protected static final Abnormal[] emptyEffectSet = new Abnormal[0];

	/**
	 * Constructor of the ItemTemplate that fill class variables.<BR><BR>
	 *
	 * @param set : StatsSet corresponding to a set of couples (key,value) for description of the item
	 */
	protected ItemTemplate(StatsSet set) {
		itemId = set.getInteger("id");
		name = set.getString("name");
		icon = set.getString("icon", null);
		weight = set.getInteger("weight", 0);
		duration = set.getInteger("duration", -1);
		time = set.getInteger("time", -1);
		autoDestroyTime = set.getInteger("autoDestroyTime", -1) * 1000;
		bodyPart = ItemTable.slots.get(set.getString("bodypart", "none"));
		bodyPartName = set.getString("bodypart", "none");
		referencePrice = set.getInteger("price", 0);
		crystalType = ItemTable.crystalTypes.get(set.getString("crystalType", "none")); // default to none-grade
		crystalCount = set.getInteger("crystalCount", 0);
		boundItem = set.getInteger("boundItem", -1);
		standardItem = set.getInteger("standardItem", -1);
		blessedItem = set.getInteger("blessedItem", -1);

		stackable = set.getBool("isStackable", false);
		sellable = set.getBool("isSellable", true);
		dropable = set.getBool("isDropable", true);
		destroyable = set.getBool("isDestroyable", true);
		tradeable = set.getBool("isTradable", true);
		depositable = set.getBool("isDepositable", true);
		questItem = set.getBool("isQuestitem", false);
		vitality = set.getBool("isVitality", false);
		isOlyRestricted = set.getBool("isOlyRestricted", false);
		isBlessed = set.getBool("isBlessed", false);
		canBeUsedAsApp = set.getBool("canBeUsedAsApp", true);
		isEnchantable = set.getBool("isEnchantable", true);
		isAttributable = set.getBool("isAttributable", true);
		isAugmentable = set.getBool("isAugmentable", true);
		isEpic = set.getBool("isEpic", false);

		//_immediate_effect - herb
		_ex_immediate_effect = set.getInteger("exImmediateEffect", 0) > 0;
		//used for custom type select
		defaultAction = set.getEnum("defaultAction", ActionType.class, ActionType.none);

		String equip_condition = set.getString("equipCondition", null);
		if (equip_condition != null) {
			//pet conditions
			ConditionLogicOr cond = new ConditionLogicOr();
			if (equip_condition.contains("all_wolf_group")) {
				cond.add(new ConditionPetType(ALL_WOLF));
			}
			if (equip_condition.contains("hatchling_group")) {
				cond.add(new ConditionPetType(HATCHLING));
			}
			if (equip_condition.contains("strider")) {
				cond.add(new ConditionPetType(STRIDER));
			}
			if (equip_condition.contains("baby_pet_group")) {
				cond.add(new ConditionPetType(BABY));
			}
			if (equip_condition.contains("upgrade_baby_pet_group")) {
				cond.add(new ConditionPetType(IMPROVED_BABY));
			}
			if (equip_condition.contains("grown_up_wolf_group")) {
				cond.add(new ConditionPetType(GROWN_WOLF));
			}
			if (equip_condition.contains("item_equip_pet_group")) {
				cond.add(new ConditionPetType(ALL_PET));
			}

			if (equip_condition.contains("all_wolf_group") || equip_condition.contains("hatchling_group") || equip_condition.contains("strider") ||
					equip_condition.contains("baby_pet_group") || equip_condition.contains("upgrade_baby_pet_group") ||
					equip_condition.contains("grown_up_wolf_group") || equip_condition.contains("item_equip_pet_group")) {
				isForPet = true;
			}

			if (cond.conditions.length > 0) {
				attach(cond);
			}
		}

		common = itemId >= 12006 && itemId <= 12361;
		heroItem = set.getBool("isHeroItem", false);
		pvpItem = itemId >= 10667 && itemId <= 10835 || itemId >= 12852 && itemId <= 12977 || itemId >= 14363 && itemId <= 14525 || itemId == 14528 ||
				itemId == 14529 || itemId == 14558 || itemId >= 15913 && itemId <= 16024 || itemId >= 16134 && itemId <= 16147 || itemId == 16149 ||
				itemId == 16151 || itemId == 16153 || itemId == 16155 || itemId == 16157 || itemId == 16159 || itemId >= 16168 && itemId <= 16176 ||
				itemId >= 16179 && itemId <= 16220;
	}

	/**
	 * Returns the itemType.
	 *
	 * @return Enum
	 */
	public abstract ItemType getItemType();

	/**
	 * Returns the duration of the item
	 *
	 * @return int
	 */
	public final int getDuration() {
		return duration;
	}

	/**
	 * Returns the time of the item
	 *
	 * @return int
	 */
	public final int getTime() {
		return time;
	}

	/**
	 * @return the auto destroy time of the item in seconds: 0 or less - default
	 */
	public final int getAutoDestroyTime() {
		return autoDestroyTime;
	}

	/**
	 * Returns the ID of the iden
	 *
	 * @return int
	 */
	public final int getItemId() {
		return itemId;
	}

	public abstract int getItemMask();

	/**
	 * Returns the type 2 of the item
	 *
	 * @return int
	 */
	public final int getType2() {
		return type2;
	}

	/**
	 * Returns the weight of the item
	 *
	 * @return int
	 */
	public final int getWeight() {
		return weight;
	}

	/**
	 * Returns if the item is crystallizable
	 *
	 * @return boolean
	 */
	public final boolean isCrystallizable() {
		return crystalType != ItemTemplate.CRYSTAL_NONE && crystalCount > 0;
	}

	/**
	 * Return the type of crystal if item is crystallizable
	 *
	 * @return int
	 */
	public final int getCrystalType() {
		return crystalType;
	}

	/**
	 * Return the type of crystal if item is crystallizable
	 *
	 * @return int
	 */
	public final int getCrystalItemId() {
		return crystalItemId[crystalType];
	}

	/**
	 * Returns the grade of the item.<BR><BR>
	 * <U><I>Concept :</I></U><BR>
	 * In fact, this fucntion returns the type of crystal of the item.
	 *
	 * @return int
	 */
	public final int getItemGrade() {
		return getCrystalType();
	}

	/**
	 * Returns the grade of the item.<BR><BR>
	 * For grades S80 and S84 return S
	 *
	 * @return int
	 */
	public final int getItemGradePlain() {
		switch (getItemGrade()) {
			case CRYSTAL_S80:
			case CRYSTAL_S84:
				return CRYSTAL_S;
			case CRYSTAL_R95:
			case CRYSTAL_R99:
				return CRYSTAL_R;
			default:
				return getItemGrade();
		}
	}

	/**
	 * Returns the quantity of crystals for crystallization
	 *
	 * @return int
	 */
	public final int getCrystalCount() {
		return crystalCount;
	}

	/**
	 * Returns the quantity of crystals for crystallization on specific enchant level
	 *
	 * @return int
	 */
	public final int getCrystalCount(int enchantLevel) {
		if (enchantLevel > 3) {
			switch (type2) {
				case TYPE2_SHIELD_ARMOR:
				case TYPE2_ACCESSORY:
					return crystalCount;// + crystalEnchantBonusArmor[getCrystalType()] * (3 * enchantLevel - 6);
				case TYPE2_WEAPON:
					return crystalCount;// + crystalEnchantBonusWeapon[getCrystalType()] * (2 * enchantLevel - 3);
				default:
					return crystalCount;
			}
		} else if (enchantLevel > 0) {
			switch (type2) {
				case TYPE2_SHIELD_ARMOR:
				case TYPE2_ACCESSORY:
					return crystalCount;// + crystalEnchantBonusArmor[getCrystalType()] * enchantLevel;
				case TYPE2_WEAPON:
					return crystalCount;// + crystalEnchantBonusWeapon[getCrystalType()] * enchantLevel;
				default:
					return crystalCount;
			}
		} else {
			return crystalCount;
		}
	}

	public final int getBoundItem() {
		return boundItem;
	}

	public final int getStandardItem() {
		return standardItem;
	}

	public final int getBlessedItem() {
		return blessedItem;
	}

	/**
	 * Returns the name of the item
	 *
	 * @return String
	 */
	public final String getName() {
		return name;
	}

	/**
	 * Returns the base elemental of the item
	 *
	 * @return Elementals
	 */
	public final Elementals[] getElementals() {
		return elementals;
	}

	public Elementals getElemental(byte attribute) {
		for (Elementals elm : elementals) {
			if (elm.getElement() == attribute) {
				return elm;
			}
		}
		return null;
	}

	/**
	 * Sets the base elemental of the item
	 */
	public void setElementals(Elementals element) {
		if (elementals == null) {
			elementals = new Elementals[1];
			elementals[0] = element;
		} else {
			Elementals elm = getElemental(element.getElement());
			if (elm != null) {
				elm.setValue(element.getValue());
			} else {
				elm = element;
				Elementals[] array = new Elementals[elementals.length + 1];
				System.arraycopy(elementals, 0, array, 0, elementals.length);
				array[elementals.length] = elm;
				elementals = array;
			}
		}
	}

	/**
	 * Return the part of the body used with the item.
	 *
	 * @return int
	 */
	public final int getBodyPart() {
		return bodyPart;
	}

	/**
	 * Returns the type 1 of the item
	 *
	 * @return int
	 */
	public final int getType1() {
		return type1;
	}

	/**
	 * Returns if the item is stackable
	 *
	 * @return boolean
	 */
	public final boolean isStackable() {
		return stackable;
	}

	/**
	 * Returns if the item is consumable
	 *
	 * @return boolean
	 */
	public boolean isConsumable() {
		return false;
	}

	public boolean isEquipable() {
		return getBodyPart() != 0 && !(getItemType() instanceof EtcItemType);
	}

	/**
	 * Returns the price of reference of the item
	 *
	 * @return int
	 */
	public final int getReferencePrice() {
		if (referencePrice == 0) {
			return 2;
		}

		if (Config.isServer(Config.TENKAI_LEGACY)) {
			return (int) Math.sqrt(referencePrice);
		}
		return isConsumable() ? (int) (referencePrice * Config.RATE_CONSUMABLE_COST) : referencePrice;
	}

	private int salePrice;

	public final void setSalePrice(final int price) {
		salePrice = price;
	}

	public final int getSalePrice() {
		return salePrice;
	}

	/**
	 * Returns if the item can be sold
	 *
	 * @return boolean
	 */
	public final boolean isSellable() {
		return sellable;
	}

	/**
	 * Returns if the item can dropped
	 *
	 * @return boolean
	 */
	public final boolean isDropable() {
		return dropable;
	}

	/**
	 * Returns if the item can destroy
	 *
	 * @return boolean
	 */
	public final boolean isDestroyable() {
		return destroyable;
	}

	/**
	 * Returns if the item can add to trade
	 *
	 * @return boolean
	 */
	public final boolean isTradeable() {
		return tradeable;
	}

	/**
	 * Returns if the item can be put into warehouse
	 *
	 * @return boolean
	 */
	public final boolean isDepositable() {
		return depositable;
	}

	/**
	 * Returns if item is common
	 *
	 * @return boolean
	 */
	public final boolean isCommon() {
		return common;
	}

	/**
	 * Returns if item is hero-only
	 *
	 */
	public final boolean isHeroItem() {
		return heroItem;
	}

	/**
	 * Returns if item is pvp
	 *
	 */
	public final boolean isPvpItem() {
		return pvpItem;
	}

	public boolean isPotion() {
		return getItemType() == EtcItemType.POTION;
	}

	public boolean isElixir() {
		return getItemType() == EtcItemType.ELIXIR;
	}

	/**
	 * Returns array of Func objects containing the list of functions used by the item
	 *
	 * @param instance : Item pointing out the item
	 * @return Func[] : array of functions
	 */
	public Func[] getStatFuncs(Item instance) {
		if (funcTemplates == null || funcTemplates.length == 0) {
			return emptyFunctionSet;
		}

		ArrayList<Func> funcs = new ArrayList<>(funcTemplates.length);

		Func f;
		for (FuncTemplate t : funcTemplates) {
			f = t.getFunc(this); // skill is owner
			if (f != null) {
				funcs.add(f);
			}
		}

		if (funcs.isEmpty()) {
			return emptyFunctionSet;
		}

		return funcs.toArray(new Func[funcs.size()]);
	}

	/**
	 * Returns the effects associated with the item.
	 *
	 * @param instance : Item pointing out the item
	 * @param player   : Creature pointing out the player
	 * @return L2Effect[] : array of effects generated by the item
	 */
	public Abnormal[] getEffects(Item instance, Creature player) {
		if (effectTemplates == null || effectTemplates.length == 0) {
			return emptyEffectSet;
		}

		ArrayList<Abnormal> effects = new ArrayList<>();

		Env env = new Env();
		env.player = player;
		env.target = player;
		env.item = instance;

		Abnormal e;

		for (AbnormalTemplate et : effectTemplates) {

			e = et.getEffect(env);
			if (e != null) {
				e.scheduleEffect();
				effects.add(e);
			}
		}

		if (effects.isEmpty()) {
			return emptyEffectSet;
		}

		return effects.toArray(new Abnormal[effects.size()]);
	}

	/**
	 * Returns effects of skills associated with the item.
	 *
	 * @return L2Effect[] : array of effects generated by the skill
	 * <p>
	 * public L2Effect[] getSkillEffects(Creature caster, Creature target)
	 * {
	 * if (skills == null)
	 * return emptyEffectSet;
	 * List<L2Effect> effects = new ArrayList<L2Effect>();
	 * <p>
	 * for (Skill skill : skills)
	 * {
	 * if (!skill.checkCondition(caster, target, true))
	 * continue; // Skill condition not met
	 * <p>
	 * if (target.getFirstEffect(skill.getId()) != null)
	 * target.removeEffect(target.getFirstEffect(skill.getId()));
	 * for (L2Effect e : skill.getEffects(caster, target))
	 * effects.add(e);
	 * }
	 * if (effects.isEmpty())
	 * return emptyEffectSet;
	 * return effects.toArray(new L2Effect[effects.size()]);
	 * }
	 */

	public void attach(SkillHolder skill) {
		if (skillHolder == null) {
			skillHolder = new SkillHolder[]{skill};
		} else {
			int len = skillHolder.length;
			SkillHolder[] tmp = new SkillHolder[len + 1];
			System.arraycopy(skillHolder, 0, tmp, 0, len);
			tmp[len] = skill;
			skillHolder = tmp;
		}
	}

	public FuncTemplate[] getFuncs() {
		return funcTemplates;
	}

	/**
	 * Add the FuncTemplate f to the list of functions used with the item
	 *
	 * @param f : FuncTemplate to add
	 */
	public void attach(FuncTemplate f) {
		switch (f.stat) {
			case FIRE_RES:
			case FIRE_POWER:
				setElementals(new Elementals(Elementals.FIRE, (int) f.lambda.calc(null)));
				break;
			case WATER_RES:
			case WATER_POWER:
				setElementals(new Elementals(Elementals.WATER, (int) f.lambda.calc(null)));
				break;
			case WIND_RES:
			case WIND_POWER:
				setElementals(new Elementals(Elementals.WIND, (int) f.lambda.calc(null)));
				break;
			case EARTH_RES:
			case EARTH_POWER:
				setElementals(new Elementals(Elementals.EARTH, (int) f.lambda.calc(null)));
				break;
			case HOLY_RES:
			case HOLY_POWER:
				setElementals(new Elementals(Elementals.HOLY, (int) f.lambda.calc(null)));
				break;
			case DARK_RES:
			case DARK_POWER:
				setElementals(new Elementals(Elementals.DARK, (int) f.lambda.calc(null)));
				break;
		}
		// If functTemplates is empty, create it and add the FuncTemplate f in it
		if (funcTemplates == null) {
			funcTemplates = new FuncTemplate[]{f};
		} else {
			int len = funcTemplates.length;
			FuncTemplate[] tmp = new FuncTemplate[len + 1];
			// Definition : arraycopy(array source, begins copy at this position of source, array destination, begins copy at this position in dest,
			//						  number of components to be copied)
			System.arraycopy(funcTemplates, 0, tmp, 0, len);
			tmp[len] = f;
			funcTemplates = tmp;
		}
	}

	/**
	 * Add the EffectTemplate effect to the list of effects generated by the item
	 *
	 * @param effect : EffectTemplate
	 */
	public void attach(AbnormalTemplate effect) {
		if (effectTemplates == null) {
			effectTemplates = new AbnormalTemplate[]{effect};
		} else {
			int len = effectTemplates.length;
			AbnormalTemplate[] tmp = new AbnormalTemplate[len + 1];
			// Definition : arraycopy(array source, begins copy at this position of source, array destination, begins copy at this position in dest,
			//						  number of components to be copied)
			System.arraycopy(effectTemplates, 0, tmp, 0, len);
			tmp[len] = effect;
			effectTemplates = tmp;
		}
	}

	public final List<Condition> getConditions() {
		return preConditions;
	}

	public final void attach(Condition c) {
		if (preConditions == null) {
			preConditions = new ArrayList<>();
		}
		if (!preConditions.contains(c)) {
			preConditions.add(c);
		}
	}

	/**
	 * Method to retrive skills linked to this item
	 * <p>
	 * armor and weapon: passive skills
	 * etcitem: skills used on item use <-- ???
	 *
	 * @return Skills linked to this item as SkillHolder[]
	 */
	public final SkillHolder[] getSkills() {
		return skillHolder;
	}

	public boolean checkCondition(Creature activeChar, WorldObject target, boolean sendMessage) {
		if (activeChar.isGM() && !Config.GM_ITEM_RESTRICTION) {
			return true;
		}

		if (preConditions == null) {
			return true;
		}

		Env env = new Env();
		env.player = activeChar;
		if (target instanceof Creature) {
			env.target = (Creature) target;
		}

		for (Condition preCondition : preConditions) {
			if (preCondition == null) {
				continue;
			}

			if (!preCondition.test(env)) {
				if (activeChar instanceof Summon) {
					activeChar.getActingPlayer().sendPacket(SystemMessage.getSystemMessage(SystemMessageId.PET_CANNOT_USE_ITEM));
					return false;
				}

				if (sendMessage) {
					String msg = preCondition.getMessage();
					int msgId = preCondition.getMessageId();
					if (msg != null) {
						activeChar.sendMessage(msg);
					} else if (msgId != 0) {
						SystemMessage sm = SystemMessage.getSystemMessage(msgId);
						if (preCondition.isAddName()) {
							sm.addItemName(itemId);
						}
						activeChar.getActingPlayer().sendPacket(sm);
					}
				}
				return false;
			}
		}
		return true;
	}

	public boolean isConditionAttached() {
		return preConditions != null && !preConditions.isEmpty();
	}

	public void attach(L2CrystallizeReward reward) {
		if (crystallizeRewards == null) {
			crystallizeRewards = new L2CrystallizeReward[]{reward};
		} else {
			int len = crystallizeRewards.length;
			L2CrystallizeReward[] tmp = new L2CrystallizeReward[len + 1];
			System.arraycopy(crystallizeRewards, 0, tmp, 0, len);
			tmp[len] = reward;
			crystallizeRewards = tmp;
		}
	}

	public final L2CrystallizeReward[] getCrystallizeRewards() {
		return crystallizeRewards;
	}

	public boolean isQuestItem() {
		return questItem;
	}

	/**
	 * Returns the name of the item
	 *
	 * @return String
	 */
	@Override
	public String toString() {
		return name + "(" + itemId + ")";
	}

	/**
	 * @return the _ex_immediate_effect
	 */
	public boolean is_ex_immediate_effect() {
		return _ex_immediate_effect;
	}

	/**
	 * @return the _default_action
	 */
	public ActionType getDefaultAction() {
		return defaultAction;
	}

	/**
	 * @return is for pet?
	 */
	public boolean isForPet() {
		return isForPet;
	}

	/**
	 * @return body part name
	 */
	public String getBodyPartName() {
		return bodyPartName;
	}

	/**
	 * Get the icon link in client files.<BR> Usable in HTML windows.
	 *
	 * @return the icon
	 */
	public String getIcon() {
		return icon;
	}

	public final boolean isVitality() {
		return vitality;
	}

	public final boolean isOlyRestricted() {
		return isOlyRestricted;
	}

	public boolean isBlessed() {
		return isBlessed;
	}

	public boolean canBeUsedAsApp() {
		return canBeUsedAsApp;
	}

	public boolean isEnchantable() {
		return isEnchantable;
	}

	public boolean isAttributable() {
		return isAttributable;
	}

	public boolean isAugmentable() {
		return isAugmentable;
	}

	public boolean isEpic() {
		return isEpic;
	}

	public int getShotTypeIndex() {
		switch (getDefaultAction()) {
			case soulshot:
				return 0;
			case spiritshot:
				return 1;
			case summon_soulshot:
				return 2;
			case summon_spiritshot:
				return 3;
		}

		return -1;
	}
}
