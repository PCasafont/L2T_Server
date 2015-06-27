# Made by Kriogen [L2-Emu Developer]
import sys
from l2server.gameserver.model.quest import State
from l2server.gameserver.model.quest import QuestState
from l2server.gameserver.model.quest.jython import QuestJython as JQuest

qn = "691_MatrasCuriosity"

#NPC
MATRAS = 32245

#MOBS
Lab1 = 22365
Lab2 = 22366
Lab3 = 22367
Lab4 = 22368

#ITEMS
RED_STONE = 10372
DYNASTIC_ESSENCE_II = 10413

class Quest (JQuest):

 def __init__(self,id,name,descr): 
     JQuest.__init__(self,id,name,descr)
     self.questItemIds = []

 def onEvent (self,event,st):
   htmltext = event
   if event == "32245-03.htm" :
     st.set("cond","1")
     st.setState(State.STARTED)
     st.playSound("ItemSound.quest_accept")
   if event == "32245-05.htm" :
     st.playSound("ItemSound.quest_finish")
     st.takeItems(RED_STONE,744)
     st.giveItems(DYNASTIC_ESSENCE_II,1)
     st.exitQuest(1)
   return htmltext

 def onTalk (self,npc,player):
   st = player.getQuestState(qn)
   htmltext = "<html><body>You are either not on a quest that involves this NPC, or you don't meet this NPC's minimum quest requirements.</body></html>"
   if not st : return htmltext
   npcId = npc.getNpcId()
   id = st.getState()
   cond = st.getInt("cond")
   if npcId == MATRAS :
     if id == State.CREATED :
       if player.getLevel() >= 76 :
         htmltext = "32245-01.htm"
       else :
         st.exitQuest(1)
         return
     elif cond == 1 :
       htmltext = "32245-03.htm"
     elif cond == 2 :
       htmltext = "32245-04.htm"
   return htmltext

 def onKill (self,npc,player,isPet):
   partyMember = self.getRandomPartyMember(player,"1")
   if not partyMember: return
   st = partyMember.getQuestState(qn)
   if not st : return
   if st.getState() == State.STARTED :
     npcId = npc.getNpcId()
     if npcId in [Lab1,Lab2,Lab3,Lab4] :
       if st.getQuestItemsCount(RED_STONE) < 744 :
         st.giveItems(RED_STONE,1)
         if st.getQuestItemsCount(RED_STONE) == 744 :
           st.playSound("ItemSound.quest_middle")
           st.set("cond","2")
         else:
           st.playSound("ItemSound.quest_itemget")
   return

QUEST       = Quest(691,qn,"Matras Curiosity!")

QUEST.addStartNpc(MATRAS)

QUEST.addTalkId(MATRAS)

QUEST.addKillId(Lab1)
QUEST.addKillId(Lab2)
QUEST.addKillId(Lab3)
QUEST.addKillId(Lab4)