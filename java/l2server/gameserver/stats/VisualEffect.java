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

package l2server.gameserver.stats;

import java.util.NoSuchElementException;

/**
 * @author DrHouse
 */
public enum VisualEffect
{
	BLEEDING(1, "bleed"),
	POISON(2, "poison"),
	REDCIRCLE(3, "redcircle"),
	ICE(4, "ice"),
	WIND(5, "wind"),
	FEAR(6, "fear"),
	STUN(7, "stun"),
	SLEEP(8, "sleep"),
	MUTED(9, "mute"),
	ROOT(10, "root"),
	HOLD_1(11, "hold1"),
	HOLD_2(12, "hold2"),
	UNKNOWN_13(13, "unknown13"),
	BIG_HEAD(14, "bighead"),
	FLAME(15, "flame"),
	UNKNOWN_16(16, "unknown16"),
	GROW(17, "grow"),
	FLOATING_ROOT(18, "floatroot"),
	DANCE_STUNNED(19, "dancestun"),
	FIREROOT_STUN(20, "firerootstun"),
	STEALTH(21, "stealth"),
	IMPRISIONING_1(22, "imprison1"),
	IMPRISIONING_2(23, "imprison2"),
	MAGIC_CIRCLE(24, "magiccircle"),
	ICE2(25, "ice2"),
	EARTHQUAKE(26, "earthquake"),
	UNKNOWN_27(27, "unknown27"),
	INVULNERABLE(28, "invulnerable"),
	VITALITY(29, "vitality"),
	REAL_TARGET(30, "realtarget"),
	DEATH_MARK(31, "deathmark"),
	SKULL_FEAR(32, "skull_fear"),
	//CONFUSED("confused", 0x0020),

	// special effects
	S_INVINCIBLE(33, "invincible"),
	S_AIR_STUN(34, "airstun"),
	S_AIR_ROOT(35, "airroot"),
	S_BAGUETTE_SWORD(36, "baguettesword"),
	S_YELLOW_AFFRO(37, "yellowafro"),
	S_PINK_AFFRO(38, "pinkafro"),
	S_BLACK_AFFRO(39, "blackafro"),
	S_UNKNOWN1(40, "unk1"),
	S_STIGMA_SHILIEN(41, "stigmashilien"),
	S_STAKATOROOT(42, "stakatoroot"),
	S_FREEZING(43, "freezing"),
	S_VESPER(44, "vesper"),
	S_VESPER2(45, "vesper2"),
	S_VESPER3(46, "vesper3"),
	S_UNKNOWN4(47, "greencloud"),
	ARCANE_BARRIER(48, "arcaneBarrier"),
	S_LIFT_HOLD(49, "lifthold"),
	S_UNKNOWN7(50, "unk7"),
	S_KNOCK_DOWN(51, "knockdown"),
	S_NEVITSBLESSING(52, "nevitsblessing"),
	S_UNKNOWN8(53, "unk8"),
	S_MITHRIL_SET(54, "mithril_set"),
	S_UNKNOWN10(55, "unk10"),
	S_CAGE(56, "cage"),
	S_KNIGHT_AURA_RECEIVED(57, "knight_aura_received"),
	S_KNIGHT_AURA(58, "knight_aura"),
	S_RAGE_AURA_RECEIVED(59, "rage_aura_received"),
	S_RAGE_AURA(60, "rage_aura"),
	S_LIGHT_EXPLOSION(61, "light_explosion"),
	S_TORNADO(62, "rollingThunder"),
	S_DEATH(63, "death"),
	S_BLUE_WOLF(64, "blue_wolf"),

	// event effects
	E_AFRO_1(65, "afrobaguette1"),
	E_AFRO_2(66, "afrobaguette2"),
	E_AFRO_3(67, "afrobaguette3"),
	E_EVASWRATH(68, "evaswrath"),
	E_HEADPHONE(69, "headphone"),

	E_VESPER_1(70, "vesper1"),
	TALISMAN_POWER5(71, "talismanpower5"),
	E_VESPER_3(72, "vesper3"),

	TALISMAN_POWER1(73, "talismanpower1"),
	TALISMAN_POWER2(74, "talismanpower2"),
	TALISMAN_POWER3(75, "talismanpower3"),
	TALISMAN_POWER4(76, "talismanpower4"),

	S_NONE_CLOAK1(77, "cloak_none1"),

	//TEST1(78, "test1"), Unknown set
	MITHIRIL_SET(79, "mithirilSet"),
	KARMIAN_SET(80, "karmianSet"),
	BLUE_WOLF_SET(81, "blueWolfSet"),
	MAJESTIC_SET(82, "majesticSet"),
	BEACH_SWIMSUIT_APP(83, "beachSwimsuitApp"),
	ALLURING_SWIMSUIT_APP(84, "alluringSwimsuitApp"),
	CHRISTMAS_APP(85, "christmasApp"),
	//TEST9(86, "test9"), Strange gold Card near the player's head
	NC_DINOS_APP(87, "ncDinosApp"),
	LOLIPOP(89, "lolipop"),
	CANDY(90, "candy"),
	COOKIE(91, "cookie"),
	STARTS_5_EMPTY(92, "starts5Empty"),
	STARTS_4_EMPTY(93, "starts4Empty"),
	STARTS_3_EMPTY(94, "starts3Empty"),
	STARTS_2_EMPTY(95, "starts2Empty"),
	STARTS_1_EMPTY(96, "starts1Empty"),
	STARTS_0_EMPTY(97, "starts0Empty"),
	FACEOFF(98, "faceoff"),
	//TEST23(99, "test23"), Strange ice effect at player legs
	//TEST24(100, "test24"), Metal app too
	//TEST25(101, "test25"), Strange gold icon between the player, looks like a 3/4 of an 8 number
	MUSICAL_YELLOW_NOTE(102, "musicalYellowNote"),
	MUSICAL_BLUE_NOTE(103, "musicalBlueNote"),
	MUSICAL_GREEN_NOTE(104, "musicalGreenNote"),
	LINEAGE_2_GOLD_ICON(105, "lineage2GoldIcon"),
	CHRISTMAS_STOCKING(106, "christmasStocking"),
	CHRISTMAS_TREE(107, "christmasTree"),
	CHRISTMAS_SNOWMAN(108, "christmasSnowman"),
	METAL_APP(109, "metalApp"),
	//Similar to spoil effect
	MAID_APP(110, "maidApp"),
	//Some kind of barrier
	SEDUCTIVE_SWIMSUIT_APP(111, "seductiveSwimsuitApp"),
	//Looks like some kind of debuff effect

	WINDY_TRAP(112, "windyTrap"),
	SAURON_RED_EYE(113, "sauronRedEye"),
	SAURON_GREEN_EYE(114, "sauronGreenEye"),
	ERTHEIA_GREEN_RUN_POWER(115, "ertheiaGreenRunPower"),
	ERTHEIA_RED_RUN_POWER(116, "ertheiaRedRunPower"),
	WINDY_REFUGE(117, "windyRefuge"),
	TARGET_HEART(118, "targetHeart"),
	SQUALL(119, "squall"),
	ERTHEIA_WIND_POWER(120, "ertheiaWindPower"),
	ERTHEIA_PINK_RUN_POWER(121, "ertheiaPinkRunPower"),
	NOTHING(122, "nothing"),
	//Nothing
	HEAVY_HAND(123, "heavyHand"),
	ERTHEIA_RUN_POWER(124, "ertheiaRunPower"),
	//The one from the video with the effect on the hands & legs
	STORM_ROOT(125, "stormRoot"),
	AIR_CRUSH(126, "airCrush"),
	SPALLATION(127, "spallation"),
	//TEST41(128, "test41"),	//Same as vitality herb but in blue
	//TEST42(129, "test42"),	//Ninja app
	//TEST43(130, "test43"),	//TW app
	//TEST44(131, "test44"),	//Military app
	//TEST45(132, "test45");	//Metal Suite
	//TEST45(133, "test45");	//Maid App
	SHIELD_AURA(141, "shieldAura"),
	//Some kind of shield aura
	BLUE_SHIELD_AURA(142, "blueShieldAura"),
	//Some kind of blue-shield aura
	TEST46(143, "test46"),
	//Looks like the old real target effect
	DIVINITY_OF_EINHASAD(144, "divinityOfEinhasad"),
	SHILLIEN_PROTECTION(145, "shillienProtection"),
	TEST49(146, "test49"),
	//5 starts over the players head, event like
	TEST50(147, "test50"),
	//5 starts over the players head, event like
	TEST51(148, "test51"),
	//5 starts over the players head, event like
	TEST52(149, "test52"),
	//5 starts over the players head, event like
	TEST53(150, "test53"),
	//5 starts over the players head, event like
	TEST54(151, "test54"),
	//5 starts over the players head, event like
	TEST55(152, "test55"),
	//Some kind of NICE yellow aura effect
	PALADIN_AURA(153, "paladinAura"),
	AVENGER_AURA(154, "avengerAura"),
	SELTINEL_AURA(155, "sentinelAura"),
	TEMPLAR_AURA(156, "templarAura"),
	SPEAR_FURY(157, "spearFury"),
	SWORD_FURY(158, "swordFury"),
	FIST_FURY(159, "fistFury"),
	test55(160, "test55"),
	//Some kind of blue barrier
	WIND_TORNADO(161, "windTornado"),
	CAST_SNOW_STORM(162, "castSnowStorm"),
	DEBUFF_SNOW_STORM(163, "debuffSnowStorm"),
	//SOME_DEBUFF(165, "debuff"),	//Looks like some kind of root/wind debuff
	AQUA_TORNADO(166, "aquaTornado"),
	EARTH_BIG_BODY(167, "earthBigBody"),
	AQUA_BIG_BODY(168, "aquaBigBody"),
	FIRE_BIG_BODY(169, "fireBigBody"),
	EARTH_AURA(170, "earthAura"),
	AQUA_AURA(171, "aquaAura"),
	FIRE_AURA(172, "fireAura"),
	BIG_BODY(173, "bigBody"),
	SMALL_BODY(174, "smallBody");

	private final int _id;
	private final String _name;

	VisualEffect(int id, String name)
	{
		_name = name;
		_id = id;
	}

	public final int getId()
	{
		return _id;
	}

	public final String getName()
	{
		return _name;
	}

	public static VisualEffect getByName(String name)
	{
		for (VisualEffect eff : VisualEffect.values())
		{
			if (eff.getName().equals(name))
			{
				return eff;
			}
		}

		throw new NoSuchElementException("VisualEffect not found for name: '" + name + "'.\n Please check " +
				VisualEffect.class.getCanonicalName());
	}
}
