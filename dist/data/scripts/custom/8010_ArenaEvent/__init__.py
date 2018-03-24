# Author Alisson Oliveira
import sys
from com.l2jfrozen.gameserver.model.quest        import State
from com.l2jfrozen.gameserver.model.quest        import QuestState
from com.l2jfrozen.gameserver.model.quest.jython import QuestJython as JQuest
from com.l2jfrozen.gameserver.model.entity.event import EvtArenaManager as Event
from java.util import Map

qn = "8010_ArenaEvent"
NPCID = 93001
initialhtml = "1.htm"

class Quest (JQuest) :
    def __init__(self, id, name, descr): JQuest.__init__(self, id, name, descr)
    
    def onEvent(self,event,st):
        player = st.getPlayer()
        html = initialhtml
        if event == "1":
           party = player.getParty()
           if not party: return "no-party.htm"
           if not party.isLeader(player): return "no-leader.htm"
           if party.getMemberCount() != 2: return "only-two.htm"
           if not player.isNoble(): return "only-noble.htm"
           if player.isInOlympiadMode(): return "nooly.htm"
           if player._inEventTvT: return "noevt.htm"
           if player._inEventDM: return "noevt.htm"
           if player._inEventCTF: return "noevt.htm"
           assist = party.getPartyMembers()[1] # get the last player in party
           if not assist.isNoble(): return "only-noble.htm"
           if assist.isInOlympiadMode(): return "nooly.htm"
           if assist._inEventTvT: return "noevt.htm"
           if assist._inEventDM: return "noevt.htm"
           if assist._inEventCTF: return "noevt.htm"
           if Event.getInstance().register(player, assist):
              return "registered.htm"
        elif event == "2":
           if Event.getInstance().remove(player):
              return "unregistered.htm"
        elif event == "3":
           fights = Event.getInstance().getFights()
           html = "<html><body>Event Arena Manager:<br> <br> Fights: <br>"
           if len(fights) == 0:
              html += "No fights going on right now<br></body></html>"
           else:
              for id in fights.keySet():
                 html += "<a action=\"bypass -h Quest "+ qn +" 4_"+str(id)+"\">"+fights.get(id)+"</a><br><br>"
              html += "</body></html>"
           return html
        elif event.startswith("4_"):
           if not Event.getInstance().isRegistered(player):
              arenaId = int(event.replace("4_",""))
              Event.getInstance().addSpectator(player, arenaId)           
        return "default.htm"
    
    def onTalk (self, npc, player):
        return initialhtml  
          
QUEST = Quest(-1, qn, "custom")
CREATED     = State('Start', QUEST)
STARTED     = State('Started', QUEST)

QUEST.setInitialState(CREATED)

QUEST.addStartNpc(NPCID)
QUEST.addTalkId(NPCID)
