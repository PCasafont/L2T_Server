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

package l2server.gameserver.network;

import l2server.gameserver.network.serverpackets.ExShowScreenMessage;
import l2server.log.Log;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;

/**
 * NpcStringId implementation, based on
 * SystemMessageId class
 *
 * @author mrTJO
 */
public final class NpcStringId
{

	private static final NSLocalisation[] EMPTY_NSL_ARRAY = new NSLocalisation[0];
	public static final NpcStringId[] EMPTY_ARRAY = new NpcStringId[0];

	/**
	 * ID: 1<br>
	 * Message: Hello! I am $s1. You are $s2, right? Hehehe
	 */
	public static final NpcStringId HELLO_I_AM_S1_YOU_ARE_S2_RIGHT_HEHEHE;

	/**
	 * ID: 2<br>
	 * Message: $s1--$s2--$s3--$s4//$s5 Hehehe
	 */
	public static final NpcStringId S1_S2_S3_S4_S5_HEHEHE;

	/**
	 * ID: 5<br>
	 * Message: Withdraw the fee for the next time at $s1/$s2 $s3
	 */
	public static final NpcStringId WITHDRAW_THE_FEE_FOR_THE_NEXT_TIME_AT_S1_S2_S4;

	/**
	 * ID: 8<br>
	 * Message: Stage
	 */
	public static final NpcStringId STAGE;

	/**
	 * ID: 9<br>
	 * Message: Stage $s1
	 */
	public static final NpcStringId STAGE_S1;

	/**
	 * ID: 10<br>
	 * Message: $s1%%
	 */
	public static final NpcStringId S1;

	/**
	 * ID: 2004<br>
	 * Message: What did you just do to me?
	 */
	public static final NpcStringId WHAT_DID_YOU_JUST_DO_TO_ME;

	/**
	 * ID: 2005<br>
	 * Message: Are you trying to tame me? Don't do that!
	 */
	public static final NpcStringId ARE_YOU_TRYING_TO_TAME_ME_DONT_DO_THAT;

	/**
	 * ID: 2006<br>
	 * Message: Don't give such a thing.  You can endanger yourself!
	 */
	public static final NpcStringId DONT_GIVE_SUCH_A_THING_YOU_CAN_ENDANGER_YOURSELF;

	/**
	 * ID: 2007<br>
	 * Message: Yuck!  What is this?  It tastes terrible!
	 */
	public static final NpcStringId YUCK_WHAT_IS_THIS_IT_TASTES_TERRIBLE;

	/**
	 * ID: 2008<br>
	 * Message: I'm hungry.  Give me a little more, please.
	 */
	public static final NpcStringId IM_HUNGRY_GIVE_ME_A_LITTLE_MORE_PLEASE;

	/**
	 * ID: 2009<br>
	 * Message: What is this?  Is this edible?
	 */
	public static final NpcStringId WHAT_IS_THIS_IS_THIS_EDIBLE;

	/**
	 * ID: 2010<br>
	 * Message: Don't worry about me.
	 */
	public static final NpcStringId DONT_WORRY_ABOUT_ME;

	/**
	 * ID: 2011<br>
	 * Message: Thank you.  That was delicious!
	 */
	public static final NpcStringId THANK_YOU_THAT_WAS_DELICIOUS;

	/**
	 * ID: 2012<br>
	 * Message: I think I am starting to like you!
	 */
	public static final NpcStringId I_THINK_I_AM_STARTING_TO_LIKE_YOU;

	/**
	 * ID: 2013<br>
	 * Message: Eeeeek!  Eeeeek!
	 */
	public static final NpcStringId EEEEEK_EEEEEK;

	/**
	 * ID: 2014<br>
	 * Message: Don't keep trying to tame me.  I don't want to be tamed.
	 */
	public static final NpcStringId DONT_KEEP_TRYING_TO_TAME_ME_I_DONT_WANT_TO_BE_TAMED;

	/**
	 * ID: 2015<br>
	 * Message: It is just food to me.  Although it may also be your hand.
	 */
	public static final NpcStringId IT_IS_JUST_FOOD_TO_ME_ALTHOUGH_IT_MAY_ALSO_BE_YOUR_HAND;

	/**
	 * ID: 2016<br>
	 * Message: If I keep eating like this, won't I become fat?  *chomp chomp*
	 */
	public static final NpcStringId IF_I_KEEP_EATING_LIKE_THIS_WONT_I_BECOME_FAT_CHOMP_CHOMP;

	/**
	 * ID: 2017<br>
	 * Message: Why do you keep feeding me?
	 */
	public static final NpcStringId WHY_DO_YOU_KEEP_FEEDING_ME;

	/**
	 * ID: 2018<br>
	 * Message: Don't trust me.  I'm afraid I may betray you later.
	 */
	public static final NpcStringId DONT_TRUST_ME_IM_AFRAID_I_MAY_BETRAY_YOU_LATER;

	/**
	 * ID: 2019<br>
	 * Message: Grrrrr....
	 */
	public static final NpcStringId GRRRRR;

	/**
	 * ID: 2020<br>
	 * Message: You brought this upon yourself...!
	 */
	public static final NpcStringId YOU_BROUGHT_THIS_UPON_YOURSELF;

	/**
	 * ID: 2021<br>
	 * Message: I feel strange...!  I keep having these evil thoughts...!
	 */
	public static final NpcStringId I_FEEL_STRANGE_I_KEEP_HAVING_THESE_EVIL_THOUGHTS;

	/**
	 * ID: 2022<br>
	 * Message: Alas... so this is how it all ends...
	 */
	public static final NpcStringId ALAS_SO_THIS_IS_HOW_IT_ALL_ENDS;

	/**
	 * ID: 2023<br>
	 * Message: I don't feel so good... Oh, my mind is very troubled...!
	 */
	public static final NpcStringId I_DONT_FEEL_SO_GOOD_OH_MY_MIND_IS_VERY_TROUBLED;

	/**
	 * ID: 2024<br>
	 * Message: $s1, so what do you think it is like to be tamed?
	 */
	public static final NpcStringId S1_SO_WHAT_DO_YOU_THINK_IT_IS_LIKE_TO_BE_TAMED;

	/**
	 * ID: 2025<br>
	 * Message: $s1, whenever I see spice, I think I will miss your hand that used to feed it to me.
	 */
	public static final NpcStringId S1_WHENEVER_I_SEE_SPICE_I_THINK_I_WILL_MISS_YOUR_HAND_THAT_USED_TO_FEED_IT_TO_ME;

	/**
	 * ID: 2026<br>
	 * Message: $s1, don't go to the village.  I don't have the strength to follow you.
	 */
	public static final NpcStringId S1_DONT_GO_TO_THE_VILLAGE_I_DONT_HAVE_THE_STRENGTH_TO_FOLLOW_YOU;

	/**
	 * ID: 2027<br>
	 * Message: Thank you for trusting me, $s1. I hope I will be helpful to you.
	 */
	public static final NpcStringId THANK_YOU_FOR_TRUSTING_ME_S1_I_HOPE_I_WILL_BE_HELPFUL_TO_YOU;

	/**
	 * ID: 2028<br>
	 * Message: $s1, will I be able to help you?
	 */
	public static final NpcStringId S1_WILL_I_BE_ABLE_TO_HELP_YOU;

	/**
	 * ID: 2029<br>
	 * Message: I guess it's just my animal magnetism.
	 */
	public static final NpcStringId I_GUESS_ITS_JUST_MY_ANIMAL_MAGNETISM;

	/**
	 * ID: 2030<br>
	 * Message: Too much spicy food makes me sweat like a beast.
	 */
	public static final NpcStringId TOO_MUCH_SPICY_FOOD_MAKES_ME_SWEAT_LIKE_A_BEAST;

	/**
	 * ID: 2031<br>
	 * Message: Animals need love too.
	 */
	public static final NpcStringId ANIMALS_NEED_LOVE_TOO;

	/**
	 * ID: 2032<br>
	 * Message: What'd I miss? What'd I miss?
	 */
	public static final NpcStringId WHATD_I_MISS_WHATD_I_MISS;

	/**
	 * ID: 2033<br>
	 * Message: I just know before this is over, I'm gonna need a lot of serious therapy.
	 */
	public static final NpcStringId I_JUST_KNOW_BEFORE_THIS_IS_OVER_IM_GONNA_NEED_A_LOT_OF_SERIOUS_THERAPY;

	/**
	 * ID: 2034<br>
	 * Message: I sense great wisdom in you... I'm an animal, and I got instincts.
	 */
	public static final NpcStringId I_SENSE_GREAT_WISDOM_IN_YOU_IM_AN_ANIMAL_AND_I_GOT_INSTINCTS;

	/**
	 * ID: 2035<br>
	 * Message: Remember, I'm here to help.
	 */
	public static final NpcStringId REMEMBER_IM_HERE_TO_HELP;

	/**
	 * ID: 2036<br>
	 * Message: Are we there yet?
	 */
	public static final NpcStringId ARE_WE_THERE_YET;

	/**
	 * ID: 2037<br>
	 * Message: That really made me feel good to see that.
	 */
	public static final NpcStringId THAT_REALLY_MADE_ME_FEEL_GOOD_TO_SEE_THAT;

	/**
	 * ID: 2038<br>
	 * Message: Oh, no, no, no, Nooooo!
	 */
	public static final NpcStringId OH_NO_NO_NO_NOOOOO;

	/**
	 * ID: 2150<br>
	 * Message: Who awoke me?
	 */
	public static final NpcStringId WHO_AWOKE_ME;

	/**
	 * ID: 2151<br>
	 * Message: My master has instructed me to be your guide, $s1.
	 */
	public static final NpcStringId MY_MASTER_HAS_INSTRUCTED_ME_TO_BE_YOUR_GUIDE_S1;

	/**
	 * ID: 2152<br>
	 * Message: Please check this bookcase, $s1.
	 */
	public static final NpcStringId PLEASE_CHECK_THIS_BOOKCASE_S1;

	/**
	 * ID: 2250<br>
	 * Message: Did you call me, $s1?
	 */
	public static final NpcStringId DID_YOU_CALL_ME_S1;

	/**
	 * ID: 2251<br>
	 * Message: I'm confused! Maybe it's time to go back.
	 */
	public static final NpcStringId IM_CONFUSED_MAYBE_ITS_TIME_TO_GO_BACK;

	/**
	 * ID: 2450<br>
	 * Message: That sign!
	 */
	public static final NpcStringId THAT_SIGN;

	/**
	 * ID: 2550<br>
	 * Message: That box was sealed by my master, $s1! Don't touch it!
	 */
	public static final NpcStringId THAT_BOX_WAS_SEALED_BY_MY_MASTER_S1_DONT_TOUCH_IT;

	/**
	 * ID: 2551<br>
	 * Message: You've ended my immortal life! You're protected by the feudal lord, aren't you?
	 */
	public static final NpcStringId YOUVE_ENDED_MY_IMMORTAL_LIFE_YOURE_PROTECTED_BY_THE_FEUDAL_LORD_ARENT_YOU;

	/**
	 * ID: 4151<br>
	 * Message: Delivery duty complete. \\n Go find the Newbie Guide.
	 */
	public static final NpcStringId DELIVERY_DUTY_COMPLETE_N_GO_FIND_THE_NEWBIE_GUIDE;

	/**
	 * ID: 4152<br>
	 * Message: Acquisition of Soulshot for beginners complete. \\n Go find the Newbie Guide.
	 */
	public static final NpcStringId ACQUISITION_OF_SOULSHOT_FOR_BEGINNERS_COMPLETE_N_GO_FIND_THE_NEWBIE_GUIDE;

	/**
	 * ID: 4153<br>
	 * Message: Acquisition of Weapon Exchange coupon for beginners complete. \\n Go speak with the Newbie Guide.
	 */
	public static final NpcStringId
			ACQUISITION_OF_WEAPON_EXCHANGE_COUPON_FOR_BEGINNERS_COMPLETE_N_GO_SPEAK_WITH_THE_NEWBIE_GUIDE;

	/**
	 * ID: 4154<br>
	 * Message: Acquisition of race-specific weapon complete. \\n Go find the Newbie Guide.
	 */
	public static final NpcStringId ACQUISITION_OF_RACE_SPECIFIC_WEAPON_COMPLETE_N_GO_FIND_THE_NEWBIE_GUIDE;

	/**
	 * ID: 4155<br>
	 * Message: Last duty complete. \\n Go find the Newbie Guide.
	 */
	public static final NpcStringId LAST_DUTY_COMPLETE_N_GO_FIND_THE_NEWBIE_GUIDE;

	/**
	 * ID: 6051<br>
	 * Message: $s1! I must kill you. Blame your own curiosity.
	 */
	public static final NpcStringId S1_I_MUST_KILL_YOU_BLAME_YOUR_OWN_CURIOSITY;

	/**
	 * ID: 6052<br>
	 * Message: You have good luck. I shall return.
	 */
	public static final NpcStringId YOU_HAVE_GOOD_LUCK_I_SHALL_RETURN;

	/**
	 * ID: 6053<br>
	 * Message: You are strong. This was a mistake.
	 */
	public static final NpcStringId YOU_ARE_STRONG_THIS_WAS_A_MISTAKE;

	/**
	 * ID: 6054<br>
	 * Message: Who are you to join in the battle? How upsetting.
	 */
	public static final NpcStringId WHO_ARE_YOU_TO_JOIN_IN_THE_BATTLE_HOW_UPSETTING;

	/**
	 * ID: 6451<br>
	 * Message: $s1, did you come to help me?
	 */
	public static final NpcStringId S1_DID_YOU_COME_TO_HELP_ME;

	/**
	 * ID: 6551<br>
	 * Message: Drats! How could I be so wrong??
	 */
	public static final NpcStringId DRATS_HOW_COULD_I_BE_SO_WRONG;

	/**
	 * ID: 6552<br>
	 * Message: $s1! Step back from the confounded box! I will take it myself!
	 */
	public static final NpcStringId S1_STEP_BACK_FROM_THE_CONFOUNDED_BOX_I_WILL_TAKE_IT_MYSELF;

	/**
	 * ID: 6553<br>
	 * Message: $s1! I will be back soon. Stay there and don't you dare wander off!
	 */
	public static final NpcStringId S1_I_WILL_BE_BACK_SOON_STAY_THERE_AND_DONT_YOU_DARE_WANDER_OFF;

	/**
	 * ID: 6554<br>
	 * Message: Grr. I've been hit...
	 */
	public static final NpcStringId GRR_IVE_BEEN_HIT;

	/**
	 * ID: 6555<br>
	 * Message: Grr! Who are you and why have you stopped me?
	 */
	public static final NpcStringId GRR_WHO_ARE_YOU_AND_WHY_HAVE_YOU_STOPPED_ME;

	/**
	 * ID: 6556<br>
	 * Message: I am late!
	 */
	public static final NpcStringId I_AM_LATE;

	/**
	 * ID: 6557<br>
	 * Message: Good luck!
	 */
	public static final NpcStringId GOOD_LUCK;

	/**
	 * ID: 6750<br>
	 * Message: $s1! You seek the forbidden knowledge and I cannot let you have it!
	 */
	public static final NpcStringId S1_YOU_SEEK_THE_FORBIDDEN_KNOWLEDGE_AND_I_CANNOT_LET_YOU_HAVE_IT;

	/**
	 * ID: 6751<br>
	 * Message: Is this all I am allowed to have?...
	 */
	public static final NpcStringId IS_THIS_ALL_I_AM_ALLOWED_TO_HAVE;

	/**
	 * ID: 6752<br>
	 * Message: You defeated me, but our doom approaches...
	 */
	public static final NpcStringId YOU_DEFEATED_ME_BUT_OUR_DOOM_APPROACHES;

	/**
	 * ID: 6753<br>
	 * Message: $s1! Who are you? Why are you bothering my minions?
	 */
	public static final NpcStringId S1_WHO_ARE_YOU_WHY_ARE_YOU_BOTHERING_MY_MINIONS;

	/**
	 * ID: 6754<br>
	 * Message: Beefcake!!
	 */
	public static final NpcStringId BEEFCAKE;

	/**
	 * ID: 6755<br>
	 * Message: Grr! Why are you sticking your nose in our business?
	 */
	public static final NpcStringId GRR_WHY_ARE_YOU_STICKING_YOUR_NOSE_IN_OUR_BUSINESS;

	/**
	 * ID: 6756<br>
	 * Message: Farewell and watch your back!
	 */
	public static final NpcStringId FAREWELL_AND_WATCH_YOUR_BACK;

	/**
	 * ID: 6757<br>
	 * Message: Kamael! Good to see you. I have something to ask you...
	 */
	public static final NpcStringId KAMAEL_GOOD_TO_SEE_YOU_I_HAVE_SOMETHING_TO_ASK_YOU;

	/**
	 * ID: 6758<br>
	 * Message: $s1! Go get him!!
	 */
	public static final NpcStringId S1_GO_GET_HIM;

	/**
	 * ID: 6759<br>
	 * Message: $s1! What are you doing? Attack him!
	 */
	public static final NpcStringId S1_WHAT_ARE_YOU_DOING_ATTACK_HIM;

	/**
	 * ID: 6760<br>
	 * Message: $s1! Is ? your full potential?
	 */
	public static final NpcStringId S1_IS_YOUR_FULL_POTENTIAL;

	/**
	 * ID: 6761<br>
	 * Message: Thanks! I must go and hunt down those that oppose me.
	 */
	public static final NpcStringId THANKS_I_MUST_GO_AND_HUNT_DOWN_THOSE_THAT_OPPOSE_ME;

	/**
	 * ID: 6762<br>
	 * Message: You are so stubborn... I must follow him now...
	 */
	public static final NpcStringId YOU_ARE_SO_STUBBORN_I_MUST_FOLLOW_HIM_NOW;

	/**
	 * ID: 6763<br>
	 * Message: Seek enlightenment from the Tablet.
	 */
	public static final NpcStringId SEEK_ENLIGHTENMENT_FROM_THE_TABLET;

	/**
	 * ID: 6764<br>
	 * Message: Arrogant beings! You are all doomed!
	 */
	public static final NpcStringId ARROGANT_BEINGS_YOU_ARE_ALL_DOOMED;

	/**
	 * ID: 6765<br>
	 * Message: My  time in your world has come to an end. Consider yourselves lucky...
	 */
	public static final NpcStringId MY_TIME_IN_YOUR_WORLD_HAS_COME_TO_AN_END_CONSIDER_YOURSELVES_LUCKY;

	/**
	 * ID: 6766<br>
	 * Message: $s1! How dare you!!!
	 */
	public static final NpcStringId S1_HOW_DARE_YOU;

	/**
	 * ID: 6767<br>
	 * Message: $s1! Ahhaa! Your god forsakes you!
	 */
	public static final NpcStringId S1_AHHAA_YOUR_GOD_FORSAKES_YOU;

	/**
	 * ID: 6851<br>
	 * Message: $s1! Your time is up. Prepare to die a horrible death.
	 */
	public static final NpcStringId S1_YOUR_TIME_IS_UP_PREPARE_TO_DIE_A_HORRIBLE_DEATH;

	/**
	 * ID: 6852<br>
	 * Message: Consider yourself lucky. The next time we meet, you will die - PERMANENTLY!
	 */
	public static final NpcStringId CONSIDER_YOURSELF_LUCKY_THE_NEXT_TIME_WE_MEET_YOU_WILL_DIE_PERMANENTLY;

	/**
	 * ID: 6853<br>
	 * Message: Fare thee well! We shall meet again.
	 */
	public static final NpcStringId FARE_THEE_WELL_WE_SHALL_MEET_AGAIN;

	/**
	 * ID: 6854<br>
	 * Message: $s1! Who are you? And better yet, why are you bothering my minions?
	 */
	public static final NpcStringId S1_WHO_ARE_YOU_AND_BETTER_YET_WHY_ARE_YOU_BOTHERING_MY_MINIONS;

	/**
	 * ID: 6855<br>
	 * Message: Strength beyond strength!!
	 */
	public static final NpcStringId STRENGTH_BEYOND_STRENGTH;

	/**
	 * ID: 6856<br>
	 * Message: Grr! Why are you sticking your nose where it doesn't belong?
	 */
	public static final NpcStringId GRR_WHY_ARE_YOU_STICKING_YOUR_NOSE_WHERE_IT_DOESNT_BELONG;

	/**
	 * ID: 6857<br>
	 * Message: You've won for now, but we will meet again!
	 */
	public static final NpcStringId YOUVE_WON_FOR_NOW_BUT_WE_WILL_MEET_AGAIN;

	/**
	 * ID: 6858<br>
	 * Message: Are they tired of following me?
	 */
	public static final NpcStringId ARE_THEY_TIRED_OF_FOLLOWING_ME;

	/**
	 * ID: 6859<br>
	 * Message: $s1! Can you help me?
	 */
	public static final NpcStringId S1_CAN_YOU_HELP_ME;

	/**
	 * ID: 6860<br>
	 * Message: Is that all you got, little $s1?
	 */
	public static final NpcStringId IS_THAT_ALL_YOU_GOT_LITTLE_S1;

	/**
	 * ID: 6861<br>
	 * Message: $s1! Wake up fool! Don't let him get away!
	 */
	public static final NpcStringId S1_WAKE_UP_FOOL_DONT_LET_HIM_GET_AWAY;

	/**
	 * ID: 6862<br>
	 * Message: The path is clear, but be careful!
	 */
	public static final NpcStringId THE_PATH_IS_CLEAR_BUT_BE_CAREFUL;

	/**
	 * ID: 6863<br>
	 * Message: I must follow someone now. See you around!
	 */
	public static final NpcStringId I_MUST_FOLLOW_SOMEONE_NOW_SEE_YOU_AROUND;

	/**
	 * ID: 6864<br>
	 * Message: May we meet again.
	 */
	public static final NpcStringId MAY_WE_MEET_AGAIN;

	/**
	 * ID: 6865<br>
	 * Message: Curses on those who blaspheme the Gods!
	 */
	public static final NpcStringId CURSES_ON_THOSE_WHO_BLASPHEME_THE_GODS;

	/**
	 * ID: 6866<br>
	 * Message: Einhasad is calling me! You are lucky and I shall continue my punishment later!
	 */
	public static final NpcStringId EINHASAD_IS_CALLING_ME_YOU_ARE_LUCKY_AND_I_SHALL_CONTINUE_MY_PUNISHMENT_LATER;

	/**
	 * ID: 6867<br>
	 * Message: By the power vested in me by the gods, you $s1, shall die!
	 */
	public static final NpcStringId BY_THE_POWER_VESTED_IN_ME_BY_THE_GODS_YOU_S1_SHALL_DIE;

	/**
	 * ID: 6868<br>
	 * Message: $s1! I will not forget you!
	 */
	public static final NpcStringId S1_I_WILL_NOT_FORGET_YOU;

	/**
	 * ID: 6950<br>
	 * Message: $s1! How dare you interfere! You shall pay for this!
	 */
	public static final NpcStringId S1_HOW_DARE_YOU_INTERFERE_YOU_SHALL_PAY_FOR_THIS;

	/**
	 * ID: 6951<br>
	 * Message: Beleth is calling me. You are lucky but still a fool...
	 */
	public static final NpcStringId BELETH_IS_CALLING_ME_YOU_ARE_LUCKY_BUT_STILL_A_FOOL;

	/**
	 * ID: 6952<br>
	 * Message: I shall take my leave, but will never surrender!
	 */
	public static final NpcStringId I_SHALL_TAKE_MY_LEAVE_BUT_WILL_NEVER_SURRENDER;

	/**
	 * ID: 6954<br>
	 * Message: Cower before my awesome and mighty power!!
	 */
	public static final NpcStringId COWER_BEFORE_MY_AWESOME_AND_MIGHTY_POWER;

	/**
	 * ID: 6955<br>
	 * Message: Grr! Can't you find something better to do with your time?
	 */
	public static final NpcStringId GRR_CANT_YOU_FIND_SOMETHING_BETTER_TO_DO_WITH_YOUR_TIME;

	/**
	 * ID: 6956<br>
	 * Message: I shall take my leave, but your head will be mine some day.. Oh yes..yes it will!
	 */
	public static final NpcStringId I_SHALL_TAKE_MY_LEAVE_BUT_YOUR_HEAD_WILL_BE_MINE_SOME_DAY_OH_YESYES_IT_WILL;

	/**
	 * ID: 6957<br>
	 * Message: My children are stronger!
	 */
	public static final NpcStringId MY_CHILDREN_ARE_STRONGER;

	/**
	 * ID: 6958<br>
	 * Message: $s1! Let's kill them all.
	 */
	public static final NpcStringId S1_LETS_KILL_THEM_ALL;

	/**
	 * ID: 6959<br>
	 * Message: $s1! Come my children...
	 */
	public static final NpcStringId S1_COME_MY_CHILDREN;

	/**
	 * ID: 6960<br>
	 * Message: $s1! Muster your strength... Pick them off like chickens.
	 */
	public static final NpcStringId S1_MUSTER_YOUR_STRENGTH_PICK_THEM_OFF_LIKE_CHICKENS;

	/**
	 * ID: 6961<br>
	 * Message: Thank you my children... Someday, we will meet again.
	 */
	public static final NpcStringId THANK_YOU_MY_CHILDREN_SOMEDAY_WE_WILL_MEET_AGAIN;

	/**
	 * ID: 6962<br>
	 * Message: My children... Seek my enemies.
	 */
	public static final NpcStringId MY_CHILDREN_SEEK_MY_ENEMIES;

	/**
	 * ID: 6963<br>
	 * Message: My children... I grant you my blessings...
	 */
	public static final NpcStringId MY_CHILDREN_I_GRANT_YOU_MY_BLESSINGS;

	/**
	 * ID: 6964<br>
	 * Message: You worthless beings!
	 */
	public static final NpcStringId YOU_WORTHLESS_BEINGS;

	/**
	 * ID: 6965<br>
	 * Message: My time on the material plane has ended. You are lucky...
	 */
	public static final NpcStringId MY_TIME_ON_THE_MATERIAL_PLANE_HAS_ENDED_YOU_ARE_LUCKY;

	/**
	 * ID: 6966<br>
	 * Message: $s1! Worthless beings! Begone!
	 */
	public static final NpcStringId S1_WORTHLESS_BEINGS_BEGONE;

	/**
	 * ID: 6967<br>
	 * Message: $s1! You are the meaning of the word 'danger'...
	 */
	public static final NpcStringId S1_YOU_ARE_THE_MEANING_OF_THE_WORD_DANGER;

	/**
	 * ID: 7050<br>
	 * Message: You made it here, $s1. I'll show my strength. Die!
	 */
	public static final NpcStringId YOU_MADE_IT_HERE_S1_ILL_SHOW_MY_STRENGTH_DIE;

	/**
	 * ID: 7051<br>
	 * Message: Ha! You failed! Are you ready to quit?
	 */
	public static final NpcStringId HA_YOU_FAILED_ARE_YOU_READY_TO_QUIT;

	/**
	 * ID: 7052<br>
	 * Message: I'm the strongest! I lost everything to win!
	 */
	public static final NpcStringId IM_THE_STRONGEST_I_LOST_EVERYTHING_TO_WIN;

	/**
	 * ID: 7053<br>
	 * Message: $s1! Are you doing this because they're Halisha's minions?
	 */
	public static final NpcStringId S1_ARE_YOU_DOING_THIS_BECAUSE_THEYRE_HALISHAS_MINIONS;

	/**
	 * ID: 7054<br>
	 * Message: My spirit is released from this shell. I'm getting close to Halisha...
	 */
	public static final NpcStringId MY_SPIRIT_IS_RELEASED_FROM_THIS_SHELL_IM_GETTING_CLOSE_TO_HALISHA;

	/**
	 * ID: 7055<br>
	 * Message: Mind your own business!
	 */
	public static final NpcStringId MIND_YOUR_OWN_BUSINESS;

	/**
	 * ID: 7056<br>
	 * Message: This fight is a waste of time. Goodbye!
	 */
	public static final NpcStringId THIS_FIGHT_IS_A_WASTE_OF_TIME_GOODBYE;

	/**
	 * ID: 7057<br>
	 * Message: Every cloud has a silver lining, don't you think?
	 */
	public static final NpcStringId EVERY_CLOUD_HAS_A_SILVER_LINING_DONT_YOU_THINK;

	/**
	 * ID: 7058<br>
	 * Message: $s1! Don't listen to this demon.
	 */
	public static final NpcStringId S1_DONT_LISTEN_TO_THIS_DEMON;

	/**
	 * ID: 7059<br>
	 * Message: $s1! Hesitation betrays your heart. Fight!
	 */
	public static final NpcStringId S1_HESITATION_BETRAYS_YOUR_HEART_FIGHT;

	/**
	 * ID: 7060<br>
	 * Message: $s1! Isn't protecting somebody worthy? Isn't that justice?
	 */
	public static final NpcStringId S1_ISNT_PROTECTING_SOMEBODY_WORTHY_ISNT_THAT_JUSTICE;

	/**
	 * ID: 7061<br>
	 * Message: I have urgent business! I gotta go.
	 */
	public static final NpcStringId I_HAVE_URGENT_BUSINESS_I_GOTTA_GO;

	/**
	 * ID: 7062<br>
	 * Message: Are my efforts in vain? Is this the end?
	 */
	public static final NpcStringId ARE_MY_EFFORTS_IN_VAIN_IS_THIS_THE_END;

	/**
	 * ID: 7063<br>
	 * Message: Goodbye, friend. I hope to see you again.
	 */
	public static final NpcStringId GOODBYE_FRIEND_I_HOPE_TO_SEE_YOU_AGAIN;

	/**
	 * ID: 7064<br>
	 * Message: Knights are always foolish!
	 */
	public static final NpcStringId KNIGHTS_ARE_ALWAYS_FOOLISH;

	/**
	 * ID: 7065<br>
	 * Message: I'll show mercy on you for now.
	 */
	public static final NpcStringId ILL_SHOW_MERCY_ON_YOU_FOR_NOW;

	/**
	 * ID: 7066<br>
	 * Message: $s1! Your justice is just hypocrisy if you give up on what you've promised to protect.
	 */
	public static final NpcStringId S1_YOUR_JUSTICE_IS_JUST_HYPOCRISY_IF_YOU_GIVE_UP_ON_WHAT_YOUVE_PROMISED_TO_PROTECT;

	/**
	 * ID: 7067<br>
	 * Message: $s1...Don't think you've won! Your dark shadow will always follow you...hypocrite!
	 */
	public static final NpcStringId S1DONT_THINK_YOUVE_WON_YOUR_DARK_SHADOW_WILL_ALWAYS_FOLLOW_YOUHYPOCRITE;

	/**
	 * ID: 7150<br>
	 * Message: A temple knight guards the Mother Tree! $s1! Has Human contact made you forget that?
	 */
	public static final NpcStringId A_TEMPLE_KNIGHT_GUARDS_THE_MOTHER_TREE_S1_HAS_HUMAN_CONTACT_MADE_YOU_FORGET_THAT;

	/**
	 * ID: 7151<br>
	 * Message: I must stop. Remember, the ones you're protecting will someday defeat the Elves.
	 */
	public static final NpcStringId I_MUST_STOP_REMEMBER_THE_ONES_YOURE_PROTECTING_WILL_SOMEDAY_DEFEAT_THE_ELVES;

	/**
	 * ID: 7152<br>
	 * Message: What! That will just strengthen the enemy!
	 */
	public static final NpcStringId WHAT_THAT_WILL_JUST_STRENGTHEN_THE_ENEMY;

	/**
	 * ID: 7153<br>
	 * Message: You dare to disturb the order of the shrine! Die, $s1!
	 */
	public static final NpcStringId YOU_DARE_TO_DISTURB_THE_ORDER_OF_THE_SHRINE_DIE_S1;

	/**
	 * ID: 7154<br>
	 * Message: My spirit is releasing from this shell. I'm getting close to Halisha...
	 */
	public static final NpcStringId MY_SPIRIT_IS_RELEASING_FROM_THIS_SHELL_IM_GETTING_CLOSE_TO_HALISHA;

	/**
	 * ID: 7156<br>
	 * Message: This is a waste of time. Goodbye!
	 */
	public static final NpcStringId THIS_IS_A_WASTE_OF_TIME_GOODBYE;

	/**
	 * ID: 7157<br>
	 * Message: I'm not like my brother Panacea. Ghost of the past, begone!
	 */
	public static final NpcStringId IM_NOT_LIKE_MY_BROTHER_PANACEA_GHOST_OF_THE_PAST_BEGONE;

	/**
	 * ID: 7158<br>
	 * Message: The Elves no longer rule! Help me, $s1!
	 */
	public static final NpcStringId THE_ELVES_NO_LONGER_RULE_HELP_ME_S1;

	/**
	 * ID: 7159<br>
	 * Message: Don't give up, $s1! He' a demon from the past!
	 */
	public static final NpcStringId DONT_GIVE_UP_S1_HE_A_DEMON_FROM_THE_PAST;

	/**
	 * ID: 7160<br>
	 * Message: Be proud, $s1. We protect this world together.
	 */
	public static final NpcStringId BE_PROUD_S1_WE_PROTECT_THIS_WORLD_TOGETHER;

	/**
	 * ID: 7161<br>
	 * Message: I have to go. I've got some business to take care of.
	 */
	public static final NpcStringId I_HAVE_TO_GO_IVE_GOT_SOME_BUSINESS_TO_TAKE_CARE_OF;

	/**
	 * ID: 7162<br>
	 * Message: Ugh! Don't let him get away!
	 */
	public static final NpcStringId UGH_DONT_LET_HIM_GET_AWAY;

	/**
	 * ID: 7163<br>
	 * Message: Don't forget your pride. You're protecting the world!
	 */
	public static final NpcStringId DONT_FORGET_YOUR_PRIDE_YOURE_PROTECTING_THE_WORLD;

	/**
	 * ID: 7164<br>
	 * Message: Ha, ha, ha!...
	 */
	public static final NpcStringId HA_HA_HA;

	/**
	 * ID: 7165<br>
	 * Message: Kuh, huh...
	 */
	public static final NpcStringId KUH_HUH;

	/**
	 * ID: 7166<br>
	 * Message: Aah! Kuh...$s1!...Kuh, huh...
	 */
	public static final NpcStringId AAH_KUHS1KUH_HUH;

	/**
	 * ID: 7167<br>
	 * Message: $s1!...Re... mem... Ugh...Uh...
	 */
	public static final NpcStringId S1RE_MEM_UGHUH;

	/**
	 * ID: 7250<br>
	 * Message: $s1, You'd better listen.
	 */
	public static final NpcStringId S1_YOUD_BETTER_LISTEN;

	/**
	 * ID: 7251<br>
	 * Message: Huh? It's curtain time! I won't get any money.
	 */
	public static final NpcStringId HUH_ITS_CURTAIN_TIME_I_WONT_GET_ANY_MONEY;

	/**
	 * ID: 7252<br>
	 * Message: Ugh...It can't be...!
	 */
	public static final NpcStringId UGHIT_CANT_BE;

	/**
	 * ID: 7257<br>
	 * Message: You won't get away this time, Narcissus!
	 */
	public static final NpcStringId YOU_WONT_GET_AWAY_THIS_TIME_NARCISSUS;

	/**
	 * ID: 7258<br>
	 * Message: $s1! Help me!
	 */
	public static final NpcStringId S1_HELP_ME;

	/**
	 * ID: 7259<br>
	 * Message: You must be aware of your audience when singing, %s! => Join us $s1! A song that nobody listens to is empty.
	 */
	public static final NpcStringId
			YOU_MUST_BE_AWARE_OF_YOUR_AUDIENCE_WHEN_SINGING_S_JOIN_US_S1_A_SONG_THAT_NOBODY_LISTENS_TO_IS_EMPTY;

	/**
	 * ID: 7260<br>
	 * Message: You must work harder to be victorious, $s1.
	 */
	public static final NpcStringId YOU_MUST_WORK_HARDER_TO_BE_VICTORIOUS_S1;

	/**
	 * ID: 7261<br>
	 * Message: My song is over, I must go. Goodbye!
	 */
	public static final NpcStringId MY_SONG_IS_OVER_I_MUST_GO_GOODBYE;

	/**
	 * ID: 7262<br>
	 * Message: How could I miss!
	 */
	public static final NpcStringId HOW_COULD_I_MISS;

	/**
	 * ID: 7263<br>
	 * Message: Don't forget. Song comes with harmony.
	 */
	public static final NpcStringId DONT_FORGET_SONG_COMES_WITH_HARMONY;

	/**
	 * ID: 7264<br>
	 * Message: Sing. Everyone will listen.
	 */
	public static final NpcStringId SING_EVERYONE_WILL_LISTEN;

	/**
	 * ID: 7265<br>
	 * Message: You don't deserve my blessing.
	 */
	public static final NpcStringId YOU_DONT_DESERVE_MY_BLESSING;

	/**
	 * ID: 7266<br>
	 * Message: Do you reject my blessing, $s1?
	 */
	public static final NpcStringId DO_YOU_REJECT_MY_BLESSING_S1;

	/**
	 * ID: 7267<br>
	 * Message: But why, $s1. Everyone would praise you!
	 */
	public static final NpcStringId BUT_WHY_S1_EVERYONE_WOULD_PRAISE_YOU;

	/**
	 * ID: 7350<br>
	 * Message: $s1! Attack me? I'm immortal! I'm unrivaled!
	 */
	public static final NpcStringId S1_ATTACK_ME_IM_IMMORTAL_IM_UNRIVALED;

	/**
	 * ID: 7351<br>
	 * Message: Ha! I'm immortal. This scar will soon heal. You'll die next time.
	 */
	public static final NpcStringId HA_IM_IMMORTAL_THIS_SCAR_WILL_SOON_HEAL_YOULL_DIE_NEXT_TIME;

	/**
	 * ID: 7352<br>
	 * Message: Metellus! You promised me an immortal life! How could I, Swordmaster Iron, lose?
	 */
	public static final NpcStringId METELLUS_YOU_PROMISED_ME_AN_IMMORTAL_LIFE_HOW_COULD_I_SWORDMASTER_IRON_LOSE;

	/**
	 * ID: 7357<br>
	 * Message: Fallen Angel? It's worth trying.
	 */
	public static final NpcStringId FALLEN_ANGEL_ITS_WORTH_TRYING;

	/**
	 * ID: 7358<br>
	 * Message: Hey $s1! Why don't you join? It's your best shot!
	 */
	public static final NpcStringId HEY_S1_WHY_DONT_YOU_JOIN_ITS_YOUR_BEST_SHOT;

	/**
	 * ID: 7359<br>
	 * Message: Are you interested in immortal life, $s1? It's too boring for me.
	 */
	public static final NpcStringId ARE_YOU_INTERESTED_IN_IMMORTAL_LIFE_S1_ITS_TOO_BORING_FOR_ME;

	/**
	 * ID: 7360<br>
	 * Message: Excellent, $s1! Show me what you learned when your life was on the line!
	 */
	public static final NpcStringId EXCELLENT_S1_SHOW_ME_WHAT_YOU_LEARNED_WHEN_YOUR_LIFE_WAS_ON_THE_LINE;

	/**
	 * ID: 7361<br>
	 * Message: I have to go take a break.
	 */
	public static final NpcStringId I_HAVE_TO_GO_TAKE_A_BREAK;

	/**
	 * ID: 7362<br>
	 * Message: You missed?! What a fool you are!
	 */
	public static final NpcStringId YOU_MISSED_WHAT_A_FOOL_YOU_ARE;

	/**
	 * ID: 7363<br>
	 * Message: Fighting without risk, training without danger and growing without hardship will only lead to an inflated ego. Don't forget.
	 */
	public static final NpcStringId
			FIGHTING_WITHOUT_RISK_TRAINING_WITHOUT_DANGER_AND_GROWING_WITHOUT_HARDSHIP_WILL_ONLY_LEAD_TO_AN_INFLATED_EGO_DONT_FORGET;

	/**
	 * ID: 7364<br>
	 * Message: Do you want an immortal life?
	 */
	public static final NpcStringId DO_YOU_WANT_AN_IMMORTAL_LIFE;

	/**
	 * ID: 7365<br>
	 * Message: Think it over. An immortal life and assured victory...
	 */
	public static final NpcStringId THINK_IT_OVER_AN_IMMORTAL_LIFE_AND_ASSURED_VICTORY;

	/**
	 * ID: 7366<br>
	 * Message: That's good, $s1! Do you want my blessing to improve your skills?
	 */
	public static final NpcStringId THATS_GOOD_S1_DO_YOU_WANT_MY_BLESSING_TO_IMPROVE_YOUR_SKILLS;

	/**
	 * ID: 7367<br>
	 * Message: $s1! Why do you reject guaranteed victory?
	 */
	public static final NpcStringId S1_WHY_DO_YOU_REJECT_GUARANTEED_VICTORY;

	/**
	 * ID: 7450<br>
	 * Message: In the name of gods, I punish you, $s1! You can't rival us all, no matter how strong you think you are!
	 */
	public static final NpcStringId
			IN_THE_NAME_OF_GODS_I_PUNISH_YOU_S1_YOU_CANT_RIVAL_US_ALL_NO_MATTER_HOW_STRONG_YOU_THINK_YOU_ARE;

	/**
	 * ID: 7451<br>
	 * Message: I have to stop for now, but I'll defeat the power of the dragon yet!
	 */
	public static final NpcStringId I_HAVE_TO_STOP_FOR_NOW_BUT_ILL_DEFEAT_THE_POWER_OF_THE_DRAGON_YET;

	/**
	 * ID: 7452<br>
	 * Message: It is...The power that shouldn't live!
	 */
	public static final NpcStringId IT_ISTHE_POWER_THAT_SHOULDNT_LIVE;

	/**
	 * ID: 7457<br>
	 * Message: Isn't it unwise for an angel to interfere in Human affairs?
	 */
	public static final NpcStringId ISNT_IT_UNWISE_FOR_AN_ANGEL_TO_INTERFERE_IN_HUMAN_AFFAIRS;

	/**
	 * ID: 7458<br>
	 * Message: This is tough! $s1, would you help me?
	 */
	public static final NpcStringId THIS_IS_TOUGH_S1_WOULD_YOU_HELP_ME;

	/**
	 * ID: 7459<br>
	 * Message: $s1! He's keeping an eye on all those in succession to the blood of dragons, including you!
	 */
	public static final NpcStringId
			S1_HES_KEEPING_AN_EYE_ON_ALL_THOSE_IN_SUCCESSION_TO_THE_BLOOD_OF_DRAGONS_INCLUDING_YOU;

	/**
	 * ID: 7460<br>
	 * Message: Attack the rear, $s1! If I'm killed, you're next!
	 */
	public static final NpcStringId ATTACK_THE_REAR_S1_IF_IM_KILLED_YOURE_NEXT;

	/**
	 * ID: 7461<br>
	 * Message: I can't stay any longer. There are too many eyes on us. Farewell!
	 */
	public static final NpcStringId I_CANT_STAY_ANY_LONGER_THERE_ARE_TOO_MANY_EYES_ON_US_FAREWELL;

	/**
	 * ID: 7462<br>
	 * Message: Get away? You're still alive. But...
	 */
	public static final NpcStringId GET_AWAY_YOURE_STILL_ALIVE_BUT;

	/**
	 * ID: 7463<br>
	 * Message: If we can't avoid this fight, we'll need great strength. It's the only way to peace.
	 */
	public static final NpcStringId IF_WE_CANT_AVOID_THIS_FIGHT_WELL_NEED_GREAT_STRENGTH_ITS_THE_ONLY_WAY_TO_PEACE;

	/**
	 * ID: 7464<br>
	 * Message: Warlord, you'll never learn the techniques of the dragon!
	 */
	public static final NpcStringId WARLORD_YOULL_NEVER_LEARN_THE_TECHNIQUES_OF_THE_DRAGON;

	/**
	 * ID: 7465<br>
	 * Message: Hey, why bother with this. Isn't it your mother's business?
	 */
	public static final NpcStringId HEY_WHY_BOTHER_WITH_THIS_ISNT_IT_YOUR_MOTHERS_BUSINESS;

	/**
	 * ID: 7466<br>
	 * Message: $s1! Are you against your mother's wishes? You're not worthy of the secrets of Shilen's children!
	 */
	public static final NpcStringId
			S1_ARE_YOU_AGAINST_YOUR_MOTHERS_WISHES_YOURE_NOT_WORTHY_OF_THE_SECRETS_OF_SHILENS_CHILDREN;

	/**
	 * ID: 7467<br>
	 * Message: Excellent technique, $s1. Unfortunately, you're the one destined for tragedy!
	 */
	public static final NpcStringId EXCELLENT_TECHNIQUE_S1_UNFORTUNATELY_YOURE_THE_ONE_DESTINED_FOR_TRAGEDY;

	/**
	 * ID: 7550<br>
	 * Message: $s1! You may follow me, but an Orc is no match for my giant's strength!
	 */
	public static final NpcStringId S1_YOU_MAY_FOLLOW_ME_BUT_AN_ORC_IS_NO_MATCH_FOR_MY_GIANTS_STRENGTH;

	/**
	 * ID: 7551<br>
	 * Message: Kuh...My body fails..This is the end!
	 */
	public static final NpcStringId KUHMY_BODY_FAILSTHIS_IS_THE_END;

	/**
	 * ID: 7552<br>
	 * Message: How could I lose with the powers of a giant? Aargh!!!
	 */
	public static final NpcStringId HOW_COULD_I_LOSE_WITH_THE_POWERS_OF_A_GIANT_AARGH;

	/**
	 * ID: 7557<br>
	 * Message: That's the enemy.
	 */
	public static final NpcStringId THATS_THE_ENEMY;

	/**
	 * ID: 7558<br>
	 * Message: Hmm.. $s1! Will you just stand there doing nothing?
	 */
	public static final NpcStringId HMM_S1_WILL_YOU_JUST_STAND_THERE_DOING_NOTHING;

	/**
	 * ID: 7559<br>
	 * Message: $s1. Nothing risked, nothing gained. Only those who fight enjoy the spoils of victory.
	 */
	public static final NpcStringId S1_NOTHING_RISKED_NOTHING_GAINED_ONLY_THOSE_WHO_FIGHT_ENJOY_THE_SPOILS_OF_VICTORY;

	/**
	 * ID: 7560<br>
	 * Message: A sword isn't jewelry, $s1. Don't you agree?
	 */
	public static final NpcStringId A_SWORD_ISNT_JEWELRY_S1_DONT_YOU_AGREE;

	/**
	 * ID: 7561<br>
	 * Message: With no fight, I have no reason to stay.
	 */
	public static final NpcStringId WITH_NO_FIGHT_I_HAVE_NO_REASON_TO_STAY;

	/**
	 * ID: 7562<br>
	 * Message: Miss...
	 */
	public static final NpcStringId MISS;

	/**
	 * ID: 7563<br>
	 * Message: Pick your battles wisely, or you'll regret it.
	 */
	public static final NpcStringId PICK_YOUR_BATTLES_WISELY_OR_YOULL_REGRET_IT;

	/**
	 * ID: 7564<br>
	 * Message: What a fool to challenge the giant of the Oroka tribe!
	 */
	public static final NpcStringId WHAT_A_FOOL_TO_CHALLENGE_THE_GIANT_OF_THE_OROKA_TRIBE;

	/**
	 * ID: 7565<br>
	 * Message: Running low on steam. I must withdraw.
	 */
	public static final NpcStringId RUNNING_LOW_ON_STEAM_I_MUST_WITHDRAW;

	/**
	 * ID: 7566<br>
	 * Message: $s1. You're the one who defeated Guardian Muhark!
	 */
	public static final NpcStringId S1_YOURE_THE_ONE_WHO_DEFEATED_GUARDIAN_MUHARK;

	/**
	 * ID: 7567<br>
	 * Message: $s1....! I must succeed...
	 */
	public static final NpcStringId S1_I_MUST_SUCCEED;

	/**
	 * ID: 7650<br>
	 * Message: $s1... Would you fight Uruz, who has reached the power of Azira?
	 */
	public static final NpcStringId S1_WOULD_YOU_FIGHT_URUZ_WHO_HAS_REACHED_THE_POWER_OF_AZIRA;

	/**
	 * ID: 7651<br>
	 * Message: I can't handle the power of Azira yet. First...
	 */
	public static final NpcStringId I_CANT_HANDLE_THE_POWER_OF_AZIRA_YET_FIRST;

	/**
	 * ID: 7652<br>
	 * Message: This can't be happening! I have the power of Azira! How could I fall so easily?
	 */
	public static final NpcStringId THIS_CANT_BE_HAPPENING_I_HAVE_THE_POWER_OF_AZIRA_HOW_COULD_I_FALL_SO_EASILY;

	/**
	 * ID: 7657<br>
	 * Message: Azira, born from the Evil Flame, I'll kill you with my bare hands!
	 */
	public static final NpcStringId AZIRA_BORN_FROM_THE_EVIL_FLAME_ILL_KILL_YOU_WITH_MY_BARE_HANDS;

	/**
	 * ID: 7658<br>
	 * Message: $s1! In the name of Khavatari Hubai, strike this evil with your fists!
	 */
	public static final NpcStringId S1_IN_THE_NAME_OF_KHAVATARI_HUBAI_STRIKE_THIS_EVIL_WITH_YOUR_FISTS;

	/**
	 * ID: 7659<br>
	 * Message: $s1! Attack from both sides! Hit hard!
	 */
	public static final NpcStringId S1_ATTACK_FROM_BOTH_SIDES_HIT_HARD;

	/**
	 * ID: 7660<br>
	 * Message: $s1! Strike with all your heart. It must work.
	 */
	public static final NpcStringId S1_STRIKE_WITH_ALL_YOUR_HEART_IT_MUST_WORK;

	/**
	 * ID: 7661<br>
	 * Message: Damn! It's time to go. Until next time.
	 */
	public static final NpcStringId DAMN_ITS_TIME_TO_GO_UNTIL_NEXT_TIME;

	/**
	 * ID: 7662<br>
	 * Message: Evil spirit of flame! I won't give up!
	 */
	public static final NpcStringId EVIL_SPIRIT_OF_FLAME_I_WONT_GIVE_UP;

	/**
	 * ID: 7663<br>
	 * Message: My fist works even on the evil spirit. Don't forget!
	 */
	public static final NpcStringId MY_FIST_WORKS_EVEN_ON_THE_EVIL_SPIRIT_DONT_FORGET;

	/**
	 * ID: 7664<br>
	 * Message: Foolish Khavatari...Do you think your power will work on me? I'm the source of your martial art!
	 */
	public static final NpcStringId
			FOOLISH_KHAVATARIDO_YOU_THINK_YOUR_POWER_WILL_WORK_ON_ME_IM_THE_SOURCE_OF_YOUR_MARTIAL_ART;

	/**
	 * ID: 7665<br>
	 * Message: No more games...
	 */
	public static final NpcStringId NO_MORE_GAMES;

	/**
	 * ID: 7666<br>
	 * Message: $s1...Are you next after Khavatari? Do you know who I am?
	 */
	public static final NpcStringId S1ARE_YOU_NEXT_AFTER_KHAVATARI_DO_YOU_KNOW_WHO_I_AM;

	/**
	 * ID: 7667<br>
	 * Message: $s1...Kashu. Not a bad attack. I can't hold on much longer.
	 */
	public static final NpcStringId S1KASHU_NOT_A_BAD_ATTACK_I_CANT_HOLD_ON_MUCH_LONGER;

	/**
	 * ID: 7750<br>
	 * Message: $s1, Akkan, you can't be my rival! I'll kill everything! I'll be the king!
	 */
	public static final NpcStringId S1_AKKAN_YOU_CANT_BE_MY_RIVAL_ILL_KILL_EVERYTHING_ILL_BE_THE_KING;

	/**
	 * ID: 7751<br>
	 * Message: Ha! I'll show mercy on you this time. I know well of your technique!
	 */
	public static final NpcStringId HA_ILL_SHOW_MERCY_ON_YOU_THIS_TIME_I_KNOW_WELL_OF_YOUR_TECHNIQUE;

	/**
	 * ID: 7752<br>
	 * Message: I have in me the blood of a king! How could I lose?!
	 */
	public static final NpcStringId I_HAVE_IN_ME_THE_BLOOD_OF_A_KING_HOW_COULD_I_LOSE;

	/**
	 * ID: 7757<br>
	 * Message: Are you....tyrant!
	 */
	public static final NpcStringId ARE_YOUTYRANT;

	/**
	 * ID: 7758<br>
	 * Message: You're not a king! You're just a tyrant! $s1, we must fight together!
	 */
	public static final NpcStringId YOURE_NOT_A_KING_YOURE_JUST_A_TYRANT_S1_WE_MUST_FIGHT_TOGETHER;

	/**
	 * ID: 7759<br>
	 * Message: Such rule is ruining the country! $s1, I can't bear this tyranny any longer!
	 */
	public static final NpcStringId SUCH_RULE_IS_RUINING_THE_COUNTRY_S1_I_CANT_BEAR_THIS_TYRANNY_ANY_LONGER;

	/**
	 * ID: 7760<br>
	 * Message: $s1, leaders must always resist tyranny!
	 */
	public static final NpcStringId S1_LEADERS_MUST_ALWAYS_RESIST_TYRANNY;

	/**
	 * ID: 7761<br>
	 * Message: I stayed too long. I'll be punished for being away so long.
	 */
	public static final NpcStringId I_STAYED_TOO_LONG_ILL_BE_PUNISHED_FOR_BEING_AWAY_SO_LONG;

	/**
	 * ID: 7762<br>
	 * Message: He got away, Dammit! We must catch this dark soul!
	 */
	public static final NpcStringId HE_GOT_AWAY_DAMMIT_WE_MUST_CATCH_THIS_DARK_SOUL;

	/**
	 * ID: 7763<br>
	 * Message: What is a king? What must one do to be a good king? Think it over.
	 */
	public static final NpcStringId WHAT_IS_A_KING_WHAT_MUST_ONE_DO_TO_BE_A_GOOD_KING_THINK_IT_OVER;

	/**
	 * ID: 7764<br>
	 * Message: Kneel down before me! Foolish people!
	 */
	public static final NpcStringId KNEEL_DOWN_BEFORE_ME_FOOLISH_PEOPLE;

	/**
	 * ID: 7765<br>
	 * Message: It's time for the king to leave! Bow your head and say goodbye!
	 */
	public static final NpcStringId ITS_TIME_FOR_THE_KING_TO_LEAVE_BOW_YOUR_HEAD_AND_SAY_GOODBYE;

	/**
	 * ID: 7766<br>
	 * Message: $s1! You dare to fight me? A king's power must be great to rule!
	 */
	public static final NpcStringId S1_YOU_DARE_TO_FIGHT_ME_A_KINGS_POWER_MUST_BE_GREAT_TO_RULE;

	/**
	 * ID: 7767<br>
	 * Message: You would fight the king, $s1? Traitor!
	 */
	public static final NpcStringId YOU_WOULD_FIGHT_THE_KING_S1_TRAITOR;

	/**
	 * ID: 7850<br>
	 * Message: Tejakar Sharuhi! $s1, I'll show you the power of Sharuhi Mouth Mudaha!
	 */
	public static final NpcStringId TEJAKAR_SHARUHI_S1_ILL_SHOW_YOU_THE_POWER_OF_SHARUHI_MOUTH_MUDAHA;

	/**
	 * ID: 7851<br>
	 * Message: Aaargh! My soul won't keep quiet. Now I must take my leave.
	 */
	public static final NpcStringId AAARGH_MY_SOUL_WONT_KEEP_QUIET_NOW_I_MUST_TAKE_MY_LEAVE;

	/**
	 * ID: 7852<br>
	 * Message: No, Sharuhi. You're soul is mine!
	 */
	public static final NpcStringId NO_SHARUHI_YOURE_SOUL_IS_MINE;

	/**
	 * ID: 7857<br>
	 * Message: Tejakar Oroca! Tejakar Duda-Mara!
	 */
	public static final NpcStringId TEJAKAR_OROCA_TEJAKAR_DUDA_MARA;

	/**
	 * ID: 7858<br>
	 * Message: $s1, we must fight this soul together to prevent an everlasting winter!
	 */
	public static final NpcStringId S1_WE_MUST_FIGHT_THIS_SOUL_TOGETHER_TO_PREVENT_AN_EVERLASTING_WINTER;

	/**
	 * ID: 7859<br>
	 * Message: $s1! The soul responds to you. May your attack quiet him!
	 */
	public static final NpcStringId S1_THE_SOUL_RESPONDS_TO_YOU_MAY_YOUR_ATTACK_QUIET_HIM;

	/**
	 * ID: 7860<br>
	 * Message: $s1! Calm Sharuhi! He doesn't listen to me anymore.
	 */
	public static final NpcStringId S1_CALM_SHARUHI_HE_DOESNT_LISTEN_TO_ME_ANYMORE;

	/**
	 * ID: 7861<br>
	 * Message: It's time to go! May the eternal flame bless you!
	 */
	public static final NpcStringId ITS_TIME_TO_GO_MAY_THE_ETERNAL_FLAME_BLESS_YOU;

	/**
	 * ID: 7862<br>
	 * Message: He left...That's too bad..Too bad...
	 */
	public static final NpcStringId HE_LEFTTHATS_TOO_BADTOO_BAD;

	/**
	 * ID: 7863<br>
	 * Message: Don't forget your strong will now!
	 */
	public static final NpcStringId DONT_FORGET_YOUR_STRONG_WILL_NOW;

	/**
	 * ID: 7864<br>
	 * Message: Ha! Nobody will rule over me anymore!
	 */
	public static final NpcStringId HA_NOBODY_WILL_RULE_OVER_ME_ANYMORE;

	/**
	 * ID: 7865<br>
	 * Message: Freedom... freedom... freedom!
	 */
	public static final NpcStringId FREEDOM_FREEDOM_FREEDOM;

	/**
	 * ID: 7866<br>
	 * Message: $s1, You released me, but you also want to catch me. Ha!
	 */
	public static final NpcStringId S1_YOU_RELEASED_ME_BUT_YOU_ALSO_WANT_TO_CATCH_ME_HA;

	/**
	 * ID: 7867<br>
	 * Message: ...$s1...Me?....All right...I'll help you.
	 */
	public static final NpcStringId S1MEALL_RIGHTILL_HELP_YOU;

	/**
	 * ID: 7950<br>
	 * Message: Get out of here! This place is forbidden by god.
	 */
	public static final NpcStringId GET_OUT_OF_HERE_THIS_PLACE_IS_FORBIDDEN_BY_GOD;

	/**
	 * ID: 7951<br>
	 * Message: Einhasad is calling me.
	 */
	public static final NpcStringId EINHASAD_IS_CALLING_ME;

	/**
	 * ID: 7952<br>
	 * Message: You killed me! Aren't you afraid of god's curse?
	 */
	public static final NpcStringId YOU_KILLED_ME_ARENT_YOU_AFRAID_OF_GODS_CURSE;

	/**
	 * ID: 7953<br>
	 * Message: You bother my minions, $s1. Do you want to die?
	 */
	public static final NpcStringId YOU_BOTHER_MY_MINIONS_S1_DO_YOU_WANT_TO_DIE;

	/**
	 * ID: 7954<br>
	 * Message: What the hell... I lost.
	 */
	public static final NpcStringId WHAT_THE_HELL_I_LOST;

	/**
	 * ID: 7955<br>
	 * Message: Who are you? Why are you interfering in our business?
	 */
	public static final NpcStringId WHO_ARE_YOU_WHY_ARE_YOU_INTERFERING_IN_OUR_BUSINESS;

	/**
	 * ID: 7956<br>
	 * Message: You're strong. I'll get you next time!
	 */
	public static final NpcStringId YOURE_STRONG_ILL_GET_YOU_NEXT_TIME;

	/**
	 * ID: 7957<br>
	 * Message: We meet again. I'll have you this time!
	 */
	public static final NpcStringId WE_MEET_AGAIN_ILL_HAVE_YOU_THIS_TIME;

	/**
	 * ID: 7958<br>
	 * Message: A worthy opponent. $s1. Help me!
	 */
	public static final NpcStringId A_WORTHY_OPPONENT_S1_HELP_ME;

	/**
	 * ID: 7959<br>
	 * Message: $s1! Hurry before he gets away!
	 */
	public static final NpcStringId S1_HURRY_BEFORE_HE_GETS_AWAY;

	/**
	 * ID: 7960<br>
	 * Message: I'll kill you!
	 */
	public static final NpcStringId ILL_KILL_YOU;

	/**
	 * ID: 7961<br>
	 * Message: Why don't you fight me someday?
	 */
	public static final NpcStringId WHY_DONT_YOU_FIGHT_ME_SOMEDAY;

	/**
	 * ID: 7962<br>
	 * Message: I missed again. Dammit!
	 */
	public static final NpcStringId I_MISSED_AGAIN_DAMMIT;

	/**
	 * ID: 7963<br>
	 * Message: I'm sure we'll meet again someday.
	 */
	public static final NpcStringId IM_SURE_WELL_MEET_AGAIN_SOMEDAY;

	/**
	 * ID: 7964<br>
	 * Message: Curse those who defy the gods!
	 */
	public static final NpcStringId CURSE_THOSE_WHO_DEFY_THE_GODS;

	/**
	 * ID: 7966<br>
	 * Message: You would fight me, a messenger of the gods?
	 */
	public static final NpcStringId YOU_WOULD_FIGHT_ME_A_MESSENGER_OF_THE_GODS;

	/**
	 * ID: 7967<br>
	 * Message: $s1! I won't forget you.
	 */
	public static final NpcStringId S1_I_WONT_FORGET_YOU;

	/**
	 * ID: 8050<br>
	 * Message: $s1! How could you desecrate a holy place?
	 */
	public static final NpcStringId S1_HOW_COULD_YOU_DESECRATE_A_HOLY_PLACE;

	/**
	 * ID: 8051<br>
	 * Message: Leave before you are severely punished!
	 */
	public static final NpcStringId LEAVE_BEFORE_YOU_ARE_SEVERELY_PUNISHED;

	/**
	 * ID: 8052<br>
	 * Message: Einhasad, don't give up on me!
	 */
	public static final NpcStringId EINHASAD_DONT_GIVE_UP_ON_ME;

	/**
	 * ID: 8053<br>
	 * Message: $s1, so you're the one who's looking for me?
	 */
	public static final NpcStringId S1_SO_YOURE_THE_ONE_WHOS_LOOKING_FOR_ME;

	/**
	 * ID: 8054<br>
	 * Message: A mere mortal has defeated me!
	 */
	public static final NpcStringId A_MERE_MORTAL_HAS_DEFEATED_ME;

	/**
	 * ID: 8055<br>
	 * Message: How cowardly to intrude in other people's business.
	 */
	public static final NpcStringId HOW_COWARDLY_TO_INTRUDE_IN_OTHER_PEOPLES_BUSINESS;

	/**
	 * ID: 8056<br>
	 * Message: Time is up.
	 */
	public static final NpcStringId TIME_IS_UP;

	/**
	 * ID: 8057<br>
	 * Message: I'll kill you with my sword!
	 */
	public static final NpcStringId ILL_KILL_YOU_WITH_MY_SWORD;

	/**
	 * ID: 8058<br>
	 * Message: Help me!
	 */
	public static final NpcStringId HELP_ME;

	/**
	 * ID: 8059<br>
	 * Message: Don't miss!
	 */
	public static final NpcStringId DONT_MISS;

	/**
	 * ID: 8060<br>
	 * Message: Keep pushing!
	 */
	public static final NpcStringId KEEP_PUSHING;

	/**
	 * ID: 8061<br>
	 * Message: I'll get him. You'll have your revenge.
	 */
	public static final NpcStringId ILL_GET_HIM_YOULL_HAVE_YOUR_REVENGE;

	/**
	 * ID: 8062<br>
	 * Message: I missed him again. I'll kill you.
	 */
	public static final NpcStringId I_MISSED_HIM_AGAIN_ILL_KILL_YOU;

	/**
	 * ID: 8063<br>
	 * Message: I must follow him.
	 */
	public static final NpcStringId I_MUST_FOLLOW_HIM;

	/**
	 * ID: 8150<br>
	 * Message: $s1, you should leave if you fear god's wrath!
	 */
	public static final NpcStringId S1_YOU_SHOULD_LEAVE_IF_YOU_FEAR_GODS_WRATH;

	/**
	 * ID: 8151<br>
	 * Message: What's going on?
	 */
	public static final NpcStringId WHATS_GOING_ON;

	/**
	 * ID: 8152<br>
	 * Message: I'll see you again!
	 */
	public static final NpcStringId ILL_SEE_YOU_AGAIN;

	/**
	 * ID: 8153<br>
	 * Message: Who are you? Why are you bothering my minions?
	 */
	public static final NpcStringId WHO_ARE_YOU_WHY_ARE_YOU_BOTHERING_MY_MINIONS;

	/**
	 * ID: 8154<br>
	 * Message: No way!!!
	 */
	public static final NpcStringId NO_WAY;

	/**
	 * ID: 8155<br>
	 * Message: Why are you sticking your nose in our business?
	 */
	public static final NpcStringId WHY_ARE_YOU_STICKING_YOUR_NOSE_IN_OUR_BUSINESS;

	/**
	 * ID: 8156<br>
	 * Message: Who are you? How can a creature from the netherworld be so powerful?
	 */
	public static final NpcStringId WHO_ARE_YOU_HOW_CAN_A_CREATURE_FROM_THE_NETHERWORLD_BE_SO_POWERFUL;

	/**
	 * ID: 8157<br>
	 * Message: Is this the end?
	 */
	public static final NpcStringId IS_THIS_THE_END;

	/**
	 * ID: 8158<br>
	 * Message: Show me what you're made of. Kill him!
	 */
	public static final NpcStringId SHOW_ME_WHAT_YOURE_MADE_OF_KILL_HIM;

	/**
	 * ID: 8159<br>
	 * Message: You think you can get him with that?
	 */
	public static final NpcStringId YOU_THINK_YOU_CAN_GET_HIM_WITH_THAT;

	/**
	 * ID: 8160<br>
	 * Message: Pull yourself together! He's trying to get away.
	 */
	public static final NpcStringId PULL_YOURSELF_TOGETHER_HES_TRYING_TO_GET_AWAY;

	/**
	 * ID: 8161<br>
	 * Message: Tell the Black Cat that I got his paid back.
	 */
	public static final NpcStringId TELL_THE_BLACK_CAT_THAT_I_GOT_HIS_PAID_BACK;

	/**
	 * ID: 8162<br>
	 * Message: Black Cat, he'll blame me.
	 */
	public static final NpcStringId BLACK_CAT_HELL_BLAME_ME;

	/**
	 * ID: 8163<br>
	 * Message: I gotta' go now.
	 */
	public static final NpcStringId I_GOTTA_GO_NOW;

	/**
	 * ID: 8166<br>
	 * Message: I'll kill you in the name of god.
	 */
	public static final NpcStringId ILL_KILL_YOU_IN_THE_NAME_OF_GOD;

	/**
	 * ID: 8167<br>
	 * Message: $s1! See you later.
	 */
	public static final NpcStringId S1_SEE_YOU_LATER;

	/**
	 * ID: 8251<br>
	 * Message: Get out before you're punished!
	 */
	public static final NpcStringId GET_OUT_BEFORE_YOURE_PUNISHED;

	/**
	 * ID: 8252<br>
	 * Message: Einhasad, please don't give up on me!
	 */
	public static final NpcStringId EINHASAD_PLEASE_DONT_GIVE_UP_ON_ME;

	/**
	 * ID: 8253<br>
	 * Message: $s1, are you looking for me?
	 */
	public static final NpcStringId S1_ARE_YOU_LOOKING_FOR_ME;

	/**
	 * ID: 8254<br>
	 * Message: A mere mortal is killing me!
	 */
	public static final NpcStringId A_MERE_MORTAL_IS_KILLING_ME;

	/**
	 * ID: 8256<br>
	 * Message: Mortal, don't you recognize my greatness?
	 */
	public static final NpcStringId MORTAL_DONT_YOU_RECOGNIZE_MY_GREATNESS;

	/**
	 * ID: 8257<br>
	 * Message: I'll get you this time.
	 */
	public static final NpcStringId ILL_GET_YOU_THIS_TIME;

	/**
	 * ID: 8258<br>
	 * Message: I'll never forget the taste of his steel, $s1! Let's fight him together!
	 */
	public static final NpcStringId ILL_NEVER_FORGET_THE_TASTE_OF_HIS_STEEL_S1_LETS_FIGHT_HIM_TOGETHER;

	/**
	 * ID: 8259<br>
	 * Message: $s1! Pull yourself together. We'll miss him!
	 */
	public static final NpcStringId S1_PULL_YOURSELF_TOGETHER_WELL_MISS_HIM;

	/**
	 * ID: 8260<br>
	 * Message: $s1! He's trying to get away.
	 */
	public static final NpcStringId S1_HES_TRYING_TO_GET_AWAY;

	/**
	 * ID: 8261<br>
	 * Message: I missed again! Next time...
	 */
	public static final NpcStringId I_MISSED_AGAIN_NEXT_TIME;

	/**
	 * ID: 8262<br>
	 * Message: Dammit! Failed again!
	 */
	public static final NpcStringId DAMMIT_FAILED_AGAIN;

	/**
	 * ID: 8353<br>
	 * Message: You are the one who's looking for me, $s1?
	 */
	public static final NpcStringId YOU_ARE_THE_ONE_WHOS_LOOKING_FOR_ME_S1;

	/**
	 * ID: 8354<br>
	 * Message: A mere mortal has killed me!
	 */
	public static final NpcStringId A_MERE_MORTAL_HAS_KILLED_ME;

	/**
	 * ID: 8355<br>
	 * Message: Who are you? This is none of your business!
	 */
	public static final NpcStringId WHO_ARE_YOU_THIS_IS_NONE_OF_YOUR_BUSINESS;

	/**
	 * ID: 8359<br>
	 * Message: $s1! Pull yourself together.
	 */
	public static final NpcStringId S1_PULL_YOURSELF_TOGETHER;

	/**
	 * ID: 8360<br>
	 * Message: $s1! He'll get away!
	 */
	public static final NpcStringId S1_HELL_GET_AWAY;

	/**
	 * ID: 8452<br>
	 * Message: Einhasad, please don't forsake me!
	 */
	public static final NpcStringId EINHASAD_PLEASE_DONT_FORSAKE_ME;

	/**
	 * ID: 8453<br>
	 * Message: Looking for me, $s1?
	 */
	public static final NpcStringId LOOKING_FOR_ME_S1;

	/**
	 * ID: 8550<br>
	 * Message: $s1! Bishop, how foolish to go against the will of god!
	 */
	public static final NpcStringId S1_BISHOP_HOW_FOOLISH_TO_GO_AGAINST_THE_WILL_OF_GOD;

	/**
	 * ID: 8551<br>
	 * Message: Your faith is stronger than I thought. I'll pay you back next time.
	 */
	public static final NpcStringId YOUR_FAITH_IS_STRONGER_THAN_I_THOUGHT_ILL_PAY_YOU_BACK_NEXT_TIME;

	/**
	 * ID: 8552<br>
	 * Message: Tanakia! Forgive me. I couldn't fulfill your dream!
	 */
	public static final NpcStringId TANAKIA_FORGIVE_ME_I_COULDNT_FULFILL_YOUR_DREAM;

	/**
	 * ID: 8553<br>
	 * Message: $s1, you are the won who's been bothering my minions?
	 */
	public static final NpcStringId S1_YOU_ARE_THE_WON_WHOS_BEEN_BOTHERING_MY_MINIONS;

	/**
	 * ID: 8554<br>
	 * Message: Damn! You've beaten me.
	 */
	public static final NpcStringId DAMN_YOUVE_BEATEN_ME;

	/**
	 * ID: 8555<br>
	 * Message: Who are you? This isn't your business, coward.
	 */
	public static final NpcStringId WHO_ARE_YOU_THIS_ISNT_YOUR_BUSINESS_COWARD;

	/**
	 * ID: 8556<br>
	 * Message: How weak. I'll forgive you this time because you made me laugh.
	 */
	public static final NpcStringId HOW_WEAK_ILL_FORGIVE_YOU_THIS_TIME_BECAUSE_YOU_MADE_ME_LAUGH;

	/**
	 * ID: 8557<br>
	 * Message: You are stronger than I thought, but I'm no weakling!
	 */
	public static final NpcStringId YOU_ARE_STRONGER_THAN_I_THOUGHT_BUT_IM_NO_WEAKLING;

	/**
	 * ID: 8558<br>
	 * Message: He's got a tough shell. $s1! Let's fight together and crack his skull!
	 */
	public static final NpcStringId HES_GOT_A_TOUGH_SHELL_S1_LETS_FIGHT_TOGETHER_AND_CRACK_HIS_SKULL;

	/**
	 * ID: 8560<br>
	 * Message: $s1! We won't beat him unless we give it our all. Come on!
	 */
	public static final NpcStringId S1_WE_WONT_BEAT_HIM_UNLESS_WE_GIVE_IT_OUR_ALL_COME_ON;

	/**
	 * ID: 8561<br>
	 * Message: I'll follow him.
	 */
	public static final NpcStringId ILL_FOLLOW_HIM;

	/**
	 * ID: 8562<br>
	 * Message: I missed again! He's hard to follow.
	 */
	public static final NpcStringId I_MISSED_AGAIN_HES_HARD_TO_FOLLOW;

	/**
	 * ID: 8563<br>
	 * Message: We'll see what the future brings.
	 */
	public static final NpcStringId WELL_SEE_WHAT_THE_FUTURE_BRINGS;

	/**
	 * ID: 8564<br>
	 * Message: For Shilen!
	 */
	public static final NpcStringId FOR_SHILEN;

	/**
	 * ID: 8565<br>
	 * Message: I'll be back. I'll deal with you then.
	 */
	public static final NpcStringId ILL_BE_BACK_ILL_DEAL_WITH_YOU_THEN;

	/**
	 * ID: 8566<br>
	 * Message: $s1! Are you going to fight me?
	 */
	public static final NpcStringId S1_ARE_YOU_GOING_TO_FIGHT_ME;

	/**
	 * ID: 8567<br>
	 * Message: $s1! I'll pay you back. I won't forget you.
	 */
	public static final NpcStringId S1_ILL_PAY_YOU_BACK_I_WONT_FORGET_YOU;

	/**
	 * ID: 8650<br>
	 * Message: $s1! Prophet, how foolish to go against the will of god!
	 */
	public static final NpcStringId S1_PROPHET_HOW_FOOLISH_TO_GO_AGAINST_THE_WILL_OF_GOD;

	/**
	 * ID: 8651<br>
	 * Message: Your faith is stronger than I thought. I'll deal with you next time.
	 */
	public static final NpcStringId YOUR_FAITH_IS_STRONGER_THAN_I_THOUGHT_ILL_DEAL_WITH_YOU_NEXT_TIME;

	/**
	 * ID: 8653<br>
	 * Message: Are you the one who's been bothering my minions, $s1?
	 */
	public static final NpcStringId ARE_YOU_THE_ONE_WHOS_BEEN_BOTHERING_MY_MINIONS_S1;

	/**
	 * ID: 8654<br>
	 * Message: Damn! I can't believe I've been beaten by you!
	 */
	public static final NpcStringId DAMN_I_CANT_BELIEVE_IVE_BEEN_BEATEN_BY_YOU;

	/**
	 * ID: 8655<br>
	 * Message: Who are you? This is none of your business, coward.
	 */
	public static final NpcStringId WHO_ARE_YOU_THIS_IS_NONE_OF_YOUR_BUSINESS_COWARD;

	/**
	 * ID: 8657<br>
	 * Message: I'll destroy the darkness surrounding the world with the power of light!
	 */
	public static final NpcStringId ILL_DESTROY_THE_DARKNESS_SURROUNDING_THE_WORLD_WITH_THE_POWER_OF_LIGHT;

	/**
	 * ID: 8658<br>
	 * Message: $s1! Fight the Fallen Angel with me. Show the true power of light!
	 */
	public static final NpcStringId S1_FIGHT_THE_FALLEN_ANGEL_WITH_ME_SHOW_THE_TRUE_POWER_OF_LIGHT;

	/**
	 * ID: 8659<br>
	 * Message: $s1! Go! We must stop fighting here.
	 */
	public static final NpcStringId S1_GO_WE_MUST_STOP_FIGHTING_HERE;

	/**
	 * ID: 8660<br>
	 * Message: We mustn't lose, $s1! Pull yourself together!
	 */
	public static final NpcStringId WE_MUSTNT_LOSE_S1_PULL_YOURSELF_TOGETHER;

	/**
	 * ID: 8661<br>
	 * Message: We'll meet again if fate wills it.
	 */
	public static final NpcStringId WELL_MEET_AGAIN_IF_FATE_WILLS_IT;

	/**
	 * ID: 8662<br>
	 * Message: I'll follow the cowardly devil.
	 */
	public static final NpcStringId ILL_FOLLOW_THE_COWARDLY_DEVIL;

	/**
	 * ID: 8750<br>
	 * Message: $s1! Elder, it's foolish of you to go against the will of the gods.
	 */
	public static final NpcStringId S1_ELDER_ITS_FOOLISH_OF_YOU_TO_GO_AGAINST_THE_WILL_OF_THE_GODS;

	/**
	 * ID: 8757<br>
	 * Message: You're stronger than I thought, but I'm no weakling either!
	 */
	public static final NpcStringId YOURE_STRONGER_THAN_I_THOUGHT_BUT_IM_NO_WEAKLING_EITHER;

	/**
	 * ID: 8760<br>
	 * Message: $s1! We'll never win unless we give it our all. Come on!
	 */
	public static final NpcStringId S1_WELL_NEVER_WIN_UNLESS_WE_GIVE_IT_OUR_ALL_COME_ON;

	/**
	 * ID: 8850<br>
	 * Message: Are you $s1? Oh! I have the Resonance Amulet!
	 */
	public static final NpcStringId ARE_YOU_S1_OH_I_HAVE_THE_RESONANCE_AMULET;

	/**
	 * ID: 8851<br>
	 * Message: You're feistier than I thought! I'll quit here for today.
	 */
	public static final NpcStringId YOURE_FEISTIER_THAN_I_THOUGHT_ILL_QUIT_HERE_FOR_TODAY;

	/**
	 * ID: 8852<br>
	 * Message: Aaargh! I can't believe I lost...
	 */
	public static final NpcStringId AAARGH_I_CANT_BELIEVE_I_LOST;

	/**
	 * ID: 8854<br>
	 * Message: Yikes! You're tough!
	 */
	public static final NpcStringId YIKES_YOURE_TOUGH;

	/**
	 * ID: 8855<br>
	 * Message: Why do you interfere in other people's business...
	 */
	public static final NpcStringId WHY_DO_YOU_INTERFERE_IN_OTHER_PEOPLES_BUSINESS;

	/**
	 * ID: 8856<br>
	 * Message: I'll stop here for today.
	 */
	public static final NpcStringId ILL_STOP_HERE_FOR_TODAY;

	/**
	 * ID: 8857<br>
	 * Message: I won't miss you this time!
	 */
	public static final NpcStringId I_WONT_MISS_YOU_THIS_TIME;

	/**
	 * ID: 8858<br>
	 * Message: Dammit! This is too hard by myself... $s1! Give me a hand!
	 */
	public static final NpcStringId DAMMIT_THIS_IS_TOO_HARD_BY_MYSELF_S1_GIVE_ME_A_HAND;

	/**
	 * ID: 8859<br>
	 * Message: $s1! Hurry up, we'll miss him.
	 */
	public static final NpcStringId S1_HURRY_UP_WELL_MISS_HIM;

	/**
	 * ID: 8860<br>
	 * Message: $s1! Come on. Hurry up!
	 */
	public static final NpcStringId S1_COME_ON_HURRY_UP;

	/**
	 * ID: 8861<br>
	 * Message: I gotta' go follow him.
	 */
	public static final NpcStringId I_GOTTA_GO_FOLLOW_HIM;

	/**
	 * ID: 8862<br>
	 * Message: Hey, quit running! Stop!
	 */
	public static final NpcStringId HEY_QUIT_RUNNING_STOP;

	/**
	 * ID: 8863<br>
	 * Message: See you next time~
	 */
	public static final NpcStringId SEE_YOU_NEXT_TIME;

	/**
	 * ID: 8864<br>
	 * Message: What? Think you can get in my way?
	 */
	public static final NpcStringId WHAT_THINK_YOU_CAN_GET_IN_MY_WAY;

	/**
	 * ID: 8865<br>
	 * Message: You are so weak. I gotta' go now!
	 */
	public static final NpcStringId YOU_ARE_SO_WEAK_I_GOTTA_GO_NOW;

	/**
	 * ID: 8866<br>
	 * Message: $s1! Good. I'll help you.
	 */
	public static final NpcStringId S1_GOOD_ILL_HELP_YOU;

	/**
	 * ID: 8867<br>
	 * Message: $s1, you're stronger than I thought. See you next time.
	 */
	public static final NpcStringId S1_YOURE_STRONGER_THAN_I_THOUGHT_SEE_YOU_NEXT_TIME;

	/**
	 * ID: 8951<br>
	 * Message: You're feistier than I thought! I'll stop here today.
	 */
	public static final NpcStringId YOURE_FEISTIER_THAN_I_THOUGHT_ILL_STOP_HERE_TODAY;

	/**
	 * ID: 8952<br>
	 * Message: Aargh! I can't believe I lost...
	 */
	public static final NpcStringId AARGH_I_CANT_BELIEVE_I_LOST;

	/**
	 * ID: 8956<br>
	 * Message: I'll stop here today.
	 */
	public static final NpcStringId ILL_STOP_HERE_TODAY;

	/**
	 * ID: 8958<br>
	 * Message: Damn! It's too much by myself...$s1! Give me a hand!
	 */
	public static final NpcStringId DAMN_ITS_TOO_MUCH_BY_MYSELFS1_GIVE_ME_A_HAND;

	/**
	 * ID: 8959<br>
	 * Message: $s1! Hurry, we'll miss him.
	 */
	public static final NpcStringId S1_HURRY_WELL_MISS_HIM;

	/**
	 * ID: 8960<br>
	 * Message: $s1! Hurry, please!
	 */
	public static final NpcStringId S1_HURRY_PLEASE;

	/**
	 * ID: 8961<br>
	 * Message: I gotta' go follow him now.
	 */
	public static final NpcStringId I_GOTTA_GO_FOLLOW_HIM_NOW;

	/**
	 * ID: 8962<br>
	 * Message: Are you running away? Stop!
	 */
	public static final NpcStringId ARE_YOU_RUNNING_AWAY_STOP;

	/**
	 * ID: 8964<br>
	 * Message: Do you think you can stop me?
	 */
	public static final NpcStringId DO_YOU_THINK_YOU_CAN_STOP_ME;

	/**
	 * ID: 8965<br>
	 * Message: You're so weak. I gotta' go now...
	 */
	public static final NpcStringId YOURE_SO_WEAK_I_GOTTA_GO_NOW;

	/**
	 * ID: 8966<br>
	 * Message: You're $s1! Good. I'll help you.
	 */
	public static final NpcStringId YOURE_S1_GOOD_ILL_HELP_YOU;

	/**
	 * ID: 9050<br>
	 * Message: Are you $s1? Oh! I have a Resonance Amulet!
	 */
	public static final NpcStringId ARE_YOU_S1_OH_I_HAVE_A_RESONANCE_AMULET;

	/**
	 * ID: 9051<br>
	 * Message: Hey, you're more tenacious than I thought! I'll stop here today.
	 */
	public static final NpcStringId HEY_YOURE_MORE_TENACIOUS_THAN_I_THOUGHT_ILL_STOP_HERE_TODAY;

	/**
	 * ID: 9058<br>
	 * Message: Dammit! I can't do this alone, $s1! Give me a hand!
	 */
	public static final NpcStringId DAMMIT_I_CANT_DO_THIS_ALONE_S1_GIVE_ME_A_HAND;

	/**
	 * ID: 9059<br>
	 * Message: $s1! Hurry or we'll miss him.
	 */
	public static final NpcStringId S1_HURRY_OR_WELL_MISS_HIM;

	/**
	 * ID: 9060<br>
	 * Message: $s1! Hurry up!
	 */
	public static final NpcStringId S1_HURRY_UP;

	/**
	 * ID: 9061<br>
	 * Message: I gotta' follow him now.
	 */
	public static final NpcStringId I_GOTTA_FOLLOW_HIM_NOW;

	/**
	 * ID: 9062<br>
	 * Message: Hey, are you running? Stop!
	 */
	public static final NpcStringId HEY_ARE_YOU_RUNNING_STOP;

	/**
	 * ID: 9066<br>
	 * Message: Oh! You're $s1! Good. I'll help you.
	 */
	public static final NpcStringId OH_YOURE_S1_GOOD_ILL_HELP_YOU;

	/**
	 * ID: 9150<br>
	 * Message: You carouse with evil spirits, $s1! You're not worthy of the holy wisdom!
	 */
	public static final NpcStringId YOU_CAROUSE_WITH_EVIL_SPIRITS_S1_YOURE_NOT_WORTHY_OF_THE_HOLY_WISDOM;

	/**
	 * ID: 9151<br>
	 * Message: You're so stubborn! I can't boss you around any more, can I?
	 */
	public static final NpcStringId YOURE_SO_STUBBORN_I_CANT_BOSS_YOU_AROUND_ANY_MORE_CAN_I;

	/**
	 * ID: 9152<br>
	 * Message: How could it happen? Defeated by a Human!
	 */
	public static final NpcStringId HOW_COULD_IT_HAPPEN_DEFEATED_BY_A_HUMAN;

	/**
	 * ID: 9157<br>
	 * Message: My master sent me here! I'll give you a hand.
	 */
	public static final NpcStringId MY_MASTER_SENT_ME_HERE_ILL_GIVE_YOU_A_HAND;

	/**
	 * ID: 9158<br>
	 * Message: Meow~! Master $s1, help me!
	 */
	public static final NpcStringId MEOW_MASTER_S1_HELP_ME;

	/**
	 * ID: 9159<br>
	 * Message: Master $s1. Punish him so he can't bother Belinda!
	 */
	public static final NpcStringId MASTER_S1_PUNISH_HIM_SO_HE_CANT_BOTHER_BELINDA;

	/**
	 * ID: 9160<br>
	 * Message: Master $s1, We'll miss him!
	 */
	public static final NpcStringId MASTER_S1_WELL_MISS_HIM;

	/**
	 * ID: 9161<br>
	 * Message: Meow~! My master is calling. Meow! I gotta' go now~!
	 */
	public static final NpcStringId MEOW_MY_MASTER_IS_CALLING_MEOW_I_GOTTA_GO_NOW;

	/**
	 * ID: 9162<br>
	 * Message: Meow~! I missed him. Meow!
	 */
	public static final NpcStringId MEOW_I_MISSED_HIM_MEOW;

	/**
	 * ID: 9163<br>
	 * Message: Good luck! Meow~! I gotta' go now.
	 */
	public static final NpcStringId GOOD_LUCK_MEOW_I_GOTTA_GO_NOW;

	/**
	 * ID: 9164<br>
	 * Message: Curiosity killed the cat? I'll show you!
	 */
	public static final NpcStringId CURIOSITY_KILLED_THE_CAT_ILL_SHOW_YOU;

	/**
	 * ID: 9165<br>
	 * Message: That's all for today...!
	 */
	public static final NpcStringId THATS_ALL_FOR_TODAY;

	/**
	 * ID: 9166<br>
	 * Message: Are you trying to take Belinda from me, $s1? I'll show you!
	 */
	public static final NpcStringId ARE_YOU_TRYING_TO_TAKE_BELINDA_FROM_ME_S1_ILL_SHOW_YOU;

	/**
	 * ID: 9167<br>
	 * Message: Belinda! I love you! Yikes!!!
	 */
	public static final NpcStringId BELINDA_I_LOVE_YOU_YIKES;

	/**
	 * ID: 9251<br>
	 * Message: You're stubborn as a mule! Guess I can't boss you around any more!
	 */
	public static final NpcStringId YOURE_STUBBORN_AS_A_MULE_GUESS_I_CANT_BOSS_YOU_AROUND_ANY_MORE;

	/**
	 * ID: 9252<br>
	 * Message: How could it be?...Defeated by an Elf!
	 */
	public static final NpcStringId HOW_COULD_IT_BEDEFEATED_BY_AN_ELF;

	/**
	 * ID: 9257<br>
	 * Message: I came to help you. It's the will of Radyss.
	 */
	public static final NpcStringId I_CAME_TO_HELP_YOU_ITS_THE_WILL_OF_RADYSS;

	/**
	 * ID: 9258<br>
	 * Message: $s1! Fight with me!
	 */
	public static final NpcStringId S1_FIGHT_WITH_ME;

	/**
	 * ID: 9259<br>
	 * Message: $s1! We must defeat him!
	 */
	public static final NpcStringId S1_WE_MUST_DEFEAT_HIM;

	/**
	 * ID: 9260<br>
	 * Message: $s1. There's no time. We must defeat him!
	 */
	public static final NpcStringId S1_THERES_NO_TIME_WE_MUST_DEFEAT_HIM;

	/**
	 * ID: 9261<br>
	 * Message: Radyss is calling me. I gotta' go now.
	 */
	public static final NpcStringId RADYSS_IS_CALLING_ME_I_GOTTA_GO_NOW;

	/**
	 * ID: 9262<br>
	 * Message: I was unable to avenge my brother.
	 */
	public static final NpcStringId I_WAS_UNABLE_TO_AVENGE_MY_BROTHER;

	/**
	 * ID: 9263<br>
	 * Message: May you be blessed.
	 */
	public static final NpcStringId MAY_YOU_BE_BLESSED;

	/**
	 * ID: 9264<br>
	 * Message: The proud, repent! The foolish, awaken! Sinners, die!
	 */
	public static final NpcStringId THE_PROUD_REPENT_THE_FOOLISH_AWAKEN_SINNERS_DIE;

	/**
	 * ID: 9265<br>
	 * Message: Hell's master is calling. Atonement will have to wait!
	 */
	public static final NpcStringId HELLS_MASTER_IS_CALLING_ATONEMENT_WILL_HAVE_TO_WAIT;

	/**
	 * ID: 9266<br>
	 * Message: $s1, I'll remember your name, heathen.
	 */
	public static final NpcStringId S1_ILL_REMEMBER_YOUR_NAME_HEATHEN;

	/**
	 * ID: 9267<br>
	 * Message: I won't forget the name of one who doesn't obey holy judgment, $s1!
	 */
	public static final NpcStringId I_WONT_FORGET_THE_NAME_OF_ONE_WHO_DOESNT_OBEY_HOLY_JUDGMENT_S1;

	/**
	 * ID: 9351<br>
	 * Message: You're stubborn as a mule! I guess I can't boss you around any more!
	 */
	public static final NpcStringId YOURE_STUBBORN_AS_A_MULE_I_GUESS_I_CANT_BOSS_YOU_AROUND_ANY_MORE;

	/**
	 * ID: 9352<br>
	 * Message: Could it be...? Defeated by a Dark Elf!
	 */
	public static final NpcStringId COULD_IT_BE_DEFEATED_BY_A_DARK_ELF;

	/**
	 * ID: 9357<br>
	 * Message: Shadow Summoner, I came here to help you.
	 */
	public static final NpcStringId SHADOW_SUMMONER_I_CAME_HERE_TO_HELP_YOU;

	/**
	 * ID: 9358<br>
	 * Message: Shadow Summoner, $s1! Fight with me!
	 */
	public static final NpcStringId SHADOW_SUMMONER_S1_FIGHT_WITH_ME;

	/**
	 * ID: 9359<br>
	 * Message: $s1, You'll die if you don't kill him!
	 */
	public static final NpcStringId S1_YOULL_DIE_IF_YOU_DONT_KILL_HIM;

	/**
	 * ID: 9360<br>
	 * Message: Hurry, $s1! Don't miss him!
	 */
	public static final NpcStringId HURRY_S1_DONT_MISS_HIM;

	/**
	 * ID: 9361<br>
	 * Message: I can't hold on any longer...
	 */
	public static final NpcStringId I_CANT_HOLD_ON_ANY_LONGER;

	/**
	 * ID: 9362<br>
	 * Message: After all that...I missed him...
	 */
	public static final NpcStringId AFTER_ALL_THATI_MISSED_HIM;

	/**
	 * ID: 9363<br>
	 * Message: Shadow summoner! May you be blessed!
	 */
	public static final NpcStringId SHADOW_SUMMONER_MAY_YOU_BE_BLESSED;

	/**
	 * ID: 9364<br>
	 * Message: My master sent me here to kill you!
	 */
	public static final NpcStringId MY_MASTER_SENT_ME_HERE_TO_KILL_YOU;

	/**
	 * ID: 9365<br>
	 * Message: The shadow is calling me...
	 */
	public static final NpcStringId THE_SHADOW_IS_CALLING_ME;

	/**
	 * ID: 9366<br>
	 * Message: $s1, you want to die early? I'll send you to the darkness!
	 */
	public static final NpcStringId S1_YOU_WANT_TO_DIE_EARLY_ILL_SEND_YOU_TO_THE_DARKNESS;

	/**
	 * ID: 9367<br>
	 * Message: You deal in darkness, $s1! I'll pay you back.
	 */
	public static final NpcStringId YOU_DEAL_IN_DARKNESS_S1_ILL_PAY_YOU_BACK;

	/**
	 * ID: 9450<br>
	 * Message: You're $s1? I won't be like Hindemith!
	 */
	public static final NpcStringId YOURE_S1_I_WONT_BE_LIKE_HINDEMITH;

	/**
	 * ID: 9451<br>
	 * Message: You're feistier than I thought! I'll stop here for today.
	 */
	public static final NpcStringId YOURE_FEISTIER_THAN_I_THOUGHT_ILL_STOP_HERE_FOR_TODAY;

	/**
	 * ID: 9453<br>
	 * Message: Are you the one who is bothering my minions, $s1?
	 */
	public static final NpcStringId ARE_YOU_THE_ONE_WHO_IS_BOTHERING_MY_MINIONS_S1;

	/**
	 * ID: 9457<br>
	 * Message: I can't let you commune with Tablet of Vision! Give me the Resonance Amulet!
	 */
	public static final NpcStringId I_CANT_LET_YOU_COMMUNE_WITH_TABLET_OF_VISION_GIVE_ME_THE_RESONANCE_AMULET;

	/**
	 * ID: 9460<br>
	 * Message: $s1! Please hurry!
	 */
	public static final NpcStringId S1_PLEASE_HURRY;

	/**
	 * ID: 9461<br>
	 * Message: I must follow him now.
	 */
	public static final NpcStringId I_MUST_FOLLOW_HIM_NOW;

	/**
	 * ID: 9462<br>
	 * Message: Are you running? Stop!
	 */
	public static final NpcStringId ARE_YOU_RUNNING_STOP;

	/**
	 * ID: 9464<br>
	 * Message: Are you betraying me? I thought something was wrong...I'll stop here.
	 */
	public static final NpcStringId ARE_YOU_BETRAYING_ME_I_THOUGHT_SOMETHING_WAS_WRONGILL_STOP_HERE;

	/**
	 * ID: 9466<br>
	 * Message: You're $s1? Even two of you can't stop me!
	 */
	public static final NpcStringId YOURE_S1_EVEN_TWO_OF_YOU_CANT_STOP_ME;

	/**
	 * ID: 9467<br>
	 * Message: Dammit! My Resonance Amulet...$s1, I'll never forget to pay you back.
	 */
	public static final NpcStringId DAMMIT_MY_RESONANCE_AMULETS1_ILL_NEVER_FORGET_TO_PAY_YOU_BACK;

	/**
	 * ID: 9550<br>
	 * Message: Are you... $s1? I won't be like Waldstein!
	 */
	public static final NpcStringId ARE_YOU_S1_I_WONT_BE_LIKE_WALDSTEIN;

	/**
	 * ID: 9552<br>
	 * Message: Yikes! I can't believe I lost...
	 */
	public static final NpcStringId YIKES_I_CANT_BELIEVE_I_LOST;

	/**
	 * ID: 9553<br>
	 * Message: Are you the one bothering my minions, $s1?
	 */
	public static final NpcStringId ARE_YOU_THE_ONE_BOTHERING_MY_MINIONS_S1;

	/**
	 * ID: 9557<br>
	 * Message: You can't commune with the Tablet of Vision! Give me the Resonance Amulet!
	 */
	public static final NpcStringId YOU_CANT_COMMUNE_WITH_THE_TABLET_OF_VISION_GIVE_ME_THE_RESONANCE_AMULET;

	/**
	 * ID: 9567<br>
	 * Message: Dammit! My Resonance Amulet...$s1, I'll never forget this.
	 */
	public static final NpcStringId DAMMIT_MY_RESONANCE_AMULETS1_ILL_NEVER_FORGET_THIS;

	/**
	 * ID: 9650<br>
	 * Message: You're $s1? I'll kill you for Hallate!
	 */
	public static final NpcStringId YOURE_S1_ILL_KILL_YOU_FOR_HALLATE;

	/**
	 * ID: 9651<br>
	 * Message: You're tougher than I thought, but you still can't rival me!
	 */
	public static final NpcStringId YOURE_TOUGHER_THAN_I_THOUGHT_BUT_YOU_STILL_CANT_RIVAL_ME;

	/**
	 * ID: 9652<br>
	 * Message: Hallate! Forgive me! I can't help you.
	 */
	public static final NpcStringId HALLATE_FORGIVE_ME_I_CANT_HELP_YOU;

	/**
	 * ID: 9654<br>
	 * Message: Dammit! I can't believe you beat me!
	 */
	public static final NpcStringId DAMMIT_I_CANT_BELIEVE_YOU_BEAT_ME;

	/**
	 * ID: 9655<br>
	 * Message: Who are you? Mind your own business, coward.
	 */
	public static final NpcStringId WHO_ARE_YOU_MIND_YOUR_OWN_BUSINESS_COWARD;

	/**
	 * ID: 9657<br>
	 * Message: Purgatory Lord, I won't fail this time.
	 */
	public static final NpcStringId PURGATORY_LORD_I_WONT_FAIL_THIS_TIME;

	/**
	 * ID: 9658<br>
	 * Message: $s1! Now's the time to put your training to the test!
	 */
	public static final NpcStringId S1_NOWS_THE_TIME_TO_PUT_YOUR_TRAINING_TO_THE_TEST;

	/**
	 * ID: 9659<br>
	 * Message: $s1! Your sword skills can't be that bad.
	 */
	public static final NpcStringId S1_YOUR_SWORD_SKILLS_CANT_BE_THAT_BAD;

	/**
	 * ID: 9660<br>
	 * Message: $s1! Show your strength!
	 */
	public static final NpcStringId S1_SHOW_YOUR_STRENGTH;

	/**
	 * ID: 9661<br>
	 * Message: I have some pressing business. I have to go.
	 */
	public static final NpcStringId I_HAVE_SOME_PRESSING_BUSINESS_I_HAVE_TO_GO;

	/**
	 * ID: 9662<br>
	 * Message: I missed him! Dammit.
	 */
	public static final NpcStringId I_MISSED_HIM_DAMMIT;

	/**
	 * ID: 9663<br>
	 * Message: Try again sometime.
	 */
	public static final NpcStringId TRY_AGAIN_SOMETIME;

	/**
	 * ID: 9664<br>
	 * Message: I'll kill anyone who gets in my way!
	 */
	public static final NpcStringId ILL_KILL_ANYONE_WHO_GETS_IN_MY_WAY;

	/**
	 * ID: 9665<br>
	 * Message: This is pathetic! You make me laugh.
	 */
	public static final NpcStringId THIS_IS_PATHETIC_YOU_MAKE_ME_LAUGH;

	/**
	 * ID: 9666<br>
	 * Message: $s1! Are you trying to get in my way?
	 */
	public static final NpcStringId S1_ARE_YOU_TRYING_TO_GET_IN_MY_WAY;

	/**
	 * ID: 9667<br>
	 * Message: $s1! When I come back, I'll kill you.
	 */
	public static final NpcStringId S1_WHEN_I_COME_BACK_ILL_KILL_YOU;

	/**
	 * ID: 9750<br>
	 * Message: $s1? Wake up! Time to die!
	 */
	public static final NpcStringId S1_WAKE_UP_TIME_TO_DIE;

	/**
	 * ID: 9751<br>
	 * Message: You're tougher than I thought!  I'll be back!
	 */
	public static final NpcStringId YOURE_TOUGHER_THAN_I_THOUGHT_ILL_BE_BACK;

	/**
	 * ID: 9752<br>
	 * Message: I lost? It can't be!
	 */
	public static final NpcStringId I_LOST_IT_CANT_BE;

	/**
	 * ID: 9757<br>
	 * Message: You're a cunning fiend. I won't fail again.
	 */
	public static final NpcStringId YOURE_A_CUNNING_FIEND_I_WONT_FAIL_AGAIN;

	/**
	 * ID: 9758<br>
	 * Message: $s1! It's after you! Fight!
	 */
	public static final NpcStringId S1_ITS_AFTER_YOU_FIGHT;

	/**
	 * ID: 9759<br>
	 * Message: $s1! You have to fight better than that if you expect to survive!
	 */
	public static final NpcStringId S1_YOU_HAVE_TO_FIGHT_BETTER_THAN_THAT_IF_YOU_EXPECT_TO_SURVIVE;

	/**
	 * ID: 9760<br>
	 * Message: $s1! Pull yourself together. Fight!
	 */
	public static final NpcStringId S1_PULL_YOURSELF_TOGETHER_FIGHT;

	/**
	 * ID: 9761<br>
	 * Message: I'll catch the cunning fiend.
	 */
	public static final NpcStringId ILL_CATCH_THE_CUNNING_FIEND;

	/**
	 * ID: 9762<br>
	 * Message: I missed him again! He's clever!
	 */
	public static final NpcStringId I_MISSED_HIM_AGAIN_HES_CLEVER;

	/**
	 * ID: 9763<br>
	 * Message: Don't cower like a puppy next time!
	 */
	public static final NpcStringId DONT_COWER_LIKE_A_PUPPY_NEXT_TIME;

	/**
	 * ID: 9764<br>
	 * Message: I have only one goal. Get out of my way.
	 */
	public static final NpcStringId I_HAVE_ONLY_ONE_GOAL_GET_OUT_OF_MY_WAY;

	/**
	 * ID: 9765<br>
	 * Message: Just wait. You'll get yours!
	 */
	public static final NpcStringId JUST_WAIT_YOULL_GET_YOURS;

	/**
	 * ID: 9766<br>
	 * Message: $s1! You're a coward, aren't you!
	 */
	public static final NpcStringId S1_YOURE_A_COWARD_ARENT_YOU;

	/**
	 * ID: 9767<br>
	 * Message: $s1! I'll kill you next time.
	 */
	public static final NpcStringId S1_ILL_KILL_YOU_NEXT_TIME;

	/**
	 * ID: 9850<br>
	 * Message: $s1! How foolish to act against the will of god.
	 */
	public static final NpcStringId S1_HOW_FOOLISH_TO_ACT_AGAINST_THE_WILL_OF_GOD;

	/**
	 * ID: 9851<br>
	 * Message: Your faith is stronger than I thought. I'll get you next time.
	 */
	public static final NpcStringId YOUR_FAITH_IS_STRONGER_THAN_I_THOUGHT_ILL_GET_YOU_NEXT_TIME;

	/**
	 * ID: 9855<br>
	 * Message: Who are you? Mind your own business, you coward!
	 */
	public static final NpcStringId WHO_ARE_YOU_MIND_YOUR_OWN_BUSINESS_YOU_COWARD;

	/**
	 * ID: 9857<br>
	 * Message: Tanakia, your lie has already been exposed!
	 */
	public static final NpcStringId TANAKIA_YOUR_LIE_HAS_ALREADY_BEEN_EXPOSED;

	/**
	 * ID: 9858<br>
	 * Message: $s1! Help me overcome this.
	 */
	public static final NpcStringId S1_HELP_ME_OVERCOME_THIS;

	/**
	 * ID: 9859<br>
	 * Message: $s1! We can't defeat Tanakia this way.
	 */
	public static final NpcStringId S1_WE_CANT_DEFEAT_TANAKIA_THIS_WAY;

	/**
	 * ID: 9860<br>
	 * Message: $s1! Here's our chance. Don't squander the opportunity!
	 */
	public static final NpcStringId S1_HERES_OUR_CHANCE_DONT_SQUANDER_THE_OPPORTUNITY;

	/**
	 * ID: 9861<br>
	 * Message: Goodbye. We'll meet again if fate allows.
	 */
	public static final NpcStringId GOODBYE_WELL_MEET_AGAIN_IF_FATE_ALLOWS;

	/**
	 * ID: 9862<br>
	 * Message: I'll follow Tanakia to correct this falsehood.
	 */
	public static final NpcStringId ILL_FOLLOW_TANAKIA_TO_CORRECT_THIS_FALSEHOOD;

	/**
	 * ID: 9863<br>
	 * Message: I'll be back if fate allows.
	 */
	public static final NpcStringId ILL_BE_BACK_IF_FATE_ALLOWS;

	/**
	 * ID: 9865<br>
	 * Message: I'll be back. You'll pay.
	 */
	public static final NpcStringId ILL_BE_BACK_YOULL_PAY;

	/**
	 * ID: 9866<br>
	 * Message: $s1! Are you trying to spoil my plan?
	 */
	public static final NpcStringId S1_ARE_YOU_TRYING_TO_SPOIL_MY_PLAN;

	/**
	 * ID: 9867<br>
	 * Message: $s1! I won't forget you. You'll pay.
	 */
	public static final NpcStringId S1_I_WONT_FORGET_YOU_YOULL_PAY;

	/**
	 * ID: 9950<br>
	 * Message: $s1, You have an affinity for dangerous ideas. Are you ready to die?
	 */
	public static final NpcStringId S1_YOU_HAVE_AN_AFFINITY_FOR_DANGEROUS_IDEAS_ARE_YOU_READY_TO_DIE;

	/**
	 * ID: 9951<br>
	 * Message: My time is up...
	 */
	public static final NpcStringId MY_TIME_IS_UP;

	/**
	 * ID: 9952<br>
	 * Message: I can't believe I must kneel to a Human!
	 */
	public static final NpcStringId I_CANT_BELIEVE_I_MUST_KNEEL_TO_A_HUMAN;

	/**
	 * ID: 9957<br>
	 * Message: Minervia! What's the matter?
	 */
	public static final NpcStringId MINERVIA_WHATS_THE_MATTER;

	/**
	 * ID: 9958<br>
	 * Message: The princess is in danger. Why are you staring?
	 */
	public static final NpcStringId THE_PRINCESS_IS_IN_DANGER_WHY_ARE_YOU_STARING;

	/**
	 * ID: 9959<br>
	 * Message: Master $s1! Come on, Hurry up!
	 */
	public static final NpcStringId MASTER_S1_COME_ON_HURRY_UP;

	/**
	 * ID: 9960<br>
	 * Message: We can't fail! Master $s1, Pull yourself together!
	 */
	public static final NpcStringId WE_CANT_FAIL_MASTER_S1_PULL_YOURSELF_TOGETHER;

	/**
	 * ID: 9961<br>
	 * Message: What am I doing... I gotta' go! Goodbye.
	 */
	public static final NpcStringId WHAT_AM_I_DOING_I_GOTTA_GO_GOODBYE;

	/**
	 * ID: 9962<br>
	 * Message: Dammit! I missed...!
	 */
	public static final NpcStringId DAMMIT_I_MISSED;

	/**
	 * ID: 9963<br>
	 * Message: Sorry, but I must say goodbye again... Good luck to you!
	 */
	public static final NpcStringId SORRY_BUT_I_MUST_SAY_GOODBYE_AGAIN_GOOD_LUCK_TO_YOU;

	/**
	 * ID: 9964<br>
	 * Message: I can't yield the secret of the tablet!
	 */
	public static final NpcStringId I_CANT_YIELD_THE_SECRET_OF_THE_TABLET;

	/**
	 * ID: 9965<br>
	 * Message: I'll stop here for now...
	 */
	public static final NpcStringId ILL_STOP_HERE_FOR_NOW;

	/**
	 * ID: 9966<br>
	 * Message: $s1, you dared to leave scar on my face! I'll kill you!!!
	 */
	public static final NpcStringId S1_YOU_DARED_TO_LEAVE_SCAR_ON_MY_FACE_ILL_KILL_YOU;

	/**
	 * ID: 9967<br>
	 * Message: $s1, I won't forget your name...Ha!
	 */
	public static final NpcStringId S1_I_WONT_FORGET_YOUR_NAMEHA;

	/**
	 * ID: 10050<br>
	 * Message: $s1? You have an affinity for bad ideas. Are you ready to die?
	 */
	public static final NpcStringId S1_YOU_HAVE_AN_AFFINITY_FOR_BAD_IDEAS_ARE_YOU_READY_TO_DIE;

	/**
	 * ID: 10052<br>
	 * Message: I can't believe I must kneel before a Human!
	 */
	public static final NpcStringId I_CANT_BELIEVE_I_MUST_KNEEL_BEFORE_A_HUMAN;

	/**
	 * ID: 10057<br>
	 * Message: You thief! Give me the Resonance Amulet!
	 */
	public static final NpcStringId YOU_THIEF_GIVE_ME_THE_RESONANCE_AMULET;

	/**
	 * ID: 10058<br>
	 * Message: Ugh! $s1, Help me!
	 */
	public static final NpcStringId UGH_S1_HELP_ME;

	/**
	 * ID: 10059<br>
	 * Message: $s1. Please, help me! Together we can beat him.
	 */
	public static final NpcStringId S1_PLEASE_HELP_ME_TOGETHER_WE_CAN_BEAT_HIM;

	/**
	 * ID: 10060<br>
	 * Message: $s1! Are you going to let a guild member die?
	 */
	public static final NpcStringId S1_ARE_YOU_GOING_TO_LET_A_GUILD_MEMBER_DIE;

	/**
	 * ID: 10061<br>
	 * Message: I'm sorry, but I gotta' go first!
	 */
	public static final NpcStringId IM_SORRY_BUT_I_GOTTA_GO_FIRST;

	/**
	 * ID: 10062<br>
	 * Message: Aaaah! I couldn't get the Resonance Amulet.
	 */
	public static final NpcStringId AAAAH_I_COULDNT_GET_THE_RESONANCE_AMULET;

	/**
	 * ID: 10063<br>
	 * Message: Take care! I gotta' go now~!
	 */
	public static final NpcStringId TAKE_CARE_I_GOTTA_GO_NOW;

	/**
	 * ID: 10064<br>
	 * Message: I'm sorry, but it's my job to kill you now!
	 */
	public static final NpcStringId IM_SORRY_BUT_ITS_MY_JOB_TO_KILL_YOU_NOW;

	/**
	 * ID: 10065<br>
	 * Message: What a waste of time!
	 */
	public static final NpcStringId WHAT_A_WASTE_OF_TIME;

	/**
	 * ID: 10066<br>
	 * Message: $s1! How could you do this? I'll kill you!
	 */
	public static final NpcStringId S1_HOW_COULD_YOU_DO_THIS_ILL_KILL_YOU;

	/**
	 * ID: 10067<br>
	 * Message: $s1! I'll pay you back!
	 */
	public static final NpcStringId S1_ILL_PAY_YOU_BACK;

	/**
	 * ID: 10068<br>
	 * Message: Why don't you just die?!
	 */
	public static final NpcStringId WHY_DONT_YOU_JUST_DIE;

	/**
	 * ID: 10069<br>
	 * Message: Taste the sting of Level 5 Spoil!
	 */
	public static final NpcStringId TASTE_THE_STING_OF_LEVEL_5_SPOIL;

	/**
	 * ID: 10070<br>
	 * Message: The item is already inside you...
	 */
	public static final NpcStringId THE_ITEM_IS_ALREADY_INSIDE_YOU;

	/**
	 * ID: 10071<br>
	 * Message: This potion you're making me drink is worth its weight in gold!
	 */
	public static final NpcStringId THIS_POTION_YOURE_MAKING_ME_DRINK_IS_WORTH_ITS_WEIGHT_IN_GOLD;

	/**
	 * ID: 10072<br>
	 * Message: This potion is prepared from the ground gall of a bear. Be careful - it packs quite a punch!
	 */
	public static final NpcStringId
			THIS_POTION_IS_PREPARED_FROM_THE_GROUND_GALL_OF_A_BEAR_BE_CAREFUL_IT_PACKS_QUITE_A_PUNCH;

	/**
	 * ID: 10073<br>
	 * Message: How can you use a potion on a newbie...
	 */
	public static final NpcStringId HOW_CAN_YOU_USE_A_POTION_ON_A_NEWBIE;

	/**
	 * ID: 10074<br>
	 * Message: Listen to me, $s1! Unless you have prior authorization, you can't carry a weapon here!
	 */
	public static final NpcStringId LISTEN_TO_ME_S1_UNLESS_YOU_HAVE_PRIOR_AUTHORIZATION_YOU_CANT_CARRY_A_WEAPON_HERE;

	/**
	 * ID: 10075<br>
	 * Message: Dear $s1, may the blessings of Einhasad be with you always.
	 */
	public static final NpcStringId DEAR_S1_MAY_THE_BLESSINGS_OF_EINHASAD_BE_WITH_YOU_ALWAYS;

	/**
	 * ID: 10076<br>
	 * Message: Dear brother $s1, follow the path of light with me...
	 */
	public static final NpcStringId DEAR_BROTHER_S1_FOLLOW_THE_PATH_OF_LIGHT_WITH_ME;

	/**
	 * ID: 10077<br>
	 * Message: $s1, why would you choose the path of darkness?!
	 */
	public static final NpcStringId S1_WHY_WOULD_YOU_CHOOSE_THE_PATH_OF_DARKNESS;

	/**
	 * ID: 10078<br>
	 * Message: $s1!  How dare you defy the will of Einhasad!
	 */
	public static final NpcStringId S1_HOW_DARE_YOU_DEFY_THE_WILL_OF_EINHASAD;

	/**
	 * ID: 10079<br>
	 * Message: The door to the 3rd floor of the altar is now open.
	 */
	public static final NpcStringId THE_DOOR_TO_THE_3RD_FLOOR_OF_THE_ALTAR_IS_NOW_OPEN;

	/**
	 * ID: 11101<br>
	 * Message: Elrokian Hunters
	 */
	public static final NpcStringId ELROKIAN_HUNTERS;

	/**
	 * ID: 11450<br>
	 * Message: You, $s1, you attacked Wendy. Prepare to die!
	 */
	public static final NpcStringId YOU_S1_YOU_ATTACKED_WENDY_PREPARE_TO_DIE;

	/**
	 * ID: 11451<br>
	 * Message: $s1, your enemy was driven out. I will now withdraw and await your next command.
	 */
	public static final NpcStringId S1_YOUR_ENEMY_WAS_DRIVEN_OUT_I_WILL_NOW_WITHDRAW_AND_AWAIT_YOUR_NEXT_COMMAND;

	/**
	 * ID: 11452<br>
	 * Message: This enemy is far too powerful for me to fight. I must withdraw.
	 */
	public static final NpcStringId THIS_ENEMY_IS_FAR_TOO_POWERFUL_FOR_ME_TO_FIGHT_I_MUST_WITHDRAW;

	/**
	 * ID: 11453<br>
	 * Message: The radio signal detector is responding. # A suspicious pile of stones catches your eye.
	 */
	public static final NpcStringId
			THE_RADIO_SIGNAL_DETECTOR_IS_RESPONDING_A_SUSPICIOUS_PILE_OF_STONES_CATCHES_YOUR_EYE;

	/**
	 * ID: 11550<br>
	 * Message: This looks like the right place...
	 */
	public static final NpcStringId THIS_LOOKS_LIKE_THE_RIGHT_PLACE;

	/**
	 * ID: 11551<br>
	 * Message: I see someone. Is this fate?
	 */
	public static final NpcStringId I_SEE_SOMEONE_IS_THIS_FATE;

	/**
	 * ID: 11552<br>
	 * Message: We meet again.
	 */
	public static final NpcStringId WE_MEET_AGAIN;

	/**
	 * ID: 11553<br>
	 * Message: Don't bother trying to find out more about me. Follow your own destiny.
	 */
	public static final NpcStringId DONT_BOTHER_TRYING_TO_FIND_OUT_MORE_ABOUT_ME_FOLLOW_YOUR_OWN_DESTINY;

	/**
	 * ID: 14204<br>
	 * Message: Fallen Angel - Select
	 */
	public static final NpcStringId FALLEN_ANGEL_SELECT;

	/**
	 * ID: 15804<br>
	 * Message: ... How dare you challenge me!
	 */
	public static final NpcStringId _HOW_DARE_YOU_CHALLENGE_ME;

	/**
	 * ID: 15805<br>
	 * Message: The power of Lord Beleth rules the whole world...!
	 */
	public static final NpcStringId THE_POWER_OF_LORD_BELETH_RULES_THE_WHOLE_WORLD;

	/**
	 * ID: 16404<br>
	 * Message: I will taste your blood!
	 */
	public static final NpcStringId I_WILL_TASTE_YOUR_BLOOD;

	/**
	 * ID: 16405<br>
	 * Message: I have fulfilled my contract with Trader Creamees.
	 */
	public static final NpcStringId I_HAVE_FULFILLED_MY_CONTRACT_WITH_TRADER_CREAMEES;

	/**
	 * ID: 17004<br>
	 * Message: I'll cast you into an eternal nightmare!
	 */
	public static final NpcStringId ILL_CAST_YOU_INTO_AN_ETERNAL_NIGHTMARE;

	/**
	 * ID: 17005<br>
	 * Message: Send my soul to Lich King Icarus...
	 */
	public static final NpcStringId SEND_MY_SOUL_TO_LICH_KING_ICARUS;

	/**
	 * ID: 17151<br>
	 * Message: You should consider going back...
	 */
	public static final NpcStringId YOU_SHOULD_CONSIDER_GOING_BACK;

	/**
	 * ID: 17951<br>
	 * Message: The Veiled Creator...
	 */
	public static final NpcStringId THE_VEILED_CREATOR;

	/**
	 * ID: 17952<br>
	 * Message: The Conspiracy of the Ancient Race
	 */
	public static final NpcStringId THE_CONSPIRACY_OF_THE_ANCIENT_RACE;

	/**
	 * ID: 17953<br>
	 * Message: Chaos and Time...
	 */
	public static final NpcStringId CHAOS_AND_TIME;

	/**
	 * ID: 18451<br>
	 * Message: Intruder Alert! The alarm will self-destruct in 2 minutes.
	 */
	public static final NpcStringId INTRUDER_ALERT_THE_ALARM_WILL_SELF_DESTRUCT_IN_2_MINUTES;

	/**
	 * ID: 18452<br>
	 * Message: The alarm will self-destruct in 60 seconds. Enter passcode to override.
	 */
	public static final NpcStringId THE_ALARM_WILL_SELF_DESTRUCT_IN_60_SECONDS_ENTER_PASSCODE_TO_OVERRIDE;

	/**
	 * ID: 18453<br>
	 * Message: The alarm will self-destruct in 30 seconds. Enter passcode to override.
	 */
	public static final NpcStringId THE_ALARM_WILL_SELF_DESTRUCT_IN_30_SECONDS_ENTER_PASSCODE_TO_OVERRIDE;

	/**
	 * ID: 18454<br>
	 * Message: The alarm will self-destruct in 10 seconds. Enter passcode to override.
	 */
	public static final NpcStringId THE_ALARM_WILL_SELF_DESTRUCT_IN_10_SECONDS_ENTER_PASSCODE_TO_OVERRIDE;

	/**
	 * ID: 18455<br>
	 * Message: Recorder crushed.
	 */
	public static final NpcStringId RECORDER_CRUSHED;

	/**
	 * ID: 18552<br>
	 * Message: The alarm will self-destruct in 60 seconds. Please evacuate immediately!
	 */
	public static final NpcStringId THE_ALARM_WILL_SELF_DESTRUCT_IN_60_SECONDS_PLEASE_EVACUATE_IMMEDIATELY;

	/**
	 * ID: 18553<br>
	 * Message: The alarm will self-destruct in 30 seconds. Please evacuate immediately!
	 */
	public static final NpcStringId THE_ALARM_WILL_SELF_DESTRUCT_IN_30_SECONDS_PLEASE_EVACUATE_IMMEDIATELY;

	/**
	 * ID: 18554<br>
	 * Message: The alarm will self-destruct in 10 seconds. Please evacuate immediately!
	 */
	public static final NpcStringId THE_ALARM_WILL_SELF_DESTRUCT_IN_10_SECONDS_PLEASE_EVACUATE_IMMEDIATELY;

	/**
	 * ID: 19304<br>
	 * Message: $s1! You are not the owner of that item.
	 */
	public static final NpcStringId S1_YOU_ARE_NOT_THE_OWNER_OF_THAT_ITEM;

	/**
	 * ID: 19305<br>
	 * Message: Next time, you will not escape!
	 */
	public static final NpcStringId NEXT_TIME_YOU_WILL_NOT_ESCAPE;

	/**
	 * ID: 19306<br>
	 * Message: $s1! You may have won this time... But next time, I will surely capture you!
	 */
	public static final NpcStringId S1_YOU_MAY_HAVE_WON_THIS_TIME_BUT_NEXT_TIME_I_WILL_SURELY_CAPTURE_YOU;

	/**
	 * ID: 19504<br>
	 * Message: Intruder! Protect the Priests of Dawn!
	 */
	public static final NpcStringId INTRUDER_PROTECT_THE_PRIESTS_OF_DAWN;

	/**
	 * ID: 19505<br>
	 * Message: Who are you?! A new face like you can't approach this place!
	 */
	public static final NpcStringId WHO_ARE_YOU_A_NEW_FACE_LIKE_YOU_CANT_APPROACH_THIS_PLACE;

	/**
	 * ID: 19506<br>
	 * Message: How dare you intrude with that transformation! Get lost!
	 */
	public static final NpcStringId HOW_DARE_YOU_INTRUDE_WITH_THAT_TRANSFORMATION_GET_LOST;

	/**
	 * ID: 19604<br>
	 * Message: Who dares summon the Merchant of Mammon?!
	 */
	public static final NpcStringId WHO_DARES_SUMMON_THE_MERCHANT_OF_MAMMON;

	/**
	 * ID: 19605<br>
	 * Message: The ancient promise to the Emperor has been fulfilled.
	 */
	public static final NpcStringId THE_ANCIENT_PROMISE_TO_THE_EMPEROR_HAS_BEEN_FULFILLED;

	/**
	 * ID: 19606<br>
	 * Message: For the eternity of Einhasad!!!
	 */
	public static final NpcStringId FOR_THE_ETERNITY_OF_EINHASAD;

	/**
	 * ID: 19607<br>
	 * Message: Dear Shillien's offsprings! You are not capable of confronting us!
	 */
	public static final NpcStringId DEAR_SHILLIENS_OFFSPRINGS_YOU_ARE_NOT_CAPABLE_OF_CONFRONTING_US;

	/**
	 * ID: 19608<br>
	 * Message: I'll show you the real power of Einhasad!
	 */
	public static final NpcStringId ILL_SHOW_YOU_THE_REAL_POWER_OF_EINHASAD;

	/**
	 * ID: 19609<br>
	 * Message: Dear Military Force of Light! Go destroy the offsprings of Shillien!!!
	 */
	public static final NpcStringId DEAR_MILITARY_FORCE_OF_LIGHT_GO_DESTROY_THE_OFFSPRINGS_OF_SHILLIEN;

	/**
	 * ID: 19610<br>
	 * Message: Everything is owing to $s1. Thank you.
	 */
	public static final NpcStringId EVERYTHING_IS_OWING_TO_S1_THANK_YOU;

	/**
	 * ID: 19611<br>
	 * Message: My power's weakening.. Hurry and turn on the sealing device!!!
	 */
	public static final NpcStringId MY_POWERS_WEAKENING_HURRY_AND_TURN_ON_THE_SEALING_DEVICE;

	/**
	 * ID: 19612<br>
	 * Message: All 4 sealing devices must be turned on!!!
	 */
	public static final NpcStringId ALL_4_SEALING_DEVICES_MUST_BE_TURNED_ON;

	/**
	 * ID: 19613<br>
	 * Message: Lilith's attack is getting stronger! Go ahead and turn it on!
	 */
	public static final NpcStringId LILITHS_ATTACK_IS_GETTING_STRONGER_GO_AHEAD_AND_TURN_IT_ON;

	/**
	 * ID: 19614<br>
	 * Message: Dear $s1, give me more strength.
	 */
	public static final NpcStringId DEAR_S1_GIVE_ME_MORE_STRENGTH;

	/**
	 * ID: 19615<br>
	 * Message: You, such a fool! The victory over this war belongs to Shilien!!!
	 */
	public static final NpcStringId YOU_SUCH_A_FOOL_THE_VICTORY_OVER_THIS_WAR_BELONGS_TO_SHILIEN;

	/**
	 * ID: 19616<br>
	 * Message: How dare you try to contend against me in strength? Ridiculous.
	 */
	public static final NpcStringId HOW_DARE_YOU_TRY_TO_CONTEND_AGAINST_ME_IN_STRENGTH_RIDICULOUS;

	/**
	 * ID: 19617<br>
	 * Message: Anakim! In the name of Great Shilien, I will cut your throat!
	 */
	public static final NpcStringId ANAKIM_IN_THE_NAME_OF_GREAT_SHILIEN_I_WILL_CUT_YOUR_THROAT;

	/**
	 * ID: 19618<br>
	 * Message: You cannot be the match of Lilith. I'll teach you a lesson!
	 */
	public static final NpcStringId YOU_CANNOT_BE_THE_MATCH_OF_LILITH_ILL_TEACH_YOU_A_LESSON;

	/**
	 * ID: 19619<br>
	 * Message: I must go back to Shilien just like this. Outrageous.
	 */
	public static final NpcStringId I_MUST_GO_BACK_TO_SHILIEN_JUST_LIKE_THIS_OUTRAGEOUS;

	/**
	 * ID: 19804<br>
	 * Message: Death to the enemies of the Lords of Dawn!!!
	 */
	public static final NpcStringId DEATH_TO_THE_ENEMIES_OF_THE_LORDS_OF_DAWN;

	/**
	 * ID: 19805<br>
	 * Message: We will be with you always...
	 */
	public static final NpcStringId WE_WILL_BE_WITH_YOU_ALWAYS;

	/**
	 * ID: 19806<br>
	 * Message: You are not the owner of that item.
	 */
	public static final NpcStringId YOU_ARE_NOT_THE_OWNER_OF_THAT_ITEM;

	/**
	 * ID: 19807<br>
	 * Message: Embryo...
	 */
	public static final NpcStringId EMBRYO;

	/**
	 * ID: 20901<br>
	 * Message: Kamael Tutorial
	 */
	public static final NpcStringId KAMAEL_TUTORIAL;

	/**
	 * ID: 22051<br>
	 * Message: Is it a lackey of Kakai?!
	 */
	public static final NpcStringId IS_IT_A_LACKEY_OF_KAKAI;

	/**
	 * ID: 22052<br>
	 * Message: Too late!
	 */
	public static final NpcStringId TOO_LATE;

	/**
	 * ID: 22055<br>
	 * Message: How regretful! Unjust dishonor!
	 */
	public static final NpcStringId HOW_REGRETFUL_UNJUST_DISHONOR;

	/**
	 * ID: 22056<br>
	 * Message: I'll get revenge someday!!
	 */
	public static final NpcStringId ILL_GET_REVENGE_SOMEDAY;

	/**
	 * ID: 22057<br>
	 * Message: Indignant and unfair death!
	 */
	public static final NpcStringId INDIGNANT_AND_UNFAIR_DEATH;

	/**
	 * ID: 22719<br>
	 * Message: The concealed truth will always be revealed...!
	 */
	public static final NpcStringId THE_CONCEALED_TRUTH_WILL_ALWAYS_BE_REVEALED;

	/**
	 * ID: 22720<br>
	 * Message: Cowardly guy!
	 */
	public static final NpcStringId COWARDLY_GUY;

	/**
	 * ID: 22819<br>
	 * Message: I am a tree of nothing... a tree... that knows where to return...
	 */
	public static final NpcStringId I_AM_A_TREE_OF_NOTHING_A_TREE_THAT_KNOWS_WHERE_TO_RETURN;

	/**
	 * ID: 22820<br>
	 * Message: I am a creature that shows the truth of the place deep in my heart...
	 */
	public static final NpcStringId I_AM_A_CREATURE_THAT_SHOWS_THE_TRUTH_OF_THE_PLACE_DEEP_IN_MY_HEART;

	/**
	 * ID: 22821<br>
	 * Message: I am a mirror of darkness... a virtual image of darkness...
	 */
	public static final NpcStringId I_AM_A_MIRROR_OF_DARKNESS_A_VIRTUAL_IMAGE_OF_DARKNESS;

	/**
	 * ID: 22933<br>
	 * Message: I absolutely cannot give it to you! It is my precious jewel!
	 */
	public static final NpcStringId I_ABSOLUTELY_CANNOT_GIVE_IT_TO_YOU_IT_IS_MY_PRECIOUS_JEWEL;

	/**
	 * ID: 22934<br>
	 * Message: I'll take your lives later!
	 */
	public static final NpcStringId ILL_TAKE_YOUR_LIVES_LATER;

	/**
	 * ID: 22935<br>
	 * Message: That sword is really...!
	 */
	public static final NpcStringId THAT_SWORD_IS_REALLY;

	/**
	 * ID: 22936<br>
	 * Message: No! I haven't completely finished the command for destruction and slaughter yet!!!
	 */
	public static final NpcStringId NO_I_HAVENT_COMPLETELY_FINISHED_THE_COMMAND_FOR_DESTRUCTION_AND_SLAUGHTER_YET;

	/**
	 * ID: 22937<br>
	 * Message: How dare you wake me!  Now you shall die!
	 */
	public static final NpcStringId HOW_DARE_YOU_WAKE_ME_NOW_YOU_SHALL_DIE;

	/**
	 * ID: 23060<br>
	 * Message: START DUEL
	 */
	public static final NpcStringId START_DUEL;

	/**
	 * ID: 23061<br>
	 * Message: RULE VIOLATION
	 */
	public static final NpcStringId RULE_VIOLATION;

	/**
	 * ID: 23062<br>
	 * Message: I LOSE
	 */
	public static final NpcStringId I_LOSE;

	/**
	 * ID: 23063<br>
	 * Message: Whhiisshh!
	 */
	public static final NpcStringId WHHIISSHH;

	/**
	 * ID: 23065<br>
	 * Message: I'm sorry, Lord!
	 */
	public static final NpcStringId IM_SORRY_LORD;

	/**
	 * ID: 23066<br>
	 * Message: Whish! Fight!
	 */
	public static final NpcStringId WHISH_FIGHT;

	/**
	 * ID: 23068<br>
	 * Message: Lost! Sorry, Lord!
	 */
	public static final NpcStringId LOST_SORRY_LORD;

	/**
	 * ID: 23072<br>
	 * Message: So shall we start?!
	 */
	public static final NpcStringId SO_SHALL_WE_START;

	/**
	 * ID: 23074<br>
	 * Message: Ugh! I lost...!
	 */
	public static final NpcStringId UGH_I_LOST;

	/**
	 * ID: 23075<br>
	 * Message: I'll walk all over you!
	 */
	public static final NpcStringId ILL_WALK_ALL_OVER_YOU;

	/**
	 * ID: 23077<br>
	 * Message: Ugh! Can this be happening?!
	 */
	public static final NpcStringId UGH_CAN_THIS_BE_HAPPENING;

	/**
	 * ID: 23078<br>
	 * Message: It's the natural result!
	 */
	public static final NpcStringId ITS_THE_NATURAL_RESULT;

	/**
	 * ID: 23079<br>
	 * Message: Ho, ho! I win!
	 */
	public static final NpcStringId HO_HO_I_WIN;

	/**
	 * ID: 23080<br>
	 * Message: I WIN
	 */
	public static final NpcStringId I_WIN;

	/**
	 * ID: 23081<br>
	 * Message: Whish! I won!
	 */
	public static final NpcStringId WHISH_I_WON;

	/**
	 * ID: 23434<br>
	 * Message: Who dares to try and steal my noble blood?
	 */
	public static final NpcStringId WHO_DARES_TO_TRY_AND_STEAL_MY_NOBLE_BLOOD;

	/**
	 * ID: 23651<br>
	 * Message: $s1! Finally, we meet!
	 */
	public static final NpcStringId S1_FINALLY_WE_MEET;

	/**
	 * ID: 23652<br>
	 * Message: Hmm, where did my friend go?
	 */
	public static final NpcStringId HMM_WHERE_DID_MY_FRIEND_GO;

	/**
	 * ID: 23653<br>
	 * Message: Best of luck with your future endeavours.
	 */
	public static final NpcStringId BEST_OF_LUCK_WITH_YOUR_FUTURE_ENDEAVOURS;

	/**
	 * ID: 23661<br>
	 * Message: $s1! Did you wait for long?
	 */
	public static final NpcStringId S1_DID_YOU_WAIT_FOR_LONG;

	/**
	 * ID: 23671<br>
	 * Message: Did you bring what I asked, $s1?
	 */
	public static final NpcStringId DID_YOU_BRING_WHAT_I_ASKED_S1;

	/**
	 * ID: 23681<br>
	 * Message: Hmm? Is someone approaching?
	 */
	public static final NpcStringId HMM_IS_SOMEONE_APPROACHING;

	/**
	 * ID: 23682<br>
	 * Message: Graaah, we're being attacked!
	 */
	public static final NpcStringId GRAAAH_WERE_BEING_ATTACKED;

	/**
	 * ID: 23683<br>
	 * Message: In that case, I wish you good luck.
	 */
	public static final NpcStringId IN_THAT_CASE_I_WISH_YOU_GOOD_LUCK;

	/**
	 * ID: 23685<br>
	 * Message: $s1, has everything been found?
	 */
	public static final NpcStringId S1_HAS_EVERYTHING_BEEN_FOUND;

	/**
	 * ID: 23687<br>
	 * Message: Safe travels!
	 */
	public static final NpcStringId SAFE_TRAVELS;

	/**
	 * ID: 25901<br>
	 * Message: Request from the Farm Owner
	 */
	public static final NpcStringId REQUEST_FROM_THE_FARM_OWNER;

	/**
	 * ID: 31603<br>
	 * Message: Why do you oppress us so?
	 */
	public static final NpcStringId WHY_DO_YOU_OPPRESS_US_SO;

	/**
	 * ID: 33409<br>
	 * Message: Don't interrupt my rest again
	 */
	public static final NpcStringId DONT_INTERRUPT_MY_REST_AGAIN;

	/**
	 * ID: 33410<br>
	 * Message: You're a great devil now...
	 */
	public static final NpcStringId YOURE_A_GREAT_DEVIL_NOW;

	/**
	 * ID: 33411<br>
	 * Message: Oh, it's not an opponent of mine. Ha, ha, ha!
	 */
	public static final NpcStringId OH_ITS_NOT_AN_OPPONENT_OF_MINE_HA_HA_HA;

	/**
	 * ID: 33412<br>
	 * Message: Oh... Great Demon King...
	 */
	public static final NpcStringId OH_GREAT_DEMON_KING;

	/**
	 * ID: 33413<br>
	 * Message: Revenge is Overlord Ramsebalius of the evil world!
	 */
	public static final NpcStringId REVENGE_IS_OVERLORD_RAMSEBALIUS_OF_THE_EVIL_WORLD;

	/**
	 * ID: 33414<br>
	 * Message: Bonaparterius, Abyss King, will punish you
	 */
	public static final NpcStringId BONAPARTERIUS_ABYSS_KING_WILL_PUNISH_YOU;

	/**
	 * ID: 33415<br>
	 * Message: OK, everybody pray fervently!
	 */
	public static final NpcStringId OK_EVERYBODY_PRAY_FERVENTLY;

	/**
	 * ID: 33416<br>
	 * Message: Both hands to heaven! Everybody yell together!
	 */
	public static final NpcStringId BOTH_HANDS_TO_HEAVEN_EVERYBODY_YELL_TOGETHER;

	/**
	 * ID: 33417<br>
	 * Message: One! Two! May your dreams come true!
	 */
	public static final NpcStringId ONE_TWO_MAY_YOUR_DREAMS_COME_TRUE;

	/**
	 * ID: 33418<br>
	 * Message: Who killed my underling devil?
	 */
	public static final NpcStringId WHO_KILLED_MY_UNDERLING_DEVIL;

	/**
	 * ID: 33420<br>
	 * Message: I will make your love come true~ love, love, love~
	 */
	public static final NpcStringId I_WILL_MAKE_YOUR_LOVE_COME_TRUE_LOVE_LOVE_LOVE;

	/**
	 * ID: 33421<br>
	 * Message: I have wisdom in me. I am the box of wisdom!
	 */
	public static final NpcStringId I_HAVE_WISDOM_IN_ME_I_AM_THE_BOX_OF_WISDOM;

	/**
	 * ID: 33422<br>
	 * Message: Oh, oh, oh!
	 */
	public static final NpcStringId OH_OH_OH;

	/**
	 * ID: 33423<br>
	 * Message: Do you want us to love you? Oh.
	 */
	public static final NpcStringId DO_YOU_WANT_US_TO_LOVE_YOU_OH;

	/**
	 * ID: 33424<br>
	 * Message: Who is calling the Lord of Darkness!
	 */
	public static final NpcStringId WHO_IS_CALLING_THE_LORD_OF_DARKNESS;

	/**
	 * ID: 33425<br>
	 * Message: I am a great empire, Bonaparterius!
	 */
	public static final NpcStringId I_AM_A_GREAT_EMPIRE_BONAPARTERIUS;

	/**
	 * ID: 33426<br>
	 * Message: Let your head down before the Lord!
	 */
	public static final NpcStringId LET_YOUR_HEAD_DOWN_BEFORE_THE_LORD;

	/**
	 * ID: 33501<br>
	 * Message: The Song of the Hunter
	 */
	public static final NpcStringId THE_SONG_OF_THE_HUNTER;

	/**
	 * ID: 33511<br>
	 * Message: We'll take the property of the ancient empire!
	 */
	public static final NpcStringId WELL_TAKE_THE_PROPERTY_OF_THE_ANCIENT_EMPIRE;

	/**
	 * ID: 33512<br>
	 * Message: Show me the pretty sparkling things! They're all mine!
	 */
	public static final NpcStringId SHOW_ME_THE_PRETTY_SPARKLING_THINGS_THEYRE_ALL_MINE;

	/**
	 * ID: 33513<br>
	 * Message: Pretty good!
	 */
	public static final NpcStringId PRETTY_GOOD;

	/**
	 * ID: 34830<br>
	 * Message: Ha, that was fun! If you wish to find the key, search the corpse.
	 */
	public static final NpcStringId HA_THAT_WAS_FUN_IF_YOU_WISH_TO_FIND_THE_KEY_SEARCH_THE_CORPSE;

	/**
	 * ID: 34831<br>
	 * Message: I have the key. Why don't you come and take it?
	 */
	public static final NpcStringId I_HAVE_THE_KEY_WHY_DONT_YOU_COME_AND_TAKE_IT;

	/**
	 * ID: 34832<br>
	 * Message: You fools will get what's coming to you!
	 */
	public static final NpcStringId YOU_FOOLS_WILL_GET_WHATS_COMING_TO_YOU;

	/**
	 * ID: 34833<br>
	 * Message: Sorry about this, but I must kill you now.
	 */
	public static final NpcStringId SORRY_ABOUT_THIS_BUT_I_MUST_KILL_YOU_NOW;

	/**
	 * ID: 34835<br>
	 * Message: You guys wouldn't know... the seven seals are...  Arrrgh!
	 */
	public static final NpcStringId YOU_GUYS_WOULDNT_KNOW_THE_SEVEN_SEALS_ARE_ARRRGH;

	/**
	 * ID: 34836<br>
	 * Message: I shall drench this mountain with your blood!
	 */
	public static final NpcStringId I_SHALL_DRENCH_THIS_MOUNTAIN_WITH_YOUR_BLOOD;

	/**
	 * ID: 34837<br>
	 * Message: That doesn't belong to you. Don't touch it!
	 */
	public static final NpcStringId THAT_DOESNT_BELONG_TO_YOU_DONT_TOUCH_IT;

	/**
	 * ID: 34838<br>
	 * Message: Get out of my sight, you infidels!
	 */
	public static final NpcStringId GET_OUT_OF_MY_SIGHT_YOU_INFIDELS;

	/**
	 * ID: 34839<br>
	 * Message: We don't have any further business to discuss... Have you searched the corpse for the key?
	 */
	public static final NpcStringId
			WE_DONT_HAVE_ANY_FURTHER_BUSINESS_TO_DISCUSS_HAVE_YOU_SEARCHED_THE_CORPSE_FOR_THE_KEY;

	/**
	 * ID: 35051<br>
	 * Message: $s1 has earned a stage $s2 Blue Soul Crystal.
	 */
	public static final NpcStringId S1_HAS_EARNED_A_STAGE_S2_BLUE_SOUL_CRYSTAL;

	/**
	 * ID: 35052<br>
	 * Message: $s1 has earned a stage $s2 Red Soul Crystal.
	 */
	public static final NpcStringId S1_HAS_EARNED_A_STAGE_S2_RED_SOUL_CRYSTAL;

	/**
	 * ID: 35053<br>
	 * Message: $s1 has earned a stage $s2 Green Soul Crystal.
	 */
	public static final NpcStringId S1_HAS_EARNED_A_STAGE_S2_GREEN_SOUL_CRYSTAL;

	/**
	 * ID: 35054<br>
	 * Message: $s1 has earned a Stage $s2 Blue Cursed Soul Crystal.
	 */
	public static final NpcStringId S1_HAS_EARNED_A_STAGE_S2_BLUE_CURSED_SOUL_CRYSTAL;

	/**
	 * ID: 35055<br>
	 * Message: $s1 has earned a Stage $s2 Red Cursed Soul Crystal.
	 */
	public static final NpcStringId S1_HAS_EARNED_A_STAGE_S2_RED_CURSED_SOUL_CRYSTAL;

	/**
	 * ID: 35056<br>
	 * Message: $s1 has earned a Stage $s2 Green Cursed Soul Crystal.
	 */
	public static final NpcStringId S1_HAS_EARNED_A_STAGE_S2_GREEN_CURSED_SOUL_CRYSTAL;

	/**
	 * ID: 37101<br>
	 * Message: Shrieks of Ghosts
	 */
	public static final NpcStringId SHRIEKS_OF_GHOSTS;

	/**
	 * ID: 38451<br>
	 * Message: Slot $s1: $s2
	 */
	public static final NpcStringId SLOT_S1_S3;

	/**
	 * ID: 40306<br>
	 * Message: You childish fool, do you think you can catch me?
	 */
	public static final NpcStringId YOU_CHILDISH_FOOL_DO_YOU_THINK_YOU_CAN_CATCH_ME;

	/**
	 * ID: 40307<br>
	 * Message: I must do something about this shameful incident...
	 */
	public static final NpcStringId I_MUST_DO_SOMETHING_ABOUT_THIS_SHAMEFUL_INCIDENT;

	/**
	 * ID: 40308<br>
	 * Message: What, do you dare to challenge me!
	 */
	public static final NpcStringId WHAT_DO_YOU_DARE_TO_CHALLENGE_ME;

	/**
	 * ID: 40309<br>
	 * Message: The red-eyed thieves will revenge!
	 */
	public static final NpcStringId THE_RED_EYED_THIEVES_WILL_REVENGE;

	/**
	 * ID: 40310<br>
	 * Message: Go ahead, you child!
	 */
	public static final NpcStringId GO_AHEAD_YOU_CHILD;

	/**
	 * ID: 40311<br>
	 * Message: My friends are sure to revenge!
	 */
	public static final NpcStringId MY_FRIENDS_ARE_SURE_TO_REVENGE;

	/**
	 * ID: 40312<br>
	 * Message: You ruthless fool, I will show you what real fighting is all about!
	 */
	public static final NpcStringId YOU_RUTHLESS_FOOL_I_WILL_SHOW_YOU_WHAT_REAL_FIGHTING_IS_ALL_ABOUT;

	/**
	 * ID: 40313<br>
	 * Message: Ahh, how can it end like this... it is not fair!
	 */
	public static final NpcStringId AHH_HOW_CAN_IT_END_LIKE_THIS_IT_IS_NOT_FAIR;

	/**
	 * ID: 40909<br>
	 * Message: The sacred flame is ours!
	 */
	public static final NpcStringId THE_SACRED_FLAME_IS_OURS;

	/**
	 * ID: 40910<br>
	 * Message: Arrghh...we shall never.. surrender...
	 */
	public static final NpcStringId ARRGHHWE_SHALL_NEVER_SURRENDER;

	/**
	 * ID: 40913<br>
	 * Message: As you wish, master!
	 */
	public static final NpcStringId AS_YOU_WISH_MASTER;

	/**
	 * ID: 41651<br>
	 * Message: My dear friend of $s1, who has gone on ahead of me!
	 */
	public static final NpcStringId MY_DEAR_FRIEND_OF_S1_WHO_HAS_GONE_ON_AHEAD_OF_ME;

	/**
	 * ID: 41652<br>
	 * Message: Listen to Tejakar Gandi, young Oroka! The spirit of the slain leopard is calling you, $s1!
	 */
	public static final NpcStringId
			LISTEN_TO_TEJAKAR_GANDI_YOUNG_OROKA_THE_SPIRIT_OF_THE_SLAIN_LEOPARD_IS_CALLING_YOU_S1;

	/**
	 * ID: 42046<br>
	 * Message: Hey! Everybody watch the eggs!
	 */
	public static final NpcStringId HEY_EVERYBODY_WATCH_THE_EGGS;

	/**
	 * ID: 42047<br>
	 * Message: I thought I'd caught one share... Whew!
	 */
	public static final NpcStringId I_THOUGHT_ID_CAUGHT_ONE_SHARE_WHEW;

	/**
	 * ID: 42048<br>
	 * Message: The stone... the Elven stone... broke...
	 */
	public static final NpcStringId THE_STONE_THE_ELVEN_STONE_BROKE;

	/**
	 * ID: 42049<br>
	 * Message: If the eggs get taken, we're dead!
	 */
	public static final NpcStringId IF_THE_EGGS_GET_TAKEN_WERE_DEAD;

	/**
	 * ID: 42111<br>
	 * Message: Give me a Fairy Leaf...!
	 */
	public static final NpcStringId GIVE_ME_A_FAIRY_LEAF;

	/**
	 * ID: 42112<br>
	 * Message: Why do you bother me again?
	 */
	public static final NpcStringId WHY_DO_YOU_BOTHER_ME_AGAIN;

	/**
	 * ID: 42113<br>
	 * Message: Hey, you've already drunk the essence of wind!
	 */
	public static final NpcStringId HEY_YOUVE_ALREADY_DRUNK_THE_ESSENCE_OF_WIND;

	/**
	 * ID: 42114<br>
	 * Message: Leave now, before you incur the wrath of the guardian ghost...
	 */
	public static final NpcStringId LEAVE_NOW_BEFORE_YOU_INCUR_THE_WRATH_OF_THE_GUARDIAN_GHOST;

	/**
	 * ID: 42115<br>
	 * Message: Hey, you've already drunk the essence of a star!
	 */
	public static final NpcStringId HEY_YOUVE_ALREADY_DRUNK_THE_ESSENCE_OF_A_STAR;

	/**
	 * ID: 42116<br>
	 * Message: Hey, you've already drunk the essence of dusk!
	 */
	public static final NpcStringId HEY_YOUVE_ALREADY_DRUNK_THE_ESSENCE_OF_DUSK;

	/**
	 * ID: 42117<br>
	 * Message: Hey, you've already drunk the essence of the abyss!
	 */
	public static final NpcStringId HEY_YOUVE_ALREADY_DRUNK_THE_ESSENCE_OF_THE_ABYSS;

	/**
	 * ID: 42118<br>
	 * Message: We must protect the fairy tree!
	 */
	public static final NpcStringId WE_MUST_PROTECT_THE_FAIRY_TREE;

	/**
	 * ID: 42119<br>
	 * Message: Get out of the sacred tree, you scoundrels!
	 */
	public static final NpcStringId GET_OUT_OF_THE_SACRED_TREE_YOU_SCOUNDRELS;

	/**
	 * ID: 42120<br>
	 * Message: Death to the thieves of the pure water of the world!
	 */
	public static final NpcStringId DEATH_TO_THE_THIEVES_OF_THE_PURE_WATER_OF_THE_WORLD;

	/**
	 * ID: 42231<br>
	 * Message: Hey, it seems like you need my help, doesn't it?
	 */
	public static final NpcStringId HEY_IT_SEEMS_LIKE_YOU_NEED_MY_HELP_DOESNT_IT;

	/**
	 * ID: 42232<br>
	 * Message: Almost got it... Ouch! Stop!  Damn these bloody manacles!
	 */
	public static final NpcStringId ALMOST_GOT_IT_OUCH_STOP_DAMN_THESE_BLOODY_MANACLES;

	/**
	 * ID: 42233<br>
	 * Message: Oh, that smarts!
	 */
	public static final NpcStringId OH_THAT_SMARTS;

	/**
	 * ID: 42234<br>
	 * Message: Hey, master! Pay attention! I'm dying over here!
	 */
	public static final NpcStringId HEY_MASTER_PAY_ATTENTION_IM_DYING_OVER_HERE;

	/**
	 * ID: 42235<br>
	 * Message: What have I done to deserve this?
	 */
	public static final NpcStringId WHAT_HAVE_I_DONE_TO_DESERVE_THIS;

	/**
	 * ID: 42236<br>
	 * Message: Oh, this is just great! What are you going to do now?
	 */
	public static final NpcStringId OH_THIS_IS_JUST_GREAT_WHAT_ARE_YOU_GOING_TO_DO_NOW;

	/**
	 * ID: 42237<br>
	 * Message: You inconsiderate moron! Can't you even take care of little old me?!
	 */
	public static final NpcStringId YOU_INCONSIDERATE_MORON_CANT_YOU_EVEN_TAKE_CARE_OF_LITTLE_OLD_ME;

	/**
	 * ID: 42238<br>
	 * Message: Oh no! The man who eats one's sins has died! Penitence is further away~!
	 */
	public static final NpcStringId OH_NO_THE_MAN_WHO_EATS_ONES_SINS_HAS_DIED_PENITENCE_IS_FURTHER_AWAY;

	/**
	 * ID: 42239<br>
	 * Message: Using a special skill here could trigger a bloodbath!
	 */
	public static final NpcStringId USING_A_SPECIAL_SKILL_HERE_COULD_TRIGGER_A_BLOODBATH;

	/**
	 * ID: 42240<br>
	 * Message: Hey, what do you expect of me?
	 */
	public static final NpcStringId HEY_WHAT_DO_YOU_EXPECT_OF_ME;

	/**
	 * ID: 42241<br>
	 * Message: Ugggggh! Push! It's not coming out!
	 */
	public static final NpcStringId UGGGGGH_PUSH_ITS_NOT_COMING_OUT;

	/**
	 * ID: 42242<br>
	 * Message: Ah, I missed the mark!
	 */
	public static final NpcStringId AH_I_MISSED_THE_MARK;

	/**
	 * ID: 42243<br>
	 * Message: Yawwwwn! It's so boring here. We should go and find some action!
	 */
	public static final NpcStringId YAWWWWN_ITS_SO_BORING_HERE_WE_SHOULD_GO_AND_FIND_SOME_ACTION;

	/**
	 * ID: 42244<br>
	 * Message: Hey, if you continue to waste time you will never finish your penance!
	 */
	public static final NpcStringId HEY_IF_YOU_CONTINUE_TO_WASTE_TIME_YOU_WILL_NEVER_FINISH_YOUR_PENANCE;

	/**
	 * ID: 42245<br>
	 * Message: I know you don't like me. The feeling is mutual!
	 */
	public static final NpcStringId I_KNOW_YOU_DONT_LIKE_ME_THE_FEELING_IS_MUTUAL;

	/**
	 * ID: 42246<br>
	 * Message: I need a drink.
	 */
	public static final NpcStringId I_NEED_A_DRINK;

	/**
	 * ID: 42247<br>
	 * Message: Oh, this is dragging on too long...  At this rate I won't make it home before the seven seals are broken.
	 */
	public static final NpcStringId
			OH_THIS_IS_DRAGGING_ON_TOO_LONG_AT_THIS_RATE_I_WONT_MAKE_IT_HOME_BEFORE_THE_SEVEN_SEALS_ARE_BROKEN;

	/**
	 * ID: 45650<br>
	 * Message: $s1 received a $s2 item as a reward from the separated soul.
	 */
	public static final NpcStringId S1_RECEIVED_A_S2_ITEM_AS_A_REWARD_FROM_THE_SEPARATED_SOUL;

	/**
	 * ID: 45651<br>
	 * Message: Sealed Vorpal Helmet
	 */
	public static final NpcStringId SEALED_VORPAL_HELMET;

	/**
	 * ID: 45652<br>
	 * Message: Sealed Vorpal Leather Helmet
	 */
	public static final NpcStringId SEALED_VORPAL_LEATHER_HELMET;

	/**
	 * ID: 45653<br>
	 * Message: Sealed Vorpal Circlet
	 */
	public static final NpcStringId SEALED_VORPAL_CIRCLET;

	/**
	 * ID: 45654<br>
	 * Message: Sealed Vorpal Breastplate
	 */
	public static final NpcStringId SEALED_VORPAL_BREASTPLATE;

	/**
	 * ID: 45655<br>
	 * Message: Sealed Vorpal Leather Breastplate
	 */
	public static final NpcStringId SEALED_VORPAL_LEATHER_BREASTPLATE;

	/**
	 * ID: 45656<br>
	 * Message: Sealed Vorpal Tunic
	 */
	public static final NpcStringId SEALED_VORPAL_TUNIC;

	/**
	 * ID: 45657<br>
	 * Message: Sealed Vorpal Gaiters
	 */
	public static final NpcStringId SEALED_VORPAL_GAITERS;

	/**
	 * ID: 45658<br>
	 * Message: Sealed Vorpal Leather Legging
	 */
	public static final NpcStringId SEALED_VORPAL_LEATHER_LEGGING;

	/**
	 * ID: 45659<br>
	 * Message: Sealed Vorpal Stocking
	 */
	public static final NpcStringId SEALED_VORPAL_STOCKING;

	/**
	 * ID: 45660<br>
	 * Message: Sealed Vorpal Gauntlet
	 */
	public static final NpcStringId SEALED_VORPAL_GAUNTLET;

	/**
	 * ID: 45661<br>
	 * Message: Sealed Vorpal Leather Gloves
	 */
	public static final NpcStringId SEALED_VORPAL_LEATHER_GLOVES;

	/**
	 * ID: 45662<br>
	 * Message: Sealed Vorpal Gloves
	 */
	public static final NpcStringId SEALED_VORPAL_GLOVES;

	/**
	 * ID: 45663<br>
	 * Message: Sealed Vorpal Boots
	 */
	public static final NpcStringId SEALED_VORPAL_BOOTS;

	/**
	 * ID: 45664<br>
	 * Message: Sealed Vorpal Leather Boots
	 */
	public static final NpcStringId SEALED_VORPAL_LEATHER_BOOTS;

	/**
	 * ID: 45665<br>
	 * Message: Sealed Vorpal Shoes
	 */
	public static final NpcStringId SEALED_VORPAL_SHOES;

	/**
	 * ID: 45666<br>
	 * Message: Sealed Vorpal Shield
	 */
	public static final NpcStringId SEALED_VORPAL_SHIELD;

	/**
	 * ID: 45667<br>
	 * Message: Sealed Vorpal Sigil
	 */
	public static final NpcStringId SEALED_VORPAL_SIGIL;

	/**
	 * ID: 45668<br>
	 * Message: Sealed Vorpal Ring
	 */
	public static final NpcStringId SEALED_VORPAL_RING;

	/**
	 * ID: 45669<br>
	 * Message: Sealed Vorpal Earring
	 */
	public static final NpcStringId SEALED_VORPAL_EARRING;

	/**
	 * ID: 45670<br>
	 * Message: Sealed Vorpal Necklace
	 */
	public static final NpcStringId SEALED_VORPAL_NECKLACE;

	/**
	 * ID: 45671<br>
	 * Message: Periel Sword
	 */
	public static final NpcStringId PERIEL_SWORD;

	/**
	 * ID: 45672<br>
	 * Message: Skull Edge
	 */
	public static final NpcStringId SKULL_EDGE;

	/**
	 * ID: 45673<br>
	 * Message: Vigwik Axe
	 */
	public static final NpcStringId VIGWIK_AXE;

	/**
	 * ID: 45674<br>
	 * Message: Devilish Maul
	 */
	public static final NpcStringId DEVILISH_MAUL;

	/**
	 * ID: 45675<br>
	 * Message: Feather Eye Blade
	 */
	public static final NpcStringId FEATHER_EYE_BLADE;

	/**
	 * ID: 45676<br>
	 * Message: Octo Claw
	 */
	public static final NpcStringId OCTO_CLAW;

	/**
	 * ID: 45677<br>
	 * Message: Doubletop Spear
	 */
	public static final NpcStringId DOUBLETOP_SPEAR;

	/**
	 * ID: 45678<br>
	 * Message: Rising Star
	 */
	public static final NpcStringId RISING_STAR;

	/**
	 * ID: 45679<br>
	 * Message: Black Visage
	 */
	public static final NpcStringId BLACK_VISAGE;

	/**
	 * ID: 45680<br>
	 * Message: Veniplant Sword
	 */
	public static final NpcStringId VENIPLANT_SWORD;

	/**
	 * ID: 45681<br>
	 * Message: Skull Carnium Bow
	 */
	public static final NpcStringId SKULL_CARNIUM_BOW;

	/**
	 * ID: 45682<br>
	 * Message: Gemtail Rapier
	 */
	public static final NpcStringId GEMTAIL_RAPIER;

	/**
	 * ID: 45683<br>
	 * Message: Finale Blade
	 */
	public static final NpcStringId FINALE_BLADE;

	/**
	 * ID: 45684<br>
	 * Message: Dominion Crossbow
	 */
	public static final NpcStringId DOMINION_CROSSBOW;

	/**
	 * ID: 45685<br>
	 * Message: Blessed Weapon Enchant Scroll - S Grade
	 */
	public static final NpcStringId BLESSED_WEAPON_ENCHANT_SCROLL_S_GRADE;

	/**
	 * ID: 45686<br>
	 * Message: Blessed Armor Enchant Scroll - S Grade
	 */
	public static final NpcStringId BLESSED_ARMOR_ENCHANT_SCROLL_S_GRADE;

	/**
	 * ID: 45687<br>
	 * Message: Fire Crystal
	 */
	public static final NpcStringId FIRE_CRYSTAL;

	/**
	 * ID: 45688<br>
	 * Message: Water Crystal
	 */
	public static final NpcStringId WATER_CRYSTAL;

	/**
	 * ID: 45689<br>
	 * Message: Earth Crystal
	 */
	public static final NpcStringId EARTH_CRYSTAL;

	/**
	 * ID: 45690<br>
	 * Message: Wind Crystal
	 */
	public static final NpcStringId WIND_CRYSTAL;

	/**
	 * ID: 45691<br>
	 * Message: Holy Crystal
	 */
	public static final NpcStringId HOLY_CRYSTAL;

	/**
	 * ID: 45692<br>
	 * Message: Dark Crystal
	 */
	public static final NpcStringId DARK_CRYSTAL;

	/**
	 * ID: 45693<br>
	 * Message: Weapon Enchant Scroll - S Grade
	 */
	public static final NpcStringId WEAPON_ENCHANT_SCROLL_S_GRADE;

	/**
	 * ID: 46350<br>
	 * Message: Att... attack... $s1.. Ro... rogue... $s2..
	 */
	public static final NpcStringId ATT_ATTACK_S1_RO_ROGUE_S2;

	/**
	 * ID: 50110<br>
	 * Message: ##########Bingo!##########
	 */
	public static final NpcStringId BINGO;

	/**
	 * ID: 50338<br>
	 * Message: Blood and honor!
	 */
	public static final NpcStringId BLOOD_AND_HONOR;

	/**
	 * ID: 50339<br>
	 * Message: Curse of the gods on the one that defiles the property of the empire!
	 */
	public static final NpcStringId CURSE_OF_THE_GODS_ON_THE_ONE_THAT_DEFILES_THE_PROPERTY_OF_THE_EMPIRE;

	/**
	 * ID: 50340<br>
	 * Message: War and death!
	 */
	public static final NpcStringId WAR_AND_DEATH;

	/**
	 * ID: 50341<br>
	 * Message: Ambition and power!
	 */
	public static final NpcStringId AMBITION_AND_POWER;

	/**
	 * ID: 50503<br>
	 * Message: $s1 has won the main event for players under level $s2, and earned $s3 points!
	 */
	public static final NpcStringId S1_HAS_WON_THE_MAIN_EVENT_FOR_PLAYERS_UNDER_LEVEL_S2_AND_EARNED_S3_POINTS;

	/**
	 * ID: 50504<br>
	 * Message: $s1 has earned $s2 points in the main event for unlimited levels.
	 */
	public static final NpcStringId S1_HAS_EARNED_S2_POINTS_IN_THE_MAIN_EVENT_FOR_UNLIMITED_LEVELS;

	/**
	 * ID: 50701<br>
	 * Message: Into the Chaos
	 */
	public static final NpcStringId INTO_THE_CHAOS;

	/**
	 * ID: 50851<br>
	 * Message: You have successfully completed a clan quest. $s1 points have been added to your clan's reputation score.
	 */
	public static final NpcStringId
			YOU_HAVE_SUCCESSFULLY_COMPLETED_A_CLAN_QUEST_S1_POINTS_HAVE_BEEN_ADDED_TO_YOUR_CLANS_REPUTATION_SCORE;

	/**
	 * ID: 60000<br>
	 * Message: The furnace will go out. Watch and see.
	 */
	public static final NpcStringId THE_FURNACE_WILL_GO_OUT_WATCH_AND_SEE;

	/**
	 * ID: 60001<br>
	 * Message: There's about 1 minute left!
	 */
	public static final NpcStringId THERES_ABOUT_1_MINUTE_LEFT;

	/**
	 * ID: 60002<br>
	 * Message: There's just 10 seconds left!
	 */
	public static final NpcStringId THERES_JUST_10_SECONDS_LEFT;

	/**
	 * ID: 60003<br>
	 * Message: Now, light the furnace's fire.
	 */
	public static final NpcStringId NOW_LIGHT_THE_FURNACES_FIRE;

	/**
	 * ID: 60004<br>
	 * Message: Time is up and you have failed. Any more will be difficult.
	 */
	public static final NpcStringId TIME_IS_UP_AND_YOU_HAVE_FAILED_ANY_MORE_WILL_BE_DIFFICULT;

	/**
	 * ID: 60005<br>
	 * Message: Oh, you've succeeded.
	 */
	public static final NpcStringId OH_YOUVE_SUCCEEDED;

	/**
	 * ID: 60006<br>
	 * Message: Ah, is this failure? But it looks like I can keep going.
	 */
	public static final NpcStringId AH_IS_THIS_FAILURE_BUT_IT_LOOKS_LIKE_I_CAN_KEEP_GOING;

	/**
	 * ID: 60007<br>
	 * Message: Ah, I've failed. Going further will be difficult.
	 */
	public static final NpcStringId AH_IVE_FAILED_GOING_FURTHER_WILL_BE_DIFFICULT;

	/**
	 * ID: 60008<br>
	 * Message: Furnace of Balance
	 */
	public static final NpcStringId FURNACE_OF_BALANCE;

	/**
	 * ID: 60009<br>
	 * Message: Furnace of Protection
	 */
	public static final NpcStringId FURNACE_OF_PROTECTION;

	/**
	 * ID: 60010<br>
	 * Message: Furnace of Will
	 */
	public static final NpcStringId FURNACE_OF_WILL;

	/**
	 * ID: 60011<br>
	 * Message: Furnace of Magic
	 */
	public static final NpcStringId FURNACE_OF_MAGIC;

	/**
	 * ID: 60012<br>
	 * Message: Divine energy is beginning to encircle.
	 */
	public static final NpcStringId DIVINE_ENERGY_IS_BEGINNING_TO_ENCIRCLE;

	/**
	 * ID: 60013<br>
	 * Message: For the glory of Solina!
	 */
	public static final NpcStringId FOR_THE_GLORY_OF_SOLINA;

	/**
	 * ID: 60014<br>
	 * Message: Punish all those who tread footsteps in this place.
	 */
	public static final NpcStringId PUNISH_ALL_THOSE_WHO_TREAD_FOOTSTEPS_IN_THIS_PLACE;

	/**
	 * ID: 60015<br>
	 * Message: We are the sword of truth, the sword of Solina.
	 */
	public static final NpcStringId WE_ARE_THE_SWORD_OF_TRUTH_THE_SWORD_OF_SOLINA;

	/**
	 * ID: 60016<br>
	 * Message: We raise our blades for the glory of Solina.
	 */
	public static final NpcStringId WE_RAISE_OUR_BLADES_FOR_THE_GLORY_OF_SOLINA;

	/**
	 * ID: 60018<br>
	 * Message: Hey, don't go so fast.
	 */
	public static final NpcStringId HEY_DONT_GO_SO_FAST;

	/**
	 * ID: 60019<br>
	 * Message: It's hard to follow.
	 */
	public static final NpcStringId ITS_HARD_TO_FOLLOW;

	/**
	 * ID: 60020<br>
	 * Message: Huff huff. You're too fast. I can't follow anymore.
	 */
	public static final NpcStringId HUFF_HUFF_YOURE_TOO_FAST_I_CANT_FOLLOW_ANYMORE;

	/**
	 * ID: 60021<br>
	 * Message: Ah... I think I remember this place.
	 */
	public static final NpcStringId AH_I_THINK_I_REMEMBER_THIS_PLACE;

	/**
	 * ID: 60022<br>
	 * Message: Ah! Fresh air!
	 */
	public static final NpcStringId AH_FRESH_AIR;

	/**
	 * ID: 60023<br>
	 * Message: What were you doing here?
	 */
	public static final NpcStringId WHAT_WERE_YOU_DOING_HERE;

	/**
	 * ID: 60024<br>
	 * Message: I guess you're the silent type. Then are you looking for treasure like me?
	 */
	public static final NpcStringId I_GUESS_YOURE_THE_SILENT_TYPE_THEN_ARE_YOU_LOOKING_FOR_TREASURE_LIKE_ME;

	/**
	 * ID: 60403<br>
	 * Message: Who is calling me?
	 */
	public static final NpcStringId WHO_IS_CALLING_ME;

	/**
	 * ID: 60404<br>
	 * Message: Can light exist without darkness?
	 */
	public static final NpcStringId CAN_LIGHT_EXIST_WITHOUT_DARKNESS;

	/**
	 * ID: 60903<br>
	 * Message: You can't avoid the eyes of Udan!
	 */
	public static final NpcStringId YOU_CANT_AVOID_THE_EYES_OF_UDAN;

	/**
	 * ID: 60904<br>
	 * Message: Udan has already seen your face!
	 */
	public static final NpcStringId UDAN_HAS_ALREADY_SEEN_YOUR_FACE;

	/**
	 * ID: 61050<br>
	 * Message: The magical power of water comes from the power of storm and hail!  If you dare to confront it, only death will await you!
	 */
	public static final NpcStringId
			THE_MAGICAL_POWER_OF_WATER_COMES_FROM_THE_POWER_OF_STORM_AND_HAIL_IF_YOU_DARE_TO_CONFRONT_IT_ONLY_DEATH_WILL_AWAIT_YOU;

	/**
	 * ID: 61051<br>
	 * Message: The power of constraint is getting weaker.  Your ritual has failed!
	 */
	public static final NpcStringId THE_POWER_OF_CONSTRAINT_IS_GETTING_WEAKER_YOUR_RITUAL_HAS_FAILED;

	/**
	 * ID: 61503<br>
	 * Message: You can't avoid the eyes of Asefa!
	 */
	public static final NpcStringId YOU_CANT_AVOID_THE_EYES_OF_ASEFA;

	/**
	 * ID: 61504<br>
	 * Message: Asefa has already seen your face!
	 */
	public static final NpcStringId ASEFA_HAS_ALREADY_SEEN_YOUR_FACE;

	/**
	 * ID: 61650<br>
	 * Message: The magical power of fire is also the power of flames and lava!  If you dare to confront it, only death will await you!
	 */
	public static final NpcStringId
			THE_MAGICAL_POWER_OF_FIRE_IS_ALSO_THE_POWER_OF_FLAMES_AND_LAVA_IF_YOU_DARE_TO_CONFRONT_IT_ONLY_DEATH_WILL_AWAIT_YOU;

	/**
	 * ID: 62503<br>
	 * Message: I smell something delicious...
	 */
	public static final NpcStringId I_SMELL_SOMETHING_DELICIOUS;

	/**
	 * ID: 62504<br>
	 * Message: Oooh!
	 */
	public static final NpcStringId OOOH;

	/**
	 * ID: 66001<br>
	 * Message: Aiding the Floran Village.
	 */
	public static final NpcStringId AIDING_THE_FLORAN_VILLAGE;

	/**
	 * ID: 66300<br>
	 * Message: No such card
	 */
	public static final NpcStringId NO_SUCH_CARD;

	/**
	 * ID: 68801<br>
	 * Message: Defeat the Elrokian Raiders!
	 */
	public static final NpcStringId DEFEAT_THE_ELROKIAN_RAIDERS;

	/**
	 * ID: 70851<br>
	 * Message: Have you completed your preparations to become a lord?
	 */
	public static final NpcStringId HAVE_YOU_COMPLETED_YOUR_PREPARATIONS_TO_BECOME_A_LORD;

	/**
	 * ID: 70852<br>
	 * Message: $s1. Now, depart!
	 */
	public static final NpcStringId S1_NOW_DEPART;

	/**
	 * ID: 70853<br>
	 * Message: Go find Saius!
	 */
	public static final NpcStringId GO_FIND_SAIUS;

	/**
	 * ID: 70854<br>
	 * Message: Listen, you villagers. Our liege, who will soon become a lord, has defeated the Headless Knight. You can now rest easy!
	 */
	public static final NpcStringId
			LISTEN_YOU_VILLAGERS_OUR_LIEGE_WHO_WILL_SOON_BECOME_A_LORD_HAS_DEFEATED_THE_HEADLESS_KNIGHT_YOU_CAN_NOW_REST_EASY;

	/**
	 * ID: 70855<br>
	 * Message: $s1! Do you dare defy my subordinates?
	 */
	public static final NpcStringId S1_DO_YOU_DARE_DEFY_MY_SUBORDINATES;

	/**
	 * ID: 70856<br>
	 * Message: Does my mission to block the supplies end here?
	 */
	public static final NpcStringId DOES_MY_MISSION_TO_BLOCK_THE_SUPPLIES_END_HERE;

	/**
	 * ID: 70859<br>
	 * Message: $s1 has become lord of the Town of Gludio. Long may he reign!
	 */
	public static final NpcStringId S1_HAS_BECOME_LORD_OF_THE_TOWN_OF_GLUDIO_LONG_MAY_HE_REIGN;

	/**
	 * ID: 70957<br>
	 * Message: You'll see! I won't forgive you next time!
	 */
	public static final NpcStringId YOULL_SEE_I_WONT_FORGIVE_YOU_NEXT_TIME;

	/**
	 * ID: 70959<br>
	 * Message: $s1 has become lord of the Town of Dion. Long may he reign!
	 */
	public static final NpcStringId S1_HAS_BECOME_LORD_OF_THE_TOWN_OF_DION_LONG_MAY_HE_REIGN;

	/**
	 * ID: 71052<br>
	 * Message: It's the enemy! No mercy!
	 */
	public static final NpcStringId ITS_THE_ENEMY_NO_MERCY;

	/**
	 * ID: 71053<br>
	 * Message: What are you doing? We are still superior!
	 */
	public static final NpcStringId WHAT_ARE_YOU_DOING_WE_ARE_STILL_SUPERIOR;

	/**
	 * ID: 71054<br>
	 * Message: How infuriating! This enemy...
	 */
	public static final NpcStringId HOW_INFURIATING_THIS_ENEMY;

	/**
	 * ID: 71059<br>
	 * Message: $s1 has become the lord of the Town of Giran. May there be glory in the territory of Giran!
	 */
	public static final NpcStringId
			S1_HAS_BECOME_THE_LORD_OF_THE_TOWN_OF_GIRAN_MAY_THERE_BE_GLORY_IN_THE_TERRITORY_OF_GIRAN;

	/**
	 * ID: 71151<br>
	 * Message: My liege! Where are you?
	 */
	public static final NpcStringId MY_LIEGE_WHERE_ARE_YOU;

	/**
	 * ID: 71159<br>
	 * Message: $s1 has become the lord of the Town of Innadril. May there be glory in the territory of Innadril!
	 */
	public static final NpcStringId
			S1_HAS_BECOME_THE_LORD_OF_THE_TOWN_OF_INNADRIL_MAY_THERE_BE_GLORY_IN_THE_TERRITORY_OF_INNADRIL;

	/**
	 * ID: 71252<br>
	 * Message: You have found all the Nebulite Orbs!
	 */
	public static final NpcStringId YOU_HAVE_FOUND_ALL_THE_NEBULITE_ORBS;

	/**
	 * ID: 71259<br>
	 * Message: $s1 has become the lord of the Town of Oren. May there be glory in the territory of Oren!
	 */
	public static final NpcStringId
			S1_HAS_BECOME_THE_LORD_OF_THE_TOWN_OF_OREN_MAY_THERE_BE_GLORY_IN_THE_TERRITORY_OF_OREN;

	/**
	 * ID: 71351<br>
	 * Message: $s1 has become the lord of the Town of Aden. May there be glory in the territory of Aden!
	 */
	public static final NpcStringId
			S1_HAS_BECOME_THE_LORD_OF_THE_TOWN_OF_ADEN_MAY_THERE_BE_GLORY_IN_THE_TERRITORY_OF_ADEN;

	/**
	 * ID: 71459<br>
	 * Message: $s1 has become the lord of the Town of Schuttgart. May there be glory in the territory of Schuttgart!
	 */
	public static final NpcStringId
			S1_HAS_BECOME_THE_LORD_OF_THE_TOWN_OF_SCHUTTGART_MAY_THERE_BE_GLORY_IN_THE_TERRITORY_OF_SCHUTTGART;

	/**
	 * ID: 71551<br>
	 * Message: $s1, I will remember you.
	 */
	public static final NpcStringId S1_I_WILL_REMEMBER_YOU;

	/**
	 * ID: 71653<br>
	 * Message: Frederick is looking for you, my liege.
	 */
	public static final NpcStringId FREDERICK_IS_LOOKING_FOR_YOU_MY_LIEGE;

	/**
	 * ID: 71654<br>
	 * Message: Ho ho! Did you think you could really stop trading with us?
	 */
	public static final NpcStringId HO_HO_DID_YOU_THINK_YOU_COULD_REALLY_STOP_TRADING_WITH_US;

	/**
	 * ID: 71655<br>
	 * Message: You have charged into the temple.
	 */
	public static final NpcStringId YOU_HAVE_CHARGED_INTO_THE_TEMPLE;

	/**
	 * ID: 71656<br>
	 * Message: You are in the midst of dealing with the heretic of Heretic Temple.
	 */
	public static final NpcStringId YOU_ARE_IN_THE_MIDST_OF_DEALING_WITH_THE_HERETIC_OF_HERETIC_TEMPLE;

	/**
	 * ID: 71657<br>
	 * Message: The Heretic Temple is descending into chaos.
	 */
	public static final NpcStringId THE_HERETIC_TEMPLE_IS_DESCENDING_INTO_CHAOS;

	/**
	 * ID: 71659<br>
	 * Message: $s1 has become the lord of the Town of Rune. May there be glory in the territory of Rune!
	 */
	public static final NpcStringId
			S1_HAS_BECOME_THE_LORD_OF_THE_TOWN_OF_RUNE_MAY_THERE_BE_GLORY_IN_THE_TERRITORY_OF_RUNE;

	/**
	 * ID: 71751<br>
	 * Message: $s1! Raise your weapons for the sake of the territory!
	 */
	public static final NpcStringId S1_RAISE_YOUR_WEAPONS_FOR_THE_SAKE_OF_THE_TERRITORY;

	/**
	 * ID: 71752<br>
	 * Message: $s1! The war is over. Lower your sword for the sake of the future.
	 */
	public static final NpcStringId S1_THE_WAR_IS_OVER_LOWER_YOUR_SWORD_FOR_THE_SAKE_OF_THE_FUTURE;

	/**
	 * ID: 71755<br>
	 * Message: 90 Territory Badges, 450 scores in Individual Fame and $s2 Adenas
	 */
	public static final NpcStringId N91_TERRITORY_BADGES_450_SCORES_IN_INDIVIDUAL_FAME_AND_S2_ADENAS;

	/**
	 * ID: 72903<br>
	 * Message: The mercenary quest number is $s1; memostate1 is $s2; and memostate2 is $s3.
	 */
	public static final NpcStringId THE_MERCENARY_QUEST_NUMBER_IS_S1_MEMOSTATE1_IS_S2_AND_MEMOSTATE2_IS_S3;

	/**
	 * ID: 72904<br>
	 * Message: user_connected event occurrence success. Siege Id is $s1, Number 728 memo2 is $s2. 729/memo2 is $s3, and 255/memo1 is $s4.
	 */
	public static final NpcStringId
			USER_CONNECTED_EVENT_OCCURRENCE_SUCCESS_SIEGE_ID_IS_S1_NUMBER_728_MEMO2_IS_S2_729_MEMO2_IS_S3_AND_255_MEMO1_IS_S4;

	/**
	 * ID: 72905<br>
	 * Message: Territory Catapult dying event catapult's territory ID $s1, party status $s2.
	 */
	public static final NpcStringId TERRITORY_CATAPULT_DYING_EVENT_CATAPULTS_TERRITORY_ID_S1_PARTY_STATUS_S2;

	/**
	 * ID: 72951<br>
	 * Message: Protect the catapult of Gludio!
	 */
	public static final NpcStringId PROTECT_THE_CATAPULT_OF_GLUDIO;

	/**
	 * ID: 72952<br>
	 * Message: Protect the catapult of Dion!
	 */
	public static final NpcStringId PROTECT_THE_CATAPULT_OF_DION;

	/**
	 * ID: 72953<br>
	 * Message: Protect the catapult of Giran!
	 */
	public static final NpcStringId PROTECT_THE_CATAPULT_OF_GIRAN;

	/**
	 * ID: 72954<br>
	 * Message: Protect the catapult of Oren!
	 */
	public static final NpcStringId PROTECT_THE_CATAPULT_OF_OREN;

	/**
	 * ID: 72955<br>
	 * Message: Protect the catapult of Aden!
	 */
	public static final NpcStringId PROTECT_THE_CATAPULT_OF_ADEN;

	/**
	 * ID: 72956<br>
	 * Message: Protect the catapult of Innadril!
	 */
	public static final NpcStringId PROTECT_THE_CATAPULT_OF_INNADRIL;

	/**
	 * ID: 72957<br>
	 * Message: Protect the catapult of Goddard!
	 */
	public static final NpcStringId PROTECT_THE_CATAPULT_OF_GODDARD;

	/**
	 * ID: 72958<br>
	 * Message: Protect the catapult of Rune!
	 */
	public static final NpcStringId PROTECT_THE_CATAPULT_OF_RUNE;

	/**
	 * ID: 72959<br>
	 * Message: Protect the catapult of Schuttgart!
	 */
	public static final NpcStringId PROTECT_THE_CATAPULT_OF_SCHUTTGART;

	/**
	 * ID: 72961<br>
	 * Message: The catapult of Gludio has been destroyed!
	 */
	public static final NpcStringId THE_CATAPULT_OF_GLUDIO_HAS_BEEN_DESTROYED;

	/**
	 * ID: 72962<br>
	 * Message: The catapult of Dion has been destroyed!
	 */
	public static final NpcStringId THE_CATAPULT_OF_DION_HAS_BEEN_DESTROYED;

	/**
	 * ID: 72963<br>
	 * Message: The catapult of Giran has been destroyed!
	 */
	public static final NpcStringId THE_CATAPULT_OF_GIRAN_HAS_BEEN_DESTROYED;

	/**
	 * ID: 72964<br>
	 * Message: The catapult of Oren has been destroyed!
	 */
	public static final NpcStringId THE_CATAPULT_OF_OREN_HAS_BEEN_DESTROYED;

	/**
	 * ID: 72965<br>
	 * Message: The catapult of Aden has been destroyed!
	 */
	public static final NpcStringId THE_CATAPULT_OF_ADEN_HAS_BEEN_DESTROYED;

	/**
	 * ID: 72966<br>
	 * Message: The catapult of Innadril has been destroyed!
	 */
	public static final NpcStringId THE_CATAPULT_OF_INNADRIL_HAS_BEEN_DESTROYED;

	/**
	 * ID: 72967<br>
	 * Message: The catapult of Goddard has been destroyed!
	 */
	public static final NpcStringId THE_CATAPULT_OF_GODDARD_HAS_BEEN_DESTROYED;

	/**
	 * ID: 72968<br>
	 * Message: The catapult of Rune has been destroyed!
	 */
	public static final NpcStringId THE_CATAPULT_OF_RUNE_HAS_BEEN_DESTROYED;

	/**
	 * ID: 72969<br>
	 * Message: The catapult of Schuttgart has been destroyed!
	 */
	public static final NpcStringId THE_CATAPULT_OF_SCHUTTGART_HAS_BEEN_DESTROYED;

	/**
	 * ID: 72981<br>
	 * Message: Gludio
	 */
	public static final NpcStringId GLUDIO;

	/**
	 * ID: 72982<br>
	 * Message: Dion
	 */
	public static final NpcStringId DION;

	/**
	 * ID: 72983<br>
	 * Message: Giran
	 */
	public static final NpcStringId GIRAN;

	/**
	 * ID: 72984<br>
	 * Message: Oren
	 */
	public static final NpcStringId OREN;

	/**
	 * ID: 72985<br>
	 * Message: Aden
	 */
	public static final NpcStringId ADEN;

	/**
	 * ID: 72986<br>
	 * Message: Innadril
	 */
	public static final NpcStringId INNADRIL;

	/**
	 * ID: 72987<br>
	 * Message: Goddard
	 */
	public static final NpcStringId GODDARD;

	/**
	 * ID: 72988<br>
	 * Message: Rune
	 */
	public static final NpcStringId RUNE;

	/**
	 * ID: 72989<br>
	 * Message: Schuttgart
	 */
	public static final NpcStringId SCHUTTGART;

	/**
	 * ID: 73051<br>
	 * Message: Protect the supplies safe of Gludio!
	 */
	public static final NpcStringId PROTECT_THE_SUPPLIES_SAFE_OF_GLUDIO;

	/**
	 * ID: 73052<br>
	 * Message: Protect the supplies safe of Dion!
	 */
	public static final NpcStringId PROTECT_THE_SUPPLIES_SAFE_OF_DION;

	/**
	 * ID: 73053<br>
	 * Message: Protect the supplies safe of Giran!
	 */
	public static final NpcStringId PROTECT_THE_SUPPLIES_SAFE_OF_GIRAN;

	/**
	 * ID: 73054<br>
	 * Message: Protect the supplies safe of Oren!
	 */
	public static final NpcStringId PROTECT_THE_SUPPLIES_SAFE_OF_OREN;

	/**
	 * ID: 73055<br>
	 * Message: Protect the supplies safe of Aden!
	 */
	public static final NpcStringId PROTECT_THE_SUPPLIES_SAFE_OF_ADEN;

	/**
	 * ID: 73056<br>
	 * Message: Protect the supplies safe of Innadril!
	 */
	public static final NpcStringId PROTECT_THE_SUPPLIES_SAFE_OF_INNADRIL;

	/**
	 * ID: 73057<br>
	 * Message: Protect the supplies safe of Goddard!
	 */
	public static final NpcStringId PROTECT_THE_SUPPLIES_SAFE_OF_GODDARD;

	/**
	 * ID: 73058<br>
	 * Message: Protect the supplies safe of Rune!
	 */
	public static final NpcStringId PROTECT_THE_SUPPLIES_SAFE_OF_RUNE;

	/**
	 * ID: 73059<br>
	 * Message: Protect the supplies safe of Schuttgart!
	 */
	public static final NpcStringId PROTECT_THE_SUPPLIES_SAFE_OF_SCHUTTGART;

	/**
	 * ID: 73061<br>
	 * Message: The supplies safe of Gludio has been destroyed!
	 */
	public static final NpcStringId THE_SUPPLIES_SAFE_OF_GLUDIO_HAS_BEEN_DESTROYED;

	/**
	 * ID: 73062<br>
	 * Message: The supplies safe of Dion has been destroyed!
	 */
	public static final NpcStringId THE_SUPPLIES_SAFE_OF_DION_HAS_BEEN_DESTROYED;

	/**
	 * ID: 73063<br>
	 * Message: The supplies safe of Giran has been destroyed!
	 */
	public static final NpcStringId THE_SUPPLIES_SAFE_OF_GIRAN_HAS_BEEN_DESTROYED;

	/**
	 * ID: 73064<br>
	 * Message: The supplies safe of Oren has been destroyed!
	 */
	public static final NpcStringId THE_SUPPLIES_SAFE_OF_OREN_HAS_BEEN_DESTROYED;

	/**
	 * ID: 73065<br>
	 * Message: The supplies safe of Aden has been destroyed!
	 */
	public static final NpcStringId THE_SUPPLIES_SAFE_OF_ADEN_HAS_BEEN_DESTROYED;

	/**
	 * ID: 73066<br>
	 * Message: The supplies safe of Innadril has been destroyed!
	 */
	public static final NpcStringId THE_SUPPLIES_SAFE_OF_INNADRIL_HAS_BEEN_DESTROYED;

	/**
	 * ID: 73067<br>
	 * Message: The supplies safe of Goddard has been destroyed!
	 */
	public static final NpcStringId THE_SUPPLIES_SAFE_OF_GODDARD_HAS_BEEN_DESTROYED;

	/**
	 * ID: 73068<br>
	 * Message: The supplies safe of Rune has been destroyed!
	 */
	public static final NpcStringId THE_SUPPLIES_SAFE_OF_RUNE_HAS_BEEN_DESTROYED;

	/**
	 * ID: 73069<br>
	 * Message: The supplies safe of Schuttgart has been destroyed!
	 */
	public static final NpcStringId THE_SUPPLIES_SAFE_OF_SCHUTTGART_HAS_BEEN_DESTROYED;

	/**
	 * ID: 73151<br>
	 * Message: Protect the Military Association Leader of Gludio!
	 */
	public static final NpcStringId PROTECT_THE_MILITARY_ASSOCIATION_LEADER_OF_GLUDIO;

	/**
	 * ID: 73152<br>
	 * Message: Protect the Military Association Leader of Dion!
	 */
	public static final NpcStringId PROTECT_THE_MILITARY_ASSOCIATION_LEADER_OF_DION;

	/**
	 * ID: 73153<br>
	 * Message: Protect the Military Association Leader of Giran!
	 */
	public static final NpcStringId PROTECT_THE_MILITARY_ASSOCIATION_LEADER_OF_GIRAN;

	/**
	 * ID: 73154<br>
	 * Message: Protect the Military Association Leader of Oren!
	 */
	public static final NpcStringId PROTECT_THE_MILITARY_ASSOCIATION_LEADER_OF_OREN;

	/**
	 * ID: 73155<br>
	 * Message: Protect the Military Association Leader of Aden!
	 */
	public static final NpcStringId PROTECT_THE_MILITARY_ASSOCIATION_LEADER_OF_ADEN;

	/**
	 * ID: 73156<br>
	 * Message: Protect the Military Association Leader of Innadril!
	 */
	public static final NpcStringId PROTECT_THE_MILITARY_ASSOCIATION_LEADER_OF_INNADRIL;

	/**
	 * ID: 73157<br>
	 * Message: Protect the Military Association Leader of Goddard!
	 */
	public static final NpcStringId PROTECT_THE_MILITARY_ASSOCIATION_LEADER_OF_GODDARD;

	/**
	 * ID: 73158<br>
	 * Message: Protect the Military Association Leader of Rune!
	 */
	public static final NpcStringId PROTECT_THE_MILITARY_ASSOCIATION_LEADER_OF_RUNE;

	/**
	 * ID: 73159<br>
	 * Message: Protect the Military Association Leader of Schuttgart!
	 */
	public static final NpcStringId PROTECT_THE_MILITARY_ASSOCIATION_LEADER_OF_SCHUTTGART;

	/**
	 * ID: 73161<br>
	 * Message: The Military Association Leader of Gludio is dead!
	 */
	public static final NpcStringId THE_MILITARY_ASSOCIATION_LEADER_OF_GLUDIO_IS_DEAD;

	/**
	 * ID: 73162<br>
	 * Message: The Military Association Leader of Dion is dead!
	 */
	public static final NpcStringId THE_MILITARY_ASSOCIATION_LEADER_OF_DION_IS_DEAD;

	/**
	 * ID: 73163<br>
	 * Message: The Military Association Leader of Giran is dead!
	 */
	public static final NpcStringId THE_MILITARY_ASSOCIATION_LEADER_OF_GIRAN_IS_DEAD;

	/**
	 * ID: 73164<br>
	 * Message: The Military Association Leader of Oren is dead!
	 */
	public static final NpcStringId THE_MILITARY_ASSOCIATION_LEADER_OF_OREN_IS_DEAD;

	/**
	 * ID: 73165<br>
	 * Message: The Military Association Leader of Aden is dead!
	 */
	public static final NpcStringId THE_MILITARY_ASSOCIATION_LEADER_OF_ADEN_IS_DEAD;

	/**
	 * ID: 73166<br>
	 * Message: The Military Association Leader of Innadril is dead!
	 */
	public static final NpcStringId THE_MILITARY_ASSOCIATION_LEADER_OF_INNADRIL_IS_DEAD;

	/**
	 * ID: 73167<br>
	 * Message: The Military Association Leader of Goddard is dead!
	 */
	public static final NpcStringId THE_MILITARY_ASSOCIATION_LEADER_OF_GODDARD_IS_DEAD;

	/**
	 * ID: 73168<br>
	 * Message: The Military Association Leader of Rune is dead!
	 */
	public static final NpcStringId THE_MILITARY_ASSOCIATION_LEADER_OF_RUNE_IS_DEAD;

	/**
	 * ID: 73169<br>
	 * Message: The Military Association Leader of Schuttgart is dead!
	 */
	public static final NpcStringId THE_MILITARY_ASSOCIATION_LEADER_OF_SCHUTTGART_IS_DEAD;

	/**
	 * ID: 73251<br>
	 * Message: Protect the Religious Association Leader of Gludio!
	 */
	public static final NpcStringId PROTECT_THE_RELIGIOUS_ASSOCIATION_LEADER_OF_GLUDIO;

	/**
	 * ID: 73252<br>
	 * Message: Protect the Religious Association Leader of Dion!
	 */
	public static final NpcStringId PROTECT_THE_RELIGIOUS_ASSOCIATION_LEADER_OF_DION;

	/**
	 * ID: 73253<br>
	 * Message: Protect the Religious Association Leader of Giran!
	 */
	public static final NpcStringId PROTECT_THE_RELIGIOUS_ASSOCIATION_LEADER_OF_GIRAN;

	/**
	 * ID: 73254<br>
	 * Message: Protect the Religious Association Leader of Oren!
	 */
	public static final NpcStringId PROTECT_THE_RELIGIOUS_ASSOCIATION_LEADER_OF_OREN;

	/**
	 * ID: 73255<br>
	 * Message: Protect the Religious Association Leader of Aden!
	 */
	public static final NpcStringId PROTECT_THE_RELIGIOUS_ASSOCIATION_LEADER_OF_ADEN;

	/**
	 * ID: 73256<br>
	 * Message: Protect the Religious Association Leader of Innadril!
	 */
	public static final NpcStringId PROTECT_THE_RELIGIOUS_ASSOCIATION_LEADER_OF_INNADRIL;

	/**
	 * ID: 73257<br>
	 * Message: Protect the Religious Association Leader of Goddard!
	 */
	public static final NpcStringId PROTECT_THE_RELIGIOUS_ASSOCIATION_LEADER_OF_GODDARD;

	/**
	 * ID: 73258<br>
	 * Message: Protect the Religious Association Leader of Rune!
	 */
	public static final NpcStringId PROTECT_THE_RELIGIOUS_ASSOCIATION_LEADER_OF_RUNE;

	/**
	 * ID: 73259<br>
	 * Message: Protect the Religious Association Leader of Schuttgart!
	 */
	public static final NpcStringId PROTECT_THE_RELIGIOUS_ASSOCIATION_LEADER_OF_SCHUTTGART;

	/**
	 * ID: 73261<br>
	 * Message: The Religious Association Leader of Gludio is dead!
	 */
	public static final NpcStringId THE_RELIGIOUS_ASSOCIATION_LEADER_OF_GLUDIO_IS_DEAD;

	/**
	 * ID: 73262<br>
	 * Message: The Religious Association Leader of Dion is dead!
	 */
	public static final NpcStringId THE_RELIGIOUS_ASSOCIATION_LEADER_OF_DION_IS_DEAD;

	/**
	 * ID: 73263<br>
	 * Message: The Religious Association Leader of Giran is dead!
	 */
	public static final NpcStringId THE_RELIGIOUS_ASSOCIATION_LEADER_OF_GIRAN_IS_DEAD;

	/**
	 * ID: 73264<br>
	 * Message: The Religious Association Leader of Oren is dead!
	 */
	public static final NpcStringId THE_RELIGIOUS_ASSOCIATION_LEADER_OF_OREN_IS_DEAD;

	/**
	 * ID: 73265<br>
	 * Message: The Religious Association Leader of Aden is dead!
	 */
	public static final NpcStringId THE_RELIGIOUS_ASSOCIATION_LEADER_OF_ADEN_IS_DEAD;

	/**
	 * ID: 73266<br>
	 * Message: The Religious Association Leader of Innadril is dead!
	 */
	public static final NpcStringId THE_RELIGIOUS_ASSOCIATION_LEADER_OF_INNADRIL_IS_DEAD;

	/**
	 * ID: 73267<br>
	 * Message: The Religious Association Leader of Goddard is dead!
	 */
	public static final NpcStringId THE_RELIGIOUS_ASSOCIATION_LEADER_OF_GODDARD_IS_DEAD;

	/**
	 * ID: 73268<br>
	 * Message: The Religious Association Leader of Rune is dead!
	 */
	public static final NpcStringId THE_RELIGIOUS_ASSOCIATION_LEADER_OF_RUNE_IS_DEAD;

	/**
	 * ID: 73269<br>
	 * Message: The Religious Association Leader of Schuttgart is dead!
	 */
	public static final NpcStringId THE_RELIGIOUS_ASSOCIATION_LEADER_OF_SCHUTTGART_IS_DEAD;

	/**
	 * ID: 73351<br>
	 * Message: Protect the Economic Association Leader of Gludio!
	 */
	public static final NpcStringId PROTECT_THE_ECONOMIC_ASSOCIATION_LEADER_OF_GLUDIO;

	/**
	 * ID: 73352<br>
	 * Message: Protect the Economic Association Leader of Dion!
	 */
	public static final NpcStringId PROTECT_THE_ECONOMIC_ASSOCIATION_LEADER_OF_DION;

	/**
	 * ID: 73353<br>
	 * Message: Protect the Economic Association Leader of Giran!
	 */
	public static final NpcStringId PROTECT_THE_ECONOMIC_ASSOCIATION_LEADER_OF_GIRAN;

	/**
	 * ID: 73354<br>
	 * Message: Protect the Economic Association Leader of Oren!
	 */
	public static final NpcStringId PROTECT_THE_ECONOMIC_ASSOCIATION_LEADER_OF_OREN;

	/**
	 * ID: 73355<br>
	 * Message: Protect the Economic Association Leader of Aden!
	 */
	public static final NpcStringId PROTECT_THE_ECONOMIC_ASSOCIATION_LEADER_OF_ADEN;

	/**
	 * ID: 73356<br>
	 * Message: Protect the Economic Association Leader of Innadril!
	 */
	public static final NpcStringId PROTECT_THE_ECONOMIC_ASSOCIATION_LEADER_OF_INNADRIL;

	/**
	 * ID: 73357<br>
	 * Message: Protect the Economic Association Leader of Goddard!
	 */
	public static final NpcStringId PROTECT_THE_ECONOMIC_ASSOCIATION_LEADER_OF_GODDARD;

	/**
	 * ID: 73358<br>
	 * Message: Protect the Economic Association Leader of Rune!
	 */
	public static final NpcStringId PROTECT_THE_ECONOMIC_ASSOCIATION_LEADER_OF_RUNE;

	/**
	 * ID: 73359<br>
	 * Message: Protect the Economic Association Leader of Schuttgart!
	 */
	public static final NpcStringId PROTECT_THE_ECONOMIC_ASSOCIATION_LEADER_OF_SCHUTTGART;

	/**
	 * ID: 73361<br>
	 * Message: The Economic Association Leader of Gludio is dead!
	 */
	public static final NpcStringId THE_ECONOMIC_ASSOCIATION_LEADER_OF_GLUDIO_IS_DEAD;

	/**
	 * ID: 73362<br>
	 * Message: The Economic Association Leader of Dion is dead!
	 */
	public static final NpcStringId THE_ECONOMIC_ASSOCIATION_LEADER_OF_DION_IS_DEAD;

	/**
	 * ID: 73363<br>
	 * Message: The Economic Association Leader of Giran is dead!
	 */
	public static final NpcStringId THE_ECONOMIC_ASSOCIATION_LEADER_OF_GIRAN_IS_DEAD;

	/**
	 * ID: 73364<br>
	 * Message: The Economic Association Leader of Oren is dead!
	 */
	public static final NpcStringId THE_ECONOMIC_ASSOCIATION_LEADER_OF_OREN_IS_DEAD;

	/**
	 * ID: 73365<br>
	 * Message: The Economic Association Leader of Aden is dead!
	 */
	public static final NpcStringId THE_ECONOMIC_ASSOCIATION_LEADER_OF_ADEN_IS_DEAD;

	/**
	 * ID: 73366<br>
	 * Message: The Economic Association Leader of Innadril is dead!
	 */
	public static final NpcStringId THE_ECONOMIC_ASSOCIATION_LEADER_OF_INNADRIL_IS_DEAD;

	/**
	 * ID: 73367<br>
	 * Message: The Economic Association Leader of Goddard is dead!
	 */
	public static final NpcStringId THE_ECONOMIC_ASSOCIATION_LEADER_OF_GODDARD_IS_DEAD;

	/**
	 * ID: 73368<br>
	 * Message: The Economic Association Leader of Rune is dead!
	 */
	public static final NpcStringId THE_ECONOMIC_ASSOCIATION_LEADER_OF_RUNE_IS_DEAD;

	/**
	 * ID: 73369<br>
	 * Message: The Economic Association Leader of Schuttgart is dead!
	 */
	public static final NpcStringId THE_ECONOMIC_ASSOCIATION_LEADER_OF_SCHUTTGART_IS_DEAD;

	/**
	 * ID: 73451<br>
	 * Message: Defeat $s1 enemy knights!
	 */
	public static final NpcStringId DEFEAT_S1_ENEMY_KNIGHTS;

	/**
	 * ID: 73461<br>
	 * Message: You have defeated $s2 of $s1 knights.
	 */
	public static final NpcStringId YOU_HAVE_DEFEATED_S2_OF_S1_KNIGHTS;

	/**
	 * ID: 73462<br>
	 * Message: You weakened the enemy's defense!
	 */
	public static final NpcStringId YOU_WEAKENED_THE_ENEMYS_DEFENSE;

	/**
	 * ID: 73551<br>
	 * Message: Defeat $s1 warriors and rogues!
	 */
	public static final NpcStringId DEFEAT_S1_WARRIORS_AND_ROGUES;

	/**
	 * ID: 73561<br>
	 * Message: You have defeated $s2 of $s1 warriors and rogues.
	 */
	public static final NpcStringId YOU_HAVE_DEFEATED_S2_OF_S1_WARRIORS_AND_ROGUES;

	/**
	 * ID: 73562<br>
	 * Message: You weakened the enemy's attack!
	 */
	public static final NpcStringId YOU_WEAKENED_THE_ENEMYS_ATTACK;

	/**
	 * ID: 73651<br>
	 * Message: Defeat $s1 wizards and summoners!
	 */
	public static final NpcStringId DEFEAT_S1_WIZARDS_AND_SUMMONERS;

	/**
	 * ID: 73661<br>
	 * Message: You have defeated $s2 of $s1 enemies.
	 */
	public static final NpcStringId YOU_HAVE_DEFEATED_S2_OF_S1_ENEMIES;

	/**
	 * ID: 73662<br>
	 * Message: You weakened the enemy's magic!
	 */
	public static final NpcStringId YOU_WEAKENED_THE_ENEMYS_MAGIC;

	/**
	 * ID: 73751<br>
	 * Message: Defeat $s1 healers and buffers.
	 */
	public static final NpcStringId DEFEAT_S1_HEALERS_AND_BUFFERS;

	/**
	 * ID: 73761<br>
	 * Message: You have defeated $s2 of $s1 healers and buffers.
	 */
	public static final NpcStringId YOU_HAVE_DEFEATED_S2_OF_S1_HEALERS_AND_BUFFERS;

	/**
	 * ID: 73762<br>
	 * Message: You have weakened the enemy's support!
	 */
	public static final NpcStringId YOU_HAVE_WEAKENED_THE_ENEMYS_SUPPORT;

	/**
	 * ID: 73851<br>
	 * Message: Defeat $s1 warsmiths and overlords.
	 */
	public static final NpcStringId DEFEAT_S1_WARSMITHS_AND_OVERLORDS;

	/**
	 * ID: 73861<br>
	 * Message: You have defeated $s2 of $s1 warsmiths and overlords.
	 */
	public static final NpcStringId YOU_HAVE_DEFEATED_S2_OF_S1_WARSMITHS_AND_OVERLORDS;

	/**
	 * ID: 73862<br>
	 * Message: You destroyed the enemy's professionals!
	 */
	public static final NpcStringId YOU_DESTROYED_THE_ENEMYS_PROFESSIONALS;

	/**
	 * ID: 99601<br>
	 * Message: Tra la la... Today, I'm going to make another fun-filled trip.  I wonder what I should look for this time...
	 */
	public static final NpcStringId
			TRA_LA_LA_TODAY_IM_GOING_TO_MAKE_ANOTHER_FUN_FILLED_TRIP_I_WONDER_WHAT_I_SHOULD_LOOK_FOR_THIS_TIME;

	/**
	 * ID: 99700<br>
	 * Message: What's this? Why am I being disturbed?
	 */
	public static final NpcStringId WHATS_THIS_WHY_AM_I_BEING_DISTURBED;

	/**
	 * ID: 99701<br>
	 * Message: Ta-da! Here I am!
	 */
	public static final NpcStringId TA_DA_HERE_I_AM;

	/**
	 * ID: 99702<br>
	 * Message: What are you looking at?
	 */
	public static final NpcStringId WHAT_ARE_YOU_LOOKING_AT;

	/**
	 * ID: 99703<br>
	 * Message: If you give me nectar, this little squash will grow up quickly!
	 */
	public static final NpcStringId IF_YOU_GIVE_ME_NECTAR_THIS_LITTLE_SQUASH_WILL_GROW_UP_QUICKLY;

	/**
	 * ID: 99704<br>
	 * Message: Are you my mommy?
	 */
	public static final NpcStringId ARE_YOU_MY_MOMMY;

	/**
	 * ID: 99705<br>
	 * Message: Fancy meeting you here!
	 */
	public static final NpcStringId FANCY_MEETING_YOU_HERE;

	/**
	 * ID: 99706<br>
	 * Message: Are you afraid of the big-bad squash?
	 */
	public static final NpcStringId ARE_YOU_AFRAID_OF_THE_BIG_BAD_SQUASH;

	/**
	 * ID: 99707<br>
	 * Message: Impressive, aren't I?
	 */
	public static final NpcStringId IMPRESSIVE_ARENT_I;

	/**
	 * ID: 99708<br>
	 * Message: Obey me!!
	 */
	public static final NpcStringId OBEY_ME;

	/**
	 * ID: 99709<br>
	 * Message: Raise me well and you'll be rewarded. Neglect me and suffer the consequences!
	 */
	public static final NpcStringId RAISE_ME_WELL_AND_YOULL_BE_REWARDED_NEGLECT_ME_AND_SUFFER_THE_CONSEQUENCES;

	/**
	 * ID: 99710<br>
	 * Message: Transform!
	 */
	public static final NpcStringId TRANSFORM;

	/**
	 * ID: 99711<br>
	 * Message: I feel different?
	 */
	public static final NpcStringId I_FEEL_DIFFERENT;

	/**
	 * ID: 99712<br>
	 * Message: I'm bigger now! Bring it on!
	 */
	public static final NpcStringId IM_BIGGER_NOW_BRING_IT_ON;

	/**
	 * ID: 99713<br>
	 * Message: I'm not a kid anymore!
	 */
	public static final NpcStringId IM_NOT_A_KID_ANYMORE;

	/**
	 * ID: 99714<br>
	 * Message: Big time!
	 */
	public static final NpcStringId BIG_TIME;

	/**
	 * ID: 99716<br>
	 * Message: I'm all grown up now!
	 */
	public static final NpcStringId IM_ALL_GROWN_UP_NOW;

	/**
	 * ID: 99717<br>
	 * Message: If you let me go, I'll be your best friend.
	 */
	public static final NpcStringId IF_YOU_LET_ME_GO_ILL_BE_YOUR_BEST_FRIEND;

	/**
	 * ID: 99718<br>
	 * Message: I'm chuck full of goodness!
	 */
	public static final NpcStringId IM_CHUCK_FULL_OF_GOODNESS;

	/**
	 * ID: 99719<br>
	 * Message: Good job! Now what are you going to do?
	 */
	public static final NpcStringId GOOD_JOB_NOW_WHAT_ARE_YOU_GOING_TO_DO;

	/**
	 * ID: 99720<br>
	 * Message: Keep it coming!
	 */
	public static final NpcStringId KEEP_IT_COMING;

	/**
	 * ID: 99721<br>
	 * Message: That's what I'm talking about!
	 */
	public static final NpcStringId THATS_WHAT_IM_TALKING_ABOUT;

	/**
	 * ID: 99722<br>
	 * Message: May I have some more?
	 */
	public static final NpcStringId MAY_I_HAVE_SOME_MORE;

	/**
	 * ID: 99723<br>
	 * Message: That hit the spot!
	 */
	public static final NpcStringId THAT_HIT_THE_SPOT;

	/**
	 * ID: 99724<br>
	 * Message: I feel special!
	 */
	public static final NpcStringId I_FEEL_SPECIAL;

	/**
	 * ID: 99725<br>
	 * Message: I think it's working!
	 */
	public static final NpcStringId I_THINK_ITS_WORKING;

	/**
	 * ID: 99726<br>
	 * Message: You DO understand!
	 */
	public static final NpcStringId YOU_DO_UNDERSTAND;

	/**
	 * ID: 99727<br>
	 * Message: Yuck! What is this? Ha Ha just kidding!
	 */
	public static final NpcStringId YUCK_WHAT_IS_THIS_HA_HA_JUST_KIDDING;

	/**
	 * ID: 99728<br>
	 * Message: A total of five and I'll be twice as alive!
	 */
	public static final NpcStringId A_TOTAL_OF_FIVE_AND_ILL_BE_TWICE_AS_ALIVE;

	/**
	 * ID: 99729<br>
	 * Message: Nectar is sublime!
	 */
	public static final NpcStringId NECTAR_IS_SUBLIME;

	/**
	 * ID: 99730<br>
	 * Message: You call that a hit?
	 */
	public static final NpcStringId YOU_CALL_THAT_A_HIT;

	/**
	 * ID: 99731<br>
	 * Message: Why are you hitting me? Ouch, stop it! Give me nectar!
	 */
	public static final NpcStringId WHY_ARE_YOU_HITTING_ME_OUCH_STOP_IT_GIVE_ME_NECTAR;

	/**
	 * ID: 99732<br>
	 * Message: Stop or I'll wilt!
	 */
	public static final NpcStringId STOP_OR_ILL_WILT;

	/**
	 * ID: 99733<br>
	 * Message: I'm not fully grown yet! Oh well, do what you will. I'll fade away without nectar anyway!
	 */
	public static final NpcStringId IM_NOT_FULLY_GROWN_YET_OH_WELL_DO_WHAT_YOU_WILL_ILL_FADE_AWAY_WITHOUT_NECTAR_ANYWAY;

	/**
	 * ID: 99734<br>
	 * Message: Go ahead and hit me again. It won't do you any good!
	 */
	public static final NpcStringId GO_AHEAD_AND_HIT_ME_AGAIN_IT_WONT_DO_YOU_ANY_GOOD;

	/**
	 * ID: 99735<br>
	 * Message: Woe is me! I'm wilting!
	 */
	public static final NpcStringId WOE_IS_ME_IM_WILTING;

	/**
	 * ID: 99736<br>
	 * Message: I'm not fully grown yet! How about some nectar to ease my pain?
	 */
	public static final NpcStringId IM_NOT_FULLY_GROWN_YET_HOW_ABOUT_SOME_NECTAR_TO_EASE_MY_PAIN;

	/**
	 * ID: 99737<br>
	 * Message: The end is near!
	 */
	public static final NpcStringId THE_END_IS_NEAR;

	/**
	 * ID: 99738<br>
	 * Message: Pretty please... with sugar on top, give me some nectar!
	 */
	public static final NpcStringId PRETTY_PLEASE_WITH_SUGAR_ON_TOP_GIVE_ME_SOME_NECTAR;

	/**
	 * ID: 99739<br>
	 * Message: If I die without nectar, you'll get nothing.
	 */
	public static final NpcStringId IF_I_DIE_WITHOUT_NECTAR_YOULL_GET_NOTHING;

	/**
	 * ID: 99740<br>
	 * Message: I'm feeling better! Another thirty seconds and I'll be out of here!
	 */
	public static final NpcStringId IM_FEELING_BETTER_ANOTHER_THIRTY_SECONDS_AND_ILL_BE_OUT_OF_HERE;

	/**
	 * ID: 99741<br>
	 * Message: Twenty seconds and it's ciao, baby!
	 */
	public static final NpcStringId TWENTY_SECONDS_AND_ITS_CIAO_BABY;

	/**
	 * ID: 99742<br>
	 * Message: Woohoo, just ten seconds left! nine... eight... seven!
	 */
	public static final NpcStringId WOOHOO_JUST_TEN_SECONDS_LEFT_NINE_EIGHT_SEVEN;

	/**
	 * ID: 99743<br>
	 * Message: Give me nectar or I'll be gone in two minutes!
	 */
	public static final NpcStringId GIVE_ME_NECTAR_OR_ILL_BE_GONE_IN_TWO_MINUTES;

	/**
	 * ID: 99744<br>
	 * Message: Give me nectar or I'll be gone in one minute!
	 */
	public static final NpcStringId GIVE_ME_NECTAR_OR_ILL_BE_GONE_IN_ONE_MINUTE;

	/**
	 * ID: 99745<br>
	 * Message: So long suckers!
	 */
	public static final NpcStringId SO_LONG_SUCKERS;

	/**
	 * ID: 99746<br>
	 * Message: I'm out of here!
	 */
	public static final NpcStringId IM_OUT_OF_HERE;

	/**
	 * ID: 99747<br>
	 * Message: I must be going! Have fun everybody!
	 */
	public static final NpcStringId I_MUST_BE_GOING_HAVE_FUN_EVERYBODY;

	/**
	 * ID: 99748<br>
	 * Message: Time is up! Put your weapons down!
	 */
	public static final NpcStringId TIME_IS_UP_PUT_YOUR_WEAPONS_DOWN;

	/**
	 * ID: 99749<br>
	 * Message: Good for me, bad for you!
	 */
	public static final NpcStringId GOOD_FOR_ME_BAD_FOR_YOU;

	/**
	 * ID: 99750<br>
	 * Message: Soundtastic!
	 */
	public static final NpcStringId SOUNDTASTIC;

	/**
	 * ID: 99751<br>
	 * Message: I can sing along if you like?
	 */
	public static final NpcStringId I_CAN_SING_ALONG_IF_YOU_LIKE;

	/**
	 * ID: 99752<br>
	 * Message: I think you need some backup!
	 */
	public static final NpcStringId I_THINK_YOU_NEED_SOME_BACKUP;

	/**
	 * ID: 99753<br>
	 * Message: Keep up that rhythm and you'll be a star!
	 */
	public static final NpcStringId KEEP_UP_THAT_RHYTHM_AND_YOULL_BE_A_STAR;

	/**
	 * ID: 99754<br>
	 * Message: My heart yearns for more music.
	 */
	public static final NpcStringId MY_HEART_YEARNS_FOR_MORE_MUSIC;

	/**
	 * ID: 99755<br>
	 * Message: You're out of tune again!
	 */
	public static final NpcStringId YOURE_OUT_OF_TUNE_AGAIN;

	/**
	 * ID: 99756<br>
	 * Message: This is awful!
	 */
	public static final NpcStringId THIS_IS_AWFUL;

	/**
	 * ID: 99757<br>
	 * Message: I think I broke something!
	 */
	public static final NpcStringId I_THINK_I_BROKE_SOMETHING;

	/**
	 * ID: 99758<br>
	 * Message: What a lovely melody! Play it again!
	 */
	public static final NpcStringId WHAT_A_LOVELY_MELODY_PLAY_IT_AGAIN;

	/**
	 * ID: 99759<br>
	 * Message: Music to my, uh... ears!
	 */
	public static final NpcStringId MUSIC_TO_MY_UH_EARS;

	/**
	 * ID: 99760<br>
	 * Message: You need music lessons!
	 */
	public static final NpcStringId YOU_NEED_MUSIC_LESSONS;

	/**
	 * ID: 99761<br>
	 * Message: I can't hear you!
	 */
	public static final NpcStringId I_CANT_HEAR_YOU;

	/**
	 * ID: 99762<br>
	 * Message: You can't hurt me like that!
	 */
	public static final NpcStringId YOU_CANT_HURT_ME_LIKE_THAT;

	/**
	 * ID: 99763<br>
	 * Message: I'm stronger than you are!
	 */
	public static final NpcStringId IM_STRONGER_THAN_YOU_ARE;

	/**
	 * ID: 99764<br>
	 * Message: No music? I'm out of here!
	 */
	public static final NpcStringId NO_MUSIC_IM_OUT_OF_HERE;

	/**
	 * ID: 99765<br>
	 * Message: That racket is getting on my nerves! Tone it down a bit!
	 */
	public static final NpcStringId THAT_RACKET_IS_GETTING_ON_MY_NERVES_TONE_IT_DOWN_A_BIT;

	/**
	 * ID: 99766<br>
	 * Message: You can only hurt me through music!
	 */
	public static final NpcStringId YOU_CAN_ONLY_HURT_ME_THROUGH_MUSIC;

	/**
	 * ID: 99767<br>
	 * Message: Only musical instruments can hurt me! Nothing else!
	 */
	public static final NpcStringId ONLY_MUSICAL_INSTRUMENTS_CAN_HURT_ME_NOTHING_ELSE;

	/**
	 * ID: 99768<br>
	 * Message: Your skills are impressive, but sadly, useless...
	 */
	public static final NpcStringId YOUR_SKILLS_ARE_IMPRESSIVE_BUT_SADLY_USELESS;

	/**
	 * ID: 99769<br>
	 * Message: Catch a Chrono for me please.
	 */
	public static final NpcStringId CATCH_A_CHRONO_FOR_ME_PLEASE;

	/**
	 * ID: 99770<br>
	 * Message: You got me!
	 */
	public static final NpcStringId YOU_GOT_ME;

	/**
	 * ID: 99771<br>
	 * Message: Now look at what you've done!
	 */
	public static final NpcStringId NOW_LOOK_AT_WHAT_YOUVE_DONE;

	/**
	 * ID: 99772<br>
	 * Message: You win!
	 */
	public static final NpcStringId YOU_WIN;

	/**
	 * ID: 99773<br>
	 * Message: Squashed......
	 */
	public static final NpcStringId SQUASHED;

	/**
	 * ID: 99774<br>
	 * Message: Don't tell anyone!
	 */
	public static final NpcStringId DONT_TELL_ANYONE;

	/**
	 * ID: 99775<br>
	 * Message: Gross! My guts are coming out!
	 */
	public static final NpcStringId GROSS_MY_GUTS_ARE_COMING_OUT;

	/**
	 * ID: 99776<br>
	 * Message: Take it and go.
	 */
	public static final NpcStringId TAKE_IT_AND_GO;

	/**
	 * ID: 99777<br>
	 * Message: I should've left when I could!
	 */
	public static final NpcStringId I_SHOULDVE_LEFT_WHEN_I_COULD;

	/**
	 * ID: 99778<br>
	 * Message: Now look what you have done!
	 */
	public static final NpcStringId NOW_LOOK_WHAT_YOU_HAVE_DONE;

	/**
	 * ID: 99779<br>
	 * Message: I feel dirty!
	 */
	public static final NpcStringId I_FEEL_DIRTY;

	/**
	 * ID: 99780<br>
	 * Message: Better luck next time!
	 */
	public static final NpcStringId BETTER_LUCK_NEXT_TIME;

	/**
	 * ID: 99781<br>
	 * Message: Nice shot!
	 */
	public static final NpcStringId NICE_SHOT;

	/**
	 * ID: 99782<br>
	 * Message: I'm not afraid of you!
	 */
	public static final NpcStringId IM_NOT_AFRAID_OF_YOU;

	/**
	 * ID: 99783<br>
	 * Message: If I knew this was going to happen, I would have stayed home!
	 */
	public static final NpcStringId IF_I_KNEW_THIS_WAS_GOING_TO_HAPPEN_I_WOULD_HAVE_STAYED_HOME;

	/**
	 * ID: 99784<br>
	 * Message: Try harder or I'm out of here!
	 */
	public static final NpcStringId TRY_HARDER_OR_IM_OUT_OF_HERE;

	/**
	 * ID: 99785<br>
	 * Message: I'm tougher than I look!
	 */
	public static final NpcStringId IM_TOUGHER_THAN_I_LOOK;

	/**
	 * ID: 99786<br>
	 * Message: Good strike!
	 */
	public static final NpcStringId GOOD_STRIKE;

	/**
	 * ID: 99787<br>
	 * Message: Oh my gourd!
	 */
	public static final NpcStringId OH_MY_GOURD;

	/**
	 * ID: 99788<br>
	 * Message: That's all you've got?
	 */
	public static final NpcStringId THATS_ALL_YOUVE_GOT;

	/**
	 * ID: 99789<br>
	 * Message: Why me?
	 */
	public static final NpcStringId WHY_ME;

	/**
	 * ID: 99790<br>
	 * Message: Bring me nectar!
	 */
	public static final NpcStringId BRING_ME_NECTAR;

	/**
	 * ID: 99791<br>
	 * Message: I must have nectar to grow!
	 */
	public static final NpcStringId I_MUST_HAVE_NECTAR_TO_GROW;

	/**
	 * ID: 99792<br>
	 * Message: Give me some nectar quickly or you'll get nothing!
	 */
	public static final NpcStringId GIVE_ME_SOME_NECTAR_QUICKLY_OR_YOULL_GET_NOTHING;

	/**
	 * ID: 99793<br>
	 * Message: Please give me some nectar! I'm hungry!
	 */
	public static final NpcStringId PLEASE_GIVE_ME_SOME_NECTAR_IM_HUNGRY;

	/**
	 * ID: 99794<br>
	 * Message: Nectar please!
	 */
	public static final NpcStringId NECTAR_PLEASE;

	/**
	 * ID: 99795<br>
	 * Message: Nectar will make me grow quickly!
	 */
	public static final NpcStringId NECTAR_WILL_MAKE_ME_GROW_QUICKLY;

	/**
	 * ID: 99796<br>
	 * Message: Don't you want a bigger squash? Give me some nectar and I'll grow much larger!
	 */
	public static final NpcStringId DONT_YOU_WANT_A_BIGGER_SQUASH_GIVE_ME_SOME_NECTAR_AND_ILL_GROW_MUCH_LARGER;

	/**
	 * ID: 99797<br>
	 * Message: If you raise me well, you'll get prizes! Or not...
	 */
	public static final NpcStringId IF_YOU_RAISE_ME_WELL_YOULL_GET_PRIZES_OR_NOT;

	/**
	 * ID: 99798<br>
	 * Message: You are here for the stuff, eh? Well it's mine, all mine!
	 */
	public static final NpcStringId YOU_ARE_HERE_FOR_THE_STUFF_EH_WELL_ITS_MINE_ALL_MINE;

	/**
	 * ID: 99799<br>
	 * Message: Trust me... give me nectar and I'll become a giant squash!
	 */
	public static final NpcStringId TRUST_ME_GIVE_ME_NECTAR_AND_ILL_BECOME_A_GIANT_SQUASH;

	/**
	 * ID: 528551<br>
	 * Message: There's nothing you can't say. I can't listen to you anymore!
	 */
	public static final NpcStringId THERES_NOTHING_YOU_CANT_SAY_I_CANT_LISTEN_TO_YOU_ANYMORE;

	/**
	 * ID: 528651<br>
	 * Message: You advanced bravely but got such a tiny result. Hohoho.
	 */
	public static final NpcStringId YOU_ADVANCED_BRAVELY_BUT_GOT_SUCH_A_TINY_RESULT_HOHOHO;

	/**
	 * ID: 1000001<br>
	 * Message: A non-permitted target has been discovered.
	 */
	public static final NpcStringId A_NON_PERMITTED_TARGET_HAS_BEEN_DISCOVERED;

	/**
	 * ID: 1000002<br>
	 * Message: Intruder removal system initiated.
	 */
	public static final NpcStringId INTRUDER_REMOVAL_SYSTEM_INITIATED;

	/**
	 * ID: 1000003<br>
	 * Message: Removing intruders.
	 */
	public static final NpcStringId REMOVING_INTRUDERS;

	/**
	 * ID: 1000004<br>
	 * Message: A fatal error has occurred.
	 */
	public static final NpcStringId A_FATAL_ERROR_HAS_OCCURRED;

	/**
	 * ID: 1000005<br>
	 * Message: System is being shut down...
	 */
	public static final NpcStringId SYSTEM_IS_BEING_SHUT_DOWN;

	/**
	 * ID: 1000006<br>
	 * Message: ......
	 */
	public static final NpcStringId DOT_DOT_DOT_DOT_DOT_DOT;

	/**
	 * ID: 1000007<br>
	 * Message: We shall see about that!
	 */
	public static final NpcStringId WE_SHALL_SEE_ABOUT_THAT;

	/**
	 * ID: 1000008<br>
	 * Message: I will definitely repay this humiliation!
	 */
	public static final NpcStringId I_WILL_DEFINITELY_REPAY_THIS_HUMILIATION;

	/**
	 * ID: 1000009<br>
	 * Message: Retreat!
	 */
	public static final NpcStringId RETREAT;

	/**
	 * ID: 1000010<br>
	 * Message: Tactical retreat!
	 */
	public static final NpcStringId TACTICAL_RETREAT;

	/**
	 * ID: 1000011<br>
	 * Message: Mass fleeing!
	 */
	public static final NpcStringId MASS_FLEEING;

	/**
	 * ID: 1000012<br>
	 * Message: It's stronger than expected!
	 */
	public static final NpcStringId ITS_STRONGER_THAN_EXPECTED;

	/**
	 * ID: 1000013<br>
	 * Message: I'll kill you next time!
	 */
	public static final NpcStringId ILL_KILL_YOU_NEXT_TIME;

	/**
	 * ID: 1000014<br>
	 * Message: I'll definitely kill you next time!
	 */
	public static final NpcStringId ILL_DEFINITELY_KILL_YOU_NEXT_TIME;

	/**
	 * ID: 1000015<br>
	 * Message: Oh! How strong!
	 */
	public static final NpcStringId OH_HOW_STRONG;

	/**
	 * ID: 1000016<br>
	 * Message: Invader!
	 */
	public static final NpcStringId INVADER;

	/**
	 * ID: 1000017<br>
	 * Message: There is no reason for you to kill me! I have nothing you need!
	 */
	public static final NpcStringId THERE_IS_NO_REASON_FOR_YOU_TO_KILL_ME_I_HAVE_NOTHING_YOU_NEED;

	/**
	 * ID: 1000018<br>
	 * Message: Someday you will pay!
	 */
	public static final NpcStringId SOMEDAY_YOU_WILL_PAY;

	/**
	 * ID: 1000019<br>
	 * Message: I won't just stand still while you hit me.
	 */
	public static final NpcStringId I_WONT_JUST_STAND_STILL_WHILE_YOU_HIT_ME;

	/**
	 * ID: 1000020<br>
	 * Message: Stop hitting!
	 */
	public static final NpcStringId STOP_HITTING;

	/**
	 * ID: 1000021<br>
	 * Message: It hurts to the bone!
	 */
	public static final NpcStringId IT_HURTS_TO_THE_BONE;

	/**
	 * ID: 1000022<br>
	 * Message: Am I the neighborhood drum for beating!
	 */
	public static final NpcStringId AM_I_THE_NEIGHBORHOOD_DRUM_FOR_BEATING;

	/**
	 * ID: 1000023<br>
	 * Message: Follow me if you want!
	 */
	public static final NpcStringId FOLLOW_ME_IF_YOU_WANT;

	/**
	 * ID: 1000024<br>
	 * Message: Surrender!
	 */
	public static final NpcStringId SURRENDER;

	/**
	 * ID: 1000025<br>
	 * Message: Oh, I'm dead!
	 */
	public static final NpcStringId OH_IM_DEAD;

	/**
	 * ID: 1000026<br>
	 * Message: I'll be back!
	 */
	public static final NpcStringId ILL_BE_BACK;

	/**
	 * ID: 1000027<br>
	 * Message: I'll give you ten million arena if you let me live!
	 */
	public static final NpcStringId ILL_GIVE_YOU_TEN_MILLION_ARENA_IF_YOU_LET_ME_LIVE;

	/**
	 * ID: 1000028<br>
	 * Message: $s1. Stop kidding yourself about your own powerlessness!
	 */
	public static final NpcStringId S1_STOP_KIDDING_YOURSELF_ABOUT_YOUR_OWN_POWERLESSNESS;

	/**
	 * ID: 1000029<br>
	 * Message: $s1. I'll make you feel what true fear is!
	 */
	public static final NpcStringId S1_ILL_MAKE_YOU_FEEL_WHAT_TRUE_FEAR_IS;

	/**
	 * ID: 1000030<br>
	 * Message: You're really stupid to have challenged me. $s1! Get ready!
	 */
	public static final NpcStringId YOURE_REALLY_STUPID_TO_HAVE_CHALLENGED_ME_S1_GET_READY;

	/**
	 * ID: 1000031<br>
	 * Message: $s1. Do you think that's going to work?!
	 */
	public static final NpcStringId S1_DO_YOU_THINK_THATS_GOING_TO_WORK;

	/**
	 * ID: 1000032<br>
	 * Message: I will definitely reclaim my honor which has been tarnished!
	 */
	public static final NpcStringId I_WILL_DEFINITELY_RECLAIM_MY_HONOR_WHICH_HAS_BEEN_TARNISHED;

	/**
	 * ID: 1000033<br>
	 * Message: Show me the wrath of the knight whose honor has been downtrodden!
	 */
	public static final NpcStringId SHOW_ME_THE_WRATH_OF_THE_KNIGHT_WHOSE_HONOR_HAS_BEEN_DOWNTRODDEN;

	/**
	 * ID: 1000034<br>
	 * Message: Death to the hypocrite!
	 */
	public static final NpcStringId DEATH_TO_THE_HYPOCRITE;

	/**
	 * ID: 1000035<br>
	 * Message: I'll never sleep until I've shed my dishonor!
	 */
	public static final NpcStringId ILL_NEVER_SLEEP_UNTIL_IVE_SHED_MY_DISHONOR;

	/**
	 * ID: 1000036<br>
	 * Message: I'm here for the ones that are cursing the world!
	 */
	public static final NpcStringId IM_HERE_FOR_THE_ONES_THAT_ARE_CURSING_THE_WORLD;

	/**
	 * ID: 1000037<br>
	 * Message: I'll turn you into a malignant spirit!
	 */
	public static final NpcStringId ILL_TURN_YOU_INTO_A_MALIGNANT_SPIRIT;

	/**
	 * ID: 1000038<br>
	 * Message: I'll curse you with the power of revenge and hate!
	 */
	public static final NpcStringId ILL_CURSE_YOU_WITH_THE_POWER_OF_REVENGE_AND_HATE;

	/**
	 * ID: 1000039<br>
	 * Message: For the glory of Gracia!
	 */
	public static final NpcStringId FOR_THE_GLORY_OF_GRACIA;

	/**
	 * ID: 1000040<br>
	 * Message: Do you dare pit your power against me?
	 */
	public static final NpcStringId DO_YOU_DARE_PIT_YOUR_POWER_AGAINST_ME;

	/**
	 * ID: 1000041<br>
	 * Message: I... I am defeated!!!
	 */
	public static final NpcStringId I_I_AM_DEFEATED;

	/**
	 * ID: 1000042<br>
	 * Message: I am conveying the will of Nurka! Everybody get out of my way!
	 */
	public static final NpcStringId I_AM_CONVEYING_THE_WILL_OF_NURKA_EVERYBODY_GET_OUT_OF_MY_WAY;

	/**
	 * ID: 1000043<br>
	 * Message: Those who stand against me shall die horribly!
	 */
	public static final NpcStringId THOSE_WHO_STAND_AGAINST_ME_SHALL_DIE_HORRIBLY;

	/**
	 * ID: 1000044<br>
	 * Message: Do you dare to block my way?!
	 */
	public static final NpcStringId DO_YOU_DARE_TO_BLOCK_MY_WAY;

	/**
	 * ID: 1000045<br>
	 * Message: My comrades will get revenge!
	 */
	public static final NpcStringId MY_COMRADES_WILL_GET_REVENGE;

	/**
	 * ID: 1000046<br>
	 * Message: You heathen blasphemers of this holy place will be punished!
	 */
	public static final NpcStringId YOU_HEATHEN_BLASPHEMERS_OF_THIS_HOLY_PLACE_WILL_BE_PUNISHED;

	/**
	 * ID: 1000047<br>
	 * Message: Step forward, you worthless creatures who challenge my authority!
	 */
	public static final NpcStringId STEP_FORWARD_YOU_WORTHLESS_CREATURES_WHO_CHALLENGE_MY_AUTHORITY;

	/**
	 * ID: 1000048<br>
	 * Message: My creator... The unchanging faithfulness to my master...
	 */
	public static final NpcStringId MY_CREATOR_THE_UNCHANGING_FAITHFULNESS_TO_MY_MASTER;

	/**
	 * ID: 1000049<br>
	 * Message: Master of the tower... My master... master... Where is he?
	 */
	public static final NpcStringId MASTER_OF_THE_TOWER_MY_MASTER_MASTER_WHERE_IS_HE;

	/**
	 * ID: 1000050<br>
	 * Message: I AM THE ONE CARRYING OUT THE WILL OF CORE.
	 */
	public static final NpcStringId I_AM_THE_ONE_CARRYING_OUT_THE_WILL_OF_CORE;

	/**
	 * ID: 1000051<br>
	 * Message: DESTROY THE INVADER.
	 */
	public static final NpcStringId DESTROY_THE_INVADER;

	/**
	 * ID: 1000052<br>
	 * Message: STRANGE CONDITION - DOESN'T WORK
	 */
	public static final NpcStringId STRANGE_CONDITION_DOESNT_WORK;

	/**
	 * ID: 1000053<br>
	 * Message: According to the command of Beleth... I'm going to observe you guys!
	 */
	public static final NpcStringId ACCORDING_TO_THE_COMMAND_OF_BELETH_IM_GOING_TO_OBSERVE_YOU_GUYS;

	/**
	 * ID: 1000054<br>
	 * Message: You people make me sick! No sense of loyalty whatsoever!
	 */
	public static final NpcStringId YOU_PEOPLE_MAKE_ME_SICK_NO_SENSE_OF_LOYALTY_WHATSOEVER;

	/**
	 * ID: 1000055<br>
	 * Message: A challenge against me is the same as a challenge against Beleth...
	 */
	public static final NpcStringId A_CHALLENGE_AGAINST_ME_IS_THE_SAME_AS_A_CHALLENGE_AGAINST_BELETH;

	/**
	 * ID: 1000056<br>
	 * Message: Beleth is always watching over you guys!
	 */
	public static final NpcStringId BELETH_IS_ALWAYS_WATCHING_OVER_YOU_GUYS;

	/**
	 * ID: 1000057<br>
	 * Message: That was really close! Antharas opened its eyes!
	 */
	public static final NpcStringId THAT_WAS_REALLY_CLOSE_ANTHARAS_OPENED_ITS_EYES;

	/**
	 * ID: 1000058<br>
	 * Message: You who disobey the will of Antharas! Die!
	 */
	public static final NpcStringId YOU_WHO_DISOBEY_THE_WILL_OF_ANTHARAS_DIE;

	/**
	 * ID: 1000059<br>
	 * Message: Antharas has taken my life!
	 */
	public static final NpcStringId ANTHARAS_HAS_TAKEN_MY_LIFE;

	/**
	 * ID: 1000060<br>
	 * Message: I crossed back over the marshlands of death to reclaim the treasure!
	 */
	public static final NpcStringId I_CROSSED_BACK_OVER_THE_MARSHLANDS_OF_DEATH_TO_RECLAIM_THE_TREASURE;

	/**
	 * ID: 1000061<br>
	 * Message: Bring over and surrender your precious gold treasure to me!
	 */
	public static final NpcStringId BRING_OVER_AND_SURRENDER_YOUR_PRECIOUS_GOLD_TREASURE_TO_ME;

	/**
	 * ID: 1000062<br>
	 * Message: I'll kill you in an instant!
	 */
	public static final NpcStringId ILL_KILL_YOU_IN_AN_INSTANT;

	/**
	 * ID: 1000063<br>
	 * Message: No! The treasure is still..!
	 */
	public static final NpcStringId NO_THE_TREASURE_IS_STILL;

	/**
	 * ID: 1000064<br>
	 * Message: Invaders of Dragon Valley will never live to return!
	 */
	public static final NpcStringId INVADERS_OF_DRAGON_VALLEY_WILL_NEVER_LIVE_TO_RETURN;

	/**
	 * ID: 1000065<br>
	 * Message: I am the guardian that honors the command of Antharas to watch over this place!
	 */
	public static final NpcStringId I_AM_THE_GUARDIAN_THAT_HONORS_THE_COMMAND_OF_ANTHARAS_TO_WATCH_OVER_THIS_PLACE;

	/**
	 * ID: 1000066<br>
	 * Message: You've set foot in Dragon Valley without permission! The penalty is death!
	 */
	public static final NpcStringId YOUVE_SET_FOOT_IN_DRAGON_VALLEY_WITHOUT_PERMISSION_THE_PENALTY_IS_DEATH;

	/**
	 * ID: 1000068<br>
	 * Message: The joy of killing! The ecstasy of looting! Hey guys, let's have a go at it again!
	 */
	public static final NpcStringId THE_JOY_OF_KILLING_THE_ECSTASY_OF_LOOTING_HEY_GUYS_LETS_HAVE_A_GO_AT_IT_AGAIN;

	/**
	 * ID: 1000069<br>
	 * Message: There really are still lots of folks in the world without fear! I'll teach you a lesson!
	 */
	public static final NpcStringId
			THERE_REALLY_ARE_STILL_LOTS_OF_FOLKS_IN_THE_WORLD_WITHOUT_FEAR_ILL_TEACH_YOU_A_LESSON;

	/**
	 * ID: 1000070<br>
	 * Message: If you hand over everything you've got, I'll at least spare your life!
	 */
	public static final NpcStringId IF_YOU_HAND_OVER_EVERYTHING_YOUVE_GOT_ILL_AT_LEAST_SPARE_YOUR_LIFE;

	/**
	 * ID: 1000071<br>
	 * Message: Kneel down before one such as this!
	 */
	public static final NpcStringId KNEEL_DOWN_BEFORE_ONE_SUCH_AS_THIS;

	/**
	 * ID: 1000072<br>
	 * Message: Honor the master's wishes and punish all the invaders!
	 */
	public static final NpcStringId HONOR_THE_MASTERS_WISHES_AND_PUNISH_ALL_THE_INVADERS;

	/**
	 * ID: 1000073<br>
	 * Message: Follow the master's wishes and punish the invaders!
	 */
	public static final NpcStringId FOLLOW_THE_MASTERS_WISHES_AND_PUNISH_THE_INVADERS;

	/**
	 * ID: 1000074<br>
	 * Message: Death is nothing more than a momentary rest...
	 */
	public static final NpcStringId DEATH_IS_NOTHING_MORE_THAN_A_MOMENTARY_REST;

	/**
	 * ID: 1000075<br>
	 * Message: Listen! This is the end of the human era! Antharas has awoken!
	 */
	public static final NpcStringId LISTEN_THIS_IS_THE_END_OF_THE_HUMAN_ERA_ANTHARAS_HAS_AWOKEN;

	/**
	 * ID: 1000076<br>
	 * Message: Present the lives of four people to Antharas!
	 */
	public static final NpcStringId PRESENT_THE_LIVES_OF_FOUR_PEOPLE_TO_ANTHARAS;

	/**
	 * ID: 1000077<br>
	 * Message: This is unbelievable! How could I have lost to one so inferior to myself?
	 */
	public static final NpcStringId THIS_IS_UNBELIEVABLE_HOW_COULD_I_HAVE_LOST_TO_ONE_SO_INFERIOR_TO_MYSELF;

	/**
	 * ID: 1000078<br>
	 * Message: I carry the power of darkness and have returned from the abyss.
	 */
	public static final NpcStringId I_CARRY_THE_POWER_OF_DARKNESS_AND_HAVE_RETURNED_FROM_THE_ABYSS;

	/**
	 * ID: 1000079<br>
	 * Message: It's detestable.
	 */
	public static final NpcStringId ITS_DETESTABLE;

	/**
	 * ID: 1000080<br>
	 * Message: I finally find rest...
	 */
	public static final NpcStringId I_FINALLY_FIND_REST;

	/**
	 * ID: 1000081<br>
	 * Message: Glory to Orfen!
	 */
	public static final NpcStringId GLORY_TO_ORFEN;

	/**
	 * ID: 1000082<br>
	 * Message: In the name of Orfen, I can never forgive you who are invading this place!
	 */
	public static final NpcStringId IN_THE_NAME_OF_ORFEN_I_CAN_NEVER_FORGIVE_YOU_WHO_ARE_INVADING_THIS_PLACE;

	/**
	 * ID: 1000083<br>
	 * Message: I'll make you pay the price for fearlessly entering Orfen's land!
	 */
	public static final NpcStringId ILL_MAKE_YOU_PAY_THE_PRICE_FOR_FEARLESSLY_ENTERING_ORFENS_LAND;

	/**
	 * ID: 1000084<br>
	 * Message: Even if you disappear into nothingness, you will still face the life-long suffering of the curse that I have given you.
	 */
	public static final NpcStringId
			EVEN_IF_YOU_DISAPPEAR_INTO_NOTHINGNESS_YOU_WILL_STILL_FACE_THE_LIFE_LONG_SUFFERING_OF_THE_CURSE_THAT_I_HAVE_GIVEN_YOU;

	/**
	 * ID: 1000085<br>
	 * Message: I'll stand against anyone that makes light of the sacred place of the Elves!
	 */
	public static final NpcStringId ILL_STAND_AGAINST_ANYONE_THAT_MAKES_LIGHT_OF_THE_SACRED_PLACE_OF_THE_ELVES;

	/**
	 * ID: 1000086<br>
	 * Message: I will kill with my own hands anyone that defiles our home!
	 */
	public static final NpcStringId I_WILL_KILL_WITH_MY_OWN_HANDS_ANYONE_THAT_DEFILES_OUR_HOME;

	/**
	 * ID: 1000087<br>
	 * Message: My brothers will never rest until we push you and your gang out of this valley!
	 */
	public static final NpcStringId MY_BROTHERS_WILL_NEVER_REST_UNTIL_WE_PUSH_YOU_AND_YOUR_GANG_OUT_OF_THIS_VALLEY;

	/**
	 * ID: 1000088<br>
	 * Message: Until the day of destruction of Hestui!
	 */
	public static final NpcStringId UNTIL_THE_DAY_OF_DESTRUCTION_OF_HESTUI;

	/**
	 * ID: 1000089<br>
	 * Message: If any intrepid Orcs remain, attack them!
	 */
	public static final NpcStringId IF_ANY_INTREPID_ORCS_REMAIN_ATTACK_THEM;

	/**
	 * ID: 1000090<br>
	 * Message: I'll break your windpipe!
	 */
	public static final NpcStringId ILL_BREAK_YOUR_WINDPIPE;

	/**
	 * ID: 1000091<br>
	 * Message: Is revenge a failure?!
	 */
	public static final NpcStringId IS_REVENGE_A_FAILURE;

	/**
	 * ID: 1000092<br>
	 * Message: The sparkling mithril of the dwarves and their pretty treasures! I'll get them all!
	 */
	public static final NpcStringId THE_SPARKLING_MITHRIL_OF_THE_DWARVES_AND_THEIR_PRETTY_TREASURES_ILL_GET_THEM_ALL;

	/**
	 * ID: 1000093<br>
	 * Message: Where are all the dreadful dwarves and their sparkling things?
	 */
	public static final NpcStringId WHERE_ARE_ALL_THE_DREADFUL_DWARVES_AND_THEIR_SPARKLING_THINGS;

	/**
	 * ID: 1000094<br>
	 * Message: Hand over your pretty treasures!
	 */
	public static final NpcStringId HAND_OVER_YOUR_PRETTY_TREASURES;

	/**
	 * ID: 1000095<br>
	 * Message: Hey! You should have run away!
	 */
	public static final NpcStringId HEY_YOU_SHOULD_HAVE_RUN_AWAY;

	/**
	 * ID: 1000096<br>
	 * Message: DESTRUCTION - EXTINCTION - SLAUGHTER - COLLAPSE! DESTRUCTION - EXTINCTION - SLAUGHTER - COLLAPSE!
	 */
	public static final NpcStringId DESTRUCTION_EXTINCTION_SLAUGHTER_COLLAPSE_DESTRUCTION_EXTINCTION_SLAUGHTER_COLLAPSE;

	/**
	 * ID: 1000097<br>
	 * Message: Destruction! Destruction! Destruction! Destruction!
	 */
	public static final NpcStringId DESTRUCTION_DESTRUCTION_DESTRUCTION_DESTRUCTION;

	/**
	 * ID: 1000098<br>
	 * Message: Destruction! Destruction! Destruction. . .
	 */
	public static final NpcStringId DESTRUCTION_DESTRUCTION_DESTRUCTION;

	/**
	 * ID: 1000099<br>
	 * Message: Ta-da! Uthanka has returned!
	 */
	public static final NpcStringId TA_DA_UTHANKA_HAS_RETURNED;

	/**
	 * ID: 1000100<br>
	 * Message: Wah, ha, ha, ha! Uthanka has taken over this island today!
	 */
	public static final NpcStringId WAH_HA_HA_HA_UTHANKA_HAS_TAKEN_OVER_THIS_ISLAND_TODAY;

	/**
	 * ID: 1000101<br>
	 * Message: Whew! He's quite a guy!
	 */
	public static final NpcStringId WHEW_HES_QUITE_A_GUY;

	/**
	 * ID: 1000102<br>
	 * Message: How exasperating and unfair to have things happen in such a meaningless way like this...
	 */
	public static final NpcStringId
			HOW_EXASPERATING_AND_UNFAIR_TO_HAVE_THINGS_HAPPEN_IN_SUCH_A_MEANINGLESS_WAY_LIKE_THIS;

	/**
	 * ID: 1000103<br>
	 * Message: This world should be filled with fear and sadness...
	 */
	public static final NpcStringId THIS_WORLD_SHOULD_BE_FILLED_WITH_FEAR_AND_SADNESS;

	/**
	 * ID: 1000104<br>
	 * Message: I won't forgive the world that cursed me!
	 */
	public static final NpcStringId I_WONT_FORGIVE_THE_WORLD_THAT_CURSED_ME;

	/**
	 * ID: 1000105<br>
	 * Message: I'll make everyone feel the same suffering as me!
	 */
	public static final NpcStringId ILL_MAKE_EVERYONE_FEEL_THE_SAME_SUFFERING_AS_ME;

	/**
	 * ID: 1000106<br>
	 * Message: I'll give you a curse that you'll never be able to remove forever!
	 */
	public static final NpcStringId ILL_GIVE_YOU_A_CURSE_THAT_YOULL_NEVER_BE_ABLE_TO_REMOVE_FOREVER;

	/**
	 * ID: 1000107<br>
	 * Message: I'll get revenge on you who slaughtered my compatriots!
	 */
	public static final NpcStringId ILL_GET_REVENGE_ON_YOU_WHO_SLAUGHTERED_MY_COMPATRIOTS;

	/**
	 * ID: 1000108<br>
	 * Message: Those who are afraid should get away and those who are brave should fight!
	 */
	public static final NpcStringId THOSE_WHO_ARE_AFRAID_SHOULD_GET_AWAY_AND_THOSE_WHO_ARE_BRAVE_SHOULD_FIGHT;

	/**
	 * ID: 1000109<br>
	 * Message: I've got power from Beleth so do you think I'll be easily defeated?!
	 */
	public static final NpcStringId IVE_GOT_POWER_FROM_BELETH_SO_DO_YOU_THINK_ILL_BE_EASILY_DEFEATED;

	/**
	 * ID: 1000110<br>
	 * Message: I am leaving now, but soon someone will come who will teach you all a lesson!
	 */
	public static final NpcStringId I_AM_LEAVING_NOW_BUT_SOON_SOMEONE_WILL_COME_WHO_WILL_TEACH_YOU_ALL_A_LESSON;

	/**
	 * ID: 1000111<br>
	 * Message: Hey guys, let's make a round of our territory!
	 */
	public static final NpcStringId HEY_GUYS_LETS_MAKE_A_ROUND_OF_OUR_TERRITORY;

	/**
	 * ID: 1000112<br>
	 * Message: The rumor is that there are wild, uncivilized ruffians who have recently arrived in my territory.
	 */
	public static final NpcStringId
			THE_RUMOR_IS_THAT_THERE_ARE_WILD_UNCIVILIZED_RUFFIANS_WHO_HAVE_RECENTLY_ARRIVED_IN_MY_TERRITORY;

	/**
	 * ID: 1000113<br>
	 * Message: Do you know who I am?! I am Sirocco! Everyone, attack!
	 */
	public static final NpcStringId DO_YOU_KNOW_WHO_I_AM_I_AM_SIROCCO_EVERYONE_ATTACK;

	/**
	 * ID: 1000114<br>
	 * Message: What's just happened?! The invincible Sirocco was defeated by someone like you?!
	 */
	public static final NpcStringId WHATS_JUST_HAPPENED_THE_INVINCIBLE_SIROCCO_WAS_DEFEATED_BY_SOMEONE_LIKE_YOU;

	/**
	 * ID: 1000115<br>
	 * Message: Oh, I'm really hungry...
	 */
	public static final NpcStringId OH_IM_REALLY_HUNGRY;

	/**
	 * ID: 1000116<br>
	 * Message: I smell food. Ooh...
	 */
	public static final NpcStringId I_SMELL_FOOD_OOH;

	/**
	 * ID: 1000117<br>
	 * Message: Ooh...
	 */
	public static final NpcStringId OOH;

	/**
	 * ID: 1000118<br>
	 * Message: What does honey of this place taste like?!
	 */
	public static final NpcStringId WHAT_DOES_HONEY_OF_THIS_PLACE_TASTE_LIKE;

	/**
	 * ID: 1000119<br>
	 * Message: Give me some sweet, delicious golden honey!
	 */
	public static final NpcStringId GIVE_ME_SOME_SWEET_DELICIOUS_GOLDEN_HONEY;

	/**
	 * ID: 1000120<br>
	 * Message: If you give me some honey, I'll at least spare your life...
	 */
	public static final NpcStringId IF_YOU_GIVE_ME_SOME_HONEY_ILL_AT_LEAST_SPARE_YOUR_LIFE;

	/**
	 * ID: 1000121<br>
	 * Message: Only for lack of honey did I lose to the likes of you.
	 */
	public static final NpcStringId ONLY_FOR_LACK_OF_HONEY_DID_I_LOSE_TO_THE_LIKES_OF_YOU;

	/**
	 * ID: 1000122<br>
	 * Message: Where is the traitor Kuroboros!?
	 */
	public static final NpcStringId WHERE_IS_THE_TRAITOR_KUROBOROS;

	/**
	 * ID: 1000123<br>
	 * Message: Look in every nook and cranny around here!
	 */
	public static final NpcStringId LOOK_IN_EVERY_NOOK_AND_CRANNY_AROUND_HERE;

	/**
	 * ID: 1000124<br>
	 * Message: Are you Lackey of Kuroboros?! I'll knock you out in one shot!
	 */
	public static final NpcStringId ARE_YOU_LACKEY_OF_KUROBOROS_ILL_KNOCK_YOU_OUT_IN_ONE_SHOT;

	/**
	 * ID: 1000125<br>
	 * Message: He just closed his eyes without disposing of the traitor... How unfair!
	 */
	public static final NpcStringId HE_JUST_CLOSED_HIS_EYES_WITHOUT_DISPOSING_OF_THE_TRAITOR_HOW_UNFAIR;

	/**
	 * ID: 1000126<br>
	 * Message: Hell for unbelievers in Kuroboros!
	 */
	public static final NpcStringId HELL_FOR_UNBELIEVERS_IN_KUROBOROS;

	/**
	 * ID: 1000127<br>
	 * Message: The person that does not believe in kuroboros, his life will soon become hell!
	 */
	public static final NpcStringId THE_PERSON_THAT_DOES_NOT_BELIEVE_IN_KUROBOROS_HIS_LIFE_WILL_SOON_BECOME_HELL;

	/**
	 * ID: 1000128<br>
	 * Message: The lackey of that demented devil, the servant of a false god! I'll send that fool straight to hell!
	 */
	public static final NpcStringId
			THE_LACKEY_OF_THAT_DEMENTED_DEVIL_THE_SERVANT_OF_A_FALSE_GOD_ILL_SEND_THAT_FOOL_STRAIGHT_TO_HELL;

	/**
	 * ID: 1000129<br>
	 * Message: Uh... I'm not dying; I'm just disappearing for a moment... I'll resurrect again!
	 */
	public static final NpcStringId UH_IM_NOT_DYING_IM_JUST_DISAPPEARING_FOR_A_MOMENT_ILL_RESURRECT_AGAIN;

	/**
	 * ID: 1000130<br>
	 * Message: Hail to Kuroboros, the founder of our religion!
	 */
	public static final NpcStringId HAIL_TO_KUROBOROS_THE_FOUNDER_OF_OUR_RELIGION;

	/**
	 * ID: 1000131<br>
	 * Message: Only those who believe in Patriarch Kuroboros shall receive salvation!
	 */
	public static final NpcStringId ONLY_THOSE_WHO_BELIEVE_IN_PATRIARCH_KUROBOROS_SHALL_RECEIVE_SALVATION;

	/**
	 * ID: 1000132<br>
	 * Message: Are you the ones that Sharuk has incited?! You also should trust in Kuroboros and be saved!
	 */
	public static final NpcStringId
			ARE_YOU_THE_ONES_THAT_SHARUK_HAS_INCITED_YOU_ALSO_SHOULD_TRUST_IN_KUROBOROS_AND_BE_SAVED;

	/**
	 * ID: 1000133<br>
	 * Message: Kuroboros will punish you.
	 */
	public static final NpcStringId KUROBOROS_WILL_PUNISH_YOU;

	/**
	 * ID: 1000134<br>
	 * Message: You who have beautiful spirits that shine brightly! I have returned!
	 */
	public static final NpcStringId YOU_WHO_HAVE_BEAUTIFUL_SPIRITS_THAT_SHINE_BRIGHTLY_I_HAVE_RETURNED;

	/**
	 * ID: 1000135<br>
	 * Message: You that are weary and exhausted... Entrust your souls to me.
	 */
	public static final NpcStringId YOU_THAT_ARE_WEARY_AND_EXHAUSTED_ENTRUST_YOUR_SOULS_TO_ME;

	/**
	 * ID: 1000136<br>
	 * Message: The color of your soul is very attractive.
	 */
	public static final NpcStringId THE_COLOR_OF_YOUR_SOUL_IS_VERY_ATTRACTIVE;

	/**
	 * ID: 1000137<br>
	 * Message: Those of you who live! Do you know how beautiful your souls are?!
	 */
	public static final NpcStringId THOSE_OF_YOU_WHO_LIVE_DO_YOU_KNOW_HOW_BEAUTIFUL_YOUR_SOULS_ARE;

	/**
	 * ID: 1000138<br>
	 * Message: It... will... kill... everyone...
	 */
	public static final NpcStringId IT_WILL_KILL_EVERYONE;

	/**
	 * ID: 1000139<br>
	 * Message: I'm... so... lonely...
	 */
	public static final NpcStringId IM_SO_LONELY;

	/**
	 * ID: 1000140<br>
	 * Message: My... enemy...!
	 */
	public static final NpcStringId MY_ENEMY;

	/**
	 * ID: 1000141<br>
	 * Message: ... Now... I'm not so lonely!
	 */
	public static final NpcStringId _NOW_IM_NOT_SO_LONELY;

	/**
	 * ID: 1000142<br>
	 * Message: I will never forgive the Pixy Murika... that is trying to... kill us!
	 */
	public static final NpcStringId I_WILL_NEVER_FORGIVE_THE_PIXY_MURIKA_THAT_IS_TRYING_TO_KILL_US;

	/**
	 * ID: 1000143<br>
	 * Message: Attack all the dull and stupid followers of Murika!
	 */
	public static final NpcStringId ATTACK_ALL_THE_DULL_AND_STUPID_FOLLOWERS_OF_MURIKA;

	/**
	 * ID: 1000144<br>
	 * Message: I didn't have any idea about such ambitions!
	 */
	public static final NpcStringId I_DIDNT_HAVE_ANY_IDEA_ABOUT_SUCH_AMBITIONS;

	/**
	 * ID: 1000145<br>
	 * Message: This is not the end... It's just the beginning.
	 */
	public static final NpcStringId THIS_IS_NOT_THE_END_ITS_JUST_THE_BEGINNING;

	/**
	 * ID: 1000146<br>
	 * Message: Hey... Shall we have some fun for the first time in a long while?...
	 */
	public static final NpcStringId HEY_SHALL_WE_HAVE_SOME_FUN_FOR_THE_FIRST_TIME_IN_A_LONG_WHILE;

	/**
	 * ID: 1000147<br>
	 * Message: There've been some things going around like crazy here recently...
	 */
	public static final NpcStringId THEREVE_BEEN_SOME_THINGS_GOING_AROUND_LIKE_CRAZY_HERE_RECENTLY;

	/**
	 * ID: 1000148<br>
	 * Message: Hey! Do you know who I am? I am Malex, Herald of Dagoniel! Attack!
	 */
	public static final NpcStringId HEY_DO_YOU_KNOW_WHO_I_AM_I_AM_MALEX_HERALD_OF_DAGONIEL_ATTACK;

	/**
	 * ID: 1000149<br>
	 * Message: What's just happened?! The invincible Malex just lost to the likes of you?!
	 */
	public static final NpcStringId WHATS_JUST_HAPPENED_THE_INVINCIBLE_MALEX_JUST_LOST_TO_THE_LIKES_OF_YOU;

	/**
	 * ID: 1000150<br>
	 * Message: It's something repeated in a vain life...
	 */
	public static final NpcStringId ITS_SOMETHING_REPEATED_IN_A_VAIN_LIFE;

	/**
	 * ID: 1000151<br>
	 * Message: Shake in fear, all you who value your lives!
	 */
	public static final NpcStringId SHAKE_IN_FEAR_ALL_YOU_WHO_VALUE_YOUR_LIVES;

	/**
	 * ID: 1000152<br>
	 * Message: I'll make you feel suffering like a flame that is never extinguished!
	 */
	public static final NpcStringId ILL_MAKE_YOU_FEEL_SUFFERING_LIKE_A_FLAME_THAT_IS_NEVER_EXTINGUISHED;

	/**
	 * ID: 1000153<br>
	 * Message: Back to the dirt...
	 */
	public static final NpcStringId BACK_TO_THE_DIRT;

	/**
	 * ID: 1000154<br>
	 * Message: Hail Varika!!
	 */
	public static final NpcStringId HAIL_VARIKA;

	/**
	 * ID: 1000155<br>
	 * Message: Nobody can stop us!
	 */
	public static final NpcStringId NOBODY_CAN_STOP_US;

	/**
	 * ID: 1000156<br>
	 * Message: You move slowly!
	 */
	public static final NpcStringId YOU_MOVE_SLOWLY;

	/**
	 * ID: 1000157<br>
	 * Message: Varika! Go first!
	 */
	public static final NpcStringId VARIKA_GO_FIRST;

	/**
	 * ID: 1000158<br>
	 * Message: Where am I? Who am I?
	 */
	public static final NpcStringId WHERE_AM_I_WHO_AM_I;

	/**
	 * ID: 1000159<br>
	 * Message: Uh... My head hurts like it's going to burst! Who am I?
	 */
	public static final NpcStringId UH_MY_HEAD_HURTS_LIKE_ITS_GOING_TO_BURST_WHO_AM_I;

	/**
	 * ID: 1000160<br>
	 * Message: You jerk. You're a devil! You're a devil to have made me like this!
	 */
	public static final NpcStringId YOU_JERK_YOURE_A_DEVIL_YOURE_A_DEVIL_TO_HAVE_MADE_ME_LIKE_THIS;

	/**
	 * ID: 1000161<br>
	 * Message: Where am I? What happened? Thank you!
	 */
	public static final NpcStringId WHERE_AM_I_WHAT_HAPPENED_THANK_YOU;

	/**
	 * ID: 1000162<br>
	 * Message: Ukru Master!
	 */
	public static final NpcStringId UKRU_MASTER;

	/**
	 * ID: 1000163<br>
	 * Message: Are you Matu?
	 */
	public static final NpcStringId ARE_YOU_MATU;

	/**
	 * ID: 1000164<br>
	 * Message: Marak! Tubarin! Sabaracha!
	 */
	public static final NpcStringId MARAK_TUBARIN_SABARACHA;

	/**
	 * ID: 1000165<br>
	 * Message: Pa'agrio Tama!
	 */
	public static final NpcStringId PAAGRIO_TAMA;

	/**
	 * ID: 1000166<br>
	 * Message: Accept the will of Icarus!
	 */
	public static final NpcStringId ACCEPT_THE_WILL_OF_ICARUS;

	/**
	 * ID: 1000167<br>
	 * Message: The people who are blocking my way will not be forgiven...
	 */
	public static final NpcStringId THE_PEOPLE_WHO_ARE_BLOCKING_MY_WAY_WILL_NOT_BE_FORGIVEN;

	/**
	 * ID: 1000168<br>
	 * Message: You are scum.
	 */
	public static final NpcStringId YOU_ARE_SCUM;

	/**
	 * ID: 1000169<br>
	 * Message: You lack power.
	 */
	public static final NpcStringId YOU_LACK_POWER;

	/**
	 * ID: 1000170<br>
	 * Message: Return
	 */
	public static final NpcStringId RETURN;

	/**
	 * ID: 1000171<br>
	 * Message: Adena has been transferred.
	 */
	public static final NpcStringId ADENA_HAS_BEEN_TRANSFERRED;

	/**
	 * ID: 1000172<br>
	 * Message: Event Number
	 */
	public static final NpcStringId EVENT_NUMBER;

	/**
	 * ID: 1000173<br>
	 * Message: First Prize
	 */
	public static final NpcStringId FIRST_PRIZE;

	/**
	 * ID: 1000174<br>
	 * Message: Second Prize
	 */
	public static final NpcStringId SECOND_PRIZE;

	/**
	 * ID: 1000175<br>
	 * Message: Third Prize
	 */
	public static final NpcStringId THIRD_PRIZE;

	/**
	 * ID: 1000176<br>
	 * Message: Fourth Prize
	 */
	public static final NpcStringId FOURTH_PRIZE;

	/**
	 * ID: 1000177<br>
	 * Message: There has been no winning lottery ticket.
	 */
	public static final NpcStringId THERE_HAS_BEEN_NO_WINNING_LOTTERY_TICKET;

	/**
	 * ID: 1000178<br>
	 * Message: The most recent winning lottery numbers
	 */
	public static final NpcStringId THE_MOST_RECENT_WINNING_LOTTERY_NUMBERS;

	/**
	 * ID: 1000179<br>
	 * Message: Your lucky numbers have been selected above.
	 */
	public static final NpcStringId YOUR_LUCKY_NUMBERS_HAVE_BEEN_SELECTED_ABOVE;

	/**
	 * ID: 1000180<br>
	 * Message: I wonder who it is that is lurking about..
	 */
	public static final NpcStringId I_WONDER_WHO_IT_IS_THAT_IS_LURKING_ABOUT;

	/**
	 * ID: 1000181<br>
	 * Message: Sacred magical research is conducted here.
	 */
	public static final NpcStringId SACRED_MAGICAL_RESEARCH_IS_CONDUCTED_HERE;

	/**
	 * ID: 1000182<br>
	 * Message: Behold the awesome power of magic!
	 */
	public static final NpcStringId BEHOLD_THE_AWESOME_POWER_OF_MAGIC;

	/**
	 * ID: 1000183<br>
	 * Message: Your powers are impressive but you must not annoy our high level sorcerer.
	 */
	public static final NpcStringId YOUR_POWERS_ARE_IMPRESSIVE_BUT_YOU_MUST_NOT_ANNOY_OUR_HIGH_LEVEL_SORCERER;

	/**
	 * ID: 1000184<br>
	 * Message: I am Barda, master of the Bandit Stronghold!
	 */
	public static final NpcStringId I_AM_BARDA_MASTER_OF_THE_BANDIT_STRONGHOLD;

	/**
	 * ID: 1000185<br>
	 * Message: I, Master Barda, once owned that stronghold,
	 */
	public static final NpcStringId I_MASTER_BARDA_ONCE_OWNED_THAT_STRONGHOLD;

	/**
	 * ID: 1000186<br>
	 * Message: Ah, very interesting!
	 */
	public static final NpcStringId AH_VERY_INTERESTING;

	/**
	 * ID: 1000187<br>
	 * Message: You are more powerful than you appear. We'll meet again!
	 */
	public static final NpcStringId YOU_ARE_MORE_POWERFUL_THAN_YOU_APPEAR_WELL_MEET_AGAIN;

	/**
	 * ID: 1000188<br>
	 * Message: You filthy sorcerers disgust me!
	 */
	public static final NpcStringId YOU_FILTHY_SORCERERS_DISGUST_ME;

	/**
	 * ID: 1000189<br>
	 * Message: Why would you build a tower in our territory?
	 */
	public static final NpcStringId WHY_WOULD_YOU_BUILD_A_TOWER_IN_OUR_TERRITORY;

	/**
	 * ID: 1000190<br>
	 * Message: Are you part of that evil gang of sorcerers?
	 */
	public static final NpcStringId ARE_YOU_PART_OF_THAT_EVIL_GANG_OF_SORCERERS;

	/**
	 * ID: 1000191<br>
	 * Message: That is why I don't bother with anyone below the level of sorcerer.
	 */
	public static final NpcStringId THAT_IS_WHY_I_DONT_BOTHER_WITH_ANYONE_BELOW_THE_LEVEL_OF_SORCERER;

	/**
	 * ID: 1000192<br>
	 * Message: Ah, another beautiful day!
	 */
	public static final NpcStringId AH_ANOTHER_BEAUTIFUL_DAY;

	/**
	 * ID: 1000193<br>
	 * Message: Our specialties are arson and looting.
	 */
	public static final NpcStringId OUR_SPECIALTIES_ARE_ARSON_AND_LOOTING;

	/**
	 * ID: 1000194<br>
	 * Message: You will leave empty-handed!
	 */
	public static final NpcStringId YOU_WILL_LEAVE_EMPTY_HANDED;

	/**
	 * ID: 1000195<br>
	 * Message: Ah, so you admire my treasure, do you? Try finding it! Ha!
	 */
	public static final NpcStringId AH_SO_YOU_ADMIRE_MY_TREASURE_DO_YOU_TRY_FINDING_IT_HA;

	/**
	 * ID: 1000196<br>
	 * Message: Is everybody listening? Sirion has come back. Everyone chant and bow...
	 */
	public static final NpcStringId IS_EVERYBODY_LISTENING_SIRION_HAS_COME_BACK_EVERYONE_CHANT_AND_BOW;

	/**
	 * ID: 1000197<br>
	 * Message: Bow down, you worthless humans!
	 */
	public static final NpcStringId BOW_DOWN_YOU_WORTHLESS_HUMANS;

	/**
	 * ID: 1000198<br>
	 * Message: Very tacky!
	 */
	public static final NpcStringId VERY_TACKY;

	/**
	 * ID: 1000199<br>
	 * Message: Don't think that you are invincible just because you defeated me!
	 */
	public static final NpcStringId DONT_THINK_THAT_YOU_ARE_INVINCIBLE_JUST_BECAUSE_YOU_DEFEATED_ME;

	/**
	 * ID: 1000200<br>
	 * Message: The material desires of mortals are ultimately meaningless.
	 */
	public static final NpcStringId THE_MATERIAL_DESIRES_OF_MORTALS_ARE_ULTIMATELY_MEANINGLESS;

	/**
	 * ID: 1000201<br>
	 * Message: Do not forget the reason the Tower of Insolence collapsed.
	 */
	public static final NpcStringId DO_NOT_FORGET_THE_REASON_THE_TOWER_OF_INSOLENCE_COLLAPSED;

	/**
	 * ID: 1000202<br>
	 * Message: You humans are all alike, full of greed and avarice!
	 */
	public static final NpcStringId YOU_HUMANS_ARE_ALL_ALIKE_FULL_OF_GREED_AND_AVARICE;

	/**
	 * ID: 1000203<br>
	 * Message: All for nothing,
	 */
	public static final NpcStringId ALL_FOR_NOTHING;

	/**
	 * ID: 1000204<br>
	 * Message: What are all these people doing here?
	 */
	public static final NpcStringId WHAT_ARE_ALL_THESE_PEOPLE_DOING_HERE;

	/**
	 * ID: 1000205<br>
	 * Message: I must find the secret of eternal life, here among these rotten angels!
	 */
	public static final NpcStringId I_MUST_FIND_THE_SECRET_OF_ETERNAL_LIFE_HERE_AMONG_THESE_ROTTEN_ANGELS;

	/**
	 * ID: 1000206<br>
	 * Message: Do you also seek the secret of immortality?
	 */
	public static final NpcStringId DO_YOU_ALSO_SEEK_THE_SECRET_OF_IMMORTALITY;

	/**
	 * ID: 1000207<br>
	 * Message: I shall never reveal my secrets!
	 */
	public static final NpcStringId I_SHALL_NEVER_REVEAL_MY_SECRETS;

	/**
	 * ID: 1000208<br>
	 * Message: Who dares enter this place?
	 */
	public static final NpcStringId WHO_DARES_ENTER_THIS_PLACE;

	/**
	 * ID: 1000209<br>
	 * Message: This is no place for humans! You must leave immediately.
	 */
	public static final NpcStringId THIS_IS_NO_PLACE_FOR_HUMANS_YOU_MUST_LEAVE_IMMEDIATELY;

	/**
	 * ID: 1000210<br>
	 * Message: You poor creatures! Too stupid to realize your own ignorance!
	 */
	public static final NpcStringId YOU_POOR_CREATURES_TOO_STUPID_TO_REALIZE_YOUR_OWN_IGNORANCE;

	/**
	 * ID: 1000211<br>
	 * Message: You mustn't go there!
	 */
	public static final NpcStringId YOU_MUSTNT_GO_THERE;

	/**
	 * ID: 1000212<br>
	 * Message: Who dares disturb this marsh?
	 */
	public static final NpcStringId WHO_DARES_DISTURB_THIS_MARSH;

	/**
	 * ID: 1000213<br>
	 * Message: The humans must not be allowed to destroy the marshland for their greedy purposes.
	 */
	public static final NpcStringId THE_HUMANS_MUST_NOT_BE_ALLOWED_TO_DESTROY_THE_MARSHLAND_FOR_THEIR_GREEDY_PURPOSES;

	/**
	 * ID: 1000214<br>
	 * Message: You are a brave man...
	 */
	public static final NpcStringId YOU_ARE_A_BRAVE_MAN;

	/**
	 * ID: 1000215<br>
	 * Message: You idiots! Some day you shall also be gone!
	 */
	public static final NpcStringId YOU_IDIOTS_SOME_DAY_YOU_SHALL_ALSO_BE_GONE;

	/**
	 * ID: 1000216<br>
	 * Message: Someone has entered the forest...
	 */
	public static final NpcStringId SOMEONE_HAS_ENTERED_THE_FOREST;

	/**
	 * ID: 1000217<br>
	 * Message: The forest is very quiet and peaceful.
	 */
	public static final NpcStringId THE_FOREST_IS_VERY_QUIET_AND_PEACEFUL;

	/**
	 * ID: 1000218<br>
	 * Message: Stay here in this wonderful forest!
	 */
	public static final NpcStringId STAY_HERE_IN_THIS_WONDERFUL_FOREST;

	/**
	 * ID: 1000219<br>
	 * Message: My... my souls...
	 */
	public static final NpcStringId MY_MY_SOULS;

	/**
	 * ID: 1000220<br>
	 * Message: This forest is a dangerous place.
	 */
	public static final NpcStringId THIS_FOREST_IS_A_DANGEROUS_PLACE;

	/**
	 * ID: 1000221<br>
	 * Message: Unless you leave this forest immediately you are bound to run into serious trouble.
	 */
	public static final NpcStringId UNLESS_YOU_LEAVE_THIS_FOREST_IMMEDIATELY_YOU_ARE_BOUND_TO_RUN_INTO_SERIOUS_TROUBLE;

	/**
	 * ID: 1000222<br>
	 * Message: Leave now!
	 */
	public static final NpcStringId LEAVE_NOW;

	/**
	 * ID: 1000223<br>
	 * Message: Why do you ignore my warning?
	 */
	public static final NpcStringId WHY_DO_YOU_IGNORE_MY_WARNING;

	/**
	 * ID: 1000224<br>
	 * Message: Harits of the world... I bring you peace!
	 */
	public static final NpcStringId HARITS_OF_THE_WORLD_I_BRING_YOU_PEACE;

	/**
	 * ID: 1000225<br>
	 * Message: Harits! Be courageous!
	 */
	public static final NpcStringId HARITS_BE_COURAGEOUS;

	/**
	 * ID: 1000226<br>
	 * Message: I shall eat your still-beating heart!.
	 */
	public static final NpcStringId I_SHALL_EAT_YOUR_STILL_BEATING_HEART;

	/**
	 * ID: 1000227<br>
	 * Message: Harits! Keep faith until the day I return... Never lose hope!
	 */
	public static final NpcStringId HARITS_KEEP_FAITH_UNTIL_THE_DAY_I_RETURN_NEVER_LOSE_HOPE;

	/**
	 * ID: 1000228<br>
	 * Message: Even the giants are gone! There's nothing left to be afraid of now!
	 */
	public static final NpcStringId EVEN_THE_GIANTS_ARE_GONE_THERES_NOTHING_LEFT_TO_BE_AFRAID_OF_NOW;

	/**
	 * ID: 1000229<br>
	 * Message: Have you heard of the giants? Their downfall was inevitable!
	 */
	public static final NpcStringId HAVE_YOU_HEARD_OF_THE_GIANTS_THEIR_DOWNFALL_WAS_INEVITABLE;

	/**
	 * ID: 1000230<br>
	 * Message: What nerve! Do you dare to challenge me?
	 */
	public static final NpcStringId WHAT_NERVE_DO_YOU_DARE_TO_CHALLENGE_ME;

	/**
	 * ID: 1000231<br>
	 * Message: You are as evil as the giants...
	 */
	public static final NpcStringId YOU_ARE_AS_EVIL_AS_THE_GIANTS;

	/**
	 * ID: 1000232<br>
	 * Message: This dungeon is still in good condition!
	 */
	public static final NpcStringId THIS_DUNGEON_IS_STILL_IN_GOOD_CONDITION;

	/**
	 * ID: 1000233<br>
	 * Message: This place is spectacular, wouldn't you say?
	 */
	public static final NpcStringId THIS_PLACE_IS_SPECTACULAR_WOULDNT_YOU_SAY;

	/**
	 * ID: 1000234<br>
	 * Message: You are very brave warriors!
	 */
	public static final NpcStringId YOU_ARE_VERY_BRAVE_WARRIORS;

	/**
	 * ID: 1000235<br>
	 * Message: Are the giants truly gone for good?
	 */
	public static final NpcStringId ARE_THE_GIANTS_TRULY_GONE_FOR_GOOD;

	/**
	 * ID: 1000236<br>
	 * Message: These graves are good.
	 */
	public static final NpcStringId THESE_GRAVES_ARE_GOOD;

	/**
	 * ID: 1000237<br>
	 * Message: Gold and silver are meaningless to a dead man!
	 */
	public static final NpcStringId GOLD_AND_SILVER_ARE_MEANINGLESS_TO_A_DEAD_MAN;

	/**
	 * ID: 1000238<br>
	 * Message: Why would those corrupt aristocrats bury such useful things?
	 */
	public static final NpcStringId WHY_WOULD_THOSE_CORRUPT_ARISTOCRATS_BURY_SUCH_USEFUL_THINGS;

	/**
	 * ID: 1000239<br>
	 * Message: You filthy pig! Eat and be merry now that you have shirked your responsibilities!
	 */
	public static final NpcStringId YOU_FILTHY_PIG_EAT_AND_BE_MERRY_NOW_THAT_YOU_HAVE_SHIRKED_YOUR_RESPONSIBILITIES;

	/**
	 * ID: 1000240<br>
	 * Message: Those thugs! It would be too merciful to rip them apart and chew them up one at a time!
	 */
	public static final NpcStringId
			THOSE_THUGS_IT_WOULD_BE_TOO_MERCIFUL_TO_RIP_THEM_APART_AND_CHEW_THEM_UP_ONE_AT_A_TIME;

	/**
	 * ID: 1000241<br>
	 * Message: You accursed scoundrels!
	 */
	public static final NpcStringId YOU_ACCURSED_SCOUNDRELS;

	/**
	 * ID: 1000242<br>
	 * Message: Hmm, could this be the assassin sent by those idiots from Aden?
	 */
	public static final NpcStringId HMM_COULD_THIS_BE_THE_ASSASSIN_SENT_BY_THOSE_IDIOTS_FROM_ADEN;

	/**
	 * ID: 1000243<br>
	 * Message: I shall curse your name with my last breath!
	 */
	public static final NpcStringId I_SHALL_CURSE_YOUR_NAME_WITH_MY_LAST_BREATH;

	/**
	 * ID: 1000244<br>
	 * Message: My beloved Lord Shilen.
	 */
	public static final NpcStringId MY_BELOVED_LORD_SHILEN;

	/**
	 * ID: 1000245<br>
	 * Message: I must break the seal and release Lord Shilen as soon as possible...
	 */
	public static final NpcStringId I_MUST_BREAK_THE_SEAL_AND_RELEASE_LORD_SHILEN_AS_SOON_AS_POSSIBLE;

	/**
	 * ID: 1000246<br>
	 * Message: You shall taste the vengeance of Lord Shilen!
	 */
	public static final NpcStringId YOU_SHALL_TASTE_THE_VENGEANCE_OF_LORD_SHILEN;

	/**
	 * ID: 1000247<br>
	 * Message: Lord Shilen... some day... you will accomplish... this mission...
	 */
	public static final NpcStringId LORD_SHILEN_SOME_DAY_YOU_WILL_ACCOMPLISH_THIS_MISSION;

	/**
	 * ID: 1000248<br>
	 * Message: Towards immortality...
	 */
	public static final NpcStringId TOWARDS_IMMORTALITY;

	/**
	 * ID: 1000249<br>
	 * Message: He who desires immortality... Come unto me.
	 */
	public static final NpcStringId HE_WHO_DESIRES_IMMORTALITY_COME_UNTO_ME;

	/**
	 * ID: 1000250<br>
	 * Message: You shall be sacrificed to gain my immortality!
	 */
	public static final NpcStringId YOU_SHALL_BE_SACRIFICED_TO_GAIN_MY_IMMORTALITY;

	/**
	 * ID: 1000251<br>
	 * Message: Eternal life in front of my eyes... I have collapsed in such a worthless way like this...
	 */
	public static final NpcStringId ETERNAL_LIFE_IN_FRONT_OF_MY_EYES_I_HAVE_COLLAPSED_IN_SUCH_A_WORTHLESS_WAY_LIKE_THIS;

	/**
	 * ID: 1000252<br>
	 * Message: Zaken, you are a cowardly cur!
	 */
	public static final NpcStringId ZAKEN_YOU_ARE_A_COWARDLY_CUR;

	/**
	 * ID: 1000253<br>
	 * Message: You are immortal, aren't you, Zaken?
	 */
	public static final NpcStringId YOU_ARE_IMMORTAL_ARENT_YOU_ZAKEN;

	/**
	 * ID: 1000254<br>
	 * Message: Please return my body to me.
	 */
	public static final NpcStringId PLEASE_RETURN_MY_BODY_TO_ME;

	/**
	 * ID: 1000255<br>
	 * Message: Finally... will I be able to rest?
	 */
	public static final NpcStringId FINALLY_WILL_I_BE_ABLE_TO_REST;

	/**
	 * ID: 1000256<br>
	 * Message: What is all that racket?
	 */
	public static final NpcStringId WHAT_IS_ALL_THAT_RACKET;

	/**
	 * ID: 1000257<br>
	 * Message: Master Gildor does not like to be disturbed.
	 */
	public static final NpcStringId MASTER_GILDOR_DOES_NOT_LIKE_TO_BE_DISTURBED;

	/**
	 * ID: 1000258<br>
	 * Message: Please, just hold it down...
	 */
	public static final NpcStringId PLEASE_JUST_HOLD_IT_DOWN;

	/**
	 * ID: 1000259<br>
	 * Message: If you disturb Master Gildor I won't be able to help you.
	 */
	public static final NpcStringId IF_YOU_DISTURB_MASTER_GILDOR_I_WONT_BE_ABLE_TO_HELP_YOU;

	/**
	 * ID: 1000260<br>
	 * Message: Who dares approach?
	 */
	public static final NpcStringId WHO_DARES_APPROACH;

	/**
	 * ID: 1000261<br>
	 * Message: These reeds are my territory...
	 */
	public static final NpcStringId THESE_REEDS_ARE_MY_TERRITORY;

	/**
	 * ID: 1000262<br>
	 * Message: You fools! Today you shall learn a lesson!
	 */
	public static final NpcStringId YOU_FOOLS_TODAY_YOU_SHALL_LEARN_A_LESSON;

	/**
	 * ID: 1000263<br>
	 * Message: The past goes by... Is a new era beginning?...
	 */
	public static final NpcStringId THE_PAST_GOES_BY_IS_A_NEW_ERA_BEGINNING;

	/**
	 * ID: 1000264<br>
	 * Message: This is the garden of Eva.
	 */
	public static final NpcStringId THIS_IS_THE_GARDEN_OF_EVA;

	/**
	 * ID: 1000265<br>
	 * Message: The garden of Eva is a sacred place.
	 */
	public static final NpcStringId THE_GARDEN_OF_EVA_IS_A_SACRED_PLACE;

	/**
	 * ID: 1000266<br>
	 * Message: Do you mean to insult Eva?
	 */
	public static final NpcStringId DO_YOU_MEAN_TO_INSULT_EVA;

	/**
	 * ID: 1000267<br>
	 * Message: How rude!  Eva's love is available to all, even to an ill-mannered lout like yourself!
	 */
	public static final NpcStringId HOW_RUDE_EVAS_LOVE_IS_AVAILABLE_TO_ALL_EVEN_TO_AN_ILL_MANNERED_LOUT_LIKE_YOURSELF;

	/**
	 * ID: 1000268<br>
	 * Message: This place once belonged to Lord Shilen.
	 */
	public static final NpcStringId THIS_PLACE_ONCE_BELONGED_TO_LORD_SHILEN;

	/**
	 * ID: 1000269<br>
	 * Message: Leave this palace to us, spirits of Eva.
	 */
	public static final NpcStringId LEAVE_THIS_PALACE_TO_US_SPIRITS_OF_EVA;

	/**
	 * ID: 1000270<br>
	 * Message: Why are you getting in our way?
	 */
	public static final NpcStringId WHY_ARE_YOU_GETTING_IN_OUR_WAY;

	/**
	 * ID: 1000271<br>
	 * Message: Shilen... our Shilen!
	 */
	public static final NpcStringId SHILEN_OUR_SHILEN;

	/**
	 * ID: 1000272<br>
	 * Message: All who fear of Fafurion... Leave this place at once!
	 */
	public static final NpcStringId ALL_WHO_FEAR_OF_FAFURION_LEAVE_THIS_PLACE_AT_ONCE;

	/**
	 * ID: 1000273<br>
	 * Message: You are being punished in the name of Fafurion!
	 */
	public static final NpcStringId YOU_ARE_BEING_PUNISHED_IN_THE_NAME_OF_FAFURION;

	/**
	 * ID: 1000274<br>
	 * Message: Oh, master... please forgive your humble servant...
	 */
	public static final NpcStringId OH_MASTER_PLEASE_FORGIVE_YOUR_HUMBLE_SERVANT;

	/**
	 * ID: 1000275<br>
	 * Message: Prepare to die, foreign invaders! I am Gustav, the eternal ruler of this fortress and I have taken up my sword to repel thee!
	 */
	public static final NpcStringId
			PREPARE_TO_DIE_FOREIGN_INVADERS_I_AM_GUSTAV_THE_ETERNAL_RULER_OF_THIS_FORTRESS_AND_I_HAVE_TAKEN_UP_MY_SWORD_TO_REPEL_THEE;

	/**
	 * ID: 1000276<br>
	 * Message: Glory to Aden, the Kingdom of the Lion! Glory to Sir Gustav, our immortal lord!
	 */
	public static final NpcStringId GLORY_TO_ADEN_THE_KINGDOM_OF_THE_LION_GLORY_TO_SIR_GUSTAV_OUR_IMMORTAL_LORD;

	/**
	 * ID: 1000277<br>
	 * Message: Soldiers of Gustav, go forth and destroy the invaders!
	 */
	public static final NpcStringId SOLDIERS_OF_GUSTAV_GO_FORTH_AND_DESTROY_THE_INVADERS;

	/**
	 * ID: 1000278<br>
	 * Message: This is unbelievable! Have I really been defeated? I shall return and take your head!
	 */
	public static final NpcStringId THIS_IS_UNBELIEVABLE_HAVE_I_REALLY_BEEN_DEFEATED_I_SHALL_RETURN_AND_TAKE_YOUR_HEAD;

	/**
	 * ID: 1000279<br>
	 * Message: Could it be that I have reached my end? I cannot die without honor, without the permission of Sir Gustav!
	 */
	public static final NpcStringId
			COULD_IT_BE_THAT_I_HAVE_REACHED_MY_END_I_CANNOT_DIE_WITHOUT_HONOR_WITHOUT_THE_PERMISSION_OF_SIR_GUSTAV;

	/**
	 * ID: 1000280<br>
	 * Message: Ah, the bitter taste of defeat... I fear my torments are not over...
	 */
	public static final NpcStringId AH_THE_BITTER_TASTE_OF_DEFEAT_I_FEAR_MY_TORMENTS_ARE_NOT_OVER;

	/**
	 * ID: 1000281<br>
	 * Message: I follow the will of Fafurion.
	 */
	public static final NpcStringId I_FOLLOW_THE_WILL_OF_FAFURION;

	/**
	 * ID: 1000282<br>
	 * Message: Tickets for the Lucky Lottery are now on sale!
	 */
	public static final NpcStringId TICKETS_FOR_THE_LUCKY_LOTTERY_ARE_NOW_ON_SALE;

	/**
	 * ID: 1000283<br>
	 * Message: The Lucky Lottery drawing is about to begin!
	 */
	public static final NpcStringId THE_LUCKY_LOTTERY_DRAWING_IS_ABOUT_TO_BEGIN;

	/**
	 * ID: 1000284<br>
	 * Message: The winning numbers for Lucky Lottery $s1 are $s2. Congratulations to the winners!
	 */
	public static final NpcStringId THE_WINNING_NUMBERS_FOR_LUCKY_LOTTERY_S1_ARE_S2_CONGRATULATIONS_TO_THE_WINNERS;

	/**
	 * ID: 1000285<br>
	 * Message: You're too young to play Lucky Lottery!
	 */
	public static final NpcStringId YOURE_TOO_YOUNG_TO_PLAY_LUCKY_LOTTERY;

	/**
	 * ID: 1000286<br>
	 * Message: $s1! Watch your back!!!
	 */
	public static final NpcStringId S1_WATCH_YOUR_BACK;

	/**
	 * ID: 1000287<br>
	 * Message: $s1! Come on, I'll take you on!
	 */
	public static final NpcStringId S1_COME_ON_ILL_TAKE_YOU_ON;

	/**
	 * ID: 1000288<br>
	 * Message: $s1! How dare you interrupt our fight! Hey guys, help!
	 */
	public static final NpcStringId S1_HOW_DARE_YOU_INTERRUPT_OUR_FIGHT_HEY_GUYS_HELP;

	/**
	 * ID: 1000289<br>
	 * Message: I'll help you! I'm no coward!
	 */
	public static final NpcStringId ILL_HELP_YOU_IM_NO_COWARD;

	/**
	 * ID: 1000290<br>
	 * Message: Dear ultimate power!!!
	 */
	public static final NpcStringId DEAR_ULTIMATE_POWER;

	/**
	 * ID: 1000291<br>
	 * Message: Everybody! Attack $s1!
	 */
	public static final NpcStringId EVERYBODY_ATTACK_S1;

	/**
	 * ID: 1000292<br>
	 * Message: I will follow your order.
	 */
	public static final NpcStringId I_WILL_FOLLOW_YOUR_ORDER;

	/**
	 * ID: 1000293<br>
	 * Message: Bet you didn't expect this!
	 */
	public static final NpcStringId BET_YOU_DIDNT_EXPECT_THIS;

	/**
	 * ID: 1000294<br>
	 * Message: Come out, you children of darkness!
	 */
	public static final NpcStringId COME_OUT_YOU_CHILDREN_OF_DARKNESS;

	/**
	 * ID: 1000295<br>
	 * Message: Summon party members!
	 */
	public static final NpcStringId SUMMON_PARTY_MEMBERS;

	/**
	 * ID: 1000296<br>
	 * Message: Master! Did you call me?
	 */
	public static final NpcStringId MASTER_DID_YOU_CALL_ME;

	/**
	 * ID: 1000297<br>
	 * Message: You idiot!
	 */
	public static final NpcStringId YOU_IDIOT;

	/**
	 * ID: 1000298<br>
	 * Message: What about this?
	 */
	public static final NpcStringId WHAT_ABOUT_THIS;

	/**
	 * ID: 1000299<br>
	 * Message: Very impressive $s1! This is the last!
	 */
	public static final NpcStringId VERY_IMPRESSIVE_S1_THIS_IS_THE_LAST;

	/**
	 * ID: 1000300<br>
	 * Message: Dawn
	 */
	public static final NpcStringId DAWN;

	/**
	 * ID: 1000301<br>
	 * Message: Dusk
	 */
	public static final NpcStringId DUSK;

	/**
	 * ID: 1000302<br>
	 * Message: Nothingness
	 */
	public static final NpcStringId NOTHINGNESS;

	/**
	 * ID: 1000303<br>
	 * Message: This world will soon be annihilated!
	 */
	public static final NpcStringId THIS_WORLD_WILL_SOON_BE_ANNIHILATED;

	/**
	 * ID: 1000304<br>
	 * Message: A curse upon you!
	 */
	public static final NpcStringId A_CURSE_UPON_YOU;

	/**
	 * ID: 1000305<br>
	 * Message: The day of judgment is near!
	 */
	public static final NpcStringId THE_DAY_OF_JUDGMENT_IS_NEAR;

	/**
	 * ID: 1000306<br>
	 * Message: I bestow upon you a blessing!
	 */
	public static final NpcStringId I_BESTOW_UPON_YOU_A_BLESSING;

	/**
	 * ID: 1000307<br>
	 * Message: The first rule of fighting is to start by killing the weak ones!
	 */
	public static final NpcStringId THE_FIRST_RULE_OF_FIGHTING_IS_TO_START_BY_KILLING_THE_WEAK_ONES;

	/**
	 * ID: 1000308<br>
	 * Message: Adena
	 */
	public static final NpcStringId ADENA;

	/**
	 * ID: 1000309<br>
	 * Message: Ancient Adena
	 */
	public static final NpcStringId ANCIENT_ADENA;

	/**
	 * ID: 1000312<br>
	 * Message: Level 31 or lower
	 */
	public static final NpcStringId LEVEL_31_OR_LOWER;

	/**
	 * ID: 1000313<br>
	 * Message: Level 42 or lower
	 */
	public static final NpcStringId LEVEL_42_OR_LOWER;

	/**
	 * ID: 1000314<br>
	 * Message: Level 53 or lower
	 */
	public static final NpcStringId LEVEL_53_OR_LOWER;

	/**
	 * ID: 1000315<br>
	 * Message: Level 64 or lower
	 */
	public static final NpcStringId LEVEL_64_OR_LOWER;

	/**
	 * ID: 1000316<br>
	 * Message: No Level Limit
	 */
	public static final NpcStringId NO_LEVEL_LIMIT;

	/**
	 * ID: 1000317<br>
	 * Message: The main event will start in 2 minutes. Please register now.
	 */
	public static final NpcStringId THE_MAIN_EVENT_WILL_START_IN_2_MINUTES_PLEASE_REGISTER_NOW;

	/**
	 * ID: 1000318<br>
	 * Message: The main event is now starting.
	 */
	public static final NpcStringId THE_MAIN_EVENT_IS_NOW_STARTING;

	/**
	 * ID: 1000319<br>
	 * Message: The main event will close in 5 minutes.
	 */
	public static final NpcStringId THE_MAIN_EVENT_WILL_CLOSE_IN_5_MINUTES;

	/**
	 * ID: 1000320<br>
	 * Message: The main event will finish in 2 minutes. Please prepare for the next game.
	 */
	public static final NpcStringId THE_MAIN_EVENT_WILL_FINISH_IN_2_MINUTES_PLEASE_PREPARE_FOR_THE_NEXT_GAME;

	/**
	 * ID: 1000321<br>
	 * Message: The amount of SSQ contribution has increased by $s1.
	 */
	public static final NpcStringId THE_AMOUNT_OF_SSQ_CONTRIBUTION_HAS_INCREASED_BY_S1;

	/**
	 * ID: 1000322<br>
	 * Message: No record exists
	 */
	public static final NpcStringId NO_RECORD_EXISTS;

	/**
	 * ID: 1000380<br>
	 * Message: That will do! I'll move you to the outside soon.
	 */
	public static final NpcStringId THAT_WILL_DO_ILL_MOVE_YOU_TO_THE_OUTSIDE_SOON;

	/**
	 * ID: 1000382<br>
	 * Message: Your rear is practically unguarded!
	 */
	public static final NpcStringId YOUR_REAR_IS_PRACTICALLY_UNGUARDED;

	/**
	 * ID: 1000383<br>
	 * Message: How dare you turn your back on me!
	 */
	public static final NpcStringId HOW_DARE_YOU_TURN_YOUR_BACK_ON_ME;

	/**
	 * ID: 1000384<br>
	 * Message: $s1! I'll deal with you myself!
	 */
	public static final NpcStringId S1_ILL_DEAL_WITH_YOU_MYSELF;

	/**
	 * ID: 1000385<br>
	 * Message: $s1! This is personal!
	 */
	public static final NpcStringId S1_THIS_IS_PERSONAL;

	/**
	 * ID: 1000386<br>
	 * Message: $s1! Leave us alone! This is between us!
	 */
	public static final NpcStringId S1_LEAVE_US_ALONE_THIS_IS_BETWEEN_US;

	/**
	 * ID: 1000387<br>
	 * Message: $s1! Killing you will be a pleasure!
	 */
	public static final NpcStringId S1_KILLING_YOU_WILL_BE_A_PLEASURE;

	/**
	 * ID: 1000388<br>
	 * Message: $s1! Hey! We're having a duel here!
	 */
	public static final NpcStringId S1_HEY_WERE_HAVING_A_DUEL_HERE;

	/**
	 * ID: 1000389<br>
	 * Message: The duel is over! Attack!
	 */
	public static final NpcStringId THE_DUEL_IS_OVER_ATTACK;

	/**
	 * ID: 1000390<br>
	 * Message: Foul! Kill the coward!
	 */
	public static final NpcStringId FOUL_KILL_THE_COWARD;

	/**
	 * ID: 1000391<br>
	 * Message: How dare you interrupt a sacred duel! You must be taught a lesson!
	 */
	public static final NpcStringId HOW_DARE_YOU_INTERRUPT_A_SACRED_DUEL_YOU_MUST_BE_TAUGHT_A_LESSON;

	/**
	 * ID: 1000392<br>
	 * Message: Die, you coward!
	 */
	public static final NpcStringId DIE_YOU_COWARD;

	/**
	 * ID: 1000394<br>
	 * Message: Kill the coward!
	 */
	public static final NpcStringId KILL_THE_COWARD;

	/**
	 * ID: 1000395<br>
	 * Message: I never thought I'd use this against a novice!
	 */
	public static final NpcStringId I_NEVER_THOUGHT_ID_USE_THIS_AGAINST_A_NOVICE;

	/**
	 * ID: 1000396<br>
	 * Message: You won't take me down easily.
	 */
	public static final NpcStringId YOU_WONT_TAKE_ME_DOWN_EASILY;

	/**
	 * ID: 1000397<br>
	 * Message: The battle has just begun!
	 */
	public static final NpcStringId THE_BATTLE_HAS_JUST_BEGUN;

	/**
	 * ID: 1000398<br>
	 * Message: Kill $s1 first!
	 */
	public static final NpcStringId KILL_S1_FIRST;

	/**
	 * ID: 1000399<br>
	 * Message: Attack $s1!
	 */
	public static final NpcStringId ATTACK_S1;

	/**
	 * ID: 1000400<br>
	 * Message: Attack! Attack!
	 */
	public static final NpcStringId ATTACK_ATTACK;

	/**
	 * ID: 1000401<br>
	 * Message: Over here!
	 */
	public static final NpcStringId OVER_HERE;

	/**
	 * ID: 1000402<br>
	 * Message: Roger!
	 */
	public static final NpcStringId ROGER;

	/**
	 * ID: 1000403<br>
	 * Message: Show yourselves!
	 */
	public static final NpcStringId SHOW_YOURSELVES;

	/**
	 * ID: 1000404<br>
	 * Message: Forces of darkness! Follow me!
	 */
	public static final NpcStringId FORCES_OF_DARKNESS_FOLLOW_ME;

	/**
	 * ID: 1000405<br>
	 * Message: Destroy the enemy, my brothers!
	 */
	public static final NpcStringId DESTROY_THE_ENEMY_MY_BROTHERS;

	/**
	 * ID: 1000406<br>
	 * Message: Now the fun starts!
	 */
	public static final NpcStringId NOW_THE_FUN_STARTS;

	/**
	 * ID: 1000407<br>
	 * Message: Enough fooling around. Get ready to die!
	 */
	public static final NpcStringId ENOUGH_FOOLING_AROUND_GET_READY_TO_DIE;

	/**
	 * ID: 1000408<br>
	 * Message: You idiot! I've just been toying with you!
	 */
	public static final NpcStringId YOU_IDIOT_IVE_JUST_BEEN_TOYING_WITH_YOU;

	/**
	 * ID: 1000409<br>
	 * Message: Witness my true power!
	 */
	public static final NpcStringId WITNESS_MY_TRUE_POWER;

	/**
	 * ID: 1000410<br>
	 * Message: Now the battle begins!
	 */
	public static final NpcStringId NOW_THE_BATTLE_BEGINS;

	/**
	 * ID: 1000411<br>
	 * Message: I must admit, no one makes my blood boil quite like you do!
	 */
	public static final NpcStringId I_MUST_ADMIT_NO_ONE_MAKES_MY_BLOOD_BOIL_QUITE_LIKE_YOU_DO;

	/**
	 * ID: 1000412<br>
	 * Message: You have more skill than I thought!
	 */
	public static final NpcStringId YOU_HAVE_MORE_SKILL_THAN_I_THOUGHT;

	/**
	 * ID: 1000413<br>
	 * Message: I'll double my strength!
	 */
	public static final NpcStringId ILL_DOUBLE_MY_STRENGTH;

	/**
	 * ID: 1000414<br>
	 * Message: Prepare to die!
	 */
	public static final NpcStringId PREPARE_TO_DIE;

	/**
	 * ID: 1000415<br>
	 * Message: All is lost! Prepare to meet the goddess of death!
	 */
	public static final NpcStringId ALL_IS_LOST_PREPARE_TO_MEET_THE_GODDESS_OF_DEATH;

	/**
	 * ID: 1000416<br>
	 * Message: All is lost! The prophecy of destruction has been fulfilled!
	 */
	public static final NpcStringId ALL_IS_LOST_THE_PROPHECY_OF_DESTRUCTION_HAS_BEEN_FULFILLED;

	/**
	 * ID: 1000417<br>
	 * Message: The end of time has come! The prophecy of destruction has been fulfilled!
	 */
	public static final NpcStringId THE_END_OF_TIME_HAS_COME_THE_PROPHECY_OF_DESTRUCTION_HAS_BEEN_FULFILLED;

	/**
	 * ID: 1000418<br>
	 * Message: $s1! You bring an ill wind!
	 */
	public static final NpcStringId S1_YOU_BRING_AN_ILL_WIND;

	/**
	 * ID: 1000419<br>
	 * Message: $s1! You might as well give up!
	 */
	public static final NpcStringId S1_YOU_MIGHT_AS_WELL_GIVE_UP;

	/**
	 * ID: 1000420<br>
	 * Message: You don't have any hope! Your end has come!
	 */
	public static final NpcStringId YOU_DONT_HAVE_ANY_HOPE_YOUR_END_HAS_COME;

	/**
	 * ID: 1000421<br>
	 * Message: The prophecy of darkness has been fulfilled!
	 */
	public static final NpcStringId THE_PROPHECY_OF_DARKNESS_HAS_BEEN_FULFILLED;

	/**
	 * ID: 1000422<br>
	 * Message: As foretold in the prophecy of darkness, the era of chaos has begun!
	 */
	public static final NpcStringId AS_FORETOLD_IN_THE_PROPHECY_OF_DARKNESS_THE_ERA_OF_CHAOS_HAS_BEGUN;

	/**
	 * ID: 1000423<br>
	 * Message: The prophecy of darkness has come to pass!
	 */
	public static final NpcStringId THE_PROPHECY_OF_DARKNESS_HAS_COME_TO_PASS;

	/**
	 * ID: 1000424<br>
	 * Message: $s1! I give you the blessing of prophecy!
	 */
	public static final NpcStringId S1_I_GIVE_YOU_THE_BLESSING_OF_PROPHECY;

	/**
	 * ID: 1000425<br>
	 * Message: $s1! I bestow upon you the authority of the abyss!
	 */
	public static final NpcStringId S1_I_BESTOW_UPON_YOU_THE_AUTHORITY_OF_THE_ABYSS;

	/**
	 * ID: 1000426<br>
	 * Message: Herald of the new era, open your eyes!
	 */
	public static final NpcStringId HERALD_OF_THE_NEW_ERA_OPEN_YOUR_EYES;

	/**
	 * ID: 1000427<br>
	 * Message: Remember, kill the weaklings first!
	 */
	public static final NpcStringId REMEMBER_KILL_THE_WEAKLINGS_FIRST;

	/**
	 * ID: 1000428<br>
	 * Message: Prepare to die, maggot!
	 */
	public static final NpcStringId PREPARE_TO_DIE_MAGGOT;

	/**
	 * ID: 1000429<br>
	 * Message: That will do. Prepare to be released!
	 */
	public static final NpcStringId THAT_WILL_DO_PREPARE_TO_BE_RELEASED;

	/**
	 * ID: 1000430<br>
	 * Message: Draw
	 */
	public static final NpcStringId DRAW;

	/**
	 * ID: 1000431<br>
	 * Message: Rulers of the seal! I bring you wondrous gifts!
	 */
	public static final NpcStringId RULERS_OF_THE_SEAL_I_BRING_YOU_WONDROUS_GIFTS;

	/**
	 * ID: 1000432<br>
	 * Message: Rulers of the seal! I have some excellent weapons to show you!
	 */
	public static final NpcStringId RULERS_OF_THE_SEAL_I_HAVE_SOME_EXCELLENT_WEAPONS_TO_SHOW_YOU;

	/**
	 * ID: 1000433<br>
	 * Message: I've been so busy lately, in addition to planning my trip!
	 */
	public static final NpcStringId IVE_BEEN_SO_BUSY_LATELY_IN_ADDITION_TO_PLANNING_MY_TRIP;

	/**
	 * ID: 1000434<br>
	 * Message: Your treatment of weaklings is unforgivable!
	 */
	public static final NpcStringId YOUR_TREATMENT_OF_WEAKLINGS_IS_UNFORGIVABLE;

	/**
	 * ID: 1000435<br>
	 * Message: I'm here to help you! Hi yah!
	 */
	public static final NpcStringId IM_HERE_TO_HELP_YOU_HI_YAH;

	/**
	 * ID: 1000436<br>
	 * Message: Justice will be served!
	 */
	public static final NpcStringId JUSTICE_WILL_BE_SERVED;

	/**
	 * ID: 1000437<br>
	 * Message: On to immortal glory!
	 */
	public static final NpcStringId ON_TO_IMMORTAL_GLORY;

	/**
	 * ID: 1000438<br>
	 * Message: Justice always avenges the powerless!
	 */
	public static final NpcStringId JUSTICE_ALWAYS_AVENGES_THE_POWERLESS;

	/**
	 * ID: 1000439<br>
	 * Message: Are you hurt? Hang in there, we've almost got them!
	 */
	public static final NpcStringId ARE_YOU_HURT_HANG_IN_THERE_WEVE_ALMOST_GOT_THEM;

	/**
	 * ID: 1000440<br>
	 * Message: Why should I tell you my name, you creep!?
	 */
	public static final NpcStringId WHY_SHOULD_I_TELL_YOU_MY_NAME_YOU_CREEP;

	/**
	 * ID: 1000441<br>
	 * Message: 0 minute
	 */
	public static final NpcStringId N1_MINUTE;

	/**
	 * ID: 1000443<br>
	 * Message: The defenders of $s1 castle will be teleported to the inner castle.
	 */
	public static final NpcStringId THE_DEFENDERS_OF_S1_CASTLE_WILL_BE_TELEPORTED_TO_THE_INNER_CASTLE;

	/**
	 * ID: 1000444<br>
	 * Message: Sunday
	 */
	public static final NpcStringId SUNDAY;

	/**
	 * ID: 1000445<br>
	 * Message: Monday
	 */
	public static final NpcStringId MONDAY;

	/**
	 * ID: 1000446<br>
	 * Message: Tuesday
	 */
	public static final NpcStringId TUESDAY;

	/**
	 * ID: 1000447<br>
	 * Message: Wednesday
	 */
	public static final NpcStringId WEDNESDAY;

	/**
	 * ID: 1000448<br>
	 * Message: Thursday
	 */
	public static final NpcStringId THURSDAY;

	/**
	 * ID: 1000449<br>
	 * Message: Friday
	 */
	public static final NpcStringId FRIDAY;

	/**
	 * ID: 1000450<br>
	 * Message: Saturday
	 */
	public static final NpcStringId SATURDAY;

	/**
	 * ID: 1000451<br>
	 * Message: Hour
	 */
	public static final NpcStringId HOUR;

	/**
	 * ID: 1000452<br>
	 * Message: It's a good day to die! Welcome to hell, maggot!
	 */
	public static final NpcStringId ITS_A_GOOD_DAY_TO_DIE_WELCOME_TO_HELL_MAGGOT;

	/**
	 * ID: 1000453<br>
	 * Message: The Festival of Darkness will end in two minutes.
	 */
	public static final NpcStringId THE_FESTIVAL_OF_DARKNESS_WILL_END_IN_TWO_MINUTES;

	/**
	 * ID: 1000454<br>
	 * Message: Noblesse Gate Pass
	 */
	public static final NpcStringId NOBLESSE_GATE_PASS;

	/**
	 * ID: 1000456<br>
	 * Message: minute(s) have passed.
	 */
	public static final NpcStringId MINUTES_HAVE_PASSED;

	/**
	 * ID: 1000457<br>
	 * Message: Game over. The teleport will appear momentarily.
	 */
	public static final NpcStringId GAME_OVER_THE_TELEPORT_WILL_APPEAR_MOMENTARILY;

	/**
	 * ID: 1000458<br>
	 * Message: You, who are like the slugs crawling on the ground. The generosity and greatness of me, Daimon the White-Eyed is endless! Ha Ha Ha!
	 */
	public static final NpcStringId
			YOU_WHO_ARE_LIKE_THE_SLUGS_CRAWLING_ON_THE_GROUND_THE_GENEROSITY_AND_GREATNESS_OF_ME_DAIMON_THE_WHITE_EYED_IS_ENDLESS_HA_HA_HA;

	/**
	 * ID: 1000459<br>
	 * Message: If you want to be the opponent of me, Daimon the White-Eyed, you should at least have the basic skills~!
	 */
	public static final NpcStringId
			IF_YOU_WANT_TO_BE_THE_OPPONENT_OF_ME_DAIMON_THE_WHITE_EYED_YOU_SHOULD_AT_LEAST_HAVE_THE_BASIC_SKILLS;

	/**
	 * ID: 1000460<br>
	 * Message: You stupid creatures that are bound to the earth. You are suffering too much while dragging your fat, heavy bodies.  I, Daimon, will lighten your burden.
	 */
	public static final NpcStringId
			YOU_STUPID_CREATURES_THAT_ARE_BOUND_TO_THE_EARTH_YOU_ARE_SUFFERING_TOO_MUCH_WHILE_DRAGGING_YOUR_FAT_HEAVY_BODIES_I_DAIMON_WILL_LIGHTEN_YOUR_BURDEN;

	/**
	 * ID: 1000461<br>
	 * Message: A weak and stupid tribe like yours doesn't deserve to be my enemy! Bwa ha ha ha!
	 */
	public static final NpcStringId A_WEAK_AND_STUPID_TRIBE_LIKE_YOURS_DOESNT_DESERVE_TO_BE_MY_ENEMY_BWA_HA_HA_HA;

	/**
	 * ID: 1000462<br>
	 * Message: You dare to invade the territory of Daimon, the White-Eyed!  Now, you will pay the price for your action!
	 */
	public static final NpcStringId
			YOU_DARE_TO_INVADE_THE_TERRITORY_OF_DAIMON_THE_WHITE_EYED_NOW_YOU_WILL_PAY_THE_PRICE_FOR_YOUR_ACTION;

	/**
	 * ID: 1000463<br>
	 * Message: This is the grace of Daimon the White-Eyed, the great Monster Eye Lord! Ha Ha Ha!
	 */
	public static final NpcStringId THIS_IS_THE_GRACE_OF_DAIMON_THE_WHITE_EYED_THE_GREAT_MONSTER_EYE_LORD_HA_HA_HA;

	/**
	 * ID: 1000464<br>
	 * Message: $s1!  You have become a Hero of Duelists.  Congratulations!
	 */
	public static final NpcStringId S1_YOU_HAVE_BECOME_A_HERO_OF_DUELISTS_CONGRATULATIONS;

	/**
	 * ID: 1000465<br>
	 * Message: $s1! You have become a Hero of Dreadnoughts. Congratulations!
	 */
	public static final NpcStringId S1_YOU_HAVE_BECOME_A_HERO_OF_DREADNOUGHTS_CONGRATULATIONS;

	/**
	 * ID: 1000466<br>
	 * Message: $s1!  You have become a Hero of Phoenix Knights.  Congratulations!
	 */
	public static final NpcStringId S1_YOU_HAVE_BECOME_A_HERO_OF_PHOENIX_KNIGHTS_CONGRATULATIONS;

	/**
	 * ID: 1000467<br>
	 * Message: $s1!  You have become a Hero of Hell Knights.  Congratulations!
	 */
	public static final NpcStringId S1_YOU_HAVE_BECOME_A_HERO_OF_HELL_KNIGHTS_CONGRATULATIONS;

	/**
	 * ID: 1000468<br>
	 * Message: $s1  You have become a Sagittarius Hero.  Congratulations!
	 */
	public static final NpcStringId S1_YOU_HAVE_BECOME_A_SAGITTARIUS_HERO_CONGRATULATIONS;

	/**
	 * ID: 1000469<br>
	 * Message: $s1!  You have become a Hero of Adventurers.  Congratulations!
	 */
	public static final NpcStringId S1_YOU_HAVE_BECOME_A_HERO_OF_ADVENTURERS_CONGRATULATIONS;

	/**
	 * ID: 1000470<br>
	 * Message: $s1!  You have become a Hero of Archmages.  Congratulations!
	 */
	public static final NpcStringId S1_YOU_HAVE_BECOME_A_HERO_OF_ARCHMAGES_CONGRATULATIONS;

	/**
	 * ID: 1000471<br>
	 * Message: $s1!  You have become a Hero of Soultakers.  Congratulations!
	 */
	public static final NpcStringId S1_YOU_HAVE_BECOME_A_HERO_OF_SOULTAKERS_CONGRATULATIONS;

	/**
	 * ID: 1000472<br>
	 * Message: $s1!  You have become a Hero of Arcana Lords.  Congratulations!
	 */
	public static final NpcStringId S1_YOU_HAVE_BECOME_A_HERO_OF_ARCANA_LORDS_CONGRATULATIONS;

	/**
	 * ID: 1000473<br>
	 * Message: $s1!  You have become a Hero of Cardinals.  Congratulations!
	 */
	public static final NpcStringId S1_YOU_HAVE_BECOME_A_HERO_OF_CARDINALS_CONGRATULATIONS;

	/**
	 * ID: 1000474<br>
	 * Message: $s1!  You have become a Hero of Hierophants.  Congratulations!
	 */
	public static final NpcStringId S1_YOU_HAVE_BECOME_A_HERO_OF_HIEROPHANTS_CONGRATULATIONS;

	/**
	 * ID: 1000475<br>
	 * Message: $s1!  You have become a Hero of Eva's Templars.  Congratulations!
	 */
	public static final NpcStringId S1_YOU_HAVE_BECOME_A_HERO_OF_EVAS_TEMPLARS_CONGRATULATIONS;

	/**
	 * ID: 1000476<br>
	 * Message: $s1!  You have become a Hero of Sword Muses.  Congratulations!
	 */
	public static final NpcStringId S1_YOU_HAVE_BECOME_A_HERO_OF_SWORD_MUSES_CONGRATULATIONS;

	/**
	 * ID: 1000477<br>
	 * Message: $s1!  You have become a Hero of Wind Riders.  Congratulations!
	 */
	public static final NpcStringId S1_YOU_HAVE_BECOME_A_HERO_OF_WIND_RIDERS_CONGRATULATIONS;

	/**
	 * ID: 1000478<br>
	 * Message: $s1! You have become a Hero of Moonlight Sentinels. Congratulations!
	 */
	public static final NpcStringId S1_YOU_HAVE_BECOME_A_HERO_OF_MOONLIGHT_SENTINELS_CONGRATULATIONS;

	/**
	 * ID: 1000479<br>
	 * Message: $s1!  You have become a Hero of Mystic Muses.  Congratulations!
	 */
	public static final NpcStringId S1_YOU_HAVE_BECOME_A_HERO_OF_MYSTIC_MUSES_CONGRATULATIONS;

	/**
	 * ID: 1000480<br>
	 * Message: $s1! You have become a Hero of Elemental Masters. Congratulations!
	 */
	public static final NpcStringId S1_YOU_HAVE_BECOME_A_HERO_OF_ELEMENTAL_MASTERS_CONGRATULATIONS;

	/**
	 * ID: 1000481<br>
	 * Message: $s1!  You have become a Hero of Eva's Saints.  Congratulations!
	 */
	public static final NpcStringId S1_YOU_HAVE_BECOME_A_HERO_OF_EVAS_SAINTS_CONGRATULATIONS;

	/**
	 * ID: 1000482<br>
	 * Message: $s1!  You have become a Hero of the Shillien Templars.  Congratulations!
	 */
	public static final NpcStringId S1_YOU_HAVE_BECOME_A_HERO_OF_THE_SHILLIEN_TEMPLARS_CONGRATULATIONS;

	/**
	 * ID: 1000483<br>
	 * Message: $s1!  You have become a Hero of Spectral Dancers.  Congratulations!
	 */
	public static final NpcStringId S1_YOU_HAVE_BECOME_A_HERO_OF_SPECTRAL_DANCERS_CONGRATULATIONS;

	/**
	 * ID: 1000484<br>
	 * Message: $s1!  You have become a Hero of Ghost Hunters.  Congratulations!
	 */
	public static final NpcStringId S1_YOU_HAVE_BECOME_A_HERO_OF_GHOST_HUNTERS_CONGRATULATIONS;

	/**
	 * ID: 1000485<br>
	 * Message: $s1! You have become a Hero of Ghost Sentinels. Congratulations!
	 */
	public static final NpcStringId S1_YOU_HAVE_BECOME_A_HERO_OF_GHOST_SENTINELS_CONGRATULATIONS;

	/**
	 * ID: 1000486<br>
	 * Message: $s1!  You have become a Hero of Storm Screamers.  Congratulations!
	 */
	public static final NpcStringId S1_YOU_HAVE_BECOME_A_HERO_OF_STORM_SCREAMERS_CONGRATULATIONS;

	/**
	 * ID: 1000487<br>
	 * Message: $s1! You have become a Hero of Spectral Masters. Congratulations!
	 */
	public static final NpcStringId S1_YOU_HAVE_BECOME_A_HERO_OF_SPECTRAL_MASTERS_CONGRATULATIONS;

	/**
	 * ID: 1000488<br>
	 * Message: $s1!  You have become a Hero of the Shillien Saints.  Congratulations!
	 */
	public static final NpcStringId S1_YOU_HAVE_BECOME_A_HERO_OF_THE_SHILLIEN_SAINTS_CONGRATULATIONS;

	/**
	 * ID: 1000489<br>
	 * Message: $s1!  You have become a Hero of Titans.  Congratulations!
	 */
	public static final NpcStringId S1_YOU_HAVE_BECOME_A_HERO_OF_TITANS_CONGRATULATIONS;

	/**
	 * ID: 1000490<br>
	 * Message: $s1!  You have become a Hero of Grand Khavataris.  Congratulations!
	 */
	public static final NpcStringId S1_YOU_HAVE_BECOME_A_HERO_OF_GRAND_KHAVATARIS_CONGRATULATIONS;

	/**
	 * ID: 1000491<br>
	 * Message: $s1!  You have become a Hero of Dominators.  Congratulations!
	 */
	public static final NpcStringId S1_YOU_HAVE_BECOME_A_HERO_OF_DOMINATORS_CONGRATULATIONS;

	/**
	 * ID: 1000492<br>
	 * Message: $s1!  You have become a Hero of Doomcryers.  Congratulations!
	 */
	public static final NpcStringId S1_YOU_HAVE_BECOME_A_HERO_OF_DOOMCRYERS_CONGRATULATIONS;

	/**
	 * ID: 1000493<br>
	 * Message: $s1!  You have become a Hero of Fortune Seekers.  Congratulations!
	 */
	public static final NpcStringId S1_YOU_HAVE_BECOME_A_HERO_OF_FORTUNE_SEEKERS_CONGRATULATIONS;

	/**
	 * ID: 1000494<br>
	 * Message: $s1!  You have become a Hero of Maestros.  Congratulations!
	 */
	public static final NpcStringId S1_YOU_HAVE_BECOME_A_HERO_OF_MAESTROS_CONGRATULATIONS;

	/**
	 * ID: 1000495<br>
	 * Message: **unregistered**
	 */
	public static final NpcStringId UNREGISTERED;

	/**
	 * ID: 1000496<br>
	 * Message: $s1! You have become a Hero of Doombringers. Congratulations!
	 */
	public static final NpcStringId S1_YOU_HAVE_BECOME_A_HERO_OF_DOOMBRINGERS_CONGRATULATIONS;

	/**
	 * ID: 1000497<br>
	 * Message: $s1! You have become a Hero of Soul Hounds. Congratulations!
	 */
	public static final NpcStringId S1_YOU_HAVE_BECOME_A_HERO_OF_SOUL_HOUNDS_CONGRATULATIONS;

	/**
	 * ID: 1000499<br>
	 * Message: $s1! You have become a Hero of Tricksters. Congratulations!
	 */
	public static final NpcStringId S1_YOU_HAVE_BECOME_A_HERO_OF_TRICKSTERS_CONGRATULATIONS;

	/**
	 * ID: 1000500<br>
	 * Message: You may now enter the Sepulcher.
	 */
	public static final NpcStringId YOU_MAY_NOW_ENTER_THE_SEPULCHER;

	/**
	 * ID: 1000501<br>
	 * Message: If you place your hand on the stone statue in front of each sepulcher, you will be able to enter.
	 */
	public static final NpcStringId
			IF_YOU_PLACE_YOUR_HAND_ON_THE_STONE_STATUE_IN_FRONT_OF_EACH_SEPULCHER_YOU_WILL_BE_ABLE_TO_ENTER;

	/**
	 * ID: 1000502<br>
	 * Message: The monsters have spawned!
	 */
	public static final NpcStringId THE_MONSTERS_HAVE_SPAWNED;

	/**
	 * ID: 1000503<br>
	 * Message: Thank you for saving me.
	 */
	public static final NpcStringId THANK_YOU_FOR_SAVING_ME;

	/**
	 * ID: 1000504<br>
	 * Message: Fewer than $s1
	 */
	public static final NpcStringId FEWER_THAN_S2;

	/**
	 * ID: 1000505<br>
	 * Message: More than $s1
	 */
	public static final NpcStringId MORE_THAN_S2;

	/**
	 * ID: 1000506<br>
	 * Message: Point
	 */
	public static final NpcStringId POINT;

	/**
	 * ID: 1000507<br>
	 * Message: Competition
	 */
	public static final NpcStringId COMPETITION;

	/**
	 * ID: 1000508<br>
	 * Message: Seal Validation
	 */
	public static final NpcStringId SEAL_VALIDATION;

	/**
	 * ID: 1000509<br>
	 * Message: Preparation
	 */
	public static final NpcStringId PREPARATION;

	/**
	 * ID: 1000512<br>
	 * Message: No Owner
	 */
	public static final NpcStringId NO_OWNER;

	/**
	 * ID: 1000513<br>
	 * Message: This place is dangerous, $s1. Please turn back.
	 */
	public static final NpcStringId THIS_PLACE_IS_DANGEROUS_S1_PLEASE_TURN_BACK;

	/**
	 * ID: 1000514<br>
	 * Message: Who disturbs my sacred sleep?
	 */
	public static final NpcStringId WHO_DISTURBS_MY_SACRED_SLEEP;

	/**
	 * ID: 1000515<br>
	 * Message: Begone, thief! Let our bones rest in peace.
	 */
	public static final NpcStringId BEGONE_THIEF_LET_OUR_BONES_REST_IN_PEACE;

	/**
	 * ID: 1000516<br>
	 * Message: Leave us be, Hestui scum!
	 */
	public static final NpcStringId _LEAVE_US_BE_HESTUI_SCUM;

	/**
	 * ID: 1000517<br>
	 * Message: Thieving Kakai, may bloodbugs gnaw you in your sleep!
	 */
	public static final NpcStringId THIEVING_KAKAI_MAY_BLOODBUGS_GNAW_YOU_IN_YOUR_SLEEP;

	/**
	 * ID: 1000518<br>
	 * Message: Newbie Travel Token
	 */
	public static final NpcStringId NEWBIE_TRAVEL_TOKEN;

	/**
	 * ID: 1000519<br>
	 * Message: Arrogant fool! You dare to challenge me, the Ruler of Flames? Here is your reward!
	 */
	public static final NpcStringId ARROGANT_FOOL_YOU_DARE_TO_CHALLENGE_ME_THE_RULER_OF_FLAMES_HERE_IS_YOUR_REWARD;

	/**
	 * ID: 1000520<br>
	 * Message: $s1!!!! You cannot hope to defeat me with your meager strength.
	 */
	public static final NpcStringId S1_YOU_CANNOT_HOPE_TO_DEFEAT_ME_WITH_YOUR_MEAGER_STRENGTH;

	/**
	 * ID: 1000521<br>
	 * Message: Not even the gods themselves could touch me. But you, $s1, you dare challenge me?! Ignorant mortal!
	 */
	public static final NpcStringId
			NOT_EVEN_THE_GODS_THEMSELVES_COULD_TOUCH_ME_BUT_YOU_S1_YOU_DARE_CHALLENGE_ME_IGNORANT_MORTAL;

	/**
	 * ID: 1000522<br>
	 * Message: Requiem of Hatred
	 */
	public static final NpcStringId REQUIEM_OF_HATRED;

	/**
	 * ID: 1000523<br>
	 * Message: Fugue of Jubilation
	 */
	public static final NpcStringId FUGUE_OF_JUBILATION;

	/**
	 * ID: 1000524<br>
	 * Message: Frenetic Toccata
	 */
	public static final NpcStringId FRENETIC_TOCCATA;

	/**
	 * ID: 1000525<br>
	 * Message: Hypnotic Mazurka
	 */
	public static final NpcStringId HYPNOTIC_MAZURKA;

	/**
	 * ID: 1000526<br>
	 * Message: Mournful Chorale Prelude
	 */
	public static final NpcStringId MOURNFUL_CHORALE_PRELUDE;

	/**
	 * ID: 1000527<br>
	 * Message: Rondo of Solitude
	 */
	public static final NpcStringId RONDO_OF_SOLITUDE;

	/**
	 * ID: 1000528<br>
	 * Message: Olympiad Token
	 */
	public static final NpcStringId OLYMPIAD_TOKEN;

	/**
	 * ID: 1001000<br>
	 * Message: The Kingdom of Aden
	 */
	public static final NpcStringId THE_KINGDOM_OF_ADEN;

	/**
	 * ID: 1001100<br>
	 * Message: The Kingdom of Elmore
	 */
	public static final NpcStringId THE_KINGDOM_OF_ELMORE;

	/**
	 * ID: 1010001<br>
	 * Message: Talking Island Village
	 */
	public static final NpcStringId TALKING_ISLAND_VILLAGE;

	/**
	 * ID: 1010002<br>
	 * Message: The Elven Village
	 */
	public static final NpcStringId THE_ELVEN_VILLAGE;

	/**
	 * ID: 1010003<br>
	 * Message: The Dark Elf Village
	 */
	public static final NpcStringId THE_DARK_ELF_VILLAGE;

	/**
	 * ID: 1010004<br>
	 * Message: The Village of Gludin
	 */
	public static final NpcStringId THE_VILLAGE_OF_GLUDIN;

	/**
	 * ID: 1010005<br>
	 * Message: The Town of Gludio
	 */
	public static final NpcStringId THE_TOWN_OF_GLUDIO;

	/**
	 * ID: 1010006<br>
	 * Message: The Town of Dion
	 */
	public static final NpcStringId THE_TOWN_OF_DION;

	/**
	 * ID: 1010007<br>
	 * Message: The Town of Giran
	 */
	public static final NpcStringId THE_TOWN_OF_GIRAN;

	/**
	 * ID: 1010008<br>
	 * Message: Orc Village
	 */
	public static final NpcStringId ORC_VILLAGE;

	/**
	 * ID: 1010009<br>
	 * Message: Dwarven Village
	 */
	public static final NpcStringId DWARVEN_VILLAGE;

	/**
	 * ID: 1010010<br>
	 * Message: The Southern Part of the Dark Forest
	 */
	public static final NpcStringId THE_SOUTHERN_PART_OF_THE_DARK_FOREST;

	/**
	 * ID: 1010011<br>
	 * Message: The Northeast Coast
	 */
	public static final NpcStringId THE_NORTHEAST_COAST;

	/**
	 * ID: 1010012<br>
	 * Message: The Southern Entrance to the Wastelandss
	 */
	public static final NpcStringId THE_SOUTHERN_ENTRANCE_TO_THE_WASTELANDSS;

	/**
	 * ID: 1010013<br>
	 * Message: Town of Oren
	 */
	public static final NpcStringId TOWN_OF_OREN;

	/**
	 * ID: 1010014<br>
	 * Message: Ivory Tower
	 */
	public static final NpcStringId IVORY_TOWER;

	/**
	 * ID: 1010015<br>
	 * Message: 1st Floor Lobby
	 */
	public static final NpcStringId N1ST_FLOOR_LOBBY;

	/**
	 * ID: 1010016<br>
	 * Message: Underground Shopping Area
	 */
	public static final NpcStringId UNDERGROUND_SHOPPING_AREA;

	/**
	 * ID: 1010017<br>
	 * Message: 2nd Floor Human Wizard Guild
	 */
	public static final NpcStringId N2ND_FLOOR_HUMAN_WIZARD_GUILD;

	/**
	 * ID: 1010018<br>
	 * Message: 3rd Floor Elven Wizard Guild
	 */
	public static final NpcStringId N3RD_FLOOR_ELVEN_WIZARD_GUILD;

	/**
	 * ID: 1010019<br>
	 * Message: 4th Floor Dark Wizard Guild
	 */
	public static final NpcStringId N4TH_FLOOR_DARK_WIZARD_GUILD;

	/**
	 * ID: 1010020<br>
	 * Message: Hunters Village
	 */
	public static final NpcStringId HUNTERS_VILLAGE;

	/**
	 * ID: 1010021<br>
	 * Message: Giran Harbor
	 */
	public static final NpcStringId GIRAN_HARBOR;

	/**
	 * ID: 1010022<br>
	 * Message: Hardin's Private Academy
	 */
	public static final NpcStringId HARDINS_PRIVATE_ACADEMY;

	/**
	 * ID: 1010023<br>
	 * Message: Town of Aden
	 */
	public static final NpcStringId TOWN_OF_ADEN;

	/**
	 * ID: 1010024<br>
	 * Message: Village Square
	 */
	public static final NpcStringId VILLAGE_SQUARE;

	/**
	 * ID: 1010025<br>
	 * Message: North Gate Entrance
	 */
	public static final NpcStringId NORTH_GATE_ENTRANCE;

	/**
	 * ID: 1010026<br>
	 * Message: East Gate Entrance
	 */
	public static final NpcStringId EAST_GATE_ENTRANCE;

	/**
	 * ID: 1010027<br>
	 * Message: West Gate Entrance
	 */
	public static final NpcStringId WEST_GATE_ENTRANCE;

	/**
	 * ID: 1010028<br>
	 * Message: South Gate Entrance
	 */
	public static final NpcStringId SOUTH_GATE_ENTRANCE;

	/**
	 * ID: 1010029<br>
	 * Message: Entrance to Turek Orc Camp
	 */
	public static final NpcStringId ENTRANCE_TO_TUREK_ORC_CAMP;

	/**
	 * ID: 1010030<br>
	 * Message: Entrance to Forgotten Temple
	 */
	public static final NpcStringId ENTRANCE_TO_FORGOTTEN_TEMPLE;

	/**
	 * ID: 1010031<br>
	 * Message: Entrance to the Wastelands
	 */
	public static final NpcStringId ENTRANCE_TO_THE_WASTELANDS;

	/**
	 * ID: 1010032<br>
	 * Message: Entrance to Abandoned Camp
	 */
	public static final NpcStringId ENTRANCE_TO_ABANDONED_CAMP;

	/**
	 * ID: 1010033<br>
	 * Message: Entrance to Cruma Marshlands
	 */
	public static final NpcStringId ENTRANCE_TO_CRUMA_MARSHLANDS;

	/**
	 * ID: 1010034<br>
	 * Message: Entrance to Execution Grounds
	 */
	public static final NpcStringId ENTRANCE_TO_EXECUTION_GROUNDS;

	/**
	 * ID: 1010035<br>
	 * Message: Entrance to the Fortress of Resistance
	 */
	public static final NpcStringId ENTRANCE_TO_THE_FORTRESS_OF_RESISTANCE;

	/**
	 * ID: 1010036<br>
	 * Message: Entrance to Floran Village
	 */
	public static final NpcStringId ENTRANCE_TO_FLORAN_VILLAGE;

	/**
	 * ID: 1010037<br>
	 * Message: Neutral Zone
	 */
	public static final NpcStringId NEUTRAL_ZONE;

	/**
	 * ID: 1010038<br>
	 * Message: Western Road of Giran
	 */
	public static final NpcStringId WESTERN_ROAD_OF_GIRAN;

	/**
	 * ID: 1010039<br>
	 * Message: Eastern Road of Gludin Village
	 */
	public static final NpcStringId EASTERN_ROAD_OF_GLUDIN_VILLAGE;

	/**
	 * ID: 1010041<br>
	 * Message: Entrance to Cruma Tower
	 */
	public static final NpcStringId ENTRANCE_TO_CRUMA_TOWER;

	/**
	 * ID: 1010042<br>
	 * Message: Death Pass
	 */
	public static final NpcStringId DEATH_PASS;

	/**
	 * ID: 1010043<br>
	 * Message: Northern part of the Marshlands
	 */
	public static final NpcStringId NORTHERN_PART_OF_THE_MARSHLANDS;

	/**
	 * ID: 1010044<br>
	 * Message: Northeast of the Neutral Zone
	 */
	public static final NpcStringId NORTHEAST_OF_THE_NEUTRAL_ZONE;

	/**
	 * ID: 1010045<br>
	 * Message: Immortal Plateau, Central Region
	 */
	public static final NpcStringId IMMORTAL_PLATEAU_CENTRAL_REGION;

	/**
	 * ID: 1010046<br>
	 * Message: Immortal Plateau, Southern Region
	 */
	public static final NpcStringId IMMORTAL_PLATEAU_SOUTHERN_REGION;

	/**
	 * ID: 1010047<br>
	 * Message: Immortal Plateau, Southeast Region
	 */
	public static final NpcStringId IMMORTAL_PLATEAU_SOUTHEAST_REGION;

	/**
	 * ID: 1010048<br>
	 * Message: Frozen Waterfall
	 */
	public static final NpcStringId FROZEN_WATERFALL;

	/**
	 * ID: 1010049<br>
	 * Message: Heine
	 */
	public static final NpcStringId HEINE;

	/**
	 * ID: 1010050<br>
	 * Message: Tower of Insolence - 1st Floor
	 */
	public static final NpcStringId TOWER_OF_INSOLENCE_1ST_FLOOR;

	/**
	 * ID: 1010051<br>
	 * Message: Tower of Insolence - 5th Floor
	 */
	public static final NpcStringId TOWER_OF_INSOLENCE_5TH_FLOOR;

	/**
	 * ID: 1010052<br>
	 * Message: Tower of Insolence - 10th Floor
	 */
	public static final NpcStringId TOWER_OF_INSOLENCE_10TH_FLOOR;

	/**
	 * ID: 1010053<br>
	 * Message: Coliseum
	 */
	public static final NpcStringId COLISEUM;

	/**
	 * ID: 1010054<br>
	 * Message: Monster Derby
	 */
	public static final NpcStringId MONSTER_DERBY;

	/**
	 * ID: 1010055<br>
	 * Message: Near the frontier post
	 */
	public static final NpcStringId NEAR_THE_FRONTIER_POST;

	/**
	 * ID: 1010056<br>
	 * Message: Entrance to the Sea of Spores
	 */
	public static final NpcStringId ENTRANCE_TO_THE_SEA_OF_SPORES;

	/**
	 * ID: 1010057<br>
	 * Message: An old battlefield
	 */
	public static final NpcStringId AN_OLD_BATTLEFIELD;

	/**
	 * ID: 1010058<br>
	 * Message: Entrance to Enchanted Valley
	 */
	public static final NpcStringId ENTRANCE_TO_ENCHANTED_VALLEY;

	/**
	 * ID: 1010059<br>
	 * Message: Entrance to the Tower of Insolence
	 */
	public static final NpcStringId ENTRANCE_TO_THE_TOWER_OF_INSOLENCE;

	/**
	 * ID: 1010060<br>
	 * Message: Blazing Swamp
	 */
	public static final NpcStringId BLAZING_SWAMP;

	/**
	 * ID: 1010061<br>
	 * Message: Entrance to the Cemetery
	 */
	public static final NpcStringId ENTRANCE_TO_THE_CEMETERY;

	/**
	 * ID: 1010062<br>
	 * Message: Entrance to The Giant's Cave
	 */
	public static final NpcStringId ENTRANCE_TO_THE_GIANTS_CAVE;

	/**
	 * ID: 1010063<br>
	 * Message: Entrance to the Forest of Mirrors
	 */
	public static final NpcStringId ENTRANCE_TO_THE_FOREST_OF_MIRRORS;

	/**
	 * ID: 1010064<br>
	 * Message: The Forbidden Gateway
	 */
	public static final NpcStringId THE_FORBIDDEN_GATEWAY;

	/**
	 * ID: 1010066<br>
	 * Message: Entrance to the Tanor Silenos Barracks
	 */
	public static final NpcStringId ENTRANCE_TO_THE_TANOR_SILENOS_BARRACKS;

	/**
	 * ID: 1010067<br>
	 * Message: Dragon Valley
	 */
	public static final NpcStringId DRAGON_VALLEY;

	/**
	 * ID: 1010068<br>
	 * Message: Oracle of Dawn
	 */
	public static final NpcStringId ORACLE_OF_DAWN;

	/**
	 * ID: 1010069<br>
	 * Message: Oracle of Dusk
	 */
	public static final NpcStringId ORACLE_OF_DUSK;

	/**
	 * ID: 1010070<br>
	 * Message: Necropolis of Sacrifice
	 */
	public static final NpcStringId NECROPOLIS_OF_SACRIFICE;

	/**
	 * ID: 1010071<br>
	 * Message: The Pilgrim's Necropolis
	 */
	public static final NpcStringId THE_PILGRIMS_NECROPOLIS;

	/**
	 * ID: 1010072<br>
	 * Message: Necropolis of Worship
	 */
	public static final NpcStringId NECROPOLIS_OF_WORSHIP;

	/**
	 * ID: 1010073<br>
	 * Message: The Patriot's Necropolis
	 */
	public static final NpcStringId THE_PATRIOTS_NECROPOLIS;

	/**
	 * ID: 1010074<br>
	 * Message: Necropolis of Devotion
	 */
	public static final NpcStringId NECROPOLIS_OF_DEVOTION;

	/**
	 * ID: 1010075<br>
	 * Message: Necropolis of Martyrdom
	 */
	public static final NpcStringId NECROPOLIS_OF_MARTYRDOM;

	/**
	 * ID: 1010076<br>
	 * Message: The Disciple's Necropolis
	 */
	public static final NpcStringId THE_DISCIPLES_NECROPOLIS;

	/**
	 * ID: 1010077<br>
	 * Message: The Saint's Necropolis
	 */
	public static final NpcStringId THE_SAINTS_NECROPOLIS;

	/**
	 * ID: 1010078<br>
	 * Message: The Catacomb of the Heretic
	 */
	public static final NpcStringId THE_CATACOMB_OF_THE_HERETIC;

	/**
	 * ID: 1010079<br>
	 * Message: Catacomb of the Branded
	 */
	public static final NpcStringId CATACOMB_OF_THE_BRANDED;

	/**
	 * ID: 1010080<br>
	 * Message: Catacomb of the Apostate
	 */
	public static final NpcStringId CATACOMB_OF_THE_APOSTATE;

	/**
	 * ID: 1010081<br>
	 * Message: Catacomb of the Witch
	 */
	public static final NpcStringId CATACOMB_OF_THE_WITCH;

	/**
	 * ID: 1010082<br>
	 * Message: Catacomb of Dark Omens
	 */
	public static final NpcStringId CATACOMB_OF_DARK_OMENS;

	/**
	 * ID: 1010083<br>
	 * Message: Catacomb of the Forbidden Path
	 */
	public static final NpcStringId CATACOMB_OF_THE_FORBIDDEN_PATH;

	/**
	 * ID: 1010084<br>
	 * Message: Entrance to the Ruins of Agony
	 */
	public static final NpcStringId ENTRANCE_TO_THE_RUINS_OF_AGONY;

	/**
	 * ID: 1010085<br>
	 * Message: Entrance to the Ruins of Despair
	 */
	public static final NpcStringId ENTRANCE_TO_THE_RUINS_OF_DESPAIR;

	/**
	 * ID: 1010086<br>
	 * Message: Entrance to the Ant Nest
	 */
	public static final NpcStringId ENTRANCE_TO_THE_ANT_NEST;

	/**
	 * ID: 1010087<br>
	 * Message: Southern Dion
	 */
	public static final NpcStringId SOUTHERN_DION;

	/**
	 * ID: 1010088<br>
	 * Message: Entrance to Dragon Valley
	 */
	public static final NpcStringId ENTRANCE_TO_DRAGON_VALLEY;

	/**
	 * ID: 1010089<br>
	 * Message: Field of Silence
	 */
	public static final NpcStringId FIELD_OF_SILENCE;

	/**
	 * ID: 1010090<br>
	 * Message: Field of Whispers
	 */
	public static final NpcStringId FIELD_OF_WHISPERS;

	/**
	 * ID: 1010091<br>
	 * Message: Entrance to Alligator Island
	 */
	public static final NpcStringId ENTRANCE_TO_ALLIGATOR_ISLAND;

	/**
	 * ID: 1010092<br>
	 * Message: Southern Plain of Oren
	 */
	public static final NpcStringId SOUTHERN_PLAIN_OF_OREN;

	/**
	 * ID: 1010093<br>
	 * Message: Entrance to the Bandit Stronghold
	 */
	public static final NpcStringId ENTRANCE_TO_THE_BANDIT_STRONGHOLD;

	/**
	 * ID: 1010094<br>
	 * Message: Windy Hill
	 */
	public static final NpcStringId WINDY_HILL;

	/**
	 * ID: 1010095<br>
	 * Message: Orc Barracks
	 */
	public static final NpcStringId ORC_BARRACKS;

	/**
	 * ID: 1010096<br>
	 * Message: Fellmere Harvesting Grounds
	 */
	public static final NpcStringId FELLMERE_HARVESTING_GROUNDS;

	/**
	 * ID: 1010097<br>
	 * Message: Ruins of Agony
	 */
	public static final NpcStringId RUINS_OF_AGONY;

	/**
	 * ID: 1010098<br>
	 * Message: Abandoned Camp
	 */
	public static final NpcStringId ABANDONED_CAMP;

	/**
	 * ID: 1010099<br>
	 * Message: Red Rock Ridge
	 */
	public static final NpcStringId RED_ROCK_RIDGE;

	/**
	 * ID: 1010100<br>
	 * Message: Langk Lizardman Dwellings
	 */
	public static final NpcStringId LANGK_LIZARDMAN_DWELLINGS;

	/**
	 * ID: 1010101<br>
	 * Message: Ruins of Despair
	 */
	public static final NpcStringId RUINS_OF_DESPAIR;

	/**
	 * ID: 1010102<br>
	 * Message: Windawood Manor
	 */
	public static final NpcStringId WINDAWOOD_MANOR;

	/**
	 * ID: 1010103<br>
	 * Message: Northern Pathway to the Wastelands
	 */
	public static final NpcStringId NORTHERN_PATHWAY_TO_THE_WASTELANDS;

	/**
	 * ID: 1010104<br>
	 * Message: Western Pathway to the Wastelands
	 */
	public static final NpcStringId WESTERN_PATHWAY_TO_THE_WASTELANDS;

	/**
	 * ID: 1010105<br>
	 * Message: Southern Pathway to the Wastelands
	 */
	public static final NpcStringId SOUTHERN_PATHWAY_TO_THE_WASTELANDS;

	/**
	 * ID: 1010106<br>
	 * Message: Forgotten Temple
	 */
	public static final NpcStringId FORGOTTEN_TEMPLE;

	/**
	 * ID: 1010107<br>
	 * Message: South Entrance of Ant Nest
	 */
	public static final NpcStringId SOUTH_ENTRANCE_OF_ANT_NEST;

	/**
	 * ID: 1010108<br>
	 * Message: East Entrance of Ant Nest
	 */
	public static final NpcStringId EAST_ENTRANCE_OF_ANT_NEST;

	/**
	 * ID: 1010109<br>
	 * Message: West Entrance of Ant Nest
	 */
	public static final NpcStringId WEST_ENTRANCE_OF_ANT_NEST;

	/**
	 * ID: 1010110<br>
	 * Message: Cruma Marshland
	 */
	public static final NpcStringId CRUMA_MARSHLAND;

	/**
	 * ID: 1010111<br>
	 * Message: Plains of Dion
	 */
	public static final NpcStringId PLAINS_OF_DION;

	/**
	 * ID: 1010112<br>
	 * Message: Bee Hive
	 */
	public static final NpcStringId BEE_HIVE;

	/**
	 * ID: 1010113<br>
	 * Message: Fortress of Resistance
	 */
	public static final NpcStringId FORTRESS_OF_RESISTANCE;

	/**
	 * ID: 1010114<br>
	 * Message: Execution Grounds
	 */
	public static final NpcStringId EXECUTION_GROUNDS;

	/**
	 * ID: 1010115<br>
	 * Message: Tanor Canyon
	 */
	public static final NpcStringId TANOR_CANYON;

	/**
	 * ID: 1010116<br>
	 * Message: Cruma Tower
	 */
	public static final NpcStringId CRUMA_TOWER;

	/**
	 * ID: 1010117<br>
	 * Message: Three-way crossroads at Dragon Valley
	 */
	public static final NpcStringId THREE_WAY_CROSSROADS_AT_DRAGON_VALLEY;

	/**
	 * ID: 1010118<br>
	 * Message: Breka's Stronghold
	 */
	public static final NpcStringId BREKAS_STRONGHOLD;

	/**
	 * ID: 1010119<br>
	 * Message: Gorgon Flower Garden
	 */
	public static final NpcStringId GORGON_FLOWER_GARDEN;

	/**
	 * ID: 1010120<br>
	 * Message: Antharas's Lair
	 */
	public static final NpcStringId ANTHARASS_LAIR;

	/**
	 * ID: 1010121<br>
	 * Message: Sea of Spores
	 */
	public static final NpcStringId SEA_OF_SPORES;

	/**
	 * ID: 1010122<br>
	 * Message: Outlaw Forest
	 */
	public static final NpcStringId OUTLAW_FOREST;

	/**
	 * ID: 1010123<br>
	 * Message: Forest of Evil and the Ivory Tower
	 */
	public static final NpcStringId FOREST_OF_EVIL_AND_THE_IVORY_TOWER;

	/**
	 * ID: 1010124<br>
	 * Message: Timak Outpost
	 */
	public static final NpcStringId TIMAK_OUTPOST;

	/**
	 * ID: 1010125<br>
	 * Message: Great Plains of Oren
	 */
	public static final NpcStringId GREAT_PLAINS_OF_OREN;

	/**
	 * ID: 1010126<br>
	 * Message: Alchemist's Hut
	 */
	public static final NpcStringId ALCHEMISTS_HUT;

	/**
	 * ID: 1010127<br>
	 * Message: Ancient Battleground
	 */
	public static final NpcStringId ANCIENT_BATTLEGROUND;

	/**
	 * ID: 1010128<br>
	 * Message: Northern Pathway of the Enchanted Valley
	 */
	public static final NpcStringId NORTHERN_PATHWAY_OF_THE_ENCHANTED_VALLEY;

	/**
	 * ID: 1010129<br>
	 * Message: Southern Pathway of the Enchanted Valley
	 */
	public static final NpcStringId SOUTHERN_PATHWAY_OF_THE_ENCHANTED_VALLEY;

	/**
	 * ID: 1010130<br>
	 * Message: Hunters Valley
	 */
	public static final NpcStringId HUNTERS_VALLEY;

	/**
	 * ID: 1010131<br>
	 * Message: Western Entrance of Blazing Swamp
	 */
	public static final NpcStringId WESTERN_ENTRANCE_OF_BLAZING_SWAMP;

	/**
	 * ID: 1010132<br>
	 * Message: Eastern Entrance of Blazing Swamp
	 */
	public static final NpcStringId EASTERN_ENTRANCE_OF_BLAZING_SWAMP;

	/**
	 * ID: 1010133<br>
	 * Message: Plains of Glory
	 */
	public static final NpcStringId PLAINS_OF_GLORY;

	/**
	 * ID: 1010134<br>
	 * Message: War-Torn Plains
	 */
	public static final NpcStringId WAR_TORN_PLAINS;

	/**
	 * ID: 1010135<br>
	 * Message: Northwestern Passage to the Forest of Mirrors
	 */
	public static final NpcStringId NORTHWESTERN_PASSAGE_TO_THE_FOREST_OF_MIRRORS;

	/**
	 * ID: 1010136<br>
	 * Message: The Front of Anghel Waterfall
	 */
	public static final NpcStringId THE_FRONT_OF_ANGHEL_WATERFALL;

	/**
	 * ID: 1010137<br>
	 * Message: South Entrance of Devastated Castle
	 */
	public static final NpcStringId SOUTH_ENTRANCE_OF_DEVASTATED_CASTLE;

	/**
	 * ID: 1010138<br>
	 * Message: North Entrance of Devastated Castle
	 */
	public static final NpcStringId NORTH_ENTRANCE_OF_DEVASTATED_CASTLE;

	/**
	 * ID: 1010139<br>
	 * Message: North Entrance of the Cemetery
	 */
	public static final NpcStringId NORTH_ENTRANCE_OF_THE_CEMETERY;

	/**
	 * ID: 1010140<br>
	 * Message: South Entrance of the Cemetery
	 */
	public static final NpcStringId SOUTH_ENTRANCE_OF_THE_CEMETERY;

	/**
	 * ID: 1010141<br>
	 * Message: West Entrance of the Cemetery
	 */
	public static final NpcStringId WEST_ENTRANCE_OF_THE_CEMETERY;

	/**
	 * ID: 1010142<br>
	 * Message: Entrance of the Forbidden Gateway
	 */
	public static final NpcStringId ENTRANCE_OF_THE_FORBIDDEN_GATEWAY;

	/**
	 * ID: 1010143<br>
	 * Message: Forsaken Plains
	 */
	public static final NpcStringId FORSAKEN_PLAINS;

	/**
	 * ID: 1010144<br>
	 * Message: Tower of Insolence
	 */
	public static final NpcStringId TOWER_OF_INSOLENCE;

	/**
	 * ID: 1010145<br>
	 * Message: The Giant's Cave
	 */
	public static final NpcStringId THE_GIANTS_CAVE;

	/**
	 * ID: 1010146<br>
	 * Message: Northern Part of the Field of Silence
	 */
	public static final NpcStringId NORTHERN_PART_OF_THE_FIELD_OF_SILENCE;

	/**
	 * ID: 1010147<br>
	 * Message: Western Part of the Field of Silence
	 */
	public static final NpcStringId WESTERN_PART_OF_THE_FIELD_OF_SILENCE;

	/**
	 * ID: 1010148<br>
	 * Message: Eastern Part of the Field of Silence
	 */
	public static final NpcStringId EASTERN_PART_OF_THE_FIELD_OF_SILENCE;

	/**
	 * ID: 1010149<br>
	 * Message: Western Part of the Field of Whispers
	 */
	public static final NpcStringId WESTERN_PART_OF_THE_FIELD_OF_WHISPERS;

	/**
	 * ID: 1010150<br>
	 * Message: Alligator Island
	 */
	public static final NpcStringId ALLIGATOR_ISLAND;

	/**
	 * ID: 1010151<br>
	 * Message: Alligator Beach
	 */
	public static final NpcStringId ALLIGATOR_BEACH;

	/**
	 * ID: 1010152<br>
	 * Message: Devil's Isle
	 */
	public static final NpcStringId DEVILS_ISLE;

	/**
	 * ID: 1010153<br>
	 * Message: Garden of Eva
	 */
	public static final NpcStringId GARDEN_OF_EVA;

	/**
	 * ID: 1010154<br>
	 * Message: Talking Island
	 */
	public static final NpcStringId TALKING_ISLAND;

	/**
	 * ID: 1010155<br>
	 * Message: Elven Village
	 */
	public static final NpcStringId ELVEN_VILLAGE;

	/**
	 * ID: 1010156<br>
	 * Message: Dark Elf Village
	 */
	public static final NpcStringId DARK_ELF_VILLAGE;

	/**
	 * ID: 1010159<br>
	 * Message: Scenic Deck of Iris Lake
	 */
	public static final NpcStringId SCENIC_DECK_OF_IRIS_LAKE;

	/**
	 * ID: 1010160<br>
	 * Message: Altar of Rites
	 */
	public static final NpcStringId ALTAR_OF_RITES;

	/**
	 * ID: 1010161<br>
	 * Message: Dark Forest, Waterfall
	 */
	public static final NpcStringId DARK_FOREST_WATERFALL;

	/**
	 * ID: 1010162<br>
	 * Message: Three-way Crossroads of the Neutral Zone
	 */
	public static final NpcStringId THREE_WAY_CROSSROADS_OF_THE_NEUTRAL_ZONE;

	/**
	 * ID: 1010163<br>
	 * Message: Dark Forest
	 */
	public static final NpcStringId DARK_FOREST;

	/**
	 * ID: 1010164<br>
	 * Message: Swampland
	 */
	public static final NpcStringId SWAMPLAND;

	/**
	 * ID: 1010165<br>
	 * Message: Black Rock Hill
	 */
	public static final NpcStringId BLACK_ROCK_HILL;

	/**
	 * ID: 1010166<br>
	 * Message: Spider Nest
	 */
	public static final NpcStringId SPIDER_NEST;

	/**
	 * ID: 1010167<br>
	 * Message: Elven Forest
	 */
	public static final NpcStringId ELVEN_FOREST;

	/**
	 * ID: 1010168<br>
	 * Message: Obelisk of Victory
	 */
	public static final NpcStringId OBELISK_OF_VICTORY;

	/**
	 * ID: 1010169<br>
	 * Message: Northern Territory of Talking Island
	 */
	public static final NpcStringId NORTHERN_TERRITORY_OF_TALKING_ISLAND;

	/**
	 * ID: 1010170<br>
	 * Message: Southern Territory of Talking Island
	 */
	public static final NpcStringId SOUTHERN_TERRITORY_OF_TALKING_ISLAND;

	/**
	 * ID: 1010171<br>
	 * Message: Evil Hunting Grounds
	 */
	public static final NpcStringId EVIL_HUNTING_GROUNDS;

	/**
	 * ID: 1010172<br>
	 * Message: Maille Lizardmen Barracks
	 */
	public static final NpcStringId MAILLE_LIZARDMEN_BARRACKS;

	/**
	 * ID: 1010173<br>
	 * Message: Ruins of Agony Bend
	 */
	public static final NpcStringId RUINS_OF_AGONY_BEND;

	/**
	 * ID: 1010174<br>
	 * Message: The Entrance to the Ruins of Despair
	 */
	public static final NpcStringId THE_ENTRANCE_TO_THE_RUINS_OF_DESPAIR;

	/**
	 * ID: 1010175<br>
	 * Message: Windmill Hill
	 */
	public static final NpcStringId WINDMILL_HILL;

	/**
	 * ID: 1010177<br>
	 * Message: Floran Agricultural Area
	 */
	public static final NpcStringId FLORAN_AGRICULTURAL_AREA;

	/**
	 * ID: 1010178<br>
	 * Message: Western Tanor Canyon
	 */
	public static final NpcStringId WESTERN_TANOR_CANYON;

	/**
	 * ID: 1010179<br>
	 * Message: Plains of the Lizardmen
	 */
	public static final NpcStringId PLAINS_OF_THE_LIZARDMEN;

	/**
	 * ID: 1010180<br>
	 * Message: Forest of Evil
	 */
	public static final NpcStringId FOREST_OF_EVIL;

	/**
	 * ID: 1010181<br>
	 * Message: Fields of Massacre
	 */
	public static final NpcStringId FIELDS_OF_MASSACRE;

	/**
	 * ID: 1010182<br>
	 * Message: Silent Valley
	 */
	public static final NpcStringId SILENT_VALLEY;

	/**
	 * ID: 1010183<br>
	 * Message: Northern Area of the Immortal Plateau, Northern Region
	 */
	public static final NpcStringId NORTHERN_AREA_OF_THE_IMMORTAL_PLATEAU_NORTHERN_REGION;

	/**
	 * ID: 1010184<br>
	 * Message: Southern Area of the Immortal Plateau, Northern Region
	 */
	public static final NpcStringId SOUTHERN_AREA_OF_THE_IMMORTAL_PLATEAU_NORTHERN_REGION;

	/**
	 * ID: 1010185<br>
	 * Message: Northern Area of the Immortal Plateau, Southern Region
	 */
	public static final NpcStringId NORTHERN_AREA_OF_THE_IMMORTAL_PLATEAU_SOUTHERN_REGION;

	/**
	 * ID: 1010186<br>
	 * Message: Southern Area of the Immortal Plateau, Southern Region
	 */
	public static final NpcStringId SOUTHERN_AREA_OF_THE_IMMORTAL_PLATEAU_SOUTHERN_REGION;

	/**
	 * ID: 1010187<br>
	 * Message: Western Mining Zone
	 */
	public static final NpcStringId WESTERN_MINING_ZONE;

	/**
	 * ID: 1010190<br>
	 * Message: Entrance to the Abandoned Coal Mines
	 */
	public static final NpcStringId ENTRANCE_TO_THE_ABANDONED_COAL_MINES;

	/**
	 * ID: 1010191<br>
	 * Message: Entrance to the Mithril Mines
	 */
	public static final NpcStringId ENTRANCE_TO_THE_MITHRIL_MINES;

	/**
	 * ID: 1010192<br>
	 * Message: West Area of the Devastated Castle
	 */
	public static final NpcStringId WEST_AREA_OF_THE_DEVASTATED_CASTLE;

	/**
	 * ID: 1010193<br>
	 * Message: Tower of Insolence, 3rd Floor
	 */
	public static final NpcStringId TOWER_OF_INSOLENCE_3RD_FLOOR;

	/**
	 * ID: 1010195<br>
	 * Message: Tower of Insolence, 7th Floor
	 */
	public static final NpcStringId TOWER_OF_INSOLENCE_7TH_FLOOR;

	/**
	 * ID: 1010197<br>
	 * Message: Tower of Insolence, 13th Floor
	 */
	public static final NpcStringId TOWER_OF_INSOLENCE_13TH_FLOOR;

	/**
	 * ID: 1010199<br>
	 * Message: Town of Goddard
	 */
	public static final NpcStringId TOWN_OF_GODDARD;

	/**
	 * ID: 1010200<br>
	 * Message: Rune Township
	 */
	public static final NpcStringId RUNE_TOWNSHIP;

	/**
	 * ID: 1010201<br>
	 * Message: A delivery for Mr. Lector? Very good!
	 */
	public static final NpcStringId A_DELIVERY_FOR_MR_LECTOR_VERY_GOOD;

	/**
	 * ID: 1010202<br>
	 * Message: I need a break!
	 */
	public static final NpcStringId I_NEED_A_BREAK;

	/**
	 * ID: 1010203<br>
	 * Message: Hello, Mr. Lector! Long time no see, Mr. Jackson!
	 */
	public static final NpcStringId HELLO_MR_LECTOR_LONG_TIME_NO_SEE_MR_JACKSON;

	/**
	 * ID: 1010204<br>
	 * Message: Lulu!
	 */
	public static final NpcStringId LULU;

	/**
	 * ID: 1010205<br>
	 * Message: Where has he gone?
	 */
	public static final NpcStringId WHERE_HAS_HE_GONE;

	/**
	 * ID: 1010206<br>
	 * Message: Have you seen Windawood?
	 */
	public static final NpcStringId HAVE_YOU_SEEN_WINDAWOOD;

	/**
	 * ID: 1010207<br>
	 * Message: Where did he go?
	 */
	public static final NpcStringId WHERE_DID_HE_GO;

	/**
	 * ID: 1010208<br>
	 * Message: The Mother Tree is slowly dying.
	 */
	public static final NpcStringId THE_MOTHER_TREE_IS_SLOWLY_DYING;

	/**
	 * ID: 1010209<br>
	 * Message: How can we save the Mother Tree?
	 */
	public static final NpcStringId HOW_CAN_WE_SAVE_THE_MOTHER_TREE;

	/**
	 * ID: 1010210<br>
	 * Message: The Mother Tree is always so gorgeous!
	 */
	public static final NpcStringId THE_MOTHER_TREE_IS_ALWAYS_SO_GORGEOUS;

	/**
	 * ID: 1010211<br>
	 * Message: Lady Mirabel, may the peace of the lake be with you!
	 */
	public static final NpcStringId LADY_MIRABEL_MAY_THE_PEACE_OF_THE_LAKE_BE_WITH_YOU;

	/**
	 * ID: 1010212<br>
	 * Message: You're a hard worker, Rayla!
	 */
	public static final NpcStringId YOURE_A_HARD_WORKER_RAYLA;

	/**
	 * ID: 1010213<br>
	 * Message: You're a hard worker!
	 */
	public static final NpcStringId YOURE_A_HARD_WORKER;

	/**
	 * ID: 1010214<br>
	 * Message: The mass of darkness will start in a couple of days. Pay more attention to the guard!
	 */
	public static final NpcStringId THE_MASS_OF_DARKNESS_WILL_START_IN_A_COUPLE_OF_DAYS_PAY_MORE_ATTENTION_TO_THE_GUARD;

	/**
	 * ID: 1010215<br>
	 * Message: Have you seen Torocco today?
	 */
	public static final NpcStringId HAVE_YOU_SEEN_TOROCCO_TODAY;

	/**
	 * ID: 1010216<br>
	 * Message: Have you seen Torocco?
	 */
	public static final NpcStringId HAVE_YOU_SEEN_TOROCCO;

	/**
	 * ID: 1010217<br>
	 * Message: Where is that fool hiding?
	 */
	public static final NpcStringId WHERE_IS_THAT_FOOL_HIDING;

	/**
	 * ID: 1010218<br>
	 * Message: Care to go a round?
	 */
	public static final NpcStringId CARE_TO_GO_A_ROUND;

	/**
	 * ID: 1010219<br>
	 * Message: Have a nice day, Mr. Garita and Mion!
	 */
	public static final NpcStringId HAVE_A_NICE_DAY_MR_GARITA_AND_MION;

	/**
	 * ID: 1010220<br>
	 * Message: Mr. Lid, Murdoc, and Airy! How are you doing?
	 */
	public static final NpcStringId MR_LID_MURDOC_AND_AIRY_HOW_ARE_YOU_DOING;

	/**
	 * ID: 1010221<br>
	 * Message: A black moon... Now do you understand that he has opened his eyes?
	 */
	public static final NpcStringId A_BLACK_MOON_NOW_DO_YOU_UNDERSTAND_THAT_HE_HAS_OPENED_HIS_EYES;

	/**
	 * ID: 1010222<br>
	 * Message: Clouds of blood are gathering. Soon, it will start to rain. The rain of crimson blood...
	 */
	public static final NpcStringId CLOUDS_OF_BLOOD_ARE_GATHERING_SOON_IT_WILL_START_TO_RAIN_THE_RAIN_OF_CRIMSON_BLOOD;

	/**
	 * ID: 1010223<br>
	 * Message: While the foolish light was asleep, the darkness will awaken first. Uh huh huh...
	 */
	public static final NpcStringId WHILE_THE_FOOLISH_LIGHT_WAS_ASLEEP_THE_DARKNESS_WILL_AWAKEN_FIRST_UH_HUH_HUH;

	/**
	 * ID: 1010224<br>
	 * Message: It is the deepest darkness. With its arrival, the world will soon die.
	 */
	public static final NpcStringId IT_IS_THE_DEEPEST_DARKNESS_WITH_ITS_ARRIVAL_THE_WORLD_WILL_SOON_DIE;

	/**
	 * ID: 1010225<br>
	 * Message: Death is just a new beginning. Huhu... Fear not.
	 */
	public static final NpcStringId DEATH_IS_JUST_A_NEW_BEGINNING_HUHU_FEAR_NOT;

	/**
	 * ID: 1010226<br>
	 * Message: Ahh! Beautiful goddess of death! Cover over the filth of this world with your darkness!
	 */
	public static final NpcStringId
			AHH_BEAUTIFUL_GODDESS_OF_DEATH_COVER_OVER_THE_FILTH_OF_THIS_WORLD_WITH_YOUR_DARKNESS;

	/**
	 * ID: 1010227<br>
	 * Message: The goddess's resurrection has already begun. Huhu... Insignificant creatures like you can do nothing!
	 */
	public static final NpcStringId
			THE_GODDESSS_RESURRECTION_HAS_ALREADY_BEGUN_HUHU_INSIGNIFICANT_CREATURES_LIKE_YOU_CAN_DO_NOTHING;

	/**
	 * ID: 1010400<br>
	 * Message: Croak, Croak! Food like $s1 in this place?!
	 */
	public static final NpcStringId CROAK_CROAK_FOOD_LIKE_S1_IN_THIS_PLACE;

	/**
	 * ID: 1010401<br>
	 * Message: $s1, How lucky I am!
	 */
	public static final NpcStringId S1_HOW_LUCKY_I_AM;

	/**
	 * ID: 1010402<br>
	 * Message: Pray that you caught a wrong fish $s1!
	 */
	public static final NpcStringId PRAY_THAT_YOU_CAUGHT_A_WRONG_FISH_S1;

	/**
	 * ID: 1010403<br>
	 * Message: Do you know what a frog tastes like?
	 */
	public static final NpcStringId DO_YOU_KNOW_WHAT_A_FROG_TASTES_LIKE;

	/**
	 * ID: 1010404<br>
	 * Message: I will show you the power of a frog!
	 */
	public static final NpcStringId I_WILL_SHOW_YOU_THE_POWER_OF_A_FROG;

	/**
	 * ID: 1010405<br>
	 * Message: I will swallow at a mouthful!
	 */
	public static final NpcStringId I_WILL_SWALLOW_AT_A_MOUTHFUL;

	/**
	 * ID: 1010406<br>
	 * Message: Ugh, no chance.  How could this elder pass away like this!
	 */
	public static final NpcStringId UGH_NO_CHANCE_HOW_COULD_THIS_ELDER_PASS_AWAY_LIKE_THIS;

	/**
	 * ID: 1010407<br>
	 * Message: Croak! Croak! A frog is dying!
	 */
	public static final NpcStringId CROAK_CROAK_A_FROG_IS_DYING;

	/**
	 * ID: 1010408<br>
	 * Message: A frog tastes bad!  Yuck!
	 */
	public static final NpcStringId A_FROG_TASTES_BAD_YUCK;

	/**
	 * ID: 1010409<br>
	 * Message: Kaak! $s1, What are you doing now?
	 */
	public static final NpcStringId KAAK_S1_WHAT_ARE_YOU_DOING_NOW;

	/**
	 * ID: 1010410<br>
	 * Message: Huh, $s1 You pierced into the body of the Spirit with a needle. Are you ready?
	 */
	public static final NpcStringId HUH_S1_YOU_PIERCED_INTO_THE_BODY_OF_THE_SPIRIT_WITH_A_NEEDLE_ARE_YOU_READY;

	/**
	 * ID: 1010411<br>
	 * Message: Ooh $s1 That's you. But, no lady is pleased with this savage invitation!
	 */
	public static final NpcStringId OOH_S1_THATS_YOU_BUT_NO_LADY_IS_PLEASED_WITH_THIS_SAVAGE_INVITATION;

	/**
	 * ID: 1010412<br>
	 * Message: You made me angry!
	 */
	public static final NpcStringId YOU_MADE_ME_ANGRY;

	/**
	 * ID: 1010413<br>
	 * Message: It is but a scratch!  Is that all you can do?!
	 */
	public static final NpcStringId IT_IS_BUT_A_SCRATCH_IS_THAT_ALL_YOU_CAN_DO;

	/**
	 * ID: 1010414<br>
	 * Message: Feel my pain!
	 */
	public static final NpcStringId FEEL_MY_PAIN;

	/**
	 * ID: 1010415<br>
	 * Message: I'll get you for this!
	 */
	public static final NpcStringId ILL_GET_YOU_FOR_THIS;

	/**
	 * ID: 1010416<br>
	 * Message: I will tell fish not to take your bait!
	 */
	public static final NpcStringId I_WILL_TELL_FISH_NOT_TO_TAKE_YOUR_BAIT;

	/**
	 * ID: 1010417<br>
	 * Message: You bothered such a weak spirit...Huh, Huh
	 */
	public static final NpcStringId YOU_BOTHERED_SUCH_A_WEAK_SPIRITHUH_HUH;

	/**
	 * ID: 1010418<br>
	 * Message: Ke, ke, ke..$s1...I'm eating..Ke..
	 */
	public static final NpcStringId KE_KE_KES1IM_EATINGKE;

	/**
	 * ID: 1010419<br>
	 * Message: Kuh..Ooh..$s1..Enmity...Fish....
	 */
	public static final NpcStringId KUHOOHS1ENMITYFISH;

	/**
	 * ID: 1010420<br>
	 * Message: $s1...? Huh... Huh..huh...
	 */
	public static final NpcStringId S1_HUH_HUHHUH;

	/**
	 * ID: 1010421<br>
	 * Message: Ke, ke, ke, Rakul! Spin! Eh, eh, eh!
	 */
	public static final NpcStringId KE_KE_KE_RAKUL_SPIN_EH_EH_EH;

	/**
	 * ID: 1010422<br>
	 * Message: Ah! Fafurion! Ah! Ah!
	 */
	public static final NpcStringId AH_FAFURION_AH_AH;

	/**
	 * ID: 1010423<br>
	 * Message: Rakul! Rakul! Ra-kul!
	 */
	public static final NpcStringId RAKUL_RAKUL_RA_KUL;

	/**
	 * ID: 1010424<br>
	 * Message: Eh..Enmity...Fish...
	 */
	public static final NpcStringId EHENMITYFISH;

	/**
	 * ID: 1010425<br>
	 * Message: I won't be eaten up...Kah, ah, ah
	 */
	public static final NpcStringId I_WONT_BE_EATEN_UPKAH_AH_AH;

	/**
	 * ID: 1010426<br>
	 * Message: Cough! Rakul! Cough, Cough! Keh...
	 */
	public static final NpcStringId COUGH_RAKUL_COUGH_COUGH_KEH;

	/**
	 * ID: 1010427<br>
	 * Message: Glory to Fafurion! Death to $s1!
	 */
	public static final NpcStringId GLORY_TO_FAFURION_DEATH_TO_S1;

	/**
	 * ID: 1010428<br>
	 * Message: $s1! You are the one who bothered my poor fish!
	 */
	public static final NpcStringId S1_YOU_ARE_THE_ONE_WHO_BOTHERED_MY_POOR_FISH;

	/**
	 * ID: 1010429<br>
	 * Message: Fafurion! A curse upon $s1!
	 */
	public static final NpcStringId FAFURION_A_CURSE_UPON_S1;

	/**
	 * ID: 1010430<br>
	 * Message: Giant Special Attack!
	 */
	public static final NpcStringId GIANT_SPECIAL_ATTACK;

	/**
	 * ID: 1010431<br>
	 * Message: Know the enmity of fish!
	 */
	public static final NpcStringId KNOW_THE_ENMITY_OF_FISH;

	/**
	 * ID: 1010432<br>
	 * Message: I will show you the power of a spear!
	 */
	public static final NpcStringId I_WILL_SHOW_YOU_THE_POWER_OF_A_SPEAR;

	/**
	 * ID: 1010433<br>
	 * Message: Glory to Fafurion!
	 */
	public static final NpcStringId GLORY_TO_FAFURION;

	/**
	 * ID: 1010434<br>
	 * Message: Yipes!
	 */
	public static final NpcStringId YIPES;

	/**
	 * ID: 1010435<br>
	 * Message: An old soldier does not die..! but just disappear...!
	 */
	public static final NpcStringId AN_OLD_SOLDIER_DOES_NOT_DIE_BUT_JUST_DISAPPEAR;

	/**
	 * ID: 1010436<br>
	 * Message: $s1, Take my challenge, the knight of water!
	 */
	public static final NpcStringId S1_TAKE_MY_CHALLENGE_THE_KNIGHT_OF_WATER;

	/**
	 * ID: 1010437<br>
	 * Message: Discover $s1 in the treasure chest of fish!
	 */
	public static final NpcStringId DISCOVER_S1_IN_THE_TREASURE_CHEST_OF_FISH;

	/**
	 * ID: 1010438<br>
	 * Message: $s1, I took your bait!
	 */
	public static final NpcStringId S1_I_TOOK_YOUR_BAIT;

	/**
	 * ID: 1010439<br>
	 * Message: I will show you spearmanship used in Dragon King's Palace!
	 */
	public static final NpcStringId I_WILL_SHOW_YOU_SPEARMANSHIP_USED_IN_DRAGON_KINGS_PALACE;

	/**
	 * ID: 1010440<br>
	 * Message: This is the last gift I give you!
	 */
	public static final NpcStringId THIS_IS_THE_LAST_GIFT_I_GIVE_YOU;

	/**
	 * ID: 1010441<br>
	 * Message: Your bait was too delicious! Now, I will kill you!
	 */
	public static final NpcStringId YOUR_BAIT_WAS_TOO_DELICIOUS_NOW_I_WILL_KILL_YOU;

	/**
	 * ID: 1010442<br>
	 * Message: What a regret! The enmity of my brethren!
	 */
	public static final NpcStringId WHAT_A_REGRET_THE_ENMITY_OF_MY_BRETHREN;

	/**
	 * ID: 1010443<br>
	 * Message: I'll pay you back! Somebody will have my revenge!
	 */
	public static final NpcStringId ILL_PAY_YOU_BACK_SOMEBODY_WILL_HAVE_MY_REVENGE;

	/**
	 * ID: 1010444<br>
	 * Message: Cough... But, I won't be eaten up by you...!
	 */
	public static final NpcStringId COUGH_BUT_I_WONT_BE_EATEN_UP_BY_YOU;

	/**
	 * ID: 1010445<br>
	 * Message: ....? $s1... I will kill you...
	 */
	public static final NpcStringId _S1_I_WILL_KILL_YOU;

	/**
	 * ID: 1010446<br>
	 * Message: $s1... How could you catch me from the deep sea...
	 */
	public static final NpcStringId S1_HOW_COULD_YOU_CATCH_ME_FROM_THE_DEEP_SEA;

	/**
	 * ID: 1010447<br>
	 * Message: $s1... Do you think I am a fish?
	 */
	public static final NpcStringId S1_DO_YOU_THINK_I_AM_A_FISH;

	/**
	 * ID: 1010448<br>
	 * Message: Ebibibi~
	 */
	public static final NpcStringId EBIBIBI;

	/**
	 * ID: 1010449<br>
	 * Message: He, he, he. Do you want me to roast you well?
	 */
	public static final NpcStringId HE_HE_HE_DO_YOU_WANT_ME_TO_ROAST_YOU_WELL;

	/**
	 * ID: 1010450<br>
	 * Message: You didn't keep your eyes on me because I come from the sea?
	 */
	public static final NpcStringId YOU_DIDNT_KEEP_YOUR_EYES_ON_ME_BECAUSE_I_COME_FROM_THE_SEA;

	/**
	 * ID: 1010451<br>
	 * Message: Eeek... I feel sick...yow...!
	 */
	public static final NpcStringId EEEK_I_FEEL_SICKYOW;

	/**
	 * ID: 1010452<br>
	 * Message: I have failed...
	 */
	public static final NpcStringId I_HAVE_FAILED;

	/**
	 * ID: 1010453<br>
	 * Message: Activity of life... Is stopped... Chizifc...
	 */
	public static final NpcStringId ACTIVITY_OF_LIFE_IS_STOPPED_CHIZIFC;

	/**
	 * ID: 1010454<br>
	 * Message: Growling! $s1~ Growling!
	 */
	public static final NpcStringId GROWLING_S1_GROWLING;

	/**
	 * ID: 1010455<br>
	 * Message: I can smell $s1..!
	 */
	public static final NpcStringId I_CAN_SMELL_S1;

	/**
	 * ID: 1010456<br>
	 * Message: Looks delicious, $s1!
	 */
	public static final NpcStringId LOOKS_DELICIOUS_S1;

	/**
	 * ID: 1010457<br>
	 * Message: I will catch you!
	 */
	public static final NpcStringId I_WILL_CATCH_YOU;

	/**
	 * ID: 1010458<br>
	 * Message: Uah, ah, ah, I couldn't eat anything for a long time!
	 */
	public static final NpcStringId UAH_AH_AH_I_COULDNT_EAT_ANYTHING_FOR_A_LONG_TIME;

	/**
	 * ID: 1010459<br>
	 * Message: I can swallow you at a mouthful!
	 */
	public static final NpcStringId I_CAN_SWALLOW_YOU_AT_A_MOUTHFUL;

	/**
	 * ID: 1010460<br>
	 * Message: What?!  I am defeated by the prey!
	 */
	public static final NpcStringId WHAT_I_AM_DEFEATED_BY_THE_PREY;

	/**
	 * ID: 1010461<br>
	 * Message: You are my food! I have to kill you!
	 */
	public static final NpcStringId YOU_ARE_MY_FOOD_I_HAVE_TO_KILL_YOU;

	/**
	 * ID: 1010462<br>
	 * Message: I can't believe I am eaten up by my prey...  Gah!
	 */
	public static final NpcStringId I_CANT_BELIEVE_I_AM_EATEN_UP_BY_MY_PREY_GAH;

	/**
	 * ID: 1010463<br>
	 * Message: ....You caught me, $s1...?
	 */
	public static final NpcStringId YOU_CAUGHT_ME_S1;

	/**
	 * ID: 1010464<br>
	 * Message: You're lucky to have even seen me, $s1.
	 */
	public static final NpcStringId YOURE_LUCKY_TO_HAVE_EVEN_SEEN_ME_S1;

	/**
	 * ID: 1010465<br>
	 * Message: $s1, you can't leave here alive. Give up.
	 */
	public static final NpcStringId S1_YOU_CANT_LEAVE_HERE_ALIVE_GIVE_UP;

	/**
	 * ID: 1010466<br>
	 * Message: I will show you the power of the deep sea!
	 */
	public static final NpcStringId I_WILL_SHOW_YOU_THE_POWER_OF_THE_DEEP_SEA;

	/**
	 * ID: 1010467<br>
	 * Message: I will break the fishing pole!
	 */
	public static final NpcStringId I_WILL_BREAK_THE_FISHING_POLE;

	/**
	 * ID: 1010468<br>
	 * Message: Your corpse will be good food for me!
	 */
	public static final NpcStringId YOUR_CORPSE_WILL_BE_GOOD_FOOD_FOR_ME;

	/**
	 * ID: 1010469<br>
	 * Message: You are a good fisherman.
	 */
	public static final NpcStringId YOU_ARE_A_GOOD_FISHERMAN;

	/**
	 * ID: 1010470<br>
	 * Message: Aren't you afraid of Fafurion?!
	 */
	public static final NpcStringId ARENT_YOU_AFRAID_OF_FAFURION;

	/**
	 * ID: 1010471<br>
	 * Message: You are excellent....
	 */
	public static final NpcStringId YOU_ARE_EXCELLENT;

	/**
	 * ID: 1010472<br>
	 * Message: The Poison device has been activated.
	 */
	public static final NpcStringId THE_POISON_DEVICE_HAS_BEEN_ACTIVATED;

	/**
	 * ID: 1010473<br>
	 * Message: The P. Atk. reduction device will be activated in 1 minute.
	 */
	public static final NpcStringId THE_P_ATK_REDUCTION_DEVICE_WILL_BE_ACTIVATED_IN_1_MINUTE;

	/**
	 * ID: 1010474<br>
	 * Message: The Defense reduction device will be activated in 2 minutes.
	 */
	public static final NpcStringId THE_DEFENSE_REDUCTION_DEVICE_WILL_BE_ACTIVATED_IN_2_MINUTES;

	/**
	 * ID: 1010475<br>
	 * Message: The HP regeneration reduction device will be activated in 3 minutes.
	 */
	public static final NpcStringId THE_HP_REGENERATION_REDUCTION_DEVICE_WILL_BE_ACTIVATED_IN_3_MINUTES;

	/**
	 * ID: 1010476<br>
	 * Message: The P. Atk. reduction device has been activated.
	 */
	public static final NpcStringId THE_P_ATK_REDUCTION_DEVICE_HAS_BEEN_ACTIVATED;

	/**
	 * ID: 1010477<br>
	 * Message: The Defense reduction device has been activated.
	 */
	public static final NpcStringId THE_DEFENSE_REDUCTION_DEVICE_HAS_BEEN_ACTIVATED;

	/**
	 * ID: 1010478<br>
	 * Message: The HP regeneration reduction device has been activated.
	 */
	public static final NpcStringId THE_HP_REGENERATION_REDUCTION_DEVICE_HAS_BEEN_ACTIVATED;

	/**
	 * ID: 1010479<br>
	 * Message: The poison device has now been destroyed.
	 */
	public static final NpcStringId THE_POISON_DEVICE_HAS_NOW_BEEN_DESTROYED;

	/**
	 * ID: 1010480<br>
	 * Message: The P. Atk. reduction device has now been destroyed.
	 */
	public static final NpcStringId THE_P_ATK_REDUCTION_DEVICE_HAS_NOW_BEEN_DESTROYED;

	/**
	 * ID: 1010481<br>
	 * Message: The Defense reduction device has been destroyed.
	 */
	public static final NpcStringId THE_DEFENSE_REDUCTION_DEVICE_HAS_BEEN_DESTROYED;

	/**
	 * ID: 1010485<br>
	 * Message: Entrance to the Cave of Trials
	 */
	public static final NpcStringId ENTRANCE_TO_THE_CAVE_OF_TRIALS;

	/**
	 * ID: 1010486<br>
	 * Message: Inside the Elven Ruins
	 */
	public static final NpcStringId INSIDE_THE_ELVEN_RUINS;

	/**
	 * ID: 1010487<br>
	 * Message: Entrance to the Elven Ruins
	 */
	public static final NpcStringId ENTRANCE_TO_THE_ELVEN_RUINS;

	/**
	 * ID: 1010488<br>
	 * Message: Entrance to the School of Dark Arts
	 */
	public static final NpcStringId ENTRANCE_TO_THE_SCHOOL_OF_DARK_ARTS;

	/**
	 * ID: 1010489<br>
	 * Message: Center of the School of Dark Arts
	 */
	public static final NpcStringId CENTER_OF_THE_SCHOOL_OF_DARK_ARTS;

	/**
	 * ID: 1010490<br>
	 * Message: Entrance to the Elven Fortress
	 */
	public static final NpcStringId ENTRANCE_TO_THE_ELVEN_FORTRESS;

	/**
	 * ID: 1010491<br>
	 * Message: Varka Silenos Stronghold
	 */
	public static final NpcStringId VARKA_SILENOS_STRONGHOLD;

	/**
	 * ID: 1010492<br>
	 * Message: Ketra Orc Outpost
	 */
	public static final NpcStringId KETRA_ORC_OUTPOST;

	/**
	 * ID: 1010493<br>
	 * Message: Rune Township Guild
	 */
	public static final NpcStringId RUNE_TOWNSHIP_GUILD;

	/**
	 * ID: 1010494<br>
	 * Message: Rune Township Temple
	 */
	public static final NpcStringId RUNE_TOWNSHIP_TEMPLE;

	/**
	 * ID: 1010495<br>
	 * Message: Rune Township Store
	 */
	public static final NpcStringId RUNE_TOWNSHIP_STORE;

	/**
	 * ID: 1010496<br>
	 * Message: Entrance to the Forest of the Dead
	 */
	public static final NpcStringId ENTRANCE_TO_THE_FOREST_OF_THE_DEAD;

	/**
	 * ID: 1010497<br>
	 * Message: Western Entrance to the Swamp of Screams
	 */
	public static final NpcStringId WESTERN_ENTRANCE_TO_THE_SWAMP_OF_SCREAMS;

	/**
	 * ID: 1010498<br>
	 * Message: Entrance to the Forgotten Temple
	 */
	public static final NpcStringId ENTRANCE_TO_THE_FORGOTTEN_TEMPLE;

	/**
	 * ID: 1010499<br>
	 * Message: Center of the Forgotten Temple
	 */
	public static final NpcStringId CENTER_OF_THE_FORGOTTEN_TEMPLE;

	/**
	 * ID: 1010500<br>
	 * Message: Entrance to the Cruma Tower
	 */
	public static final NpcStringId ENTRANCE_TO_THE_CRUMA_TOWER;

	/**
	 * ID: 1010501<br>
	 * Message: Cruma Tower - First Floor
	 */
	public static final NpcStringId CRUMA_TOWER_FIRST_FLOOR;

	/**
	 * ID: 1010502<br>
	 * Message: Cruma Tower - Second Floor
	 */
	public static final NpcStringId CRUMA_TOWER_SECOND_FLOOR;

	/**
	 * ID: 1010503<br>
	 * Message: Cruma Tower - Third Floor
	 */
	public static final NpcStringId CRUMA_TOWER_THIRD_FLOOR;

	/**
	 * ID: 1010504<br>
	 * Message: Entrance to Devil's Isle
	 */
	public static final NpcStringId ENTRANCE_TO_DEVILS_ISLE;

	/**
	 * ID: 1010506<br>
	 * Message: Gludin Arena
	 */
	public static final NpcStringId GLUDIN_ARENA;

	/**
	 * ID: 1010507<br>
	 * Message: Giran Arena
	 */
	public static final NpcStringId GIRAN_ARENA;

	/**
	 * ID: 1010508<br>
	 * Message: Entrance to Antharas's Lair
	 */
	public static final NpcStringId ENTRANCE_TO_ANTHARASS_LAIR;

	/**
	 * ID: 1010509<br>
	 * Message: Antharas's Lair - 1st Level
	 */
	public static final NpcStringId ANTHARASS_LAIR_1ST_LEVEL;

	/**
	 * ID: 1010510<br>
	 * Message: Antharas's Lair - 2nd Level
	 */
	public static final NpcStringId ANTHARASS_LAIR_2ND_LEVEL;

	/**
	 * ID: 1010511<br>
	 * Message: Antharas's Lair -  Magic Force Field Bridge
	 */
	public static final NpcStringId ANTHARASS_LAIR_MAGIC_FORCE_FIELD_BRIDGE;

	/**
	 * ID: 1010512<br>
	 * Message: The Heart of Antharas's Lair
	 */
	public static final NpcStringId THE_HEART_OF_ANTHARASS_LAIR;

	/**
	 * ID: 1010513<br>
	 * Message: East of the Field of Silence
	 */
	public static final NpcStringId EAST_OF_THE_FIELD_OF_SILENCE;

	/**
	 * ID: 1010514<br>
	 * Message: West of the Field of Silence
	 */
	public static final NpcStringId WEST_OF_THE_FIELD_OF_SILENCE;

	/**
	 * ID: 1010515<br>
	 * Message: East of the Field of Whispers
	 */
	public static final NpcStringId EAST_OF_THE_FIELD_OF_WHISPERS;

	/**
	 * ID: 1010516<br>
	 * Message: West of the Field of Whispers
	 */
	public static final NpcStringId WEST_OF_THE_FIELD_OF_WHISPERS;

	/**
	 * ID: 1010517<br>
	 * Message: Entrance to the Garden of Eva
	 */
	public static final NpcStringId ENTRANCE_TO_THE_GARDEN_OF_EVA;

	/**
	 * ID: 1010520<br>
	 * Message: Northern part of Alligator Island
	 */
	public static final NpcStringId NORTHERN_PART_OF_ALLIGATOR_ISLAND;

	/**
	 * ID: 1010521<br>
	 * Message: Central part of Alligator Island
	 */
	public static final NpcStringId CENTRAL_PART_OF_ALLIGATOR_ISLAND;

	/**
	 * ID: 1010522<br>
	 * Message: Garden of Eva - 2nd Level
	 */
	public static final NpcStringId GARDEN_OF_EVA_2ND_LEVEL;

	/**
	 * ID: 1010523<br>
	 * Message: Garden of Eva - 3rd Level
	 */
	public static final NpcStringId GARDEN_OF_EVA_3RD_LEVEL;

	/**
	 * ID: 1010524<br>
	 * Message: Garden of Eva - 4th Level
	 */
	public static final NpcStringId GARDEN_OF_EVA_4TH_LEVEL;

	/**
	 * ID: 1010525<br>
	 * Message: Garden of Eva - 5th Level
	 */
	public static final NpcStringId GARDEN_OF_EVA_5TH_LEVEL;

	/**
	 * ID: 1010526<br>
	 * Message: Inside the Garden of Eva
	 */
	public static final NpcStringId INSIDE_THE_GARDEN_OF_EVA;

	/**
	 * ID: 1010527<br>
	 * Message: Four Sepulchers
	 */
	public static final NpcStringId FOUR_SEPULCHERS;

	/**
	 * ID: 1010528<br>
	 * Message: Imperial Tomb
	 */
	public static final NpcStringId IMPERIAL_TOMB;

	/**
	 * ID: 1010529<br>
	 * Message: Shrine of Loyalty
	 */
	public static final NpcStringId SHRINE_OF_LOYALTY;

	/**
	 * ID: 1010530<br>
	 * Message: Entrance to the Forge of the Gods
	 */
	public static final NpcStringId ENTRANCE_TO_THE_FORGE_OF_THE_GODS;

	/**
	 * ID: 1010531<br>
	 * Message: Forge of the Gods - Top Level
	 */
	public static final NpcStringId FORGE_OF_THE_GODS_TOP_LEVEL;

	/**
	 * ID: 1010532<br>
	 * Message: Forge of the Gods - Lower Level
	 */
	public static final NpcStringId FORGE_OF_THE_GODS_LOWER_LEVEL;

	/**
	 * ID: 1010533<br>
	 * Message: Entrance to the Wall of Argos
	 */
	public static final NpcStringId ENTRANCE_TO_THE_WALL_OF_ARGOS;

	/**
	 * ID: 1010534<br>
	 * Message: Varka Silenos Village
	 */
	public static final NpcStringId VARKA_SILENOS_VILLAGE;

	/**
	 * ID: 1010535<br>
	 * Message: Ketra Orc Village
	 */
	public static final NpcStringId KETRA_ORC_VILLAGE;

	/**
	 * ID: 1010536<br>
	 * Message: Entrance to the Hot Springs Region
	 */
	public static final NpcStringId ENTRANCE_TO_THE_HOT_SPRINGS_REGION;

	/**
	 * ID: 1010537<br>
	 * Message: Wild Beast Pastures
	 */
	public static final NpcStringId WILD_BEAST_PASTURES;

	/**
	 * ID: 1010538<br>
	 * Message: Entrance to the Valley of Saints
	 */
	public static final NpcStringId ENTRANCE_TO_THE_VALLEY_OF_SAINTS;

	/**
	 * ID: 1010539<br>
	 * Message: Cursed Village
	 */
	public static final NpcStringId CURSED_VILLAGE;

	/**
	 * ID: 1010540<br>
	 * Message: Southern Entrance of the Wild Beast Pastures
	 */
	public static final NpcStringId SOUTHERN_ENTRANCE_OF_THE_WILD_BEAST_PASTURES;

	/**
	 * ID: 1010541<br>
	 * Message: Eastern Part of the Wild Beast Pastures
	 */
	public static final NpcStringId EASTERN_PART_OF_THE_WILD_BEAST_PASTURES;

	/**
	 * ID: 1010542<br>
	 * Message: Western Part of the Wild Beast Pastures
	 */
	public static final NpcStringId WESTERN_PART_OF_THE_WILD_BEAST_PASTURES;

	/**
	 * ID: 1010543<br>
	 * Message: Eastern Part of the Swamp of Screams
	 */
	public static final NpcStringId EASTERN_PART_OF_THE_SWAMP_OF_SCREAMS;

	/**
	 * ID: 1010544<br>
	 * Message: Western Part of the Swamp of Screams
	 */
	public static final NpcStringId WESTERN_PART_OF_THE_SWAMP_OF_SCREAMS;

	/**
	 * ID: 1010545<br>
	 * Message: Center of the Swamp of Screams
	 */
	public static final NpcStringId CENTER_OF_THE_SWAMP_OF_SCREAMS;

	/**
	 * ID: 1010547<br>
	 * Message: Aden Frontier Gateway
	 */
	public static final NpcStringId ADEN_FRONTIER_GATEWAY;

	/**
	 * ID: 1010548<br>
	 * Message: Oren Frontier Gateway
	 */
	public static final NpcStringId OREN_FRONTIER_GATEWAY;

	/**
	 * ID: 1010549<br>
	 * Message: Garden of Wild Beasts
	 */
	public static final NpcStringId GARDEN_OF_WILD_BEASTS;

	/**
	 * ID: 1010550<br>
	 * Message: Devil's Pass
	 */
	public static final NpcStringId DEVILS_PASS;

	/**
	 * ID: 1010551<br>
	 * Message: The bullets are being loaded.
	 */
	public static final NpcStringId THE_BULLETS_ARE_BEING_LOADED;

	/**
	 * ID: 1010552<br>
	 * Message: You can start at the scheduled time.
	 */
	public static final NpcStringId YOU_CAN_START_AT_THE_SCHEDULED_TIME;

	/**
	 * ID: 1010554<br>
	 * Message: Upper Level of The Giant's Cave
	 */
	public static final NpcStringId UPPER_LEVEL_OF_THE_GIANTS_CAVE;

	/**
	 * ID: 1010555<br>
	 * Message: Lower Level of The Giant's Cave
	 */
	public static final NpcStringId LOWER_LEVEL_OF_THE_GIANTS_CAVE;

	/**
	 * ID: 1010556<br>
	 * Message: Immortal Plateau, Northern Region
	 */
	public static final NpcStringId IMMORTAL_PLATEAU_NORTHERN_REGION;

	/**
	 * ID: 1010557<br>
	 * Message: Elven Ruins
	 */
	public static final NpcStringId ELVEN_RUINS;

	/**
	 * ID: 1010558<br>
	 * Message: Singing Waterfall
	 */
	public static final NpcStringId SINGING_WATERFALL;

	/**
	 * ID: 1010559<br>
	 * Message: Talking Island, Northern Territory
	 */
	public static final NpcStringId TALKING_ISLAND_NORTHERN_TERRITORY;

	/**
	 * ID: 1010560<br>
	 * Message: Elven Fortress
	 */
	public static final NpcStringId ELVEN_FORTRESS;

	/**
	 * ID: 1010561<br>
	 * Message: Pilgrim's Temple
	 */
	public static final NpcStringId PILGRIMS_TEMPLE;

	/**
	 * ID: 1010562<br>
	 * Message: Gludin Harbor
	 */
	public static final NpcStringId GLUDIN_HARBOR;

	/**
	 * ID: 1010563<br>
	 * Message: Shilen's Garden
	 */
	public static final NpcStringId SHILENS_GARDEN;

	/**
	 * ID: 1010564<br>
	 * Message: School of Dark Arts
	 */
	public static final NpcStringId SCHOOL_OF_DARK_ARTS;

	/**
	 * ID: 1010565<br>
	 * Message: Swamp of Screams
	 */
	public static final NpcStringId SWAMP_OF_SCREAMS;

	/**
	 * ID: 1010566<br>
	 * Message: The Ant Nest
	 */
	public static final NpcStringId THE_ANT_NEST;

	/**
	 * ID: 1010568<br>
	 * Message: Wall of Argos
	 */
	public static final NpcStringId WALL_OF_ARGOS;

	/**
	 * ID: 1010569<br>
	 * Message: Den of Evil
	 */
	public static final NpcStringId DEN_OF_EVIL;

	/**
	 * ID: 1010570<br>
	 * Message: Iceman's Hut
	 */
	public static final NpcStringId ICEMANS_HUT;

	/**
	 * ID: 1010571<br>
	 * Message: Crypts of Disgrace
	 */
	public static final NpcStringId CRYPTS_OF_DISGRACE;

	/**
	 * ID: 1010572<br>
	 * Message: Plunderous Plains
	 */
	public static final NpcStringId PLUNDEROUS_PLAINS;

	/**
	 * ID: 1010573<br>
	 * Message: Pavel Ruins
	 */
	public static final NpcStringId PAVEL_RUINS;

	/**
	 * ID: 1010574<br>
	 * Message: Town of Schuttgart
	 */
	public static final NpcStringId TOWN_OF_SCHUTTGART;

	/**
	 * ID: 1010575<br>
	 * Message: Monastery of Silence
	 */
	public static final NpcStringId MONASTERY_OF_SILENCE;

	/**
	 * ID: 1010576<br>
	 * Message: Monastery of Silence: Rear Gate
	 */
	public static final NpcStringId MONASTERY_OF_SILENCE_REAR_GATE;

	/**
	 * ID: 1010577<br>
	 * Message: Stakato Nest
	 */
	public static final NpcStringId STAKATO_NEST;

	/**
	 * ID: 1010578<br>
	 * Message: How dare you trespass into my territory! Have you no fear?
	 */
	public static final NpcStringId HOW_DARE_YOU_TRESPASS_INTO_MY_TERRITORY_HAVE_YOU_NO_FEAR;

	/**
	 * ID: 1010579<br>
	 * Message: Fools! Why haven't you fled yet? Prepare to learn a lesson!
	 */
	public static final NpcStringId FOOLS_WHY_HAVENT_YOU_FLED_YET_PREPARE_TO_LEARN_A_LESSON;

	/**
	 * ID: 1010580<br>
	 * Message: Bwah-ha-ha! Your doom is at hand! Behold the Ultra Secret Super Weapon!
	 */
	public static final NpcStringId BWAH_HA_HA_YOUR_DOOM_IS_AT_HAND_BEHOLD_THE_ULTRA_SECRET_SUPER_WEAPON;

	/**
	 * ID: 1010581<br>
	 * Message: Foolish, insignificant creatures! How dare you challenge me!
	 */
	public static final NpcStringId FOOLISH_INSIGNIFICANT_CREATURES_HOW_DARE_YOU_CHALLENGE_ME;

	/**
	 * ID: 1010582<br>
	 * Message: I see that none will challenge me now!
	 */
	public static final NpcStringId I_SEE_THAT_NONE_WILL_CHALLENGE_ME_NOW;

	/**
	 * ID: 1010583<br>
	 * Message: Urggh! You will pay dearly for this insult.
	 */
	public static final NpcStringId URGGH_YOU_WILL_PAY_DEARLY_FOR_THIS_INSULT;

	/**
	 * ID: 1010584<br>
	 * Message: What? You haven't even two pennies to rub together? Harumph!
	 */
	public static final NpcStringId WHAT_YOU_HAVENT_EVEN_TWO_PENNIES_TO_RUB_TOGETHER_HARUMPH;

	/**
	 * ID: 1010585<br>
	 * Message: Forest of Mirrors
	 */
	public static final NpcStringId FOREST_OF_MIRRORS;

	/**
	 * ID: 1010586<br>
	 * Message: The Center of the Forest of Mirrors
	 */
	public static final NpcStringId THE_CENTER_OF_THE_FOREST_OF_MIRRORS;

	/**
	 * ID: 1010588<br>
	 * Message: Sky Wagon Relic
	 */
	public static final NpcStringId SKY_WAGON_RELIC;

	/**
	 * ID: 1010590<br>
	 * Message: The Center of the Dark Forest
	 */
	public static final NpcStringId THE_CENTER_OF_THE_DARK_FOREST;

	/**
	 * ID: 1010591<br>
	 * Message: Grave Robber Hideout
	 */
	public static final NpcStringId GRAVE_ROBBER_HIDEOUT;

	/**
	 * ID: 1010592<br>
	 * Message: Forest of the Dead
	 */
	public static final NpcStringId FOREST_OF_THE_DEAD;

	/**
	 * ID: 1010593<br>
	 * Message: The Center of the Forest of the Dead
	 */
	public static final NpcStringId THE_CENTER_OF_THE_FOREST_OF_THE_DEAD;

	/**
	 * ID: 1010594<br>
	 * Message: Mithril Mines
	 */
	public static final NpcStringId MITHRIL_MINES;

	/**
	 * ID: 1010595<br>
	 * Message: The Center of the Mithril Mines
	 */
	public static final NpcStringId THE_CENTER_OF_THE_MITHRIL_MINES;

	/**
	 * ID: 1010596<br>
	 * Message: Abandoned Coal Mines
	 */
	public static final NpcStringId ABANDONED_COAL_MINES;

	/**
	 * ID: 1010597<br>
	 * Message: The Center of the Abandoned Coal Mines
	 */
	public static final NpcStringId THE_CENTER_OF_THE_ABANDONED_COAL_MINES;

	/**
	 * ID: 1010598<br>
	 * Message: Immortal Plateau, Western Region
	 */
	public static final NpcStringId IMMORTAL_PLATEAU_WESTERN_REGION;

	/**
	 * ID: 1010600<br>
	 * Message: Valley of Saints
	 */
	public static final NpcStringId VALLEY_OF_SAINTS;

	/**
	 * ID: 1010601<br>
	 * Message: The Center of the Valley of Saints
	 */
	public static final NpcStringId THE_CENTER_OF_THE_VALLEY_OF_SAINTS;

	/**
	 * ID: 1010603<br>
	 * Message: Cave of Trials
	 */
	public static final NpcStringId CAVE_OF_TRIALS;

	/**
	 * ID: 1010604<br>
	 * Message: Seal of Shilen
	 */
	public static final NpcStringId SEAL_OF_SHILEN;

	/**
	 * ID: 1010605<br>
	 * Message: The Center of the Wall of Argos
	 */
	public static final NpcStringId THE_CENTER_OF_THE_WALL_OF_ARGOS;

	/**
	 * ID: 1010606<br>
	 * Message: The Center of Alligator Island
	 */
	public static final NpcStringId THE_CENTER_OF_ALLIGATOR_ISLAND;

	/**
	 * ID: 1010607<br>
	 * Message: Anghel Waterfall
	 */
	public static final NpcStringId ANGHEL_WATERFALL;

	/**
	 * ID: 1010608<br>
	 * Message: Center of the Elven Ruins
	 */
	public static final NpcStringId CENTER_OF_THE_ELVEN_RUINS;

	/**
	 * ID: 1010609<br>
	 * Message: Hot Springs
	 */
	public static final NpcStringId HOT_SPRINGS;

	/**
	 * ID: 1010610<br>
	 * Message: The Center of the Hot Springs
	 */
	public static final NpcStringId THE_CENTER_OF_THE_HOT_SPRINGS;

	/**
	 * ID: 1010611<br>
	 * Message: The Center of Dragon Valley
	 */
	public static final NpcStringId THE_CENTER_OF_DRAGON_VALLEY;

	/**
	 * ID: 1010613<br>
	 * Message: The Center of the Neutral Zone
	 */
	public static final NpcStringId THE_CENTER_OF_THE_NEUTRAL_ZONE;

	/**
	 * ID: 1010614<br>
	 * Message: Cruma Marshlands
	 */
	public static final NpcStringId CRUMA_MARSHLANDS;

	/**
	 * ID: 1010615<br>
	 * Message: The Center of the Cruma Marshlands
	 */
	public static final NpcStringId THE_CENTER_OF_THE_CRUMA_MARSHLANDS;

	/**
	 * ID: 1010617<br>
	 * Message: The Center of the Enchanted Valley
	 */
	public static final NpcStringId THE_CENTER_OF_THE_ENCHANTED_VALLEY;

	/**
	 * ID: 1010618<br>
	 * Message: Enchanted Valley, Southern Region
	 */
	public static final NpcStringId ENCHANTED_VALLEY_SOUTHERN_REGION;

	/**
	 * ID: 1010619<br>
	 * Message: Enchanted Valley, Northern Region
	 */
	public static final NpcStringId ENCHANTED_VALLEY_NORTHERN_REGION;

	/**
	 * ID: 1010620<br>
	 * Message: Frost Lake
	 */
	public static final NpcStringId FROST_LAKE;

	/**
	 * ID: 1010621<br>
	 * Message: Wastelands
	 */
	public static final NpcStringId WASTELANDS;

	/**
	 * ID: 1010622<br>
	 * Message: Wastelands, Western Region
	 */
	public static final NpcStringId WASTELANDS_WESTERN_REGION;

	/**
	 * ID: 1010623<br>
	 * Message: Who dares to covet the throne of our castle!  Leave immediately or you will pay the price of your audacity with your very own blood!
	 */
	public static final NpcStringId
			WHO_DARES_TO_COVET_THE_THRONE_OF_OUR_CASTLE_LEAVE_IMMEDIATELY_OR_YOU_WILL_PAY_THE_PRICE_OF_YOUR_AUDACITY_WITH_YOUR_VERY_OWN_BLOOD;

	/**
	 * ID: 1010624<br>
	 * Message: Hmm, those who are not of the bloodline are coming this way to take over the castle?!  Humph!  The bitter grudges of the dead.  You must not make light of their power!
	 */
	public static final NpcStringId
			HMM_THOSE_WHO_ARE_NOT_OF_THE_BLOODLINE_ARE_COMING_THIS_WAY_TO_TAKE_OVER_THE_CASTLE_HUMPH_THE_BITTER_GRUDGES_OF_THE_DEAD_YOU_MUST_NOT_MAKE_LIGHT_OF_THEIR_POWER;

	/**
	 * ID: 1010625<br>
	 * Message: Aargh...!  If I die, then the magic force field of blood will...!
	 */
	public static final NpcStringId AARGH_IF_I_DIE_THEN_THE_MAGIC_FORCE_FIELD_OF_BLOOD_WILL;

	/**
	 * ID: 1010626<br>
	 * Message: It's not over yet...  It won't be... over... like this... Never...
	 */
	public static final NpcStringId ITS_NOT_OVER_YET_IT_WONT_BE_OVER_LIKE_THIS_NEVER;

	/**
	 * ID: 1010627<br>
	 * Message: Oooh! Who poured nectar on my head while I was sleeping?
	 */
	public static final NpcStringId OOOH_WHO_POURED_NECTAR_ON_MY_HEAD_WHILE_I_WAS_SLEEPING;

	/**
	 * ID: 1010628<br>
	 * Message: Please wait a moment.
	 */
	public static final NpcStringId PLEASE_WAIT_A_MOMENT;

	/**
	 * ID: 1010629<br>
	 * Message: The word you need this time is $s1.
	 */
	public static final NpcStringId THE_WORD_YOU_NEED_THIS_TIME_IS_S1;

	/**
	 * ID: 1010630<br>
	 * Message: Intruders! Sound the alarm!
	 */
	public static final NpcStringId INTRUDERS_SOUND_THE_ALARM;

	/**
	 * ID: 1010631<br>
	 * Message: De-activate the alarm.
	 */
	public static final NpcStringId DE_ACTIVATE_THE_ALARM;

	/**
	 * ID: 1010632<br>
	 * Message: Oh no! The defenses have failed. It is too dangerous to remain inside the castle. Flee! Every man for himself!
	 */
	public static final NpcStringId
			OH_NO_THE_DEFENSES_HAVE_FAILED_IT_IS_TOO_DANGEROUS_TO_REMAIN_INSIDE_THE_CASTLE_FLEE_EVERY_MAN_FOR_HIMSELF;

	/**
	 * ID: 1010633<br>
	 * Message: The game has begun. Participants, prepare to learn an important word.
	 */
	public static final NpcStringId THE_GAME_HAS_BEGUN_PARTICIPANTS_PREPARE_TO_LEARN_AN_IMPORTANT_WORD;

	/**
	 * ID: 1010634<br>
	 * Message: $s1 team's jackpot has $s2 percent of its HP remaining.
	 */
	public static final NpcStringId S1_TEAMS_JACKPOT_HAS_S2_PERCENT_OF_ITS_HP_REMAINING;

	/**
	 * ID: 1010635<br>
	 * Message: Undecided
	 */
	public static final NpcStringId UNDECIDED;

	/**
	 * ID: 1010636<br>
	 * Message: Heh Heh... I see that the feast has begun! Be wary! The curse of the Hellmann family has poisoned this land!
	 */
	public static final NpcStringId
			HEH_HEH_I_SEE_THAT_THE_FEAST_HAS_BEGUN_BE_WARY_THE_CURSE_OF_THE_HELLMANN_FAMILY_HAS_POISONED_THIS_LAND;

	/**
	 * ID: 1010637<br>
	 * Message: Arise, my faithful servants! You, my people who have inherited the blood.  It is the calling of my daughter.  The feast of blood will now begin!
	 */
	public static final NpcStringId
			ARISE_MY_FAITHFUL_SERVANTS_YOU_MY_PEOPLE_WHO_HAVE_INHERITED_THE_BLOOD_IT_IS_THE_CALLING_OF_MY_DAUGHTER_THE_FEAST_OF_BLOOD_WILL_NOW_BEGIN;

	/**
	 * ID: 1010639<br>
	 * Message: Grarr! For the next 2 minutes or so, the game arena are will be cleaned. Throw any items you don't need to the floor now.
	 */
	public static final NpcStringId
			GRARR_FOR_THE_NEXT_2_MINUTES_OR_SO_THE_GAME_ARENA_ARE_WILL_BE_CLEANED_THROW_ANY_ITEMS_YOU_DONT_NEED_TO_THE_FLOOR_NOW;

	/**
	 * ID: 1010640<br>
	 * Message: Grarr! $s1 team is using the hot springs sulfur on the opponent's camp.
	 */
	public static final NpcStringId GRARR_S1_TEAM_IS_USING_THE_HOT_SPRINGS_SULFUR_ON_THE_OPPONENTS_CAMP;

	/**
	 * ID: 1010641<br>
	 * Message: Grarr! $s1 team is attempting to steal the jackpot.
	 */
	public static final NpcStringId GRARR_S1_TEAM_IS_ATTEMPTING_TO_STEAL_THE_JACKPOT;

	/**
	 * ID: 1010642<br>
	 * Message: ** Vacant Seat **
	 */
	public static final NpcStringId _VACANT_SEAT;

	/**
	 * ID: 1010643<br>
	 * Message: $s1 minute(s) are remaining.
	 */
	public static final NpcStringId S1_MINUTES_REMAINING;

	/**
	 * ID: 1010644<br>
	 * Message: How dare you ruin the performance of the Dark Choir... Unforgivable!
	 */
	public static final NpcStringId HOW_DARE_YOU_RUIN_THE_PERFORMANCE_OF_THE_DARK_CHOIR_UNFORGIVABLE;

	/**
	 * ID: 1010645<br>
	 * Message: Get rid of the invaders who interrupt the performance of the Dark Choir!
	 */
	public static final NpcStringId GET_RID_OF_THE_INVADERS_WHO_INTERRUPT_THE_PERFORMANCE_OF_THE_DARK_CHOIR;

	/**
	 * ID: 1010646<br>
	 * Message: Don't you hear the music of death? Reveal the horror of the Dark Choir!
	 */
	public static final NpcStringId DONT_YOU_HEAR_THE_MUSIC_OF_DEATH_REVEAL_THE_HORROR_OF_THE_DARK_CHOIR;

	/**
	 * ID: 1010647<br>
	 * Message: The Immortal Plateau
	 */
	public static final NpcStringId THE_IMMORTAL_PLATEAU;

	/**
	 * ID: 1010648<br>
	 * Message: Kamael Village
	 */
	public static final NpcStringId KAMAEL_VILLAGE;

	/**
	 * ID: 1010649<br>
	 * Message: Isle of Souls Base
	 */
	public static final NpcStringId ISLE_OF_SOULS_BASE;

	/**
	 * ID: 1010650<br>
	 * Message: Golden Hills Base
	 */
	public static final NpcStringId GOLDEN_HILLS_BASE;

	/**
	 * ID: 1010651<br>
	 * Message: Mimir's Forest Base
	 */
	public static final NpcStringId MIMIRS_FOREST_BASE;

	/**
	 * ID: 1010652<br>
	 * Message: Isle of Souls Harbor
	 */
	public static final NpcStringId ISLE_OF_SOULS_HARBOR;

	/**
	 * ID: 1010653<br>
	 * Message: Stronghold I
	 */
	public static final NpcStringId STRONGHOLD_I;

	/**
	 * ID: 1010654<br>
	 * Message: Stronghold II
	 */
	public static final NpcStringId STRONGHOLD_II;

	/**
	 * ID: 1010655<br>
	 * Message: Stronghold III
	 */
	public static final NpcStringId STRONGHOLD_III;

	/**
	 * ID: 1010656<br>
	 * Message: Fortress West Gate
	 */
	public static final NpcStringId FORTRESS_WEST_GATE;

	/**
	 * ID: 1010657<br>
	 * Message: Fortress East Gate
	 */
	public static final NpcStringId FORTRESS_EAST_GATE;

	/**
	 * ID: 1010658<br>
	 * Message: Fortress North Gate
	 */
	public static final NpcStringId FORTRESS_NORTH_GATE;

	/**
	 * ID: 1010659<br>
	 * Message: Fortress South Gate
	 */
	public static final NpcStringId FORTRESS_SOUTH_GATE;

	/**
	 * ID: 1010660<br>
	 * Message: Front of the Valley Fortress
	 */
	public static final NpcStringId FRONT_OF_THE_VALLEY_FORTRESS;

	/**
	 * ID: 1010661<br>
	 * Message: Goddard Town Square
	 */
	public static final NpcStringId GODDARD_TOWN_SQUARE;

	/**
	 * ID: 1010662<br>
	 * Message: Front of the Goddard Castle Gate
	 */
	public static final NpcStringId FRONT_OF_THE_GODDARD_CASTLE_GATE;

	/**
	 * ID: 1010663<br>
	 * Message: Gludio Town Square
	 */
	public static final NpcStringId GLUDIO_TOWN_SQUARE;

	/**
	 * ID: 1010664<br>
	 * Message: Front of the Gludio Castle Gate
	 */
	public static final NpcStringId FRONT_OF_THE_GLUDIO_CASTLE_GATE;

	/**
	 * ID: 1010665<br>
	 * Message: Giran Town Square
	 */
	public static final NpcStringId GIRAN_TOWN_SQUARE;

	/**
	 * ID: 1010666<br>
	 * Message: Front of the Giran Castle Gate
	 */
	public static final NpcStringId FRONT_OF_THE_GIRAN_CASTLE_GATE;

	/**
	 * ID: 1010667<br>
	 * Message: Front of the Southern Fortress
	 */
	public static final NpcStringId FRONT_OF_THE_SOUTHERN_FORTRESS;

	/**
	 * ID: 1010668<br>
	 * Message: Front of the Swamp Fortress
	 */
	public static final NpcStringId FRONT_OF_THE_SWAMP_FORTRESS;

	/**
	 * ID: 1010669<br>
	 * Message: Dion Town Square
	 */
	public static final NpcStringId DION_TOWN_SQUARE;

	/**
	 * ID: 1010670<br>
	 * Message: Front of the Dion Castle Gate
	 */
	public static final NpcStringId FRONT_OF_THE_DION_CASTLE_GATE;

	/**
	 * ID: 1010671<br>
	 * Message: Rune Town Square
	 */
	public static final NpcStringId RUNE_TOWN_SQUARE;

	/**
	 * ID: 1010672<br>
	 * Message: Front of the Rune Castle Gate
	 */
	public static final NpcStringId FRONT_OF_THE_RUNE_CASTLE_GATE;

	/**
	 * ID: 1010673<br>
	 * Message: Front of the White Sand Fortress
	 */
	public static final NpcStringId FRONT_OF_THE_WHITE_SAND_FORTRESS;

	/**
	 * ID: 1010674<br>
	 * Message: Front of the Basin Fortress
	 */
	public static final NpcStringId FRONT_OF_THE_BASIN_FORTRESS;

	/**
	 * ID: 1010675<br>
	 * Message: Front of the Ivory Fortress
	 */
	public static final NpcStringId FRONT_OF_THE_IVORY_FORTRESS;

	/**
	 * ID: 1010676<br>
	 * Message: Schuttgart Town Square
	 */
	public static final NpcStringId SCHUTTGART_TOWN_SQUARE;

	/**
	 * ID: 1010677<br>
	 * Message: Front of the Schuttgart Castle Gate
	 */
	public static final NpcStringId FRONT_OF_THE_SCHUTTGART_CASTLE_GATE;

	/**
	 * ID: 1010678<br>
	 * Message: Aden Town Square
	 */
	public static final NpcStringId ADEN_TOWN_SQUARE;

	/**
	 * ID: 1010679<br>
	 * Message: Front of the Aden Castle Gate
	 */
	public static final NpcStringId FRONT_OF_THE_ADEN_CASTLE_GATE;

	/**
	 * ID: 1010680<br>
	 * Message: Front of the Shanty Fortress
	 */
	public static final NpcStringId FRONT_OF_THE_SHANTY_FORTRESS;

	/**
	 * ID: 1010681<br>
	 * Message: Oren Town Square
	 */
	public static final NpcStringId OREN_TOWN_SQUARE;

	/**
	 * ID: 1010682<br>
	 * Message: Front of the Oren Castle Gate
	 */
	public static final NpcStringId FRONT_OF_THE_OREN_CASTLE_GATE;

	/**
	 * ID: 1010683<br>
	 * Message: Front of the Archaic Fortress
	 */
	public static final NpcStringId FRONT_OF_THE_ARCHAIC_FORTRESS;

	/**
	 * ID: 1010684<br>
	 * Message: Front of the Innadril Castle Gate
	 */
	public static final NpcStringId FRONT_OF_THE_INNADRIL_CASTLE_GATE;

	/**
	 * ID: 1010685<br>
	 * Message: Front of the Border Fortress
	 */
	public static final NpcStringId FRONT_OF_THE_BORDER_FORTRESS;

	/**
	 * ID: 1010686<br>
	 * Message: Heine Town Square
	 */
	public static final NpcStringId HEINE_TOWN_SQUARE;

	/**
	 * ID: 1010687<br>
	 * Message: Front of the Hive Fortress
	 */
	public static final NpcStringId FRONT_OF_THE_HIVE_FORTRESS;

	/**
	 * ID: 1010688<br>
	 * Message: Front of the Narsell Fortress
	 */
	public static final NpcStringId FRONT_OF_THE_NARSELL_FORTRESS;

	/**
	 * ID: 1010689<br>
	 * Message: Front of the Gludio Castle
	 */
	public static final NpcStringId FRONT_OF_THE_GLUDIO_CASTLE;

	/**
	 * ID: 1010690<br>
	 * Message: Front of the Dion Castle
	 */
	public static final NpcStringId FRONT_OF_THE_DION_CASTLE;

	/**
	 * ID: 1010691<br>
	 * Message: Front of the Giran Castle
	 */
	public static final NpcStringId FRONT_OF_THE_GIRAN_CASTLE;

	/**
	 * ID: 1010692<br>
	 * Message: Front of the Oren Castle
	 */
	public static final NpcStringId FRONT_OF_THE_OREN_CASTLE;

	/**
	 * ID: 1010693<br>
	 * Message: Front of the Aden Castle
	 */
	public static final NpcStringId FRONT_OF_THE_ADEN_CASTLE;

	/**
	 * ID: 1010694<br>
	 * Message: Front of the Innadril Castle
	 */
	public static final NpcStringId FRONT_OF_THE_INNADRIL_CASTLE;

	/**
	 * ID: 1010695<br>
	 * Message: Front of the Goddard Castle
	 */
	public static final NpcStringId FRONT_OF_THE_GODDARD_CASTLE;

	/**
	 * ID: 1010696<br>
	 * Message: Front of the Rune Castle
	 */
	public static final NpcStringId FRONT_OF_THE_RUNE_CASTLE;

	/**
	 * ID: 1010697<br>
	 * Message: Front of the Schuttgart Castle
	 */
	public static final NpcStringId FRONT_OF_THE_SCHUTTGART_CASTLE;

	/**
	 * ID: 1010698<br>
	 * Message: Primeval Isle Wharf
	 */
	public static final NpcStringId PRIMEVAL_ISLE_WHARF;

	/**
	 * ID: 1010699<br>
	 * Message: Isle of Prayer
	 */
	public static final NpcStringId ISLE_OF_PRAYER;

	/**
	 * ID: 1010700<br>
	 * Message: Mithril Mines Western Entrance
	 */
	public static final NpcStringId MITHRIL_MINES_WESTERN_ENTRANCE;

	/**
	 * ID: 1010701<br>
	 * Message: Mithril Mines Eastern Entrance
	 */
	public static final NpcStringId MITHRIL_MINES_EASTERN_ENTRANCE;

	/**
	 * ID: 1010702<br>
	 * Message: The Giant's Cave Upper Layer
	 */
	public static final NpcStringId THE_GIANTS_CAVE_UPPER_LAYER;

	/**
	 * ID: 1010703<br>
	 * Message: The Giant's Cave Lower Layer
	 */
	public static final NpcStringId THE_GIANTS_CAVE_LOWER_LAYER;

	/**
	 * ID: 1010704<br>
	 * Message: Field of Silence Center
	 */
	public static final NpcStringId FIELD_OF_SILENCE_CENTER;

	/**
	 * ID: 1010705<br>
	 * Message: Field of Whispers Center
	 */
	public static final NpcStringId FIELD_OF_WHISPERS_CENTER;

	/**
	 * ID: 1010706<br>
	 * Message: Shyeed's Cavern
	 */
	public static final NpcStringId SHYEEDS_CAVERN;

	/**
	 * ID: 1010709<br>
	 * Message: Seed of Infinity Dock
	 */
	public static final NpcStringId SEED_OF_INFINITY_DOCK;

	/**
	 * ID: 1010710<br>
	 * Message: Seed of Destruction Dock
	 */
	public static final NpcStringId SEED_OF_DESTRUCTION_DOCK;

	/**
	 * ID: 1010711<br>
	 * Message: Seed of Annihilation Dock
	 */
	public static final NpcStringId SEED_OF_ANNIHILATION_DOCK;

	/**
	 * ID: 1010712<br>
	 * Message: Town of Aden Einhasad Temple Priest Wood
	 */
	public static final NpcStringId TOWN_OF_ADEN_EINHASAD_TEMPLE_PRIEST_WOOD;

	/**
	 * ID: 1010713<br>
	 * Message: Hunter's Village Separated Soul Front
	 */
	public static final NpcStringId HUNTERS_VILLAGE_SEPARATED_SOUL_FRONT;

	/**
	 * ID: 1029350<br>
	 * Message: What took so long? I waited for ever.
	 */
	public static final NpcStringId WHAT_TOOK_SO_LONG_I_WAITED_FOR_EVER;

	/**
	 * ID: 1029351<br>
	 * Message: I must ask Librarian Sophia about the book.
	 */
	public static final NpcStringId I_MUST_ASK_LIBRARIAN_SOPHIA_ABOUT_THE_BOOK;

	/**
	 * ID: 1029352<br>
	 * Message: This library... It's huge but there aren't many useful books, right?
	 */
	public static final NpcStringId THIS_LIBRARY_ITS_HUGE_BUT_THERE_ARENT_MANY_USEFUL_BOOKS_RIGHT;

	/**
	 * ID: 1029353<br>
	 * Message: An underground library... I hate damp and smelly places...
	 */
	public static final NpcStringId AN_UNDERGROUND_LIBRARY_I_HATE_DAMP_AND_SMELLY_PLACES;

	/**
	 * ID: 1029354<br>
	 * Message: The book that we seek is certainly here. Search inch by inch.
	 */
	public static final NpcStringId THE_BOOK_THAT_WE_SEEK_IS_CERTAINLY_HERE_SEARCH_INCH_BY_INCH;

	/**
	 * ID: 1029450<br>
	 * Message: We must search high and low in every room for the reading desk that contains the book we seek.
	 */
	public static final NpcStringId
			WE_MUST_SEARCH_HIGH_AND_LOW_IN_EVERY_ROOM_FOR_THE_READING_DESK_THAT_CONTAINS_THE_BOOK_WE_SEEK;

	/**
	 * ID: 1029451<br>
	 * Message: Remember the content of the books that you found. You can't take them out with you.
	 */
	public static final NpcStringId REMEMBER_THE_CONTENT_OF_THE_BOOKS_THAT_YOU_FOUND_YOU_CANT_TAKE_THEM_OUT_WITH_YOU;

	/**
	 * ID: 1029452<br>
	 * Message: It seems that you cannot remember to the room of the watcher who found the book.
	 */
	public static final NpcStringId IT_SEEMS_THAT_YOU_CANNOT_REMEMBER_TO_THE_ROOM_OF_THE_WATCHER_WHO_FOUND_THE_BOOK;

	/**
	 * ID: 1029453<br>
	 * Message: Your work here is done, so return to the central guardian.
	 */
	public static final NpcStringId YOUR_WORK_HERE_IS_DONE_SO_RETURN_TO_THE_CENTRAL_GUARDIAN;

	/**
	 * ID: 1029460<br>
	 * Message: You foolish invaders who disturb the rest of Solina, be gone from this place.
	 */
	public static final NpcStringId YOU_FOOLISH_INVADERS_WHO_DISTURB_THE_REST_OF_SOLINA_BE_GONE_FROM_THIS_PLACE;

	/**
	 * ID: 1029461<br>
	 * Message: I know not what you seek, but this truth cannot be handled by mere humans.
	 */
	public static final NpcStringId I_KNOW_NOT_WHAT_YOU_SEEK_BUT_THIS_TRUTH_CANNOT_BE_HANDLED_BY_MERE_HUMANS;

	/**
	 * ID: 1029462<br>
	 * Message: I will not stand by and watch your foolish actions. I warn you, leave this place at once.
	 */
	public static final NpcStringId
			I_WILL_NOT_STAND_BY_AND_WATCH_YOUR_FOOLISH_ACTIONS_I_WARN_YOU_LEAVE_THIS_PLACE_AT_ONCE;

	/**
	 * ID: 1029550<br>
	 * Message: The guardian of the seal doesn't seem to get injured at all until the barrier is destroyed.
	 */
	public static final NpcStringId
			THE_GUARDIAN_OF_THE_SEAL_DOESNT_SEEM_TO_GET_INJURED_AT_ALL_UNTIL_THE_BARRIER_IS_DESTROYED;

	/**
	 * ID: 1029551<br>
	 * Message: The device located in the room in front of the guardian of the seal is definitely the barrier that controls the guardian's power.
	 */
	public static final NpcStringId
			THE_DEVICE_LOCATED_IN_THE_ROOM_IN_FRONT_OF_THE_GUARDIAN_OF_THE_SEAL_IS_DEFINITELY_THE_BARRIER_THAT_CONTROLS_THE_GUARDIANS_POWER;

	/**
	 * ID: 1029552<br>
	 * Message: To remove the barrier, you must find the relics that fit the barrier and activate the device.
	 */
	public static final NpcStringId
			TO_REMOVE_THE_BARRIER_YOU_MUST_FIND_THE_RELICS_THAT_FIT_THE_BARRIER_AND_ACTIVATE_THE_DEVICE;

	/**
	 * ID: 1029553<br>
	 * Message: All the guardians were defeated, and the seal was removed. Teleport to the center.
	 */
	public static final NpcStringId ALL_THE_GUARDIANS_WERE_DEFEATED_AND_THE_SEAL_WAS_REMOVED_TELEPORT_TO_THE_CENTER;

	/**
	 * ID: 1110071<br>
	 * Message: ... is the process of standing up.
	 */
	public static final NpcStringId _IS_THE_PROCESS_OF_STANDING_UP;

	/**
	 * ID: 1110072<br>
	 * Message: ... is the process of sitting down.
	 */
	public static final NpcStringId _IS_THE_PROCESS_OF_SITTING_DOWN;

	/**
	 * ID: 1110073<br>
	 * Message: It is possible to use a skill while sitting down.
	 */
	public static final NpcStringId IT_IS_POSSIBLE_TO_USE_A_SKILL_WHILE_SITTING_DOWN;

	/**
	 * ID: 1110074<br>
	 * Message: ...is out of range.
	 */
	public static final NpcStringId IS_OUT_OF_RANGE;

	/**
	 * ID: 1120300<br>
	 * Message: Thank you... My book... Child...
	 */
	public static final NpcStringId THANK_YOU_MY_BOOK_CHILD;

	/**
	 * ID: 1120301<br>
	 * Message: Killed by $s1
	 */
	public static final NpcStringId KILLED_BY_S2;

	/**
	 * ID: 1121000<br>
	 * Message: Steward: Please wait a moment.
	 */
	public static final NpcStringId STEWARD_PLEASE_WAIT_A_MOMENT;

	/**
	 * ID: 1121001<br>
	 * Message: Steward: Please restore the Queen's former appearance!
	 */
	public static final NpcStringId STEWARD_PLEASE_RESTORE_THE_QUEENS_FORMER_APPEARANCE;

	/**
	 * ID: 1121002<br>
	 * Message: Steward: Waste no time!  Please hurry!
	 */
	public static final NpcStringId STEWARD_WASTE_NO_TIME_PLEASE_HURRY;

	/**
	 * ID: 1121003<br>
	 * Message: Steward: Was it indeed too much to ask...
	 */
	public static final NpcStringId STEWARD_WAS_IT_INDEED_TOO_MUCH_TO_ASK;

	/**
	 * ID: 1121004<br>
	 * Message: Freya: Heathens! Feel my chill!
	 */
	public static final NpcStringId FREYA_HEATHENS_FEEL_MY_CHILL;

	/**
	 * ID: 1121005<br>
	 * Message: Attention please! The gates will be closing shortly. All visitors to the Queen's Castle should leave immediately.
	 */
	public static final NpcStringId
			ATTENTION_PLEASE_THE_GATES_WILL_BE_CLOSING_SHORTLY_ALL_VISITORS_TO_THE_QUEENS_CASTLE_SHOULD_LEAVE_IMMEDIATELY;

	/**
	 * ID: 1121006<br>
	 * Message: You cannot carry a weapon without authorization!
	 */
	public static final NpcStringId YOU_CANNOT_CARRY_A_WEAPON_WITHOUT_AUTHORIZATION;

	/**
	 * ID: 1121007<br>
	 * Message: Are you trying to deceive me?  I'm disappointed.
	 */
	public static final NpcStringId ARE_YOU_TRYING_TO_DECEIVE_ME_IM_DISAPPOINTED;

	/**
	 * ID: 1121008<br>
	 * Message: 30 minutes remain.
	 */
	public static final NpcStringId N30_MINUTES_REMAIN;

	/**
	 * ID: 1121009<br>
	 * Message: 20 minutes remain.
	 */
	public static final NpcStringId N20_MINUTES_REMAIN;

	/**
	 * ID: 1200001<br>
	 * Message: Chilly Coda
	 */
	public static final NpcStringId CHILLY_CODA;

	/**
	 * ID: 1200002<br>
	 * Message: Burning Coda
	 */
	public static final NpcStringId BURNING_CODA;

	/**
	 * ID: 1200003<br>
	 * Message: Blue Coda
	 */
	public static final NpcStringId BLUE_CODA;

	/**
	 * ID: 1200004<br>
	 * Message: Red Coda
	 */
	public static final NpcStringId RED_CODA;

	/**
	 * ID: 1200005<br>
	 * Message: Golden Coda
	 */
	public static final NpcStringId GOLDEN_CODA;

	/**
	 * ID: 1200006<br>
	 * Message: Desert Coda
	 */
	public static final NpcStringId DESERT_CODA;

	/**
	 * ID: 1200007<br>
	 * Message: Lute Coda
	 */
	public static final NpcStringId LUTE_CODA;

	/**
	 * ID: 1200008<br>
	 * Message: Twin Coda
	 */
	public static final NpcStringId TWIN_CODA;

	/**
	 * ID: 1200009<br>
	 * Message: Dark Coda
	 */
	public static final NpcStringId DARK_CODA;

	/**
	 * ID: 1200010<br>
	 * Message: Shining Coda
	 */
	public static final NpcStringId SHINING_CODA;

	/**
	 * ID: 1200011<br>
	 * Message: Chilly Cobol
	 */
	public static final NpcStringId CHILLY_COBOL;

	/**
	 * ID: 1200012<br>
	 * Message: Burning Cobol
	 */
	public static final NpcStringId BURNING_COBOL;

	/**
	 * ID: 1200013<br>
	 * Message: Blue Cobol
	 */
	public static final NpcStringId BLUE_COBOL;

	/**
	 * ID: 1200014<br>
	 * Message: Red Cobol
	 */
	public static final NpcStringId RED_COBOL;

	/**
	 * ID: 1200015<br>
	 * Message: Golden Cobol
	 */
	public static final NpcStringId GOLDEN_COBOL;

	/**
	 * ID: 1200016<br>
	 * Message: Desert Cobol
	 */
	public static final NpcStringId DESERT_COBOL;

	/**
	 * ID: 1200017<br>
	 * Message: Sea Cobol
	 */
	public static final NpcStringId SEA_COBOL;

	/**
	 * ID: 1200018<br>
	 * Message: Thorn Cobol
	 */
	public static final NpcStringId THORN_COBOL;

	/**
	 * ID: 1200019<br>
	 * Message: Dapple Cobol
	 */
	public static final NpcStringId DAPPLE_COBOL;

	/**
	 * ID: 1200020<br>
	 * Message: Great Cobol
	 */
	public static final NpcStringId GREAT_COBOL;

	/**
	 * ID: 1200021<br>
	 * Message: Chilly Codran
	 */
	public static final NpcStringId CHILLY_CODRAN;

	/**
	 * ID: 1200022<br>
	 * Message: Burning Codran
	 */
	public static final NpcStringId BURNING_CODRAN;

	/**
	 * ID: 1200023<br>
	 * Message: Blue Codran
	 */
	public static final NpcStringId BLUE_CODRAN;

	/**
	 * ID: 1200024<br>
	 * Message: Red Codran
	 */
	public static final NpcStringId RED_CODRAN;

	/**
	 * ID: 1200025<br>
	 * Message: Dapple Codran
	 */
	public static final NpcStringId DAPPLE_CODRAN;

	/**
	 * ID: 1200026<br>
	 * Message: Desert Codran
	 */
	public static final NpcStringId DESERT_CODRAN;

	/**
	 * ID: 1200027<br>
	 * Message: Sea Codran
	 */
	public static final NpcStringId SEA_CODRAN;

	/**
	 * ID: 1200028<br>
	 * Message: Twin Codran
	 */
	public static final NpcStringId TWIN_CODRAN;

	/**
	 * ID: 1200029<br>
	 * Message: Thorn Codran
	 */
	public static final NpcStringId THORN_CODRAN;

	/**
	 * ID: 1200030<br>
	 * Message: Great Codran
	 */
	public static final NpcStringId GREAT_CODRAN;

	/**
	 * ID: 1200031<br>
	 * Message: Alternative Dark Coda
	 */
	public static final NpcStringId ALTERNATIVE_DARK_CODA;

	/**
	 * ID: 1200032<br>
	 * Message: Alternative Red Coda
	 */
	public static final NpcStringId ALTERNATIVE_RED_CODA;

	/**
	 * ID: 1200033<br>
	 * Message: Alternative Chilly Coda
	 */
	public static final NpcStringId ALTERNATIVE_CHILLY_CODA;

	/**
	 * ID: 1200034<br>
	 * Message: Alternative Blue Coda
	 */
	public static final NpcStringId ALTERNATIVE_BLUE_CODA;

	/**
	 * ID: 1200035<br>
	 * Message: Alternative Golden Coda
	 */
	public static final NpcStringId ALTERNATIVE_GOLDEN_CODA;

	/**
	 * ID: 1200036<br>
	 * Message: Alternative Lute Coda
	 */
	public static final NpcStringId ALTERNATIVE_LUTE_CODA;

	/**
	 * ID: 1200037<br>
	 * Message: Alternative Desert Coda
	 */
	public static final NpcStringId ALTERNATIVE_DESERT_CODA;

	/**
	 * ID: 1200038<br>
	 * Message: Alternative Red Cobol
	 */
	public static final NpcStringId ALTERNATIVE_RED_COBOL;

	/**
	 * ID: 1200039<br>
	 * Message: Alternative Chilly Cobol
	 */
	public static final NpcStringId ALTERNATIVE_CHILLY_COBOL;

	/**
	 * ID: 1200040<br>
	 * Message: Alternative Blue Cobol
	 */
	public static final NpcStringId ALTERNATIVE_BLUE_COBOL;

	/**
	 * ID: 1200041<br>
	 * Message: Alternative Thorn Cobol
	 */
	public static final NpcStringId ALTERNATIVE_THORN_COBOL;

	/**
	 * ID: 1200042<br>
	 * Message: Alternative Golden Cobol
	 */
	public static final NpcStringId ALTERNATIVE_GOLDEN_COBOL;

	/**
	 * ID: 1200043<br>
	 * Message: Alternative Great Cobol
	 */
	public static final NpcStringId ALTERNATIVE_GREAT_COBOL;

	/**
	 * ID: 1200044<br>
	 * Message: Alternative Red Codran
	 */
	public static final NpcStringId ALTERNATIVE_RED_CODRAN;

	/**
	 * ID: 1200045<br>
	 * Message: Alternative Sea Codran
	 */
	public static final NpcStringId ALTERNATIVE_SEA_CODRAN;

	/**
	 * ID: 1200046<br>
	 * Message: Alternative Chilly Codran
	 */
	public static final NpcStringId ALTERNATIVE_CHILLY_CODRAN;

	/**
	 * ID: 1200047<br>
	 * Message: Alternative Blue Codran
	 */
	public static final NpcStringId ALTERNATIVE_BLUE_CODRAN;

	/**
	 * ID: 1200048<br>
	 * Message: Alternative Twin Codran
	 */
	public static final NpcStringId ALTERNATIVE_TWIN_CODRAN;

	/**
	 * ID: 1200049<br>
	 * Message: Alternative Great Codran
	 */
	public static final NpcStringId ALTERNATIVE_GREAT_CODRAN;

	/**
	 * ID: 1200050<br>
	 * Message: Alternative Desert Codran
	 */
	public static final NpcStringId ALTERNATIVE_DESERT_CODRAN;

	/**
	 * ID: 1300001<br>
	 * Message: We have broken through the gate! Destroy the encampment and move to the Command Post!
	 */
	public static final NpcStringId WE_HAVE_BROKEN_THROUGH_THE_GATE_DESTROY_THE_ENCAMPMENT_AND_MOVE_TO_THE_COMMAND_POST;

	/**
	 * ID: 1300002<br>
	 * Message: The command gate has opened! Capture the flag quickly and raise it high to proclaim our victory!
	 */
	public static final NpcStringId
			THE_COMMAND_GATE_HAS_OPENED_CAPTURE_THE_FLAG_QUICKLY_AND_RAISE_IT_HIGH_TO_PROCLAIM_OUR_VICTORY;

	/**
	 * ID: 1300003<br>
	 * Message: The gods have forsaken us... Retreat!!
	 */
	public static final NpcStringId THE_GODS_HAVE_FORSAKEN_US_RETREAT;

	/**
	 * ID: 1300004<br>
	 * Message: You may have broken our arrows, but you will never break our will! Archers, retreat!
	 */
	public static final NpcStringId YOU_MAY_HAVE_BROKEN_OUR_ARROWS_BUT_YOU_WILL_NEVER_BREAK_OUR_WILL_ARCHERS_RETREAT;

	/**
	 * ID: 1300005<br>
	 * Message: At last! The Magic Field that protects the fortress has weakened! Volunteers, stand back!
	 */
	public static final NpcStringId
			AT_LAST_THE_MAGIC_FIELD_THAT_PROTECTS_THE_FORTRESS_HAS_WEAKENED_VOLUNTEERS_STAND_BACK;

	/**
	 * ID: 1300006<br>
	 * Message: Aiieeee! Command Center! This is guard unit! We need backup right away!
	 */
	public static final NpcStringId AIIEEEE_COMMAND_CENTER_THIS_IS_GUARD_UNIT_WE_NEED_BACKUP_RIGHT_AWAY;

	/**
	 * ID: 1300007<br>
	 * Message: Fortress power disabled.
	 */
	public static final NpcStringId FORTRESS_POWER_DISABLED;

	/**
	 * ID: 1300008<br>
	 * Message: Oh my, what has become of me???  My fame.. my friends.. lost.. all lost...
	 */
	public static final NpcStringId OH_MY_WHAT_HAS_BECOME_OF_ME_MY_FAME_MY_FRIENDS_LOST_ALL_LOST;

	/**
	 * ID: 1300009<br>
	 * Message: Machine No. 1 - Power Off!
	 */
	public static final NpcStringId MACHINE_NO_1_POWER_OFF;

	/**
	 * ID: 1300010<br>
	 * Message: Machine No. 2  - Power Off!
	 */
	public static final NpcStringId MACHINE_NO_2_POWER_OFF;

	/**
	 * ID: 1300011<br>
	 * Message: Machine No. 3  - Power Off!
	 */
	public static final NpcStringId MACHINE_NO_3_POWER_OFF;

	/**
	 * ID: 1300012<br>
	 * Message: Everyone, concentrate your attacks on $s1! Show the enemy your resolve!
	 */
	public static final NpcStringId EVERYONE_CONCENTRATE_YOUR_ATTACKS_ON_S1_SHOW_THE_ENEMY_YOUR_RESOLVE;

	/**
	 * ID: 1300013<br>
	 * Message: Attacking the enemy's reinforcements is necessary. Time to die!
	 */
	public static final NpcStringId ATTACKING_THE_ENEMYS_REINFORCEMENTS_IS_NECESSARY_TIME_TO_DIE;

	/**
	 * ID: 1300014<br>
	 * Message: Spirit of Fire, unleash your power! Burn the enemy!!
	 */
	public static final NpcStringId SPIRIT_OF_FIRE_UNLEASH_YOUR_POWER_BURN_THE_ENEMY;

	/**
	 * ID: 1300015<br>
	 * Message: Hey, these foes are tougher than they look. I'm going to need some help here.
	 */
	public static final NpcStringId HEY_THESE_FOES_ARE_TOUGHER_THAN_THEY_LOOK_IM_GOING_TO_NEED_SOME_HELP_HERE;

	/**
	 * ID: 1300016<br>
	 * Message: Do you need my power? You seem to be struggling.
	 */
	public static final NpcStringId DO_YOU_NEED_MY_POWER_YOU_SEEM_TO_BE_STRUGGLING;

	/**
	 * ID: 1300017<br>
	 * Message: I'm rather busy here as well.
	 */
	public static final NpcStringId IM_RATHER_BUSY_HERE_AS_WELL;

	/**
	 * ID: 1300018<br>
	 * Message: Don't think that it's gonna end like this. Your ambition will soon be destroyed as well.
	 */
	public static final NpcStringId
			DONT_THINK_THAT_ITS_GONNA_END_LIKE_THIS_YOUR_AMBITION_WILL_SOON_BE_DESTROYED_AS_WELL;

	/**
	 * ID: 1300019<br>
	 * Message: You must have been prepared to die!
	 */
	public static final NpcStringId YOU_MUST_HAVE_BEEN_PREPARED_TO_DIE;

	/**
	 * ID: 1300020<br>
	 * Message: I feel so much grief that I can't even take care of myself. There isn't any reason for me to stay here any longer.
	 */
	public static final NpcStringId
			I_FEEL_SO_MUCH_GRIEF_THAT_I_CANT_EVEN_TAKE_CARE_OF_MYSELF_THERE_ISNT_ANY_REASON_FOR_ME_TO_STAY_HERE_ANY_LONGER;

	/**
	 * ID: 1300101<br>
	 * Message: Shanty Fortress
	 */
	public static final NpcStringId SHANTY_FORTRESS;

	/**
	 * ID: 1300102<br>
	 * Message: Southern Fortress
	 */
	public static final NpcStringId SOUTHERN_FORTRESS;

	/**
	 * ID: 1300103<br>
	 * Message: Hive Fortress
	 */
	public static final NpcStringId HIVE_FORTRESS;

	/**
	 * ID: 1300104<br>
	 * Message: Valley Fortress
	 */
	public static final NpcStringId VALLEY_FORTRESS;

	/**
	 * ID: 1300105<br>
	 * Message: Ivory Fortress
	 */
	public static final NpcStringId IVORY_FORTRESS;

	/**
	 * ID: 1300106<br>
	 * Message: Narsell Fortress
	 */
	public static final NpcStringId NARSELL_FORTRESS;

	/**
	 * ID: 1300107<br>
	 * Message: Basin Fortress
	 */
	public static final NpcStringId BASIN_FORTRESS;

	/**
	 * ID: 1300108<br>
	 * Message: White Sands Fortress
	 */
	public static final NpcStringId WHITE_SANDS_FORTRESS;

	/**
	 * ID: 1300109<br>
	 * Message: Borderland Fortress
	 */
	public static final NpcStringId BORDERLAND_FORTRESS;

	/**
	 * ID: 1300110<br>
	 * Message: Swamp Fortress
	 */
	public static final NpcStringId SWAMP_FORTRESS;

	/**
	 * ID: 1300111<br>
	 * Message: Archaic Fortress
	 */
	public static final NpcStringId ARCHAIC_FORTRESS;

	/**
	 * ID: 1300112<br>
	 * Message: Floran Fortress
	 */
	public static final NpcStringId FLORAN_FORTRESS;

	/**
	 * ID: 1300113<br>
	 * Message: Cloud Mountain Fortress
	 */
	public static final NpcStringId CLOUD_MOUNTAIN_FORTRESS;

	/**
	 * ID: 1300114<br>
	 * Message: Tanor Fortress
	 */
	public static final NpcStringId TANOR_FORTRESS;

	/**
	 * ID: 1300115<br>
	 * Message: Dragonspine Fortress
	 */
	public static final NpcStringId DRAGONSPINE_FORTRESS;

	/**
	 * ID: 1300116<br>
	 * Message: Antharas's Fortress
	 */
	public static final NpcStringId ANTHARASS_FORTRESS;

	/**
	 * ID: 1300117<br>
	 * Message: Western Fortress
	 */
	public static final NpcStringId WESTERN_FORTRESS;

	/**
	 * ID: 1300118<br>
	 * Message: Hunter's Fortress
	 */
	public static final NpcStringId HUNTERS_FORTRESS;

	/**
	 * ID: 1300119<br>
	 * Message: Aaru Fortress
	 */
	public static final NpcStringId AARU_FORTRESS;

	/**
	 * ID: 1300120<br>
	 * Message: Demon Fortress
	 */
	public static final NpcStringId DEMON_FORTRESS;

	/**
	 * ID: 1300121<br>
	 * Message: Monastic Fortress
	 */
	public static final NpcStringId MONASTIC_FORTRESS;

	/**
	 * ID: 1300122<br>
	 * Message: Independent State
	 */
	public static final NpcStringId INDEPENDENT_STATE;

	/**
	 * ID: 1300123<br>
	 * Message: Nonpartisan
	 */
	public static final NpcStringId NONPARTISAN;

	/**
	 * ID: 1300124<br>
	 * Message: Contract State
	 */
	public static final NpcStringId CONTRACT_STATE;

	/**
	 * ID: 1300125<br>
	 * Message: First password has been entered.
	 */
	public static final NpcStringId FIRST_PASSWORD_HAS_BEEN_ENTERED;

	/**
	 * ID: 1300126<br>
	 * Message: Second password has been entered.
	 */
	public static final NpcStringId SECOND_PASSWORD_HAS_BEEN_ENTERED;

	/**
	 * ID: 1300127<br>
	 * Message: Password has not been entered.
	 */
	public static final NpcStringId PASSWORD_HAS_NOT_BEEN_ENTERED;

	/**
	 * ID: 1300128<br>
	 * Message: Attempt $s1 / 3 is in progress. => This is the third attempt on $s1.
	 */
	public static final NpcStringId ATTEMPT_S1_3_IS_IN_PROGRESS_THIS_IS_THE_THIRD_ATTEMPT_ON_S1;

	/**
	 * ID: 1300129<br>
	 * Message: The 1st Mark is correct.
	 */
	public static final NpcStringId THE_1ST_MARK_IS_CORRECT;

	/**
	 * ID: 1300130<br>
	 * Message: The 2nd Mark is correct.
	 */
	public static final NpcStringId THE_2ND_MARK_IS_CORRECT;

	/**
	 * ID: 1300131<br>
	 * Message: The Marks have not been assembled.
	 */
	public static final NpcStringId THE_MARKS_HAVE_NOT_BEEN_ASSEMBLED;

	/**
	 * ID: 1300132<br>
	 * Message: Olympiad class-free team match is going to begin in Arena $s1 in a moment.
	 */
	public static final NpcStringId OLYMPIAD_CLASS_FREE_TEAM_MATCH_IS_GOING_TO_BEGIN_IN_ARENA_S1_IN_A_MOMENT;

	/**
	 * ID: 1300133<br>
	 * Message: Domain Fortress
	 */
	public static final NpcStringId DOMAIN_FORTRESS;

	/**
	 * ID: 1300134<br>
	 * Message: Boundary Fortress
	 */
	public static final NpcStringId BOUNDARY_FORTRESS;

	/**
	 * ID: 1300135<br>
	 * Message: $s1hour $s2minute
	 */
	public static final NpcStringId S1HOUR_S2MINUTE;

	/**
	 * ID: 1300136<br>
	 * Message: Not designated
	 */
	public static final NpcStringId NOT_DESIGNATED;

	/**
	 * ID: 1300137<br>
	 * Message: Warriors, have you come to help those who are imprisoned here?
	 */
	public static final NpcStringId WARRIORS_HAVE_YOU_COME_TO_HELP_THOSE_WHO_ARE_IMPRISONED_HERE;

	/**
	 * ID: 1300138<br>
	 * Message: Take that, you weakling!
	 */
	public static final NpcStringId TAKE_THAT_YOU_WEAKLING;

	/**
	 * ID: 1300139<br>
	 * Message: Behold my might!
	 */
	public static final NpcStringId BEHOLD_MY_MIGHT;

	/**
	 * ID: 1300140<br>
	 * Message: Your mind is going blank...
	 */
	public static final NpcStringId YOUR_MIND_IS_GOING_BLANK;

	/**
	 * ID: 1300141<br>
	 * Message: Ugh, it hurts down to the bones...
	 */
	public static final NpcStringId UGH_IT_HURTS_DOWN_TO_THE_BONES;

	/**
	 * ID: 1300142<br>
	 * Message: I can't stand it anymore. Aah...
	 */
	public static final NpcStringId I_CANT_STAND_IT_ANYMORE_AAH;

	/**
	 * ID: 1300143<br>
	 * Message: Kyaaak!
	 */
	public static final NpcStringId KYAAAK;

	/**
	 * ID: 1300144<br>
	 * Message: Gasp! How can this be?
	 */
	public static final NpcStringId GASP_HOW_CAN_THIS_BE;

	/**
	 * ID: 1300145<br>
	 * Message: I'll rip the flesh from your bones!
	 */
	public static final NpcStringId ILL_RIP_THE_FLESH_FROM_YOUR_BONES;

	/**
	 * ID: 1300146<br>
	 * Message: You'll flounder in delusion for the rest of your life!
	 */
	public static final NpcStringId YOULL_FLOUNDER_IN_DELUSION_FOR_THE_REST_OF_YOUR_LIFE;

	/**
	 * ID: 1300147<br>
	 * Message: There is no escape from this place!
	 */
	public static final NpcStringId THERE_IS_NO_ESCAPE_FROM_THIS_PLACE;

	/**
	 * ID: 1300148<br>
	 * Message: How dare you!
	 */
	public static final NpcStringId HOW_DARE_YOU;

	/**
	 * ID: 1300149<br>
	 * Message: I shall defeat you.
	 */
	public static final NpcStringId I_SHALL_DEFEAT_YOU;

	/**
	 * ID: 1300150<br>
	 * Message: Begin stage 1!
	 */
	public static final NpcStringId BEGIN_STAGE_1;

	/**
	 * ID: 1300151<br>
	 * Message: Begin stage 2!
	 */
	public static final NpcStringId BEGIN_STAGE_2;

	/**
	 * ID: 1300152<br>
	 * Message: Begin stage 3!
	 */
	public static final NpcStringId BEGIN_STAGE_3;

	/**
	 * ID: 1300153<br>
	 * Message: You've done it! We believed in you, warrior. We want to show our sincerity, though it is small. Please give me some of your time.
	 */
	public static final NpcStringId
			YOUVE_DONE_IT_WE_BELIEVED_IN_YOU_WARRIOR_WE_WANT_TO_SHOW_OUR_SINCERITY_THOUGH_IT_IS_SMALL_PLEASE_GIVE_ME_SOME_OF_YOUR_TIME;

	/**
	 * ID: 1300154<br>
	 * Message: The Central Stronghold's compressor is working.
	 */
	public static final NpcStringId THE_CENTRAL_STRONGHOLDS_COMPRESSOR_IS_WORKING;

	/**
	 * ID: 1300155<br>
	 * Message: Stronghold I's compressor is working.
	 */
	public static final NpcStringId STRONGHOLD_IS_COMPRESSOR_IS_WORKING;

	/**
	 * ID: 1300156<br>
	 * Message: Stronghold II's compressor is working.
	 */
	public static final NpcStringId STRONGHOLD_IIS_COMPRESSOR_IS_WORKING;

	/**
	 * ID: 1300157<br>
	 * Message: Stronghold III's compressor is working.
	 */
	public static final NpcStringId STRONGHOLD_IIIS_COMPRESSOR_IS_WORKING;

	/**
	 * ID: 1300158<br>
	 * Message: The Central Stronghold's compressor has been destroyed.
	 */
	public static final NpcStringId THE_CENTRAL_STRONGHOLDS_COMPRESSOR_HAS_BEEN_DESTROYED;

	/**
	 * ID: 1300159<br>
	 * Message: Stronghold I's compressor has been destroyed.
	 */
	public static final NpcStringId STRONGHOLD_IS_COMPRESSOR_HAS_BEEN_DESTROYED;

	/**
	 * ID: 1300160<br>
	 * Message: Stronghold II's compressor has been destroyed.
	 */
	public static final NpcStringId STRONGHOLD_IIS_COMPRESSOR_HAS_BEEN_DESTROYED;

	/**
	 * ID: 1300161<br>
	 * Message: Stronghold III's compressor has been destroyed.
	 */
	public static final NpcStringId STRONGHOLD_IIIS_COMPRESSOR_HAS_BEEN_DESTROYED;

	/**
	 * ID: 1300162<br>
	 * Message: What a predicament... my attempts were unsuccessful.
	 */
	public static final NpcStringId WHAT_A_PREDICAMENT_MY_ATTEMPTS_WERE_UNSUCCESSFUL;

	/**
	 * ID: 1300163<br>
	 * Message: Courage! Ambition! Passion! Mercenaries who want to realize their dream of fighting in the territory war, come to me! Fortune and glory are waiting for you!
	 */
	public static final NpcStringId
			COURAGE_AMBITION_PASSION_MERCENARIES_WHO_WANT_TO_REALIZE_THEIR_DREAM_OF_FIGHTING_IN_THE_TERRITORY_WAR_COME_TO_ME_FORTUNE_AND_GLORY_ARE_WAITING_FOR_YOU;

	/**
	 * ID: 1300164<br>
	 * Message: Do you wish to fight? Are you afraid? No matter how hard you try, you have nowhere to run. But if you face it head on, our mercenary troop will help you out!
	 */
	public static final NpcStringId
			DO_YOU_WISH_TO_FIGHT_ARE_YOU_AFRAID_NO_MATTER_HOW_HARD_YOU_TRY_YOU_HAVE_NOWHERE_TO_RUN_BUT_IF_YOU_FACE_IT_HEAD_ON_OUR_MERCENARY_TROOP_WILL_HELP_YOU_OUT;

	/**
	 * ID: 1300165<br>
	 * Message: Charge! Charge! Charge!
	 */
	public static final NpcStringId CHARGE_CHARGE_CHARGE;

	/**
	 * ID: 1300166<br>
	 * Message: Olympiad class-free individual match is going to begin in Arena $s1 in a moment.
	 */
	public static final NpcStringId OLYMPIAD_CLASS_FREE_INDIVIDUAL_MATCH_IS_GOING_TO_BEGIN_IN_ARENA_S1_IN_A_MOMENT;

	/**
	 * ID: 1300167<br>
	 * Message: Olympiad class individual match is going to begin in Arena $s1 in a moment.
	 */
	public static final NpcStringId OLYMPIAD_CLASS_INDIVIDUAL_MATCH_IS_GOING_TO_BEGIN_IN_ARENA_S1_IN_A_MOMENT;

	/**
	 * ID: 1600001<br>
	 * Message: Another player is currently being buffed. Please try again in a moment.
	 */
	public static final NpcStringId ANOTHER_PLAYER_IS_CURRENTLY_BEING_BUFFED_PLEASE_TRY_AGAIN_IN_A_MOMENT;

	/**
	 * ID: 1600002<br>
	 * Message: You cannot mount while you are polymorphed.
	 */
	public static final NpcStringId YOU_CANNOT_MOUNT_WHILE_YOU_ARE_POLYMORPHED;

	/**
	 * ID: 1600003<br>
	 * Message: You cannot mount a Wyvern while in battle mode or while mounted on a Strider.
	 */
	public static final NpcStringId YOU_CANNOT_MOUNT_A_WYVERN_WHILE_IN_BATTLE_MODE_OR_WHILE_MOUNTED_ON_A_STRIDER;

	/**
	 * ID: 1600004<br>
	 * Message: Boo-hoo... I hate...
	 */
	public static final NpcStringId BOO_HOO_I_HATE;

	/**
	 * ID: 1600005<br>
	 * Message: See you later.
	 */
	public static final NpcStringId SEE_YOU_LATER;

	/**
	 * ID: 1600006<br>
	 * Message: You've made a great choice.
	 */
	public static final NpcStringId YOUVE_MADE_A_GREAT_CHOICE;

	/**
	 * ID: 1600007<br>
	 * Message: Thanks! I feel more relaxed
	 */
	public static final NpcStringId THANKS_I_FEEL_MORE_RELAXED;

	/**
	 * ID: 1600008<br>
	 * Message: Did you see that Firecracker explode?
	 */
	public static final NpcStringId DID_YOU_SEE_THAT_FIRECRACKER_EXPLODE;

	/**
	 * ID: 1600009<br>
	 * Message: I am nothing.
	 */
	public static final NpcStringId I_AM_NOTHING;

	/**
	 * ID: 1600010<br>
	 * Message: I am telling the truth.
	 */
	public static final NpcStringId I_AM_TELLING_THE_TRUTH;

	/**
	 * ID: 1600012<br>
	 * Message: It's free to go back to the village you teleported from.
	 */
	public static final NpcStringId ITS_FREE_TO_GO_BACK_TO_THE_VILLAGE_YOU_TELEPORTED_FROM;

	/**
	 * ID: 1600013<br>
	 * Message: If you collect 50 individual Treasure Sack Pieces, you can exchange them for a Treasure Sack.
	 */
	public static final NpcStringId
			IF_YOU_COLLECT_50_INDIVIDUAL_TREASURE_SACK_PIECES_YOU_CAN_EXCHANGE_THEM_FOR_A_TREASURE_SACK;

	/**
	 * ID: 1600014<br>
	 * Message: You must be transformed into a Treasure Hunter to find a chest.
	 */
	public static final NpcStringId YOU_MUST_BE_TRANSFORMED_INTO_A_TREASURE_HUNTER_TO_FIND_A_CHEST;

	/**
	 * ID: 1600015<br>
	 * Message: You'd better use the Transformation spell at the right moment since it doesn't last long.
	 */
	public static final NpcStringId
			YOUD_BETTER_USE_THE_TRANSFORMATION_SPELL_AT_THE_RIGHT_MOMENT_SINCE_IT_DOESNT_LAST_LONG;

	/**
	 * ID: 1600016<br>
	 * Message: All of Fantasy Isle is a Peace Zone.
	 */
	public static final NpcStringId ALL_OF_FANTASY_ISLE_IS_A_PEACE_ZONE;

	/**
	 * ID: 1600017<br>
	 * Message: If you need to go to Fantasy Isle, come see me.
	 */
	public static final NpcStringId IF_YOU_NEED_TO_GO_TO_FANTASY_ISLE_COME_SEE_ME;

	/**
	 * ID: 1600018<br>
	 * Message: You can only purchase a Treasure Hunter Transformation Scroll once every 12 hours.
	 */
	public static final NpcStringId YOU_CAN_ONLY_PURCHASE_A_TREASURE_HUNTER_TRANSFORMATION_SCROLL_ONCE_EVERY_12_HOURS;

	/**
	 * ID: 1600019<br>
	 * Message: If your means of arrival was a bit unconventional, then I'll be sending you back to Rune Township, which is the nearest town.
	 */
	public static final NpcStringId
			IF_YOUR_MEANS_OF_ARRIVAL_WAS_A_BIT_UNCONVENTIONAL_THEN_ILL_BE_SENDING_YOU_BACK_TO_RUNE_TOWNSHIP_WHICH_IS_THE_NEAREST_TOWN;

	/**
	 * ID: 1600020<br>
	 * Message: *Rattle*
	 */
	public static final NpcStringId RATTLE;

	/**
	 * ID: 1600021<br>
	 * Message: *Bump*
	 */
	public static final NpcStringId BUMP;

	/**
	 * ID: 1600022<br>
	 * Message: You will regret this.
	 */
	public static final NpcStringId YOU_WILL_REGRET_THIS;

	/**
	 * ID: 1600023<br>
	 * Message: Care to challenge fate and test your luck?
	 */
	public static final NpcStringId CARE_TO_CHALLENGE_FATE_AND_TEST_YOUR_LUCK;

	/**
	 * ID: 1600024<br>
	 * Message: Don't pass up the chance to win an S80 weapon.
	 */
	public static final NpcStringId DONT_PASS_UP_THE_CHANCE_TO_WIN_AN_S80_WEAPON;

	/**
	 * ID: 1800009<br>
	 * Message: # $s1's Command Channel has looting rights.
	 */
	public static final NpcStringId _S1S_COMMAND_CHANNEL_HAS_LOOTING_RIGHTS;

	/**
	 * ID: 1800010<br>
	 * Message: Looting rules are no longer active.
	 */
	public static final NpcStringId LOOTING_RULES_ARE_NO_LONGER_ACTIVE;

	/**
	 * ID: 1800011<br>
	 * Message: Our master now comes to claim our vengeance. Soon you will all be nothing more than dirt...
	 */
	public static final NpcStringId
			OUR_MASTER_NOW_COMES_TO_CLAIM_OUR_VENGEANCE_SOON_YOU_WILL_ALL_BE_NOTHING_MORE_THAN_DIRT;

	/**
	 * ID: 1800012<br>
	 * Message: Death to those who challenge the Lords of Dawn!
	 */
	public static final NpcStringId DEATH_TO_THOSE_WHO_CHALLENGE_THE_LORDS_OF_DAWN;

	/**
	 * ID: 1800013<br>
	 * Message: Death to those who challenge the Lord!
	 */
	public static final NpcStringId DEATH_TO_THOSE_WHO_CHALLENGE_THE_LORD;

	/**
	 * ID: 1800014<br>
	 * Message: Oink oink! Pigs can do it too! Antharas-Surpassing Super Powered Pig Stun! How do like them apples?
	 */
	public static final NpcStringId
			OINK_OINK_PIGS_CAN_DO_IT_TOO_ANTHARAS_SURPASSING_SUPER_POWERED_PIG_STUN_HOW_DO_LIKE_THEM_APPLES;

	/**
	 * ID: 1800015<br>
	 * Message: Oink oink! Take that! Valakas-Terrorizing Ultra Pig Fear! Ha ha!
	 */
	public static final NpcStringId OINK_OINK_TAKE_THAT_VALAKAS_TERRORIZING_ULTRA_PIG_FEAR_HA_HA;

	/**
	 * ID: 1800016<br>
	 * Message: Oink oink! Go away! Stop bothering me!
	 */
	public static final NpcStringId OINK_OINK_GO_AWAY_STOP_BOTHERING_ME;

	/**
	 * ID: 1800017<br>
	 * Message: Oink oink! Pigs of the world unite! Let's show them our strength!
	 */
	public static final NpcStringId OINK_OINK_PIGS_OF_THE_WORLD_UNITE_LETS_SHOW_THEM_OUR_STRENGTH;

	/**
	 * ID: 1800018<br>
	 * Message: You healed me. Thanks a lot! Oink oink!
	 */
	public static final NpcStringId YOU_HEALED_ME_THANKS_A_LOT_OINK_OINK;

	/**
	 * ID: 1800019<br>
	 * Message: Oink oink! This treatment hurts too much! Are you sure that you're trying to heal me?
	 */
	public static final NpcStringId OINK_OINK_THIS_TREATMENT_HURTS_TOO_MUCH_ARE_YOU_SURE_THAT_YOURE_TRYING_TO_HEAL_ME;

	/**
	 * ID: 1800020<br>
	 * Message: Oink oink! Transform with Moon Crystal Prism Power!
	 */
	public static final NpcStringId OINK_OINK_TRANSFORM_WITH_MOON_CRYSTAL_PRISM_POWER;

	/**
	 * ID: 1800021<br>
	 * Message: Oink oink! Nooo! I don't want to go back to normal!
	 */
	public static final NpcStringId OINK_OINK_NOOO_I_DONT_WANT_TO_GO_BACK_TO_NORMAL;

	/**
	 * ID: 1800022<br>
	 * Message: Oink oink! I'm rich, so I've got plenty to share! Thanks!
	 */
	public static final NpcStringId OINK_OINK_IM_RICH_SO_IVE_GOT_PLENTY_TO_SHARE_THANKS;

	/**
	 * ID: 1800023<br>
	 * Message: It's a weapon surrounded by an ominous aura. I'll discard it because it may be dangerous. Miss...!
	 */
	public static final NpcStringId
			ITS_A_WEAPON_SURROUNDED_BY_AN_OMINOUS_AURA_ILL_DISCARD_IT_BECAUSE_IT_MAY_BE_DANGEROUS_MISS;

	/**
	 * ID: 1800024<br>
	 * Message: Thank you for saving me from the clutches of evil!
	 */
	public static final NpcStringId THANK_YOU_FOR_SAVING_ME_FROM_THE_CLUTCHES_OF_EVIL;

	/**
	 * ID: 1800025<br>
	 * Message: That is it for today...let's retreat. Everyone pull back!
	 */
	public static final NpcStringId THAT_IS_IT_FOR_TODAYLETS_RETREAT_EVERYONE_PULL_BACK;

	/**
	 * ID: 1800026<br>
	 * Message: Thank you for the rescue. It's a small gift.
	 */
	public static final NpcStringId THANK_YOU_FOR_THE_RESCUE_ITS_A_SMALL_GIFT;

	/**
	 * ID: 1800027<br>
	 * Message: $s1.. You don't have a Red Crystal...
	 */
	public static final NpcStringId S1_YOU_DONT_HAVE_A_RED_CRYSTAL;

	/**
	 * ID: 1800028<br>
	 * Message: $s1.. You don't have a Blue Crystal...
	 */
	public static final NpcStringId S1_YOU_DONT_HAVE_A_BLUE_CRYSTAL;

	/**
	 * ID: 1800029<br>
	 * Message: $s1.. You don't have a Clear Crystal...
	 */
	public static final NpcStringId S1_YOU_DONT_HAVE_A_CLEAR_CRYSTAL;

	/**
	 * ID: 1800030<br>
	 * Message: $s1.. If you are too far away from me...I can't let you go...
	 */
	public static final NpcStringId S1_IF_YOU_ARE_TOO_FAR_AWAY_FROM_MEI_CANT_LET_YOU_GO;

	/**
	 * ID: 1800031<br>
	 * Message: An alarm has been set off! Everybody will be in danger if they are not taken care of immediately!
	 */
	public static final NpcStringId
			AN_ALARM_HAS_BEEN_SET_OFF_EVERYBODY_WILL_BE_IN_DANGER_IF_THEY_ARE_NOT_TAKEN_CARE_OF_IMMEDIATELY;

	/**
	 * ID: 1800032<br>
	 * Message: It will not be that easy to kill me!
	 */
	public static final NpcStringId IT_WILL_NOT_BE_THAT_EASY_TO_KILL_ME;

	/**
	 * ID: 1800033<br>
	 * Message: No...You knew my weakness...
	 */
	public static final NpcStringId NOYOU_KNEW_MY_WEAKNESS;

	/**
	 * ID: 1800034<br>
	 * Message: Hello? Is anyone there?
	 */
	public static final NpcStringId HELLO_IS_ANYONE_THERE;

	/**
	 * ID: 1800035<br>
	 * Message: Is no one there? How long have I been hiding? I have been starving for days and cannot hold out anymore!
	 */
	public static final NpcStringId
			IS_NO_ONE_THERE_HOW_LONG_HAVE_I_BEEN_HIDING_I_HAVE_BEEN_STARVING_FOR_DAYS_AND_CANNOT_HOLD_OUT_ANYMORE;

	/**
	 * ID: 1800036<br>
	 * Message: If someone would give me some of those tasty Crystal Fragments, I would gladly tell them where Tears is hiding! Yummy, yummy!
	 */
	public static final NpcStringId
			IF_SOMEONE_WOULD_GIVE_ME_SOME_OF_THOSE_TASTY_CRYSTAL_FRAGMENTS_I_WOULD_GLADLY_TELL_THEM_WHERE_TEARS_IS_HIDING_YUMMY_YUMMY;

	/**
	 * ID: 1800037<br>
	 * Message: Hey! You from above the ground! Let's share some Crystal Fragments, if you have any.
	 */
	public static final NpcStringId HEY_YOU_FROM_ABOVE_THE_GROUND_LETS_SHARE_SOME_CRYSTAL_FRAGMENTS_IF_YOU_HAVE_ANY;

	/**
	 * ID: 1800038<br>
	 * Message: Crispy and cold feeling! Teehee! Delicious!!
	 */
	public static final NpcStringId CRISPY_AND_COLD_FEELING_TEEHEE_DELICIOUS;

	/**
	 * ID: 1800039<br>
	 * Message: Yummy! This is so tasty.
	 */
	public static final NpcStringId YUMMY_THIS_IS_SO_TASTY;

	/**
	 * ID: 1800040<br>
	 * Message: Sniff, sniff! Give me more Crystal Fragments!
	 */
	public static final NpcStringId SNIFF_SNIFF_GIVE_ME_MORE_CRYSTAL_FRAGMENTS;

	/**
	 * ID: 1800041<br>
	 * Message: How insensitive! It's not nice to give me just a piece! Can't you give me more?
	 */
	public static final NpcStringId HOW_INSENSITIVE_ITS_NOT_NICE_TO_GIVE_ME_JUST_A_PIECE_CANT_YOU_GIVE_ME_MORE;

	/**
	 * ID: 1800042<br>
	 * Message: Ah - I'm hungry!
	 */
	public static final NpcStringId AH_IM_HUNGRY;

	/**
	 * ID: 1800043<br>
	 * Message: I'm the real one!
	 */
	public static final NpcStringId IM_THE_REAL_ONE;

	/**
	 * ID: 1800044<br>
	 * Message: Pick me!
	 */
	public static final NpcStringId PICK_ME;

	/**
	 * ID: 1800045<br>
	 * Message: Trust me!
	 */
	public static final NpcStringId TRUST_ME;

	/**
	 * ID: 1800046<br>
	 * Message: Not that dude, I'm the real one!
	 */
	public static final NpcStringId NOT_THAT_DUDE_IM_THE_REAL_ONE;

	/**
	 * ID: 1800047<br>
	 * Message: Don't be fooled! Don't be fooled! I'm the real one!!
	 */
	public static final NpcStringId DONT_BE_FOOLED_DONT_BE_FOOLED_IM_THE_REAL_ONE;

	/**
	 * ID: 1800048<br>
	 * Message: Just act like the real one! Oops!
	 */
	public static final NpcStringId JUST_ACT_LIKE_THE_REAL_ONE_OOPS;

	/**
	 * ID: 1800049<br>
	 * Message: You've been fooled!
	 */
	public static final NpcStringId YOUVE_BEEN_FOOLED;

	/**
	 * ID: 1800050<br>
	 * Message: Sorry, but...I'm the fake one.
	 */
	public static final NpcStringId SORRY_BUT_IM_THE_FAKE_ONE;

	/**
	 * ID: 1800051<br>
	 * Message: I'm the real one! Phew!!
	 */
	public static final NpcStringId IM_THE_REAL_ONE_PHEW;

	/**
	 * ID: 1800052<br>
	 * Message: Can't you even find out?
	 */
	public static final NpcStringId CANT_YOU_EVEN_FIND_OUT;

	/**
	 * ID: 1800053<br>
	 * Message: Find me!
	 */
	public static final NpcStringId FIND_ME;

	/**
	 * ID: 1800054<br>
	 * Message: Huh?! How did you know it was me?
	 */
	public static final NpcStringId HUH_HOW_DID_YOU_KNOW_IT_WAS_ME;

	/**
	 * ID: 1800055<br>
	 * Message: Excellent choice! Teehee!
	 */
	public static final NpcStringId EXCELLENT_CHOICE_TEEHEE;

	/**
	 * ID: 1800056<br>
	 * Message: You've done well!
	 */
	public static final NpcStringId YOUVE_DONE_WELL;

	/**
	 * ID: 1800057<br>
	 * Message: Oh... very sensible?
	 */
	public static final NpcStringId OH_VERY_SENSIBLE;

	/**
	 * ID: 1800058<br>
	 * Message: Behold the mighty power of Baylor, foolish mortal!
	 */
	public static final NpcStringId BEHOLD_THE_MIGHTY_POWER_OF_BAYLOR_FOOLISH_MORTAL;

	/**
	 * ID: 1800059<br>
	 * Message: No one is going to survive!
	 */
	public static final NpcStringId NO_ONE_IS_GOING_TO_SURVIVE;

	/**
	 * ID: 1800060<br>
	 * Message: You'll see what hell is like...
	 */
	public static final NpcStringId YOULL_SEE_WHAT_HELL_IS_LIKE;

	/**
	 * ID: 1800061<br>
	 * Message: You will be put in jail!
	 */
	public static final NpcStringId YOU_WILL_BE_PUT_IN_JAIL;

	/**
	 * ID: 1800062<br>
	 * Message: Worthless creature... Go to hell!
	 */
	public static final NpcStringId WORTHLESS_CREATURE_GO_TO_HELL;

	/**
	 * ID: 1800063<br>
	 * Message: I'll give you something that you'll never forget!
	 */
	public static final NpcStringId ILL_GIVE_YOU_SOMETHING_THAT_YOULL_NEVER_FORGET;

	/**
	 * ID: 1800064<br>
	 * Message: Why did you trust to choose me? Hahahaha....
	 */
	public static final NpcStringId WHY_DID_YOU_TRUST_TO_CHOOSE_ME_HAHAHAHA;

	/**
	 * ID: 1800065<br>
	 * Message: I'll make you regret that you ever chose me...
	 */
	public static final NpcStringId ILL_MAKE_YOU_REGRET_THAT_YOU_EVER_CHOSE_ME;

	/**
	 * ID: 1800066<br>
	 * Message: Don't expect to get out alive..
	 */
	public static final NpcStringId DONT_EXPECT_TO_GET_OUT_ALIVE;

	/**
	 * ID: 1800067<br>
	 * Message: Demon King Beleth, give me the power! Aaahh!!!
	 */
	public static final NpcStringId DEMON_KING_BELETH_GIVE_ME_THE_POWER_AAAHH;

	/**
	 * ID: 1800068<br>
	 * Message: No....... I feel the power of Fafurion.......
	 */
	public static final NpcStringId NO_I_FEEL_THE_POWER_OF_FAFURION;

	/**
	 * ID: 1800069<br>
	 * Message: Fafurion! Please give power to this helpless witch!!
	 */
	public static final NpcStringId FAFURION_PLEASE_GIVE_POWER_TO_THIS_HELPLESS_WITCH;

	/**
	 * ID: 1800070<br>
	 * Message: I can't help you much, but I can weaken the power of Baylor since I'm locked up here.
	 */
	public static final NpcStringId I_CANT_HELP_YOU_MUCH_BUT_I_CAN_WEAKEN_THE_POWER_OF_BAYLOR_SINCE_IM_LOCKED_UP_HERE;

	/**
	 * ID: 1800071<br>
	 * Message: Your skill is impressive. I'll admit that you are good enough to pass. Take the key and leave this place.
	 */
	public static final NpcStringId
			YOUR_SKILL_IS_IMPRESSIVE_ILL_ADMIT_THAT_YOU_ARE_GOOD_ENOUGH_TO_PASS_TAKE_THE_KEY_AND_LEAVE_THIS_PLACE;

	/**
	 * ID: 1800072<br>
	 * Message: Give me all you have! It's the only way I'll let you live!!
	 */
	public static final NpcStringId GIVE_ME_ALL_YOU_HAVE_ITS_THE_ONLY_WAY_ILL_LET_YOU_LIVE;

	/**
	 * ID: 1800073<br>
	 * Message: Hun.. hungry
	 */
	public static final NpcStringId HUN_HUNGRY;

	/**
	 * ID: 1800074<br>
	 * Message: Don't be lazy! You bastards!
	 */
	public static final NpcStringId DONT_BE_LAZY_YOU_BASTARDS;

	/**
	 * ID: 1800075<br>
	 * Message: They are just henchmen of the Iron Castle... Why did we hide?
	 */
	public static final NpcStringId THEY_ARE_JUST_HENCHMEN_OF_THE_IRON_CASTLE_WHY_DID_WE_HIDE;

	/**
	 * ID: 1800076<br>
	 * Message: Guys, show them our power!!!!
	 */
	public static final NpcStringId GUYS_SHOW_THEM_OUR_POWER;

	/**
	 * ID: 1800077<br>
	 * Message: You have finally come here. But you will not be able to find the secret room!
	 */
	public static final NpcStringId YOU_HAVE_FINALLY_COME_HERE_BUT_YOU_WILL_NOT_BE_ABLE_TO_FIND_THE_SECRET_ROOM;

	/**
	 * ID: 1800078<br>
	 * Message: You have done well in finding me, but I cannot just hand you the key!
	 */
	public static final NpcStringId YOU_HAVE_DONE_WELL_IN_FINDING_ME_BUT_I_CANNOT_JUST_HAND_YOU_THE_KEY;

	/**
	 * ID: 1800079<br>
	 * Message: $s1 second(s) remaining.
	 */
	public static final NpcStringId S1_SECONDS_REMAINING;

	/**
	 * ID: 1800081<br>
	 * Message: The match is automatically canceled because you are too far from the admission manager.
	 */
	public static final NpcStringId
			THE_MATCH_IS_AUTOMATICALLY_CANCELED_BECAUSE_YOU_ARE_TOO_FAR_FROM_THE_ADMISSION_MANAGER;

	/**
	 * ID: 1800082<br>
	 * Message: Ugh, I have butterflies in my stomach.. The show starts soon...
	 */
	public static final NpcStringId UGH_I_HAVE_BUTTERFLIES_IN_MY_STOMACH_THE_SHOW_STARTS_SOON;

	/**
	 * ID: 1800083<br>
	 * Message: Thank you all for coming here tonight.
	 */
	public static final NpcStringId THANK_YOU_ALL_FOR_COMING_HERE_TONIGHT;

	/**
	 * ID: 1800084<br>
	 * Message: It is an honor to have the special show today.
	 */
	public static final NpcStringId IT_IS_AN_HONOR_TO_HAVE_THE_SPECIAL_SHOW_TODAY;

	/**
	 * ID: 1800085<br>
	 * Message: Fantasy Isle is fully committed to your happiness.
	 */
	public static final NpcStringId FANTASY_ISLE_IS_FULLY_COMMITTED_TO_YOUR_HAPPINESS;

	/**
	 * ID: 1800086<br>
	 * Message: Now I'd like to introduce the most beautiful singer in Aden. Please welcome...Leyla Mira!
	 */
	public static final NpcStringId NOW_ID_LIKE_TO_INTRODUCE_THE_MOST_BEAUTIFUL_SINGER_IN_ADEN_PLEASE_WELCOMELEYLA_MIRA;

	/**
	 * ID: 1800087<br>
	 * Message: Here she comes!
	 */
	public static final NpcStringId HERE_SHE_COMES;

	/**
	 * ID: 1800088<br>
	 * Message: Thank you very much, Leyla!
	 */
	public static final NpcStringId THANK_YOU_VERY_MUCH_LEYLA;

	/**
	 * ID: 1800089<br>
	 * Message: Now we're in for a real treat.
	 */
	public static final NpcStringId NOW_WERE_IN_FOR_A_REAL_TREAT;

	/**
	 * ID: 1800090<br>
	 * Message: Just back from their world tour put your hands together for the Fantasy Isle Circus!
	 */
	public static final NpcStringId JUST_BACK_FROM_THEIR_WORLD_TOUR_PUT_YOUR_HANDS_TOGETHER_FOR_THE_FANTASY_ISLE_CIRCUS;

	/**
	 * ID: 1800091<br>
	 * Message: Come on ~ everyone
	 */
	public static final NpcStringId COME_ON_EVERYONE;

	/**
	 * ID: 1800092<br>
	 * Message: Did you like it? That was so amazing.
	 */
	public static final NpcStringId DID_YOU_LIKE_IT_THAT_WAS_SO_AMAZING;

	/**
	 * ID: 1800093<br>
	 * Message: Now we also invited individuals with special talents.
	 */
	public static final NpcStringId NOW_WE_ALSO_INVITED_INDIVIDUALS_WITH_SPECIAL_TALENTS;

	/**
	 * ID: 1800094<br>
	 * Message: Let's welcome the first person here!
	 */
	public static final NpcStringId LETS_WELCOME_THE_FIRST_PERSON_HERE;

	/**
	 * ID: 1800095<br>
	 * Message: ;;;;;;Oh
	 */
	public static final NpcStringId OH;

	/**
	 * ID: 1800096<br>
	 * Message: Okay, now here comes the next person. Come on up please.
	 */
	public static final NpcStringId OKAY_NOW_HERE_COMES_THE_NEXT_PERSON_COME_ON_UP_PLEASE;

	/**
	 * ID: 1800097<br>
	 * Message: Oh, it looks like something great is going to happen, right?
	 */
	public static final NpcStringId OH_IT_LOOKS_LIKE_SOMETHING_GREAT_IS_GOING_TO_HAPPEN_RIGHT;

	/**
	 * ID: 1800098<br>
	 * Message: Oh, my ;;;;
	 */
	public static final NpcStringId OH_MY;

	/**
	 * ID: 1800099<br>
	 * Message: That's g- .. great. Now, here comes the last person.
	 */
	public static final NpcStringId THATS_G_GREAT_NOW_HERE_COMES_THE_LAST_PERSON;

	/**
	 * ID: 1800100<br>
	 * Message: Now this is the end of today's show.
	 */
	public static final NpcStringId NOW_THIS_IS_THE_END_OF_TODAYS_SHOW;

	/**
	 * ID: 1800101<br>
	 * Message: How was it? I hope you all enjoyed it.
	 */
	public static final NpcStringId HOW_WAS_IT_I_HOPE_YOU_ALL_ENJOYED_IT;

	/**
	 * ID: 1800102<br>
	 * Message: Please remember that Fantasy Isle is always planning a lot of great shows for you.
	 */
	public static final NpcStringId PLEASE_REMEMBER_THAT_FANTASY_ISLE_IS_ALWAYS_PLANNING_A_LOT_OF_GREAT_SHOWS_FOR_YOU;

	/**
	 * ID: 1800103<br>
	 * Message: Well, I wish I could continue all night long, but this is it for today. Thank you.
	 */
	public static final NpcStringId WELL_I_WISH_I_COULD_CONTINUE_ALL_NIGHT_LONG_BUT_THIS_IS_IT_FOR_TODAY_THANK_YOU;

	/**
	 * ID: 1800104<br>
	 * Message: We love you.
	 */
	public static final NpcStringId WE_LOVE_YOU;

	/**
	 * ID: 1800105<br>
	 * Message: How come people are not here... We are about to start the show.. Hmm
	 */
	public static final NpcStringId HOW_COME_PEOPLE_ARE_NOT_HERE_WE_ARE_ABOUT_TO_START_THE_SHOW_HMM;

	/**
	 * ID: 1800106<br>
	 * Message: The opponent team canceled the match.
	 */
	public static final NpcStringId THE_OPPONENT_TEAM_CANCELED_THE_MATCH;

	/**
	 * ID: 1800107<br>
	 * Message: It's not easy to obtain.
	 */
	public static final NpcStringId ITS_NOT_EASY_TO_OBTAIN;

	/**
	 * ID: 1800108<br>
	 * Message: You're out of your mind coming here...
	 */
	public static final NpcStringId YOURE_OUT_OF_YOUR_MIND_COMING_HERE;

	/**
	 * ID: 1800109<br>
	 * Message: Enemy invasion! Hurry up!
	 */
	public static final NpcStringId ENEMY_INVASION_HURRY_UP;

	/**
	 * ID: 1800110<br>
	 * Message: Process... shouldn't... be delayed... because of me...
	 */
	public static final NpcStringId PROCESS_SHOULDNT_BE_DELAYED_BECAUSE_OF_ME;

	/**
	 * ID: 1800111<br>
	 * Message: Alright, now Leodas is yours!
	 */
	public static final NpcStringId ALRIGHT_NOW_LEODAS_IS_YOURS;

	/**
	 * ID: 1800112<br>
	 * Message: We might need new slaves... I'll be back soon, so wait!
	 */
	public static final NpcStringId WE_MIGHT_NEED_NEW_SLAVES_ILL_BE_BACK_SOON_SO_WAIT;

	/**
	 * ID: 1800113<br>
	 * Message: Time rift device activation successful!
	 */
	public static final NpcStringId TIME_RIFT_DEVICE_ACTIVATION_SUCCESSFUL;

	/**
	 * ID: 1800114<br>
	 * Message: Freedom or death!!!
	 */
	public static final NpcStringId FREEDOM_OR_DEATH;

	/**
	 * ID: 1800115<br>
	 * Message: This is the will of true warriors!!!
	 */
	public static final NpcStringId THIS_IS_THE_WILL_OF_TRUE_WARRIORS;

	/**
	 * ID: 1800116<br>
	 * Message: We'll have dinner in hell!!!
	 */
	public static final NpcStringId WELL_HAVE_DINNER_IN_HELL;

	/**
	 * ID: 1800117<br>
	 * Message: Detonator initialization- time set for $s1 minute(s) from now-
	 */
	public static final NpcStringId DETONATOR_INITIALIZATION_TIME_S1_MINUTES_FROM_NOW;

	/**
	 * ID: 1800118<br>
	 * Message: Zzzz- city interference error - forward effect created-
	 */
	public static final NpcStringId ZZZZ_CITY_INTERFERENCE_ERROR_FORWARD_EFFECT_CREATED;

	/**
	 * ID: 1800119<br>
	 * Message: Zzzz- city interference error - recurrence effect created-
	 */
	public static final NpcStringId ZZZZ_CITY_INTERFERENCE_ERROR_RECURRENCE_EFFECT_CREATED;

	/**
	 * ID: 1800120<br>
	 * Message: Guards are coming, run!
	 */
	public static final NpcStringId GUARDS_ARE_COMING_RUN;

	/**
	 * ID: 1800121<br>
	 * Message: Now I can escape on my own!
	 */
	public static final NpcStringId NOW_I_CAN_ESCAPE_ON_MY_OWN;

	/**
	 * ID: 1800122<br>
	 * Message: Thanks for your help!
	 */
	public static final NpcStringId THANKS_FOR_YOUR_HELP;

	/**
	 * ID: 1800123<br>
	 * Message: Match cancelled. Opponent did not meet the stadium admission requirements.
	 */
	public static final NpcStringId MATCH_CANCELLED_OPPONENT_DID_NOT_MEET_THE_STADIUM_ADMISSION_REQUIREMENTS;

	/**
	 * ID: 1800124<br>
	 * Message: Ha-ha yes, die slowly writhing in pain and agony!
	 */
	public static final NpcStringId HA_HA_YES_DIE_SLOWLY_WRITHING_IN_PAIN_AND_AGONY;

	/**
	 * ID: 1800125<br>
	 * Message: More... need more... severe pain...
	 */
	public static final NpcStringId MORE_NEED_MORE_SEVERE_PAIN;

	/**
	 * ID: 1800126<br>
	 * Message: Ahh! My life is being drained out!
	 */
	public static final NpcStringId AHH_MY_LIFE_IS_BEING_DRAINED_OUT;

	/**
	 * ID: 1800127<br>
	 * Message: Something is burning inside my body!
	 */
	public static final NpcStringId SOMETHING_IS_BURNING_INSIDE_MY_BODY;

	/**
	 * ID: 1800128<br>
	 * Message: $s1. Not adequate for the stadium level.
	 */
	public static final NpcStringId S1_NOT_ADEQUATE_FOR_THE_STADIUM_LEVEL;

	/**
	 * ID: 1800129<br>
	 * Message: $s1, thank you... for giving me your life...
	 */
	public static final NpcStringId S1_THANK_YOU_FOR_GIVING_ME_YOUR_LIFE;

	/**
	 * ID: 1800130<br>
	 * Message: I failed... Please forgive me, Darion...
	 */
	public static final NpcStringId I_FAILED_PLEASE_FORGIVE_ME_DARION;

	/**
	 * ID: 1800131<br>
	 * Message: $s1, I'll be back... don't get comfortable...
	 */
	public static final NpcStringId S1_ILL_BE_BACK_DONT_GET_COMFORTABLE;

	/**
	 * ID: 1800132<br>
	 * Message: If you think I'm giving up like this.. You're wrong!!
	 */
	public static final NpcStringId IF_YOU_THINK_IM_GIVING_UP_LIKE_THIS_YOURE_WRONG;

	/**
	 * ID: 1800133<br>
	 * Message: So you're just going to watch me suffer?!
	 */
	public static final NpcStringId SO_YOURE_JUST_GOING_TO_WATCH_ME_SUFFER;

	/**
	 * ID: 1800134<br>
	 * Message: It's not over yet!!
	 */
	public static final NpcStringId ITS_NOT_OVER_YET;

	/**
	 * ID: 1800135<br>
	 * Message: HA-HA! You were so afraid of death... let me see... If you find me in time... maybe you can... find a way ...
	 */
	public static final NpcStringId
			HA_HA_YOU_WERE_SO_AFRAID_OF_DEATH_LET_ME_SEE_IF_YOU_FIND_ME_IN_TIME_MAYBE_YOU_CAN_FIND_A_WAY;

	/**
	 * ID: 1800136<br>
	 * Message: Don't kill me please.. Something's strangling me...
	 */
	public static final NpcStringId DONT_KILL_ME_PLEASE_SOMETHINGS_STRANGLING_ME;

	/**
	 * ID: 1800137<br>
	 * Message: Who will be the lucky one tonight? Ha-ha! Curious, very curious!
	 */
	public static final NpcStringId WHO_WILL_BE_THE_LUCKY_ONE_TONIGHT_HA_HA_CURIOUS_VERY_CURIOUS;

	/**
	 * ID: 1800138<br>
	 * Message: Squeak! This will be stronger than the stun the pig used last time!
	 */
	public static final NpcStringId SQUEAK_THIS_WILL_BE_STRONGER_THAN_THE_STUN_THE_PIG_USED_LAST_TIME;

	/**
	 * ID: 1800139<br>
	 * Message: Squeak! Here it goes! Extremely scary, even to Valakas!
	 */
	public static final NpcStringId SQUEAK_HERE_IT_GOES_EXTREMELY_SCARY_EVEN_TO_VALAKAS;

	/**
	 * ID: 1800140<br>
	 * Message: Squeak! Go away! Leave us alone!
	 */
	public static final NpcStringId SQUEAK_GO_AWAY_LEAVE_US_ALONE;

	/**
	 * ID: 1800141<br>
	 * Message: Squeak! Guys, gather up! Let's show our power!
	 */
	public static final NpcStringId SQUEAK_GUYS_GATHER_UP_LETS_SHOW_OUR_POWER;

	/**
	 * ID: 1800142<br>
	 * Message: It's not like I'm giving this because I'm grateful~ Squeak!
	 */
	public static final NpcStringId ITS_NOT_LIKE_IM_GIVING_THIS_BECAUSE_IM_GRATEFUL_SQUEAK;

	/**
	 * ID: 1800143<br>
	 * Message: Squeak! Even if it is treatment, my bottom hurts so much!
	 */
	public static final NpcStringId SQUEAK_EVEN_IF_IT_IS_TREATMENT_MY_BOTTOM_HURTS_SO_MUCH;

	/**
	 * ID: 1800144<br>
	 * Message: Squeak! Transform to Moon Crystal Prism Power!
	 */
	public static final NpcStringId SQUEAK_TRANSFORM_TO_MOON_CRYSTAL_PRISM_POWER;

	/**
	 * ID: 1800145<br>
	 * Message: Squeak! Oh, no! I don't want to turn back again...
	 */
	public static final NpcStringId SQUEAK_OH_NO_I_DONT_WANT_TO_TURN_BACK_AGAIN;

	/**
	 * ID: 1800146<br>
	 * Message: Squeak! I'm specially giving you a lot since I'm rich. Thank you
	 */
	public static final NpcStringId SQUEAK_IM_SPECIALLY_GIVING_YOU_A_LOT_SINCE_IM_RICH_THANK_YOU;

	/**
	 * ID: 1800147<br>
	 * Message: Oink-oink! Rage is boiling up inside of me! Power! Infinite power!!
	 */
	public static final NpcStringId OINK_OINK_RAGE_IS_BOILING_UP_INSIDE_OF_ME_POWER_INFINITE_POWER;

	/**
	 * ID: 1800148<br>
	 * Message: Oink-oink! I'm really furious right now!
	 */
	public static final NpcStringId OINK_OINK_IM_REALLY_FURIOUS_RIGHT_NOW;

	/**
	 * ID: 1800149<br>
	 * Message: Squeak! Rage is boiling up inside of me! Power! Infinite power!!
	 */
	public static final NpcStringId SQUEAK_RAGE_IS_BOILING_UP_INSIDE_OF_ME_POWER_INFINITE_POWER;

	/**
	 * ID: 1800150<br>
	 * Message: Squeak! I'm really furious right now!!
	 */
	public static final NpcStringId SQUEAK_IM_REALLY_FURIOUS_RIGHT_NOW;

	/**
	 * ID: 1800162<br>
	 * Message: G Rank
	 */
	public static final NpcStringId G_RANK;

	/**
	 * ID: 1800163<br>
	 * Message: Huh... No one would have guessed that a doomed creature would be so powerful...
	 */
	public static final NpcStringId HUH_NO_ONE_WOULD_HAVE_GUESSED_THAT_A_DOOMED_CREATURE_WOULD_BE_SO_POWERFUL;

	/**
	 * ID: 1800164<br>
	 * Message: S-Grade
	 */
	public static final NpcStringId S_GRADE;

	/**
	 * ID: 1800165<br>
	 * Message: A-Grade
	 */
	public static final NpcStringId A_GRADE;

	/**
	 * ID: 1800166<br>
	 * Message: B-Grade
	 */
	public static final NpcStringId B_GRADE;

	/**
	 * ID: 1800167<br>
	 * Message: C-Grade
	 */
	public static final NpcStringId C_GRADE;

	/**
	 * ID: 1800168<br>
	 * Message: D-Grade
	 */
	public static final NpcStringId D_GRADE;

	/**
	 * ID: 1800169<br>
	 * Message: F-Grade
	 */
	public static final NpcStringId F_GRADE;

	/**
	 * ID: 1800170<br>
	 * Message: This is... This is a great achievement that is worthy of the true heroes of legend!
	 */
	public static final NpcStringId THIS_IS_THIS_IS_A_GREAT_ACHIEVEMENT_THAT_IS_WORTHY_OF_THE_TRUE_HEROES_OF_LEGEND;

	/**
	 * ID: 1800171<br>
	 * Message: Admirable. You greatly decreased the speed of invasion through Kamaloka.
	 */
	public static final NpcStringId ADMIRABLE_YOU_GREATLY_DECREASED_THE_SPEED_OF_INVASION_THROUGH_KAMALOKA;

	/**
	 * ID: 1800172<br>
	 * Message: Very good. Your skill makes you a model for other adventurers to follow.
	 */
	public static final NpcStringId VERY_GOOD_YOUR_SKILL_MAKES_YOU_A_MODEL_FOR_OTHER_ADVENTURERS_TO_FOLLOW;

	/**
	 * ID: 1800173<br>
	 * Message: Good work. If all adventurers produce results like you, we will slowly start to see the glimmer of hope.
	 */
	public static final NpcStringId
			GOOD_WORK_IF_ALL_ADVENTURERS_PRODUCE_RESULTS_LIKE_YOU_WE_WILL_SLOWLY_START_TO_SEE_THE_GLIMMER_OF_HOPE;

	/**
	 * ID: 1800174<br>
	 * Message: Unfortunately, it seems that Rim Kamaloka cannot be easily approached by everyone.
	 */
	public static final NpcStringId UNFORTUNATELY_IT_SEEMS_THAT_RIM_KAMALOKA_CANNOT_BE_EASILY_APPROACHED_BY_EVERYONE;

	/**
	 * ID: 1800175<br>
	 * Message: How disappointing. It looks like I made a mistake in sending you inside Rim Kamaloka.
	 */
	public static final NpcStringId HOW_DISAPPOINTING_IT_LOOKS_LIKE_I_MADE_A_MISTAKE_IN_SENDING_YOU_INSIDE_RIM_KAMALOKA;

	/**
	 * ID: 1800176<br>
	 * Message: Intruder alert. Intruder alert.
	 */
	public static final NpcStringId INTRUDER_ALERT_INTRUDER_ALERT;

	/**
	 * ID: 1800177<br>
	 * Message: What are you doing? Hurry up and help me!
	 */
	public static final NpcStringId WHAT_ARE_YOU_DOING_HURRY_UP_AND_HELP_ME;

	/**
	 * ID: 1800178<br>
	 * Message: I've had it up to here with you! I'll take care of you!
	 */
	public static final NpcStringId IVE_HAD_IT_UP_TO_HERE_WITH_YOU_ILL_TAKE_CARE_OF_YOU;

	/**
	 * ID: 1800179<br>
	 * Message: Ah... My mind is a wreck.
	 */
	public static final NpcStringId AH_MY_MIND_IS_A_WRECK;

	/**
	 * ID: 1800180<br>
	 * Message: If you thought that my subordinates would be so few, you are mistaken!
	 */
	public static final NpcStringId IF_YOU_THOUGHT_THAT_MY_SUBORDINATES_WOULD_BE_SO_FEW_YOU_ARE_MISTAKEN;

	/**
	 * ID: 1800181<br>
	 * Message: There's not much I can do, but I want to help you.
	 */
	public static final NpcStringId THERES_NOT_MUCH_I_CAN_DO_BUT_I_WANT_TO_HELP_YOU;

	/**
	 * ID: 1800182<br>
	 * Message: You $s1! Attack them!
	 */
	public static final NpcStringId YOU_S1_ATTACK_THEM;

	/**
	 * ID: 1800183<br>
	 * Message: Come out! My subordinate! I summon you to drive them out!
	 */
	public static final NpcStringId COME_OUT_MY_SUBORDINATE_I_SUMMON_YOU_TO_DRIVE_THEM_OUT;

	/**
	 * ID: 1800184<br>
	 * Message: There's not much I can do, but I will risk my life to help you!
	 */
	public static final NpcStringId THERES_NOT_MUCH_I_CAN_DO_BUT_I_WILL_RISK_MY_LIFE_TO_HELP_YOU;

	/**
	 * ID: 1800185<br>
	 * Message: Arg! The pain is more than I can stand!
	 */
	public static final NpcStringId ARG_THE_PAIN_IS_MORE_THAN_I_CAN_STAND;

	/**
	 * ID: 1800186<br>
	 * Message: Ahh! How did he find my weakness?
	 */
	public static final NpcStringId AHH_HOW_DID_HE_FIND_MY_WEAKNESS;

	/**
	 * ID: 1800187<br>
	 * Message: We were able to successfully collect the Essence of Kamaloka from the Kanabions that you defeated. Here they are.
	 */
	public static final NpcStringId
			WE_WERE_ABLE_TO_SUCCESSFULLY_COLLECT_THE_ESSENCE_OF_KAMALOKA_FROM_THE_KANABIONS_THAT_YOU_DEFEATED_HERE_THEY_ARE;

	/**
	 * ID: 1800188<br>
	 * Message: But we were able to collect somehow the Essence of Kamaloka from the Kanabions that you defeated. Here they are.
	 */
	public static final NpcStringId
			BUT_WE_WERE_ABLE_TO_COLLECT_SOMEHOW_THE_ESSENCE_OF_KAMALOKA_FROM_THE_KANABIONS_THAT_YOU_DEFEATED_HERE_THEY_ARE;

	/**
	 * ID: 1800189<br>
	 * Message: I'm sorry, but we were unable to collect the Essence of Kamaloka from the Kanabions that you defeated because their dark energy was too weak.
	 */
	public static final NpcStringId
			IM_SORRY_BUT_WE_WERE_UNABLE_TO_COLLECT_THE_ESSENCE_OF_KAMALOKA_FROM_THE_KANABIONS_THAT_YOU_DEFEATED_BECAUSE_THEIR_DARK_ENERGY_WAS_TOO_WEAK;

	/**
	 * ID: 1800190<br>
	 * Message: Rather than simply defeating the enemies, you seem to understand our goal and purpose as well.
	 */
	public static final NpcStringId
			RATHER_THAN_SIMPLY_DEFEATING_THE_ENEMIES_YOU_SEEM_TO_UNDERSTAND_OUR_GOAL_AND_PURPOSE_AS_WELL;

	/**
	 * ID: 1800191<br>
	 * Message: Dopplers and Voids possess an enhanced amount of the Kanabions' dark energy, so it is important to concentrate on defeating them when blocking the Kamalokians attack.
	 */
	public static final NpcStringId
			DOPPLERS_AND_VOIDS_POSSESS_AN_ENHANCED_AMOUNT_OF_THE_KANABIONS_DARK_ENERGY_SO_IT_IS_IMPORTANT_TO_CONCENTRATE_ON_DEFEATING_THEM_WHEN_BLOCKING_THE_KAMALOKIANS_ATTACK;

	/**
	 * ID: 1800192<br>
	 * Message: Have you seen Kanabions being remade as new Kanabions sometimes? You can see it occur more often by inflicting great damage during an attack or at the moment you defeat them.
	 */
	public static final NpcStringId
			HAVE_YOU_SEEN_KANABIONS_BEING_REMADE_AS_NEW_KANABIONS_SOMETIMES_YOU_CAN_SEE_IT_OCCUR_MORE_OFTEN_BY_INFLICTING_GREAT_DAMAGE_DURING_AN_ATTACK_OR_AT_THE_MOMENT_YOU_DEFEAT_THEM;

	/**
	 * ID: 1800193<br>
	 * Message: As in any other battle, it is critical to protect yourself while you are inside Rim Kamaloka. We do not want to attack recklessly.
	 */
	public static final NpcStringId
			AS_IN_ANY_OTHER_BATTLE_IT_IS_CRITICAL_TO_PROTECT_YOURSELF_WHILE_YOU_ARE_INSIDE_RIM_KAMALOKA_WE_DO_NOT_WANT_TO_ATTACK_RECKLESSLY;

	/**
	 * ID: 1800194<br>
	 * Message: We value developing an individual's overall power rather than a one-time victory. If you relied on another person's support this time, why don't you try to rely on your own strength next time?
	 */
	public static final NpcStringId
			WE_VALUE_DEVELOPING_AN_INDIVIDUALS_OVERALL_POWER_RATHER_THAN_A_ONE_TIME_VICTORY_IF_YOU_RELIED_ON_ANOTHER_PERSONS_SUPPORT_THIS_TIME_WHY_DONT_YOU_TRY_TO_RELY_ON_YOUR_OWN_STRENGTH_NEXT_TIME;

	/**
	 * ID: 1800195<br>
	 * Message: Are you sure that the battle just now was at the appropriate level? Bothering lower Kanabions, as if for mere entertainment, is considered to be a wasted battle for us.
	 */
	public static final NpcStringId
			ARE_YOU_SURE_THAT_THE_BATTLE_JUST_NOW_WAS_AT_THE_APPROPRIATE_LEVEL_BOTHERING_LOWER_KANABIONS_AS_IF_FOR_MERE_ENTERTAINMENT_IS_CONSIDERED_TO_BE_A_WASTED_BATTLE_FOR_US;

	/**
	 * ID: 1800196<br>
	 * Message: The greatest victory involves using all available resources, eliminating all of the enemy's opportunities, and bringing it to the fastest possible end. Don't you think so?
	 */
	public static final NpcStringId
			THE_GREATEST_VICTORY_INVOLVES_USING_ALL_AVAILABLE_RESOURCES_ELIMINATING_ALL_OF_THE_ENEMYS_OPPORTUNITIES_AND_BRINGING_IT_TO_THE_FASTEST_POSSIBLE_END_DONT_YOU_THINK_SO;

	/**
	 * ID: 1800197<br>
	 * Message: Emergency! Emergency! The outer wall is weakening rapidly!
	 */
	public static final NpcStringId EMERGENCY_EMERGENCY_THE_OUTER_WALL_IS_WEAKENING_RAPIDLY;

	/**
	 * ID: 1800198<br>
	 * Message: The remaining strength of the outer wall is $s1!
	 */
	public static final NpcStringId THE_REMAINING_STRENGTH_OF_THE_OUTER_WALL_IS_S1;

	/**
	 * ID: 1800199<br>
	 * Message: Pathfinder Savior
	 */
	public static final NpcStringId PATHFINDER_SAVIOR;

	/**
	 * ID: 1800200<br>
	 * Message: Pathfinder Supporter
	 */
	public static final NpcStringId PATHFINDER_SUPPORTER;

	/**
	 * ID: 1800201<br>
	 * Message: Some Kanabions who haven't fully adjusted yet to their new physical form are known to exhibit symptoms of an extremely weakened body structure sometimes. If you attack them at that moment, you will have great results.
	 */
	public static final NpcStringId
			SOME_KANABIONS_WHO_HAVENT_FULLY_ADJUSTED_YET_TO_THEIR_NEW_PHYSICAL_FORM_ARE_KNOWN_TO_EXHIBIT_SYMPTOMS_OF_AN_EXTREMELY_WEAKENED_BODY_STRUCTURE_SOMETIMES_IF_YOU_ATTACK_THEM_AT_THAT_MOMENT_YOU_WILL_HAVE_GREAT_RESULTS;

	/**
	 * ID: 1800202<br>
	 * Message: Have you ever heard of $s1? They say it's a genuine $s2!
	 */
	public static final NpcStringId HAVE_YOU_EVER_HEARD_OF_S1_THEY_SAY_ITS_A_GENUINE_S2;

	/**
	 * ID: 1800203<br>
	 * Message: There are 5 minutes remaining to register for Kratei's cube match.
	 */
	public static final NpcStringId THERE_ARE_5_MINUTES_REMAINING_TO_REGISTER_FOR_KRATEIS_CUBE_MATCH;

	/**
	 * ID: 1800204<br>
	 * Message: There are 3 minutes remaining to register for Kratei's cube match.
	 */
	public static final NpcStringId THERE_ARE_3_MINUTES_REMAINING_TO_REGISTER_FOR_KRATEIS_CUBE_MATCH;

	/**
	 * ID: 1800205<br>
	 * Message: There are 1 minutes remaining to register for Kratei's cube match.
	 */
	public static final NpcStringId THERE_ARE_1_MINUTES_REMAINING_TO_REGISTER_FOR_KRATEIS_CUBE_MATCH;

	/**
	 * ID: 1800207<br>
	 * Message: The match will begin shortly.
	 */
	public static final NpcStringId THE_MATCH_WILL_BEGIN_SHORTLY;

	/**
	 * ID: 1800209<br>
	 * Message: Ohh...oh...oh...
	 */
	public static final NpcStringId OHHOHOH;

	/**
	 * ID: 1800210<br>
	 * Message: Fire
	 */
	public static final NpcStringId FIRE;

	/**
	 * ID: 1800211<br>
	 * Message: Water
	 */
	public static final NpcStringId WATER;

	/**
	 * ID: 1800212<br>
	 * Message: Wind
	 */
	public static final NpcStringId WIND;

	/**
	 * ID: 1800213<br>
	 * Message: Earth
	 */
	public static final NpcStringId EARTH;

	/**
	 * ID: 1800214<br>
	 * Message: ...It's $s1...
	 */
	public static final NpcStringId ITS_S1;

	/**
	 * ID: 1800215<br>
	 * Message: ...$s1 is strong...
	 */
	public static final NpcStringId S1_IS_STRONG;

	/**
	 * ID: 1800216<br>
	 * Message: ...It's always $s1...
	 */
	public static final NpcStringId ITS_ALWAYS_S1;

	/**
	 * ID: 1800217<br>
	 * Message: ...$s1 won't do...
	 */
	public static final NpcStringId S1_WONT_DO;

	/**
	 * ID: 1800218<br>
	 * Message: You will be cursed for seeking the treasure!
	 */
	public static final NpcStringId YOU_WILL_BE_CURSED_FOR_SEEKING_THE_TREASURE;

	/**
	 * ID: 1800219<br>
	 * Message: The airship has been summoned. It will automatically depart in 5 minutes.
	 */
	public static final NpcStringId THE_AIRSHIP_HAS_BEEN_SUMMONED_IT_WILL_AUTOMATICALLY_DEPART_IN_5_MINUTES;

	/**
	 * ID: 1800220<br>
	 * Message: The regularly scheduled airship has arrived. It will depart for the Aden continent in 1 minute.
	 */
	public static final NpcStringId
			THE_REGULARLY_SCHEDULED_AIRSHIP_HAS_ARRIVED_IT_WILL_DEPART_FOR_THE_ADEN_CONTINENT_IN_1_MINUTE;

	/**
	 * ID: 1800221<br>
	 * Message: The regularly scheduled airship that flies to the Aden continent has departed.
	 */
	public static final NpcStringId THE_REGULARLY_SCHEDULED_AIRSHIP_THAT_FLIES_TO_THE_ADEN_CONTINENT_HAS_DEPARTED;

	/**
	 * ID: 1800222<br>
	 * Message: The regularly scheduled airship has arrived. It will depart for the Gracia continent in 1 minute.
	 */
	public static final NpcStringId
			THE_REGULARLY_SCHEDULED_AIRSHIP_HAS_ARRIVED_IT_WILL_DEPART_FOR_THE_GRACIA_CONTINENT_IN_1_MINUTE;

	/**
	 * ID: 1800223<br>
	 * Message: The regularly scheduled airship that flies to the Gracia continent has departed.
	 */
	public static final NpcStringId THE_REGULARLY_SCHEDULED_AIRSHIP_THAT_FLIES_TO_THE_GRACIA_CONTINENT_HAS_DEPARTED;

	/**
	 * ID: 1800224<br>
	 * Message: Another airship has been summoned to the wharf. Please try again later.
	 */
	public static final NpcStringId ANOTHER_AIRSHIP_HAS_BEEN_SUMMONED_TO_THE_WHARF_PLEASE_TRY_AGAIN_LATER;

	/**
	 * ID: 1800225<br>
	 * Message: Huh? The sky looks funny. What's that?
	 */
	public static final NpcStringId HUH_THE_SKY_LOOKS_FUNNY_WHATS_THAT;

	/**
	 * ID: 1800226<br>
	 * Message: A powerful subordinate is being held by the Barrier Orb! This reaction means...!
	 */
	public static final NpcStringId A_POWERFUL_SUBORDINATE_IS_BEING_HELD_BY_THE_BARRIER_ORB_THIS_REACTION_MEANS;

	/**
	 * ID: 1800227<br>
	 * Message: Be careful...! Something's coming...!
	 */
	public static final NpcStringId BE_CAREFUL_SOMETHINGS_COMING;

	/**
	 * ID: 1800228<br>
	 * Message: You must first found a clan or belong to one.
	 */
	public static final NpcStringId YOU_MUST_FIRST_FOUND_A_CLAN_OR_BELONG_TO_ONE;

	/**
	 * ID: 1800229<br>
	 * Message: There is no party currently challenging Ekimus. \\n If no party enters within $s1 seconds, the attack on the Heart of Immortality will fail...
	 */
	public static final NpcStringId
			THERE_IS_NO_PARTY_CURRENTLY_CHALLENGING_EKIMUS_N_IF_NO_PARTY_ENTERS_WITHIN_S1_SECONDS_THE_ATTACK_ON_THE_HEART_OF_IMMORTALITY_WILL_FAIL;

	/**
	 * ID: 1800230<br>
	 * Message: Ekimus has gained strength from a tumor...
	 */
	public static final NpcStringId EKIMUS_HAS_GAINED_STRENGTH_FROM_A_TUMOR;

	/**
	 * ID: 1800231<br>
	 * Message: Ekimus has been weakened by losing strength from a tumor!
	 */
	public static final NpcStringId EKIMUS_HAS_BEEN_WEAKENED_BY_LOSING_STRENGTH_FROM_A_TUMOR;

	/**
	 * ID: 1800233<br>
	 * Message: C'mon, c'mon! Show your face, you little rats! Let me see what the doomed weaklings are scheming!
	 */
	public static final NpcStringId
			CMON_CMON_SHOW_YOUR_FACE_YOU_LITTLE_RATS_LET_ME_SEE_WHAT_THE_DOOMED_WEAKLINGS_ARE_SCHEMING;

	/**
	 * ID: 1800234<br>
	 * Message: Impressive.... Hahaha it's so much fun, but I need to chill a little while.  Argekunte, clear the way!
	 */
	public static final NpcStringId
			IMPRESSIVE_HAHAHA_ITS_SO_MUCH_FUN_BUT_I_NEED_TO_CHILL_A_LITTLE_WHILE_ARGEKUNTE_CLEAR_THE_WAY;

	/**
	 * ID: 1800235<br>
	 * Message: Kyahaha! Since the tumor has been resurrected, I no longer need to waste my time on you!
	 */
	public static final NpcStringId
			KYAHAHA_SINCE_THE_TUMOR_HAS_BEEN_RESURRECTED_I_NO_LONGER_NEED_TO_WASTE_MY_TIME_ON_YOU;

	/**
	 * ID: 1800236<br>
	 * Message: Keu... I will leave for now... But don't think this is over... The Seed of Infinity can never die...
	 */
	public static final NpcStringId
			KEU_I_WILL_LEAVE_FOR_NOW_BUT_DONT_THINK_THIS_IS_OVER_THE_SEED_OF_INFINITY_CAN_NEVER_DIE;

	/**
	 * ID: 1800237<br>
	 * Message: Kahahaha! That guy's nothing! He can't even kill without my permission! See here! Ultimate forgotten magic! Deathless Guardian!
	 */
	public static final NpcStringId
			KAHAHAHA_THAT_GUYS_NOTHING_HE_CANT_EVEN_KILL_WITHOUT_MY_PERMISSION_SEE_HERE_ULTIMATE_FORGOTTEN_MAGIC_DEATHLESS_GUARDIAN;

	/**
	 * ID: 1800238<br>
	 * Message: I curse the day that I became your slave in order to escape death, Cohemenes! I swear that I shall see you die with my own eyes!
	 */
	public static final NpcStringId
			I_CURSE_THE_DAY_THAT_I_BECAME_YOUR_SLAVE_IN_ORDER_TO_ESCAPE_DEATH_COHEMENES_I_SWEAR_THAT_I_SHALL_SEE_YOU_DIE_WITH_MY_OWN_EYES;

	/**
	 * ID: 1800239<br>
	 * Message: My enemy is dying, and my blood is boiling! What cruel curse is this!
	 */
	public static final NpcStringId MY_ENEMY_IS_DYING_AND_MY_BLOOD_IS_BOILING_WHAT_CRUEL_CURSE_IS_THIS;

	/**
	 * ID: 1800240<br>
	 * Message: Hall of Suffering
	 */
	public static final NpcStringId HALL_OF_SUFFERING;

	/**
	 * ID: 1800241<br>
	 * Message: Hall of Erosion
	 */
	public static final NpcStringId HALL_OF_EROSION;

	/**
	 * ID: 1800242<br>
	 * Message: Heart of Immortality
	 */
	public static final NpcStringId HEART_OF_IMMORTALITY;

	/**
	 * ID: 1800243<br>
	 * Message: Attack
	 */
	public static final NpcStringId ATTACK;

	/**
	 * ID: 1800244<br>
	 * Message: Defend
	 */
	public static final NpcStringId DEFEND;

	/**
	 * ID: 1800245<br>
	 * Message: Congratulations! You have succeeded at $s1 $s2! The instance will shortly expire.
	 */
	public static final NpcStringId CONGRATULATIONS_YOU_HAVE_SUCCEEDED_AT_S1_S2_THE_INSTANCE_WILL_SHORTLY_EXPIRE;

	/**
	 * ID: 1800246<br>
	 * Message: You have failed at $s1 $s2... The instance will shortly expire.
	 */
	public static final NpcStringId YOU_HAVE_FAILED_AT_S1_S2_THE_INSTANCE_WILL_SHORTLY_EXPIRE;

	/**
	 * ID: 1800247<br>
	 * Message: $s1's party has moved to a different location through the crack in the tumor!
	 */
	public static final NpcStringId S1S_PARTY_HAS_MOVED_TO_A_DIFFERENT_LOCATION_THROUGH_THE_CRACK_IN_THE_TUMOR;

	/**
	 * ID: 1800248<br>
	 * Message: $s1's party has entered the Chamber of Ekimus through the crack in the tumor!
	 */
	public static final NpcStringId S1S_PARTY_HAS_ENTERED_THE_CHAMBER_OF_EKIMUS_THROUGH_THE_CRACK_IN_THE_TUMOR;

	/**
	 * ID: 1800249<br>
	 * Message: Ekimus has sensed abnormal activity. \\nThe advancing party is forcefully expelled!
	 */
	public static final NpcStringId EKIMUS_HAS_SENSED_ABNORMAL_ACTIVITY_NTHE_ADVANCING_PARTY_IS_FORCEFULLY_EXPELLED;

	/**
	 * ID: 1800250<br>
	 * Message: There aren't enough items. In order to summon the airship, you need 5 Energy Star Stones.
	 */
	public static final NpcStringId
			THERE_ARENT_ENOUGH_ITEMS_IN_ORDER_TO_SUMMON_THE_AIRSHIP_YOU_NEED_5_ENERGY_STAR_STONES;

	/**
	 * ID: 1800251<br>
	 * Message: The Soul Devourers who are greedy to eat the Seeds of Life that remain alive until the end have awakened...!
	 */
	public static final NpcStringId
			THE_SOUL_DEVOURERS_WHO_ARE_GREEDY_TO_EAT_THE_SEEDS_OF_LIFE_THAT_REMAIN_ALIVE_UNTIL_THE_END_HAVE_AWAKENED;

	/**
	 * ID: 1800252<br>
	 * Message: The first Feral Hound of the Netherworld has awakened!
	 */
	public static final NpcStringId THE_FIRST_FERAL_HOUND_OF_THE_NETHERWORLD_HAS_AWAKENED;

	/**
	 * ID: 1800253<br>
	 * Message: The second Feral Hound of the Netherworld has awakened!
	 */
	public static final NpcStringId THE_SECOND_FERAL_HOUND_OF_THE_NETHERWORLD_HAS_AWAKENED;

	/**
	 * ID: 1800254<br>
	 * Message: Clinging on won't help you! Ultimate forgotten magic, Blade Turn!
	 */
	public static final NpcStringId CLINGING_ON_WONT_HELP_YOU_ULTIMATE_FORGOTTEN_MAGIC_BLADE_TURN;

	/**
	 * ID: 1800255<br>
	 * Message: Even special sauce can't help you! Ultimate forgotten magic, Force Shield!
	 */
	public static final NpcStringId EVEN_SPECIAL_SAUCE_CANT_HELP_YOU_ULTIMATE_FORGOTTEN_MAGIC_FORCE_SHIELD;

	/**
	 * ID: 1800256<br>
	 * Message: You little doomed maggots! Even if you keep swarming, the power of Immortality will only grow stronger!
	 */
	public static final NpcStringId
			YOU_LITTLE_DOOMED_MAGGOTS_EVEN_IF_YOU_KEEP_SWARMING_THE_POWER_OF_IMMORTALITY_WILL_ONLY_GROW_STRONGER;

	/**
	 * ID: 1800257<br>
	 * Message: The Airship Summon License has been awarded. Your clan can now summon an airship.
	 */
	public static final NpcStringId THE_AIRSHIP_SUMMON_LICENSE_HAS_BEEN_AWARDED_YOUR_CLAN_CAN_NOW_SUMMON_AN_AIRSHIP;

	/**
	 * ID: 1800258<br>
	 * Message: The Gracia treasure box has appeared!
	 */
	public static final NpcStringId THE_GRACIA_TREASURE_BOX_HAS_APPEARED;

	/**
	 * ID: 1800259<br>
	 * Message: The Gracia treasure box will soon disappear!
	 */
	public static final NpcStringId THE_GRACIA_TREASURE_BOX_WILL_SOON_DISAPPEAR;

	/**
	 * ID: 1800260<br>
	 * Message: You have been cursed by the tumor and have incurred $s1 damage.
	 */
	public static final NpcStringId YOU_HAVE_BEEN_CURSED_BY_THE_TUMOR_AND_HAVE_INCURRED_S1_DAMAGE;

	/**
	 * ID: 1800261<br>
	 * Message: I shall accept your challenge, $s1! Come and die in the arms of immortality!
	 */
	public static final NpcStringId I_SHALL_ACCEPT_YOUR_CHALLENGE_S1_COME_AND_DIE_IN_THE_ARMS_OF_IMMORTALITY;

	/**
	 * ID: 1800262<br>
	 * Message: You will participate in $s1 $s2 shortly. Be prepared for anything.
	 */
	public static final NpcStringId YOU_WILL_PARTICIPATE_IN_S1_S2_SHORTLY_BE_PREPARED_FOR_ANYTHING;

	/**
	 * ID: 1800263<br>
	 * Message: You can hear the undead of Ekimus rushing toward you. $s1 $s2, it has now begun!
	 */
	public static final NpcStringId YOU_CAN_HEAR_THE_UNDEAD_OF_EKIMUS_RUSHING_TOWARD_YOU_S1_S2_IT_HAS_NOW_BEGUN;

	/**
	 * ID: 1800264<br>
	 * Message: You can feel the surging energy of death from the tumor.
	 */
	public static final NpcStringId YOU_CAN_FEEL_THE_SURGING_ENERGY_OF_DEATH_FROM_THE_TUMOR;

	/**
	 * ID: 1800265<br>
	 * Message: The area near the tumor is full of ominous energy.
	 */
	public static final NpcStringId THE_AREA_NEAR_THE_TUMOR_IS_FULL_OF_OMINOUS_ENERGY;

	/**
	 * ID: 1800266<br>
	 * Message: You tried to drop us. How stupid!
	 */
	public static final NpcStringId YOU_TRIED_TO_DROP_US_HOW_STUPID;

	/**
	 * ID: 1800267<br>
	 * Message: We are blood brethren. I can't fall so easily here and leave my brother behind.
	 */
	public static final NpcStringId WE_ARE_BLOOD_BRETHREN_I_CANT_FALL_SO_EASILY_HERE_AND_LEAVE_MY_BROTHER_BEHIND;

	/**
	 * ID: 1800268<br>
	 * Message: You were always what I aspired to be. Do you think I would fall so easily here when I have a brother like that?
	 */
	public static final NpcStringId
			YOU_WERE_ALWAYS_WHAT_I_ASPIRED_TO_BE_DO_YOU_THINK_I_WOULD_FALL_SO_EASILY_HERE_WHEN_I_HAVE_A_BROTHER_LIKE_THAT;

	/**
	 * ID: 1800269<br>
	 * Message: With all connections to the tumor severed, Ekimus has lost its power to control the Feral Hound!
	 */
	public static final NpcStringId
			WITH_ALL_CONNECTIONS_TO_THE_TUMOR_SEVERED_EKIMUS_HAS_LOST_ITS_POWER_TO_CONTROL_THE_FERAL_HOUND;

	/**
	 * ID: 1800270<br>
	 * Message: With the connection to the tumor restored, Ekimus has regained control over the Feral Hound...
	 */
	public static final NpcStringId
			WITH_THE_CONNECTION_TO_THE_TUMOR_RESTORED_EKIMUS_HAS_REGAINED_CONTROL_OVER_THE_FERAL_HOUND;

	/**
	 * ID: 1800271<br>
	 * Message: Woooong!
	 */
	public static final NpcStringId WOOOONG;

	/**
	 * ID: 1800272<br>
	 * Message: Woong... Woong... Woo...
	 */
	public static final NpcStringId WOONG_WOONG_WOO;

	/**
	 * ID: 1800273<br>
	 * Message: The enemies have attacked. Everyone come out and fight!!!! ... Urgh~!
	 */
	public static final NpcStringId THE_ENEMIES_HAVE_ATTACKED_EVERYONE_COME_OUT_AND_FIGHT_URGH;

	/**
	 * ID: 1800274<br>
	 * Message: The tumor inside $s1 has been destroyed! \\nIn order to draw out the cowardly Cohemenes, you must destroy all the tumors!
	 */
	public static final NpcStringId
			THE_TUMOR_INSIDE_S1_HAS_BEEN_DESTROYED_NIN_ORDER_TO_DRAW_OUT_THE_COWARDLY_COHEMENES_YOU_MUST_DESTROY_ALL_THE_TUMORS;

	/**
	 * ID: 1800275<br>
	 * Message: The tumor inside $s1 has completely revived. \\nThe restrengthened Cohemenes has fled deeper inside the seed...
	 */
	public static final NpcStringId
			THE_TUMOR_INSIDE_S1_HAS_COMPLETELY_REVIVED_NTHE_RESTRENGTHENED_COHEMENES_HAS_FLED_DEEPER_INSIDE_THE_SEED;

	/**
	 * ID: 1800276<br>
	 * Message: The awarded Airship Summon License has been received.
	 */
	public static final NpcStringId THE_AWARDED_AIRSHIP_SUMMON_LICENSE_HAS_BEEN_RECEIVED;

	/**
	 * ID: 1800277<br>
	 * Message: You do not currently have an Airship Summon License. You can earn your Airship Summon License through Engineer Lekon.
	 */
	public static final NpcStringId
			YOU_DO_NOT_CURRENTLY_HAVE_AN_AIRSHIP_SUMMON_LICENSE_YOU_CAN_EARN_YOUR_AIRSHIP_SUMMON_LICENSE_THROUGH_ENGINEER_LEKON;

	/**
	 * ID: 1800278<br>
	 * Message: The Airship Summon License has already been awarded.
	 */
	public static final NpcStringId THE_AIRSHIP_SUMMON_LICENSE_HAS_ALREADY_BEEN_AWARDED;

	/**
	 * ID: 1800279<br>
	 * Message: If you have items, please give them to me.
	 */
	public static final NpcStringId IF_YOU_HAVE_ITEMS_PLEASE_GIVE_THEM_TO_ME;

	/**
	 * ID: 1800280<br>
	 * Message: My stomach is empty.
	 */
	public static final NpcStringId MY_STOMACH_IS_EMPTY;

	/**
	 * ID: 1800281<br>
	 * Message: I'm hungry, I'm hungry!
	 */
	public static final NpcStringId IM_HUNGRY_IM_HUNGRY;

	/**
	 * ID: 1800282<br>
	 * Message: I'm still not full...
	 */
	public static final NpcStringId IM_STILL_NOT_FULL;

	/**
	 * ID: 1800283<br>
	 * Message: I'm still hungry~
	 */
	public static final NpcStringId IM_STILL_HUNGRY;

	/**
	 * ID: 1800284<br>
	 * Message: I feel a little woozy...
	 */
	public static final NpcStringId I_FEEL_A_LITTLE_WOOZY;

	/**
	 * ID: 1800285<br>
	 * Message: Give me something to eat.
	 */
	public static final NpcStringId GIVE_ME_SOMETHING_TO_EAT;

	/**
	 * ID: 1800286<br>
	 * Message: Now it's time to eat~
	 */
	public static final NpcStringId NOW_ITS_TIME_TO_EAT;

	/**
	 * ID: 1800287<br>
	 * Message: I also need a dessert.
	 */
	public static final NpcStringId I_ALSO_NEED_A_DESSERT;

	/**
	 * ID: 1800289<br>
	 * Message: I'm full now, I don't want to eat anymore.
	 */
	public static final NpcStringId IM_FULL_NOW_I_DONT_WANT_TO_EAT_ANYMORE;

	/**
	 * ID: 1800290<br>
	 * Message: I haven't eaten anything, I'm so weak~
	 */
	public static final NpcStringId I_HAVENT_EATEN_ANYTHING_IM_SO_WEAK;

	/**
	 * ID: 1800291<br>
	 * Message: Yum-yum, yum-yum
	 */
	public static final NpcStringId YUM_YUM_YUM_YUM;

	/**
	 * ID: 1800292<br>
	 * Message: You've sustained $s1 damage as Tumor's shell started melting after touching the sacred seal on the shield!
	 */
	public static final NpcStringId
			YOUVE_SUSTAINED_S1_DAMAGE_AS_TUMORS_SHELL_STARTED_MELTING_AFTER_TOUCHING_THE_SACRED_SEAL_ON_THE_SHIELD;

	/**
	 * ID: 1800293<br>
	 * Message: You've sustained $s1 damage as Soul Coffin's shell started melting after touching the sacred seal on the shield!
	 */
	public static final NpcStringId
			YOUVE_SUSTAINED_S1_DAMAGE_AS_SOUL_COFFINS_SHELL_STARTED_MELTING_AFTER_TOUCHING_THE_SACRED_SEAL_ON_THE_SHIELD;

	/**
	 * ID: 1800295<br>
	 * Message: Obelisk has collapsed. Don't let the enemies jump around wildly anymore!!!!
	 */
	public static final NpcStringId OBELISK_HAS_COLLAPSED_DONT_LET_THE_ENEMIES_JUMP_AROUND_WILDLY_ANYMORE;

	/**
	 * ID: 1800296<br>
	 * Message: Enemies are trying to destroy the fortress. Everyone defend the fortress!!!!
	 */
	public static final NpcStringId ENEMIES_ARE_TRYING_TO_DESTROY_THE_FORTRESS_EVERYONE_DEFEND_THE_FORTRESS;

	/**
	 * ID: 1800297<br>
	 * Message: Come out, warriors. Protect Seed of Destruction.
	 */
	public static final NpcStringId COME_OUT_WARRIORS_PROTECT_SEED_OF_DESTRUCTION;

	/**
	 * ID: 1800298<br>
	 * Message: The undead of Ekimus is attacking Seed of Life. Defending Hall of Erosion will fail even if one Seed of Life is destroyed...
	 */
	public static final NpcStringId
			THE_UNDEAD_OF_EKIMUS_IS_ATTACKING_SEED_OF_LIFE_DEFENDING_HALL_OF_EROSION_WILL_FAIL_EVEN_IF_ONE_SEED_OF_LIFE_IS_DESTROYED;

	/**
	 * ID: 1800299<br>
	 * Message: All the tumors inside $s1 have been destroyed! Driven into a corner, Cohemenes appears close by!
	 */
	public static final NpcStringId
			ALL_THE_TUMORS_INSIDE_S1_HAVE_BEEN_DESTROYED_DRIVEN_INTO_A_CORNER_COHEMENES_APPEARS_CLOSE_BY;

	/**
	 * ID: 1800300<br>
	 * Message: The tumor inside $s1 has been destroyed! \\nThe nearby Undead that were attacking Seed of Life start losing their energy and run away!
	 */
	public static final NpcStringId
			THE_TUMOR_INSIDE_S1_HAS_BEEN_DESTROYED_NTHE_NEARBY_UNDEAD_THAT_WERE_ATTACKING_SEED_OF_LIFE_START_LOSING_THEIR_ENERGY_AND_RUN_AWAY;

	/**
	 * ID: 1800301<br>
	 * Message: The tumor inside $s1 has completely revived. \\nRecovered nearby Undead are swarming toward Seed of Life...
	 */
	public static final NpcStringId
			THE_TUMOR_INSIDE_S1_HAS_COMPLETELY_REVIVED_NRECOVERED_NEARBY_UNDEAD_ARE_SWARMING_TOWARD_SEED_OF_LIFE;

	/**
	 * ID: 1800302<br>
	 * Message: The tumor inside $s1 that has provided energy \\n to Ekimus is destroyed!
	 */
	public static final NpcStringId THE_TUMOR_INSIDE_S1_THAT_HAS_PROVIDED_ENERGY_N_TO_EKIMUS_IS_DESTROYED;

	/**
	 * ID: 1800303<br>
	 * Message: The tumor inside $s1 has been completely resurrected \\n and started to energize Ekimus again...
	 */
	public static final NpcStringId
			THE_TUMOR_INSIDE_S1_HAS_BEEN_COMPLETELY_RESURRECTED_N_AND_STARTED_TO_ENERGIZE_EKIMUS_AGAIN;

	/**
	 * ID: 1800304<br>
	 * Message: The tumor inside $s1 has been destroyed! \\nThe speed that Ekimus calls out his prey has slowed down!
	 */
	public static final NpcStringId
			THE_TUMOR_INSIDE_S1_HAS_BEEN_DESTROYED_NTHE_SPEED_THAT_EKIMUS_CALLS_OUT_HIS_PREY_HAS_SLOWED_DOWN;

	/**
	 * ID: 1800305<br>
	 * Message: The tumor inside $s1 has completely revived. \\nEkimus started to regain his energy and is desperately looking for his prey...
	 */
	public static final NpcStringId
			THE_TUMOR_INSIDE_S1_HAS_COMPLETELY_REVIVED_NEKIMUS_STARTED_TO_REGAIN_HIS_ENERGY_AND_IS_DESPERATELY_LOOKING_FOR_HIS_PREY;

	/**
	 * ID: 1800306<br>
	 * Message: Bring more, more souls...!
	 */
	public static final NpcStringId BRING_MORE_MORE_SOULS;

	/**
	 * ID: 1800307<br>
	 * Message: The Hall of Erosion attack will fail unless you make a quick attack against the tumor!
	 */
	public static final NpcStringId
			THE_HALL_OF_EROSION_ATTACK_WILL_FAIL_UNLESS_YOU_MAKE_A_QUICK_ATTACK_AGAINST_THE_TUMOR;

	/**
	 * ID: 1800308<br>
	 * Message: As the tumor was not threatened, Cohemenes completely ran away to deep inside the Seed...
	 */
	public static final NpcStringId
			AS_THE_TUMOR_WAS_NOT_THREATENED_COHEMENES_COMPLETELY_RAN_AWAY_TO_DEEP_INSIDE_THE_SEED;

	/**
	 * ID: 1800309<br>
	 * Message: Your goal will be obstructed or be under a restraint.
	 */
	public static final NpcStringId YOUR_GOAL_WILL_BE_OBSTRUCTED_OR_BE_UNDER_A_RESTRAINT;

	/**
	 * ID: 1800310<br>
	 * Message: You may face an unforeseen problem on your course toward the goal.
	 */
	public static final NpcStringId YOU_MAY_FACE_AN_UNFORESEEN_PROBLEM_ON_YOUR_COURSE_TOWARD_THE_GOAL;

	/**
	 * ID: 1800311<br>
	 * Message: You may feel nervous and anxious because of unfavorable situations.
	 */
	public static final NpcStringId YOU_MAY_FEEL_NERVOUS_AND_ANXIOUS_BECAUSE_OF_UNFAVORABLE_SITUATIONS;

	/**
	 * ID: 1800312<br>
	 * Message: Be warned when the situation is difficult because you may lose your judgment and make an irrational mistake.
	 */
	public static final NpcStringId
			BE_WARNED_WHEN_THE_SITUATION_IS_DIFFICULT_BECAUSE_YOU_MAY_LOSE_YOUR_JUDGMENT_AND_MAKE_AN_IRRATIONAL_MISTAKE;

	/**
	 * ID: 1800313<br>
	 * Message: You may meet a trustworthy person or a good opportunity.
	 */
	public static final NpcStringId YOU_MAY_MEET_A_TRUSTWORTHY_PERSON_OR_A_GOOD_OPPORTUNITY;

	/**
	 * ID: 1800314<br>
	 * Message: Your downward life starts taking an upturn.
	 */
	public static final NpcStringId YOUR_DOWNWARD_LIFE_STARTS_TAKING_AN_UPTURN;

	/**
	 * ID: 1800315<br>
	 * Message: You will attract attention from people with your popularity.
	 */
	public static final NpcStringId YOU_WILL_ATTRACT_ATTENTION_FROM_PEOPLE_WITH_YOUR_POPULARITY;

	/**
	 * ID: 1800316<br>
	 * Message: Your star of fortune says there'll be fish snapping at your bait.
	 */
	public static final NpcStringId YOUR_STAR_OF_FORTUNE_SAYS_THERELL_BE_FISH_SNAPPING_AT_YOUR_BAIT;

	/**
	 * ID: 1800317<br>
	 * Message: There may be a conflict born of your dogmatic procedures.
	 */
	public static final NpcStringId THERE_MAY_BE_A_CONFLICT_BORN_OF_YOUR_DOGMATIC_PROCEDURES;

	/**
	 * ID: 1800318<br>
	 * Message: Your wisdom and creativity may lead you to success with your goal.
	 */
	public static final NpcStringId YOUR_WISDOM_AND_CREATIVITY_MAY_LEAD_YOU_TO_SUCCESS_WITH_YOUR_GOAL;

	/**
	 * ID: 1800319<br>
	 * Message: You may accomplish your goals if you diligently pursue them without giving up.
	 */
	public static final NpcStringId YOU_MAY_ACCOMPLISH_YOUR_GOALS_IF_YOU_DILIGENTLY_PURSUE_THEM_WITHOUT_GIVING_UP;

	/**
	 * ID: 1800320<br>
	 * Message: You may get help if you go through the front door without seeking tricks or maneuvers.
	 */
	public static final NpcStringId
			YOU_MAY_GET_HELP_IF_YOU_GO_THROUGH_THE_FRONT_DOOR_WITHOUT_SEEKING_TRICKS_OR_MANEUVERS;

	/**
	 * ID: 1800321<br>
	 * Message: A good result is on the way if you set a goal and bravely proceed toward it.
	 */
	public static final NpcStringId A_GOOD_RESULT_IS_ON_THE_WAY_IF_YOU_SET_A_GOAL_AND_BRAVELY_PROCEED_TOWARD_IT;

	/**
	 * ID: 1800322<br>
	 * Message: Everything will be smoothly managed no matter how difficult.
	 */
	public static final NpcStringId EVERYTHING_WILL_BE_SMOOTHLY_MANAGED_NO_MATTER_HOW_DIFFICULT;

	/**
	 * ID: 1800323<br>
	 * Message: Be firm and carefully scrutinize circumstances even when things are difficult.
	 */
	public static final NpcStringId BE_FIRM_AND_CAREFULLY_SCRUTINIZE_CIRCUMSTANCES_EVEN_WHEN_THINGS_ARE_DIFFICULT;

	/**
	 * ID: 1800324<br>
	 * Message: Always think over to find neglected problems you haven't taken care of yet.
	 */
	public static final NpcStringId ALWAYS_THINK_OVER_TO_FIND_NEGLECTED_PROBLEMS_YOU_HAVENT_TAKEN_CARE_OF_YET;

	/**
	 * ID: 1800325<br>
	 * Message: Financial fortune will greet your poor life.
	 */
	public static final NpcStringId FINANCIAL_FORTUNE_WILL_GREET_YOUR_POOR_LIFE;

	/**
	 * ID: 1800326<br>
	 * Message: You may acquire wealth and fame after unlucky circumstances.
	 */
	public static final NpcStringId YOU_MAY_ACQUIRE_WEALTH_AND_FAME_AFTER_UNLUCKY_CIRCUMSTANCES;

	/**
	 * ID: 1800327<br>
	 * Message: The difficult situations will turn to hope with unforeseen help.
	 */
	public static final NpcStringId THE_DIFFICULT_SITUATIONS_WILL_TURN_TO_HOPE_WITH_UNFORESEEN_HELP;

	/**
	 * ID: 1800328<br>
	 * Message: A great task will result in success.
	 */
	public static final NpcStringId A_GREAT_TASK_WILL_RESULT_IN_SUCCESS;

	/**
	 * ID: 1800329<br>
	 * Message: You may encounter a precious person who will lift your loneliness and discord.
	 */
	public static final NpcStringId YOU_MAY_ENCOUNTER_A_PRECIOUS_PERSON_WHO_WILL_LIFT_YOUR_LONELINESS_AND_DISCORD;

	/**
	 * ID: 1800330<br>
	 * Message: People around you will encourage your every task in the future.
	 */
	public static final NpcStringId PEOPLE_AROUND_YOU_WILL_ENCOURAGE_YOUR_EVERY_TASK_IN_THE_FUTURE;

	/**
	 * ID: 1800331<br>
	 * Message: Everything will be smoothly managed.
	 */
	public static final NpcStringId EVERYTHING_WILL_BE_SMOOTHLY_MANAGED;

	/**
	 * ID: 1800332<br>
	 * Message: You will meet a person who can cherish your values if you maintain good ties with people.
	 */
	public static final NpcStringId
			YOU_WILL_MEET_A_PERSON_WHO_CAN_CHERISH_YOUR_VALUES_IF_YOU_MAINTAIN_GOOD_TIES_WITH_PEOPLE;

	/**
	 * ID: 1800333<br>
	 * Message: Maintain cooperative attitude since you will meet someone to be of help.
	 */
	public static final NpcStringId MAINTAIN_COOPERATIVE_ATTITUDE_SINCE_YOU_WILL_MEET_SOMEONE_TO_BE_OF_HELP;

	/**
	 * ID: 1800334<br>
	 * Message: Keep your moderation and ego in check even in successful phases of your life.
	 */
	public static final NpcStringId KEEP_YOUR_MODERATION_AND_EGO_IN_CHECK_EVEN_IN_SUCCESSFUL_PHASES_OF_YOUR_LIFE;

	/**
	 * ID: 1800335<br>
	 * Message: When it comes to work, lifestyle and relationships you'll be better off to go by the text rather than tricks.
	 */
	public static final NpcStringId
			WHEN_IT_COMES_TO_WORK_LIFESTYLE_AND_RELATIONSHIPS_YOULL_BE_BETTER_OFF_TO_GO_BY_THE_TEXT_RATHER_THAN_TRICKS;

	/**
	 * ID: 1800336<br>
	 * Message: Your task will receive substantial support since the surroundings will fully develop.
	 */
	public static final NpcStringId
			YOUR_TASK_WILL_RECEIVE_SUBSTANTIAL_SUPPORT_SINCE_THE_SURROUNDINGS_WILL_FULLY_DEVELOP;

	/**
	 * ID: 1800337<br>
	 * Message: Your star of fortune indicate a success with mental and material assistance.
	 */
	public static final NpcStringId YOUR_STAR_OF_FORTUNE_INDICATE_A_SUCCESS_WITH_MENTAL_AND_MATERIAL_ASSISTANCE;

	/**
	 * ID: 1800338<br>
	 * Message: You will enjoy popularity with your creative talents and clever acts.
	 */
	public static final NpcStringId YOU_WILL_ENJOY_POPULARITY_WITH_YOUR_CREATIVE_TALENTS_AND_CLEVER_ACTS;

	/**
	 * ID: 1800339<br>
	 * Message: People will line up to be of assistance to you.
	 */
	public static final NpcStringId PEOPLE_WILL_LINE_UP_TO_BE_OF_ASSISTANCE_TO_YOU;

	/**
	 * ID: 1800340<br>
	 * Message: You may meet someone to share your journey.
	 */
	public static final NpcStringId YOU_MAY_MEET_SOMEONE_TO_SHARE_YOUR_JOURNEY;

	/**
	 * ID: 1800341<br>
	 * Message: You may achieve connections in many fields.
	 */
	public static final NpcStringId YOU_MAY_ACHIEVE_CONNECTIONS_IN_MANY_FIELDS;

	/**
	 * ID: 1800342<br>
	 * Message: An attitude that continually studies and explores is needed, and always be sincere.
	 */
	public static final NpcStringId AN_ATTITUDE_THAT_CONTINUALLY_STUDIES_AND_EXPLORES_IS_NEEDED_AND_ALWAYS_BE_SINCERE;

	/**
	 * ID: 1800343<br>
	 * Message: It's an image of a butterfly on a flower in warm spring air.
	 */
	public static final NpcStringId ITS_AN_IMAGE_OF_A_BUTTERFLY_ON_A_FLOWER_IN_WARM_SPRING_AIR;

	/**
	 * ID: 1800344<br>
	 * Message: Your goals will move smoothly with peace and happiness in your life.
	 */
	public static final NpcStringId YOUR_GOALS_WILL_MOVE_SMOOTHLY_WITH_PEACE_AND_HAPPINESS_IN_YOUR_LIFE;

	/**
	 * ID: 1800345<br>
	 * Message: Love may sprout its leaves when you treat those around you with care.
	 */
	public static final NpcStringId LOVE_MAY_SPROUT_ITS_LEAVES_WHEN_YOU_TREAT_THOSE_AROUND_YOU_WITH_CARE;

	/**
	 * ID: 1800346<br>
	 * Message: You may climb into a higher position with others' trust if you faithfully carry out your duties.
	 */
	public static final NpcStringId
			YOU_MAY_CLIMB_INTO_A_HIGHER_POSITION_WITH_OTHERS_TRUST_IF_YOU_FAITHFULLY_CARRY_OUT_YOUR_DUTIES;

	/**
	 * ID: 1800347<br>
	 * Message: Everything can fall apart if you greedily aim by pure luck.
	 */
	public static final NpcStringId EVERYTHING_CAN_FALL_APART_IF_YOU_GREEDILY_AIM_BY_PURE_LUCK;

	/**
	 * ID: 1800348<br>
	 * Message: Do not underestimate the importance of meeting people.
	 */
	public static final NpcStringId DO_NOT_UNDERESTIMATE_THE_IMPORTANCE_OF_MEETING_PEOPLE;

	/**
	 * ID: 1800349<br>
	 * Message: An arrow will coalesce into the bow.
	 */
	public static final NpcStringId AN_ARROW_WILL_COALESCE_INTO_THE_BOW;

	/**
	 * ID: 1800350<br>
	 * Message: A bony limb of a tree may bear its fruit.
	 */
	public static final NpcStringId A_BONY_LIMB_OF_A_TREE_MAY_BEAR_ITS_FRUIT;

	/**
	 * ID: 1800351<br>
	 * Message: You will be rewarded for your efforts and accomplishments.
	 */
	public static final NpcStringId YOU_WILL_BE_REWARDED_FOR_YOUR_EFFORTS_AND_ACCOMPLISHMENTS;

	/**
	 * ID: 1800352<br>
	 * Message: No matter where it lies, your faithful drive leads you to success.
	 */
	public static final NpcStringId NO_MATTER_WHERE_IT_LIES_YOUR_FAITHFUL_DRIVE_LEADS_YOU_TO_SUCCESS;

	/**
	 * ID: 1800353<br>
	 * Message: People will be attracted to your loyalties.
	 */
	public static final NpcStringId PEOPLE_WILL_BE_ATTRACTED_TO_YOUR_LOYALTIES;

	/**
	 * ID: 1800354<br>
	 * Message: You may trust yourself rather than others' talks.
	 */
	public static final NpcStringId YOU_MAY_TRUST_YOURSELF_RATHER_THAN_OTHERS_TALKS;

	/**
	 * ID: 1800355<br>
	 * Message: Creative thinking away from the old viewpoint may help you.
	 */
	public static final NpcStringId CREATIVE_THINKING_AWAY_FROM_THE_OLD_VIEWPOINT_MAY_HELP_YOU;

	/**
	 * ID: 1800356<br>
	 * Message: Patience without being impetuous of the results will only bear a positive outcome.
	 */
	public static final NpcStringId PATIENCE_WITHOUT_BEING_IMPETUOUS_OF_THE_RESULTS_WILL_ONLY_BEAR_A_POSITIVE_OUTCOME;

	/**
	 * ID: 1800357<br>
	 * Message: The dead will come alive.
	 */
	public static final NpcStringId THE_DEAD_WILL_COME_ALIVE;

	/**
	 * ID: 1800358<br>
	 * Message: There will be a shocking incident.
	 */
	public static final NpcStringId THERE_WILL_BE_A_SHOCKING_INCIDENT;

	/**
	 * ID: 1800359<br>
	 * Message: You will enjoy a huge success after unforeseen luck comes before you.
	 */
	public static final NpcStringId YOU_WILL_ENJOY_A_HUGE_SUCCESS_AFTER_UNFORESEEN_LUCK_COMES_BEFORE_YOU;

	/**
	 * ID: 1800360<br>
	 * Message: Do not give up since there may be a miraculous rescue from the course of despair.
	 */
	public static final NpcStringId DO_NOT_GIVE_UP_SINCE_THERE_MAY_BE_A_MIRACULOUS_RESCUE_FROM_THE_COURSE_OF_DESPAIR;

	/**
	 * ID: 1800361<br>
	 * Message: An attitude to try one's best to pursue the goal is needed.
	 */
	public static final NpcStringId AN_ATTITUDE_TO_TRY_ONES_BEST_TO_PURSUE_THE_GOAL_IS_NEEDED;

	/**
	 * ID: 1800362<br>
	 * Message: You may get a shot in the arm in your life after meeting a good person.
	 */
	public static final NpcStringId YOU_MAY_GET_A_SHOT_IN_THE_ARM_IN_YOUR_LIFE_AFTER_MEETING_A_GOOD_PERSON;

	/**
	 * ID: 1800363<br>
	 * Message: You may get a big help in the course of your life.
	 */
	public static final NpcStringId YOU_MAY_GET_A_BIG_HELP_IN_THE_COURSE_OF_YOUR_LIFE;

	/**
	 * ID: 1800364<br>
	 * Message: A rare opportunity will come to you so you may prosper.
	 */
	public static final NpcStringId A_RARE_OPPORTUNITY_WILL_COME_TO_YOU_SO_YOU_MAY_PROSPER;

	/**
	 * ID: 1800365<br>
	 * Message: A hungry falcon will have meat.
	 */
	public static final NpcStringId A_HUNGRY_FALCON_WILL_HAVE_MEAT;

	/**
	 * ID: 1800366<br>
	 * Message: A household in need will acquire a fortune and meat.
	 */
	public static final NpcStringId A_HOUSEHOLD_IN_NEED_WILL_ACQUIRE_A_FORTUNE_AND_MEAT;

	/**
	 * ID: 1800367<br>
	 * Message: A hard situation will come to its end with materialistic and mental help from others.
	 */
	public static final NpcStringId
			A_HARD_SITUATION_WILL_COME_TO_ITS_END_WITH_MATERIALISTIC_AND_MENTAL_HELP_FROM_OTHERS;

	/**
	 * ID: 1800368<br>
	 * Message: If you set a firm goal without surrender, there will be a person who can offer help and care.
	 */
	public static final NpcStringId
			IF_YOU_SET_A_FIRM_GOAL_WITHOUT_SURRENDER_THERE_WILL_BE_A_PERSON_WHO_CAN_OFFER_HELP_AND_CARE;

	/**
	 * ID: 1800369<br>
	 * Message: You'll gain others' trust when you maintain a sincere and honest attitude.
	 */
	public static final NpcStringId YOULL_GAIN_OTHERS_TRUST_WHEN_YOU_MAINTAIN_A_SINCERE_AND_HONEST_ATTITUDE;

	/**
	 * ID: 1800370<br>
	 * Message: Be independent at all times.
	 */
	public static final NpcStringId BE_INDEPENDENT_AT_ALL_TIMES;

	/**
	 * ID: 1800371<br>
	 * Message: It's a wagon with no wheels.
	 */
	public static final NpcStringId ITS_A_WAGON_WITH_NO_WHEELS;

	/**
	 * ID: 1800372<br>
	 * Message: You've set a goal but there may be obstacles in reality.
	 */
	public static final NpcStringId YOUVE_SET_A_GOAL_BUT_THERE_MAY_BE_OBSTACLES_IN_REALITY;

	/**
	 * ID: 1800373<br>
	 * Message: You're running toward the goal but there won't be as many outcomes as you thought.
	 */
	public static final NpcStringId YOURE_RUNNING_TOWARD_THE_GOAL_BUT_THERE_WONT_BE_AS_MANY_OUTCOMES_AS_YOU_THOUGHT;

	/**
	 * ID: 1800374<br>
	 * Message: There are many things to consider after encountering hindrances.
	 */
	public static final NpcStringId THERE_ARE_MANY_THINGS_TO_CONSIDER_AFTER_ENCOUNTERING_HINDRANCES;

	/**
	 * ID: 1800375<br>
	 * Message: A reckless move may bring a failure.
	 */
	public static final NpcStringId A_RECKLESS_MOVE_MAY_BRING_A_FAILURE;

	/**
	 * ID: 1800376<br>
	 * Message: You may lose people's trust if you lack prudence at all times.
	 */
	public static final NpcStringId YOU_MAY_LOSE_PEOPLES_TRUST_IF_YOU_LACK_PRUDENCE_AT_ALL_TIMES;

	/**
	 * ID: 1800377<br>
	 * Message: You may need to reflect on yourself with deliberation and wait for an opportunity.
	 */
	public static final NpcStringId YOU_MAY_NEED_TO_REFLECT_ON_YOURSELF_WITH_DELIBERATION_AND_WAIT_FOR_AN_OPPORTUNITY;

	/**
	 * ID: 1800378<br>
	 * Message: A poor scholar receives a stipend.
	 */
	public static final NpcStringId A_POOR_SCHOLAR_RECEIVES_A_STIPEND;

	/**
	 * ID: 1800379<br>
	 * Message: A scholar gets a pass toward fame and fortune.
	 */
	public static final NpcStringId A_SCHOLAR_GETS_A_PASS_TOWARD_FAME_AND_FORTUNE;

	/**
	 * ID: 1800380<br>
	 * Message: Your ambition and dream will come true.
	 */
	public static final NpcStringId YOUR_AMBITION_AND_DREAM_WILL_COME_TRUE;

	/**
	 * ID: 1800381<br>
	 * Message: Complicated problems around you may start being solved one after another.
	 */
	public static final NpcStringId COMPLICATED_PROBLEMS_AROUND_YOU_MAY_START_BEING_SOLVED_ONE_AFTER_ANOTHER;

	/**
	 * ID: 1800382<br>
	 * Message: You will have a good result if you diligently pursue one goal without being dragged from your past.
	 */
	public static final NpcStringId
			YOU_WILL_HAVE_A_GOOD_RESULT_IF_YOU_DILIGENTLY_PURSUE_ONE_GOAL_WITHOUT_BEING_DRAGGED_FROM_YOUR_PAST;

	/**
	 * ID: 1800383<br>
	 * Message: You may need to rid yourself of old and worn habits.
	 */
	public static final NpcStringId YOU_MAY_NEED_TO_RID_YOURSELF_OF_OLD_AND_WORN_HABITS;

	/**
	 * ID: 1800384<br>
	 * Message: Be responsible with your tasks but do not hesitate to ask for colleagues' help.
	 */
	public static final NpcStringId BE_RESPONSIBLE_WITH_YOUR_TASKS_BUT_DO_NOT_HESITATE_TO_ASK_FOR_COLLEAGUES_HELP;

	/**
	 * ID: 1800385<br>
	 * Message: Fish transforms into a dragon.
	 */
	public static final NpcStringId FISH_TRANSFORMS_INTO_A_DRAGON;

	/**
	 * ID: 1800386<br>
	 * Message: Your dream may come true, and fame and fortune will come to you.
	 */
	public static final NpcStringId YOUR_DREAM_MAY_COME_TRUE_AND_FAME_AND_FORTUNE_WILL_COME_TO_YOU;

	/**
	 * ID: 1800387<br>
	 * Message: What you've planed will be accomplished.
	 */
	public static final NpcStringId WHAT_YOUVE_PLANED_WILL_BE_ACCOMPLISHED;

	/**
	 * ID: 1800388<br>
	 * Message: You may acquire money or a new opportunity from a place you wouldn't have thought of.
	 */
	public static final NpcStringId YOU_MAY_ACQUIRE_MONEY_OR_A_NEW_OPPORTUNITY_FROM_A_PLACE_YOU_WOULDNT_HAVE_THOUGHT_OF;

	/**
	 * ID: 1800389<br>
	 * Message: There will be many offers to you, you may think them over carefully.
	 */
	public static final NpcStringId THERE_WILL_BE_MANY_OFFERS_TO_YOU_YOU_MAY_THINK_THEM_OVER_CAREFULLY;

	/**
	 * ID: 1800390<br>
	 * Message: It may be a good idea not to become involved in others' business.
	 */
	public static final NpcStringId IT_MAY_BE_A_GOOD_IDEA_NOT_TO_BECOME_INVOLVED_IN_OTHERS_BUSINESS;

	/**
	 * ID: 1800391<br>
	 * Message: Everything will go smoothly but be aware of danger from being careless and remiss.
	 */
	public static final NpcStringId EVERYTHING_WILL_GO_SMOOTHLY_BUT_BE_AWARE_OF_DANGER_FROM_BEING_CARELESS_AND_REMISS;

	/**
	 * ID: 1800392<br>
	 * Message: If you sincerely care for someone you love, a big reward will return to you.
	 */
	public static final NpcStringId IF_YOU_SINCERELY_CARE_FOR_SOMEONE_YOU_LOVE_A_BIG_REWARD_WILL_RETURN_TO_YOU;

	/**
	 * ID: 1800393<br>
	 * Message: A remedy is on its way for a serious illness.
	 */
	public static final NpcStringId A_REMEDY_IS_ON_ITS_WAY_FOR_A_SERIOUS_ILLNESS;

	/**
	 * ID: 1800394<br>
	 * Message: You may acquire a precious medicine to recover after suffering a disease of a serious nature.
	 */
	public static final NpcStringId
			YOU_MAY_ACQUIRE_A_PRECIOUS_MEDICINE_TO_RECOVER_AFTER_SUFFERING_A_DISEASE_OF_A_SERIOUS_NATURE;

	/**
	 * ID: 1800395<br>
	 * Message: You may realize your dream by meeting a man of distinction at a difficult time.
	 */
	public static final NpcStringId YOU_MAY_REALIZE_YOUR_DREAM_BY_MEETING_A_MAN_OF_DISTINCTION_AT_A_DIFFICULT_TIME;

	/**
	 * ID: 1800396<br>
	 * Message: You may suffer one or two hardships on your journey.
	 */
	public static final NpcStringId YOU_MAY_SUFFER_ONE_OR_TWO_HARDSHIPS_ON_YOUR_JOURNEY;

	/**
	 * ID: 1800397<br>
	 * Message: If you keep smiling without despair, people will come to trust you and offer help.
	 */
	public static final NpcStringId IF_YOU_KEEP_SMILING_WITHOUT_DESPAIR_PEOPLE_WILL_COME_TO_TRUST_YOU_AND_OFFER_HELP;

	/**
	 * ID: 1800398<br>
	 * Message: Seek stability rather than dynamics in your life.
	 */
	public static final NpcStringId SEEK_STABILITY_RATHER_THAN_DYNAMICS_IN_YOUR_LIFE;

	/**
	 * ID: 1800399<br>
	 * Message: It's a good idea to be careful and secure at all times.
	 */
	public static final NpcStringId ITS_A_GOOD_IDEA_TO_BE_CAREFUL_AND_SECURE_AT_ALL_TIMES;

	/**
	 * ID: 1800400<br>
	 * Message: You can't perform the job with bound hands.
	 */
	public static final NpcStringId YOU_CANT_PERFORM_THE_JOB_WITH_BOUND_HANDS;

	/**
	 * ID: 1800401<br>
	 * Message: You may lose your drive and feel lost.
	 */
	public static final NpcStringId YOU_MAY_LOSE_YOUR_DRIVE_AND_FEEL_LOST;

	/**
	 * ID: 1800402<br>
	 * Message: You may be unable to concentrate with so many problems occurring.
	 */
	public static final NpcStringId YOU_MAY_BE_UNABLE_TO_CONCENTRATE_WITH_SO_MANY_PROBLEMS_OCCURRING;

	/**
	 * ID: 1800403<br>
	 * Message: Your achievement unfairly may go somewhere else.
	 */
	public static final NpcStringId YOUR_ACHIEVEMENT_UNFAIRLY_MAY_GO_SOMEWHERE_ELSE;

	/**
	 * ID: 1800404<br>
	 * Message: Do not start a task that's not clear to you.
	 */
	public static final NpcStringId DO_NOT_START_A_TASK_THATS_NOT_CLEAR_TO_YOU;

	/**
	 * ID: 1800405<br>
	 * Message: You will need to be prepared for all events.
	 */
	public static final NpcStringId YOU_WILL_NEED_TO_BE_PREPARED_FOR_ALL_EVENTS;

	/**
	 * ID: 1800406<br>
	 * Message: Someone will acknowledge you if you relentlessly keep trying and do not give up when facing hardships.
	 */
	public static final NpcStringId
			SOMEONE_WILL_ACKNOWLEDGE_YOU_IF_YOU_RELENTLESSLY_KEEP_TRYING_AND_DO_NOT_GIVE_UP_WHEN_FACING_HARDSHIPS;

	/**
	 * ID: 1800407<br>
	 * Message: You may perfect yourself like a dragon's horn decorates the dragon.
	 */
	public static final NpcStringId YOU_MAY_PERFECT_YOURSELF_LIKE_A_DRAGONS_HORN_DECORATES_THE_DRAGON;

	/**
	 * ID: 1800408<br>
	 * Message: Your true value starts to shine.
	 */
	public static final NpcStringId YOUR_TRUE_VALUE_STARTS_TO_SHINE;

	/**
	 * ID: 1800409<br>
	 * Message: Your steady pursuit of new information and staying ahead of others will raise your value.
	 */
	public static final NpcStringId
			YOUR_STEADY_PURSUIT_OF_NEW_INFORMATION_AND_STAYING_AHEAD_OF_OTHERS_WILL_RAISE_YOUR_VALUE;

	/**
	 * ID: 1800410<br>
	 * Message: Maintaining confidence with work or relationships may bring good results.
	 */
	public static final NpcStringId MAINTAINING_CONFIDENCE_WITH_WORK_OR_RELATIONSHIPS_MAY_BRING_GOOD_RESULTS;

	/**
	 * ID: 1800411<br>
	 * Message: Learn to work with others since overconfidence will bear wrath.
	 */
	public static final NpcStringId LEARN_TO_WORK_WITH_OTHERS_SINCE_OVERCONFIDENCE_WILL_BEAR_WRATH;

	/**
	 * ID: 1800412<br>
	 * Message: The dragon now acquires an eagle's wings.
	 */
	public static final NpcStringId THE_DRAGON_NOW_ACQUIRES_AN_EAGLES_WINGS;

	/**
	 * ID: 1800413<br>
	 * Message: As the dragon flies high in the sky, your goals and dreams may come true.
	 */
	public static final NpcStringId AS_THE_DRAGON_FLIES_HIGH_IN_THE_SKY_YOUR_GOALS_AND_DREAMS_MAY_COME_TRUE;

	/**
	 * ID: 1800414<br>
	 * Message: Luck enters into your work, hobby, family and love.
	 */
	public static final NpcStringId LUCK_ENTERS_INTO_YOUR_WORK_HOBBY_FAMILY_AND_LOVE;

	/**
	 * ID: 1800415<br>
	 * Message: Whatever you do, it will accompany winning.
	 */
	public static final NpcStringId WHATEVER_YOU_DO_IT_WILL_ACCOMPANY_WINNING;

	/**
	 * ID: 1800416<br>
	 * Message: It's as good as it gets with unforeseen fortune rolling your way.
	 */
	public static final NpcStringId ITS_AS_GOOD_AS_IT_GETS_WITH_UNFORESEEN_FORTUNE_ROLLING_YOUR_WAY;

	/**
	 * ID: 1800417<br>
	 * Message: A greedy act with no prudence will bring a surprise at the end.
	 */
	public static final NpcStringId A_GREEDY_ACT_WITH_NO_PRUDENCE_WILL_BRING_A_SURPRISE_AT_THE_END;

	/**
	 * ID: 1800418<br>
	 * Message: Think carefully and act with caution at all times.
	 */
	public static final NpcStringId THINK_CAREFULLY_AND_ACT_WITH_CAUTION_AT_ALL_TIMES;

	/**
	 * ID: 1800419<br>
	 * Message: If a tree doesn't have its roots, there will be no fruit.
	 */
	public static final NpcStringId IF_A_TREE_DOESNT_HAVE_ITS_ROOTS_THERE_WILL_BE_NO_FRUIT;

	/**
	 * ID: 1800420<br>
	 * Message: Hard work doesn't bear fruit.
	 */
	public static final NpcStringId HARD_WORK_DOESNT_BEAR_FRUIT;

	/**
	 * ID: 1800421<br>
	 * Message: Financial difficulties may bring an ordeal.
	 */
	public static final NpcStringId FINANCIAL_DIFFICULTIES_MAY_BRING_AN_ORDEAL;

	/**
	 * ID: 1800422<br>
	 * Message: What used to be well managed may stumble one after another.
	 */
	public static final NpcStringId WHAT_USED_TO_BE_WELL_MANAGED_MAY_STUMBLE_ONE_AFTER_ANOTHER;

	/**
	 * ID: 1800423<br>
	 * Message: A feeling of frustration may follow disappointment.
	 */
	public static final NpcStringId A_FEELING_OF_FRUSTRATION_MAY_FOLLOW_DISAPPOINTMENT;

	/**
	 * ID: 1800424<br>
	 * Message: Be cautioned as unharnessed behavior at difficult times can ruin relationships.
	 */
	public static final NpcStringId BE_CAUTIONED_AS_UNHARNESSED_BEHAVIOR_AT_DIFFICULT_TIMES_CAN_RUIN_RELATIONSHIPS;

	/**
	 * ID: 1800425<br>
	 * Message: Curtail greed and be grateful for small returns as modesty is needed.
	 */
	public static final NpcStringId CURTAIL_GREED_AND_BE_GRATEFUL_FOR_SMALL_RETURNS_AS_MODESTY_IS_NEEDED;

	/**
	 * ID: 1800426<br>
	 * Message: The person that came under your wings will leave.
	 */
	public static final NpcStringId THE_PERSON_THAT_CAME_UNDER_YOUR_WINGS_WILL_LEAVE;

	/**
	 * ID: 1800427<br>
	 * Message: Your work and relationship with colleagues will be well managed if you maintain your devotion.
	 */
	public static final NpcStringId
			YOUR_WORK_AND_RELATIONSHIP_WITH_COLLEAGUES_WILL_BE_WELL_MANAGED_IF_YOU_MAINTAIN_YOUR_DEVOTION;

	/**
	 * ID: 1800428<br>
	 * Message: Calculating your profit in relationships without displaying any courteous manners will bring malicious gossip and ruin your value.
	 */
	public static final NpcStringId
			CALCULATING_YOUR_PROFIT_IN_RELATIONSHIPS_WITHOUT_DISPLAYING_ANY_COURTEOUS_MANNERS_WILL_BRING_MALICIOUS_GOSSIP_AND_RUIN_YOUR_VALUE;

	/**
	 * ID: 1800429<br>
	 * Message: Consider other's situations and treat them sincerely at all times.
	 */
	public static final NpcStringId CONSIDER_OTHERS_SITUATIONS_AND_TREAT_THEM_SINCERELY_AT_ALL_TIMES;

	/**
	 * ID: 1800430<br>
	 * Message: Do not loosen up with your precautions.
	 */
	public static final NpcStringId DO_NOT_LOOSEN_UP_WITH_YOUR_PRECAUTIONS;

	/**
	 * ID: 1800431<br>
	 * Message: Reflect other's opinions as a mistake always lies ahead of an arbitrary decision.
	 */
	public static final NpcStringId REFLECT_OTHERS_OPINIONS_AS_A_MISTAKE_ALWAYS_LIES_AHEAD_OF_AN_ARBITRARY_DECISION;

	/**
	 * ID: 1800432<br>
	 * Message: A blind man goes right through the door.
	 */
	public static final NpcStringId A_BLIND_MAN_GOES_RIGHT_THROUGH_THE_DOOR;

	/**
	 * ID: 1800433<br>
	 * Message: A heart falls into hopelessness as things are in disarray.
	 */
	public static final NpcStringId A_HEART_FALLS_INTO_HOPELESSNESS_AS_THINGS_ARE_IN_DISARRAY;

	/**
	 * ID: 1800434<br>
	 * Message: Hopelessness may fill your heart as your work falls into a maze.
	 */
	public static final NpcStringId HOPELESSNESS_MAY_FILL_YOUR_HEART_AS_YOUR_WORK_FALLS_INTO_A_MAZE;

	/**
	 * ID: 1800435<br>
	 * Message: Difficulties lie ahead of an unforeseen problem even with your hard work.
	 */
	public static final NpcStringId DIFFICULTIES_LIE_AHEAD_OF_AN_UNFORESEEN_PROBLEM_EVEN_WITH_YOUR_HARD_WORK;

	/**
	 * ID: 1800436<br>
	 * Message: There may be more occasions you will want to ask favors from others as you lose confidence in you.
	 */
	public static final NpcStringId
			THERE_MAY_BE_MORE_OCCASIONS_YOU_WILL_WANT_TO_ASK_FAVORS_FROM_OTHERS_AS_YOU_LOSE_CONFIDENCE_IN_YOU;

	/**
	 * ID: 1800437<br>
	 * Message: Be brave and ambitious as no bird can fly into the sky by staying in their nest.
	 */
	public static final NpcStringId BE_BRAVE_AND_AMBITIOUS_AS_NO_BIRD_CAN_FLY_INTO_THE_SKY_BY_STAYING_IN_THEIR_NEST;

	/**
	 * ID: 1800438<br>
	 * Message: It's a good idea not to start an unclear task and always look for someone you can trust and rely upon.
	 */
	public static final NpcStringId
			ITS_A_GOOD_IDEA_NOT_TO_START_AN_UNCLEAR_TASK_AND_ALWAYS_LOOK_FOR_SOMEONE_YOU_CAN_TRUST_AND_RELY_UPON;

	/**
	 * ID: 1800439<br>
	 * Message: Hunting won't be successful as the falcon lacks its claws.
	 */
	public static final NpcStringId HUNTING_WONT_BE_SUCCESSFUL_AS_THE_FALCON_LACKS_ITS_CLAWS;

	/**
	 * ID: 1800440<br>
	 * Message: A prepared plan won't move smoothly.
	 */
	public static final NpcStringId A_PREPARED_PLAN_WONT_MOVE_SMOOTHLY;

	/**
	 * ID: 1800441<br>
	 * Message: An easy task may fail if one is consumed by greed and overconfidence.
	 */
	public static final NpcStringId AN_EASY_TASK_MAY_FAIL_IF_ONE_IS_CONSUMED_BY_GREED_AND_OVERCONFIDENCE;

	/**
	 * ID: 1800442<br>
	 * Message: Impatience may lie ahead as the situation is unfavorable.
	 */
	public static final NpcStringId IMPATIENCE_MAY_LIE_AHEAD_AS_THE_SITUATION_IS_UNFAVORABLE;

	/**
	 * ID: 1800443<br>
	 * Message: Thoughtful foresight is needed before a disaster may fall upon you.
	 */
	public static final NpcStringId THOUGHTFUL_FORESIGHT_IS_NEEDED_BEFORE_A_DISASTER_MAY_FALL_UPON_YOU;

	/**
	 * ID: 1800444<br>
	 * Message: Refrain from dictatorial acts as caring for others around you with dignity is much needed.
	 */
	public static final NpcStringId
			REFRAIN_FROM_DICTATORIAL_ACTS_AS_CARING_FOR_OTHERS_AROUND_YOU_WITH_DIGNITY_IS_MUCH_NEEDED;

	/**
	 * ID: 1800445<br>
	 * Message: Things are messy with no good sign.
	 */
	public static final NpcStringId THINGS_ARE_MESSY_WITH_NO_GOOD_SIGN;

	/**
	 * ID: 1800446<br>
	 * Message: You may fall into a vexing situation as bad circumstances will arise.
	 */
	public static final NpcStringId YOU_MAY_FALL_INTO_A_VEXING_SITUATION_AS_BAD_CIRCUMSTANCES_WILL_ARISE;

	/**
	 * ID: 1800447<br>
	 * Message: Relationships with people may be contrary to your expectations.
	 */
	public static final NpcStringId RELATIONSHIPS_WITH_PEOPLE_MAY_BE_CONTRARY_TO_YOUR_EXPECTATIONS;

	/**
	 * ID: 1800448<br>
	 * Message: Do not seek a quick fix as the problem needs a fundamental resolution.
	 */
	public static final NpcStringId DO_NOT_SEEK_A_QUICK_FIX_AS_THE_PROBLEM_NEEDS_A_FUNDAMENTAL_RESOLUTION;

	/**
	 * ID: 1800449<br>
	 * Message: Seek peace in your heart as vulgar display of your emotions may harm you.
	 */
	public static final NpcStringId SEEK_PEACE_IN_YOUR_HEART_AS_VULGAR_DISPLAY_OF_YOUR_EMOTIONS_MAY_HARM_YOU;

	/**
	 * ID: 1800450<br>
	 * Message: Information for success may come from the conversations with people around you.
	 */
	public static final NpcStringId INFORMATION_FOR_SUCCESS_MAY_COME_FROM_THE_CONVERSATIONS_WITH_PEOPLE_AROUND_YOU;

	/**
	 * ID: 1800451<br>
	 * Message: Be confident and act reliantly at all times.
	 */
	public static final NpcStringId BE_CONFIDENT_AND_ACT_RELIANTLY_AT_ALL_TIMES;

	/**
	 * ID: 1800452<br>
	 * Message: A child gets a treasure.
	 */
	public static final NpcStringId A_CHILD_GETS_A_TREASURE;

	/**
	 * ID: 1800453<br>
	 * Message: Good fortune and opportunity may lie ahead as if one's born in a golden spoon.
	 */
	public static final NpcStringId GOOD_FORTUNE_AND_OPPORTUNITY_MAY_LIE_AHEAD_AS_IF_ONES_BORN_IN_A_GOLDEN_SPOON;

	/**
	 * ID: 1800454<br>
	 * Message: Your life flows as if it's on a silk surface and unexpected fortune and success may come to you.
	 */
	public static final NpcStringId
			YOUR_LIFE_FLOWS_AS_IF_ITS_ON_A_SILK_SURFACE_AND_UNEXPECTED_FORTUNE_AND_SUCCESS_MAY_COME_TO_YOU;

	/**
	 * ID: 1800455<br>
	 * Message: Temporary luck may come to you with no effort.
	 */
	public static final NpcStringId TEMPORARY_LUCK_MAY_COME_TO_YOU_WITH_NO_EFFORT;

	/**
	 * ID: 1800456<br>
	 * Message: Plan ahead with patience but execute with swiftness.
	 */
	public static final NpcStringId PLAN_AHEAD_WITH_PATIENCE_BUT_EXECUTE_WITH_SWIFTNESS;

	/**
	 * ID: 1800457<br>
	 * Message: The abilities to amend, foresee and analyze may raise your value.
	 */
	public static final NpcStringId THE_ABILITIES_TO_AMEND_FORESEE_AND_ANALYZE_MAY_RAISE_YOUR_VALUE;

	/**
	 * ID: 1800458<br>
	 * Message: Bigger mistakes will be on the road if you fail to correct a small mistake.
	 */
	public static final NpcStringId BIGGER_MISTAKES_WILL_BE_ON_THE_ROAD_IF_YOU_FAIL_TO_CORRECT_A_SMALL_MISTAKE;

	/**
	 * ID: 1800459<br>
	 * Message: Don't be evasive to accept new findings or experiences.
	 */
	public static final NpcStringId DONT_BE_EVASIVE_TO_ACCEPT_NEW_FINDINGS_OR_EXPERIENCES;

	/**
	 * ID: 1800460<br>
	 * Message: Don't be irritated as the situations don't move as planned.
	 */
	public static final NpcStringId DONT_BE_IRRITATED_AS_THE_SITUATIONS_DONT_MOVE_AS_PLANNED;

	/**
	 * ID: 1800461<br>
	 * Message: Be warned as you may be overwhelmed by surroundings if you lack a clear opinion.
	 */
	public static final NpcStringId BE_WARNED_AS_YOU_MAY_BE_OVERWHELMED_BY_SURROUNDINGS_IF_YOU_LACK_A_CLEAR_OPINION;

	/**
	 * ID: 1800462<br>
	 * Message: You may live an affluent life even without possessions.
	 */
	public static final NpcStringId YOU_MAY_LIVE_AN_AFFLUENT_LIFE_EVEN_WITHOUT_POSSESSIONS;

	/**
	 * ID: 1800463<br>
	 * Message: You will gain popularity as you help people with money you earnestly earned.
	 */
	public static final NpcStringId YOU_WILL_GAIN_POPULARITY_AS_YOU_HELP_PEOPLE_WITH_MONEY_YOU_EARNESTLY_EARNED;

	/**
	 * ID: 1800464<br>
	 * Message: Your heart and body may be in health.
	 */
	public static final NpcStringId YOUR_HEART_AND_BODY_MAY_BE_IN_HEALTH;

	/**
	 * ID: 1800465<br>
	 * Message: Be warned as you may be dragged to an unwanted direction if not cautious.
	 */
	public static final NpcStringId BE_WARNED_AS_YOU_MAY_BE_DRAGGED_TO_AN_UNWANTED_DIRECTION_IF_NOT_CAUTIOUS;

	/**
	 * ID: 1800466<br>
	 * Message: You may meet many new people but it will be difficult to find a perfect person who wins your heart.
	 */
	public static final NpcStringId
			YOU_MAY_MEET_MANY_NEW_PEOPLE_BUT_IT_WILL_BE_DIFFICULT_TO_FIND_A_PERFECT_PERSON_WHO_WINS_YOUR_HEART;

	/**
	 * ID: 1800467<br>
	 * Message: There may be an occasion where you are consoled by people.
	 */
	public static final NpcStringId THERE_MAY_BE_AN_OCCASION_WHERE_YOU_ARE_CONSOLED_BY_PEOPLE;

	/**
	 * ID: 1800468<br>
	 * Message: It may not be a good time for a change even if there's tedium in daily life.
	 */
	public static final NpcStringId IT_MAY_NOT_BE_A_GOOD_TIME_FOR_A_CHANGE_EVEN_IF_THERES_TEDIUM_IN_DAILY_LIFE;

	/**
	 * ID: 1800469<br>
	 * Message: The money you spend for yourself may act as an investment and bring you a return.
	 */
	public static final NpcStringId THE_MONEY_YOU_SPEND_FOR_YOURSELF_MAY_ACT_AS_AN_INVESTMENT_AND_BRING_YOU_A_RETURN;

	/**
	 * ID: 1800470<br>
	 * Message: The money you spend for others will be wasted so be cautious.
	 */
	public static final NpcStringId THE_MONEY_YOU_SPEND_FOR_OTHERS_WILL_BE_WASTED_SO_BE_CAUTIOUS;

	/**
	 * ID: 1800471<br>
	 * Message: Be warned so as not to have unnecessary expenses.
	 */
	public static final NpcStringId BE_WARNED_SO_AS_NOT_TO_HAVE_UNNECESSARY_EXPENSES;

	/**
	 * ID: 1800472<br>
	 * Message: Your star indicated such good luck, participate in bonus giveaways or events.
	 */
	public static final NpcStringId YOUR_STAR_INDICATED_SUCH_GOOD_LUCK_PARTICIPATE_IN_BONUS_GIVEAWAYS_OR_EVENTS;

	/**
	 * ID: 1800473<br>
	 * Message: You may grab unexpected luck.
	 */
	public static final NpcStringId YOU_MAY_GRAB_UNEXPECTED_LUCK;

	/**
	 * ID: 1800474<br>
	 * Message: The person in your heart may naturally come to you.
	 */
	public static final NpcStringId THE_PERSON_IN_YOUR_HEART_MAY_NATURALLY_COME_TO_YOU;

	/**
	 * ID: 1800475<br>
	 * Message: There will be a good result if you keep your own pace regardless of others' judgment.
	 */
	public static final NpcStringId THERE_WILL_BE_A_GOOD_RESULT_IF_YOU_KEEP_YOUR_OWN_PACE_REGARDLESS_OF_OTHERS_JUDGMENT;

	/**
	 * ID: 1800476<br>
	 * Message: Be warned as unexpected luck may be wasted with your reckless comments.
	 */
	public static final NpcStringId BE_WARNED_AS_UNEXPECTED_LUCK_MAY_BE_WASTED_WITH_YOUR_RECKLESS_COMMENTS;

	/**
	 * ID: 1800477<br>
	 * Message: Overconfidence will convince you to carry a task above your reach and there will be consequences.
	 */
	public static final NpcStringId
			OVERCONFIDENCE_WILL_CONVINCE_YOU_TO_CARRY_A_TASK_ABOVE_YOUR_REACH_AND_THERE_WILL_BE_CONSEQUENCES;

	/**
	 * ID: 1800478<br>
	 * Message: Momentarily delay an important decision.
	 */
	public static final NpcStringId MOMENTARILY_DELAY_AN_IMPORTANT_DECISION;

	/**
	 * ID: 1800479<br>
	 * Message: Trouble spots lie ahead when talking to superiors or people close to you.
	 */
	public static final NpcStringId TROUBLE_SPOTS_LIE_AHEAD_WHEN_TALKING_TO_SUPERIORS_OR_PEOPLE_CLOSE_TO_YOU;

	/**
	 * ID: 1800480<br>
	 * Message: Be warned as your words can hurt others or others' words can hurt you.
	 */
	public static final NpcStringId BE_WARNED_AS_YOUR_WORDS_CAN_HURT_OTHERS_OR_OTHERS_WORDS_CAN_HURT_YOU;

	/**
	 * ID: 1800481<br>
	 * Message: Make a loud boast and you may have to pay to cover unnecessary expenses.
	 */
	public static final NpcStringId MAKE_A_LOUD_BOAST_AND_YOU_MAY_HAVE_TO_PAY_TO_COVER_UNNECESSARY_EXPENSES;

	/**
	 * ID: 1800482<br>
	 * Message: Skillful evasion is needed when dealing with people who pick fights as a disaster may arise from it.
	 */
	public static final NpcStringId
			SKILLFUL_EVASION_IS_NEEDED_WHEN_DEALING_WITH_PEOPLE_WHO_PICK_FIGHTS_AS_A_DISASTER_MAY_ARISE_FROM_IT;

	/**
	 * ID: 1800483<br>
	 * Message: Keep a low profile as too strong an opinion will attract adverse reactions.
	 */
	public static final NpcStringId KEEP_A_LOW_PROFILE_AS_TOO_STRONG_AN_OPINION_WILL_ATTRACT_ADVERSE_REACTIONS;

	/**
	 * ID: 1800484<br>
	 * Message: Do not unnecessarily provoke misunderstanding as you may be involved in malicious gossip.
	 */
	public static final NpcStringId
			DO_NOT_UNNECESSARILY_PROVOKE_MISUNDERSTANDING_AS_YOU_MAY_BE_INVOLVED_IN_MALICIOUS_GOSSIP;

	/**
	 * ID: 1800485<br>
	 * Message: Check your belongings as you may lose what you possess.
	 */
	public static final NpcStringId CHECK_YOUR_BELONGINGS_AS_YOU_MAY_LOSE_WHAT_YOU_POSSESS;

	/**
	 * ID: 1800486<br>
	 * Message: Be flexible enough to play up to others.
	 */
	public static final NpcStringId BE_FLEXIBLE_ENOUGH_TO_PLAY_UP_TO_OTHERS;

	/**
	 * ID: 1800487<br>
	 * Message: Pay special attention when meeting or talking to people as relationships may go amiss.
	 */
	public static final NpcStringId
			PAY_SPECIAL_ATTENTION_WHEN_MEETING_OR_TALKING_TO_PEOPLE_AS_RELATIONSHIPS_MAY_GO_AMISS;

	/**
	 * ID: 1800488<br>
	 * Message: When the important moment arrives, decide upon what you truly want without measuring others' judgment.
	 */
	public static final NpcStringId
			WHEN_THE_IMPORTANT_MOMENT_ARRIVES_DECIDE_UPON_WHAT_YOU_TRULY_WANT_WITHOUT_MEASURING_OTHERS_JUDGMENT;

	/**
	 * ID: 1800489<br>
	 * Message: Luck will always follow you if you travel and read many books.
	 */
	public static final NpcStringId LUCK_WILL_ALWAYS_FOLLOW_YOU_IF_YOU_TRAVEL_AND_READ_MANY_BOOKS;

	/**
	 * ID: 1800490<br>
	 * Message: Head to a place that needs your advice as good ideas and wisdom will flourish.
	 */
	public static final NpcStringId HEAD_TO_A_PLACE_THAT_NEEDS_YOUR_ADVICE_AS_GOOD_IDEAS_AND_WISDOM_WILL_FLOURISH;

	/**
	 * ID: 1800491<br>
	 * Message: Someone's life may change upon your advice.
	 */
	public static final NpcStringId SOMEONES_LIFE_MAY_CHANGE_UPON_YOUR_ADVICE;

	/**
	 * ID: 1800492<br>
	 * Message: It's a proper time to plan for the future rather than a short term plan.
	 */
	public static final NpcStringId ITS_A_PROPER_TIME_TO_PLAN_FOR_THE_FUTURE_RATHER_THAN_A_SHORT_TERM_PLAN;

	/**
	 * ID: 1800493<br>
	 * Message: Many thoughtful plans at present time will be of great help in the future.
	 */
	public static final NpcStringId MANY_THOUGHTFUL_PLANS_AT_PRESENT_TIME_WILL_BE_OF_GREAT_HELP_IN_THE_FUTURE;

	/**
	 * ID: 1800494<br>
	 * Message: Patience may be needed as a big quarrel arises between you and a person close to you.
	 */
	public static final NpcStringId
			PATIENCE_MAY_BE_NEEDED_AS_A_BIG_QUARREL_ARISES_BETWEEN_YOU_AND_A_PERSON_CLOSE_TO_YOU;

	/**
	 * ID: 1800495<br>
	 * Message: Do not ask for financial help when the time is difficult. Your pride will be hurt without gaining any money.
	 */
	public static final NpcStringId
			DO_NOT_ASK_FOR_FINANCIAL_HELP_WHEN_THE_TIME_IS_DIFFICULT_YOUR_PRIDE_WILL_BE_HURT_WITHOUT_GAINING_ANY_MONEY;

	/**
	 * ID: 1800496<br>
	 * Message: Connection with a special person starts with a mere incident.
	 */
	public static final NpcStringId CONNECTION_WITH_A_SPECIAL_PERSON_STARTS_WITH_A_MERE_INCIDENT;

	/**
	 * ID: 1800497<br>
	 * Message: Stubbornness regardless of the matter will only bear danger.
	 */
	public static final NpcStringId STUBBORNNESS_REGARDLESS_OF_THE_MATTER_WILL_ONLY_BEAR_DANGER;

	/**
	 * ID: 1800498<br>
	 * Message: Keep good manners and value taciturnity as light-heartedness may bring misfortune.
	 */
	public static final NpcStringId KEEP_GOOD_MANNERS_AND_VALUE_TACITURNITY_AS_LIGHT_HEARTEDNESS_MAY_BRING_MISFORTUNE;

	/**
	 * ID: 1800499<br>
	 * Message: You may meet the opposite sex.
	 */
	public static final NpcStringId YOU_MAY_MEET_THE_OPPOSITE_SEX;

	/**
	 * ID: 1800500<br>
	 * Message: Greed by wanting to take wealth may bring unfortunate disaster.
	 */
	public static final NpcStringId GREED_BY_WANTING_TO_TAKE_WEALTH_MAY_BRING_UNFORTUNATE_DISASTER;

	/**
	 * ID: 1800501<br>
	 * Message: Loss is ahead, refrain from investing. Try to save the money in your pockets.
	 */
	public static final NpcStringId LOSS_IS_AHEAD_REFRAIN_FROM_INVESTING_TRY_TO_SAVE_THE_MONEY_IN_YOUR_POCKETS;

	/**
	 * ID: 1800502<br>
	 * Message: Your wealth luck is dim, avoid any offers.
	 */
	public static final NpcStringId YOUR_WEALTH_LUCK_IS_DIM_AVOID_ANY_OFFERS;

	/**
	 * ID: 1800503<br>
	 * Message: A bigger challenge may be when delaying today's work.
	 */
	public static final NpcStringId A_BIGGER_CHALLENGE_MAY_BE_WHEN_DELAYING_TODAYS_WORK;

	/**
	 * ID: 1800504<br>
	 * Message: There will be difficulty, but a good result may be ahead when facing it responsibly.
	 */
	public static final NpcStringId THERE_WILL_BE_DIFFICULTY_BUT_A_GOOD_RESULT_MAY_BE_AHEAD_WHEN_FACING_IT_RESPONSIBLY;

	/**
	 * ID: 1800505<br>
	 * Message: Even with some difficulties, expand the range of your scope where you are in charge. It will return to you as help.
	 */
	public static final NpcStringId
			EVEN_WITH_SOME_DIFFICULTIES_EXPAND_THE_RANGE_OF_YOUR_SCOPE_WHERE_YOU_ARE_IN_CHARGE_IT_WILL_RETURN_TO_YOU_AS_HELP;

	/**
	 * ID: 1800506<br>
	 * Message: Focus on maintaining organized surroundings to help reduce your losses.
	 */
	public static final NpcStringId FOCUS_ON_MAINTAINING_ORGANIZED_SURROUNDINGS_TO_HELP_REDUCE_YOUR_LOSSES;

	/**
	 * ID: 1800507<br>
	 * Message: Luck lies ahead when waiting for people rather than following them.
	 */
	public static final NpcStringId LUCK_LIES_AHEAD_WHEN_WAITING_FOR_PEOPLE_RATHER_THAN_FOLLOWING_THEM;

	/**
	 * ID: 1800508<br>
	 * Message: Do not offer your hand first even when things are hasty. The relationship may fall apart.
	 */
	public static final NpcStringId
			DO_NOT_OFFER_YOUR_HAND_FIRST_EVEN_WHEN_THINGS_ARE_HASTY_THE_RELATIONSHIP_MAY_FALL_APART;

	/**
	 * ID: 1800509<br>
	 * Message: Your wealth luck is rising, there will be some good result.
	 */
	public static final NpcStringId YOUR_WEALTH_LUCK_IS_RISING_THERE_WILL_BE_SOME_GOOD_RESULT;

	/**
	 * ID: 1800510<br>
	 * Message: You may fall in danger each time when acting upon improvisation.
	 */
	public static final NpcStringId YOU_MAY_FALL_IN_DANGER_EACH_TIME_WHEN_ACTING_UPON_IMPROVISATION;

	/**
	 * ID: 1800511<br>
	 * Message: Be warned as a childishly act before elders may ruin everything.
	 */
	public static final NpcStringId BE_WARNED_AS_A_CHILDISHLY_ACT_BEFORE_ELDERS_MAY_RUIN_EVERYTHING;

	/**
	 * ID: 1800512<br>
	 * Message: Things will move effortlessly but luck will vanish with your audacity.
	 */
	public static final NpcStringId THINGS_WILL_MOVE_EFFORTLESSLY_BUT_LUCK_WILL_VANISH_WITH_YOUR_AUDACITY;

	/**
	 * ID: 1800513<br>
	 * Message: Luck may be continued only when humility is maintained after success.
	 */
	public static final NpcStringId LUCK_MAY_BE_CONTINUED_ONLY_WHEN_HUMILITY_IS_MAINTAINED_AFTER_SUCCESS;

	/**
	 * ID: 1800514<br>
	 * Message: A new person may appear to create a love triangle.
	 */
	public static final NpcStringId A_NEW_PERSON_MAY_APPEAR_TO_CREATE_A_LOVE_TRIANGLE;

	/**
	 * ID: 1800515<br>
	 * Message: Look for someone with a similar style. It will open up for the good.
	 */
	public static final NpcStringId LOOK_FOR_SOMEONE_WITH_A_SIMILAR_STYLE_IT_WILL_OPEN_UP_FOR_THE_GOOD;

	/**
	 * ID: 1800516<br>
	 * Message: An offer may soon be made to collaborate a task but delaying it will be a good idea.
	 */
	public static final NpcStringId AN_OFFER_MAY_SOON_BE_MADE_TO_COLLABORATE_A_TASK_BUT_DELAYING_IT_WILL_BE_A_GOOD_IDEA;

	/**
	 * ID: 1800517<br>
	 * Message: Partnership is out of luck, avoid someone who rushes you to start a collaboration.
	 */
	public static final NpcStringId PARTNERSHIP_IS_OUT_OF_LUCK_AVOID_SOMEONE_WHO_RUSHES_YOU_TO_START_A_COLLABORATION;

	/**
	 * ID: 1800518<br>
	 * Message: Focus on networking with like-minded people. They may join you for a big mission in the future.
	 */
	public static final NpcStringId
			FOCUS_ON_NETWORKING_WITH_LIKE_MINDED_PEOPLE_THEY_MAY_JOIN_YOU_FOR_A_BIG_MISSION_IN_THE_FUTURE;

	/**
	 * ID: 1800519<br>
	 * Message: Be warned when someone says you are innocent as that's not a compliment.
	 */
	public static final NpcStringId BE_WARNED_WHEN_SOMEONE_SAYS_YOU_ARE_INNOCENT_AS_THATS_NOT_A_COMPLIMENT;

	/**
	 * ID: 1800520<br>
	 * Message: You may be scammed. Be cautious as there may be a big loss by underestimating others.
	 */
	public static final NpcStringId YOU_MAY_BE_SCAMMED_BE_CAUTIOUS_AS_THERE_MAY_BE_A_BIG_LOSS_BY_UNDERESTIMATING_OTHERS;

	/**
	 * ID: 1800521<br>
	 * Message: Luck at decision-making is dim, avoid subjective conclusions and rely on universal common-sense.
	 */
	public static final NpcStringId
			LUCK_AT_DECISION_MAKING_IS_DIM_AVOID_SUBJECTIVE_CONCLUSIONS_AND_RELY_ON_UNIVERSAL_COMMON_SENSE;

	/**
	 * ID: 1800522<br>
	 * Message: Your weakness may invite hardships, cautiously take a strong position as needed.
	 */
	public static final NpcStringId YOUR_WEAKNESS_MAY_INVITE_HARDSHIPS_CAUTIOUSLY_TAKE_A_STRONG_POSITION_AS_NEEDED;

	/**
	 * ID: 1800523<br>
	 * Message: Be wary of someone who talks and entertains too much. The person may bring you misfortune.
	 */
	public static final NpcStringId
			BE_WARY_OF_SOMEONE_WHO_TALKS_AND_ENTERTAINS_TOO_MUCH_THE_PERSON_MAY_BRING_YOU_MISFORTUNE;

	/**
	 * ID: 1800524<br>
	 * Message: You may enjoy a beginner's luck.
	 */
	public static final NpcStringId YOU_MAY_ENJOY_A_BEGINNERS_LUCK;

	/**
	 * ID: 1800525<br>
	 * Message: Your wealth luck is strong but you should know when to withdraw.
	 */
	public static final NpcStringId YOUR_WEALTH_LUCK_IS_STRONG_BUT_YOU_SHOULD_KNOW_WHEN_TO_WITHDRAW;

	/**
	 * ID: 1800526<br>
	 * Message: Already acquired wealth can be lost by greed.
	 */
	public static final NpcStringId ALREADY_ACQUIRED_WEALTH_CAN_BE_LOST_BY_GREED;

	/**
	 * ID: 1800527<br>
	 * Message: Even if you can complete it by yourself, it's a good idea to have someone help you.
	 */
	public static final NpcStringId EVEN_IF_YOU_CAN_COMPLETE_IT_BY_YOURSELF_ITS_A_GOOD_IDEA_TO_HAVE_SOMEONE_HELP_YOU;

	/**
	 * ID: 1800528<br>
	 * Message: Make harmony with people the priority. Stubbornness may bring hardships.
	 */
	public static final NpcStringId MAKE_HARMONY_WITH_PEOPLE_THE_PRIORITY_STUBBORNNESS_MAY_BRING_HARDSHIPS;

	/**
	 * ID: 1800529<br>
	 * Message: There may be a chance when you can see a new aspect of a close friend.
	 */
	public static final NpcStringId THERE_MAY_BE_A_CHANCE_WHEN_YOU_CAN_SEE_A_NEW_ASPECT_OF_A_CLOSE_FRIEND;

	/**
	 * ID: 1800530<br>
	 * Message: Try to be close to someone different from you without any stereotypical judgment.
	 */
	public static final NpcStringId TRY_TO_BE_CLOSE_TO_SOMEONE_DIFFERENT_FROM_YOU_WITHOUT_ANY_STEREOTYPICAL_JUDGMENT;

	/**
	 * ID: 1800531<br>
	 * Message: Good luck in becoming a leader with many followers. However, it'll only be after hard work.
	 */
	public static final NpcStringId
			GOOD_LUCK_IN_BECOMING_A_LEADER_WITH_MANY_FOLLOWERS_HOWEVER_ITLL_ONLY_BE_AFTER_HARD_WORK;

	/**
	 * ID: 1800532<br>
	 * Message: Your wealth luck is rising, expenditures will be followed by substantial income as you are able to sustain.
	 */
	public static final NpcStringId
			YOUR_WEALTH_LUCK_IS_RISING_EXPENDITURES_WILL_BE_FOLLOWED_BY_SUBSTANTIAL_INCOME_AS_YOU_ARE_ABLE_TO_SUSTAIN;

	/**
	 * ID: 1800533<br>
	 * Message: Be cautious as your wealth luck can be either very good or very bad.
	 */
	public static final NpcStringId BE_CAUTIOUS_AS_YOUR_WEALTH_LUCK_CAN_BE_EITHER_VERY_GOOD_OR_VERY_BAD;

	/**
	 * ID: 1800534<br>
	 * Message: Be warned as a small argument can distance you from a close friend.
	 */
	public static final NpcStringId BE_WARNED_AS_A_SMALL_ARGUMENT_CAN_DISTANCE_YOU_FROM_A_CLOSE_FRIEND;

	/**
	 * ID: 1800535<br>
	 * Message: There is luck in love with a new person.
	 */
	public static final NpcStringId THERE_IS_LUCK_IN_LOVE_WITH_A_NEW_PERSON;

	/**
	 * ID: 1800536<br>
	 * Message: A bigger fortune will be followed by your good deed.
	 */
	public static final NpcStringId A_BIGGER_FORTUNE_WILL_BE_FOLLOWED_BY_YOUR_GOOD_DEED;

	/**
	 * ID: 1800537<br>
	 * Message: There may be a relationship breaking, try to eliminate misunderstandings.
	 */
	public static final NpcStringId THERE_MAY_BE_A_RELATIONSHIP_BREAKING_TRY_TO_ELIMINATE_MISUNDERSTANDINGS;

	/**
	 * ID: 1800538<br>
	 * Message: Be cautious not to be emotionally moved even if it's convincing.
	 */
	public static final NpcStringId BE_CAUTIOUS_NOT_TO_BE_EMOTIONALLY_MOVED_EVEN_IF_ITS_CONVINCING;

	/**
	 * ID: 1800539<br>
	 * Message: Smiling will bring good luck.
	 */
	public static final NpcStringId SMILING_WILL_BRING_GOOD_LUCK;

	/**
	 * ID: 1800540<br>
	 * Message: It's a good idea to let go of a small loss.
	 */
	public static final NpcStringId ITS_A_GOOD_IDEA_TO_LET_GO_OF_A_SMALL_LOSS;

	/**
	 * ID: 1800541<br>
	 * Message: Conveying your own truth may be difficult and easy misunderstandings will follow.
	 */
	public static final NpcStringId CONVEYING_YOUR_OWN_TRUTH_MAY_BE_DIFFICULT_AND_EASY_MISUNDERSTANDINGS_WILL_FOLLOW;

	/**
	 * ID: 1800542<br>
	 * Message: There is good luck in a place with many people.
	 */
	public static final NpcStringId THERE_IS_GOOD_LUCK_IN_A_PLACE_WITH_MANY_PEOPLE;

	/**
	 * ID: 1800543<br>
	 * Message: Try to avoid directness if you can.
	 */
	public static final NpcStringId TRY_TO_AVOID_DIRECTNESS_IF_YOU_CAN;

	/**
	 * ID: 1800544<br>
	 * Message: Value substance opposed to the sake honor and look beyond what's in front of you.
	 */
	public static final NpcStringId VALUE_SUBSTANCE_OPPOSED_TO_THE_SAKE_HONOR_AND_LOOK_BEYOND_WHATS_IN_FRONT_OF_YOU;

	/**
	 * ID: 1800545<br>
	 * Message: Expanding a relationship with humor may be a good idea.
	 */
	public static final NpcStringId EXPANDING_A_RELATIONSHIP_WITH_HUMOR_MAY_BE_A_GOOD_IDEA;

	/**
	 * ID: 1800546<br>
	 * Message: An enjoyable event may be ahead if you accept a simple bet.
	 */
	public static final NpcStringId AN_ENJOYABLE_EVENT_MAY_BE_AHEAD_IF_YOU_ACCEPT_A_SIMPLE_BET;

	/**
	 * ID: 1800547<br>
	 * Message: Being level-headed not focusing on emotions may help with relationships.
	 */
	public static final NpcStringId BEING_LEVEL_HEADED_NOT_FOCUSING_ON_EMOTIONS_MAY_HELP_WITH_RELATIONSHIPS;

	/**
	 * ID: 1800548<br>
	 * Message: It's a good idea to take care of matters in sequential order without measuring their importance.
	 */
	public static final NpcStringId
			ITS_A_GOOD_IDEA_TO_TAKE_CARE_OF_MATTERS_IN_SEQUENTIAL_ORDER_WITHOUT_MEASURING_THEIR_IMPORTANCE;

	/**
	 * ID: 1800549<br>
	 * Message: A determined act after prepared research will attract people.
	 */
	public static final NpcStringId A_DETERMINED_ACT_AFTER_PREPARED_RESEARCH_WILL_ATTRACT_PEOPLE;

	/**
	 * ID: 1800550<br>
	 * Message: A little humor may bring complete attention to you.
	 */
	public static final NpcStringId A_LITTLE_HUMOR_MAY_BRING_COMPLETE_ATTENTION_TO_YOU;

	/**
	 * ID: 1800551<br>
	 * Message: It may not be a good time for an important decision, be wary of temptations and avoid monetary dealings.
	 */
	public static final NpcStringId
			IT_MAY_NOT_BE_A_GOOD_TIME_FOR_AN_IMPORTANT_DECISION_BE_WARY_OF_TEMPTATIONS_AND_AVOID_MONETARY_DEALINGS;

	/**
	 * ID: 1800552<br>
	 * Message: Pay special attention to advice from a close friend.
	 */
	public static final NpcStringId PAY_SPECIAL_ATTENTION_TO_ADVICE_FROM_A_CLOSE_FRIEND;

	/**
	 * ID: 1800553<br>
	 * Message: There may be moderate solutions to every problem when they're viewed from a 3rd party's point of view.
	 */
	public static final NpcStringId
			THERE_MAY_BE_MODERATE_SOLUTIONS_TO_EVERY_PROBLEM_WHEN_THEYRE_VIEWED_FROM_A_3RD_PARTYS_POINT_OF_VIEW;

	/**
	 * ID: 1800554<br>
	 * Message: Dealings with close friends only bring frustration and headache, politely decline and mention another chance.
	 */
	public static final NpcStringId
			DEALINGS_WITH_CLOSE_FRIENDS_ONLY_BRING_FRUSTRATION_AND_HEADACHE_POLITELY_DECLINE_AND_MENTION_ANOTHER_CHANCE;

	/**
	 * ID: 1800555<br>
	 * Message: There may be a problem at completion if the basic matters are not considered from the beginning.
	 */
	public static final NpcStringId
			THERE_MAY_BE_A_PROBLEM_AT_COMPLETION_IF_THE_BASIC_MATTERS_ARE_NOT_CONSIDERED_FROM_THE_BEGINNING;

	/**
	 * ID: 1800556<br>
	 * Message: Distinguishing business from a private matter is needed to succeed.
	 */
	public static final NpcStringId DISTINGUISHING_BUSINESS_FROM_A_PRIVATE_MATTER_IS_NEEDED_TO_SUCCEED;

	/**
	 * ID: 1800557<br>
	 * Message: A change in rules may be helpful when problems are persistent.
	 */
	public static final NpcStringId A_CHANGE_IN_RULES_MAY_BE_HELPFUL_WHEN_PROBLEMS_ARE_PERSISTENT;

	/**
	 * ID: 1800558<br>
	 * Message: Preparing for an unforeseen situation will be difficult when small matters are ignored.
	 */
	public static final NpcStringId
			PREPARING_FOR_AN_UNFORESEEN_SITUATION_WILL_BE_DIFFICULT_WHEN_SMALL_MATTERS_ARE_IGNORED;

	/**
	 * ID: 1800559<br>
	 * Message: Refrain from getting involved in others' business, try to be loose as a goose.
	 */
	public static final NpcStringId REFRAIN_FROM_GETTING_INVOLVED_IN_OTHERS_BUSINESS_TRY_TO_BE_LOOSE_AS_A_GOOSE;

	/**
	 * ID: 1800560<br>
	 * Message: Being neutral is a good way to go, but clarity may be helpful contrary to your hesitance.
	 */
	public static final NpcStringId
			BEING_NEUTRAL_IS_A_GOOD_WAY_TO_GO_BUT_CLARITY_MAY_BE_HELPFUL_CONTRARY_TO_YOUR_HESITANCE;

	/**
	 * ID: 1800561<br>
	 * Message: Be cautious of your own actions, the past may bring misunderstandings.
	 */
	public static final NpcStringId BE_CAUTIOUS_OF_YOUR_OWN_ACTIONS_THE_PAST_MAY_BRING_MISUNDERSTANDINGS;

	/**
	 * ID: 1800562<br>
	 * Message: Pay attention to time management, emotions may waste your time.
	 */
	public static final NpcStringId PAY_ATTENTION_TO_TIME_MANAGEMENT_EMOTIONS_MAY_WASTE_YOUR_TIME;

	/**
	 * ID: 1800563<br>
	 * Message: Heroism will be rewarded, but be careful not to display arrogance or lack of sincerity.
	 */
	public static final NpcStringId
			HEROISM_WILL_BE_REWARDED_BUT_BE_CAREFUL_NOT_TO_DISPLAY_ARROGANCE_OR_LACK_OF_SINCERITY;

	/**
	 * ID: 1800564<br>
	 * Message: If you want to maintain relationship connections, offer reconciliation to those who had misunderstandings with you.
	 */
	public static final NpcStringId
			IF_YOU_WANT_TO_MAINTAIN_RELATIONSHIP_CONNECTIONS_OFFER_RECONCILIATION_TO_THOSE_WHO_HAD_MISUNDERSTANDINGS_WITH_YOU;

	/**
	 * ID: 1800565<br>
	 * Message: Step forward to solve others' problems when they are unable.
	 */
	public static final NpcStringId STEP_FORWARD_TO_SOLVE_OTHERS_PROBLEMS_WHEN_THEY_ARE_UNABLE;

	/**
	 * ID: 1800566<br>
	 * Message: There may be a little loss, but think of it as an investment for yourself.
	 */
	public static final NpcStringId THERE_MAY_BE_A_LITTLE_LOSS_BUT_THINK_OF_IT_AS_AN_INVESTMENT_FOR_YOURSELF;

	/**
	 * ID: 1800567<br>
	 * Message: Avarice bears a bigger greed, being satisfied with moderation is needed.
	 */
	public static final NpcStringId AVARICE_BEARS_A_BIGGER_GREED_BEING_SATISFIED_WITH_MODERATION_IS_NEEDED;

	/**
	 * ID: 1800568<br>
	 * Message: A rational analysis is needed as unplanned actions may bring criticism.
	 */
	public static final NpcStringId A_RATIONAL_ANALYSIS_IS_NEEDED_AS_UNPLANNED_ACTIONS_MAY_BRING_CRITICISM;

	/**
	 * ID: 1800569<br>
	 * Message: Reflect upon your shortcomings before criticizing others.
	 */
	public static final NpcStringId REFLECT_UPON_YOUR_SHORTCOMINGS_BEFORE_CRITICIZING_OTHERS;

	/**
	 * ID: 1800570<br>
	 * Message: Follow-up care is always needed after an emergency evasion.
	 */
	public static final NpcStringId FOLLOW_UP_CARE_IS_ALWAYS_NEEDED_AFTER_AN_EMERGENCY_EVASION;

	/**
	 * ID: 1800571<br>
	 * Message: You may look for a new challenge but vast knowledge is required.
	 */
	public static final NpcStringId YOU_MAY_LOOK_FOR_A_NEW_CHALLENGE_BUT_VAST_KNOWLEDGE_IS_REQUIRED;

	/**
	 * ID: 1800572<br>
	 * Message: When one puts aside their ego any misunderstanding will be solved.
	 */
	public static final NpcStringId WHEN_ONE_PUTS_ASIDE_THEIR_EGO_ANY_MISUNDERSTANDING_WILL_BE_SOLVED;

	/**
	 * ID: 1800573<br>
	 * Message: Listen to the advice that's given to you with a humble attitude.
	 */
	public static final NpcStringId LISTEN_TO_THE_ADVICE_THATS_GIVEN_TO_YOU_WITH_A_HUMBLE_ATTITUDE;

	/**
	 * ID: 1800574<br>
	 * Message: Equilibrium is achieved when one understands a downshift is evident after the rise.
	 */
	public static final NpcStringId EQUILIBRIUM_IS_ACHIEVED_WHEN_ONE_UNDERSTANDS_A_DOWNSHIFT_IS_EVIDENT_AFTER_THE_RISE;

	/**
	 * ID: 1800575<br>
	 * Message: What you sow is what you reap, faithfully follow the plan.
	 */
	public static final NpcStringId WHAT_YOU_SOW_IS_WHAT_YOU_REAP_FAITHFULLY_FOLLOW_THE_PLAN;

	/**
	 * ID: 1800576<br>
	 * Message: Meticulous preparation is needed as spontaneous actions only bear mental and monetary losses.
	 */
	public static final NpcStringId
			METICULOUS_PREPARATION_IS_NEEDED_AS_SPONTANEOUS_ACTIONS_ONLY_BEAR_MENTAL_AND_MONETARY_LOSSES;

	/**
	 * ID: 1800577<br>
	 * Message: The right time to bear fruit is delayed while the farmer ponders opinions.
	 */
	public static final NpcStringId THE_RIGHT_TIME_TO_BEAR_FRUIT_IS_DELAYED_WHILE_THE_FARMER_PONDERS_OPINIONS;

	/**
	 * ID: 1800578<br>
	 * Message: Help each other among close friends.
	 */
	public static final NpcStringId HELP_EACH_OTHER_AMONG_CLOSE_FRIENDS;

	/**
	 * ID: 1800579<br>
	 * Message: Obsessing over a small profit will place people apart.
	 */
	public static final NpcStringId OBSESSING_OVER_A_SMALL_PROFIT_WILL_PLACE_PEOPLE_APART;

	/**
	 * ID: 1800580<br>
	 * Message: Don't cling to the result of a gamble.
	 */
	public static final NpcStringId DONT_CLING_TO_THE_RESULT_OF_A_GAMBLE;

	/**
	 * ID: 1800581<br>
	 * Message: Small troubles and arguments are ahead, face them with a mature attitude.
	 */
	public static final NpcStringId SMALL_TROUBLES_AND_ARGUMENTS_ARE_AHEAD_FACE_THEM_WITH_A_MATURE_ATTITUDE;

	/**
	 * ID: 1800582<br>
	 * Message: Neglecting a promise may put you in distress.
	 */
	public static final NpcStringId NEGLECTING_A_PROMISE_MAY_PUT_YOU_IN_DISTRESS;

	/**
	 * ID: 1800583<br>
	 * Message: Delay any dealings as you may easily omit addressing what's important to you.
	 */
	public static final NpcStringId DELAY_ANY_DEALINGS_AS_YOU_MAY_EASILY_OMIT_ADDRESSING_WHATS_IMPORTANT_TO_YOU;

	/**
	 * ID: 1800584<br>
	 * Message: A comparison to others may be helpful.
	 */
	public static final NpcStringId A_COMPARISON_TO_OTHERS_MAY_BE_HELPFUL;

	/**
	 * ID: 1800585<br>
	 * Message: What you've endured will return as a benefit.
	 */
	public static final NpcStringId WHAT_YOUVE_ENDURED_WILL_RETURN_AS_A_BENEFIT;

	/**
	 * ID: 1800586<br>
	 * Message: Try to be courteous to the opposite sex and follow a virtuous path.
	 */
	public static final NpcStringId TRY_TO_BE_COURTEOUS_TO_THE_OPPOSITE_SEX_AND_FOLLOW_A_VIRTUOUS_PATH;

	/**
	 * ID: 1800587<br>
	 * Message: Joy may come from small things.
	 */
	public static final NpcStringId JOY_MAY_COME_FROM_SMALL_THINGS;

	/**
	 * ID: 1800588<br>
	 * Message: Be confident in your actions as good luck shadows the result.
	 */
	public static final NpcStringId BE_CONFIDENT_IN_YOUR_ACTIONS_AS_GOOD_LUCK_SHADOWS_THE_RESULT;

	/**
	 * ID: 1800589<br>
	 * Message: Be confident without hesitation when your honesty is above reproach in dealings.
	 */
	public static final NpcStringId BE_CONFIDENT_WITHOUT_HESITATION_WHEN_YOUR_HONESTY_IS_ABOVE_REPROACH_IN_DEALINGS;

	/**
	 * ID: 1800590<br>
	 * Message: A matter related to a close friend can isolate you, keep staying on the right path.
	 */
	public static final NpcStringId A_MATTER_RELATED_TO_A_CLOSE_FRIEND_CAN_ISOLATE_YOU_KEEP_STAYING_ON_THE_RIGHT_PATH;

	/**
	 * ID: 1800591<br>
	 * Message: Too much focus on the result may bring continuous misfortune.
	 */
	public static final NpcStringId TOO_MUCH_FOCUS_ON_THE_RESULT_MAY_BRING_CONTINUOUS_MISFORTUNE;

	/**
	 * ID: 1800592<br>
	 * Message: Be tenacious until the finish as halfway abandonment causes a troubled ending.
	 */
	public static final NpcStringId BE_TENACIOUS_UNTIL_THE_FINISH_AS_HALFWAY_ABANDONMENT_CAUSES_A_TROUBLED_ENDING;

	/**
	 * ID: 1800593<br>
	 * Message: There will be no advantage in a group deal.
	 */
	public static final NpcStringId THERE_WILL_BE_NO_ADVANTAGE_IN_A_GROUP_DEAL;

	/**
	 * ID: 1800594<br>
	 * Message: Refrain from stepping-up but take a moment to ponder to be flexible with situations.
	 */
	public static final NpcStringId REFRAIN_FROM_STEPPING_UP_BUT_TAKE_A_MOMENT_TO_PONDER_TO_BE_FLEXIBLE_WITH_SITUATIONS;

	/**
	 * ID: 1800595<br>
	 * Message: There will be a small opportunity when information is best utilized.
	 */
	public static final NpcStringId THERE_WILL_BE_A_SMALL_OPPORTUNITY_WHEN_INFORMATION_IS_BEST_UTILIZED;

	/**
	 * ID: 1800596<br>
	 * Message: Belongings are at loose ends, keep track of the things you value.
	 */
	public static final NpcStringId BELONGINGS_ARE_AT_LOOSE_ENDS_KEEP_TRACK_OF_THE_THINGS_YOU_VALUE;

	/**
	 * ID: 1800597<br>
	 * Message: What you sow is what you reap, try your best.
	 */
	public static final NpcStringId WHAT_YOU_SOW_IS_WHAT_YOU_REAP_TRY_YOUR_BEST;

	/**
	 * ID: 1800598<br>
	 * Message: With the beginner's attitude, shortcomings can be easily mended.
	 */
	public static final NpcStringId WITH_THE_BEGINNERS_ATTITUDE_SHORTCOMINGS_CAN_BE_EASILY_MENDED;

	/**
	 * ID: 1800599<br>
	 * Message: When facing difficulties, seek a totally different direction.
	 */
	public static final NpcStringId WHEN_FACING_DIFFICULTIES_SEEK_A_TOTALLY_DIFFERENT_DIRECTION;

	/**
	 * ID: 1800600<br>
	 * Message: Lifetime savings can disappear with one-time greed.
	 */
	public static final NpcStringId LIFETIME_SAVINGS_CAN_DISAPPEAR_WITH_ONE_TIME_GREED;

	/**
	 * ID: 1800601<br>
	 * Message: With your heart avoid extremes and peace will stay.
	 */
	public static final NpcStringId WITH_YOUR_HEART_AVOID_EXTREMES_AND_PEACE_WILL_STAY;

	/**
	 * ID: 1800602<br>
	 * Message: Be cautious as instant recklessness may bring malicious gossip.
	 */
	public static final NpcStringId BE_CAUTIOUS_AS_INSTANT_RECKLESSNESS_MAY_BRING_MALICIOUS_GOSSIP;

	/**
	 * ID: 1800603<br>
	 * Message: Be tenacious to the end because a strong luck with winning is ahead.
	 */
	public static final NpcStringId BE_TENACIOUS_TO_THE_END_BECAUSE_A_STRONG_LUCK_WITH_WINNING_IS_AHEAD;

	/**
	 * ID: 1800604<br>
	 * Message: Be kind to and care for those close to you, they may help in the future.
	 */
	public static final NpcStringId BE_KIND_TO_AND_CARE_FOR_THOSE_CLOSE_TO_YOU_THEY_MAY_HELP_IN_THE_FUTURE;

	/**
	 * ID: 1800605<br>
	 * Message: Positivity may bring good results.
	 */
	public static final NpcStringId POSITIVITY_MAY_BRING_GOOD_RESULTS;

	/**
	 * ID: 1800606<br>
	 * Message: Be gracious to cover a close friend's fault.
	 */
	public static final NpcStringId BE_GRACIOUS_TO_COVER_A_CLOSE_FRIENDS_FAULT;

	/**
	 * ID: 1800607<br>
	 * Message: Be prepared for an expected cost.
	 */
	public static final NpcStringId BE_PREPARED_FOR_AN_EXPECTED_COST;

	/**
	 * ID: 1800608<br>
	 * Message: Be considerate to others and avoid focusing only on winning or a wound will be left untreated.
	 */
	public static final NpcStringId
			BE_CONSIDERATE_TO_OTHERS_AND_AVOID_FOCUSING_ONLY_ON_WINNING_OR_A_WOUND_WILL_BE_LEFT_UNTREATED;

	/**
	 * ID: 1800609<br>
	 * Message: An accessory or decoration may bring a good luck.
	 */
	public static final NpcStringId AN_ACCESSORY_OR_DECORATION_MAY_BRING_A_GOOD_LUCK;

	/**
	 * ID: 1800610<br>
	 * Message: Only reflection and humility may bring success.
	 */
	public static final NpcStringId ONLY_REFLECTION_AND_HUMILITY_MAY_BRING_SUCCESS;

	/**
	 * ID: 1800611<br>
	 * Message: A small misunderstanding may cause quarrels.
	 */
	public static final NpcStringId A_SMALL_MISUNDERSTANDING_MAY_CAUSE_QUARRELS;

	/**
	 * ID: 1800612<br>
	 * Message: Avoid advancing beyond your ability and focus on the flowing stream.
	 */
	public static final NpcStringId AVOID_ADVANCING_BEYOND_YOUR_ABILITY_AND_FOCUS_ON_THE_FLOWING_STREAM;

	/**
	 * ID: 1800613<br>
	 * Message: Considering others with a good heart before self-interest will bring a triumph.
	 */
	public static final NpcStringId CONSIDERING_OTHERS_WITH_A_GOOD_HEART_BEFORE_SELF_INTEREST_WILL_BRING_A_TRIUMPH;

	/**
	 * ID: 1800614<br>
	 * Message: Visiting a place you've never been before may bring luck.
	 */
	public static final NpcStringId VISITING_A_PLACE_YOUVE_NEVER_BEEN_BEFORE_MAY_BRING_LUCK;

	/**
	 * ID: 1800615<br>
	 * Message: A good thing may happen in a place with a few people.
	 */
	public static final NpcStringId A_GOOD_THING_MAY_HAPPEN_IN_A_PLACE_WITH_A_FEW_PEOPLE;

	/**
	 * ID: 1800616<br>
	 * Message: Being high-strung can cause loss of trust from others because it can be viewed as light-hearted, act sincerely but yet do not lack humor.
	 */
	public static final NpcStringId
			BEING_HIGH_STRUNG_CAN_CAUSE_LOSS_OF_TRUST_FROM_OTHERS_BECAUSE_IT_CAN_BE_VIEWED_AS_LIGHT_HEARTED_ACT_SINCERELY_BUT_YET_DO_NOT_LACK_HUMOR;

	/**
	 * ID: 1800617<br>
	 * Message: Perfection at the finish can cover faulty work in the process.
	 */
	public static final NpcStringId PERFECTION_AT_THE_FINISH_CAN_COVER_FAULTY_WORK_IN_THE_PROCESS;

	/**
	 * ID: 1800618<br>
	 * Message: Abstain from laziness, much work brings many gains and satisfactory rewards.
	 */
	public static final NpcStringId ABSTAIN_FROM_LAZINESS_MUCH_WORK_BRINGS_MANY_GAINS_AND_SATISFACTORY_REWARDS;

	/**
	 * ID: 1800619<br>
	 * Message: Staying busy rather than being stationary will help.
	 */
	public static final NpcStringId STAYING_BUSY_RATHER_THAN_BEING_STATIONARY_WILL_HELP;

	/**
	 * ID: 1800620<br>
	 * Message: Handling the work by yourself may lead you into temptation.
	 */
	public static final NpcStringId HANDLING_THE_WORK_BY_YOURSELF_MAY_LEAD_YOU_INTO_TEMPTATION;

	/**
	 * ID: 1800621<br>
	 * Message: Pay attention to any small advice without being indifferent.
	 */
	public static final NpcStringId PAY_ATTENTION_TO_ANY_SMALL_ADVICE_WITHOUT_BEING_INDIFFERENT;

	/**
	 * ID: 1800622<br>
	 * Message: Small things make up big things so even value trivial matters.
	 */
	public static final NpcStringId SMALL_THINGS_MAKE_UP_BIG_THINGS_SO_EVEN_VALUE_TRIVIAL_MATTERS;

	/**
	 * ID: 1800623<br>
	 * Message: Action toward the result rather than waiting for the right circumstances may lead you to a fast success.
	 */
	public static final NpcStringId
			ACTION_TOWARD_THE_RESULT_RATHER_THAN_WAITING_FOR_THE_RIGHT_CIRCUMSTANCES_MAY_LEAD_YOU_TO_A_FAST_SUCCESS;

	/**
	 * ID: 1800624<br>
	 * Message: Don't try to save small expenditures, it will lead to future returns.
	 */
	public static final NpcStringId DONT_TRY_TO_SAVE_SMALL_EXPENDITURES_IT_WILL_LEAD_TO_FUTURE_RETURNS;

	/**
	 * ID: 1800625<br>
	 * Message: Be cautious to control emotions as temptations are nearby.
	 */
	public static final NpcStringId BE_CAUTIOUS_TO_CONTROL_EMOTIONS_AS_TEMPTATIONS_ARE_NEARBY;

	/**
	 * ID: 1800626<br>
	 * Message: Be warned as neglecting a matter because it's small can cause you trouble.
	 */
	public static final NpcStringId BE_WARNED_AS_NEGLECTING_A_MATTER_BECAUSE_ITS_SMALL_CAN_CAUSE_YOU_TROUBLE;

	/**
	 * ID: 1800627<br>
	 * Message: Spend when needed rather than trying to unconditionally save.
	 */
	public static final NpcStringId SPEND_WHEN_NEEDED_RATHER_THAN_TRYING_TO_UNCONDITIONALLY_SAVE;

	/**
	 * ID: 1800628<br>
	 * Message: Prejudice will take you to a small gain with a big loss.
	 */
	public static final NpcStringId PREJUDICE_WILL_TAKE_YOU_TO_A_SMALL_GAIN_WITH_A_BIG_LOSS;

	/**
	 * ID: 1800629<br>
	 * Message: Sweet food may bring good luck.
	 */
	public static final NpcStringId SWEET_FOOD_MAY_BRING_GOOD_LUCK;

	/**
	 * ID: 1800630<br>
	 * Message: You may be paid for what you're owed or for your past loss.
	 */
	public static final NpcStringId YOU_MAY_BE_PAID_FOR_WHAT_YOURE_OWED_OR_FOR_YOUR_PAST_LOSS;

	/**
	 * ID: 1800631<br>
	 * Message: There may be conflict in basic matters.
	 */
	public static final NpcStringId THERE_MAY_BE_CONFLICT_IN_BASIC_MATTERS;

	/**
	 * ID: 1800632<br>
	 * Message: Be observant to close friends' small behaviors while refraining from excessive kindness.
	 */
	public static final NpcStringId
			BE_OBSERVANT_TO_CLOSE_FRIENDS_SMALL_BEHAVIORS_WHILE_REFRAINING_FROM_EXCESSIVE_KINDNESS;

	/**
	 * ID: 1800633<br>
	 * Message: Do not show your distress nor lose your smile.
	 */
	public static final NpcStringId DO_NOT_SHOW_YOUR_DISTRESS_NOR_LOSE_YOUR_SMILE;

	/**
	 * ID: 1800634<br>
	 * Message: Showing change may be of help.
	 */
	public static final NpcStringId SHOWING_CHANGE_MAY_BE_OF_HELP;

	/**
	 * ID: 1800635<br>
	 * Message: The intended result may be on your way if the time is perfectly managed.
	 */
	public static final NpcStringId THE_INTENDED_RESULT_MAY_BE_ON_YOUR_WAY_IF_THE_TIME_IS_PERFECTLY_MANAGED;

	/**
	 * ID: 1800636<br>
	 * Message: Hardships may arise if flexibility is not well played.
	 */
	public static final NpcStringId HARDSHIPS_MAY_ARISE_IF_FLEXIBILITY_IS_NOT_WELL_PLAYED;

	/**
	 * ID: 1800637<br>
	 * Message: Keep cool headed because carelessness or inattentiveness may cause misfortune.
	 */
	public static final NpcStringId KEEP_COOL_HEADED_BECAUSE_CARELESSNESS_OR_INATTENTIVENESS_MAY_CAUSE_MISFORTUNE;

	/**
	 * ID: 1800638<br>
	 * Message: Be cautious as you may get hurt after last night's sinister dream.
	 */
	public static final NpcStringId BE_CAUTIOUS_AS_YOU_MAY_GET_HURT_AFTER_LAST_NIGHTS_SINISTER_DREAM;

	/**
	 * ID: 1800639<br>
	 * Message: A strong wealth luck is ahead but be careful with emotions that may bring losses.
	 */
	public static final NpcStringId A_STRONG_WEALTH_LUCK_IS_AHEAD_BUT_BE_CAREFUL_WITH_EMOTIONS_THAT_MAY_BRING_LOSSES;

	/**
	 * ID: 1800640<br>
	 * Message: Proceed as you wish when it's pertinent to the person you like.
	 */
	public static final NpcStringId PROCEED_AS_YOU_WISH_WHEN_ITS_PERTINENT_TO_THE_PERSON_YOU_LIKE;

	/**
	 * ID: 1800641<br>
	 * Message: You may deepen the relationship with the opposite sex through conversation.
	 */
	public static final NpcStringId YOU_MAY_DEEPEN_THE_RELATIONSHIP_WITH_THE_OPPOSITE_SEX_THROUGH_CONVERSATION;

	/**
	 * ID: 1800642<br>
	 * Message: Investment into solid material may bring profit.
	 */
	public static final NpcStringId INVESTMENT_INTO_SOLID_MATERIAL_MAY_BRING_PROFIT;

	/**
	 * ID: 1800643<br>
	 * Message: Investment into what you enjoy may be of help.
	 */
	public static final NpcStringId INVESTMENT_INTO_WHAT_YOU_ENJOY_MAY_BE_OF_HELP;

	/**
	 * ID: 1800644<br>
	 * Message: Being busy may help catching up with many changes.
	 */
	public static final NpcStringId BEING_BUSY_MAY_HELP_CATCHING_UP_WITH_MANY_CHANGES;

	/**
	 * ID: 1800645<br>
	 * Message: Choose substance over honor.
	 */
	public static final NpcStringId CHOOSE_SUBSTANCE_OVER_HONOR;

	/**
	 * ID: 1800646<br>
	 * Message: Remember to decline any financial dealings because a good deed may return as resentment.
	 */
	public static final NpcStringId
			REMEMBER_TO_DECLINE_ANY_FINANCIAL_DEALINGS_BECAUSE_A_GOOD_DEED_MAY_RETURN_AS_RESENTMENT;

	/**
	 * ID: 1800647<br>
	 * Message: Be careful not to make a mistake with a new person.
	 */
	public static final NpcStringId BE_CAREFUL_NOT_TO_MAKE_A_MISTAKE_WITH_A_NEW_PERSON;

	/**
	 * ID: 1800648<br>
	 * Message: Do not be obsessive over a dragged out project since it won't get any better with more time.
	 */
	public static final NpcStringId
			DO_NOT_BE_OBSESSIVE_OVER_A_DRAGGED_OUT_PROJECT_SINCE_IT_WONT_GET_ANY_BETTER_WITH_MORE_TIME;

	/**
	 * ID: 1800649<br>
	 * Message: Do not yield what's rightfully yours or tolerate losses.
	 */
	public static final NpcStringId DO_NOT_YIELD_WHATS_RIGHTFULLY_YOURS_OR_TOLERATE_LOSSES;

	/**
	 * ID: 1800650<br>
	 * Message: There's luck in relationships so become interested in the opposite sex.
	 */
	public static final NpcStringId THERES_LUCK_IN_RELATIONSHIPS_SO_BECOME_INTERESTED_IN_THE_OPPOSITE_SEX;

	/**
	 * ID: 1800651<br>
	 * Message: Seeking others' help rather than trying by yourself may result in two birds with one stone.
	 */
	public static final NpcStringId
			SEEKING_OTHERS_HELP_RATHER_THAN_TRYING_BY_YOURSELF_MAY_RESULT_IN_TWO_BIRDS_WITH_ONE_STONE;

	/**
	 * ID: 1800652<br>
	 * Message: Persuading the other may result in your gain.
	 */
	public static final NpcStringId PERSUADING_THE_OTHER_MAY_RESULT_IN_YOUR_GAIN;

	/**
	 * ID: 1800653<br>
	 * Message: A good opportunity may come when keeping patience without excessiveness.
	 */
	public static final NpcStringId A_GOOD_OPPORTUNITY_MAY_COME_WHEN_KEEPING_PATIENCE_WITHOUT_EXCESSIVENESS;

	/**
	 * ID: 1800654<br>
	 * Message: The opposite sex may bring fortune.
	 */
	public static final NpcStringId THE_OPPOSITE_SEX_MAY_BRING_FORTUNE;

	/**
	 * ID: 1800655<br>
	 * Message: Doing favor for other people may bring fortune in the future.
	 */
	public static final NpcStringId DOING_FAVOR_FOR_OTHER_PEOPLE_MAY_BRING_FORTUNE_IN_THE_FUTURE;

	/**
	 * ID: 1800656<br>
	 * Message: Luck may stay near if a smile is kept during difficult times.
	 */
	public static final NpcStringId LUCK_MAY_STAY_NEAR_IF_A_SMILE_IS_KEPT_DURING_DIFFICULT_TIMES;

	/**
	 * ID: 1800657<br>
	 * Message: You may reveal your true self like iron is molten into an strong sword.
	 */
	public static final NpcStringId YOU_MAY_REVEAL_YOUR_TRUE_SELF_LIKE_IRON_IS_MOLTEN_INTO_AN_STRONG_SWORD;

	/**
	 * ID: 1800658<br>
	 * Message: Your value will shine as your potential is finally realized.
	 */
	public static final NpcStringId YOUR_VALUE_WILL_SHINE_AS_YOUR_POTENTIAL_IS_FINALLY_REALIZED;

	/**
	 * ID: 1800659<br>
	 * Message: Tenacious efforts in solving a difficult mission or hardship may bring good results as well as realizing your hidden potential.
	 */
	public static final NpcStringId
			TENACIOUS_EFFORTS_IN_SOLVING_A_DIFFICULT_MISSION_OR_HARDSHIP_MAY_BRING_GOOD_RESULTS_AS_WELL_AS_REALIZING_YOUR_HIDDEN_POTENTIAL;

	/**
	 * ID: 1800660<br>
	 * Message: People will appreciate your positivity and joyful entertaining.
	 */
	public static final NpcStringId PEOPLE_WILL_APPRECIATE_YOUR_POSITIVITY_AND_JOYFUL_ENTERTAINING;

	/**
	 * ID: 1800661<br>
	 * Message: Things will move smoothly with your full wisdom and abilities.
	 */
	public static final NpcStringId THINGS_WILL_MOVE_SMOOTHLY_WITH_YOUR_FULL_WISDOM_AND_ABILITIES;

	/**
	 * ID: 1800662<br>
	 * Message: You may meet a sage who can help you find the right path.
	 */
	public static final NpcStringId YOU_MAY_MEET_A_SAGE_WHO_CAN_HELP_YOU_FIND_THE_RIGHT_PATH;

	/**
	 * ID: 1800663<br>
	 * Message: Keen instinct and foresight will shine their values.
	 */
	public static final NpcStringId KEEN_INSTINCT_AND_FORESIGHT_WILL_SHINE_THEIR_VALUES;

	/**
	 * ID: 1800664<br>
	 * Message: You may bring good luck to those around you.
	 */
	public static final NpcStringId YOU_MAY_BRING_GOOD_LUCK_TO_THOSE_AROUND_YOU;

	/**
	 * ID: 1800665<br>
	 * Message: Your goal may be realized when emotional details are well defined.
	 */
	public static final NpcStringId YOUR_GOAL_MAY_BE_REALIZED_WHEN_EMOTIONAL_DETAILS_ARE_WELL_DEFINED;

	/**
	 * ID: 1800666<br>
	 * Message: You may enjoy affluence after meeting a precious person.
	 */
	public static final NpcStringId YOU_MAY_ENJOY_AFFLUENCE_AFTER_MEETING_A_PRECIOUS_PERSON;

	/**
	 * ID: 1800667<br>
	 * Message: You may meet the opposite sex who has materialistic attractions.
	 */
	public static final NpcStringId YOU_MAY_MEET_THE_OPPOSITE_SEX_WHO_HAS_MATERIALISTIC_ATTRACTIONS;

	/**
	 * ID: 1800668<br>
	 * Message: A big success will follow all possible efforts in competition.
	 */
	public static final NpcStringId A_BIG_SUCCESS_WILL_FOLLOW_ALL_POSSIBLE_EFFORTS_IN_COMPETITION;

	/**
	 * ID: 1800669<br>
	 * Message: A consequence from past actions will be on display.
	 */
	public static final NpcStringId A_CONSEQUENCE_FROM_PAST_ACTIONS_WILL_BE_ON_DISPLAY;

	/**
	 * ID: 1800670<br>
	 * Message: Whatever happened to you and the other person will replay, but this time, the opposite will be the result.
	 */
	public static final NpcStringId
			WHATEVER_HAPPENED_TO_YOU_AND_THE_OTHER_PERSON_WILL_REPLAY_BUT_THIS_TIME_THE_OPPOSITE_WILL_BE_THE_RESULT;

	/**
	 * ID: 1800671<br>
	 * Message: You may need to sacrifice for a higher cause.
	 */
	public static final NpcStringId YOU_MAY_NEED_TO_SACRIFICE_FOR_A_HIGHER_CAUSE;

	/**
	 * ID: 1800672<br>
	 * Message: You may lose an item but will gain honor.
	 */
	public static final NpcStringId YOU_MAY_LOSE_AN_ITEM_BUT_WILL_GAIN_HONOR;

	/**
	 * ID: 1800673<br>
	 * Message: A new trial or start may be successful as luck shadows changes.
	 */
	public static final NpcStringId A_NEW_TRIAL_OR_START_MAY_BE_SUCCESSFUL_AS_LUCK_SHADOWS_CHANGES;

	/**
	 * ID: 1800674<br>
	 * Message: Be sophisticated without showing your true emotions as tricks and materialistic temptations lie ahead.
	 */
	public static final NpcStringId
			BE_SOPHISTICATED_WITHOUT_SHOWING_YOUR_TRUE_EMOTIONS_AS_TRICKS_AND_MATERIALISTIC_TEMPTATIONS_LIE_AHEAD;

	/**
	 * ID: 1800675<br>
	 * Message: Do not attempt a dangerous adventure.
	 */
	public static final NpcStringId DO_NOT_ATTEMPT_A_DANGEROUS_ADVENTURE;

	/**
	 * ID: 1800676<br>
	 * Message: Do not be afraid of change. A risk will be another opportunity.
	 */
	public static final NpcStringId DO_NOT_BE_AFRAID_OF_CHANGE_A_RISK_WILL_BE_ANOTHER_OPPORTUNITY;

	/**
	 * ID: 1800677<br>
	 * Message: Be confident and act tenaciously at all times. You may be able to accomplish to perfection during somewhat unstable situations.
	 */
	public static final NpcStringId
			BE_CONFIDENT_AND_ACT_TENACIOUSLY_AT_ALL_TIMES_YOU_MAY_BE_ABLE_TO_ACCOMPLISH_TO_PERFECTION_DURING_SOMEWHAT_UNSTABLE_SITUATIONS;

	/**
	 * ID: 1800678<br>
	 * Message: You may expect a bright and hopeful future.
	 */
	public static final NpcStringId YOU_MAY_EXPECT_A_BRIGHT_AND_HOPEFUL_FUTURE;

	/**
	 * ID: 1800679<br>
	 * Message: A rest will promise a bigger development.
	 */
	public static final NpcStringId A_REST_WILL_PROMISE_A_BIGGER_DEVELOPMENT;

	/**
	 * ID: 1800680<br>
	 * Message: Fully utilize positive views.
	 */
	public static final NpcStringId FULLY_UTILIZE_POSITIVE_VIEWS;

	/**
	 * ID: 1800681<br>
	 * Message: Positive thinking and energetic actions will take you to the center of the glorious stage.
	 */
	public static final NpcStringId
			POSITIVE_THINKING_AND_ENERGETIC_ACTIONS_WILL_TAKE_YOU_TO_THE_CENTER_OF_THE_GLORIOUS_STAGE;

	/**
	 * ID: 1800682<br>
	 * Message: Your self confidence and intuition may solve the difficulties.
	 */
	public static final NpcStringId YOUR_SELF_CONFIDENCE_AND_INTUITION_MAY_SOLVE_THE_DIFFICULTIES;

	/**
	 * ID: 1800683<br>
	 * Message: Everything is brilliant and joyful, share it with others. A bigger fortune will follow.
	 */
	public static final NpcStringId
			EVERYTHING_IS_BRILLIANT_AND_JOYFUL_SHARE_IT_WITH_OTHERS_A_BIGGER_FORTUNE_WILL_FOLLOW;

	/**
	 * ID: 1800684<br>
	 * Message: A fair assessment and reward for past actions lie ahead.
	 */
	public static final NpcStringId A_FAIR_ASSESSMENT_AND_REWARD_FOR_PAST_ACTIONS_LIE_AHEAD;

	/**
	 * ID: 1800685<br>
	 * Message: Pay accurately the old liability or debt, if applicable. A new joy lies ahead.
	 */
	public static final NpcStringId PAY_ACCURATELY_THE_OLD_LIABILITY_OR_DEBT_IF_APPLICABLE_A_NEW_JOY_LIES_AHEAD;

	/**
	 * ID: 1800686<br>
	 * Message: An excessive humility can harm you back.
	 */
	public static final NpcStringId AN_EXCESSIVE_HUMILITY_CAN_HARM_YOU_BACK;

	/**
	 * ID: 1800687<br>
	 * Message: A reward for the past work will come through.
	 */
	public static final NpcStringId A_REWARD_FOR_THE_PAST_WORK_WILL_COME_THROUGH;

	/**
	 * ID: 1800688<br>
	 * Message: Your past fruitless effort will finally be rewarded with something unexpected.
	 */
	public static final NpcStringId YOUR_PAST_FRUITLESS_EFFORT_WILL_FINALLY_BE_REWARDED_WITH_SOMETHING_UNEXPECTED;

	/**
	 * ID: 1800689<br>
	 * Message: There's strong luck in a revival, abandon the old and create the new.
	 */
	public static final NpcStringId THERES_STRONG_LUCK_IN_A_REVIVAL_ABANDON_THE_OLD_AND_CREATE_THE_NEW;

	/**
	 * ID: 1800690<br>
	 * Message: You may gain materialistic or mental aid from close friends.
	 */
	public static final NpcStringId YOU_MAY_GAIN_MATERIALISTIC_OR_MENTAL_AID_FROM_CLOSE_FRIENDS;

	/**
	 * ID: 1800691<br>
	 * Message: A good beginning is awaiting you.
	 */
	public static final NpcStringId A_GOOD_BEGINNING_IS_AWAITING_YOU;

	/**
	 * ID: 1800692<br>
	 * Message: You may meet the person you've longed to see.
	 */
	public static final NpcStringId YOU_MAY_MEET_THE_PERSON_YOUVE_LONGED_TO_SEE;

	/**
	 * ID: 1800693<br>
	 * Message: You may sustain a loss due to your kindness.
	 */
	public static final NpcStringId YOU_MAY_SUSTAIN_A_LOSS_DUE_TO_YOUR_KINDNESS;

	/**
	 * ID: 1800694<br>
	 * Message: Closely observe people who pass by since you may meet a precious person who can help you.
	 */
	public static final NpcStringId
			CLOSELY_OBSERVE_PEOPLE_WHO_PASS_BY_SINCE_YOU_MAY_MEET_A_PRECIOUS_PERSON_WHO_CAN_HELP_YOU;

	/**
	 * ID: 1800695<br>
	 * Message: Messenger, inform the patrons of the Keucereus Alliance Base! We're gathering brave adventurers to attack Tiat's Mounted Troop that's rooted in the Seed of Destruction.
	 */
	public static final NpcStringId
			MESSENGER_INFORM_THE_PATRONS_OF_THE_KEUCEREUS_ALLIANCE_BASE_WERE_GATHERING_BRAVE_ADVENTURERS_TO_ATTACK_TIATS_MOUNTED_TROOP_THATS_ROOTED_IN_THE_SEED_OF_DESTRUCTION;

	/**
	 * ID: 1800696<br>
	 * Message: Messenger, inform the patrons of the Keucereus Alliance Base! The Seed of Destruction is currently secured under the flag of the Keucereus Alliance!
	 */
	public static final NpcStringId
			MESSENGER_INFORM_THE_PATRONS_OF_THE_KEUCEREUS_ALLIANCE_BASE_THE_SEED_OF_DESTRUCTION_IS_CURRENTLY_SECURED_UNDER_THE_FLAG_OF_THE_KEUCEREUS_ALLIANCE;

	/**
	 * ID: 1800697<br>
	 * Message: Messenger, inform the patrons of the Keucereus Alliance Base! Tiat's Mounted Troop is currently trying to retake Seed of Destruction! Commit all the available reinforcements into Seed of Destruction!
	 */
	public static final NpcStringId
			MESSENGER_INFORM_THE_PATRONS_OF_THE_KEUCEREUS_ALLIANCE_BASE_TIATS_MOUNTED_TROOP_IS_CURRENTLY_TRYING_TO_RETAKE_SEED_OF_DESTRUCTION_COMMIT_ALL_THE_AVAILABLE_REINFORCEMENTS_INTO_SEED_OF_DESTRUCTION;

	/**
	 * ID: 1800698<br>
	 * Message: Messenger, inform the brothers in Kucereus' clan outpost! Brave adventurers who have challenged the Seed of Infinity are currently infiltrating the Hall of Erosion through the defensively weak Hall of Suffering!
	 */
	public static final NpcStringId
			MESSENGER_INFORM_THE_BROTHERS_IN_KUCEREUS_CLAN_OUTPOST_BRAVE_ADVENTURERS_WHO_HAVE_CHALLENGED_THE_SEED_OF_INFINITY_ARE_CURRENTLY_INFILTRATING_THE_HALL_OF_EROSION_THROUGH_THE_DEFENSIVELY_WEAK_HALL_OF_SUFFERING;

	/**
	 * ID: 1800699<br>
	 * Message: Messenger, inform the brothers in Kucereus' clan outpost! Brave adventurers who have challenged the Seed of Infinity are currently infiltrating the Hall of Erosion through the defensively weak Hall of Suffering!
	 */
	public static final NpcStringId
			MESSENGER_INFORM_THE_BROTHERS_IN_KUCEREUS_CLAN_OUTPOST_SWEEPING_THE_SEED_OF_INFINITY_IS_CURRENTLY_COMPLETE_TO_THE_HEART_OF_THE_SEED_EKIMUS_IS_BEING_DIRECTLY_ATTACKED_AND_THE_UNDEAD_REMAINING_IN_THE_HALL_OF_SUFFERING_ARE_BEING_ERADICATED;

	/**
	 * ID: 1800700<br>
	 * Message: Messenger, inform the patrons of the Keucereus Alliance Base! The Seed of Infinity is currently secured under the flag of the Keucereus Alliance!
	 */
	public static final NpcStringId
			MESSENGER_INFORM_THE_PATRONS_OF_THE_KEUCEREUS_ALLIANCE_BASE_THE_SEED_OF_INFINITY_IS_CURRENTLY_SECURED_UNDER_THE_FLAG_OF_THE_KEUCEREUS_ALLIANCE;

	/**
	 * ID: 1800701<br>
	 * Message:
	 */
	public static final NpcStringId NOTHING;

	/**
	 * ID: 1800702<br>
	 * Message: Messenger, inform the patrons of the Keucereus Alliance Base! The resurrected Undead in the Seed of Infinity are pouring into the Hall of Suffering and the Hall of Erosion!
	 */
	public static final NpcStringId
			MESSENGER_INFORM_THE_PATRONS_OF_THE_KEUCEREUS_ALLIANCE_BASE_THE_RESURRECTED_UNDEAD_IN_THE_SEED_OF_INFINITY_ARE_POURING_INTO_THE_HALL_OF_SUFFERING_AND_THE_HALL_OF_EROSION;

	/**
	 * ID: 1800703<br>
	 * Message: Messenger, inform the brothers in Kucereus' clan outpost! Ekimus is about to be revived by the resurrected Undead in Seed of Infinity. Send all reinforcements to the Heart and the Hall of Suffering!
	 */
	public static final NpcStringId
			MESSENGER_INFORM_THE_BROTHERS_IN_KUCEREUS_CLAN_OUTPOST_EKIMUS_IS_ABOUT_TO_BE_REVIVED_BY_THE_RESURRECTED_UNDEAD_IN_SEED_OF_INFINITY_SEND_ALL_REINFORCEMENTS_TO_THE_HEART_AND_THE_HALL_OF_SUFFERING;

	/**
	 * ID: 1800704<br>
	 * Message: Stabbing three times!
	 */
	public static final NpcStringId STABBING_THREE_TIMES;

	/**
	 * ID: 1800705<br>
	 * Message: Poor creatures, feel the power of darkness!
	 */
	public static final NpcStringId POOR_CREATURES_FEEL_THE_POWER_OF_DARKNESS;

	/**
	 * ID: 1800706<br>
	 * Message: Whoaaaaaa!!!!
	 */
	public static final NpcStringId WHOAAAAAA;

	/**
	 * ID: 1800707<br>
	 * Message: You'll regret challenging me!!!!
	 */
	public static final NpcStringId YOULL_REGRET_CHALLENGING_ME;

	/**
	 * ID: 1800708<br>
	 * Message: It's currently occupied by the enemy and our troops are attacking.
	 */
	public static final NpcStringId ITS_CURRENTLY_OCCUPIED_BY_THE_ENEMY_AND_OUR_TROOPS_ARE_ATTACKING;

	/**
	 * ID: 1800709<br>
	 * Message: It's under occupation by our forces, and I heard that Kucereus' clan is organizing the remnants.
	 */
	public static final NpcStringId
			ITS_UNDER_OCCUPATION_BY_OUR_FORCES_AND_I_HEARD_THAT_KUCEREUS_CLAN_IS_ORGANIZING_THE_REMNANTS;

	/**
	 * ID: 1800710<br>
	 * Message: Although we currently have control of it, the enemy is pushing back with a powerful attack.
	 */
	public static final NpcStringId
			ALTHOUGH_WE_CURRENTLY_HAVE_CONTROL_OF_IT_THE_ENEMY_IS_PUSHING_BACK_WITH_A_POWERFUL_ATTACK;

	/**
	 * ID: 1800711<br>
	 * Message: It's under the enemy's occupation, and the military forces of adventurers and clan members are unleashing an onslaught upon the Hall of Suffering and the Hall of Erosion.
	 */
	public static final NpcStringId
			ITS_UNDER_THE_ENEMYS_OCCUPATION_AND_THE_MILITARY_FORCES_OF_ADVENTURERS_AND_CLAN_MEMBERS_ARE_UNLEASHING_AN_ONSLAUGHT_UPON_THE_HALL_OF_SUFFERING_AND_THE_HALL_OF_EROSION;

	/**
	 * ID: 1800713<br>
	 * Message: Our forces have occupied it and are currently investigating the depths.
	 */
	public static final NpcStringId OUR_FORCES_HAVE_OCCUPIED_IT_AND_ARE_CURRENTLY_INVESTIGATING_THE_DEPTHS;

	/**
	 * ID: 1800714<br>
	 * Message: It's under occupation by our forces, but the enemy has resurrected and is attacking toward the Hall of Suffering and the Hall of Erosion.
	 */
	public static final NpcStringId
			ITS_UNDER_OCCUPATION_BY_OUR_FORCES_BUT_THE_ENEMY_HAS_RESURRECTED_AND_IS_ATTACKING_TOWARD_THE_HALL_OF_SUFFERING_AND_THE_HALL_OF_EROSION;

	/**
	 * ID: 1800715<br>
	 * Message: It's under occupation by our forces, but the enemy has already overtaken the Hall of Erosion and is driving out our forces from the Hall of Suffering toward the Heart. It seems that Ekimus will revive shortly.
	 */
	public static final NpcStringId
			ITS_UNDER_OCCUPATION_BY_OUR_FORCES_BUT_THE_ENEMY_HAS_ALREADY_OVERTAKEN_THE_HALL_OF_EROSION_AND_IS_DRIVING_OUT_OUR_FORCES_FROM_THE_HALL_OF_SUFFERING_TOWARD_THE_HEART_IT_SEEMS_THAT_EKIMUS_WILL_REVIVE_SHORTLY;

	/**
	 * ID: 1800717<br>
	 * Message: Tiat's followers are coming to retake the Seed of Destruction! Get ready to stop the enemies!
	 */
	public static final NpcStringId
			TIATS_FOLLOWERS_ARE_COMING_TO_RETAKE_THE_SEED_OF_DESTRUCTION_GET_READY_TO_STOP_THE_ENEMIES;

	/**
	 * ID: 1800718<br>
	 * Message: It's hurting... I'm in pain... What can I do for the pain...
	 */
	public static final NpcStringId ITS_HURTING_IM_IN_PAIN_WHAT_CAN_I_DO_FOR_THE_PAIN;

	/**
	 * ID: 1800719<br>
	 * Message: No... When I lose that one... I'll be in more pain...
	 */
	public static final NpcStringId NO_WHEN_I_LOSE_THAT_ONE_ILL_BE_IN_MORE_PAIN;

	/**
	 * ID: 1800720<br>
	 * Message: Hahahah!!! I captured Santa Claus!! There will be no gifts this year!!!
	 */
	public static final NpcStringId HAHAHAH_I_CAPTURED_SANTA_CLAUS_THERE_WILL_BE_NO_GIFTS_THIS_YEAR;

	/**
	 * ID: 1800721<br>
	 * Message: Now! Why don't you take up the challenge?
	 */
	public static final NpcStringId NOW_WHY_DONT_YOU_TAKE_UP_THE_CHALLENGE;

	/**
	 * ID: 1800722<br>
	 * Message: Come on, I'll take all of you on!
	 */
	public static final NpcStringId COME_ON_ILL_TAKE_ALL_OF_YOU_ON;

	/**
	 * ID: 1800723<br>
	 * Message: How about it? I think I won?
	 */
	public static final NpcStringId HOW_ABOUT_IT_I_THINK_I_WON;

	/**
	 * ID: 1800724<br>
	 * Message: Now!! Those of you who lost, go away!
	 */
	public static final NpcStringId NOW_THOSE_OF_YOU_WHO_LOST_GO_AWAY;

	/**
	 * ID: 1800725<br>
	 * Message: What a bunch of losers.
	 */
	public static final NpcStringId WHAT_A_BUNCH_OF_LOSERS;

	/**
	 * ID: 1800726<br>
	 * Message: I guess you came to rescue Santa. But you picked the wrong person.
	 */
	public static final NpcStringId I_GUESS_YOU_CAME_TO_RESCUE_SANTA_BUT_YOU_PICKED_THE_WRONG_PERSON;

	/**
	 * ID: 1800727<br>
	 * Message: Ah, okay...
	 */
	public static final NpcStringId AH_OKAY;

	/**
	 * ID: 1800728<br>
	 * Message: Agh!! I wasn't going to do that!
	 */
	public static final NpcStringId UAGH_I_WASNT_GOING_TO_DO_THAT;

	/**
	 * ID: 1800729<br>
	 * Message: You're cursed!! Oh.. What?
	 */
	public static final NpcStringId YOURE_CURSED_OH_WHAT;

	/**
	 * ID: 1800730<br>
	 * Message: Have you done nothing but rock-paper-scissors??
	 */
	public static final NpcStringId HAVE_YOU_DONE_NOTHING_BUT_ROCK_PAPER_SCISSORS;

	/**
	 * ID: 1800731<br>
	 * Message: Stop it, no more... I did it because I was too lonely...
	 */
	public static final NpcStringId STOP_IT_NO_MORE_I_DID_IT_BECAUSE_I_WAS_TOO_LONELY;

	/**
	 * ID: 1800732<br>
	 * Message: I have to release Santa... How infuriating!!!
	 */
	public static final NpcStringId I_HAVE_TO_RELEASE_SANTA_HOW_INFURIATING;

	/**
	 * ID: 1800733<br>
	 * Message: I hate happy Merry Christmas!!!
	 */
	public static final NpcStringId I_HATE_HAPPY_MERRY_CHRISTMAS;

	/**
	 * ID: 1800734<br>
	 * Message: Oh. I'm bored.
	 */
	public static final NpcStringId OH_IM_BORED;

	/**
	 * ID: 1800735<br>
	 * Message: Shall I go to take a look if Santa is still there? Hehe
	 */
	public static final NpcStringId SHALL_I_GO_TO_TAKE_A_LOOK_IF_SANTA_IS_STILL_THERE_HEHE;

	/**
	 * ID: 1800736<br>
	 * Message: Oh ho ho.... Merry Christmas!!
	 */
	public static final NpcStringId OH_HO_HO_MERRY_CHRISTMAS;

	/**
	 * ID: 1800737<br>
	 * Message: Santa could give nice presents only if he's released from the Turkey...
	 */
	public static final NpcStringId SANTA_COULD_GIVE_NICE_PRESENTS_ONLY_IF_HES_RELEASED_FROM_THE_TURKEY;

	/**
	 * ID: 1800738<br>
	 * Message: Oh ho ho... Oh ho ho... Thank you. Ladies and gentlemen!  I will repay you for sure.
	 */
	public static final NpcStringId OH_HO_HO_OH_HO_HO_THANK_YOU_LADIES_AND_GENTLEMEN_I_WILL_REPAY_YOU_FOR_SURE;

	/**
	 * ID: 1800739<br>
	 * Message: Merry Christmas~ You're doing a good job.
	 */
	public static final NpcStringId UMERRY_CHRISTMAS_YOURE_DOING_A_GOOD_JOB;

	/**
	 * ID: 1800740<br>
	 * Message: Merry Christmas~ Thank you for rescuing me from that wretched Turkey.
	 */
	public static final NpcStringId MERRY_CHRISTMAS_THANK_YOU_FOR_RESCUING_ME_FROM_THAT_WRETCHED_TURKEY;

	/**
	 * ID: 1800741<br>
	 * Message: $s1 . I have prepared a gift for you.
	 */
	public static final NpcStringId S1_I_HAVE_PREPARED_A_GIFT_FOR_YOU;

	/**
	 * ID: 1800742<br>
	 * Message: I have a gift for $s1.
	 */
	public static final NpcStringId I_HAVE_A_GIFT_FOR_S1;

	/**
	 * ID: 1800743<br>
	 * Message: Take a look at the inventory. I hope you like the gift I gave you.
	 */
	public static final NpcStringId TAKE_A_LOOK_AT_THE_INVENTORY_I_HOPE_YOU_LIKE_THE_GIFT_I_GAVE_YOU;

	/**
	 * ID: 1800744<br>
	 * Message: Take a look at the inventory. Perhaps there might be a big present~
	 */
	public static final NpcStringId TAKE_A_LOOK_AT_THE_INVENTORY_PERHAPS_THERE_MIGHT_BE_A_BIG_PRESENT;

	/**
	 * ID: 1800745<br>
	 * Message: I'm tired of dealing with you. I'm leaving.
	 */
	public static final NpcStringId IM_TIRED_OF_DEALING_WITH_YOU_IM_LEAVING;

	/**
	 * ID: 1800746<br>
	 * Message: When are you going to stop? I slowly started to be tired of it.
	 */
	public static final NpcStringId WHEN_ARE_YOU_GOING_TO_STOP_I_SLOWLY_STARTED_TO_BE_TIRED_OF_IT;

	/**
	 * ID: 1800747<br>
	 * Message: Message from Santa Claus: Many blessings to $s1, who saved me~
	 */
	public static final NpcStringId MESSAGE_FROM_SANTA_CLAUS_MANY_BLESSINGS_TO_S1_WHO_SAVED_ME;

	/**
	 * ID: 1800748<br>
	 * Message: I am already dead. You cannot kill me again...
	 */
	public static final NpcStringId I_AM_ALREADY_DEAD_YOU_CANNOT_KILL_ME_AGAIN;

	/**
	 * ID: 1800749<br>
	 * Message: Oh followers of the Dragon of Darkness, fight by my side!
	 */
	public static final NpcStringId OH_FOLLOWERS_OF_THE_DRAGON_OF_DARKNESS_FIGHT_BY_MY_SIDE;

	/**
	 * ID: 1800750<br>
	 * Message: The Dragon Race... are invading... Prepare... for battle...
	 */
	public static final NpcStringId THE_DRAGON_RACE_ARE_INVADING_PREPARE_FOR_BATTLE;

	/**
	 * ID: 1800751<br>
	 * Message: $s1 rescued Santa Claus of $s2 territory from the turkey.
	 */
	public static final NpcStringId S1_RESCUED_SANTA_CLAUS_OF_S2_TERRITORY_FROM_THE_TURKEY;

	/**
	 * ID: 1800752<br>
	 * Message: Santa Rescue Success!
	 */
	public static final NpcStringId SANTA_RESCUE_SUCCESS;

	/**
	 * ID: 1800753<br>
	 * Message: $s1 received +$s2 $s3 through the weapon exchange coupon.
	 */
	public static final NpcStringId S1_RECEIVED_S2_S3_THROUGH_THE_WEAPON_EXCHANGE_COUPON;

	/**
	 * ID: 1800754<br>
	 * Message: Don't go prattling on!
	 */
	public static final NpcStringId DONT_GO_PRATTLING_ON;

	/**
	 * ID: 1800755<br>
	 * Message: You lowlifes with not even an ounce of pride! You're not worthy of opposing me!
	 */
	public static final NpcStringId YOU_LOWLIFES_WITH_NOT_EVEN_AN_OUNCE_OF_PRIDE_YOURE_NOT_WORTHY_OF_OPPOSING_ME;

	/**
	 * ID: 1800756<br>
	 * Message: Roar~! No~ Oink oink~! See~! I'm a pig~! Oink oink~!!
	 */
	public static final NpcStringId ROAR_NO_OINK_OINK_SEE_IM_A_PIG_OINK_OINK;

	/**
	 * ID: 1800757<br>
	 * Message: Who am I~ Where am I... Oink oink!
	 */
	public static final NpcStringId WHO_AM_I_WHERE_AM_I_OINK_OINK;

	/**
	 * ID: 1800758<br>
	 * Message: I just followed my friend here for fun~ Oink oink!
	 */
	public static final NpcStringId I_JUST_FOLLOWED_MY_FRIEND_HERE_FOR_FUN_OINK_OINK;

	/**
	 * ID: 1800759<br>
	 * Message: Wow! That's what I call a Cure-All!
	 */
	public static final NpcStringId WOW_THATS_WHAT_I_CALL_A_CURE_ALL;

	/**
	 * ID: 1800760<br>
	 * Message: I'm starving. Should I go chew some grass?
	 */
	public static final NpcStringId IM_STARVING_SHOULD_I_GO_CHEW_SOME_GRASS;

	/**
	 * ID: 1800761<br>
	 * Message: Thank you, thank you!
	 */
	public static final NpcStringId THANK_YOU_THANK_YOU;

	/**
	 * ID: 1800762<br>
	 * Message: What's this feeling? Oh oh~! Feels like my energy is back~!
	 */
	public static final NpcStringId WHATS_THIS_FEELING_OH_OH_FEELS_LIKE_MY_ENERGY_IS_BACK;

	/**
	 * ID: 1800763<br>
	 * Message: My body's getting lighter~ This feeling! Feels familiar somehow!
	 */
	public static final NpcStringId MY_BODYS_GETTING_LIGHTER_THIS_FEELING_FEELS_FAMILIAR_SOMEHOW;

	/**
	 * ID: 1800764<br>
	 * Message: Wow~ My fatigue is completely gone!
	 */
	public static final NpcStringId WOW_MY_FATIGUE_IS_COMPLETELY_GONE;

	/**
	 * ID: 1800765<br>
	 * Message: Hey~ The ominous energy is disappeared~!
	 */
	public static final NpcStringId HEY_THE_OMINOUS_ENERGY_IS_DISAPPEARED;

	/**
	 * ID: 1800766<br>
	 * Message: My body feels as light as a feather~
	 */
	public static final NpcStringId MY_BODY_FEELS_AS_LIGHT_AS_A_FEATHER;

	/**
	 * ID: 1800767<br>
	 * Message: What's this? Food?
	 */
	public static final NpcStringId WHATS_THIS_FOOD;

	/**
	 * ID: 1800768<br>
	 * Message: My energy is overflowing!!! I don't need any Fatigue Recovery Potion~
	 */
	public static final NpcStringId MY_ENERGY_IS_OVERFLOWING_I_DONT_NEED_ANY_FATIGUE_RECOVERY_POTION;

	/**
	 * ID: 1800769<br>
	 * Message: What's the matter? That's an amateur move!
	 */
	public static final NpcStringId WHATS_THE_MATTER_THATS_AN_AMATEUR_MOVE;

	/**
	 * ID: 1800770<br>
	 * Message: Fortune Timer: Reward increases 2 times if completed within 10 seconds!
	 */
	public static final NpcStringId FORTUNE_TIMER_REWARD_INCREASES_2_TIMES_IF_COMPLETED_WITHIN_10_SECONDS;

	/**
	 * ID: 1800771<br>
	 * Message: Fortune Timer: Reward increases 2 times if completed within 40 seconds!
	 */
	public static final NpcStringId FORTUNE_TIMER_REWARD_INCREASES_2_TIMES_IF_COMPLETED_WITHIN_40_SECONDS;

	/**
	 * ID: 1800772<br>
	 * Message: 40 seconds are remaining.
	 */
	public static final NpcStringId N40_SECONDS_ARE_REMAINING;

	/**
	 * ID: 1800773<br>
	 * Message: 39 seconds are remaining.
	 */
	public static final NpcStringId N39_SECONDS_ARE_REMAINING;

	/**
	 * ID: 1800774<br>
	 * Message: 38 seconds are remaining.
	 */
	public static final NpcStringId N38_SECONDS_ARE_REMAINING;

	/**
	 * ID: 1800775<br>
	 * Message: 37 seconds are remaining.
	 */
	public static final NpcStringId N37_SECONDS_ARE_REMAINING;

	/**
	 * ID: 1800776<br>
	 * Message: 36 seconds are remaining.
	 */
	public static final NpcStringId N36_SECONDS_ARE_REMAINING;

	/**
	 * ID: 1800777<br>
	 * Message: 35 seconds are remaining.
	 */
	public static final NpcStringId N35_SECONDS_ARE_REMAINING;

	/**
	 * ID: 1800778<br>
	 * Message: 34 seconds are remaining.
	 */
	public static final NpcStringId N34_SECONDS_ARE_REMAINING;

	/**
	 * ID: 1800779<br>
	 * Message: 33 seconds are remaining.
	 */
	public static final NpcStringId N33_SECONDS_ARE_REMAINING;

	/**
	 * ID: 1800780<br>
	 * Message: 32 seconds are remaining.
	 */
	public static final NpcStringId N32_SECONDS_ARE_REMAINING;

	/**
	 * ID: 1800781<br>
	 * Message: 31 seconds are remaining.
	 */
	public static final NpcStringId N31_SECONDS_ARE_REMAINING;

	/**
	 * ID: 1800782<br>
	 * Message: 30 seconds are remaining.
	 */
	public static final NpcStringId N30_SECONDS_ARE_REMAINING;

	/**
	 * ID: 1800783<br>
	 * Message: 29 seconds are remaining.
	 */
	public static final NpcStringId N29_SECONDS_ARE_REMAINING;

	/**
	 * ID: 1800784<br>
	 * Message: 28 seconds are remaining.
	 */
	public static final NpcStringId N28_SECONDS_ARE_REMAINING;

	/**
	 * ID: 1800785<br>
	 * Message: 27 seconds are remaining.
	 */
	public static final NpcStringId N27_SECONDS_ARE_REMAINING;

	/**
	 * ID: 1800786<br>
	 * Message: 26 seconds are remaining.
	 */
	public static final NpcStringId N26_SECONDS_ARE_REMAINING;

	/**
	 * ID: 1800787<br>
	 * Message: 25 seconds are remaining.
	 */
	public static final NpcStringId N25_SECONDS_ARE_REMAINING;

	/**
	 * ID: 1800788<br>
	 * Message: 24 seconds are remaining.
	 */
	public static final NpcStringId N24_SECONDS_ARE_REMAINING;

	/**
	 * ID: 1800789<br>
	 * Message: 23 seconds are remaining.
	 */
	public static final NpcStringId N23_SECONDS_ARE_REMAINING;

	/**
	 * ID: 1800790<br>
	 * Message: 22 seconds are remaining.
	 */
	public static final NpcStringId N22_SECONDS_ARE_REMAINING;

	/**
	 * ID: 1800791<br>
	 * Message: 21 seconds are remaining.
	 */
	public static final NpcStringId N21_SECONDS_ARE_REMAINING;

	/**
	 * ID: 1800792<br>
	 * Message: 20 seconds are remaining.
	 */
	public static final NpcStringId N20_SECONDS_ARE_REMAINING;

	/**
	 * ID: 1800793<br>
	 * Message: 19 seconds are remaining.
	 */
	public static final NpcStringId N19_SECONDS_ARE_REMAINING;

	/**
	 * ID: 1800794<br>
	 * Message: 18 seconds are remaining.
	 */
	public static final NpcStringId N18_SECONDS_ARE_REMAINING;

	/**
	 * ID: 1800795<br>
	 * Message: 17 seconds are remaining.
	 */
	public static final NpcStringId N17_SECONDS_ARE_REMAINING;

	/**
	 * ID: 1800796<br>
	 * Message: 16 seconds are remaining.
	 */
	public static final NpcStringId N16_SECONDS_ARE_REMAINING;

	/**
	 * ID: 1800797<br>
	 * Message: 15 seconds are remaining.
	 */
	public static final NpcStringId N15_SECONDS_ARE_REMAINING;

	/**
	 * ID: 1800798<br>
	 * Message: 14 seconds are remaining.
	 */
	public static final NpcStringId N14_SECONDS_ARE_REMAINING;

	/**
	 * ID: 1800799<br>
	 * Message: 13 seconds are remaining.
	 */
	public static final NpcStringId N13_SECONDS_ARE_REMAINING;

	/**
	 * ID: 1800800<br>
	 * Message: 12 seconds are remaining.
	 */
	public static final NpcStringId N12_SECONDS_ARE_REMAINING;

	/**
	 * ID: 1800801<br>
	 * Message: 11 seconds are remaining.
	 */
	public static final NpcStringId N11_SECONDS_ARE_REMAINING;

	/**
	 * ID: 1800802<br>
	 * Message: 10 seconds are remaining.
	 */
	public static final NpcStringId N10_SECONDS_ARE_REMAINING;

	/**
	 * ID: 1800803<br>
	 * Message: 9 seconds are remaining.
	 */
	public static final NpcStringId N9_SECONDS_ARE_REMAINING;

	/**
	 * ID: 1800804<br>
	 * Message: 8 seconds are remaining.
	 */
	public static final NpcStringId N8_SECONDS_ARE_REMAINING;

	/**
	 * ID: 1800805<br>
	 * Message: 7 seconds are remaining.
	 */
	public static final NpcStringId N7_SECONDS_ARE_REMAINING;

	/**
	 * ID: 1800806<br>
	 * Message: 6 seconds are remaining.
	 */
	public static final NpcStringId N6_SECONDS_ARE_REMAINING;

	/**
	 * ID: 1800807<br>
	 * Message: 5 seconds are remaining.
	 */
	public static final NpcStringId N5_SECONDS_ARE_REMAINING;

	/**
	 * ID: 1800808<br>
	 * Message: 4 seconds are remaining.
	 */
	public static final NpcStringId N4_SECONDS_ARE_REMAINING;

	/**
	 * ID: 1800809<br>
	 * Message: 3 seconds are remaining.
	 */
	public static final NpcStringId N3_SECONDS_ARE_REMAINING;

	/**
	 * ID: 1800810<br>
	 * Message: 2 seconds are remaining.
	 */
	public static final NpcStringId N2_SECONDS_ARE_REMAINING;

	/**
	 * ID: 1800811<br>
	 * Message: 1 seconds are remaining.
	 */
	public static final NpcStringId N1_SECONDS_ARE_REMAINING;

	/**
	 * ID: 1800812<br>
	 * Message: Time up!
	 */
	public static final NpcStringId TIME_UP;

	/**
	 * ID: 1800813<br>
	 * Message: Mission failed!
	 */
	public static final NpcStringId MISSION_FAILED;

	/**
	 * ID: 1800814<br>
	 * Message: Mission success!
	 */
	public static final NpcStringId MISSION_SUCCESS;

	/**
	 * ID: 1800815<br>
	 * Message: Hey! I already have an owner!
	 */
	public static final NpcStringId HEY_I_ALREADY_HAVE_AN_OWNER;

	/**
	 * ID: 1800816<br>
	 * Message: Hey~. Are you planning on eating me?! Use a Cupid's Fatigue Recovery Potion already!
	 */
	public static final NpcStringId HEY_ARE_YOU_PLANNING_ON_EATING_ME_USE_A_CUPIDS_FATIGUE_RECOVERY_POTION_ALREADY;

	/**
	 * ID: 1800817<br>
	 * Message: I'll pass on an amateur's meridian massage. Use a Cupid's Fatigue Recovery Potion already!
	 */
	public static final NpcStringId
			ILL_PASS_ON_AN_AMATEURS_MERIDIAN_MASSAGE_USE_A_CUPIDS_FATIGUE_RECOVERY_POTION_ALREADY;

	/**
	 * ID: 1800818<br>
	 * Message: I already feel more energetic. Thanks, $s1~
	 */
	public static final NpcStringId I_ALREADY_FEEL_MORE_ENERGETIC_THANKS_S1;

	/**
	 * ID: 1800819<br>
	 * Message: How refreshing! You wouldn't happen to be a master masseuse, $s1, would you?
	 */
	public static final NpcStringId HOW_REFRESHING_YOU_WOULDNT_HAPPEN_TO_BE_A_MASTER_MASSEUSE_S1_WOULD_YOU;

	/**
	 * ID: 1800820<br>
	 * Message: Incredible. From now on, I'll compare all massages to this one with $s1!
	 */
	public static final NpcStringId INCREDIBLE_FROM_NOW_ON_ILL_COMPARE_ALL_MASSAGES_TO_THIS_ONE_WITH_S1;

	/**
	 * ID: 1800821<br>
	 * Message: Isn't it tough doing it all on your own! Next time, try making a party with some comrades~
	 */
	public static final NpcStringId
			ISNT_IT_TOUGH_DOING_IT_ALL_ON_YOUR_OWN_NEXT_TIME_TRY_MAKING_A_PARTY_WITH_SOME_COMRADES;

	/**
	 * ID: 1800822<br>
	 * Message: Sorry, but I'll leave my friend in your care as well~ Thanks.
	 */
	public static final NpcStringId SORRY_BUT_ILL_LEAVE_MY_FRIEND_IN_YOUR_CARE_AS_WELL_THANKS;

	/**
	 * ID: 1800823<br>
	 * Message: Sniff sniff~ Do you smell the scent of a fresh-baked baguette?
	 */
	public static final NpcStringId SNIFF_SNIFF_DO_YOU_SMELL_THE_SCENT_OF_A_FRESH_BAKED_BAGUETTE;

	/**
	 * ID: 1800824<br>
	 * Message: Who am I~? Let me know if you wanna buy my bread~~
	 */
	public static final NpcStringId WHO_AM_I_LET_ME_KNOW_IF_YOU_WANNA_BUY_MY_BREAD;

	/**
	 * ID: 1800825<br>
	 * Message: I just want to make your weapons stronger~ Abra Kadabra~!
	 */
	public static final NpcStringId I_JUST_WANT_TO_MAKE_YOUR_WEAPONS_STRONGER_ABRA_KADABRA;

	/**
	 * ID: 1800826<br>
	 * Message: What? You don't like it? What's the matter with you~ Like an amateur!
	 */
	public static final NpcStringId WHAT_YOU_DONT_LIKE_IT_WHATS_THE_MATTER_WITH_YOU_LIKE_AN_AMATEUR;

	/**
	 * ID: 1800827<br>
	 * Message: Hey~ Did you tell a lie on April Fool's Day? Don't talk to me if you didn't~!
	 */
	public static final NpcStringId HEY_DID_YOU_TELL_A_LIE_ON_APRIL_FOOLS_DAY_DONT_TALK_TO_ME_IF_YOU_DIDNT;

	/**
	 * ID: 1800828<br>
	 * Message: Grunt... What's... wrong with me...
	 */
	public static final NpcStringId GRUNT_WHATS_WRONG_WITH_ME;

	/**
	 * ID: 1800829<br>
	 * Message: ...Grunt... Oh...
	 */
	public static final NpcStringId GRUNT_OH;

	/**
	 * ID: 1800830<br>
	 * Message: The grave robber warrior has been filled with dark energy and is attacking you!
	 */
	public static final NpcStringId THE_GRAVE_ROBBER_WARRIOR_HAS_BEEN_FILLED_WITH_DARK_ENERGY_AND_IS_ATTACKING_YOU;

	/**
	 * ID: 1800831<br>
	 * Message: The altar guardian is scrutinizing you!! \\nThose who dare to challenge using the power of evil shall be punished with death.
	 */
	public static final NpcStringId
			THE_ALTAR_GUARDIAN_IS_SCRUTINIZING_YOU_NTHOSE_WHO_DARE_TO_CHALLENGE_USING_THE_POWER_OF_EVIL_SHALL_BE_PUNISHED_WITH_DEATH;

	/**
	 * ID: 1800832<br>
	 * Message: Wait... Wait! Stop! Save me, and I'll give you 10,000,000 adena!!
	 */
	public static final NpcStringId WAIT_WAIT_STOP_SAVE_ME_AND_ILL_GIVE_YOU_10000000_ADENA;

	/**
	 * ID: 1800833<br>
	 * Message: I... don't want to fight...
	 */
	public static final NpcStringId I_DONT_WANT_TO_FIGHT;

	/**
	 * ID: 1800834<br>
	 * Message: Is this really necessary...?
	 */
	public static final NpcStringId IS_THIS_REALLY_NECESSARY;

	/**
	 * ID: 1800835<br>
	 * Message: Th... Thanks... I could have become good friends with you...
	 */
	public static final NpcStringId TH_THANKS_I_COULD_HAVE_BECOME_GOOD_FRIENDS_WITH_YOU;

	/**
	 * ID: 1800836<br>
	 * Message: I'll give you 10,000,000 adena, like I promised! I might be an orc who keeps my promises!
	 */
	public static final NpcStringId ILL_GIVE_YOU_10000000_ADENA_LIKE_I_PROMISED_I_MIGHT_BE_AN_ORC_WHO_KEEPS_MY_PROMISES;

	/**
	 * ID: 1800837<br>
	 * Message: Thanks, but that thing about 10,000,000 adena was a lie! See ya!!
	 */
	public static final NpcStringId THANKS_BUT_THAT_THING_ABOUT_10000000_ADENA_WAS_A_LIE_SEE_YA;

	/**
	 * ID: 1800838<br>
	 * Message: You're pretty dumb to believe me!
	 */
	public static final NpcStringId YOURE_PRETTY_DUMB_TO_BELIEVE_ME;

	/**
	 * ID: 1800839<br>
	 * Message: Ugh... A curse upon you...!
	 */
	public static final NpcStringId UGH_A_CURSE_UPON_YOU;

	/**
	 * ID: 1800840<br>
	 * Message: I really... didn't want... to fight...
	 */
	public static final NpcStringId I_REALLY_DIDNT_WANT_TO_FIGHT;

	/**
	 * ID: 1800841<br>
	 * Message: Kasha's Eye is scrutinizing you!!
	 */
	public static final NpcStringId KASHAS_EYE_IS_SCRUTINIZING_YOU;

	/**
	 * ID: 1800842<br>
	 * Message: The Kasha's Eye gives you a strange feeling.
	 */
	public static final NpcStringId THE_KASHAS_EYE_GIVES_YOU_A_STRANGE_FEELING;

	/**
	 * ID: 1800843<br>
	 * Message: The evil aura of the Kasha's Eye seems to be increasing quickly!!
	 */
	public static final NpcStringId THE_EVIL_AURA_OF_THE_KASHAS_EYE_SEEMS_TO_BE_INCREASING_QUICKLY;

	/**
	 * ID: 1800844<br>
	 * Message: I protect the altar! You can't escape the altar!
	 */
	public static final NpcStringId I_PROTECT_THE_ALTAR_YOU_CANT_ESCAPE_THE_ALTAR;

	/**
	 * ID: 1800845<br>
	 * Message: $s1! That stranger must be defeated. Here is the ultimate help!
	 */
	public static final NpcStringId S1_THAT_STRANGER_MUST_BE_DEFEATED_HERE_IS_THE_ULTIMATE_HELP;

	/**
	 * ID: 1800846<br>
	 * Message: Look here!! $s1. Don't fall too far behind.
	 */
	public static final NpcStringId LOOK_HERE_S1_DONT_FALL_TOO_FAR_BEHIND;

	/**
	 * ID: 1800847<br>
	 * Message: Well done. $s1. Your help is much appreciated.
	 */
	public static final NpcStringId WELL_DONE_S1_YOUR_HELP_IS_MUCH_APPRECIATED;

	/**
	 * ID: 1800848<br>
	 * Message: Who has awakened us from our slumber?
	 */
	public static final NpcStringId WHO_HAS_AWAKENED_US_FROM_OUR_SLUMBER;

	/**
	 * ID: 1800849<br>
	 * Message: All will pay a severe price to me and these here.
	 */
	public static final NpcStringId ALL_WILL_PAY_A_SEVERE_PRICE_TO_ME_AND_THESE_HERE;

	/**
	 * ID: 1800850<br>
	 * Message: Shyeed's cry is steadily dying down.
	 */
	public static final NpcStringId SHYEEDS_CRY_IS_STEADILY_DYING_DOWN;

	/**
	 * ID: 1800851<br>
	 * Message: Alert! Alert! Damage detection recognized. Countermeasures enabled.
	 */
	public static final NpcStringId ALERT_ALERT_DAMAGE_DETECTION_RECOGNIZED_COUNTERMEASURES_ENABLED;

	/**
	 * ID: 1800852<br>
	 * Message: Target recognition achieved. Attack sequence commencing.
	 */
	public static final NpcStringId TARGET_RECOGNITION_ACHIEVED_ATTACK_SEQUENCE_COMMENCING;

	/**
	 * ID: 1800853<br>
	 * Message: Target. Threat. Level. Launching. Strongest. Countermeasure.
	 */
	public static final NpcStringId TARGET_THREAT_LEVEL_LAUNCHING_STRONGEST_COUNTERMEASURE;

	/**
	 * ID: 1800854<br>
	 * Message: The purification field is being attacked. Guardian Spirits! Protect the Magic Force!!
	 */
	public static final NpcStringId THE_PURIFICATION_FIELD_IS_BEING_ATTACKED_GUARDIAN_SPIRITS_PROTECT_THE_MAGIC_FORCE;

	/**
	 * ID: 1800855<br>
	 * Message: Protect the Braziers of Purity at all costs!!
	 */
	public static final NpcStringId PROTECT_THE_BRAZIERS_OF_PURITY_AT_ALL_COSTS;

	/**
	 * ID: 1800856<br>
	 * Message: Defend our domain even at risk of your own life!
	 */
	public static final NpcStringId DEFEND_OUR_DOMAIN_EVEN_AT_RISK_OF_YOUR_OWN_LIFE;

	/**
	 * ID: 1800857<br>
	 * Message: Peunglui muglanep!
	 */
	public static final NpcStringId PEUNGLUI_MUGLANEP;

	/**
	 * ID: 1800858<br>
	 * Message: Naia waganagel peutagun!
	 */
	public static final NpcStringId NAIA_WAGANAGEL_PEUTAGUN;

	/**
	 * ID: 1800859<br>
	 * Message: Drive device partial destruction impulse result
	 */
	public static final NpcStringId DRIVE_DEVICE_PARTIAL_DESTRUCTION_IMPULSE_RESULT;

	/**
	 * ID: 1800860<br>
	 * Message: Even the Magic Force  binds you, you  will never be forgiven...
	 */
	public static final NpcStringId EVEN_THE_MAGIC_FORCE_BINDS_YOU_YOU_WILL_NEVER_BE_FORGIVEN;

	/**
	 * ID: 1800861<br>
	 * Message: Oh giants, an intruder has been discovered.
	 */
	public static final NpcStringId OH_GIANTS_AN_INTRUDER_HAS_BEEN_DISCOVERED;

	/**
	 * ID: 1800862<br>
	 * Message: All is vanity! but this cannot be the end!
	 */
	public static final NpcStringId ALL_IS_VANITY_BUT_THIS_CANNOT_BE_THE_END;

	/**
	 * ID: 1800863<br>
	 * Message: Those who are in front of my eyes! will be destroyed!
	 */
	public static final NpcStringId THOSE_WHO_ARE_IN_FRONT_OF_MY_EYES_WILL_BE_DESTROYED;

	/**
	 * ID: 1800864<br>
	 * Message: I am tired! Do not wake me up again!
	 */
	public static final NpcStringId I_AM_TIRED_DO_NOT_WAKE_ME_UP_AGAIN;

	/**
	 * ID: 1800865<br>
	 * Message: Intruder detected
	 */
	public static final NpcStringId _INTRUDER_DETECTED;

	/**
	 * ID: 1800866<br>
	 * Message: The candles can lead you to Zaken. Destroy him
	 */
	public static final NpcStringId THE_CANDLES_CAN_LEAD_YOU_TO_ZAKEN_DESTROY_HIM;

	/**
	 * ID: 1800867<br>
	 * Message: Who dares awkawen the mighty Zaken?
	 */
	public static final NpcStringId WHO_DARES_AWKAWEN_THE_MIGHTY_ZAKEN;

	/**
	 * ID: 1800868<br>
	 * Message: Ye not be finding me below the drink!
	 */
	public static final NpcStringId YE_NOT_BE_FINDING_ME_BELOW_THE_DRINK;

	/**
	 * ID: 1800869<br>
	 * Message: Ye must be three sheets to the wind if yer lookin for me there.
	 */
	public static final NpcStringId YE_MUST_BE_THREE_SHEETS_TO_THE_WIND_IF_YER_LOOKIN_FOR_ME_THERE;

	/**
	 * ID: 1800870<br>
	 * Message: Ye not be finding me in the Crows Nest.
	 */
	public static final NpcStringId YE_NOT_BE_FINDING_ME_IN_THE_CROWS_NEST;

	/**
	 * ID: 1800871<br>
	 * Message: Sorry but this is all I have.. Give me a break!
	 */
	public static final NpcStringId SORRY_BUT_THIS_IS_ALL_I_HAVE_GIVE_ME_A_BREAK;

	/**
	 * ID: 1800872<br>
	 * Message: Peunglui muglanep Naia waganagel peutagun!
	 */
	public static final NpcStringId PEUNGLUI_MUGLANEP_NAIA_WAGANAGEL_PEUTAGUN;

	/**
	 * ID: 1800873<br>
	 * Message: Drive device entire destruction moving suspension
	 */
	public static final NpcStringId DRIVE_DEVICE_ENTIRE_DESTRUCTION_MOVING_SUSPENSION;

	/**
	 * ID: 1800874<br>
	 * Message: Ah ah... From the Magic Force,  no more... I will be freed
	 */
	public static final NpcStringId AH_AH_FROM_THE_MAGIC_FORCE_NO_MORE_I_WILL_BE_FREED;

	/**
	 * ID: 1800875<br>
	 * Message: You guys are detected!
	 */
	public static final NpcStringId YOU_GUYS_ARE_DETECTED;

	/**
	 * ID: 1800876<br>
	 * Message: What kind of creatures are you!
	 */
	public static final NpcStringId WHAT_KIND_OF_CREATURES_ARE_YOU;

	/**
	 * ID: 1800877<br>
	 * Message: $s2 of level $s1 is acquired
	 */
	public static final NpcStringId S2_OF_LEVEL_S1_IS_ACQUIRED;

	/**
	 * ID: 1800878<br>
	 * Message: Life Stone from the Beginning acquired
	 */
	public static final NpcStringId LIFE_STONE_FROM_THE_BEGINNING_ACQUIRED;

	/**
	 * ID: 1800879<br>
	 * Message: When inventory weight/number are more than 80%, the Life Stone from the Beginning cannot be acquired.
	 */
	public static final NpcStringId
			WHEN_INVENTORY_WEIGHT_NUMBER_ARE_MORE_THAN_80_THE_LIFE_STONE_FROM_THE_BEGINNING_CANNOT_BE_ACQUIRED;

	/**
	 * ID: 1800880<br>
	 * Message: You are under my thumb!!
	 */
	public static final NpcStringId YOU_ARE_UNDER_MY_THUMB;

	/**
	 * ID: 1800881<br>
	 * Message: 20 minutes are added to the  remaining time in the Instant Zone.
	 */
	public static final NpcStringId N21_MINUTES_ARE_ADDED_TO_THE_REMAINING_TIME_IN_THE_INSTANT_ZONE;

	/**
	 * ID: 1800882<br>
	 * Message: Hurry hurry
	 */
	public static final NpcStringId HURRY_HURRY;

	/**
	 * ID: 1800883<br>
	 * Message: I am not that type of person who stays in one place for a long time
	 */
	public static final NpcStringId I_AM_NOT_THAT_TYPE_OF_PERSON_WHO_STAYS_IN_ONE_PLACE_FOR_A_LONG_TIME;

	/**
	 * ID: 1800884<br>
	 * Message: It's hard for me to keep standing like this
	 */
	public static final NpcStringId ITS_HARD_FOR_ME_TO_KEEP_STANDING_LIKE_THIS;

	/**
	 * ID: 1800885<br>
	 * Message: Why don't I go that way this time
	 */
	public static final NpcStringId WHY_DONT_I_GO_THAT_WAY_THIS_TIME;

	/**
	 * ID: 1800886<br>
	 * Message: Welcome!
	 */
	public static final NpcStringId WELCOME;

	/**
	 * ID: 1800887<br>
	 * Message: Is that it? Is that the extent of your abilities? Put in a little more effort!!
	 */
	public static final NpcStringId IS_THAT_IT_IS_THAT_THE_EXTENT_OF_YOUR_ABILITIES_PUT_IN_A_LITTLE_MORE_EFFORT;

	/**
	 * ID: 1800888<br>
	 * Message: Your abilities are pitiful... You are far from a worthy opponent.
	 */
	public static final NpcStringId YOUR_ABILITIES_ARE_PITIFUL_YOU_ARE_FAR_FROM_A_WORTHY_OPPONENT;

	/**
	 * ID: 1800889<br>
	 * Message: Even after death you order me to wander around looking for the scapegoats!
	 */
	public static final NpcStringId EVEN_AFTER_DEATH_YOU_ORDER_ME_TO_WANDER_AROUND_LOOKING_FOR_THE_SCAPEGOATS;

	/**
	 * ID: 1800890<br>
	 * Message: Here goes the Heatstroke! If you can withstand the hot heatstroke up to the 3rd stage, the Sultriness will come to you.
	 */
	public static final NpcStringId
			HERE_GOES_THE_HEATSTROKE_IF_YOU_CAN_WITHSTAND_THE_HOT_HEATSTROKE_UP_TO_THE_3RD_STAGE_THE_SULTRINESS_WILL_COME_TO_YOU;

	/**
	 * ID: 1800891<br>
	 * Message: Just you wait! Humidity is a blistering fireball which can easily withstand plenty of Cool Air Cannon attacks!
	 */
	public static final NpcStringId
			JUST_YOU_WAIT_HUMIDITY_IS_A_BLISTERING_FIREBALL_WHICH_CAN_EASILY_WITHSTAND_PLENTY_OF_COOL_AIR_CANNON_ATTACKS;

	/**
	 * ID: 1800892<br>
	 * Message: In order to defeat Humidity, you must obtain the Headstroke Prevention Effect from Doctor Ice and fire more than 10 rounds of the Cool Air Cannon on it!
	 */
	public static final NpcStringId
			IN_ORDER_TO_DEFEAT_HUMIDITY_YOU_MUST_OBTAIN_THE_HEADSTROKE_PREVENTION_EFFECT_FROM_DOCTOR_ICE_AND_FIRE_MORE_THAN_10_ROUNDS_OF_THE_COOL_AIR_CANNON_ON_IT;

	/**
	 * ID: 1800893<br>
	 * Message: You are here, $s1! I'll teach you a lesson! Bring it on!
	 */
	public static final NpcStringId YOU_ARE_HERE_S1_ILL_TEACH_YOU_A_LESSON_BRING_IT_ON;

	/**
	 * ID: 1800894<br>
	 * Message: That's cold! Isn't it one of those Cool Packs? I hate anything that's cold!
	 */
	public static final NpcStringId THATS_COLD_ISNT_IT_ONE_OF_THOSE_COOL_PACKS_I_HATE_ANYTHING_THATS_COLD;

	/**
	 * ID: 1800895<br>
	 * Message: Huh! You've missed! Is that all you have?
	 */
	public static final NpcStringId HUH_YOUVE_MISSED_IS_THAT_ALL_YOU_HAVE;

	/**
	 * ID: 1800896<br>
	 * Message: I will give you precious things that I have stolen. So stop bothering me!
	 */
	public static final NpcStringId I_WILL_GIVE_YOU_PRECIOUS_THINGS_THAT_I_HAVE_STOLEN_SO_STOP_BOTHERING_ME;

	/**
	 * ID: 1800897<br>
	 * Message: I was going to give you a jackpot item. You don't have enough inventory room. See you next time.
	 */
	public static final NpcStringId
			I_WAS_GOING_TO_GIVE_YOU_A_JACKPOT_ITEM_YOU_DONT_HAVE_ENOUGH_INVENTORY_ROOM_SEE_YOU_NEXT_TIME;

	/**
	 * ID: 1800898<br>
	 * Message: $s1 defeated the Sultriness and acquired item S84
	 */
	public static final NpcStringId S1_DEFEATED_THE_SULTRINESS_AND_ACQUIRED_ITEM_S84;

	/**
	 * ID: 1800899<br>
	 * Message: $s1 defeated the Sultriness and acquired item S80
	 */
	public static final NpcStringId S1_DEFEATED_THE_SULTRINESS_AND_ACQUIRED_ITEM_S80;

	/**
	 * ID: 1800900<br>
	 * Message: I am not here for you! Your Cool Pack attack does not work against me!
	 */
	public static final NpcStringId I_AM_NOT_HERE_FOR_YOU_YOUR_COOL_PACK_ATTACK_DOES_NOT_WORK_AGAINST_ME;

	/**
	 * ID: 1800901<br>
	 * Message: Uh oh? Where are you hiding? There is nobody who matches my skills? Well, I guess I'd better get going....
	 */
	public static final NpcStringId
			UH_OH_WHERE_ARE_YOU_HIDING_THERE_IS_NOBODY_WHO_MATCHES_MY_SKILLS_WELL_I_GUESS_ID_BETTER_GET_GOING;

	/**
	 * ID: 1800902<br>
	 * Message: Why are you not responding? You don't even have any Cool Packs! You can't fight me!
	 */
	public static final NpcStringId WHY_ARE_YOU_NOT_RESPONDING_YOU_DONT_EVEN_HAVE_ANY_COOL_PACKS_YOU_CANT_FIGHT_ME;

	/**
	 * ID: 1800903<br>
	 * Message: Oh~! Where I be~? Who call me~?
	 */
	public static final NpcStringId OH_WHERE_I_BE_WHO_CALL_ME;

	/**
	 * ID: 1800904<br>
	 * Message: Tada~! It's a watermelon~!
	 */
	public static final NpcStringId TADA_ITS_A_WATERMELON;

	/**
	 * ID: 1800906<br>
	 * Message: Enter the watermelon~! It's gonna grow and grow from now on!
	 */
	public static final NpcStringId ENTER_THE_WATERMELON_ITS_GONNA_GROW_AND_GROW_FROM_NOW_ON;

	/**
	 * ID: 1800907<br>
	 * Message: Oh, ouch~! Did I see you before~?
	 */
	public static final NpcStringId OH_OUCH_DID_I_SEE_YOU_BEFORE;

	/**
	 * ID: 1800908<br>
	 * Message: A new season! Summer is all about the watermelon~!
	 */
	public static final NpcStringId A_NEW_SEASON_SUMMER_IS_ALL_ABOUT_THE_WATERMELON;

	/**
	 * ID: 1800909<br>
	 * Message: Did ya call~? Ho! Thought you'd get something~?^^
	 */
	public static final NpcStringId DID_YA_CALL_HO_THOUGHT_YOUD_GET_SOMETHING;

	/**
	 * ID: 1800910<br>
	 * Message: Do you want to see my beautiful self~?
	 */
	public static final NpcStringId DO_YOU_WANT_TO_SEE_MY_BEAUTIFUL_SELF;

	/**
	 * ID: 1800911<br>
	 * Message: Hohoho! Let's do it together~!
	 */
	public static final NpcStringId HOHOHO_LETS_DO_IT_TOGETHER;

	/**
	 * ID: 1800912<br>
	 * Message: It's a giant watermelon if you raise it right~ And a watermelon slice if you mess up~!
	 */
	public static final NpcStringId ITS_A_GIANT_WATERMELON_IF_YOU_RAISE_IT_RIGHT_AND_A_WATERMELON_SLICE_IF_YOU_MESS_UP;

	/**
	 * ID: 1800913<br>
	 * Message: Tada~ Transformation~ complete~!
	 */
	public static final NpcStringId TADA_TRANSFORMATION_COMPLETE;

	/**
	 * ID: 1800914<br>
	 * Message: Am I a rain watermelon? Or a defective watermelon?
	 */
	public static final NpcStringId AM_I_A_RAIN_WATERMELON_OR_A_DEFECTIVE_WATERMELON;

	/**
	 * ID: 1800915<br>
	 * Message: Now! I've gotten big~! Everyone, come at me!
	 */
	public static final NpcStringId NOW_IVE_GOTTEN_BIG_EVERYONE_COME_AT_ME;

	/**
	 * ID: 1800916<br>
	 * Message: Get bigger~ Get stronger~! Tell me your wish~~!
	 */
	public static final NpcStringId GET_BIGGER_GET_STRONGER_TELL_ME_YOUR_WISH;

	/**
	 * ID: 1800917<br>
	 * Message: A watermelon slice's wish! But I'm bigger already?
	 */
	public static final NpcStringId A_WATERMELON_SLICES_WISH_BUT_IM_BIGGER_ALREADY;

	/**
	 * ID: 1800918<br>
	 * Message: A large watermelon's wish! Well, try to break me!
	 */
	public static final NpcStringId A_LARGE_WATERMELONS_WISH_WELL_TRY_TO_BREAK_ME;

	/**
	 * ID: 1800919<br>
	 * Message: I'm done growing~! I'm running away now~^^
	 */
	public static final NpcStringId IM_DONE_GROWING_IM_RUNNING_AWAY_NOW;

	/**
	 * ID: 1800920<br>
	 * Message: If you let me go, I'll give you ten million adena!
	 */
	public static final NpcStringId IF_YOU_LET_ME_GO_ILL_GIVE_YOU_TEN_MILLION_ADENA;

	/**
	 * ID: 1800921<br>
	 * Message: Freedom~! What do you think I have inside?
	 */
	public static final NpcStringId FREEDOM_WHAT_DO_YOU_THINK_I_HAVE_INSIDE;

	/**
	 * ID: 1800922<br>
	 * Message: OK~ OK~ Good job. You know what to do next, right?
	 */
	public static final NpcStringId OK_OK_GOOD_JOB_YOU_KNOW_WHAT_TO_DO_NEXT_RIGHT;

	/**
	 * ID: 1800923<br>
	 * Message: Look here! Do it right! You spilled! This precious...
	 */
	public static final NpcStringId LOOK_HERE_DO_IT_RIGHT_YOU_SPILLED_THIS_PRECIOUS;

	/**
	 * ID: 1800924<br>
	 * Message: Ah~ Refreshing! Spray a little more~!
	 */
	public static final NpcStringId AH_REFRESHING_SPRAY_A_LITTLE_MORE;

	/**
	 * ID: 1800925<br>
	 * Message: Gulp gulp~ Great! But isn't there more?
	 */
	public static final NpcStringId GULP_GULP_GREAT_BUT_ISNT_THERE_MORE;

	/**
	 * ID: 1800926<br>
	 * Message: Can't you even aim right? Have you even been to the army?
	 */
	public static final NpcStringId CANT_YOU_EVEN_AIM_RIGHT_HAVE_YOU_EVEN_BEEN_TO_THE_ARMY;

	/**
	 * ID: 1800927<br>
	 * Message: Did you mix this with water~? Why's it taste like this~!
	 */
	public static final NpcStringId DID_YOU_MIX_THIS_WITH_WATER_WHYS_IT_TASTE_LIKE_THIS;

	/**
	 * ID: 1800928<br>
	 * Message: Oh! Good! Do a little more. Yeah~
	 */
	public static final NpcStringId OH_GOOD_DO_A_LITTLE_MORE_YEAH;

	/**
	 * ID: 1800929<br>
	 * Message: Hoho! It's not there! Over here~! Am I so small that you can even spray me right?
	 */
	public static final NpcStringId HOHO_ITS_NOT_THERE_OVER_HERE_AM_I_SO_SMALL_THAT_YOU_CAN_EVEN_SPRAY_ME_RIGHT;

	/**
	 * ID: 1800930<br>
	 * Message: Yuck! What is this~! Are you sure this is nectar~?
	 */
	public static final NpcStringId YUCK_WHAT_IS_THIS_ARE_YOU_SURE_THIS_IS_NECTAR;

	/**
	 * ID: 1800931<br>
	 * Message: Do your best~! I become a big watermelon after just five bottles~!
	 */
	public static final NpcStringId DO_YOUR_BEST_I_BECOME_A_BIG_WATERMELON_AFTER_JUST_FIVE_BOTTLES;

	/**
	 * ID: 1800932<br>
	 * Message: Of course~! Watermelon is the best nectar! Hahaha~!
	 */
	public static final NpcStringId OF_COURSE_WATERMELON_IS_THE_BEST_NECTAR_HAHAHA;

	/**
	 * ID: 1800934<br>
	 * Message: Oww! You're just beating me now~ Give me nectar~
	 */
	public static final NpcStringId OWW_YOURE_JUST_BEATING_ME_NOW_GIVE_ME_NECTAR;

	/**
	 * ID: 1800935<br>
	 * Message: Look~!! It's gonna break~!
	 */
	public static final NpcStringId LOOK_ITS_GONNA_BREAK;

	/**
	 * ID: 1800936<br>
	 * Message: Now! Are you trying to eat without doing the work? Fine~ Do what you want~ I'll hate you if you don't give me any nectar!
	 */
	public static final NpcStringId
			NOW_ARE_YOU_TRYING_TO_EAT_WITHOUT_DOING_THE_WORK_FINE_DO_WHAT_YOU_WANT_ILL_HATE_YOU_IF_YOU_DONT_GIVE_ME_ANY_NECTAR;

	/**
	 * ID: 1800937<br>
	 * Message: Hit me more! Hit me more!
	 */
	public static final NpcStringId HIT_ME_MORE_HIT_ME_MORE;

	/**
	 * ID: 1800938<br>
	 * Message: I'm gonna wither like this~ Damn it~!
	 */
	public static final NpcStringId IM_GONNA_WITHER_LIKE_THIS_DAMN_IT;

	/**
	 * ID: 1800939<br>
	 * Message: Hey you~ If I die like this, there'll be no item either~ Are you really so stingy with the nectar?
	 */
	public static final NpcStringId
			HEY_YOU_IF_I_DIE_LIKE_THIS_THERELL_BE_NO_ITEM_EITHER_ARE_YOU_REALLY_SO_STINGY_WITH_THE_NECTAR;

	/**
	 * ID: 1800940<br>
	 * Message: It's just a little more!! Good luck~!
	 */
	public static final NpcStringId ITS_JUST_A_LITTLE_MORE_GOOD_LUCK;

	/**
	 * ID: 1800941<br>
	 * Message: Save me~ I'm about to die without tasting nectar even once~
	 */
	public static final NpcStringId SAVE_ME_IM_ABOUT_TO_DIE_WITHOUT_TASTING_NECTAR_EVEN_ONCE;

	/**
	 * ID: 1800942<br>
	 * Message: If I die like this, I'll just be a watermelon slice~!
	 */
	public static final NpcStringId IF_I_DIE_LIKE_THIS_ILL_JUST_BE_A_WATERMELON_SLICE;

	/**
	 * ID: 1800943<br>
	 * Message: I'm getting stronger~? I think I'll be able to run away in 30 seconds~ Hoho~
	 */
	public static final NpcStringId IM_GETTING_STRONGER_I_THINK_ILL_BE_ABLE_TO_RUN_AWAY_IN_30_SECONDS_HOHO;

	/**
	 * ID: 1800944<br>
	 * Message: It's goodbye after 20 seconds~!
	 */
	public static final NpcStringId ITS_GOODBYE_AFTER_20_SECONDS;

	/**
	 * ID: 1800945<br>
	 * Message: Yeah, 10 seconds left~! 9... 8... 7...!
	 */
	public static final NpcStringId YEAH_10_SECONDS_LEFT_9_8_7;

	/**
	 * ID: 1800946<br>
	 * Message: I'm leaving in 2 minutes if you don't give me any nectar~!
	 */
	public static final NpcStringId IM_LEAVING_IN_2_MINUTES_IF_YOU_DONT_GIVE_ME_ANY_NECTAR;

	/**
	 * ID: 1800947<br>
	 * Message: I'm leaving in 1 minutes if you don't give me any nectar~!
	 */
	public static final NpcStringId IM_LEAVING_IN_1_MINUTES_IF_YOU_DONT_GIVE_ME_ANY_NECTAR;

	/**
	 * ID: 1800948<br>
	 * Message: I'm leaving now~! Then, goodbye~!
	 */
	public static final NpcStringId IM_LEAVING_NOW_THEN_GOODBYE;

	/**
	 * ID: 1800949<br>
	 * Message: Sorry, but this large watermelon is disappearing here~!
	 */
	public static final NpcStringId SORRY_BUT_THIS_LARGE_WATERMELON_IS_DISAPPEARING_HERE;

	/**
	 * ID: 1800950<br>
	 * Message: Too late~! Have a good time~!
	 */
	public static final NpcStringId TOO_LATE_HAVE_A_GOOD_TIME;

	/**
	 * ID: 1800951<br>
	 * Message: Ding ding~ That's the bell. Put away your weapons and try for next time~!
	 */
	public static final NpcStringId DING_DING_THATS_THE_BELL_PUT_AWAY_YOUR_WEAPONS_AND_TRY_FOR_NEXT_TIME;

	/**
	 * ID: 1800952<br>
	 * Message: Too bad~! You raised it up, too! -_-
	 */
	public static final NpcStringId TOO_BAD_YOU_RAISED_IT_UP_TOO;

	/**
	 * ID: 1800953<br>
	 * Message: Oh~ What a nice sound~
	 */
	public static final NpcStringId OH_WHAT_A_NICE_SOUND;

	/**
	 * ID: 1800954<br>
	 * Message: The instrument is nice, but there's no song. Shall I sing for you?
	 */
	public static final NpcStringId THE_INSTRUMENT_IS_NICE_BUT_THERES_NO_SONG_SHALL_I_SING_FOR_YOU;

	/**
	 * ID: 1800955<br>
	 * Message: What beautiful music!
	 */
	public static final NpcStringId WHAT_BEAUTIFUL_MUSIC;

	/**
	 * ID: 1800956<br>
	 * Message: I feel good~ Play some more!
	 */
	public static final NpcStringId I_FEEL_GOOD_PLAY_SOME_MORE;

	/**
	 * ID: 1800957<br>
	 * Message: My heart is being captured by the sound of Crono!
	 */
	public static final NpcStringId MY_HEART_IS_BEING_CAPTURED_BY_THE_SOUND_OF_CRONO;

	/**
	 * ID: 1800958<br>
	 * Message: Get the notes right~! Hey old man~ That was wrong!
	 */
	public static final NpcStringId GET_THE_NOTES_RIGHT_HEY_OLD_MAN_THAT_WAS_WRONG;

	/**
	 * ID: 1800959<br>
	 * Message: I like it~!
	 */
	public static final NpcStringId I_LIKE_IT;

	/**
	 * ID: 1800960<br>
	 * Message: Ooh~~ My body wants to open!
	 */
	public static final NpcStringId OOH_MY_BODY_WANTS_TO_OPEN;

	/**
	 * ID: 1800961<br>
	 * Message: Oh~! This chord! My heart is being torn! Play a little more!
	 */
	public static final NpcStringId OH_THIS_CHORD_MY_HEART_IS_BEING_TORN_PLAY_A_LITTLE_MORE;

	/**
	 * ID: 1800962<br>
	 * Message: It's this~ This! I wanted this sound! Why don't you try becoming a singer?
	 */
	public static final NpcStringId ITS_THIS_THIS_I_WANTED_THIS_SOUND_WHY_DONT_YOU_TRY_BECOMING_A_SINGER;

	/**
	 * ID: 1800963<br>
	 * Message: You can try a hundred times on this~ You won't get anything good~!
	 */
	public static final NpcStringId YOU_CAN_TRY_A_HUNDRED_TIMES_ON_THIS_YOU_WONT_GET_ANYTHING_GOOD;

	/**
	 * ID: 1800964<br>
	 * Message: It hurts~! Play just the instrument~!
	 */
	public static final NpcStringId IT_HURTS_PLAY_JUST_THE_INSTRUMENT;

	/**
	 * ID: 1800965<br>
	 * Message: Only good music can open my body!
	 */
	public static final NpcStringId ONLY_GOOD_MUSIC_CAN_OPEN_MY_BODY;

	/**
	 * ID: 1800966<br>
	 * Message: Not this, but you know, that~ What you got as a Chronicle souvenir. Play with that!
	 */
	public static final NpcStringId NOT_THIS_BUT_YOU_KNOW_THAT_WHAT_YOU_GOT_AS_A_CHRONICLE_SOUVENIR_PLAY_WITH_THAT;

	/**
	 * ID: 1800967<br>
	 * Message: Why~ You have no music? Boring... I'm leaving now~
	 */
	public static final NpcStringId WHY_YOU_HAVE_NO_MUSIC_BORING_IM_LEAVING_NOW;

	/**
	 * ID: 1800968<br>
	 * Message: Not those sharp things~! Use the ones that make nice sounds!
	 */
	public static final NpcStringId NOT_THOSE_SHARP_THINGS_USE_THE_ONES_THAT_MAKE_NICE_SOUNDS;

	/**
	 * ID: 1800969<br>
	 * Message: Large watermelons only open with music~ Just striking with a weapon won't work~!
	 */
	public static final NpcStringId LARGE_WATERMELONS_ONLY_OPEN_WITH_MUSIC_JUST_STRIKING_WITH_A_WEAPON_WONT_WORK;

	/**
	 * ID: 1800970<br>
	 * Message: Strike with music~! Not with something like this. You need music~!
	 */
	public static final NpcStringId STRIKE_WITH_MUSIC_NOT_WITH_SOMETHING_LIKE_THIS_YOU_NEED_MUSIC;

	/**
	 * ID: 1800971<br>
	 * Message: You're pretty amazing~! But it's all for nothing~~!
	 */
	public static final NpcStringId YOURE_PRETTY_AMAZING_BUT_ITS_ALL_FOR_NOTHING;

	/**
	 * ID: 1800972<br>
	 * Message: Use that on monsters, OK? I want Crono~!
	 */
	public static final NpcStringId USE_THAT_ON_MONSTERS_OK_I_WANT_CRONO;

	/**
	 * ID: 1800973<br>
	 * Message: Everyone~ The watermelon is breaking!!!
	 */
	public static final NpcStringId EVERYONE_THE_WATERMELON_IS_BREAKING;

	/**
	 * ID: 1800974<br>
	 * Message: It's like a watermelon slice~!
	 */
	public static final NpcStringId ITS_LIKE_A_WATERMELON_SLICE;

	/**
	 * ID: 1800976<br>
	 * Message: Large watermelon~! Make a wish!!
	 */
	public static final NpcStringId LARGE_WATERMELON_MAKE_A_WISH;

	/**
	 * ID: 1800977<br>
	 * Message: Don't tell anyone about my death~
	 */
	public static final NpcStringId DONT_TELL_ANYONE_ABOUT_MY_DEATH;

	/**
	 * ID: 1800978<br>
	 * Message: Ugh~ The red juice is flowing out!
	 */
	public static final NpcStringId UGH_THE_RED_JUICE_IS_FLOWING_OUT;

	/**
	 * ID: 1800979<br>
	 * Message: This is all~
	 */
	public static final NpcStringId THIS_IS_ALL;

	/**
	 * ID: 1800980<br>
	 * Message: Kyaahh!!! I'm mad...!
	 */
	public static final NpcStringId KYAAHH_IM_MAD;

	/**
	 * ID: 1800981<br>
	 * Message: Everyone~ This watermelon broke open!! The item is falling out!
	 */
	public static final NpcStringId EVERYONE_THIS_WATERMELON_BROKE_OPEN_THE_ITEM_IS_FALLING_OUT;

	/**
	 * ID: 1800982<br>
	 * Message: Oh! It burst! The contents are spilling out~
	 */
	public static final NpcStringId OH_IT_BURST_THE_CONTENTS_ARE_SPILLING_OUT;

	/**
	 * ID: 1800983<br>
	 * Message: Hohoho~ Play better!
	 */
	public static final NpcStringId HOHOHO_PLAY_BETTER;

	/**
	 * ID: 1800984<br>
	 * Message: Oh~!! You're very talented, huh~?
	 */
	public static final NpcStringId OH_YOURE_VERY_TALENTED_HUH;

	/**
	 * ID: 1800985<br>
	 * Message: Play some more! More!  More!  More!
	 */
	public static final NpcStringId PLAY_SOME_MORE_MORE_MORE_MORE;

	/**
	 * ID: 1800986<br>
	 * Message: I eat hits and grow!!
	 */
	public static final NpcStringId I_EAT_HITS_AND_GROW;

	/**
	 * ID: 1800987<br>
	 * Message: Buck up~ There isn't much time~
	 */
	public static final NpcStringId BUCK_UP_THERE_ISNT_MUCH_TIME;

	/**
	 * ID: 1800988<br>
	 * Message: Do you think I'll burst with just that~?
	 */
	public static final NpcStringId DO_YOU_THINK_ILL_BURST_WITH_JUST_THAT;

	/**
	 * ID: 1800989<br>
	 * Message: What a nice attack~ You might be able to kill a passing fly~
	 */
	public static final NpcStringId WHAT_A_NICE_ATTACK_YOU_MIGHT_BE_ABLE_TO_KILL_A_PASSING_FLY;

	/**
	 * ID: 1800990<br>
	 * Message: Right there! A little to the right~! Ah~ Refreshing.
	 */
	public static final NpcStringId RIGHT_THERE_A_LITTLE_TO_THE_RIGHT_AH_REFRESHING;

	/**
	 * ID: 1800991<br>
	 * Message: You call that hitting? Bring some more talented friends!
	 */
	public static final NpcStringId YOU_CALL_THAT_HITTING_BRING_SOME_MORE_TALENTED_FRIENDS;

	/**
	 * ID: 1800992<br>
	 * Message: Don't think! Just hit! We're hitting!
	 */
	public static final NpcStringId DONT_THINK_JUST_HIT_WERE_HITTING;

	/**
	 * ID: 1800993<br>
	 * Message: I need nectar~ Gourd nectar!
	 */
	public static final NpcStringId I_NEED_NECTAR_GOURD_NECTAR;

	/**
	 * ID: 1800994<br>
	 * Message: I can only grow by drinking nectar~
	 */
	public static final NpcStringId I_CAN_ONLY_GROW_BY_DRINKING_NECTAR;

	/**
	 * ID: 1800995<br>
	 * Message: Grow me quick! If you're good, it's a large watermelon. If you're bad, it a watermelon slice~!
	 */
	public static final NpcStringId
			GROW_ME_QUICK_IF_YOURE_GOOD_ITS_A_LARGE_WATERMELON_IF_YOURE_BAD_IT_A_WATERMELON_SLICE;

	/**
	 * ID: 1800996<br>
	 * Message: Gimme nectar~ I'm hungry~
	 */
	public static final NpcStringId GIMME_NECTAR_IM_HUNGRY;

	/**
	 * ID: 1800998<br>
	 * Message: Bring me nectar. Then, I'll drink and grow!
	 */
	public static final NpcStringId BRING_ME_NECTAR_THEN_ILL_DRINK_AND_GROW;

	/**
	 * ID: 1800999<br>
	 * Message: You wanna eat a tiny watermelon like me? Try giving me some nectar. I'll get huge~!
	 */
	public static final NpcStringId YOU_WANNA_EAT_A_TINY_WATERMELON_LIKE_ME_TRY_GIVING_ME_SOME_NECTAR_ILL_GET_HUGE;

	/**
	 * ID: 1801000<br>
	 * Message: Hehehe... Grow me well and you'll get a reward. Grow me bad and who knows what'll happen~
	 */
	public static final NpcStringId HEHEHE_GROW_ME_WELL_AND_YOULL_GET_A_REWARD_GROW_ME_BAD_AND_WHO_KNOWS_WHATLL_HAPPEN;

	/**
	 * ID: 1801001<br>
	 * Message: You want a large watermelon? I'd like to be a watermelon slice~
	 */
	public static final NpcStringId YOU_WANT_A_LARGE_WATERMELON_ID_LIKE_TO_BE_A_WATERMELON_SLICE;

	/**
	 * ID: 1801002<br>
	 * Message: Trust me and bring me some nectar!! I'll become a large watermelon for you~!
	 */
	public static final NpcStringId TRUST_ME_AND_BRING_ME_SOME_NECTAR_ILL_BECOME_A_LARGE_WATERMELON_FOR_YOU;

	/**
	 * ID: 1801003<br>
	 * Message: I see. Beleth has recovered all of its magic power. What remains here is just its trace.
	 */
	public static final NpcStringId
			I_SEE_BELETH_HAS_RECOVERED_ALL_OF_ITS_MAGIC_POWER_WHAT_REMAINS_HERE_IS_JUST_ITS_TRACE;

	/**
	 * ID: 1801004<br>
	 * Message: Command Channel Leader $s1, Beleth's Ring has been acquired.
	 */
	public static final NpcStringId COMMAND_CHANNEL_LEADER_S1_BELETHS_RING_HAS_BEEN_ACQUIRED;

	/**
	 * ID: 1801005<br>
	 * Message: You summoned me, so you must be confident, huh? Here I come! Jack game!
	 */
	public static final NpcStringId YOU_SUMMONED_ME_SO_YOU_MUST_BE_CONFIDENT_HUH_HERE_I_COME_JACK_GAME;

	/**
	 * ID: 1801006<br>
	 * Message: Hello. Let's have a good Jack game.
	 */
	public static final NpcStringId HELLO_LETS_HAVE_A_GOOD_JACK_GAME;

	/**
	 * ID: 1801007<br>
	 * Message: I'm starting! Now, show me the card you want!
	 */
	public static final NpcStringId IM_STARTING_NOW_SHOW_ME_THE_CARD_YOU_WANT;

	/**
	 * ID: 1801008<br>
	 * Message: We'll start now! Show me the card you want!
	 */
	public static final NpcStringId WELL_START_NOW_SHOW_ME_THE_CARD_YOU_WANT;

	/**
	 * ID: 1801009<br>
	 * Message: I'm showing the Rotten Pumpkin Card!
	 */
	public static final NpcStringId IM_SHOWING_THE_ROTTEN_PUMPKIN_CARD;

	/**
	 * ID: 1801010<br>
	 * Message: I'll be showing the Rotten Pumpkin Card!
	 */
	public static final NpcStringId ILL_BE_SHOWING_THE_ROTTEN_PUMPKIN_CARD;

	/**
	 * ID: 1801011<br>
	 * Message: I'm showing the Jack Pumpkin Card!
	 */
	public static final NpcStringId IM_SHOWING_THE_JACK_PUMPKIN_CARD;

	/**
	 * ID: 1801012<br>
	 * Message: I'll be showing the Jack Pumpkin Card!
	 */
	public static final NpcStringId ILL_BE_SHOWING_THE_JACK_PUMPKIN_CARD;

	/**
	 * ID: 1801013<br>
	 * Message: That's my precious Fantastic Chocolate Banana Ultra Favor Candy!!! I'm definitely winning the next round!!
	 */
	public static final NpcStringId
			THATS_MY_PRECIOUS_FANTASTIC_CHOCOLATE_BANANA_ULTRA_FAVOR_CANDY_IM_DEFINITELY_WINNING_THE_NEXT_ROUND;

	/**
	 * ID: 1801014<br>
	 * Message: It's my precious candy, but... I'll happily give it to you~!
	 */
	public static final NpcStringId ITS_MY_PRECIOUS_CANDY_BUT_ILL_HAPPILY_GIVE_IT_TO_YOU;

	/**
	 * ID: 1801015<br>
	 * Message: The candy fell. I'll give you my toy chest instead.
	 */
	public static final NpcStringId THE_CANDY_FELL_ILL_GIVE_YOU_MY_TOY_CHEST_INSTEAD;

	/**
	 * ID: 1801016<br>
	 * Message: Since the candy fell, I will give you my toy chest instead.
	 */
	public static final NpcStringId SINCE_THE_CANDY_FELL_I_WILL_GIVE_YOU_MY_TOY_CHEST_INSTEAD;

	/**
	 * ID: 1801017<br>
	 * Message: You're not peeking at my card, are you? This time, I'll wager a special scroll.
	 */
	public static final NpcStringId YOURE_NOT_PEEKING_AT_MY_CARD_ARE_YOU_THIS_TIME_ILL_WAGER_A_SPECIAL_SCROLL;

	/**
	 * ID: 1801018<br>
	 * Message: We're getting serious now. If you win again, I'll give you a special scroll.
	 */
	public static final NpcStringId WERE_GETTING_SERIOUS_NOW_IF_YOU_WIN_AGAIN_ILL_GIVE_YOU_A_SPECIAL_SCROLL;

	/**
	 * ID: 1801019<br>
	 * Message: You could probably enter the underworld pro league!
	 */
	public static final NpcStringId YOU_COULD_PROBABLY_ENTER_THE_UNDERWORLD_PRO_LEAGUE;

	/**
	 * ID: 1801020<br>
	 * Message: Even pros can't do this much. You're amazing.
	 */
	public static final NpcStringId EVEN_PROS_CANT_DO_THIS_MUCH_YOURE_AMAZING;

	/**
	 * ID: 1801021<br>
	 * Message: Who's the monster here?! This time, I'll bet my precious Transformation Stick.
	 */
	public static final NpcStringId WHOS_THE_MONSTER_HERE_THIS_TIME_ILL_BET_MY_PRECIOUS_TRANSFORMATION_STICK;

	/**
	 * ID: 1801022<br>
	 * Message: I lost again. I won't lose this time. I'm betting my Transformation Stick.
	 */
	public static final NpcStringId I_LOST_AGAIN_I_WONT_LOSE_THIS_TIME_IM_BETTING_MY_TRANSFORMATION_STICK;

	/**
	 * ID: 1801023<br>
	 * Message: Lost again! Hmph. Next time, I'll bet an incredible gift! Wait for it if you want!
	 */
	public static final NpcStringId LOST_AGAIN_HMPH_NEXT_TIME_ILL_BET_AN_INCREDIBLE_GIFT_WAIT_FOR_IT_IF_YOU_WANT;

	/**
	 * ID: 1801024<br>
	 * Message: You're too good. Next time, I'll give you an incredible gift! Please wait for you.
	 */
	public static final NpcStringId YOURE_TOO_GOOD_NEXT_TIME_ILL_GIVE_YOU_AN_INCREDIBLE_GIFT_PLEASE_WAIT_FOR_YOU;

	/**
	 * ID: 1801025<br>
	 * Message: My pride can't handle you winning anymore!
	 */
	public static final NpcStringId MY_PRIDE_CANT_HANDLE_YOU_WINNING_ANYMORE;

	/**
	 * ID: 1801026<br>
	 * Message: I would be embarrassing to lose again here...
	 */
	public static final NpcStringId I_WOULD_BE_EMBARRASSING_TO_LOSE_AGAIN_HERE;

	/**
	 * ID: 1801027<br>
	 * Message: What's your name? I'm gonna remember you!
	 */
	public static final NpcStringId WHATS_YOUR_NAME_IM_GONNA_REMEMBER_YOU;

	/**
	 * ID: 1801028<br>
	 * Message: People from the above ground world are really good at games.
	 */
	public static final NpcStringId PEOPLE_FROM_THE_ABOVE_GROUND_WORLD_ARE_REALLY_GOOD_AT_GAMES;

	/**
	 * ID: 1801029<br>
	 * Message: You've played a lot in the underworld, haven't you?!
	 */
	public static final NpcStringId YOUVE_PLAYED_A_LOT_IN_THE_UNDERWORLD_HAVENT_YOU;

	/**
	 * ID: 1801030<br>
	 * Message: I've never met someone so good before.
	 */
	public static final NpcStringId IVE_NEVER_MET_SOMEONE_SO_GOOD_BEFORE;

	/**
	 * ID: 1801031<br>
	 * Message: 13 wins in a row. You're pretty lucky today, huh?
	 */
	public static final NpcStringId N13_WINS_IN_A_ROW_YOURE_PRETTY_LUCKY_TODAY_HUH;

	/**
	 * ID: 1801032<br>
	 * Message: I never thought I would see 13 wins in a row.
	 */
	public static final NpcStringId I_NEVER_THOUGHT_I_WOULD_SEE_13_WINS_IN_A_ROW;

	/**
	 * ID: 1801033<br>
	 * Message: This is the highest record in my life! Next time, I'll give you my treasure -- the Golden Jack O'Lantern!
	 */
	public static final NpcStringId
			THIS_IS_THE_HIGHEST_RECORD_IN_MY_LIFE_NEXT_TIME_ILL_GIVE_YOU_MY_TREASURE_THE_GOLDEN_JACK_OLANTERN;

	/**
	 * ID: 1801034<br>
	 * Message: Even pros can't do 14 wins...! Next time, I will give you my treasure, the Golden Jack O'Lantern.
	 */
	public static final NpcStringId
			EVEN_PROS_CANT_DO_14_WINS_NEXT_TIME_I_WILL_GIVE_YOU_MY_TREASURE_THE_GOLDEN_JACK_OLANTERN;

	/**
	 * ID: 1801035<br>
	 * Message: I can't do this anymore! You win! I acknowledge you as the best I've ever met in all my 583 years!
	 */
	public static final NpcStringId
			I_CANT_DO_THIS_ANYMORE_YOU_WIN_I_ACKNOWLEDGE_YOU_AS_THE_BEST_IVE_EVER_MET_IN_ALL_MY_583_YEARS;

	/**
	 * ID: 1801036<br>
	 * Message: Playing any more is meaningless. You were my greatest opponent.
	 */
	public static final NpcStringId PLAYING_ANY_MORE_IS_MEANINGLESS_YOU_WERE_MY_GREATEST_OPPONENT;

	/**
	 * ID: 1801037<br>
	 * Message: I won this round...! It was fun.
	 */
	public static final NpcStringId I_WON_THIS_ROUND_IT_WAS_FUN;

	/**
	 * ID: 1801038<br>
	 * Message: I won this round. It was enjoyable.
	 */
	public static final NpcStringId I_WON_THIS_ROUND_IT_WAS_ENJOYABLE;

	/**
	 * ID: 1801039<br>
	 * Message: Above world people are so fun...! Then, see you later!
	 */
	public static final NpcStringId ABOVE_WORLD_PEOPLE_ARE_SO_FUN_THEN_SEE_YOU_LATER;

	/**
	 * ID: 1801040<br>
	 * Message: Call me again next time. I want to play again with you.
	 */
	public static final NpcStringId CALL_ME_AGAIN_NEXT_TIME_I_WANT_TO_PLAY_AGAIN_WITH_YOU;

	/**
	 * ID: 1801041<br>
	 * Message: You wanna play some more? I'm out of presents, but I'll give you candy!
	 */
	public static final NpcStringId YOU_WANNA_PLAY_SOME_MORE_IM_OUT_OF_PRESENTS_BUT_ILL_GIVE_YOU_CANDY;

	/**
	 * ID: 1801042<br>
	 * Message: Will you play some more? I don't have any more presents, but I will give you candy if you win.
	 */
	public static final NpcStringId
			WILL_YOU_PLAY_SOME_MORE_I_DONT_HAVE_ANY_MORE_PRESENTS_BUT_I_WILL_GIVE_YOU_CANDY_IF_YOU_WIN;

	/**
	 * ID: 1801043<br>
	 * Message: You're the best. Out of all the Jack's game players I've ever met... I give up!
	 */
	public static final NpcStringId YOURE_THE_BEST_OUT_OF_ALL_THE_JACKS_GAME_PLAYERS_IVE_EVER_MET_I_GIVE_UP;

	/**
	 * ID: 1801044<br>
	 * Message: Wowww. Awesome. Really. I have never met someone as good as you before. Now... I can't play anymore.
	 */
	public static final NpcStringId
			WOWWW_AWESOME_REALLY_I_HAVE_NEVER_MET_SOMEONE_AS_GOOD_AS_YOU_BEFORE_NOW_I_CANT_PLAY_ANYMORE;

	/**
	 * ID: 1801045<br>
	 * Message: $s1 has won $s2 Jack's games in a row.
	 */
	public static final NpcStringId S1_HAS_WON_S2_JACKS_GAMES_IN_A_ROW;

	/**
	 * ID: 1801046<br>
	 * Message: Congratulations! $s1 has won $s2 Jack's games in a row.
	 */
	public static final NpcStringId CONGRATULATIONS_S1_HAS_WON_S2_JACKS_GAMES_IN_A_ROW;

	/**
	 * ID: 1801047<br>
	 * Message: Congratulations on getting 1st place in Jack's game!
	 */
	public static final NpcStringId CONGRATULATIONS_ON_GETTING_1ST_PLACE_IN_JACKS_GAME;

	/**
	 * ID: 1801048<br>
	 * Message: Hello~! I'm Belldandy. Congratulations on winning 1st place in Jack's game... If you go and find my sibling Skooldie in the village, you'll get an amazing gift! Let's play Jack's game again!
	 */
	public static final NpcStringId
			HELLO_IM_BELLDANDY_CONGRATULATIONS_ON_WINNING_1ST_PLACE_IN_JACKS_GAME_IF_YOU_GO_AND_FIND_MY_SIBLING_SKOOLDIE_IN_THE_VILLAGE_YOULL_GET_AN_AMAZING_GIFT_LETS_PLAY_JACKS_GAME_AGAIN;

	/**
	 * ID: 1801049<br>
	 * Message: Hmm. You're playing Jack's game for the first time, huh? You couldn't even take out your card at the right time~! My goodness...
	 */
	public static final NpcStringId
			HMM_YOURE_PLAYING_JACKS_GAME_FOR_THE_FIRST_TIME_HUH_YOU_COULDNT_EVEN_TAKE_OUT_YOUR_CARD_AT_THE_RIGHT_TIME_MY_GOODNESS;

	/**
	 * ID: 1801050<br>
	 * Message: Oh. You're not very familiar with Jack's game, right? You didn't take out your card at the right time...
	 */
	public static final NpcStringId
			OH_YOURE_NOT_VERY_FAMILIAR_WITH_JACKS_GAME_RIGHT_YOU_DIDNT_TAKE_OUT_YOUR_CARD_AT_THE_RIGHT_TIME;

	/**
	 * ID: 1801051<br>
	 * Message: You have to use the card skill on the mask before the gauge above my head disappears.
	 */
	public static final NpcStringId
			YOU_HAVE_TO_USE_THE_CARD_SKILL_ON_THE_MASK_BEFORE_THE_GAUGE_ABOVE_MY_HEAD_DISAPPEARS;

	/**
	 * ID: 1801052<br>
	 * Message: You must use the card skill on the mask before the gauge above my head disappears.
	 */
	public static final NpcStringId YOU_MUST_USE_THE_CARD_SKILL_ON_THE_MASK_BEFORE_THE_GAUGE_ABOVE_MY_HEAD_DISAPPEARS;

	/**
	 * ID: 1801053<br>
	 * Message: If you show the same card as me, you win. If they're different, I win. Understand? Now, let's go!
	 */
	public static final NpcStringId
			IF_YOU_SHOW_THE_SAME_CARD_AS_ME_YOU_WIN_IF_THEYRE_DIFFERENT_I_WIN_UNDERSTAND_NOW_LETS_GO;

	/**
	 * ID: 1801054<br>
	 * Message: You will win if you show the same card as me. It's my victory if the cards are different. Well, let's start again~
	 */
	public static final NpcStringId
			YOU_WILL_WIN_IF_YOU_SHOW_THE_SAME_CARD_AS_ME_ITS_MY_VICTORY_IF_THE_CARDS_ARE_DIFFERENT_WELL_LETS_START_AGAIN;

	/**
	 * ID: 1801055<br>
	 * Message: Ack! You didn't show a card? You have to use the card skill before the gauge disappears. Hmph. Then, I'm going.
	 */
	public static final NpcStringId
			ACK_YOU_DIDNT_SHOW_A_CARD_YOU_HAVE_TO_USE_THE_CARD_SKILL_BEFORE_THE_GAUGE_DISAPPEARS_HMPH_THEN_IM_GOING;

	/**
	 * ID: 1801056<br>
	 * Message: Ahh. You didn't show a card. You must use the card skill at the right time. It's unfortunate. Then, I will go now~
	 */
	public static final NpcStringId
			AHH_YOU_DIDNT_SHOW_A_CARD_YOU_MUST_USE_THE_CARD_SKILL_AT_THE_RIGHT_TIME_ITS_UNFORTUNATE_THEN_I_WILL_GO_NOW;

	/**
	 * ID: 1801057<br>
	 * Message: Let's learn about the Jack's game together~! You can play with me 3 times.
	 */
	public static final NpcStringId LETS_LEARN_ABOUT_THE_JACKS_GAME_TOGETHER_YOU_CAN_PLAY_WITH_ME_3_TIMES;

	/**
	 * ID: 1801058<br>
	 * Message: Let's start! Show the card you want! The card skill is attached to the mask.
	 */
	public static final NpcStringId LETS_START_SHOW_THE_CARD_YOU_WANT_THE_CARD_SKILL_IS_ATTACHED_TO_THE_MASK;

	/**
	 * ID: 1801059<br>
	 * Message: You showed the same card as me, so you win.
	 */
	public static final NpcStringId YOU_SHOWED_THE_SAME_CARD_AS_ME_SO_YOU_WIN;

	/**
	 * ID: 1801060<br>
	 * Message: You showed a different card from me, so you lose.
	 */
	public static final NpcStringId YOU_SHOWED_A_DIFFERENT_CARD_FROM_ME_SO_YOU_LOSE;

	/**
	 * ID: 1801061<br>
	 * Message: That was practice, so there's no candy even if you win~
	 */
	public static final NpcStringId THAT_WAS_PRACTICE_SO_THERES_NO_CANDY_EVEN_IF_YOU_WIN;

	/**
	 * ID: 1801062<br>
	 * Message: It's unfortunate. Let's practice one more time.
	 */
	public static final NpcStringId ITS_UNFORTUNATE_LETS_PRACTICE_ONE_MORE_TIME;

	/**
	 * ID: 1801063<br>
	 * Message: You gotta show the card at the right time. Use the card skill you want before the gauge above my head disappears!
	 */
	public static final NpcStringId
			YOU_GOTTA_SHOW_THE_CARD_AT_THE_RIGHT_TIME_USE_THE_CARD_SKILL_YOU_WANT_BEFORE_THE_GAUGE_ABOVE_MY_HEAD_DISAPPEARS;

	/**
	 * ID: 1801064<br>
	 * Message: The card skills are attached to the Jack O'Lantern mask, right? That's what you use.
	 */
	public static final NpcStringId THE_CARD_SKILLS_ARE_ATTACHED_TO_THE_JACK_OLANTERN_MASK_RIGHT_THATS_WHAT_YOU_USE;

	/**
	 * ID: 1801065<br>
	 * Message: You win if you show the same card as me, and I win if the cards are different. OK, let's go~
	 */
	public static final NpcStringId
			YOU_WIN_IF_YOU_SHOW_THE_SAME_CARD_AS_ME_AND_I_WIN_IF_THE_CARDS_ARE_DIFFERENT_OK_LETS_GO;

	/**
	 * ID: 1801066<br>
	 * Message: You didn't show a card again? We'll try again later. I'm gonna go now~
	 */
	public static final NpcStringId YOU_DIDNT_SHOW_A_CARD_AGAIN_WELL_TRY_AGAIN_LATER_IM_GONNA_GO_NOW;

	/**
	 * ID: 1801067<br>
	 * Message: Now, do you understand a little about Jack's game? The real game's with Uldie and Belldandy. Well, see you later!
	 */
	public static final NpcStringId
			NOW_DO_YOU_UNDERSTAND_A_LITTLE_ABOUT_JACKS_GAME_THE_REAL_GAMES_WITH_ULDIE_AND_BELLDANDY_WELL_SEE_YOU_LATER;

	/**
	 * ID: 1801068<br>
	 * Message: Hahahaha!
	 */
	public static final NpcStringId HAHAHAHA;

	/**
	 * ID: 1801069<br>
	 * Message: Where are you looking?
	 */
	public static final NpcStringId WHERE_ARE_YOU_LOOKING;

	/**
	 * ID: 1801070<br>
	 * Message: I'm right here.
	 */
	public static final NpcStringId IM_RIGHT_HERE;

	/**
	 * ID: 1801071<br>
	 * Message: Annoying concentration attacks are disrupting Valakas's concentration!\\nIf it continues, you may get a great opportunity!
	 */
	public static final NpcStringId
			ANNOYING_CONCENTRATION_ATTACKS_ARE_DISRUPTING_VALAKASS_CONCENTRATIONNIF_IT_CONTINUES_YOU_MAY_GET_A_GREAT_OPPORTUNITY;

	/**
	 * ID: 1801072<br>
	 * Message: Some warrior's blow has left a huge gash between the great scales of Valakas!\\nValakas's P. Def. is greatly decreased!
	 */
	public static final NpcStringId
			SOME_WARRIORS_BLOW_HAS_LEFT_A_HUGE_GASH_BETWEEN_THE_GREAT_SCALES_OF_VALAKASNVALAKASS_P_DEF_IS_GREATLY_DECREASED;

	/**
	 * ID: 1801073<br>
	 * Message: Annoying concentration attacks overwhelmed Valakas, making it forget its rage and become distracted!
	 */
	public static final NpcStringId
			ANNOYING_CONCENTRATION_ATTACKS_OVERWHELMED_VALAKAS_MAKING_IT_FORGET_ITS_RAGE_AND_BECOME_DISTRACTED;

	/**
	 * ID: 1801074<br>
	 * Message: Long-range concentration attacks have enraged Valakas!\\nIf you continue, it may become a dangerous situation!
	 */
	public static final NpcStringId
			LONG_RANGE_CONCENTRATION_ATTACKS_HAVE_ENRAGED_VALAKASNIF_YOU_CONTINUE_IT_MAY_BECOME_A_DANGEROUS_SITUATION;

	/**
	 * ID: 1801075<br>
	 * Message: Because the cowardly counterattacks continued, Valakas's fury has reached its maximum!\\nValakas's P. Atk. is greatly increased!
	 */
	public static final NpcStringId
			BECAUSE_THE_COWARDLY_COUNTERATTACKS_CONTINUED_VALAKASS_FURY_HAS_REACHED_ITS_MAXIMUMNVALAKASS_P_ATK_IS_GREATLY_INCREASED;

	/**
	 * ID: 1801076<br>
	 * Message: Valakas has been enraged by the long-range concentration attacks and is coming toward its target with even greater zeal!
	 */
	public static final NpcStringId
			VALAKAS_HAS_BEEN_ENRAGED_BY_THE_LONG_RANGE_CONCENTRATION_ATTACKS_AND_IS_COMING_TOWARD_ITS_TARGET_WITH_EVEN_GREATER_ZEAL;

	/**
	 * ID: 1801077<br>
	 * Message: Listen, oh Tantas! I have returned! The Prophet Yugoros of the Black Abyss is with me, so do not be afraid!
	 */
	public static final NpcStringId
			LISTEN_OH_TANTAS_I_HAVE_RETURNED_THE_PROPHET_YUGOROS_OF_THE_BLACK_ABYSS_IS_WITH_ME_SO_DO_NOT_BE_AFRAID;

	/**
	 * ID: 1801078<br>
	 * Message: Welcome, $s1! Let us see if you have brought a worthy offering for the Black Abyss!
	 */
	public static final NpcStringId WELCOME_S1_LET_US_SEE_IF_YOU_HAVE_BROUGHT_A_WORTHY_OFFERING_FOR_THE_BLACK_ABYSS;

	/**
	 * ID: 1801079<br>
	 * Message: What a formidable foe! But I have the Abyss Weed given to me by the Black Abyss! Let me see...
	 */
	public static final NpcStringId
			WHAT_A_FORMIDABLE_FOE_BUT_I_HAVE_THE_ABYSS_WEED_GIVEN_TO_ME_BY_THE_BLACK_ABYSS_LET_ME_SEE;

	/**
	 * ID: 1801080<br>
	 * Message: Muhahaha! Ah, this burning sensation in my mouth! The power of the Black Abyss is reviving me!
	 */
	public static final NpcStringId
			MUHAHAHA_AH_THIS_BURNING_SENSATION_IN_MY_MOUTH_THE_POWER_OF_THE_BLACK_ABYSS_IS_REVIVING_ME;

	/**
	 * ID: 1801081<br>
	 * Message: No! How dare you stop me from using the Abyss Weed... Do you know what you have done?!
	 */
	public static final NpcStringId NO_HOW_DARE_YOU_STOP_ME_FROM_USING_THE_ABYSS_WEED_DO_YOU_KNOW_WHAT_YOU_HAVE_DONE;

	/**
	 * ID: 1801082<br>
	 * Message: A limp creature like this is unworthy to be an offering... You have no right to sacrifice to the Black Abyss!
	 */
	public static final NpcStringId
			A_LIMP_CREATURE_LIKE_THIS_IS_UNWORTHY_TO_BE_AN_OFFERING_YOU_HAVE_NO_RIGHT_TO_SACRIFICE_TO_THE_BLACK_ABYSS;

	/**
	 * ID: 1801083<br>
	 * Message: Listen, oh Tantas! The Black Abyss is famished! Find some fresh offerings!
	 */
	public static final NpcStringId LISTEN_OH_TANTAS_THE_BLACK_ABYSS_IS_FAMISHED_FIND_SOME_FRESH_OFFERINGS;

	/**
	 * ID: 1801084<br>
	 * Message: Ah... How could I lose... Oh, Black Abyss, receive me...
	 */
	public static final NpcStringId AH_HOW_COULD_I_LOSE_OH_BLACK_ABYSS_RECEIVE_ME;

	/**
	 * ID: 1801085<br>
	 * Message: Alarm system destroyed. Intruder excluded.
	 */
	public static final NpcStringId ALARM_SYSTEM_DESTROYED_INTRUDER_EXCLUDED;

	/**
	 * ID: 1801089<br>
	 * Message: Begin stage 4
	 */
	public static final NpcStringId BEGIN_STAGE_4;

	/**
	 * ID: 1801090<br>
	 * Message: Time remaining until next battle
	 */
	public static final NpcStringId TIME_REMAINING_UNTIL_NEXT_BATTLE;

	/**
	 * ID: 1801091<br>
	 * Message: The beast ate the feed, but it isn't showing a response, perhaps because it's already full.
	 */
	public static final NpcStringId
			THE_BEAST_ATE_THE_FEED_BUT_IT_ISNT_SHOWING_A_RESPONSE_PERHAPS_BECAUSE_ITS_ALREADY_FULL;

	/**
	 * ID: 1801092<br>
	 * Message: The beast spit out the feed instead of eating it.
	 */
	public static final NpcStringId THE_BEAST_SPIT_OUT_THE_FEED_INSTEAD_OF_EATING_IT;

	/**
	 * ID: 1801093<br>
	 * Message: The beast spit out the feed and is instead attacking you!
	 */
	public static final NpcStringId THE_BEAST_SPIT_OUT_THE_FEED_AND_IS_INSTEAD_ATTACKING_YOU;

	/**
	 * ID: 1801094<br>
	 * Message: $s1 is leaving you because you don't have enough Golden Spices.
	 */
	public static final NpcStringId S1_IS_LEAVING_YOU_BECAUSE_YOU_DONT_HAVE_ENOUGH_GOLDEN_SPICES;

	/**
	 * ID: 1801095<br>
	 * Message: $s1 is leaving you because you don't have enough Crystal Spices.
	 */
	public static final NpcStringId S1_IS_LEAVING_YOU_BECAUSE_YOU_DONT_HAVE_ENOUGH_CRYSTAL_SPICES;

	/**
	 * ID: 1801096<br>
	 * Message: $s1. May the protection of the gods be upon you!
	 */
	public static final NpcStringId S1_MAY_THE_PROTECTION_OF_THE_GODS_BE_UPON_YOU;

	/**
	 * ID: 1801097<br>
	 * Message: Freya has started to move.
	 */
	public static final NpcStringId FREYA_HAS_STARTED_TO_MOVE;

	/**
	 * ID: 1801098<br>
	 * Message: How could I... fall... in a place like this...
	 */
	public static final NpcStringId HOW_COULD_I_FALL_IN_A_PLACE_LIKE_THIS;

	/**
	 * ID: 1801099<br>
	 * Message: I can finally take a breather. By the way, who are you? Hmm... I think I know who sent you.
	 */
	public static final NpcStringId
			I_CAN_FINALLY_TAKE_A_BREATHER_BY_THE_WAY_WHO_ARE_YOU_HMM_I_THINK_I_KNOW_WHO_SENT_YOU;

	/**
	 * ID: 1801100<br>
	 * Message: $s1 of Balance
	 */
	public static final NpcStringId S1_OF_BALANCE;

	/**
	 * ID: 1801101<br>
	 * Message: Swift $s1
	 */
	public static final NpcStringId SWIFT_S2;

	/**
	 * ID: 1801102<br>
	 * Message: $s1 of Blessing
	 */
	public static final NpcStringId S1_OF_BLESSING;

	/**
	 * ID: 1801103<br>
	 * Message: Sharp $s1
	 */
	public static final NpcStringId SHARP_S2;

	/**
	 * ID: 1801104<br>
	 * Message: Useful $s1
	 */
	public static final NpcStringId USEFUL_S2;

	/**
	 * ID: 1801105<br>
	 * Message: Reckless $s1
	 */
	public static final NpcStringId RECKLESS_S2;

	/**
	 * ID: 1801106<br>
	 * Message: Alpen Kookaburra
	 */
	public static final NpcStringId ALPEN_KOOKABURRA;

	/**
	 * ID: 1801107<br>
	 * Message: Alpen Cougar
	 */
	public static final NpcStringId ALPEN_COUGAR;

	/**
	 * ID: 1801108<br>
	 * Message: Alpen Buffalo
	 */
	public static final NpcStringId ALPEN_BUFFALO;

	/**
	 * ID: 1801109<br>
	 * Message: Alpen Grendel
	 */
	public static final NpcStringId ALPEN_GRENDEL;

	/**
	 * ID: 1801110<br>
	 * Message: Battle end limit time
	 */
	public static final NpcStringId BATTLE_END_LIMIT_TIME;

	/**
	 * ID: 1801111<br>
	 * Message: Strong magic power can be felt from somewhere!!
	 */
	public static final NpcStringId STRONG_MAGIC_POWER_CAN_BE_FELT_FROM_SOMEWHERE;

	/**
	 * ID: 1801112<br>
	 * Message: How dare you attack my recruits!!
	 */
	public static final NpcStringId HOW_DARE_YOU_ATTACK_MY_RECRUITS;

	/**
	 * ID: 1801113<br>
	 * Message: Who is disrupting the order?!
	 */
	public static final NpcStringId WHO_IS_DISRUPTING_THE_ORDER;

	/**
	 * ID: 1801114<br>
	 * Message: The drillmaster is dead!
	 */
	public static final NpcStringId THE_DRILLMASTER_IS_DEAD;

	/**
	 * ID: 1801115<br>
	 * Message: Line up the ranks!!
	 */
	public static final NpcStringId LINE_UP_THE_RANKS;

	/**
	 * ID: 1801116<br>
	 * Message: I brought the food.
	 */
	public static final NpcStringId I_BROUGHT_THE_FOOD;

	/**
	 * ID: 1801117<br>
	 * Message: Come and eat.
	 */
	public static final NpcStringId COME_AND_EAT;

	/**
	 * ID: 1801118<br>
	 * Message: Looks delicious.
	 */
	public static final NpcStringId LOOKS_DELICIOUS;

	/**
	 * ID: 1801119<br>
	 * Message: Let's go eat.
	 */
	public static final NpcStringId LETS_GO_EAT;

	/**
	 * ID: 1801120<br>
	 * Message: Archer. Give your breath for the intruder!
	 */
	public static final NpcStringId ARCHER_GIVE_YOUR_BREATH_FOR_THE_INTRUDER;

	/**
	 * ID: 1801121<br>
	 * Message: My knights. Show your loyalty!!
	 */
	public static final NpcStringId MY_KNIGHTS_SHOW_YOUR_LOYALTY;

	/**
	 * ID: 1801122<br>
	 * Message: I can take it no longer!!!
	 */
	public static final NpcStringId I_CAN_TAKE_IT_NO_LONGER;

	/**
	 * ID: 1801123<br>
	 * Message: Archer. Heed my call!
	 */
	public static final NpcStringId ARCHER_HEED_MY_CALL;

	/**
	 * ID: 1801124<br>
	 * Message: The space feels like its gradually starting to shake.
	 */
	public static final NpcStringId THE_SPACE_FEELS_LIKE_ITS_GRADUALLY_STARTING_TO_SHAKE;

	/**
	 * ID: 1801125<br>
	 * Message: I can no longer stand by.
	 */
	public static final NpcStringId I_CAN_NO_LONGER_STAND_BY;

	/**
	 * ID: 1801126<br>
	 * Message: Taklacan is gathering its strength and launching an attack.
	 */
	public static final NpcStringId TAKLACAN_IS_GATHERING_ITS_STRENGTH_AND_LAUNCHING_AN_ATTACK;

	/**
	 * ID: 1801127<br>
	 * Message: Taklacan received Cokrakon and became weaker.
	 */
	public static final NpcStringId TAKLACAN_RECEIVED_COKRAKON_AND_BECAME_WEAKER;

	/**
	 * ID: 1801128<br>
	 * Message: Cokrakon's power can be felt nearby.
	 */
	public static final NpcStringId COKRAKONS_POWER_CAN_BE_FELT_NEARBY;

	/**
	 * ID: 1801129<br>
	 * Message: Taklacan is preparing to hide itself.
	 */
	public static final NpcStringId TAKLACAN_IS_PREPARING_TO_HIDE_ITSELF;

	/**
	 * ID: 1801130<br>
	 * Message: Taklacan disappears into the space of futility with a roar.
	 */
	public static final NpcStringId TAKLACAN_DISAPPEARS_INTO_THE_SPACE_OF_FUTILITY_WITH_A_ROAR;

	/**
	 * ID: 1801131<br>
	 * Message: Torumba's body is starting to move.
	 */
	public static final NpcStringId TORUMBAS_BODY_IS_STARTING_TO_MOVE;

	/**
	 * ID: 1801132<br>
	 * Message: A response can be felt from Torumba's tough skin.
	 */
	public static final NpcStringId A_RESPONSE_CAN_BE_FELT_FROM_TORUMBAS_TOUGH_SKIN;

	/**
	 * ID: 1801133<br>
	 * Message: A mark remains on Torumba's tough skin.
	 */
	public static final NpcStringId A_MARK_REMAINS_ON_TORUMBAS_TOUGH_SKIN;

	/**
	 * ID: 1801134<br>
	 * Message: The light is beginning to weaken in Torumba's eyes.
	 */
	public static final NpcStringId THE_LIGHT_IS_BEGINNING_TO_WEAKEN_IN_TORUMBAS_EYES;

	/**
	 * ID: 1801135<br>
	 * Message: Torumba's left leg was damaged.
	 */
	public static final NpcStringId TORUMBAS_LEFT_LEG_WAS_DAMAGED;

	/**
	 * ID: 1801136<br>
	 * Message: Torumba's right leg was damaged.
	 */
	public static final NpcStringId TORUMBAS_RIGHT_LEG_WAS_DAMAGED;

	/**
	 * ID: 1801137<br>
	 * Message: Torumba's left arm was damaged.
	 */
	public static final NpcStringId TORUMBAS_LEFT_ARM_WAS_DAMAGED;

	/**
	 * ID: 1801138<br>
	 * Message: Torumba's right arm was damaged.
	 */
	public static final NpcStringId TORUMBAS_RIGHT_ARM_WAS_DAMAGED;

	/**
	 * ID: 1801139<br>
	 * Message: A deep wound appeared on Torumba's tough skin.
	 */
	public static final NpcStringId A_DEEP_WOUND_APPEARED_ON_TORUMBAS_TOUGH_SKIN;

	/**
	 * ID: 1801140<br>
	 * Message: The light is slowly fading from Torumba's eyes.
	 */
	public static final NpcStringId THE_LIGHT_IS_SLOWLY_FADING_FROM_TORUMBAS_EYES;

	/**
	 * ID: 1801141<br>
	 * Message: Torumba is preparing to hide its body.
	 */
	public static final NpcStringId TORUMBA_IS_PREPARING_TO_HIDE_ITS_BODY;

	/**
	 * ID: 1801142<br>
	 * Message: Torumba disappeared into his space.
	 */
	public static final NpcStringId TORUMBA_DISAPPEARED_INTO_HIS_SPACE;

	/**
	 * ID: 1801143<br>
	 * Message: Torumba is preparing to hide itself in the twisted space.
	 */
	public static final NpcStringId TORUMBA_IS_PREPARING_TO_HIDE_ITSELF_IN_THE_TWISTED_SPACE;

	/**
	 * ID: 1801144<br>
	 * Message: Dopagen has started to move.
	 */
	public static final NpcStringId DOPAGEN_HAS_STARTED_TO_MOVE;

	/**
	 * ID: 1801145<br>
	 * Message: Leptilikon's energy feels like it's being condensed.
	 */
	public static final NpcStringId LEPTILIKONS_ENERGY_FEELS_LIKE_ITS_BEING_CONDENSED;

	/**
	 * ID: 1801146<br>
	 * Message: Dopagen is preparing to hide itself with a strange scent.
	 */
	public static final NpcStringId DOPAGEN_IS_PREPARING_TO_HIDE_ITSELF_WITH_A_STRANGE_SCENT;

	/**
	 * ID: 1801147<br>
	 * Message: Dopagen is preparing to hide itself.
	 */
	public static final NpcStringId DOPAGEN_IS_PREPARING_TO_HIDE_ITSELF;

	/**
	 * ID: 1801148<br>
	 * Message: Dopagen is disappearing in between the twisted space.
	 */
	public static final NpcStringId DOPAGEN_IS_DISAPPEARING_IN_BETWEEN_THE_TWISTED_SPACE;

	/**
	 * ID: 1801149<br>
	 * Message: Maguen appearance!!!
	 */
	public static final NpcStringId MAGUEN_APPEARANCE;

	/**
	 * ID: 1801150<br>
	 * Message: Enough Maguen Plasma - Bistakon have gathered.
	 */
	public static final NpcStringId ENOUGH_MAGUEN_PLASMA_BISTAKON_HAVE_GATHERED;

	/**
	 * ID: 1801151<br>
	 * Message: Enough Maguen Plasma - Cokrakon have gathered.
	 */
	public static final NpcStringId ENOUGH_MAGUEN_PLASMA_COKRAKON_HAVE_GATHERED;

	/**
	 * ID: 1801152<br>
	 * Message: Enough Maguen Plasma - Leptilikon have gathered.
	 */
	public static final NpcStringId ENOUGH_MAGUEN_PLASMA_LEPTILIKON_HAVE_GATHERED;

	/**
	 * ID: 1801153<br>
	 * Message: The plasmas have filled the aeroscope and are harmonized.
	 */
	public static final NpcStringId THE_PLASMAS_HAVE_FILLED_THE_AEROSCOPE_AND_ARE_HARMONIZED;

	/**
	 * ID: 1801154<br>
	 * Message: The plasmas have filled the aeroscope but they are ramming into each other, exploding, and dying.
	 */
	public static final NpcStringId
			THE_PLASMAS_HAVE_FILLED_THE_AEROSCOPE_BUT_THEY_ARE_RAMMING_INTO_EACH_OTHER_EXPLODING_AND_DYING;

	/**
	 * ID: 1801155<br>
	 * Message: Amazing. $s1 took 100 of these soul stone fragments. What a complete swindler.
	 */
	public static final NpcStringId AMAZING_S1_TOOK_100_OF_THESE_SOUL_STONE_FRAGMENTS_WHAT_A_COMPLETE_SWINDLER;

	/**
	 * ID: 1801156<br>
	 * Message: Hmm? Hey, did you give $s1 something? But it was just 1. Haha.
	 */
	public static final NpcStringId HMM_HEY_DID_YOU_GIVE_S1_SOMETHING_BUT_IT_WAS_JUST_1_HAHA;

	/**
	 * ID: 1801157<br>
	 * Message: $s1 pulled one with $s2 digits. Lucky~ Not bad~
	 */
	public static final NpcStringId S1_PULLED_ONE_WITH_S2_DIGITS_LUCKY_NOT_BAD;

	/**
	 * ID: 1801158<br>
	 * Message: It's better than losing it all, right? Or does this feel worse?
	 */
	public static final NpcStringId ITS_BETTER_THAN_LOSING_IT_ALL_RIGHT_OR_DOES_THIS_FEEL_WORSE;

	/**
	 * ID: 1801159<br>
	 * Message: Ahem~!  $s1 has no luck at all. Try praying.
	 */
	public static final NpcStringId AHEM_S1_HAS_NO_LUCK_AT_ALL_TRY_PRAYING;

	/**
	 * ID: 1801160<br>
	 * Message: Ah... It's over. What kind of guy is that? Damn... Fine, you $s1, take it and get outta here.
	 */
	public static final NpcStringId AH_ITS_OVER_WHAT_KIND_OF_GUY_IS_THAT_DAMN_FINE_YOU_S1_TAKE_IT_AND_GET_OUTTA_HERE;

	/**
	 * ID: 1801161<br>
	 * Message: A big piece is made up of little pieces. So here's a little piece~
	 */
	public static final NpcStringId A_BIG_PIECE_IS_MADE_UP_OF_LITTLE_PIECES_SO_HERES_A_LITTLE_PIECE;

	/**
	 * ID: 1801162<br>
	 * Message: You don't feel bad, right? Are you sad? But don't cry~
	 */
	public static final NpcStringId YOU_DONT_FEEL_BAD_RIGHT_ARE_YOU_SAD_BUT_DONT_CRY;

	/**
	 * ID: 1801163<br>
	 * Message: OK~ Who's next? It all depends on your fate and luck, right? At least come and take a look.
	 */
	public static final NpcStringId
			OK_WHOS_NEXT_IT_ALL_DEPENDS_ON_YOUR_FATE_AND_LUCK_RIGHT_AT_LEAST_COME_AND_TAKE_A_LOOK;

	/**
	 * ID: 1801164<br>
	 * Message: No one else? Don't worry~ I don't bite. Haha~!
	 */
	public static final NpcStringId NO_ONE_ELSE_DONT_WORRY_I_DONT_BITE_HAHA;

	/**
	 * ID: 1801165<br>
	 * Message: There was someone who won 10,000 from me. A warrior shouldn't just be good at fighting, right? You've gotta be good in everything.
	 */
	public static final NpcStringId
			THERE_WAS_SOMEONE_WHO_WON_10000_FROM_ME_A_WARRIOR_SHOULDNT_JUST_BE_GOOD_AT_FIGHTING_RIGHT_YOUVE_GOTTA_BE_GOOD_IN_EVERYTHING;

	/**
	 * ID: 1801166<br>
	 * Message: OK~ Master of luck? That's you? Haha~! Well, anyone can come after all.
	 */
	public static final NpcStringId OK_MASTER_OF_LUCK_THATS_YOU_HAHA_WELL_ANYONE_CAN_COME_AFTER_ALL;

	/**
	 * ID: 1801167<br>
	 * Message: Shedding blood is a given on the battlefield. At least it's safe here.
	 */
	public static final NpcStringId SHEDDING_BLOOD_IS_A_GIVEN_ON_THE_BATTLEFIELD_AT_LEAST_ITS_SAFE_HERE;

	/**
	 * ID: 1801168<br>
	 * Message: Gasp!
	 */
	public static final NpcStringId GASP;

	/**
	 * ID: 1801169<br>
	 * Message: Is it still long off?
	 */
	public static final NpcStringId IS_IT_STILL_LONG_OFF;

	/**
	 * ID: 1801170<br>
	 * Message: Is Ermian well? Even I can't believe that I survived in a place like this...
	 */
	public static final NpcStringId IS_ERMIAN_WELL_EVEN_I_CANT_BELIEVE_THAT_I_SURVIVED_IN_A_PLACE_LIKE_THIS;

	/**
	 * ID: 1801171<br>
	 * Message: I don't know how long it's been since I parted company with you... Time doesn't seem to move... It just feels too long...
	 */
	public static final NpcStringId
			I_DONT_KNOW_HOW_LONG_ITS_BEEN_SINCE_I_PARTED_COMPANY_WITH_YOU_TIME_DOESNT_SEEM_TO_MOVE_IT_JUST_FEELS_TOO_LONG;

	/**
	 * ID: 1801172<br>
	 * Message: Sorry to say this, but... The place you struck me before now hurts greatly...
	 */
	public static final NpcStringId SORRY_TO_SAY_THIS_BUT_THE_PLACE_YOU_STRUCK_ME_BEFORE_NOW_HURTS_GREATLY;

	/**
	 * ID: 1801173<br>
	 * Message: Ugh... I'm sorry... It looks like this is it for me... I wanted to live and see my family...
	 */
	public static final NpcStringId UGH_IM_SORRY_IT_LOOKS_LIKE_THIS_IS_IT_FOR_ME_I_WANTED_TO_LIVE_AND_SEE_MY_FAMILY;

	/**
	 * ID: 1801174<br>
	 * Message: Where are you? I can't see anything.
	 */
	public static final NpcStringId WHERE_ARE_YOU_I_CANT_SEE_ANYTHING;

	/**
	 * ID: 1801175<br>
	 * Message: Where are you, really? I can't follow you like this.
	 */
	public static final NpcStringId WHERE_ARE_YOU_REALLY_I_CANT_FOLLOW_YOU_LIKE_THIS;

	/**
	 * ID: 1801176<br>
	 * Message: I'm sorry... This is it for me.
	 */
	public static final NpcStringId IM_SORRY_THIS_IS_IT_FOR_ME;

	/**
	 * ID: 1801177<br>
	 * Message: Sob~ To see Ermian again... Can I go to my family now?
	 */
	public static final NpcStringId SOB_TO_SEE_ERMIAN_AGAIN_CAN_I_GO_TO_MY_FAMILY_NOW;

	/**
	 * ID: 1801183<br>
	 * Message: My energy was recovered so quickly. Thanks, $s1~
	 */
	public static final NpcStringId MY_ENERGY_WAS_RECOVERED_SO_QUICKLY_THANKS_S1;

	/**
	 * ID: 1801187<br>
	 * Message: I'm starving!
	 */
	public static final NpcStringId IM_STARVING;

	/**
	 * ID: 1801189<br>
	 * Message: Magic power so strong that it could make you lose your mind can be felt from somewhere!!
	 */
	public static final NpcStringId
			MAGIC_POWER_SO_STRONG_THAT_IT_COULD_MAKE_YOU_LOSE_YOUR_MIND_CAN_BE_FELT_FROM_SOMEWHERE;

	/**
	 * ID: 1801190<br>
	 * Message: Even though you bring something called a gift among your humans, it would just be problematic for me...
	 */
	public static final NpcStringId
			EVEN_THOUGH_YOU_BRING_SOMETHING_CALLED_A_GIFT_AMONG_YOUR_HUMANS_IT_WOULD_JUST_BE_PROBLEMATIC_FOR_ME;

	/**
	 * ID: 1801191<br>
	 * Message: I just don't know what expression I should have it appeared on me. Are human's emotions like this feeling?
	 */
	public static final NpcStringId
			I_JUST_DONT_KNOW_WHAT_EXPRESSION_I_SHOULD_HAVE_IT_APPEARED_ON_ME_ARE_HUMANS_EMOTIONS_LIKE_THIS_FEELING;

	/**
	 * ID: 1801192<br>
	 * Message: The feeling of thanks is just too much distant memory for me...
	 */
	public static final NpcStringId THE_FEELING_OF_THANKS_IS_JUST_TOO_MUCH_DISTANT_MEMORY_FOR_ME;

	/**
	 * ID: 1801193<br>
	 * Message: But I kind of miss it... Like I had felt this feeling before...
	 */
	public static final NpcStringId BUT_I_KIND_OF_MISS_IT_LIKE_I_HAD_FELT_THIS_FEELING_BEFORE;

	/**
	 * ID: 1801194<br>
	 * Message: I am Ice Queen Freya... This feeling and emotion are nothing but a part of Melissa'a memories.
	 */
	public static final NpcStringId
			I_AM_ICE_QUEEN_FREYA_THIS_FEELING_AND_EMOTION_ARE_NOTHING_BUT_A_PART_OF_MELISSAA_MEMORIES;

	/**
	 * ID: 1801195<br>
	 * Message: Dear $s1... Think of this as my appreciation for the gift. Take this with you. There's nothing strange about it. It's just a bit of my capriciousness...
	 */
	public static final NpcStringId
			DEAR_S1_THINK_OF_THIS_AS_MY_APPRECIATION_FOR_THE_GIFT_TAKE_THIS_WITH_YOU_THERES_NOTHING_STRANGE_ABOUT_IT_ITS_JUST_A_BIT_OF_MY_CAPRICIOUSNESS;

	/**
	 * ID: 1801196<br>
	 * Message: The kindness of somebody is not a bad feeling... Dear $s1, I will take this gift out of respect your kindness.
	 */
	public static final NpcStringId
			THE_KINDNESS_OF_SOMEBODY_IS_NOT_A_BAD_FEELING_DEAR_S1_I_WILL_TAKE_THIS_GIFT_OUT_OF_RESPECT_YOUR_KINDNESS;

	/**
	 * ID: 1811000<br>
	 * Message: Fighter
	 */
	public static final NpcStringId FIGHTER;

	/**
	 * ID: 1811001<br>
	 * Message: Warrior
	 */
	public static final NpcStringId WARRIOR;

	/**
	 * ID: 1811002<br>
	 * Message: Gladiator
	 */
	public static final NpcStringId GLADIATOR;

	/**
	 * ID: 1811003<br>
	 * Message: Warlord
	 */
	public static final NpcStringId WARLORD;

	/**
	 * ID: 1811004<br>
	 * Message: Knight
	 */
	public static final NpcStringId KNIGHT;

	/**
	 * ID: 1811005<br>
	 * Message: Paladin
	 */
	public static final NpcStringId PALADIN;

	/**
	 * ID: 1811006<br>
	 * Message: Dark Avenger
	 */
	public static final NpcStringId DARK_AVENGER;

	/**
	 * ID: 1811007<br>
	 * Message: Rogue
	 */
	public static final NpcStringId ROGUE;

	/**
	 * ID: 1811008<br>
	 * Message: Treasure Hunter
	 */
	public static final NpcStringId TREASURE_HUNTER;

	/**
	 * ID: 1811009<br>
	 * Message: Hawkeye
	 */
	public static final NpcStringId HAWKEYE;

	/**
	 * ID: 1811010<br>
	 * Message: Mage
	 */
	public static final NpcStringId MAGE;

	/**
	 * ID: 1811011<br>
	 * Message: Wizard
	 */
	public static final NpcStringId WIZARD;

	/**
	 * ID: 1811012<br>
	 * Message: Sorcerer
	 */
	public static final NpcStringId SORCERER;

	/**
	 * ID: 1811013<br>
	 * Message: Necromancer
	 */
	public static final NpcStringId NECROMANCER;

	/**
	 * ID: 1811014<br>
	 * Message: Warlock
	 */
	public static final NpcStringId WARLOCK;

	/**
	 * ID: 1811015<br>
	 * Message: Cleric
	 */
	public static final NpcStringId CLERIC;

	/**
	 * ID: 1811016<br>
	 * Message: Bishop
	 */
	public static final NpcStringId BISHOP;

	/**
	 * ID: 1811017<br>
	 * Message: Prophet
	 */
	public static final NpcStringId PROPHET;

	/**
	 * ID: 1811018<br>
	 * Message: Elven Fighter
	 */
	public static final NpcStringId ELVEN_FIGHTER;

	/**
	 * ID: 1811019<br>
	 * Message: Elven Knight
	 */
	public static final NpcStringId ELVEN_KNIGHT;

	/**
	 * ID: 1811020<br>
	 * Message: Temple Knight
	 */
	public static final NpcStringId TEMPLE_KNIGHT;

	/**
	 * ID: 1811021<br>
	 * Message: Sword Singer
	 */
	public static final NpcStringId SWORD_SINGER;

	/**
	 * ID: 1811022<br>
	 * Message: Elven Scout
	 */
	public static final NpcStringId ELVEN_SCOUT;

	/**
	 * ID: 1811023<br>
	 * Message: Plain Walker
	 */
	public static final NpcStringId PLAIN_WALKER;

	/**
	 * ID: 1811024<br>
	 * Message: Silver Ranger
	 */
	public static final NpcStringId SILVER_RANGER;

	/**
	 * ID: 1811025<br>
	 * Message: Elven Mage
	 */
	public static final NpcStringId ELVEN_MAGE;

	/**
	 * ID: 1811026<br>
	 * Message: Elven Wizard
	 */
	public static final NpcStringId ELVEN_WIZARD;

	/**
	 * ID: 1811027<br>
	 * Message: Spell Singer
	 */
	public static final NpcStringId SPELL_SINGER;

	/**
	 * ID: 1811028<br>
	 * Message: Elemental Summoner
	 */
	public static final NpcStringId ELEMENTAL_SUMMONER;

	/**
	 * ID: 1811029<br>
	 * Message: Oracle
	 */
	public static final NpcStringId ORACLE;

	/**
	 * ID: 1811030<br>
	 * Message: Elder
	 */
	public static final NpcStringId ELDER;

	/**
	 * ID: 1811031<br>
	 * Message: Dark Fighter
	 */
	public static final NpcStringId DARK_FIGHTER;

	/**
	 * ID: 1811032<br>
	 * Message: Palace Knight
	 */
	public static final NpcStringId PALACE_KNIGHT;

	/**
	 * ID: 1811033<br>
	 * Message: Shillien Knight
	 */
	public static final NpcStringId SHILLIEN_KNIGHT;

	/**
	 * ID: 1811034<br>
	 * Message: Blade Dancer
	 */
	public static final NpcStringId BLADE_DANCER;

	/**
	 * ID: 1811035<br>
	 * Message: Assassin
	 */
	public static final NpcStringId ASSASSIN;

	/**
	 * ID: 1811036<br>
	 * Message: Abyss Walker
	 */
	public static final NpcStringId ABYSS_WALKER;

	/**
	 * ID: 1811037<br>
	 * Message: Phantom Ranger
	 */
	public static final NpcStringId PHANTOM_RANGER;

	/**
	 * ID: 1811038<br>
	 * Message: Dark Mage
	 */
	public static final NpcStringId DARK_MAGE;

	/**
	 * ID: 1811039<br>
	 * Message: Dark Wizard
	 */
	public static final NpcStringId DARK_WIZARD;

	/**
	 * ID: 1811040<br>
	 * Message: Spellhowler
	 */
	public static final NpcStringId SPELLHOWLER;

	/**
	 * ID: 1811041<br>
	 * Message: Phantom Summoner
	 */
	public static final NpcStringId PHANTOM_SUMMONER;

	/**
	 * ID: 1811042<br>
	 * Message: Shillien Oracle
	 */
	public static final NpcStringId SHILLIEN_ORACLE;

	/**
	 * ID: 1811043<br>
	 * Message: Shillien Elder
	 */
	public static final NpcStringId SHILLIEN_ELDER;

	/**
	 * ID: 1811044<br>
	 * Message: Orc Fighter
	 */
	public static final NpcStringId ORC_FIGHTER;

	/**
	 * ID: 1811045<br>
	 * Message: Orc Raider
	 */
	public static final NpcStringId ORC_RAIDER;

	/**
	 * ID: 1811046<br>
	 * Message: Destroyer
	 */
	public static final NpcStringId DESTROYER;

	/**
	 * ID: 1811047<br>
	 * Message: Orc Monk
	 */
	public static final NpcStringId ORC_MONK;

	/**
	 * ID: 1811048<br>
	 * Message: Tyrant
	 */
	public static final NpcStringId TYRANT;

	/**
	 * ID: 1811049<br>
	 * Message: Orc Mage
	 */
	public static final NpcStringId ORC_MAGE;

	/**
	 * ID: 1811050<br>
	 * Message: Orc Shaman
	 */
	public static final NpcStringId ORC_SHAMAN;

	/**
	 * ID: 1811051<br>
	 * Message: Overlord
	 */
	public static final NpcStringId OVERLORD;

	/**
	 * ID: 1811052<br>
	 * Message: Warcryer
	 */
	public static final NpcStringId WARCRYER;

	/**
	 * ID: 1811053<br>
	 * Message: Dwarven Fighter
	 */
	public static final NpcStringId DWARVEN_FIGHTER;

	/**
	 * ID: 1811054<br>
	 * Message: Scavenger
	 */
	public static final NpcStringId SCAVENGER;

	/**
	 * ID: 1811055<br>
	 * Message: Bounty Hunter
	 */
	public static final NpcStringId BOUNTY_HUNTER;

	/**
	 * ID: 1811056<br>
	 * Message: Artisan
	 */
	public static final NpcStringId ARTISAN;

	/**
	 * ID: 1811057<br>
	 * Message: Warsmith
	 */
	public static final NpcStringId WARSMITH;

	/**
	 * ID: 1811088<br>
	 * Message: Duelist
	 */
	public static final NpcStringId DUELIST;

	/**
	 * ID: 1811089<br>
	 * Message: Dreadnought
	 */
	public static final NpcStringId DREADNOUGHT;

	/**
	 * ID: 1811090<br>
	 * Message: Phoenix Knight
	 */
	public static final NpcStringId PHOENIX_KNIGHT;

	/**
	 * ID: 1811091<br>
	 * Message: Hell Knight
	 */
	public static final NpcStringId HELL_KNIGHT;

	/**
	 * ID: 1811092<br>
	 * Message: Sagittarius
	 */
	public static final NpcStringId SAGITTARIUS;

	/**
	 * ID: 1811093<br>
	 * Message: Adventurer
	 */
	public static final NpcStringId ADVENTURER;

	/**
	 * ID: 1811094<br>
	 * Message: Archmage
	 */
	public static final NpcStringId ARCHMAGE;

	/**
	 * ID: 1811095<br>
	 * Message: Soultaker
	 */
	public static final NpcStringId SOULTAKER;

	/**
	 * ID: 1811096<br>
	 * Message: Arcana Lord
	 */
	public static final NpcStringId ARCANA_LORD;

	/**
	 * ID: 1811097<br>
	 * Message: Cardinal
	 */
	public static final NpcStringId CARDINAL;

	/**
	 * ID: 1811098<br>
	 * Message: Hierophant
	 */
	public static final NpcStringId HIEROPHANT;

	/**
	 * ID: 1811099<br>
	 * Message: Eva's Templar
	 */
	public static final NpcStringId EVAS_TEMPLAR;

	/**
	 * ID: 1811100<br>
	 * Message: Sword Muse
	 */
	public static final NpcStringId SWORD_MUSE;

	/**
	 * ID: 1811101<br>
	 * Message: Wind Rider
	 */
	public static final NpcStringId WIND_RIDER;

	/**
	 * ID: 1811102<br>
	 * Message: Moonlight Sentinel
	 */
	public static final NpcStringId MOONLIGHT_SENTINEL;

	/**
	 * ID: 1811103<br>
	 * Message: Mystic Muse
	 */
	public static final NpcStringId MYSTIC_MUSE;

	/**
	 * ID: 1811104<br>
	 * Message: Elemental Master
	 */
	public static final NpcStringId ELEMENTAL_MASTER;

	/**
	 * ID: 1811105<br>
	 * Message: Eva's Saint
	 */
	public static final NpcStringId EVAS_SAINT;

	/**
	 * ID: 1811106<br>
	 * Message: Shillien Templar
	 */
	public static final NpcStringId SHILLIEN_TEMPLAR;

	/**
	 * ID: 1811107<br>
	 * Message: Spectral Dancer
	 */
	public static final NpcStringId SPECTRAL_DANCER;

	/**
	 * ID: 1811108<br>
	 * Message: Ghost Hunter
	 */
	public static final NpcStringId GHOST_HUNTER;

	/**
	 * ID: 1811109<br>
	 * Message: Ghost Sentinel
	 */
	public static final NpcStringId GHOST_SENTINEL;

	/**
	 * ID: 1811110<br>
	 * Message: Storm Screamer
	 */
	public static final NpcStringId STORM_SCREAMER;

	/**
	 * ID: 1811111<br>
	 * Message: Spectral Master
	 */
	public static final NpcStringId SPECTRAL_MASTER;

	/**
	 * ID: 1811112<br>
	 * Message: Shillien Saint
	 */
	public static final NpcStringId SHILLIEN_SAINT;

	/**
	 * ID: 1811113<br>
	 * Message: Titan
	 */
	public static final NpcStringId TITAN;

	/**
	 * ID: 1811114<br>
	 * Message: Grand Khavatari
	 */
	public static final NpcStringId GRAND_KHAVATARI;

	/**
	 * ID: 1811115<br>
	 * Message: Dominator
	 */
	public static final NpcStringId DOMINATOR;

	/**
	 * ID: 1811116<br>
	 * Message: Doom Cryer
	 */
	public static final NpcStringId DOOM_CRYER;

	/**
	 * ID: 1811117<br>
	 * Message: Fortune Seeker
	 */
	public static final NpcStringId FORTUNE_SEEKER;

	/**
	 * ID: 1811118<br>
	 * Message: Maestro
	 */
	public static final NpcStringId MAESTRO;

	/**
	 * ID: 1811123<br>
	 * Message: Kamael Soldier
	 */
	public static final NpcStringId KAMAEL_SOLDIER;

	/**
	 * ID: 1811125<br>
	 * Message: Trooper
	 */
	public static final NpcStringId TROOPER;

	/**
	 * ID: 1811126<br>
	 * Message: Warder
	 */
	public static final NpcStringId WARDER;

	/**
	 * ID: 1811127<br>
	 * Message: Berserker
	 */
	public static final NpcStringId BERSERKER;

	/**
	 * ID: 1811128<br>
	 * Message: Soul Breaker
	 */
	public static final NpcStringId SOUL_BREAKER;

	/**
	 * ID: 1811130<br>
	 * Message: Arbalester
	 */
	public static final NpcStringId ARBALESTER;

	/**
	 * ID: 1811131<br>
	 * Message: Doombringer
	 */
	public static final NpcStringId DOOMBRINGER;

	/**
	 * ID: 1811132<br>
	 * Message: Soul Hound
	 */
	public static final NpcStringId SOUL_HOUND;

	/**
	 * ID: 1811134<br>
	 * Message: Trickster
	 */
	public static final NpcStringId TRICKSTER;

	/**
	 * ID: 1811135<br>
	 * Message: Inspector
	 */
	public static final NpcStringId INSPECTOR;

	/**
	 * ID: 1811136<br>
	 * Message: Judicator
	 */
	public static final NpcStringId JUDICATOR;

	/**
	 * ID: 1811137<br>
	 * Message: Who's there? If you disturb the temper of the great Land Dragon Antharas, I will never forgive you!
	 */
	public static final NpcStringId
			WHOS_THERE_IF_YOU_DISTURB_THE_TEMPER_OF_THE_GREAT_LAND_DRAGON_ANTHARAS_I_WILL_NEVER_FORGIVE_YOU;

	/**
	 * ID: 1900000<br>
	 * Message: Kehahaha!!! I captured Santa Claus!! There will be no gifts this year!!!
	 */
	public static final NpcStringId KEHAHAHA_I_CAPTURED_SANTA_CLAUS_THERE_WILL_BE_NO_GIFTS_THIS_YEAR;

	/**
	 * ID: 1900003<br>
	 * Message: Well? I win, right?
	 */
	public static final NpcStringId WELL_I_WIN_RIGHT;

	/**
	 * ID: 1900004<br>
	 * Message: Now!! All of you losers, get out of here!
	 */
	public static final NpcStringId NOW_ALL_OF_YOU_LOSERS_GET_OUT_OF_HERE;

	/**
	 * ID: 1900006<br>
	 * Message: I guess you came to rescue Santa. But you picked the wrong opponent.
	 */
	public static final NpcStringId I_GUESS_YOU_CAME_TO_RESCUE_SANTA_BUT_YOU_PICKED_THE_WRONG_OPPONENT;

	/**
	 * ID: 1900007<br>
	 * Message: Oh, not bad...
	 */
	public static final NpcStringId OH_NOT_BAD;

	/**
	 * ID: 1900008<br>
	 * Message: Agh!! That's not what I meant to do!
	 */
	public static final NpcStringId AGH_THATS_NOT_WHAT_I_MEANT_TO_DO;

	/**
	 * ID: 1900009<br>
	 * Message: Curse you!! Huh... What...?
	 */
	public static final NpcStringId CURSE_YOU_HUH_WHAT;

	/**
	 * ID: 1900015<br>
	 * Message: Shall I go to see if Santa is still there? Hehe~
	 */
	public static final NpcStringId SHALL_I_GO_TO_SEE_IF_SANTA_IS_STILL_THERE_HEHE;

	/**
	 * ID: 1900017<br>
	 * Message: Santa can give nice presents only if he's released from the Turkey...
	 */
	public static final NpcStringId SANTA_CAN_GIVE_NICE_PRESENTS_ONLY_IF_HES_RELEASED_FROM_THE_TURKEY;

	/**
	 * ID: 1900018<br>
	 * Message: Oh ho ho... Oh ho ho... Thank you. Everyone!  I will repay you for sure.
	 */
	public static final NpcStringId OH_HO_HO_OH_HO_HO_THANK_YOU_EVERYONE_I_WILL_REPAY_YOU_FOR_SURE;

	/**
	 * ID: 1900019<br>
	 * Message: Merry Christmas~ Well done.
	 */
	public static final NpcStringId MERRY_CHRISTMAS_WELL_DONE;

	/**
	 * ID: 1900021<br>
	 * Message: %s. I have prepared a gift for you.
	 */
	public static final NpcStringId S_I_HAVE_PREPARED_A_GIFT_FOR_YOU;

	/**
	 * ID: 1900022<br>
	 * Message: I have a gift for %s.
	 */
	public static final NpcStringId I_HAVE_A_GIFT_FOR_S;

	/**
	 * ID: 1900024<br>
	 * Message: Take a look at the inventory. Perhaps there will be a big present~
	 */
	public static final NpcStringId TAKE_A_LOOK_AT_THE_INVENTORY_PERHAPS_THERE_WILL_BE_A_BIG_PRESENT;

	/**
	 * ID: 1900026<br>
	 * Message: When are you going to stop? I'm slowly getting tired of this.
	 */
	public static final NpcStringId WHEN_ARE_YOU_GOING_TO_STOP_IM_SLOWLY_GETTING_TIRED_OF_THIS;

	/**
	 * ID: 1900027<br>
	 * Message: Message from Santa Claus: Many blessings to %s, who saved me~
	 */
	public static final NpcStringId MESSAGE_FROM_SANTA_CLAUS_MANY_BLESSINGS_TO_S_WHO_SAVED_ME;

	/**
	 * ID: 1900028<br>
	 * Message: How dare you awaken me. Feel the pain of the flames.
	 */
	public static final NpcStringId HOW_DARE_YOU_AWAKEN_ME_FEEL_THE_PAIN_OF_THE_FLAMES;

	/**
	 * ID: 1900029<br>
	 * Message: Who dares oppose the majesty of fire...?
	 */
	public static final NpcStringId WHO_DARES_OPPOSE_THE_MAJESTY_OF_FIRE;

	/**
	 * ID: 1900030<br>
	 * Message: Oh... Ouch! No, not there! Not my bead!
	 */
	public static final NpcStringId OH_OUCH_NO_NOT_THERE_NOT_MY_BEAD;

	/**
	 * ID: 1900031<br>
	 * Message: Co... Cold! That's cold! Ack! Ack!
	 */
	public static final NpcStringId CO_COLD_THATS_COLD_ACK_ACK;

	/**
	 * ID: 1900032<br>
	 * Message: Please, %s... Don't hit me... Please.
	 */
	public static final NpcStringId PLEASE_S_DONT_HIT_ME_PLEASE;

	/**
	 * ID: 1900033<br>
	 * Message: Kuaaannggg! Shake in fear!
	 */
	public static final NpcStringId KUAAANNGGG_SHAKE_IN_FEAR;

	/**
	 * ID: 1900034<br>
	 * Message: If you attack me right now, you're really going to get it!!!
	 */
	public static final NpcStringId IF_YOU_ATTACK_ME_RIGHT_NOW_YOURE_REALLY_GOING_TO_GET_IT;

	/**
	 * ID: 1900035<br>
	 * Message: Just you wait! I'm going to show you my killer technique.
	 */
	public static final NpcStringId JUST_YOU_WAIT_IM_GOING_TO_SHOW_YOU_MY_KILLER_TECHNIQUE;

	/**
	 * ID: 1900036<br>
	 * Message: You don't dare attack me!
	 */
	public static final NpcStringId YOU_DONT_DARE_ATTACK_ME;

	/**
	 * ID: 1900037<br>
	 * Message: It's different from the spirit of fire. It's not the spirit of fire! Feel my wrath!
	 */
	public static final NpcStringId ITS_DIFFERENT_FROM_THE_SPIRIT_OF_FIRE_ITS_NOT_THE_SPIRIT_OF_FIRE_FEEL_MY_WRATH;

	/**
	 * ID: 1900038<br>
	 * Message: Cold... This place...  Is this where I die...?
	 */
	public static final NpcStringId COLD_THIS_PLACE_IS_THIS_WHERE_I_DIE;

	/**
	 * ID: 1900039<br>
	 * Message: My body is cooling. Oh, Gran Kain... Forgive me...
	 */
	public static final NpcStringId MY_BODY_IS_COOLING_OH_GRAN_KAIN_FORGIVE_ME;

	/**
	 * ID: 1900040<br>
	 * Message: Idiot! I only incur damage from bare-handed attacks!
	 */
	public static final NpcStringId IDIOT_I_ONLY_INCUR_DAMAGE_FROM_BARE_HANDED_ATTACKS;

	/**
	 * ID: 1900051<br>
	 * Message: I'm out of candy. I'll give you my toy chest instead.
	 */
	public static final NpcStringId IM_OUT_OF_CANDY_ILL_GIVE_YOU_MY_TOY_CHEST_INSTEAD;

	/**
	 * ID: 1900052<br>
	 * Message: Since I'm out of candy, I will give you my toy chest instead.
	 */
	public static final NpcStringId SINCE_IM_OUT_OF_CANDY_I_WILL_GIVE_YOU_MY_TOY_CHEST_INSTEAD;

	/**
	 * ID: 1900060<br>
	 * Message: You're too good. Next time, I'll give you an incredible gift! Please wait for it.
	 */
	public static final NpcStringId YOURE_TOO_GOOD_NEXT_TIME_ILL_GIVE_YOU_AN_INCREDIBLE_GIFT_PLEASE_WAIT_FOR_IT;

	/**
	 * ID: 1900062<br>
	 * Message: I would be embarrassed to lose again here...
	 */
	public static final NpcStringId I_WOULD_BE_EMBARRASSED_TO_LOSE_AGAIN_HERE;

	/**
	 * ID: 1900070<br>
	 * Message: Even pros can't do 14 wins in a row...! Next time, I'll give you my treasure, the Golden Jack O'Lantern.
	 */
	public static final NpcStringId
			EVEN_PROS_CANT_DO_14_WINS_IN_A_ROW_NEXT_TIME_ILL_GIVE_YOU_MY_TREASURE_THE_GOLDEN_JACK_OLANTERN;

	/**
	 * ID: 1900071<br>
	 * Message: I can't do this anymore! You win! In all my 583 years, you're the best that I've seen!
	 */
	public static final NpcStringId I_CANT_DO_THIS_ANYMORE_YOU_WIN_IN_ALL_MY_583_YEARS_YOURE_THE_BEST_THAT_IVE_SEEN;

	/**
	 * ID: 1900081<br>
	 * Message: %s has won %s Jack's games in a row.
	 */
	public static final NpcStringId S_HAS_WON_S_JACKS_GAMES_IN_A_ROW;

	/**
	 * ID: 1900082<br>
	 * Message: Congratulations! %s has won %s Jack's games in a row.
	 */
	public static final NpcStringId CONGRATULATIONS_S_HAS_WON_S_JACKS_GAMES_IN_A_ROW;

	/**
	 * ID: 1900084<br>
	 * Message: Hello~! I'm Belldandy. Congratulations on getting 1st place in Jack's game. If you go and find my sibling Skooldie in the village, you'll get an amazing gift! Let's play Jack's game again!
	 */
	public static final NpcStringId
			HELLO_IM_BELLDANDY_CONGRATULATIONS_ON_GETTING_1ST_PLACE_IN_JACKS_GAME_IF_YOU_GO_AND_FIND_MY_SIBLING_SKOOLDIE_IN_THE_VILLAGE_YOULL_GET_AN_AMAZING_GIFT_LETS_PLAY_JACKS_GAME_AGAIN;

	/**
	 * ID: 1900104<br>
	 * Message: Yawn~ Ahh... Hello~! Nice weather we're having.
	 */
	public static final NpcStringId YAWN_AHH_HELLO_NICE_WEATHER_WERE_HAVING;

	/**
	 * ID: 1900105<br>
	 * Message: Ah, I'm hungry. Do you have any baby food? That's what I need to eat to get bigger.
	 */
	public static final NpcStringId AH_IM_HUNGRY_DO_YOU_HAVE_ANY_BABY_FOOD_THATS_WHAT_I_NEED_TO_EAT_TO_GET_BIGGER;

	/**
	 * ID: 1900106<br>
	 * Message: I gotta grow up fast. I want to pull Santa's sled, too.
	 */
	public static final NpcStringId I_GOTTA_GROW_UP_FAST_I_WANT_TO_PULL_SANTAS_SLED_TOO;

	/**
	 * ID: 1900107<br>
	 * Message: Yummy! I think I can grow up now!
	 */
	public static final NpcStringId YUMMY_I_THINK_I_CAN_GROW_UP_NOW;

	/**
	 * ID: 1900108<br>
	 * Message: Thanks to you, I grew up into a Boy Rudolph!
	 */
	public static final NpcStringId THANKS_TO_YOU_I_GREW_UP_INTO_A_BOY_RUDOLPH;

	/**
	 * ID: 1900109<br>
	 * Message: It's great weather for running around.
	 */
	public static final NpcStringId ITS_GREAT_WEATHER_FOR_RUNNING_AROUND;

	/**
	 * ID: 1900110<br>
	 * Message: What'll I be when I grow up? I wonder.
	 */
	public static final NpcStringId WHATLL_I_BE_WHEN_I_GROW_UP_I_WONDER;

	/**
	 * ID: 1900111<br>
	 * Message: If you take good care of me, I'll never forget it!
	 */
	public static final NpcStringId IF_YOU_TAKE_GOOD_CARE_OF_ME_ILL_NEVER_FORGET_IT;

	/**
	 * ID: 1900112<br>
	 * Message: Please pet me lovingly. You can use the "Hand of Warmth" skill under the action tab.
	 */
	public static final NpcStringId PLEASE_PET_ME_LOVINGLY_YOU_CAN_USE_THE_HAND_OF_WARMTH_SKILL_UNDER_THE_ACTION_TAB;

	/**
	 * ID: 1900113<br>
	 * Message: I feel great! Thank you!
	 */
	public static final NpcStringId I_FEEL_GREAT_THANK_YOU;

	/**
	 * ID: 1900114<br>
	 * Message: Woo. What a good feeling~ I gotta grow more and more!
	 */
	public static final NpcStringId WOO_WHAT_A_GOOD_FEELING_I_GOTTA_GROW_MORE_AND_MORE;

	/**
	 * ID: 1900115<br>
	 * Message: Oh, yummy! If I keep eating this, I think I can grow up all the way.
	 */
	public static final NpcStringId OH_YUMMY_IF_I_KEEP_EATING_THIS_I_THINK_I_CAN_GROW_UP_ALL_THE_WAY;

	/**
	 * ID: 1900116<br>
	 * Message: Yum yum. Delicious. Give me more of this!
	 */
	public static final NpcStringId YUM_YUM_DELICIOUS_GIVE_ME_MORE_OF_THIS;

	/**
	 * ID: 1900117<br>
	 * Message: Wow. This taste. There's a whole new world in my mouth!
	 */
	public static final NpcStringId WOW_THIS_TASTE_THERES_A_WHOLE_NEW_WORLD_IN_MY_MOUTH;

	/**
	 * ID: 1900118<br>
	 * Message: Yeah. It's so delicious. This is that star food!
	 */
	public static final NpcStringId YEAH_ITS_SO_DELICIOUS_THIS_IS_THAT_STAR_FOOD;

	/**
	 * ID: 1900119<br>
	 * Message: Pay some attention to me! Hmph.
	 */
	public static final NpcStringId PAY_SOME_ATTENTION_TO_ME_HMPH;

	/**
	 * ID: 1900120<br>
	 * Message: Thank you. I was able to grow up into an adult. Here is my gift.
	 */
	public static final NpcStringId THANK_YOU_I_WAS_ABLE_TO_GROW_UP_INTO_AN_ADULT_HERE_IS_MY_GIFT;

	/**
	 * ID: 1900121<br>
	 * Message: Thank you. %s. Now, I can pull the sled.
	 */
	public static final NpcStringId THANK_YOU_S_NOW_I_CAN_PULL_THE_SLED;

	/**
	 * ID: 1900122<br>
	 * Message: %s. Thank you for taking care of me all this time. I enjoyed it very much.
	 */
	public static final NpcStringId S_THANK_YOU_FOR_TAKING_CARE_OF_ME_ALL_THIS_TIME_I_ENJOYED_IT_VERY_MUCH;

	/**
	 * ID: 1900123<br>
	 * Message: %s. It won't be long now until it becomes time to pull the sled. It's too bad.
	 */
	public static final NpcStringId S_IT_WONT_BE_LONG_NOW_UNTIL_IT_BECOMES_TIME_TO_PULL_THE_SLED_ITS_TOO_BAD;

	/**
	 * ID: 1900124<br>
	 * Message: I must return to Santa now. Thank you for everything!
	 */
	public static final NpcStringId I_MUST_RETURN_TO_SANTA_NOW_THANK_YOU_FOR_EVERYTHING;

	/**
	 * ID: 1900125<br>
	 * Message: Hello. I'm a girl Rudolph. I was able to find my true self thanks to you.
	 */
	public static final NpcStringId HELLO_IM_A_GIRL_RUDOLPH_I_WAS_ABLE_TO_FIND_MY_TRUE_SELF_THANKS_TO_YOU;

	/**
	 * ID: 1900126<br>
	 * Message: This is my gift of thanks. Thank you for taking care of me~
	 */
	public static final NpcStringId THIS_IS_MY_GIFT_OF_THANKS_THANK_YOU_FOR_TAKING_CARE_OF_ME;

	/**
	 * ID: 1900127<br>
	 * Message: %s. I was always grateful.
	 */
	public static final NpcStringId S_I_WAS_ALWAYS_GRATEFUL;

	/**
	 * ID: 1900128<br>
	 * Message: I'm a little sad. It's time to leave now.
	 */
	public static final NpcStringId IM_A_LITTLE_SAD_ITS_TIME_TO_LEAVE_NOW;

	/**
	 * ID: 1900129<br>
	 * Message: %s. The time has come for me to return to my home.
	 */
	public static final NpcStringId S_THE_TIME_HAS_COME_FOR_ME_TO_RETURN_TO_MY_HOME;

	/**
	 * ID: 1900130<br>
	 * Message: %s. Thank you.
	 */
	public static final NpcStringId S_THANK_YOU;

	/**
	 * ID: 1900131<br>
	 * Message: Hahaha!!! I captured Santa Claus!! Huh? Where is this? Who are you?
	 */
	public static final NpcStringId HAHAHA_I_CAPTURED_SANTA_CLAUS_HUH_WHERE_IS_THIS_WHO_ARE_YOU;

	/**
	 * ID: 1900132<br>
	 * Message: ...I lost at Rock Paper Scissors and was taken captive... I might as well take out my anger on you! Huh~ What?
	 */
	public static final NpcStringId
			I_LOST_AT_ROCK_PAPER_SCISSORS_AND_WAS_TAKEN_CAPTIVE_I_MIGHT_AS_WELL_TAKE_OUT_MY_ANGER_ON_YOU_HUH_WHAT;

	/**
	 * ID: 1900133<br>
	 * Message: Nothing's working... I'm leaving! I'll definitely capture Santa again! Just you wait!
	 */
	public static final NpcStringId NOTHINGS_WORKING_IM_LEAVING_ILL_DEFINITELY_CAPTURE_SANTA_AGAIN_JUST_YOU_WAIT;

	/**
	 * ID: 1900134<br>
	 * Message: I must raise Rudolph quickly. This year's Christmas gifts have to be delivered...
	 */
	public static final NpcStringId I_MUST_RAISE_RUDOLPH_QUICKLY_THIS_YEARS_CHRISTMAS_GIFTS_HAVE_TO_BE_DELIVERED;

	/**
	 * ID: 1900135<br>
	 * Message: Merry Christmas~ Thanks to your efforts in raising Rudolph, the gift delivery was a success.
	 */
	public static final NpcStringId
			MERRY_CHRISTMAS_THANKS_TO_YOUR_EFFORTS_IN_RAISING_RUDOLPH_THE_GIFT_DELIVERY_WAS_A_SUCCESS;

	/**
	 * ID: 1900136<br>
	 * Message: In 10 minutes, it will be 1 hour since you started raising me.
	 */
	public static final NpcStringId IN_10_MINUTES_IT_WILL_BE_1_HOUR_SINCE_YOU_STARTED_RAISING_ME;

	/**
	 * ID: 1900137<br>
	 * Message: After 5 minutes, if my Full Feeling and Affection Level reach 99%, I can grow bigger.
	 */
	public static final NpcStringId AFTER_5_MINUTES_IF_MY_FULL_FEELING_AND_AFFECTION_LEVEL_REACH_99_I_CAN_GROW_BIGGER;

	/**
	 * ID: 1900139<br>
	 * Message: Lucky! I'm Lucky, the spirit that loves adena
	 */
	public static final NpcStringId LUCKY_IM_LUCKY_THE_SPIRIT_THAT_LOVES_ADENA;

	/**
	 * ID: 1900140<br>
	 * Message: Lucky! I want to eat adena. Give it to me!
	 */
	public static final NpcStringId LUCKY_I_WANT_TO_EAT_ADENA_GIVE_IT_TO_ME;

	/**
	 * ID: 1900141<br>
	 * Message: Lucky! If I eat too much adena, my wings disappear!
	 */
	public static final NpcStringId LUCKY_IF_I_EAT_TOO_MUCH_ADENA_MY_WINGS_DISAPPEAR;

	/**
	 * ID: 1900142<br>
	 * Message: Yummy. Thanks! Lucky!
	 */
	public static final NpcStringId YUMMY_THANKS_LUCKY;

	/**
	 * ID: 1900143<br>
	 * Message: Grrrr... Yuck!
	 */
	public static final NpcStringId GRRRR_YUCK;

	/**
	 * ID: 1900144<br>
	 * Message: Lucky! The adena is so yummy! I'm getting bigger!
	 */
	public static final NpcStringId LUCKY_THE_ADENA_IS_SO_YUMMY_IM_GETTING_BIGGER;

	/**
	 * ID: 1900145<br>
	 * Message: Lucky! No more adena? Oh... I'm so heavy!
	 */
	public static final NpcStringId LUCKY_NO_MORE_ADENA_OH_IM_SO_HEAVY;

	/**
	 * ID: 1900146<br>
	 * Message: Lucky! I'm full~ Thanks for the yummy adena! Oh... I'm so heavy!
	 */
	public static final NpcStringId LUCKY_IM_FULL_THANKS_FOR_THE_YUMMY_ADENA_OH_IM_SO_HEAVY;

	/**
	 * ID: 1900147<br>
	 * Message: Lucky! It wasn't enough adena. It's gotta be at least %s!
	 */
	public static final NpcStringId LUCKY_IT_WASNT_ENOUGH_ADENA_ITS_GOTTA_BE_AT_LEAST_S;

	/**
	 * ID: 1900148<br>
	 * Message: Oh! My wings disappeared! Are you gonna hit me? If you hit me, I'll throw up everything that I ate!
	 */
	public static final NpcStringId
			OH_MY_WINGS_DISAPPEARED_ARE_YOU_GONNA_HIT_ME_IF_YOU_HIT_ME_ILL_THROW_UP_EVERYTHING_THAT_I_ATE;

	/**
	 * ID: 1900149<br>
	 * Message: Oh! My wings... Ack! Are you gonna hit me?! Scary, scary! If you hit me, something bad will happen!
	 */
	public static final NpcStringId
			OH_MY_WINGS_ACK_ARE_YOU_GONNA_HIT_ME_SCARY_SCARY_IF_YOU_HIT_ME_SOMETHING_BAD_WILL_HAPPEN;

	/**
	 * ID: 1900150<br>
	 * Message: The evil Land Dragon Antharas has been defeated!
	 */
	public static final NpcStringId THE_EVIL_LAND_DRAGON_ANTHARAS_HAS_BEEN_DEFEATED;

	/**
	 * ID: 1900151<br>
	 * Message: The evil Fire Dragon Valakas has been defeated!
	 */
	public static final NpcStringId THE_EVIL_FIRE_DRAGON_VALAKAS_HAS_BEEN_DEFEATED;

	/**
	 * ID: 1900152<br>
	 * Message: To serve him now means you will be able to escape a worse situation.
	 */
	public static final NpcStringId TO_SERVE_HIM_NOW_MEANS_YOU_WILL_BE_ABLE_TO_ESCAPE_A_WORSE_SITUATION;

	/**
	 * ID: 1900153<br>
	 * Message: Oh goddess of destruction, forgive us...
	 */
	public static final NpcStringId OH_GODDESS_OF_DESTRUCTION_FORGIVE_US;

	/**
	 * ID: 1900154<br>
	 * Message: When the sky turns blood red and the earth begins to crumble from the darkness she will return.
	 */
	public static final NpcStringId
			WHEN_THE_SKY_TURNS_BLOOD_RED_AND_THE_EARTH_BEGINS_TO_CRUMBLE_FROM_THE_DARKNESS_SHE_WILL_RETURN;

	/**
	 * ID: 1900155<br>
	 * Message: Earth energy is gathering near  Antharas's legs.
	 */
	public static final NpcStringId EARTH_ENERGY_IS_GATHERING_NEAR_ANTHARASS_LEGS;

	/**
	 * ID: 1900156<br>
	 * Message: Antharas starts to absorb the earth energy.
	 */
	public static final NpcStringId ANTHARAS_STARTS_TO_ABSORB_THE_EARTH_ENERGY;

	/**
	 * ID: 1900157<br>
	 * Message: Antharas raises its thick tail.
	 */
	public static final NpcStringId ANTHARAS_RAISES_ITS_THICK_TAIL;

	/**
	 * ID: 1900158<br>
	 * Message: You are overcome by the strength of Antharas.
	 */
	public static final NpcStringId YOU_ARE_OVERCOME_BY_THE_STRENGTH_OF_ANTHARAS;

	/**
	 * ID: 1900159<br>
	 * Message: Antharas's eyes are filled with rage.
	 */
	public static final NpcStringId ANTHARASS_EYES_ARE_FILLED_WITH_RAGE;

	/**
	 * ID: 1900160<br>
	 * Message: $s1, I can feel their presence from you.
	 */
	public static final NpcStringId S1_I_CAN_FEEL_THEIR_PRESENCE_FROM_YOU;

	/**
	 * ID: 1900161<br>
	 * Message: $s1, brethren, come to my side and follow me.
	 */
	public static final NpcStringId S1_BRETHREN_COME_TO_MY_SIDE_AND_FOLLOW_ME;

	/**
	 * ID: 1900162<br>
	 * Message: Antharas roars.
	 */
	public static final NpcStringId ANTHARAS_ROARS;

	/**
	 * ID: 1900163<br>
	 * Message: Flame energy is being directed towards Valakas.
	 */
	public static final NpcStringId FLAME_ENERGY_IS_BEING_DIRECTED_TOWARDS_VALAKAS;

	/**
	 * ID: 1900164<br>
	 * Message: You are overcome by the strength of Valakas.
	 */
	public static final NpcStringId YOU_ARE_OVERCOME_BY_THE_STRENGTH_OF_VALAKAS;

	/**
	 * ID: 1900165<br>
	 * Message: Valakas's tail flails dangerously.
	 */
	public static final NpcStringId VALAKASS_TAIL_FLAILS_DANGEROUSLY;

	/**
	 * ID: 1900166<br>
	 * Message: Valakas raises its tail.
	 */
	public static final NpcStringId VALAKAS_RAISES_ITS_TAIL;

	/**
	 * ID: 1900167<br>
	 * Message: Valakas starts to absorb the flame energy.
	 */
	public static final NpcStringId VALAKAS_STARTS_TO_ABSORB_THE_FLAME_ENERGY;

	/**
	 * ID: 1900168<br>
	 * Message: Valakas looks to its left.
	 */
	public static final NpcStringId VALAKAS_LOOKS_TO_ITS_LEFT;

	/**
	 * ID: 1900169<br>
	 * Message: Valakas looks to its right.
	 */
	public static final NpcStringId VALAKAS_LOOKS_TO_ITS_RIGHT;

	/**
	 * ID: 1900170<br>
	 * Message: By my authority, I command you, creature, turn to dust.
	 */
	public static final NpcStringId BY_MY_AUTHORITY_I_COMMAND_YOU_CREATURE_TURN_TO_DUST;

	/**
	 * ID: 1900171<br>
	 * Message: By my wrath, I command you, creature, lose your mind.
	 */
	public static final NpcStringId BY_MY_WRATH_I_COMMAND_YOU_CREATURE_LOSE_YOUR_MIND;

	/**
	 * ID: 1900172<br>
	 * Message: Show respect to the heroes who defeated the evil dragon and protected this Aden world!
	 */
	public static final NpcStringId
			SHOW_RESPECT_TO_THE_HEROES_WHO_DEFEATED_THE_EVIL_DRAGON_AND_PROTECTED_THIS_ADEN_WORLD;

	/**
	 * ID: 1900173<br>
	 * Message: Shout to celebrate the victory of the heroes!
	 */
	public static final NpcStringId SHOUT_TO_CELEBRATE_THE_VICTORY_OF_THE_HEROES;

	/**
	 * ID: 1900174<br>
	 * Message: Praise the achievement of the heroes and receive Nevit's blessing!
	 */
	public static final NpcStringId PRAISE_THE_ACHIEVEMENT_OF_THE_HEROES_AND_RECEIVE_NEVITS_BLESSING;

	/**
	 * ID: 1900175<br>
	 * Message: Ugh I think this is it for me!
	 */
	public static final NpcStringId UGH_I_THINK_THIS_IS_IT_FOR_ME;

	/**
	 * ID: 1900176<br>
	 * Message: Valakas calls forth the servitor's master.
	 */
	public static final NpcStringId VALAKAS_CALLS_FORTH_THE_SERVITORS_MASTER;

	/**
	 * ID: 1911111<br>
	 * Message: You will soon become the sacrifice for us, those full of deceit and sin whom you despise!
	 */
	public static final NpcStringId
			YOU_WILL_SOON_BECOME_THE_SACRIFICE_FOR_US_THOSE_FULL_OF_DECEIT_AND_SIN_WHOM_YOU_DESPISE;

	/**
	 * ID: 1911112<br>
	 * Message: My brethren who are stronger than me will punish you. You will soon be covered in your own blood and crying in anguish!
	 */
	public static final NpcStringId
			MY_BRETHREN_WHO_ARE_STRONGER_THAN_ME_WILL_PUNISH_YOU_YOU_WILL_SOON_BE_COVERED_IN_YOUR_OWN_BLOOD_AND_CRYING_IN_ANGUISH;

	/**
	 * ID: 1911113<br>
	 * Message: How could I lose against these worthless creatures...?
	 */
	public static final NpcStringId HOW_COULD_I_LOSE_AGAINST_THESE_WORTHLESS_CREATURES;

	/**
	 * ID: 1911114<br>
	 * Message: Foolish creatures... the flames of hell are drawing closer.
	 */
	public static final NpcStringId FOOLISH_CREATURES_THE_FLAMES_OF_HELL_ARE_DRAWING_CLOSER;

	/**
	 * ID: 1911115<br>
	 * Message: No matter how you struggle this place will soon be covered with your blood.
	 */
	public static final NpcStringId NO_MATTER_HOW_YOU_STRUGGLE_THIS_PLACE_WILL_SOON_BE_COVERED_WITH_YOUR_BLOOD;

	/**
	 * ID: 1911116<br>
	 * Message: Those who set foot in this place shall not leave alive.
	 */
	public static final NpcStringId THOSE_WHO_SET_FOOT_IN_THIS_PLACE_SHALL_NOT_LEAVE_ALIVE;

	/**
	 * ID: 1911117<br>
	 * Message: Worthless creatures, I will grant you eternal sleep in fire and brimstone.
	 */
	public static final NpcStringId WORTHLESS_CREATURES_I_WILL_GRANT_YOU_ETERNAL_SLEEP_IN_FIRE_AND_BRIMSTONE;

	/**
	 * ID: 1911118<br>
	 * Message: If you wish to see hell, I will grant you your wish.
	 */
	public static final NpcStringId IF_YOU_WISH_TO_SEE_HELL_I_WILL_GRANT_YOU_YOUR_WISH;

	/**
	 * ID: 1911119<br>
	 * Message: Elapsed Time :
	 */
	public static final NpcStringId ELAPSED_TIME;

	/**
	 * ID: 1911120<br>
	 * Message: Time Remaining :
	 */
	public static final NpcStringId TIME_REMAINING;

	/**
	 * ID: 10004431<br>
	 * Message: It teleports the guard members of the Aden Imperial Castle to the inside of the castle.
	 */
	public static final NpcStringId
			IT_TELEPORTS_THE_GUARD_MEMBERS_OF_THE_ADEN_IMPERIAL_CASTLE_TO_THE_INSIDE_OF_THE_CASTLE;

	/**
	 * ID: 10004432<br>
	 * Message: It teleports the guard members of the Gludio Castle to the inside of the castle.
	 */
	public static final NpcStringId IT_TELEPORTS_THE_GUARD_MEMBERS_OF_THE_GLUDIO_CASTLE_TO_THE_INSIDE_OF_THE_CASTLE;

	/**
	 * ID: 10004433<br>
	 * Message: It teleports the guard members of the Dion Castle to the inside of the castle.
	 */
	public static final NpcStringId IT_TELEPORTS_THE_GUARD_MEMBERS_OF_THE_DION_CASTLE_TO_THE_INSIDE_OF_THE_CASTLE;

	/**
	 * ID: 10004434<br>
	 * Message: It teleports the guard members of the Giran Castle to the inside of the castle.
	 */
	public static final NpcStringId IT_TELEPORTS_THE_GUARD_MEMBERS_OF_THE_GIRAN_CASTLE_TO_THE_INSIDE_OF_THE_CASTLE;

	/**
	 * ID: 10004436<br>
	 * Message: It teleports the guard members of the Aden Castle to the inside of the castle.
	 */
	public static final NpcStringId IT_TELEPORTS_THE_GUARD_MEMBERS_OF_THE_ADEN_CASTLE_TO_THE_INSIDE_OF_THE_CASTLE;

	/**
	 * ID: 10004437<br>
	 * Message: It teleports the guard members of the Innadril Castle to the inside of the castle.
	 */
	public static final NpcStringId IT_TELEPORTS_THE_GUARD_MEMBERS_OF_THE_INNADRIL_CASTLE_TO_THE_INSIDE_OF_THE_CASTLE;

	/**
	 * ID: 10004438<br>
	 * Message: It teleports the guard members of the Goddard Castle to the inside of the castle.
	 */
	public static final NpcStringId IT_TELEPORTS_THE_GUARD_MEMBERS_OF_THE_GODDARD_CASTLE_TO_THE_INSIDE_OF_THE_CASTLE;

	/**
	 * ID: 10004439<br>
	 * Message: It teleports the guard members of the Rune Castle to the inside of the castle.
	 */
	public static final NpcStringId IT_TELEPORTS_THE_GUARD_MEMBERS_OF_THE_RUNE_CASTLE_TO_THE_INSIDE_OF_THE_CASTLE;

	/**
	 * ID: 10004440<br>
	 * Message: It teleports the guard members of the Schuttgart Castle to the inside of the castle.
	 */
	public static final NpcStringId IT_TELEPORTS_THE_GUARD_MEMBERS_OF_THE_SCHUTTGART_CASTLE_TO_THE_INSIDE_OF_THE_CASTLE;

	/**
	 * ID: 10004441<br>
	 * Message: It teleports the guard members of the Elmore Imperial Castle to the inside of the castle.
	 */
	public static final NpcStringId
			IT_TELEPORTS_THE_GUARD_MEMBERS_OF_THE_ELMORE_IMPERIAL_CASTLE_TO_THE_INSIDE_OF_THE_CASTLE;

	/**
	 * Array containing all NpcStringId<br>
	 * Important: Always initialize with a length of the highest NpcStringId + 1!!!
	 */
	private static NpcStringId[] VALUES;

	static
	{
		HELLO_I_AM_S1_YOU_ARE_S2_RIGHT_HEHEHE = new NpcStringId(1);
		S1_S2_S3_S4_S5_HEHEHE = new NpcStringId(2);
		WITHDRAW_THE_FEE_FOR_THE_NEXT_TIME_AT_S1_S2_S4 = new NpcStringId(5);
		STAGE = new NpcStringId(8);
		STAGE_S1 = new NpcStringId(9);
		S1 = new NpcStringId(10);
		WHAT_DID_YOU_JUST_DO_TO_ME = new NpcStringId(2004);
		ARE_YOU_TRYING_TO_TAME_ME_DONT_DO_THAT = new NpcStringId(2005);
		DONT_GIVE_SUCH_A_THING_YOU_CAN_ENDANGER_YOURSELF = new NpcStringId(2006);
		YUCK_WHAT_IS_THIS_IT_TASTES_TERRIBLE = new NpcStringId(2007);
		IM_HUNGRY_GIVE_ME_A_LITTLE_MORE_PLEASE = new NpcStringId(2008);
		WHAT_IS_THIS_IS_THIS_EDIBLE = new NpcStringId(2009);
		DONT_WORRY_ABOUT_ME = new NpcStringId(2010);
		THANK_YOU_THAT_WAS_DELICIOUS = new NpcStringId(2011);
		I_THINK_I_AM_STARTING_TO_LIKE_YOU = new NpcStringId(2012);
		EEEEEK_EEEEEK = new NpcStringId(2013);
		DONT_KEEP_TRYING_TO_TAME_ME_I_DONT_WANT_TO_BE_TAMED = new NpcStringId(2014);
		IT_IS_JUST_FOOD_TO_ME_ALTHOUGH_IT_MAY_ALSO_BE_YOUR_HAND = new NpcStringId(2015);
		IF_I_KEEP_EATING_LIKE_THIS_WONT_I_BECOME_FAT_CHOMP_CHOMP = new NpcStringId(2016);
		WHY_DO_YOU_KEEP_FEEDING_ME = new NpcStringId(2017);
		DONT_TRUST_ME_IM_AFRAID_I_MAY_BETRAY_YOU_LATER = new NpcStringId(2018);
		GRRRRR = new NpcStringId(2019);
		YOU_BROUGHT_THIS_UPON_YOURSELF = new NpcStringId(2020);
		I_FEEL_STRANGE_I_KEEP_HAVING_THESE_EVIL_THOUGHTS = new NpcStringId(2021);
		ALAS_SO_THIS_IS_HOW_IT_ALL_ENDS = new NpcStringId(2022);
		I_DONT_FEEL_SO_GOOD_OH_MY_MIND_IS_VERY_TROUBLED = new NpcStringId(2023);
		S1_SO_WHAT_DO_YOU_THINK_IT_IS_LIKE_TO_BE_TAMED = new NpcStringId(2024);
		S1_WHENEVER_I_SEE_SPICE_I_THINK_I_WILL_MISS_YOUR_HAND_THAT_USED_TO_FEED_IT_TO_ME = new NpcStringId(2025);
		S1_DONT_GO_TO_THE_VILLAGE_I_DONT_HAVE_THE_STRENGTH_TO_FOLLOW_YOU = new NpcStringId(2026);
		THANK_YOU_FOR_TRUSTING_ME_S1_I_HOPE_I_WILL_BE_HELPFUL_TO_YOU = new NpcStringId(2027);
		S1_WILL_I_BE_ABLE_TO_HELP_YOU = new NpcStringId(2028);
		I_GUESS_ITS_JUST_MY_ANIMAL_MAGNETISM = new NpcStringId(2029);
		TOO_MUCH_SPICY_FOOD_MAKES_ME_SWEAT_LIKE_A_BEAST = new NpcStringId(2030);
		ANIMALS_NEED_LOVE_TOO = new NpcStringId(2031);
		WHATD_I_MISS_WHATD_I_MISS = new NpcStringId(2032);
		I_JUST_KNOW_BEFORE_THIS_IS_OVER_IM_GONNA_NEED_A_LOT_OF_SERIOUS_THERAPY = new NpcStringId(2033);
		I_SENSE_GREAT_WISDOM_IN_YOU_IM_AN_ANIMAL_AND_I_GOT_INSTINCTS = new NpcStringId(2034);
		REMEMBER_IM_HERE_TO_HELP = new NpcStringId(2035);
		ARE_WE_THERE_YET = new NpcStringId(2036);
		THAT_REALLY_MADE_ME_FEEL_GOOD_TO_SEE_THAT = new NpcStringId(2037);
		OH_NO_NO_NO_NOOOOO = new NpcStringId(2038);
		WHO_AWOKE_ME = new NpcStringId(2150);
		MY_MASTER_HAS_INSTRUCTED_ME_TO_BE_YOUR_GUIDE_S1 = new NpcStringId(2151);
		PLEASE_CHECK_THIS_BOOKCASE_S1 = new NpcStringId(2152);
		DID_YOU_CALL_ME_S1 = new NpcStringId(2250);
		IM_CONFUSED_MAYBE_ITS_TIME_TO_GO_BACK = new NpcStringId(2251);
		THAT_SIGN = new NpcStringId(2450);
		THAT_BOX_WAS_SEALED_BY_MY_MASTER_S1_DONT_TOUCH_IT = new NpcStringId(2550);
		YOUVE_ENDED_MY_IMMORTAL_LIFE_YOURE_PROTECTED_BY_THE_FEUDAL_LORD_ARENT_YOU = new NpcStringId(2551);
		DELIVERY_DUTY_COMPLETE_N_GO_FIND_THE_NEWBIE_GUIDE = new NpcStringId(4151);
		ACQUISITION_OF_SOULSHOT_FOR_BEGINNERS_COMPLETE_N_GO_FIND_THE_NEWBIE_GUIDE = new NpcStringId(4152);
		ACQUISITION_OF_WEAPON_EXCHANGE_COUPON_FOR_BEGINNERS_COMPLETE_N_GO_SPEAK_WITH_THE_NEWBIE_GUIDE =
				new NpcStringId(4153);
		ACQUISITION_OF_RACE_SPECIFIC_WEAPON_COMPLETE_N_GO_FIND_THE_NEWBIE_GUIDE = new NpcStringId(4154);
		LAST_DUTY_COMPLETE_N_GO_FIND_THE_NEWBIE_GUIDE = new NpcStringId(4155);
		S1_I_MUST_KILL_YOU_BLAME_YOUR_OWN_CURIOSITY = new NpcStringId(6051);
		YOU_HAVE_GOOD_LUCK_I_SHALL_RETURN = new NpcStringId(6052);
		YOU_ARE_STRONG_THIS_WAS_A_MISTAKE = new NpcStringId(6053);
		WHO_ARE_YOU_TO_JOIN_IN_THE_BATTLE_HOW_UPSETTING = new NpcStringId(6054);
		S1_DID_YOU_COME_TO_HELP_ME = new NpcStringId(6451);
		DRATS_HOW_COULD_I_BE_SO_WRONG = new NpcStringId(6551);
		S1_STEP_BACK_FROM_THE_CONFOUNDED_BOX_I_WILL_TAKE_IT_MYSELF = new NpcStringId(6552);
		S1_I_WILL_BE_BACK_SOON_STAY_THERE_AND_DONT_YOU_DARE_WANDER_OFF = new NpcStringId(6553);
		GRR_IVE_BEEN_HIT = new NpcStringId(6554);
		GRR_WHO_ARE_YOU_AND_WHY_HAVE_YOU_STOPPED_ME = new NpcStringId(6555);
		I_AM_LATE = new NpcStringId(6556);
		GOOD_LUCK = new NpcStringId(6557);
		S1_YOU_SEEK_THE_FORBIDDEN_KNOWLEDGE_AND_I_CANNOT_LET_YOU_HAVE_IT = new NpcStringId(6750);
		IS_THIS_ALL_I_AM_ALLOWED_TO_HAVE = new NpcStringId(6751);
		YOU_DEFEATED_ME_BUT_OUR_DOOM_APPROACHES = new NpcStringId(6752);
		S1_WHO_ARE_YOU_WHY_ARE_YOU_BOTHERING_MY_MINIONS = new NpcStringId(6753);
		BEEFCAKE = new NpcStringId(6754);
		GRR_WHY_ARE_YOU_STICKING_YOUR_NOSE_IN_OUR_BUSINESS = new NpcStringId(6755);
		FAREWELL_AND_WATCH_YOUR_BACK = new NpcStringId(6756);
		KAMAEL_GOOD_TO_SEE_YOU_I_HAVE_SOMETHING_TO_ASK_YOU = new NpcStringId(6757);
		S1_GO_GET_HIM = new NpcStringId(6758);
		S1_WHAT_ARE_YOU_DOING_ATTACK_HIM = new NpcStringId(6759);
		S1_IS_YOUR_FULL_POTENTIAL = new NpcStringId(6760);
		THANKS_I_MUST_GO_AND_HUNT_DOWN_THOSE_THAT_OPPOSE_ME = new NpcStringId(6761);
		YOU_ARE_SO_STUBBORN_I_MUST_FOLLOW_HIM_NOW = new NpcStringId(6762);
		SEEK_ENLIGHTENMENT_FROM_THE_TABLET = new NpcStringId(6763);
		ARROGANT_BEINGS_YOU_ARE_ALL_DOOMED = new NpcStringId(6764);
		MY_TIME_IN_YOUR_WORLD_HAS_COME_TO_AN_END_CONSIDER_YOURSELVES_LUCKY = new NpcStringId(6765);
		S1_HOW_DARE_YOU = new NpcStringId(6766);
		S1_AHHAA_YOUR_GOD_FORSAKES_YOU = new NpcStringId(6767);
		S1_YOUR_TIME_IS_UP_PREPARE_TO_DIE_A_HORRIBLE_DEATH = new NpcStringId(6851);
		CONSIDER_YOURSELF_LUCKY_THE_NEXT_TIME_WE_MEET_YOU_WILL_DIE_PERMANENTLY = new NpcStringId(6852);
		FARE_THEE_WELL_WE_SHALL_MEET_AGAIN = new NpcStringId(6853);
		S1_WHO_ARE_YOU_AND_BETTER_YET_WHY_ARE_YOU_BOTHERING_MY_MINIONS = new NpcStringId(6854);
		STRENGTH_BEYOND_STRENGTH = new NpcStringId(6855);
		GRR_WHY_ARE_YOU_STICKING_YOUR_NOSE_WHERE_IT_DOESNT_BELONG = new NpcStringId(6856);
		YOUVE_WON_FOR_NOW_BUT_WE_WILL_MEET_AGAIN = new NpcStringId(6857);
		ARE_THEY_TIRED_OF_FOLLOWING_ME = new NpcStringId(6858);
		S1_CAN_YOU_HELP_ME = new NpcStringId(6859);
		IS_THAT_ALL_YOU_GOT_LITTLE_S1 = new NpcStringId(6860);
		S1_WAKE_UP_FOOL_DONT_LET_HIM_GET_AWAY = new NpcStringId(6861);
		THE_PATH_IS_CLEAR_BUT_BE_CAREFUL = new NpcStringId(6862);
		I_MUST_FOLLOW_SOMEONE_NOW_SEE_YOU_AROUND = new NpcStringId(6863);
		MAY_WE_MEET_AGAIN = new NpcStringId(6864);
		CURSES_ON_THOSE_WHO_BLASPHEME_THE_GODS = new NpcStringId(6865);
		EINHASAD_IS_CALLING_ME_YOU_ARE_LUCKY_AND_I_SHALL_CONTINUE_MY_PUNISHMENT_LATER = new NpcStringId(6866);
		BY_THE_POWER_VESTED_IN_ME_BY_THE_GODS_YOU_S1_SHALL_DIE = new NpcStringId(6867);
		S1_I_WILL_NOT_FORGET_YOU = new NpcStringId(6868);
		S1_HOW_DARE_YOU_INTERFERE_YOU_SHALL_PAY_FOR_THIS = new NpcStringId(6950);
		BELETH_IS_CALLING_ME_YOU_ARE_LUCKY_BUT_STILL_A_FOOL = new NpcStringId(6951);
		I_SHALL_TAKE_MY_LEAVE_BUT_WILL_NEVER_SURRENDER = new NpcStringId(6952);
		COWER_BEFORE_MY_AWESOME_AND_MIGHTY_POWER = new NpcStringId(6954);
		GRR_CANT_YOU_FIND_SOMETHING_BETTER_TO_DO_WITH_YOUR_TIME = new NpcStringId(6955);
		I_SHALL_TAKE_MY_LEAVE_BUT_YOUR_HEAD_WILL_BE_MINE_SOME_DAY_OH_YESYES_IT_WILL = new NpcStringId(6956);
		MY_CHILDREN_ARE_STRONGER = new NpcStringId(6957);
		S1_LETS_KILL_THEM_ALL = new NpcStringId(6958);
		S1_COME_MY_CHILDREN = new NpcStringId(6959);
		S1_MUSTER_YOUR_STRENGTH_PICK_THEM_OFF_LIKE_CHICKENS = new NpcStringId(6960);
		THANK_YOU_MY_CHILDREN_SOMEDAY_WE_WILL_MEET_AGAIN = new NpcStringId(6961);
		MY_CHILDREN_SEEK_MY_ENEMIES = new NpcStringId(6962);
		MY_CHILDREN_I_GRANT_YOU_MY_BLESSINGS = new NpcStringId(6963);
		YOU_WORTHLESS_BEINGS = new NpcStringId(6964);
		MY_TIME_ON_THE_MATERIAL_PLANE_HAS_ENDED_YOU_ARE_LUCKY = new NpcStringId(6965);
		S1_WORTHLESS_BEINGS_BEGONE = new NpcStringId(6966);
		S1_YOU_ARE_THE_MEANING_OF_THE_WORD_DANGER = new NpcStringId(6967);
		YOU_MADE_IT_HERE_S1_ILL_SHOW_MY_STRENGTH_DIE = new NpcStringId(7050);
		HA_YOU_FAILED_ARE_YOU_READY_TO_QUIT = new NpcStringId(7051);
		IM_THE_STRONGEST_I_LOST_EVERYTHING_TO_WIN = new NpcStringId(7052);
		S1_ARE_YOU_DOING_THIS_BECAUSE_THEYRE_HALISHAS_MINIONS = new NpcStringId(7053);
		MY_SPIRIT_IS_RELEASED_FROM_THIS_SHELL_IM_GETTING_CLOSE_TO_HALISHA = new NpcStringId(7054);
		MIND_YOUR_OWN_BUSINESS = new NpcStringId(7055);
		THIS_FIGHT_IS_A_WASTE_OF_TIME_GOODBYE = new NpcStringId(7056);
		EVERY_CLOUD_HAS_A_SILVER_LINING_DONT_YOU_THINK = new NpcStringId(7057);
		S1_DONT_LISTEN_TO_THIS_DEMON = new NpcStringId(7058);
		S1_HESITATION_BETRAYS_YOUR_HEART_FIGHT = new NpcStringId(7059);
		S1_ISNT_PROTECTING_SOMEBODY_WORTHY_ISNT_THAT_JUSTICE = new NpcStringId(7060);
		I_HAVE_URGENT_BUSINESS_I_GOTTA_GO = new NpcStringId(7061);
		ARE_MY_EFFORTS_IN_VAIN_IS_THIS_THE_END = new NpcStringId(7062);
		GOODBYE_FRIEND_I_HOPE_TO_SEE_YOU_AGAIN = new NpcStringId(7063);
		KNIGHTS_ARE_ALWAYS_FOOLISH = new NpcStringId(7064);
		ILL_SHOW_MERCY_ON_YOU_FOR_NOW = new NpcStringId(7065);
		S1_YOUR_JUSTICE_IS_JUST_HYPOCRISY_IF_YOU_GIVE_UP_ON_WHAT_YOUVE_PROMISED_TO_PROTECT = new NpcStringId(7066);
		S1DONT_THINK_YOUVE_WON_YOUR_DARK_SHADOW_WILL_ALWAYS_FOLLOW_YOUHYPOCRITE = new NpcStringId(7067);
		A_TEMPLE_KNIGHT_GUARDS_THE_MOTHER_TREE_S1_HAS_HUMAN_CONTACT_MADE_YOU_FORGET_THAT = new NpcStringId(7150);
		I_MUST_STOP_REMEMBER_THE_ONES_YOURE_PROTECTING_WILL_SOMEDAY_DEFEAT_THE_ELVES = new NpcStringId(7151);
		WHAT_THAT_WILL_JUST_STRENGTHEN_THE_ENEMY = new NpcStringId(7152);
		YOU_DARE_TO_DISTURB_THE_ORDER_OF_THE_SHRINE_DIE_S1 = new NpcStringId(7153);
		MY_SPIRIT_IS_RELEASING_FROM_THIS_SHELL_IM_GETTING_CLOSE_TO_HALISHA = new NpcStringId(7154);
		THIS_IS_A_WASTE_OF_TIME_GOODBYE = new NpcStringId(7156);
		IM_NOT_LIKE_MY_BROTHER_PANACEA_GHOST_OF_THE_PAST_BEGONE = new NpcStringId(7157);
		THE_ELVES_NO_LONGER_RULE_HELP_ME_S1 = new NpcStringId(7158);
		DONT_GIVE_UP_S1_HE_A_DEMON_FROM_THE_PAST = new NpcStringId(7159);
		BE_PROUD_S1_WE_PROTECT_THIS_WORLD_TOGETHER = new NpcStringId(7160);
		I_HAVE_TO_GO_IVE_GOT_SOME_BUSINESS_TO_TAKE_CARE_OF = new NpcStringId(7161);
		UGH_DONT_LET_HIM_GET_AWAY = new NpcStringId(7162);
		DONT_FORGET_YOUR_PRIDE_YOURE_PROTECTING_THE_WORLD = new NpcStringId(7163);
		HA_HA_HA = new NpcStringId(7164);
		KUH_HUH = new NpcStringId(7165);
		AAH_KUHS1KUH_HUH = new NpcStringId(7166);
		S1RE_MEM_UGHUH = new NpcStringId(7167);
		S1_YOUD_BETTER_LISTEN = new NpcStringId(7250);
		HUH_ITS_CURTAIN_TIME_I_WONT_GET_ANY_MONEY = new NpcStringId(7251);
		UGHIT_CANT_BE = new NpcStringId(7252);
		YOU_WONT_GET_AWAY_THIS_TIME_NARCISSUS = new NpcStringId(7257);
		S1_HELP_ME = new NpcStringId(7258);
		YOU_MUST_BE_AWARE_OF_YOUR_AUDIENCE_WHEN_SINGING_S_JOIN_US_S1_A_SONG_THAT_NOBODY_LISTENS_TO_IS_EMPTY =
				new NpcStringId(7259);
		YOU_MUST_WORK_HARDER_TO_BE_VICTORIOUS_S1 = new NpcStringId(7260);
		MY_SONG_IS_OVER_I_MUST_GO_GOODBYE = new NpcStringId(7261);
		HOW_COULD_I_MISS = new NpcStringId(7262);
		DONT_FORGET_SONG_COMES_WITH_HARMONY = new NpcStringId(7263);
		SING_EVERYONE_WILL_LISTEN = new NpcStringId(7264);
		YOU_DONT_DESERVE_MY_BLESSING = new NpcStringId(7265);
		DO_YOU_REJECT_MY_BLESSING_S1 = new NpcStringId(7266);
		BUT_WHY_S1_EVERYONE_WOULD_PRAISE_YOU = new NpcStringId(7267);
		S1_ATTACK_ME_IM_IMMORTAL_IM_UNRIVALED = new NpcStringId(7350);
		HA_IM_IMMORTAL_THIS_SCAR_WILL_SOON_HEAL_YOULL_DIE_NEXT_TIME = new NpcStringId(7351);
		METELLUS_YOU_PROMISED_ME_AN_IMMORTAL_LIFE_HOW_COULD_I_SWORDMASTER_IRON_LOSE = new NpcStringId(7352);
		FALLEN_ANGEL_ITS_WORTH_TRYING = new NpcStringId(7357);
		HEY_S1_WHY_DONT_YOU_JOIN_ITS_YOUR_BEST_SHOT = new NpcStringId(7358);
		ARE_YOU_INTERESTED_IN_IMMORTAL_LIFE_S1_ITS_TOO_BORING_FOR_ME = new NpcStringId(7359);
		EXCELLENT_S1_SHOW_ME_WHAT_YOU_LEARNED_WHEN_YOUR_LIFE_WAS_ON_THE_LINE = new NpcStringId(7360);
		I_HAVE_TO_GO_TAKE_A_BREAK = new NpcStringId(7361);
		YOU_MISSED_WHAT_A_FOOL_YOU_ARE = new NpcStringId(7362);
		FIGHTING_WITHOUT_RISK_TRAINING_WITHOUT_DANGER_AND_GROWING_WITHOUT_HARDSHIP_WILL_ONLY_LEAD_TO_AN_INFLATED_EGO_DONT_FORGET =
				new NpcStringId(7363);
		DO_YOU_WANT_AN_IMMORTAL_LIFE = new NpcStringId(7364);
		THINK_IT_OVER_AN_IMMORTAL_LIFE_AND_ASSURED_VICTORY = new NpcStringId(7365);
		THATS_GOOD_S1_DO_YOU_WANT_MY_BLESSING_TO_IMPROVE_YOUR_SKILLS = new NpcStringId(7366);
		S1_WHY_DO_YOU_REJECT_GUARANTEED_VICTORY = new NpcStringId(7367);
		IN_THE_NAME_OF_GODS_I_PUNISH_YOU_S1_YOU_CANT_RIVAL_US_ALL_NO_MATTER_HOW_STRONG_YOU_THINK_YOU_ARE =
				new NpcStringId(7450);
		I_HAVE_TO_STOP_FOR_NOW_BUT_ILL_DEFEAT_THE_POWER_OF_THE_DRAGON_YET = new NpcStringId(7451);
		IT_ISTHE_POWER_THAT_SHOULDNT_LIVE = new NpcStringId(7452);
		ISNT_IT_UNWISE_FOR_AN_ANGEL_TO_INTERFERE_IN_HUMAN_AFFAIRS = new NpcStringId(7457);
		THIS_IS_TOUGH_S1_WOULD_YOU_HELP_ME = new NpcStringId(7458);
		S1_HES_KEEPING_AN_EYE_ON_ALL_THOSE_IN_SUCCESSION_TO_THE_BLOOD_OF_DRAGONS_INCLUDING_YOU = new NpcStringId(7459);
		ATTACK_THE_REAR_S1_IF_IM_KILLED_YOURE_NEXT = new NpcStringId(7460);
		I_CANT_STAY_ANY_LONGER_THERE_ARE_TOO_MANY_EYES_ON_US_FAREWELL = new NpcStringId(7461);
		GET_AWAY_YOURE_STILL_ALIVE_BUT = new NpcStringId(7462);
		IF_WE_CANT_AVOID_THIS_FIGHT_WELL_NEED_GREAT_STRENGTH_ITS_THE_ONLY_WAY_TO_PEACE = new NpcStringId(7463);
		WARLORD_YOULL_NEVER_LEARN_THE_TECHNIQUES_OF_THE_DRAGON = new NpcStringId(7464);
		HEY_WHY_BOTHER_WITH_THIS_ISNT_IT_YOUR_MOTHERS_BUSINESS = new NpcStringId(7465);
		S1_ARE_YOU_AGAINST_YOUR_MOTHERS_WISHES_YOURE_NOT_WORTHY_OF_THE_SECRETS_OF_SHILENS_CHILDREN =
				new NpcStringId(7466);
		EXCELLENT_TECHNIQUE_S1_UNFORTUNATELY_YOURE_THE_ONE_DESTINED_FOR_TRAGEDY = new NpcStringId(7467);
		S1_YOU_MAY_FOLLOW_ME_BUT_AN_ORC_IS_NO_MATCH_FOR_MY_GIANTS_STRENGTH = new NpcStringId(7550);
		KUHMY_BODY_FAILSTHIS_IS_THE_END = new NpcStringId(7551);
		HOW_COULD_I_LOSE_WITH_THE_POWERS_OF_A_GIANT_AARGH = new NpcStringId(7552);
		THATS_THE_ENEMY = new NpcStringId(7557);
		HMM_S1_WILL_YOU_JUST_STAND_THERE_DOING_NOTHING = new NpcStringId(7558);
		S1_NOTHING_RISKED_NOTHING_GAINED_ONLY_THOSE_WHO_FIGHT_ENJOY_THE_SPOILS_OF_VICTORY = new NpcStringId(7559);
		A_SWORD_ISNT_JEWELRY_S1_DONT_YOU_AGREE = new NpcStringId(7560);
		WITH_NO_FIGHT_I_HAVE_NO_REASON_TO_STAY = new NpcStringId(7561);
		MISS = new NpcStringId(7562);
		PICK_YOUR_BATTLES_WISELY_OR_YOULL_REGRET_IT = new NpcStringId(7563);
		WHAT_A_FOOL_TO_CHALLENGE_THE_GIANT_OF_THE_OROKA_TRIBE = new NpcStringId(7564);
		RUNNING_LOW_ON_STEAM_I_MUST_WITHDRAW = new NpcStringId(7565);
		S1_YOURE_THE_ONE_WHO_DEFEATED_GUARDIAN_MUHARK = new NpcStringId(7566);
		S1_I_MUST_SUCCEED = new NpcStringId(7567);
		S1_WOULD_YOU_FIGHT_URUZ_WHO_HAS_REACHED_THE_POWER_OF_AZIRA = new NpcStringId(7650);
		I_CANT_HANDLE_THE_POWER_OF_AZIRA_YET_FIRST = new NpcStringId(7651);
		THIS_CANT_BE_HAPPENING_I_HAVE_THE_POWER_OF_AZIRA_HOW_COULD_I_FALL_SO_EASILY = new NpcStringId(7652);
		AZIRA_BORN_FROM_THE_EVIL_FLAME_ILL_KILL_YOU_WITH_MY_BARE_HANDS = new NpcStringId(7657);
		S1_IN_THE_NAME_OF_KHAVATARI_HUBAI_STRIKE_THIS_EVIL_WITH_YOUR_FISTS = new NpcStringId(7658);
		S1_ATTACK_FROM_BOTH_SIDES_HIT_HARD = new NpcStringId(7659);
		S1_STRIKE_WITH_ALL_YOUR_HEART_IT_MUST_WORK = new NpcStringId(7660);
		DAMN_ITS_TIME_TO_GO_UNTIL_NEXT_TIME = new NpcStringId(7661);
		EVIL_SPIRIT_OF_FLAME_I_WONT_GIVE_UP = new NpcStringId(7662);
		MY_FIST_WORKS_EVEN_ON_THE_EVIL_SPIRIT_DONT_FORGET = new NpcStringId(7663);
		FOOLISH_KHAVATARIDO_YOU_THINK_YOUR_POWER_WILL_WORK_ON_ME_IM_THE_SOURCE_OF_YOUR_MARTIAL_ART =
				new NpcStringId(7664);
		NO_MORE_GAMES = new NpcStringId(7665);
		S1ARE_YOU_NEXT_AFTER_KHAVATARI_DO_YOU_KNOW_WHO_I_AM = new NpcStringId(7666);
		S1KASHU_NOT_A_BAD_ATTACK_I_CANT_HOLD_ON_MUCH_LONGER = new NpcStringId(7667);
		S1_AKKAN_YOU_CANT_BE_MY_RIVAL_ILL_KILL_EVERYTHING_ILL_BE_THE_KING = new NpcStringId(7750);
		HA_ILL_SHOW_MERCY_ON_YOU_THIS_TIME_I_KNOW_WELL_OF_YOUR_TECHNIQUE = new NpcStringId(7751);
		I_HAVE_IN_ME_THE_BLOOD_OF_A_KING_HOW_COULD_I_LOSE = new NpcStringId(7752);
		ARE_YOUTYRANT = new NpcStringId(7757);
		YOURE_NOT_A_KING_YOURE_JUST_A_TYRANT_S1_WE_MUST_FIGHT_TOGETHER = new NpcStringId(7758);
		SUCH_RULE_IS_RUINING_THE_COUNTRY_S1_I_CANT_BEAR_THIS_TYRANNY_ANY_LONGER = new NpcStringId(7759);
		S1_LEADERS_MUST_ALWAYS_RESIST_TYRANNY = new NpcStringId(7760);
		I_STAYED_TOO_LONG_ILL_BE_PUNISHED_FOR_BEING_AWAY_SO_LONG = new NpcStringId(7761);
		HE_GOT_AWAY_DAMMIT_WE_MUST_CATCH_THIS_DARK_SOUL = new NpcStringId(7762);
		WHAT_IS_A_KING_WHAT_MUST_ONE_DO_TO_BE_A_GOOD_KING_THINK_IT_OVER = new NpcStringId(7763);
		KNEEL_DOWN_BEFORE_ME_FOOLISH_PEOPLE = new NpcStringId(7764);
		ITS_TIME_FOR_THE_KING_TO_LEAVE_BOW_YOUR_HEAD_AND_SAY_GOODBYE = new NpcStringId(7765);
		S1_YOU_DARE_TO_FIGHT_ME_A_KINGS_POWER_MUST_BE_GREAT_TO_RULE = new NpcStringId(7766);
		YOU_WOULD_FIGHT_THE_KING_S1_TRAITOR = new NpcStringId(7767);
		TEJAKAR_SHARUHI_S1_ILL_SHOW_YOU_THE_POWER_OF_SHARUHI_MOUTH_MUDAHA = new NpcStringId(7850);
		AAARGH_MY_SOUL_WONT_KEEP_QUIET_NOW_I_MUST_TAKE_MY_LEAVE = new NpcStringId(7851);
		NO_SHARUHI_YOURE_SOUL_IS_MINE = new NpcStringId(7852);
		TEJAKAR_OROCA_TEJAKAR_DUDA_MARA = new NpcStringId(7857);
		S1_WE_MUST_FIGHT_THIS_SOUL_TOGETHER_TO_PREVENT_AN_EVERLASTING_WINTER = new NpcStringId(7858);
		S1_THE_SOUL_RESPONDS_TO_YOU_MAY_YOUR_ATTACK_QUIET_HIM = new NpcStringId(7859);
		S1_CALM_SHARUHI_HE_DOESNT_LISTEN_TO_ME_ANYMORE = new NpcStringId(7860);
		ITS_TIME_TO_GO_MAY_THE_ETERNAL_FLAME_BLESS_YOU = new NpcStringId(7861);
		HE_LEFTTHATS_TOO_BADTOO_BAD = new NpcStringId(7862);
		DONT_FORGET_YOUR_STRONG_WILL_NOW = new NpcStringId(7863);
		HA_NOBODY_WILL_RULE_OVER_ME_ANYMORE = new NpcStringId(7864);
		FREEDOM_FREEDOM_FREEDOM = new NpcStringId(7865);
		S1_YOU_RELEASED_ME_BUT_YOU_ALSO_WANT_TO_CATCH_ME_HA = new NpcStringId(7866);
		S1MEALL_RIGHTILL_HELP_YOU = new NpcStringId(7867);
		GET_OUT_OF_HERE_THIS_PLACE_IS_FORBIDDEN_BY_GOD = new NpcStringId(7950);
		EINHASAD_IS_CALLING_ME = new NpcStringId(7951);
		YOU_KILLED_ME_ARENT_YOU_AFRAID_OF_GODS_CURSE = new NpcStringId(7952);
		YOU_BOTHER_MY_MINIONS_S1_DO_YOU_WANT_TO_DIE = new NpcStringId(7953);
		WHAT_THE_HELL_I_LOST = new NpcStringId(7954);
		WHO_ARE_YOU_WHY_ARE_YOU_INTERFERING_IN_OUR_BUSINESS = new NpcStringId(7955);
		YOURE_STRONG_ILL_GET_YOU_NEXT_TIME = new NpcStringId(7956);
		WE_MEET_AGAIN_ILL_HAVE_YOU_THIS_TIME = new NpcStringId(7957);
		A_WORTHY_OPPONENT_S1_HELP_ME = new NpcStringId(7958);
		S1_HURRY_BEFORE_HE_GETS_AWAY = new NpcStringId(7959);
		ILL_KILL_YOU = new NpcStringId(7960);
		WHY_DONT_YOU_FIGHT_ME_SOMEDAY = new NpcStringId(7961);
		I_MISSED_AGAIN_DAMMIT = new NpcStringId(7962);
		IM_SURE_WELL_MEET_AGAIN_SOMEDAY = new NpcStringId(7963);
		CURSE_THOSE_WHO_DEFY_THE_GODS = new NpcStringId(7964);
		YOU_WOULD_FIGHT_ME_A_MESSENGER_OF_THE_GODS = new NpcStringId(7966);
		S1_I_WONT_FORGET_YOU = new NpcStringId(7967);
		S1_HOW_COULD_YOU_DESECRATE_A_HOLY_PLACE = new NpcStringId(8050);
		LEAVE_BEFORE_YOU_ARE_SEVERELY_PUNISHED = new NpcStringId(8051);
		EINHASAD_DONT_GIVE_UP_ON_ME = new NpcStringId(8052);
		S1_SO_YOURE_THE_ONE_WHOS_LOOKING_FOR_ME = new NpcStringId(8053);
		A_MERE_MORTAL_HAS_DEFEATED_ME = new NpcStringId(8054);
		HOW_COWARDLY_TO_INTRUDE_IN_OTHER_PEOPLES_BUSINESS = new NpcStringId(8055);
		TIME_IS_UP = new NpcStringId(8056);
		ILL_KILL_YOU_WITH_MY_SWORD = new NpcStringId(8057);
		HELP_ME = new NpcStringId(8058);
		DONT_MISS = new NpcStringId(8059);
		KEEP_PUSHING = new NpcStringId(8060);
		ILL_GET_HIM_YOULL_HAVE_YOUR_REVENGE = new NpcStringId(8061);
		I_MISSED_HIM_AGAIN_ILL_KILL_YOU = new NpcStringId(8062);
		I_MUST_FOLLOW_HIM = new NpcStringId(8063);
		S1_YOU_SHOULD_LEAVE_IF_YOU_FEAR_GODS_WRATH = new NpcStringId(8150);
		WHATS_GOING_ON = new NpcStringId(8151);
		ILL_SEE_YOU_AGAIN = new NpcStringId(8152);
		WHO_ARE_YOU_WHY_ARE_YOU_BOTHERING_MY_MINIONS = new NpcStringId(8153);
		NO_WAY = new NpcStringId(8154);
		WHY_ARE_YOU_STICKING_YOUR_NOSE_IN_OUR_BUSINESS = new NpcStringId(8155);
		WHO_ARE_YOU_HOW_CAN_A_CREATURE_FROM_THE_NETHERWORLD_BE_SO_POWERFUL = new NpcStringId(8156);
		IS_THIS_THE_END = new NpcStringId(8157);
		SHOW_ME_WHAT_YOURE_MADE_OF_KILL_HIM = new NpcStringId(8158);
		YOU_THINK_YOU_CAN_GET_HIM_WITH_THAT = new NpcStringId(8159);
		PULL_YOURSELF_TOGETHER_HES_TRYING_TO_GET_AWAY = new NpcStringId(8160);
		TELL_THE_BLACK_CAT_THAT_I_GOT_HIS_PAID_BACK = new NpcStringId(8161);
		BLACK_CAT_HELL_BLAME_ME = new NpcStringId(8162);
		I_GOTTA_GO_NOW = new NpcStringId(8163);
		ILL_KILL_YOU_IN_THE_NAME_OF_GOD = new NpcStringId(8166);
		S1_SEE_YOU_LATER = new NpcStringId(8167);
		GET_OUT_BEFORE_YOURE_PUNISHED = new NpcStringId(8251);
		EINHASAD_PLEASE_DONT_GIVE_UP_ON_ME = new NpcStringId(8252);
		S1_ARE_YOU_LOOKING_FOR_ME = new NpcStringId(8253);
		A_MERE_MORTAL_IS_KILLING_ME = new NpcStringId(8254);
		MORTAL_DONT_YOU_RECOGNIZE_MY_GREATNESS = new NpcStringId(8256);
		ILL_GET_YOU_THIS_TIME = new NpcStringId(8257);
		ILL_NEVER_FORGET_THE_TASTE_OF_HIS_STEEL_S1_LETS_FIGHT_HIM_TOGETHER = new NpcStringId(8258);
		S1_PULL_YOURSELF_TOGETHER_WELL_MISS_HIM = new NpcStringId(8259);
		S1_HES_TRYING_TO_GET_AWAY = new NpcStringId(8260);
		I_MISSED_AGAIN_NEXT_TIME = new NpcStringId(8261);
		DAMMIT_FAILED_AGAIN = new NpcStringId(8262);
		YOU_ARE_THE_ONE_WHOS_LOOKING_FOR_ME_S1 = new NpcStringId(8353);
		A_MERE_MORTAL_HAS_KILLED_ME = new NpcStringId(8354);
		WHO_ARE_YOU_THIS_IS_NONE_OF_YOUR_BUSINESS = new NpcStringId(8355);
		S1_PULL_YOURSELF_TOGETHER = new NpcStringId(8359);
		S1_HELL_GET_AWAY = new NpcStringId(8360);
		EINHASAD_PLEASE_DONT_FORSAKE_ME = new NpcStringId(8452);
		LOOKING_FOR_ME_S1 = new NpcStringId(8453);
		S1_BISHOP_HOW_FOOLISH_TO_GO_AGAINST_THE_WILL_OF_GOD = new NpcStringId(8550);
		YOUR_FAITH_IS_STRONGER_THAN_I_THOUGHT_ILL_PAY_YOU_BACK_NEXT_TIME = new NpcStringId(8551);
		TANAKIA_FORGIVE_ME_I_COULDNT_FULFILL_YOUR_DREAM = new NpcStringId(8552);
		S1_YOU_ARE_THE_WON_WHOS_BEEN_BOTHERING_MY_MINIONS = new NpcStringId(8553);
		DAMN_YOUVE_BEATEN_ME = new NpcStringId(8554);
		WHO_ARE_YOU_THIS_ISNT_YOUR_BUSINESS_COWARD = new NpcStringId(8555);
		HOW_WEAK_ILL_FORGIVE_YOU_THIS_TIME_BECAUSE_YOU_MADE_ME_LAUGH = new NpcStringId(8556);
		YOU_ARE_STRONGER_THAN_I_THOUGHT_BUT_IM_NO_WEAKLING = new NpcStringId(8557);
		HES_GOT_A_TOUGH_SHELL_S1_LETS_FIGHT_TOGETHER_AND_CRACK_HIS_SKULL = new NpcStringId(8558);
		S1_WE_WONT_BEAT_HIM_UNLESS_WE_GIVE_IT_OUR_ALL_COME_ON = new NpcStringId(8560);
		ILL_FOLLOW_HIM = new NpcStringId(8561);
		I_MISSED_AGAIN_HES_HARD_TO_FOLLOW = new NpcStringId(8562);
		WELL_SEE_WHAT_THE_FUTURE_BRINGS = new NpcStringId(8563);
		FOR_SHILEN = new NpcStringId(8564);
		ILL_BE_BACK_ILL_DEAL_WITH_YOU_THEN = new NpcStringId(8565);
		S1_ARE_YOU_GOING_TO_FIGHT_ME = new NpcStringId(8566);
		S1_ILL_PAY_YOU_BACK_I_WONT_FORGET_YOU = new NpcStringId(8567);
		S1_PROPHET_HOW_FOOLISH_TO_GO_AGAINST_THE_WILL_OF_GOD = new NpcStringId(8650);
		YOUR_FAITH_IS_STRONGER_THAN_I_THOUGHT_ILL_DEAL_WITH_YOU_NEXT_TIME = new NpcStringId(8651);
		ARE_YOU_THE_ONE_WHOS_BEEN_BOTHERING_MY_MINIONS_S1 = new NpcStringId(8653);
		DAMN_I_CANT_BELIEVE_IVE_BEEN_BEATEN_BY_YOU = new NpcStringId(8654);
		WHO_ARE_YOU_THIS_IS_NONE_OF_YOUR_BUSINESS_COWARD = new NpcStringId(8655);
		ILL_DESTROY_THE_DARKNESS_SURROUNDING_THE_WORLD_WITH_THE_POWER_OF_LIGHT = new NpcStringId(8657);
		S1_FIGHT_THE_FALLEN_ANGEL_WITH_ME_SHOW_THE_TRUE_POWER_OF_LIGHT = new NpcStringId(8658);
		S1_GO_WE_MUST_STOP_FIGHTING_HERE = new NpcStringId(8659);
		WE_MUSTNT_LOSE_S1_PULL_YOURSELF_TOGETHER = new NpcStringId(8660);
		WELL_MEET_AGAIN_IF_FATE_WILLS_IT = new NpcStringId(8661);
		ILL_FOLLOW_THE_COWARDLY_DEVIL = new NpcStringId(8662);
		S1_ELDER_ITS_FOOLISH_OF_YOU_TO_GO_AGAINST_THE_WILL_OF_THE_GODS = new NpcStringId(8750);
		YOURE_STRONGER_THAN_I_THOUGHT_BUT_IM_NO_WEAKLING_EITHER = new NpcStringId(8757);
		S1_WELL_NEVER_WIN_UNLESS_WE_GIVE_IT_OUR_ALL_COME_ON = new NpcStringId(8760);
		ARE_YOU_S1_OH_I_HAVE_THE_RESONANCE_AMULET = new NpcStringId(8850);
		YOURE_FEISTIER_THAN_I_THOUGHT_ILL_QUIT_HERE_FOR_TODAY = new NpcStringId(8851);
		AAARGH_I_CANT_BELIEVE_I_LOST = new NpcStringId(8852);
		YIKES_YOURE_TOUGH = new NpcStringId(8854);
		WHY_DO_YOU_INTERFERE_IN_OTHER_PEOPLES_BUSINESS = new NpcStringId(8855);
		ILL_STOP_HERE_FOR_TODAY = new NpcStringId(8856);
		I_WONT_MISS_YOU_THIS_TIME = new NpcStringId(8857);
		DAMMIT_THIS_IS_TOO_HARD_BY_MYSELF_S1_GIVE_ME_A_HAND = new NpcStringId(8858);
		S1_HURRY_UP_WELL_MISS_HIM = new NpcStringId(8859);
		S1_COME_ON_HURRY_UP = new NpcStringId(8860);
		I_GOTTA_GO_FOLLOW_HIM = new NpcStringId(8861);
		HEY_QUIT_RUNNING_STOP = new NpcStringId(8862);
		SEE_YOU_NEXT_TIME = new NpcStringId(8863);
		WHAT_THINK_YOU_CAN_GET_IN_MY_WAY = new NpcStringId(8864);
		YOU_ARE_SO_WEAK_I_GOTTA_GO_NOW = new NpcStringId(8865);
		S1_GOOD_ILL_HELP_YOU = new NpcStringId(8866);
		S1_YOURE_STRONGER_THAN_I_THOUGHT_SEE_YOU_NEXT_TIME = new NpcStringId(8867);
		YOURE_FEISTIER_THAN_I_THOUGHT_ILL_STOP_HERE_TODAY = new NpcStringId(8951);
		AARGH_I_CANT_BELIEVE_I_LOST = new NpcStringId(8952);
		ILL_STOP_HERE_TODAY = new NpcStringId(8956);
		DAMN_ITS_TOO_MUCH_BY_MYSELFS1_GIVE_ME_A_HAND = new NpcStringId(8958);
		S1_HURRY_WELL_MISS_HIM = new NpcStringId(8959);
		S1_HURRY_PLEASE = new NpcStringId(8960);
		I_GOTTA_GO_FOLLOW_HIM_NOW = new NpcStringId(8961);
		ARE_YOU_RUNNING_AWAY_STOP = new NpcStringId(8962);
		DO_YOU_THINK_YOU_CAN_STOP_ME = new NpcStringId(8964);
		YOURE_SO_WEAK_I_GOTTA_GO_NOW = new NpcStringId(8965);
		YOURE_S1_GOOD_ILL_HELP_YOU = new NpcStringId(8966);
		ARE_YOU_S1_OH_I_HAVE_A_RESONANCE_AMULET = new NpcStringId(9050);
		HEY_YOURE_MORE_TENACIOUS_THAN_I_THOUGHT_ILL_STOP_HERE_TODAY = new NpcStringId(9051);
		DAMMIT_I_CANT_DO_THIS_ALONE_S1_GIVE_ME_A_HAND = new NpcStringId(9058);
		S1_HURRY_OR_WELL_MISS_HIM = new NpcStringId(9059);
		S1_HURRY_UP = new NpcStringId(9060);
		I_GOTTA_FOLLOW_HIM_NOW = new NpcStringId(9061);
		HEY_ARE_YOU_RUNNING_STOP = new NpcStringId(9062);
		OH_YOURE_S1_GOOD_ILL_HELP_YOU = new NpcStringId(9066);
		YOU_CAROUSE_WITH_EVIL_SPIRITS_S1_YOURE_NOT_WORTHY_OF_THE_HOLY_WISDOM = new NpcStringId(9150);
		YOURE_SO_STUBBORN_I_CANT_BOSS_YOU_AROUND_ANY_MORE_CAN_I = new NpcStringId(9151);
		HOW_COULD_IT_HAPPEN_DEFEATED_BY_A_HUMAN = new NpcStringId(9152);
		MY_MASTER_SENT_ME_HERE_ILL_GIVE_YOU_A_HAND = new NpcStringId(9157);
		MEOW_MASTER_S1_HELP_ME = new NpcStringId(9158);
		MASTER_S1_PUNISH_HIM_SO_HE_CANT_BOTHER_BELINDA = new NpcStringId(9159);
		MASTER_S1_WELL_MISS_HIM = new NpcStringId(9160);
		MEOW_MY_MASTER_IS_CALLING_MEOW_I_GOTTA_GO_NOW = new NpcStringId(9161);
		MEOW_I_MISSED_HIM_MEOW = new NpcStringId(9162);
		GOOD_LUCK_MEOW_I_GOTTA_GO_NOW = new NpcStringId(9163);
		CURIOSITY_KILLED_THE_CAT_ILL_SHOW_YOU = new NpcStringId(9164);
		THATS_ALL_FOR_TODAY = new NpcStringId(9165);
		ARE_YOU_TRYING_TO_TAKE_BELINDA_FROM_ME_S1_ILL_SHOW_YOU = new NpcStringId(9166);
		BELINDA_I_LOVE_YOU_YIKES = new NpcStringId(9167);
		YOURE_STUBBORN_AS_A_MULE_GUESS_I_CANT_BOSS_YOU_AROUND_ANY_MORE = new NpcStringId(9251);
		HOW_COULD_IT_BEDEFEATED_BY_AN_ELF = new NpcStringId(9252);
		I_CAME_TO_HELP_YOU_ITS_THE_WILL_OF_RADYSS = new NpcStringId(9257);
		S1_FIGHT_WITH_ME = new NpcStringId(9258);
		S1_WE_MUST_DEFEAT_HIM = new NpcStringId(9259);
		S1_THERES_NO_TIME_WE_MUST_DEFEAT_HIM = new NpcStringId(9260);
		RADYSS_IS_CALLING_ME_I_GOTTA_GO_NOW = new NpcStringId(9261);
		I_WAS_UNABLE_TO_AVENGE_MY_BROTHER = new NpcStringId(9262);
		MAY_YOU_BE_BLESSED = new NpcStringId(9263);
		THE_PROUD_REPENT_THE_FOOLISH_AWAKEN_SINNERS_DIE = new NpcStringId(9264);
		HELLS_MASTER_IS_CALLING_ATONEMENT_WILL_HAVE_TO_WAIT = new NpcStringId(9265);
		S1_ILL_REMEMBER_YOUR_NAME_HEATHEN = new NpcStringId(9266);
		I_WONT_FORGET_THE_NAME_OF_ONE_WHO_DOESNT_OBEY_HOLY_JUDGMENT_S1 = new NpcStringId(9267);
		YOURE_STUBBORN_AS_A_MULE_I_GUESS_I_CANT_BOSS_YOU_AROUND_ANY_MORE = new NpcStringId(9351);
		COULD_IT_BE_DEFEATED_BY_A_DARK_ELF = new NpcStringId(9352);
		SHADOW_SUMMONER_I_CAME_HERE_TO_HELP_YOU = new NpcStringId(9357);
		SHADOW_SUMMONER_S1_FIGHT_WITH_ME = new NpcStringId(9358);
		S1_YOULL_DIE_IF_YOU_DONT_KILL_HIM = new NpcStringId(9359);
		HURRY_S1_DONT_MISS_HIM = new NpcStringId(9360);
		I_CANT_HOLD_ON_ANY_LONGER = new NpcStringId(9361);
		AFTER_ALL_THATI_MISSED_HIM = new NpcStringId(9362);
		SHADOW_SUMMONER_MAY_YOU_BE_BLESSED = new NpcStringId(9363);
		MY_MASTER_SENT_ME_HERE_TO_KILL_YOU = new NpcStringId(9364);
		THE_SHADOW_IS_CALLING_ME = new NpcStringId(9365);
		S1_YOU_WANT_TO_DIE_EARLY_ILL_SEND_YOU_TO_THE_DARKNESS = new NpcStringId(9366);
		YOU_DEAL_IN_DARKNESS_S1_ILL_PAY_YOU_BACK = new NpcStringId(9367);
		YOURE_S1_I_WONT_BE_LIKE_HINDEMITH = new NpcStringId(9450);
		YOURE_FEISTIER_THAN_I_THOUGHT_ILL_STOP_HERE_FOR_TODAY = new NpcStringId(9451);
		ARE_YOU_THE_ONE_WHO_IS_BOTHERING_MY_MINIONS_S1 = new NpcStringId(9453);
		I_CANT_LET_YOU_COMMUNE_WITH_TABLET_OF_VISION_GIVE_ME_THE_RESONANCE_AMULET = new NpcStringId(9457);
		S1_PLEASE_HURRY = new NpcStringId(9460);
		I_MUST_FOLLOW_HIM_NOW = new NpcStringId(9461);
		ARE_YOU_RUNNING_STOP = new NpcStringId(9462);
		ARE_YOU_BETRAYING_ME_I_THOUGHT_SOMETHING_WAS_WRONGILL_STOP_HERE = new NpcStringId(9464);
		YOURE_S1_EVEN_TWO_OF_YOU_CANT_STOP_ME = new NpcStringId(9466);
		DAMMIT_MY_RESONANCE_AMULETS1_ILL_NEVER_FORGET_TO_PAY_YOU_BACK = new NpcStringId(9467);
		ARE_YOU_S1_I_WONT_BE_LIKE_WALDSTEIN = new NpcStringId(9550);
		YIKES_I_CANT_BELIEVE_I_LOST = new NpcStringId(9552);
		ARE_YOU_THE_ONE_BOTHERING_MY_MINIONS_S1 = new NpcStringId(9553);
		YOU_CANT_COMMUNE_WITH_THE_TABLET_OF_VISION_GIVE_ME_THE_RESONANCE_AMULET = new NpcStringId(9557);
		DAMMIT_MY_RESONANCE_AMULETS1_ILL_NEVER_FORGET_THIS = new NpcStringId(9567);
		YOURE_S1_ILL_KILL_YOU_FOR_HALLATE = new NpcStringId(9650);
		YOURE_TOUGHER_THAN_I_THOUGHT_BUT_YOU_STILL_CANT_RIVAL_ME = new NpcStringId(9651);
		HALLATE_FORGIVE_ME_I_CANT_HELP_YOU = new NpcStringId(9652);
		DAMMIT_I_CANT_BELIEVE_YOU_BEAT_ME = new NpcStringId(9654);
		WHO_ARE_YOU_MIND_YOUR_OWN_BUSINESS_COWARD = new NpcStringId(9655);
		PURGATORY_LORD_I_WONT_FAIL_THIS_TIME = new NpcStringId(9657);
		S1_NOWS_THE_TIME_TO_PUT_YOUR_TRAINING_TO_THE_TEST = new NpcStringId(9658);
		S1_YOUR_SWORD_SKILLS_CANT_BE_THAT_BAD = new NpcStringId(9659);
		S1_SHOW_YOUR_STRENGTH = new NpcStringId(9660);
		I_HAVE_SOME_PRESSING_BUSINESS_I_HAVE_TO_GO = new NpcStringId(9661);
		I_MISSED_HIM_DAMMIT = new NpcStringId(9662);
		TRY_AGAIN_SOMETIME = new NpcStringId(9663);
		ILL_KILL_ANYONE_WHO_GETS_IN_MY_WAY = new NpcStringId(9664);
		THIS_IS_PATHETIC_YOU_MAKE_ME_LAUGH = new NpcStringId(9665);
		S1_ARE_YOU_TRYING_TO_GET_IN_MY_WAY = new NpcStringId(9666);
		S1_WHEN_I_COME_BACK_ILL_KILL_YOU = new NpcStringId(9667);
		S1_WAKE_UP_TIME_TO_DIE = new NpcStringId(9750);
		YOURE_TOUGHER_THAN_I_THOUGHT_ILL_BE_BACK = new NpcStringId(9751);
		I_LOST_IT_CANT_BE = new NpcStringId(9752);
		YOURE_A_CUNNING_FIEND_I_WONT_FAIL_AGAIN = new NpcStringId(9757);
		S1_ITS_AFTER_YOU_FIGHT = new NpcStringId(9758);
		S1_YOU_HAVE_TO_FIGHT_BETTER_THAN_THAT_IF_YOU_EXPECT_TO_SURVIVE = new NpcStringId(9759);
		S1_PULL_YOURSELF_TOGETHER_FIGHT = new NpcStringId(9760);
		ILL_CATCH_THE_CUNNING_FIEND = new NpcStringId(9761);
		I_MISSED_HIM_AGAIN_HES_CLEVER = new NpcStringId(9762);
		DONT_COWER_LIKE_A_PUPPY_NEXT_TIME = new NpcStringId(9763);
		I_HAVE_ONLY_ONE_GOAL_GET_OUT_OF_MY_WAY = new NpcStringId(9764);
		JUST_WAIT_YOULL_GET_YOURS = new NpcStringId(9765);
		S1_YOURE_A_COWARD_ARENT_YOU = new NpcStringId(9766);
		S1_ILL_KILL_YOU_NEXT_TIME = new NpcStringId(9767);
		S1_HOW_FOOLISH_TO_ACT_AGAINST_THE_WILL_OF_GOD = new NpcStringId(9850);
		YOUR_FAITH_IS_STRONGER_THAN_I_THOUGHT_ILL_GET_YOU_NEXT_TIME = new NpcStringId(9851);
		WHO_ARE_YOU_MIND_YOUR_OWN_BUSINESS_YOU_COWARD = new NpcStringId(9855);
		TANAKIA_YOUR_LIE_HAS_ALREADY_BEEN_EXPOSED = new NpcStringId(9857);
		S1_HELP_ME_OVERCOME_THIS = new NpcStringId(9858);
		S1_WE_CANT_DEFEAT_TANAKIA_THIS_WAY = new NpcStringId(9859);
		S1_HERES_OUR_CHANCE_DONT_SQUANDER_THE_OPPORTUNITY = new NpcStringId(9860);
		GOODBYE_WELL_MEET_AGAIN_IF_FATE_ALLOWS = new NpcStringId(9861);
		ILL_FOLLOW_TANAKIA_TO_CORRECT_THIS_FALSEHOOD = new NpcStringId(9862);
		ILL_BE_BACK_IF_FATE_ALLOWS = new NpcStringId(9863);
		ILL_BE_BACK_YOULL_PAY = new NpcStringId(9865);
		S1_ARE_YOU_TRYING_TO_SPOIL_MY_PLAN = new NpcStringId(9866);
		S1_I_WONT_FORGET_YOU_YOULL_PAY = new NpcStringId(9867);
		S1_YOU_HAVE_AN_AFFINITY_FOR_DANGEROUS_IDEAS_ARE_YOU_READY_TO_DIE = new NpcStringId(9950);
		MY_TIME_IS_UP = new NpcStringId(9951);
		I_CANT_BELIEVE_I_MUST_KNEEL_TO_A_HUMAN = new NpcStringId(9952);
		MINERVIA_WHATS_THE_MATTER = new NpcStringId(9957);
		THE_PRINCESS_IS_IN_DANGER_WHY_ARE_YOU_STARING = new NpcStringId(9958);
		MASTER_S1_COME_ON_HURRY_UP = new NpcStringId(9959);
		WE_CANT_FAIL_MASTER_S1_PULL_YOURSELF_TOGETHER = new NpcStringId(9960);
		WHAT_AM_I_DOING_I_GOTTA_GO_GOODBYE = new NpcStringId(9961);
		DAMMIT_I_MISSED = new NpcStringId(9962);
		SORRY_BUT_I_MUST_SAY_GOODBYE_AGAIN_GOOD_LUCK_TO_YOU = new NpcStringId(9963);
		I_CANT_YIELD_THE_SECRET_OF_THE_TABLET = new NpcStringId(9964);
		ILL_STOP_HERE_FOR_NOW = new NpcStringId(9965);
		S1_YOU_DARED_TO_LEAVE_SCAR_ON_MY_FACE_ILL_KILL_YOU = new NpcStringId(9966);
		S1_I_WONT_FORGET_YOUR_NAMEHA = new NpcStringId(9967);
		S1_YOU_HAVE_AN_AFFINITY_FOR_BAD_IDEAS_ARE_YOU_READY_TO_DIE = new NpcStringId(10050);
		I_CANT_BELIEVE_I_MUST_KNEEL_BEFORE_A_HUMAN = new NpcStringId(10052);
		YOU_THIEF_GIVE_ME_THE_RESONANCE_AMULET = new NpcStringId(10057);
		UGH_S1_HELP_ME = new NpcStringId(10058);
		S1_PLEASE_HELP_ME_TOGETHER_WE_CAN_BEAT_HIM = new NpcStringId(10059);
		S1_ARE_YOU_GOING_TO_LET_A_GUILD_MEMBER_DIE = new NpcStringId(10060);
		IM_SORRY_BUT_I_GOTTA_GO_FIRST = new NpcStringId(10061);
		AAAAH_I_COULDNT_GET_THE_RESONANCE_AMULET = new NpcStringId(10062);
		TAKE_CARE_I_GOTTA_GO_NOW = new NpcStringId(10063);
		IM_SORRY_BUT_ITS_MY_JOB_TO_KILL_YOU_NOW = new NpcStringId(10064);
		WHAT_A_WASTE_OF_TIME = new NpcStringId(10065);
		S1_HOW_COULD_YOU_DO_THIS_ILL_KILL_YOU = new NpcStringId(10066);
		S1_ILL_PAY_YOU_BACK = new NpcStringId(10067);
		WHY_DONT_YOU_JUST_DIE = new NpcStringId(10068);
		TASTE_THE_STING_OF_LEVEL_5_SPOIL = new NpcStringId(10069);
		THE_ITEM_IS_ALREADY_INSIDE_YOU = new NpcStringId(10070);
		THIS_POTION_YOURE_MAKING_ME_DRINK_IS_WORTH_ITS_WEIGHT_IN_GOLD = new NpcStringId(10071);
		THIS_POTION_IS_PREPARED_FROM_THE_GROUND_GALL_OF_A_BEAR_BE_CAREFUL_IT_PACKS_QUITE_A_PUNCH =
				new NpcStringId(10072);
		HOW_CAN_YOU_USE_A_POTION_ON_A_NEWBIE = new NpcStringId(10073);
		LISTEN_TO_ME_S1_UNLESS_YOU_HAVE_PRIOR_AUTHORIZATION_YOU_CANT_CARRY_A_WEAPON_HERE = new NpcStringId(10074);
		DEAR_S1_MAY_THE_BLESSINGS_OF_EINHASAD_BE_WITH_YOU_ALWAYS = new NpcStringId(10075);
		DEAR_BROTHER_S1_FOLLOW_THE_PATH_OF_LIGHT_WITH_ME = new NpcStringId(10076);
		S1_WHY_WOULD_YOU_CHOOSE_THE_PATH_OF_DARKNESS = new NpcStringId(10077);
		S1_HOW_DARE_YOU_DEFY_THE_WILL_OF_EINHASAD = new NpcStringId(10078);
		THE_DOOR_TO_THE_3RD_FLOOR_OF_THE_ALTAR_IS_NOW_OPEN = new NpcStringId(10079);
		ELROKIAN_HUNTERS = new NpcStringId(11101);
		YOU_S1_YOU_ATTACKED_WENDY_PREPARE_TO_DIE = new NpcStringId(11450);
		S1_YOUR_ENEMY_WAS_DRIVEN_OUT_I_WILL_NOW_WITHDRAW_AND_AWAIT_YOUR_NEXT_COMMAND = new NpcStringId(11451);
		THIS_ENEMY_IS_FAR_TOO_POWERFUL_FOR_ME_TO_FIGHT_I_MUST_WITHDRAW = new NpcStringId(11452);
		THE_RADIO_SIGNAL_DETECTOR_IS_RESPONDING_A_SUSPICIOUS_PILE_OF_STONES_CATCHES_YOUR_EYE = new NpcStringId(11453);
		THIS_LOOKS_LIKE_THE_RIGHT_PLACE = new NpcStringId(11550);
		I_SEE_SOMEONE_IS_THIS_FATE = new NpcStringId(11551);
		WE_MEET_AGAIN = new NpcStringId(11552);
		DONT_BOTHER_TRYING_TO_FIND_OUT_MORE_ABOUT_ME_FOLLOW_YOUR_OWN_DESTINY = new NpcStringId(11553);
		FALLEN_ANGEL_SELECT = new NpcStringId(14204);
		_HOW_DARE_YOU_CHALLENGE_ME = new NpcStringId(15804);
		THE_POWER_OF_LORD_BELETH_RULES_THE_WHOLE_WORLD = new NpcStringId(15805);
		I_WILL_TASTE_YOUR_BLOOD = new NpcStringId(16404);
		I_HAVE_FULFILLED_MY_CONTRACT_WITH_TRADER_CREAMEES = new NpcStringId(16405);
		ILL_CAST_YOU_INTO_AN_ETERNAL_NIGHTMARE = new NpcStringId(17004);
		SEND_MY_SOUL_TO_LICH_KING_ICARUS = new NpcStringId(17005);
		YOU_SHOULD_CONSIDER_GOING_BACK = new NpcStringId(17151);
		THE_VEILED_CREATOR = new NpcStringId(17951);
		THE_CONSPIRACY_OF_THE_ANCIENT_RACE = new NpcStringId(17952);
		CHAOS_AND_TIME = new NpcStringId(17953);
		INTRUDER_ALERT_THE_ALARM_WILL_SELF_DESTRUCT_IN_2_MINUTES = new NpcStringId(18451);
		THE_ALARM_WILL_SELF_DESTRUCT_IN_60_SECONDS_ENTER_PASSCODE_TO_OVERRIDE = new NpcStringId(18452);
		THE_ALARM_WILL_SELF_DESTRUCT_IN_30_SECONDS_ENTER_PASSCODE_TO_OVERRIDE = new NpcStringId(18453);
		THE_ALARM_WILL_SELF_DESTRUCT_IN_10_SECONDS_ENTER_PASSCODE_TO_OVERRIDE = new NpcStringId(18454);
		RECORDER_CRUSHED = new NpcStringId(18455);
		THE_ALARM_WILL_SELF_DESTRUCT_IN_60_SECONDS_PLEASE_EVACUATE_IMMEDIATELY = new NpcStringId(18552);
		THE_ALARM_WILL_SELF_DESTRUCT_IN_30_SECONDS_PLEASE_EVACUATE_IMMEDIATELY = new NpcStringId(18553);
		THE_ALARM_WILL_SELF_DESTRUCT_IN_10_SECONDS_PLEASE_EVACUATE_IMMEDIATELY = new NpcStringId(18554);
		S1_YOU_ARE_NOT_THE_OWNER_OF_THAT_ITEM = new NpcStringId(19304);
		NEXT_TIME_YOU_WILL_NOT_ESCAPE = new NpcStringId(19305);
		S1_YOU_MAY_HAVE_WON_THIS_TIME_BUT_NEXT_TIME_I_WILL_SURELY_CAPTURE_YOU = new NpcStringId(19306);
		INTRUDER_PROTECT_THE_PRIESTS_OF_DAWN = new NpcStringId(19504);
		WHO_ARE_YOU_A_NEW_FACE_LIKE_YOU_CANT_APPROACH_THIS_PLACE = new NpcStringId(19505);
		HOW_DARE_YOU_INTRUDE_WITH_THAT_TRANSFORMATION_GET_LOST = new NpcStringId(19506);
		WHO_DARES_SUMMON_THE_MERCHANT_OF_MAMMON = new NpcStringId(19604);
		THE_ANCIENT_PROMISE_TO_THE_EMPEROR_HAS_BEEN_FULFILLED = new NpcStringId(19605);
		FOR_THE_ETERNITY_OF_EINHASAD = new NpcStringId(19606);
		DEAR_SHILLIENS_OFFSPRINGS_YOU_ARE_NOT_CAPABLE_OF_CONFRONTING_US = new NpcStringId(19607);
		ILL_SHOW_YOU_THE_REAL_POWER_OF_EINHASAD = new NpcStringId(19608);
		DEAR_MILITARY_FORCE_OF_LIGHT_GO_DESTROY_THE_OFFSPRINGS_OF_SHILLIEN = new NpcStringId(19609);
		EVERYTHING_IS_OWING_TO_S1_THANK_YOU = new NpcStringId(19610);
		MY_POWERS_WEAKENING_HURRY_AND_TURN_ON_THE_SEALING_DEVICE = new NpcStringId(19611);
		ALL_4_SEALING_DEVICES_MUST_BE_TURNED_ON = new NpcStringId(19612);
		LILITHS_ATTACK_IS_GETTING_STRONGER_GO_AHEAD_AND_TURN_IT_ON = new NpcStringId(19613);
		DEAR_S1_GIVE_ME_MORE_STRENGTH = new NpcStringId(19614);
		YOU_SUCH_A_FOOL_THE_VICTORY_OVER_THIS_WAR_BELONGS_TO_SHILIEN = new NpcStringId(19615);
		HOW_DARE_YOU_TRY_TO_CONTEND_AGAINST_ME_IN_STRENGTH_RIDICULOUS = new NpcStringId(19616);
		ANAKIM_IN_THE_NAME_OF_GREAT_SHILIEN_I_WILL_CUT_YOUR_THROAT = new NpcStringId(19617);
		YOU_CANNOT_BE_THE_MATCH_OF_LILITH_ILL_TEACH_YOU_A_LESSON = new NpcStringId(19618);
		I_MUST_GO_BACK_TO_SHILIEN_JUST_LIKE_THIS_OUTRAGEOUS = new NpcStringId(19619);
		DEATH_TO_THE_ENEMIES_OF_THE_LORDS_OF_DAWN = new NpcStringId(19804);
		WE_WILL_BE_WITH_YOU_ALWAYS = new NpcStringId(19805);
		YOU_ARE_NOT_THE_OWNER_OF_THAT_ITEM = new NpcStringId(19806);
		EMBRYO = new NpcStringId(19807);
		KAMAEL_TUTORIAL = new NpcStringId(20901);
		IS_IT_A_LACKEY_OF_KAKAI = new NpcStringId(22051);
		TOO_LATE = new NpcStringId(22052);
		HOW_REGRETFUL_UNJUST_DISHONOR = new NpcStringId(22055);
		ILL_GET_REVENGE_SOMEDAY = new NpcStringId(22056);
		INDIGNANT_AND_UNFAIR_DEATH = new NpcStringId(22057);
		THE_CONCEALED_TRUTH_WILL_ALWAYS_BE_REVEALED = new NpcStringId(22719);
		COWARDLY_GUY = new NpcStringId(22720);
		I_AM_A_TREE_OF_NOTHING_A_TREE_THAT_KNOWS_WHERE_TO_RETURN = new NpcStringId(22819);
		I_AM_A_CREATURE_THAT_SHOWS_THE_TRUTH_OF_THE_PLACE_DEEP_IN_MY_HEART = new NpcStringId(22820);
		I_AM_A_MIRROR_OF_DARKNESS_A_VIRTUAL_IMAGE_OF_DARKNESS = new NpcStringId(22821);
		I_ABSOLUTELY_CANNOT_GIVE_IT_TO_YOU_IT_IS_MY_PRECIOUS_JEWEL = new NpcStringId(22933);
		ILL_TAKE_YOUR_LIVES_LATER = new NpcStringId(22934);
		THAT_SWORD_IS_REALLY = new NpcStringId(22935);
		NO_I_HAVENT_COMPLETELY_FINISHED_THE_COMMAND_FOR_DESTRUCTION_AND_SLAUGHTER_YET = new NpcStringId(22936);
		HOW_DARE_YOU_WAKE_ME_NOW_YOU_SHALL_DIE = new NpcStringId(22937);
		START_DUEL = new NpcStringId(23060);
		RULE_VIOLATION = new NpcStringId(23061);
		I_LOSE = new NpcStringId(23062);
		WHHIISSHH = new NpcStringId(23063);
		IM_SORRY_LORD = new NpcStringId(23065);
		WHISH_FIGHT = new NpcStringId(23066);
		LOST_SORRY_LORD = new NpcStringId(23068);
		SO_SHALL_WE_START = new NpcStringId(23072);
		UGH_I_LOST = new NpcStringId(23074);
		ILL_WALK_ALL_OVER_YOU = new NpcStringId(23075);
		UGH_CAN_THIS_BE_HAPPENING = new NpcStringId(23077);
		ITS_THE_NATURAL_RESULT = new NpcStringId(23078);
		HO_HO_I_WIN = new NpcStringId(23079);
		I_WIN = new NpcStringId(23080);
		WHISH_I_WON = new NpcStringId(23081);
		WHO_DARES_TO_TRY_AND_STEAL_MY_NOBLE_BLOOD = new NpcStringId(23434);
		S1_FINALLY_WE_MEET = new NpcStringId(23651);
		HMM_WHERE_DID_MY_FRIEND_GO = new NpcStringId(23652);
		BEST_OF_LUCK_WITH_YOUR_FUTURE_ENDEAVOURS = new NpcStringId(23653);
		S1_DID_YOU_WAIT_FOR_LONG = new NpcStringId(23661);
		DID_YOU_BRING_WHAT_I_ASKED_S1 = new NpcStringId(23671);
		HMM_IS_SOMEONE_APPROACHING = new NpcStringId(23681);
		GRAAAH_WERE_BEING_ATTACKED = new NpcStringId(23682);
		IN_THAT_CASE_I_WISH_YOU_GOOD_LUCK = new NpcStringId(23683);
		S1_HAS_EVERYTHING_BEEN_FOUND = new NpcStringId(23685);
		SAFE_TRAVELS = new NpcStringId(23687);
		REQUEST_FROM_THE_FARM_OWNER = new NpcStringId(25901);
		WHY_DO_YOU_OPPRESS_US_SO = new NpcStringId(31603);
		DONT_INTERRUPT_MY_REST_AGAIN = new NpcStringId(33409);
		YOURE_A_GREAT_DEVIL_NOW = new NpcStringId(33410);
		OH_ITS_NOT_AN_OPPONENT_OF_MINE_HA_HA_HA = new NpcStringId(33411);
		OH_GREAT_DEMON_KING = new NpcStringId(33412);
		REVENGE_IS_OVERLORD_RAMSEBALIUS_OF_THE_EVIL_WORLD = new NpcStringId(33413);
		BONAPARTERIUS_ABYSS_KING_WILL_PUNISH_YOU = new NpcStringId(33414);
		OK_EVERYBODY_PRAY_FERVENTLY = new NpcStringId(33415);
		BOTH_HANDS_TO_HEAVEN_EVERYBODY_YELL_TOGETHER = new NpcStringId(33416);
		ONE_TWO_MAY_YOUR_DREAMS_COME_TRUE = new NpcStringId(33417);
		WHO_KILLED_MY_UNDERLING_DEVIL = new NpcStringId(33418);
		I_WILL_MAKE_YOUR_LOVE_COME_TRUE_LOVE_LOVE_LOVE = new NpcStringId(33420);
		I_HAVE_WISDOM_IN_ME_I_AM_THE_BOX_OF_WISDOM = new NpcStringId(33421);
		OH_OH_OH = new NpcStringId(33422);
		DO_YOU_WANT_US_TO_LOVE_YOU_OH = new NpcStringId(33423);
		WHO_IS_CALLING_THE_LORD_OF_DARKNESS = new NpcStringId(33424);
		I_AM_A_GREAT_EMPIRE_BONAPARTERIUS = new NpcStringId(33425);
		LET_YOUR_HEAD_DOWN_BEFORE_THE_LORD = new NpcStringId(33426);
		THE_SONG_OF_THE_HUNTER = new NpcStringId(33501);
		WELL_TAKE_THE_PROPERTY_OF_THE_ANCIENT_EMPIRE = new NpcStringId(33511);
		SHOW_ME_THE_PRETTY_SPARKLING_THINGS_THEYRE_ALL_MINE = new NpcStringId(33512);
		PRETTY_GOOD = new NpcStringId(33513);
		HA_THAT_WAS_FUN_IF_YOU_WISH_TO_FIND_THE_KEY_SEARCH_THE_CORPSE = new NpcStringId(34830);
		I_HAVE_THE_KEY_WHY_DONT_YOU_COME_AND_TAKE_IT = new NpcStringId(34831);
		YOU_FOOLS_WILL_GET_WHATS_COMING_TO_YOU = new NpcStringId(34832);
		SORRY_ABOUT_THIS_BUT_I_MUST_KILL_YOU_NOW = new NpcStringId(34833);
		YOU_GUYS_WOULDNT_KNOW_THE_SEVEN_SEALS_ARE_ARRRGH = new NpcStringId(34835);
		I_SHALL_DRENCH_THIS_MOUNTAIN_WITH_YOUR_BLOOD = new NpcStringId(34836);
		THAT_DOESNT_BELONG_TO_YOU_DONT_TOUCH_IT = new NpcStringId(34837);
		GET_OUT_OF_MY_SIGHT_YOU_INFIDELS = new NpcStringId(34838);
		WE_DONT_HAVE_ANY_FURTHER_BUSINESS_TO_DISCUSS_HAVE_YOU_SEARCHED_THE_CORPSE_FOR_THE_KEY = new NpcStringId(34839);
		S1_HAS_EARNED_A_STAGE_S2_BLUE_SOUL_CRYSTAL = new NpcStringId(35051);
		S1_HAS_EARNED_A_STAGE_S2_RED_SOUL_CRYSTAL = new NpcStringId(35052);
		S1_HAS_EARNED_A_STAGE_S2_GREEN_SOUL_CRYSTAL = new NpcStringId(35053);
		S1_HAS_EARNED_A_STAGE_S2_BLUE_CURSED_SOUL_CRYSTAL = new NpcStringId(35054);
		S1_HAS_EARNED_A_STAGE_S2_RED_CURSED_SOUL_CRYSTAL = new NpcStringId(35055);
		S1_HAS_EARNED_A_STAGE_S2_GREEN_CURSED_SOUL_CRYSTAL = new NpcStringId(35056);
		SHRIEKS_OF_GHOSTS = new NpcStringId(37101);
		SLOT_S1_S3 = new NpcStringId(38451);
		YOU_CHILDISH_FOOL_DO_YOU_THINK_YOU_CAN_CATCH_ME = new NpcStringId(40306);
		I_MUST_DO_SOMETHING_ABOUT_THIS_SHAMEFUL_INCIDENT = new NpcStringId(40307);
		WHAT_DO_YOU_DARE_TO_CHALLENGE_ME = new NpcStringId(40308);
		THE_RED_EYED_THIEVES_WILL_REVENGE = new NpcStringId(40309);
		GO_AHEAD_YOU_CHILD = new NpcStringId(40310);
		MY_FRIENDS_ARE_SURE_TO_REVENGE = new NpcStringId(40311);
		YOU_RUTHLESS_FOOL_I_WILL_SHOW_YOU_WHAT_REAL_FIGHTING_IS_ALL_ABOUT = new NpcStringId(40312);
		AHH_HOW_CAN_IT_END_LIKE_THIS_IT_IS_NOT_FAIR = new NpcStringId(40313);
		THE_SACRED_FLAME_IS_OURS = new NpcStringId(40909);
		ARRGHHWE_SHALL_NEVER_SURRENDER = new NpcStringId(40910);
		AS_YOU_WISH_MASTER = new NpcStringId(40913);
		MY_DEAR_FRIEND_OF_S1_WHO_HAS_GONE_ON_AHEAD_OF_ME = new NpcStringId(41651);
		LISTEN_TO_TEJAKAR_GANDI_YOUNG_OROKA_THE_SPIRIT_OF_THE_SLAIN_LEOPARD_IS_CALLING_YOU_S1 = new NpcStringId(41652);
		HEY_EVERYBODY_WATCH_THE_EGGS = new NpcStringId(42046);
		I_THOUGHT_ID_CAUGHT_ONE_SHARE_WHEW = new NpcStringId(42047);
		THE_STONE_THE_ELVEN_STONE_BROKE = new NpcStringId(42048);
		IF_THE_EGGS_GET_TAKEN_WERE_DEAD = new NpcStringId(42049);
		GIVE_ME_A_FAIRY_LEAF = new NpcStringId(42111);
		WHY_DO_YOU_BOTHER_ME_AGAIN = new NpcStringId(42112);
		HEY_YOUVE_ALREADY_DRUNK_THE_ESSENCE_OF_WIND = new NpcStringId(42113);
		LEAVE_NOW_BEFORE_YOU_INCUR_THE_WRATH_OF_THE_GUARDIAN_GHOST = new NpcStringId(42114);
		HEY_YOUVE_ALREADY_DRUNK_THE_ESSENCE_OF_A_STAR = new NpcStringId(42115);
		HEY_YOUVE_ALREADY_DRUNK_THE_ESSENCE_OF_DUSK = new NpcStringId(42116);
		HEY_YOUVE_ALREADY_DRUNK_THE_ESSENCE_OF_THE_ABYSS = new NpcStringId(42117);
		WE_MUST_PROTECT_THE_FAIRY_TREE = new NpcStringId(42118);
		GET_OUT_OF_THE_SACRED_TREE_YOU_SCOUNDRELS = new NpcStringId(42119);
		DEATH_TO_THE_THIEVES_OF_THE_PURE_WATER_OF_THE_WORLD = new NpcStringId(42120);
		HEY_IT_SEEMS_LIKE_YOU_NEED_MY_HELP_DOESNT_IT = new NpcStringId(42231);
		ALMOST_GOT_IT_OUCH_STOP_DAMN_THESE_BLOODY_MANACLES = new NpcStringId(42232);
		OH_THAT_SMARTS = new NpcStringId(42233);
		HEY_MASTER_PAY_ATTENTION_IM_DYING_OVER_HERE = new NpcStringId(42234);
		WHAT_HAVE_I_DONE_TO_DESERVE_THIS = new NpcStringId(42235);
		OH_THIS_IS_JUST_GREAT_WHAT_ARE_YOU_GOING_TO_DO_NOW = new NpcStringId(42236);
		YOU_INCONSIDERATE_MORON_CANT_YOU_EVEN_TAKE_CARE_OF_LITTLE_OLD_ME = new NpcStringId(42237);
		OH_NO_THE_MAN_WHO_EATS_ONES_SINS_HAS_DIED_PENITENCE_IS_FURTHER_AWAY = new NpcStringId(42238);
		USING_A_SPECIAL_SKILL_HERE_COULD_TRIGGER_A_BLOODBATH = new NpcStringId(42239);
		HEY_WHAT_DO_YOU_EXPECT_OF_ME = new NpcStringId(42240);
		UGGGGGH_PUSH_ITS_NOT_COMING_OUT = new NpcStringId(42241);
		AH_I_MISSED_THE_MARK = new NpcStringId(42242);
		YAWWWWN_ITS_SO_BORING_HERE_WE_SHOULD_GO_AND_FIND_SOME_ACTION = new NpcStringId(42243);
		HEY_IF_YOU_CONTINUE_TO_WASTE_TIME_YOU_WILL_NEVER_FINISH_YOUR_PENANCE = new NpcStringId(42244);
		I_KNOW_YOU_DONT_LIKE_ME_THE_FEELING_IS_MUTUAL = new NpcStringId(42245);
		I_NEED_A_DRINK = new NpcStringId(42246);
		OH_THIS_IS_DRAGGING_ON_TOO_LONG_AT_THIS_RATE_I_WONT_MAKE_IT_HOME_BEFORE_THE_SEVEN_SEALS_ARE_BROKEN =
				new NpcStringId(42247);
		S1_RECEIVED_A_S2_ITEM_AS_A_REWARD_FROM_THE_SEPARATED_SOUL = new NpcStringId(45650);
		SEALED_VORPAL_HELMET = new NpcStringId(45651);
		SEALED_VORPAL_LEATHER_HELMET = new NpcStringId(45652);
		SEALED_VORPAL_CIRCLET = new NpcStringId(45653);
		SEALED_VORPAL_BREASTPLATE = new NpcStringId(45654);
		SEALED_VORPAL_LEATHER_BREASTPLATE = new NpcStringId(45655);
		SEALED_VORPAL_TUNIC = new NpcStringId(45656);
		SEALED_VORPAL_GAITERS = new NpcStringId(45657);
		SEALED_VORPAL_LEATHER_LEGGING = new NpcStringId(45658);
		SEALED_VORPAL_STOCKING = new NpcStringId(45659);
		SEALED_VORPAL_GAUNTLET = new NpcStringId(45660);
		SEALED_VORPAL_LEATHER_GLOVES = new NpcStringId(45661);
		SEALED_VORPAL_GLOVES = new NpcStringId(45662);
		SEALED_VORPAL_BOOTS = new NpcStringId(45663);
		SEALED_VORPAL_LEATHER_BOOTS = new NpcStringId(45664);
		SEALED_VORPAL_SHOES = new NpcStringId(45665);
		SEALED_VORPAL_SHIELD = new NpcStringId(45666);
		SEALED_VORPAL_SIGIL = new NpcStringId(45667);
		SEALED_VORPAL_RING = new NpcStringId(45668);
		SEALED_VORPAL_EARRING = new NpcStringId(45669);
		SEALED_VORPAL_NECKLACE = new NpcStringId(45670);
		PERIEL_SWORD = new NpcStringId(45671);
		SKULL_EDGE = new NpcStringId(45672);
		VIGWIK_AXE = new NpcStringId(45673);
		DEVILISH_MAUL = new NpcStringId(45674);
		FEATHER_EYE_BLADE = new NpcStringId(45675);
		OCTO_CLAW = new NpcStringId(45676);
		DOUBLETOP_SPEAR = new NpcStringId(45677);
		RISING_STAR = new NpcStringId(45678);
		BLACK_VISAGE = new NpcStringId(45679);
		VENIPLANT_SWORD = new NpcStringId(45680);
		SKULL_CARNIUM_BOW = new NpcStringId(45681);
		GEMTAIL_RAPIER = new NpcStringId(45682);
		FINALE_BLADE = new NpcStringId(45683);
		DOMINION_CROSSBOW = new NpcStringId(45684);
		BLESSED_WEAPON_ENCHANT_SCROLL_S_GRADE = new NpcStringId(45685);
		BLESSED_ARMOR_ENCHANT_SCROLL_S_GRADE = new NpcStringId(45686);
		FIRE_CRYSTAL = new NpcStringId(45687);
		WATER_CRYSTAL = new NpcStringId(45688);
		EARTH_CRYSTAL = new NpcStringId(45689);
		WIND_CRYSTAL = new NpcStringId(45690);
		HOLY_CRYSTAL = new NpcStringId(45691);
		DARK_CRYSTAL = new NpcStringId(45692);
		WEAPON_ENCHANT_SCROLL_S_GRADE = new NpcStringId(45693);
		ATT_ATTACK_S1_RO_ROGUE_S2 = new NpcStringId(46350);
		BINGO = new NpcStringId(50110);
		BLOOD_AND_HONOR = new NpcStringId(50338);
		CURSE_OF_THE_GODS_ON_THE_ONE_THAT_DEFILES_THE_PROPERTY_OF_THE_EMPIRE = new NpcStringId(50339);
		WAR_AND_DEATH = new NpcStringId(50340);
		AMBITION_AND_POWER = new NpcStringId(50341);
		S1_HAS_WON_THE_MAIN_EVENT_FOR_PLAYERS_UNDER_LEVEL_S2_AND_EARNED_S3_POINTS = new NpcStringId(50503);
		S1_HAS_EARNED_S2_POINTS_IN_THE_MAIN_EVENT_FOR_UNLIMITED_LEVELS = new NpcStringId(50504);
		INTO_THE_CHAOS = new NpcStringId(50701);
		YOU_HAVE_SUCCESSFULLY_COMPLETED_A_CLAN_QUEST_S1_POINTS_HAVE_BEEN_ADDED_TO_YOUR_CLANS_REPUTATION_SCORE =
				new NpcStringId(50851);
		THE_FURNACE_WILL_GO_OUT_WATCH_AND_SEE = new NpcStringId(60000);
		THERES_ABOUT_1_MINUTE_LEFT = new NpcStringId(60001);
		THERES_JUST_10_SECONDS_LEFT = new NpcStringId(60002);
		NOW_LIGHT_THE_FURNACES_FIRE = new NpcStringId(60003);
		TIME_IS_UP_AND_YOU_HAVE_FAILED_ANY_MORE_WILL_BE_DIFFICULT = new NpcStringId(60004);
		OH_YOUVE_SUCCEEDED = new NpcStringId(60005);
		AH_IS_THIS_FAILURE_BUT_IT_LOOKS_LIKE_I_CAN_KEEP_GOING = new NpcStringId(60006);
		AH_IVE_FAILED_GOING_FURTHER_WILL_BE_DIFFICULT = new NpcStringId(60007);
		FURNACE_OF_BALANCE = new NpcStringId(60008);
		FURNACE_OF_PROTECTION = new NpcStringId(60009);
		FURNACE_OF_WILL = new NpcStringId(60010);
		FURNACE_OF_MAGIC = new NpcStringId(60011);
		DIVINE_ENERGY_IS_BEGINNING_TO_ENCIRCLE = new NpcStringId(60012);
		FOR_THE_GLORY_OF_SOLINA = new NpcStringId(60013);
		PUNISH_ALL_THOSE_WHO_TREAD_FOOTSTEPS_IN_THIS_PLACE = new NpcStringId(60014);
		WE_ARE_THE_SWORD_OF_TRUTH_THE_SWORD_OF_SOLINA = new NpcStringId(60015);
		WE_RAISE_OUR_BLADES_FOR_THE_GLORY_OF_SOLINA = new NpcStringId(60016);
		HEY_DONT_GO_SO_FAST = new NpcStringId(60018);
		ITS_HARD_TO_FOLLOW = new NpcStringId(60019);
		HUFF_HUFF_YOURE_TOO_FAST_I_CANT_FOLLOW_ANYMORE = new NpcStringId(60020);
		AH_I_THINK_I_REMEMBER_THIS_PLACE = new NpcStringId(60021);
		AH_FRESH_AIR = new NpcStringId(60022);
		WHAT_WERE_YOU_DOING_HERE = new NpcStringId(60023);
		I_GUESS_YOURE_THE_SILENT_TYPE_THEN_ARE_YOU_LOOKING_FOR_TREASURE_LIKE_ME = new NpcStringId(60024);
		WHO_IS_CALLING_ME = new NpcStringId(60403);
		CAN_LIGHT_EXIST_WITHOUT_DARKNESS = new NpcStringId(60404);
		YOU_CANT_AVOID_THE_EYES_OF_UDAN = new NpcStringId(60903);
		UDAN_HAS_ALREADY_SEEN_YOUR_FACE = new NpcStringId(60904);
		THE_MAGICAL_POWER_OF_WATER_COMES_FROM_THE_POWER_OF_STORM_AND_HAIL_IF_YOU_DARE_TO_CONFRONT_IT_ONLY_DEATH_WILL_AWAIT_YOU =
				new NpcStringId(61050);
		THE_POWER_OF_CONSTRAINT_IS_GETTING_WEAKER_YOUR_RITUAL_HAS_FAILED = new NpcStringId(61051);
		YOU_CANT_AVOID_THE_EYES_OF_ASEFA = new NpcStringId(61503);
		ASEFA_HAS_ALREADY_SEEN_YOUR_FACE = new NpcStringId(61504);
		THE_MAGICAL_POWER_OF_FIRE_IS_ALSO_THE_POWER_OF_FLAMES_AND_LAVA_IF_YOU_DARE_TO_CONFRONT_IT_ONLY_DEATH_WILL_AWAIT_YOU =
				new NpcStringId(61650);
		I_SMELL_SOMETHING_DELICIOUS = new NpcStringId(62503);
		OOOH = new NpcStringId(62504);
		AIDING_THE_FLORAN_VILLAGE = new NpcStringId(66001);
		NO_SUCH_CARD = new NpcStringId(66300);
		DEFEAT_THE_ELROKIAN_RAIDERS = new NpcStringId(68801);
		HAVE_YOU_COMPLETED_YOUR_PREPARATIONS_TO_BECOME_A_LORD = new NpcStringId(70851);
		S1_NOW_DEPART = new NpcStringId(70852);
		GO_FIND_SAIUS = new NpcStringId(70853);
		LISTEN_YOU_VILLAGERS_OUR_LIEGE_WHO_WILL_SOON_BECOME_A_LORD_HAS_DEFEATED_THE_HEADLESS_KNIGHT_YOU_CAN_NOW_REST_EASY =
				new NpcStringId(70854);
		S1_DO_YOU_DARE_DEFY_MY_SUBORDINATES = new NpcStringId(70855);
		DOES_MY_MISSION_TO_BLOCK_THE_SUPPLIES_END_HERE = new NpcStringId(70856);
		S1_HAS_BECOME_LORD_OF_THE_TOWN_OF_GLUDIO_LONG_MAY_HE_REIGN = new NpcStringId(70859);
		YOULL_SEE_I_WONT_FORGIVE_YOU_NEXT_TIME = new NpcStringId(70957);
		S1_HAS_BECOME_LORD_OF_THE_TOWN_OF_DION_LONG_MAY_HE_REIGN = new NpcStringId(70959);
		ITS_THE_ENEMY_NO_MERCY = new NpcStringId(71052);
		WHAT_ARE_YOU_DOING_WE_ARE_STILL_SUPERIOR = new NpcStringId(71053);
		HOW_INFURIATING_THIS_ENEMY = new NpcStringId(71054);
		S1_HAS_BECOME_THE_LORD_OF_THE_TOWN_OF_GIRAN_MAY_THERE_BE_GLORY_IN_THE_TERRITORY_OF_GIRAN =
				new NpcStringId(71059);
		MY_LIEGE_WHERE_ARE_YOU = new NpcStringId(71151);
		S1_HAS_BECOME_THE_LORD_OF_THE_TOWN_OF_INNADRIL_MAY_THERE_BE_GLORY_IN_THE_TERRITORY_OF_INNADRIL =
				new NpcStringId(71159);
		YOU_HAVE_FOUND_ALL_THE_NEBULITE_ORBS = new NpcStringId(71252);
		S1_HAS_BECOME_THE_LORD_OF_THE_TOWN_OF_OREN_MAY_THERE_BE_GLORY_IN_THE_TERRITORY_OF_OREN = new NpcStringId(71259);
		S1_HAS_BECOME_THE_LORD_OF_THE_TOWN_OF_ADEN_MAY_THERE_BE_GLORY_IN_THE_TERRITORY_OF_ADEN = new NpcStringId(71351);
		S1_HAS_BECOME_THE_LORD_OF_THE_TOWN_OF_SCHUTTGART_MAY_THERE_BE_GLORY_IN_THE_TERRITORY_OF_SCHUTTGART =
				new NpcStringId(71459);
		S1_I_WILL_REMEMBER_YOU = new NpcStringId(71551);
		FREDERICK_IS_LOOKING_FOR_YOU_MY_LIEGE = new NpcStringId(71653);
		HO_HO_DID_YOU_THINK_YOU_COULD_REALLY_STOP_TRADING_WITH_US = new NpcStringId(71654);
		YOU_HAVE_CHARGED_INTO_THE_TEMPLE = new NpcStringId(71655);
		YOU_ARE_IN_THE_MIDST_OF_DEALING_WITH_THE_HERETIC_OF_HERETIC_TEMPLE = new NpcStringId(71656);
		THE_HERETIC_TEMPLE_IS_DESCENDING_INTO_CHAOS = new NpcStringId(71657);
		S1_HAS_BECOME_THE_LORD_OF_THE_TOWN_OF_RUNE_MAY_THERE_BE_GLORY_IN_THE_TERRITORY_OF_RUNE = new NpcStringId(71659);
		S1_RAISE_YOUR_WEAPONS_FOR_THE_SAKE_OF_THE_TERRITORY = new NpcStringId(71751);
		S1_THE_WAR_IS_OVER_LOWER_YOUR_SWORD_FOR_THE_SAKE_OF_THE_FUTURE = new NpcStringId(71752);
		N91_TERRITORY_BADGES_450_SCORES_IN_INDIVIDUAL_FAME_AND_S2_ADENAS = new NpcStringId(71755);
		THE_MERCENARY_QUEST_NUMBER_IS_S1_MEMOSTATE1_IS_S2_AND_MEMOSTATE2_IS_S3 = new NpcStringId(72903);
		USER_CONNECTED_EVENT_OCCURRENCE_SUCCESS_SIEGE_ID_IS_S1_NUMBER_728_MEMO2_IS_S2_729_MEMO2_IS_S3_AND_255_MEMO1_IS_S4 =
				new NpcStringId(72904);
		TERRITORY_CATAPULT_DYING_EVENT_CATAPULTS_TERRITORY_ID_S1_PARTY_STATUS_S2 = new NpcStringId(72905);
		PROTECT_THE_CATAPULT_OF_GLUDIO = new NpcStringId(72951);
		PROTECT_THE_CATAPULT_OF_DION = new NpcStringId(72952);
		PROTECT_THE_CATAPULT_OF_GIRAN = new NpcStringId(72953);
		PROTECT_THE_CATAPULT_OF_OREN = new NpcStringId(72954);
		PROTECT_THE_CATAPULT_OF_ADEN = new NpcStringId(72955);
		PROTECT_THE_CATAPULT_OF_INNADRIL = new NpcStringId(72956);
		PROTECT_THE_CATAPULT_OF_GODDARD = new NpcStringId(72957);
		PROTECT_THE_CATAPULT_OF_RUNE = new NpcStringId(72958);
		PROTECT_THE_CATAPULT_OF_SCHUTTGART = new NpcStringId(72959);
		THE_CATAPULT_OF_GLUDIO_HAS_BEEN_DESTROYED = new NpcStringId(72961);
		THE_CATAPULT_OF_DION_HAS_BEEN_DESTROYED = new NpcStringId(72962);
		THE_CATAPULT_OF_GIRAN_HAS_BEEN_DESTROYED = new NpcStringId(72963);
		THE_CATAPULT_OF_OREN_HAS_BEEN_DESTROYED = new NpcStringId(72964);
		THE_CATAPULT_OF_ADEN_HAS_BEEN_DESTROYED = new NpcStringId(72965);
		THE_CATAPULT_OF_INNADRIL_HAS_BEEN_DESTROYED = new NpcStringId(72966);
		THE_CATAPULT_OF_GODDARD_HAS_BEEN_DESTROYED = new NpcStringId(72967);
		THE_CATAPULT_OF_RUNE_HAS_BEEN_DESTROYED = new NpcStringId(72968);
		THE_CATAPULT_OF_SCHUTTGART_HAS_BEEN_DESTROYED = new NpcStringId(72969);
		GLUDIO = new NpcStringId(72981);
		DION = new NpcStringId(72982);
		GIRAN = new NpcStringId(72983);
		OREN = new NpcStringId(72984);
		ADEN = new NpcStringId(72985);
		INNADRIL = new NpcStringId(72986);
		GODDARD = new NpcStringId(72987);
		RUNE = new NpcStringId(72988);
		SCHUTTGART = new NpcStringId(72989);
		PROTECT_THE_SUPPLIES_SAFE_OF_GLUDIO = new NpcStringId(73051);
		PROTECT_THE_SUPPLIES_SAFE_OF_DION = new NpcStringId(73052);
		PROTECT_THE_SUPPLIES_SAFE_OF_GIRAN = new NpcStringId(73053);
		PROTECT_THE_SUPPLIES_SAFE_OF_OREN = new NpcStringId(73054);
		PROTECT_THE_SUPPLIES_SAFE_OF_ADEN = new NpcStringId(73055);
		PROTECT_THE_SUPPLIES_SAFE_OF_INNADRIL = new NpcStringId(73056);
		PROTECT_THE_SUPPLIES_SAFE_OF_GODDARD = new NpcStringId(73057);
		PROTECT_THE_SUPPLIES_SAFE_OF_RUNE = new NpcStringId(73058);
		PROTECT_THE_SUPPLIES_SAFE_OF_SCHUTTGART = new NpcStringId(73059);
		THE_SUPPLIES_SAFE_OF_GLUDIO_HAS_BEEN_DESTROYED = new NpcStringId(73061);
		THE_SUPPLIES_SAFE_OF_DION_HAS_BEEN_DESTROYED = new NpcStringId(73062);
		THE_SUPPLIES_SAFE_OF_GIRAN_HAS_BEEN_DESTROYED = new NpcStringId(73063);
		THE_SUPPLIES_SAFE_OF_OREN_HAS_BEEN_DESTROYED = new NpcStringId(73064);
		THE_SUPPLIES_SAFE_OF_ADEN_HAS_BEEN_DESTROYED = new NpcStringId(73065);
		THE_SUPPLIES_SAFE_OF_INNADRIL_HAS_BEEN_DESTROYED = new NpcStringId(73066);
		THE_SUPPLIES_SAFE_OF_GODDARD_HAS_BEEN_DESTROYED = new NpcStringId(73067);
		THE_SUPPLIES_SAFE_OF_RUNE_HAS_BEEN_DESTROYED = new NpcStringId(73068);
		THE_SUPPLIES_SAFE_OF_SCHUTTGART_HAS_BEEN_DESTROYED = new NpcStringId(73069);
		PROTECT_THE_MILITARY_ASSOCIATION_LEADER_OF_GLUDIO = new NpcStringId(73151);
		PROTECT_THE_MILITARY_ASSOCIATION_LEADER_OF_DION = new NpcStringId(73152);
		PROTECT_THE_MILITARY_ASSOCIATION_LEADER_OF_GIRAN = new NpcStringId(73153);
		PROTECT_THE_MILITARY_ASSOCIATION_LEADER_OF_OREN = new NpcStringId(73154);
		PROTECT_THE_MILITARY_ASSOCIATION_LEADER_OF_ADEN = new NpcStringId(73155);
		PROTECT_THE_MILITARY_ASSOCIATION_LEADER_OF_INNADRIL = new NpcStringId(73156);
		PROTECT_THE_MILITARY_ASSOCIATION_LEADER_OF_GODDARD = new NpcStringId(73157);
		PROTECT_THE_MILITARY_ASSOCIATION_LEADER_OF_RUNE = new NpcStringId(73158);
		PROTECT_THE_MILITARY_ASSOCIATION_LEADER_OF_SCHUTTGART = new NpcStringId(73159);
		THE_MILITARY_ASSOCIATION_LEADER_OF_GLUDIO_IS_DEAD = new NpcStringId(73161);
		THE_MILITARY_ASSOCIATION_LEADER_OF_DION_IS_DEAD = new NpcStringId(73162);
		THE_MILITARY_ASSOCIATION_LEADER_OF_GIRAN_IS_DEAD = new NpcStringId(73163);
		THE_MILITARY_ASSOCIATION_LEADER_OF_OREN_IS_DEAD = new NpcStringId(73164);
		THE_MILITARY_ASSOCIATION_LEADER_OF_ADEN_IS_DEAD = new NpcStringId(73165);
		THE_MILITARY_ASSOCIATION_LEADER_OF_INNADRIL_IS_DEAD = new NpcStringId(73166);
		THE_MILITARY_ASSOCIATION_LEADER_OF_GODDARD_IS_DEAD = new NpcStringId(73167);
		THE_MILITARY_ASSOCIATION_LEADER_OF_RUNE_IS_DEAD = new NpcStringId(73168);
		THE_MILITARY_ASSOCIATION_LEADER_OF_SCHUTTGART_IS_DEAD = new NpcStringId(73169);
		PROTECT_THE_RELIGIOUS_ASSOCIATION_LEADER_OF_GLUDIO = new NpcStringId(73251);
		PROTECT_THE_RELIGIOUS_ASSOCIATION_LEADER_OF_DION = new NpcStringId(73252);
		PROTECT_THE_RELIGIOUS_ASSOCIATION_LEADER_OF_GIRAN = new NpcStringId(73253);
		PROTECT_THE_RELIGIOUS_ASSOCIATION_LEADER_OF_OREN = new NpcStringId(73254);
		PROTECT_THE_RELIGIOUS_ASSOCIATION_LEADER_OF_ADEN = new NpcStringId(73255);
		PROTECT_THE_RELIGIOUS_ASSOCIATION_LEADER_OF_INNADRIL = new NpcStringId(73256);
		PROTECT_THE_RELIGIOUS_ASSOCIATION_LEADER_OF_GODDARD = new NpcStringId(73257);
		PROTECT_THE_RELIGIOUS_ASSOCIATION_LEADER_OF_RUNE = new NpcStringId(73258);
		PROTECT_THE_RELIGIOUS_ASSOCIATION_LEADER_OF_SCHUTTGART = new NpcStringId(73259);
		THE_RELIGIOUS_ASSOCIATION_LEADER_OF_GLUDIO_IS_DEAD = new NpcStringId(73261);
		THE_RELIGIOUS_ASSOCIATION_LEADER_OF_DION_IS_DEAD = new NpcStringId(73262);
		THE_RELIGIOUS_ASSOCIATION_LEADER_OF_GIRAN_IS_DEAD = new NpcStringId(73263);
		THE_RELIGIOUS_ASSOCIATION_LEADER_OF_OREN_IS_DEAD = new NpcStringId(73264);
		THE_RELIGIOUS_ASSOCIATION_LEADER_OF_ADEN_IS_DEAD = new NpcStringId(73265);
		THE_RELIGIOUS_ASSOCIATION_LEADER_OF_INNADRIL_IS_DEAD = new NpcStringId(73266);
		THE_RELIGIOUS_ASSOCIATION_LEADER_OF_GODDARD_IS_DEAD = new NpcStringId(73267);
		THE_RELIGIOUS_ASSOCIATION_LEADER_OF_RUNE_IS_DEAD = new NpcStringId(73268);
		THE_RELIGIOUS_ASSOCIATION_LEADER_OF_SCHUTTGART_IS_DEAD = new NpcStringId(73269);
		PROTECT_THE_ECONOMIC_ASSOCIATION_LEADER_OF_GLUDIO = new NpcStringId(73351);
		PROTECT_THE_ECONOMIC_ASSOCIATION_LEADER_OF_DION = new NpcStringId(73352);
		PROTECT_THE_ECONOMIC_ASSOCIATION_LEADER_OF_GIRAN = new NpcStringId(73353);
		PROTECT_THE_ECONOMIC_ASSOCIATION_LEADER_OF_OREN = new NpcStringId(73354);
		PROTECT_THE_ECONOMIC_ASSOCIATION_LEADER_OF_ADEN = new NpcStringId(73355);
		PROTECT_THE_ECONOMIC_ASSOCIATION_LEADER_OF_INNADRIL = new NpcStringId(73356);
		PROTECT_THE_ECONOMIC_ASSOCIATION_LEADER_OF_GODDARD = new NpcStringId(73357);
		PROTECT_THE_ECONOMIC_ASSOCIATION_LEADER_OF_RUNE = new NpcStringId(73358);
		PROTECT_THE_ECONOMIC_ASSOCIATION_LEADER_OF_SCHUTTGART = new NpcStringId(73359);
		THE_ECONOMIC_ASSOCIATION_LEADER_OF_GLUDIO_IS_DEAD = new NpcStringId(73361);
		THE_ECONOMIC_ASSOCIATION_LEADER_OF_DION_IS_DEAD = new NpcStringId(73362);
		THE_ECONOMIC_ASSOCIATION_LEADER_OF_GIRAN_IS_DEAD = new NpcStringId(73363);
		THE_ECONOMIC_ASSOCIATION_LEADER_OF_OREN_IS_DEAD = new NpcStringId(73364);
		THE_ECONOMIC_ASSOCIATION_LEADER_OF_ADEN_IS_DEAD = new NpcStringId(73365);
		THE_ECONOMIC_ASSOCIATION_LEADER_OF_INNADRIL_IS_DEAD = new NpcStringId(73366);
		THE_ECONOMIC_ASSOCIATION_LEADER_OF_GODDARD_IS_DEAD = new NpcStringId(73367);
		THE_ECONOMIC_ASSOCIATION_LEADER_OF_RUNE_IS_DEAD = new NpcStringId(73368);
		THE_ECONOMIC_ASSOCIATION_LEADER_OF_SCHUTTGART_IS_DEAD = new NpcStringId(73369);
		DEFEAT_S1_ENEMY_KNIGHTS = new NpcStringId(73451);
		YOU_HAVE_DEFEATED_S2_OF_S1_KNIGHTS = new NpcStringId(73461);
		YOU_WEAKENED_THE_ENEMYS_DEFENSE = new NpcStringId(73462);
		DEFEAT_S1_WARRIORS_AND_ROGUES = new NpcStringId(73551);
		YOU_HAVE_DEFEATED_S2_OF_S1_WARRIORS_AND_ROGUES = new NpcStringId(73561);
		YOU_WEAKENED_THE_ENEMYS_ATTACK = new NpcStringId(73562);
		DEFEAT_S1_WIZARDS_AND_SUMMONERS = new NpcStringId(73651);
		YOU_HAVE_DEFEATED_S2_OF_S1_ENEMIES = new NpcStringId(73661);
		YOU_WEAKENED_THE_ENEMYS_MAGIC = new NpcStringId(73662);
		DEFEAT_S1_HEALERS_AND_BUFFERS = new NpcStringId(73751);
		YOU_HAVE_DEFEATED_S2_OF_S1_HEALERS_AND_BUFFERS = new NpcStringId(73761);
		YOU_HAVE_WEAKENED_THE_ENEMYS_SUPPORT = new NpcStringId(73762);
		DEFEAT_S1_WARSMITHS_AND_OVERLORDS = new NpcStringId(73851);
		YOU_HAVE_DEFEATED_S2_OF_S1_WARSMITHS_AND_OVERLORDS = new NpcStringId(73861);
		YOU_DESTROYED_THE_ENEMYS_PROFESSIONALS = new NpcStringId(73862);
		TRA_LA_LA_TODAY_IM_GOING_TO_MAKE_ANOTHER_FUN_FILLED_TRIP_I_WONDER_WHAT_I_SHOULD_LOOK_FOR_THIS_TIME =
				new NpcStringId(99601);
		WHATS_THIS_WHY_AM_I_BEING_DISTURBED = new NpcStringId(99700);
		TA_DA_HERE_I_AM = new NpcStringId(99701);
		WHAT_ARE_YOU_LOOKING_AT = new NpcStringId(99702);
		IF_YOU_GIVE_ME_NECTAR_THIS_LITTLE_SQUASH_WILL_GROW_UP_QUICKLY = new NpcStringId(99703);
		ARE_YOU_MY_MOMMY = new NpcStringId(99704);
		FANCY_MEETING_YOU_HERE = new NpcStringId(99705);
		ARE_YOU_AFRAID_OF_THE_BIG_BAD_SQUASH = new NpcStringId(99706);
		IMPRESSIVE_ARENT_I = new NpcStringId(99707);
		OBEY_ME = new NpcStringId(99708);
		RAISE_ME_WELL_AND_YOULL_BE_REWARDED_NEGLECT_ME_AND_SUFFER_THE_CONSEQUENCES = new NpcStringId(99709);
		TRANSFORM = new NpcStringId(99710);
		I_FEEL_DIFFERENT = new NpcStringId(99711);
		IM_BIGGER_NOW_BRING_IT_ON = new NpcStringId(99712);
		IM_NOT_A_KID_ANYMORE = new NpcStringId(99713);
		BIG_TIME = new NpcStringId(99714);
		IM_ALL_GROWN_UP_NOW = new NpcStringId(99716);
		IF_YOU_LET_ME_GO_ILL_BE_YOUR_BEST_FRIEND = new NpcStringId(99717);
		IM_CHUCK_FULL_OF_GOODNESS = new NpcStringId(99718);
		GOOD_JOB_NOW_WHAT_ARE_YOU_GOING_TO_DO = new NpcStringId(99719);
		KEEP_IT_COMING = new NpcStringId(99720);
		THATS_WHAT_IM_TALKING_ABOUT = new NpcStringId(99721);
		MAY_I_HAVE_SOME_MORE = new NpcStringId(99722);
		THAT_HIT_THE_SPOT = new NpcStringId(99723);
		I_FEEL_SPECIAL = new NpcStringId(99724);
		I_THINK_ITS_WORKING = new NpcStringId(99725);
		YOU_DO_UNDERSTAND = new NpcStringId(99726);
		YUCK_WHAT_IS_THIS_HA_HA_JUST_KIDDING = new NpcStringId(99727);
		A_TOTAL_OF_FIVE_AND_ILL_BE_TWICE_AS_ALIVE = new NpcStringId(99728);
		NECTAR_IS_SUBLIME = new NpcStringId(99729);
		YOU_CALL_THAT_A_HIT = new NpcStringId(99730);
		WHY_ARE_YOU_HITTING_ME_OUCH_STOP_IT_GIVE_ME_NECTAR = new NpcStringId(99731);
		STOP_OR_ILL_WILT = new NpcStringId(99732);
		IM_NOT_FULLY_GROWN_YET_OH_WELL_DO_WHAT_YOU_WILL_ILL_FADE_AWAY_WITHOUT_NECTAR_ANYWAY = new NpcStringId(99733);
		GO_AHEAD_AND_HIT_ME_AGAIN_IT_WONT_DO_YOU_ANY_GOOD = new NpcStringId(99734);
		WOE_IS_ME_IM_WILTING = new NpcStringId(99735);
		IM_NOT_FULLY_GROWN_YET_HOW_ABOUT_SOME_NECTAR_TO_EASE_MY_PAIN = new NpcStringId(99736);
		THE_END_IS_NEAR = new NpcStringId(99737);
		PRETTY_PLEASE_WITH_SUGAR_ON_TOP_GIVE_ME_SOME_NECTAR = new NpcStringId(99738);
		IF_I_DIE_WITHOUT_NECTAR_YOULL_GET_NOTHING = new NpcStringId(99739);
		IM_FEELING_BETTER_ANOTHER_THIRTY_SECONDS_AND_ILL_BE_OUT_OF_HERE = new NpcStringId(99740);
		TWENTY_SECONDS_AND_ITS_CIAO_BABY = new NpcStringId(99741);
		WOOHOO_JUST_TEN_SECONDS_LEFT_NINE_EIGHT_SEVEN = new NpcStringId(99742);
		GIVE_ME_NECTAR_OR_ILL_BE_GONE_IN_TWO_MINUTES = new NpcStringId(99743);
		GIVE_ME_NECTAR_OR_ILL_BE_GONE_IN_ONE_MINUTE = new NpcStringId(99744);
		SO_LONG_SUCKERS = new NpcStringId(99745);
		IM_OUT_OF_HERE = new NpcStringId(99746);
		I_MUST_BE_GOING_HAVE_FUN_EVERYBODY = new NpcStringId(99747);
		TIME_IS_UP_PUT_YOUR_WEAPONS_DOWN = new NpcStringId(99748);
		GOOD_FOR_ME_BAD_FOR_YOU = new NpcStringId(99749);
		SOUNDTASTIC = new NpcStringId(99750);
		I_CAN_SING_ALONG_IF_YOU_LIKE = new NpcStringId(99751);
		I_THINK_YOU_NEED_SOME_BACKUP = new NpcStringId(99752);
		KEEP_UP_THAT_RHYTHM_AND_YOULL_BE_A_STAR = new NpcStringId(99753);
		MY_HEART_YEARNS_FOR_MORE_MUSIC = new NpcStringId(99754);
		YOURE_OUT_OF_TUNE_AGAIN = new NpcStringId(99755);
		THIS_IS_AWFUL = new NpcStringId(99756);
		I_THINK_I_BROKE_SOMETHING = new NpcStringId(99757);
		WHAT_A_LOVELY_MELODY_PLAY_IT_AGAIN = new NpcStringId(99758);
		MUSIC_TO_MY_UH_EARS = new NpcStringId(99759);
		YOU_NEED_MUSIC_LESSONS = new NpcStringId(99760);
		I_CANT_HEAR_YOU = new NpcStringId(99761);
		YOU_CANT_HURT_ME_LIKE_THAT = new NpcStringId(99762);
		IM_STRONGER_THAN_YOU_ARE = new NpcStringId(99763);
		NO_MUSIC_IM_OUT_OF_HERE = new NpcStringId(99764);
		THAT_RACKET_IS_GETTING_ON_MY_NERVES_TONE_IT_DOWN_A_BIT = new NpcStringId(99765);
		YOU_CAN_ONLY_HURT_ME_THROUGH_MUSIC = new NpcStringId(99766);
		ONLY_MUSICAL_INSTRUMENTS_CAN_HURT_ME_NOTHING_ELSE = new NpcStringId(99767);
		YOUR_SKILLS_ARE_IMPRESSIVE_BUT_SADLY_USELESS = new NpcStringId(99768);
		CATCH_A_CHRONO_FOR_ME_PLEASE = new NpcStringId(99769);
		YOU_GOT_ME = new NpcStringId(99770);
		NOW_LOOK_AT_WHAT_YOUVE_DONE = new NpcStringId(99771);
		YOU_WIN = new NpcStringId(99772);
		SQUASHED = new NpcStringId(99773);
		DONT_TELL_ANYONE = new NpcStringId(99774);
		GROSS_MY_GUTS_ARE_COMING_OUT = new NpcStringId(99775);
		TAKE_IT_AND_GO = new NpcStringId(99776);
		I_SHOULDVE_LEFT_WHEN_I_COULD = new NpcStringId(99777);
		NOW_LOOK_WHAT_YOU_HAVE_DONE = new NpcStringId(99778);
		I_FEEL_DIRTY = new NpcStringId(99779);
		BETTER_LUCK_NEXT_TIME = new NpcStringId(99780);
		NICE_SHOT = new NpcStringId(99781);
		IM_NOT_AFRAID_OF_YOU = new NpcStringId(99782);
		IF_I_KNEW_THIS_WAS_GOING_TO_HAPPEN_I_WOULD_HAVE_STAYED_HOME = new NpcStringId(99783);
		TRY_HARDER_OR_IM_OUT_OF_HERE = new NpcStringId(99784);
		IM_TOUGHER_THAN_I_LOOK = new NpcStringId(99785);
		GOOD_STRIKE = new NpcStringId(99786);
		OH_MY_GOURD = new NpcStringId(99787);
		THATS_ALL_YOUVE_GOT = new NpcStringId(99788);
		WHY_ME = new NpcStringId(99789);
		BRING_ME_NECTAR = new NpcStringId(99790);
		I_MUST_HAVE_NECTAR_TO_GROW = new NpcStringId(99791);
		GIVE_ME_SOME_NECTAR_QUICKLY_OR_YOULL_GET_NOTHING = new NpcStringId(99792);
		PLEASE_GIVE_ME_SOME_NECTAR_IM_HUNGRY = new NpcStringId(99793);
		NECTAR_PLEASE = new NpcStringId(99794);
		NECTAR_WILL_MAKE_ME_GROW_QUICKLY = new NpcStringId(99795);
		DONT_YOU_WANT_A_BIGGER_SQUASH_GIVE_ME_SOME_NECTAR_AND_ILL_GROW_MUCH_LARGER = new NpcStringId(99796);
		IF_YOU_RAISE_ME_WELL_YOULL_GET_PRIZES_OR_NOT = new NpcStringId(99797);
		YOU_ARE_HERE_FOR_THE_STUFF_EH_WELL_ITS_MINE_ALL_MINE = new NpcStringId(99798);
		TRUST_ME_GIVE_ME_NECTAR_AND_ILL_BECOME_A_GIANT_SQUASH = new NpcStringId(99799);
		THERES_NOTHING_YOU_CANT_SAY_I_CANT_LISTEN_TO_YOU_ANYMORE = new NpcStringId(528551);
		YOU_ADVANCED_BRAVELY_BUT_GOT_SUCH_A_TINY_RESULT_HOHOHO = new NpcStringId(528651);
		A_NON_PERMITTED_TARGET_HAS_BEEN_DISCOVERED = new NpcStringId(1000001);
		INTRUDER_REMOVAL_SYSTEM_INITIATED = new NpcStringId(1000002);
		REMOVING_INTRUDERS = new NpcStringId(1000003);
		A_FATAL_ERROR_HAS_OCCURRED = new NpcStringId(1000004);
		SYSTEM_IS_BEING_SHUT_DOWN = new NpcStringId(1000005);
		DOT_DOT_DOT_DOT_DOT_DOT = new NpcStringId(1000006);
		WE_SHALL_SEE_ABOUT_THAT = new NpcStringId(1000007);
		I_WILL_DEFINITELY_REPAY_THIS_HUMILIATION = new NpcStringId(1000008);
		RETREAT = new NpcStringId(1000009);
		TACTICAL_RETREAT = new NpcStringId(1000010);
		MASS_FLEEING = new NpcStringId(1000011);
		ITS_STRONGER_THAN_EXPECTED = new NpcStringId(1000012);
		ILL_KILL_YOU_NEXT_TIME = new NpcStringId(1000013);
		ILL_DEFINITELY_KILL_YOU_NEXT_TIME = new NpcStringId(1000014);
		OH_HOW_STRONG = new NpcStringId(1000015);
		INVADER = new NpcStringId(1000016);
		THERE_IS_NO_REASON_FOR_YOU_TO_KILL_ME_I_HAVE_NOTHING_YOU_NEED = new NpcStringId(1000017);
		SOMEDAY_YOU_WILL_PAY = new NpcStringId(1000018);
		I_WONT_JUST_STAND_STILL_WHILE_YOU_HIT_ME = new NpcStringId(1000019);
		STOP_HITTING = new NpcStringId(1000020);
		IT_HURTS_TO_THE_BONE = new NpcStringId(1000021);
		AM_I_THE_NEIGHBORHOOD_DRUM_FOR_BEATING = new NpcStringId(1000022);
		FOLLOW_ME_IF_YOU_WANT = new NpcStringId(1000023);
		SURRENDER = new NpcStringId(1000024);
		OH_IM_DEAD = new NpcStringId(1000025);
		ILL_BE_BACK = new NpcStringId(1000026);
		ILL_GIVE_YOU_TEN_MILLION_ARENA_IF_YOU_LET_ME_LIVE = new NpcStringId(1000027);
		S1_STOP_KIDDING_YOURSELF_ABOUT_YOUR_OWN_POWERLESSNESS = new NpcStringId(1000028);
		S1_ILL_MAKE_YOU_FEEL_WHAT_TRUE_FEAR_IS = new NpcStringId(1000029);
		YOURE_REALLY_STUPID_TO_HAVE_CHALLENGED_ME_S1_GET_READY = new NpcStringId(1000030);
		S1_DO_YOU_THINK_THATS_GOING_TO_WORK = new NpcStringId(1000031);
		I_WILL_DEFINITELY_RECLAIM_MY_HONOR_WHICH_HAS_BEEN_TARNISHED = new NpcStringId(1000032);
		SHOW_ME_THE_WRATH_OF_THE_KNIGHT_WHOSE_HONOR_HAS_BEEN_DOWNTRODDEN = new NpcStringId(1000033);
		DEATH_TO_THE_HYPOCRITE = new NpcStringId(1000034);
		ILL_NEVER_SLEEP_UNTIL_IVE_SHED_MY_DISHONOR = new NpcStringId(1000035);
		IM_HERE_FOR_THE_ONES_THAT_ARE_CURSING_THE_WORLD = new NpcStringId(1000036);
		ILL_TURN_YOU_INTO_A_MALIGNANT_SPIRIT = new NpcStringId(1000037);
		ILL_CURSE_YOU_WITH_THE_POWER_OF_REVENGE_AND_HATE = new NpcStringId(1000038);
		FOR_THE_GLORY_OF_GRACIA = new NpcStringId(1000039);
		DO_YOU_DARE_PIT_YOUR_POWER_AGAINST_ME = new NpcStringId(1000040);
		I_I_AM_DEFEATED = new NpcStringId(1000041);
		I_AM_CONVEYING_THE_WILL_OF_NURKA_EVERYBODY_GET_OUT_OF_MY_WAY = new NpcStringId(1000042);
		THOSE_WHO_STAND_AGAINST_ME_SHALL_DIE_HORRIBLY = new NpcStringId(1000043);
		DO_YOU_DARE_TO_BLOCK_MY_WAY = new NpcStringId(1000044);
		MY_COMRADES_WILL_GET_REVENGE = new NpcStringId(1000045);
		YOU_HEATHEN_BLASPHEMERS_OF_THIS_HOLY_PLACE_WILL_BE_PUNISHED = new NpcStringId(1000046);
		STEP_FORWARD_YOU_WORTHLESS_CREATURES_WHO_CHALLENGE_MY_AUTHORITY = new NpcStringId(1000047);
		MY_CREATOR_THE_UNCHANGING_FAITHFULNESS_TO_MY_MASTER = new NpcStringId(1000048);
		MASTER_OF_THE_TOWER_MY_MASTER_MASTER_WHERE_IS_HE = new NpcStringId(1000049);
		I_AM_THE_ONE_CARRYING_OUT_THE_WILL_OF_CORE = new NpcStringId(1000050);
		DESTROY_THE_INVADER = new NpcStringId(1000051);
		STRANGE_CONDITION_DOESNT_WORK = new NpcStringId(1000052);
		ACCORDING_TO_THE_COMMAND_OF_BELETH_IM_GOING_TO_OBSERVE_YOU_GUYS = new NpcStringId(1000053);
		YOU_PEOPLE_MAKE_ME_SICK_NO_SENSE_OF_LOYALTY_WHATSOEVER = new NpcStringId(1000054);
		A_CHALLENGE_AGAINST_ME_IS_THE_SAME_AS_A_CHALLENGE_AGAINST_BELETH = new NpcStringId(1000055);
		BELETH_IS_ALWAYS_WATCHING_OVER_YOU_GUYS = new NpcStringId(1000056);
		THAT_WAS_REALLY_CLOSE_ANTHARAS_OPENED_ITS_EYES = new NpcStringId(1000057);
		YOU_WHO_DISOBEY_THE_WILL_OF_ANTHARAS_DIE = new NpcStringId(1000058);
		ANTHARAS_HAS_TAKEN_MY_LIFE = new NpcStringId(1000059);
		I_CROSSED_BACK_OVER_THE_MARSHLANDS_OF_DEATH_TO_RECLAIM_THE_TREASURE = new NpcStringId(1000060);
		BRING_OVER_AND_SURRENDER_YOUR_PRECIOUS_GOLD_TREASURE_TO_ME = new NpcStringId(1000061);
		ILL_KILL_YOU_IN_AN_INSTANT = new NpcStringId(1000062);
		NO_THE_TREASURE_IS_STILL = new NpcStringId(1000063);
		INVADERS_OF_DRAGON_VALLEY_WILL_NEVER_LIVE_TO_RETURN = new NpcStringId(1000064);
		I_AM_THE_GUARDIAN_THAT_HONORS_THE_COMMAND_OF_ANTHARAS_TO_WATCH_OVER_THIS_PLACE = new NpcStringId(1000065);
		YOUVE_SET_FOOT_IN_DRAGON_VALLEY_WITHOUT_PERMISSION_THE_PENALTY_IS_DEATH = new NpcStringId(1000066);
		THE_JOY_OF_KILLING_THE_ECSTASY_OF_LOOTING_HEY_GUYS_LETS_HAVE_A_GO_AT_IT_AGAIN = new NpcStringId(1000068);
		THERE_REALLY_ARE_STILL_LOTS_OF_FOLKS_IN_THE_WORLD_WITHOUT_FEAR_ILL_TEACH_YOU_A_LESSON =
				new NpcStringId(1000069);
		IF_YOU_HAND_OVER_EVERYTHING_YOUVE_GOT_ILL_AT_LEAST_SPARE_YOUR_LIFE = new NpcStringId(1000070);
		KNEEL_DOWN_BEFORE_ONE_SUCH_AS_THIS = new NpcStringId(1000071);
		HONOR_THE_MASTERS_WISHES_AND_PUNISH_ALL_THE_INVADERS = new NpcStringId(1000072);
		FOLLOW_THE_MASTERS_WISHES_AND_PUNISH_THE_INVADERS = new NpcStringId(1000073);
		DEATH_IS_NOTHING_MORE_THAN_A_MOMENTARY_REST = new NpcStringId(1000074);
		LISTEN_THIS_IS_THE_END_OF_THE_HUMAN_ERA_ANTHARAS_HAS_AWOKEN = new NpcStringId(1000075);
		PRESENT_THE_LIVES_OF_FOUR_PEOPLE_TO_ANTHARAS = new NpcStringId(1000076);
		THIS_IS_UNBELIEVABLE_HOW_COULD_I_HAVE_LOST_TO_ONE_SO_INFERIOR_TO_MYSELF = new NpcStringId(1000077);
		I_CARRY_THE_POWER_OF_DARKNESS_AND_HAVE_RETURNED_FROM_THE_ABYSS = new NpcStringId(1000078);
		ITS_DETESTABLE = new NpcStringId(1000079);
		I_FINALLY_FIND_REST = new NpcStringId(1000080);
		GLORY_TO_ORFEN = new NpcStringId(1000081);
		IN_THE_NAME_OF_ORFEN_I_CAN_NEVER_FORGIVE_YOU_WHO_ARE_INVADING_THIS_PLACE = new NpcStringId(1000082);
		ILL_MAKE_YOU_PAY_THE_PRICE_FOR_FEARLESSLY_ENTERING_ORFENS_LAND = new NpcStringId(1000083);
		EVEN_IF_YOU_DISAPPEAR_INTO_NOTHINGNESS_YOU_WILL_STILL_FACE_THE_LIFE_LONG_SUFFERING_OF_THE_CURSE_THAT_I_HAVE_GIVEN_YOU =
				new NpcStringId(1000084);
		ILL_STAND_AGAINST_ANYONE_THAT_MAKES_LIGHT_OF_THE_SACRED_PLACE_OF_THE_ELVES = new NpcStringId(1000085);
		I_WILL_KILL_WITH_MY_OWN_HANDS_ANYONE_THAT_DEFILES_OUR_HOME = new NpcStringId(1000086);
		MY_BROTHERS_WILL_NEVER_REST_UNTIL_WE_PUSH_YOU_AND_YOUR_GANG_OUT_OF_THIS_VALLEY = new NpcStringId(1000087);
		UNTIL_THE_DAY_OF_DESTRUCTION_OF_HESTUI = new NpcStringId(1000088);
		IF_ANY_INTREPID_ORCS_REMAIN_ATTACK_THEM = new NpcStringId(1000089);
		ILL_BREAK_YOUR_WINDPIPE = new NpcStringId(1000090);
		IS_REVENGE_A_FAILURE = new NpcStringId(1000091);
		THE_SPARKLING_MITHRIL_OF_THE_DWARVES_AND_THEIR_PRETTY_TREASURES_ILL_GET_THEM_ALL = new NpcStringId(1000092);
		WHERE_ARE_ALL_THE_DREADFUL_DWARVES_AND_THEIR_SPARKLING_THINGS = new NpcStringId(1000093);
		HAND_OVER_YOUR_PRETTY_TREASURES = new NpcStringId(1000094);
		HEY_YOU_SHOULD_HAVE_RUN_AWAY = new NpcStringId(1000095);
		DESTRUCTION_EXTINCTION_SLAUGHTER_COLLAPSE_DESTRUCTION_EXTINCTION_SLAUGHTER_COLLAPSE = new NpcStringId(1000096);
		DESTRUCTION_DESTRUCTION_DESTRUCTION_DESTRUCTION = new NpcStringId(1000097);
		DESTRUCTION_DESTRUCTION_DESTRUCTION = new NpcStringId(1000098);
		TA_DA_UTHANKA_HAS_RETURNED = new NpcStringId(1000099);
		WAH_HA_HA_HA_UTHANKA_HAS_TAKEN_OVER_THIS_ISLAND_TODAY = new NpcStringId(1000100);
		WHEW_HES_QUITE_A_GUY = new NpcStringId(1000101);
		HOW_EXASPERATING_AND_UNFAIR_TO_HAVE_THINGS_HAPPEN_IN_SUCH_A_MEANINGLESS_WAY_LIKE_THIS =
				new NpcStringId(1000102);
		THIS_WORLD_SHOULD_BE_FILLED_WITH_FEAR_AND_SADNESS = new NpcStringId(1000103);
		I_WONT_FORGIVE_THE_WORLD_THAT_CURSED_ME = new NpcStringId(1000104);
		ILL_MAKE_EVERYONE_FEEL_THE_SAME_SUFFERING_AS_ME = new NpcStringId(1000105);
		ILL_GIVE_YOU_A_CURSE_THAT_YOULL_NEVER_BE_ABLE_TO_REMOVE_FOREVER = new NpcStringId(1000106);
		ILL_GET_REVENGE_ON_YOU_WHO_SLAUGHTERED_MY_COMPATRIOTS = new NpcStringId(1000107);
		THOSE_WHO_ARE_AFRAID_SHOULD_GET_AWAY_AND_THOSE_WHO_ARE_BRAVE_SHOULD_FIGHT = new NpcStringId(1000108);
		IVE_GOT_POWER_FROM_BELETH_SO_DO_YOU_THINK_ILL_BE_EASILY_DEFEATED = new NpcStringId(1000109);
		I_AM_LEAVING_NOW_BUT_SOON_SOMEONE_WILL_COME_WHO_WILL_TEACH_YOU_ALL_A_LESSON = new NpcStringId(1000110);
		HEY_GUYS_LETS_MAKE_A_ROUND_OF_OUR_TERRITORY = new NpcStringId(1000111);
		THE_RUMOR_IS_THAT_THERE_ARE_WILD_UNCIVILIZED_RUFFIANS_WHO_HAVE_RECENTLY_ARRIVED_IN_MY_TERRITORY =
				new NpcStringId(1000112);
		DO_YOU_KNOW_WHO_I_AM_I_AM_SIROCCO_EVERYONE_ATTACK = new NpcStringId(1000113);
		WHATS_JUST_HAPPENED_THE_INVINCIBLE_SIROCCO_WAS_DEFEATED_BY_SOMEONE_LIKE_YOU = new NpcStringId(1000114);
		OH_IM_REALLY_HUNGRY = new NpcStringId(1000115);
		I_SMELL_FOOD_OOH = new NpcStringId(1000116);
		OOH = new NpcStringId(1000117);
		WHAT_DOES_HONEY_OF_THIS_PLACE_TASTE_LIKE = new NpcStringId(1000118);
		GIVE_ME_SOME_SWEET_DELICIOUS_GOLDEN_HONEY = new NpcStringId(1000119);
		IF_YOU_GIVE_ME_SOME_HONEY_ILL_AT_LEAST_SPARE_YOUR_LIFE = new NpcStringId(1000120);
		ONLY_FOR_LACK_OF_HONEY_DID_I_LOSE_TO_THE_LIKES_OF_YOU = new NpcStringId(1000121);
		WHERE_IS_THE_TRAITOR_KUROBOROS = new NpcStringId(1000122);
		LOOK_IN_EVERY_NOOK_AND_CRANNY_AROUND_HERE = new NpcStringId(1000123);
		ARE_YOU_LACKEY_OF_KUROBOROS_ILL_KNOCK_YOU_OUT_IN_ONE_SHOT = new NpcStringId(1000124);
		HE_JUST_CLOSED_HIS_EYES_WITHOUT_DISPOSING_OF_THE_TRAITOR_HOW_UNFAIR = new NpcStringId(1000125);
		HELL_FOR_UNBELIEVERS_IN_KUROBOROS = new NpcStringId(1000126);
		THE_PERSON_THAT_DOES_NOT_BELIEVE_IN_KUROBOROS_HIS_LIFE_WILL_SOON_BECOME_HELL = new NpcStringId(1000127);
		THE_LACKEY_OF_THAT_DEMENTED_DEVIL_THE_SERVANT_OF_A_FALSE_GOD_ILL_SEND_THAT_FOOL_STRAIGHT_TO_HELL =
				new NpcStringId(1000128);
		UH_IM_NOT_DYING_IM_JUST_DISAPPEARING_FOR_A_MOMENT_ILL_RESURRECT_AGAIN = new NpcStringId(1000129);
		HAIL_TO_KUROBOROS_THE_FOUNDER_OF_OUR_RELIGION = new NpcStringId(1000130);
		ONLY_THOSE_WHO_BELIEVE_IN_PATRIARCH_KUROBOROS_SHALL_RECEIVE_SALVATION = new NpcStringId(1000131);
		ARE_YOU_THE_ONES_THAT_SHARUK_HAS_INCITED_YOU_ALSO_SHOULD_TRUST_IN_KUROBOROS_AND_BE_SAVED =
				new NpcStringId(1000132);
		KUROBOROS_WILL_PUNISH_YOU = new NpcStringId(1000133);
		YOU_WHO_HAVE_BEAUTIFUL_SPIRITS_THAT_SHINE_BRIGHTLY_I_HAVE_RETURNED = new NpcStringId(1000134);
		YOU_THAT_ARE_WEARY_AND_EXHAUSTED_ENTRUST_YOUR_SOULS_TO_ME = new NpcStringId(1000135);
		THE_COLOR_OF_YOUR_SOUL_IS_VERY_ATTRACTIVE = new NpcStringId(1000136);
		THOSE_OF_YOU_WHO_LIVE_DO_YOU_KNOW_HOW_BEAUTIFUL_YOUR_SOULS_ARE = new NpcStringId(1000137);
		IT_WILL_KILL_EVERYONE = new NpcStringId(1000138);
		IM_SO_LONELY = new NpcStringId(1000139);
		MY_ENEMY = new NpcStringId(1000140);
		_NOW_IM_NOT_SO_LONELY = new NpcStringId(1000141);
		I_WILL_NEVER_FORGIVE_THE_PIXY_MURIKA_THAT_IS_TRYING_TO_KILL_US = new NpcStringId(1000142);
		ATTACK_ALL_THE_DULL_AND_STUPID_FOLLOWERS_OF_MURIKA = new NpcStringId(1000143);
		I_DIDNT_HAVE_ANY_IDEA_ABOUT_SUCH_AMBITIONS = new NpcStringId(1000144);
		THIS_IS_NOT_THE_END_ITS_JUST_THE_BEGINNING = new NpcStringId(1000145);
		HEY_SHALL_WE_HAVE_SOME_FUN_FOR_THE_FIRST_TIME_IN_A_LONG_WHILE = new NpcStringId(1000146);
		THEREVE_BEEN_SOME_THINGS_GOING_AROUND_LIKE_CRAZY_HERE_RECENTLY = new NpcStringId(1000147);
		HEY_DO_YOU_KNOW_WHO_I_AM_I_AM_MALEX_HERALD_OF_DAGONIEL_ATTACK = new NpcStringId(1000148);
		WHATS_JUST_HAPPENED_THE_INVINCIBLE_MALEX_JUST_LOST_TO_THE_LIKES_OF_YOU = new NpcStringId(1000149);
		ITS_SOMETHING_REPEATED_IN_A_VAIN_LIFE = new NpcStringId(1000150);
		SHAKE_IN_FEAR_ALL_YOU_WHO_VALUE_YOUR_LIVES = new NpcStringId(1000151);
		ILL_MAKE_YOU_FEEL_SUFFERING_LIKE_A_FLAME_THAT_IS_NEVER_EXTINGUISHED = new NpcStringId(1000152);
		BACK_TO_THE_DIRT = new NpcStringId(1000153);
		HAIL_VARIKA = new NpcStringId(1000154);
		NOBODY_CAN_STOP_US = new NpcStringId(1000155);
		YOU_MOVE_SLOWLY = new NpcStringId(1000156);
		VARIKA_GO_FIRST = new NpcStringId(1000157);
		WHERE_AM_I_WHO_AM_I = new NpcStringId(1000158);
		UH_MY_HEAD_HURTS_LIKE_ITS_GOING_TO_BURST_WHO_AM_I = new NpcStringId(1000159);
		YOU_JERK_YOURE_A_DEVIL_YOURE_A_DEVIL_TO_HAVE_MADE_ME_LIKE_THIS = new NpcStringId(1000160);
		WHERE_AM_I_WHAT_HAPPENED_THANK_YOU = new NpcStringId(1000161);
		UKRU_MASTER = new NpcStringId(1000162);
		ARE_YOU_MATU = new NpcStringId(1000163);
		MARAK_TUBARIN_SABARACHA = new NpcStringId(1000164);
		PAAGRIO_TAMA = new NpcStringId(1000165);
		ACCEPT_THE_WILL_OF_ICARUS = new NpcStringId(1000166);
		THE_PEOPLE_WHO_ARE_BLOCKING_MY_WAY_WILL_NOT_BE_FORGIVEN = new NpcStringId(1000167);
		YOU_ARE_SCUM = new NpcStringId(1000168);
		YOU_LACK_POWER = new NpcStringId(1000169);
		RETURN = new NpcStringId(1000170);
		ADENA_HAS_BEEN_TRANSFERRED = new NpcStringId(1000171);
		EVENT_NUMBER = new NpcStringId(1000172);
		FIRST_PRIZE = new NpcStringId(1000173);
		SECOND_PRIZE = new NpcStringId(1000174);
		THIRD_PRIZE = new NpcStringId(1000175);
		FOURTH_PRIZE = new NpcStringId(1000176);
		THERE_HAS_BEEN_NO_WINNING_LOTTERY_TICKET = new NpcStringId(1000177);
		THE_MOST_RECENT_WINNING_LOTTERY_NUMBERS = new NpcStringId(1000178);
		YOUR_LUCKY_NUMBERS_HAVE_BEEN_SELECTED_ABOVE = new NpcStringId(1000179);
		I_WONDER_WHO_IT_IS_THAT_IS_LURKING_ABOUT = new NpcStringId(1000180);
		SACRED_MAGICAL_RESEARCH_IS_CONDUCTED_HERE = new NpcStringId(1000181);
		BEHOLD_THE_AWESOME_POWER_OF_MAGIC = new NpcStringId(1000182);
		YOUR_POWERS_ARE_IMPRESSIVE_BUT_YOU_MUST_NOT_ANNOY_OUR_HIGH_LEVEL_SORCERER = new NpcStringId(1000183);
		I_AM_BARDA_MASTER_OF_THE_BANDIT_STRONGHOLD = new NpcStringId(1000184);
		I_MASTER_BARDA_ONCE_OWNED_THAT_STRONGHOLD = new NpcStringId(1000185);
		AH_VERY_INTERESTING = new NpcStringId(1000186);
		YOU_ARE_MORE_POWERFUL_THAN_YOU_APPEAR_WELL_MEET_AGAIN = new NpcStringId(1000187);
		YOU_FILTHY_SORCERERS_DISGUST_ME = new NpcStringId(1000188);
		WHY_WOULD_YOU_BUILD_A_TOWER_IN_OUR_TERRITORY = new NpcStringId(1000189);
		ARE_YOU_PART_OF_THAT_EVIL_GANG_OF_SORCERERS = new NpcStringId(1000190);
		THAT_IS_WHY_I_DONT_BOTHER_WITH_ANYONE_BELOW_THE_LEVEL_OF_SORCERER = new NpcStringId(1000191);
		AH_ANOTHER_BEAUTIFUL_DAY = new NpcStringId(1000192);
		OUR_SPECIALTIES_ARE_ARSON_AND_LOOTING = new NpcStringId(1000193);
		YOU_WILL_LEAVE_EMPTY_HANDED = new NpcStringId(1000194);
		AH_SO_YOU_ADMIRE_MY_TREASURE_DO_YOU_TRY_FINDING_IT_HA = new NpcStringId(1000195);
		IS_EVERYBODY_LISTENING_SIRION_HAS_COME_BACK_EVERYONE_CHANT_AND_BOW = new NpcStringId(1000196);
		BOW_DOWN_YOU_WORTHLESS_HUMANS = new NpcStringId(1000197);
		VERY_TACKY = new NpcStringId(1000198);
		DONT_THINK_THAT_YOU_ARE_INVINCIBLE_JUST_BECAUSE_YOU_DEFEATED_ME = new NpcStringId(1000199);
		THE_MATERIAL_DESIRES_OF_MORTALS_ARE_ULTIMATELY_MEANINGLESS = new NpcStringId(1000200);
		DO_NOT_FORGET_THE_REASON_THE_TOWER_OF_INSOLENCE_COLLAPSED = new NpcStringId(1000201);
		YOU_HUMANS_ARE_ALL_ALIKE_FULL_OF_GREED_AND_AVARICE = new NpcStringId(1000202);
		ALL_FOR_NOTHING = new NpcStringId(1000203);
		WHAT_ARE_ALL_THESE_PEOPLE_DOING_HERE = new NpcStringId(1000204);
		I_MUST_FIND_THE_SECRET_OF_ETERNAL_LIFE_HERE_AMONG_THESE_ROTTEN_ANGELS = new NpcStringId(1000205);
		DO_YOU_ALSO_SEEK_THE_SECRET_OF_IMMORTALITY = new NpcStringId(1000206);
		I_SHALL_NEVER_REVEAL_MY_SECRETS = new NpcStringId(1000207);
		WHO_DARES_ENTER_THIS_PLACE = new NpcStringId(1000208);
		THIS_IS_NO_PLACE_FOR_HUMANS_YOU_MUST_LEAVE_IMMEDIATELY = new NpcStringId(1000209);
		YOU_POOR_CREATURES_TOO_STUPID_TO_REALIZE_YOUR_OWN_IGNORANCE = new NpcStringId(1000210);
		YOU_MUSTNT_GO_THERE = new NpcStringId(1000211);
		WHO_DARES_DISTURB_THIS_MARSH = new NpcStringId(1000212);
		THE_HUMANS_MUST_NOT_BE_ALLOWED_TO_DESTROY_THE_MARSHLAND_FOR_THEIR_GREEDY_PURPOSES = new NpcStringId(1000213);
		YOU_ARE_A_BRAVE_MAN = new NpcStringId(1000214);
		YOU_IDIOTS_SOME_DAY_YOU_SHALL_ALSO_BE_GONE = new NpcStringId(1000215);
		SOMEONE_HAS_ENTERED_THE_FOREST = new NpcStringId(1000216);
		THE_FOREST_IS_VERY_QUIET_AND_PEACEFUL = new NpcStringId(1000217);
		STAY_HERE_IN_THIS_WONDERFUL_FOREST = new NpcStringId(1000218);
		MY_MY_SOULS = new NpcStringId(1000219);
		THIS_FOREST_IS_A_DANGEROUS_PLACE = new NpcStringId(1000220);
		UNLESS_YOU_LEAVE_THIS_FOREST_IMMEDIATELY_YOU_ARE_BOUND_TO_RUN_INTO_SERIOUS_TROUBLE = new NpcStringId(1000221);
		LEAVE_NOW = new NpcStringId(1000222);
		WHY_DO_YOU_IGNORE_MY_WARNING = new NpcStringId(1000223);
		HARITS_OF_THE_WORLD_I_BRING_YOU_PEACE = new NpcStringId(1000224);
		HARITS_BE_COURAGEOUS = new NpcStringId(1000225);
		I_SHALL_EAT_YOUR_STILL_BEATING_HEART = new NpcStringId(1000226);
		HARITS_KEEP_FAITH_UNTIL_THE_DAY_I_RETURN_NEVER_LOSE_HOPE = new NpcStringId(1000227);
		EVEN_THE_GIANTS_ARE_GONE_THERES_NOTHING_LEFT_TO_BE_AFRAID_OF_NOW = new NpcStringId(1000228);
		HAVE_YOU_HEARD_OF_THE_GIANTS_THEIR_DOWNFALL_WAS_INEVITABLE = new NpcStringId(1000229);
		WHAT_NERVE_DO_YOU_DARE_TO_CHALLENGE_ME = new NpcStringId(1000230);
		YOU_ARE_AS_EVIL_AS_THE_GIANTS = new NpcStringId(1000231);
		THIS_DUNGEON_IS_STILL_IN_GOOD_CONDITION = new NpcStringId(1000232);
		THIS_PLACE_IS_SPECTACULAR_WOULDNT_YOU_SAY = new NpcStringId(1000233);
		YOU_ARE_VERY_BRAVE_WARRIORS = new NpcStringId(1000234);
		ARE_THE_GIANTS_TRULY_GONE_FOR_GOOD = new NpcStringId(1000235);
		THESE_GRAVES_ARE_GOOD = new NpcStringId(1000236);
		GOLD_AND_SILVER_ARE_MEANINGLESS_TO_A_DEAD_MAN = new NpcStringId(1000237);
		WHY_WOULD_THOSE_CORRUPT_ARISTOCRATS_BURY_SUCH_USEFUL_THINGS = new NpcStringId(1000238);
		YOU_FILTHY_PIG_EAT_AND_BE_MERRY_NOW_THAT_YOU_HAVE_SHIRKED_YOUR_RESPONSIBILITIES = new NpcStringId(1000239);
		THOSE_THUGS_IT_WOULD_BE_TOO_MERCIFUL_TO_RIP_THEM_APART_AND_CHEW_THEM_UP_ONE_AT_A_TIME =
				new NpcStringId(1000240);
		YOU_ACCURSED_SCOUNDRELS = new NpcStringId(1000241);
		HMM_COULD_THIS_BE_THE_ASSASSIN_SENT_BY_THOSE_IDIOTS_FROM_ADEN = new NpcStringId(1000242);
		I_SHALL_CURSE_YOUR_NAME_WITH_MY_LAST_BREATH = new NpcStringId(1000243);
		MY_BELOVED_LORD_SHILEN = new NpcStringId(1000244);
		I_MUST_BREAK_THE_SEAL_AND_RELEASE_LORD_SHILEN_AS_SOON_AS_POSSIBLE = new NpcStringId(1000245);
		YOU_SHALL_TASTE_THE_VENGEANCE_OF_LORD_SHILEN = new NpcStringId(1000246);
		LORD_SHILEN_SOME_DAY_YOU_WILL_ACCOMPLISH_THIS_MISSION = new NpcStringId(1000247);
		TOWARDS_IMMORTALITY = new NpcStringId(1000248);
		HE_WHO_DESIRES_IMMORTALITY_COME_UNTO_ME = new NpcStringId(1000249);
		YOU_SHALL_BE_SACRIFICED_TO_GAIN_MY_IMMORTALITY = new NpcStringId(1000250);
		ETERNAL_LIFE_IN_FRONT_OF_MY_EYES_I_HAVE_COLLAPSED_IN_SUCH_A_WORTHLESS_WAY_LIKE_THIS = new NpcStringId(1000251);
		ZAKEN_YOU_ARE_A_COWARDLY_CUR = new NpcStringId(1000252);
		YOU_ARE_IMMORTAL_ARENT_YOU_ZAKEN = new NpcStringId(1000253);
		PLEASE_RETURN_MY_BODY_TO_ME = new NpcStringId(1000254);
		FINALLY_WILL_I_BE_ABLE_TO_REST = new NpcStringId(1000255);
		WHAT_IS_ALL_THAT_RACKET = new NpcStringId(1000256);
		MASTER_GILDOR_DOES_NOT_LIKE_TO_BE_DISTURBED = new NpcStringId(1000257);
		PLEASE_JUST_HOLD_IT_DOWN = new NpcStringId(1000258);
		IF_YOU_DISTURB_MASTER_GILDOR_I_WONT_BE_ABLE_TO_HELP_YOU = new NpcStringId(1000259);
		WHO_DARES_APPROACH = new NpcStringId(1000260);
		THESE_REEDS_ARE_MY_TERRITORY = new NpcStringId(1000261);
		YOU_FOOLS_TODAY_YOU_SHALL_LEARN_A_LESSON = new NpcStringId(1000262);
		THE_PAST_GOES_BY_IS_A_NEW_ERA_BEGINNING = new NpcStringId(1000263);
		THIS_IS_THE_GARDEN_OF_EVA = new NpcStringId(1000264);
		THE_GARDEN_OF_EVA_IS_A_SACRED_PLACE = new NpcStringId(1000265);
		DO_YOU_MEAN_TO_INSULT_EVA = new NpcStringId(1000266);
		HOW_RUDE_EVAS_LOVE_IS_AVAILABLE_TO_ALL_EVEN_TO_AN_ILL_MANNERED_LOUT_LIKE_YOURSELF = new NpcStringId(1000267);
		THIS_PLACE_ONCE_BELONGED_TO_LORD_SHILEN = new NpcStringId(1000268);
		LEAVE_THIS_PALACE_TO_US_SPIRITS_OF_EVA = new NpcStringId(1000269);
		WHY_ARE_YOU_GETTING_IN_OUR_WAY = new NpcStringId(1000270);
		SHILEN_OUR_SHILEN = new NpcStringId(1000271);
		ALL_WHO_FEAR_OF_FAFURION_LEAVE_THIS_PLACE_AT_ONCE = new NpcStringId(1000272);
		YOU_ARE_BEING_PUNISHED_IN_THE_NAME_OF_FAFURION = new NpcStringId(1000273);
		OH_MASTER_PLEASE_FORGIVE_YOUR_HUMBLE_SERVANT = new NpcStringId(1000274);
		PREPARE_TO_DIE_FOREIGN_INVADERS_I_AM_GUSTAV_THE_ETERNAL_RULER_OF_THIS_FORTRESS_AND_I_HAVE_TAKEN_UP_MY_SWORD_TO_REPEL_THEE =
				new NpcStringId(1000275);
		GLORY_TO_ADEN_THE_KINGDOM_OF_THE_LION_GLORY_TO_SIR_GUSTAV_OUR_IMMORTAL_LORD = new NpcStringId(1000276);
		SOLDIERS_OF_GUSTAV_GO_FORTH_AND_DESTROY_THE_INVADERS = new NpcStringId(1000277);
		THIS_IS_UNBELIEVABLE_HAVE_I_REALLY_BEEN_DEFEATED_I_SHALL_RETURN_AND_TAKE_YOUR_HEAD = new NpcStringId(1000278);
		COULD_IT_BE_THAT_I_HAVE_REACHED_MY_END_I_CANNOT_DIE_WITHOUT_HONOR_WITHOUT_THE_PERMISSION_OF_SIR_GUSTAV =
				new NpcStringId(1000279);
		AH_THE_BITTER_TASTE_OF_DEFEAT_I_FEAR_MY_TORMENTS_ARE_NOT_OVER = new NpcStringId(1000280);
		I_FOLLOW_THE_WILL_OF_FAFURION = new NpcStringId(1000281);
		TICKETS_FOR_THE_LUCKY_LOTTERY_ARE_NOW_ON_SALE = new NpcStringId(1000282);
		THE_LUCKY_LOTTERY_DRAWING_IS_ABOUT_TO_BEGIN = new NpcStringId(1000283);
		THE_WINNING_NUMBERS_FOR_LUCKY_LOTTERY_S1_ARE_S2_CONGRATULATIONS_TO_THE_WINNERS = new NpcStringId(1000284);
		YOURE_TOO_YOUNG_TO_PLAY_LUCKY_LOTTERY = new NpcStringId(1000285);
		S1_WATCH_YOUR_BACK = new NpcStringId(1000286);
		S1_COME_ON_ILL_TAKE_YOU_ON = new NpcStringId(1000287);
		S1_HOW_DARE_YOU_INTERRUPT_OUR_FIGHT_HEY_GUYS_HELP = new NpcStringId(1000288);
		ILL_HELP_YOU_IM_NO_COWARD = new NpcStringId(1000289);
		DEAR_ULTIMATE_POWER = new NpcStringId(1000290);
		EVERYBODY_ATTACK_S1 = new NpcStringId(1000291);
		I_WILL_FOLLOW_YOUR_ORDER = new NpcStringId(1000292);
		BET_YOU_DIDNT_EXPECT_THIS = new NpcStringId(1000293);
		COME_OUT_YOU_CHILDREN_OF_DARKNESS = new NpcStringId(1000294);
		SUMMON_PARTY_MEMBERS = new NpcStringId(1000295);
		MASTER_DID_YOU_CALL_ME = new NpcStringId(1000296);
		YOU_IDIOT = new NpcStringId(1000297);
		WHAT_ABOUT_THIS = new NpcStringId(1000298);
		VERY_IMPRESSIVE_S1_THIS_IS_THE_LAST = new NpcStringId(1000299);
		DAWN = new NpcStringId(1000300);
		DUSK = new NpcStringId(1000301);
		NOTHINGNESS = new NpcStringId(1000302);
		THIS_WORLD_WILL_SOON_BE_ANNIHILATED = new NpcStringId(1000303);
		A_CURSE_UPON_YOU = new NpcStringId(1000304);
		THE_DAY_OF_JUDGMENT_IS_NEAR = new NpcStringId(1000305);
		I_BESTOW_UPON_YOU_A_BLESSING = new NpcStringId(1000306);
		THE_FIRST_RULE_OF_FIGHTING_IS_TO_START_BY_KILLING_THE_WEAK_ONES = new NpcStringId(1000307);
		ADENA = new NpcStringId(1000308);
		ANCIENT_ADENA = new NpcStringId(1000309);
		LEVEL_31_OR_LOWER = new NpcStringId(1000312);
		LEVEL_42_OR_LOWER = new NpcStringId(1000313);
		LEVEL_53_OR_LOWER = new NpcStringId(1000314);
		LEVEL_64_OR_LOWER = new NpcStringId(1000315);
		NO_LEVEL_LIMIT = new NpcStringId(1000316);
		THE_MAIN_EVENT_WILL_START_IN_2_MINUTES_PLEASE_REGISTER_NOW = new NpcStringId(1000317);
		THE_MAIN_EVENT_IS_NOW_STARTING = new NpcStringId(1000318);
		THE_MAIN_EVENT_WILL_CLOSE_IN_5_MINUTES = new NpcStringId(1000319);
		THE_MAIN_EVENT_WILL_FINISH_IN_2_MINUTES_PLEASE_PREPARE_FOR_THE_NEXT_GAME = new NpcStringId(1000320);
		THE_AMOUNT_OF_SSQ_CONTRIBUTION_HAS_INCREASED_BY_S1 = new NpcStringId(1000321);
		NO_RECORD_EXISTS = new NpcStringId(1000322);
		THAT_WILL_DO_ILL_MOVE_YOU_TO_THE_OUTSIDE_SOON = new NpcStringId(1000380);
		YOUR_REAR_IS_PRACTICALLY_UNGUARDED = new NpcStringId(1000382);
		HOW_DARE_YOU_TURN_YOUR_BACK_ON_ME = new NpcStringId(1000383);
		S1_ILL_DEAL_WITH_YOU_MYSELF = new NpcStringId(1000384);
		S1_THIS_IS_PERSONAL = new NpcStringId(1000385);
		S1_LEAVE_US_ALONE_THIS_IS_BETWEEN_US = new NpcStringId(1000386);
		S1_KILLING_YOU_WILL_BE_A_PLEASURE = new NpcStringId(1000387);
		S1_HEY_WERE_HAVING_A_DUEL_HERE = new NpcStringId(1000388);
		THE_DUEL_IS_OVER_ATTACK = new NpcStringId(1000389);
		FOUL_KILL_THE_COWARD = new NpcStringId(1000390);
		HOW_DARE_YOU_INTERRUPT_A_SACRED_DUEL_YOU_MUST_BE_TAUGHT_A_LESSON = new NpcStringId(1000391);
		DIE_YOU_COWARD = new NpcStringId(1000392);
		KILL_THE_COWARD = new NpcStringId(1000394);
		I_NEVER_THOUGHT_ID_USE_THIS_AGAINST_A_NOVICE = new NpcStringId(1000395);
		YOU_WONT_TAKE_ME_DOWN_EASILY = new NpcStringId(1000396);
		THE_BATTLE_HAS_JUST_BEGUN = new NpcStringId(1000397);
		KILL_S1_FIRST = new NpcStringId(1000398);
		ATTACK_S1 = new NpcStringId(1000399);
		ATTACK_ATTACK = new NpcStringId(1000400);
		OVER_HERE = new NpcStringId(1000401);
		ROGER = new NpcStringId(1000402);
		SHOW_YOURSELVES = new NpcStringId(1000403);
		FORCES_OF_DARKNESS_FOLLOW_ME = new NpcStringId(1000404);
		DESTROY_THE_ENEMY_MY_BROTHERS = new NpcStringId(1000405);
		NOW_THE_FUN_STARTS = new NpcStringId(1000406);
		ENOUGH_FOOLING_AROUND_GET_READY_TO_DIE = new NpcStringId(1000407);
		YOU_IDIOT_IVE_JUST_BEEN_TOYING_WITH_YOU = new NpcStringId(1000408);
		WITNESS_MY_TRUE_POWER = new NpcStringId(1000409);
		NOW_THE_BATTLE_BEGINS = new NpcStringId(1000410);
		I_MUST_ADMIT_NO_ONE_MAKES_MY_BLOOD_BOIL_QUITE_LIKE_YOU_DO = new NpcStringId(1000411);
		YOU_HAVE_MORE_SKILL_THAN_I_THOUGHT = new NpcStringId(1000412);
		ILL_DOUBLE_MY_STRENGTH = new NpcStringId(1000413);
		PREPARE_TO_DIE = new NpcStringId(1000414);
		ALL_IS_LOST_PREPARE_TO_MEET_THE_GODDESS_OF_DEATH = new NpcStringId(1000415);
		ALL_IS_LOST_THE_PROPHECY_OF_DESTRUCTION_HAS_BEEN_FULFILLED = new NpcStringId(1000416);
		THE_END_OF_TIME_HAS_COME_THE_PROPHECY_OF_DESTRUCTION_HAS_BEEN_FULFILLED = new NpcStringId(1000417);
		S1_YOU_BRING_AN_ILL_WIND = new NpcStringId(1000418);
		S1_YOU_MIGHT_AS_WELL_GIVE_UP = new NpcStringId(1000419);
		YOU_DONT_HAVE_ANY_HOPE_YOUR_END_HAS_COME = new NpcStringId(1000420);
		THE_PROPHECY_OF_DARKNESS_HAS_BEEN_FULFILLED = new NpcStringId(1000421);
		AS_FORETOLD_IN_THE_PROPHECY_OF_DARKNESS_THE_ERA_OF_CHAOS_HAS_BEGUN = new NpcStringId(1000422);
		THE_PROPHECY_OF_DARKNESS_HAS_COME_TO_PASS = new NpcStringId(1000423);
		S1_I_GIVE_YOU_THE_BLESSING_OF_PROPHECY = new NpcStringId(1000424);
		S1_I_BESTOW_UPON_YOU_THE_AUTHORITY_OF_THE_ABYSS = new NpcStringId(1000425);
		HERALD_OF_THE_NEW_ERA_OPEN_YOUR_EYES = new NpcStringId(1000426);
		REMEMBER_KILL_THE_WEAKLINGS_FIRST = new NpcStringId(1000427);
		PREPARE_TO_DIE_MAGGOT = new NpcStringId(1000428);
		THAT_WILL_DO_PREPARE_TO_BE_RELEASED = new NpcStringId(1000429);
		DRAW = new NpcStringId(1000430);
		RULERS_OF_THE_SEAL_I_BRING_YOU_WONDROUS_GIFTS = new NpcStringId(1000431);
		RULERS_OF_THE_SEAL_I_HAVE_SOME_EXCELLENT_WEAPONS_TO_SHOW_YOU = new NpcStringId(1000432);
		IVE_BEEN_SO_BUSY_LATELY_IN_ADDITION_TO_PLANNING_MY_TRIP = new NpcStringId(1000433);
		YOUR_TREATMENT_OF_WEAKLINGS_IS_UNFORGIVABLE = new NpcStringId(1000434);
		IM_HERE_TO_HELP_YOU_HI_YAH = new NpcStringId(1000435);
		JUSTICE_WILL_BE_SERVED = new NpcStringId(1000436);
		ON_TO_IMMORTAL_GLORY = new NpcStringId(1000437);
		JUSTICE_ALWAYS_AVENGES_THE_POWERLESS = new NpcStringId(1000438);
		ARE_YOU_HURT_HANG_IN_THERE_WEVE_ALMOST_GOT_THEM = new NpcStringId(1000439);
		WHY_SHOULD_I_TELL_YOU_MY_NAME_YOU_CREEP = new NpcStringId(1000440);
		N1_MINUTE = new NpcStringId(1000441);
		THE_DEFENDERS_OF_S1_CASTLE_WILL_BE_TELEPORTED_TO_THE_INNER_CASTLE = new NpcStringId(1000443);
		SUNDAY = new NpcStringId(1000444);
		MONDAY = new NpcStringId(1000445);
		TUESDAY = new NpcStringId(1000446);
		WEDNESDAY = new NpcStringId(1000447);
		THURSDAY = new NpcStringId(1000448);
		FRIDAY = new NpcStringId(1000449);
		SATURDAY = new NpcStringId(1000450);
		HOUR = new NpcStringId(1000451);
		ITS_A_GOOD_DAY_TO_DIE_WELCOME_TO_HELL_MAGGOT = new NpcStringId(1000452);
		THE_FESTIVAL_OF_DARKNESS_WILL_END_IN_TWO_MINUTES = new NpcStringId(1000453);
		NOBLESSE_GATE_PASS = new NpcStringId(1000454);
		MINUTES_HAVE_PASSED = new NpcStringId(1000456);
		GAME_OVER_THE_TELEPORT_WILL_APPEAR_MOMENTARILY = new NpcStringId(1000457);
		YOU_WHO_ARE_LIKE_THE_SLUGS_CRAWLING_ON_THE_GROUND_THE_GENEROSITY_AND_GREATNESS_OF_ME_DAIMON_THE_WHITE_EYED_IS_ENDLESS_HA_HA_HA =
				new NpcStringId(1000458);
		IF_YOU_WANT_TO_BE_THE_OPPONENT_OF_ME_DAIMON_THE_WHITE_EYED_YOU_SHOULD_AT_LEAST_HAVE_THE_BASIC_SKILLS =
				new NpcStringId(1000459);
		YOU_STUPID_CREATURES_THAT_ARE_BOUND_TO_THE_EARTH_YOU_ARE_SUFFERING_TOO_MUCH_WHILE_DRAGGING_YOUR_FAT_HEAVY_BODIES_I_DAIMON_WILL_LIGHTEN_YOUR_BURDEN =
				new NpcStringId(1000460);
		A_WEAK_AND_STUPID_TRIBE_LIKE_YOURS_DOESNT_DESERVE_TO_BE_MY_ENEMY_BWA_HA_HA_HA = new NpcStringId(1000461);
		YOU_DARE_TO_INVADE_THE_TERRITORY_OF_DAIMON_THE_WHITE_EYED_NOW_YOU_WILL_PAY_THE_PRICE_FOR_YOUR_ACTION =
				new NpcStringId(1000462);
		THIS_IS_THE_GRACE_OF_DAIMON_THE_WHITE_EYED_THE_GREAT_MONSTER_EYE_LORD_HA_HA_HA = new NpcStringId(1000463);
		S1_YOU_HAVE_BECOME_A_HERO_OF_DUELISTS_CONGRATULATIONS = new NpcStringId(1000464);
		S1_YOU_HAVE_BECOME_A_HERO_OF_DREADNOUGHTS_CONGRATULATIONS = new NpcStringId(1000465);
		S1_YOU_HAVE_BECOME_A_HERO_OF_PHOENIX_KNIGHTS_CONGRATULATIONS = new NpcStringId(1000466);
		S1_YOU_HAVE_BECOME_A_HERO_OF_HELL_KNIGHTS_CONGRATULATIONS = new NpcStringId(1000467);
		S1_YOU_HAVE_BECOME_A_SAGITTARIUS_HERO_CONGRATULATIONS = new NpcStringId(1000468);
		S1_YOU_HAVE_BECOME_A_HERO_OF_ADVENTURERS_CONGRATULATIONS = new NpcStringId(1000469);
		S1_YOU_HAVE_BECOME_A_HERO_OF_ARCHMAGES_CONGRATULATIONS = new NpcStringId(1000470);
		S1_YOU_HAVE_BECOME_A_HERO_OF_SOULTAKERS_CONGRATULATIONS = new NpcStringId(1000471);
		S1_YOU_HAVE_BECOME_A_HERO_OF_ARCANA_LORDS_CONGRATULATIONS = new NpcStringId(1000472);
		S1_YOU_HAVE_BECOME_A_HERO_OF_CARDINALS_CONGRATULATIONS = new NpcStringId(1000473);
		S1_YOU_HAVE_BECOME_A_HERO_OF_HIEROPHANTS_CONGRATULATIONS = new NpcStringId(1000474);
		S1_YOU_HAVE_BECOME_A_HERO_OF_EVAS_TEMPLARS_CONGRATULATIONS = new NpcStringId(1000475);
		S1_YOU_HAVE_BECOME_A_HERO_OF_SWORD_MUSES_CONGRATULATIONS = new NpcStringId(1000476);
		S1_YOU_HAVE_BECOME_A_HERO_OF_WIND_RIDERS_CONGRATULATIONS = new NpcStringId(1000477);
		S1_YOU_HAVE_BECOME_A_HERO_OF_MOONLIGHT_SENTINELS_CONGRATULATIONS = new NpcStringId(1000478);
		S1_YOU_HAVE_BECOME_A_HERO_OF_MYSTIC_MUSES_CONGRATULATIONS = new NpcStringId(1000479);
		S1_YOU_HAVE_BECOME_A_HERO_OF_ELEMENTAL_MASTERS_CONGRATULATIONS = new NpcStringId(1000480);
		S1_YOU_HAVE_BECOME_A_HERO_OF_EVAS_SAINTS_CONGRATULATIONS = new NpcStringId(1000481);
		S1_YOU_HAVE_BECOME_A_HERO_OF_THE_SHILLIEN_TEMPLARS_CONGRATULATIONS = new NpcStringId(1000482);
		S1_YOU_HAVE_BECOME_A_HERO_OF_SPECTRAL_DANCERS_CONGRATULATIONS = new NpcStringId(1000483);
		S1_YOU_HAVE_BECOME_A_HERO_OF_GHOST_HUNTERS_CONGRATULATIONS = new NpcStringId(1000484);
		S1_YOU_HAVE_BECOME_A_HERO_OF_GHOST_SENTINELS_CONGRATULATIONS = new NpcStringId(1000485);
		S1_YOU_HAVE_BECOME_A_HERO_OF_STORM_SCREAMERS_CONGRATULATIONS = new NpcStringId(1000486);
		S1_YOU_HAVE_BECOME_A_HERO_OF_SPECTRAL_MASTERS_CONGRATULATIONS = new NpcStringId(1000487);
		S1_YOU_HAVE_BECOME_A_HERO_OF_THE_SHILLIEN_SAINTS_CONGRATULATIONS = new NpcStringId(1000488);
		S1_YOU_HAVE_BECOME_A_HERO_OF_TITANS_CONGRATULATIONS = new NpcStringId(1000489);
		S1_YOU_HAVE_BECOME_A_HERO_OF_GRAND_KHAVATARIS_CONGRATULATIONS = new NpcStringId(1000490);
		S1_YOU_HAVE_BECOME_A_HERO_OF_DOMINATORS_CONGRATULATIONS = new NpcStringId(1000491);
		S1_YOU_HAVE_BECOME_A_HERO_OF_DOOMCRYERS_CONGRATULATIONS = new NpcStringId(1000492);
		S1_YOU_HAVE_BECOME_A_HERO_OF_FORTUNE_SEEKERS_CONGRATULATIONS = new NpcStringId(1000493);
		S1_YOU_HAVE_BECOME_A_HERO_OF_MAESTROS_CONGRATULATIONS = new NpcStringId(1000494);
		UNREGISTERED = new NpcStringId(1000495);
		S1_YOU_HAVE_BECOME_A_HERO_OF_DOOMBRINGERS_CONGRATULATIONS = new NpcStringId(1000496);
		S1_YOU_HAVE_BECOME_A_HERO_OF_SOUL_HOUNDS_CONGRATULATIONS = new NpcStringId(1000497);
		S1_YOU_HAVE_BECOME_A_HERO_OF_TRICKSTERS_CONGRATULATIONS = new NpcStringId(1000499);
		YOU_MAY_NOW_ENTER_THE_SEPULCHER = new NpcStringId(1000500);
		IF_YOU_PLACE_YOUR_HAND_ON_THE_STONE_STATUE_IN_FRONT_OF_EACH_SEPULCHER_YOU_WILL_BE_ABLE_TO_ENTER =
				new NpcStringId(1000501);
		THE_MONSTERS_HAVE_SPAWNED = new NpcStringId(1000502);
		THANK_YOU_FOR_SAVING_ME = new NpcStringId(1000503);
		FEWER_THAN_S2 = new NpcStringId(1000504);
		MORE_THAN_S2 = new NpcStringId(1000505);
		POINT = new NpcStringId(1000506);
		COMPETITION = new NpcStringId(1000507);
		SEAL_VALIDATION = new NpcStringId(1000508);
		PREPARATION = new NpcStringId(1000509);
		NO_OWNER = new NpcStringId(1000512);
		THIS_PLACE_IS_DANGEROUS_S1_PLEASE_TURN_BACK = new NpcStringId(1000513);
		WHO_DISTURBS_MY_SACRED_SLEEP = new NpcStringId(1000514);
		BEGONE_THIEF_LET_OUR_BONES_REST_IN_PEACE = new NpcStringId(1000515);
		_LEAVE_US_BE_HESTUI_SCUM = new NpcStringId(1000516);
		THIEVING_KAKAI_MAY_BLOODBUGS_GNAW_YOU_IN_YOUR_SLEEP = new NpcStringId(1000517);
		NEWBIE_TRAVEL_TOKEN = new NpcStringId(1000518);
		ARROGANT_FOOL_YOU_DARE_TO_CHALLENGE_ME_THE_RULER_OF_FLAMES_HERE_IS_YOUR_REWARD = new NpcStringId(1000519);
		S1_YOU_CANNOT_HOPE_TO_DEFEAT_ME_WITH_YOUR_MEAGER_STRENGTH = new NpcStringId(1000520);
		NOT_EVEN_THE_GODS_THEMSELVES_COULD_TOUCH_ME_BUT_YOU_S1_YOU_DARE_CHALLENGE_ME_IGNORANT_MORTAL =
				new NpcStringId(1000521);
		REQUIEM_OF_HATRED = new NpcStringId(1000522);
		FUGUE_OF_JUBILATION = new NpcStringId(1000523);
		FRENETIC_TOCCATA = new NpcStringId(1000524);
		HYPNOTIC_MAZURKA = new NpcStringId(1000525);
		MOURNFUL_CHORALE_PRELUDE = new NpcStringId(1000526);
		RONDO_OF_SOLITUDE = new NpcStringId(1000527);
		OLYMPIAD_TOKEN = new NpcStringId(1000528);
		THE_KINGDOM_OF_ADEN = new NpcStringId(1001000);
		THE_KINGDOM_OF_ELMORE = new NpcStringId(1001100);
		TALKING_ISLAND_VILLAGE = new NpcStringId(1010001);
		THE_ELVEN_VILLAGE = new NpcStringId(1010002);
		THE_DARK_ELF_VILLAGE = new NpcStringId(1010003);
		THE_VILLAGE_OF_GLUDIN = new NpcStringId(1010004);
		THE_TOWN_OF_GLUDIO = new NpcStringId(1010005);
		THE_TOWN_OF_DION = new NpcStringId(1010006);
		THE_TOWN_OF_GIRAN = new NpcStringId(1010007);
		ORC_VILLAGE = new NpcStringId(1010008);
		DWARVEN_VILLAGE = new NpcStringId(1010009);
		THE_SOUTHERN_PART_OF_THE_DARK_FOREST = new NpcStringId(1010010);
		THE_NORTHEAST_COAST = new NpcStringId(1010011);
		THE_SOUTHERN_ENTRANCE_TO_THE_WASTELANDSS = new NpcStringId(1010012);
		TOWN_OF_OREN = new NpcStringId(1010013);
		IVORY_TOWER = new NpcStringId(1010014);
		N1ST_FLOOR_LOBBY = new NpcStringId(1010015);
		UNDERGROUND_SHOPPING_AREA = new NpcStringId(1010016);
		N2ND_FLOOR_HUMAN_WIZARD_GUILD = new NpcStringId(1010017);
		N3RD_FLOOR_ELVEN_WIZARD_GUILD = new NpcStringId(1010018);
		N4TH_FLOOR_DARK_WIZARD_GUILD = new NpcStringId(1010019);
		HUNTERS_VILLAGE = new NpcStringId(1010020);
		GIRAN_HARBOR = new NpcStringId(1010021);
		HARDINS_PRIVATE_ACADEMY = new NpcStringId(1010022);
		TOWN_OF_ADEN = new NpcStringId(1010023);
		VILLAGE_SQUARE = new NpcStringId(1010024);
		NORTH_GATE_ENTRANCE = new NpcStringId(1010025);
		EAST_GATE_ENTRANCE = new NpcStringId(1010026);
		WEST_GATE_ENTRANCE = new NpcStringId(1010027);
		SOUTH_GATE_ENTRANCE = new NpcStringId(1010028);
		ENTRANCE_TO_TUREK_ORC_CAMP = new NpcStringId(1010029);
		ENTRANCE_TO_FORGOTTEN_TEMPLE = new NpcStringId(1010030);
		ENTRANCE_TO_THE_WASTELANDS = new NpcStringId(1010031);
		ENTRANCE_TO_ABANDONED_CAMP = new NpcStringId(1010032);
		ENTRANCE_TO_CRUMA_MARSHLANDS = new NpcStringId(1010033);
		ENTRANCE_TO_EXECUTION_GROUNDS = new NpcStringId(1010034);
		ENTRANCE_TO_THE_FORTRESS_OF_RESISTANCE = new NpcStringId(1010035);
		ENTRANCE_TO_FLORAN_VILLAGE = new NpcStringId(1010036);
		NEUTRAL_ZONE = new NpcStringId(1010037);
		WESTERN_ROAD_OF_GIRAN = new NpcStringId(1010038);
		EASTERN_ROAD_OF_GLUDIN_VILLAGE = new NpcStringId(1010039);
		ENTRANCE_TO_CRUMA_TOWER = new NpcStringId(1010041);
		DEATH_PASS = new NpcStringId(1010042);
		NORTHERN_PART_OF_THE_MARSHLANDS = new NpcStringId(1010043);
		NORTHEAST_OF_THE_NEUTRAL_ZONE = new NpcStringId(1010044);
		IMMORTAL_PLATEAU_CENTRAL_REGION = new NpcStringId(1010045);
		IMMORTAL_PLATEAU_SOUTHERN_REGION = new NpcStringId(1010046);
		IMMORTAL_PLATEAU_SOUTHEAST_REGION = new NpcStringId(1010047);
		FROZEN_WATERFALL = new NpcStringId(1010048);
		HEINE = new NpcStringId(1010049);
		TOWER_OF_INSOLENCE_1ST_FLOOR = new NpcStringId(1010050);
		TOWER_OF_INSOLENCE_5TH_FLOOR = new NpcStringId(1010051);
		TOWER_OF_INSOLENCE_10TH_FLOOR = new NpcStringId(1010052);
		COLISEUM = new NpcStringId(1010053);
		MONSTER_DERBY = new NpcStringId(1010054);
		NEAR_THE_FRONTIER_POST = new NpcStringId(1010055);
		ENTRANCE_TO_THE_SEA_OF_SPORES = new NpcStringId(1010056);
		AN_OLD_BATTLEFIELD = new NpcStringId(1010057);
		ENTRANCE_TO_ENCHANTED_VALLEY = new NpcStringId(1010058);
		ENTRANCE_TO_THE_TOWER_OF_INSOLENCE = new NpcStringId(1010059);
		BLAZING_SWAMP = new NpcStringId(1010060);
		ENTRANCE_TO_THE_CEMETERY = new NpcStringId(1010061);
		ENTRANCE_TO_THE_GIANTS_CAVE = new NpcStringId(1010062);
		ENTRANCE_TO_THE_FOREST_OF_MIRRORS = new NpcStringId(1010063);
		THE_FORBIDDEN_GATEWAY = new NpcStringId(1010064);
		ENTRANCE_TO_THE_TANOR_SILENOS_BARRACKS = new NpcStringId(1010066);
		DRAGON_VALLEY = new NpcStringId(1010067);
		ORACLE_OF_DAWN = new NpcStringId(1010068);
		ORACLE_OF_DUSK = new NpcStringId(1010069);
		NECROPOLIS_OF_SACRIFICE = new NpcStringId(1010070);
		THE_PILGRIMS_NECROPOLIS = new NpcStringId(1010071);
		NECROPOLIS_OF_WORSHIP = new NpcStringId(1010072);
		THE_PATRIOTS_NECROPOLIS = new NpcStringId(1010073);
		NECROPOLIS_OF_DEVOTION = new NpcStringId(1010074);
		NECROPOLIS_OF_MARTYRDOM = new NpcStringId(1010075);
		THE_DISCIPLES_NECROPOLIS = new NpcStringId(1010076);
		THE_SAINTS_NECROPOLIS = new NpcStringId(1010077);
		THE_CATACOMB_OF_THE_HERETIC = new NpcStringId(1010078);
		CATACOMB_OF_THE_BRANDED = new NpcStringId(1010079);
		CATACOMB_OF_THE_APOSTATE = new NpcStringId(1010080);
		CATACOMB_OF_THE_WITCH = new NpcStringId(1010081);
		CATACOMB_OF_DARK_OMENS = new NpcStringId(1010082);
		CATACOMB_OF_THE_FORBIDDEN_PATH = new NpcStringId(1010083);
		ENTRANCE_TO_THE_RUINS_OF_AGONY = new NpcStringId(1010084);
		ENTRANCE_TO_THE_RUINS_OF_DESPAIR = new NpcStringId(1010085);
		ENTRANCE_TO_THE_ANT_NEST = new NpcStringId(1010086);
		SOUTHERN_DION = new NpcStringId(1010087);
		ENTRANCE_TO_DRAGON_VALLEY = new NpcStringId(1010088);
		FIELD_OF_SILENCE = new NpcStringId(1010089);
		FIELD_OF_WHISPERS = new NpcStringId(1010090);
		ENTRANCE_TO_ALLIGATOR_ISLAND = new NpcStringId(1010091);
		SOUTHERN_PLAIN_OF_OREN = new NpcStringId(1010092);
		ENTRANCE_TO_THE_BANDIT_STRONGHOLD = new NpcStringId(1010093);
		WINDY_HILL = new NpcStringId(1010094);
		ORC_BARRACKS = new NpcStringId(1010095);
		FELLMERE_HARVESTING_GROUNDS = new NpcStringId(1010096);
		RUINS_OF_AGONY = new NpcStringId(1010097);
		ABANDONED_CAMP = new NpcStringId(1010098);
		RED_ROCK_RIDGE = new NpcStringId(1010099);
		LANGK_LIZARDMAN_DWELLINGS = new NpcStringId(1010100);
		RUINS_OF_DESPAIR = new NpcStringId(1010101);
		WINDAWOOD_MANOR = new NpcStringId(1010102);
		NORTHERN_PATHWAY_TO_THE_WASTELANDS = new NpcStringId(1010103);
		WESTERN_PATHWAY_TO_THE_WASTELANDS = new NpcStringId(1010104);
		SOUTHERN_PATHWAY_TO_THE_WASTELANDS = new NpcStringId(1010105);
		FORGOTTEN_TEMPLE = new NpcStringId(1010106);
		SOUTH_ENTRANCE_OF_ANT_NEST = new NpcStringId(1010107);
		EAST_ENTRANCE_OF_ANT_NEST = new NpcStringId(1010108);
		WEST_ENTRANCE_OF_ANT_NEST = new NpcStringId(1010109);
		CRUMA_MARSHLAND = new NpcStringId(1010110);
		PLAINS_OF_DION = new NpcStringId(1010111);
		BEE_HIVE = new NpcStringId(1010112);
		FORTRESS_OF_RESISTANCE = new NpcStringId(1010113);
		EXECUTION_GROUNDS = new NpcStringId(1010114);
		TANOR_CANYON = new NpcStringId(1010115);
		CRUMA_TOWER = new NpcStringId(1010116);
		THREE_WAY_CROSSROADS_AT_DRAGON_VALLEY = new NpcStringId(1010117);
		BREKAS_STRONGHOLD = new NpcStringId(1010118);
		GORGON_FLOWER_GARDEN = new NpcStringId(1010119);
		ANTHARASS_LAIR = new NpcStringId(1010120);
		SEA_OF_SPORES = new NpcStringId(1010121);
		OUTLAW_FOREST = new NpcStringId(1010122);
		FOREST_OF_EVIL_AND_THE_IVORY_TOWER = new NpcStringId(1010123);
		TIMAK_OUTPOST = new NpcStringId(1010124);
		GREAT_PLAINS_OF_OREN = new NpcStringId(1010125);
		ALCHEMISTS_HUT = new NpcStringId(1010126);
		ANCIENT_BATTLEGROUND = new NpcStringId(1010127);
		NORTHERN_PATHWAY_OF_THE_ENCHANTED_VALLEY = new NpcStringId(1010128);
		SOUTHERN_PATHWAY_OF_THE_ENCHANTED_VALLEY = new NpcStringId(1010129);
		HUNTERS_VALLEY = new NpcStringId(1010130);
		WESTERN_ENTRANCE_OF_BLAZING_SWAMP = new NpcStringId(1010131);
		EASTERN_ENTRANCE_OF_BLAZING_SWAMP = new NpcStringId(1010132);
		PLAINS_OF_GLORY = new NpcStringId(1010133);
		WAR_TORN_PLAINS = new NpcStringId(1010134);
		NORTHWESTERN_PASSAGE_TO_THE_FOREST_OF_MIRRORS = new NpcStringId(1010135);
		THE_FRONT_OF_ANGHEL_WATERFALL = new NpcStringId(1010136);
		SOUTH_ENTRANCE_OF_DEVASTATED_CASTLE = new NpcStringId(1010137);
		NORTH_ENTRANCE_OF_DEVASTATED_CASTLE = new NpcStringId(1010138);
		NORTH_ENTRANCE_OF_THE_CEMETERY = new NpcStringId(1010139);
		SOUTH_ENTRANCE_OF_THE_CEMETERY = new NpcStringId(1010140);
		WEST_ENTRANCE_OF_THE_CEMETERY = new NpcStringId(1010141);
		ENTRANCE_OF_THE_FORBIDDEN_GATEWAY = new NpcStringId(1010142);
		FORSAKEN_PLAINS = new NpcStringId(1010143);
		TOWER_OF_INSOLENCE = new NpcStringId(1010144);
		THE_GIANTS_CAVE = new NpcStringId(1010145);
		NORTHERN_PART_OF_THE_FIELD_OF_SILENCE = new NpcStringId(1010146);
		WESTERN_PART_OF_THE_FIELD_OF_SILENCE = new NpcStringId(1010147);
		EASTERN_PART_OF_THE_FIELD_OF_SILENCE = new NpcStringId(1010148);
		WESTERN_PART_OF_THE_FIELD_OF_WHISPERS = new NpcStringId(1010149);
		ALLIGATOR_ISLAND = new NpcStringId(1010150);
		ALLIGATOR_BEACH = new NpcStringId(1010151);
		DEVILS_ISLE = new NpcStringId(1010152);
		GARDEN_OF_EVA = new NpcStringId(1010153);
		TALKING_ISLAND = new NpcStringId(1010154);
		ELVEN_VILLAGE = new NpcStringId(1010155);
		DARK_ELF_VILLAGE = new NpcStringId(1010156);
		SCENIC_DECK_OF_IRIS_LAKE = new NpcStringId(1010159);
		ALTAR_OF_RITES = new NpcStringId(1010160);
		DARK_FOREST_WATERFALL = new NpcStringId(1010161);
		THREE_WAY_CROSSROADS_OF_THE_NEUTRAL_ZONE = new NpcStringId(1010162);
		DARK_FOREST = new NpcStringId(1010163);
		SWAMPLAND = new NpcStringId(1010164);
		BLACK_ROCK_HILL = new NpcStringId(1010165);
		SPIDER_NEST = new NpcStringId(1010166);
		ELVEN_FOREST = new NpcStringId(1010167);
		OBELISK_OF_VICTORY = new NpcStringId(1010168);
		NORTHERN_TERRITORY_OF_TALKING_ISLAND = new NpcStringId(1010169);
		SOUTHERN_TERRITORY_OF_TALKING_ISLAND = new NpcStringId(1010170);
		EVIL_HUNTING_GROUNDS = new NpcStringId(1010171);
		MAILLE_LIZARDMEN_BARRACKS = new NpcStringId(1010172);
		RUINS_OF_AGONY_BEND = new NpcStringId(1010173);
		THE_ENTRANCE_TO_THE_RUINS_OF_DESPAIR = new NpcStringId(1010174);
		WINDMILL_HILL = new NpcStringId(1010175);
		FLORAN_AGRICULTURAL_AREA = new NpcStringId(1010177);
		WESTERN_TANOR_CANYON = new NpcStringId(1010178);
		PLAINS_OF_THE_LIZARDMEN = new NpcStringId(1010179);
		FOREST_OF_EVIL = new NpcStringId(1010180);
		FIELDS_OF_MASSACRE = new NpcStringId(1010181);
		SILENT_VALLEY = new NpcStringId(1010182);
		NORTHERN_AREA_OF_THE_IMMORTAL_PLATEAU_NORTHERN_REGION = new NpcStringId(1010183);
		SOUTHERN_AREA_OF_THE_IMMORTAL_PLATEAU_NORTHERN_REGION = new NpcStringId(1010184);
		NORTHERN_AREA_OF_THE_IMMORTAL_PLATEAU_SOUTHERN_REGION = new NpcStringId(1010185);
		SOUTHERN_AREA_OF_THE_IMMORTAL_PLATEAU_SOUTHERN_REGION = new NpcStringId(1010186);
		WESTERN_MINING_ZONE = new NpcStringId(1010187);
		ENTRANCE_TO_THE_ABANDONED_COAL_MINES = new NpcStringId(1010190);
		ENTRANCE_TO_THE_MITHRIL_MINES = new NpcStringId(1010191);
		WEST_AREA_OF_THE_DEVASTATED_CASTLE = new NpcStringId(1010192);
		TOWER_OF_INSOLENCE_3RD_FLOOR = new NpcStringId(1010193);
		TOWER_OF_INSOLENCE_7TH_FLOOR = new NpcStringId(1010195);
		TOWER_OF_INSOLENCE_13TH_FLOOR = new NpcStringId(1010197);
		TOWN_OF_GODDARD = new NpcStringId(1010199);
		RUNE_TOWNSHIP = new NpcStringId(1010200);
		A_DELIVERY_FOR_MR_LECTOR_VERY_GOOD = new NpcStringId(1010201);
		I_NEED_A_BREAK = new NpcStringId(1010202);
		HELLO_MR_LECTOR_LONG_TIME_NO_SEE_MR_JACKSON = new NpcStringId(1010203);
		LULU = new NpcStringId(1010204);
		WHERE_HAS_HE_GONE = new NpcStringId(1010205);
		HAVE_YOU_SEEN_WINDAWOOD = new NpcStringId(1010206);
		WHERE_DID_HE_GO = new NpcStringId(1010207);
		THE_MOTHER_TREE_IS_SLOWLY_DYING = new NpcStringId(1010208);
		HOW_CAN_WE_SAVE_THE_MOTHER_TREE = new NpcStringId(1010209);
		THE_MOTHER_TREE_IS_ALWAYS_SO_GORGEOUS = new NpcStringId(1010210);
		LADY_MIRABEL_MAY_THE_PEACE_OF_THE_LAKE_BE_WITH_YOU = new NpcStringId(1010211);
		YOURE_A_HARD_WORKER_RAYLA = new NpcStringId(1010212);
		YOURE_A_HARD_WORKER = new NpcStringId(1010213);
		THE_MASS_OF_DARKNESS_WILL_START_IN_A_COUPLE_OF_DAYS_PAY_MORE_ATTENTION_TO_THE_GUARD = new NpcStringId(1010214);
		HAVE_YOU_SEEN_TOROCCO_TODAY = new NpcStringId(1010215);
		HAVE_YOU_SEEN_TOROCCO = new NpcStringId(1010216);
		WHERE_IS_THAT_FOOL_HIDING = new NpcStringId(1010217);
		CARE_TO_GO_A_ROUND = new NpcStringId(1010218);
		HAVE_A_NICE_DAY_MR_GARITA_AND_MION = new NpcStringId(1010219);
		MR_LID_MURDOC_AND_AIRY_HOW_ARE_YOU_DOING = new NpcStringId(1010220);
		A_BLACK_MOON_NOW_DO_YOU_UNDERSTAND_THAT_HE_HAS_OPENED_HIS_EYES = new NpcStringId(1010221);
		CLOUDS_OF_BLOOD_ARE_GATHERING_SOON_IT_WILL_START_TO_RAIN_THE_RAIN_OF_CRIMSON_BLOOD = new NpcStringId(1010222);
		WHILE_THE_FOOLISH_LIGHT_WAS_ASLEEP_THE_DARKNESS_WILL_AWAKEN_FIRST_UH_HUH_HUH = new NpcStringId(1010223);
		IT_IS_THE_DEEPEST_DARKNESS_WITH_ITS_ARRIVAL_THE_WORLD_WILL_SOON_DIE = new NpcStringId(1010224);
		DEATH_IS_JUST_A_NEW_BEGINNING_HUHU_FEAR_NOT = new NpcStringId(1010225);
		AHH_BEAUTIFUL_GODDESS_OF_DEATH_COVER_OVER_THE_FILTH_OF_THIS_WORLD_WITH_YOUR_DARKNESS = new NpcStringId(1010226);
		THE_GODDESSS_RESURRECTION_HAS_ALREADY_BEGUN_HUHU_INSIGNIFICANT_CREATURES_LIKE_YOU_CAN_DO_NOTHING =
				new NpcStringId(1010227);
		CROAK_CROAK_FOOD_LIKE_S1_IN_THIS_PLACE = new NpcStringId(1010400);
		S1_HOW_LUCKY_I_AM = new NpcStringId(1010401);
		PRAY_THAT_YOU_CAUGHT_A_WRONG_FISH_S1 = new NpcStringId(1010402);
		DO_YOU_KNOW_WHAT_A_FROG_TASTES_LIKE = new NpcStringId(1010403);
		I_WILL_SHOW_YOU_THE_POWER_OF_A_FROG = new NpcStringId(1010404);
		I_WILL_SWALLOW_AT_A_MOUTHFUL = new NpcStringId(1010405);
		UGH_NO_CHANCE_HOW_COULD_THIS_ELDER_PASS_AWAY_LIKE_THIS = new NpcStringId(1010406);
		CROAK_CROAK_A_FROG_IS_DYING = new NpcStringId(1010407);
		A_FROG_TASTES_BAD_YUCK = new NpcStringId(1010408);
		KAAK_S1_WHAT_ARE_YOU_DOING_NOW = new NpcStringId(1010409);
		HUH_S1_YOU_PIERCED_INTO_THE_BODY_OF_THE_SPIRIT_WITH_A_NEEDLE_ARE_YOU_READY = new NpcStringId(1010410);
		OOH_S1_THATS_YOU_BUT_NO_LADY_IS_PLEASED_WITH_THIS_SAVAGE_INVITATION = new NpcStringId(1010411);
		YOU_MADE_ME_ANGRY = new NpcStringId(1010412);
		IT_IS_BUT_A_SCRATCH_IS_THAT_ALL_YOU_CAN_DO = new NpcStringId(1010413);
		FEEL_MY_PAIN = new NpcStringId(1010414);
		ILL_GET_YOU_FOR_THIS = new NpcStringId(1010415);
		I_WILL_TELL_FISH_NOT_TO_TAKE_YOUR_BAIT = new NpcStringId(1010416);
		YOU_BOTHERED_SUCH_A_WEAK_SPIRITHUH_HUH = new NpcStringId(1010417);
		KE_KE_KES1IM_EATINGKE = new NpcStringId(1010418);
		KUHOOHS1ENMITYFISH = new NpcStringId(1010419);
		S1_HUH_HUHHUH = new NpcStringId(1010420);
		KE_KE_KE_RAKUL_SPIN_EH_EH_EH = new NpcStringId(1010421);
		AH_FAFURION_AH_AH = new NpcStringId(1010422);
		RAKUL_RAKUL_RA_KUL = new NpcStringId(1010423);
		EHENMITYFISH = new NpcStringId(1010424);
		I_WONT_BE_EATEN_UPKAH_AH_AH = new NpcStringId(1010425);
		COUGH_RAKUL_COUGH_COUGH_KEH = new NpcStringId(1010426);
		GLORY_TO_FAFURION_DEATH_TO_S1 = new NpcStringId(1010427);
		S1_YOU_ARE_THE_ONE_WHO_BOTHERED_MY_POOR_FISH = new NpcStringId(1010428);
		FAFURION_A_CURSE_UPON_S1 = new NpcStringId(1010429);
		GIANT_SPECIAL_ATTACK = new NpcStringId(1010430);
		KNOW_THE_ENMITY_OF_FISH = new NpcStringId(1010431);
		I_WILL_SHOW_YOU_THE_POWER_OF_A_SPEAR = new NpcStringId(1010432);
		GLORY_TO_FAFURION = new NpcStringId(1010433);
		YIPES = new NpcStringId(1010434);
		AN_OLD_SOLDIER_DOES_NOT_DIE_BUT_JUST_DISAPPEAR = new NpcStringId(1010435);
		S1_TAKE_MY_CHALLENGE_THE_KNIGHT_OF_WATER = new NpcStringId(1010436);
		DISCOVER_S1_IN_THE_TREASURE_CHEST_OF_FISH = new NpcStringId(1010437);
		S1_I_TOOK_YOUR_BAIT = new NpcStringId(1010438);
		I_WILL_SHOW_YOU_SPEARMANSHIP_USED_IN_DRAGON_KINGS_PALACE = new NpcStringId(1010439);
		THIS_IS_THE_LAST_GIFT_I_GIVE_YOU = new NpcStringId(1010440);
		YOUR_BAIT_WAS_TOO_DELICIOUS_NOW_I_WILL_KILL_YOU = new NpcStringId(1010441);
		WHAT_A_REGRET_THE_ENMITY_OF_MY_BRETHREN = new NpcStringId(1010442);
		ILL_PAY_YOU_BACK_SOMEBODY_WILL_HAVE_MY_REVENGE = new NpcStringId(1010443);
		COUGH_BUT_I_WONT_BE_EATEN_UP_BY_YOU = new NpcStringId(1010444);
		_S1_I_WILL_KILL_YOU = new NpcStringId(1010445);
		S1_HOW_COULD_YOU_CATCH_ME_FROM_THE_DEEP_SEA = new NpcStringId(1010446);
		S1_DO_YOU_THINK_I_AM_A_FISH = new NpcStringId(1010447);
		EBIBIBI = new NpcStringId(1010448);
		HE_HE_HE_DO_YOU_WANT_ME_TO_ROAST_YOU_WELL = new NpcStringId(1010449);
		YOU_DIDNT_KEEP_YOUR_EYES_ON_ME_BECAUSE_I_COME_FROM_THE_SEA = new NpcStringId(1010450);
		EEEK_I_FEEL_SICKYOW = new NpcStringId(1010451);
		I_HAVE_FAILED = new NpcStringId(1010452);
		ACTIVITY_OF_LIFE_IS_STOPPED_CHIZIFC = new NpcStringId(1010453);
		GROWLING_S1_GROWLING = new NpcStringId(1010454);
		I_CAN_SMELL_S1 = new NpcStringId(1010455);
		LOOKS_DELICIOUS_S1 = new NpcStringId(1010456);
		I_WILL_CATCH_YOU = new NpcStringId(1010457);
		UAH_AH_AH_I_COULDNT_EAT_ANYTHING_FOR_A_LONG_TIME = new NpcStringId(1010458);
		I_CAN_SWALLOW_YOU_AT_A_MOUTHFUL = new NpcStringId(1010459);
		WHAT_I_AM_DEFEATED_BY_THE_PREY = new NpcStringId(1010460);
		YOU_ARE_MY_FOOD_I_HAVE_TO_KILL_YOU = new NpcStringId(1010461);
		I_CANT_BELIEVE_I_AM_EATEN_UP_BY_MY_PREY_GAH = new NpcStringId(1010462);
		YOU_CAUGHT_ME_S1 = new NpcStringId(1010463);
		YOURE_LUCKY_TO_HAVE_EVEN_SEEN_ME_S1 = new NpcStringId(1010464);
		S1_YOU_CANT_LEAVE_HERE_ALIVE_GIVE_UP = new NpcStringId(1010465);
		I_WILL_SHOW_YOU_THE_POWER_OF_THE_DEEP_SEA = new NpcStringId(1010466);
		I_WILL_BREAK_THE_FISHING_POLE = new NpcStringId(1010467);
		YOUR_CORPSE_WILL_BE_GOOD_FOOD_FOR_ME = new NpcStringId(1010468);
		YOU_ARE_A_GOOD_FISHERMAN = new NpcStringId(1010469);
		ARENT_YOU_AFRAID_OF_FAFURION = new NpcStringId(1010470);
		YOU_ARE_EXCELLENT = new NpcStringId(1010471);
		THE_POISON_DEVICE_HAS_BEEN_ACTIVATED = new NpcStringId(1010472);
		THE_P_ATK_REDUCTION_DEVICE_WILL_BE_ACTIVATED_IN_1_MINUTE = new NpcStringId(1010473);
		THE_DEFENSE_REDUCTION_DEVICE_WILL_BE_ACTIVATED_IN_2_MINUTES = new NpcStringId(1010474);
		THE_HP_REGENERATION_REDUCTION_DEVICE_WILL_BE_ACTIVATED_IN_3_MINUTES = new NpcStringId(1010475);
		THE_P_ATK_REDUCTION_DEVICE_HAS_BEEN_ACTIVATED = new NpcStringId(1010476);
		THE_DEFENSE_REDUCTION_DEVICE_HAS_BEEN_ACTIVATED = new NpcStringId(1010477);
		THE_HP_REGENERATION_REDUCTION_DEVICE_HAS_BEEN_ACTIVATED = new NpcStringId(1010478);
		THE_POISON_DEVICE_HAS_NOW_BEEN_DESTROYED = new NpcStringId(1010479);
		THE_P_ATK_REDUCTION_DEVICE_HAS_NOW_BEEN_DESTROYED = new NpcStringId(1010480);
		THE_DEFENSE_REDUCTION_DEVICE_HAS_BEEN_DESTROYED = new NpcStringId(1010481);
		ENTRANCE_TO_THE_CAVE_OF_TRIALS = new NpcStringId(1010485);
		INSIDE_THE_ELVEN_RUINS = new NpcStringId(1010486);
		ENTRANCE_TO_THE_ELVEN_RUINS = new NpcStringId(1010487);
		ENTRANCE_TO_THE_SCHOOL_OF_DARK_ARTS = new NpcStringId(1010488);
		CENTER_OF_THE_SCHOOL_OF_DARK_ARTS = new NpcStringId(1010489);
		ENTRANCE_TO_THE_ELVEN_FORTRESS = new NpcStringId(1010490);
		VARKA_SILENOS_STRONGHOLD = new NpcStringId(1010491);
		KETRA_ORC_OUTPOST = new NpcStringId(1010492);
		RUNE_TOWNSHIP_GUILD = new NpcStringId(1010493);
		RUNE_TOWNSHIP_TEMPLE = new NpcStringId(1010494);
		RUNE_TOWNSHIP_STORE = new NpcStringId(1010495);
		ENTRANCE_TO_THE_FOREST_OF_THE_DEAD = new NpcStringId(1010496);
		WESTERN_ENTRANCE_TO_THE_SWAMP_OF_SCREAMS = new NpcStringId(1010497);
		ENTRANCE_TO_THE_FORGOTTEN_TEMPLE = new NpcStringId(1010498);
		CENTER_OF_THE_FORGOTTEN_TEMPLE = new NpcStringId(1010499);
		ENTRANCE_TO_THE_CRUMA_TOWER = new NpcStringId(1010500);
		CRUMA_TOWER_FIRST_FLOOR = new NpcStringId(1010501);
		CRUMA_TOWER_SECOND_FLOOR = new NpcStringId(1010502);
		CRUMA_TOWER_THIRD_FLOOR = new NpcStringId(1010503);
		ENTRANCE_TO_DEVILS_ISLE = new NpcStringId(1010504);
		GLUDIN_ARENA = new NpcStringId(1010506);
		GIRAN_ARENA = new NpcStringId(1010507);
		ENTRANCE_TO_ANTHARASS_LAIR = new NpcStringId(1010508);
		ANTHARASS_LAIR_1ST_LEVEL = new NpcStringId(1010509);
		ANTHARASS_LAIR_2ND_LEVEL = new NpcStringId(1010510);
		ANTHARASS_LAIR_MAGIC_FORCE_FIELD_BRIDGE = new NpcStringId(1010511);
		THE_HEART_OF_ANTHARASS_LAIR = new NpcStringId(1010512);
		EAST_OF_THE_FIELD_OF_SILENCE = new NpcStringId(1010513);
		WEST_OF_THE_FIELD_OF_SILENCE = new NpcStringId(1010514);
		EAST_OF_THE_FIELD_OF_WHISPERS = new NpcStringId(1010515);
		WEST_OF_THE_FIELD_OF_WHISPERS = new NpcStringId(1010516);
		ENTRANCE_TO_THE_GARDEN_OF_EVA = new NpcStringId(1010517);
		NORTHERN_PART_OF_ALLIGATOR_ISLAND = new NpcStringId(1010520);
		CENTRAL_PART_OF_ALLIGATOR_ISLAND = new NpcStringId(1010521);
		GARDEN_OF_EVA_2ND_LEVEL = new NpcStringId(1010522);
		GARDEN_OF_EVA_3RD_LEVEL = new NpcStringId(1010523);
		GARDEN_OF_EVA_4TH_LEVEL = new NpcStringId(1010524);
		GARDEN_OF_EVA_5TH_LEVEL = new NpcStringId(1010525);
		INSIDE_THE_GARDEN_OF_EVA = new NpcStringId(1010526);
		FOUR_SEPULCHERS = new NpcStringId(1010527);
		IMPERIAL_TOMB = new NpcStringId(1010528);
		SHRINE_OF_LOYALTY = new NpcStringId(1010529);
		ENTRANCE_TO_THE_FORGE_OF_THE_GODS = new NpcStringId(1010530);
		FORGE_OF_THE_GODS_TOP_LEVEL = new NpcStringId(1010531);
		FORGE_OF_THE_GODS_LOWER_LEVEL = new NpcStringId(1010532);
		ENTRANCE_TO_THE_WALL_OF_ARGOS = new NpcStringId(1010533);
		VARKA_SILENOS_VILLAGE = new NpcStringId(1010534);
		KETRA_ORC_VILLAGE = new NpcStringId(1010535);
		ENTRANCE_TO_THE_HOT_SPRINGS_REGION = new NpcStringId(1010536);
		WILD_BEAST_PASTURES = new NpcStringId(1010537);
		ENTRANCE_TO_THE_VALLEY_OF_SAINTS = new NpcStringId(1010538);
		CURSED_VILLAGE = new NpcStringId(1010539);
		SOUTHERN_ENTRANCE_OF_THE_WILD_BEAST_PASTURES = new NpcStringId(1010540);
		EASTERN_PART_OF_THE_WILD_BEAST_PASTURES = new NpcStringId(1010541);
		WESTERN_PART_OF_THE_WILD_BEAST_PASTURES = new NpcStringId(1010542);
		EASTERN_PART_OF_THE_SWAMP_OF_SCREAMS = new NpcStringId(1010543);
		WESTERN_PART_OF_THE_SWAMP_OF_SCREAMS = new NpcStringId(1010544);
		CENTER_OF_THE_SWAMP_OF_SCREAMS = new NpcStringId(1010545);
		ADEN_FRONTIER_GATEWAY = new NpcStringId(1010547);
		OREN_FRONTIER_GATEWAY = new NpcStringId(1010548);
		GARDEN_OF_WILD_BEASTS = new NpcStringId(1010549);
		DEVILS_PASS = new NpcStringId(1010550);
		THE_BULLETS_ARE_BEING_LOADED = new NpcStringId(1010551);
		YOU_CAN_START_AT_THE_SCHEDULED_TIME = new NpcStringId(1010552);
		UPPER_LEVEL_OF_THE_GIANTS_CAVE = new NpcStringId(1010554);
		LOWER_LEVEL_OF_THE_GIANTS_CAVE = new NpcStringId(1010555);
		IMMORTAL_PLATEAU_NORTHERN_REGION = new NpcStringId(1010556);
		ELVEN_RUINS = new NpcStringId(1010557);
		SINGING_WATERFALL = new NpcStringId(1010558);
		TALKING_ISLAND_NORTHERN_TERRITORY = new NpcStringId(1010559);
		ELVEN_FORTRESS = new NpcStringId(1010560);
		PILGRIMS_TEMPLE = new NpcStringId(1010561);
		GLUDIN_HARBOR = new NpcStringId(1010562);
		SHILENS_GARDEN = new NpcStringId(1010563);
		SCHOOL_OF_DARK_ARTS = new NpcStringId(1010564);
		SWAMP_OF_SCREAMS = new NpcStringId(1010565);
		THE_ANT_NEST = new NpcStringId(1010566);
		WALL_OF_ARGOS = new NpcStringId(1010568);
		DEN_OF_EVIL = new NpcStringId(1010569);
		ICEMANS_HUT = new NpcStringId(1010570);
		CRYPTS_OF_DISGRACE = new NpcStringId(1010571);
		PLUNDEROUS_PLAINS = new NpcStringId(1010572);
		PAVEL_RUINS = new NpcStringId(1010573);
		TOWN_OF_SCHUTTGART = new NpcStringId(1010574);
		MONASTERY_OF_SILENCE = new NpcStringId(1010575);
		MONASTERY_OF_SILENCE_REAR_GATE = new NpcStringId(1010576);
		STAKATO_NEST = new NpcStringId(1010577);
		HOW_DARE_YOU_TRESPASS_INTO_MY_TERRITORY_HAVE_YOU_NO_FEAR = new NpcStringId(1010578);
		FOOLS_WHY_HAVENT_YOU_FLED_YET_PREPARE_TO_LEARN_A_LESSON = new NpcStringId(1010579);
		BWAH_HA_HA_YOUR_DOOM_IS_AT_HAND_BEHOLD_THE_ULTRA_SECRET_SUPER_WEAPON = new NpcStringId(1010580);
		FOOLISH_INSIGNIFICANT_CREATURES_HOW_DARE_YOU_CHALLENGE_ME = new NpcStringId(1010581);
		I_SEE_THAT_NONE_WILL_CHALLENGE_ME_NOW = new NpcStringId(1010582);
		URGGH_YOU_WILL_PAY_DEARLY_FOR_THIS_INSULT = new NpcStringId(1010583);
		WHAT_YOU_HAVENT_EVEN_TWO_PENNIES_TO_RUB_TOGETHER_HARUMPH = new NpcStringId(1010584);
		FOREST_OF_MIRRORS = new NpcStringId(1010585);
		THE_CENTER_OF_THE_FOREST_OF_MIRRORS = new NpcStringId(1010586);
		SKY_WAGON_RELIC = new NpcStringId(1010588);
		THE_CENTER_OF_THE_DARK_FOREST = new NpcStringId(1010590);
		GRAVE_ROBBER_HIDEOUT = new NpcStringId(1010591);
		FOREST_OF_THE_DEAD = new NpcStringId(1010592);
		THE_CENTER_OF_THE_FOREST_OF_THE_DEAD = new NpcStringId(1010593);
		MITHRIL_MINES = new NpcStringId(1010594);
		THE_CENTER_OF_THE_MITHRIL_MINES = new NpcStringId(1010595);
		ABANDONED_COAL_MINES = new NpcStringId(1010596);
		THE_CENTER_OF_THE_ABANDONED_COAL_MINES = new NpcStringId(1010597);
		IMMORTAL_PLATEAU_WESTERN_REGION = new NpcStringId(1010598);
		VALLEY_OF_SAINTS = new NpcStringId(1010600);
		THE_CENTER_OF_THE_VALLEY_OF_SAINTS = new NpcStringId(1010601);
		CAVE_OF_TRIALS = new NpcStringId(1010603);
		SEAL_OF_SHILEN = new NpcStringId(1010604);
		THE_CENTER_OF_THE_WALL_OF_ARGOS = new NpcStringId(1010605);
		THE_CENTER_OF_ALLIGATOR_ISLAND = new NpcStringId(1010606);
		ANGHEL_WATERFALL = new NpcStringId(1010607);
		CENTER_OF_THE_ELVEN_RUINS = new NpcStringId(1010608);
		HOT_SPRINGS = new NpcStringId(1010609);
		THE_CENTER_OF_THE_HOT_SPRINGS = new NpcStringId(1010610);
		THE_CENTER_OF_DRAGON_VALLEY = new NpcStringId(1010611);
		THE_CENTER_OF_THE_NEUTRAL_ZONE = new NpcStringId(1010613);
		CRUMA_MARSHLANDS = new NpcStringId(1010614);
		THE_CENTER_OF_THE_CRUMA_MARSHLANDS = new NpcStringId(1010615);
		THE_CENTER_OF_THE_ENCHANTED_VALLEY = new NpcStringId(1010617);
		ENCHANTED_VALLEY_SOUTHERN_REGION = new NpcStringId(1010618);
		ENCHANTED_VALLEY_NORTHERN_REGION = new NpcStringId(1010619);
		FROST_LAKE = new NpcStringId(1010620);
		WASTELANDS = new NpcStringId(1010621);
		WASTELANDS_WESTERN_REGION = new NpcStringId(1010622);
		WHO_DARES_TO_COVET_THE_THRONE_OF_OUR_CASTLE_LEAVE_IMMEDIATELY_OR_YOU_WILL_PAY_THE_PRICE_OF_YOUR_AUDACITY_WITH_YOUR_VERY_OWN_BLOOD =
				new NpcStringId(1010623);
		HMM_THOSE_WHO_ARE_NOT_OF_THE_BLOODLINE_ARE_COMING_THIS_WAY_TO_TAKE_OVER_THE_CASTLE_HUMPH_THE_BITTER_GRUDGES_OF_THE_DEAD_YOU_MUST_NOT_MAKE_LIGHT_OF_THEIR_POWER =
				new NpcStringId(1010624);
		AARGH_IF_I_DIE_THEN_THE_MAGIC_FORCE_FIELD_OF_BLOOD_WILL = new NpcStringId(1010625);
		ITS_NOT_OVER_YET_IT_WONT_BE_OVER_LIKE_THIS_NEVER = new NpcStringId(1010626);
		OOOH_WHO_POURED_NECTAR_ON_MY_HEAD_WHILE_I_WAS_SLEEPING = new NpcStringId(1010627);
		PLEASE_WAIT_A_MOMENT = new NpcStringId(1010628);
		THE_WORD_YOU_NEED_THIS_TIME_IS_S1 = new NpcStringId(1010629);
		INTRUDERS_SOUND_THE_ALARM = new NpcStringId(1010630);
		DE_ACTIVATE_THE_ALARM = new NpcStringId(1010631);
		OH_NO_THE_DEFENSES_HAVE_FAILED_IT_IS_TOO_DANGEROUS_TO_REMAIN_INSIDE_THE_CASTLE_FLEE_EVERY_MAN_FOR_HIMSELF =
				new NpcStringId(1010632);
		THE_GAME_HAS_BEGUN_PARTICIPANTS_PREPARE_TO_LEARN_AN_IMPORTANT_WORD = new NpcStringId(1010633);
		S1_TEAMS_JACKPOT_HAS_S2_PERCENT_OF_ITS_HP_REMAINING = new NpcStringId(1010634);
		UNDECIDED = new NpcStringId(1010635);
		HEH_HEH_I_SEE_THAT_THE_FEAST_HAS_BEGUN_BE_WARY_THE_CURSE_OF_THE_HELLMANN_FAMILY_HAS_POISONED_THIS_LAND =
				new NpcStringId(1010636);
		ARISE_MY_FAITHFUL_SERVANTS_YOU_MY_PEOPLE_WHO_HAVE_INHERITED_THE_BLOOD_IT_IS_THE_CALLING_OF_MY_DAUGHTER_THE_FEAST_OF_BLOOD_WILL_NOW_BEGIN =
				new NpcStringId(1010637);
		GRARR_FOR_THE_NEXT_2_MINUTES_OR_SO_THE_GAME_ARENA_ARE_WILL_BE_CLEANED_THROW_ANY_ITEMS_YOU_DONT_NEED_TO_THE_FLOOR_NOW =
				new NpcStringId(1010639);
		GRARR_S1_TEAM_IS_USING_THE_HOT_SPRINGS_SULFUR_ON_THE_OPPONENTS_CAMP = new NpcStringId(1010640);
		GRARR_S1_TEAM_IS_ATTEMPTING_TO_STEAL_THE_JACKPOT = new NpcStringId(1010641);
		_VACANT_SEAT = new NpcStringId(1010642);
		S1_MINUTES_REMAINING = new NpcStringId(1010643);
		HOW_DARE_YOU_RUIN_THE_PERFORMANCE_OF_THE_DARK_CHOIR_UNFORGIVABLE = new NpcStringId(1010644);
		GET_RID_OF_THE_INVADERS_WHO_INTERRUPT_THE_PERFORMANCE_OF_THE_DARK_CHOIR = new NpcStringId(1010645);
		DONT_YOU_HEAR_THE_MUSIC_OF_DEATH_REVEAL_THE_HORROR_OF_THE_DARK_CHOIR = new NpcStringId(1010646);
		THE_IMMORTAL_PLATEAU = new NpcStringId(1010647);
		KAMAEL_VILLAGE = new NpcStringId(1010648);
		ISLE_OF_SOULS_BASE = new NpcStringId(1010649);
		GOLDEN_HILLS_BASE = new NpcStringId(1010650);
		MIMIRS_FOREST_BASE = new NpcStringId(1010651);
		ISLE_OF_SOULS_HARBOR = new NpcStringId(1010652);
		STRONGHOLD_I = new NpcStringId(1010653);
		STRONGHOLD_II = new NpcStringId(1010654);
		STRONGHOLD_III = new NpcStringId(1010655);
		FORTRESS_WEST_GATE = new NpcStringId(1010656);
		FORTRESS_EAST_GATE = new NpcStringId(1010657);
		FORTRESS_NORTH_GATE = new NpcStringId(1010658);
		FORTRESS_SOUTH_GATE = new NpcStringId(1010659);
		FRONT_OF_THE_VALLEY_FORTRESS = new NpcStringId(1010660);
		GODDARD_TOWN_SQUARE = new NpcStringId(1010661);
		FRONT_OF_THE_GODDARD_CASTLE_GATE = new NpcStringId(1010662);
		GLUDIO_TOWN_SQUARE = new NpcStringId(1010663);
		FRONT_OF_THE_GLUDIO_CASTLE_GATE = new NpcStringId(1010664);
		GIRAN_TOWN_SQUARE = new NpcStringId(1010665);
		FRONT_OF_THE_GIRAN_CASTLE_GATE = new NpcStringId(1010666);
		FRONT_OF_THE_SOUTHERN_FORTRESS = new NpcStringId(1010667);
		FRONT_OF_THE_SWAMP_FORTRESS = new NpcStringId(1010668);
		DION_TOWN_SQUARE = new NpcStringId(1010669);
		FRONT_OF_THE_DION_CASTLE_GATE = new NpcStringId(1010670);
		RUNE_TOWN_SQUARE = new NpcStringId(1010671);
		FRONT_OF_THE_RUNE_CASTLE_GATE = new NpcStringId(1010672);
		FRONT_OF_THE_WHITE_SAND_FORTRESS = new NpcStringId(1010673);
		FRONT_OF_THE_BASIN_FORTRESS = new NpcStringId(1010674);
		FRONT_OF_THE_IVORY_FORTRESS = new NpcStringId(1010675);
		SCHUTTGART_TOWN_SQUARE = new NpcStringId(1010676);
		FRONT_OF_THE_SCHUTTGART_CASTLE_GATE = new NpcStringId(1010677);
		ADEN_TOWN_SQUARE = new NpcStringId(1010678);
		FRONT_OF_THE_ADEN_CASTLE_GATE = new NpcStringId(1010679);
		FRONT_OF_THE_SHANTY_FORTRESS = new NpcStringId(1010680);
		OREN_TOWN_SQUARE = new NpcStringId(1010681);
		FRONT_OF_THE_OREN_CASTLE_GATE = new NpcStringId(1010682);
		FRONT_OF_THE_ARCHAIC_FORTRESS = new NpcStringId(1010683);
		FRONT_OF_THE_INNADRIL_CASTLE_GATE = new NpcStringId(1010684);
		FRONT_OF_THE_BORDER_FORTRESS = new NpcStringId(1010685);
		HEINE_TOWN_SQUARE = new NpcStringId(1010686);
		FRONT_OF_THE_HIVE_FORTRESS = new NpcStringId(1010687);
		FRONT_OF_THE_NARSELL_FORTRESS = new NpcStringId(1010688);
		FRONT_OF_THE_GLUDIO_CASTLE = new NpcStringId(1010689);
		FRONT_OF_THE_DION_CASTLE = new NpcStringId(1010690);
		FRONT_OF_THE_GIRAN_CASTLE = new NpcStringId(1010691);
		FRONT_OF_THE_OREN_CASTLE = new NpcStringId(1010692);
		FRONT_OF_THE_ADEN_CASTLE = new NpcStringId(1010693);
		FRONT_OF_THE_INNADRIL_CASTLE = new NpcStringId(1010694);
		FRONT_OF_THE_GODDARD_CASTLE = new NpcStringId(1010695);
		FRONT_OF_THE_RUNE_CASTLE = new NpcStringId(1010696);
		FRONT_OF_THE_SCHUTTGART_CASTLE = new NpcStringId(1010697);
		PRIMEVAL_ISLE_WHARF = new NpcStringId(1010698);
		ISLE_OF_PRAYER = new NpcStringId(1010699);
		MITHRIL_MINES_WESTERN_ENTRANCE = new NpcStringId(1010700);
		MITHRIL_MINES_EASTERN_ENTRANCE = new NpcStringId(1010701);
		THE_GIANTS_CAVE_UPPER_LAYER = new NpcStringId(1010702);
		THE_GIANTS_CAVE_LOWER_LAYER = new NpcStringId(1010703);
		FIELD_OF_SILENCE_CENTER = new NpcStringId(1010704);
		FIELD_OF_WHISPERS_CENTER = new NpcStringId(1010705);
		SHYEEDS_CAVERN = new NpcStringId(1010706);
		SEED_OF_INFINITY_DOCK = new NpcStringId(1010709);
		SEED_OF_DESTRUCTION_DOCK = new NpcStringId(1010710);
		SEED_OF_ANNIHILATION_DOCK = new NpcStringId(1010711);
		TOWN_OF_ADEN_EINHASAD_TEMPLE_PRIEST_WOOD = new NpcStringId(1010712);
		HUNTERS_VILLAGE_SEPARATED_SOUL_FRONT = new NpcStringId(1010713);
		WHAT_TOOK_SO_LONG_I_WAITED_FOR_EVER = new NpcStringId(1029350);
		I_MUST_ASK_LIBRARIAN_SOPHIA_ABOUT_THE_BOOK = new NpcStringId(1029351);
		THIS_LIBRARY_ITS_HUGE_BUT_THERE_ARENT_MANY_USEFUL_BOOKS_RIGHT = new NpcStringId(1029352);
		AN_UNDERGROUND_LIBRARY_I_HATE_DAMP_AND_SMELLY_PLACES = new NpcStringId(1029353);
		THE_BOOK_THAT_WE_SEEK_IS_CERTAINLY_HERE_SEARCH_INCH_BY_INCH = new NpcStringId(1029354);
		WE_MUST_SEARCH_HIGH_AND_LOW_IN_EVERY_ROOM_FOR_THE_READING_DESK_THAT_CONTAINS_THE_BOOK_WE_SEEK =
				new NpcStringId(1029450);
		REMEMBER_THE_CONTENT_OF_THE_BOOKS_THAT_YOU_FOUND_YOU_CANT_TAKE_THEM_OUT_WITH_YOU = new NpcStringId(1029451);
		IT_SEEMS_THAT_YOU_CANNOT_REMEMBER_TO_THE_ROOM_OF_THE_WATCHER_WHO_FOUND_THE_BOOK = new NpcStringId(1029452);
		YOUR_WORK_HERE_IS_DONE_SO_RETURN_TO_THE_CENTRAL_GUARDIAN = new NpcStringId(1029453);
		YOU_FOOLISH_INVADERS_WHO_DISTURB_THE_REST_OF_SOLINA_BE_GONE_FROM_THIS_PLACE = new NpcStringId(1029460);
		I_KNOW_NOT_WHAT_YOU_SEEK_BUT_THIS_TRUTH_CANNOT_BE_HANDLED_BY_MERE_HUMANS = new NpcStringId(1029461);
		I_WILL_NOT_STAND_BY_AND_WATCH_YOUR_FOOLISH_ACTIONS_I_WARN_YOU_LEAVE_THIS_PLACE_AT_ONCE =
				new NpcStringId(1029462);
		THE_GUARDIAN_OF_THE_SEAL_DOESNT_SEEM_TO_GET_INJURED_AT_ALL_UNTIL_THE_BARRIER_IS_DESTROYED =
				new NpcStringId(1029550);
		THE_DEVICE_LOCATED_IN_THE_ROOM_IN_FRONT_OF_THE_GUARDIAN_OF_THE_SEAL_IS_DEFINITELY_THE_BARRIER_THAT_CONTROLS_THE_GUARDIANS_POWER =
				new NpcStringId(1029551);
		TO_REMOVE_THE_BARRIER_YOU_MUST_FIND_THE_RELICS_THAT_FIT_THE_BARRIER_AND_ACTIVATE_THE_DEVICE =
				new NpcStringId(1029552);
		ALL_THE_GUARDIANS_WERE_DEFEATED_AND_THE_SEAL_WAS_REMOVED_TELEPORT_TO_THE_CENTER = new NpcStringId(1029553);
		_IS_THE_PROCESS_OF_STANDING_UP = new NpcStringId(1110071);
		_IS_THE_PROCESS_OF_SITTING_DOWN = new NpcStringId(1110072);
		IT_IS_POSSIBLE_TO_USE_A_SKILL_WHILE_SITTING_DOWN = new NpcStringId(1110073);
		IS_OUT_OF_RANGE = new NpcStringId(1110074);
		THANK_YOU_MY_BOOK_CHILD = new NpcStringId(1120300);
		KILLED_BY_S2 = new NpcStringId(1120301);
		STEWARD_PLEASE_WAIT_A_MOMENT = new NpcStringId(1121000);
		STEWARD_PLEASE_RESTORE_THE_QUEENS_FORMER_APPEARANCE = new NpcStringId(1121001);
		STEWARD_WASTE_NO_TIME_PLEASE_HURRY = new NpcStringId(1121002);
		STEWARD_WAS_IT_INDEED_TOO_MUCH_TO_ASK = new NpcStringId(1121003);
		FREYA_HEATHENS_FEEL_MY_CHILL = new NpcStringId(1121004);
		ATTENTION_PLEASE_THE_GATES_WILL_BE_CLOSING_SHORTLY_ALL_VISITORS_TO_THE_QUEENS_CASTLE_SHOULD_LEAVE_IMMEDIATELY =
				new NpcStringId(1121005);
		YOU_CANNOT_CARRY_A_WEAPON_WITHOUT_AUTHORIZATION = new NpcStringId(1121006);
		ARE_YOU_TRYING_TO_DECEIVE_ME_IM_DISAPPOINTED = new NpcStringId(1121007);
		N30_MINUTES_REMAIN = new NpcStringId(1121008);
		N20_MINUTES_REMAIN = new NpcStringId(1121009);
		CHILLY_CODA = new NpcStringId(1200001);
		BURNING_CODA = new NpcStringId(1200002);
		BLUE_CODA = new NpcStringId(1200003);
		RED_CODA = new NpcStringId(1200004);
		GOLDEN_CODA = new NpcStringId(1200005);
		DESERT_CODA = new NpcStringId(1200006);
		LUTE_CODA = new NpcStringId(1200007);
		TWIN_CODA = new NpcStringId(1200008);
		DARK_CODA = new NpcStringId(1200009);
		SHINING_CODA = new NpcStringId(1200010);
		CHILLY_COBOL = new NpcStringId(1200011);
		BURNING_COBOL = new NpcStringId(1200012);
		BLUE_COBOL = new NpcStringId(1200013);
		RED_COBOL = new NpcStringId(1200014);
		GOLDEN_COBOL = new NpcStringId(1200015);
		DESERT_COBOL = new NpcStringId(1200016);
		SEA_COBOL = new NpcStringId(1200017);
		THORN_COBOL = new NpcStringId(1200018);
		DAPPLE_COBOL = new NpcStringId(1200019);
		GREAT_COBOL = new NpcStringId(1200020);
		CHILLY_CODRAN = new NpcStringId(1200021);
		BURNING_CODRAN = new NpcStringId(1200022);
		BLUE_CODRAN = new NpcStringId(1200023);
		RED_CODRAN = new NpcStringId(1200024);
		DAPPLE_CODRAN = new NpcStringId(1200025);
		DESERT_CODRAN = new NpcStringId(1200026);
		SEA_CODRAN = new NpcStringId(1200027);
		TWIN_CODRAN = new NpcStringId(1200028);
		THORN_CODRAN = new NpcStringId(1200029);
		GREAT_CODRAN = new NpcStringId(1200030);
		ALTERNATIVE_DARK_CODA = new NpcStringId(1200031);
		ALTERNATIVE_RED_CODA = new NpcStringId(1200032);
		ALTERNATIVE_CHILLY_CODA = new NpcStringId(1200033);
		ALTERNATIVE_BLUE_CODA = new NpcStringId(1200034);
		ALTERNATIVE_GOLDEN_CODA = new NpcStringId(1200035);
		ALTERNATIVE_LUTE_CODA = new NpcStringId(1200036);
		ALTERNATIVE_DESERT_CODA = new NpcStringId(1200037);
		ALTERNATIVE_RED_COBOL = new NpcStringId(1200038);
		ALTERNATIVE_CHILLY_COBOL = new NpcStringId(1200039);
		ALTERNATIVE_BLUE_COBOL = new NpcStringId(1200040);
		ALTERNATIVE_THORN_COBOL = new NpcStringId(1200041);
		ALTERNATIVE_GOLDEN_COBOL = new NpcStringId(1200042);
		ALTERNATIVE_GREAT_COBOL = new NpcStringId(1200043);
		ALTERNATIVE_RED_CODRAN = new NpcStringId(1200044);
		ALTERNATIVE_SEA_CODRAN = new NpcStringId(1200045);
		ALTERNATIVE_CHILLY_CODRAN = new NpcStringId(1200046);
		ALTERNATIVE_BLUE_CODRAN = new NpcStringId(1200047);
		ALTERNATIVE_TWIN_CODRAN = new NpcStringId(1200048);
		ALTERNATIVE_GREAT_CODRAN = new NpcStringId(1200049);
		ALTERNATIVE_DESERT_CODRAN = new NpcStringId(1200050);
		WE_HAVE_BROKEN_THROUGH_THE_GATE_DESTROY_THE_ENCAMPMENT_AND_MOVE_TO_THE_COMMAND_POST = new NpcStringId(1300001);
		THE_COMMAND_GATE_HAS_OPENED_CAPTURE_THE_FLAG_QUICKLY_AND_RAISE_IT_HIGH_TO_PROCLAIM_OUR_VICTORY =
				new NpcStringId(1300002);
		THE_GODS_HAVE_FORSAKEN_US_RETREAT = new NpcStringId(1300003);
		YOU_MAY_HAVE_BROKEN_OUR_ARROWS_BUT_YOU_WILL_NEVER_BREAK_OUR_WILL_ARCHERS_RETREAT = new NpcStringId(1300004);
		AT_LAST_THE_MAGIC_FIELD_THAT_PROTECTS_THE_FORTRESS_HAS_WEAKENED_VOLUNTEERS_STAND_BACK =
				new NpcStringId(1300005);
		AIIEEEE_COMMAND_CENTER_THIS_IS_GUARD_UNIT_WE_NEED_BACKUP_RIGHT_AWAY = new NpcStringId(1300006);
		FORTRESS_POWER_DISABLED = new NpcStringId(1300007);
		OH_MY_WHAT_HAS_BECOME_OF_ME_MY_FAME_MY_FRIENDS_LOST_ALL_LOST = new NpcStringId(1300008);
		MACHINE_NO_1_POWER_OFF = new NpcStringId(1300009);
		MACHINE_NO_2_POWER_OFF = new NpcStringId(1300010);
		MACHINE_NO_3_POWER_OFF = new NpcStringId(1300011);
		EVERYONE_CONCENTRATE_YOUR_ATTACKS_ON_S1_SHOW_THE_ENEMY_YOUR_RESOLVE = new NpcStringId(1300012);
		ATTACKING_THE_ENEMYS_REINFORCEMENTS_IS_NECESSARY_TIME_TO_DIE = new NpcStringId(1300013);
		SPIRIT_OF_FIRE_UNLEASH_YOUR_POWER_BURN_THE_ENEMY = new NpcStringId(1300014);
		HEY_THESE_FOES_ARE_TOUGHER_THAN_THEY_LOOK_IM_GOING_TO_NEED_SOME_HELP_HERE = new NpcStringId(1300015);
		DO_YOU_NEED_MY_POWER_YOU_SEEM_TO_BE_STRUGGLING = new NpcStringId(1300016);
		IM_RATHER_BUSY_HERE_AS_WELL = new NpcStringId(1300017);
		DONT_THINK_THAT_ITS_GONNA_END_LIKE_THIS_YOUR_AMBITION_WILL_SOON_BE_DESTROYED_AS_WELL = new NpcStringId(1300018);
		YOU_MUST_HAVE_BEEN_PREPARED_TO_DIE = new NpcStringId(1300019);
		I_FEEL_SO_MUCH_GRIEF_THAT_I_CANT_EVEN_TAKE_CARE_OF_MYSELF_THERE_ISNT_ANY_REASON_FOR_ME_TO_STAY_HERE_ANY_LONGER =
				new NpcStringId(1300020);
		SHANTY_FORTRESS = new NpcStringId(1300101);
		SOUTHERN_FORTRESS = new NpcStringId(1300102);
		HIVE_FORTRESS = new NpcStringId(1300103);
		VALLEY_FORTRESS = new NpcStringId(1300104);
		IVORY_FORTRESS = new NpcStringId(1300105);
		NARSELL_FORTRESS = new NpcStringId(1300106);
		BASIN_FORTRESS = new NpcStringId(1300107);
		WHITE_SANDS_FORTRESS = new NpcStringId(1300108);
		BORDERLAND_FORTRESS = new NpcStringId(1300109);
		SWAMP_FORTRESS = new NpcStringId(1300110);
		ARCHAIC_FORTRESS = new NpcStringId(1300111);
		FLORAN_FORTRESS = new NpcStringId(1300112);
		CLOUD_MOUNTAIN_FORTRESS = new NpcStringId(1300113);
		TANOR_FORTRESS = new NpcStringId(1300114);
		DRAGONSPINE_FORTRESS = new NpcStringId(1300115);
		ANTHARASS_FORTRESS = new NpcStringId(1300116);
		WESTERN_FORTRESS = new NpcStringId(1300117);
		HUNTERS_FORTRESS = new NpcStringId(1300118);
		AARU_FORTRESS = new NpcStringId(1300119);
		DEMON_FORTRESS = new NpcStringId(1300120);
		MONASTIC_FORTRESS = new NpcStringId(1300121);
		INDEPENDENT_STATE = new NpcStringId(1300122);
		NONPARTISAN = new NpcStringId(1300123);
		CONTRACT_STATE = new NpcStringId(1300124);
		FIRST_PASSWORD_HAS_BEEN_ENTERED = new NpcStringId(1300125);
		SECOND_PASSWORD_HAS_BEEN_ENTERED = new NpcStringId(1300126);
		PASSWORD_HAS_NOT_BEEN_ENTERED = new NpcStringId(1300127);
		ATTEMPT_S1_3_IS_IN_PROGRESS_THIS_IS_THE_THIRD_ATTEMPT_ON_S1 = new NpcStringId(1300128);
		THE_1ST_MARK_IS_CORRECT = new NpcStringId(1300129);
		THE_2ND_MARK_IS_CORRECT = new NpcStringId(1300130);
		THE_MARKS_HAVE_NOT_BEEN_ASSEMBLED = new NpcStringId(1300131);
		OLYMPIAD_CLASS_FREE_TEAM_MATCH_IS_GOING_TO_BEGIN_IN_ARENA_S1_IN_A_MOMENT = new NpcStringId(1300132);
		DOMAIN_FORTRESS = new NpcStringId(1300133);
		BOUNDARY_FORTRESS = new NpcStringId(1300134);
		S1HOUR_S2MINUTE = new NpcStringId(1300135);
		NOT_DESIGNATED = new NpcStringId(1300136);
		WARRIORS_HAVE_YOU_COME_TO_HELP_THOSE_WHO_ARE_IMPRISONED_HERE = new NpcStringId(1300137);
		TAKE_THAT_YOU_WEAKLING = new NpcStringId(1300138);
		BEHOLD_MY_MIGHT = new NpcStringId(1300139);
		YOUR_MIND_IS_GOING_BLANK = new NpcStringId(1300140);
		UGH_IT_HURTS_DOWN_TO_THE_BONES = new NpcStringId(1300141);
		I_CANT_STAND_IT_ANYMORE_AAH = new NpcStringId(1300142);
		KYAAAK = new NpcStringId(1300143);
		GASP_HOW_CAN_THIS_BE = new NpcStringId(1300144);
		ILL_RIP_THE_FLESH_FROM_YOUR_BONES = new NpcStringId(1300145);
		YOULL_FLOUNDER_IN_DELUSION_FOR_THE_REST_OF_YOUR_LIFE = new NpcStringId(1300146);
		THERE_IS_NO_ESCAPE_FROM_THIS_PLACE = new NpcStringId(1300147);
		HOW_DARE_YOU = new NpcStringId(1300148);
		I_SHALL_DEFEAT_YOU = new NpcStringId(1300149);
		BEGIN_STAGE_1 = new NpcStringId(1300150);
		BEGIN_STAGE_2 = new NpcStringId(1300151);
		BEGIN_STAGE_3 = new NpcStringId(1300152);
		YOUVE_DONE_IT_WE_BELIEVED_IN_YOU_WARRIOR_WE_WANT_TO_SHOW_OUR_SINCERITY_THOUGH_IT_IS_SMALL_PLEASE_GIVE_ME_SOME_OF_YOUR_TIME =
				new NpcStringId(1300153);
		THE_CENTRAL_STRONGHOLDS_COMPRESSOR_IS_WORKING = new NpcStringId(1300154);
		STRONGHOLD_IS_COMPRESSOR_IS_WORKING = new NpcStringId(1300155);
		STRONGHOLD_IIS_COMPRESSOR_IS_WORKING = new NpcStringId(1300156);
		STRONGHOLD_IIIS_COMPRESSOR_IS_WORKING = new NpcStringId(1300157);
		THE_CENTRAL_STRONGHOLDS_COMPRESSOR_HAS_BEEN_DESTROYED = new NpcStringId(1300158);
		STRONGHOLD_IS_COMPRESSOR_HAS_BEEN_DESTROYED = new NpcStringId(1300159);
		STRONGHOLD_IIS_COMPRESSOR_HAS_BEEN_DESTROYED = new NpcStringId(1300160);
		STRONGHOLD_IIIS_COMPRESSOR_HAS_BEEN_DESTROYED = new NpcStringId(1300161);
		WHAT_A_PREDICAMENT_MY_ATTEMPTS_WERE_UNSUCCESSFUL = new NpcStringId(1300162);
		COURAGE_AMBITION_PASSION_MERCENARIES_WHO_WANT_TO_REALIZE_THEIR_DREAM_OF_FIGHTING_IN_THE_TERRITORY_WAR_COME_TO_ME_FORTUNE_AND_GLORY_ARE_WAITING_FOR_YOU =
				new NpcStringId(1300163);
		DO_YOU_WISH_TO_FIGHT_ARE_YOU_AFRAID_NO_MATTER_HOW_HARD_YOU_TRY_YOU_HAVE_NOWHERE_TO_RUN_BUT_IF_YOU_FACE_IT_HEAD_ON_OUR_MERCENARY_TROOP_WILL_HELP_YOU_OUT =
				new NpcStringId(1300164);
		CHARGE_CHARGE_CHARGE = new NpcStringId(1300165);
		OLYMPIAD_CLASS_FREE_INDIVIDUAL_MATCH_IS_GOING_TO_BEGIN_IN_ARENA_S1_IN_A_MOMENT = new NpcStringId(1300166);
		OLYMPIAD_CLASS_INDIVIDUAL_MATCH_IS_GOING_TO_BEGIN_IN_ARENA_S1_IN_A_MOMENT = new NpcStringId(1300167);
		ANOTHER_PLAYER_IS_CURRENTLY_BEING_BUFFED_PLEASE_TRY_AGAIN_IN_A_MOMENT = new NpcStringId(1600001);
		YOU_CANNOT_MOUNT_WHILE_YOU_ARE_POLYMORPHED = new NpcStringId(1600002);
		YOU_CANNOT_MOUNT_A_WYVERN_WHILE_IN_BATTLE_MODE_OR_WHILE_MOUNTED_ON_A_STRIDER = new NpcStringId(1600003);
		BOO_HOO_I_HATE = new NpcStringId(1600004);
		SEE_YOU_LATER = new NpcStringId(1600005);
		YOUVE_MADE_A_GREAT_CHOICE = new NpcStringId(1600006);
		THANKS_I_FEEL_MORE_RELAXED = new NpcStringId(1600007);
		DID_YOU_SEE_THAT_FIRECRACKER_EXPLODE = new NpcStringId(1600008);
		I_AM_NOTHING = new NpcStringId(1600009);
		I_AM_TELLING_THE_TRUTH = new NpcStringId(1600010);
		ITS_FREE_TO_GO_BACK_TO_THE_VILLAGE_YOU_TELEPORTED_FROM = new NpcStringId(1600012);
		IF_YOU_COLLECT_50_INDIVIDUAL_TREASURE_SACK_PIECES_YOU_CAN_EXCHANGE_THEM_FOR_A_TREASURE_SACK =
				new NpcStringId(1600013);
		YOU_MUST_BE_TRANSFORMED_INTO_A_TREASURE_HUNTER_TO_FIND_A_CHEST = new NpcStringId(1600014);
		YOUD_BETTER_USE_THE_TRANSFORMATION_SPELL_AT_THE_RIGHT_MOMENT_SINCE_IT_DOESNT_LAST_LONG =
				new NpcStringId(1600015);
		ALL_OF_FANTASY_ISLE_IS_A_PEACE_ZONE = new NpcStringId(1600016);
		IF_YOU_NEED_TO_GO_TO_FANTASY_ISLE_COME_SEE_ME = new NpcStringId(1600017);
		YOU_CAN_ONLY_PURCHASE_A_TREASURE_HUNTER_TRANSFORMATION_SCROLL_ONCE_EVERY_12_HOURS = new NpcStringId(1600018);
		IF_YOUR_MEANS_OF_ARRIVAL_WAS_A_BIT_UNCONVENTIONAL_THEN_ILL_BE_SENDING_YOU_BACK_TO_RUNE_TOWNSHIP_WHICH_IS_THE_NEAREST_TOWN =
				new NpcStringId(1600019);
		RATTLE = new NpcStringId(1600020);
		BUMP = new NpcStringId(1600021);
		YOU_WILL_REGRET_THIS = new NpcStringId(1600022);
		CARE_TO_CHALLENGE_FATE_AND_TEST_YOUR_LUCK = new NpcStringId(1600023);
		DONT_PASS_UP_THE_CHANCE_TO_WIN_AN_S80_WEAPON = new NpcStringId(1600024);
		_S1S_COMMAND_CHANNEL_HAS_LOOTING_RIGHTS = new NpcStringId(1800009);
		LOOTING_RULES_ARE_NO_LONGER_ACTIVE = new NpcStringId(1800010);
		OUR_MASTER_NOW_COMES_TO_CLAIM_OUR_VENGEANCE_SOON_YOU_WILL_ALL_BE_NOTHING_MORE_THAN_DIRT =
				new NpcStringId(1800011);
		DEATH_TO_THOSE_WHO_CHALLENGE_THE_LORDS_OF_DAWN = new NpcStringId(1800012);
		DEATH_TO_THOSE_WHO_CHALLENGE_THE_LORD = new NpcStringId(1800013);
		OINK_OINK_PIGS_CAN_DO_IT_TOO_ANTHARAS_SURPASSING_SUPER_POWERED_PIG_STUN_HOW_DO_LIKE_THEM_APPLES =
				new NpcStringId(1800014);
		OINK_OINK_TAKE_THAT_VALAKAS_TERRORIZING_ULTRA_PIG_FEAR_HA_HA = new NpcStringId(1800015);
		OINK_OINK_GO_AWAY_STOP_BOTHERING_ME = new NpcStringId(1800016);
		OINK_OINK_PIGS_OF_THE_WORLD_UNITE_LETS_SHOW_THEM_OUR_STRENGTH = new NpcStringId(1800017);
		YOU_HEALED_ME_THANKS_A_LOT_OINK_OINK = new NpcStringId(1800018);
		OINK_OINK_THIS_TREATMENT_HURTS_TOO_MUCH_ARE_YOU_SURE_THAT_YOURE_TRYING_TO_HEAL_ME = new NpcStringId(1800019);
		OINK_OINK_TRANSFORM_WITH_MOON_CRYSTAL_PRISM_POWER = new NpcStringId(1800020);
		OINK_OINK_NOOO_I_DONT_WANT_TO_GO_BACK_TO_NORMAL = new NpcStringId(1800021);
		OINK_OINK_IM_RICH_SO_IVE_GOT_PLENTY_TO_SHARE_THANKS = new NpcStringId(1800022);
		ITS_A_WEAPON_SURROUNDED_BY_AN_OMINOUS_AURA_ILL_DISCARD_IT_BECAUSE_IT_MAY_BE_DANGEROUS_MISS =
				new NpcStringId(1800023);
		THANK_YOU_FOR_SAVING_ME_FROM_THE_CLUTCHES_OF_EVIL = new NpcStringId(1800024);
		THAT_IS_IT_FOR_TODAYLETS_RETREAT_EVERYONE_PULL_BACK = new NpcStringId(1800025);
		THANK_YOU_FOR_THE_RESCUE_ITS_A_SMALL_GIFT = new NpcStringId(1800026);
		S1_YOU_DONT_HAVE_A_RED_CRYSTAL = new NpcStringId(1800027);
		S1_YOU_DONT_HAVE_A_BLUE_CRYSTAL = new NpcStringId(1800028);
		S1_YOU_DONT_HAVE_A_CLEAR_CRYSTAL = new NpcStringId(1800029);
		S1_IF_YOU_ARE_TOO_FAR_AWAY_FROM_MEI_CANT_LET_YOU_GO = new NpcStringId(1800030);
		AN_ALARM_HAS_BEEN_SET_OFF_EVERYBODY_WILL_BE_IN_DANGER_IF_THEY_ARE_NOT_TAKEN_CARE_OF_IMMEDIATELY =
				new NpcStringId(1800031);
		IT_WILL_NOT_BE_THAT_EASY_TO_KILL_ME = new NpcStringId(1800032);
		NOYOU_KNEW_MY_WEAKNESS = new NpcStringId(1800033);
		HELLO_IS_ANYONE_THERE = new NpcStringId(1800034);
		IS_NO_ONE_THERE_HOW_LONG_HAVE_I_BEEN_HIDING_I_HAVE_BEEN_STARVING_FOR_DAYS_AND_CANNOT_HOLD_OUT_ANYMORE =
				new NpcStringId(1800035);
		IF_SOMEONE_WOULD_GIVE_ME_SOME_OF_THOSE_TASTY_CRYSTAL_FRAGMENTS_I_WOULD_GLADLY_TELL_THEM_WHERE_TEARS_IS_HIDING_YUMMY_YUMMY =
				new NpcStringId(1800036);
		HEY_YOU_FROM_ABOVE_THE_GROUND_LETS_SHARE_SOME_CRYSTAL_FRAGMENTS_IF_YOU_HAVE_ANY = new NpcStringId(1800037);
		CRISPY_AND_COLD_FEELING_TEEHEE_DELICIOUS = new NpcStringId(1800038);
		YUMMY_THIS_IS_SO_TASTY = new NpcStringId(1800039);
		SNIFF_SNIFF_GIVE_ME_MORE_CRYSTAL_FRAGMENTS = new NpcStringId(1800040);
		HOW_INSENSITIVE_ITS_NOT_NICE_TO_GIVE_ME_JUST_A_PIECE_CANT_YOU_GIVE_ME_MORE = new NpcStringId(1800041);
		AH_IM_HUNGRY = new NpcStringId(1800042);
		IM_THE_REAL_ONE = new NpcStringId(1800043);
		PICK_ME = new NpcStringId(1800044);
		TRUST_ME = new NpcStringId(1800045);
		NOT_THAT_DUDE_IM_THE_REAL_ONE = new NpcStringId(1800046);
		DONT_BE_FOOLED_DONT_BE_FOOLED_IM_THE_REAL_ONE = new NpcStringId(1800047);
		JUST_ACT_LIKE_THE_REAL_ONE_OOPS = new NpcStringId(1800048);
		YOUVE_BEEN_FOOLED = new NpcStringId(1800049);
		SORRY_BUT_IM_THE_FAKE_ONE = new NpcStringId(1800050);
		IM_THE_REAL_ONE_PHEW = new NpcStringId(1800051);
		CANT_YOU_EVEN_FIND_OUT = new NpcStringId(1800052);
		FIND_ME = new NpcStringId(1800053);
		HUH_HOW_DID_YOU_KNOW_IT_WAS_ME = new NpcStringId(1800054);
		EXCELLENT_CHOICE_TEEHEE = new NpcStringId(1800055);
		YOUVE_DONE_WELL = new NpcStringId(1800056);
		OH_VERY_SENSIBLE = new NpcStringId(1800057);
		BEHOLD_THE_MIGHTY_POWER_OF_BAYLOR_FOOLISH_MORTAL = new NpcStringId(1800058);
		NO_ONE_IS_GOING_TO_SURVIVE = new NpcStringId(1800059);
		YOULL_SEE_WHAT_HELL_IS_LIKE = new NpcStringId(1800060);
		YOU_WILL_BE_PUT_IN_JAIL = new NpcStringId(1800061);
		WORTHLESS_CREATURE_GO_TO_HELL = new NpcStringId(1800062);
		ILL_GIVE_YOU_SOMETHING_THAT_YOULL_NEVER_FORGET = new NpcStringId(1800063);
		WHY_DID_YOU_TRUST_TO_CHOOSE_ME_HAHAHAHA = new NpcStringId(1800064);
		ILL_MAKE_YOU_REGRET_THAT_YOU_EVER_CHOSE_ME = new NpcStringId(1800065);
		DONT_EXPECT_TO_GET_OUT_ALIVE = new NpcStringId(1800066);
		DEMON_KING_BELETH_GIVE_ME_THE_POWER_AAAHH = new NpcStringId(1800067);
		NO_I_FEEL_THE_POWER_OF_FAFURION = new NpcStringId(1800068);
		FAFURION_PLEASE_GIVE_POWER_TO_THIS_HELPLESS_WITCH = new NpcStringId(1800069);
		I_CANT_HELP_YOU_MUCH_BUT_I_CAN_WEAKEN_THE_POWER_OF_BAYLOR_SINCE_IM_LOCKED_UP_HERE = new NpcStringId(1800070);
		YOUR_SKILL_IS_IMPRESSIVE_ILL_ADMIT_THAT_YOU_ARE_GOOD_ENOUGH_TO_PASS_TAKE_THE_KEY_AND_LEAVE_THIS_PLACE =
				new NpcStringId(1800071);
		GIVE_ME_ALL_YOU_HAVE_ITS_THE_ONLY_WAY_ILL_LET_YOU_LIVE = new NpcStringId(1800072);
		HUN_HUNGRY = new NpcStringId(1800073);
		DONT_BE_LAZY_YOU_BASTARDS = new NpcStringId(1800074);
		THEY_ARE_JUST_HENCHMEN_OF_THE_IRON_CASTLE_WHY_DID_WE_HIDE = new NpcStringId(1800075);
		GUYS_SHOW_THEM_OUR_POWER = new NpcStringId(1800076);
		YOU_HAVE_FINALLY_COME_HERE_BUT_YOU_WILL_NOT_BE_ABLE_TO_FIND_THE_SECRET_ROOM = new NpcStringId(1800077);
		YOU_HAVE_DONE_WELL_IN_FINDING_ME_BUT_I_CANNOT_JUST_HAND_YOU_THE_KEY = new NpcStringId(1800078);
		S1_SECONDS_REMAINING = new NpcStringId(1800079);
		THE_MATCH_IS_AUTOMATICALLY_CANCELED_BECAUSE_YOU_ARE_TOO_FAR_FROM_THE_ADMISSION_MANAGER =
				new NpcStringId(1800081);
		UGH_I_HAVE_BUTTERFLIES_IN_MY_STOMACH_THE_SHOW_STARTS_SOON = new NpcStringId(1800082);
		THANK_YOU_ALL_FOR_COMING_HERE_TONIGHT = new NpcStringId(1800083);
		IT_IS_AN_HONOR_TO_HAVE_THE_SPECIAL_SHOW_TODAY = new NpcStringId(1800084);
		FANTASY_ISLE_IS_FULLY_COMMITTED_TO_YOUR_HAPPINESS = new NpcStringId(1800085);
		NOW_ID_LIKE_TO_INTRODUCE_THE_MOST_BEAUTIFUL_SINGER_IN_ADEN_PLEASE_WELCOMELEYLA_MIRA = new NpcStringId(1800086);
		HERE_SHE_COMES = new NpcStringId(1800087);
		THANK_YOU_VERY_MUCH_LEYLA = new NpcStringId(1800088);
		NOW_WERE_IN_FOR_A_REAL_TREAT = new NpcStringId(1800089);
		JUST_BACK_FROM_THEIR_WORLD_TOUR_PUT_YOUR_HANDS_TOGETHER_FOR_THE_FANTASY_ISLE_CIRCUS = new NpcStringId(1800090);
		COME_ON_EVERYONE = new NpcStringId(1800091);
		DID_YOU_LIKE_IT_THAT_WAS_SO_AMAZING = new NpcStringId(1800092);
		NOW_WE_ALSO_INVITED_INDIVIDUALS_WITH_SPECIAL_TALENTS = new NpcStringId(1800093);
		LETS_WELCOME_THE_FIRST_PERSON_HERE = new NpcStringId(1800094);
		OH = new NpcStringId(1800095);
		OKAY_NOW_HERE_COMES_THE_NEXT_PERSON_COME_ON_UP_PLEASE = new NpcStringId(1800096);
		OH_IT_LOOKS_LIKE_SOMETHING_GREAT_IS_GOING_TO_HAPPEN_RIGHT = new NpcStringId(1800097);
		OH_MY = new NpcStringId(1800098);
		THATS_G_GREAT_NOW_HERE_COMES_THE_LAST_PERSON = new NpcStringId(1800099);
		NOW_THIS_IS_THE_END_OF_TODAYS_SHOW = new NpcStringId(1800100);
		HOW_WAS_IT_I_HOPE_YOU_ALL_ENJOYED_IT = new NpcStringId(1800101);
		PLEASE_REMEMBER_THAT_FANTASY_ISLE_IS_ALWAYS_PLANNING_A_LOT_OF_GREAT_SHOWS_FOR_YOU = new NpcStringId(1800102);
		WELL_I_WISH_I_COULD_CONTINUE_ALL_NIGHT_LONG_BUT_THIS_IS_IT_FOR_TODAY_THANK_YOU = new NpcStringId(1800103);
		WE_LOVE_YOU = new NpcStringId(1800104);
		HOW_COME_PEOPLE_ARE_NOT_HERE_WE_ARE_ABOUT_TO_START_THE_SHOW_HMM = new NpcStringId(1800105);
		THE_OPPONENT_TEAM_CANCELED_THE_MATCH = new NpcStringId(1800106);
		ITS_NOT_EASY_TO_OBTAIN = new NpcStringId(1800107);
		YOURE_OUT_OF_YOUR_MIND_COMING_HERE = new NpcStringId(1800108);
		ENEMY_INVASION_HURRY_UP = new NpcStringId(1800109);
		PROCESS_SHOULDNT_BE_DELAYED_BECAUSE_OF_ME = new NpcStringId(1800110);
		ALRIGHT_NOW_LEODAS_IS_YOURS = new NpcStringId(1800111);
		WE_MIGHT_NEED_NEW_SLAVES_ILL_BE_BACK_SOON_SO_WAIT = new NpcStringId(1800112);
		TIME_RIFT_DEVICE_ACTIVATION_SUCCESSFUL = new NpcStringId(1800113);
		FREEDOM_OR_DEATH = new NpcStringId(1800114);
		THIS_IS_THE_WILL_OF_TRUE_WARRIORS = new NpcStringId(1800115);
		WELL_HAVE_DINNER_IN_HELL = new NpcStringId(1800116);
		DETONATOR_INITIALIZATION_TIME_S1_MINUTES_FROM_NOW = new NpcStringId(1800117);
		ZZZZ_CITY_INTERFERENCE_ERROR_FORWARD_EFFECT_CREATED = new NpcStringId(1800118);
		ZZZZ_CITY_INTERFERENCE_ERROR_RECURRENCE_EFFECT_CREATED = new NpcStringId(1800119);
		GUARDS_ARE_COMING_RUN = new NpcStringId(1800120);
		NOW_I_CAN_ESCAPE_ON_MY_OWN = new NpcStringId(1800121);
		THANKS_FOR_YOUR_HELP = new NpcStringId(1800122);
		MATCH_CANCELLED_OPPONENT_DID_NOT_MEET_THE_STADIUM_ADMISSION_REQUIREMENTS = new NpcStringId(1800123);
		HA_HA_YES_DIE_SLOWLY_WRITHING_IN_PAIN_AND_AGONY = new NpcStringId(1800124);
		MORE_NEED_MORE_SEVERE_PAIN = new NpcStringId(1800125);
		AHH_MY_LIFE_IS_BEING_DRAINED_OUT = new NpcStringId(1800126);
		SOMETHING_IS_BURNING_INSIDE_MY_BODY = new NpcStringId(1800127);
		S1_NOT_ADEQUATE_FOR_THE_STADIUM_LEVEL = new NpcStringId(1800128);
		S1_THANK_YOU_FOR_GIVING_ME_YOUR_LIFE = new NpcStringId(1800129);
		I_FAILED_PLEASE_FORGIVE_ME_DARION = new NpcStringId(1800130);
		S1_ILL_BE_BACK_DONT_GET_COMFORTABLE = new NpcStringId(1800131);
		IF_YOU_THINK_IM_GIVING_UP_LIKE_THIS_YOURE_WRONG = new NpcStringId(1800132);
		SO_YOURE_JUST_GOING_TO_WATCH_ME_SUFFER = new NpcStringId(1800133);
		ITS_NOT_OVER_YET = new NpcStringId(1800134);
		HA_HA_YOU_WERE_SO_AFRAID_OF_DEATH_LET_ME_SEE_IF_YOU_FIND_ME_IN_TIME_MAYBE_YOU_CAN_FIND_A_WAY =
				new NpcStringId(1800135);
		DONT_KILL_ME_PLEASE_SOMETHINGS_STRANGLING_ME = new NpcStringId(1800136);
		WHO_WILL_BE_THE_LUCKY_ONE_TONIGHT_HA_HA_CURIOUS_VERY_CURIOUS = new NpcStringId(1800137);
		SQUEAK_THIS_WILL_BE_STRONGER_THAN_THE_STUN_THE_PIG_USED_LAST_TIME = new NpcStringId(1800138);
		SQUEAK_HERE_IT_GOES_EXTREMELY_SCARY_EVEN_TO_VALAKAS = new NpcStringId(1800139);
		SQUEAK_GO_AWAY_LEAVE_US_ALONE = new NpcStringId(1800140);
		SQUEAK_GUYS_GATHER_UP_LETS_SHOW_OUR_POWER = new NpcStringId(1800141);
		ITS_NOT_LIKE_IM_GIVING_THIS_BECAUSE_IM_GRATEFUL_SQUEAK = new NpcStringId(1800142);
		SQUEAK_EVEN_IF_IT_IS_TREATMENT_MY_BOTTOM_HURTS_SO_MUCH = new NpcStringId(1800143);
		SQUEAK_TRANSFORM_TO_MOON_CRYSTAL_PRISM_POWER = new NpcStringId(1800144);
		SQUEAK_OH_NO_I_DONT_WANT_TO_TURN_BACK_AGAIN = new NpcStringId(1800145);
		SQUEAK_IM_SPECIALLY_GIVING_YOU_A_LOT_SINCE_IM_RICH_THANK_YOU = new NpcStringId(1800146);
		OINK_OINK_RAGE_IS_BOILING_UP_INSIDE_OF_ME_POWER_INFINITE_POWER = new NpcStringId(1800147);
		OINK_OINK_IM_REALLY_FURIOUS_RIGHT_NOW = new NpcStringId(1800148);
		SQUEAK_RAGE_IS_BOILING_UP_INSIDE_OF_ME_POWER_INFINITE_POWER = new NpcStringId(1800149);
		SQUEAK_IM_REALLY_FURIOUS_RIGHT_NOW = new NpcStringId(1800150);
		G_RANK = new NpcStringId(1800162);
		HUH_NO_ONE_WOULD_HAVE_GUESSED_THAT_A_DOOMED_CREATURE_WOULD_BE_SO_POWERFUL = new NpcStringId(1800163);
		S_GRADE = new NpcStringId(1800164);
		A_GRADE = new NpcStringId(1800165);
		B_GRADE = new NpcStringId(1800166);
		C_GRADE = new NpcStringId(1800167);
		D_GRADE = new NpcStringId(1800168);
		F_GRADE = new NpcStringId(1800169);
		THIS_IS_THIS_IS_A_GREAT_ACHIEVEMENT_THAT_IS_WORTHY_OF_THE_TRUE_HEROES_OF_LEGEND = new NpcStringId(1800170);
		ADMIRABLE_YOU_GREATLY_DECREASED_THE_SPEED_OF_INVASION_THROUGH_KAMALOKA = new NpcStringId(1800171);
		VERY_GOOD_YOUR_SKILL_MAKES_YOU_A_MODEL_FOR_OTHER_ADVENTURERS_TO_FOLLOW = new NpcStringId(1800172);
		GOOD_WORK_IF_ALL_ADVENTURERS_PRODUCE_RESULTS_LIKE_YOU_WE_WILL_SLOWLY_START_TO_SEE_THE_GLIMMER_OF_HOPE =
				new NpcStringId(1800173);
		UNFORTUNATELY_IT_SEEMS_THAT_RIM_KAMALOKA_CANNOT_BE_EASILY_APPROACHED_BY_EVERYONE = new NpcStringId(1800174);
		HOW_DISAPPOINTING_IT_LOOKS_LIKE_I_MADE_A_MISTAKE_IN_SENDING_YOU_INSIDE_RIM_KAMALOKA = new NpcStringId(1800175);
		INTRUDER_ALERT_INTRUDER_ALERT = new NpcStringId(1800176);
		WHAT_ARE_YOU_DOING_HURRY_UP_AND_HELP_ME = new NpcStringId(1800177);
		IVE_HAD_IT_UP_TO_HERE_WITH_YOU_ILL_TAKE_CARE_OF_YOU = new NpcStringId(1800178);
		AH_MY_MIND_IS_A_WRECK = new NpcStringId(1800179);
		IF_YOU_THOUGHT_THAT_MY_SUBORDINATES_WOULD_BE_SO_FEW_YOU_ARE_MISTAKEN = new NpcStringId(1800180);
		THERES_NOT_MUCH_I_CAN_DO_BUT_I_WANT_TO_HELP_YOU = new NpcStringId(1800181);
		YOU_S1_ATTACK_THEM = new NpcStringId(1800182);
		COME_OUT_MY_SUBORDINATE_I_SUMMON_YOU_TO_DRIVE_THEM_OUT = new NpcStringId(1800183);
		THERES_NOT_MUCH_I_CAN_DO_BUT_I_WILL_RISK_MY_LIFE_TO_HELP_YOU = new NpcStringId(1800184);
		ARG_THE_PAIN_IS_MORE_THAN_I_CAN_STAND = new NpcStringId(1800185);
		AHH_HOW_DID_HE_FIND_MY_WEAKNESS = new NpcStringId(1800186);
		WE_WERE_ABLE_TO_SUCCESSFULLY_COLLECT_THE_ESSENCE_OF_KAMALOKA_FROM_THE_KANABIONS_THAT_YOU_DEFEATED_HERE_THEY_ARE =
				new NpcStringId(1800187);
		BUT_WE_WERE_ABLE_TO_COLLECT_SOMEHOW_THE_ESSENCE_OF_KAMALOKA_FROM_THE_KANABIONS_THAT_YOU_DEFEATED_HERE_THEY_ARE =
				new NpcStringId(1800188);
		IM_SORRY_BUT_WE_WERE_UNABLE_TO_COLLECT_THE_ESSENCE_OF_KAMALOKA_FROM_THE_KANABIONS_THAT_YOU_DEFEATED_BECAUSE_THEIR_DARK_ENERGY_WAS_TOO_WEAK =
				new NpcStringId(1800189);
		RATHER_THAN_SIMPLY_DEFEATING_THE_ENEMIES_YOU_SEEM_TO_UNDERSTAND_OUR_GOAL_AND_PURPOSE_AS_WELL =
				new NpcStringId(1800190);
		DOPPLERS_AND_VOIDS_POSSESS_AN_ENHANCED_AMOUNT_OF_THE_KANABIONS_DARK_ENERGY_SO_IT_IS_IMPORTANT_TO_CONCENTRATE_ON_DEFEATING_THEM_WHEN_BLOCKING_THE_KAMALOKIANS_ATTACK =
				new NpcStringId(1800191);
		HAVE_YOU_SEEN_KANABIONS_BEING_REMADE_AS_NEW_KANABIONS_SOMETIMES_YOU_CAN_SEE_IT_OCCUR_MORE_OFTEN_BY_INFLICTING_GREAT_DAMAGE_DURING_AN_ATTACK_OR_AT_THE_MOMENT_YOU_DEFEAT_THEM =
				new NpcStringId(1800192);
		AS_IN_ANY_OTHER_BATTLE_IT_IS_CRITICAL_TO_PROTECT_YOURSELF_WHILE_YOU_ARE_INSIDE_RIM_KAMALOKA_WE_DO_NOT_WANT_TO_ATTACK_RECKLESSLY =
				new NpcStringId(1800193);
		WE_VALUE_DEVELOPING_AN_INDIVIDUALS_OVERALL_POWER_RATHER_THAN_A_ONE_TIME_VICTORY_IF_YOU_RELIED_ON_ANOTHER_PERSONS_SUPPORT_THIS_TIME_WHY_DONT_YOU_TRY_TO_RELY_ON_YOUR_OWN_STRENGTH_NEXT_TIME =
				new NpcStringId(1800194);
		ARE_YOU_SURE_THAT_THE_BATTLE_JUST_NOW_WAS_AT_THE_APPROPRIATE_LEVEL_BOTHERING_LOWER_KANABIONS_AS_IF_FOR_MERE_ENTERTAINMENT_IS_CONSIDERED_TO_BE_A_WASTED_BATTLE_FOR_US =
				new NpcStringId(1800195);
		THE_GREATEST_VICTORY_INVOLVES_USING_ALL_AVAILABLE_RESOURCES_ELIMINATING_ALL_OF_THE_ENEMYS_OPPORTUNITIES_AND_BRINGING_IT_TO_THE_FASTEST_POSSIBLE_END_DONT_YOU_THINK_SO =
				new NpcStringId(1800196);
		EMERGENCY_EMERGENCY_THE_OUTER_WALL_IS_WEAKENING_RAPIDLY = new NpcStringId(1800197);
		THE_REMAINING_STRENGTH_OF_THE_OUTER_WALL_IS_S1 = new NpcStringId(1800198);
		PATHFINDER_SAVIOR = new NpcStringId(1800199);
		PATHFINDER_SUPPORTER = new NpcStringId(1800200);
		SOME_KANABIONS_WHO_HAVENT_FULLY_ADJUSTED_YET_TO_THEIR_NEW_PHYSICAL_FORM_ARE_KNOWN_TO_EXHIBIT_SYMPTOMS_OF_AN_EXTREMELY_WEAKENED_BODY_STRUCTURE_SOMETIMES_IF_YOU_ATTACK_THEM_AT_THAT_MOMENT_YOU_WILL_HAVE_GREAT_RESULTS =
				new NpcStringId(1800201);
		HAVE_YOU_EVER_HEARD_OF_S1_THEY_SAY_ITS_A_GENUINE_S2 = new NpcStringId(1800202);
		THERE_ARE_5_MINUTES_REMAINING_TO_REGISTER_FOR_KRATEIS_CUBE_MATCH = new NpcStringId(1800203);
		THERE_ARE_3_MINUTES_REMAINING_TO_REGISTER_FOR_KRATEIS_CUBE_MATCH = new NpcStringId(1800204);
		THERE_ARE_1_MINUTES_REMAINING_TO_REGISTER_FOR_KRATEIS_CUBE_MATCH = new NpcStringId(1800205);
		THE_MATCH_WILL_BEGIN_SHORTLY = new NpcStringId(1800207);
		OHHOHOH = new NpcStringId(1800209);
		FIRE = new NpcStringId(1800210);
		WATER = new NpcStringId(1800211);
		WIND = new NpcStringId(1800212);
		EARTH = new NpcStringId(1800213);
		ITS_S1 = new NpcStringId(1800214);
		S1_IS_STRONG = new NpcStringId(1800215);
		ITS_ALWAYS_S1 = new NpcStringId(1800216);
		S1_WONT_DO = new NpcStringId(1800217);
		YOU_WILL_BE_CURSED_FOR_SEEKING_THE_TREASURE = new NpcStringId(1800218);
		THE_AIRSHIP_HAS_BEEN_SUMMONED_IT_WILL_AUTOMATICALLY_DEPART_IN_5_MINUTES = new NpcStringId(1800219);
		THE_REGULARLY_SCHEDULED_AIRSHIP_HAS_ARRIVED_IT_WILL_DEPART_FOR_THE_ADEN_CONTINENT_IN_1_MINUTE =
				new NpcStringId(1800220);
		THE_REGULARLY_SCHEDULED_AIRSHIP_THAT_FLIES_TO_THE_ADEN_CONTINENT_HAS_DEPARTED = new NpcStringId(1800221);
		THE_REGULARLY_SCHEDULED_AIRSHIP_HAS_ARRIVED_IT_WILL_DEPART_FOR_THE_GRACIA_CONTINENT_IN_1_MINUTE =
				new NpcStringId(1800222);
		THE_REGULARLY_SCHEDULED_AIRSHIP_THAT_FLIES_TO_THE_GRACIA_CONTINENT_HAS_DEPARTED = new NpcStringId(1800223);
		ANOTHER_AIRSHIP_HAS_BEEN_SUMMONED_TO_THE_WHARF_PLEASE_TRY_AGAIN_LATER = new NpcStringId(1800224);
		HUH_THE_SKY_LOOKS_FUNNY_WHATS_THAT = new NpcStringId(1800225);
		A_POWERFUL_SUBORDINATE_IS_BEING_HELD_BY_THE_BARRIER_ORB_THIS_REACTION_MEANS = new NpcStringId(1800226);
		BE_CAREFUL_SOMETHINGS_COMING = new NpcStringId(1800227);
		YOU_MUST_FIRST_FOUND_A_CLAN_OR_BELONG_TO_ONE = new NpcStringId(1800228);
		THERE_IS_NO_PARTY_CURRENTLY_CHALLENGING_EKIMUS_N_IF_NO_PARTY_ENTERS_WITHIN_S1_SECONDS_THE_ATTACK_ON_THE_HEART_OF_IMMORTALITY_WILL_FAIL =
				new NpcStringId(1800229);
		EKIMUS_HAS_GAINED_STRENGTH_FROM_A_TUMOR = new NpcStringId(1800230);
		EKIMUS_HAS_BEEN_WEAKENED_BY_LOSING_STRENGTH_FROM_A_TUMOR = new NpcStringId(1800231);
		CMON_CMON_SHOW_YOUR_FACE_YOU_LITTLE_RATS_LET_ME_SEE_WHAT_THE_DOOMED_WEAKLINGS_ARE_SCHEMING =
				new NpcStringId(1800233);
		IMPRESSIVE_HAHAHA_ITS_SO_MUCH_FUN_BUT_I_NEED_TO_CHILL_A_LITTLE_WHILE_ARGEKUNTE_CLEAR_THE_WAY =
				new NpcStringId(1800234);
		KYAHAHA_SINCE_THE_TUMOR_HAS_BEEN_RESURRECTED_I_NO_LONGER_NEED_TO_WASTE_MY_TIME_ON_YOU =
				new NpcStringId(1800235);
		KEU_I_WILL_LEAVE_FOR_NOW_BUT_DONT_THINK_THIS_IS_OVER_THE_SEED_OF_INFINITY_CAN_NEVER_DIE =
				new NpcStringId(1800236);
		KAHAHAHA_THAT_GUYS_NOTHING_HE_CANT_EVEN_KILL_WITHOUT_MY_PERMISSION_SEE_HERE_ULTIMATE_FORGOTTEN_MAGIC_DEATHLESS_GUARDIAN =
				new NpcStringId(1800237);
		I_CURSE_THE_DAY_THAT_I_BECAME_YOUR_SLAVE_IN_ORDER_TO_ESCAPE_DEATH_COHEMENES_I_SWEAR_THAT_I_SHALL_SEE_YOU_DIE_WITH_MY_OWN_EYES =
				new NpcStringId(1800238);
		MY_ENEMY_IS_DYING_AND_MY_BLOOD_IS_BOILING_WHAT_CRUEL_CURSE_IS_THIS = new NpcStringId(1800239);
		HALL_OF_SUFFERING = new NpcStringId(1800240);
		HALL_OF_EROSION = new NpcStringId(1800241);
		HEART_OF_IMMORTALITY = new NpcStringId(1800242);
		ATTACK = new NpcStringId(1800243);
		DEFEND = new NpcStringId(1800244);
		CONGRATULATIONS_YOU_HAVE_SUCCEEDED_AT_S1_S2_THE_INSTANCE_WILL_SHORTLY_EXPIRE = new NpcStringId(1800245);
		YOU_HAVE_FAILED_AT_S1_S2_THE_INSTANCE_WILL_SHORTLY_EXPIRE = new NpcStringId(1800246);
		S1S_PARTY_HAS_MOVED_TO_A_DIFFERENT_LOCATION_THROUGH_THE_CRACK_IN_THE_TUMOR = new NpcStringId(1800247);
		S1S_PARTY_HAS_ENTERED_THE_CHAMBER_OF_EKIMUS_THROUGH_THE_CRACK_IN_THE_TUMOR = new NpcStringId(1800248);
		EKIMUS_HAS_SENSED_ABNORMAL_ACTIVITY_NTHE_ADVANCING_PARTY_IS_FORCEFULLY_EXPELLED = new NpcStringId(1800249);
		THERE_ARENT_ENOUGH_ITEMS_IN_ORDER_TO_SUMMON_THE_AIRSHIP_YOU_NEED_5_ENERGY_STAR_STONES =
				new NpcStringId(1800250);
		THE_SOUL_DEVOURERS_WHO_ARE_GREEDY_TO_EAT_THE_SEEDS_OF_LIFE_THAT_REMAIN_ALIVE_UNTIL_THE_END_HAVE_AWAKENED =
				new NpcStringId(1800251);
		THE_FIRST_FERAL_HOUND_OF_THE_NETHERWORLD_HAS_AWAKENED = new NpcStringId(1800252);
		THE_SECOND_FERAL_HOUND_OF_THE_NETHERWORLD_HAS_AWAKENED = new NpcStringId(1800253);
		CLINGING_ON_WONT_HELP_YOU_ULTIMATE_FORGOTTEN_MAGIC_BLADE_TURN = new NpcStringId(1800254);
		EVEN_SPECIAL_SAUCE_CANT_HELP_YOU_ULTIMATE_FORGOTTEN_MAGIC_FORCE_SHIELD = new NpcStringId(1800255);
		YOU_LITTLE_DOOMED_MAGGOTS_EVEN_IF_YOU_KEEP_SWARMING_THE_POWER_OF_IMMORTALITY_WILL_ONLY_GROW_STRONGER =
				new NpcStringId(1800256);
		THE_AIRSHIP_SUMMON_LICENSE_HAS_BEEN_AWARDED_YOUR_CLAN_CAN_NOW_SUMMON_AN_AIRSHIP = new NpcStringId(1800257);
		THE_GRACIA_TREASURE_BOX_HAS_APPEARED = new NpcStringId(1800258);
		THE_GRACIA_TREASURE_BOX_WILL_SOON_DISAPPEAR = new NpcStringId(1800259);
		YOU_HAVE_BEEN_CURSED_BY_THE_TUMOR_AND_HAVE_INCURRED_S1_DAMAGE = new NpcStringId(1800260);
		I_SHALL_ACCEPT_YOUR_CHALLENGE_S1_COME_AND_DIE_IN_THE_ARMS_OF_IMMORTALITY = new NpcStringId(1800261);
		YOU_WILL_PARTICIPATE_IN_S1_S2_SHORTLY_BE_PREPARED_FOR_ANYTHING = new NpcStringId(1800262);
		YOU_CAN_HEAR_THE_UNDEAD_OF_EKIMUS_RUSHING_TOWARD_YOU_S1_S2_IT_HAS_NOW_BEGUN = new NpcStringId(1800263);
		YOU_CAN_FEEL_THE_SURGING_ENERGY_OF_DEATH_FROM_THE_TUMOR = new NpcStringId(1800264);
		THE_AREA_NEAR_THE_TUMOR_IS_FULL_OF_OMINOUS_ENERGY = new NpcStringId(1800265);
		YOU_TRIED_TO_DROP_US_HOW_STUPID = new NpcStringId(1800266);
		WE_ARE_BLOOD_BRETHREN_I_CANT_FALL_SO_EASILY_HERE_AND_LEAVE_MY_BROTHER_BEHIND = new NpcStringId(1800267);
		YOU_WERE_ALWAYS_WHAT_I_ASPIRED_TO_BE_DO_YOU_THINK_I_WOULD_FALL_SO_EASILY_HERE_WHEN_I_HAVE_A_BROTHER_LIKE_THAT =
				new NpcStringId(1800268);
		WITH_ALL_CONNECTIONS_TO_THE_TUMOR_SEVERED_EKIMUS_HAS_LOST_ITS_POWER_TO_CONTROL_THE_FERAL_HOUND =
				new NpcStringId(1800269);
		WITH_THE_CONNECTION_TO_THE_TUMOR_RESTORED_EKIMUS_HAS_REGAINED_CONTROL_OVER_THE_FERAL_HOUND =
				new NpcStringId(1800270);
		WOOOONG = new NpcStringId(1800271);
		WOONG_WOONG_WOO = new NpcStringId(1800272);
		THE_ENEMIES_HAVE_ATTACKED_EVERYONE_COME_OUT_AND_FIGHT_URGH = new NpcStringId(1800273);
		THE_TUMOR_INSIDE_S1_HAS_BEEN_DESTROYED_NIN_ORDER_TO_DRAW_OUT_THE_COWARDLY_COHEMENES_YOU_MUST_DESTROY_ALL_THE_TUMORS =
				new NpcStringId(1800274);
		THE_TUMOR_INSIDE_S1_HAS_COMPLETELY_REVIVED_NTHE_RESTRENGTHENED_COHEMENES_HAS_FLED_DEEPER_INSIDE_THE_SEED =
				new NpcStringId(1800275);
		THE_AWARDED_AIRSHIP_SUMMON_LICENSE_HAS_BEEN_RECEIVED = new NpcStringId(1800276);
		YOU_DO_NOT_CURRENTLY_HAVE_AN_AIRSHIP_SUMMON_LICENSE_YOU_CAN_EARN_YOUR_AIRSHIP_SUMMON_LICENSE_THROUGH_ENGINEER_LEKON =
				new NpcStringId(1800277);
		THE_AIRSHIP_SUMMON_LICENSE_HAS_ALREADY_BEEN_AWARDED = new NpcStringId(1800278);
		IF_YOU_HAVE_ITEMS_PLEASE_GIVE_THEM_TO_ME = new NpcStringId(1800279);
		MY_STOMACH_IS_EMPTY = new NpcStringId(1800280);
		IM_HUNGRY_IM_HUNGRY = new NpcStringId(1800281);
		IM_STILL_NOT_FULL = new NpcStringId(1800282);
		IM_STILL_HUNGRY = new NpcStringId(1800283);
		I_FEEL_A_LITTLE_WOOZY = new NpcStringId(1800284);
		GIVE_ME_SOMETHING_TO_EAT = new NpcStringId(1800285);
		NOW_ITS_TIME_TO_EAT = new NpcStringId(1800286);
		I_ALSO_NEED_A_DESSERT = new NpcStringId(1800287);
		IM_FULL_NOW_I_DONT_WANT_TO_EAT_ANYMORE = new NpcStringId(1800289);
		I_HAVENT_EATEN_ANYTHING_IM_SO_WEAK = new NpcStringId(1800290);
		YUM_YUM_YUM_YUM = new NpcStringId(1800291);
		YOUVE_SUSTAINED_S1_DAMAGE_AS_TUMORS_SHELL_STARTED_MELTING_AFTER_TOUCHING_THE_SACRED_SEAL_ON_THE_SHIELD =
				new NpcStringId(1800292);
		YOUVE_SUSTAINED_S1_DAMAGE_AS_SOUL_COFFINS_SHELL_STARTED_MELTING_AFTER_TOUCHING_THE_SACRED_SEAL_ON_THE_SHIELD =
				new NpcStringId(1800293);
		OBELISK_HAS_COLLAPSED_DONT_LET_THE_ENEMIES_JUMP_AROUND_WILDLY_ANYMORE = new NpcStringId(1800295);
		ENEMIES_ARE_TRYING_TO_DESTROY_THE_FORTRESS_EVERYONE_DEFEND_THE_FORTRESS = new NpcStringId(1800296);
		COME_OUT_WARRIORS_PROTECT_SEED_OF_DESTRUCTION = new NpcStringId(1800297);
		THE_UNDEAD_OF_EKIMUS_IS_ATTACKING_SEED_OF_LIFE_DEFENDING_HALL_OF_EROSION_WILL_FAIL_EVEN_IF_ONE_SEED_OF_LIFE_IS_DESTROYED =
				new NpcStringId(1800298);
		ALL_THE_TUMORS_INSIDE_S1_HAVE_BEEN_DESTROYED_DRIVEN_INTO_A_CORNER_COHEMENES_APPEARS_CLOSE_BY =
				new NpcStringId(1800299);
		THE_TUMOR_INSIDE_S1_HAS_BEEN_DESTROYED_NTHE_NEARBY_UNDEAD_THAT_WERE_ATTACKING_SEED_OF_LIFE_START_LOSING_THEIR_ENERGY_AND_RUN_AWAY =
				new NpcStringId(1800300);
		THE_TUMOR_INSIDE_S1_HAS_COMPLETELY_REVIVED_NRECOVERED_NEARBY_UNDEAD_ARE_SWARMING_TOWARD_SEED_OF_LIFE =
				new NpcStringId(1800301);
		THE_TUMOR_INSIDE_S1_THAT_HAS_PROVIDED_ENERGY_N_TO_EKIMUS_IS_DESTROYED = new NpcStringId(1800302);
		THE_TUMOR_INSIDE_S1_HAS_BEEN_COMPLETELY_RESURRECTED_N_AND_STARTED_TO_ENERGIZE_EKIMUS_AGAIN =
				new NpcStringId(1800303);
		THE_TUMOR_INSIDE_S1_HAS_BEEN_DESTROYED_NTHE_SPEED_THAT_EKIMUS_CALLS_OUT_HIS_PREY_HAS_SLOWED_DOWN =
				new NpcStringId(1800304);
		THE_TUMOR_INSIDE_S1_HAS_COMPLETELY_REVIVED_NEKIMUS_STARTED_TO_REGAIN_HIS_ENERGY_AND_IS_DESPERATELY_LOOKING_FOR_HIS_PREY =
				new NpcStringId(1800305);
		BRING_MORE_MORE_SOULS = new NpcStringId(1800306);
		THE_HALL_OF_EROSION_ATTACK_WILL_FAIL_UNLESS_YOU_MAKE_A_QUICK_ATTACK_AGAINST_THE_TUMOR =
				new NpcStringId(1800307);
		AS_THE_TUMOR_WAS_NOT_THREATENED_COHEMENES_COMPLETELY_RAN_AWAY_TO_DEEP_INSIDE_THE_SEED =
				new NpcStringId(1800308);
		YOUR_GOAL_WILL_BE_OBSTRUCTED_OR_BE_UNDER_A_RESTRAINT = new NpcStringId(1800309);
		YOU_MAY_FACE_AN_UNFORESEEN_PROBLEM_ON_YOUR_COURSE_TOWARD_THE_GOAL = new NpcStringId(1800310);
		YOU_MAY_FEEL_NERVOUS_AND_ANXIOUS_BECAUSE_OF_UNFAVORABLE_SITUATIONS = new NpcStringId(1800311);
		BE_WARNED_WHEN_THE_SITUATION_IS_DIFFICULT_BECAUSE_YOU_MAY_LOSE_YOUR_JUDGMENT_AND_MAKE_AN_IRRATIONAL_MISTAKE =
				new NpcStringId(1800312);
		YOU_MAY_MEET_A_TRUSTWORTHY_PERSON_OR_A_GOOD_OPPORTUNITY = new NpcStringId(1800313);
		YOUR_DOWNWARD_LIFE_STARTS_TAKING_AN_UPTURN = new NpcStringId(1800314);
		YOU_WILL_ATTRACT_ATTENTION_FROM_PEOPLE_WITH_YOUR_POPULARITY = new NpcStringId(1800315);
		YOUR_STAR_OF_FORTUNE_SAYS_THERELL_BE_FISH_SNAPPING_AT_YOUR_BAIT = new NpcStringId(1800316);
		THERE_MAY_BE_A_CONFLICT_BORN_OF_YOUR_DOGMATIC_PROCEDURES = new NpcStringId(1800317);
		YOUR_WISDOM_AND_CREATIVITY_MAY_LEAD_YOU_TO_SUCCESS_WITH_YOUR_GOAL = new NpcStringId(1800318);
		YOU_MAY_ACCOMPLISH_YOUR_GOALS_IF_YOU_DILIGENTLY_PURSUE_THEM_WITHOUT_GIVING_UP = new NpcStringId(1800319);
		YOU_MAY_GET_HELP_IF_YOU_GO_THROUGH_THE_FRONT_DOOR_WITHOUT_SEEKING_TRICKS_OR_MANEUVERS =
				new NpcStringId(1800320);
		A_GOOD_RESULT_IS_ON_THE_WAY_IF_YOU_SET_A_GOAL_AND_BRAVELY_PROCEED_TOWARD_IT = new NpcStringId(1800321);
		EVERYTHING_WILL_BE_SMOOTHLY_MANAGED_NO_MATTER_HOW_DIFFICULT = new NpcStringId(1800322);
		BE_FIRM_AND_CAREFULLY_SCRUTINIZE_CIRCUMSTANCES_EVEN_WHEN_THINGS_ARE_DIFFICULT = new NpcStringId(1800323);
		ALWAYS_THINK_OVER_TO_FIND_NEGLECTED_PROBLEMS_YOU_HAVENT_TAKEN_CARE_OF_YET = new NpcStringId(1800324);
		FINANCIAL_FORTUNE_WILL_GREET_YOUR_POOR_LIFE = new NpcStringId(1800325);
		YOU_MAY_ACQUIRE_WEALTH_AND_FAME_AFTER_UNLUCKY_CIRCUMSTANCES = new NpcStringId(1800326);
		THE_DIFFICULT_SITUATIONS_WILL_TURN_TO_HOPE_WITH_UNFORESEEN_HELP = new NpcStringId(1800327);
		A_GREAT_TASK_WILL_RESULT_IN_SUCCESS = new NpcStringId(1800328);
		YOU_MAY_ENCOUNTER_A_PRECIOUS_PERSON_WHO_WILL_LIFT_YOUR_LONELINESS_AND_DISCORD = new NpcStringId(1800329);
		PEOPLE_AROUND_YOU_WILL_ENCOURAGE_YOUR_EVERY_TASK_IN_THE_FUTURE = new NpcStringId(1800330);
		EVERYTHING_WILL_BE_SMOOTHLY_MANAGED = new NpcStringId(1800331);
		YOU_WILL_MEET_A_PERSON_WHO_CAN_CHERISH_YOUR_VALUES_IF_YOU_MAINTAIN_GOOD_TIES_WITH_PEOPLE =
				new NpcStringId(1800332);
		MAINTAIN_COOPERATIVE_ATTITUDE_SINCE_YOU_WILL_MEET_SOMEONE_TO_BE_OF_HELP = new NpcStringId(1800333);
		KEEP_YOUR_MODERATION_AND_EGO_IN_CHECK_EVEN_IN_SUCCESSFUL_PHASES_OF_YOUR_LIFE = new NpcStringId(1800334);
		WHEN_IT_COMES_TO_WORK_LIFESTYLE_AND_RELATIONSHIPS_YOULL_BE_BETTER_OFF_TO_GO_BY_THE_TEXT_RATHER_THAN_TRICKS =
				new NpcStringId(1800335);
		YOUR_TASK_WILL_RECEIVE_SUBSTANTIAL_SUPPORT_SINCE_THE_SURROUNDINGS_WILL_FULLY_DEVELOP = new NpcStringId(1800336);
		YOUR_STAR_OF_FORTUNE_INDICATE_A_SUCCESS_WITH_MENTAL_AND_MATERIAL_ASSISTANCE = new NpcStringId(1800337);
		YOU_WILL_ENJOY_POPULARITY_WITH_YOUR_CREATIVE_TALENTS_AND_CLEVER_ACTS = new NpcStringId(1800338);
		PEOPLE_WILL_LINE_UP_TO_BE_OF_ASSISTANCE_TO_YOU = new NpcStringId(1800339);
		YOU_MAY_MEET_SOMEONE_TO_SHARE_YOUR_JOURNEY = new NpcStringId(1800340);
		YOU_MAY_ACHIEVE_CONNECTIONS_IN_MANY_FIELDS = new NpcStringId(1800341);
		AN_ATTITUDE_THAT_CONTINUALLY_STUDIES_AND_EXPLORES_IS_NEEDED_AND_ALWAYS_BE_SINCERE = new NpcStringId(1800342);
		ITS_AN_IMAGE_OF_A_BUTTERFLY_ON_A_FLOWER_IN_WARM_SPRING_AIR = new NpcStringId(1800343);
		YOUR_GOALS_WILL_MOVE_SMOOTHLY_WITH_PEACE_AND_HAPPINESS_IN_YOUR_LIFE = new NpcStringId(1800344);
		LOVE_MAY_SPROUT_ITS_LEAVES_WHEN_YOU_TREAT_THOSE_AROUND_YOU_WITH_CARE = new NpcStringId(1800345);
		YOU_MAY_CLIMB_INTO_A_HIGHER_POSITION_WITH_OTHERS_TRUST_IF_YOU_FAITHFULLY_CARRY_OUT_YOUR_DUTIES =
				new NpcStringId(1800346);
		EVERYTHING_CAN_FALL_APART_IF_YOU_GREEDILY_AIM_BY_PURE_LUCK = new NpcStringId(1800347);
		DO_NOT_UNDERESTIMATE_THE_IMPORTANCE_OF_MEETING_PEOPLE = new NpcStringId(1800348);
		AN_ARROW_WILL_COALESCE_INTO_THE_BOW = new NpcStringId(1800349);
		A_BONY_LIMB_OF_A_TREE_MAY_BEAR_ITS_FRUIT = new NpcStringId(1800350);
		YOU_WILL_BE_REWARDED_FOR_YOUR_EFFORTS_AND_ACCOMPLISHMENTS = new NpcStringId(1800351);
		NO_MATTER_WHERE_IT_LIES_YOUR_FAITHFUL_DRIVE_LEADS_YOU_TO_SUCCESS = new NpcStringId(1800352);
		PEOPLE_WILL_BE_ATTRACTED_TO_YOUR_LOYALTIES = new NpcStringId(1800353);
		YOU_MAY_TRUST_YOURSELF_RATHER_THAN_OTHERS_TALKS = new NpcStringId(1800354);
		CREATIVE_THINKING_AWAY_FROM_THE_OLD_VIEWPOINT_MAY_HELP_YOU = new NpcStringId(1800355);
		PATIENCE_WITHOUT_BEING_IMPETUOUS_OF_THE_RESULTS_WILL_ONLY_BEAR_A_POSITIVE_OUTCOME = new NpcStringId(1800356);
		THE_DEAD_WILL_COME_ALIVE = new NpcStringId(1800357);
		THERE_WILL_BE_A_SHOCKING_INCIDENT = new NpcStringId(1800358);
		YOU_WILL_ENJOY_A_HUGE_SUCCESS_AFTER_UNFORESEEN_LUCK_COMES_BEFORE_YOU = new NpcStringId(1800359);
		DO_NOT_GIVE_UP_SINCE_THERE_MAY_BE_A_MIRACULOUS_RESCUE_FROM_THE_COURSE_OF_DESPAIR = new NpcStringId(1800360);
		AN_ATTITUDE_TO_TRY_ONES_BEST_TO_PURSUE_THE_GOAL_IS_NEEDED = new NpcStringId(1800361);
		YOU_MAY_GET_A_SHOT_IN_THE_ARM_IN_YOUR_LIFE_AFTER_MEETING_A_GOOD_PERSON = new NpcStringId(1800362);
		YOU_MAY_GET_A_BIG_HELP_IN_THE_COURSE_OF_YOUR_LIFE = new NpcStringId(1800363);
		A_RARE_OPPORTUNITY_WILL_COME_TO_YOU_SO_YOU_MAY_PROSPER = new NpcStringId(1800364);
		A_HUNGRY_FALCON_WILL_HAVE_MEAT = new NpcStringId(1800365);
		A_HOUSEHOLD_IN_NEED_WILL_ACQUIRE_A_FORTUNE_AND_MEAT = new NpcStringId(1800366);
		A_HARD_SITUATION_WILL_COME_TO_ITS_END_WITH_MATERIALISTIC_AND_MENTAL_HELP_FROM_OTHERS = new NpcStringId(1800367);
		IF_YOU_SET_A_FIRM_GOAL_WITHOUT_SURRENDER_THERE_WILL_BE_A_PERSON_WHO_CAN_OFFER_HELP_AND_CARE =
				new NpcStringId(1800368);
		YOULL_GAIN_OTHERS_TRUST_WHEN_YOU_MAINTAIN_A_SINCERE_AND_HONEST_ATTITUDE = new NpcStringId(1800369);
		BE_INDEPENDENT_AT_ALL_TIMES = new NpcStringId(1800370);
		ITS_A_WAGON_WITH_NO_WHEELS = new NpcStringId(1800371);
		YOUVE_SET_A_GOAL_BUT_THERE_MAY_BE_OBSTACLES_IN_REALITY = new NpcStringId(1800372);
		YOURE_RUNNING_TOWARD_THE_GOAL_BUT_THERE_WONT_BE_AS_MANY_OUTCOMES_AS_YOU_THOUGHT = new NpcStringId(1800373);
		THERE_ARE_MANY_THINGS_TO_CONSIDER_AFTER_ENCOUNTERING_HINDRANCES = new NpcStringId(1800374);
		A_RECKLESS_MOVE_MAY_BRING_A_FAILURE = new NpcStringId(1800375);
		YOU_MAY_LOSE_PEOPLES_TRUST_IF_YOU_LACK_PRUDENCE_AT_ALL_TIMES = new NpcStringId(1800376);
		YOU_MAY_NEED_TO_REFLECT_ON_YOURSELF_WITH_DELIBERATION_AND_WAIT_FOR_AN_OPPORTUNITY = new NpcStringId(1800377);
		A_POOR_SCHOLAR_RECEIVES_A_STIPEND = new NpcStringId(1800378);
		A_SCHOLAR_GETS_A_PASS_TOWARD_FAME_AND_FORTUNE = new NpcStringId(1800379);
		YOUR_AMBITION_AND_DREAM_WILL_COME_TRUE = new NpcStringId(1800380);
		COMPLICATED_PROBLEMS_AROUND_YOU_MAY_START_BEING_SOLVED_ONE_AFTER_ANOTHER = new NpcStringId(1800381);
		YOU_WILL_HAVE_A_GOOD_RESULT_IF_YOU_DILIGENTLY_PURSUE_ONE_GOAL_WITHOUT_BEING_DRAGGED_FROM_YOUR_PAST =
				new NpcStringId(1800382);
		YOU_MAY_NEED_TO_RID_YOURSELF_OF_OLD_AND_WORN_HABITS = new NpcStringId(1800383);
		BE_RESPONSIBLE_WITH_YOUR_TASKS_BUT_DO_NOT_HESITATE_TO_ASK_FOR_COLLEAGUES_HELP = new NpcStringId(1800384);
		FISH_TRANSFORMS_INTO_A_DRAGON = new NpcStringId(1800385);
		YOUR_DREAM_MAY_COME_TRUE_AND_FAME_AND_FORTUNE_WILL_COME_TO_YOU = new NpcStringId(1800386);
		WHAT_YOUVE_PLANED_WILL_BE_ACCOMPLISHED = new NpcStringId(1800387);
		YOU_MAY_ACQUIRE_MONEY_OR_A_NEW_OPPORTUNITY_FROM_A_PLACE_YOU_WOULDNT_HAVE_THOUGHT_OF = new NpcStringId(1800388);
		THERE_WILL_BE_MANY_OFFERS_TO_YOU_YOU_MAY_THINK_THEM_OVER_CAREFULLY = new NpcStringId(1800389);
		IT_MAY_BE_A_GOOD_IDEA_NOT_TO_BECOME_INVOLVED_IN_OTHERS_BUSINESS = new NpcStringId(1800390);
		EVERYTHING_WILL_GO_SMOOTHLY_BUT_BE_AWARE_OF_DANGER_FROM_BEING_CARELESS_AND_REMISS = new NpcStringId(1800391);
		IF_YOU_SINCERELY_CARE_FOR_SOMEONE_YOU_LOVE_A_BIG_REWARD_WILL_RETURN_TO_YOU = new NpcStringId(1800392);
		A_REMEDY_IS_ON_ITS_WAY_FOR_A_SERIOUS_ILLNESS = new NpcStringId(1800393);
		YOU_MAY_ACQUIRE_A_PRECIOUS_MEDICINE_TO_RECOVER_AFTER_SUFFERING_A_DISEASE_OF_A_SERIOUS_NATURE =
				new NpcStringId(1800394);
		YOU_MAY_REALIZE_YOUR_DREAM_BY_MEETING_A_MAN_OF_DISTINCTION_AT_A_DIFFICULT_TIME = new NpcStringId(1800395);
		YOU_MAY_SUFFER_ONE_OR_TWO_HARDSHIPS_ON_YOUR_JOURNEY = new NpcStringId(1800396);
		IF_YOU_KEEP_SMILING_WITHOUT_DESPAIR_PEOPLE_WILL_COME_TO_TRUST_YOU_AND_OFFER_HELP = new NpcStringId(1800397);
		SEEK_STABILITY_RATHER_THAN_DYNAMICS_IN_YOUR_LIFE = new NpcStringId(1800398);
		ITS_A_GOOD_IDEA_TO_BE_CAREFUL_AND_SECURE_AT_ALL_TIMES = new NpcStringId(1800399);
		YOU_CANT_PERFORM_THE_JOB_WITH_BOUND_HANDS = new NpcStringId(1800400);
		YOU_MAY_LOSE_YOUR_DRIVE_AND_FEEL_LOST = new NpcStringId(1800401);
		YOU_MAY_BE_UNABLE_TO_CONCENTRATE_WITH_SO_MANY_PROBLEMS_OCCURRING = new NpcStringId(1800402);
		YOUR_ACHIEVEMENT_UNFAIRLY_MAY_GO_SOMEWHERE_ELSE = new NpcStringId(1800403);
		DO_NOT_START_A_TASK_THATS_NOT_CLEAR_TO_YOU = new NpcStringId(1800404);
		YOU_WILL_NEED_TO_BE_PREPARED_FOR_ALL_EVENTS = new NpcStringId(1800405);
		SOMEONE_WILL_ACKNOWLEDGE_YOU_IF_YOU_RELENTLESSLY_KEEP_TRYING_AND_DO_NOT_GIVE_UP_WHEN_FACING_HARDSHIPS =
				new NpcStringId(1800406);
		YOU_MAY_PERFECT_YOURSELF_LIKE_A_DRAGONS_HORN_DECORATES_THE_DRAGON = new NpcStringId(1800407);
		YOUR_TRUE_VALUE_STARTS_TO_SHINE = new NpcStringId(1800408);
		YOUR_STEADY_PURSUIT_OF_NEW_INFORMATION_AND_STAYING_AHEAD_OF_OTHERS_WILL_RAISE_YOUR_VALUE =
				new NpcStringId(1800409);
		MAINTAINING_CONFIDENCE_WITH_WORK_OR_RELATIONSHIPS_MAY_BRING_GOOD_RESULTS = new NpcStringId(1800410);
		LEARN_TO_WORK_WITH_OTHERS_SINCE_OVERCONFIDENCE_WILL_BEAR_WRATH = new NpcStringId(1800411);
		THE_DRAGON_NOW_ACQUIRES_AN_EAGLES_WINGS = new NpcStringId(1800412);
		AS_THE_DRAGON_FLIES_HIGH_IN_THE_SKY_YOUR_GOALS_AND_DREAMS_MAY_COME_TRUE = new NpcStringId(1800413);
		LUCK_ENTERS_INTO_YOUR_WORK_HOBBY_FAMILY_AND_LOVE = new NpcStringId(1800414);
		WHATEVER_YOU_DO_IT_WILL_ACCOMPANY_WINNING = new NpcStringId(1800415);
		ITS_AS_GOOD_AS_IT_GETS_WITH_UNFORESEEN_FORTUNE_ROLLING_YOUR_WAY = new NpcStringId(1800416);
		A_GREEDY_ACT_WITH_NO_PRUDENCE_WILL_BRING_A_SURPRISE_AT_THE_END = new NpcStringId(1800417);
		THINK_CAREFULLY_AND_ACT_WITH_CAUTION_AT_ALL_TIMES = new NpcStringId(1800418);
		IF_A_TREE_DOESNT_HAVE_ITS_ROOTS_THERE_WILL_BE_NO_FRUIT = new NpcStringId(1800419);
		HARD_WORK_DOESNT_BEAR_FRUIT = new NpcStringId(1800420);
		FINANCIAL_DIFFICULTIES_MAY_BRING_AN_ORDEAL = new NpcStringId(1800421);
		WHAT_USED_TO_BE_WELL_MANAGED_MAY_STUMBLE_ONE_AFTER_ANOTHER = new NpcStringId(1800422);
		A_FEELING_OF_FRUSTRATION_MAY_FOLLOW_DISAPPOINTMENT = new NpcStringId(1800423);
		BE_CAUTIONED_AS_UNHARNESSED_BEHAVIOR_AT_DIFFICULT_TIMES_CAN_RUIN_RELATIONSHIPS = new NpcStringId(1800424);
		CURTAIL_GREED_AND_BE_GRATEFUL_FOR_SMALL_RETURNS_AS_MODESTY_IS_NEEDED = new NpcStringId(1800425);
		THE_PERSON_THAT_CAME_UNDER_YOUR_WINGS_WILL_LEAVE = new NpcStringId(1800426);
		YOUR_WORK_AND_RELATIONSHIP_WITH_COLLEAGUES_WILL_BE_WELL_MANAGED_IF_YOU_MAINTAIN_YOUR_DEVOTION =
				new NpcStringId(1800427);
		CALCULATING_YOUR_PROFIT_IN_RELATIONSHIPS_WITHOUT_DISPLAYING_ANY_COURTEOUS_MANNERS_WILL_BRING_MALICIOUS_GOSSIP_AND_RUIN_YOUR_VALUE =
				new NpcStringId(1800428);
		CONSIDER_OTHERS_SITUATIONS_AND_TREAT_THEM_SINCERELY_AT_ALL_TIMES = new NpcStringId(1800429);
		DO_NOT_LOOSEN_UP_WITH_YOUR_PRECAUTIONS = new NpcStringId(1800430);
		REFLECT_OTHERS_OPINIONS_AS_A_MISTAKE_ALWAYS_LIES_AHEAD_OF_AN_ARBITRARY_DECISION = new NpcStringId(1800431);
		A_BLIND_MAN_GOES_RIGHT_THROUGH_THE_DOOR = new NpcStringId(1800432);
		A_HEART_FALLS_INTO_HOPELESSNESS_AS_THINGS_ARE_IN_DISARRAY = new NpcStringId(1800433);
		HOPELESSNESS_MAY_FILL_YOUR_HEART_AS_YOUR_WORK_FALLS_INTO_A_MAZE = new NpcStringId(1800434);
		DIFFICULTIES_LIE_AHEAD_OF_AN_UNFORESEEN_PROBLEM_EVEN_WITH_YOUR_HARD_WORK = new NpcStringId(1800435);
		THERE_MAY_BE_MORE_OCCASIONS_YOU_WILL_WANT_TO_ASK_FAVORS_FROM_OTHERS_AS_YOU_LOSE_CONFIDENCE_IN_YOU =
				new NpcStringId(1800436);
		BE_BRAVE_AND_AMBITIOUS_AS_NO_BIRD_CAN_FLY_INTO_THE_SKY_BY_STAYING_IN_THEIR_NEST = new NpcStringId(1800437);
		ITS_A_GOOD_IDEA_NOT_TO_START_AN_UNCLEAR_TASK_AND_ALWAYS_LOOK_FOR_SOMEONE_YOU_CAN_TRUST_AND_RELY_UPON =
				new NpcStringId(1800438);
		HUNTING_WONT_BE_SUCCESSFUL_AS_THE_FALCON_LACKS_ITS_CLAWS = new NpcStringId(1800439);
		A_PREPARED_PLAN_WONT_MOVE_SMOOTHLY = new NpcStringId(1800440);
		AN_EASY_TASK_MAY_FAIL_IF_ONE_IS_CONSUMED_BY_GREED_AND_OVERCONFIDENCE = new NpcStringId(1800441);
		IMPATIENCE_MAY_LIE_AHEAD_AS_THE_SITUATION_IS_UNFAVORABLE = new NpcStringId(1800442);
		THOUGHTFUL_FORESIGHT_IS_NEEDED_BEFORE_A_DISASTER_MAY_FALL_UPON_YOU = new NpcStringId(1800443);
		REFRAIN_FROM_DICTATORIAL_ACTS_AS_CARING_FOR_OTHERS_AROUND_YOU_WITH_DIGNITY_IS_MUCH_NEEDED =
				new NpcStringId(1800444);
		THINGS_ARE_MESSY_WITH_NO_GOOD_SIGN = new NpcStringId(1800445);
		YOU_MAY_FALL_INTO_A_VEXING_SITUATION_AS_BAD_CIRCUMSTANCES_WILL_ARISE = new NpcStringId(1800446);
		RELATIONSHIPS_WITH_PEOPLE_MAY_BE_CONTRARY_TO_YOUR_EXPECTATIONS = new NpcStringId(1800447);
		DO_NOT_SEEK_A_QUICK_FIX_AS_THE_PROBLEM_NEEDS_A_FUNDAMENTAL_RESOLUTION = new NpcStringId(1800448);
		SEEK_PEACE_IN_YOUR_HEART_AS_VULGAR_DISPLAY_OF_YOUR_EMOTIONS_MAY_HARM_YOU = new NpcStringId(1800449);
		INFORMATION_FOR_SUCCESS_MAY_COME_FROM_THE_CONVERSATIONS_WITH_PEOPLE_AROUND_YOU = new NpcStringId(1800450);
		BE_CONFIDENT_AND_ACT_RELIANTLY_AT_ALL_TIMES = new NpcStringId(1800451);
		A_CHILD_GETS_A_TREASURE = new NpcStringId(1800452);
		GOOD_FORTUNE_AND_OPPORTUNITY_MAY_LIE_AHEAD_AS_IF_ONES_BORN_IN_A_GOLDEN_SPOON = new NpcStringId(1800453);
		YOUR_LIFE_FLOWS_AS_IF_ITS_ON_A_SILK_SURFACE_AND_UNEXPECTED_FORTUNE_AND_SUCCESS_MAY_COME_TO_YOU =
				new NpcStringId(1800454);
		TEMPORARY_LUCK_MAY_COME_TO_YOU_WITH_NO_EFFORT = new NpcStringId(1800455);
		PLAN_AHEAD_WITH_PATIENCE_BUT_EXECUTE_WITH_SWIFTNESS = new NpcStringId(1800456);
		THE_ABILITIES_TO_AMEND_FORESEE_AND_ANALYZE_MAY_RAISE_YOUR_VALUE = new NpcStringId(1800457);
		BIGGER_MISTAKES_WILL_BE_ON_THE_ROAD_IF_YOU_FAIL_TO_CORRECT_A_SMALL_MISTAKE = new NpcStringId(1800458);
		DONT_BE_EVASIVE_TO_ACCEPT_NEW_FINDINGS_OR_EXPERIENCES = new NpcStringId(1800459);
		DONT_BE_IRRITATED_AS_THE_SITUATIONS_DONT_MOVE_AS_PLANNED = new NpcStringId(1800460);
		BE_WARNED_AS_YOU_MAY_BE_OVERWHELMED_BY_SURROUNDINGS_IF_YOU_LACK_A_CLEAR_OPINION = new NpcStringId(1800461);
		YOU_MAY_LIVE_AN_AFFLUENT_LIFE_EVEN_WITHOUT_POSSESSIONS = new NpcStringId(1800462);
		YOU_WILL_GAIN_POPULARITY_AS_YOU_HELP_PEOPLE_WITH_MONEY_YOU_EARNESTLY_EARNED = new NpcStringId(1800463);
		YOUR_HEART_AND_BODY_MAY_BE_IN_HEALTH = new NpcStringId(1800464);
		BE_WARNED_AS_YOU_MAY_BE_DRAGGED_TO_AN_UNWANTED_DIRECTION_IF_NOT_CAUTIOUS = new NpcStringId(1800465);
		YOU_MAY_MEET_MANY_NEW_PEOPLE_BUT_IT_WILL_BE_DIFFICULT_TO_FIND_A_PERFECT_PERSON_WHO_WINS_YOUR_HEART =
				new NpcStringId(1800466);
		THERE_MAY_BE_AN_OCCASION_WHERE_YOU_ARE_CONSOLED_BY_PEOPLE = new NpcStringId(1800467);
		IT_MAY_NOT_BE_A_GOOD_TIME_FOR_A_CHANGE_EVEN_IF_THERES_TEDIUM_IN_DAILY_LIFE = new NpcStringId(1800468);
		THE_MONEY_YOU_SPEND_FOR_YOURSELF_MAY_ACT_AS_AN_INVESTMENT_AND_BRING_YOU_A_RETURN = new NpcStringId(1800469);
		THE_MONEY_YOU_SPEND_FOR_OTHERS_WILL_BE_WASTED_SO_BE_CAUTIOUS = new NpcStringId(1800470);
		BE_WARNED_SO_AS_NOT_TO_HAVE_UNNECESSARY_EXPENSES = new NpcStringId(1800471);
		YOUR_STAR_INDICATED_SUCH_GOOD_LUCK_PARTICIPATE_IN_BONUS_GIVEAWAYS_OR_EVENTS = new NpcStringId(1800472);
		YOU_MAY_GRAB_UNEXPECTED_LUCK = new NpcStringId(1800473);
		THE_PERSON_IN_YOUR_HEART_MAY_NATURALLY_COME_TO_YOU = new NpcStringId(1800474);
		THERE_WILL_BE_A_GOOD_RESULT_IF_YOU_KEEP_YOUR_OWN_PACE_REGARDLESS_OF_OTHERS_JUDGMENT = new NpcStringId(1800475);
		BE_WARNED_AS_UNEXPECTED_LUCK_MAY_BE_WASTED_WITH_YOUR_RECKLESS_COMMENTS = new NpcStringId(1800476);
		OVERCONFIDENCE_WILL_CONVINCE_YOU_TO_CARRY_A_TASK_ABOVE_YOUR_REACH_AND_THERE_WILL_BE_CONSEQUENCES =
				new NpcStringId(1800477);
		MOMENTARILY_DELAY_AN_IMPORTANT_DECISION = new NpcStringId(1800478);
		TROUBLE_SPOTS_LIE_AHEAD_WHEN_TALKING_TO_SUPERIORS_OR_PEOPLE_CLOSE_TO_YOU = new NpcStringId(1800479);
		BE_WARNED_AS_YOUR_WORDS_CAN_HURT_OTHERS_OR_OTHERS_WORDS_CAN_HURT_YOU = new NpcStringId(1800480);
		MAKE_A_LOUD_BOAST_AND_YOU_MAY_HAVE_TO_PAY_TO_COVER_UNNECESSARY_EXPENSES = new NpcStringId(1800481);
		SKILLFUL_EVASION_IS_NEEDED_WHEN_DEALING_WITH_PEOPLE_WHO_PICK_FIGHTS_AS_A_DISASTER_MAY_ARISE_FROM_IT =
				new NpcStringId(1800482);
		KEEP_A_LOW_PROFILE_AS_TOO_STRONG_AN_OPINION_WILL_ATTRACT_ADVERSE_REACTIONS = new NpcStringId(1800483);
		DO_NOT_UNNECESSARILY_PROVOKE_MISUNDERSTANDING_AS_YOU_MAY_BE_INVOLVED_IN_MALICIOUS_GOSSIP =
				new NpcStringId(1800484);
		CHECK_YOUR_BELONGINGS_AS_YOU_MAY_LOSE_WHAT_YOU_POSSESS = new NpcStringId(1800485);
		BE_FLEXIBLE_ENOUGH_TO_PLAY_UP_TO_OTHERS = new NpcStringId(1800486);
		PAY_SPECIAL_ATTENTION_WHEN_MEETING_OR_TALKING_TO_PEOPLE_AS_RELATIONSHIPS_MAY_GO_AMISS =
				new NpcStringId(1800487);
		WHEN_THE_IMPORTANT_MOMENT_ARRIVES_DECIDE_UPON_WHAT_YOU_TRULY_WANT_WITHOUT_MEASURING_OTHERS_JUDGMENT =
				new NpcStringId(1800488);
		LUCK_WILL_ALWAYS_FOLLOW_YOU_IF_YOU_TRAVEL_AND_READ_MANY_BOOKS = new NpcStringId(1800489);
		HEAD_TO_A_PLACE_THAT_NEEDS_YOUR_ADVICE_AS_GOOD_IDEAS_AND_WISDOM_WILL_FLOURISH = new NpcStringId(1800490);
		SOMEONES_LIFE_MAY_CHANGE_UPON_YOUR_ADVICE = new NpcStringId(1800491);
		ITS_A_PROPER_TIME_TO_PLAN_FOR_THE_FUTURE_RATHER_THAN_A_SHORT_TERM_PLAN = new NpcStringId(1800492);
		MANY_THOUGHTFUL_PLANS_AT_PRESENT_TIME_WILL_BE_OF_GREAT_HELP_IN_THE_FUTURE = new NpcStringId(1800493);
		PATIENCE_MAY_BE_NEEDED_AS_A_BIG_QUARREL_ARISES_BETWEEN_YOU_AND_A_PERSON_CLOSE_TO_YOU = new NpcStringId(1800494);
		DO_NOT_ASK_FOR_FINANCIAL_HELP_WHEN_THE_TIME_IS_DIFFICULT_YOUR_PRIDE_WILL_BE_HURT_WITHOUT_GAINING_ANY_MONEY =
				new NpcStringId(1800495);
		CONNECTION_WITH_A_SPECIAL_PERSON_STARTS_WITH_A_MERE_INCIDENT = new NpcStringId(1800496);
		STUBBORNNESS_REGARDLESS_OF_THE_MATTER_WILL_ONLY_BEAR_DANGER = new NpcStringId(1800497);
		KEEP_GOOD_MANNERS_AND_VALUE_TACITURNITY_AS_LIGHT_HEARTEDNESS_MAY_BRING_MISFORTUNE = new NpcStringId(1800498);
		YOU_MAY_MEET_THE_OPPOSITE_SEX = new NpcStringId(1800499);
		GREED_BY_WANTING_TO_TAKE_WEALTH_MAY_BRING_UNFORTUNATE_DISASTER = new NpcStringId(1800500);
		LOSS_IS_AHEAD_REFRAIN_FROM_INVESTING_TRY_TO_SAVE_THE_MONEY_IN_YOUR_POCKETS = new NpcStringId(1800501);
		YOUR_WEALTH_LUCK_IS_DIM_AVOID_ANY_OFFERS = new NpcStringId(1800502);
		A_BIGGER_CHALLENGE_MAY_BE_WHEN_DELAYING_TODAYS_WORK = new NpcStringId(1800503);
		THERE_WILL_BE_DIFFICULTY_BUT_A_GOOD_RESULT_MAY_BE_AHEAD_WHEN_FACING_IT_RESPONSIBLY = new NpcStringId(1800504);
		EVEN_WITH_SOME_DIFFICULTIES_EXPAND_THE_RANGE_OF_YOUR_SCOPE_WHERE_YOU_ARE_IN_CHARGE_IT_WILL_RETURN_TO_YOU_AS_HELP =
				new NpcStringId(1800505);
		FOCUS_ON_MAINTAINING_ORGANIZED_SURROUNDINGS_TO_HELP_REDUCE_YOUR_LOSSES = new NpcStringId(1800506);
		LUCK_LIES_AHEAD_WHEN_WAITING_FOR_PEOPLE_RATHER_THAN_FOLLOWING_THEM = new NpcStringId(1800507);
		DO_NOT_OFFER_YOUR_HAND_FIRST_EVEN_WHEN_THINGS_ARE_HASTY_THE_RELATIONSHIP_MAY_FALL_APART =
				new NpcStringId(1800508);
		YOUR_WEALTH_LUCK_IS_RISING_THERE_WILL_BE_SOME_GOOD_RESULT = new NpcStringId(1800509);
		YOU_MAY_FALL_IN_DANGER_EACH_TIME_WHEN_ACTING_UPON_IMPROVISATION = new NpcStringId(1800510);
		BE_WARNED_AS_A_CHILDISHLY_ACT_BEFORE_ELDERS_MAY_RUIN_EVERYTHING = new NpcStringId(1800511);
		THINGS_WILL_MOVE_EFFORTLESSLY_BUT_LUCK_WILL_VANISH_WITH_YOUR_AUDACITY = new NpcStringId(1800512);
		LUCK_MAY_BE_CONTINUED_ONLY_WHEN_HUMILITY_IS_MAINTAINED_AFTER_SUCCESS = new NpcStringId(1800513);
		A_NEW_PERSON_MAY_APPEAR_TO_CREATE_A_LOVE_TRIANGLE = new NpcStringId(1800514);
		LOOK_FOR_SOMEONE_WITH_A_SIMILAR_STYLE_IT_WILL_OPEN_UP_FOR_THE_GOOD = new NpcStringId(1800515);
		AN_OFFER_MAY_SOON_BE_MADE_TO_COLLABORATE_A_TASK_BUT_DELAYING_IT_WILL_BE_A_GOOD_IDEA = new NpcStringId(1800516);
		PARTNERSHIP_IS_OUT_OF_LUCK_AVOID_SOMEONE_WHO_RUSHES_YOU_TO_START_A_COLLABORATION = new NpcStringId(1800517);
		FOCUS_ON_NETWORKING_WITH_LIKE_MINDED_PEOPLE_THEY_MAY_JOIN_YOU_FOR_A_BIG_MISSION_IN_THE_FUTURE =
				new NpcStringId(1800518);
		BE_WARNED_WHEN_SOMEONE_SAYS_YOU_ARE_INNOCENT_AS_THATS_NOT_A_COMPLIMENT = new NpcStringId(1800519);
		YOU_MAY_BE_SCAMMED_BE_CAUTIOUS_AS_THERE_MAY_BE_A_BIG_LOSS_BY_UNDERESTIMATING_OTHERS = new NpcStringId(1800520);
		LUCK_AT_DECISION_MAKING_IS_DIM_AVOID_SUBJECTIVE_CONCLUSIONS_AND_RELY_ON_UNIVERSAL_COMMON_SENSE =
				new NpcStringId(1800521);
		YOUR_WEAKNESS_MAY_INVITE_HARDSHIPS_CAUTIOUSLY_TAKE_A_STRONG_POSITION_AS_NEEDED = new NpcStringId(1800522);
		BE_WARY_OF_SOMEONE_WHO_TALKS_AND_ENTERTAINS_TOO_MUCH_THE_PERSON_MAY_BRING_YOU_MISFORTUNE =
				new NpcStringId(1800523);
		YOU_MAY_ENJOY_A_BEGINNERS_LUCK = new NpcStringId(1800524);
		YOUR_WEALTH_LUCK_IS_STRONG_BUT_YOU_SHOULD_KNOW_WHEN_TO_WITHDRAW = new NpcStringId(1800525);
		ALREADY_ACQUIRED_WEALTH_CAN_BE_LOST_BY_GREED = new NpcStringId(1800526);
		EVEN_IF_YOU_CAN_COMPLETE_IT_BY_YOURSELF_ITS_A_GOOD_IDEA_TO_HAVE_SOMEONE_HELP_YOU = new NpcStringId(1800527);
		MAKE_HARMONY_WITH_PEOPLE_THE_PRIORITY_STUBBORNNESS_MAY_BRING_HARDSHIPS = new NpcStringId(1800528);
		THERE_MAY_BE_A_CHANCE_WHEN_YOU_CAN_SEE_A_NEW_ASPECT_OF_A_CLOSE_FRIEND = new NpcStringId(1800529);
		TRY_TO_BE_CLOSE_TO_SOMEONE_DIFFERENT_FROM_YOU_WITHOUT_ANY_STEREOTYPICAL_JUDGMENT = new NpcStringId(1800530);
		GOOD_LUCK_IN_BECOMING_A_LEADER_WITH_MANY_FOLLOWERS_HOWEVER_ITLL_ONLY_BE_AFTER_HARD_WORK =
				new NpcStringId(1800531);
		YOUR_WEALTH_LUCK_IS_RISING_EXPENDITURES_WILL_BE_FOLLOWED_BY_SUBSTANTIAL_INCOME_AS_YOU_ARE_ABLE_TO_SUSTAIN =
				new NpcStringId(1800532);
		BE_CAUTIOUS_AS_YOUR_WEALTH_LUCK_CAN_BE_EITHER_VERY_GOOD_OR_VERY_BAD = new NpcStringId(1800533);
		BE_WARNED_AS_A_SMALL_ARGUMENT_CAN_DISTANCE_YOU_FROM_A_CLOSE_FRIEND = new NpcStringId(1800534);
		THERE_IS_LUCK_IN_LOVE_WITH_A_NEW_PERSON = new NpcStringId(1800535);
		A_BIGGER_FORTUNE_WILL_BE_FOLLOWED_BY_YOUR_GOOD_DEED = new NpcStringId(1800536);
		THERE_MAY_BE_A_RELATIONSHIP_BREAKING_TRY_TO_ELIMINATE_MISUNDERSTANDINGS = new NpcStringId(1800537);
		BE_CAUTIOUS_NOT_TO_BE_EMOTIONALLY_MOVED_EVEN_IF_ITS_CONVINCING = new NpcStringId(1800538);
		SMILING_WILL_BRING_GOOD_LUCK = new NpcStringId(1800539);
		ITS_A_GOOD_IDEA_TO_LET_GO_OF_A_SMALL_LOSS = new NpcStringId(1800540);
		CONVEYING_YOUR_OWN_TRUTH_MAY_BE_DIFFICULT_AND_EASY_MISUNDERSTANDINGS_WILL_FOLLOW = new NpcStringId(1800541);
		THERE_IS_GOOD_LUCK_IN_A_PLACE_WITH_MANY_PEOPLE = new NpcStringId(1800542);
		TRY_TO_AVOID_DIRECTNESS_IF_YOU_CAN = new NpcStringId(1800543);
		VALUE_SUBSTANCE_OPPOSED_TO_THE_SAKE_HONOR_AND_LOOK_BEYOND_WHATS_IN_FRONT_OF_YOU = new NpcStringId(1800544);
		EXPANDING_A_RELATIONSHIP_WITH_HUMOR_MAY_BE_A_GOOD_IDEA = new NpcStringId(1800545);
		AN_ENJOYABLE_EVENT_MAY_BE_AHEAD_IF_YOU_ACCEPT_A_SIMPLE_BET = new NpcStringId(1800546);
		BEING_LEVEL_HEADED_NOT_FOCUSING_ON_EMOTIONS_MAY_HELP_WITH_RELATIONSHIPS = new NpcStringId(1800547);
		ITS_A_GOOD_IDEA_TO_TAKE_CARE_OF_MATTERS_IN_SEQUENTIAL_ORDER_WITHOUT_MEASURING_THEIR_IMPORTANCE =
				new NpcStringId(1800548);
		A_DETERMINED_ACT_AFTER_PREPARED_RESEARCH_WILL_ATTRACT_PEOPLE = new NpcStringId(1800549);
		A_LITTLE_HUMOR_MAY_BRING_COMPLETE_ATTENTION_TO_YOU = new NpcStringId(1800550);
		IT_MAY_NOT_BE_A_GOOD_TIME_FOR_AN_IMPORTANT_DECISION_BE_WARY_OF_TEMPTATIONS_AND_AVOID_MONETARY_DEALINGS =
				new NpcStringId(1800551);
		PAY_SPECIAL_ATTENTION_TO_ADVICE_FROM_A_CLOSE_FRIEND = new NpcStringId(1800552);
		THERE_MAY_BE_MODERATE_SOLUTIONS_TO_EVERY_PROBLEM_WHEN_THEYRE_VIEWED_FROM_A_3RD_PARTYS_POINT_OF_VIEW =
				new NpcStringId(1800553);
		DEALINGS_WITH_CLOSE_FRIENDS_ONLY_BRING_FRUSTRATION_AND_HEADACHE_POLITELY_DECLINE_AND_MENTION_ANOTHER_CHANCE =
				new NpcStringId(1800554);
		THERE_MAY_BE_A_PROBLEM_AT_COMPLETION_IF_THE_BASIC_MATTERS_ARE_NOT_CONSIDERED_FROM_THE_BEGINNING =
				new NpcStringId(1800555);
		DISTINGUISHING_BUSINESS_FROM_A_PRIVATE_MATTER_IS_NEEDED_TO_SUCCEED = new NpcStringId(1800556);
		A_CHANGE_IN_RULES_MAY_BE_HELPFUL_WHEN_PROBLEMS_ARE_PERSISTENT = new NpcStringId(1800557);
		PREPARING_FOR_AN_UNFORESEEN_SITUATION_WILL_BE_DIFFICULT_WHEN_SMALL_MATTERS_ARE_IGNORED =
				new NpcStringId(1800558);
		REFRAIN_FROM_GETTING_INVOLVED_IN_OTHERS_BUSINESS_TRY_TO_BE_LOOSE_AS_A_GOOSE = new NpcStringId(1800559);
		BEING_NEUTRAL_IS_A_GOOD_WAY_TO_GO_BUT_CLARITY_MAY_BE_HELPFUL_CONTRARY_TO_YOUR_HESITANCE =
				new NpcStringId(1800560);
		BE_CAUTIOUS_OF_YOUR_OWN_ACTIONS_THE_PAST_MAY_BRING_MISUNDERSTANDINGS = new NpcStringId(1800561);
		PAY_ATTENTION_TO_TIME_MANAGEMENT_EMOTIONS_MAY_WASTE_YOUR_TIME = new NpcStringId(1800562);
		HEROISM_WILL_BE_REWARDED_BUT_BE_CAREFUL_NOT_TO_DISPLAY_ARROGANCE_OR_LACK_OF_SINCERITY =
				new NpcStringId(1800563);
		IF_YOU_WANT_TO_MAINTAIN_RELATIONSHIP_CONNECTIONS_OFFER_RECONCILIATION_TO_THOSE_WHO_HAD_MISUNDERSTANDINGS_WITH_YOU =
				new NpcStringId(1800564);
		STEP_FORWARD_TO_SOLVE_OTHERS_PROBLEMS_WHEN_THEY_ARE_UNABLE = new NpcStringId(1800565);
		THERE_MAY_BE_A_LITTLE_LOSS_BUT_THINK_OF_IT_AS_AN_INVESTMENT_FOR_YOURSELF = new NpcStringId(1800566);
		AVARICE_BEARS_A_BIGGER_GREED_BEING_SATISFIED_WITH_MODERATION_IS_NEEDED = new NpcStringId(1800567);
		A_RATIONAL_ANALYSIS_IS_NEEDED_AS_UNPLANNED_ACTIONS_MAY_BRING_CRITICISM = new NpcStringId(1800568);
		REFLECT_UPON_YOUR_SHORTCOMINGS_BEFORE_CRITICIZING_OTHERS = new NpcStringId(1800569);
		FOLLOW_UP_CARE_IS_ALWAYS_NEEDED_AFTER_AN_EMERGENCY_EVASION = new NpcStringId(1800570);
		YOU_MAY_LOOK_FOR_A_NEW_CHALLENGE_BUT_VAST_KNOWLEDGE_IS_REQUIRED = new NpcStringId(1800571);
		WHEN_ONE_PUTS_ASIDE_THEIR_EGO_ANY_MISUNDERSTANDING_WILL_BE_SOLVED = new NpcStringId(1800572);
		LISTEN_TO_THE_ADVICE_THATS_GIVEN_TO_YOU_WITH_A_HUMBLE_ATTITUDE = new NpcStringId(1800573);
		EQUILIBRIUM_IS_ACHIEVED_WHEN_ONE_UNDERSTANDS_A_DOWNSHIFT_IS_EVIDENT_AFTER_THE_RISE = new NpcStringId(1800574);
		WHAT_YOU_SOW_IS_WHAT_YOU_REAP_FAITHFULLY_FOLLOW_THE_PLAN = new NpcStringId(1800575);
		METICULOUS_PREPARATION_IS_NEEDED_AS_SPONTANEOUS_ACTIONS_ONLY_BEAR_MENTAL_AND_MONETARY_LOSSES =
				new NpcStringId(1800576);
		THE_RIGHT_TIME_TO_BEAR_FRUIT_IS_DELAYED_WHILE_THE_FARMER_PONDERS_OPINIONS = new NpcStringId(1800577);
		HELP_EACH_OTHER_AMONG_CLOSE_FRIENDS = new NpcStringId(1800578);
		OBSESSING_OVER_A_SMALL_PROFIT_WILL_PLACE_PEOPLE_APART = new NpcStringId(1800579);
		DONT_CLING_TO_THE_RESULT_OF_A_GAMBLE = new NpcStringId(1800580);
		SMALL_TROUBLES_AND_ARGUMENTS_ARE_AHEAD_FACE_THEM_WITH_A_MATURE_ATTITUDE = new NpcStringId(1800581);
		NEGLECTING_A_PROMISE_MAY_PUT_YOU_IN_DISTRESS = new NpcStringId(1800582);
		DELAY_ANY_DEALINGS_AS_YOU_MAY_EASILY_OMIT_ADDRESSING_WHATS_IMPORTANT_TO_YOU = new NpcStringId(1800583);
		A_COMPARISON_TO_OTHERS_MAY_BE_HELPFUL = new NpcStringId(1800584);
		WHAT_YOUVE_ENDURED_WILL_RETURN_AS_A_BENEFIT = new NpcStringId(1800585);
		TRY_TO_BE_COURTEOUS_TO_THE_OPPOSITE_SEX_AND_FOLLOW_A_VIRTUOUS_PATH = new NpcStringId(1800586);
		JOY_MAY_COME_FROM_SMALL_THINGS = new NpcStringId(1800587);
		BE_CONFIDENT_IN_YOUR_ACTIONS_AS_GOOD_LUCK_SHADOWS_THE_RESULT = new NpcStringId(1800588);
		BE_CONFIDENT_WITHOUT_HESITATION_WHEN_YOUR_HONESTY_IS_ABOVE_REPROACH_IN_DEALINGS = new NpcStringId(1800589);
		A_MATTER_RELATED_TO_A_CLOSE_FRIEND_CAN_ISOLATE_YOU_KEEP_STAYING_ON_THE_RIGHT_PATH = new NpcStringId(1800590);
		TOO_MUCH_FOCUS_ON_THE_RESULT_MAY_BRING_CONTINUOUS_MISFORTUNE = new NpcStringId(1800591);
		BE_TENACIOUS_UNTIL_THE_FINISH_AS_HALFWAY_ABANDONMENT_CAUSES_A_TROUBLED_ENDING = new NpcStringId(1800592);
		THERE_WILL_BE_NO_ADVANTAGE_IN_A_GROUP_DEAL = new NpcStringId(1800593);
		REFRAIN_FROM_STEPPING_UP_BUT_TAKE_A_MOMENT_TO_PONDER_TO_BE_FLEXIBLE_WITH_SITUATIONS = new NpcStringId(1800594);
		THERE_WILL_BE_A_SMALL_OPPORTUNITY_WHEN_INFORMATION_IS_BEST_UTILIZED = new NpcStringId(1800595);
		BELONGINGS_ARE_AT_LOOSE_ENDS_KEEP_TRACK_OF_THE_THINGS_YOU_VALUE = new NpcStringId(1800596);
		WHAT_YOU_SOW_IS_WHAT_YOU_REAP_TRY_YOUR_BEST = new NpcStringId(1800597);
		WITH_THE_BEGINNERS_ATTITUDE_SHORTCOMINGS_CAN_BE_EASILY_MENDED = new NpcStringId(1800598);
		WHEN_FACING_DIFFICULTIES_SEEK_A_TOTALLY_DIFFERENT_DIRECTION = new NpcStringId(1800599);
		LIFETIME_SAVINGS_CAN_DISAPPEAR_WITH_ONE_TIME_GREED = new NpcStringId(1800600);
		WITH_YOUR_HEART_AVOID_EXTREMES_AND_PEACE_WILL_STAY = new NpcStringId(1800601);
		BE_CAUTIOUS_AS_INSTANT_RECKLESSNESS_MAY_BRING_MALICIOUS_GOSSIP = new NpcStringId(1800602);
		BE_TENACIOUS_TO_THE_END_BECAUSE_A_STRONG_LUCK_WITH_WINNING_IS_AHEAD = new NpcStringId(1800603);
		BE_KIND_TO_AND_CARE_FOR_THOSE_CLOSE_TO_YOU_THEY_MAY_HELP_IN_THE_FUTURE = new NpcStringId(1800604);
		POSITIVITY_MAY_BRING_GOOD_RESULTS = new NpcStringId(1800605);
		BE_GRACIOUS_TO_COVER_A_CLOSE_FRIENDS_FAULT = new NpcStringId(1800606);
		BE_PREPARED_FOR_AN_EXPECTED_COST = new NpcStringId(1800607);
		BE_CONSIDERATE_TO_OTHERS_AND_AVOID_FOCUSING_ONLY_ON_WINNING_OR_A_WOUND_WILL_BE_LEFT_UNTREATED =
				new NpcStringId(1800608);
		AN_ACCESSORY_OR_DECORATION_MAY_BRING_A_GOOD_LUCK = new NpcStringId(1800609);
		ONLY_REFLECTION_AND_HUMILITY_MAY_BRING_SUCCESS = new NpcStringId(1800610);
		A_SMALL_MISUNDERSTANDING_MAY_CAUSE_QUARRELS = new NpcStringId(1800611);
		AVOID_ADVANCING_BEYOND_YOUR_ABILITY_AND_FOCUS_ON_THE_FLOWING_STREAM = new NpcStringId(1800612);
		CONSIDERING_OTHERS_WITH_A_GOOD_HEART_BEFORE_SELF_INTEREST_WILL_BRING_A_TRIUMPH = new NpcStringId(1800613);
		VISITING_A_PLACE_YOUVE_NEVER_BEEN_BEFORE_MAY_BRING_LUCK = new NpcStringId(1800614);
		A_GOOD_THING_MAY_HAPPEN_IN_A_PLACE_WITH_A_FEW_PEOPLE = new NpcStringId(1800615);
		BEING_HIGH_STRUNG_CAN_CAUSE_LOSS_OF_TRUST_FROM_OTHERS_BECAUSE_IT_CAN_BE_VIEWED_AS_LIGHT_HEARTED_ACT_SINCERELY_BUT_YET_DO_NOT_LACK_HUMOR =
				new NpcStringId(1800616);
		PERFECTION_AT_THE_FINISH_CAN_COVER_FAULTY_WORK_IN_THE_PROCESS = new NpcStringId(1800617);
		ABSTAIN_FROM_LAZINESS_MUCH_WORK_BRINGS_MANY_GAINS_AND_SATISFACTORY_REWARDS = new NpcStringId(1800618);
		STAYING_BUSY_RATHER_THAN_BEING_STATIONARY_WILL_HELP = new NpcStringId(1800619);
		HANDLING_THE_WORK_BY_YOURSELF_MAY_LEAD_YOU_INTO_TEMPTATION = new NpcStringId(1800620);
		PAY_ATTENTION_TO_ANY_SMALL_ADVICE_WITHOUT_BEING_INDIFFERENT = new NpcStringId(1800621);
		SMALL_THINGS_MAKE_UP_BIG_THINGS_SO_EVEN_VALUE_TRIVIAL_MATTERS = new NpcStringId(1800622);
		ACTION_TOWARD_THE_RESULT_RATHER_THAN_WAITING_FOR_THE_RIGHT_CIRCUMSTANCES_MAY_LEAD_YOU_TO_A_FAST_SUCCESS =
				new NpcStringId(1800623);
		DONT_TRY_TO_SAVE_SMALL_EXPENDITURES_IT_WILL_LEAD_TO_FUTURE_RETURNS = new NpcStringId(1800624);
		BE_CAUTIOUS_TO_CONTROL_EMOTIONS_AS_TEMPTATIONS_ARE_NEARBY = new NpcStringId(1800625);
		BE_WARNED_AS_NEGLECTING_A_MATTER_BECAUSE_ITS_SMALL_CAN_CAUSE_YOU_TROUBLE = new NpcStringId(1800626);
		SPEND_WHEN_NEEDED_RATHER_THAN_TRYING_TO_UNCONDITIONALLY_SAVE = new NpcStringId(1800627);
		PREJUDICE_WILL_TAKE_YOU_TO_A_SMALL_GAIN_WITH_A_BIG_LOSS = new NpcStringId(1800628);
		SWEET_FOOD_MAY_BRING_GOOD_LUCK = new NpcStringId(1800629);
		YOU_MAY_BE_PAID_FOR_WHAT_YOURE_OWED_OR_FOR_YOUR_PAST_LOSS = new NpcStringId(1800630);
		THERE_MAY_BE_CONFLICT_IN_BASIC_MATTERS = new NpcStringId(1800631);
		BE_OBSERVANT_TO_CLOSE_FRIENDS_SMALL_BEHAVIORS_WHILE_REFRAINING_FROM_EXCESSIVE_KINDNESS =
				new NpcStringId(1800632);
		DO_NOT_SHOW_YOUR_DISTRESS_NOR_LOSE_YOUR_SMILE = new NpcStringId(1800633);
		SHOWING_CHANGE_MAY_BE_OF_HELP = new NpcStringId(1800634);
		THE_INTENDED_RESULT_MAY_BE_ON_YOUR_WAY_IF_THE_TIME_IS_PERFECTLY_MANAGED = new NpcStringId(1800635);
		HARDSHIPS_MAY_ARISE_IF_FLEXIBILITY_IS_NOT_WELL_PLAYED = new NpcStringId(1800636);
		KEEP_COOL_HEADED_BECAUSE_CARELESSNESS_OR_INATTENTIVENESS_MAY_CAUSE_MISFORTUNE = new NpcStringId(1800637);
		BE_CAUTIOUS_AS_YOU_MAY_GET_HURT_AFTER_LAST_NIGHTS_SINISTER_DREAM = new NpcStringId(1800638);
		A_STRONG_WEALTH_LUCK_IS_AHEAD_BUT_BE_CAREFUL_WITH_EMOTIONS_THAT_MAY_BRING_LOSSES = new NpcStringId(1800639);
		PROCEED_AS_YOU_WISH_WHEN_ITS_PERTINENT_TO_THE_PERSON_YOU_LIKE = new NpcStringId(1800640);
		YOU_MAY_DEEPEN_THE_RELATIONSHIP_WITH_THE_OPPOSITE_SEX_THROUGH_CONVERSATION = new NpcStringId(1800641);
		INVESTMENT_INTO_SOLID_MATERIAL_MAY_BRING_PROFIT = new NpcStringId(1800642);
		INVESTMENT_INTO_WHAT_YOU_ENJOY_MAY_BE_OF_HELP = new NpcStringId(1800643);
		BEING_BUSY_MAY_HELP_CATCHING_UP_WITH_MANY_CHANGES = new NpcStringId(1800644);
		CHOOSE_SUBSTANCE_OVER_HONOR = new NpcStringId(1800645);
		REMEMBER_TO_DECLINE_ANY_FINANCIAL_DEALINGS_BECAUSE_A_GOOD_DEED_MAY_RETURN_AS_RESENTMENT =
				new NpcStringId(1800646);
		BE_CAREFUL_NOT_TO_MAKE_A_MISTAKE_WITH_A_NEW_PERSON = new NpcStringId(1800647);
		DO_NOT_BE_OBSESSIVE_OVER_A_DRAGGED_OUT_PROJECT_SINCE_IT_WONT_GET_ANY_BETTER_WITH_MORE_TIME =
				new NpcStringId(1800648);
		DO_NOT_YIELD_WHATS_RIGHTFULLY_YOURS_OR_TOLERATE_LOSSES = new NpcStringId(1800649);
		THERES_LUCK_IN_RELATIONSHIPS_SO_BECOME_INTERESTED_IN_THE_OPPOSITE_SEX = new NpcStringId(1800650);
		SEEKING_OTHERS_HELP_RATHER_THAN_TRYING_BY_YOURSELF_MAY_RESULT_IN_TWO_BIRDS_WITH_ONE_STONE =
				new NpcStringId(1800651);
		PERSUADING_THE_OTHER_MAY_RESULT_IN_YOUR_GAIN = new NpcStringId(1800652);
		A_GOOD_OPPORTUNITY_MAY_COME_WHEN_KEEPING_PATIENCE_WITHOUT_EXCESSIVENESS = new NpcStringId(1800653);
		THE_OPPOSITE_SEX_MAY_BRING_FORTUNE = new NpcStringId(1800654);
		DOING_FAVOR_FOR_OTHER_PEOPLE_MAY_BRING_FORTUNE_IN_THE_FUTURE = new NpcStringId(1800655);
		LUCK_MAY_STAY_NEAR_IF_A_SMILE_IS_KEPT_DURING_DIFFICULT_TIMES = new NpcStringId(1800656);
		YOU_MAY_REVEAL_YOUR_TRUE_SELF_LIKE_IRON_IS_MOLTEN_INTO_AN_STRONG_SWORD = new NpcStringId(1800657);
		YOUR_VALUE_WILL_SHINE_AS_YOUR_POTENTIAL_IS_FINALLY_REALIZED = new NpcStringId(1800658);
		TENACIOUS_EFFORTS_IN_SOLVING_A_DIFFICULT_MISSION_OR_HARDSHIP_MAY_BRING_GOOD_RESULTS_AS_WELL_AS_REALIZING_YOUR_HIDDEN_POTENTIAL =
				new NpcStringId(1800659);
		PEOPLE_WILL_APPRECIATE_YOUR_POSITIVITY_AND_JOYFUL_ENTERTAINING = new NpcStringId(1800660);
		THINGS_WILL_MOVE_SMOOTHLY_WITH_YOUR_FULL_WISDOM_AND_ABILITIES = new NpcStringId(1800661);
		YOU_MAY_MEET_A_SAGE_WHO_CAN_HELP_YOU_FIND_THE_RIGHT_PATH = new NpcStringId(1800662);
		KEEN_INSTINCT_AND_FORESIGHT_WILL_SHINE_THEIR_VALUES = new NpcStringId(1800663);
		YOU_MAY_BRING_GOOD_LUCK_TO_THOSE_AROUND_YOU = new NpcStringId(1800664);
		YOUR_GOAL_MAY_BE_REALIZED_WHEN_EMOTIONAL_DETAILS_ARE_WELL_DEFINED = new NpcStringId(1800665);
		YOU_MAY_ENJOY_AFFLUENCE_AFTER_MEETING_A_PRECIOUS_PERSON = new NpcStringId(1800666);
		YOU_MAY_MEET_THE_OPPOSITE_SEX_WHO_HAS_MATERIALISTIC_ATTRACTIONS = new NpcStringId(1800667);
		A_BIG_SUCCESS_WILL_FOLLOW_ALL_POSSIBLE_EFFORTS_IN_COMPETITION = new NpcStringId(1800668);
		A_CONSEQUENCE_FROM_PAST_ACTIONS_WILL_BE_ON_DISPLAY = new NpcStringId(1800669);
		WHATEVER_HAPPENED_TO_YOU_AND_THE_OTHER_PERSON_WILL_REPLAY_BUT_THIS_TIME_THE_OPPOSITE_WILL_BE_THE_RESULT =
				new NpcStringId(1800670);
		YOU_MAY_NEED_TO_SACRIFICE_FOR_A_HIGHER_CAUSE = new NpcStringId(1800671);
		YOU_MAY_LOSE_AN_ITEM_BUT_WILL_GAIN_HONOR = new NpcStringId(1800672);
		A_NEW_TRIAL_OR_START_MAY_BE_SUCCESSFUL_AS_LUCK_SHADOWS_CHANGES = new NpcStringId(1800673);
		BE_SOPHISTICATED_WITHOUT_SHOWING_YOUR_TRUE_EMOTIONS_AS_TRICKS_AND_MATERIALISTIC_TEMPTATIONS_LIE_AHEAD =
				new NpcStringId(1800674);
		DO_NOT_ATTEMPT_A_DANGEROUS_ADVENTURE = new NpcStringId(1800675);
		DO_NOT_BE_AFRAID_OF_CHANGE_A_RISK_WILL_BE_ANOTHER_OPPORTUNITY = new NpcStringId(1800676);
		BE_CONFIDENT_AND_ACT_TENACIOUSLY_AT_ALL_TIMES_YOU_MAY_BE_ABLE_TO_ACCOMPLISH_TO_PERFECTION_DURING_SOMEWHAT_UNSTABLE_SITUATIONS =
				new NpcStringId(1800677);
		YOU_MAY_EXPECT_A_BRIGHT_AND_HOPEFUL_FUTURE = new NpcStringId(1800678);
		A_REST_WILL_PROMISE_A_BIGGER_DEVELOPMENT = new NpcStringId(1800679);
		FULLY_UTILIZE_POSITIVE_VIEWS = new NpcStringId(1800680);
		POSITIVE_THINKING_AND_ENERGETIC_ACTIONS_WILL_TAKE_YOU_TO_THE_CENTER_OF_THE_GLORIOUS_STAGE =
				new NpcStringId(1800681);
		YOUR_SELF_CONFIDENCE_AND_INTUITION_MAY_SOLVE_THE_DIFFICULTIES = new NpcStringId(1800682);
		EVERYTHING_IS_BRILLIANT_AND_JOYFUL_SHARE_IT_WITH_OTHERS_A_BIGGER_FORTUNE_WILL_FOLLOW = new NpcStringId(1800683);
		A_FAIR_ASSESSMENT_AND_REWARD_FOR_PAST_ACTIONS_LIE_AHEAD = new NpcStringId(1800684);
		PAY_ACCURATELY_THE_OLD_LIABILITY_OR_DEBT_IF_APPLICABLE_A_NEW_JOY_LIES_AHEAD = new NpcStringId(1800685);
		AN_EXCESSIVE_HUMILITY_CAN_HARM_YOU_BACK = new NpcStringId(1800686);
		A_REWARD_FOR_THE_PAST_WORK_WILL_COME_THROUGH = new NpcStringId(1800687);
		YOUR_PAST_FRUITLESS_EFFORT_WILL_FINALLY_BE_REWARDED_WITH_SOMETHING_UNEXPECTED = new NpcStringId(1800688);
		THERES_STRONG_LUCK_IN_A_REVIVAL_ABANDON_THE_OLD_AND_CREATE_THE_NEW = new NpcStringId(1800689);
		YOU_MAY_GAIN_MATERIALISTIC_OR_MENTAL_AID_FROM_CLOSE_FRIENDS = new NpcStringId(1800690);
		A_GOOD_BEGINNING_IS_AWAITING_YOU = new NpcStringId(1800691);
		YOU_MAY_MEET_THE_PERSON_YOUVE_LONGED_TO_SEE = new NpcStringId(1800692);
		YOU_MAY_SUSTAIN_A_LOSS_DUE_TO_YOUR_KINDNESS = new NpcStringId(1800693);
		CLOSELY_OBSERVE_PEOPLE_WHO_PASS_BY_SINCE_YOU_MAY_MEET_A_PRECIOUS_PERSON_WHO_CAN_HELP_YOU =
				new NpcStringId(1800694);
		MESSENGER_INFORM_THE_PATRONS_OF_THE_KEUCEREUS_ALLIANCE_BASE_WERE_GATHERING_BRAVE_ADVENTURERS_TO_ATTACK_TIATS_MOUNTED_TROOP_THATS_ROOTED_IN_THE_SEED_OF_DESTRUCTION =
				new NpcStringId(1800695);
		MESSENGER_INFORM_THE_PATRONS_OF_THE_KEUCEREUS_ALLIANCE_BASE_THE_SEED_OF_DESTRUCTION_IS_CURRENTLY_SECURED_UNDER_THE_FLAG_OF_THE_KEUCEREUS_ALLIANCE =
				new NpcStringId(1800696);
		MESSENGER_INFORM_THE_PATRONS_OF_THE_KEUCEREUS_ALLIANCE_BASE_TIATS_MOUNTED_TROOP_IS_CURRENTLY_TRYING_TO_RETAKE_SEED_OF_DESTRUCTION_COMMIT_ALL_THE_AVAILABLE_REINFORCEMENTS_INTO_SEED_OF_DESTRUCTION =
				new NpcStringId(1800697);
		MESSENGER_INFORM_THE_BROTHERS_IN_KUCEREUS_CLAN_OUTPOST_BRAVE_ADVENTURERS_WHO_HAVE_CHALLENGED_THE_SEED_OF_INFINITY_ARE_CURRENTLY_INFILTRATING_THE_HALL_OF_EROSION_THROUGH_THE_DEFENSIVELY_WEAK_HALL_OF_SUFFERING =
				new NpcStringId(1800698);
		MESSENGER_INFORM_THE_BROTHERS_IN_KUCEREUS_CLAN_OUTPOST_SWEEPING_THE_SEED_OF_INFINITY_IS_CURRENTLY_COMPLETE_TO_THE_HEART_OF_THE_SEED_EKIMUS_IS_BEING_DIRECTLY_ATTACKED_AND_THE_UNDEAD_REMAINING_IN_THE_HALL_OF_SUFFERING_ARE_BEING_ERADICATED =
				new NpcStringId(1800699);
		MESSENGER_INFORM_THE_PATRONS_OF_THE_KEUCEREUS_ALLIANCE_BASE_THE_SEED_OF_INFINITY_IS_CURRENTLY_SECURED_UNDER_THE_FLAG_OF_THE_KEUCEREUS_ALLIANCE =
				new NpcStringId(1800700);
		NOTHING = new NpcStringId(1800701);
		MESSENGER_INFORM_THE_PATRONS_OF_THE_KEUCEREUS_ALLIANCE_BASE_THE_RESURRECTED_UNDEAD_IN_THE_SEED_OF_INFINITY_ARE_POURING_INTO_THE_HALL_OF_SUFFERING_AND_THE_HALL_OF_EROSION =
				new NpcStringId(1800702);
		MESSENGER_INFORM_THE_BROTHERS_IN_KUCEREUS_CLAN_OUTPOST_EKIMUS_IS_ABOUT_TO_BE_REVIVED_BY_THE_RESURRECTED_UNDEAD_IN_SEED_OF_INFINITY_SEND_ALL_REINFORCEMENTS_TO_THE_HEART_AND_THE_HALL_OF_SUFFERING =
				new NpcStringId(1800703);
		STABBING_THREE_TIMES = new NpcStringId(1800704);
		POOR_CREATURES_FEEL_THE_POWER_OF_DARKNESS = new NpcStringId(1800705);
		WHOAAAAAA = new NpcStringId(1800706);
		YOULL_REGRET_CHALLENGING_ME = new NpcStringId(1800707);
		ITS_CURRENTLY_OCCUPIED_BY_THE_ENEMY_AND_OUR_TROOPS_ARE_ATTACKING = new NpcStringId(1800708);
		ITS_UNDER_OCCUPATION_BY_OUR_FORCES_AND_I_HEARD_THAT_KUCEREUS_CLAN_IS_ORGANIZING_THE_REMNANTS =
				new NpcStringId(1800709);
		ALTHOUGH_WE_CURRENTLY_HAVE_CONTROL_OF_IT_THE_ENEMY_IS_PUSHING_BACK_WITH_A_POWERFUL_ATTACK =
				new NpcStringId(1800710);
		ITS_UNDER_THE_ENEMYS_OCCUPATION_AND_THE_MILITARY_FORCES_OF_ADVENTURERS_AND_CLAN_MEMBERS_ARE_UNLEASHING_AN_ONSLAUGHT_UPON_THE_HALL_OF_SUFFERING_AND_THE_HALL_OF_EROSION =
				new NpcStringId(1800711);
		OUR_FORCES_HAVE_OCCUPIED_IT_AND_ARE_CURRENTLY_INVESTIGATING_THE_DEPTHS = new NpcStringId(1800713);
		ITS_UNDER_OCCUPATION_BY_OUR_FORCES_BUT_THE_ENEMY_HAS_RESURRECTED_AND_IS_ATTACKING_TOWARD_THE_HALL_OF_SUFFERING_AND_THE_HALL_OF_EROSION =
				new NpcStringId(1800714);
		ITS_UNDER_OCCUPATION_BY_OUR_FORCES_BUT_THE_ENEMY_HAS_ALREADY_OVERTAKEN_THE_HALL_OF_EROSION_AND_IS_DRIVING_OUT_OUR_FORCES_FROM_THE_HALL_OF_SUFFERING_TOWARD_THE_HEART_IT_SEEMS_THAT_EKIMUS_WILL_REVIVE_SHORTLY =
				new NpcStringId(1800715);
		TIATS_FOLLOWERS_ARE_COMING_TO_RETAKE_THE_SEED_OF_DESTRUCTION_GET_READY_TO_STOP_THE_ENEMIES =
				new NpcStringId(1800717);
		ITS_HURTING_IM_IN_PAIN_WHAT_CAN_I_DO_FOR_THE_PAIN = new NpcStringId(1800718);
		NO_WHEN_I_LOSE_THAT_ONE_ILL_BE_IN_MORE_PAIN = new NpcStringId(1800719);
		HAHAHAH_I_CAPTURED_SANTA_CLAUS_THERE_WILL_BE_NO_GIFTS_THIS_YEAR = new NpcStringId(1800720);
		NOW_WHY_DONT_YOU_TAKE_UP_THE_CHALLENGE = new NpcStringId(1800721);
		COME_ON_ILL_TAKE_ALL_OF_YOU_ON = new NpcStringId(1800722);
		HOW_ABOUT_IT_I_THINK_I_WON = new NpcStringId(1800723);
		NOW_THOSE_OF_YOU_WHO_LOST_GO_AWAY = new NpcStringId(1800724);
		WHAT_A_BUNCH_OF_LOSERS = new NpcStringId(1800725);
		I_GUESS_YOU_CAME_TO_RESCUE_SANTA_BUT_YOU_PICKED_THE_WRONG_PERSON = new NpcStringId(1800726);
		AH_OKAY = new NpcStringId(1800727);
		UAGH_I_WASNT_GOING_TO_DO_THAT = new NpcStringId(1800728);
		YOURE_CURSED_OH_WHAT = new NpcStringId(1800729);
		HAVE_YOU_DONE_NOTHING_BUT_ROCK_PAPER_SCISSORS = new NpcStringId(1800730);
		STOP_IT_NO_MORE_I_DID_IT_BECAUSE_I_WAS_TOO_LONELY = new NpcStringId(1800731);
		I_HAVE_TO_RELEASE_SANTA_HOW_INFURIATING = new NpcStringId(1800732);
		I_HATE_HAPPY_MERRY_CHRISTMAS = new NpcStringId(1800733);
		OH_IM_BORED = new NpcStringId(1800734);
		SHALL_I_GO_TO_TAKE_A_LOOK_IF_SANTA_IS_STILL_THERE_HEHE = new NpcStringId(1800735);
		OH_HO_HO_MERRY_CHRISTMAS = new NpcStringId(1800736);
		SANTA_COULD_GIVE_NICE_PRESENTS_ONLY_IF_HES_RELEASED_FROM_THE_TURKEY = new NpcStringId(1800737);
		OH_HO_HO_OH_HO_HO_THANK_YOU_LADIES_AND_GENTLEMEN_I_WILL_REPAY_YOU_FOR_SURE = new NpcStringId(1800738);
		UMERRY_CHRISTMAS_YOURE_DOING_A_GOOD_JOB = new NpcStringId(1800739);
		MERRY_CHRISTMAS_THANK_YOU_FOR_RESCUING_ME_FROM_THAT_WRETCHED_TURKEY = new NpcStringId(1800740);
		S1_I_HAVE_PREPARED_A_GIFT_FOR_YOU = new NpcStringId(1800741);
		I_HAVE_A_GIFT_FOR_S1 = new NpcStringId(1800742);
		TAKE_A_LOOK_AT_THE_INVENTORY_I_HOPE_YOU_LIKE_THE_GIFT_I_GAVE_YOU = new NpcStringId(1800743);
		TAKE_A_LOOK_AT_THE_INVENTORY_PERHAPS_THERE_MIGHT_BE_A_BIG_PRESENT = new NpcStringId(1800744);
		IM_TIRED_OF_DEALING_WITH_YOU_IM_LEAVING = new NpcStringId(1800745);
		WHEN_ARE_YOU_GOING_TO_STOP_I_SLOWLY_STARTED_TO_BE_TIRED_OF_IT = new NpcStringId(1800746);
		MESSAGE_FROM_SANTA_CLAUS_MANY_BLESSINGS_TO_S1_WHO_SAVED_ME = new NpcStringId(1800747);
		I_AM_ALREADY_DEAD_YOU_CANNOT_KILL_ME_AGAIN = new NpcStringId(1800748);
		OH_FOLLOWERS_OF_THE_DRAGON_OF_DARKNESS_FIGHT_BY_MY_SIDE = new NpcStringId(1800749);
		THE_DRAGON_RACE_ARE_INVADING_PREPARE_FOR_BATTLE = new NpcStringId(1800750);
		S1_RESCUED_SANTA_CLAUS_OF_S2_TERRITORY_FROM_THE_TURKEY = new NpcStringId(1800751);
		SANTA_RESCUE_SUCCESS = new NpcStringId(1800752);
		S1_RECEIVED_S2_S3_THROUGH_THE_WEAPON_EXCHANGE_COUPON = new NpcStringId(1800753);
		DONT_GO_PRATTLING_ON = new NpcStringId(1800754);
		YOU_LOWLIFES_WITH_NOT_EVEN_AN_OUNCE_OF_PRIDE_YOURE_NOT_WORTHY_OF_OPPOSING_ME = new NpcStringId(1800755);
		ROAR_NO_OINK_OINK_SEE_IM_A_PIG_OINK_OINK = new NpcStringId(1800756);
		WHO_AM_I_WHERE_AM_I_OINK_OINK = new NpcStringId(1800757);
		I_JUST_FOLLOWED_MY_FRIEND_HERE_FOR_FUN_OINK_OINK = new NpcStringId(1800758);
		WOW_THATS_WHAT_I_CALL_A_CURE_ALL = new NpcStringId(1800759);
		IM_STARVING_SHOULD_I_GO_CHEW_SOME_GRASS = new NpcStringId(1800760);
		THANK_YOU_THANK_YOU = new NpcStringId(1800761);
		WHATS_THIS_FEELING_OH_OH_FEELS_LIKE_MY_ENERGY_IS_BACK = new NpcStringId(1800762);
		MY_BODYS_GETTING_LIGHTER_THIS_FEELING_FEELS_FAMILIAR_SOMEHOW = new NpcStringId(1800763);
		WOW_MY_FATIGUE_IS_COMPLETELY_GONE = new NpcStringId(1800764);
		HEY_THE_OMINOUS_ENERGY_IS_DISAPPEARED = new NpcStringId(1800765);
		MY_BODY_FEELS_AS_LIGHT_AS_A_FEATHER = new NpcStringId(1800766);
		WHATS_THIS_FOOD = new NpcStringId(1800767);
		MY_ENERGY_IS_OVERFLOWING_I_DONT_NEED_ANY_FATIGUE_RECOVERY_POTION = new NpcStringId(1800768);
		WHATS_THE_MATTER_THATS_AN_AMATEUR_MOVE = new NpcStringId(1800769);
		FORTUNE_TIMER_REWARD_INCREASES_2_TIMES_IF_COMPLETED_WITHIN_10_SECONDS = new NpcStringId(1800770);
		FORTUNE_TIMER_REWARD_INCREASES_2_TIMES_IF_COMPLETED_WITHIN_40_SECONDS = new NpcStringId(1800771);
		N40_SECONDS_ARE_REMAINING = new NpcStringId(1800772);
		N39_SECONDS_ARE_REMAINING = new NpcStringId(1800773);
		N38_SECONDS_ARE_REMAINING = new NpcStringId(1800774);
		N37_SECONDS_ARE_REMAINING = new NpcStringId(1800775);
		N36_SECONDS_ARE_REMAINING = new NpcStringId(1800776);
		N35_SECONDS_ARE_REMAINING = new NpcStringId(1800777);
		N34_SECONDS_ARE_REMAINING = new NpcStringId(1800778);
		N33_SECONDS_ARE_REMAINING = new NpcStringId(1800779);
		N32_SECONDS_ARE_REMAINING = new NpcStringId(1800780);
		N31_SECONDS_ARE_REMAINING = new NpcStringId(1800781);
		N30_SECONDS_ARE_REMAINING = new NpcStringId(1800782);
		N29_SECONDS_ARE_REMAINING = new NpcStringId(1800783);
		N28_SECONDS_ARE_REMAINING = new NpcStringId(1800784);
		N27_SECONDS_ARE_REMAINING = new NpcStringId(1800785);
		N26_SECONDS_ARE_REMAINING = new NpcStringId(1800786);
		N25_SECONDS_ARE_REMAINING = new NpcStringId(1800787);
		N24_SECONDS_ARE_REMAINING = new NpcStringId(1800788);
		N23_SECONDS_ARE_REMAINING = new NpcStringId(1800789);
		N22_SECONDS_ARE_REMAINING = new NpcStringId(1800790);
		N21_SECONDS_ARE_REMAINING = new NpcStringId(1800791);
		N20_SECONDS_ARE_REMAINING = new NpcStringId(1800792);
		N19_SECONDS_ARE_REMAINING = new NpcStringId(1800793);
		N18_SECONDS_ARE_REMAINING = new NpcStringId(1800794);
		N17_SECONDS_ARE_REMAINING = new NpcStringId(1800795);
		N16_SECONDS_ARE_REMAINING = new NpcStringId(1800796);
		N15_SECONDS_ARE_REMAINING = new NpcStringId(1800797);
		N14_SECONDS_ARE_REMAINING = new NpcStringId(1800798);
		N13_SECONDS_ARE_REMAINING = new NpcStringId(1800799);
		N12_SECONDS_ARE_REMAINING = new NpcStringId(1800800);
		N11_SECONDS_ARE_REMAINING = new NpcStringId(1800801);
		N10_SECONDS_ARE_REMAINING = new NpcStringId(1800802);
		N9_SECONDS_ARE_REMAINING = new NpcStringId(1800803);
		N8_SECONDS_ARE_REMAINING = new NpcStringId(1800804);
		N7_SECONDS_ARE_REMAINING = new NpcStringId(1800805);
		N6_SECONDS_ARE_REMAINING = new NpcStringId(1800806);
		N5_SECONDS_ARE_REMAINING = new NpcStringId(1800807);
		N4_SECONDS_ARE_REMAINING = new NpcStringId(1800808);
		N3_SECONDS_ARE_REMAINING = new NpcStringId(1800809);
		N2_SECONDS_ARE_REMAINING = new NpcStringId(1800810);
		N1_SECONDS_ARE_REMAINING = new NpcStringId(1800811);
		TIME_UP = new NpcStringId(1800812);
		MISSION_FAILED = new NpcStringId(1800813);
		MISSION_SUCCESS = new NpcStringId(1800814);
		HEY_I_ALREADY_HAVE_AN_OWNER = new NpcStringId(1800815);
		HEY_ARE_YOU_PLANNING_ON_EATING_ME_USE_A_CUPIDS_FATIGUE_RECOVERY_POTION_ALREADY = new NpcStringId(1800816);
		ILL_PASS_ON_AN_AMATEURS_MERIDIAN_MASSAGE_USE_A_CUPIDS_FATIGUE_RECOVERY_POTION_ALREADY =
				new NpcStringId(1800817);
		I_ALREADY_FEEL_MORE_ENERGETIC_THANKS_S1 = new NpcStringId(1800818);
		HOW_REFRESHING_YOU_WOULDNT_HAPPEN_TO_BE_A_MASTER_MASSEUSE_S1_WOULD_YOU = new NpcStringId(1800819);
		INCREDIBLE_FROM_NOW_ON_ILL_COMPARE_ALL_MASSAGES_TO_THIS_ONE_WITH_S1 = new NpcStringId(1800820);
		ISNT_IT_TOUGH_DOING_IT_ALL_ON_YOUR_OWN_NEXT_TIME_TRY_MAKING_A_PARTY_WITH_SOME_COMRADES =
				new NpcStringId(1800821);
		SORRY_BUT_ILL_LEAVE_MY_FRIEND_IN_YOUR_CARE_AS_WELL_THANKS = new NpcStringId(1800822);
		SNIFF_SNIFF_DO_YOU_SMELL_THE_SCENT_OF_A_FRESH_BAKED_BAGUETTE = new NpcStringId(1800823);
		WHO_AM_I_LET_ME_KNOW_IF_YOU_WANNA_BUY_MY_BREAD = new NpcStringId(1800824);
		I_JUST_WANT_TO_MAKE_YOUR_WEAPONS_STRONGER_ABRA_KADABRA = new NpcStringId(1800825);
		WHAT_YOU_DONT_LIKE_IT_WHATS_THE_MATTER_WITH_YOU_LIKE_AN_AMATEUR = new NpcStringId(1800826);
		HEY_DID_YOU_TELL_A_LIE_ON_APRIL_FOOLS_DAY_DONT_TALK_TO_ME_IF_YOU_DIDNT = new NpcStringId(1800827);
		GRUNT_WHATS_WRONG_WITH_ME = new NpcStringId(1800828);
		GRUNT_OH = new NpcStringId(1800829);
		THE_GRAVE_ROBBER_WARRIOR_HAS_BEEN_FILLED_WITH_DARK_ENERGY_AND_IS_ATTACKING_YOU = new NpcStringId(1800830);
		THE_ALTAR_GUARDIAN_IS_SCRUTINIZING_YOU_NTHOSE_WHO_DARE_TO_CHALLENGE_USING_THE_POWER_OF_EVIL_SHALL_BE_PUNISHED_WITH_DEATH =
				new NpcStringId(1800831);
		WAIT_WAIT_STOP_SAVE_ME_AND_ILL_GIVE_YOU_10000000_ADENA = new NpcStringId(1800832);
		I_DONT_WANT_TO_FIGHT = new NpcStringId(1800833);
		IS_THIS_REALLY_NECESSARY = new NpcStringId(1800834);
		TH_THANKS_I_COULD_HAVE_BECOME_GOOD_FRIENDS_WITH_YOU = new NpcStringId(1800835);
		ILL_GIVE_YOU_10000000_ADENA_LIKE_I_PROMISED_I_MIGHT_BE_AN_ORC_WHO_KEEPS_MY_PROMISES = new NpcStringId(1800836);
		THANKS_BUT_THAT_THING_ABOUT_10000000_ADENA_WAS_A_LIE_SEE_YA = new NpcStringId(1800837);
		YOURE_PRETTY_DUMB_TO_BELIEVE_ME = new NpcStringId(1800838);
		UGH_A_CURSE_UPON_YOU = new NpcStringId(1800839);
		I_REALLY_DIDNT_WANT_TO_FIGHT = new NpcStringId(1800840);
		KASHAS_EYE_IS_SCRUTINIZING_YOU = new NpcStringId(1800841);
		THE_KASHAS_EYE_GIVES_YOU_A_STRANGE_FEELING = new NpcStringId(1800842);
		THE_EVIL_AURA_OF_THE_KASHAS_EYE_SEEMS_TO_BE_INCREASING_QUICKLY = new NpcStringId(1800843);
		I_PROTECT_THE_ALTAR_YOU_CANT_ESCAPE_THE_ALTAR = new NpcStringId(1800844);
		S1_THAT_STRANGER_MUST_BE_DEFEATED_HERE_IS_THE_ULTIMATE_HELP = new NpcStringId(1800845);
		LOOK_HERE_S1_DONT_FALL_TOO_FAR_BEHIND = new NpcStringId(1800846);
		WELL_DONE_S1_YOUR_HELP_IS_MUCH_APPRECIATED = new NpcStringId(1800847);
		WHO_HAS_AWAKENED_US_FROM_OUR_SLUMBER = new NpcStringId(1800848);
		ALL_WILL_PAY_A_SEVERE_PRICE_TO_ME_AND_THESE_HERE = new NpcStringId(1800849);
		SHYEEDS_CRY_IS_STEADILY_DYING_DOWN = new NpcStringId(1800850);
		ALERT_ALERT_DAMAGE_DETECTION_RECOGNIZED_COUNTERMEASURES_ENABLED = new NpcStringId(1800851);
		TARGET_RECOGNITION_ACHIEVED_ATTACK_SEQUENCE_COMMENCING = new NpcStringId(1800852);
		TARGET_THREAT_LEVEL_LAUNCHING_STRONGEST_COUNTERMEASURE = new NpcStringId(1800853);
		THE_PURIFICATION_FIELD_IS_BEING_ATTACKED_GUARDIAN_SPIRITS_PROTECT_THE_MAGIC_FORCE = new NpcStringId(1800854);
		PROTECT_THE_BRAZIERS_OF_PURITY_AT_ALL_COSTS = new NpcStringId(1800855);
		DEFEND_OUR_DOMAIN_EVEN_AT_RISK_OF_YOUR_OWN_LIFE = new NpcStringId(1800856);
		PEUNGLUI_MUGLANEP = new NpcStringId(1800857);
		NAIA_WAGANAGEL_PEUTAGUN = new NpcStringId(1800858);
		DRIVE_DEVICE_PARTIAL_DESTRUCTION_IMPULSE_RESULT = new NpcStringId(1800859);
		EVEN_THE_MAGIC_FORCE_BINDS_YOU_YOU_WILL_NEVER_BE_FORGIVEN = new NpcStringId(1800860);
		OH_GIANTS_AN_INTRUDER_HAS_BEEN_DISCOVERED = new NpcStringId(1800861);
		ALL_IS_VANITY_BUT_THIS_CANNOT_BE_THE_END = new NpcStringId(1800862);
		THOSE_WHO_ARE_IN_FRONT_OF_MY_EYES_WILL_BE_DESTROYED = new NpcStringId(1800863);
		I_AM_TIRED_DO_NOT_WAKE_ME_UP_AGAIN = new NpcStringId(1800864);
		_INTRUDER_DETECTED = new NpcStringId(1800865);
		THE_CANDLES_CAN_LEAD_YOU_TO_ZAKEN_DESTROY_HIM = new NpcStringId(1800866);
		WHO_DARES_AWKAWEN_THE_MIGHTY_ZAKEN = new NpcStringId(1800867);
		YE_NOT_BE_FINDING_ME_BELOW_THE_DRINK = new NpcStringId(1800868);
		YE_MUST_BE_THREE_SHEETS_TO_THE_WIND_IF_YER_LOOKIN_FOR_ME_THERE = new NpcStringId(1800869);
		YE_NOT_BE_FINDING_ME_IN_THE_CROWS_NEST = new NpcStringId(1800870);
		SORRY_BUT_THIS_IS_ALL_I_HAVE_GIVE_ME_A_BREAK = new NpcStringId(1800871);
		PEUNGLUI_MUGLANEP_NAIA_WAGANAGEL_PEUTAGUN = new NpcStringId(1800872);
		DRIVE_DEVICE_ENTIRE_DESTRUCTION_MOVING_SUSPENSION = new NpcStringId(1800873);
		AH_AH_FROM_THE_MAGIC_FORCE_NO_MORE_I_WILL_BE_FREED = new NpcStringId(1800874);
		YOU_GUYS_ARE_DETECTED = new NpcStringId(1800875);
		WHAT_KIND_OF_CREATURES_ARE_YOU = new NpcStringId(1800876);
		S2_OF_LEVEL_S1_IS_ACQUIRED = new NpcStringId(1800877);
		LIFE_STONE_FROM_THE_BEGINNING_ACQUIRED = new NpcStringId(1800878);
		WHEN_INVENTORY_WEIGHT_NUMBER_ARE_MORE_THAN_80_THE_LIFE_STONE_FROM_THE_BEGINNING_CANNOT_BE_ACQUIRED =
				new NpcStringId(1800879);
		YOU_ARE_UNDER_MY_THUMB = new NpcStringId(1800880);
		N21_MINUTES_ARE_ADDED_TO_THE_REMAINING_TIME_IN_THE_INSTANT_ZONE = new NpcStringId(1800881);
		HURRY_HURRY = new NpcStringId(1800882);
		I_AM_NOT_THAT_TYPE_OF_PERSON_WHO_STAYS_IN_ONE_PLACE_FOR_A_LONG_TIME = new NpcStringId(1800883);
		ITS_HARD_FOR_ME_TO_KEEP_STANDING_LIKE_THIS = new NpcStringId(1800884);
		WHY_DONT_I_GO_THAT_WAY_THIS_TIME = new NpcStringId(1800885);
		WELCOME = new NpcStringId(1800886);
		IS_THAT_IT_IS_THAT_THE_EXTENT_OF_YOUR_ABILITIES_PUT_IN_A_LITTLE_MORE_EFFORT = new NpcStringId(1800887);
		YOUR_ABILITIES_ARE_PITIFUL_YOU_ARE_FAR_FROM_A_WORTHY_OPPONENT = new NpcStringId(1800888);
		EVEN_AFTER_DEATH_YOU_ORDER_ME_TO_WANDER_AROUND_LOOKING_FOR_THE_SCAPEGOATS = new NpcStringId(1800889);
		HERE_GOES_THE_HEATSTROKE_IF_YOU_CAN_WITHSTAND_THE_HOT_HEATSTROKE_UP_TO_THE_3RD_STAGE_THE_SULTRINESS_WILL_COME_TO_YOU =
				new NpcStringId(1800890);
		JUST_YOU_WAIT_HUMIDITY_IS_A_BLISTERING_FIREBALL_WHICH_CAN_EASILY_WITHSTAND_PLENTY_OF_COOL_AIR_CANNON_ATTACKS =
				new NpcStringId(1800891);
		IN_ORDER_TO_DEFEAT_HUMIDITY_YOU_MUST_OBTAIN_THE_HEADSTROKE_PREVENTION_EFFECT_FROM_DOCTOR_ICE_AND_FIRE_MORE_THAN_10_ROUNDS_OF_THE_COOL_AIR_CANNON_ON_IT =
				new NpcStringId(1800892);
		YOU_ARE_HERE_S1_ILL_TEACH_YOU_A_LESSON_BRING_IT_ON = new NpcStringId(1800893);
		THATS_COLD_ISNT_IT_ONE_OF_THOSE_COOL_PACKS_I_HATE_ANYTHING_THATS_COLD = new NpcStringId(1800894);
		HUH_YOUVE_MISSED_IS_THAT_ALL_YOU_HAVE = new NpcStringId(1800895);
		I_WILL_GIVE_YOU_PRECIOUS_THINGS_THAT_I_HAVE_STOLEN_SO_STOP_BOTHERING_ME = new NpcStringId(1800896);
		I_WAS_GOING_TO_GIVE_YOU_A_JACKPOT_ITEM_YOU_DONT_HAVE_ENOUGH_INVENTORY_ROOM_SEE_YOU_NEXT_TIME =
				new NpcStringId(1800897);
		S1_DEFEATED_THE_SULTRINESS_AND_ACQUIRED_ITEM_S84 = new NpcStringId(1800898);
		S1_DEFEATED_THE_SULTRINESS_AND_ACQUIRED_ITEM_S80 = new NpcStringId(1800899);
		I_AM_NOT_HERE_FOR_YOU_YOUR_COOL_PACK_ATTACK_DOES_NOT_WORK_AGAINST_ME = new NpcStringId(1800900);
		UH_OH_WHERE_ARE_YOU_HIDING_THERE_IS_NOBODY_WHO_MATCHES_MY_SKILLS_WELL_I_GUESS_ID_BETTER_GET_GOING =
				new NpcStringId(1800901);
		WHY_ARE_YOU_NOT_RESPONDING_YOU_DONT_EVEN_HAVE_ANY_COOL_PACKS_YOU_CANT_FIGHT_ME = new NpcStringId(1800902);
		OH_WHERE_I_BE_WHO_CALL_ME = new NpcStringId(1800903);
		TADA_ITS_A_WATERMELON = new NpcStringId(1800904);
		ENTER_THE_WATERMELON_ITS_GONNA_GROW_AND_GROW_FROM_NOW_ON = new NpcStringId(1800906);
		OH_OUCH_DID_I_SEE_YOU_BEFORE = new NpcStringId(1800907);
		A_NEW_SEASON_SUMMER_IS_ALL_ABOUT_THE_WATERMELON = new NpcStringId(1800908);
		DID_YA_CALL_HO_THOUGHT_YOUD_GET_SOMETHING = new NpcStringId(1800909);
		DO_YOU_WANT_TO_SEE_MY_BEAUTIFUL_SELF = new NpcStringId(1800910);
		HOHOHO_LETS_DO_IT_TOGETHER = new NpcStringId(1800911);
		ITS_A_GIANT_WATERMELON_IF_YOU_RAISE_IT_RIGHT_AND_A_WATERMELON_SLICE_IF_YOU_MESS_UP = new NpcStringId(1800912);
		TADA_TRANSFORMATION_COMPLETE = new NpcStringId(1800913);
		AM_I_A_RAIN_WATERMELON_OR_A_DEFECTIVE_WATERMELON = new NpcStringId(1800914);
		NOW_IVE_GOTTEN_BIG_EVERYONE_COME_AT_ME = new NpcStringId(1800915);
		GET_BIGGER_GET_STRONGER_TELL_ME_YOUR_WISH = new NpcStringId(1800916);
		A_WATERMELON_SLICES_WISH_BUT_IM_BIGGER_ALREADY = new NpcStringId(1800917);
		A_LARGE_WATERMELONS_WISH_WELL_TRY_TO_BREAK_ME = new NpcStringId(1800918);
		IM_DONE_GROWING_IM_RUNNING_AWAY_NOW = new NpcStringId(1800919);
		IF_YOU_LET_ME_GO_ILL_GIVE_YOU_TEN_MILLION_ADENA = new NpcStringId(1800920);
		FREEDOM_WHAT_DO_YOU_THINK_I_HAVE_INSIDE = new NpcStringId(1800921);
		OK_OK_GOOD_JOB_YOU_KNOW_WHAT_TO_DO_NEXT_RIGHT = new NpcStringId(1800922);
		LOOK_HERE_DO_IT_RIGHT_YOU_SPILLED_THIS_PRECIOUS = new NpcStringId(1800923);
		AH_REFRESHING_SPRAY_A_LITTLE_MORE = new NpcStringId(1800924);
		GULP_GULP_GREAT_BUT_ISNT_THERE_MORE = new NpcStringId(1800925);
		CANT_YOU_EVEN_AIM_RIGHT_HAVE_YOU_EVEN_BEEN_TO_THE_ARMY = new NpcStringId(1800926);
		DID_YOU_MIX_THIS_WITH_WATER_WHYS_IT_TASTE_LIKE_THIS = new NpcStringId(1800927);
		OH_GOOD_DO_A_LITTLE_MORE_YEAH = new NpcStringId(1800928);
		HOHO_ITS_NOT_THERE_OVER_HERE_AM_I_SO_SMALL_THAT_YOU_CAN_EVEN_SPRAY_ME_RIGHT = new NpcStringId(1800929);
		YUCK_WHAT_IS_THIS_ARE_YOU_SURE_THIS_IS_NECTAR = new NpcStringId(1800930);
		DO_YOUR_BEST_I_BECOME_A_BIG_WATERMELON_AFTER_JUST_FIVE_BOTTLES = new NpcStringId(1800931);
		OF_COURSE_WATERMELON_IS_THE_BEST_NECTAR_HAHAHA = new NpcStringId(1800932);
		OWW_YOURE_JUST_BEATING_ME_NOW_GIVE_ME_NECTAR = new NpcStringId(1800934);
		LOOK_ITS_GONNA_BREAK = new NpcStringId(1800935);
		NOW_ARE_YOU_TRYING_TO_EAT_WITHOUT_DOING_THE_WORK_FINE_DO_WHAT_YOU_WANT_ILL_HATE_YOU_IF_YOU_DONT_GIVE_ME_ANY_NECTAR =
				new NpcStringId(1800936);
		HIT_ME_MORE_HIT_ME_MORE = new NpcStringId(1800937);
		IM_GONNA_WITHER_LIKE_THIS_DAMN_IT = new NpcStringId(1800938);
		HEY_YOU_IF_I_DIE_LIKE_THIS_THERELL_BE_NO_ITEM_EITHER_ARE_YOU_REALLY_SO_STINGY_WITH_THE_NECTAR =
				new NpcStringId(1800939);
		ITS_JUST_A_LITTLE_MORE_GOOD_LUCK = new NpcStringId(1800940);
		SAVE_ME_IM_ABOUT_TO_DIE_WITHOUT_TASTING_NECTAR_EVEN_ONCE = new NpcStringId(1800941);
		IF_I_DIE_LIKE_THIS_ILL_JUST_BE_A_WATERMELON_SLICE = new NpcStringId(1800942);
		IM_GETTING_STRONGER_I_THINK_ILL_BE_ABLE_TO_RUN_AWAY_IN_30_SECONDS_HOHO = new NpcStringId(1800943);
		ITS_GOODBYE_AFTER_20_SECONDS = new NpcStringId(1800944);
		YEAH_10_SECONDS_LEFT_9_8_7 = new NpcStringId(1800945);
		IM_LEAVING_IN_2_MINUTES_IF_YOU_DONT_GIVE_ME_ANY_NECTAR = new NpcStringId(1800946);
		IM_LEAVING_IN_1_MINUTES_IF_YOU_DONT_GIVE_ME_ANY_NECTAR = new NpcStringId(1800947);
		IM_LEAVING_NOW_THEN_GOODBYE = new NpcStringId(1800948);
		SORRY_BUT_THIS_LARGE_WATERMELON_IS_DISAPPEARING_HERE = new NpcStringId(1800949);
		TOO_LATE_HAVE_A_GOOD_TIME = new NpcStringId(1800950);
		DING_DING_THATS_THE_BELL_PUT_AWAY_YOUR_WEAPONS_AND_TRY_FOR_NEXT_TIME = new NpcStringId(1800951);
		TOO_BAD_YOU_RAISED_IT_UP_TOO = new NpcStringId(1800952);
		OH_WHAT_A_NICE_SOUND = new NpcStringId(1800953);
		THE_INSTRUMENT_IS_NICE_BUT_THERES_NO_SONG_SHALL_I_SING_FOR_YOU = new NpcStringId(1800954);
		WHAT_BEAUTIFUL_MUSIC = new NpcStringId(1800955);
		I_FEEL_GOOD_PLAY_SOME_MORE = new NpcStringId(1800956);
		MY_HEART_IS_BEING_CAPTURED_BY_THE_SOUND_OF_CRONO = new NpcStringId(1800957);
		GET_THE_NOTES_RIGHT_HEY_OLD_MAN_THAT_WAS_WRONG = new NpcStringId(1800958);
		I_LIKE_IT = new NpcStringId(1800959);
		OOH_MY_BODY_WANTS_TO_OPEN = new NpcStringId(1800960);
		OH_THIS_CHORD_MY_HEART_IS_BEING_TORN_PLAY_A_LITTLE_MORE = new NpcStringId(1800961);
		ITS_THIS_THIS_I_WANTED_THIS_SOUND_WHY_DONT_YOU_TRY_BECOMING_A_SINGER = new NpcStringId(1800962);
		YOU_CAN_TRY_A_HUNDRED_TIMES_ON_THIS_YOU_WONT_GET_ANYTHING_GOOD = new NpcStringId(1800963);
		IT_HURTS_PLAY_JUST_THE_INSTRUMENT = new NpcStringId(1800964);
		ONLY_GOOD_MUSIC_CAN_OPEN_MY_BODY = new NpcStringId(1800965);
		NOT_THIS_BUT_YOU_KNOW_THAT_WHAT_YOU_GOT_AS_A_CHRONICLE_SOUVENIR_PLAY_WITH_THAT = new NpcStringId(1800966);
		WHY_YOU_HAVE_NO_MUSIC_BORING_IM_LEAVING_NOW = new NpcStringId(1800967);
		NOT_THOSE_SHARP_THINGS_USE_THE_ONES_THAT_MAKE_NICE_SOUNDS = new NpcStringId(1800968);
		LARGE_WATERMELONS_ONLY_OPEN_WITH_MUSIC_JUST_STRIKING_WITH_A_WEAPON_WONT_WORK = new NpcStringId(1800969);
		STRIKE_WITH_MUSIC_NOT_WITH_SOMETHING_LIKE_THIS_YOU_NEED_MUSIC = new NpcStringId(1800970);
		YOURE_PRETTY_AMAZING_BUT_ITS_ALL_FOR_NOTHING = new NpcStringId(1800971);
		USE_THAT_ON_MONSTERS_OK_I_WANT_CRONO = new NpcStringId(1800972);
		EVERYONE_THE_WATERMELON_IS_BREAKING = new NpcStringId(1800973);
		ITS_LIKE_A_WATERMELON_SLICE = new NpcStringId(1800974);
		LARGE_WATERMELON_MAKE_A_WISH = new NpcStringId(1800976);
		DONT_TELL_ANYONE_ABOUT_MY_DEATH = new NpcStringId(1800977);
		UGH_THE_RED_JUICE_IS_FLOWING_OUT = new NpcStringId(1800978);
		THIS_IS_ALL = new NpcStringId(1800979);
		KYAAHH_IM_MAD = new NpcStringId(1800980);
		EVERYONE_THIS_WATERMELON_BROKE_OPEN_THE_ITEM_IS_FALLING_OUT = new NpcStringId(1800981);
		OH_IT_BURST_THE_CONTENTS_ARE_SPILLING_OUT = new NpcStringId(1800982);
		HOHOHO_PLAY_BETTER = new NpcStringId(1800983);
		OH_YOURE_VERY_TALENTED_HUH = new NpcStringId(1800984);
		PLAY_SOME_MORE_MORE_MORE_MORE = new NpcStringId(1800985);
		I_EAT_HITS_AND_GROW = new NpcStringId(1800986);
		BUCK_UP_THERE_ISNT_MUCH_TIME = new NpcStringId(1800987);
		DO_YOU_THINK_ILL_BURST_WITH_JUST_THAT = new NpcStringId(1800988);
		WHAT_A_NICE_ATTACK_YOU_MIGHT_BE_ABLE_TO_KILL_A_PASSING_FLY = new NpcStringId(1800989);
		RIGHT_THERE_A_LITTLE_TO_THE_RIGHT_AH_REFRESHING = new NpcStringId(1800990);
		YOU_CALL_THAT_HITTING_BRING_SOME_MORE_TALENTED_FRIENDS = new NpcStringId(1800991);
		DONT_THINK_JUST_HIT_WERE_HITTING = new NpcStringId(1800992);
		I_NEED_NECTAR_GOURD_NECTAR = new NpcStringId(1800993);
		I_CAN_ONLY_GROW_BY_DRINKING_NECTAR = new NpcStringId(1800994);
		GROW_ME_QUICK_IF_YOURE_GOOD_ITS_A_LARGE_WATERMELON_IF_YOURE_BAD_IT_A_WATERMELON_SLICE =
				new NpcStringId(1800995);
		GIMME_NECTAR_IM_HUNGRY = new NpcStringId(1800996);
		BRING_ME_NECTAR_THEN_ILL_DRINK_AND_GROW = new NpcStringId(1800998);
		YOU_WANNA_EAT_A_TINY_WATERMELON_LIKE_ME_TRY_GIVING_ME_SOME_NECTAR_ILL_GET_HUGE = new NpcStringId(1800999);
		HEHEHE_GROW_ME_WELL_AND_YOULL_GET_A_REWARD_GROW_ME_BAD_AND_WHO_KNOWS_WHATLL_HAPPEN = new NpcStringId(1801000);
		YOU_WANT_A_LARGE_WATERMELON_ID_LIKE_TO_BE_A_WATERMELON_SLICE = new NpcStringId(1801001);
		TRUST_ME_AND_BRING_ME_SOME_NECTAR_ILL_BECOME_A_LARGE_WATERMELON_FOR_YOU = new NpcStringId(1801002);
		I_SEE_BELETH_HAS_RECOVERED_ALL_OF_ITS_MAGIC_POWER_WHAT_REMAINS_HERE_IS_JUST_ITS_TRACE =
				new NpcStringId(1801003);
		COMMAND_CHANNEL_LEADER_S1_BELETHS_RING_HAS_BEEN_ACQUIRED = new NpcStringId(1801004);
		YOU_SUMMONED_ME_SO_YOU_MUST_BE_CONFIDENT_HUH_HERE_I_COME_JACK_GAME = new NpcStringId(1801005);
		HELLO_LETS_HAVE_A_GOOD_JACK_GAME = new NpcStringId(1801006);
		IM_STARTING_NOW_SHOW_ME_THE_CARD_YOU_WANT = new NpcStringId(1801007);
		WELL_START_NOW_SHOW_ME_THE_CARD_YOU_WANT = new NpcStringId(1801008);
		IM_SHOWING_THE_ROTTEN_PUMPKIN_CARD = new NpcStringId(1801009);
		ILL_BE_SHOWING_THE_ROTTEN_PUMPKIN_CARD = new NpcStringId(1801010);
		IM_SHOWING_THE_JACK_PUMPKIN_CARD = new NpcStringId(1801011);
		ILL_BE_SHOWING_THE_JACK_PUMPKIN_CARD = new NpcStringId(1801012);
		THATS_MY_PRECIOUS_FANTASTIC_CHOCOLATE_BANANA_ULTRA_FAVOR_CANDY_IM_DEFINITELY_WINNING_THE_NEXT_ROUND =
				new NpcStringId(1801013);
		ITS_MY_PRECIOUS_CANDY_BUT_ILL_HAPPILY_GIVE_IT_TO_YOU = new NpcStringId(1801014);
		THE_CANDY_FELL_ILL_GIVE_YOU_MY_TOY_CHEST_INSTEAD = new NpcStringId(1801015);
		SINCE_THE_CANDY_FELL_I_WILL_GIVE_YOU_MY_TOY_CHEST_INSTEAD = new NpcStringId(1801016);
		YOURE_NOT_PEEKING_AT_MY_CARD_ARE_YOU_THIS_TIME_ILL_WAGER_A_SPECIAL_SCROLL = new NpcStringId(1801017);
		WERE_GETTING_SERIOUS_NOW_IF_YOU_WIN_AGAIN_ILL_GIVE_YOU_A_SPECIAL_SCROLL = new NpcStringId(1801018);
		YOU_COULD_PROBABLY_ENTER_THE_UNDERWORLD_PRO_LEAGUE = new NpcStringId(1801019);
		EVEN_PROS_CANT_DO_THIS_MUCH_YOURE_AMAZING = new NpcStringId(1801020);
		WHOS_THE_MONSTER_HERE_THIS_TIME_ILL_BET_MY_PRECIOUS_TRANSFORMATION_STICK = new NpcStringId(1801021);
		I_LOST_AGAIN_I_WONT_LOSE_THIS_TIME_IM_BETTING_MY_TRANSFORMATION_STICK = new NpcStringId(1801022);
		LOST_AGAIN_HMPH_NEXT_TIME_ILL_BET_AN_INCREDIBLE_GIFT_WAIT_FOR_IT_IF_YOU_WANT = new NpcStringId(1801023);
		YOURE_TOO_GOOD_NEXT_TIME_ILL_GIVE_YOU_AN_INCREDIBLE_GIFT_PLEASE_WAIT_FOR_YOU = new NpcStringId(1801024);
		MY_PRIDE_CANT_HANDLE_YOU_WINNING_ANYMORE = new NpcStringId(1801025);
		I_WOULD_BE_EMBARRASSING_TO_LOSE_AGAIN_HERE = new NpcStringId(1801026);
		WHATS_YOUR_NAME_IM_GONNA_REMEMBER_YOU = new NpcStringId(1801027);
		PEOPLE_FROM_THE_ABOVE_GROUND_WORLD_ARE_REALLY_GOOD_AT_GAMES = new NpcStringId(1801028);
		YOUVE_PLAYED_A_LOT_IN_THE_UNDERWORLD_HAVENT_YOU = new NpcStringId(1801029);
		IVE_NEVER_MET_SOMEONE_SO_GOOD_BEFORE = new NpcStringId(1801030);
		N13_WINS_IN_A_ROW_YOURE_PRETTY_LUCKY_TODAY_HUH = new NpcStringId(1801031);
		I_NEVER_THOUGHT_I_WOULD_SEE_13_WINS_IN_A_ROW = new NpcStringId(1801032);
		THIS_IS_THE_HIGHEST_RECORD_IN_MY_LIFE_NEXT_TIME_ILL_GIVE_YOU_MY_TREASURE_THE_GOLDEN_JACK_OLANTERN =
				new NpcStringId(1801033);
		EVEN_PROS_CANT_DO_14_WINS_NEXT_TIME_I_WILL_GIVE_YOU_MY_TREASURE_THE_GOLDEN_JACK_OLANTERN =
				new NpcStringId(1801034);
		I_CANT_DO_THIS_ANYMORE_YOU_WIN_I_ACKNOWLEDGE_YOU_AS_THE_BEST_IVE_EVER_MET_IN_ALL_MY_583_YEARS =
				new NpcStringId(1801035);
		PLAYING_ANY_MORE_IS_MEANINGLESS_YOU_WERE_MY_GREATEST_OPPONENT = new NpcStringId(1801036);
		I_WON_THIS_ROUND_IT_WAS_FUN = new NpcStringId(1801037);
		I_WON_THIS_ROUND_IT_WAS_ENJOYABLE = new NpcStringId(1801038);
		ABOVE_WORLD_PEOPLE_ARE_SO_FUN_THEN_SEE_YOU_LATER = new NpcStringId(1801039);
		CALL_ME_AGAIN_NEXT_TIME_I_WANT_TO_PLAY_AGAIN_WITH_YOU = new NpcStringId(1801040);
		YOU_WANNA_PLAY_SOME_MORE_IM_OUT_OF_PRESENTS_BUT_ILL_GIVE_YOU_CANDY = new NpcStringId(1801041);
		WILL_YOU_PLAY_SOME_MORE_I_DONT_HAVE_ANY_MORE_PRESENTS_BUT_I_WILL_GIVE_YOU_CANDY_IF_YOU_WIN =
				new NpcStringId(1801042);
		YOURE_THE_BEST_OUT_OF_ALL_THE_JACKS_GAME_PLAYERS_IVE_EVER_MET_I_GIVE_UP = new NpcStringId(1801043);
		WOWWW_AWESOME_REALLY_I_HAVE_NEVER_MET_SOMEONE_AS_GOOD_AS_YOU_BEFORE_NOW_I_CANT_PLAY_ANYMORE =
				new NpcStringId(1801044);
		S1_HAS_WON_S2_JACKS_GAMES_IN_A_ROW = new NpcStringId(1801045);
		CONGRATULATIONS_S1_HAS_WON_S2_JACKS_GAMES_IN_A_ROW = new NpcStringId(1801046);
		CONGRATULATIONS_ON_GETTING_1ST_PLACE_IN_JACKS_GAME = new NpcStringId(1801047);
		HELLO_IM_BELLDANDY_CONGRATULATIONS_ON_WINNING_1ST_PLACE_IN_JACKS_GAME_IF_YOU_GO_AND_FIND_MY_SIBLING_SKOOLDIE_IN_THE_VILLAGE_YOULL_GET_AN_AMAZING_GIFT_LETS_PLAY_JACKS_GAME_AGAIN =
				new NpcStringId(1801048);
		HMM_YOURE_PLAYING_JACKS_GAME_FOR_THE_FIRST_TIME_HUH_YOU_COULDNT_EVEN_TAKE_OUT_YOUR_CARD_AT_THE_RIGHT_TIME_MY_GOODNESS =
				new NpcStringId(1801049);
		OH_YOURE_NOT_VERY_FAMILIAR_WITH_JACKS_GAME_RIGHT_YOU_DIDNT_TAKE_OUT_YOUR_CARD_AT_THE_RIGHT_TIME =
				new NpcStringId(1801050);
		YOU_HAVE_TO_USE_THE_CARD_SKILL_ON_THE_MASK_BEFORE_THE_GAUGE_ABOVE_MY_HEAD_DISAPPEARS = new NpcStringId(1801051);
		YOU_MUST_USE_THE_CARD_SKILL_ON_THE_MASK_BEFORE_THE_GAUGE_ABOVE_MY_HEAD_DISAPPEARS = new NpcStringId(1801052);
		IF_YOU_SHOW_THE_SAME_CARD_AS_ME_YOU_WIN_IF_THEYRE_DIFFERENT_I_WIN_UNDERSTAND_NOW_LETS_GO =
				new NpcStringId(1801053);
		YOU_WILL_WIN_IF_YOU_SHOW_THE_SAME_CARD_AS_ME_ITS_MY_VICTORY_IF_THE_CARDS_ARE_DIFFERENT_WELL_LETS_START_AGAIN =
				new NpcStringId(1801054);
		ACK_YOU_DIDNT_SHOW_A_CARD_YOU_HAVE_TO_USE_THE_CARD_SKILL_BEFORE_THE_GAUGE_DISAPPEARS_HMPH_THEN_IM_GOING =
				new NpcStringId(1801055);
		AHH_YOU_DIDNT_SHOW_A_CARD_YOU_MUST_USE_THE_CARD_SKILL_AT_THE_RIGHT_TIME_ITS_UNFORTUNATE_THEN_I_WILL_GO_NOW =
				new NpcStringId(1801056);
		LETS_LEARN_ABOUT_THE_JACKS_GAME_TOGETHER_YOU_CAN_PLAY_WITH_ME_3_TIMES = new NpcStringId(1801057);
		LETS_START_SHOW_THE_CARD_YOU_WANT_THE_CARD_SKILL_IS_ATTACHED_TO_THE_MASK = new NpcStringId(1801058);
		YOU_SHOWED_THE_SAME_CARD_AS_ME_SO_YOU_WIN = new NpcStringId(1801059);
		YOU_SHOWED_A_DIFFERENT_CARD_FROM_ME_SO_YOU_LOSE = new NpcStringId(1801060);
		THAT_WAS_PRACTICE_SO_THERES_NO_CANDY_EVEN_IF_YOU_WIN = new NpcStringId(1801061);
		ITS_UNFORTUNATE_LETS_PRACTICE_ONE_MORE_TIME = new NpcStringId(1801062);
		YOU_GOTTA_SHOW_THE_CARD_AT_THE_RIGHT_TIME_USE_THE_CARD_SKILL_YOU_WANT_BEFORE_THE_GAUGE_ABOVE_MY_HEAD_DISAPPEARS =
				new NpcStringId(1801063);
		THE_CARD_SKILLS_ARE_ATTACHED_TO_THE_JACK_OLANTERN_MASK_RIGHT_THATS_WHAT_YOU_USE = new NpcStringId(1801064);
		YOU_WIN_IF_YOU_SHOW_THE_SAME_CARD_AS_ME_AND_I_WIN_IF_THE_CARDS_ARE_DIFFERENT_OK_LETS_GO =
				new NpcStringId(1801065);
		YOU_DIDNT_SHOW_A_CARD_AGAIN_WELL_TRY_AGAIN_LATER_IM_GONNA_GO_NOW = new NpcStringId(1801066);
		NOW_DO_YOU_UNDERSTAND_A_LITTLE_ABOUT_JACKS_GAME_THE_REAL_GAMES_WITH_ULDIE_AND_BELLDANDY_WELL_SEE_YOU_LATER =
				new NpcStringId(1801067);
		HAHAHAHA = new NpcStringId(1801068);
		WHERE_ARE_YOU_LOOKING = new NpcStringId(1801069);
		IM_RIGHT_HERE = new NpcStringId(1801070);
		ANNOYING_CONCENTRATION_ATTACKS_ARE_DISRUPTING_VALAKASS_CONCENTRATIONNIF_IT_CONTINUES_YOU_MAY_GET_A_GREAT_OPPORTUNITY =
				new NpcStringId(1801071);
		SOME_WARRIORS_BLOW_HAS_LEFT_A_HUGE_GASH_BETWEEN_THE_GREAT_SCALES_OF_VALAKASNVALAKASS_P_DEF_IS_GREATLY_DECREASED =
				new NpcStringId(1801072);
		ANNOYING_CONCENTRATION_ATTACKS_OVERWHELMED_VALAKAS_MAKING_IT_FORGET_ITS_RAGE_AND_BECOME_DISTRACTED =
				new NpcStringId(1801073);
		LONG_RANGE_CONCENTRATION_ATTACKS_HAVE_ENRAGED_VALAKASNIF_YOU_CONTINUE_IT_MAY_BECOME_A_DANGEROUS_SITUATION =
				new NpcStringId(1801074);
		BECAUSE_THE_COWARDLY_COUNTERATTACKS_CONTINUED_VALAKASS_FURY_HAS_REACHED_ITS_MAXIMUMNVALAKASS_P_ATK_IS_GREATLY_INCREASED =
				new NpcStringId(1801075);
		VALAKAS_HAS_BEEN_ENRAGED_BY_THE_LONG_RANGE_CONCENTRATION_ATTACKS_AND_IS_COMING_TOWARD_ITS_TARGET_WITH_EVEN_GREATER_ZEAL =
				new NpcStringId(1801076);
		LISTEN_OH_TANTAS_I_HAVE_RETURNED_THE_PROPHET_YUGOROS_OF_THE_BLACK_ABYSS_IS_WITH_ME_SO_DO_NOT_BE_AFRAID =
				new NpcStringId(1801077);
		WELCOME_S1_LET_US_SEE_IF_YOU_HAVE_BROUGHT_A_WORTHY_OFFERING_FOR_THE_BLACK_ABYSS = new NpcStringId(1801078);
		WHAT_A_FORMIDABLE_FOE_BUT_I_HAVE_THE_ABYSS_WEED_GIVEN_TO_ME_BY_THE_BLACK_ABYSS_LET_ME_SEE =
				new NpcStringId(1801079);
		MUHAHAHA_AH_THIS_BURNING_SENSATION_IN_MY_MOUTH_THE_POWER_OF_THE_BLACK_ABYSS_IS_REVIVING_ME =
				new NpcStringId(1801080);
		NO_HOW_DARE_YOU_STOP_ME_FROM_USING_THE_ABYSS_WEED_DO_YOU_KNOW_WHAT_YOU_HAVE_DONE = new NpcStringId(1801081);
		A_LIMP_CREATURE_LIKE_THIS_IS_UNWORTHY_TO_BE_AN_OFFERING_YOU_HAVE_NO_RIGHT_TO_SACRIFICE_TO_THE_BLACK_ABYSS =
				new NpcStringId(1801082);
		LISTEN_OH_TANTAS_THE_BLACK_ABYSS_IS_FAMISHED_FIND_SOME_FRESH_OFFERINGS = new NpcStringId(1801083);
		AH_HOW_COULD_I_LOSE_OH_BLACK_ABYSS_RECEIVE_ME = new NpcStringId(1801084);
		ALARM_SYSTEM_DESTROYED_INTRUDER_EXCLUDED = new NpcStringId(1801085);
		BEGIN_STAGE_4 = new NpcStringId(1801089);
		TIME_REMAINING_UNTIL_NEXT_BATTLE = new NpcStringId(1801090);
		THE_BEAST_ATE_THE_FEED_BUT_IT_ISNT_SHOWING_A_RESPONSE_PERHAPS_BECAUSE_ITS_ALREADY_FULL =
				new NpcStringId(1801091);
		THE_BEAST_SPIT_OUT_THE_FEED_INSTEAD_OF_EATING_IT = new NpcStringId(1801092);
		THE_BEAST_SPIT_OUT_THE_FEED_AND_IS_INSTEAD_ATTACKING_YOU = new NpcStringId(1801093);
		S1_IS_LEAVING_YOU_BECAUSE_YOU_DONT_HAVE_ENOUGH_GOLDEN_SPICES = new NpcStringId(1801094);
		S1_IS_LEAVING_YOU_BECAUSE_YOU_DONT_HAVE_ENOUGH_CRYSTAL_SPICES = new NpcStringId(1801095);
		S1_MAY_THE_PROTECTION_OF_THE_GODS_BE_UPON_YOU = new NpcStringId(1801096);
		FREYA_HAS_STARTED_TO_MOVE = new NpcStringId(1801097);
		HOW_COULD_I_FALL_IN_A_PLACE_LIKE_THIS = new NpcStringId(1801098);
		I_CAN_FINALLY_TAKE_A_BREATHER_BY_THE_WAY_WHO_ARE_YOU_HMM_I_THINK_I_KNOW_WHO_SENT_YOU = new NpcStringId(1801099);
		S1_OF_BALANCE = new NpcStringId(1801100);
		SWIFT_S2 = new NpcStringId(1801101);
		S1_OF_BLESSING = new NpcStringId(1801102);
		SHARP_S2 = new NpcStringId(1801103);
		USEFUL_S2 = new NpcStringId(1801104);
		RECKLESS_S2 = new NpcStringId(1801105);
		ALPEN_KOOKABURRA = new NpcStringId(1801106);
		ALPEN_COUGAR = new NpcStringId(1801107);
		ALPEN_BUFFALO = new NpcStringId(1801108);
		ALPEN_GRENDEL = new NpcStringId(1801109);
		BATTLE_END_LIMIT_TIME = new NpcStringId(1801110);
		STRONG_MAGIC_POWER_CAN_BE_FELT_FROM_SOMEWHERE = new NpcStringId(1801111);
		HOW_DARE_YOU_ATTACK_MY_RECRUITS = new NpcStringId(1801112);
		WHO_IS_DISRUPTING_THE_ORDER = new NpcStringId(1801113);
		THE_DRILLMASTER_IS_DEAD = new NpcStringId(1801114);
		LINE_UP_THE_RANKS = new NpcStringId(1801115);
		I_BROUGHT_THE_FOOD = new NpcStringId(1801116);
		COME_AND_EAT = new NpcStringId(1801117);
		LOOKS_DELICIOUS = new NpcStringId(1801118);
		LETS_GO_EAT = new NpcStringId(1801119);
		ARCHER_GIVE_YOUR_BREATH_FOR_THE_INTRUDER = new NpcStringId(1801120);
		MY_KNIGHTS_SHOW_YOUR_LOYALTY = new NpcStringId(1801121);
		I_CAN_TAKE_IT_NO_LONGER = new NpcStringId(1801122);
		ARCHER_HEED_MY_CALL = new NpcStringId(1801123);
		THE_SPACE_FEELS_LIKE_ITS_GRADUALLY_STARTING_TO_SHAKE = new NpcStringId(1801124);
		I_CAN_NO_LONGER_STAND_BY = new NpcStringId(1801125);
		TAKLACAN_IS_GATHERING_ITS_STRENGTH_AND_LAUNCHING_AN_ATTACK = new NpcStringId(1801126);
		TAKLACAN_RECEIVED_COKRAKON_AND_BECAME_WEAKER = new NpcStringId(1801127);
		COKRAKONS_POWER_CAN_BE_FELT_NEARBY = new NpcStringId(1801128);
		TAKLACAN_IS_PREPARING_TO_HIDE_ITSELF = new NpcStringId(1801129);
		TAKLACAN_DISAPPEARS_INTO_THE_SPACE_OF_FUTILITY_WITH_A_ROAR = new NpcStringId(1801130);
		TORUMBAS_BODY_IS_STARTING_TO_MOVE = new NpcStringId(1801131);
		A_RESPONSE_CAN_BE_FELT_FROM_TORUMBAS_TOUGH_SKIN = new NpcStringId(1801132);
		A_MARK_REMAINS_ON_TORUMBAS_TOUGH_SKIN = new NpcStringId(1801133);
		THE_LIGHT_IS_BEGINNING_TO_WEAKEN_IN_TORUMBAS_EYES = new NpcStringId(1801134);
		TORUMBAS_LEFT_LEG_WAS_DAMAGED = new NpcStringId(1801135);
		TORUMBAS_RIGHT_LEG_WAS_DAMAGED = new NpcStringId(1801136);
		TORUMBAS_LEFT_ARM_WAS_DAMAGED = new NpcStringId(1801137);
		TORUMBAS_RIGHT_ARM_WAS_DAMAGED = new NpcStringId(1801138);
		A_DEEP_WOUND_APPEARED_ON_TORUMBAS_TOUGH_SKIN = new NpcStringId(1801139);
		THE_LIGHT_IS_SLOWLY_FADING_FROM_TORUMBAS_EYES = new NpcStringId(1801140);
		TORUMBA_IS_PREPARING_TO_HIDE_ITS_BODY = new NpcStringId(1801141);
		TORUMBA_DISAPPEARED_INTO_HIS_SPACE = new NpcStringId(1801142);
		TORUMBA_IS_PREPARING_TO_HIDE_ITSELF_IN_THE_TWISTED_SPACE = new NpcStringId(1801143);
		DOPAGEN_HAS_STARTED_TO_MOVE = new NpcStringId(1801144);
		LEPTILIKONS_ENERGY_FEELS_LIKE_ITS_BEING_CONDENSED = new NpcStringId(1801145);
		DOPAGEN_IS_PREPARING_TO_HIDE_ITSELF_WITH_A_STRANGE_SCENT = new NpcStringId(1801146);
		DOPAGEN_IS_PREPARING_TO_HIDE_ITSELF = new NpcStringId(1801147);
		DOPAGEN_IS_DISAPPEARING_IN_BETWEEN_THE_TWISTED_SPACE = new NpcStringId(1801148);
		MAGUEN_APPEARANCE = new NpcStringId(1801149);
		ENOUGH_MAGUEN_PLASMA_BISTAKON_HAVE_GATHERED = new NpcStringId(1801150);
		ENOUGH_MAGUEN_PLASMA_COKRAKON_HAVE_GATHERED = new NpcStringId(1801151);
		ENOUGH_MAGUEN_PLASMA_LEPTILIKON_HAVE_GATHERED = new NpcStringId(1801152);
		THE_PLASMAS_HAVE_FILLED_THE_AEROSCOPE_AND_ARE_HARMONIZED = new NpcStringId(1801153);
		THE_PLASMAS_HAVE_FILLED_THE_AEROSCOPE_BUT_THEY_ARE_RAMMING_INTO_EACH_OTHER_EXPLODING_AND_DYING =
				new NpcStringId(1801154);
		AMAZING_S1_TOOK_100_OF_THESE_SOUL_STONE_FRAGMENTS_WHAT_A_COMPLETE_SWINDLER = new NpcStringId(1801155);
		HMM_HEY_DID_YOU_GIVE_S1_SOMETHING_BUT_IT_WAS_JUST_1_HAHA = new NpcStringId(1801156);
		S1_PULLED_ONE_WITH_S2_DIGITS_LUCKY_NOT_BAD = new NpcStringId(1801157);
		ITS_BETTER_THAN_LOSING_IT_ALL_RIGHT_OR_DOES_THIS_FEEL_WORSE = new NpcStringId(1801158);
		AHEM_S1_HAS_NO_LUCK_AT_ALL_TRY_PRAYING = new NpcStringId(1801159);
		AH_ITS_OVER_WHAT_KIND_OF_GUY_IS_THAT_DAMN_FINE_YOU_S1_TAKE_IT_AND_GET_OUTTA_HERE = new NpcStringId(1801160);
		A_BIG_PIECE_IS_MADE_UP_OF_LITTLE_PIECES_SO_HERES_A_LITTLE_PIECE = new NpcStringId(1801161);
		YOU_DONT_FEEL_BAD_RIGHT_ARE_YOU_SAD_BUT_DONT_CRY = new NpcStringId(1801162);
		OK_WHOS_NEXT_IT_ALL_DEPENDS_ON_YOUR_FATE_AND_LUCK_RIGHT_AT_LEAST_COME_AND_TAKE_A_LOOK =
				new NpcStringId(1801163);
		NO_ONE_ELSE_DONT_WORRY_I_DONT_BITE_HAHA = new NpcStringId(1801164);
		THERE_WAS_SOMEONE_WHO_WON_10000_FROM_ME_A_WARRIOR_SHOULDNT_JUST_BE_GOOD_AT_FIGHTING_RIGHT_YOUVE_GOTTA_BE_GOOD_IN_EVERYTHING =
				new NpcStringId(1801165);
		OK_MASTER_OF_LUCK_THATS_YOU_HAHA_WELL_ANYONE_CAN_COME_AFTER_ALL = new NpcStringId(1801166);
		SHEDDING_BLOOD_IS_A_GIVEN_ON_THE_BATTLEFIELD_AT_LEAST_ITS_SAFE_HERE = new NpcStringId(1801167);
		GASP = new NpcStringId(1801168);
		IS_IT_STILL_LONG_OFF = new NpcStringId(1801169);
		IS_ERMIAN_WELL_EVEN_I_CANT_BELIEVE_THAT_I_SURVIVED_IN_A_PLACE_LIKE_THIS = new NpcStringId(1801170);
		I_DONT_KNOW_HOW_LONG_ITS_BEEN_SINCE_I_PARTED_COMPANY_WITH_YOU_TIME_DOESNT_SEEM_TO_MOVE_IT_JUST_FEELS_TOO_LONG =
				new NpcStringId(1801171);
		SORRY_TO_SAY_THIS_BUT_THE_PLACE_YOU_STRUCK_ME_BEFORE_NOW_HURTS_GREATLY = new NpcStringId(1801172);
		UGH_IM_SORRY_IT_LOOKS_LIKE_THIS_IS_IT_FOR_ME_I_WANTED_TO_LIVE_AND_SEE_MY_FAMILY = new NpcStringId(1801173);
		WHERE_ARE_YOU_I_CANT_SEE_ANYTHING = new NpcStringId(1801174);
		WHERE_ARE_YOU_REALLY_I_CANT_FOLLOW_YOU_LIKE_THIS = new NpcStringId(1801175);
		IM_SORRY_THIS_IS_IT_FOR_ME = new NpcStringId(1801176);
		SOB_TO_SEE_ERMIAN_AGAIN_CAN_I_GO_TO_MY_FAMILY_NOW = new NpcStringId(1801177);
		MY_ENERGY_WAS_RECOVERED_SO_QUICKLY_THANKS_S1 = new NpcStringId(1801183);
		IM_STARVING = new NpcStringId(1801187);
		MAGIC_POWER_SO_STRONG_THAT_IT_COULD_MAKE_YOU_LOSE_YOUR_MIND_CAN_BE_FELT_FROM_SOMEWHERE =
				new NpcStringId(1801189);
		EVEN_THOUGH_YOU_BRING_SOMETHING_CALLED_A_GIFT_AMONG_YOUR_HUMANS_IT_WOULD_JUST_BE_PROBLEMATIC_FOR_ME =
				new NpcStringId(1801190);
		I_JUST_DONT_KNOW_WHAT_EXPRESSION_I_SHOULD_HAVE_IT_APPEARED_ON_ME_ARE_HUMANS_EMOTIONS_LIKE_THIS_FEELING =
				new NpcStringId(1801191);
		THE_FEELING_OF_THANKS_IS_JUST_TOO_MUCH_DISTANT_MEMORY_FOR_ME = new NpcStringId(1801192);
		BUT_I_KIND_OF_MISS_IT_LIKE_I_HAD_FELT_THIS_FEELING_BEFORE = new NpcStringId(1801193);
		I_AM_ICE_QUEEN_FREYA_THIS_FEELING_AND_EMOTION_ARE_NOTHING_BUT_A_PART_OF_MELISSAA_MEMORIES =
				new NpcStringId(1801194);
		DEAR_S1_THINK_OF_THIS_AS_MY_APPRECIATION_FOR_THE_GIFT_TAKE_THIS_WITH_YOU_THERES_NOTHING_STRANGE_ABOUT_IT_ITS_JUST_A_BIT_OF_MY_CAPRICIOUSNESS =
				new NpcStringId(1801195);
		THE_KINDNESS_OF_SOMEBODY_IS_NOT_A_BAD_FEELING_DEAR_S1_I_WILL_TAKE_THIS_GIFT_OUT_OF_RESPECT_YOUR_KINDNESS =
				new NpcStringId(1801196);
		FIGHTER = new NpcStringId(1811000);
		WARRIOR = new NpcStringId(1811001);
		GLADIATOR = new NpcStringId(1811002);
		WARLORD = new NpcStringId(1811003);
		KNIGHT = new NpcStringId(1811004);
		PALADIN = new NpcStringId(1811005);
		DARK_AVENGER = new NpcStringId(1811006);
		ROGUE = new NpcStringId(1811007);
		TREASURE_HUNTER = new NpcStringId(1811008);
		HAWKEYE = new NpcStringId(1811009);
		MAGE = new NpcStringId(1811010);
		WIZARD = new NpcStringId(1811011);
		SORCERER = new NpcStringId(1811012);
		NECROMANCER = new NpcStringId(1811013);
		WARLOCK = new NpcStringId(1811014);
		CLERIC = new NpcStringId(1811015);
		BISHOP = new NpcStringId(1811016);
		PROPHET = new NpcStringId(1811017);
		ELVEN_FIGHTER = new NpcStringId(1811018);
		ELVEN_KNIGHT = new NpcStringId(1811019);
		TEMPLE_KNIGHT = new NpcStringId(1811020);
		SWORD_SINGER = new NpcStringId(1811021);
		ELVEN_SCOUT = new NpcStringId(1811022);
		PLAIN_WALKER = new NpcStringId(1811023);
		SILVER_RANGER = new NpcStringId(1811024);
		ELVEN_MAGE = new NpcStringId(1811025);
		ELVEN_WIZARD = new NpcStringId(1811026);
		SPELL_SINGER = new NpcStringId(1811027);
		ELEMENTAL_SUMMONER = new NpcStringId(1811028);
		ORACLE = new NpcStringId(1811029);
		ELDER = new NpcStringId(1811030);
		DARK_FIGHTER = new NpcStringId(1811031);
		PALACE_KNIGHT = new NpcStringId(1811032);
		SHILLIEN_KNIGHT = new NpcStringId(1811033);
		BLADE_DANCER = new NpcStringId(1811034);
		ASSASSIN = new NpcStringId(1811035);
		ABYSS_WALKER = new NpcStringId(1811036);
		PHANTOM_RANGER = new NpcStringId(1811037);
		DARK_MAGE = new NpcStringId(1811038);
		DARK_WIZARD = new NpcStringId(1811039);
		SPELLHOWLER = new NpcStringId(1811040);
		PHANTOM_SUMMONER = new NpcStringId(1811041);
		SHILLIEN_ORACLE = new NpcStringId(1811042);
		SHILLIEN_ELDER = new NpcStringId(1811043);
		ORC_FIGHTER = new NpcStringId(1811044);
		ORC_RAIDER = new NpcStringId(1811045);
		DESTROYER = new NpcStringId(1811046);
		ORC_MONK = new NpcStringId(1811047);
		TYRANT = new NpcStringId(1811048);
		ORC_MAGE = new NpcStringId(1811049);
		ORC_SHAMAN = new NpcStringId(1811050);
		OVERLORD = new NpcStringId(1811051);
		WARCRYER = new NpcStringId(1811052);
		DWARVEN_FIGHTER = new NpcStringId(1811053);
		SCAVENGER = new NpcStringId(1811054);
		BOUNTY_HUNTER = new NpcStringId(1811055);
		ARTISAN = new NpcStringId(1811056);
		WARSMITH = new NpcStringId(1811057);
		DUELIST = new NpcStringId(1811088);
		DREADNOUGHT = new NpcStringId(1811089);
		PHOENIX_KNIGHT = new NpcStringId(1811090);
		HELL_KNIGHT = new NpcStringId(1811091);
		SAGITTARIUS = new NpcStringId(1811092);
		ADVENTURER = new NpcStringId(1811093);
		ARCHMAGE = new NpcStringId(1811094);
		SOULTAKER = new NpcStringId(1811095);
		ARCANA_LORD = new NpcStringId(1811096);
		CARDINAL = new NpcStringId(1811097);
		HIEROPHANT = new NpcStringId(1811098);
		EVAS_TEMPLAR = new NpcStringId(1811099);
		SWORD_MUSE = new NpcStringId(1811100);
		WIND_RIDER = new NpcStringId(1811101);
		MOONLIGHT_SENTINEL = new NpcStringId(1811102);
		MYSTIC_MUSE = new NpcStringId(1811103);
		ELEMENTAL_MASTER = new NpcStringId(1811104);
		EVAS_SAINT = new NpcStringId(1811105);
		SHILLIEN_TEMPLAR = new NpcStringId(1811106);
		SPECTRAL_DANCER = new NpcStringId(1811107);
		GHOST_HUNTER = new NpcStringId(1811108);
		GHOST_SENTINEL = new NpcStringId(1811109);
		STORM_SCREAMER = new NpcStringId(1811110);
		SPECTRAL_MASTER = new NpcStringId(1811111);
		SHILLIEN_SAINT = new NpcStringId(1811112);
		TITAN = new NpcStringId(1811113);
		GRAND_KHAVATARI = new NpcStringId(1811114);
		DOMINATOR = new NpcStringId(1811115);
		DOOM_CRYER = new NpcStringId(1811116);
		FORTUNE_SEEKER = new NpcStringId(1811117);
		MAESTRO = new NpcStringId(1811118);
		KAMAEL_SOLDIER = new NpcStringId(1811123);
		TROOPER = new NpcStringId(1811125);
		WARDER = new NpcStringId(1811126);
		BERSERKER = new NpcStringId(1811127);
		SOUL_BREAKER = new NpcStringId(1811128);
		ARBALESTER = new NpcStringId(1811130);
		DOOMBRINGER = new NpcStringId(1811131);
		SOUL_HOUND = new NpcStringId(1811132);
		TRICKSTER = new NpcStringId(1811134);
		INSPECTOR = new NpcStringId(1811135);
		JUDICATOR = new NpcStringId(1811136);
		WHOS_THERE_IF_YOU_DISTURB_THE_TEMPER_OF_THE_GREAT_LAND_DRAGON_ANTHARAS_I_WILL_NEVER_FORGIVE_YOU =
				new NpcStringId(1811137);
		KEHAHAHA_I_CAPTURED_SANTA_CLAUS_THERE_WILL_BE_NO_GIFTS_THIS_YEAR = new NpcStringId(1900000);
		WELL_I_WIN_RIGHT = new NpcStringId(1900003);
		NOW_ALL_OF_YOU_LOSERS_GET_OUT_OF_HERE = new NpcStringId(1900004);
		I_GUESS_YOU_CAME_TO_RESCUE_SANTA_BUT_YOU_PICKED_THE_WRONG_OPPONENT = new NpcStringId(1900006);
		OH_NOT_BAD = new NpcStringId(1900007);
		AGH_THATS_NOT_WHAT_I_MEANT_TO_DO = new NpcStringId(1900008);
		CURSE_YOU_HUH_WHAT = new NpcStringId(1900009);
		SHALL_I_GO_TO_SEE_IF_SANTA_IS_STILL_THERE_HEHE = new NpcStringId(1900015);
		SANTA_CAN_GIVE_NICE_PRESENTS_ONLY_IF_HES_RELEASED_FROM_THE_TURKEY = new NpcStringId(1900017);
		OH_HO_HO_OH_HO_HO_THANK_YOU_EVERYONE_I_WILL_REPAY_YOU_FOR_SURE = new NpcStringId(1900018);
		MERRY_CHRISTMAS_WELL_DONE = new NpcStringId(1900019);
		S_I_HAVE_PREPARED_A_GIFT_FOR_YOU = new NpcStringId(1900021);
		I_HAVE_A_GIFT_FOR_S = new NpcStringId(1900022);
		TAKE_A_LOOK_AT_THE_INVENTORY_PERHAPS_THERE_WILL_BE_A_BIG_PRESENT = new NpcStringId(1900024);
		WHEN_ARE_YOU_GOING_TO_STOP_IM_SLOWLY_GETTING_TIRED_OF_THIS = new NpcStringId(1900026);
		MESSAGE_FROM_SANTA_CLAUS_MANY_BLESSINGS_TO_S_WHO_SAVED_ME = new NpcStringId(1900027);
		HOW_DARE_YOU_AWAKEN_ME_FEEL_THE_PAIN_OF_THE_FLAMES = new NpcStringId(1900028);
		WHO_DARES_OPPOSE_THE_MAJESTY_OF_FIRE = new NpcStringId(1900029);
		OH_OUCH_NO_NOT_THERE_NOT_MY_BEAD = new NpcStringId(1900030);
		CO_COLD_THATS_COLD_ACK_ACK = new NpcStringId(1900031);
		PLEASE_S_DONT_HIT_ME_PLEASE = new NpcStringId(1900032);
		KUAAANNGGG_SHAKE_IN_FEAR = new NpcStringId(1900033);
		IF_YOU_ATTACK_ME_RIGHT_NOW_YOURE_REALLY_GOING_TO_GET_IT = new NpcStringId(1900034);
		JUST_YOU_WAIT_IM_GOING_TO_SHOW_YOU_MY_KILLER_TECHNIQUE = new NpcStringId(1900035);
		YOU_DONT_DARE_ATTACK_ME = new NpcStringId(1900036);
		ITS_DIFFERENT_FROM_THE_SPIRIT_OF_FIRE_ITS_NOT_THE_SPIRIT_OF_FIRE_FEEL_MY_WRATH = new NpcStringId(1900037);
		COLD_THIS_PLACE_IS_THIS_WHERE_I_DIE = new NpcStringId(1900038);
		MY_BODY_IS_COOLING_OH_GRAN_KAIN_FORGIVE_ME = new NpcStringId(1900039);
		IDIOT_I_ONLY_INCUR_DAMAGE_FROM_BARE_HANDED_ATTACKS = new NpcStringId(1900040);
		IM_OUT_OF_CANDY_ILL_GIVE_YOU_MY_TOY_CHEST_INSTEAD = new NpcStringId(1900051);
		SINCE_IM_OUT_OF_CANDY_I_WILL_GIVE_YOU_MY_TOY_CHEST_INSTEAD = new NpcStringId(1900052);
		YOURE_TOO_GOOD_NEXT_TIME_ILL_GIVE_YOU_AN_INCREDIBLE_GIFT_PLEASE_WAIT_FOR_IT = new NpcStringId(1900060);
		I_WOULD_BE_EMBARRASSED_TO_LOSE_AGAIN_HERE = new NpcStringId(1900062);
		EVEN_PROS_CANT_DO_14_WINS_IN_A_ROW_NEXT_TIME_ILL_GIVE_YOU_MY_TREASURE_THE_GOLDEN_JACK_OLANTERN =
				new NpcStringId(1900070);
		I_CANT_DO_THIS_ANYMORE_YOU_WIN_IN_ALL_MY_583_YEARS_YOURE_THE_BEST_THAT_IVE_SEEN = new NpcStringId(1900071);
		S_HAS_WON_S_JACKS_GAMES_IN_A_ROW = new NpcStringId(1900081);
		CONGRATULATIONS_S_HAS_WON_S_JACKS_GAMES_IN_A_ROW = new NpcStringId(1900082);
		HELLO_IM_BELLDANDY_CONGRATULATIONS_ON_GETTING_1ST_PLACE_IN_JACKS_GAME_IF_YOU_GO_AND_FIND_MY_SIBLING_SKOOLDIE_IN_THE_VILLAGE_YOULL_GET_AN_AMAZING_GIFT_LETS_PLAY_JACKS_GAME_AGAIN =
				new NpcStringId(1900084);
		YAWN_AHH_HELLO_NICE_WEATHER_WERE_HAVING = new NpcStringId(1900104);
		AH_IM_HUNGRY_DO_YOU_HAVE_ANY_BABY_FOOD_THATS_WHAT_I_NEED_TO_EAT_TO_GET_BIGGER = new NpcStringId(1900105);
		I_GOTTA_GROW_UP_FAST_I_WANT_TO_PULL_SANTAS_SLED_TOO = new NpcStringId(1900106);
		YUMMY_I_THINK_I_CAN_GROW_UP_NOW = new NpcStringId(1900107);
		THANKS_TO_YOU_I_GREW_UP_INTO_A_BOY_RUDOLPH = new NpcStringId(1900108);
		ITS_GREAT_WEATHER_FOR_RUNNING_AROUND = new NpcStringId(1900109);
		WHATLL_I_BE_WHEN_I_GROW_UP_I_WONDER = new NpcStringId(1900110);
		IF_YOU_TAKE_GOOD_CARE_OF_ME_ILL_NEVER_FORGET_IT = new NpcStringId(1900111);
		PLEASE_PET_ME_LOVINGLY_YOU_CAN_USE_THE_HAND_OF_WARMTH_SKILL_UNDER_THE_ACTION_TAB = new NpcStringId(1900112);
		I_FEEL_GREAT_THANK_YOU = new NpcStringId(1900113);
		WOO_WHAT_A_GOOD_FEELING_I_GOTTA_GROW_MORE_AND_MORE = new NpcStringId(1900114);
		OH_YUMMY_IF_I_KEEP_EATING_THIS_I_THINK_I_CAN_GROW_UP_ALL_THE_WAY = new NpcStringId(1900115);
		YUM_YUM_DELICIOUS_GIVE_ME_MORE_OF_THIS = new NpcStringId(1900116);
		WOW_THIS_TASTE_THERES_A_WHOLE_NEW_WORLD_IN_MY_MOUTH = new NpcStringId(1900117);
		YEAH_ITS_SO_DELICIOUS_THIS_IS_THAT_STAR_FOOD = new NpcStringId(1900118);
		PAY_SOME_ATTENTION_TO_ME_HMPH = new NpcStringId(1900119);
		THANK_YOU_I_WAS_ABLE_TO_GROW_UP_INTO_AN_ADULT_HERE_IS_MY_GIFT = new NpcStringId(1900120);
		THANK_YOU_S_NOW_I_CAN_PULL_THE_SLED = new NpcStringId(1900121);
		S_THANK_YOU_FOR_TAKING_CARE_OF_ME_ALL_THIS_TIME_I_ENJOYED_IT_VERY_MUCH = new NpcStringId(1900122);
		S_IT_WONT_BE_LONG_NOW_UNTIL_IT_BECOMES_TIME_TO_PULL_THE_SLED_ITS_TOO_BAD = new NpcStringId(1900123);
		I_MUST_RETURN_TO_SANTA_NOW_THANK_YOU_FOR_EVERYTHING = new NpcStringId(1900124);
		HELLO_IM_A_GIRL_RUDOLPH_I_WAS_ABLE_TO_FIND_MY_TRUE_SELF_THANKS_TO_YOU = new NpcStringId(1900125);
		THIS_IS_MY_GIFT_OF_THANKS_THANK_YOU_FOR_TAKING_CARE_OF_ME = new NpcStringId(1900126);
		S_I_WAS_ALWAYS_GRATEFUL = new NpcStringId(1900127);
		IM_A_LITTLE_SAD_ITS_TIME_TO_LEAVE_NOW = new NpcStringId(1900128);
		S_THE_TIME_HAS_COME_FOR_ME_TO_RETURN_TO_MY_HOME = new NpcStringId(1900129);
		S_THANK_YOU = new NpcStringId(1900130);
		HAHAHA_I_CAPTURED_SANTA_CLAUS_HUH_WHERE_IS_THIS_WHO_ARE_YOU = new NpcStringId(1900131);
		I_LOST_AT_ROCK_PAPER_SCISSORS_AND_WAS_TAKEN_CAPTIVE_I_MIGHT_AS_WELL_TAKE_OUT_MY_ANGER_ON_YOU_HUH_WHAT =
				new NpcStringId(1900132);
		NOTHINGS_WORKING_IM_LEAVING_ILL_DEFINITELY_CAPTURE_SANTA_AGAIN_JUST_YOU_WAIT = new NpcStringId(1900133);
		I_MUST_RAISE_RUDOLPH_QUICKLY_THIS_YEARS_CHRISTMAS_GIFTS_HAVE_TO_BE_DELIVERED = new NpcStringId(1900134);
		MERRY_CHRISTMAS_THANKS_TO_YOUR_EFFORTS_IN_RAISING_RUDOLPH_THE_GIFT_DELIVERY_WAS_A_SUCCESS =
				new NpcStringId(1900135);
		IN_10_MINUTES_IT_WILL_BE_1_HOUR_SINCE_YOU_STARTED_RAISING_ME = new NpcStringId(1900136);
		AFTER_5_MINUTES_IF_MY_FULL_FEELING_AND_AFFECTION_LEVEL_REACH_99_I_CAN_GROW_BIGGER = new NpcStringId(1900137);
		LUCKY_IM_LUCKY_THE_SPIRIT_THAT_LOVES_ADENA = new NpcStringId(1900139);
		LUCKY_I_WANT_TO_EAT_ADENA_GIVE_IT_TO_ME = new NpcStringId(1900140);
		LUCKY_IF_I_EAT_TOO_MUCH_ADENA_MY_WINGS_DISAPPEAR = new NpcStringId(1900141);
		YUMMY_THANKS_LUCKY = new NpcStringId(1900142);
		GRRRR_YUCK = new NpcStringId(1900143);
		LUCKY_THE_ADENA_IS_SO_YUMMY_IM_GETTING_BIGGER = new NpcStringId(1900144);
		LUCKY_NO_MORE_ADENA_OH_IM_SO_HEAVY = new NpcStringId(1900145);
		LUCKY_IM_FULL_THANKS_FOR_THE_YUMMY_ADENA_OH_IM_SO_HEAVY = new NpcStringId(1900146);
		LUCKY_IT_WASNT_ENOUGH_ADENA_ITS_GOTTA_BE_AT_LEAST_S = new NpcStringId(1900147);
		OH_MY_WINGS_DISAPPEARED_ARE_YOU_GONNA_HIT_ME_IF_YOU_HIT_ME_ILL_THROW_UP_EVERYTHING_THAT_I_ATE =
				new NpcStringId(1900148);
		OH_MY_WINGS_ACK_ARE_YOU_GONNA_HIT_ME_SCARY_SCARY_IF_YOU_HIT_ME_SOMETHING_BAD_WILL_HAPPEN =
				new NpcStringId(1900149);
		THE_EVIL_LAND_DRAGON_ANTHARAS_HAS_BEEN_DEFEATED = new NpcStringId(1900150);
		THE_EVIL_FIRE_DRAGON_VALAKAS_HAS_BEEN_DEFEATED = new NpcStringId(1900151);
		TO_SERVE_HIM_NOW_MEANS_YOU_WILL_BE_ABLE_TO_ESCAPE_A_WORSE_SITUATION = new NpcStringId(1900152);
		OH_GODDESS_OF_DESTRUCTION_FORGIVE_US = new NpcStringId(1900153);
		WHEN_THE_SKY_TURNS_BLOOD_RED_AND_THE_EARTH_BEGINS_TO_CRUMBLE_FROM_THE_DARKNESS_SHE_WILL_RETURN =
				new NpcStringId(1900154);
		EARTH_ENERGY_IS_GATHERING_NEAR_ANTHARASS_LEGS = new NpcStringId(1900155);
		ANTHARAS_STARTS_TO_ABSORB_THE_EARTH_ENERGY = new NpcStringId(1900156);
		ANTHARAS_RAISES_ITS_THICK_TAIL = new NpcStringId(1900157);
		YOU_ARE_OVERCOME_BY_THE_STRENGTH_OF_ANTHARAS = new NpcStringId(1900158);
		ANTHARASS_EYES_ARE_FILLED_WITH_RAGE = new NpcStringId(1900159);
		S1_I_CAN_FEEL_THEIR_PRESENCE_FROM_YOU = new NpcStringId(1900160);
		S1_BRETHREN_COME_TO_MY_SIDE_AND_FOLLOW_ME = new NpcStringId(1900161);
		ANTHARAS_ROARS = new NpcStringId(1900162);
		FLAME_ENERGY_IS_BEING_DIRECTED_TOWARDS_VALAKAS = new NpcStringId(1900163);
		YOU_ARE_OVERCOME_BY_THE_STRENGTH_OF_VALAKAS = new NpcStringId(1900164);
		VALAKASS_TAIL_FLAILS_DANGEROUSLY = new NpcStringId(1900165);
		VALAKAS_RAISES_ITS_TAIL = new NpcStringId(1900166);
		VALAKAS_STARTS_TO_ABSORB_THE_FLAME_ENERGY = new NpcStringId(1900167);
		VALAKAS_LOOKS_TO_ITS_LEFT = new NpcStringId(1900168);
		VALAKAS_LOOKS_TO_ITS_RIGHT = new NpcStringId(1900169);
		BY_MY_AUTHORITY_I_COMMAND_YOU_CREATURE_TURN_TO_DUST = new NpcStringId(1900170);
		BY_MY_WRATH_I_COMMAND_YOU_CREATURE_LOSE_YOUR_MIND = new NpcStringId(1900171);
		SHOW_RESPECT_TO_THE_HEROES_WHO_DEFEATED_THE_EVIL_DRAGON_AND_PROTECTED_THIS_ADEN_WORLD =
				new NpcStringId(1900172);
		SHOUT_TO_CELEBRATE_THE_VICTORY_OF_THE_HEROES = new NpcStringId(1900173);
		PRAISE_THE_ACHIEVEMENT_OF_THE_HEROES_AND_RECEIVE_NEVITS_BLESSING = new NpcStringId(1900174);
		UGH_I_THINK_THIS_IS_IT_FOR_ME = new NpcStringId(1900175);
		VALAKAS_CALLS_FORTH_THE_SERVITORS_MASTER = new NpcStringId(1900176);
		YOU_WILL_SOON_BECOME_THE_SACRIFICE_FOR_US_THOSE_FULL_OF_DECEIT_AND_SIN_WHOM_YOU_DESPISE =
				new NpcStringId(1911111);
		MY_BRETHREN_WHO_ARE_STRONGER_THAN_ME_WILL_PUNISH_YOU_YOU_WILL_SOON_BE_COVERED_IN_YOUR_OWN_BLOOD_AND_CRYING_IN_ANGUISH =
				new NpcStringId(1911112);
		HOW_COULD_I_LOSE_AGAINST_THESE_WORTHLESS_CREATURES = new NpcStringId(1911113);
		FOOLISH_CREATURES_THE_FLAMES_OF_HELL_ARE_DRAWING_CLOSER = new NpcStringId(1911114);
		NO_MATTER_HOW_YOU_STRUGGLE_THIS_PLACE_WILL_SOON_BE_COVERED_WITH_YOUR_BLOOD = new NpcStringId(1911115);
		THOSE_WHO_SET_FOOT_IN_THIS_PLACE_SHALL_NOT_LEAVE_ALIVE = new NpcStringId(1911116);
		WORTHLESS_CREATURES_I_WILL_GRANT_YOU_ETERNAL_SLEEP_IN_FIRE_AND_BRIMSTONE = new NpcStringId(1911117);
		IF_YOU_WISH_TO_SEE_HELL_I_WILL_GRANT_YOU_YOUR_WISH = new NpcStringId(1911118);
		ELAPSED_TIME = new NpcStringId(1911119);
		TIME_REMAINING = new NpcStringId(1911120);
		IT_TELEPORTS_THE_GUARD_MEMBERS_OF_THE_ADEN_IMPERIAL_CASTLE_TO_THE_INSIDE_OF_THE_CASTLE =
				new NpcStringId(10004431);
		IT_TELEPORTS_THE_GUARD_MEMBERS_OF_THE_GLUDIO_CASTLE_TO_THE_INSIDE_OF_THE_CASTLE = new NpcStringId(10004432);
		IT_TELEPORTS_THE_GUARD_MEMBERS_OF_THE_DION_CASTLE_TO_THE_INSIDE_OF_THE_CASTLE = new NpcStringId(10004433);
		IT_TELEPORTS_THE_GUARD_MEMBERS_OF_THE_GIRAN_CASTLE_TO_THE_INSIDE_OF_THE_CASTLE = new NpcStringId(10004434);
		IT_TELEPORTS_THE_GUARD_MEMBERS_OF_THE_ADEN_CASTLE_TO_THE_INSIDE_OF_THE_CASTLE = new NpcStringId(10004436);
		IT_TELEPORTS_THE_GUARD_MEMBERS_OF_THE_INNADRIL_CASTLE_TO_THE_INSIDE_OF_THE_CASTLE = new NpcStringId(10004437);
		IT_TELEPORTS_THE_GUARD_MEMBERS_OF_THE_GODDARD_CASTLE_TO_THE_INSIDE_OF_THE_CASTLE = new NpcStringId(10004438);
		IT_TELEPORTS_THE_GUARD_MEMBERS_OF_THE_RUNE_CASTLE_TO_THE_INSIDE_OF_THE_CASTLE = new NpcStringId(10004439);
		IT_TELEPORTS_THE_GUARD_MEMBERS_OF_THE_SCHUTTGART_CASTLE_TO_THE_INSIDE_OF_THE_CASTLE = new NpcStringId(10004440);
		IT_TELEPORTS_THE_GUARD_MEMBERS_OF_THE_ELMORE_IMPERIAL_CASTLE_TO_THE_INSIDE_OF_THE_CASTLE =
				new NpcStringId(10004441);

		buildFastLookupTable();
	}

	private static void buildFastLookupTable()
	{
		final Field[] fields = NpcStringId.class.getDeclaredFields();
		final ArrayList<NpcStringId> nsIds = new ArrayList<>(fields.length);

		int maxId = 0, mod;
		NpcStringId nsId;
		for (final Field field : fields)
		{
			mod = field.getModifiers();
			if (Modifier.isStatic(mod) && Modifier.isPublic(mod) && Modifier.isFinal(mod) &&
					field.getType().equals(NpcStringId.class))
			{
				try
				{
					nsId = (NpcStringId) field.get(null);
					nsId.setName(field.getName());
					nsId.setParamCount(parseMessageParameters(field.getName()));
					maxId = Math.max(maxId, nsId.getId());
					nsIds.add(nsId);
				}
				catch (final Exception e)
				{
					Log.log(Level.WARNING, "NpcStringId: Failed field access for '" + field.getName() + "'", e);
				}
			}
		}

		VALUES = new NpcStringId[maxId + 1];
		for (int i = nsIds.size(); i-- > 0; )
		{
			nsId = nsIds.get(i);
			VALUES[nsId.getId()] = nsId;
		}
	}

	private static int parseMessageParameters(final String name)
	{
		int paramCount = 0;
		char c1, c2;
		for (int i = 0; i < name.length() - 1; i++)
		{
			c1 = name.charAt(i);
			if (c1 == 'C' || c1 == 'S')
			{
				c2 = name.charAt(i + 1);
				if (Character.isDigit(c2))
				{
					paramCount = Math.max(paramCount, Character.getNumericValue(c2));
					i++;
				}
			}
		}
		return paramCount;
	}

	public static NpcStringId getNpcStringId(final int id)
	{
		final NpcStringId nsi = getNpcStringIdInternal(id);
		return nsi == null ? new NpcStringId(id) : nsi;
	}

	private static NpcStringId getNpcStringIdInternal(final int id)
	{
		if (id < 0 || id >= VALUES.length)
		{
			return null;
		}

		return VALUES[id];
	}

	public static NpcStringId getNpcStringId(final String name)
	{
		try
		{
			return (NpcStringId) NpcStringId.class.getField(name).get(null);
		}
		catch (final Exception e)
		{
			return null;
		}
	}

	private static Builder newBuilder(final String text)
	{
		final ArrayList<Builder> builders = new ArrayList<>();

		int index1 = 0, index2 = 0, paramId, subTextLen;

		final char[] array = text.toCharArray();
		final int arrayLength = array.length;

		char c, c2, c3;
		LOOP:
		for (; index1 < arrayLength; index1++)
		{
			c = array[index1];
			if (c == '$' && index1 < arrayLength - 2)
			{
				c2 = array[index1 + 1];
				if (c2 == 'c' || c2 == 's' || c2 == 'p' || c2 == 'C' || c2 == 'S' || c2 == 'P')
				{
					c3 = array[index1 + 2];
					if (Character.isDigit(c3))
					{
						paramId = Character.getNumericValue(c3);
						subTextLen = index1 - index2;
						if (subTextLen != 0)
						{
							builders.add(new BuilderText(new String(array, index2, subTextLen)));
						}

						builders.add(new BuilderObject(paramId));
						index1 += 2;
						index2 = index1 + 1;
					}
				}
			}
		}

		if (arrayLength >= index1)
		{
			subTextLen = index1 - index2;
			if (subTextLen != 0)
			{
				builders.add(new BuilderText(new String(array, index2, subTextLen)));
			}
		}

		if (builders.size() == 1)
		{
			return builders.get(0);
		}
		return new BuilderContainer(builders.toArray(new Builder[builders.size()]));
	}

	private final int _id;
	private String _name;
	private byte _params;
	private NSLocalisation[] _localisations;
	private ExShowScreenMessage _staticScreenMessage;

	private NpcStringId(final int id)
	{
		_id = id;
		_localisations = EMPTY_NSL_ARRAY;
	}

	public final int getId()
	{
		return _id;
	}

	private void setName(final String name)
	{
		_name = name;
	}

	public final String getName()
	{
		return _name;
	}

	public final int getParamCount()
	{
		return _params;
	}

	/**
	 * You better don`t touch this!
	 *
	 * @param params
	 */
	public final void setParamCount(final int params)
	{
		if (params < 0)
		{
			throw new IllegalArgumentException("Invalid negative param count: " + params);
		}

		if (params > 10)
		{
			throw new IllegalArgumentException("Maximum param count exceeded: " + params);
		}

		if (params != 0)
		{
			_staticScreenMessage = null;
		}

		_params = (byte) params;
	}

	public final NSLocalisation getLocalisation(final String lang)
	{
		NSLocalisation nsl;
		for (int i = _localisations.length; i-- > 0; )
		{
			nsl = _localisations[i];
			if (nsl.getLanguage().hashCode() == lang.hashCode())
			{
				return nsl;
			}
		}
		return null;
	}

	public final void attachLocalizedText(final String lang, final String text)
	{
		final int length = _localisations.length;
		final NSLocalisation[] localisations = Arrays.copyOf(_localisations, length + 1);
		localisations[length] = new NSLocalisation(lang, text);
		_localisations = localisations;
	}

	public final void removeAllLocalisations()
	{
		_localisations = EMPTY_NSL_ARRAY;
	}

	public final ExShowScreenMessage getStaticScreenMessage()
	{
		return _staticScreenMessage;
	}

	public final void setStaticSystemMessage(final ExShowScreenMessage ns)
	{
		_staticScreenMessage = ns;
	}

	@Override
	public final String toString()
	{
		return "NS[" + getId() + ":" + getName() + "]";
	}

	public static final class NSLocalisation
	{
		private final String _lang;
		private final Builder _builder;

		public NSLocalisation(final String lang, final String text)
		{
			_lang = lang;
			_builder = newBuilder(text);
		}

		public final String getLanguage()
		{
			return _lang;
		}

		public final String getLocalisation(final Object... params)
		{
			return _builder.toString(params);
		}
	}

	/**
	 * @author Forsaiken
	 */
	private interface Builder
	{
		String toString(final Object param);

		String toString(final Object... params);

		int getIndex();
	}

	/**
	 * @author Forsaiken
	 */
	private static final class BuilderContainer implements Builder
	{
		private final Builder[] _builders;

		public BuilderContainer(final Builder[] builders)
		{
			_builders = builders;
		}

		@Override
		public final String toString(final Object param)
		{
			return toString(new Object[]{param});
		}

		@Override
		public final String toString(final Object... params)
		{
			final int buildersLength = _builders.length;
			final int paramsLength = params.length;
			final String[] builds = new String[buildersLength];

			Builder builder;
			String build;
			int i, paramIndex, buildTextLen = 0;
			if (paramsLength != 0)
			{
				for (i = buildersLength; i-- > 0; )
				{
					builder = _builders[i];
					paramIndex = builder.getIndex();
					build = paramIndex != -1 && paramIndex < paramsLength ? builder.toString(params[paramIndex]) :
							builder.toString();
					buildTextLen += build.length();
					builds[i] = build;
				}
			}
			else
			{
				for (i = buildersLength; i-- > 0; )
				{
					build = _builders[i].toString();
					buildTextLen += build.length();
					builds[i] = build;
				}
			}

			final FastStringBuilder fsb = new FastStringBuilder(buildTextLen);
			for (i = 0; i < buildersLength; i++)
			{
				fsb.append(builds[i]);
			}
			return fsb.toString();
		}

		@Override
		public final int getIndex()
		{
			return -1;
		}
	}

	/**
	 * @author Forsaiken
	 */
	private static final class BuilderText implements Builder
	{
		private final String _text;

		public BuilderText(final String text)
		{
			_text = text;
		}

		@Override
		public final String toString(final Object param)
		{
			return toString();
		}

		@Override
		public final String toString(final Object... params)
		{
			return toString();
		}

		@Override
		public final int getIndex()
		{
			return -1;
		}

		@Override
		public final String toString()
		{
			return _text;
		}
	}

	/**
	 * @author Forsaiken
	 */
	private static final class BuilderObject implements Builder
	{
		private final int _index;

		public BuilderObject(final int id)
		{
			if (id < 1 || id > 9)
			{
				throw new RuntimeException("Illegal id " + id);
			}

			_index = id - 1;
		}

		@Override
		public final String toString(final Object param)
		{
			return param == null ? "null" : param.toString();
		}

		@Override
		public final String toString(final Object... params)
		{
			if (params == null || params.length == 0)
			{
				return "null";
			}

			return params[0].toString();
		}

		@Override
		public final int getIndex()
		{
			return _index;
		}

		@Override
		public final String toString()
		{
			return "[PARAM-" + (_index + 1) + "]";
		}
	}

	/**
	 * @author Forsaiken
	 */
	private static final class FastStringBuilder
	{
		private final char[] _array;
		private int _len;

		public FastStringBuilder(final int capacity)
		{
			_array = new char[capacity];
		}

		public final void append(final String text)
		{
			text.getChars(0, text.length(), _array, _len);
			_len += text.length();
		}

		@Override
		public final String toString()
		{
			return new String(_array);
		}
	}
}
