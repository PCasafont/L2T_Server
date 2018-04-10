# Made by Luna and Kilian
# This script is part of the TnS Datapack
# http://www.l2server.com

from l2server.gameserver.model.quest import State
from l2server.gameserver.model.quest.jython import QuestJython as JQuest

qn = "692_HowToOpposeEvil"

################# NPC ################################################################
# General (Start NPC)
Dilios = 32549
# Special Product Broker
Kirklan = 32550

################# MONSTERS ###########################################################
SeedOfInfMobs = range(22510, 22515)
SeedOfDestrMobs = range(22537, 22552) + [22593, 22596, 22597]

################# ITEMS ##############################################################
# Certificate of quest Good Day To Fly is necessary
Certificate = 13857
# Nucleus of an Incomplete Soul (from SoI)
NuclIncSoul = 13863
# Fleet Steed Troup's Totem (from SoD)
FleetSteedTotem = 13865
# Chance to obtain items from quest relevant mobs
Chance = 60

################# REWARDS ############################################################
# Nucleus of a Freed Soul	= 13796
# Fleet Steed Troup's Charm	= 13841
NoaFS = [5, 13796, 1]
FSTC = [5, 13841, 1]

class Quest(JQuest):
    def __init__(self, id, name, descr):
        JQuest.__init__(self, id, name, descr)
        self.questItemIds = [NuclIncSoul, FleetSteedTotem]

    def onAdvEvent(self, event, npc, player):
        htmltext = event
        st = player.getQuestState(qn)
        if not st: return
        # Quest start html
        if event == "htmlnumber.htm":
            st.set("cond", "1")
            st.setState(State.STARTED)
            st.playSound("ItemSound.quest_accept")
        # Reward player by exchanging items
        elif event == "PutHereSomeBypassCommand":
            # Determine exchange rate for both items
            costNucl, itemNucl, amountNucl = NoaFS
            costCharm, itemCharm, amountCharm = FSTC
            # Exchange all available items of both types
            HasEnough = False
            if st.getQuestItemsCount(NuclIncSoul) >= cost:
                st.takeItems(NuclIncSoul, costNucl)
                st.rewardItems(itemNucl, amountNucl)
                HasEnough = True
            if st.getQuestItemsCount(FleetSteedTotem) >= cost:
                st.takeItems(FleetSteedTotem, costCharm)
                st.rewardItems(itemCharm, amountCharm)
                HasEnough = True
            # Congratulation html? Send player for more?
            if HasEnough:
                htmltext = "htmlnumber.htm"
            # If not enough items for exchanging, send player to collect more
            else:
                htmltext = "htmlnumber.htm"
        # Display chosen html window
        return htmltext

    def onTalk(self, npc, player):
        htmltext = Quest.getNoQuestMsg(player)
        st = player.getQuestState(qn)
        if not st: return htmltext

        id = st.getState()
        if id == State.CREATED:
            if player.getLevel() >= 75:
                htmltext = "32549-01.htm"
            else:
                htmltext = "32549-00.htm"
        else:
            if npcId == Dilios:
                st2 = st.getPlayer().getQuestState("10273_GoodDayToFly")
                # Player must have quest done and certificate in inventory
                if cond == 1 and st.getQuestItemsCount(Certificate) >= 1 and st2 and st2.getState() == State.COMPLETED:
                    htmltext = "32549-04.htm"
                    st.set("cond", "2")
                elif cond == 2:
                    htmltext = "32549-05.htm"
            elif npcId == Kirklan:
                if cond == 2:
                    htmltext = "32550-01.htm"
                elif cond == 3:
                    htmltext = "32550-04.htm"
        return htmltext

    # On mob kill, give the player quest items
    def onKill(self, npc, player, isPet):
        partyMember = self.getRandomPartyMemberState(player, State.STARTED)
        if not partyMember: return
        st = partyMember.getQuestState(qn)
        if st:
            if st.getRandom(100) < chance:
                if npc.getNpcId() in SeedOfDestrMobs:
                    st.giveItems(FleetSteedTotem, 1)
                elif npc.getNpcId() in SeedOfInfMobs:
                    st.giveItems(NuclIncSoul, 1)
                st.playSound("ItemSound.quest_itemget")
        return

QUEST = Quest(692, qn, "How To Oppose Evil")

QUEST.addStartNpc(Dilios)
QUEST.addTalkId(Dilios)
QUEST.addTalkId(Kirklan)

for i in SeedOfInfMobs:
    QUEST.addKillId(i)
for i in SeedOfDestrMobs:
    QUEST.addKillId(i)
