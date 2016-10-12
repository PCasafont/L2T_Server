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

package l2server.gameserver.instancemanager;

import l2server.L2DatabaseFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Kilian, Pere
 */
public class SurveyManager
{
	private static SurveyManager _instance;

	private final String GET_CURRENT_SURVEY =
			"SELECT survey_id,question,description FROM survey WHERE survey_id = (SELECT MAX(survey_id) FROM survey where active = 1)";
	private final String GET_CURRENT_SURVEY_POSSIBLE_ANSWERS =
			"SELECT answer_id,answer FROM survey_possible_answer WHERE survey_id = ?";
	private final String GET_CURRENT_SURVEY_ANSWERS = "SELECT charId FROM survey_answer WHERE survey_id = ?";
	private final String STORE_ANSWER = "INSERT INTO survey_answer (charId,survey_id,answer_id) VALUES (?,?)";

	private int _id = 0;
	private String _question;
	private String _description;
	private Map<Integer, String> _possibleAnswers;
	private List<Integer> _answers;

	private SurveyManager()
	{
		load();
	}

	private void load()
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(GET_CURRENT_SURVEY);
			ResultSet rset = statement.executeQuery();

			if (rset.next())
			{
				_id = rset.getInt("survey_id");
				_question = rset.getString("question");
				_description = rset.getString("description");

				PreparedStatement statement2 = con.prepareStatement(GET_CURRENT_SURVEY_POSSIBLE_ANSWERS);
				statement2.setInt(1, _id);
				ResultSet rset2 = statement2.executeQuery();
				Map<Integer, String> possibleAnswers = new HashMap<>();
				while (rset2.next())
				{
					possibleAnswers.put(rset.getInt("answer_id"), rset.getString("answer"));
				}

				_possibleAnswers = possibleAnswers;

				statement2 = con.prepareStatement(GET_CURRENT_SURVEY_ANSWERS);
				statement2.setInt(1, _id);
				rset2 = statement2.executeQuery();
				List<Integer> answers = new ArrayList<>();
				while (rset2.next())
				{
					answers.add(rset.getInt("charId"));
				}

				_answers = answers;
			}

			rset.close();
			statement.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	public boolean isActive()
	{
		return _id > 0;
	}

	public String getQuestion()
	{
		return _question;
	}

	public String getDescription()
	{
		return _description;
	}

	public Integer[] getPossibleAnswerIds()
	{
		return (Integer[]) _possibleAnswers.keySet().toArray();
	}

	public String getPossibleAnswer(int id)
	{
		return _possibleAnswers.get(id);
	}

	public boolean playerAnswered(int playerObjId)
	{
		return _answers.contains(playerObjId);
	}

	public boolean storeAnswer(int playerObjId, int answerIndex)
	{
		if (_answers.contains(playerObjId))
		{
			return false;
		}
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(STORE_ANSWER);
			statement.setInt(1, playerObjId);
			statement.setInt(2, answerIndex);
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
		_answers.add(playerObjId);
		return true;
	}

	public static SurveyManager getInstance()
	{
		if (_instance == null)
		{
			_instance = new SurveyManager();
		}
		return _instance;
	}
}
